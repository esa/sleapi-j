/**
 * @(#) EE_APISE_ServiceInstance.java
 */

package esa.sle.impl.api.apise.slese;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.icc.ISLE_TimeoutProcessor;
import ccsds.sle.api.isle.icc.ISLE_TraceControl;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iop.ISLE_PeerAbort;
import ccsds.sle.api.isle.ipx.ISLE_AssocFactory;
import ccsds.sle.api.isle.ipx.ISLE_ProxyAdmin;
import ccsds.sle.api.isle.ipx.ISLE_SrvProxyInitiate;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SIAdmin;
import ccsds.sle.api.isle.ise.ISLE_ServiceInitiate;
import ccsds.sle.api.isle.ise.ISLE_SrvProxyInform;
import ccsds.sle.api.isle.it.SLE_AbortOriginator;
import ccsds.sle.api.isle.it.SLE_Alarm;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_BindDiagnostic;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_Diagnostics;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_PeerAbortDiagnostic;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.it.SLE_SIState;
import ccsds.sle.api.isle.it.SLE_TimeFmt;
import ccsds.sle.api.isle.it.SLE_TimeRes;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.api.apise.slese.types.EE_TI_SLESE_Event;
import esa.sle.impl.ifs.gen.EE_APIOpSequencer;
import esa.sle.impl.ifs.gen.EE_LogMsg;
import esa.sle.impl.ifs.gen.EE_MessageRepository;
import esa.sle.impl.ifs.time.EE_Duration;
import esa.sle.impl.ifs.time.EE_ElapsedTimer;
import esa.sle.impl.ifs.time.EE_Time;

/**
 * The class EE_APISE_ServiceInstance provides the functionality of the
 * component class API Service Instance defined by reference [SLE-API]. It is a
 * base-class for specialized service instance classes providing functionality
 * common to all service instances. It is responsible for 1) configuration of
 * the service instance requested via the interface ISLE_SIAdmin 2) assignment
 * of unique invocation identifiers to operation objects received from the proxy
 * and the application and checking of invocation identifiers 3) memorising
 * local and remote returns of operation invocations passed from the proxy and
 * the application 4) handling of the return-timeout, this includes a) start of
 * the return timer for confirmed operations b) cancellation of the return-timer
 * when the operation return arrives in time c) association abort and reporting
 * to the application on a return timeout 5) checking of the validity of
 * operation objects, which are independent of the role and service type, in the
 * current state 6) reception of PDU's from the application/proxy and passing
 * them to the proxy/application The class provides a set of virtual member
 * functions, which must be re-implemented by derived classes. These functions
 * are called by the base-class by the member functions implementing an
 * interface (e.g. by ISLE_SrvProxyInform::InformOpInvoke() ) in order to direct
 * the provider/user specific and service type specific requests to the derived
 * class which handles the request accordingly. Intermediate derived classes use
 * the same approach to further delegate functionality to more specialised
 * classes. Internal Note: The _startTime and _stopTime is actually only
 * relevant for the provider service instance (PSI), but the user can also set
 * the provision period (which has no effect for processing) via ISLE_SIAdmin,
 * which is implemnted by the base-class. Therefore, the start-and stop time is
 * part of the SI base-class; accessor functions are provided in the DD. Object
 * Creation: Derived classes have to use the protected constructor for object
 * creation. The service instance creator has to call initialise() after object
 * creation. Service Instance Configuration: The call to ConfigCompleted() on
 * the interface ISLE_SIAdmin first performs all checks on consistency and
 * completeness of the configuration parameters. This is started by a call to
 * doConfigCompleted(); this member function is virtual and must be
 * re-implemented by the derived class(es). When doConfigCompleted() returns
 * S_OK, the service instance (in the provider role) base-class performs
 * port-registration. If that succeeds, the configuration is completed and the
 * service instance is ready for operation. Service Instance Location: For the
 * service instance location (Service Element) the client has to call the public
 * method checkBindInvocation(). This method performs all necessary checks on
 * the BIND invocation and links the service instance object to the association
 * object using the supplied interface. When the function returns S_OK, the
 * service instance is ready to receive a BIND and other invocations from the
 * proxy. Processing of operation objects: Processing of operation invocations
 * and returns received from the application interface is performed such that
 * sequencing of the supplied operation object is done as the very first action.
 * As soon as the sequencer has returned with success, the Service instance is
 * locked and the supplied operation is processed. The object-lock is released
 * before the operation is passed to the ISLE_SrvProxyInitiate interface. The
 * same mechanism is applied for processing of operation objects received from
 * the interface ISLE_SrvProxyInform. Operation objects passed to the service
 * instance via the interface ISLE_ServiceInitiate are first passed to the
 * virtual function doInitiateOpInvoke() or doInitiateOpReturn() respectively.
 * These functions have to be implemented by the most derived classes such that
 * the operation objects are forwarded to operation-specific functions, e.g.
 * bindInv(), which perform operation-specific checks. When these checks have
 * been passed, the operation object is forwarded to the state-processing
 * function doStateProcessing(). State-processing is performed according to the
 * specification, including forwarding of operation objects to the proxy. For
 * operation objects received from the proxy via the interface
 * ISLE_SrvProxyInform , the same approach is used, starting with a call to
 * doInformOpInvoke() or doInformOpReturn(). The class provides a set of
 * abstract member functions for operation invocations and returns that have to
 * be implemented by derived classes in the specialised way. These functions are
 * bindInv/Rtn, unbindUnv/Rtn and scheduleStatusReportInv/Rtn. All other types
 * of operation invocations and returns need special service-type specific
 * treatment and are therefore not prepared in the base-class. Logging and
 * Tracing: For logging and tracing, derived classes have to use the protected
 * member functions logRecord(), notify() and trace(). The base-class
 * automatically forwards the supplied information to the application by adding
 * the service instance identifier and the component id to the function on the
 * corresponding interface. Aborting an association: If an association to the
 * user/provider has to be aborted, the service instance (also derived service
 * instances) calls the abort() member function, which is also accessible by
 * derived classes. The abort() member function generates a PEER-ABORT
 * operation, passes it to the proxy and to the application. Subsequently a call
 * to the virtual function cleanup() is performed. The base-class implementation
 * removes all pending local and remote returns and cancels active
 * return-timers. Derived classes that need to do cleanup activities must
 * re-implement the cleanup() function.
 */

