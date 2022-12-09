/**
 * @(#) ESLE_APIBuilder.java
 */

package esa.sle.impl.eapi.bld;

import java.util.HashMap;
import java.util.Map;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_TimeSource;
import ccsds.sle.api.isle.icc.ISLE_Concurrent;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.ipx.ISLE_ProxyAdmin;
import ccsds.sle.api.isle.ise.ISLE_Locator;
import ccsds.sle.api.isle.ise.ISLE_SEAdmin;
import ccsds.sle.api.isle.ise.ISLE_SIFactory;
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
import esa.sle.impl.eapi.dfl.EE_DFL_DefaultLogger;
import esa.sle.impl.ifs.gen.EE_MessageRepository;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * This is a standard Java class and not an interface following the conventions
 * defined in [SLE-API], Appendix A. This class provides methods to instantiate,
 * configure and shutdown a SLE application that uses the SLE API components.
 * The ESLE_APIBuilder is a multiton accessed via the class method
 * getESLE_APIBuilder(String). If no string is provided, then the internal default
 * instance key is used. First the SLE components are instantiated and configured
 * by calling Initialise. Then API processing is started by calling Start. The
 * components are now ready for Service Instance creation. Then, for a SLE user
 * application, the application creates and invokes a BIND invocation using the
 * Service Instance's ISLE_SIOpFactory and ISLE_ServiceInitiate interfaces. 
 * It then continues a dialogue with the service provider, using the Service 
 * Instance's ISLE_ServiceInitiate interface to send invocations and returns 
 * to the service provider, and IDCW_EventQueue to receive PDUs from the provider. 
 * For a SLE provider application, the application waits for a BIND invocation 
 * using the IDCW_EventQueue interface, processes the bind and replies using the 
 * Service Instance's ISLE_ServiceInitiate interface. It then continues a dialogue 
 * with the service user, using IDCW_EventQueue to receive invocations and returns
 * from the service user, and the Service Instance's ISLE_SIOpFactory and
 * ISLE_ServiceInitiate interfaces to transfer PDUs to the service user. When
 * processing is finished the application stops processing within the SLE
 * API by calling the Terminate method. This will abort and destroy any bound
 * Service Instances too. Finally the application deletes the SLE API
 * components using the Shutdown method.
 */
public class ESLE_APIBuilder
{
    /**
     * The singleton instance of the builder.
     */
    private static Map<String, ESLE_APIBuilder> apiBuilderMap = new HashMap<>();

    /**
     * The pointer to the Utility Factory.
     */
    private ISLE_UtilFactory pUtilFactory = null;

    /**
     * The pointer to the Operation Factory.
     */
    private ISLE_OperationFactory pOpFactory = null;

    /**
     * The pointer to the Service Element Admin.
     */
    private ISLE_SEAdmin pSeAdmin = null;

    private ISLE_SIFactory pSIFactory = null;

    /**
     * The pointer to the Proxy Admin.
     */
    private ISLE_ProxyAdmin pProxyAdmin = null;

    private boolean initialised = false;

    private boolean started = false;

    private ISLE_Reporter pReporter = null;

    private ISLE_TimeSource pTimeSource = null;

    private String pxConfigFilePath = null;

    private boolean localLinkStarted = false;

    private boolean localDflStarted = false;
    
    private final String instanceId;

    private void setpUtilFactory(ISLE_UtilFactory pUtilFactory)
    {
        this.pUtilFactory = pUtilFactory;
    }

    private void setpOpFactory(ISLE_OperationFactory pOpFactory)
    {
        this.pOpFactory = pOpFactory;
    }

    private void setpSeAdmin(ISLE_SEAdmin pSeAdmin)
    {
        this.pSeAdmin = pSeAdmin;
    }

