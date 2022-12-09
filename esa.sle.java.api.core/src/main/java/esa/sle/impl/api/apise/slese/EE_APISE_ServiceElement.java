package esa.sle.impl.api.apise.slese;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.icc.ISLE_Concurrent;
import ccsds.sle.api.isle.icc.ISLE_TraceControl;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.ipx.ISLE_ProxyAdmin;
import ccsds.sle.api.isle.ipx.ISLE_SrvProxyInitiate;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_Locator;
import ccsds.sle.api.isle.ise.ISLE_SEAdmin;
import ccsds.sle.api.isle.ise.ISLE_SIFactory;
import ccsds.sle.api.isle.ise.ISLE_SrvProxyInform;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_BindDiagnostic;
import ccsds.sle.api.isle.it.SLE_BindRole;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_SIState;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.api.apise.cltuse.EE_APISE_CLTU_PFSI;
import esa.sle.impl.api.apise.cltuse.EE_APISE_CLTU_UFSI;
import esa.sle.impl.api.apise.fspse.EE_APISE_FSP_PFSI;
import esa.sle.impl.api.apise.fspse.EE_APISE_FSP_UFSI;
import esa.sle.impl.api.apise.rafse.EE_APISE_RAF_PRSI;
import esa.sle.impl.api.apise.rafse.EE_APISE_RAF_URSI;
import esa.sle.impl.api.apise.rcfse.EE_APISE_RCF_PRSI;
import esa.sle.impl.api.apise.rcfse.EE_APISE_RCF_URSI;
import esa.sle.impl.api.apise.rocfse.EE_APISE_ROCF_PRSI;
import esa.sle.impl.api.apise.rocfse.EE_APISE_ROCF_URSI;
import esa.sle.impl.ifs.gen.EE_LogMsg;
import esa.sle.impl.ifs.gen.EE_MessageRepository;

/**
 * The Service Element class implements the interfaces exported by the component
 * class API Service Element, defined in reference [SLE-API]. It is responsible
 * for configuration and initialization of the API Service Element component
 * control of the used proxies management of service instances location of
 * service instances logging, notification and the production of diagnostic
 * traces. After a successful configuration, which is done by a call to
 * ISLE_SEAdmin::Configure), the component must be given the Proxy to use. This
 * is done by AddProxy() using ISLE_SEAdmin. When processing is started via the
 * ISLE_Concurrent interface, the SE also starts processing of all linked Proxy
 * components. After processing is started, the SE is ready for creation of
 * Service instances (ISLE_SIFactory) and further on for receiving operation
 * invocations either from the application or from the Proxy. Note also that
 * those member functions, which change or access private data perform a
 * lock()/unlock() on object level in order to provide MT-safe behaviour.
 */
