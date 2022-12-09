/**
 * @(#) EE_APIPX_InitiatingChannel.java
 */

package esa.sle.impl.api.apipx.pxtml;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_ResponderPort;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_ResponderPortList;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_ISP1ProtocolAbortOriginator;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_TMLErrors;
import esa.sle.impl.ifs.gen.EE_LogMsg;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * This class extends the EE_APIPX_Channel class by providing handling for event
 * notifications in the establishing state, and provides functionality to
 * connect to a remote peer.
 */
public class EE_APIPX_InitiatingChannel extends EE_APIPX_Channel
{
    private static final int DEFAULT_CONENCT_TIMEOUT_MSEC = 2000;

	private static final Logger LOG = Logger.getLogger(EE_APIPX_InitiatingChannel.class.getName());

    private static int tcpConnectionTimeout = 8;

    /**
     * Contains the sockets created when connecting. The array is allocated
     * during sendConnect, and deallocated after leaving the establishing state.
     */
    private EE_APIPX_SocketConnectionMng[] connecting;

    /**
     * Set from the responder port used in the connect.
     */
    private int hbt;

    private int deadFactor;

    private final Semaphore semaphore;

    private String portID;

    EE_APIPX_ResponderPort pport;

    private boolean isConnected;


    /**
     * Constructor
     */
    public EE_APIPX_InitiatingChannel()
    {
        super();
        this.connecting = null;
        this.portID = null;
        this.pport = null;
        this.hbt = 0;
        this.deadFactor = 0;
        this.semaphore = new Semaphore(0);
        this.isConnected = false;
    }

    /**
     * This is invoked when the channel enters the closed state.
     */
    @Override
    public void cleanup()
    {
        super.cleanup();
    }

    /**
     * Initialize the TCP Socket in the Channel.
     */
    @Override
    public void initialise(Socket pSock, ServerSocket sSock)
    {
        // Nothing to do here
    }

