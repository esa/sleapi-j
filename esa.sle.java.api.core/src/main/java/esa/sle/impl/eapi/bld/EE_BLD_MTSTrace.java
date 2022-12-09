/**
 * @(#) EE_BLD_MTSTrace.java
 */

package esa.sle.impl.eapi.bld;

import java.util.concurrent.locks.ReentrantLock;

import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import ccsds.sle.api.isle.iutl.ISLE_SII;

/**
 * Multi Thread Save Trace The class implements a multi thread safe wrapper
 * around the ESLE_Trace.
 */
public class EE_BLD_MTSTrace implements ISLE_Trace
{
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * The pointer to the trace object to be used.
     */
    private ESLE_Trace pEsleTrace = null;


    /**
     * Constructor which sets the pointer to the ESLE_Trace.
     */
    public EE_BLD_MTSTrace(ESLE_Trace ptrace)
    {
        this.pEsleTrace = ptrace;
    }

    /**
     * Processes a trace record.
     */
    @Override
    public void traceRecord(SLE_TraceLevel level, SLE_Component component, ISLE_SII psii, String text)
    {
        this.lock.lock();
        this.pEsleTrace.traceRecord(level, component, psii, text);
        this.lock.unlock();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (ISLE_Trace.class == iid)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

}
