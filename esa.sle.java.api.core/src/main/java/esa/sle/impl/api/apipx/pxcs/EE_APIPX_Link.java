/**
 * @(#) EE_APIPX_BaseLink.java
 */

package esa.sle.impl.api.apipx.pxcs;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.icc.ISLE_TraceControl;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class contains all non operating system specific attributes and methods
 * to manage the link.
 */
public class EE_APIPX_Link
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_Link.class.getName());

    /**
     * Indicates if the link object is in the application process or in the
     * communication server process.
     */
    protected boolean inComServer;

    /**
     * Indicates if it is the link to the default logger.
     */
    protected boolean isDefaultLogger;

    protected IEE_APIPX_LoggerAdapter ieeAPIPXLoggerAdapter;

    protected EE_APIPX_AssocPxy eeAPIPXAssocPxy;

    protected EE_APIPX_BinderPxy eeAPIPXBinderPxy;

    protected EE_APIPX_ChannelPxy eeAPIPXChannelPxy;

    protected EE_APIPX_LoggerPxy eeAPIPXLoggerPxy;

    protected EE_APIPX_BinderAdapter eeAPIPXBinderAdapter;

    protected volatile ReceivingThread recThread;

    private Socket socket;

    private boolean useNagleFlag;

    protected ReentrantLock mutex;

    protected volatile boolean disconnectionRequested;

    private final String instanceId;

    public EE_APIPX_Link(String instanceKey)
    {
    	this.instanceId = instanceKey;
        this.socket = null;
        this.useNagleFlag = true;
        this.mutex = new ReentrantLock();
        this.disconnectionRequested = false;
    }

    public EE_APIPX_Link(String instanceKey, Socket socket, boolean isDfl)
    {
    	this.instanceId = instanceKey;
        this.inComServer = true;
        this.isDefaultLogger = false;
        this.ieeAPIPXLoggerAdapter = null;
        this.eeAPIPXAssocPxy = null;
        this.eeAPIPXBinderPxy = null;
        this.eeAPIPXChannelPxy = null;
        this.eeAPIPXBinderAdapter = null;
        this.recThread = null;
        this.socket = socket;
        this.disconnectionRequested = false;

        // the logger pxy must be ready for reporting
        this.eeAPIPXLoggerPxy = new EE_APIPX_LoggerPxy(instanceKey);
        this.eeAPIPXLoggerPxy.setLink(this);

        if (isDfl)
        {
            EE_APIPX_ReportTrace.setDefaultLogger(this.instanceId, this);
        }

        this.useNagleFlag = true;
        this.mutex = new ReentrantLock();
    }

    /**
     * Sets the LoggerAdapter associated with the link object.
     */
    public void setLoggerAdapter(IEE_APIPX_LoggerAdapter pLoggerAdapter)
    {
        this.ieeAPIPXLoggerAdapter = pLoggerAdapter;
    }

    /**
     * Gets the LoggerAdapter associated with the link object.
     */
    public IEE_APIPX_LoggerAdapter getLoggerAdapter()
    {
        return this.ieeAPIPXLoggerAdapter;
    }

    /**
     * Sets the AssocPxy associated with the link object.
     */
    public void setAssocPxy(EE_APIPX_AssocPxy pAssocPxy)
    {
        this.eeAPIPXAssocPxy = pAssocPxy;
    }

    /**
     * Gets the AssocPxy associated with the link object.
     */
    public EE_APIPX_AssocPxy getAssocPxy()
    {
        return this.eeAPIPXAssocPxy;
    }

    /**
     * Sets the BinderPxy associated with the link object.
     */
    public void setBinderPxy(EE_APIPX_BinderPxy pBinderPxy)
    {
        this.eeAPIPXBinderPxy = pBinderPxy;
    }

    /**
     * Gets the ChannelPxy associated with the link object.
     */
    public EE_APIPX_ChannelPxy getChannelPxy()
    {
        return this.eeAPIPXChannelPxy;
    }

    /**
     * Sets the ChannelPxy associated with the link object.
     */
    public void setChannelPxy(EE_APIPX_ChannelPxy pChannelPxy)
    {
        if (this.eeAPIPXChannelPxy != null)
        {
            // close the previous channel proxy
            this.eeAPIPXChannelPxy.setChannelInform(null);
            this.eeAPIPXChannelPxy.ipcClosed(this);
        }

        this.eeAPIPXChannelPxy = pChannelPxy;
    }

    /**
     * Gets the LoggerPxy associated with the link object.
     */
    public EE_APIPX_LoggerPxy getLoggerPxy()
    {
        return this.eeAPIPXLoggerPxy;
    }

    /**
     * Gets the IsDefaultLogger attribute.
     */
    public boolean getIsDefaultLogger()
    {
        return this.isDefaultLogger;
    }

    /**
     * This function is called to terminate the thread which waits for incoming
     * message on the IPC link. It waits for the deletion of the thread (join).
     */
    protected void terminateThread()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Stopping the receiving thread on link " + this + ": " + this.recThread.isCanceled());
        }
        this.recThread.cancel();

    }

    /**
     * This function is called when the IPC connection is closed by the peer
     * side or lost. The close message is forwarded to all the objects using the
     * link.
     */
    protected void ipcClosed()
    {
        disconnect();
    }

    /**
     * Gets the TraceControl interface of the Channel associated with the
     * AssocPxy associated with the link object.
     */
    public ISLE_TraceControl getChannelTraceControl()
    {
        if (this.eeAPIPXAssocPxy == null)
        {
            return null;
        }

        return this.eeAPIPXAssocPxy.getChannelTraceControl();
    }

    /**
     * Gets the TraceControl interface of the PDU Translator of the AssocPxy
     * associated with the link object.
     */
    public ISLE_TraceControl getTranslatorTraceControl()
    {
        if (this.eeAPIPXAssocPxy == null)
        {
            return null;
        }

        return this.eeAPIPXAssocPxy.getTranslatorTraceControl();
    }

    /**
     * This is the "main  function" of the thread class instance, i.e. when it
     * completes, then the thread will terminate. The goal of the thread is here
     * to manage the incoming messages on the IPC link. The thread completes
     * when the IPC connection is closed or lost. It should be noted that there
     * is no need to pass any objects in here - the class instance itself can
     * contain any reference to data needed by the threadMain function.
     */
    protected void threadMain()
    {
        HRESULT res = HRESULT.E_FAIL;
        EE_Reference<Integer> dataToBeRead = new EE_Reference<Integer>();
        dataToBeRead.setReference(new Integer(0));
        EE_Reference<Integer> messId = new EE_Reference<Integer>();
        EE_Reference<Boolean> lastPdu = new EE_Reference<Boolean>();
        boolean readHeader = true;

        while (!this.recThread.isCanceled())
        {
            if (readHeader)
            {
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("About to read the PXCS header on link " + this);
                }
                // read the header
                res = readHeader(dataToBeRead, messId, lastPdu);
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("PXCS header read on link " + this + " with result " + res);
                }
                if (res != HRESULT.S_OK)
                {
                    ipcClosed();
                }
                readHeader = false;
            }
            else
            {
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("About to read the PXCS data on link " + this);
                }
                // read data
                byte[] data = readData(dataToBeRead.getReference());
                if (res != HRESULT.S_OK)
                {
                    ipcClosed();
                }
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("About to process the PXCS data on link " + this + ", data.length=" + data.length);
                }
                rcvData(data, messId.getReference().intValue(), lastPdu.getReference());
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("PXCS data read on link " + this + ", messageId=" + messId.getReference() + ", lastPdu="
                               + lastPdu.getReference());
                }
                readHeader = true;
            }
        }
    }

    /**
     * This function is called when data are received on the IPC link. It
     * analyzes the type of data received and forwards the message to the
     * appropriate object.
     */
    protected void rcvData(byte[] data, int dataType, boolean lastPdu)
    {
        if (data == null)
        {
            return;
        }

        switch (PXCS_MessId.getPXCSMessIdByCode(dataType))
        {
        case mid_Rsp_RegisterPort:
        case mid_Rsp_DeregisterPort:
        case mid_Rsp_NormalStop:
        {
            if (!this.inComServer)
            {
                if (this.eeAPIPXBinderPxy != null)
                {
                    this.eeAPIPXBinderPxy.takeData(data, dataType, this, lastPdu);
                }
            }
            break;
        }
        case mid_BindPdu:
        {
            if (!this.inComServer)
            {
                // give the bind pdu to the binder proxy for creation of the
                // channel pxy
                // and responding association
                if (this.eeAPIPXBinderPxy != null)
                {
                    this.eeAPIPXBinderPxy.takeData(data, dataType, this, lastPdu);
                    // give the bind pdu to the channel proxy for processing
                    if (this.eeAPIPXChannelPxy != null)
                    {
                        this.eeAPIPXChannelPxy.takeData(data, dataType, this, lastPdu);
                    }
                }
            }
            break;
        }
        case mid_Disconnect:
        case mid_SlePdu:
        case mid_PeerAbort:
        case mid_ProtocolAbort:
        case mid_Reset:
        case mid_ResumeReceive:
        case mid_SuspendReceive:
        case mid_ResumeXmit:
        case mid_SuspendXmit:
        {
            if (this.inComServer)
            {
                // must be sent to the AssocPxy
                if (this.eeAPIPXAssocPxy != null)
                {
                    this.eeAPIPXAssocPxy.takeData(data, dataType, this, lastPdu);
                }
            }
            else
            {
                // must be sent to the ChannelPxy
                if (this.eeAPIPXChannelPxy != null)
                {
                    this.eeAPIPXChannelPxy.takeData(data, dataType, this, lastPdu);
                }
            }
            break;
        }
        case mid_NormalStop:
        {
            if (this.inComServer)
            {
                this.eeAPIPXLoggerPxy.takeData(data, dataType, this, lastPdu);
                if (this.eeAPIPXAssocPxy != null)
                {
                    this.eeAPIPXAssocPxy.takeData(data, dataType, this, lastPdu);
                }
            }
            break;
        }
        case mid_RegisterPort:
        case mid_DeregisterPort:
        {
            if (this.inComServer)
            {
                if (this.eeAPIPXBinderAdapter == null)
                {
                    this.eeAPIPXBinderAdapter = new EE_APIPX_BinderAdapter(this.instanceId);
                    this.eeAPIPXBinderAdapter.setLink(this);
                }
                this.eeAPIPXBinderAdapter.takeData(data, dataType, this, lastPdu);
            }
            break;
        }
        case mid_StartTrace:
        case mid_StopTrace:
        {
            if (this.inComServer)
            {
                if (this.eeAPIPXLoggerPxy != null)
                {
                    this.eeAPIPXLoggerPxy.takeData(data, dataType, this, lastPdu);
                }
            }
            break;
        }
        case mid_Rsp_StartTrace:
        case mid_Rsp_StopTrace:
        case mid_TraceRecord:
        case mid_LogRecord:
        case mid_Notify:
        {
            if (!this.inComServer)
            {
                if (this.ieeAPIPXLoggerAdapter != null)
                {
                    this.ieeAPIPXLoggerAdapter.takeData(data, dataType, this, lastPdu);
                }
            }
            break;
        }
        default:
        {
            break;
        }
        }
    }

    /**
     * Waits for incoming messages on the IPC link. For this purpose, a thread
     * is created. S_OK The receiving thread is created and wait for incoming
     * messages. E_FAIL The receiving thread cannot be created due to a further
     * unspecified error.
     */
    public HRESULT waitMsg()
    {
        this.recThread = new ReceivingThread();
        this.recThread.start();
        return HRESULT.S_OK;
    }

    /**
     * Connects an IPC link to the specified ipc address. S_OK The IPC
     * connection is established. E_FAIL The connection fails due to a further
     * unspecified error.
     */
    public HRESULT connect(String ipcAddress)
    {
        try
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Creating the client socket on link " + this);
            }
            this.socket = new Socket("localhost", Integer.parseInt(ipcAddress));
        }
        catch (NumberFormatException | IOException e1)
        {
            LOG.log(Level.FINE, "NumberFormatException or IOException:", e1);
            return HRESULT.E_FAIL;
        }

        if (!this.useNagleFlag)
        {
            // disable the NAGLE Algorithm
            try
            {
                this.socket.setTcpNoDelay(false);
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("***** NAGLE ALGORITHM DISABLED");
                }
            }
            catch (SocketException e)
            {
                LOG.log(Level.SEVERE, "Cannot disable Nagle algorithm: " + e.getMessage(), e);
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("***** Cannot disable Nagle algorithm");
                }
                return HRESULT.E_FAIL;
            }
        }

        return HRESULT.S_OK;
    }

    /**
     * Closes the IPC connection.
     */
    public void disconnect()
    {
        if (this.disconnectionRequested)
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Disconnect has been already invoked on link " + this + ". Ignoring...");
            }
            return;
        }

        this.disconnectionRequested = true;
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Disconnect has been invoked on link " + this);
        }

        // stop the receiving thread
        this.recThread.cancel();

        // close the client socket
        try
        {
            this.socket.close();
        }
        catch (IOException e)
        {
            LOG.log(Level.SEVERE, "Cannot close socket on link " + this + ":" + e.getMessage(), e);
        }

        if (this.inComServer)
        {
            // notification first at the assoc pxy --> can send PEER Abort
            // before it is deleted by the binder !
            if (this.eeAPIPXAssocPxy != null)
            {
                this.eeAPIPXAssocPxy.ipcClosed(this);
                this.eeAPIPXAssocPxy = null;
            }

            // binder adapter before the logger proxy.
            if (this.eeAPIPXBinderAdapter != null)
            {
                this.eeAPIPXBinderAdapter.ipcClosed(this);
            }

            if (this.eeAPIPXLoggerPxy != null)
            {
                this.eeAPIPXLoggerPxy.ipcClosed(this);
            }
        }
        else
        {
            if (this.eeAPIPXBinderPxy != null)
            {
                this.eeAPIPXBinderPxy.ipcClosed(this);
                this.eeAPIPXBinderPxy = null;
            }

            if (this.eeAPIPXChannelPxy != null)
            {
                this.eeAPIPXChannelPxy.ipcClosed(this);
                this.eeAPIPXChannelPxy = null;
            }

            if (this.isDefaultLogger)
            {
                if (this.ieeAPIPXLoggerAdapter != null)
                {
                    this.ieeAPIPXLoggerAdapter.ipcClosed(this);
                }
                this.ieeAPIPXLoggerAdapter = null;
            }
        }
    }

    /**
     * Sends a message on the IPC link. S_OK The message has been sent. E_FAIL
     * The message cannot be sent due to a further unspecified error.
     */
    public HRESULT sndMess(byte[] data)
    {
        this.mutex.lock();

        try
        {
            this.socket.getOutputStream().write(data);
        }
        catch (IOException e)
        {
            LOG.log(Level.SEVERE, "Cannot send data on the client socket on link " + this + ":" + e.getMessage(), e);
            return HRESULT.E_FAIL;
        }
        finally
        {
            this.mutex.unlock();
        }

        EE_GenStrUtil.print("Writing to socket on link " + this + ": ", data);

        return HRESULT.S_OK;
    }

    /**
     * Indicates if the IPC link is closed.
     */
    public boolean isClosed()
    {
        return this.socket.isClosed();
    }

    /**
     * This function reads a header message on the IPC link. This header
     * indicates the length and the type of the next incoming message. CodesS_OK
     * The header has been received. E_FAIL Cannot receive a header message.
     */
    private HRESULT readHeader(EE_Reference<Integer> lg, EE_Reference<Integer> dataType, EE_Reference<Boolean> lastPdu)
    {
        // the socket can be closed by other thread!
        if (isClosed())
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("The socket on link " + this + " has been closed by another thread");
            }
            return HRESULT.E_FAIL;
        }

        PXCS_Header_Mess header = null;
        byte[] headerByte = readData(PXCS_Header_Mess.hMsgLength);
        if (headerByte != null)
        {
            // the header is complete
            header = new PXCS_Header_Mess(headerByte);
            lg.setReference(header.getLength());
            dataType.setReference(header.getMid());
            lastPdu.setReference(header.isLastPdu());
            return HRESULT.S_OK;
        }

        return HRESULT.E_FAIL;
    }

    /**
     * This function reads a message on the IPC link. S_OK The data had been
     * received. E_FAIL Cannot receive the data.
     */
    protected byte[] readData(int toBeRead)
    {
        int dataRead = 0;
        byte[] data = new byte[toBeRead];
        int length = data.length;
        try
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("About to read " + toBeRead + " data on the link " + this);
            }           
            while(dataRead < length) {
    			int currentlyRead = this.socket.getInputStream().read(data, dataRead, length - dataRead);
    			if(currentlyRead <= 0) {
    				return null;
    			}
    			dataRead += currentlyRead;
    		}
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Read " + dataRead + " data on the link " + this);
            }
            if (dataRead < 0)
            {
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("The end of the stream has been reached on link " + this);
                }
                return null;
            }
        }
        catch (IOException e)
        {
            if (!this.recThread.cancelThread)
            {
                LOG.log(Level.SEVERE, "Link disconnection detected on link " + this + ":" + e.getMessage(), e);
            }
            return null;
        }

        EE_GenStrUtil.print("Read from socket on link " + this + ": ", data);

        return data;
    }

    public void setUseNagleFlag(boolean useNagleFlag)
    {
        this.useNagleFlag = useNagleFlag;
    }


    protected class ReceivingThread extends Thread
    {

        protected volatile boolean cancelThread;

         public ReceivingThread()
        {
         	super("SLE EE_APIPX_Link Thread (IPC read / socket write)");
            this.cancelThread = false;
        }

        @Override
        public void run()
        {
            threadMain();
        }

        public void cancel()
        {
            this.cancelThread = true;
        }

        public boolean isCanceled()
        {
            return this.cancelThread;
        }
    }
}