public abstract class EE_APISE_ServiceInstance implements ISLE_TraceControl, ISLE_ServiceInitiate,
                                              ISLE_TimeoutProcessor, ISLE_SIAdmin, ISLE_SrvProxyInform
{
    static private Logger LOG = Logger.getLogger(EE_APISE_ServiceInstance.class.getName());

    /**
     * The Service Instance state.
     */
    private SLE_SIState state;

    /**
     * The peer identifier of the peer application (Authority Identifier).
     */
    private String peerId;

    /**
     * The port identifier of the responding application.
     */
    private String rspPortId;

    /**
     * The service type the Service Instance supports.
     */
    private SLE_ApplicationIdentifier serviceType;

    /**
     * The version number of the service type the Service Instance supports.
     */
    private int version;

    /**
     * The value of the return timeout in seconds.
     */
    private int returnTimeout;

    /**
     * The provision period start time.
     */
    private ISLE_Time startTime;

    /**
     * The provision period stop time.
     */
    private ISLE_Time stopTime;

    /**
     * The timer for the end of provision period.
     */
    private EE_ElapsedTimer ppTimer;

    /**
     * The role supported by the service instance.
     */
    private SLE_AppRole supportedRole;

    /**
     * Specifies whether user or server initiated binding shall be used.
     */
    private SLE_AppRole bindInitiative;

    /**
     * The ISLE_ServiceInform interface to the application
     */
    private ISLE_ServiceInform aplSrvInform;

    /**
     * The interface to the proxy association.
     */
    private ISLE_SrvProxyInitiate pxySrvInit;

    /**
     * The operation factory to be used within the service instance.
     */
    private ISLE_OperationFactory opFactory;

    /**
     * The utility factory to be used within the service instance.
     */
    private ISLE_UtilFactory utilFactory;

    /**
     * The reporter interface to be used within the service instance.
     */
    private ISLE_Reporter reporter;

    /**
     * The trace interface to be used within the service instance if tracing is
     * on. The pointer to the interface is set when tracing is switched on via
     * the interface ISLE_TraceControl.
     */
    private ISLE_Trace trace;

    /**
     * The tracing level to be applied if tracing is on.
     */
    protected SLE_TraceLevel traceLevel; // SLEAPIJ-14

    /**
     * The information whether a start-trace request has been forwarded to the
     * association.
     */
    private boolean startTraceForwarded;

    /**
     * Holds the information whether or not the service instance is configured.
     */
    private boolean isConfigured;

    /**
     * The invocation identifier to be assigned to the next confirmed operation
     * invocation received from the application.
     */
    private int invokeId;

    /**
     * The pointer to the proxy to be used e.g. for port registration or for
     * using the association factory. The pointer to this interface is obtained
     * from the service element object when it is needed the first time.
     */
    private IUnknown proxy;

    /**
     * The port registration Id for responding service instances.
     */
    private int portRegId;

    /**
     * The sequence-count to be used for operations that are being passed to the
     * application.
     */
    private long aplSeqCount;

    /**
     * The sequence-count to be used for operations that are being passed to the
     * proxy component.
     */
    private long pxySeqCount;

    /**
     * The information whether or not the provision-period has ended either by a
     * user-request (Unbind, reason = end) or by the expiration of the
     * provision-period timer.
     */
    private boolean ppEnded;

    /**
     * The outer mutex is used to protect the object to become inconsistent when
     * the (outer) lock is given up before the Proxy is called. The outer mutex
     * must be locked when a thread coming from the application propagates into
     * the service instance and must be unlocked before it returns. This outer
     * mutex is essential if the application uses the API with multiple threads
     * (e.g. one thread per frame).
     */
//    private final ReentrantLock outerMutex;

    protected final ReentrantLock objMutex;

    private final EE_APIOpSequencer aplOpSequencer;

    private final List<ReturnsPair> remoteReturns;

    private final EE_APIOpSequencer pxyOpSequencer;

    private ISLE_SII sii = null;

    private List<ISLE_ConfirmedOperation> localReturns = new ArrayList<ISLE_ConfirmedOperation>();

    /**
     * The library instance id, which this object refers to.
     */
    protected final String instanceId;

    /**
     * The protected constructor with no arguments.
     */
    protected EE_APISE_ServiceInstance(String instanceKey)
    {
    	this.instanceId = instanceKey;
        this.state = SLE_SIState.sleSIS_unbound;
        this.peerId = null;
        this.rspPortId = null;
        this.serviceType = SLE_ApplicationIdentifier.sleAI_invalid;
        this.version = 0;
        this.returnTimeout = 0;
        this.startTime = null;
        this.stopTime = null;
        this.ppTimer = null;
        this.supportedRole = SLE_AppRole.sleAR_user;
        this.bindInitiative = SLE_AppRole.sleAR_user;
        this.aplSrvInform = null;
        this.pxySrvInit = null;
        this.opFactory = null;
        this.utilFactory = null;
        this.reporter = null;
        this.trace = null;
        this.traceLevel = SLE_TraceLevel.sleTL_low;
        this.startTraceForwarded = false;
        this.isConfigured = false;
        this.invokeId = 0;
        this.proxy = null;
        this.portRegId = -1;
        this.aplSeqCount = 0;
        this.pxySeqCount = 0;
        this.ppEnded = false;
        this.aplOpSequencer = new EE_APIOpSequencer();
        this.pxyOpSequencer = new EE_APIOpSequencer();
        this.sii = null;
        this.remoteReturns = new ArrayList<ReturnsPair>();
        this.localReturns = new ArrayList<ISLE_ConfirmedOperation>();
//        this.outerMutex = new ReentrantLock();
        this.objMutex = new ReentrantLock();
    }

    /**
     * The protected constructor to be used for service instance creation.
     * 
     * @param srvType
     * @param clientIf
     * @param suppRole
     */
    protected EE_APISE_ServiceInstance(String instanceKey,
    								   SLE_ApplicationIdentifier srvType,
                                       ISLE_ServiceInform clientIf,
                                       SLE_AppRole suppRole)
    {
    	this.instanceId = instanceKey;
        this.state = SLE_SIState.sleSIS_unbound;
        this.peerId = null;
        this.rspPortId = null;
        this.serviceType = SLE_ApplicationIdentifier.sleAI_invalid;
        this.version = 0;
        this.returnTimeout = 0;
        this.startTime = null;
        this.stopTime = null;
        this.ppTimer = null;
        this.supportedRole = SLE_AppRole.sleAR_user;
        this.bindInitiative = SLE_AppRole.sleAR_user;
        this.pxySrvInit = null;
        this.opFactory = null;
        this.utilFactory = null;
        this.reporter = null;
        this.trace = null;
        this.traceLevel = SLE_TraceLevel.sleTL_low;
        this.startTraceForwarded = false;
        this.isConfigured = false;
        this.invokeId = 0;
        this.proxy = null;
        this.portRegId = -1;
        this.aplSeqCount = 0;
        this.pxySeqCount = 0;
        this.ppEnded = false;
        this.aplOpSequencer = new EE_APIOpSequencer();
        this.pxyOpSequencer = new EE_APIOpSequencer();
        this.sii = null;

        this.serviceType = srvType;
        this.aplSrvInform = clientIf;
        this.supportedRole = suppRole;
        this.remoteReturns = new ArrayList<ReturnsPair>();
        this.localReturns = new ArrayList<ISLE_ConfirmedOperation>();
//        this.outerMutex = new ReentrantLock();
        this.objMutex = new ReentrantLock();
    }

    /**
     * Initializes the service instance object with the supplied interfaces.
     * This function must be called right after object creation.
     *
     * @param opFactory
     * @param utlFactory
     * @param reporter
     * @param version
     */
    public void initialise(ISLE_OperationFactory opFactory,
                           ISLE_UtilFactory utlFactory,
                           ISLE_Reporter reporter,
                           int version)
    {
        this.opFactory = opFactory;
        this.utilFactory = utlFactory;
        this.reporter = reporter;

        if (this.supportedRole == SLE_AppRole.sleAR_user)
        {
            this.version = version;
        }
        else
        {
            // ignore the versoin parameter:
            // the provider SI takes the version from the received BIND.
            this.version = 0;
        }
    }

    /**
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_SIAdmin.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_ServiceInitiate.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_SrvProxyInform.class)
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

    /**
     * @param id
     */
    @Override
    public void setServiceInstanceId(ISLE_SII id)
    {
        if (this.isConfigured)
        {
            return;
        }
        this.sii = id.copy();
    }

    /**
     * @param id
     */
    @Override
    public void putServiceInstanceId(ISLE_SII id)
    {
        if (this.isConfigured)
        {
            return;
        }
        this.sii = id;
    }

    /**
     * @param id
     */
    @Override
    public void setPeerIdentifier(String id)
    {
        if (this.isConfigured)
        {
            return;
        }

        this.peerId = new String(id);
    }

    /**
     * @param start
     * @param stop
     */
    @Override
    public void setProvisionPeriod(ISLE_Time start, ISLE_Time stop)
    {
        if (this.isConfigured)
        {
            return;
        }

        this.startTime = start;
        this.stopTime = stop;

    }

    @Override
    public void setBindInitiative(SLE_AppRole role)
    {
        if (this.isConfigured)
        {
            return;
        }

        this.bindInitiative = role;
    }

    @Override
    public void setResponderPortIdentifier(String portId)
    {
        if (this.isConfigured)
        {
            return;
        }

        this.rspPortId = new String(portId);
    }

    @Override
    public void setReturnTimeout(int timeout)
    {
        if (this.isConfigured)
        {
            return;
        }

        this.returnTimeout = timeout;
    }

    @Override
    public void configCompleted() throws SleApiException
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }
        this.objMutex.lock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }
        HRESULT rc = HRESULT.S_OK;

        if (this.isConfigured)
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                           + this.objMutex.getHoldCount());
            }
            this.objMutex.unlock();
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                           + this.objMutex.getHoldCount());
            }
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        rc = doConfigCompleted();

        if (rc == HRESULT.S_OK)
        {
            if (this.supportedRole == SLE_AppRole.sleAR_provider)
            {
                // port registration for a responding SI
                EE_APISE_Database db = EE_APISE_Database.getDb(this.instanceId);
                String protId = db.getProtocolId(this.rspPortId);
                this.proxy = EE_APISE_ServiceElement.getInstance(this.instanceId).getProxy(protId);
                if (this.proxy == null)
                {
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                                   + this.objMutex.getHoldCount());
                    }
                    this.objMutex.unlock();
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                                   + this.objMutex.getHoldCount());
                    }
                    throw new SleApiException(HRESULT.SLE_E_CONFIG);
                }

                ISLE_ProxyAdmin pa = this.proxy.queryInterface(ISLE_ProxyAdmin.class);
                if (pa == null)
                {
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                                   + this.objMutex.getHoldCount());
                    }
                    this.objMutex.unlock();
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                                   + this.objMutex.getHoldCount());
                    }
                    throw new SleApiException(HRESULT.SLE_E_CONFIG);
                }

                try
                {
                    this.portRegId = pa.registerPort(this.sii, this.rspPortId);
                }
                catch (SleApiException e)
                {
                    if (e.getHResult() == HRESULT.SLE_E_DUPLICATE)
                    {
                        logRecord(SLE_LogMessageType.sleLM_alarm,
                                  EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                                  "duplicate port registration");
                    }
                    else if (e.getHResult() == HRESULT.E_NOTIMPL)
                    {
                        logRecord(SLE_LogMessageType.sleLM_alarm,
                                  EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                                  "port registration not supported by the proxy");
                    }

                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                                   + this.objMutex.getHoldCount());
                    }
                    this.objMutex.unlock();
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                                   + this.objMutex.getHoldCount());
                    }

                    throw new SleApiException(HRESULT.SLE_E_PORT, this.rspPortId);
                }

                // the provision-period timer can be started now
                String endTime = this.stopTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_seconds);

                EE_Time currentTime = new EE_Time();
                EE_Time stopTime = new EE_Time();

                ISLE_Time pcurrentTime = this.utilFactory.createTime(ISLE_Time.class);
                if (pcurrentTime == null)
                {
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                                   + this.objMutex.getHoldCount());
                    }
                    this.objMutex.unlock();
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                                   + this.objMutex.getHoldCount());
                    }
                    throw new SleApiException(HRESULT.SLE_E_CONFIG);
                }

                try
                {
                    stopTime.setCCSDSDateAndTime(endTime);
                }
                catch (SleApiException e1)
                {
                    LOG.log(Level.FINE, "SleApiException ", e1);
                }
                pcurrentTime.update();

                String nowTime = pcurrentTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_seconds);

                try
                {
                    currentTime.setCCSDSDateAndTime(nowTime);
                }
                catch (SleApiException e1)
                {
                    LOG.log(Level.FINE, "SleApiException ", e1);
                }

                this.ppTimer = new EE_ElapsedTimer();
                EE_Duration dur = stopTime.subtractTime(currentTime);

                try
                {
                    this.ppTimer.start(dur, this, 0);
                }
                catch (SleApiException e)
                {
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                                   + this.objMutex.getHoldCount());
                    }
                    this.objMutex.unlock();
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                                   + this.objMutex.getHoldCount());
                    }

                    throw new SleApiException(HRESULT.SLE_E_CONFIG);
                }
            } // role == provider
            this.isConfigured = true;
        }
        else
        {
        	LOG.severe("ConfigCompleted returned with error: " + rc);
        	throw new SleApiException(rc);
        }
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
        this.objMutex.unlock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
    }

    @Override
    public SLE_ApplicationIdentifier getServiceType()
    {
        return this.serviceType;
    }

    @Override
    public int getVersion()
    {
        int vn = this.version;
        return vn;
    }

    @Override
    public SLE_AppRole getRole()
    {
        return this.supportedRole;
    }

    @Override
    public ISLE_SII getServiceInstanceIdentifier()
    {
        return this.sii;
    }

    @Override
    public String getPeerIdentifier()
    {
        return this.peerId;
    }

    @Override
    public ISLE_Time getProvisionPeriodStop()
    {
        return this.stopTime;
    }

    @Override
    public ISLE_Time getProvisionPeriodStart()
    {
        return this.startTime;
    }

    @Override
    public SLE_AppRole getBindInitiative()
    {
        return this.bindInitiative;
    }

    @Override
    public String getResponderPortIdentifier()
    {
        return this.rspPortId;
    }

    @Override
    public int getReturnTimeout()
    {
        return this.returnTimeout;
    }

    @Override
    public void initiateOpInvoke(ISLE_Operation poperation, long seqCount) throws SleApiException
    {
        HRESULT rc = HRESULT.S_OK;
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("seqCount : " + seqCount + "   " + poperation.getOperationType());
        }
        rc = this.aplOpSequencer.serialise(poperation, seqCount);

        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before double locking " + Thread.currentThread().getId());
        }
