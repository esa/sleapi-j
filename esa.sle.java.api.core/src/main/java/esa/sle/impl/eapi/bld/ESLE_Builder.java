/**
 * @(#) ESLE_Builder.java
 */

package esa.sle.impl.eapi.bld;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_TimeSource;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.ipx.ISLE_ProxyAdmin;
import ccsds.sle.api.isle.ise.ISLE_Locator;
import ccsds.sle.api.isle.ise.ISLE_SEAdmin;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_BindRole;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.api.apiop.sleop.EE_SLE_OpFactory;
import esa.sle.impl.api.apipx.pxcs.local.EE_APIPX_LocalLink;
import esa.sle.impl.api.apipx.pxcs.local.EE_APIPX_LocalMasterLink;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_Database;
import esa.sle.impl.api.apipx.pxspl.EE_APIPX_Proxy;
import esa.sle.impl.api.apise.slese.EE_APISE_ServiceElement;
import esa.sle.impl.api.apiut.EE_SLE_LibraryInstance;
import esa.sle.impl.api.apiut.EE_SLE_UtilityFactory;
import esa.sle.impl.eapi.dcw.DCW_ComponentCreator;
import esa.sle.impl.eapi.dcw.IDCW_Admin;
import esa.sle.impl.eapi.dcw.IDCW_EventQueue;
import esa.sle.impl.eapi.dcw.IDCW_SIFactory;
import esa.sle.impl.eapi.dfl.EE_DFL_DefaultLogger;
import esa.sle.impl.ifs.gen.EE_MessageRepository;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * This is a standard Java class and not an interface following the conventions
 * defined in [SLE-API], Appendix A. This class provides methods to instantiate,
 * configure and shutdown a SLE application that uses the DCW and SLE API
 * components. The ESLE_Builder is a multiton accessed via the class method
 * Get_ESLE_Builder. First the DCW and SLE components are instantiated and
 * configured by calling Initialise. Then API processing is started by calling
 * Start. The components are now ready for Service Instance creation. The
 * required Service Instances are created and configured using the
 * IDCW_SIFactory, ISLE_SIAdmin and service specific configuration interfaces.
 * Then, for a SLE user application, the application creates and invokes a BIND
 * invocation using the Service Instance's ISLE_SIOpFactory and
 * ISLE_ServiceInitiate interfaces and waits for the return using the
 * IDCW_EventQueue interface. It then continues a dialogue with the service
 * provider, using the Service Instance's ISLE_ServiceInitiate interface to send
 * invocations and returns to the service provider, and IDCW_EventQueue to
 * receive PDUs from the provider. For a SLE provider application, the
 * application waits for a BIND invocation using the IDCW_EventQueue interface,
 * processes the bind and replies using the Service Instance's
 * ISLE_ServiceInitiate interface. It then continues a dialogue with the service
 * user, using IDCW_EventQueue to receive invocations and returns from the
 * service user, and the Service Instance's ISLE_SIOpFactory and
 * ISLE_ServiceInitiate interfaces to transfer PDUs to the service user. When
 * processing is finished the application stops processing within the SLE
 * API by calling the Terminate method. This will abort and destroy any bound
 * Service Instances too. Finally the application deletes the SLE API
 * components using the Shutdown method.
 */
public class ESLE_Builder
{
    static private Logger LOG = Logger.getLogger(ESLE_Builder.class.getName());

    /**
     * The pointer to the Utility Factory.
     */
    private ISLE_UtilFactory pUtilFactory = null;

    /**
     * The singleton instance of the builder.
     */
    private static Map<String, ESLE_Builder> builderMap = new HashMap<>();

    /**
     * Indicates if the builder is already initialised.
     */
    private boolean init = false;

    private IDCW_Admin pDcwAdmin = null;

    private IDCW_EventQueue pDcwEventQueue = null;

    private IDCW_SIFactory pDcwSIFactory = null;

