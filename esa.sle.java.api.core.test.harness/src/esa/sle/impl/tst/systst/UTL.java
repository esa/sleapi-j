package esa.sle.impl.tst.systst;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.ifsp.types.FSP_AbsolutePriority;
import esa.sle.impl.api.apiut.EE_SLE_UtilityFactory;
import esa.sle.impl.ifs.gen.EE_Reference;

public class UTL
{

    private static final Logger LOG = Logger.getLogger(UTL.class.getName());

    private BufferedReader is;
    
    private BufferedWriter recordingStream;
    
    private String instanceId;
    
    public UTL(String instanceId, BufferedReader is, BufferedWriter recordingStream)
    {
    	this.instanceId = instanceId;
    	this.is = is;
    	this.recordingStream = recordingStream;
    }

    public String getInstanceId()
    {
    	return this.instanceId;
    }
    
    public BufferedReader getInputReader() 
    {
    	return this.is;
    }
    
    public void setInputReader(BufferedReader is) 
    {
    	this.is = is;
    }
    
    public BufferedWriter getRecordingStream() 
    {
		return this.recordingStream;
	}

	public void setRecordingStream(BufferedWriter recordingStream) 
	{
		this.recordingStream = recordingStream;
	}

	/**
     * reads a colon-separated list of vc-priority pairs into an array created
     * with new. For this to work with input from input stream, there MUST BE NO
     * SPACES, i.e. the list is one input token! memory must be freed by caller.
     * returns the number of items in the array if the input is empty (normally
     * not possible with input stream), returns null if the input cannot be
     * parsed at all, returns null in the error case, the parameter 'outArray'
     * is not changed! parsing stops after the last pair that could be properly
     * converted. if there were more illegal elements in the input list, the
     * remaining outArray elements are not initialized. Example input:
     * "1,3:2,1:13,4"
     * 
     * @param prompt
     * @param playback
     * @param prioList
     * @return
     */
    public FSP_AbsolutePriority[] readPriorityList(final String prompt, boolean playback)
    {
    	FSP_AbsolutePriority[] prioList = null;
    	try
    	{
	        EE_Reference<String> arg = new EE_Reference<String>();
	        read(arg, playback);
	        // assume that no leading or trailing WS exists!
	        final String pStart = arg.getReference();
	
	        // 1. check if input starts and ends with digit
	        if (pStart == null || pStart.length() < 1)
	        {
	            return prioList;
	        }
	        if (!Character.isDigit(pStart.charAt(0)) || !Character.isDigit(pStart.charAt(pStart.length() - 1)))
	        {
	            return prioList;
	        }
	
	        // 2. count the number of elements, we have to pre-allocate the array
	        int nElems = 1;
	        for (int i = 0; i < pStart.length(); i++)
	        {
	            if (pStart.charAt(i) == ':')
	            {
	                nElems++;
	            }
	        }
	
	        // 3. allocate array
	        prioList = new FSP_AbsolutePriority[nElems];
	
	        // 4. parse array
	        int nElemsParsed = 0;
	        String[] tokens = pStart.split(":");
	        String pToken = tokens[nElemsParsed];
	        while (nElemsParsed < tokens.length)
	        {
	            boolean passed = true;
	            try
	            {
	                pToken.charAt(pToken.indexOf(","));
	            }
	            catch (IndexOutOfBoundsException e)
	            {
	            	System.err.println("IndexOutOfBoundsException ");
	                LOG.log(Level.FINE, "IndexOutOfBoundsException ", e);
	                passed = false;
	            }
	            if (passed)
	            {
	                // there is a comma between 2 strings.
	                String[] parts = pToken.split(",");
	
	                int vc;
	                try
	                {
	                    vc = Integer.parseInt(parts[0], 10);
	                }
	                catch (NumberFormatException e)
	                {
	                    System.err.println("Cannot parse vc from token <" + parts[0] + ">");
	                    break;
	                }
	                if (vc < 0 || vc > 63)
	                {
	                    System.out.println("WARNING: Illegal VC input: " + vc);
	                }
	
	                int prio;
	                try
	                {
	                    prio = Integer.parseInt(parts[1], 10);
	                }
	                catch (NumberFormatException e)
	                {
	                    System.err.println("Cannot parse prio from token <" + parts[1] + ">");
	                    break;
	                }
	                if (prio < 1 || prio > 64)
	                {
	                    System.out.println("WARNING: Illegal Priority input: " + prio);
	                }
	                prioList[nElemsParsed] = new FSP_AbsolutePriority();
	                prioList[nElemsParsed].setMapOrVc(vc);
	                prioList[nElemsParsed].setPriority(prio);
	                nElemsParsed++;
	                if(nElemsParsed < tokens.length)
	                {
	                	pToken = tokens[nElemsParsed]; // read next token
	                }
	            }
	            else
	            {
	                System.out.println("comma not found in token <" + pToken + ">");
	                break;
	            }
	        }
	
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
        return prioList;
    }

    public int readInt(final String prompt, boolean playback)
    {
        EE_Reference<String> arg = new EE_Reference<String>();
        System.out.print(prompt);
        read(arg, playback);
        return Integer.parseInt(arg.getReference());
    }

    public ISLE_SII readSII(boolean playback)
    {

        EE_Reference<String> arg = new EE_Reference<>();

        // CHANGED-v2: request use of initial format
        System.out.print("Use Initial Format (0:No, 1:Yes): ");
        read(arg, playback);
        long useInitial = Integer.parseInt(arg.getReference());
        arg.setReference("");
        System.out.print("SIID ascii form: ");
        read(arg, playback);

        HRESULT res = HRESULT.S_OK;
        ISLE_SII sii = null;
        try
        {
            sii = EE_SLE_UtilityFactory.getInstance(this.instanceId).createSII(ISLE_SII.class);
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }
        if (useInitial > 1 && res == HRESULT.S_OK)
        {
            sii.setInitialFormat();
        }

        // sii->Add_LocalRDN(objId, arg.c_str());

        res = HRESULT.S_OK;
        try
        {
            sii.setAsciiForm(arg.getReference());
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }
        if (res != HRESULT.S_OK)
        {
            System.out.println("WARNING: Invalid Service Instance Id: " + arg);
        }
        return sii;

    }

    public ISLE_Time readTime(String prompt, boolean playback)
    {

        HRESULT rc = HRESULT.E_FAIL;
        ISLE_Time time = null;
        try
        {

            time = EE_SLE_UtilityFactory.getInstance(this.instanceId).createTime(ISLE_Time.class);

        }
        catch (SleApiException e)
        {
            rc = e.getHResult();
        }

        while (rc != HRESULT.S_OK)
        {
            rc = HRESULT.S_OK;
            EE_Reference<String> tm = new EE_Reference<>();
            System.out.println(prompt);
            read(tm, playback);
            System.out.println(tm.getReference());
            try
            {
                time.setDateAndTime(tm.getReference());
            }
            catch (SleApiException e)
            {
                rc = e.getHResult();
            }
            if (rc != HRESULT.S_OK)
            {
                System.out.println("Invalid time format " + tm.getReference() + " try  yyyy-mm-ddThh:mm:ss.d..d");
            }
        }
        return time;
    }

    // reads from the input stream and records to the output stream
    public boolean read(EE_Reference<String> arg, boolean playback)
    {
        boolean rc = false;
        arg.setReference("");

        rc = readString(is, arg);
        if (playback)
        {
            System.out.println(arg.getReference());
        }
        return rc;
    }

    private boolean readString(BufferedReader is, EE_Reference<String> arg)
    {
        return readString(is, arg, '"');
    }

    private Map<String, BufferedReader> skipComments(BufferedReader in)
    {
        boolean non_ws_found = false;
        String line = "";

        Map<String, BufferedReader> result = new HashMap<String, BufferedReader>();

        try
        {
            while (!non_ws_found)
            {
                line = in.readLine();

                if (line != null) // not end of the file
                {
                    boolean resetLine = false;
                    while (!resetLine)
                    {
                        if (line.trim().isEmpty())
                        {
                            resetLine = true;
                        }
                        for (char c : line.toCharArray())
                        {
                            if (resetLine || non_ws_found)
                            {
                                break;
                            }
                            switch (c)
                            {
                            case '#':
                                resetLine = true;
                                break;
                            case ' ':
                            case '\t':
                            case '\r':
                                resetLine = true;
                                break;
                            case '\n':
                                resetLine = true;
                                break;
                            default:
                                non_ws_found = true;
                                resetLine = true;
                                break;
                            }
                        }
                    }
                }
                else
                // end of file
                {
                    result.put(line, in);
                    return result;
                }
            }
        }
        catch (IOException e)
        {
            LOG.log(Level.FINE, "IOException ", e);
        }
        result.put(line, in);
        return result;
    }

    private boolean readString(BufferedReader in, EE_Reference<String> str, char delimiter)
    {
        str.setReference("");
        String lineString = "";
        boolean out = false;

        Map<String, BufferedReader> input = skipComments(in);
        Iterator<String> it = input.keySet().iterator();
        String line = it.next();

        if (line == null)
        {
            return false;
        }
        while (line != null && !out)
        {
            for (char c : line.toCharArray())
            {
                if (!Character.isSpaceChar(c) && c != '\t' && c != '\f' && c != '\n' && c != '\r' && c != delimiter)
                {
                    lineString += c;
                }
                else
                {
                    out = true;
                    break;
                }
            }
            out = true;
            str.setReference(lineString);
        }
        return true;
    }

    /**
     * This method returns always a long array. The conversion between the long
     * array and another type of array (eg an array of enums) is done by the
     * client method.
     * 
     * @param prompt
     * @param playback
     * @return
     */
    public long[] readIntList(final String prompt, boolean playback)
    {
        EE_Reference<String> arg = new EE_Reference<String>();
        System.out.print(prompt);
        read(arg, playback);
        // assume that no leading or trailing WS exists!
        final String pStart = arg.getReference();

        // 1. check if input starts and ends with digit

        if (arg.getReference().isEmpty())
        {
            return new long[] { 0 };
        }
        if (pStart.isEmpty() || pStart.length() < 1)
        {
            return new long[] { 0 };
        }
        if (!Character.isDigit(pStart.charAt(0)) || !Character.isDigit(pStart.charAt(pStart.length() - 1)))
        {

            return new long[] { -1 };
        }

        // 2. count the number of elements, we have to pre-allocate the array
        String[] pVec = pStart.split(",");

        // 3. allocate array
        List<Long> local = new ArrayList<Long>();

        // 4. parse array
        int nElemsParsed = 0;
        String pToken = null;
        pToken = pVec[nElemsParsed]; // read first token
        while (pToken != null || nElemsParsed < pVec.length)
        {
            long number;
            boolean passed = true;
            try
            {
                number = Long.parseLong(pToken);
            }
            catch (NumberFormatException e)
            {
                passed = false;
                System.err.println("Cannot parse integer from token <" + pToken + ">");
                break;
            }

            if (passed)
            {
                local.add(number);
                nElemsParsed++;
                if (nElemsParsed == pVec.length)
                {
                    break;
                }
                pToken = pVec[nElemsParsed]; // read next token
            }
        }

        long[] result = new long[nElemsParsed];
        int k = 0;
        for (Long el : local)
        {
            result[k] = el;
            k++;
        }
        return result;
    }

    public boolean readYn(final String prompt, boolean playback)
    {
        EE_Reference<String> arg = new EE_Reference<String>();
        System.out.print(prompt);
        read(arg, playback);
        char c = Character.toLowerCase(arg.getReference().charAt(0)); // just
                                                                      // take
                                                                      // first
                                                                      // char
        return (c == 'y' ? true : false);
    }

    public SLE_Result readResult(final String prompt,
                                        final String positive,
                                        final String negative,
                                        final String invalid,
                                        boolean playback)
    {
        int rc = -2;
        while (rc < -1 || rc > 1)
        {
            System.out.print(prompt);
            System.out.print(": (0=" + positive);
            System.out.print(", 1=" + negative);
            System.out.print(", -1=" + invalid);
            System.out.print(") ");

            rc = readInt("", playback);
            if (rc < -1 || rc > 1)
            {
                System.out.print("Invalid value for result: " + rc);
            }
        }
        return SLE_Result.getSLE_ResultByCode(rc);
    }

    public static void traceIF0(String func)
    {
        if (THApiexe.traceApiCalls)
        {
            System.out.println("*** " + func + "()");
        }
    }

    public static void traceIF1(String func, String par1)
    {
        if (THApiexe.traceApiCalls)
        {
            System.out.println("*** " + func + "(" + par1 + ")");
        }
    }

    public static void traceIF3(String func, String par1, String par2, String par3)
    {
        if (THApiexe.traceApiCalls)
        {
            System.out.println("*** " + func + "(" + par1 + ", " + par2 + ", " + par3 + ")");
        }
    }

    public static void traceIF4(String func, String par1, String par2, String par3, String par4)
    {
        if (THApiexe.traceApiCalls)
        {
            System.out.println("*** " + func + "(" + par1 + ", " + par2 + ", " + par3 + ", " + par4 + ")");
        }
    }

    public static void traceIF5(String func, String par1, String par2, String par3, String par4, String par5)
    {
        if (THApiexe.traceApiCalls)
        {
            System.out
                    .println("*** " + func + "(" + par1 + ", " + par2 + ", " + par3 + ", " + par4 + ", " + par5 + ")");
        }
    }

}
