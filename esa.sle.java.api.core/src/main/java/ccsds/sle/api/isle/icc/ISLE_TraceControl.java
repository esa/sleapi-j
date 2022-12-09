package ccsds.sle.api.isle.icc;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_TraceLevel;

/**
 * The interface is exported by objects that support generation of diagnostic
 * traces. Trace records are entered to the interface ISLE_Trace passed to the
 * function StartTrace. This interface is provided by the SLE Application. Trace
 * records and the trace levels are specified in chapter 4.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_TraceControl extends IUnknown
{
    /**
     * Starts tracing based on the supplied trace level.
     * 
     * @param trace
     * @param level
     * @param forward
     * @throws SleApiException
     */
    void startTrace(ISLE_Trace trace, SLE_TraceLevel level, boolean forward) throws SleApiException;

    /**
     * Stops tracing.
     * 
     * @throws SleApiException
     */
    void stopTrace() throws SleApiException;
}