    /**
     * The pointer to the Operation Factory.
     */
    private ISLE_OperationFactory pOpFactory = null;

    /**
     * The pointer to the Service Element Admin.
     */
    private ISLE_SEAdmin pSleAdmin = null;

    /**
     * The pointer to the Proxy Admin.
     */
    private ISLE_ProxyAdmin pProxyAdmin = null;

    /**
     * Indicates if the builder is already started.
     */
    private boolean start = false;

    private String pxConfigFilePath = null;

    private ISLE_Reporter pReporter = null;

    private ISLE_TimeSource pTimeSource = null;

    private boolean localLinkStarted = false;

    private boolean localDflStarted = false;

    private final String instanceId;

    private ESLE_Builder(String instanceKey)
    {
    	this.instanceId = instanceKey;
        this.pUtilFactory = null;
        this.init = false;
        this.pDcwAdmin = null;
        this.pDcwEventQueue = null;
        this.pDcwSIFactory = null;
        this.pOpFactory = null;
        this.pSleAdmin = null;
        this.pProxyAdmin = null;
        this.start = false;
    }

    /**
     * Class method that return the ESLE_Builder singleton object.
     * Kept for backward compatibility
     */
    public static synchronized ESLE_Builder getESLE_Builder()
    {
    	return getESLE_Builder(EE_SLE_LibraryInstance.LIBRARY_INSTANCE_KEY);
    }
    
    /**
     * Class method that return the ESLE_Builder singleton object.
     */
    public static synchronized ESLE_Builder getESLE_Builder(String instanceKey)
    {
    	ESLE_Builder builder = builderMap.get(instanceKey);
        if (builder == null)
        {
            builder = new ESLE_Builder(instanceKey);
            builderMap.put(instanceKey, builder);
        }
        return builder;
    }

    private void logRecord(SLE_LogMessageType msgType, long msgId, String... p)
    {
        if (this.pReporter == null)
        {
            return;
        }
        String theMsg = EE_MessageRepository.getMessage(msgId, p);
        this.pReporter.logRecord(SLE_Component.sleCP_application, null, msgType, msgId, theMsg);
    }

