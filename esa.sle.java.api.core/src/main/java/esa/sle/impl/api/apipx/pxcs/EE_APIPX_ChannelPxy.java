/**
 * @(#) EE_APIPX_ChannelPxy.java
 */

package esa.sle.impl.api.apipx.pxcs;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.icc.ISLE_TraceControl;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_Database;
import esa.sle.impl.api.apipx.pxspl.IEE_ChannelInform;
import esa.sle.impl.api.apipx.pxspl.IEE_ChannelInitiate;
import esa.sle.impl.api.apipx.pxtml.EE_APIPX_ISP1ProtocolAbortDiagnostics;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_ISP1ProtocolAbortOriginator;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_TCPErrors;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_TMLErrors;

/**
 * The class implements the interface IEE_ChannelInitiate and transfers encoded
 * PDU's from the client residing in the SLE application process to the channel
 * object in the communication server process, for transfer to the peer
 * application over the network. Encoded PDU's received from the communication
 * server process are forwarded to the client by using the interface
 * ISLE_ChannelInform. The class is only used for responding associations that
 * need to transfer PDU's to the communication server process and to receive
 * encoded PDU's from the communication server. The ChannelPxy object creates a
 * thread in order to send a received PDU to the link object. When the
 * ChannelPxy receives a PDU from the association (sendSLE_PDU), it returns
 * immediately. The additional thread has to manage the sending of the PDU on
 * the IPC link through the link object. To be able to synchronize the thread
 * with the link object, a condition variable is needed.
 */