    public void tcpConnectReq(String respPortId)
    {
        this.objMutex.lock();

        this.portID = respPortId;
        EE_APIPX_ResponderPortList pportList = this.db.getResponderPortList();
        EE_Reference<EE_APIPX_ResponderPort> pport = new EE_Reference<EE_APIPX_ResponderPort>();
        HRESULT retval = pportList.getResponderPort(this.portID, pport);
        this.pport = pport.getReference();
        int connectTimeout = DEFAULT_CONENCT_TIMEOUT_MSEC;
        try
        {
        	int value = this.db.getTMLData().getStartupTimer(); // SLEAPI-16
        	if(value >= 0) // allow 0
        	{
        		connectTimeout = value * 1000; // convert to ms
        	}
        }
        catch(Exception e)
        {
        	LOG.severe("Failed to read STARTUP_TIME from proxy configuration");
        }
        if(connectTimeout < 0 || connectTimeout > DEFAULT_CONENCT_TIMEOUT_MSEC * 10)
        {        	
        	connectTimeout = DEFAULT_CONENCT_TIMEOUT_MSEC;
        	LOG.info("SLE API: Use connect timeout " + connectTimeout + " for port " + respPortId);
        }
        
        if (retval != HRESULT.S_OK)
        {
            String strerr = "BAD PORT ID";
            logError(EE_LogMsg.TMLCONNECTFAILALL.getCode(), true, this.portID, strerr);
            forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.localTML,
                         EE_APIPX_TMLErrors.eeAPIPXtml_other.getCode(),
                         false,
                         0);
            this.objMutex.unlock();
        }
        else if (!pport.getReference().getIsForeign())
        {
            String strerr = "BAD PORT ID, specifies a LOCAL port ";
            logError(EE_LogMsg.TMLCONNECTFAILALL.getCode(), true, this.portID, strerr);
            forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.localTML,
                         EE_APIPX_TMLErrors.eeAPIPXtml_other.getCode(),
                         false,
                         0);
            this.objMutex.unlock();
        }
        else
        {
            this.objMutex.unlock();

            // create an array of socket managers
            this.connecting = new EE_APIPX_SocketConnectionMng[pport.getReference().getPortDataCardinality()];

            for (int i = 0; i < this.connecting.length; i++)
            {
                // try to connect starting a specific socket connection manager
                EE_APIPX_SocketConnectionMng scMng = new EE_APIPX_SocketConnectionMng(this, this.pport, this.portID, i, connectTimeout);
                this.connecting[i] = scMng;
            }

            for (EE_APIPX_SocketConnectionMng scMng : this.connecting)
            {
                scMng.start();
            }

            // wait for a successful TCP connection
            try
            {
                if (!this.semaphore.tryAcquire(tcpConnectionTimeout, TimeUnit.SECONDS))
                {
                    // no connections
                    logError(EE_LogMsg.TMLCONNECTFAILALL.getCode(),
                             true,
                             this.portID,
                             "Failure to connect to all the sockets");
                    forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.localTML,
                                 EE_APIPX_TMLErrors.eeAPIPXtml_other.getCode(),
                                 false,
                                 0);

                    return;
                }
            }
            catch (InterruptedException e)
            {
                LOG.log(Level.FINE, "InterruptedException ", e);
                // no connections
                logError(EE_LogMsg.TMLCONNECTFAIL.getCode(), true, this.portID, "Main thread was interrupted");
                return;
            }

            // connection succeeded
            this.channelState.tcpConnectCnf();
        }
    }

    public void connectionSucceeded(EE_APIPX_SocketConnectionMng scMng)
    {
        this.objMutex.lock();

        if (this.isConnected)
        {
            this.objMutex.unlock();
            return;
        }

        // create the context message and set the hb and the dead factor
        this.hbt = this.pport.getHbt();
        this.deadFactor = this.pport.getDeadFactor();

        // set the connected socket
        setConnectedSocket(scMng.getSocket(), null, this.hbt, this.hbt * this.deadFactor);

        this.isConnected = true;

        // send a TCP abort to all the other sockets that are attempting to
        // connect
        cancelOutstanding(scMng);

        this.objMutex.unlock();

        // release the semaphore
        this.semaphore.release();
    }

    /**
     * Cancels all the outstanding connection attempts.
     */
    private void cancelOutstanding(EE_APIPX_SocketConnectionMng scMng)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("--------- SENDING TCP-ABORT TO THE SOCKET IN THE SET");
        }

        if (this.connecting != null)
        {
            for (EE_APIPX_SocketConnectionMng scmOutstanding : this.connecting)
            {
                if (scMng != null
                    && (!scmOutstanding.getPortData().getTcpIPAddress().toString()
                            .equals(scMng.getPortData().getTcpIPAddress().toString()) || scmOutstanding.getPortData()
                            .getTcpPortNumber() != scMng.getPortData().getTcpPortNumber()))
                {
                    // send the TCP abort
                    try
                    {
                        if (LOG.isLoggable(Level.FINEST))
                        {
                            LOG.finest("Closing socket " + scmOutstanding.getPortData().getTcpIPAddress() + ", "
                                       + scmOutstanding.getPortData().getTcpPortNumber());
                        }

                        scmOutstanding.getSocket().close();
                    }
                    catch (IOException e)
                    {
                        LOG.log(Level.FINE, "IOException ", e);
                        String msg = "Failure while closing the socket";
                        // logError(EE_LogMsg.TMLTR_IOEVENT.getCode(), true,
                        // msg);
                        tcpError(EE_LogMsg.TMLTR_IOEVENT.getCode(), true, msg);
                    }
                }
            }
        }
    }

    public void sendContextMsg()
    {
        this.objMutex.lock();

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("--------- SENDING CONTEXT MESSAGE");
        }

        // create the context message and set the hb and the dead factor
        this.hbt = this.pport.getHbt();
        this.deadFactor = this.pport.getDeadFactor();

        this.commMng.sendMsg(this.tmlMsgFactory.createCtxMsg(this.hbt, this.deadFactor));

        // TCP connection OK
        trace(EE_LogMsg.TMLTR_ONCONNECTED.getCode(), SLE_TraceLevel.sleTL_low, this.portID);

        this.objMutex.unlock();

        // inform the application
        hlConnectedInd();
    }
}
