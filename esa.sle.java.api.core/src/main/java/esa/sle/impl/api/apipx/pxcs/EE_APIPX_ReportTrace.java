/**
 * @(#) EE_APIPX_ReportTrace.java
 */

package esa.sle.impl.api.apipx.pxcs;

import java.util.HashMap;
import java.util.Map;

import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.it.SLE_TraceLevel;

/**
 * The class creates an instance of the ReportTracePxy via the function
 * EE_APIPX_CreateReportTrace(), which creates an object of the class
 * EE_APIPX_ReportTracePxy. Note that the created object is a singleton and as
 * such only created once. Every subsequent function call uses the originally
 * created object to query the desired interface.
 */
public class EE_APIPX_ReportTrace
{
    /**
     * This is the singleton class pointer.
     */
    private static Map<String, EE_APIPX_ReportTracePxy> pReportTraceMap = new HashMap<>();


    /**
     * Returns the trace interface of the ReportTracePxy object.
     */
    public static synchronized ISLE_Trace getTraceInterface(String instanceKey)
    {
    	EE_APIPX_ReportTracePxy pReportTrace = pReportTraceMap.get(instanceKey);
    	
        ISLE_Trace pIsleTrace = null;
        if (pReportTrace == null)
        {
            return null;
        }

        pIsleTrace = pReportTrace.queryInterface(ISLE_Trace.class);
        if (pIsleTrace != null)
        {
            return pIsleTrace;
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns the reporter interface of the ReportTracePxy object.
     */
    public static synchronized ISLE_Reporter getReporterInterface(String instanceKey)
    {
    	EE_APIPX_ReportTracePxy pReportTrace = pReportTraceMap.get(instanceKey);
    	
        ISLE_Reporter pIsleReporter = null;
        if (pReportTrace == null)
        {
            return null;
        }

        pIsleReporter = pReportTrace.queryInterface(ISLE_Reporter.class);
        if (pIsleReporter != null)
        {
            return pIsleReporter;
        }
        else
        {
            return null;
        }
    }

    /**
     * Creates the ReportTracePxy component. The function ensures that only one
     * single instance of the ReportTracePxy is ever created. Every subsequent
     * call to this function returns a pointer to the same instance. S_OK the
     * component has been instantiated. E_FAIL failure due to unspecified error.
     */
    public static synchronized EE_APIPX_ReportTracePxy createReportTrace(String instanceKey)
    {
    	EE_APIPX_ReportTracePxy pReportTrace = pReportTraceMap.get(instanceKey);
    	
        if (pReportTrace == null)
        {
            pReportTrace = new EE_APIPX_ReportTracePxy(instanceKey);
            pReportTraceMap.put(instanceKey, pReportTrace);
        }
        return pReportTrace;
    }

    /**
     * Sets the reference of the link to the default logger in the
     * ReportTracePxy object.
     */
    public static synchronized void setDefaultLogger(String instanceKey, EE_APIPX_Link pLink)
    {
    	EE_APIPX_ReportTracePxy pReportTrace = pReportTraceMap.get(instanceKey);
    	
        if (pReportTrace != null)
        {
            pReportTrace.setDflLink(pLink);
        }
    }

    /**
     * Sets the reference to the local ISLE_Reporter interface for default
     * logging. This is needed if the Communication Server is used as a library
     * in an application.
     */
    public static synchronized void setLocalDefaultReporter(String instanceKey, ISLE_Reporter pReporter)
    {
    	EE_APIPX_ReportTracePxy pReportTrace = pReportTraceMap.get(instanceKey);
    	
        if (pReportTrace != null)
        {
            pReportTrace.setLocalDefaultReporter(pReporter);
        }
    }

    /**
     * Sets the reference to the local ISLE_Trace interface. This is needed if
     * the Communication Server is used as a library in an application.
     */
    public static synchronized void setLocalTrace(String instanceKey, ISLE_Trace pTrace, SLE_TraceLevel traceLevel)
    {
    	EE_APIPX_ReportTracePxy pReportTrace = pReportTraceMap.get(instanceKey);
    	
        if (pReportTrace != null)
        {
            pReportTrace.setLocalTrace(pTrace, traceLevel);
        }
    }

}
