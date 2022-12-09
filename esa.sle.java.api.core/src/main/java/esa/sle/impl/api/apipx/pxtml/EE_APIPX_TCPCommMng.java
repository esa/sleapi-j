package esa.sle.impl.api.apipx.pxtml;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_TMLErrors;
import esa.sle.impl.ifs.gen.EE_LogMsg;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_APIPX_TCPCommMng
{

    private static final Logger LOG = Logger.getLogger(EE_APIPX_TCPCommMng.class.getName());

    /**
     * Sending queue capacity
     */
    private static int sendingCapacity = 1;

    private final EE_APIPX_Channel channel;

    private final BlockingQueue<EE_APIPX_TMLMessage> sendingQueue;

    private final SendingThread sendingThr;

    private final ReceivingThread receivingThr;


    public EE_APIPX_TCPCommMng(EE_APIPX_Channel channel)
    {
        this.channel = channel;
        this.sendingThr = new SendingThread();
        this.receivingThr = new ReceivingThread();
        this.sendingQueue = new ArrayBlockingQueue<EE_APIPX_TMLMessage>(sendingCapacity, true);
    }

    public void sendMsg(EE_APIPX_TMLMessage msg)
    {
        if (this.channel.getTraceLevel().getCode() >= SLE_TraceLevel.sleTL_high.getCode())
        {
            String txt = "Adding the TML Message to the sending queue";
            this.channel.trace(EE_LogMsg.TMLTR_SENDPDU.getCode(), SLE_TraceLevel.sleTL_full, txt);
        }
        sendTMLMessage(msg);
    }

    public void startThreads()
    {
        this.sendingThr.start();
        this.receivingThr.start();
        // this.processingThr.start();
    }

    public void stopThreads()
    {
        if (this.sendingThr.isRunning == true)
        {
            if (LOG.isLoggable(Level.FINER))
            {
                LOG.log(Level.FINER, "Sending Thread terminating on TCP Comm Mng " + this);
            }
            try
            {
            	this.sendingThr.terminate();
            }
            catch(Exception e)
            {
            	LOG.log(Level.SEVERE, "EE_APIPX_TCPCommMng exception terminating send thread", e);
            }
        }

        if (this.receivingThr.isRunning == true)
        {
            if (LOG.isLoggable(Level.FINER))
            {
                LOG.log(Level.FINER, "Receiving Thread terminating on TCP Comm Mng " + this);
            }
            try
            {
            	this.receivingThr.terminate();
            }
            catch(Exception e) 
            {
            	LOG.log(Level.SEVERE, "EE_APIPX_TCPCommMng exception terminating receive thread", e);
            }
        }
    }

    /**
     * Takes TMLMessages from the sending queue and transmits them.
     */
    public void sendTMLMessage()
    {
        EE_APIPX_TMLMessage msgToSend = null;

        // take the TMLMessage from the queue
        try {
			msgToSend = this.sendingQueue.poll(100, TimeUnit.MILLISECONDS); // without timeout performance is damaged - SLEAPIJ-14
		} catch (InterruptedException e) {
			LOG.log(Level.SEVERE, "Exception polling queue", e);
		}

        sendTMLMessage(msgToSend);
    }

    private void sendTMLMessage(EE_APIPX_TMLMessage msgToSend)
    {
        if (msgToSend != null)
        {
            if (this.channel.traceLevel.getCode() >= SLE_TraceLevel.sleTL_high.getCode())
            {
                String msg = "Sending TML Message of " + msgToSend.getLength() + "bytes on socket"
                             + this.channel.getConnectedSock().toString();
                this.channel.trace(EE_LogMsg.TMLTR_ONPDUTRANSMITTED.getCode(), SLE_TraceLevel.sleTL_high, msg);
            }

            // send the TMLMessage to the socket
            try
            {
                if (LOG.isLoggable(Level.FINE))
                {
                    LOG.log(Level.FINE, "Writing TML message " + msgToSend.getClass().getSimpleName() + " to channel " + this.channel);
                }
                msgToSend.writeTo(this.channel.connectedSock.getOutputStream());
            }
            catch (SleApiException | IOException e)
            {
                SendingThread st = this.sendingThr;
                if (st != null && st.isRunning)
                {
                    LOG.log(Level.FINE, "SleApiException | IOException e ", e);
                    String msg = "Failure while sending TML Message";
                    // this.channel.logError(EE_LogMsg.TMLSENDFAIL.getCode(),
                    // true, msg);
                    this.channel.tcpError(EE_LogMsg.TMLSENDFAIL.getCode(), true, msg);
                }
            }
        }
    }

    /**
     * Reads bytes from the socket, rebuilds the TMLmessage and adds it to the
     * receiving queue
     */
    public boolean readTMLMessage()
    {
        // read the input stream from the socket and re-build the TMLMessage
        if (this.channel.getConnectedSock() != null)
        {
            EE_APIPX_TMLMessage tmlMsg = null;
            EE_Reference<EE_APIPX_TMLErrors> error = new EE_Reference<EE_APIPX_TMLErrors>();
            error.setReference(EE_APIPX_TMLErrors.eeAPIPXtml_empty);
            try
            {
                if (LOG.isLoggable(Level.FINE))
                {
                    LOG.log(Level.FINE, " Waiting for TML message on channel " + this.channel);
                }
                tmlMsg = this.channel.getTmlMsgFactory().decodeFrom(this.channel.getConnectedSock().getInputStream(), error);                               
            }
            catch (IOException e)
            {
                ReceivingThread st = this.receivingThr;
                if (!this.channel.isAboutToClose() && st != null && st.isRunning)
                {
                	// do not print the stack trace, that happens for lost TCP connections and is not an application error
                	LOG.log(Level.SEVERE, "Failure reading from the socket input stream: " + e); 
                    String msg = "Failure reading from the socket input stream";
                    // this.channel.logError(EE_LogMsg.TMLTR_IOEVENT.getCode(),
                    // true, msg);
                    this.channel.tcpError(EE_LogMsg.TMLTR_IOEVENT.getCode(), true, msg);
                }
                return false;
            }
            catch (SleApiException e)
            {
                ReceivingThread st = this.receivingThr;
                if (!this.channel.isAboutToClose() && st != null && st.isRunning)
                {
                    LOG.log(Level.SEVERE, "Failure reading from the socket input stream", e);
                    String msg = "Failure while decoding the TML Message from the socket output stream";
                    this.channel.logError(EE_LogMsg.TMLTR_PDUREAD.getCode(), true, msg);
                    return false;
                }
            }

            if (tmlMsg == null)
            {
                if (error.getReference() == EE_APIPX_TMLErrors.eeAPIPXtml_badTMLMsg)
                {
                    // invalid TML Message
                    this.channel.manageBadFormattedMsg();
                    return false;
                }
                else
                {
                	if(!this.channel.isAboutToClose()) 
                	{
	                	LOG.log(Level.SEVERE, "Failure receiving TML message: " + error.getReference());
	                	this.channel.tcpAbortInd(); // indicate that there is a disconnect SLEAPIJ-TBD
	                    // throw new RuntimeException("Unexpect error message: " + error.getReference());
	                	return false;
                	}
                	else
                	{
                		LOG.log(Level.FINEST, "Expected failure when receiving TML message when channel is closing: error " + error.getReference());
	                    // throw new RuntimeException("Unexpect error message: " + error.getReference());
	                	return false;	
                	}
                }
            }

            if (this.channel.traceLevel.getCode() >= SLE_TraceLevel.sleTL_high.getCode())
            {
                String oss = "Read a TML message of " + tmlMsg.getLength() + " bytes from the socket "
                             + this.channel.getConnectedSock().toString();
                this.channel.trace(EE_LogMsg.TMLTR_PDUREAD.getCode(), SLE_TraceLevel.sleTL_high, oss);
            }

            if (tmlMsg instanceof EE_APIPX_UrgentByteMessage)
            {
                // discard normal data (out coming)
                this.sendingQueue.clear();

                LOG.log(Level.FINE, "Received EE_APIPX_UrgentByteMessage: forwarding it to channel " + this.channel);
                tmlMsg.processOn(this.channel);

                return false;
            }
            else
            {
            	if(LOG.isLoggable(Level.FINE))
            	{
            		LOG.log(Level.FINE, "Received TML Message " + tmlMsg.getClass().getSimpleName() + ": forwarding it to channel " + this.channel);
            	}
                tmlMsg.processOn(this.channel);
            }
        }

        return true;
    }


    private class SendingThread extends Thread
    {
        private boolean isRunning;


        public SendingThread()
        {
        	super("SLE EE_APIPX_TCPCommMng.SendingThread");
            this.isRunning = false;
        }

        @Override
        public void run()
        {
            this.isRunning = true;
            while (this.isRunning)
            {
                EE_APIPX_TCPCommMng.this.sendTMLMessage();
            }
        }

        public void terminate()
        {
            this.isRunning = false;
        }
    }

    private class ReceivingThread extends Thread
    {
        private volatile boolean isRunning;


        public ReceivingThread()
        {
        	super("SLE EE_APIPX_TCPCommMng.ReceivingThread");
            this.isRunning = false;
        }

        @Override
        public void run()
        {
            this.isRunning = true;
            while (this.isRunning)
            {

                boolean readResult = readTMLMessage();
                if (readResult == false)
                {
                    this.isRunning = false;
                }
            }
        }

        public void terminate()
        {
            this.isRunning = false;
        }
    }
}