public class EE_APIPX_ChannelPxy extends IEE_APIPX_LoggerAdapter implements IEE_ChannelInitiate, ISLE_TraceControl
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_ChannelPxy.class.getName());

    private IEE_ChannelInform ieeChannelInform;

    private EE_APIPX_Link eeAPIPXLink;

    private final WritingThread writingTh;

    private final ReentrantLock mutex;

    /**
     * Set the Link associated with the ChannelPxy object.
     */
    public EE_APIPX_ChannelPxy(String instanceKey, ISLE_Reporter pReporter, EE_APIPX_Link pLink)
    {
    	super(instanceKey);
        this.threadRunning = true;
        this.eeAPIPXLink = pLink;
        this.ieeChannelInform = null;
        // create the thread for writing on the link
        this.writingTh = new WritingThread();
        this.writingTh.start();

        // initialise the logger adapter
        setIsDefaultLogger(false);
        setLink(pLink);
        pLink.setLoggerAdapter(this);
        setReporter(pReporter);
        this.mutex = new ReentrantLock();
    }

    /**
     * The link object calls this function when some data are received on the
     * IPC link and must be performed by the ChannelPxy. The ChannelPxy then
     * transmit the data to the association through the IEE_ChannelInform
     * interface.
     */
    @Override
    public void takeData(byte[] data, int dataType, EE_APIPX_Link pLink, boolean lastPdu)
    {
        if (this.ieeChannelInform == null)
        {
            return;
        }

        if (dataType == PXCS_MessId.mid_SlePdu.getCode() || dataType == PXCS_MessId.mid_BindPdu.getCode())
        {
            this.ieeChannelInform.rcvSLEPDU(data);
        }
        else if (dataType == PXCS_MessId.mid_Connect.getCode())
        {
            this.ieeChannelInform.rcvConnect();
        }
        else if (dataType == PXCS_MessId.mid_PeerAbort.getCode())
        {
            PXCS_AssocChannel_Mess mess = new PXCS_AssocChannel_Mess(data);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest(mess.toString());
            }
            this.ieeChannelInform.rcvPeerAbort(mess.getDiagnostic(), mess.isOriginatorIsLocal());
            this.ieeChannelInform = null;
        }
        else if (dataType == PXCS_MessId.mid_ProtocolAbort.getCode())
        {
            PXCS_AssocChannel_Mess mess = new PXCS_AssocChannel_Mess(data);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest(mess.toString());
            }
            EE_APIPX_ISP1ProtocolAbortDiagnostics paDiag = mess.getPaOriginator();
            this.ieeChannelInform.rcvProtocolAbort(paDiag);
            this.ieeChannelInform = null;
        }
        else if (dataType == PXCS_MessId.mid_SuspendReceive.getCode())
        {
            this.ieeChannelInform.suspendXmit();
        }
        else if (dataType == PXCS_MessId.mid_ResumeReceive.getCode())
        {
            this.ieeChannelInform.resumeXmit();
        }
        else
        {
            super.takeData(data, dataType, pLink, lastPdu);
        }
    }

    /**
     * This function is called when the IPC connection is closed by the peer
     * side or lost.
     */
    @Override
    public void ipcClosed(EE_APIPX_Link pLink)
    {
        this.mutex.lock();
        setLink(null);
        if (!this.linkClosed)
        {
            if (this.ieeChannelInform != null)
            {
                EE_APIPX_ISP1ProtocolAbortDiagnostics pdiag = new EE_APIPX_ISP1ProtocolAbortDiagnostics(EE_APIPX_ISP1ProtocolAbortOriginator.localTML,
                                                                                                        EE_APIPX_TMLErrors.eeAPIPXtml_unexpectedClose
                                                                                                                .getCode(),
                                                                                                        EE_APIPX_TCPErrors.eeAPIPXtcp_other
                                                                                                                .getCode());
                this.ieeChannelInform.rcvProtocolAbort(pdiag);
                this.ieeChannelInform = null;
            }
            this.linkClosed = true;
        }

        if (this.eeAPIPXLink != null)
        {
            this.eeAPIPXLink = null;
        }

        this.mutex.unlock();
        terminateThread();
    }

    /**
     * Sends an encoded PDU.
     */
    @Override
    public HRESULT sendSLEPDU(byte[] data, boolean last)
    {
    	HRESULT res = HRESULT.SLE_S_TRANSMITTED;
        PXCS_Header_Mess header = new PXCS_Header_Mess(last, PXCS_MessId.mid_SlePdu.getCode(), data.length);
        this.mutex.lock();

        byte[] headerArray = header.toByteArray();
        byte[] newArray = new byte[headerArray.length + data.length];
        System.arraycopy(headerArray, 0, newArray, 0, headerArray.length);
        System.arraycopy(data, 0, newArray, headerArray.length, data.length);
        res = sendMessageNoWait(newArray); // SLEAPIJ-26

        // sendMessageNoWait(header.toByteArray(), data);

        // sendMessageNoWait(header.toByteArray());
        // sendMessageNoWait(data);

        this.mutex.unlock();

        if (last)
        {
            if (this.ieeChannelInform != null)
            {
                this.ieeChannelInform = null;
            }
        }
        
        return res;
    }

    /**
     * Sends a DISCONNECT request.
     */
    @Override
    public void sendDisconnect()
    {
        PXCS_AssocChannel_Mess mess = new PXCS_AssocChannel_Mess();
        byte[] messByteArray = mess.toByteArray();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(mess.toString());
        }

        PXCS_Header_Mess header = new PXCS_Header_Mess(false,
                                                       PXCS_MessId.mid_Disconnect.getCode(),
                                                       messByteArray.length);
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(header.toString());
        }

        byte[] data = new byte[PXCS_Header_Mess.hMsgLength + messByteArray.length];
        System.arraycopy(header.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);

        this.mutex.lock();
        sendMessageNoWait(data);
        this.mutex.unlock();

        if (this.ieeChannelInform != null)
        {
            this.ieeChannelInform = null;
        }
    }

    /**
     * Sends a CONNECT request.
     */
    @Override
    public void sendConnect(String rspPortId)
    {
        PXCS_AssocChannel_Mess mess = new PXCS_AssocChannel_Mess();
        byte[] messByteArray = mess.toByteArray();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(mess.toString());
        }

        PXCS_Header_Mess header = new PXCS_Header_Mess(false, PXCS_MessId.mid_Connect.getCode(), messByteArray.length);
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(header.toString());
        }

        byte[] data = new byte[PXCS_Header_Mess.hMsgLength + messByteArray.length];
        System.arraycopy(header.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);

        this.mutex.lock();
        sendMessageNoWait(data);
        this.mutex.unlock();

        if (this.ieeChannelInform != null)
        {
            this.ieeChannelInform = null;
        }
    }

    /**
     * Sends a RESET request.
     */
    @Override
    public void sendReset()
    {
        // FIXME: remove        
        new Exception().printStackTrace();

        PXCS_AssocChannel_Mess mess = new PXCS_AssocChannel_Mess();
        byte[] messByteArray = mess.toByteArray();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(mess.toString());
        }

        PXCS_Header_Mess header = new PXCS_Header_Mess(false, PXCS_MessId.mid_Reset.getCode(), messByteArray.length);
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(header.toString());
        }

        byte[] data = new byte[PXCS_Header_Mess.hMsgLength + messByteArray.length];
        System.arraycopy(header.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);

        this.mutex.lock();
        sendMessageNoWait(data);
        this.mutex.unlock();

        if (this.ieeChannelInform != null)
        {
            this.ieeChannelInform = null;
        }
    }

    /**
     * Sends a PEER_ABORT request.
     */
    @Override
    public void sendPeerAbort(int diagnostic)
    {
        PXCS_AssocChannel_Mess mess = new PXCS_AssocChannel_Mess();
        mess.setDiagnostic(diagnostic);
        byte[] mesByteArray = mess.toByteArray();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(mess.toString());
        }

        PXCS_Header_Mess header = new PXCS_Header_Mess(false, PXCS_MessId.mid_PeerAbort.getCode(), mesByteArray.length);
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(header.toString());
        }

        byte[] data = new byte[PXCS_Header_Mess.hMsgLength + mesByteArray.length];
        System.arraycopy(header.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);
        System.arraycopy(mesByteArray, 0, data, PXCS_Header_Mess.hMsgLength, mesByteArray.length);

        this.mutex.lock();
        sendMessageNoWait(data);
        this.mutex.unlock();

        if (this.ieeChannelInform != null)
        {
            this.ieeChannelInform = null;
        }
    }

    /**
     * Request a suspention of the receiving.
     */
    @Override
    public void suspendReceive()
    {
        PXCS_AssocChannel_Mess mess = new PXCS_AssocChannel_Mess();
        byte[] mesByteArray = mess.toByteArray();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(mess.toString());
        }

        PXCS_Header_Mess header = new PXCS_Header_Mess(false,
                                                       PXCS_MessId.mid_SuspendReceive.getCode(),
                                                       mesByteArray.length);
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(header.toString());
        }

        byte[] data = new byte[PXCS_Header_Mess.hMsgLength + mesByteArray.length];
        System.arraycopy(header.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);

        this.mutex.lock();
        sendMessageNoWait(data);
        this.mutex.unlock();

        if (this.ieeChannelInform != null)
        {
            this.ieeChannelInform = null;
        }
    }

    /**
     * Request a resumption of the receiving.
     */
    @Override
    public void resumeReceive()
    {
        PXCS_AssocChannel_Mess mess = new PXCS_AssocChannel_Mess();
        byte[] mesByteArray = mess.toByteArray();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(mess.toString());
        }

        PXCS_Header_Mess header = new PXCS_Header_Mess(false,
                                                       PXCS_MessId.mid_ResumeReceive.getCode(),
                                                       mesByteArray.length);
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(header.toString());
        }

        byte[] data = new byte[PXCS_Header_Mess.hMsgLength + mesByteArray.length];
        System.arraycopy(header.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);

        this.mutex.lock();
        sendMessageNoWait(data);
        this.mutex.unlock();

        if (this.ieeChannelInform != null)
        {
            this.ieeChannelInform = null;
        }
    }

    @Override
    public void setChannelInform(IEE_ChannelInform inform)
    {
        this.ieeChannelInform = inform;
    }

    /**
     * Configure the ChannelInitiate interface.
     */
    @Override
    public void configure(ISLE_Reporter preporter, EE_APIPX_Database pdatabase)
    {
        // Nothing to do here
    }

    /**
     * @FunctionStart the traces. Forward the StartTrace to the LoggerAdapter.@EndFunction
     */
    @Override
    public void startTrace(ISLE_Trace trace, SLE_TraceLevel level, boolean forward) throws SleApiException
    {
        if (this.eeAPIPXLink == null)
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }

        IEE_APIPX_LoggerAdapter pLoggerAdapter = this.eeAPIPXLink.getLoggerAdapter();
        if (pLoggerAdapter == null)
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }

        pLoggerAdapter.startTrace(level, trace, false);
    }

    /**
     * Stop the traces. Forward the StopTrace to the LoggerAdapter.
     */
    @Override
    public void stopTrace() throws SleApiException
    {
        if (this.eeAPIPXLink == null)
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }

        IEE_APIPX_LoggerAdapter pLoggerAdapter = this.eeAPIPXLink.getLoggerAdapter();
        if (pLoggerAdapter == null)
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }

        // #hd# avoid an endless loop SLEAPIJ-19
        if(this != pLoggerAdapter) {
        	pLoggerAdapter.stopTrace();
        }
    }

    /**
     * This is the "main  function" of the thread class instance, ie when it
     * completes, then the thread will terminate. The goal of the thread here is
     * to manage the writing on the link object of a received PDU. This is done
     * in a separate thread in order to be able to respond immediatly to the
     * sender. It should be noted that there is no need to pass any objects in
     * here - the class instance itself can contain any reference to data needed
     * by the threadMain function.
     */
    private void threadMain()
    {
        while (true)
        {
        	// SLEAPIJ-26 wait for the transmission and resume the channel inform instance 
            waitForWriteAndResumeXmit(this.eeAPIPXLink, this.ieeChannelInform);
            
            if (this.eeAPIPXLink == null)
            {
                break;
            }
            if (this.eeAPIPXLink.isClosed())
            {
                break;
            }
        }
    }

    /**
     * This function is called to terminate the thread which waits for writting
     * message on the IPC link. It signals the condition variables, and waits
     * for the deletion of the thread (join).
     */
    private void terminateThread()
    {
        this.writingTh.terminate();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IEE_ChannelInitiate.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_TraceControl.class)
        {
            return (T) this;
        }

        else
        {
            return null;
        }
    }


    private class WritingThread extends Thread
    {
        private boolean isRunning = true;

        public WritingThread()
        {
        	super("SLE EE_APIPX_ChannelPxy.WritingThread");
        }        

        @Override
        public void run()
        {
            while (this.isRunning)
            {
                threadMain();
            }
        }

        public void terminate()
        {
            this.isRunning = false;
        }
    }


    @Override
    public void initialise(Socket pSock, ServerSocket sSock)
    {
        // Nothing to do here
    }

    @Override
    public void dispose()
    {
    	// Nothing to do here
    }

}
