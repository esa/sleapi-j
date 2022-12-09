package esa.sle.impl.tst.systst;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import esa.sle.impl.api.apise.slese.EE_APISE_Database;
import esa.sle.impl.api.apiut.EE_SLE_LibraryInstance;

public class THApiexe
{

    public static final long stime = System.currentTimeMillis();

    public static boolean traceApiCalls = false;


    public static void main(String[] args) throws IOException
    {
        String dbNameSE = null;
        String dbNameProxy = null;
        InputStream autoRun = null;
        boolean playback = true;
        int userDb = 1;
        SLE_TraceLevel traceLevel = SLE_TraceLevel.sleTL_high;
        boolean traceStarted = false;
        boolean traceInput = false;

        // read startup-arguments
        if (args.length == 0)
        {
            System.out.println("usage : THApi -u|-p [-e] [-q] [-T] " + "[-x <proxy database>]"
                               + " [-s <service element database>] " + "[-a <command file>] " + "[-t <tracelevel>]");
            return;
        }

        for (int count = 0; count < args.length; count++)
        {
            if (args[count].equals("-q"))
            {
                playback = false;
            }
            else if (args[count].equals("-s"))
            {
                dbNameSE = args[++count];
            }
            else if (args[count].equals("-t"))
            {
                int i = Integer.parseInt(args[++count]);
                switch (i)
                {
                case 0:
                {
                    traceLevel = SLE_TraceLevel.sleTL_low;
                    traceStarted = true;
                    break;
                }
                case 1:
                {
                    traceLevel = SLE_TraceLevel.sleTL_medium;
                    traceStarted = true;
                    break;
                }
                case 2:
                {
                    traceLevel = SLE_TraceLevel.sleTL_high;
                    traceStarted = true;
                    break;
                }
                case 3:
                {
                    traceLevel = SLE_TraceLevel.sleTL_full;
                    traceStarted = true;
                    break;
                }
                default:
                {
                    break;
                }
                }
            }
            else if (args[count].equals("-T"))
            {
                traceApiCalls = true;
            }
            else if (args[count].equals("-x"))
            {
                dbNameProxy = args[++count];
            }
            else if (args[count].equals("-a"))
            {
                autoRun = new FileInputStream(args[++count]);
            }
            else if (args[count].equals("-u"))
            {
                userDb = 1;
            }
            else if (args[count].equals("-e"))
            {
                traceInput = true;
            }
            else if (args[count].equals("-p"))
            {
                userDb = 2;
            }
            else
            {
                System.out.println("usage : THApi -u|-p [-e] [-T] [-x <proxy database>] "
                                   + "[-s <service element database>] [-a <command file>] [-t <tracelevel>]");
                return;
            }
        }

        if (dbNameSE == null)
        {
            if (userDb == 1)
            {
                dbNameSE = "DBSEUser.txt";
            }
            else if (userDb == 2)
            {
                dbNameSE = "DBSEProvider.txt";
            }
        }

        if (dbNameSE == null)
        {
            System.out.println("Missing Service Element Database");
            return;
        }

        if (dbNameProxy == null)
        {
            if (userDb == 1)
            {
                dbNameProxy = "DBProxyUser.txt";
            }
            else if (userDb == 2)
            {
                dbNameProxy = "DBProxyProvider.txt";
            }
        }

        if (dbNameProxy == null)
        {
            System.out.println("Missing Proxy Database");
            return;
        }

        // read role from the database
        EE_APISE_Database db = EE_APISE_Database.getDb();

        HRESULT rc = db.open(dbNameSE);
        if (rc != HRESULT.S_OK)
        {
            System.out.println("*** WARNING: Open Configuration File <" + dbNameSE + "> failed: ");
            if (rc == HRESULT.E_ACCESSDENIED)
            {
                System.out.println(": access denied");
            }
            else if (rc == HRESULT.SLE_E_NOFILE)
            {
                System.out.println(": no such file");
            }
            else
            {
                System.out.println("");
            }
        }

        rc = db.readConfigPrms();
        if (rc != HRESULT.S_OK)
        {
            System.out.println("");
            System.out.println("*** WARNING: Read Configuration Parameters failed. ");
            System.out.println("    File:  " + dbNameSE);
            System.out.println("    Error: " + db.getErrorText());
            System.out.println("");
            return;
        }

        SLE_AppRole role = db.getApplicationRole();

        db.close();
        EE_APISE_Database.resetDb();

        // initialize and start the test
        System.out.println("SLE API Test Harness, trace " + traceLevel);

        EE_SYSTST_Test test = new EE_SYSTST_Test();
        rc = test.configure(dbNameSE, dbNameProxy, role, traceLevel, traceStarted);
        if (rc == HRESULT.S_OK)
        {
            InputStreamReader ifs = null;

            if (autoRun != null)
            {
                ifs = new InputStreamReader(autoRun);
                BufferedReader is = new BufferedReader(ifs);
                test.start(new UTL(EE_SLE_LibraryInstance.LIBRARY_INSTANCE_KEY, is, null), playback);
            }
            else
            {
            	BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
                test.start(new UTL(EE_SLE_LibraryInstance.LIBRARY_INSTANCE_KEY, is, null), traceInput);
            }

            if (autoRun != null)
            {
                ifs.close();
                BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
                test.start(new UTL(EE_SLE_LibraryInstance.LIBRARY_INSTANCE_KEY, is, null), false);
            }
        }
    }
}