    /**
     * Creates and configures the Down Call Wrapper, Service Element,
     * TCP/IP Proxy, SLE Utilities and SLE Operations components. S_OK
     * initialisation and configuration completed without errors. SLE_E_NOFILE
     * configuration file not founded. SLE_E_CONFIG error or inconsistency in
     * configuration data. SLE_E_STATE already initialised. E_FAIL failure due
     * to unspecified error.
     */
    public HRESULT initialise(String SEconfigFilePath,
                              String proxyConfigFilePath,
                              ISLE_Reporter preporter,
                              SLE_BindRole bindRole,
                              ISLE_TimeSource ptimeSource)
    {

        if (this.init == true)
        {
            return HRESULT.SLE_E_STATE;
        }
        this.pxConfigFilePath = proxyConfigFilePath;
        this.pReporter = preporter;
        this.pTimeSource = ptimeSource;
        // create the utilFactory;
        EE_SLE_UtilityFactory.initialiseInstance(this.instanceId, ptimeSource);
        EE_SLE_UtilityFactory pUtilFactory = null;
        pUtilFactory = EE_SLE_UtilityFactory.getInstance(this.instanceId);

        if (pUtilFactory == null)
        {
            logRecord(preporter, SLE_LogMessageType.sleLM_alarm, 1017, "Cannot create the Utility Factory");
            return HRESULT.E_FAIL;
        }
        setpUtilFactory(pUtilFactory);

        // create the Down Call Wrapper
        IDCW_Admin pDcwAdminLocal = DCW_ComponentCreator.createDownCallWrapper(this.instanceId, IDCW_Admin.class);
        if (pDcwAdminLocal == null)
        {
            logRecord(preporter, SLE_LogMessageType.sleLM_alarm, 1017, "Cannot create the Down Call Wrapper");
            return HRESULT.E_FAIL;
        }
        this.pDcwAdmin = pDcwAdminLocal;

        // create the Down Call Wrapper Event Queue
        IDCW_EventQueue pDcwEventQueueLocal = DCW_ComponentCreator.createDownCallWrapper(this.instanceId, IDCW_EventQueue.class);
        if (pDcwEventQueueLocal == null)
        {
            logRecord(preporter,
                      SLE_LogMessageType.sleLM_alarm,
                      1017,
                      "Cannot create the Down Call Wrapper Event Queue");
            return HRESULT.E_FAIL;
        }
        this.pDcwEventQueue = pDcwEventQueueLocal;

        // create the Down Call Wrapper SI Factory
        IDCW_SIFactory pDcwSIFactoryLocal = DCW_ComponentCreator.createDownCallWrapper(this.instanceId, IDCW_SIFactory.class);
        if (pDcwSIFactoryLocal == null)
        {
            logRecord(preporter, SLE_LogMessageType.sleLM_alarm, 1017, "Cannot create the Down Call Wrapper SI Factory");
            return HRESULT.E_FAIL;
        }
        this.pDcwSIFactory = pDcwSIFactoryLocal;

        // create the Operation Factory
        EE_SLE_OpFactory.initialiseInstance(this.instanceId, preporter);
        EE_SLE_OpFactory opFac = EE_SLE_OpFactory.getInstance(this.instanceId);
        if (opFac == null)
        {
            logRecord(preporter, SLE_LogMessageType.sleLM_alarm, 1017, "Cannot create the Operation Factory");
            return HRESULT.E_FAIL;
        }
        this.pOpFactory = opFac;

        // create the Service Element Admin
        EE_APISE_ServiceElement.initialiseInstance(this.instanceId);
        EE_APISE_ServiceElement serviceElement = EE_APISE_ServiceElement.getInstance(this.instanceId);
        if (serviceElement == null)
        {
            logRecord(preporter, SLE_LogMessageType.sleLM_alarm, 1017, "Cannot create the Service Element");
            return HRESULT.E_FAIL;
        }
        this.pSleAdmin = serviceElement;

        // create the Proxy
        EE_APIPX_Proxy.initialiseInstance(this.instanceId);
        EE_APIPX_Proxy proxyAdmin = EE_APIPX_Proxy.getInstance(this.instanceId);
        if (proxyAdmin == null)
        {
            logRecord(preporter, SLE_LogMessageType.sleLM_alarm, 1017, "Cannot create the Proxy");
            return HRESULT.E_FAIL;
        }
        this.pProxyAdmin = proxyAdmin;

        HRESULT res = HRESULT.S_OK;

        // configure the Down Call Wrapper
        try
        {

            this.pDcwAdmin.configure(this.pSleAdmin, pUtilFactory, preporter);
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }
        if (res != HRESULT.S_OK)
        {
            logRecord(preporter, SLE_LogMessageType.sleLM_alarm, 1017, "Cannot configure the Down Call Wrapper");
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Cannot configure the Down Call Wrapper");
            }
            return res;
        }