//        this.outerMutex.lock();
        this.objMutex.lock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after double locking " + Thread.currentThread().getId());
        }

        if (this.trace != null && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_medium.getCode())
        {
            String txt = poperation.getOperationType().toString();
            txt += " invocation received from the application";
            trace(SLE_TraceLevel.sleTL_medium, txt);
        }

        rc = doInitiateOpInvoke(poperation);

        if (rc == HRESULT.SLE_S_TRANSMITTED || rc == HRESULT.SLE_S_QUEUED)
        {
            rc = HRESULT.S_OK;
        }
        else if (rc == HRESULT.EE_E_NOSUCHEVENT)
        {
            rc = HRESULT.SLE_E_INVALIDPDU;
        }

        // only for failed invokes, the default dump is shifted to
        // initiatePxyOpInvoke
        if (rc != HRESULT.S_OK)
        {
            traceOperation(poperation, " invocation received from the application");
        }

        // check if a BIND invocation was successful and reset sequencer if
        // necessary:
        if (rc != HRESULT.S_OK && this.state == SLE_SIState.sleSIS_unbound
            && poperation.getOperationType() == SLE_OpType.sleOT_bind)
        {
            this.aplOpSequencer.reset(HRESULT.SLE_E_ABORTED);
        }
        else
        {
            this.aplOpSequencer.cont();
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before double unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount()/* + " outerMutex=" + this.outerMutex.getHoldCount()*/);
        }
        this.objMutex.unlock();
//        this.outerMutex.unlock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after double unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount()/* + " outerMutex=" + this.outerMutex.getHoldCount()*/);
        }

        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }
    }

    /**
     */
    @Override
    public void initiateOpReturn(ISLE_ConfirmedOperation poperation, long seqCount) throws SleApiException
    {
        // Sequencer
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("seqCount : " + seqCount + "   " + poperation.getOperationType());
        }
        HRESULT rc = this.aplOpSequencer.serialise(poperation, seqCount);
        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before double locking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount()/* + " outerMutex=" + this.outerMutex.getHoldCount()*/);
        }
      
        
//        this.outerMutex.lock();
        this.objMutex.lock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after double locking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount()/* + " outerMutex=" + this.outerMutex.getHoldCount()*/);
        }

        if (this.trace != null && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_medium.getCode())
        {
            String txt = poperation.getOperationType().toString();
            txt += " return received from the application";
            trace(SLE_TraceLevel.sleTL_medium, txt);
        }

        rc = doInitiateOpReturn(poperation);

        if (rc == HRESULT.SLE_S_TRANSMITTED || rc == HRESULT.SLE_S_QUEUED)
        {
            rc = HRESULT.S_OK;
        }
        else if (rc == HRESULT.EE_E_NOSUCHEVENT)
        {
            rc = HRESULT.SLE_E_INVALIDPDU;
        }

        // valid operations tracing shifted to initiatePxyRtn
        if (rc != HRESULT.S_OK)
        {
            traceOperation(poperation, " return received from the application");
        }

        this.aplOpSequencer.cont();

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before double unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount()/* + " outerMutex=" + this.outerMutex.getHoldCount()*/);
        }
        this.objMutex.unlock();
//        this.outerMutex.unlock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after double unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount()/* + " outerMutex=" + this.outerMutex.getHoldCount()*/);
        }

        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }
    }

    @Override
    public SLE_SIState getSIState()
    {
        return this.state;
    }

    /**
     */
    @Override
    public void informOpInvoke(ISLE_Operation poperation, long seqCount) throws SleApiException
    {
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Invoke operation " + poperation.getOperationType()
                     + " received from proxy with sequence counter " + seqCount);
        }
        // Sequencer
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("seqCount : " + seqCount + "   " + poperation.getOperationType());
        }
        HRESULT rc = this.pxyOpSequencer.serialise(poperation, seqCount);
        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }

//        this.outerMutex.lock(); // DL: added
        this.objMutex.lock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }

        traceOperation(poperation, " invocation received from the proxy");

        rc = doInformOpInvoke(poperation);
        if (rc == HRESULT.SLE_E_ROLE)
        {
            String name = poperation.getOperationType().toString();
            logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_IncompatibleInvPDU.getCode(), name);
            abort(SLE_PeerAbortDiagnostic.slePAD_protocolError);
        }

        if (rc == HRESULT.EE_E_REJECTED || rc == HRESULT.SLE_E_ROLE)
        {
            rc = HRESULT.S_OK;
        }
        else if (rc == HRESULT.EE_E_NOSUCHEVENT)
        {
            rc = HRESULT.SLE_E_INVALIDPDU;
        }

        // check if a BIND invocation was successful and reset
        // sequencer if necessary:
        if (rc != HRESULT.S_OK && this.state == SLE_SIState.sleSIS_unbound
            && poperation.getOperationType() == SLE_OpType.sleOT_bind)
        {
            this.pxyOpSequencer.reset(HRESULT.SLE_E_ABORTED);
        }
        else
        {
            this.pxyOpSequencer.cont();
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
        this.objMutex.unlock();
//        this.outerMutex.unlock(); // DL: added
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }

        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }
    }

    @Override
    public void informOpReturn(ISLE_ConfirmedOperation poperation, long seqCount) throws SleApiException
    {
        // Sequencer
        HRESULT rc = HRESULT.S_OK;
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("seqCount : " + seqCount + "   " + poperation.getOperationType());
        }
        rc = this.pxyOpSequencer.serialise(poperation, seqCount);
        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }
//        this.outerMutex.lock(); // DL: added
        this.objMutex.lock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }

        traceOperation(poperation, " return received from the proxy");

        rc = doInformOpReturn(poperation);
        if (rc == HRESULT.SLE_E_ROLE)
        {
            String name = poperation.getOperationType().toString();
            logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_IncompatibleRtnPDU.getCode(), name);
            abort(SLE_PeerAbortDiagnostic.slePAD_protocolError);
        }

        if (rc == HRESULT.EE_E_NOSUCHEVENT)
        {
            rc = HRESULT.SLE_E_INVALIDPDU;
        }
        else if (rc == HRESULT.EE_E_REJECTED || rc == HRESULT.SLE_E_ROLE)
        {
            rc = HRESULT.S_OK;
        }

        this.pxyOpSequencer.cont();

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
        this.objMutex.unlock();
//        this.outerMutex.unlock(); // DL: added
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }

        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }
    }

    @Override
    public void pduTransmitted(ISLE_Operation poperation) throws SleApiException
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }
//        this.outerMutex.lock(); // DL: added
        this.objMutex.lock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }

        if (this.trace != null && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_medium.getCode())
        {
            if (poperation.getOperationType() == SLE_OpType.sleOT_transferBuffer)
            {
                trace(SLE_TraceLevel.sleTL_medium, "Transfer buffer transmitted");
            }
        }

        HRESULT rc = doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_PduTransmitted, poperation);

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
        this.objMutex.unlock();
