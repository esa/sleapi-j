/**
 * @(#) EE_APIPX_Listener.java
 */

package esa.sle.impl.api.apipx.pxtml;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.icc.ISLE_TraceControl;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import esa.sle.impl.api.apipx.pxcs.EE_APIPX_Binder;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_Database;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_PortData;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_ResponderPort;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_ResponderPortList;
import esa.sle.impl.api.apipx.pxspl.EE_APIPX_ChannelFactory;
import esa.sle.impl.api.apipx.pxspl.IEE_ChannelInitiate;
import esa.sle.impl.ifs.gen.EE_LogMsg;
import esa.sle.impl.ifs.gen.EE_MessageRepository;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * Accepts requests to listen on channels, maintains the list of listening
 * sockets, and notifies the EE_APIPX_Binder singleton object when a new
 * connection occurs. The class opens listening sockets on logical ports in
 * response to startListen() requests, and when a connection is detected, it
 * creates a channel object (via the channel factory) and passes its interface
 * to the EE_APIPX_Binder singleton object. The calls to startListen specify a
 * port ID which resolves to a physical address. If a listening socket exists
 * for this address, the call will have no effect other than incrementing a
 * reference counter. If there is no listening socket for this address, then
 * listen is called on a socket and the reference count associated with this
 * listening socket is set to 1. The reference count is subsequently decremented
 * by stopListen. When the reference count reaches 0 the listener closes the
 * listening socket.
 */
public class EE_APIPX_Listener implements ISLE_TraceControl
{
    static private Logger LOG = Logger.getLogger(EE_APIPX_Listener.class.getName());

    /**
     * The unique instance of this class
     */
    private static Map<String, EE_APIPX_Listener> instanceMap = new HashMap<>();

    /**
     * Used to memories whether the object is initialized or not.
     */
    private boolean initialised;

    /**
     * Contains the trace level passed in to StartTrace.
     */
    private SLE_TraceLevel level;

    private ISLE_Reporter reporter;

    private EE_APIPX_Database db;

    private EE_APIPX_Binder binder;

    private ISLE_Trace trace;

    private final ReentrantLock objMutex;

    private final Map<String, ConnManagerThread> portThMap;

    private final String instanceId;
    
    /**
     * This method is called once to create the EE_APIPX_Listener instance
     * 
     * @param popFactory
     * @param putilFactory
     * @param pDatabase
     */
    public static synchronized void createListener(String instanceKey)
    {
    	EE_APIPX_Listener instance = instanceMap.get(instanceKey);
        if (instance == null)
        {
            instance = new EE_APIPX_Listener(instanceKey);
            instanceMap.put(instanceKey, instance);
        }
    }

    /**
     * This method is called every time the EE_APIPX_Listener instance is needed
     * 
     * @return
     */
    public static synchronized EE_APIPX_Listener getInstance(String instanceKey)
    {
    	EE_APIPX_Listener instance = instanceMap.get(instanceKey);
        if (instance == null)
        {
            throw new IllegalStateException("The createListener method has never been called and the instance never created for instance " + instanceKey);
        }

        return instance;
    }

    private EE_APIPX_Listener(String instanceKey)
    {
        this.initialised = false;
        this.level = SLE_TraceLevel.sleTL_low;
        this.reporter = null;
        this.binder = null;
        this.db = null;
        this.trace = null;
        this.objMutex = new ReentrantLock();
        this.portThMap = new HashMap<String, EE_APIPX_Listener.ConnManagerThread>();
        this.instanceId = instanceKey;
    }

    /**
     * Starts listening on a specified logical port. If the logical port
     * resolves to an address for which a listening socket exists, then no
     * listening socket is created, instead a reference count associated with
     * the socket is incremented. Note that if the request fails, then a log
     * output will be made to the reporter using LogRecord(). S_OK The request
     * was successfully made E_SLE_STATE The Listener object is not yet
     * Initialized E_SLE_NOPORT The logical port specified is not known to the
     * listener E_FAIL The request failed.
     */
    public HRESULT startListen(String port)
    {
        ServerSocket serverSocket = null;

        this.objMutex.lock();

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Start the listening thread on port " + port);
        }

