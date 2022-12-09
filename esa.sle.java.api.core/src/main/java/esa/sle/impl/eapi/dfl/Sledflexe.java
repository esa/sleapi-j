package esa.sle.impl.eapi.dfl;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.icc.ISLE_TraceControl;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_Database;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_IPCConfig;
import esa.sle.impl.api.apiut.EE_SLE_LibraryInstance;
import esa.sle.impl.eapi.dfl.idfl.ESLE_DefaultLogger;
import esa.sle.impl.ifs.gen.EE_CondVar;
import esa.sle.impl.ifs.gen.EE_Reference;
import esa.sle.impl.ifs.gen.EE_StubReporter;
import esa.sle.impl.ifs.gen.EE_StubTrace;

public class Sledflexe
{
    static EE_CondVar eecondVar = new EE_CondVar();

    private static final Logger LOG = Logger.getLogger(Sledflexe.class.getName());


    public static void main(String[] args)
    {
        String dbfilename = "";
        SLE_TraceLevel tracelevel = SLE_TraceLevel.sleTL_high;
        boolean trace_started = false;
        boolean argcheck = true;
        int size = args.length;

        for (int k = 0; k < size; k++)
        {
            String argument = args[k];
            switch (argument)
            {
            case "-t":
            {
                if (k + 1 < size)
                {
                    int i = Integer.parseInt(args[k + 1]);
                    k++;
                    switch (i)
                    {
                    case 0:
                        tracelevel = SLE_TraceLevel.sleTL_low;
                        trace_started = true;
                        break;
                    case 1:
                        tracelevel = SLE_TraceLevel.sleTL_medium;
                        trace_started = true;
                        break;
                    case 2:
                        tracelevel = SLE_TraceLevel.sleTL_high;
                        trace_started = true;
                        break;
                    case 3:
                        tracelevel = SLE_TraceLevel.sleTL_full;
                        trace_started = true;
                        break;
                    default:
                        break;
                    }
                    break;
                }
                else
                {
                    argcheck = false;
                    break;
                }
            }
            case "-d":
                if (k + 1 < size)
                {
                    String file = args[k + 1];
                    dbfilename = new String(file);
                    k++;
                    break;
                }
                else
                {
                    argcheck = false;
                }
            default:
                argcheck = false;
                break;

            }
        }

        if ((dbfilename.isEmpty()) || (!argcheck))
        {
            System.out.println("SLE Default Logger usage : sledfl -d <proxy database file name> [-t tracelevel]");
            return;
        }

        dflLogger(dbfilename, tracelevel, trace_started);

    }

    static HRESULT dflLogger(String configFilePath, SLE_TraceLevel tracelevel, boolean trace_started)
    {
        ESLE_DefaultLogger p_esle_dfl = null;
        ISLE_TraceControl p_isle_tcc = null;
        ISLE_Reporter p_isle_reporter = null;
        ISLE_Trace p_isle_trace = null;
        EE_StubReporter ee_stubreporter;
        EE_StubTrace ee_stubtrace;
        String ipcAddress;

        // get the Database
        EE_APIPX_Database pDatabase = new EE_APIPX_Database();

        if (pDatabase.open(configFilePath) != HRESULT.S_OK)
        {
            System.err.println("SLE Default Logger : Cannot open the Configuration File");
            return HRESULT.E_FAIL;
        }

        EE_Reference<String> diagnostic = new EE_Reference<String>();
        diagnostic.setReference("");
        EE_Reference<Integer> line_number = new EE_Reference<Integer>();
        if (pDatabase.readValues(diagnostic, line_number) != HRESULT.S_OK)
        {
            System.err.println("SLE Default Logger : Cannot read the Configuration File: ");
            System.err.println("Line " + line_number + ", Diag " + diagnostic);
            return HRESULT.E_FAIL;
        }

        // fill ipcAddress
        final EE_APIPX_IPCConfig pIpcConfig = pDatabase.getIPCConfigData();
        ipcAddress = pIpcConfig.getDefaultReportingAddress();

        // creation of the default logger

        EE_DFL_DefaultLogger.initialiseInstance(EE_SLE_LibraryInstance.LIBRARY_INSTANCE_KEY);
        p_esle_dfl = EE_DFL_DefaultLogger.getInstance(EE_SLE_LibraryInstance.LIBRARY_INSTANCE_KEY);
        p_esle_dfl = p_esle_dfl.queryInterface(ESLE_DefaultLogger.class);

        if (p_esle_dfl == null)
        {
            System.err.println("SLE Default Logger not created. Error ");
            return HRESULT.E_FAIL;
        }

        // create an instance of the stub reporter
        ee_stubreporter = new EE_StubReporter();
        p_isle_reporter = ee_stubreporter.queryInterface(ISLE_Reporter.class);

        if (p_isle_reporter == null)
        {
            System.out.println("SLE Default Logger : Query Interface for ISLE_Reporter failed. Error ");
            return HRESULT.E_FAIL;
        }

        // set the reporter
        p_esle_dfl.setReporter(p_isle_reporter);

        // creation an instance of the stub trace
        ee_stubtrace = new EE_StubTrace();
        p_isle_trace = ee_stubtrace.queryInterface(ISLE_Trace.class);

        if (p_isle_trace == null)
        {
            System.err.println("SLE Default Logger : Query Interface for ISLE_Trace failed. Error ");
            return HRESULT.E_FAIL;
        }

        // get the trace control interface of the default logger

        p_isle_tcc = p_esle_dfl.queryInterface(ISLE_TraceControl.class);

        if (p_isle_tcc == null)
        {
            System.err.println("SLE Default Logger : Cannot get the Trace Control interface. Error ");
            return HRESULT.E_FAIL;
        }
        HRESULT res = HRESULT.S_OK;
        try
        {
            p_esle_dfl.connect(ipcAddress);
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }
        if (res != HRESULT.S_OK)
        {
            System.err.println("SLE Default Logger : Connect to Communication Server failed. Error " + res);
            System.err.println("Ipcname " + ipcAddress);
            return HRESULT.E_FAIL;
        }

        if (trace_started)
        {
            // start the trace
            res = HRESULT.S_OK;
            try
            {
                p_isle_tcc.startTrace(p_isle_trace, tracelevel, true);
            }
            catch (SleApiException e)
            {
                res = e.getHResult();
            }
            if (res != HRESULT.S_OK)
            {
                System.err.println("SLE Default Logger : StartTrace failed. Error " + res);
                return HRESULT.E_FAIL;
            }
        }

        // infinite wait
        eecondVar.lock();
        try
        {
            eecondVar.await();
        }
        catch (InterruptedException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }
        eecondVar.unlock();

        System.out.println("End of the SLE Default Logger Process");

        return res;
    }

}
