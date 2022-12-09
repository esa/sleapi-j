/**
 * @(#) EE_APIPX_MasterLink.java
 */

package esa.sle.impl.api.apipx.pxcs;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_TimeSource;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.api.apiop.sleop.EE_SLE_OpFactory;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_Database;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_IPCConfig;
import esa.sle.impl.api.apipx.pxtml.EE_APIPX_Listener;
import esa.sle.impl.api.apiut.EE_SLE_UtilityFactory;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class resides only in the communication server process. It creates a
 * thread which listens on the IPC-channels for incoming connect requests and
 * creates a new link object (class EE_APIPX_Link) for each new connection.
 * Furthermore, the class manages a list of created links for cleanup, and it
 * instantiates the binder and reportTrace singleton.
 */
public class EE_APIPX_MasterLink
{
    private static Map<String, EE_APIPX_WaitingCnx> waitCnxApplMap = new HashMap<>();

    private static Map<String, EE_APIPX_WaitingCnx> waitCnxDflMap  = new HashMap<>();

    private static Map<String, EE_APIPX_Database> eeAPIPXDatabaseMap  = new HashMap<>();


    /**
     * Initialize the communication server. S_OK Waiting for connection. E_FAIL
     * Unable to wait for incoming IPC connection due to a further unspecified
     * error.
     */
    public static synchronized HRESULT initialise(String instanceKey, 
    								 		      ISLE_Reporter reporter,
    								 		      ISLE_TimeSource timeSource,
    								 		      String configFilePath,
    								 		      boolean verbose)
    {
        HRESULT res = HRESULT.S_FALSE;
        String ipcAddress = "";
        String ipcAddressDfl = "";

        // create the Utility Factory
        EE_SLE_UtilityFactory.initialiseInstance(instanceKey, timeSource);
        EE_SLE_UtilityFactory utilFac = EE_SLE_UtilityFactory.getInstance(instanceKey);
        ISLE_UtilFactory pUtilFactory = null;
        pUtilFactory = utilFac.queryInterface(ISLE_UtilFactory.class);
        if (pUtilFactory == null)
        {
            if (verbose)
            {
                System.err.println("Cannot create the Utility Factory");
            }

            return HRESULT.E_FAIL;
        }

        // create the report trace
        EE_APIPX_ReportTracePxy rtp = EE_APIPX_ReportTrace.createReportTrace(instanceKey);
        if (rtp == null)
        {
            if (verbose)
            {
                System.err.println("Cannot create the Reporter Object");
            }

            return HRESULT.E_FAIL;
        }
        if (reporter != null)
        {
            rtp.setLocalDefaultReporter(reporter);
        }

        // check the configuration file existence
        if (Files.notExists(Paths.get(configFilePath), LinkOption.NOFOLLOW_LINKS))
        {
            if (verbose)
            {
                System.err.println("Cannot access the configuration file <" + configFilePath + ">");
            }

            return HRESULT.E_FAIL;
        }

        // create the database
        EE_APIPX_Database eeAPIPXDatabase = new EE_APIPX_Database();
        if (eeAPIPXDatabase.open(configFilePath) != HRESULT.S_OK)
        {
            if (verbose)
            {
                System.err.println("Cannot open the database < " + configFilePath + ">");
            }
            eeAPIPXDatabase = null;
            return HRESULT.E_FAIL;
        }
        
        eeAPIPXDatabaseMap.put(instanceKey, eeAPIPXDatabase);

        EE_Reference<String> diagnostic = new EE_Reference<String>();
        diagnostic.setReference("");
        EE_Reference<Integer> lineNumber = new EE_Reference<Integer>();
        if (eeAPIPXDatabase.readValues(diagnostic, lineNumber) != HRESULT.S_OK)
        {
            if (verbose)
            {
                System.err.println("Pb in the database line " + lineNumber + ": " + diagnostic);
            }
            eeAPIPXDatabase = null;
            return HRESULT.E_FAIL;
        }

        eeAPIPXDatabase.close();

        // fill ipcAddress
        EE_APIPX_IPCConfig pIpcConfig = eeAPIPXDatabase.getIPCConfigData();
        ipcAddress = pIpcConfig.getServiceAddress();
        ipcAddressDfl = pIpcConfig.getDefaultReportingAddress();

        // create the Operation Factory
        ISLE_OperationFactory pOpFactory = null;
        EE_SLE_OpFactory.initialiseInstance(instanceKey, reporter);
        EE_SLE_OpFactory opFactory = EE_SLE_OpFactory.getInstance(instanceKey);
        pOpFactory = opFactory.queryInterface(ISLE_OperationFactory.class);
        if (pOpFactory == null)
        {
            if (verbose)
            {
                System.err.println("Cannot create the Operation Factory");
            }
            return HRESULT.E_FAIL;
        }

        // create the Listener before the Binder
        EE_APIPX_Listener.createListener(instanceKey);
        try
        {
            EE_APIPX_Listener.getInstance(instanceKey);
        }
        catch (IllegalStateException e)
        {
            if (verbose)
            {
                System.err.println("Cannot create the Listener");
            }

            return HRESULT.E_FAIL;
        }

        // create the Binder
        EE_APIPX_Binder.createBinder(instanceKey, pOpFactory, pUtilFactory, eeAPIPXDatabase);
        try
        {
            EE_APIPX_Binder.getInstance(instanceKey);
        }
        catch (IllegalStateException e)
        {
            if (verbose)
            {
                System.err.println("Cannot create the Binder");
            }

            return HRESULT.E_FAIL;
        }

        // wait for client
        EE_APIPX_WaitingCnx waitCnxAppl = new EE_APIPX_WaitingCnx(instanceKey, ipcAddress, false);
        EE_APIPX_WaitingCnx waitCnxDfl = new EE_APIPX_WaitingCnx(instanceKey, ipcAddressDfl, true);

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
    	EE_APIPX_WaitingCnx waitCnxAppl = waitCnxApplMap.get(instanceKey);
        if(waitCnxAppl != null) 
        {
        	waitCnxAppl.shutdown();
        }
        
        EE_APIPX_WaitingCnx waitCnxDfl = waitCnxDflMap.get(instanceKey);
        if(waitCnxDfl != null) 
        {
        	waitCnxDfl.shutdown();
        }

        waitCnxApplMap.remove(instanceKey);
        waitCnxDflMap.remove(instanceKey);
        eeAPIPXDatabaseMap.remove(instanceKey);
    }