        if (this.trace != null && this.level.getCode() >= SLE_TraceLevel.sleTL_low.getCode())
        {
            String msg = EE_MessageRepository.getMessage(EE_LogMsg.TMLTR_STARTLISTEN.getCode(), port);
            this.trace.traceRecord(SLE_TraceLevel.sleTL_low, SLE_Component.sleCP_proxy, null, msg);
        }

        if (!this.initialised)
        {
            this.objMutex.unlock();
            return HRESULT.SLE_E_STATE;
        }

        try
        {
            serverSocket = new ServerSocket();
        }
        catch (IOException e)
        {
            LOG.log(Level.FINE, "IOException ", e);
            String msg = "Failure while opening the server socket";
            this.reporter.logRecord(SLE_Component.sleCP_proxy,
                                    null,
                                    SLE_LogMessageType.sleLM_alarm,
                                    EE_LogMsg.TMLTR_IOEVENT.getCode(),
                                    msg);
            this.objMutex.unlock();
            return HRESULT.E_FAIL;
        }

        EE_APIPX_ResponderPortList prespList = this.db.getResponderPortList();
        EE_Reference<EE_APIPX_ResponderPort> pport = new EE_Reference<EE_APIPX_ResponderPort>();
        HRESULT retVal = prespList.getResponderPort(port, pport);

        if (retVal != HRESULT.S_OK)
        {
            String msg = EE_MessageRepository.getMessage(EE_LogMsg.TMLLISTENUNK.getCode(), port);
            this.reporter.logRecord(SLE_Component.sleCP_proxy,
                                    null,
                                    SLE_LogMessageType.sleLM_alarm,
                                    EE_LogMsg.TMLLISTENUNK.getCode(),
                                    msg);
            try
            {
                serverSocket.close();
            }
            catch (IOException e)
            {
                LOG.log(Level.FINE, "IOException ", e);
                msg = EE_MessageRepository.getMessage(EE_LogMsg.TMLTR_IOEVENT.getCode(), port);
                this.reporter.logRecord(SLE_Component.sleCP_proxy,
                                        null,
                                        SLE_LogMessageType.sleLM_alarm,
                                        EE_LogMsg.TMLTR_IOEVENT.getCode(),
                                        msg);
            }
            finally
            {
                this.objMutex.unlock();
            }

            return HRESULT.EE_E_NOPORT;
        }

        if (pport.getReference().getIsForeign())
        {
            try
            {
                serverSocket.close();
            }
            catch (IOException e)
            {
                LOG.log(Level.FINE, "IOException ", e);
                String msg = EE_MessageRepository.getMessage(EE_LogMsg.TMLTR_IOEVENT.getCode(), port);
                this.reporter.logRecord(SLE_Component.sleCP_proxy,
                                        null,
                                        SLE_LogMessageType.sleLM_alarm,
                                        EE_LogMsg.TMLTR_IOEVENT.getCode(),
                                        msg);
            }
            finally
            {
                this.objMutex.unlock();
            }

            return HRESULT.E_FAIL;
        }

        EE_APIPX_PortData pportData = pport.getReference().getPortData(0);
        assert (pportData != null) : "could not get physical address";

        try
        {
            int rcvBuffSize = pport.getReference().getRecvBufferSize();
            if (rcvBuffSize > 0)
            {
                serverSocket.setReceiveBufferSize(rcvBuffSize);
            }
            serverSocket.bind(new InetSocketAddress(pportData.getTcpPortNumber()));

            // start the connection accepting thread
            ConnManagerThread caThread = new ConnManagerThread(serverSocket);
            caThread.start();

            this.portThMap.put(port, caThread);
        }
        catch (IOException e)
        {
            LOG.log(Level.FINE, "IOException ", e);
            String msg = EE_MessageRepository.getMessage(EE_LogMsg.TMLLISTENFAIL.getCode(), port);
            this.reporter.logRecord(SLE_Component.sleCP_proxy,
                                    null,
                                    SLE_LogMessageType.sleLM_alarm,
                                    EE_LogMsg.TMLLISTENFAIL.getCode(),
                                    msg);
            try
            {
                serverSocket.close();
            }
            catch (IOException ioe)
            {
                LOG.log(Level.FINE, "IOException ", e);
                msg = EE_MessageRepository.getMessage(EE_LogMsg.TMLTR_IOEVENT.getCode(), port);
                this.reporter.logRecord(SLE_Component.sleCP_proxy,
                                        null,
                                        SLE_LogMessageType.sleLM_alarm,
                                        EE_LogMsg.TMLTR_IOEVENT.getCode(),
                                        msg);
            }

            return HRESULT.E_FAIL;
        }
        finally
        {
            this.objMutex.unlock();
        }