        // configure the Service Element Admin
        try
        {
            // proxyConfigFilePath
            this.pSleAdmin.configure(SEconfigFilePath, this.pOpFactory, pUtilFactory, preporter);
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }
        if (res != HRESULT.S_OK)
        {
            logRecord(preporter, SLE_LogMessageType.sleLM_alarm, 1017, "Cannot configure the Service Element");
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Cannot configure the Service Element " + res);
            }
            return res;
        }

        // configure the Proxy Admin
        ISLE_Locator pLocator = this.pSleAdmin.queryInterface(ISLE_Locator.class);
        if (this.pSleAdmin.queryInterface(ISLE_Locator.class) == null)
        {
            return HRESULT.E_FAIL;
        }
        res = HRESULT.S_OK;
        try
        {
            this.pProxyAdmin.configure(proxyConfigFilePath, pLocator, this.pOpFactory, pUtilFactory, preporter);
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }
        if (res != HRESULT.S_OK)
        {
            logRecord(preporter, SLE_LogMessageType.sleLM_alarm, 1017, "Cannot configure the Proxy");
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Cannot configure the Proxy");
            }
            return res;
        }

        try
        {
            this.pSleAdmin.addProxy(this.pProxyAdmin.getProtocolId(), bindRole, this.pProxyAdmin);
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }
        if (res != HRESULT.S_OK)
        {
            logRecord(preporter, SLE_LogMessageType.sleLM_alarm, 1017, "Cannot link the Proxy and the Service Element");
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Cannot link the Proxy and the Service Element");
            }
            return res;
        }

        this.init = true;

        return HRESULT.S_OK;
    }

    /**
     * Starts processing in the SLE API. The DCW becomes ready for creation
     * of Service Instances. CodesS_OK procesing started. SLE_E_STATE not
     * initialised or already started. E_FAIL failure due to unspecified error.
     */
    public HRESULT start()
    {
        if (this.init != true)
        {
            return HRESULT.SLE_E_STATE;
        }
        if (this.start == true)
        {
            return HRESULT.SLE_E_STATE;
        }

        HRESULT res = HRESULT.S_OK;
        try
        {
            if (this.pDcwAdmin != null)
            {
                this.pDcwAdmin.start();
            }
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }
        if (res != HRESULT.S_OK)
        {
            return res;
        }

        // Start the local communication server if needed (ipc address starting
        // with "LOCAL-")
        // -------------------------------------------
        EE_APIPX_Database database = new EE_APIPX_Database();
        if ((res = database.open(this.pxConfigFilePath)) == HRESULT.S_OK)
        {
            EE_Reference<String> diag = new EE_Reference<String>();
            EE_Reference<Integer> lineNumber = new EE_Reference<Integer>();

            if ((res = database.readValues(diag, lineNumber)) == HRESULT.S_OK)
            {
                SLE_AppRole role = database.getProxySettings().getRole();
                if (role == SLE_AppRole.sleAR_provider)
                {
                    String address = database.getIPCConfigData().getServiceAddress();
                    if (address != null && EE_APIPX_LocalLink.isLocalAddress(address))
                    {
                        res = EE_APIPX_LocalMasterLink.initialise(this.instanceId, database,
                                                                  this.pUtilFactory,
                                                                  this.pOpFactory,
                                                                  this.pReporter,
                                                                  this.pTimeSource);
                        if (res != HRESULT.S_OK)
                        {
                            logRecord(SLE_LogMessageType.sleLM_alarm,
                                      1017,
                                      "Cannot start the local communication server");
                        }
                        else
                        {
                            this.localLinkStarted = true;
                            // Start the default logger as well
                            try
                            {
                                EE_DFL_DefaultLogger.initialiseInstance(this.instanceId);
                                EE_DFL_DefaultLogger.getInstance(this.instanceId).connect(database.getIPCConfigData()
                                        .getDefaultReportingAddress());
                                this.localDflStarted = true;
                            }
                            catch (SleApiException e)
                            {
                                logRecord(SLE_LogMessageType.sleLM_alarm, 1017, "Cannot start the local default logger");
                            }
                        }
                    }
                    else
                    {
                        database.close();
                    }
                }
                else
                {
                    database.close();
                }
            }
        }

        this.start = true;

        return HRESULT.S_OK;
    }

    /**
     * Stops processing in the SLE API. Service Instances are stopped and
     * released and unprocessed DCW events are deleted, but the DCW event handle
     * is still valid. CodesS_OK procesing terminated. SLE_E_STATE not
     * initialised or already terminated. E_FAIL failure due to unspecified
     * error.
     */
    public HRESULT terminate()
    {
        HRESULT res = HRESULT.S_OK;
        if (this.start == false)
        {
            return HRESULT.SLE_E_STATE;
        }
        this.start = false;
        if (this.pDcwAdmin != null)
        {
            try
            {
                this.pDcwAdmin.terminate();
            }
            catch (SleApiException e)
            {
                res = e.getHResult();
            }
        }
        if (res != HRESULT.S_OK)
        {
            return res;
        }

        // Terminate the local communication server if needed (ipc address
        // starting with "LOCAL-")
        // -------------------------------------------
        try
        {
            if (this.localDflStarted)
            {
                EE_DFL_DefaultLogger.getInstance(this.instanceId).disconnect();
            }
        }
        catch (SleApiException e)
        {
            // Ignore
        }
        if (this.localLinkStarted)
        {
            EE_APIPX_LocalMasterLink.shutdown(this.instanceId);
        }

        return HRESULT.S_OK;

    }

    /**
     * Invokes the method Shutdown on all components. Note that the client must
     * also release any interfaces it references (before or after this call) for
     * the objects to be deleted. The DCW Event Handle will be invalid after
     * Shutdown. Processing must be explicitly terminated before Shutdown is
     * called. CodesS_OK the API has been shut down. SLE_E_STATE not terminated.
     * E_FAIL failure due to unspecified error.
     */
    public HRESULT shutdown()
    {
        if (this.start == true)
        {
            return HRESULT.SLE_E_STATE;
        }

        // shutdown the Proxy
        HRESULT res = HRESULT.S_OK;
        try
        {
            if (this.pProxyAdmin != null)
            {
                this.pProxyAdmin.shutDown();
            }
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }
        if (res != HRESULT.S_OK)
        {
            return res;
        }

        // Shutdown the Service Element Admin
        res = HRESULT.S_OK;
        try
        {
            if (this.pSleAdmin != null)
            {
                this.pSleAdmin.shutDown();
            }
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }
        if (res != HRESULT.S_OK)
        {
            return res;
        }

        this.init = false;
        if (this.pUtilFactory != null)
        {
            this.pUtilFactory = null;
        }

        if (this.pOpFactory != null)
        {
            this.pOpFactory = null;
        }

        if (this.pDcwAdmin != null)
        {
            this.pDcwAdmin = null;
        }
        if (this.pDcwEventQueue != null)
        {
            this.pDcwEventQueue = null;
        }
        if (this.pDcwSIFactory != null)
        {
            this.pDcwSIFactory = null;
        }

        if (this.pProxyAdmin != null)
        {
            this.pProxyAdmin = null;
        }

        if (this.pSleAdmin != null)
        {
            this.pSleAdmin = null;
        }
        return HRESULT.S_OK;
    }

    public IDCW_SIFactory getSIFactory()
    {
        return this.pDcwSIFactory;
    }

    /**
     * Returns the SE Admin interface (or NULL if not initialised)
     */
    public ISLE_SEAdmin getSEAdmin()
    {
        return this.pSleAdmin;
    }

    /**
     * Returns the Utility Factory interface (or NULL if not initialised), used
     * for creating utility objects such as Time and Service Instance
     * Identifier.
     */
    public ISLE_UtilFactory getUtilFactory()
    {
        return this.pUtilFactory;
    }

    /**
     * Log an alarm or information message.
     */
    private void logRecord(ISLE_Reporter preporter, SLE_LogMessageType msgType, long messId, String p1)
    {
        if (preporter == null)
        {
            return;
        }
        String theMsg = EE_MessageRepository.getMessage(messId, p1);
        preporter.logRecord(SLE_Component.sleCP_application, null, msgType, messId, theMsg);
    }

    private void setpUtilFactory(ISLE_UtilFactory pUtilFactory)
    {
        this.pUtilFactory = pUtilFactory;
    }

    public IDCW_Admin getpDcwAdmin()
    {
        return this.pDcwAdmin;
    }

    public IDCW_EventQueue getEventQueue()
    {
        return this.pDcwEventQueue;
    }

    public IDCW_SIFactory getpDcwSIFactory()
    {
        return this.pDcwSIFactory;
    }

}
