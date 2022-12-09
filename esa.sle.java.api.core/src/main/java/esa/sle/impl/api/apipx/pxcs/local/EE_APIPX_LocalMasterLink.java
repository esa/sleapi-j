package esa.sle.impl.api.apipx.pxcs.local;

import java.util.HashMap;
import java.util.Map;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_TimeSource;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.api.apipx.pxcs.EE_APIPX_Binder;
import esa.sle.impl.api.apipx.pxcs.EE_APIPX_ReportTrace;
import esa.sle.impl.api.apipx.pxcs.EE_APIPX_ReportTracePxy;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_Database;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_IPCConfig;
import esa.sle.impl.api.apipx.pxtml.EE_APIPX_Listener;

/**
 * The class resides only in the application process. It creates a thread which
 * listens on internal requests and creates a new link object (class
 * EE_APIPX_LocalLink) for each new request. Furthermore, the class manages a
 * list of created links for cleanup, and it instantiates the binder and
 * reportTrace singleton.
 */
public class EE_APIPX_LocalMasterLink
{

	private static Map<String, EE_APIPX_LocalWaitingCnx> waitCnxApplMap = new HashMap<>();

    private static Map<String, EE_APIPX_LocalWaitingCnx> waitCnxDflMap  = new HashMap<>();

    private static Map<String, EE_APIPX_Database> eeAPIPXDatabaseMap  = new HashMap<>();


    /**
     * Initialize the communication server. S_OK Waiting for connection. E_FAIL
     * Unable to wait for incoming IPC connection due to a further unspecified
     * error.
     */
    public static synchronized HRESULT initialise(String instanceKey,
    								 EE_APIPX_Database database,
                                     ISLE_UtilFactory pUtilFactory,
                                     ISLE_OperationFactory pOpFactory,
                                     ISLE_Reporter reporter,
                                     ISLE_TimeSource timeSource)
    {

        eeAPIPXDatabaseMap.put(instanceKey, database);

        HRESULT res = HRESULT.S_FALSE;
        String ipcAddress = "";
        String ipcAddressDfl = "";

        // create the report trace
        EE_APIPX_ReportTracePxy rtp = EE_APIPX_ReportTrace.createReportTrace(instanceKey);
        if (rtp == null)
        {
            return HRESULT.E_FAIL;
        }

        if (reporter != null)
        {
            rtp.setLocalDefaultReporter(reporter);
        }

        // fill ipcAddress
        EE_APIPX_IPCConfig pIpcConfig = database.getIPCConfigData();
        ipcAddress = pIpcConfig.getServiceAddress();
        ipcAddressDfl = pIpcConfig.getDefaultReportingAddress();

        // create the Listener before the Binder
        EE_APIPX_Listener.createListener(instanceKey);
        try
        {
            EE_APIPX_Listener.getInstance(instanceKey);
        }
        catch (IllegalStateException e)
        {
            return HRESULT.E_FAIL;
        }

        // create the Binder
        EE_APIPX_Binder.createBinder(instanceKey, pOpFactory, pUtilFactory, database);
        try
        {
            EE_APIPX_Binder.getInstance(instanceKey);
        }
        catch (IllegalStateException e)
        {
            return HRESULT.E_FAIL;
        }

        // wait for client
        EE_APIPX_LocalWaitingCnx waitCnxAppl = new EE_APIPX_LocalWaitingCnx(instanceKey, ipcAddress, false);
        EE_APIPX_LocalWaitingCnx waitCnxDfl = new EE_APIPX_LocalWaitingCnx(instanceKey, ipcAddressDfl, true);

        waitCnxApplMap.put(instanceKey, waitCnxAppl);
        waitCnxDflMap.put(instanceKey, waitCnxDfl);
        
        res = waitCnxAppl.start();
        if (res == HRESULT.S_OK)
        {
            res = waitCnxDfl.start();
        }
        
        return res;
    }

    /**
     * Stop the communication server by closing the pipe and deleting all the
     * created objects.
     */
    public static synchronized void shutdown(String instanceKey)
    {
    	EE_APIPX_LocalWaitingCnx waitCnxAppl = waitCnxApplMap.get(instanceKey);
        if(waitCnxAppl != null) 
        {
        	waitCnxAppl.shutdown();
        }
        
        EE_APIPX_LocalWaitingCnx waitCnxDfl = waitCnxDflMap.get(instanceKey);
        if(waitCnxDfl != null) 
        {
        	waitCnxDfl.shutdown();
        }

        waitCnxApplMap.remove(instanceKey);
        waitCnxDflMap.remove(instanceKey);
        eeAPIPXDatabaseMap.remove(instanceKey);
    }
}
