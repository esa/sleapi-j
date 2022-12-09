/**
 * @(#) EE_APIPX_Proxy.java
 */

package esa.sle.impl.api.apipx.pxspl;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.icc.ISLE_Concurrent;
import ccsds.sle.api.isle.icc.ISLE_TraceControl;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.ipx.ISLE_AssocFactory;
import ccsds.sle.api.isle.ipx.ISLE_ProxyAdmin;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_Locator;
import ccsds.sle.api.isle.ise.ISLE_SrvProxyInform;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_AssocState;
import ccsds.sle.api.isle.it.SLE_BindRole;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import ccsds.sle.api.isle.iutl.ISLE_SecAttributes;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.api.apipx.pxcs.EE_APIPX_BinderPxy;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_Database;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_LocalApplData;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_ProxySettings;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_ResponderPort;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_ResponderPortList;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_SrvType;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_SrvTypeList;
import esa.sle.impl.ifs.gen.EE_MessageRepository;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class EE_APIPX_Proxy implements the interfaces exported by the component
 * class 'API Proxy' and provides its functionality defined in reference
 * [SLE-API]. It is responsible for : - startup, configuration and closedown of
 * the proxy component. - creation, initialisation and release of initiating
 * association objects. - delegation of port registration/de-registration for
 * responding applications using IEE_Binder. - generation of log records. -
 * generation of trace records that are not related to a particular association.
 * After a successful configuration, which is done by a call to
 * ISLE_ProxyAdmin::Configure, the processing is started via the ISLE_Concurrent
 * interface. After processing is started, the Proxy is ready for
 * registration/de-registration, and for creation/deletion of association.
 */
