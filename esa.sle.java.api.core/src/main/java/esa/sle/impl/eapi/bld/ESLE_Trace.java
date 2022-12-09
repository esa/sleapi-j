/**
 * @(#) ESLE_Trace.java
 */

package esa.sle.impl.eapi.bld;

import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import ccsds.sle.api.isle.iutl.ISLE_SII;

/**
 * This class provides a base for implementing the method of the ISLE_Trace
 * interface that is used by the API for passing trace messages to the
 * application. The application implements the TraceRecord method by
 * specialising this class. The Create_MTsafeTrace method is used to obtain a
 * MT-safe Trace interface that is passed to the Trace Control interface of API
 * components when tracing is required.
 */
public abstract class ESLE_Trace
{
    /**
     * Processes a trace record. See [SLE-API] Section 6.9.3. Implemented by the
     * client application.
     */
    public abstract void traceRecord(SLE_TraceLevel level, SLE_Component component, ISLE_SII psii, String text);

    /**
     * Creates and returns a pointer to a MT-safe ISLE_Trace interface suitable
     * for use by the SLE API. The interface is implemented in a MT-safe way
     * using the method of the given object. The object passed with the argument
     * is assumed to be owned by the MT-safe Trace object and will be eventually
     * deleted by that object.
     */
    public ISLE_Trace create_ISLE_Trace(ESLE_Trace ptrace)
    {
        EE_BLD_MTSTrace pMTSTrace;
        ISLE_Trace pIsleTrace;
        pMTSTrace = new EE_BLD_MTSTrace(ptrace);
        pIsleTrace = pMTSTrace.queryInterface(ISLE_Trace.class);
        if (pIsleTrace != null)
        {
            return pIsleTrace;
        }
        else
        {
            pMTSTrace = null;
            return null;
        }
    }
}