    private void setpProxyAdmin(ISLE_ProxyAdmin pProxyAdmin)
    {
        this.pProxyAdmin = pProxyAdmin;
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

    private ESLE_APIBuilder(String instanceKey)
    {
    	this.instanceId = instanceKey;
        this.pUtilFactory = null;
        this.pOpFactory = null;
        this.pSeAdmin = null;
        this.pSIFactory = null;
        this.pProxyAdmin = null;
        this.initialised = false;
        this.started = false;
        this.pReporter = null;
        this.pTimeSource = null;
    }

    
    /**
     * Class method that return the ESLE_APIBuilder singleton object per instance.
     * Kept for backward compatibility.
     */
    public static synchronized ESLE_APIBuilder getESLEAPIBuilder()
    {
    	return getESLEAPIBuilder(EE_SLE_LibraryInstance.LIBRARY_INSTANCE_KEY);
    }
    
    /**
     * Class method that return the ESLE_APIBuilder singleton object per instance.
     */
    public static synchronized ESLE_APIBuilder getESLEAPIBuilder(String instanceKey)
    {
    	ESLE_APIBuilder apiBuilder = apiBuilderMap.get(instanceKey);
        if (apiBuilder == null)
        {
            apiBuilder = new ESLE_APIBuilder(instanceKey);
            apiBuilderMap.put(instanceKey, apiBuilder);
        }
        return apiBuilder;
    }

    /**
     * Returns the Operation Factory interface (or NULL if not initialised),
     * used for creating operation objects.
     */
    public final ISLE_OperationFactory getOperationFactory()
    {
        return this.pOpFactory;
    }

    /**
     * Returns the Proxy Admin interface (or NULL if not initialised), used for
     * administrating the proxy element.
     */
    public final ISLE_ProxyAdmin getProxyAdmin()
    {
        return this.pProxyAdmin;
    }

    /**
     * Returns the Service Element Admin interface (or NULL if not initialised),
     * used for administrating the service element.
     */
    public ISLE_SEAdmin getSEAdmin()
    {
        return this.pSeAdmin;

    }

    /**
     * Returns the SLE interface for creating and destroying Service Instances
     * (or NULL if not initialised). Service Instances can not be created if
     * processing has not been started.
     */
    public ISLE_SIFactory getSIFactory()
    {
        return this.pSIFactory;
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
     * Creates and configures the Service Element, TCP/IP Proxy, SLE
     * Utilities and SLE Operations components.@EndFunction CodesS_OK
     * initialisation and configuration completed without errors. SLE_E_STATE
     * already initialised. E_FAIL failure due to unspecified error. see also
     * the result codes of the following functions:
     * EE_SLE_UtilityCreator::EE_CreateUtilFactory
     * EE_SLE_OperationsCreator::EE_CreateOpFactory
     * EE_API_ServiceElementCreator::EE_CreateServiceElement
     * EE_APIPX_ProxyCreator::EE_createProxy ISLE_ProxyAdmin::Configure
     * ISLE_SEAdmin::QueryInterface ISLE_SEAdmin::Configure
     * ISLE_SEAdmin::AddProxy
     */
    public HRESULT initialise(final String SEconfigFilePath,
                              final String proxyConfigFilePath,
                              ISLE_Reporter preporter,
                              SLE_BindRole bindRole,
                              ISLE_TimeSource ptimeSource)
    {

        if (this.initialised)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, 1016, "Already initialised");
            return HRESULT.SLE_E_STATE;
        }
        this.pxConfigFilePath = proxyConfigFilePath;
        this.pReporter = preporter;
        this.pTimeSource = ptimeSource;

        // setting the pUtilFactory
        EE_SLE_UtilityFactory.initialiseInstance(this.instanceId, this.pTimeSource);
        EE_SLE_UtilityFactory utilFac = EE_SLE_UtilityFactory.getInstance(this.instanceId);
        setpUtilFactory(utilFac);

        // setting the pOpFactory
        EE_SLE_OpFactory.initialiseInstance(this.instanceId, this.pReporter);
        EE_SLE_OpFactory opFac = EE_SLE_OpFactory.getInstance(this.instanceId);
        setpOpFactory(opFac);

        // setting pSeAdmin
        EE_APISE_ServiceElement.initialiseInstance(this.instanceId);
        EE_APISE_ServiceElement serviceElement = EE_APISE_ServiceElement.getInstance(this.instanceId);
        setpSeAdmin(serviceElement);

        // setting pProxyAdmin
        EE_APIPX_Proxy.initialiseInstance(this.instanceId);
        EE_APIPX_Proxy proxyAdmin = EE_APIPX_Proxy.getInstance(this.instanceId);
        setpProxyAdmin(proxyAdmin);

        // get the Service Instance Factory interface
        // ---------------------------------------------------------------
        this.pSIFactory = this.pSeAdmin.queryInterface(ISLE_SIFactory.class);
        if (this.pSIFactory == null)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, 1016, "Cannot get the Service Instance Factory interface");
            return HRESULT.E_FAIL;
        }

        // Configure the Service Element, make the operation factory, the
        // util factory and the reporter known to the service element
        // ---------------------------------------------------------------

        HRESULT res = HRESULT.S_OK;
        try
        {
            this.pSeAdmin.configure(SEconfigFilePath, this.pOpFactory, this.pUtilFactory, this.pReporter);
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }

        if (res != HRESULT.S_OK)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, 1016, "Cannot configure the Service Element");
            return res;
        }

        ISLE_Locator iloc = this.pSeAdmin.queryInterface(ISLE_Locator.class);
        if (iloc == null)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, 1016, "Cannot get the Locator interface");
            return HRESULT.E_FAIL;
        }

        res = HRESULT.S_OK;
        try
        {
            this.pProxyAdmin.configure(proxyConfigFilePath, iloc, this.pOpFactory, this.pUtilFactory, this.pReporter);
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }
        if (res != HRESULT.S_OK)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, 1016, "Cannot configure the Proxy");
            return res;
        }

        // make the proxy known to the service element
        // -------------------------------------------
        // "ISP1" is the protocol id, which must be known
        // to the service element (SE config file: PORTLIST)
        res = HRESULT.S_OK;
        try
        {
            this.pSeAdmin.addProxy(this.pProxyAdmin.getProtocolId(), bindRole, this.pProxyAdmin);
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }
        if (res != HRESULT.S_OK)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, 1017, "Cannot link the Proxy and the Service Element");
            return res;
        }

        this.initialised = true;
        return HRESULT.S_OK;
    }

    /**
     * Starts processing in the SLE API.@EndFunction CodesS_OK procesing
     * started. SLE_E_STATE not initialised or already started. see also the
     * result codes of the following functions: ISLE_SEAdmin::QueryInterface
     * ISLE_Concurrent::StartConcurrent
     */
    public HRESULT start()
    {

        if (!this.initialised)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, 1004, "Startup error", "Not yet initialised");
            return HRESULT.SLE_E_STATE;
        }

        if (this.started)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, 1004, "Startup error", "SLE API already started");
            return HRESULT.SLE_E_STATE;
        }

        // Start operation of the SLE API
        // ------------------------------

        ISLE_Concurrent iconcurrent = this.pSeAdmin.queryInterface(ISLE_Concurrent.class);
        if (iconcurrent == null)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, 1016, "Cannot get the Concurrent interface");
            return HRESULT.E_FAIL;
        }

        HRESULT res = HRESULT.S_OK;
        try
        {
            iconcurrent.startConcurrent();
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }

        if (res != HRESULT.S_OK)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, 1016, "Cannot Start Concurrent");
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

        this.started = true;
        return HRESULT.S_OK;
    }

    /**
     * Stops processing in the SLE API. Service Instances are stopped and
     * released.@EndFunction S_OK procesing terminated. SLE_E_STATE not started.
     * see also the result codes of the following functions:
     * ISLE_SEAdmin::QueryInterface ISLE_Concurrent::TerminateConcurrent
     */
    public HRESULT terminate()
    {

        if (!this.started)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, 1004, "Termination error", "Not yet started");
            return HRESULT.SLE_E_STATE;
        }

        this.started = false;

        ISLE_Concurrent iconcurrent = this.pSeAdmin.queryInterface(ISLE_Concurrent.class);

        if (iconcurrent == null)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, 1016, "Cannot get the Concurrent interface");
            return HRESULT.E_FAIL;
        }

        HRESULT res = HRESULT.S_OK;
        try
        {
            iconcurrent.terminateConcurrent();
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }

        if (res != HRESULT.S_OK)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, 1004, "Termination Error", "Cannot Terminate Concurrent");
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
     * the objects to be deleted. CodesS_OK the API has been shut down.
     * SLE_E_STATE not terminated. E_FAIL failure due to unspecified error. see
     * also the result codes of the following functions:
     * ISLE_ProxyAdmin::ShutDown ISLE_SEAdmin::ShutDown
     */
    public HRESULT shutdown()
    {

        if (this.started)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, 1004, "Shutdown error", "SLE API already running");
            return HRESULT.SLE_E_STATE;
        }

        HRESULT res = HRESULT.S_OK;

        // shutdown the Proxy
        // -------------------------------------------------
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
            logRecord(SLE_LogMessageType.sleLM_alarm, 1004, "Shutdown error", "Cannot shut down the Proxy");
            return res;
        }

        // Shutdown the Service Element Admin
        // -------------------------------------------------
        res = HRESULT.S_OK;
        try
        {
            if (this.pSeAdmin != null)
            {
                this.pSeAdmin.shutDown();
            }
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }
        if (res != HRESULT.S_OK)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, 1004, "Shutdown error", "Cannot shut down the Service Element");
            return res;
        }

        this.initialised = false;

        // release the Utility Factory:
        // -------------------------------------------------
        if (this.pUtilFactory != null)
        {
            this.pUtilFactory = null;
        }

        // release the Operation Factory:
        // -------------------------------------------------
        if (this.pOpFactory != null)
        {
            this.pOpFactory = null;
        }

        // release the Service Instance Factory
        // -------------------------------------------------
        if (this.pSIFactory != null)
        {
            this.pSIFactory = null;
        }

        // release the Proxy:
        // -------------------------------------------------
        if (this.pProxyAdmin != null)
        {
            this.pProxyAdmin = null;
        }

        // release the Service Element:
        // -------------------------------------------------
        if (this.pSeAdmin != null)
        {
            this.pSeAdmin = null;
        }

        return HRESULT.S_OK;

    }

}
