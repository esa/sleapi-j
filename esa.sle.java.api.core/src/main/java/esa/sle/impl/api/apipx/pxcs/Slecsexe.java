package esa.sle.impl.api.apipx.pxcs;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import esa.sle.impl.api.apiut.EE_SLE_LibraryInstance;

public class Slecsexe
{
    private static boolean verbose = false;

    private static final Logger LOG = Logger.getLogger(Slecsexe.class.getName());

    private static HRESULT comServer(String configFilePath)
    {
        HRESULT res = HRESULT.S_FALSE;
        res = EE_APIPX_MasterLink.initialise(EE_SLE_LibraryInstance.LIBRARY_INSTANCE_KEY, null, null, configFilePath, verbose);
        if (res != HRESULT.S_OK)
        {
            System.err.println("SLE Communication Server : Initialise failed. return " + res.toString());
            return HRESULT.E_FAIL;
        }

        // Register to the ShutdownHook for SIGTERM
        Runtime rST = Runtime.getRuntime();
        rST.addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                if (verbose)
                {
                    System.out.println("SLE Communication Server : SIGTERM received");
                    System.out.println("Shutdown of the SLE Communication Server Process");
                }

                EE_APIPX_MasterLink.shutdown(EE_SLE_LibraryInstance.LIBRARY_INSTANCE_KEY);
            }
        });

        // infinite wait
        try
        {
            new Semaphore(0).acquire();
        }
        catch (InterruptedException e)
        {
            LOG.log(Level.FINE, "InterruptedException ", e);
        }

        return res;
    }

    public static void main(String[] args)
    {
        String configFilePath = "";
        verbose = false;
        boolean argcheck = true;

        for (int count = 0; count < args.length; count++)
        {
            if (args[count].equals("-d"))
            {
                configFilePath = args[++count];
            }
            else if (args[count].equals("-v"))
            {
                verbose = true;
            }
            else
            {
                argcheck = false;
            }
        }

        if (configFilePath.isEmpty() || !argcheck)
        {
            System.err.println("SLE Communication Server usage : slecs [-v] -d <proxy database file name>");
            return;
        }

        comServer(configFilePath);

        return;
    }
}