public class EE_APIPX_Proxy implements ISLE_Concurrent, ISLE_ProxyAdmin, ISLE_AssocFactory, ISLE_TraceControl
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_Proxy.class.getName());

    /**
     * Pointer to the operation factory interface.
     */
    private ISLE_OperationFactory opFactory;

    /**
     * Pointer to the ISLE_Reporter interface.
     */
    private ISLE_Reporter reporter;

    /**
     * Pointer to the ISLE_Trace interface.
     */
    private ISLE_Trace trace;

    /**
     * Pointer to the ISLE_Locator interface.
     */
    private ISLE_Locator locator;

    /**
     * Pointer to the ISLE_UtilFactory interface.
     */
    private ISLE_UtilFactory utilFactory;

    /**
     * Pointer to the IEE_Binder interface.
     */
    private IEE_Binder binder;

    /**
     * The protocol Id identifying the communication technology used by the
     * proxy component, i.e. "ISP1".
     */
    private String protocolId;

    /**
     * Indicates if the proxy has been started.
     */
    private boolean started;

    /**
     * Indicates if the traces had been started for the Proxy.
     */
    private boolean traceStarted;

    /**
     * The trace level set for tracing in the Proxy. This trace level must be
     * forwarded to new created association.
     */
    private SLE_TraceLevel traceLevel;

    /**
     * Indicates if the configuration of the proxy has already been done.
     */
    private boolean configOk;

    public LinkedList<EE_APIPX_Association> associationsList;

    public EE_APIPX_Database database;

    public ISLE_SecAttributes iSecAttr;

    private final ReentrantLock objMutex;

    private final String instanceId; 
    
    /**
     * The only instance of this class
     */
    private static Map<String, EE_APIPX_Proxy> pProxyInstanceMap = new HashMap<>();


    /**
     * This method is called once to create the EE_APIPX_Proxy instance
     */
    public static synchronized void initialiseInstance(String instanceKey)
    {
    	EE_APIPX_Proxy pProxyInstance = pProxyInstanceMap.get(instanceKey);
    	
        if (pProxyInstance == null)
        {
            pProxyInstance = new EE_APIPX_Proxy(instanceKey);
            pProxyInstanceMap.put(instanceKey, pProxyInstance);
        }
    }

    /**
     * This method is called every time the EE_APIPX_Proxy instance is needed
     * 
     * @return
     */
    public static synchronized EE_APIPX_Proxy getInstance(String instanceKey)
    {
    	EE_APIPX_Proxy pProxyInstance = pProxyInstanceMap.get(instanceKey);
    	
        if (pProxyInstance == null)
        {
            throw new IllegalStateException("The initialise method has never been called and the instance never created for instance " + instanceKey);
        }

        return pProxyInstance;
    }

    private EE_APIPX_Proxy(String instanceKey)
    {
    	this.instanceId = instanceKey;
        this.opFactory = null;
        this.reporter = null;
        this.trace = null;
        this.locator = null;
        this.utilFactory = null;
        this.binder = null;
        this.started = false;
        this.traceStarted = false;
        this.configOk = false;
        this.associationsList = new LinkedList<EE_APIPX_Association>();
        this.iSecAttr = null;
        this.database = null;
        this.objMutex = new ReentrantLock();
        this.protocolId = "ISP1";
        this.configOk = false;
    }

    /**
     * Registers the association in the association list.
     */
    public void registerAssoc(EE_APIPX_Association pAssoc)
    {
        // insert the association in the list
        this.associationsList.addFirst(pAssoc);

        // if the trace are set in the proxy, sets it also in the assoc
        if (this.traceStarted)
        {
            try
            {
                final ISLE_Trace pTrace = getTrace();
                pAssoc.startTrace(pTrace, this.traceLevel, false);
            }
            catch (SleApiException e)
            {
                LOG.log(Level.FINE, "SleApiException ", e);
            }
        }
    }

    /**
     * De-registers the associations which have the release attribute set from
     * the association list.
     */
    public void deregisterAssoc()
    {
        // check all the associations
        // if one association is release, the proxy can release it
        this.objMutex.lock();
        ListIterator<EE_APIPX_Association> li = this.associationsList.listIterator();
        while (li.hasNext())
        {
            EE_APIPX_Association pAssoc = li.next();
            if (pAssoc != null && pAssoc.getIsReleased())
            {
                li.remove();
            }
        }
        this.objMutex.unlock();
    }

    /**
     * See specification of ISLE_ProxyAdmin.
     */
    @Override
    public void configure(String configFilePath,
                          ISLE_Locator plocator,
                          ISLE_OperationFactory popFactory,
                          ISLE_UtilFactory putilFactory,
                          ISLE_Reporter preporter) throws SleApiException
    {
        this.objMutex.lock();
        HRESULT res = HRESULT.E_FAIL;

        if (configFilePath == null || plocator == null || popFactory == null || putilFactory == null
            || preporter == null)
        {
            this.objMutex.unlock();
            if (preporter != null)
            {
                // report an alarm
                String mess = EE_MessageRepository.getMessage(1016, "Invalid argument");
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("Invalid argument");
                }
                preporter.logRecord(SLE_Component.sleCP_proxy, null, SLE_LogMessageType.sleLM_alarm, 1016, mess);
            }

            throw new SleApiException(HRESULT.E_INVALIDARG);
        }

        this.opFactory = popFactory;
        this.reporter = preporter;
        this.locator = plocator;
        this.utilFactory = putilFactory;

        File configFile = new File(configFilePath);
        if (!configFile.exists() || configFile.isDirectory())
        {
            this.objMutex.unlock();
            if (this.reporter != null)
            {
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("Cannot access the Proxy configuration file");
                }
                // report an alarm
                String mess = EE_MessageRepository.getMessage(1016, "Cannot access the Proxy configuration file");
                this.reporter.logRecord(SLE_Component.sleCP_proxy, null, SLE_LogMessageType.sleLM_alarm, 1016, mess);
            }

            throw new SleApiException(HRESULT.SLE_E_NOFILE);
        }

        this.objMutex.unlock();

        // instantiate and check the database
        // report any problem

        // throw away the old database
        if (this.database != null)
        {
            this.database = null;
        }
        this.database = new EE_APIPX_Database();
        if (this.database != null)
        {
            if ((res = this.database.open(configFilePath)) == HRESULT.S_OK)
            {
                EE_Reference<String> diag = new EE_Reference<String>();
                EE_Reference<Integer> lineNumber = new EE_Reference<Integer>();

                res = this.database.readValues(diag, lineNumber);

                if (res == HRESULT.S_OK)
                {
                    // check if the responder role is supported
                    EE_APIPX_ProxySettings pProxySettings = this.database.getProxySettings();
                    if (pProxySettings.getRole() == SLE_AppRole.sleAR_provider)
                    {
                        // create a binder proxy
                        if (this.binder == null)
                        {
                            EE_APIPX_BinderPxy pBinderPxy = new EE_APIPX_BinderPxy(this.instanceId,
                            													   this,
                                                                                   this.reporter,
                                                                                   this.database,
                                                                                   this.opFactory,
                                                                                   this.utilFactory);
                            this.binder = pBinderPxy.queryInterface(IEE_Binder.class);
                            if (this.binder == null)
                            {
                                throw new SleApiException(HRESULT.E_FAIL);
                            }
                        }
                    }

                    // set the security attributes
                    if (this.iSecAttr == null)
                    {
                        ISLE_SecAttributes pIsleSecAtt = this.utilFactory.createSecAttributes(ISLE_SecAttributes.class);
                        if (pIsleSecAtt != null)
                        {
                            this.iSecAttr = pIsleSecAtt;
                            // set the user name and password
                            EE_APIPX_LocalApplData pLocalApplData = this.database.getLocalApplicationData();
                            String username = pLocalApplData.getID();
                            byte[] password = pLocalApplData.getPassword();
                            this.iSecAttr.setUserName(username);
                            this.iSecAttr.setPassword(password);
                        }
                    }

                    this.configOk = true;
                }
                else
                {
                    if (this.reporter != null)
                    {
                        if (LOG.isLoggable(Level.FINEST))
                        {
                            LOG.finest("set an alarm");
                        }
                        String line = lineNumber.getReference().toString();
                        String mess = EE_MessageRepository.getMessage(1006, line, diag.getReference(), null);
                        this.reporter.logRecord(SLE_Component.sleCP_proxy,
                                                null,
                                                SLE_LogMessageType.sleLM_alarm,
                                                1006,
                                                mess);
                    }

                    res = HRESULT.SLE_E_CONFIG;
                }

                this.database.close();
            }
            else
            {
                if (this.reporter != null)
                {
                    // report alarm
                    String mess = EE_MessageRepository.getMessage(1016, "Cannot open the database");
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("Cannot open the database");
                    }
                    this.reporter
                            .logRecord(SLE_Component.sleCP_proxy, null, SLE_LogMessageType.sleLM_alarm, 1016, mess);
                }

                res = HRESULT.SLE_E_CONFIG;
            }

            if (res != HRESULT.S_OK)
            {
                this.database = null;
                throw new SleApiException(res);
            }
        }
    }

    /**
     * See specification of ISLE_ProxyAdmin.
     */
    @Override
    public void shutDown() throws SleApiException
    {
        deregisterAssoc();
        this.objMutex.lock();

        // check if all the associations are terminated
        for (EE_APIPX_Association passoc : this.associationsList)
        {
            if (passoc.getAssocState() != SLE_AssocState.sleAST_unbound)
            {
                this.objMutex.unlock();
                throw new SleApiException(HRESULT.SLE_E_STATE);
            }
        }

        this.opFactory = null;

        this.reporter = null;

        this.trace = null;

        this.traceStarted = false;

        this.locator = null;

        this.utilFactory = null;

        this.binder = null;

        this.configOk = false;

        this.objMutex.unlock();
    }

    /**
     * See specification of ISLE_ProxyAdmin.
     */
    @Override
    public int registerPort(ISLE_SII sii, String responderPort) throws SleApiException
    {
        this.objMutex.lock();

        // check if the responder role is supported
        EE_APIPX_ProxySettings pProxySettings = this.database.getProxySettings();
        if (pProxySettings.getRole() == SLE_AppRole.sleAR_user)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.E_NOTIMPL);
        }

        // check if the responder port is part of the address mapping
        // information
        EE_APIPX_ResponderPortList pResponderPortList = this.database.getResponderPortList();
        EE_Reference<EE_APIPX_ResponderPort> pResponderPort = new EE_Reference<EE_APIPX_ResponderPort>();

        if (pResponderPortList.getResponderPort(responderPort, pResponderPort) != HRESULT.S_OK)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_E_UNKNOWN);
        }

        // check if the responder port identifier is a local port
        if (pResponderPort.getReference().getIsForeign())
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_E_INVALIDID);
        }

        EE_Reference<Integer> regId = new EE_Reference<Integer>();

        if (this.binder != null)
        {
            HRESULT res = this.binder.registerPort(sii, responderPort, regId);
            if (res != HRESULT.S_OK)
            {
                this.objMutex.unlock();
                throw new SleApiException(res);
            }
        }

        this.objMutex.unlock();

        if (regId.getReference() == null)
        {
            return -1;
        }
        else
        {
            return regId.getReference();
        }
    }

    /**
     * See specification of ISLE_ProxyAdmin.
     */
    @Override
    public void deregisterPort(int regId) throws SleApiException
    {
        this.objMutex.lock();

        // check if the responder role is supported
        EE_APIPX_ProxySettings pProxySettings = this.database.getProxySettings();
        if (pProxySettings.getRole() == SLE_AppRole.sleAR_user)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.E_NOTIMPL);
        }

        if (regId < 0)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.E_INVALIDARG);
        }

        if (this.binder != null)
        {            
            HRESULT res = this.binder.deregisterPort(regId);
            if (res != HRESULT.S_OK)
            {
                this.objMutex.unlock();
                throw new SleApiException(res);
            }
        }

        this.objMutex.unlock();
    }

    /**
     * See specification of ISLE_ProxyAdmin.
     */
    @Override
    public String getProtocolId()
    {
        return this.protocolId;
    }

    /**
     * See specification of ISLE_AssocFactory.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends IUnknown> T createAssociation(Class<T> iid,
                                                    SLE_ApplicationIdentifier srvType,
                                                    ISLE_SrvProxyInform pclientIf) throws SleApiException
    {
        this.objMutex.lock();

        if (!this.started)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        // check if the service type is a supported service type in the database
        EE_APIPX_ProxySettings pProxySettings = this.database.getProxySettings();
        SLE_AppRole sleAppRole = pProxySettings.getRole();
        if (sleAppRole == SLE_AppRole.sleAR_provider)
        {
            EE_APIPX_SrvTypeList pSrvTypeList = this.database.getSrvTypeList();
            EE_APIPX_SrvType pSrvType = pSrvTypeList.getSrvTypeByType(srvType);
            if (pSrvType == null)
            {
                this.objMutex.unlock();
                throw new SleApiException(HRESULT.E_NOTIMPL);
            }
        }

        EE_APIPX_Association pAssoc = new EE_APIPX_InitiatingAssoc(this.instanceId,
        														   this,
        														   this.database,
                                                                   this.reporter,
                                                                   this.opFactory,
                                                                   this.utilFactory);
        // try to get the interface
        if (pAssoc.queryInterface(iid) == null)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.E_NOINTERFACE);
        }

        // insert the association in the list
        registerAssoc(pAssoc);
        // set the inform interface
        pAssoc.setSrvProxyInform(pclientIf);

        this.objMutex.unlock();

        return (T) pAssoc;
    }

    /**
     * See specification of ISLE_AssocFactory.
     */
    @Override
    public void destroyAssociation(IUnknown passoc) throws SleApiException
    {
        IUnknown pUnknown1 = null;
        IUnknown pUnknown2 = null;
        HRESULT res = HRESULT.SLE_E_UNKNOWN;

        this.objMutex.lock();

        // take the iunknown interface from the given parameter
        pUnknown1 = passoc.queryInterface(IUnknown.class);
        if (pUnknown1 != null)
        {
            for (EE_APIPX_Association passocList : this.associationsList)
            {
                // take the iunknown interface from the assoc of the list
                pUnknown2 = passocList.queryInterface(IUnknown.class);
                if (pUnknown2 != null)
                {
                    if (pUnknown1.equals(pUnknown2))
                    {
                        // the association has been founded in the list
                        // check if it is a initiating association
                        if (passocList.getRole() != SLE_BindRole.sleBR_initiator)
                        {
                            res = HRESULT.SLE_E_TYPE;
                        }
                        else
                        {
                            // check if the state is bound
                            if (passocList.getAssocState() != SLE_AssocState.sleAST_unbound)
                            {
                                res = HRESULT.SLE_E_STATE;
                            }
                            else
                            {
                                passocList.releaseChannel();
                                // erase the association from the list
                                this.associationsList.remove(this.associationsList.indexOf(passocList));
                                res = HRESULT.S_OK;
                            }
                        }

                        break;
                    }
                }
            }
        }

        this.objMutex.unlock();

        if (res != HRESULT.S_OK)
        {
            throw new SleApiException(res);
        }
    }

    /**
     * See specification of ISLE_TraceControl.
     */
    @Override
    public void startTrace(ISLE_Trace ptrace, SLE_TraceLevel level, boolean forward) throws SleApiException
    {
        this.objMutex.lock();

        // check if already started
        if (this.traceStarted)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        // check the pTrace pointer
        assert (ptrace != null) : "ptrace is null";
        if (ptrace == null)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.E_INVALIDARG);
        }

        // check the trace level
        assert (level.getCode() >= SLE_TraceLevel.sleTL_low.getCode() && level.getCode() <= SLE_TraceLevel.sleTL_full
                .getCode()) : "Trace level unknown";
        if (level.getCode() < SLE_TraceLevel.sleTL_low.getCode()
            || level.getCode() > SLE_TraceLevel.sleTL_full.getCode())
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.E_INVALIDARG);
        }

        this.trace = ptrace;
        this.traceLevel = level;
        this.traceStarted = true;

        // start the trace for all the associations of the proxy
        for (EE_APIPX_Association passoc : this.associationsList)
        {
            passoc.startTrace(ptrace, level, forward);
        }

        this.objMutex.unlock();
    }

    /**
     * See specification of ISLE_TraceControl.
     */
    @Override
    public void stopTrace() throws SleApiException
    {
        this.objMutex.lock();

        if (!this.traceStarted)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        this.traceStarted = false;
        this.trace = null;

        // stop the trace for all the association of the proxy
        for (EE_APIPX_Association passoc : this.associationsList)
        {
            passoc.stopTrace();
        }

        this.objMutex.unlock();
    }

    /**
     * See specification of ISLE_Concurrent.
     * 
     * @throws SleApiException
     */
    @Override
    public void startConcurrent() throws SleApiException
    {
        this.objMutex.lock();

        if (this.started)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        if (!this.configOk)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_E_CONFIG);
        }

        this.started = true;

        this.objMutex.unlock();
    }

    /**
     * See specification of ISLE_Concurrent.
     * 
     * @throws SleApiException
     */
    @Override
    public void terminateConcurrent() throws SleApiException
    {
        this.objMutex.lock();

        if (!this.started)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        this.started = false;

        this.objMutex.unlock();
    }

    /**
     * See specification of IUnknown.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == ISLE_ProxyAdmin.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Concurrent.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_AssocFactory.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_TraceControl.class)
        {
            return (T) this;
        }
        else if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    /**
     * Gets the locator.
     */
    public ISLE_Locator getLocator()
    {
        return this.locator;
    }

    /**
     * Gets the ConfigOk attribute of the object.
     */
    public boolean getConfigOk()
    {
        return this.configOk;
    }

    public ISLE_SecAttributes getSecurityAttribures()
    {
        return this.iSecAttr;
    }

    /**
     * Checks if the trace level given as parameter is compatible with the
     * attribute traceLevel set by the StartTrace() method.
     */
    public boolean checkTraceLevel(SLE_TraceLevel traceLevel)
    {
        if (!this.traceStarted)
        {
            return false;
        }
        else
        {
            if (this.traceLevel.getCode() >= traceLevel.getCode())
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    public ISLE_OperationFactory getOpFactory()
    {
        return this.opFactory;
    }

    public void setOpFactory(ISLE_OperationFactory opFactory)
    {
        this.opFactory = opFactory;
    }

    public ISLE_Reporter getReporter()
    {
        return this.reporter;
    }

    public void setReporter(ISLE_Reporter reporter)
    {
        this.reporter = reporter;
    }

    public ISLE_Trace getTrace()
    {
        return this.trace;
    }

    public void setTrace(ISLE_Trace trace)
    {
        this.trace = trace;
    }

    public ISLE_UtilFactory getUtilFactory()
    {
        return this.utilFactory;
    }

    public void setUtilFactory(ISLE_UtilFactory utilFactory)
    {
        this.utilFactory = utilFactory;
    }

    public IEE_Binder getBinder()
    {
        return this.binder;
    }

    public void setBinder(IEE_Binder binder)
    {
        this.binder = binder;
    }

    public boolean isStarted()
    {
        return this.started;
    }

    public void setStarted(boolean started)
    {
        this.started = started;
    }

    public boolean isTraceStarted()
    {
        return this.traceStarted;
    }

    public void setTraceStarted(boolean traceStarted)
    {
        this.traceStarted = traceStarted;
    }

    public SLE_TraceLevel getTraceLevel()
    {
        return this.traceLevel;
    }

    public void setTraceLevel(SLE_TraceLevel traceLevel)
    {
        this.traceLevel = traceLevel;
    }

    public void setLocator(ISLE_Locator locator)
    {
        this.locator = locator;
    }

    public void setProtocolId(String protocolId)
    {
        this.protocolId = protocolId;
    }

    public void setConfigOk(boolean configOk)
    {
        this.configOk = configOk;
    }
}
