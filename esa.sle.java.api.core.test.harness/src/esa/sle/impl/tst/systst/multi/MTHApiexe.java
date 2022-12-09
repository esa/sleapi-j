package esa.sle.impl.tst.systst.multi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import esa.sle.impl.api.apise.slese.EE_APISE_Database;
import esa.sle.impl.tst.systst.EE_SYSTST_Test;
import esa.sle.impl.tst.systst.THApiexe;
import esa.sle.impl.tst.systst.UTL;

public class MTHApiexe {

    public static final long stime = System.currentTimeMillis();

    private static SLE_TraceLevel traceLevel = SLE_TraceLevel.sleTL_full;
    
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
    {
        // read startup-arguments
        if (args.length == 0)
        {
            System.out.println("usage : MTHApi [-u|-p <proxy database> <service element database> <command file> <delay time>]+");
            return;
        }
        
        Logger.getLogger("").setLevel(Level.ALL);
        for(Handler h : Logger.getLogger("").getHandlers()) 
        {
        	h.setLevel(Level.ALL);
        }

        THApiexe.traceApiCalls = true;
        
        int instances = 0;
        List<Callable<String>> runningInstances = new ArrayList<Callable<String>>();
        
        for (int count = 0; count < args.length; count++)
        {
        	instances++;
        	
        	final String dbNameSE;;
            final String dbNameProxy;
            final FileInputStream autoRun;
            final int userDb;
            final int delayTime;
            
        	// Read if user or provider
        	if (args[count].equals("-u"))
            {
                userDb = 1;
            }
            else if (args[count].equals("-p"))
            {
            	userDb = 2;
            }
            else
            {
            	System.out.println("Cannot recognize argument " + args[count] + ", expected either -u or -p");
            	return;
            }
        	
        	count++;
        	dbNameProxy = args[count];
        	
        	count++;
        	dbNameSE = args[count];
        	
			if (dbNameSE == null) 
			{
				System.out.println("Missing Service Element Database for instance "
								+ instances);
				return;
			}

			if (dbNameProxy == null) 
			{
				System.out.println("Missing Proxy Database for instance "
						+ instances);
				return;
			}

			count++;
        	autoRun = new FileInputStream(args[count]);
        				
        	count++;
        	delayTime = Integer.parseInt(args[count]);
        	
			final String libraryInstanceKey = "SLE_API_INSTANCE_" + instances;

			runningInstances.add(() -> runTestHarness(libraryInstanceKey,
					userDb, dbNameProxy, dbNameSE, autoRun, delayTime));    	 
        }
        System.out.println("*** SLE API Test Harness: detected " + runningInstances.size() + " instances");
        List<Future<String>> futures = new ArrayList<>();
        ExecutorService es = Executors.newFixedThreadPool(runningInstances.size());
        for(Callable<String> r : runningInstances) {
        	futures.add(es.submit(r));
        }
        es.shutdown();
        for(Future<String> f : futures) {
            String done = f.get();
            System.out.println("*** SLE API Test Harness: test completed for instance " + done);
        }
        System.out.println("*** SLE API Test Harness: test completed");
        System.exit(0);
    }

	private static String runTestHarness(String libraryInstanceKey, int userDb,
			String dbNameProxy, String dbNameSE, FileInputStream autoRun, final int delayTime) 
	{

		boolean traceStarted = false;
        
		if(delayTime > 0) 
		{
			try 
			{
				System.out.println("*** SLE API Test Harness instance " + libraryInstanceKey + ": waiting " + delayTime + " ms before starting the test");
				Thread.sleep(delayTime);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
				Thread.interrupted();
			}
		}
		String rolestr = userDb == 2 ? "Provider" : "User";
		System.out.println("*** SLE API Test Harness instance " + libraryInstanceKey + ": role=" + rolestr + ", PX=" + dbNameProxy + ", SE=" + dbNameSE + " ready to run");
        // read role from the database
        EE_APISE_Database db = EE_APISE_Database.getDb(libraryInstanceKey);

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
            return libraryInstanceKey;
        }

        SLE_AppRole role = db.getApplicationRole();

        db.close();
        EE_APISE_Database.resetDb(libraryInstanceKey);

        // initialize and start the test
        System.out.println("*** SLE API Test Harness instance " + libraryInstanceKey + ", trace " + traceLevel);

        EE_SYSTST_Test test = new EE_SYSTST_Test(libraryInstanceKey);
        rc = test.configure(dbNameSE, dbNameProxy, role, traceLevel, traceStarted);
        if (rc == HRESULT.S_OK)
        {
			InputStreamReader ifs = new InputStreamReader(autoRun);
			BufferedReader is = new BufferedReader(ifs);
			test.start(new UTL(libraryInstanceKey, is, null), true);
			System.out.println("*** SLE API Test Harness instance " + libraryInstanceKey + " - Test completed");
			try 
			{
				ifs.close();
			} 
			catch (IOException e) 
			{
				// Ignore
			}
        }
        System.out.println("*** SLE API Test Harness instance " + libraryInstanceKey + " - Done");
        return libraryInstanceKey;
	}
}
