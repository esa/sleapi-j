package ccsds.sle.api.isle.iapl;

import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import ccsds.sle.api.isle.iutl.ISLE_SII;

/**
 * The interface is provided to API components to enter trace records, when
 * tracing is started via the interface ISLE_TraceControl. The trace method in
 * this interface does not report the time of an event. It is expected that the
 * time is added by the implementation of the interface.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_Trace extends IUnknown
{
    /**
     * Passes traces messages to the application
     * 
     * @param level the trace level
     * @param component the SLE_Component
     * @param psii the Service Instance identifier
     * @param text the trace
     */
    public void traceRecord(SLE_TraceLevel level, SLE_Component component, ISLE_SII psii, final String text);
}