//        this.outerMutex.unlock(); // DL: added
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }

        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }
    }

    @Override
    public void protocolAbort(byte[] diagnostic) throws SleApiException
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }
//        this.outerMutex.lock(); // DL: added
        this.objMutex.lock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }

        HRESULT rc = doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_ProtocolAbort, null);
        if (rc == HRESULT.S_OK)
        {
            // inform both sequencer
            this.aplOpSequencer.reset(HRESULT.SLE_E_ABORTED);
            this.pxyOpSequencer.reset(HRESULT.SLE_E_ABORTED);
            this.objMutex.unlock();
            this.aplSrvInform.protocolAbort(diagnostic);
            this.objMutex.lock();
            cleanup();
        }
        else
        {
            rc = HRESULT.E_UNEXPECTED;
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
        this.objMutex.unlock();
//        this.outerMutex.unlock(); // DL: added
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }

        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }
    }

    @Override
    public void startTrace(ISLE_Trace trace, SLE_TraceLevel level, boolean forward) throws SleApiException
    {
        if (this.trace != null)
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }
        this.objMutex.lock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }

        this.traceLevel = level;
        this.trace = trace;

        try // SLEAPIJ-32 Deadlock when stopping trace
        {
	        if (forward && this.pxySrvInit != null)
	        {
	            ISLE_TraceControl tcIf = this.pxySrvInit.queryInterface(ISLE_TraceControl.class);
	            if (tcIf != null)
	            {
	                tcIf.startTrace(trace, level, forward);
	            }
	        }
        } catch(Exception e)
        {
        	LOG.log(Level.WARNING, "Exception starting tracing: " + e.getMessage());
        }

        // it could be that no assoc is created yet, so memorise if
        // tracing has to be forwarded when a assoc is created:
        if (forward)
        {
            this.startTraceForwarded = true;
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
        this.objMutex.unlock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
    }

    @Override
    public void stopTrace() throws SleApiException
    {
        if (this.trace == null)
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }
        this.objMutex.lock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }

        this.traceLevel = SLE_TraceLevel.sleTL_low;
        this.trace = null;

        try // SLEAPIJ-32 Deadlock when stopping trace
        {
	        if (this.startTraceForwarded && this.pxySrvInit != null)
	        {
	            ISLE_TraceControl tcIf = this.pxySrvInit.queryInterface(ISLE_TraceControl.class);
	            if (tcIf != null)
	            {
	                tcIf.stopTrace();
	            }
	        }
        }
        catch(Exception e)
        {
        	LOG.log(Level.SEVERE, "Exception stopping trace:" + e.getMessage(), e);
        }
        
        this.startTraceForwarded = false;

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
        this.objMutex.unlock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
    }

    /**
     */
    @Override
    public void processTimeout(Object timer, int invocationId)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before locking " + this.objMutex.getHoldCount());
        }
        this.objMutex.lock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after locking " + this.objMutex.getHoldCount());
        }
        doProcessTimeout(timer, invocationId);
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before unlocking " + this.objMutex.getHoldCount());
        }
        this.objMutex.unlock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after unlocking " + this.objMutex.getHoldCount());
        }
    }

    /**
     */
    @Override
    public void handlerAbort(Object timer)
    {
        if (timer.equals(this.ppTimer))
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                      EE_LogMsg.EE_SE_LM_TimerAborted.getCode(),
                      "Provision period timer");
            if (this.ppTimer != null)
            {
                this.ppTimer = null;
            }
            return;
        }

        // check for return timers
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }
        this.objMutex.lock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }
        for (ReturnsPair i : this.remoteReturns)
        {
            if (i.getElapsTimer().equals(timer))
            {
                logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_TimerAborted.getCode(), "Return Timer");
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                               + this.objMutex.getHoldCount());
                }
                this.objMutex.unlock();
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                               + this.objMutex.getHoldCount());
                }
                return;
            }
        }
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
        this.objMutex.unlock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
    }

    /**
     * This function is called under lock() by ProcessTimeout(). See
     * specification of ISLE_TimeoutProcessor. If the derived class has any
     * timers to handle, it has to re-implement this function.
     */
    protected void doProcessTimeout(Object timer, int invocationId)
    {
        // check for end of provision period
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("method  in doProcessTimeout");
        }

        if (timer.equals(this.ppTimer))
        {
            ISLE_Time pcurrentTime = null;
            try
            {
                pcurrentTime = this.utilFactory.createTime(ISLE_Time.class);
            }
            catch (SleApiException e)
            {
                e.getHResult();
            }
            pcurrentTime.update();
            if (pcurrentTime.compareTo(this.stopTime) < 0)
            {
                // the PP stop time not yet reached, so restart the timer
                // with the new remaining duration
                EE_Duration newDur = new EE_Duration((long) this.stopTime.subtract(pcurrentTime));
                try
                {
                    this.ppTimer.restart(newDur, invocationId + 1);
                }
                catch (SleApiException e)
                {
                    e.getHResult();
                }
                if (this.trace != null)
                {
                    trace(SLE_TraceLevel.sleTL_low, "provision Period Timer restarted end time not yet reached");
                }
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("method  out 1 doProcessTimeout");
                }

                return;
            }

            logRecord(SLE_LogMessageType.sleLM_information, EE_LogMsg.EE_SE_LM_PpEnds.getCode());

            doStateProcessing(SLE_Component.sleCP_serviceElement, EE_TI_SLESE_Event.eeSLESE_ProvisionPeriodEnds, null);

            if (this.ppTimer != null)
            {
                this.ppTimer = null;
            }

            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("method  out 2 doProcessTimeout");
            }

            return;
        }

        // check for return timeout
        for (ReturnsPair rr : this.remoteReturns)
        {
            if (rr.getElapsTimer().equals(timer))
            {
                // abort and cleanup
                logRecord(SLE_LogMessageType.sleLM_information, EE_LogMsg.EE_SE_LM_ReturnTimerExpired.getCode());
                // print all operation information
                ISLE_ConfirmedOperation pop = rr.getIConfOperation();
                String opDump = pop.print(500);

                logRecord(SLE_LogMessageType.sleLM_alarm, 1004, "Return Timer expired", opDump);

                doStateProcessing(SLE_Component.sleCP_serviceElement, EE_TI_SLESE_Event.eeSLESE_ReturnTimeout, null);
                return;
            }
        }
    }

    /**
     * InvocationId default value set to 0
     * 
     * @param timer
     */
    protected void doProcessTimeout(EE_ElapsedTimer timer)
    {
        doProcessTimeout(timer, 0);
    }

    /**
     * Returns true if the service instance has been configured successfully by
     * a previous call to ConfigCompleted(). If true is returned, the service
     * instance is ready for operation.
     */
    public boolean isConfigured()
    {
        return this.isConfigured;
    }

    /**
     * The member function performs all service instance specific checks on the
     * BIND invocation. If any check fails, it sets the diagnostic code and
     * returns an appropriate error-code. On success, it memorises the supplied
     * interface to the proxy association. This member function is meant to be
     * called in the context of service instance location.@EndFunction
     * 
     * @throws SleApiException
     */
    public void checkBindInvocation(ISLE_Bind bindOp, ISLE_SrvProxyInitiate assocIf) throws SleApiException
    {
        // note that the checks must be done in the following sequence

        // check initiator identifier
        String bindinitId = bindOp.getInitiatorIdentifier();
        if (!(bindinitId.toLowerCase()).equals(this.peerId.toLowerCase()))
        {

            bindOp.setBindDiagnostic(SLE_BindDiagnostic.sleBD_siNotAccessibleToThisInitiator);
            logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_AccessViolation.getCode(), bindinitId);
            notify(SLE_Alarm.sleAL_accessViolation, EE_LogMsg.EE_SE_LM_AccessViolation.getCode(), bindinitId);
            throw new SleApiException(HRESULT.E_ACCESSDENIED);

        }

        // check for service type
        if (this.serviceType != bindOp.getServiceType())
        {
            bindOp.setBindDiagnostic(SLE_BindDiagnostic.sleBD_inconsistentServiceType);
            throw new SleApiException(HRESULT.SLE_E_TYPE);
        }

        // check for supported version (new for V2 of SLE API)
        int vn = bindOp.getOpVersionNumber();
        if (vn < 1 || vn > 5)
        {
            bindOp.setBindDiagnostic(SLE_BindDiagnostic.sleBD_versionNotSupported);
            throw new SleApiException(HRESULT.E_FAIL);
        }

        if (this.ppEnded)
        {
            // the user has ended the provision-period (only provider)
            // using 'end' in the Unbind operation.
            bindOp.setBindDiagnostic(SLE_BindDiagnostic.sleBD_invalidTime);
            throw new SleApiException(HRESULT.SLE_E_TIME);
        }

        // check if time is within provision period
        ISLE_Time currentTime = this.utilFactory.createTime(ISLE_Time.class);
        if (currentTime == null)
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }

        currentTime.update();
        if (!(currentTime.compareTo(this.startTime) > 0 && currentTime.compareTo(this.stopTime) < 0))
        {
            // out of provision period
            bindOp.setBindDiagnostic(SLE_BindDiagnostic.sleBD_invalidTime); // SLEAPIJ-17
            throw new SleApiException(HRESULT.SLE_E_TIME);
        }

        // check si state, must be UNBOUND
        if (this.state != SLE_SIState.sleSIS_unbound)
        {
            bindOp.setBindDiagnostic(SLE_BindDiagnostic.sleBD_alreadyBound);
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        this.pxySrvInit = assocIf;
        
        // SLEAPIJ-37 the trace is not forwarded for such case otherwise
        try
        {
        	if(this.startTraceForwarded == true && this.pxySrvInit != null)
        	{
	            ISLE_TraceControl tcIf = this.pxySrvInit.queryInterface(ISLE_TraceControl.class);
	            if (tcIf != null)
	            {
	            	tcIf.startTrace(trace, this.traceLevel, this.startTraceForwarded);
	            }
        	}
        }
        catch(Exception e)
        {
        	LOG.log(Level.SEVERE, "Exception forwarding trace level to proxy", e);
        }
        
    }

    /**
     * Aborts an active association. This member function is meant to be called
     * in the context of service instance termination, if the state of the SI is
     * not unbound.
     */
    public void abortAssoc()
    {
        // NOTE: THIS FUNCTION IS MEANT TO BE CALLED
        // BY the SERVICE-ELEMENT FROM THE APPLICATION
        // THREAD, THEREFORE lock() IS REQUIRED
        if (this.state == SLE_SIState.sleSIS_unbound)
        {
            return;
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }
        this.objMutex.lock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }

        ISLE_PeerAbort pa = null;

        try
        {
            pa = this.opFactory.createOperation(ISLE_PeerAbort.class,
                                                SLE_OpType.sleOT_peerAbort,
                                                this.serviceType,
                                                this.version);

            pa.setAbortOriginator(SLE_AbortOriginator.sleAO_serviceElement);
            pa.setPeerAbortDiagnostic(SLE_PeerAbortDiagnostic.slePAD_operationalRequirement);

            doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_PeerAbortInv, pa);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }
        finally
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                           + this.objMutex.getHoldCount());
            }
            this.objMutex.unlock();
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                           + this.objMutex.getHoldCount());
            }
        }
    }

    /**
     * Creates an association object and memories the interface returned by the
     * proxy. This function is only allowed to be called for initiating
     * applications
     * 
     * @throws SleApiException
     */
    protected HRESULT createAssoc()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Creating association for service instance " + getServiceInstanceIdentifier().getAsciiForm());
        }
        HRESULT rc = HRESULT.S_OK;
        if (this.proxy != null)
        {
            this.proxy = null;
        }

        // get proxy I/F that supports the rspPortId
        EE_APISE_Database db = EE_APISE_Database.getDb(this.instanceId);
        String protId = db.getProtocolId(this.rspPortId);
        this.proxy = EE_APISE_ServiceElement.getInstance(this.instanceId).getProxy(protId);

        if (this.proxy == null)
        {
            String msg = "No proxy available for port ";
            msg += this.rspPortId;
            logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_ConfigError.getCode(), msg);
            return HRESULT.SLE_E_CONFIG;
        }

        ISLE_AssocFactory af = this.proxy.queryInterface(ISLE_AssocFactory.class);
        if (af == null)
        {
            return HRESULT.E_FAIL;
        }

        ISLE_SrvProxyInitiate pi = null;
        ISLE_SrvProxyInform ppServiceInstance = this.queryInterface(ISLE_SrvProxyInform.class);

        try
        {
            pi = af.createAssociation(ISLE_SrvProxyInitiate.class, this.serviceType, ppServiceInstance);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            rc = e.getHResult();
            return rc;
        }

        this.pxySrvInit = pi;

        // start assoc trace if trace is on
        if (this.trace != null && this.startTraceForwarded)
        {
            ISLE_TraceControl tcIf = this.pxySrvInit.queryInterface(ISLE_TraceControl.class);
            if (tcIf != null)
            {
                try
                {
                    tcIf.startTrace(this.trace, this.traceLevel, true);
                }
                catch (SleApiException e)
                {
                    if (LOG.isLoggable(Level.FINE))
                    {
                        LOG.fine("Returned state: " + e.getHResult() + " (expected)");
                    }
                }
            }
        }

        return HRESULT.S_OK;
    }

    /**
     * Releases an association object. This function may only be called by
     * initiating applications.
     */
    protected HRESULT releaseAssoc()
    {
        ISLE_AssocFactory af = this.proxy.queryInterface(ISLE_AssocFactory.class);
        if (af == null)
        {
            return HRESULT.E_FAIL;
        }

        IUnknown iu = this.pxySrvInit.queryInterface(IUnknown.class);

        try
        {
            af.destroyAssociation(iu);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            return e.getHResult();
        }

        return HRESULT.S_OK;
    }

    /**
     * Returns a pointer to the operation factory to be used by derived classes.
     */
    protected ISLE_OperationFactory getOpFactory()
    {
        return this.opFactory;
    }

    /**
     * Returns a pointer to the utility factory to be used by derived classes.
     */
    protected ISLE_UtilFactory getUtilFactory()
    {
        return this.utilFactory;
    }

    /**
     * Performs the common set-up of the supplied operation object. Common
     * set-up is supported for the BIND and the PEER-ABORT operations.
     */
    protected void setUpOperation(SLE_OpType optype, ISLE_Operation op)
    {
        // BIND specific set-up:
        if (optype == SLE_OpType.sleOT_bind)
        {
            ISLE_Bind b = (ISLE_Bind) op;
            b.setResponderIdentifier(this.peerId);
            b.setResponderPortIdentifier(this.rspPortId);
            b.setServiceInstanceId(this.sii);
            b.setServiceType(this.serviceType);
            b.setVersionNumber(this.version);
        }
        else
        {
            // PEER-ABORT specific set-up
            if (optype == SLE_OpType.sleOT_peerAbort)
            {
                ISLE_PeerAbort pa = (ISLE_PeerAbort) op;
                pa.setAbortOriginator(SLE_AbortOriginator.sleAO_application);
            }
        }
    }

    /**
     * The member function performs all checks on service instance configuration
     * parameters for completeness and consistency. See also specification of
     * the ConfigCompleted() method of the interface ISLE_SIAdmin and the
     * configuratoin specifications of the service-type specific supplements to
     * [SLE-API].Implementation Note: The base-class (ServiceInstance) first
     * calls doConfigCompleted() and then it performs port-registration if the
     * called function returns S_OK. Derived implementations of this function
     * shall first cal the base-class and then do its own checks. If the call to
     * the base-class does not succeed, it returns without performing the
     * specific checks.
     */
    protected HRESULT doConfigCompleted()
    {
        // check for user initiated binding
        if (this.bindInitiative == SLE_AppRole.sleAR_provider)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                      EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                      "provider initiated bind not supported");
            return HRESULT.E_NOTIMPL;
        }

        // check if attributes have been set
        HRESULT configRc = HRESULT.S_OK;
        if (this.peerId == null)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                      EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                      "missing Peer Identifier");
            configRc = HRESULT.SLE_E_CONFIG;
        }

        if (this.rspPortId == null)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                      EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                      "missing Responder Port Identifier");
            configRc = HRESULT.SLE_E_CONFIG;
        }

        if (this.returnTimeout == 0)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                      EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                      "missing Return Timeout");
            configRc = HRESULT.SLE_E_CONFIG;
        }

        if (configRc != HRESULT.S_OK)
        {
            return HRESULT.SLE_E_CONFIG;
        }

        // this.sii.isNull() means actually the sii is empty.
        if (this.sii == null || this.sii.isNull())
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                      EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                      "invalid or missing SIID");
            return HRESULT.SLE_E_INVALIDID;
        }

        // check validity of sii
        String theSIID = this.sii.getAsciiForm();
        if (theSIID != null)
        {
            ISLE_SII tempSI = this.sii.copy();
            tempSI.setToNull();
            if (this.sii.getInitialFormatUsed())
            {
                tempSI.setInitialFormat();
            }
            try
            {
                tempSI.setAsciiForm(theSIID);
            }
            catch (SleApiException e)
            {
                LOG.log(Level.FINE, "SleApiException ", e);
                logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_ConfigError.getCode(), "invalid SIID");
                return HRESULT.SLE_E_INVALIDID;
            }
        }
        else
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                      EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                      "invalid or missing SIID");
            return HRESULT.SLE_E_INVALIDID;
        }

        if (!(EE_APISE_ServiceElement.getInstance(this.instanceId).isUnique(this.sii)))
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                      EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                      "SIID already exsisting");
            return HRESULT.SLE_E_INVALIDID;
        }

        // check provision period
        if (this.supportedRole == SLE_AppRole.sleAR_provider)
        {
            // ----------- NEW FOR V2 OF API -----------
            // never ending SI
            ISLE_Time pcurrentTime = null;
            if (this.startTime == null)
            {
                try
                {
                    pcurrentTime = this.utilFactory.createTime(ISLE_Time.class);
                }
                catch (SleApiException e)
                {
                    LOG.log(Level.FINE, "SleApiException ", e);
                    return HRESULT.SLE_E_CONFIG;
                }

                pcurrentTime.update();
                this.startTime = pcurrentTime;
            }

            if (this.stopTime == null)
            {
                EE_Time newStopTime = new EE_Time();
                byte[] cdsTime = this.startTime.getCDS();
                try
                {
                    newStopTime.setCDSlevel1(cdsTime);
                }
                catch (SleApiException e)
                {
                    logRecord(SLE_LogMessageType.sleLM_alarm,
                              EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                              "start time conversion failed");
                    LOG.log(Level.FINE, "SleApiException ", e);
                    return HRESULT.SLE_E_CONFIG;
                }

                EE_Duration ppDuration = new EE_Duration(86400 * 365); // in
                                                                       // seconds,
                                                                       // 1 year
                newStopTime = newStopTime.add(ppDuration);

                try
                {
                    this.stopTime = this.utilFactory.createTime(ISLE_Time.class);
                }
                catch (SleApiException e)
                {
                    LOG.log(Level.FINE, "SleApiException ", e);
                    return HRESULT.SLE_E_CONFIG;
                }

                try
                {
                    newStopTime.getCDSlevel1(cdsTime);
                    this.stopTime.setCDS(cdsTime);
                }
                catch (SleApiException e)
                {
                    logRecord(SLE_LogMessageType.sleLM_alarm,
                              EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                              "stop time calculation wrong");
                    return HRESULT.SLE_E_CONFIG;
                }
            }
            // ---------------------------------------------

            if (this.startTime == null || this.stopTime == null)
            {
                logRecord(SLE_LogMessageType.sleLM_alarm,
                          EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                          "missing Start/Stop time");
                return HRESULT.SLE_E_TIME;
            }

            if (this.stopTime.compareTo(this.startTime) < 0)
            {
                logRecord(SLE_LogMessageType.sleLM_alarm,
                          EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                          "inconsistent Start/Stop time");
                return HRESULT.SLE_E_TIME;
            }

            ISLE_Time currentTime = null;

            try
            {
                currentTime = this.utilFactory.createTime(ISLE_Time.class);
            }
            catch (SleApiException e)
            {
                LOG.log(Level.FINE, "SleApiException ", e);
                return HRESULT.SLE_E_CONFIG;
            }

            currentTime.update();

            // check if stop time is in the future
            if (currentTime.compareTo(this.stopTime) > 0)
            {
                logRecord(SLE_LogMessageType.sleLM_alarm,
                          EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                          "Stop time " + this.stopTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfYear) 
                          		+ "already expired compared to now: " + 
                        		  currentTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfYear));
                return HRESULT.SLE_E_TIME;
            }
        } // end provider

        // check if responder-port is defined in the db
        // this is needed for
        // a) port registration for a responding SI.
        // b) to find the correct proxy for a BIND initiative

        EE_APISE_Database db = EE_APISE_Database.getDb(this.instanceId);
        String protId = db.getProtocolId(this.rspPortId);
        if (protId == null)
        {
            String msg = "no such Responder Port Identifier: ";
            msg += this.rspPortId;
            logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_ConfigError.getCode(), msg);
            return HRESULT.SLE_E_PORT;
        }

        return HRESULT.S_OK;
    }

    /**
     * The function performs a state transition to the new state supplied as
     * input argument. The function reports every state-change to the
     * application on the reporter interface. If tracing is on, the state-change
     * is also passed to the tracing interface.
     */
    protected void stateTransition(SLE_SIState newState)
    {
        if (this.state != newState)
        {
            String oldStateS = this.state.toString();
            this.state = newState;
            String newStateS = this.state.toString();

            // no log for a state transition

            if (this.trace != null)
            {
                String traceMsg = "State transition from ";
                traceMsg += oldStateS;
                traceMsg += " to ";
                traceMsg += newStateS;
                trace(SLE_TraceLevel.sleTL_low, traceMsg);
            }
        }
    }

    /**
     * Forwards the logging information to the application.
     */
    public void logRecord(SLE_LogMessageType msgType, long msgId, String... p)
    {
        if (this.reporter == null)
        {
            return;
        }

        String theMsg = EE_MessageRepository.getMessage(msgId, p);
        this.reporter.logRecord(SLE_Component.sleCP_serviceElement, this.sii, msgType, msgId, theMsg);
    }

    /**
     * Forwards the notification to the application via the interface
     * ISLE_Reporter.
     */
    protected void notify(SLE_Alarm alarm, long msgId, String... p)
    {
        if (this.reporter == null)
        {
            return;
        }

        String theMsg = EE_MessageRepository.getMessage(msgId, p);
        this.reporter.notify(alarm, SLE_Component.sleCP_serviceElement, this.sii, msgId, theMsg);
    }

    /**
     * Forwards the tracing information to the application if tracing is on and
     * if the supplied tracing level is compatible with the tracing-level
     * requested.
     */
    protected void trace(SLE_TraceLevel level, String text)
    {
        if (this.trace == null)
        {
            return;
        }

        if (level.getCode() <= this.traceLevel.getCode())
        {
            this.trace.traceRecord(level, SLE_Component.sleCP_serviceElement, this.sii, text);
        }
    }

    /**
     * Generates and initializes a PEER-ABORT operation and performs
     * state-processing, which passes the PDU to the proxy and to the
     * application. Subsequently a cleanup of the service instance is performed.
     * This function shall be called whenever an internal peer-abort has to be
     * initiated.
     */
    protected void abort(SLE_PeerAbortDiagnostic diagnostic)
    {
        ISLE_PeerAbort pa = null;

        try
        {
            pa = this.opFactory.createOperation(ISLE_PeerAbort.class,
                                                SLE_OpType.sleOT_peerAbort,
                                                this.serviceType,
                                                this.version);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            return;
        }

        pa.setAbortOriginator(SLE_AbortOriginator.sleAO_serviceElement);
        pa.setPeerAbortDiagnostic(diagnostic);

        doStateProcessing(SLE_Component.sleCP_serviceElement, EE_TI_SLESE_Event.eeSLESE_PeerAbortInv, pa);
    }

    /**
     * Performs a cleanup of the service instance internal data. The base-class
     * implementation clears all remote and local pending returns. Derived
     * classes re-implement that function if specific clean-up is required, or a
     * reset of service parameters has to be performed. The base-class has to be
     * called first.
     */
    protected void cleanup()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Running clean-up on service instance " + getServiceInstanceIdentifier().getAsciiForm());
        }

        this.invokeId = 0;
        this.aplSeqCount = 0;
        this.pxySeqCount = 0;

        // reset the service version number for a provider SI
        // done after UNBIND, PEER-ABORT and Protocol Abort.
        if (this.supportedRole == SLE_AppRole.sleAR_provider)
        {
            this.version = 0;
        }

        clearRemoteReturns();
        clearLocalReturns();
    }

    /**
     * Checks the operation arguments for completeness, consistency and range
     * (VerifyInvocationArguments()). If the supplied operation object is a
     * return-PDU, a check for duplicate invocation identifiers is performed. If
     * the argument <sendReturn> is true, for a confirmed operation the
     * return-PDU is sent if any error is detected; for an unconfirmed PDU an
     * PEER-ABORT is performed if any check fails.
     */
    protected HRESULT checkInformOpInvoke(ISLE_Operation poperation, boolean sendReturn)
    {

        if (poperation.getOpServiceType() != this.serviceType)
        {
            return HRESULT.SLE_E_INVALIDPDU;
        }

        // check for duplicate invocation identifiers
        // (but not for BIND and UNBIND invocations)

        SLE_OpType optype = poperation.getOperationType();
        if (poperation.isConfirmed())
        {
            ISLE_ConfirmedOperation cop = poperation.queryInterface(ISLE_ConfirmedOperation.class);

            if (optype != SLE_OpType.sleOT_bind && optype != SLE_OpType.sleOT_unbind)
            {
                int invId = cop.getInvokeId();
                for (ISLE_ConfirmedOperation theCop : this.localReturns)
                {
                    int localInvId = theCop.getInvokeId();
                    if (localInvId == invId)
                    {
                        // GENERATE AND SEND RETURN
                        cop.setDiagnostics(SLE_Diagnostics.sleD_duplicateInvokeId);
                        if (LOG.isLoggable(Level.FINEST))
                        {
                            LOG.finest("cop.getResult " + cop.getResult());
                        }

                        if (sendReturn)
                        {
                            initiatePxyOpRtn(cop, false);
                        }
                        return HRESULT.EE_E_REJECTED;
                    }
                }
            }// end optype

            if (optype != SLE_OpType.sleOT_bind)
            {
                try
                {
                    poperation.verifyInvocationArguments();
                }
                catch (SleApiException e)
                {
                    LOG.log(Level.SEVERE, "SleApiException " + e.getHResult() + " on verification arguments: ", e);
                    String name = optype.toString();
                    logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_InconsistentInvArgs.getCode(), name);
                    // GENERATE AND SEND RETURN
                    cop.setDiagnostics(SLE_Diagnostics.sleD_otherReason); // parameter
                                                                          // error
                    if (sendReturn)
                    {
                        initiatePxyOpRtn(cop, false);
                        return HRESULT.EE_E_REJECTED;
                    }
                }
            }
            return HRESULT.S_OK;
        } // end IsConfirmed

        // unconfirmed
        try
        {
            poperation.verifyInvocationArguments();
        }
        catch (SleApiException e)
        {
            String name = optype.toString();
            logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_InconsistentInvArgs.getCode(), name);
            if (sendReturn)
            {
                abort(SLE_PeerAbortDiagnostic.slePAD_otherReason);
            }
            // Added as return error
            return e.getHResult();
        }

        return HRESULT.S_OK;
    }

    /**
     * Starts processing of the operation invocation received from the
     * application. The base-class implementation checks the
     * invocation-arguments on completeness, consistency and range (
     * VerifyInvocationArguments() ).
     */
    protected HRESULT doInitiateOpInvoke(ISLE_Operation poperation)
    {
        if (poperation.getOpServiceType() != this.serviceType)
        {
            return HRESULT.SLE_E_INVALIDPDU;
        }

        if (this.supportedRole == SLE_AppRole.sleAR_user && poperation.getOperationType() == SLE_OpType.sleOT_bind)
        {

            ISLE_Bind ib = (ISLE_Bind) poperation;
            if (ib.getResponderIdentifier() == null)
            {
                ib.setResponderIdentifier(this.peerId);
            }
            if (ib.getResponderPortIdentifier() == null)
            {
                ib.setResponderPortIdentifier(this.rspPortId);
            }
        }

        try
        {
            poperation.verifyInvocationArguments();
        }
        catch (SleApiException e)
        {
            return e.getHResult();
        }

        return HRESULT.S_OK;
    }

    /**
     * Starts processing of the return-operation received from the application.
     * For a return-operation the base-class implementation checks that the
     * passed operation is on the list of pending local returns, and removes it
     * from the list. Furthermore the base-class implementation checks the
     * return-arguments on completeness, consistency and range (
     * VerifyReturnArguments() ).
     */
    protected HRESULT doInitiateOpReturn(ISLE_ConfirmedOperation poperation)
    {
        if (poperation.getOpServiceType() != this.serviceType)
        {
            return HRESULT.SLE_E_INVALIDPDU;
        }

        for (Iterator<ISLE_ConfirmedOperation> it = this.localReturns.iterator(); it.hasNext();)
        {
            ISLE_ConfirmedOperation cop = it.next();
            if (cop.equals(poperation))
            {
                it.remove();
                try
                {
                    poperation.verifyReturnArguments();
                    return HRESULT.S_OK;
                }
                catch (SleApiException e)
                {
                    LOG.log(Level.FINE, "SleApiException ", e);
                    return e.getHResult();
                }
            }
        }

        return HRESULT.SLE_E_UNSOLICITED;
    }

    /**
     * Starts processing of the operation invocation received from the proxy.
     * The base-class implementation checks the operation arguments for
     * completeness, consistency and range (VerifyInvocationArguments()). If the
     * supplied operation object is a return-PDU, a check for duplicate
     * invocation identifiers is performed.
     */
    protected HRESULT doInformOpInvoke(ISLE_Operation poperation)
    {
        return checkInformOpInvoke(poperation, true);
    }

    /**
     * Starts processing of the return-operation received from the proxy. For a
     * return-operation the base-class implementation performs the following
     * actions: - it checks that the passed operation is on the list of pending
     * remote returns, and removes it from the list - it checks the
     * return-arguments on completeness, consistency and range (
     * VerifyReturnArguments() ) - it cancels the return-timer
     */
    protected HRESULT doInformOpReturn(ISLE_ConfirmedOperation poperation)
    {

        if (poperation.getOpServiceType() != this.serviceType)
        {
            return HRESULT.SLE_E_INVALIDPDU;
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("on remove from list remoteReturns of size " + this.remoteReturns.size() + " "
                       + poperation.getOperationType());
        }

        for (ReturnsPair rr : this.remoteReturns)
        {
            ISLE_ConfirmedOperation cop = rr.getIConfOperation();
            EE_ElapsedTimer rt = rr.getElapsTimer();
            if (cop.equals(poperation))
            {
                int index = this.remoteReturns.indexOf(rr);

                this.remoteReturns.remove(index);
                rt.cancel();
                try
                {
                    poperation.verifyReturnArguments();
                }
                catch (SleApiException e)
                {
                    LOG.log(Level.SEVERE, "SleApiException " + e.getHResult() + "", e);
                    String name = poperation.getOperationType().toString();
                    logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_InconsistentRtnArgs.getCode(), name);
                    abort(SLE_PeerAbortDiagnostic.slePAD_otherReason);
                    return HRESULT.EE_E_REJECTED;
                }
                return HRESULT.S_OK;
            }
        }
        return HRESULT.SLE_E_UNSOLICITED;
    }

    /**
     * The function performs the last action to be taken before the transmission
     * of the operation object to the proxy. These actions include: - For
     * confirmed operations the unique invocation identifier is added to the
     * operation - Adding of confirmed operation objects to the list of pending
     * remote returns and start the return-timer - Generating the sequence-count
     * - Forwarding the operation to the proxy. If the return-code indicates
     * that the transmission-queue is full, it generates a PEER-ABORT operation.
     */
    protected HRESULT initiatePxyOpInv(ISLE_Operation poperation, boolean reportTransmission)
    {
        // insert invocation identifiers for confirmed ops
        // (but not for BIND and UNBIND invocations)
        ISLE_ConfirmedOperation cop = null;

        SLE_OpType opType = poperation.getOperationType();

        if (poperation.isConfirmed())
        {
            cop = poperation.queryInterface(ISLE_ConfirmedOperation.class);

            if (opType != SLE_OpType.sleOT_bind && opType != SLE_OpType.sleOT_unbind)
            {
                this.invokeId++; // this is made under lock
                cop.setInvokeId(this.invokeId);
            }
        } // end isConfirmed

        // inform the pxy op sequencer to reset:
        if (opType == SLE_OpType.sleOT_peerAbort)
        {
            this.pxyOpSequencer.reset(HRESULT.SLE_E_ABORTED);
        }

        this.pxySeqCount++;

        long theSeqCount = this.pxySeqCount;

        if (this.trace != null && this.traceLevel == SLE_TraceLevel.sleTL_medium)
        {
            String txt = poperation.getOperationType().toString();
            txt += " invocation is being passed to the proxy";
            trace(SLE_TraceLevel.sleTL_medium, txt);
        }

        traceOperation(poperation, " invocation is beeing passed to the proxy");

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
        this.objMutex.unlock();

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }

        HRESULT rc = HRESULT.S_OK;
        try
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Forwarding operation " + poperation.getOperationType()
                           + " to proxy with report transmission " + reportTransmission + " and sequence counter "
                           + theSeqCount);
            }
            this.pxySrvInit.initiateOpInvoke(poperation, reportTransmission, theSeqCount);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Operation " + poperation.getOperationType() + " forwarded");
            }
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            rc = e.getHResult();

        }
        finally
        {

            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("before locking " + Thread.currentThread().getId() + " objMutex="
                           + this.objMutex.getHoldCount());
            }
            this.objMutex.lock();
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("after locking " + Thread.currentThread().getId() + " objMutex="
                           + this.objMutex.getHoldCount());

            }
        }

        // SLEAPIJ-31 Only set the return timer if we are not unbound, which happens if we cannot connect for a BIND
        if (rc != HRESULT.E_FAIL && cop != null && this.state != SLE_SIState.sleSIS_unbound)
        {
            EE_ElapsedTimer et = new EE_ElapsedTimer();
            ReturnsPair rr = new ReturnsPair(cop, et);

            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("on insert on remoteReturns " + this.remoteReturns.size() + " " + cop.getOperationType());
            }

            this.remoteReturns.add(rr);
            EE_Duration tmo = new EE_Duration(this.returnTimeout);
            try
            {
                et.start(tmo, this, 0); // start return timer
            }
            catch (SleApiException e1)
            {

                LOG.log(Level.FINE, "SleApiException ", e1);
            }
        }

        if (this.trace != null && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_medium.getCode())
        {
            if (poperation.getOperationType() == SLE_OpType.sleOT_transferBuffer)
            {
                if (rc == HRESULT.SLE_S_QUEUED)
                {
                    trace(SLE_TraceLevel.sleTL_medium, "Transfer buffer queued");
                }
                else
                {
                    // transfer buffer transmitted
                    trace(SLE_TraceLevel.sleTL_medium, "Transfer buffer transmitted");
                }
            }
        }

        if (rc == HRESULT.SLE_E_OVERFLOW)
        {
            abort(SLE_PeerAbortDiagnostic.slePAD_communicationsFailure);
        }
        else if (rc == HRESULT.SLE_E_PROTOCOL)
        {
            String opS = poperation.getOperationType().toString();
            String pstateS = this.pxySrvInit.getAssocState().toString();
            logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_PxyProtocolError.getCode(), opS, pstateS);
            abort(SLE_PeerAbortDiagnostic.slePAD_protocolError);
        }

        return rc;
    }

    /**
     * The function performs the last action to be taken before the transmission
     * of the operation-return object to the proxy. These actions include: -
     * Generating the sequence-count - Forwarding the operation to the proxy. If
     * the return-code indicates that the transmission-queue is full, it
     * generates a PEER-ABORT operation.
     */
    protected HRESULT initiatePxyOpRtn(ISLE_ConfirmedOperation poperation, boolean reportTransmission)
    {
        this.pxySeqCount++;
        long theSeqCount = this.pxySeqCount;

        // If an UNBIND-RETURN is passed to the proxy, the pxy-sequencer has to
        // be
        // informed:

        SLE_OpType optype = poperation.getOperationType();
        if (optype == SLE_OpType.sleOT_unbind)
        {
            this.pxyOpSequencer.reset(HRESULT.SLE_E_UNBINDING);
            this.aplOpSequencer.reset(HRESULT.SLE_E_UNBINDING);
        }
        else if (optype == SLE_OpType.sleOT_bind)
        {
            // check if a BIND (return) has been accepted:
            if (this.supportedRole == SLE_AppRole.sleAR_provider
                && poperation.getResult() == SLE_Result.sleRES_negative && this.state == SLE_SIState.sleSIS_unbound)
            {
                this.pxyOpSequencer.reset(HRESULT.SLE_E_ABORTED);
                this.aplOpSequencer.reset(HRESULT.SLE_E_ABORTED);
                this.pxySeqCount = 0;
                this.aplSeqCount = 0;
            }
        }

        if (this.trace != null && this.traceLevel == SLE_TraceLevel.sleTL_medium)
        {
            String txt = poperation.getOperationType().toString();
            txt += " return is being passed to the proxy";
            trace(SLE_TraceLevel.sleTL_medium, txt);
        }

        traceOperation(poperation, " return is being passed to the proxy");

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
        this.objMutex.unlock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
        try
        {
            this.pxySrvInit.initiateOpReturn(poperation, reportTransmission, theSeqCount);
        }
        catch (SleApiException e)
        {
            HRESULT rc = e.getHResult();
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("initiateOpReturn result: " + rc);
            }

            if (rc == HRESULT.SLE_E_OVERFLOW)
            {
                abort(SLE_PeerAbortDiagnostic.slePAD_communicationsFailure);
            }
            else if (rc == HRESULT.SLE_E_PROTOCOL)
            {
                String opS = optype.toString();
                String pstateS = this.pxySrvInit.getAssocState().toString();
                logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_PxyProtocolError.getCode(), opS, pstateS);
                abort(SLE_PeerAbortDiagnostic.slePAD_protocolError);
            }

            return rc;
        }
        finally
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("before locking " + Thread.currentThread().getId() + " objMutex="
                           + this.objMutex.getHoldCount());
            }
            this.objMutex.lock();
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("after locking " + Thread.currentThread().getId() + " objMutex="
                           + this.objMutex.getHoldCount());
            }
        }

        return HRESULT.S_OK;
    }

    /**
     * Forwards a DiscardBuffer() request to the proxy.
     */
    protected HRESULT initiatePxyDiscardBuffer()
    {
        // lock should NOT be given up here
        try
        {
            this.pxySrvInit.discardBuffer();
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);

            HRESULT rc = e.getHResult();

            if (this.trace != null && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_medium.getCode())
            {
                trace(SLE_TraceLevel.sleTL_medium, "Requested proxy to discard buffer");
            }

            if (rc == HRESULT.SLE_S_DISCARDED)
            {
                logRecord(SLE_LogMessageType.sleLM_information, EE_LogMsg.EE_SE_LM_BufferDiscarded.getCode());
            }

            return rc;
        }

        if (this.trace != null && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_medium.getCode())
        {
            trace(SLE_TraceLevel.sleTL_medium, "Requested proxy to discard buffer");
        }

        return HRESULT.S_OK;
    }

    /**
     * The function performs the last action to be taken before the transmission
     * of the operation object to the application. These actions include: - Add
     * confirmed operation objects to the list of pending local returns -
     * Generating the sequence-count - Forwarding the operation to the
     * application.
     */
    protected HRESULT informAplOpInv(ISLE_Operation poperation)
    {
        if (poperation.isConfirmed())
        {
            ISLE_ConfirmedOperation cop = poperation.queryInterface(ISLE_ConfirmedOperation.class);
            this.localReturns.add(cop);
        }

        // If a PEER-ABORT is passed to the apl, the apl-sequencer has to be
        // informed:
        if (poperation.getOperationType() == SLE_OpType.sleOT_peerAbort)
        {
            this.aplOpSequencer.reset(HRESULT.SLE_E_ABORTED);
        }

        this.aplSeqCount++;
        long theAplSeqCount = this.aplSeqCount;

        if (this.trace != null && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_medium.getCode())
        {
            String txt = poperation.getOperationType().toString();
            txt += " invocation is beeing passed to the application ";
            trace(SLE_TraceLevel.sleTL_medium, txt);
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before unlocking before informOpInvoke" + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
        this.objMutex.unlock();
        
        // #hd# this should not be under outer lock - we are coming up from the proxy 
        // calling into the application. If this is done under lock it can deadlock with threads of the application
        // coming down  
//        this.outerMutex.unlock();
        
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after unlocking before informOpInvoke" + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
        try
        {
            this.aplSrvInform.informOpInvoke(poperation, theAplSeqCount);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            return e.getHResult();
        }
        finally
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("before locking finally" + Thread.currentThread().getId() + " objMutex="
                           + this.objMutex.getHoldCount());
            }
//            this.outerMutex.lock();  // #hd# do not call into the application under lock
            this.objMutex.lock();            
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("after locking finally" + Thread.currentThread().getId() + " objMutex="
                           + this.objMutex.getHoldCount());
            }
        }

        return HRESULT.S_OK;
    }

    /**
     * The function performs the last action to be taken before the transmission
     * of the operation-return object to the application. These actions include:
     * - Generating the sequence-count - Forwarding the operation to the
     * application.
     */
    protected HRESULT informAplOpRtn(ISLE_ConfirmedOperation poperation)
    {
        // If an UNBIND-RETURN is passed to the apl, the apl-sequencer has to be
        // informed, the pxy sequence as well:
        SLE_OpType optype = poperation.getOperationType();
        if (optype == SLE_OpType.sleOT_unbind)
        {
            this.aplOpSequencer.reset(HRESULT.SLE_E_UNBINDING);
            this.pxyOpSequencer.reset(HRESULT.SLE_E_UNBINDING);
            if (this.supportedRole == SLE_AppRole.sleAR_user && poperation.getResult() == SLE_Result.sleRES_negative
                && this.state == SLE_SIState.sleSIS_unbound)
            {
                this.pxySeqCount = 0;
            }
        }
        else if (optype == SLE_OpType.sleOT_bind)
        {
            // check if a BIND (return) has been accepted:
            if (this.supportedRole == SLE_AppRole.sleAR_user && poperation.getResult() == SLE_Result.sleRES_negative
                && this.state == SLE_SIState.sleSIS_unbound)
            {
                this.pxyOpSequencer.reset(HRESULT.SLE_E_ABORTED);
                this.aplOpSequencer.reset(HRESULT.SLE_E_ABORTED);
                this.aplSeqCount = 0;
            }
        }

        this.aplSeqCount++;

        long theAplSeqCount = this.aplSeqCount;

        if (this.trace != null && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_medium.getCode())
        {
            String txt = poperation.getOperationType().toString();
            txt += " return is being passed to the application";
            trace(SLE_TraceLevel.sleTL_medium, txt);
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
        this.objMutex.unlock();
        try
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                           + this.objMutex.getHoldCount());
            }
            this.aplSrvInform.informOpReturn(poperation, theAplSeqCount);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            return e.getHResult();
        }
        finally
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("before locking " + Thread.currentThread().getId() + " objMutex="
                           + this.objMutex.getHoldCount());
            }
            this.objMutex.lock();
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("after locking " + Thread.currentThread().getId() + " objMutex="
                           + this.objMutex.getHoldCount());
            }
        }

        return HRESULT.S_OK;
    }

    /**
     * Informs the application that the provision period ends.
     */
    protected void informAplPpEnds()
    {
        if (this.aplSrvInform == null)
        {
            return;
        }

        this.aplSrvInform.provisionPeriodEnds();

        if (this.trace != null && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_medium.getCode())
        {
            trace(SLE_TraceLevel.sleTL_medium, "Application informed about the End of Provision period");
        }
    }

    /**
     * Informs the application that the data transfer can be resumed.
     */
    protected void informAplResumeDT()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
        this.objMutex.unlock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after unlocking " + Thread.currentThread().getId() + " objMutex="
                       + this.objMutex.getHoldCount());
        }
        this.aplSrvInform.resumeDataTransfer();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }
        this.objMutex.lock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }

        if (this.trace != null && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_medium.getCode())
        {
            trace(SLE_TraceLevel.sleTL_medium, "Requested application to resume data transfer");
        }
    }

    /**
     * Performs state processing for common operations on the provider/user side
     * as specified in the state-table. The member-function performs a state
     * change if necessary, and initiates all necessary actions e.g. the
     * invocation of returns, aborting an association, etc. Note that this
     * member-function is only called after a successful pre-processing of the
     * received operation objects. Derived classes have to implement this
     * member-function for more specific state processing.
     */
    protected abstract HRESULT doStateProcessing(SLE_Component originator,
                                                 EE_TI_SLESE_Event event,
                                                 ISLE_Operation poperation);

    /**
     * The BIND invocation
     * 
     * @param poperation
     * @return
     */
    protected abstract HRESULT bindInv(ISLE_Operation poperation);

    /**
     * The BIND return
     * 
     * @param poperation
     * @return
     */
    protected abstract HRESULT bindRtn(ISLE_ConfirmedOperation poperation);

    /**
     * The UNBIND invocation
     * 
     * @param poperation
     * @return
     */
    protected abstract HRESULT unbindInv(ISLE_Operation poperation);

    /**
     * The UNBIND return
     * 
     * @param poperation
     * @return
     */
    protected abstract HRESULT unbindRtn(ISLE_ConfirmedOperation poperation);

    /**
     * The SCHEDULE-STATUS-REPORT invocation
     * 
     * @param poperation
     * @return
     */
    protected abstract HRESULT scheduleStatusReportInv(ISLE_Operation poperation);

    /**
     * The SCHEDULE-STATUS-REPORT return
     * 
     * @param poperation
     * @return
     */
    protected abstract HRESULT scheduleStatusReportRtn(ISLE_ConfirmedOperation poperation);

    /**
     * Traces the state-event supplied by the arguments if tracing is on (level
     * = medium)
     */
    protected void traceStateEvent(SLE_Component originator, EE_TI_SLESE_Event event, ISLE_Operation poperation)
    {
        if (this.trace != null && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_medium.getCode())
        {
            String ts = "Event received from ";
            ts += originator.toString();
            ts += ", ";
            ts += "Event = ";
            ts += event.toString();
            if (poperation != null)
            {
                ts += ", ";
                ts += "Op = ";
                ts += poperation.getOpServiceType().toString();
                ts += "-";
                ts += poperation.getOperationType().toString();
            }
            trace(SLE_TraceLevel.sleTL_medium, ts);
        }
    }

    /**
     * Processes the PEER-ABORT invocation received from the proxy or
     * application. The function also invokes state-processing, which performs
     * an internal cleanup.
     */
    protected HRESULT peerAbortInv(ISLE_Operation poperation, SLE_AbortOriginator originator)
    {
        SLE_Component cp = SLE_Component.sleCP_application;
        if (originator == SLE_AbortOriginator.sleAO_peer || originator == SLE_AbortOriginator.sleAO_proxy)
        {
            cp = SLE_Component.sleCP_proxy;
        }

        return doStateProcessing(cp, EE_TI_SLESE_Event.eeSLESE_PeerAbortInv, poperation);
    }

    /**
     * Clears all pending local return PDUs.
     */
    protected void clearLocalReturns()
    {
        Iterator<ISLE_ConfirmedOperation> iter = this.localReturns.listIterator();
        while (iter.hasNext())
        {
            iter.next();
            iter.remove();
        }
    }

    /**
     * Clears all pending remote return PDUs.
     */
    protected void clearRemoteReturns()
    {
        Iterator<ReturnsPair> iter = this.remoteReturns.listIterator();
        while (iter.hasNext())
        {
            ReturnsPair rr = iter.next();
            rr.getElapsTimer().cancel();
            iter.remove();
        }
    }

    /**
     * Reports the protocol error to the application including the supplied
     * argument and returns SLE_E_PROTOCLOL. This function shall be used only
     * during state-processing if a protocol error is encountered.
     * 
     * @param event
     * @param originator
     * @param siState
     * @return
     */
    protected HRESULT protocolError(EE_TI_SLESE_Event event, SLE_Component originator, SLE_SIState siState)
    {
        String p1 = event.toString();
        String p2 = originator.toString();
        String p3 = siState.toString();

        logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_ProtocolError.getCode(), p1, p2, p3);
        return HRESULT.SLE_E_PROTOCOL;
    }

    /**
     * Sets the information that the provision-period has ended.
     */
    protected void setEndOfProvisionPeriod()
    {
        this.ppEnded = true;
    }

    /**
     * Sets the service version obtained from the BIND invocation. This is only
     * needed for provider SIs, which read the version of the service from the
     * BIND invocation.
     */
    protected void setVersion(int version)
    {
        this.version = version;
    }

    /**
     * Requests the service instance to prepare for being released. The function
     * can e.g. perform port deregistration or can destroy the corresponding
     * association, if applicable. The client shall call this function before
     * Release() is called. The baseclass implementation performs port
     * deregistration in the responder role.
     */
    public void prepareRelease()
    {
        if (this.supportedRole == SLE_AppRole.sleAR_provider && this.rspPortId != null)
        {
            // port de-registration for a responding SI.
            EE_APISE_Database db = EE_APISE_Database.getDb(this.instanceId);
            String protId = db.getProtocolId(this.rspPortId);
            IUnknown iup = EE_APISE_ServiceElement.getInstance(this.instanceId).getProxy(protId);
            if (iup != null)
            {
                ISLE_ProxyAdmin pa = iup.queryInterface(ISLE_ProxyAdmin.class);
                if (pa != null)
                {
                    try
                    {
                        pa.deregisterPort(this.portRegId);
                    }
                    catch (SleApiException e)
                    {
                        LOG.log(Level.FINE, "SleApiException ", e);
                    }
                }
            }
        }

        if (this.ppTimer != null)
        {
            this.ppTimer.cancel();
            this.ppTimer = null;
        }

        if (this.peerId != null)
        {
            this.peerId = null;
        }

        if (this.rspPortId != null)
        {
            this.rspPortId = null;
        }

        if (this.startTime != null)
        {
            this.startTime = null;
        }

        if (this.stopTime != null)
        {
            this.stopTime = null;
        }

        if (this.aplSrvInform != null)
        {
            this.aplSrvInform = null;
        }

        if (this.pxySrvInit != null)
        {
            this.pxySrvInit = null;
        }

        if (this.proxy != null)
        {
            this.proxy = null;
        }

        if (this.reporter != null)
        {
            this.reporter = null;
        }
    }

    /**
     * Traces the reception of an operation invocation/return. This function
     * shall be used when an incoming operation is to be traced. Trace output is
     * only produced for medium and high trace levels.
     */
    protected void traceOperation(ISLE_Operation poperation, String txt)
    {
        if (this.trace != null && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_medium.getCode())
        {
            String aText = poperation.getOperationType().toString();
            aText += " ";
            aText += txt;
            trace(SLE_TraceLevel.sleTL_medium, aText);
            if (this.traceLevel.getCode() > SLE_TraceLevel.sleTL_medium.getCode())
            {
                EE_APISE_Database db = EE_APISE_Database.getDb(this.instanceId);
                int maxTraceLength = db.getMaxTraceLength();
                String opS = poperation.print(maxTraceLength);
                trace(SLE_TraceLevel.sleTL_high, opS);
            }
        }
    }

    /**
     * @return
     */
    public boolean getPpEnded()
    {
        return this.ppEnded;
    }

    /**
     * @param value
     */
    public void setPpEnded(boolean value)
    {
        this.ppEnded = value;
    }
}