        return HRESULT.S_OK;
    }

    /**
     * Stops listening on a specified logical port. The listener resolves the
     * port to a physical address, locates the socket listening on that address,
     * decrements a reference counter associated with the socket, and closes the
     * socket if the reference count reaches 0. Note that if the port id cannot
     * be resolved, or there exists no listening socket for that address, then
     * the listener will log an error using the reporter.
     */
    public void stopListen(String port)
    {
        this.objMutex.lock();

        if (this.trace != null && this.level.getCode() >= SLE_TraceLevel.sleTL_low.getCode())
        {
            String msg = EE_MessageRepository.getMessage(EE_LogMsg.TMLTR_STOPLISTEN.getCode(), port);
            this.trace.traceRecord(SLE_TraceLevel.sleTL_low, SLE_Component.sleCP_proxy, null, msg);
        }

        ServerSocket serverSocket = null;
        if(this.portThMap != null && this.portThMap.get(port) != null) // SLEAPIJ-53
        {
        	serverSocket = this.portThMap.get(port).getServerSocket();
        }
        
        if (serverSocket == null)
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("No listening on port " + port);
            }

            this.objMutex.unlock(); // SLEAPIJ-58
            return;
        }

        if (!serverSocket.isClosed())
        {
            // the socket must be closed
            try
            {
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("Stop the listening thread on port " + port + " and close the server socket");
                }

                this.portThMap.get(port).terminateThread();
                serverSocket.close();
                // remove the port and thread from map
                for (Iterator<Map.Entry<String, ConnManagerThread>> it = this.portThMap.entrySet().iterator(); it
                        .hasNext();)
                {
                    Map.Entry<String, ConnManagerThread> entry = it.next();
                    if (entry.getKey().equals(port))
                    {
                        it.remove();
                    }
                }

            }
            catch (IOException e)
            {
                LOG.log(Level.FINE, "IOException ", e);
                String msg = "Failure while closing the server socket";
                this.reporter.logRecord(SLE_Component.sleCP_proxy,
                                        null,
                                        SLE_LogMessageType.sleLM_alarm,
                                        EE_LogMsg.TMLTR_IOEVENT.getCode(),
                                        msg);
            }
            finally
            {
                this.objMutex.unlock();
            }
        }
        else
        {
            // no socket listening to this port
            String msg = EE_MessageRepository.getMessage(EE_LogMsg.TMLCLOSELISTENFAIL.getCode(), port);
            this.reporter.logRecord(SLE_Component.sleCP_proxy,
                                    null,
                                    SLE_LogMessageType.sleLM_alarm,
                                    EE_LogMsg.TMLCLOSELISTENFAIL.getCode(),
                                    msg);

            this.objMutex.unlock();
        }
    }

    /**
     * This call prepares the listener to begin receiving requests. S_OK The
     * listener is initialized E_SLE_STATE The listener is already initialized.
     */
    public HRESULT initialise(EE_APIPX_Binder binder, ISLE_Reporter reporter, EE_APIPX_Database db)
    {
        this.objMutex.lock();

        if (this.db != null)
        {
            this.objMutex.unlock();
            return HRESULT.SLE_E_STATE;
        }

        this.db = db;
        this.binder = binder;
        this.reporter = reporter;
        this.initialised = true;

        this.objMutex.unlock();
        return HRESULT.S_OK;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == ISLE_TraceControl.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    /**
     * See the documentation of ISLE_TraceControl
     */
    @Override
    public void startTrace(ISLE_Trace trace, SLE_TraceLevel level, boolean forward)
    {
        this.objMutex.lock();
        if (this.trace != null)
        {
            this.trace = null;
        }

        this.trace = trace;
        this.level = level;
        this.objMutex.unlock();
    }

    /**
     * See the documentation of ISLE_TraceControl
     */
    @Override
    public void stopTrace()
    {
        this.objMutex.lock();
        if (this.trace != null)
        {
            this.trace = null;
        }
        this.objMutex.unlock();
    }

    /**
     * This call sets the new configuration. S_OK The new configuration is
     * successfully set. E_SLE_STATE The listener is still not initialized.
     */
    public HRESULT updateConfiguration(EE_APIPX_Database db)
    {
        this.objMutex.lock();
        if (this.db == null)
        {
            this.objMutex.unlock();
            return HRESULT.SLE_E_STATE;
        }
        this.db = db;
        this.objMutex.unlock();
        return HRESULT.S_OK;
    }

    public SLE_TraceLevel getLevel()
    {
        return this.level;
    }

    public void setLevel(SLE_TraceLevel level)
    {
        this.level = level;
    }

    public ISLE_Reporter getReporter()
    {
        return this.reporter;
    }

    public void setReporter(ISLE_Reporter reporter)
    {
        this.reporter = reporter;
    }

    public EE_APIPX_Binder getBinder()
    {
        return this.binder;
    }

    public void setBinder(EE_APIPX_Binder binder)
    {
        this.binder = binder;
    }

    public ISLE_Trace getTrace()
    {
        return this.trace;
    }

    public void setTrace(ISLE_Trace trace)
    {
        this.trace = trace;
    }


    private class ConnManagerThread extends Thread
    {
        private IEE_ChannelInitiate pchan;

        private final ServerSocket servSocket;

        private boolean isRunning;


        public ConnManagerThread(ServerSocket servSocket)
        {
            this.pchan = null;
            this.servSocket = servSocket;
            this.isRunning = true;
        }

        @Override
        public void run()
        {
            while (this.isRunning)
            {
                Socket clientSocket = null;
                try
                {
                    clientSocket = this.servSocket.accept();
                    clientSocket.setOOBInline(true);

                    if (getTrace() != null && getLevel().getCode() >= SLE_TraceLevel.sleTL_high.getCode())
                    {
                        String msg = EE_MessageRepository.getMessage(EE_LogMsg.TMLTR_NEWCONN.getCode());
                        getTrace().traceRecord(SLE_TraceLevel.sleTL_high, SLE_Component.sleCP_proxy, null, msg);
                    }
                }
                catch (IOException e)
                {
                    if (!this.isRunning)
                    {
                        return;
                    }

                    LOG.log(Level.FINE, "IOException ", e);
                    String msg = "Failure while waiting on the socket for a connection";
                    getReporter().logRecord(SLE_Component.sleCP_proxy,
                                            null,
                                            SLE_LogMessageType.sleLM_alarm,
                                            EE_LogMsg.TMLTR_IOEVENT.getCode(),
                                            msg);
                    break;
                }

                // a socket is connected, create the responding channel
                this.pchan = EE_APIPX_ChannelFactory.createChannel(EE_APIPX_Listener.this.instanceId, false, getReporter(), null);
                getBinder().rcvTcpCnx(this.pchan);

                // Initialize the responding channel
                this.pchan.initialise(clientSocket, this.servSocket);
                if (getTrace() != null && getLevel().getCode() >= SLE_TraceLevel.sleTL_high.getCode())
                {
                    String msg = EE_MessageRepository.getMessage(3052);
                    getTrace().traceRecord(SLE_TraceLevel.sleTL_high, SLE_Component.sleCP_proxy, null, msg);
                }
            }
        }

        public ServerSocket getServerSocket()
        {
            return this.servSocket;
        }

        public void terminateThread()
        {
            this.isRunning = false;
        }
    }
}