    /**
     * Update the configuration of the communication server. S_OK configuration
     * update completed without errors SLE_E_NOFILE configuration data file not
     * found SLE_E_CONFIG error or inconsistency in configuration data
     * SLE_E_STATE not yet initialized: Initialize() was not yet called E_FAIL
     * failure due to unspecified error
     */
    public static synchronized HRESULT updateConfiguration(String instanceKey, String configFilePath)
    {
    	EE_APIPX_Database eeAPIPXDatabase = eeAPIPXDatabaseMap.get(instanceKey);
        if (eeAPIPXDatabase == null)
        {
            return HRESULT.SLE_E_STATE;
        }

        // check the configuration file existence
        if (Files.notExists(Paths.get(configFilePath), LinkOption.NOFOLLOW_LINKS))
        {
            return HRESULT.SLE_E_NOFILE;
        }

        EE_APIPX_Database updatedDB = new EE_APIPX_Database();
        if (updatedDB.open(configFilePath) != HRESULT.S_OK)
        {
            return HRESULT.E_FAIL;
        }

        EE_Reference<String> diagnostic = new EE_Reference<String>();
        diagnostic.setReference("");
        EE_Reference<Integer> lineNumber = new EE_Reference<Integer>();
        if (updatedDB.readValues(diagnostic, lineNumber) != HRESULT.S_OK)
        {
            return HRESULT.SLE_E_CONFIG;
        }

        // close the file
        updatedDB.close();

        // database loaded - try to update the Binder
        EE_APIPX_Binder pBinder = EE_APIPX_Binder.getInstance(instanceKey);
        if (pBinder != null)
        {
            // update
            HRESULT res = pBinder.updateConfiguration(updatedDB);
            if (res == HRESULT.S_OK)
            {
                // Listener and Binder updated
                eeAPIPXDatabase = updatedDB;
            }

            return res;
        }
        else
        {
            // No binder, error
            return HRESULT.E_FAIL;
        }
    }
}