public class EE_APISE_ServiceElement implements ISLE_SEAdmin, ISLE_TraceControl, ISLE_Concurrent, ISLE_SIFactory,
                                    ISLE_Locator
{
    static private Logger LOG = Logger.getLogger(EE_APISE_ServiceElement.class.getName());

    /**
     * The role of the SLE Application, the Service Element supports.
     */
    private SLE_AppRole role;

    /**
     * The utility factory interface to be used within the service element.
     */
    private ISLE_UtilFactory utilFactory;

    /**
     * The operation factory interface to be used within the service element.
     */
    private ISLE_OperationFactory opFactory;

    /**
     * The interface to the reporter to be used within the service element.
     */
    private ISLE_Reporter reporter;

    /**
     * The tracing interface to be used if trace is on.
     */
    private ISLE_Trace trace;

    /**
     * The tracing level selected in the StartTrace() call.
     */
    private SLE_TraceLevel traceLevel;

    /**
     * The information whether the StartTrace() call has been forwarded to all
     * registered proxies.
     */
    private boolean startTraceForwarded;

    /**
     * The information whether or not the service element component has been
     * started (successfully) previously.
     */
    private boolean isStarted;

    /**
     * The information whether or not the Service Element component has been
     * configured (successfully) previously by a call to Configure(). This is
     * needed to prevent from starting the Service Element before it has been
     * configured.
     */
    private boolean isConfigured;
    
    /**
     * The library instance ID.
     */
    private final String instanceId;

    private EE_APISE_Database database;

    private final LinkedList<EE_APISE_ServiceInstance> srvInstanceList;

    private Map<String, ProxyPair> proxyList;

    protected ReentrantLock objMutex;

    /**
     * The unique instance of this class. Volatile enables threads to have
     * access to the modified value of instance on memory level.
     */
    private static Map<String, EE_APISE_ServiceElement> uniqueInstanceMap = new HashMap<>();


    /**
     * This method is called once to create the EE_APISE_ServiceElement instance
     * 
     * @param source
     * @return
     */
    public static synchronized void initialiseInstance(String instanceKey)
    {
    	EE_APISE_ServiceElement uniqueInstance = uniqueInstanceMap.get(instanceKey);
        
    	if (uniqueInstance == null)
        {
            uniqueInstance = new EE_APISE_ServiceElement(instanceKey);
            uniqueInstanceMap.put(instanceKey, uniqueInstance);
        }
    }

    /**
     * This method is called every time the EE_APISE_ServiceElement instance is
     * needed
     * 
     * @return
     */
    public static synchronized EE_APISE_ServiceElement getInstance(String instanceKey)
    {
    	EE_APISE_ServiceElement uniqueInstance = uniqueInstanceMap.get(instanceKey);
    	
        if (uniqueInstance == null)
        {
            throw new IllegalStateException("The initialise method has never been called and the instance never created for instance " + instanceKey);
        }

        return uniqueInstance;
    }

    /**
     * The default constructor
     */
    private EE_APISE_ServiceElement(String instanceKey)
    {
        this.role = SLE_AppRole.sleAR_provider;
        this.utilFactory = null;
        this.opFactory = null;
        this.reporter = null;
        this.trace = null;
        this.traceLevel = SLE_TraceLevel.sleTL_low;
        this.startTraceForwarded = false;
        this.isStarted = false;
        this.isConfigured = false;
        this.database = null;
        this.srvInstanceList = new LinkedList<EE_APISE_ServiceInstance>();
        this.proxyList = new LinkedHashMap<String, ProxyPair>();
        this.objMutex = new ReentrantLock();
        this.instanceId = instanceKey;
    }

    /**
     * The copy constructor
     */
    private EE_APISE_ServiceElement(EE_APISE_ServiceElement right)
    {
        this.role = right.role;
        this.utilFactory = right.utilFactory;
        this.opFactory = right.opFactory;
        this.reporter = right.reporter;
        this.trace = right.trace;
        this.traceLevel = right.traceLevel;
        this.startTraceForwarded = right.startTraceForwarded;
        this.isStarted = right.startTraceForwarded;
        this.isConfigured = right.isConfigured;
        this.database = right.database;
        this.srvInstanceList = right.srvInstanceList;
        this.proxyList = right.proxyList;
        this.instanceId = right.instanceId;
        this.objMutex = new ReentrantLock();
    }

    /**
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_SEAdmin.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_SIFactory.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Concurrent.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Locator.class)
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

    @Override
    public void configure(String configFilePath,
                          ISLE_OperationFactory popFactory,
                          ISLE_UtilFactory putilFactory,
                          ISLE_Reporter preporter) throws SleApiException
    {
        if (this.isConfigured)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_ConfigError.getCode(), "already configured");
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("already configured");
            }
            throw new SleApiException(HRESULT.E_FAIL, "already configured");
        }

        if (configFilePath == null || popFactory == null || putilFactory == null || preporter == null)
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("something is null");
            }
            throw new SleApiException(HRESULT.E_INVALIDARG);

        }

        this.opFactory = popFactory;
        this.utilFactory = putilFactory;
        this.reporter = preporter;

        this.database = EE_APISE_Database.getDb(this.instanceId);
        HRESULT rc = this.database.open(configFilePath);
        if (rc != HRESULT.S_OK)
        {
            this.database.close();
            this.database = null;
            if (rc == HRESULT.SLE_E_NOFILE)
            {
                logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_NoSuchFile.getCode(), configFilePath);
                throw new SleApiException(rc);
            }
            else
            {
                String aTxt = "";
                if (rc == HRESULT.E_ACCESSDENIED)
                {
                    aTxt += ": access denied";
                }
                logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_OpenDbFailed.getCode(), aTxt);
                throw new SleApiException(HRESULT.E_FAIL); // could also be a
                                                           // permission problem
            }
        }

        rc = this.database.readConfigPrms();
        if (rc != HRESULT.S_OK)
        {
            if (rc != HRESULT.SLE_E_CONFIG)
            {
                logRecord(SLE_LogMessageType.sleLM_alarm,
                          EE_LogMsg.EE_SE_LM_ParsingError.getCode(),
                          this.database.getErrorText());
            }
            else
            {
                logRecord(SLE_LogMessageType.sleLM_alarm,
                          EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                          this.database.getErrorText());
            }

            this.database.close();
            this.database = null;

            throw new SleApiException(HRESULT.SLE_E_CONFIG);
        }

        // the database readConfigPrms() also made a
        // consistency check. The database can be closed
        // after successful parameter reading
        this.database.close();

        this.role = this.database.getApplicationRole();
        this.isConfigured = true;
    }

    @Override
    public void addProxy(String protocolId, SLE_BindRole role, ISLE_ProxyAdmin pproxy) throws SleApiException
    {
        // Proxies can be added after configure()
        // and before startConcurrent()
        if (!(this.isConfigured && !this.isStarted))
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_AddPxyRejected.getCode());
            throw new SleApiException(HRESULT.E_FAIL);
        }

        // check if the protocol id is supported
        if (!this.database.isSupported(protocolId))
        {
            logRecord(SLE_LogMessageType.sleLM_information, EE_LogMsg.EE_SE_LM_ProtIdNotSupported.getCode(), protocolId);
            throw new SleApiException(HRESULT.E_FAIL);
        }

        // check for duplicate registration
        IUnknown iup = getProxy(protocolId);
        if (iup != null)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_ProtIdNotSupported.getCode(), protocolId);
            throw new SleApiException(HRESULT.SLE_E_DUPLICATE);
        }

        // CHECK ALSO BIND-ROLE AGAINST APPLICATION-ROLE :
        // The check is based on the assumption that this implementation
        // of the SE only supports user initiated binding
        HRESULT rc = HRESULT.S_OK;

        if (this.role == SLE_AppRole.sleAR_user && role == SLE_BindRole.sleBR_responder)
        {
            rc = HRESULT.E_FAIL;
        }
        else if (this.role == SLE_AppRole.sleAR_provider && role == SLE_BindRole.sleBR_initiator)
        {
            rc = HRESULT.E_FAIL;
        }

        if (rc != HRESULT.S_OK)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                      EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                      "Proxy bind role incompatible to application role");
            throw new SleApiException(rc);
        }

        IUnknown pxyIU = pproxy.queryInterface(IUnknown.class);

        if (pxyIU == null)
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }

        // Check if the SAME proxy (same interface pointer) is already
        // registered
        // for a different protocol Id:
        Iterator<Entry<String, ProxyPair>> pliter = this.proxyList.entrySet().iterator();
        while (pliter.hasNext())
        {
            Entry<String, ProxyPair> nameProxyPair = pliter.next();
            ProxyPair appNewPxy = nameProxyPair.getValue();
            IUnknown piu = appNewPxy.getIunknown();
            if (piu == pxyIU)
            {
                logRecord(SLE_LogMessageType.sleLM_alarm,
                          EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                          "Same proxy already registered for different protocol id");
                throw new SleApiException(HRESULT.E_FAIL);
            }
        }

        ProxyPair newPxy = new ProxyPair(pxyIU, role);

        this.proxyList.put(protocolId, newPxy);
    }

    /**
     */
    @Override
    public void shutDown() throws SleApiException
    {
        if (this.isStarted)
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        this.proxyList.clear();
        this.proxyList = null;
    }

    @Override
    public <T extends IUnknown> T createServiceInstance(Class<T> iid,
                                                        SLE_ApplicationIdentifier srvType,
                                                        int version,
                                                        SLE_AppRole role,
                                                        ISLE_ServiceInform pclientIf) throws SleApiException
    {
        if (!this.isStarted)
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }
        else if (this.role != role)
        {
            throw new SleApiException(HRESULT.E_NOTIMPL);
        }

        if (role == SLE_AppRole.sleAR_user && version == 0)
        {
            throw new SleApiException(HRESULT.SLE_E_INVALIDID);
        }
        else if (role == SLE_AppRole.sleAR_user && version > 5)
        {
            throw new SleApiException(HRESULT.E_NOTIMPL);
        }

        EE_APISE_ServiceInstance si = null;

        switch (srvType)
        {
        case sleAI_rtnAllFrames:
        {
            if (role == SLE_AppRole.sleAR_provider)
            {
                si = new EE_APISE_RAF_PRSI(this.instanceId, pclientIf);
            }
            else if (role == SLE_AppRole.sleAR_user)
            {
                si = new EE_APISE_RAF_URSI(this.instanceId, pclientIf);
            }
            break;
        }
        case sleAI_rtnChFrames:
        {
            if (role == SLE_AppRole.sleAR_provider)
            {
                si = new EE_APISE_RCF_PRSI(this.instanceId, pclientIf);
            }
            else if (role == SLE_AppRole.sleAR_user)
            {
                si = new EE_APISE_RCF_URSI(this.instanceId, pclientIf);
            }
            break;
        }
        case sleAI_rtnChOcf:
        {
            if (role == SLE_AppRole.sleAR_provider)
            {
                si = new EE_APISE_ROCF_PRSI(this.instanceId, pclientIf);
            }
            else if (role == SLE_AppRole.sleAR_user)
            {
                si = new EE_APISE_ROCF_URSI(this.instanceId, pclientIf);
            }
            break;
        }
        case sleAI_fwdTcSpacePkt:
        {
            if (role == SLE_AppRole.sleAR_provider)
            {
                si = new EE_APISE_FSP_PFSI(this.instanceId, pclientIf);
            }
            else if (role == SLE_AppRole.sleAR_user)
            {
                si = new EE_APISE_FSP_UFSI(this.instanceId, pclientIf);
            }
            break;
        }
        case sleAI_fwdCltu:
        {
            if (role == SLE_AppRole.sleAR_provider)
            {
                si = new EE_APISE_CLTU_PFSI(this.instanceId, pclientIf);
            }
            else if (role == SLE_AppRole.sleAR_user)
            {
                si = new EE_APISE_CLTU_UFSI(this.instanceId, pclientIf);
            }
            break;
        }
        default:
        {
            throw new SleApiException(HRESULT.E_NOTIMPL);
        }
        }

        if (si == null)
        {
            throw new SleApiException(HRESULT.E_NOTIMPL);
        }

        si.initialise(this.opFactory, this.utilFactory, this.reporter, version);

        if (si.queryInterface(iid) != null)
        {
            this.srvInstanceList.add(si);
            if (this.trace != null && this.startTraceForwarded)
            {
                si.startTrace(this.trace, this.traceLevel, true);
            }
        }

        return si.queryInterface(iid);
    }

    /**
     */
    @Override
    public void destroyServiceInstance(IUnknown psi) throws SleApiException
    {
        if (psi == null)
        {
            throw new SleApiException(HRESULT.SLE_E_UNKNOWN);
        }

        IUnknown ppsi = psi.queryInterface(IUnknown.class);

        if (ppsi == null)
        {
            throw new SleApiException(HRESULT.SLE_E_UNKNOWN);
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

        for (Iterator<EE_APISE_ServiceInstance> it = this.srvInstanceList.iterator(); it.hasNext();)
        {
            EE_APISE_ServiceInstance si = it.next();

            IUnknown iup = si.queryInterface(IUnknown.class);
            if (iup != null)
            {
                if (iup == ppsi)
                {
                    if (si.getSIState() != SLE_SIState.sleSIS_unbound)
                    {

                        this.objMutex.unlock();
                        throw new SleApiException(HRESULT.SLE_E_STATE);
                    }

                    si.prepareRelease();
                    it.remove();

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
        throw new SleApiException(HRESULT.SLE_E_UNKNOWN);
    }

    /**
     * See specification of ISLE_Locator. Implementation note: The
     * ServiceElement class first searches the corresponding service instance.
     * If it is found, it calls the method checkBindInvocation() on the service
     * instance object, which performs all service instance specific checks. If
     * the service instance is not found, the bind invocation is rejected with
     * the diagnostic "no such service instance".
     */
    @Override
    public ISLE_SrvProxyInform locateInstance(ISLE_SrvProxyInitiate passociation, ISLE_Bind pbindop) throws SleApiException
    {
        // note that if the SI is found in the SI-list, it must
        // have been configured before it can be used
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }
        this.objMutex.lock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after locking " + Thread.currentThread().getId() + " objMutex=" + this.objMutex.getHoldCount());
        }

        // verifyInvocationArguments() not done for a BIND op
        for (EE_APISE_ServiceInstance si : this.srvInstanceList)
        {
            // the SI is only available if it is configured
            if (si.isConfigured())
            {
                ISLE_SII sii = si.getServiceInstanceIdentifier();
                ISLE_SII bindSII = pbindop.getServiceInstanceId();

                if (sii.equals(bindSII))
                {
                    try
                    {
                        si.checkBindInvocation(pbindop, passociation);
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
                        throw new SleApiException(e.getHResult());
                    }

                    ISLE_SrvProxyInform ppServiceInstance = si.queryInterface(ISLE_SrvProxyInform.class);
                    if (ppServiceInstance == null)
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
                        throw new SleApiException(HRESULT.E_FAIL);
                    }
                    else
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
                        return ppServiceInstance;
                    }
                }
            } // is configured
        } // end iteration

        pbindop.setBindDiagnostic(SLE_BindDiagnostic.sleBD_noSuchServiceInstance);
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
        throw new SleApiException(HRESULT.SLE_E_UNKNOWN);
    }

    @Override
    public void startConcurrent() throws SleApiException
    {
        if (this.isStarted)
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }
        else if (!this.isConfigured)
        {
            throw new SleApiException(HRESULT.SLE_E_CONFIG);
        }

        // check if all configured protocol identifiers are
        // supported by registered proxies:
        List<EE_APISE_PeerData> pd = this.database.getPeerData();
        if (pd == null)
        {
            throw new SleApiException(HRESULT.SLE_E_CONFIG);
        }

        for (EE_APISE_PeerData peerData : pd)
        {
            String protocolId = peerData.getProtocolId();
            IUnknown pxy = getProxy(protocolId);
            if (pxy == null)
            {
                logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_ProxyNotRegistered.getCode(), protocolId);
                throw new SleApiException(HRESULT.SLE_E_CONFIG);
            }
        }

        // start all registered proxies
        HRESULT startRc = HRESULT.S_OK;
        int numProxiesStarted = 0;

        Iterator<Entry<String, ProxyPair>> pliter = this.proxyList.entrySet().iterator();
        while (pliter.hasNext())
        {
            Entry<String, ProxyPair> namePair = pliter.next();
            String protocolId = namePair.getKey();
            ProxyPair newPxy = namePair.getValue();

            IUnknown piu = newPxy.getIunknown();
            ISLE_Concurrent concurrentIf = null;
            concurrentIf = piu.queryInterface(ISLE_Concurrent.class);

            if (concurrentIf != null)
            {
                try
                {
                    concurrentIf.startConcurrent();
                    numProxiesStarted++;
                }
                catch (SleApiException e)
                {
                    startRc = HRESULT.SLE_S_DEGRADED;
                    logRecord(SLE_LogMessageType.sleLM_information,
                              EE_LogMsg.EE_SE_LM_ProxyNotStarted.getCode(),
                              protocolId);
                }

            }

        } // end iteration over all proxies

        if (numProxiesStarted >= 1)
        {
            this.isStarted = true;
        }
        else
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_NoProxyStarted.getCode());
            throw new SleApiException(HRESULT.E_FAIL);
        }

        if (startRc != HRESULT.S_OK)
        {
            throw new SleApiException(startRc);
        }
    }

    @Override
    public void terminateConcurrent() throws SleApiException
    {
        EE_APISE_ServiceInstance si = null;

        if (!this.isStarted)
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        while (!this.srvInstanceList.isEmpty())
        {
            si = this.srvInstanceList.removeFirst();
            if (si.getSIState() != SLE_SIState.sleSIS_unbound)
            {
                si.abortAssoc();
            }
        }

        // terminate all registered proxies
        Iterator<Entry<String, ProxyPair>> pliter = this.proxyList.entrySet().iterator();
        while (pliter.hasNext())
        {
            Entry<String, ProxyPair> namePair = pliter.next();
            ProxyPair newPxy = namePair.getValue();

            IUnknown piu = newPxy.getIunknown();
            ISLE_Concurrent concurrentIf = piu.queryInterface(ISLE_Concurrent.class);
            if (concurrentIf != null)
            {
                concurrentIf.terminateConcurrent();
            }
        } // end iteration over all proxies

        this.isStarted = false;
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

        this.trace = trace;
        this.traceLevel = level;

        for (EE_APISE_ServiceInstance si : this.srvInstanceList)
        {
            si.startTrace(trace, level, forward);
        }

        if (forward)
        {
            // forward to all registered proxies
            Iterator<Entry<String, ProxyPair>> pliter = this.proxyList.entrySet().iterator();
            while (pliter.hasNext())
            {
                Entry<String, ProxyPair> namePair = pliter.next();
                ProxyPair newPxy = namePair.getValue();
                IUnknown piu = newPxy.getIunknown();

                ISLE_TraceControl tcIf = piu.queryInterface(ISLE_TraceControl.class);
                if (tcIf != null)
                {
                    tcIf.startTrace(trace, level, forward);
                    this.startTraceForwarded = true;
                }
            } // end iteration over all proxies
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
            throw new SleApiException(HRESULT.SLE_E_STATE); // already stopped
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

        for (EE_APISE_ServiceInstance si : this.srvInstanceList)
        {
            si.stopTrace();
        }

        // stop tracing on the proxy only if it was started from here with
        // forward set to true

        if (this.startTraceForwarded)
        {
            // forward to all registered proxies
            Iterator<Entry<String, ProxyPair>> pliter = this.proxyList.entrySet().iterator();
            while (pliter.hasNext())
            {
                Entry<String, ProxyPair> namePair = pliter.next();
                ProxyPair newPxy = namePair.getValue();
                IUnknown piu = newPxy.getIunknown();

                ISLE_TraceControl tcIf = piu.queryInterface(ISLE_TraceControl.class);
                if (tcIf != null)
                {
                    tcIf.stopTrace();
                }
            } // end iteration over all proxies
        }

        this.startTraceForwarded = false;
        this.traceLevel = SLE_TraceLevel.sleTL_low;

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
     * Returns a pointer to the IUnknown interface of the proxy component that
     * corresponds to the supplied protocol identifier. If the corresponding
     * proxy component is not registered at the service element, 0 is returned.
     */

    public IUnknown getProxy(String protocolIdent)
    {
        if (protocolIdent == null)
        {
            return null;
        }

        Iterator<Entry<String, ProxyPair>> pliter = this.proxyList.entrySet().iterator();
        while (pliter.hasNext())
        {
            Entry<String, ProxyPair> namePair = pliter.next();
            String protocolId = namePair.getKey();

            if (protocolIdent.equals(protocolId))
            {
                ProxyPair newPxy = namePair.getValue();
                IUnknown piu = newPxy.getIunknown();
                return piu;
            }
        } // end iteration

        return null;
    }

    /**
     * Checks if the supplied service instance identifier is unique within the
     * service element. This function is foreseen to be called by a contained
     * service instance during a configuration check.
     */
    public boolean isUnique(ISLE_SII sii)
    {
        int num = 0;
        for (EE_APISE_ServiceInstance si : this.srvInstanceList)
        {
            ISLE_SII theSII = si.getServiceInstanceIdentifier();
            if (theSII != null)
            {
                // the comparision is only meaningful if the other
                // SIID is already configured (ConfigCompleted succeeded)
                if (sii.equals(theSII))
                {
                    num++;
                }
            }
        }
        if (num == 1)
        {
            return true;
        }

        return false;
    }

    /**
     * Reads the message corresponding to the supplied id from the message
     * repository, appends the additional text (if available) and forwards the
     * logging information to the application.
     */
    private void logRecord(SLE_LogMessageType msgType, long msgId, String... p)
    {
        if (this.reporter == null)
        {
            return;
        }
        if (p != null && p.length == 1)
        {
            String theMsg = EE_MessageRepository.getMessage(msgId, p[0]);
            this.reporter.logRecord(SLE_Component.sleCP_serviceElement, null, msgType, msgId, theMsg);
        }
    }
}
