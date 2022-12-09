/**
 * @(#) ESLE_Reporter.java
 */

package esa.sle.impl.eapi.bld;

import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.it.SLE_Alarm;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.iutl.ISLE_SII;

/**
 * This class provides a base for implementing the methods of the ISLE_Reporter
 * interface that are used by the API for passing of log messages and
 * notifications to the application. The application implements the LogRecord
 * and Notify methods by specialising this class. The Create_ISLE_Reporter
 * method is used to obtain a MT-safe Reporter interface for configuration of
 * SLE API and DCW. The interface is implemented in a MT-safe way using the
 * methods of the given object.
 */
public abstract class ESLE_Reporter
{
    /**
     * Enters a message into the system log. See [SLE-API] Section 6.9.2.
     */
    public abstract void logRecord(SLE_Component component,
                                   ISLE_SII psii,
                                   SLE_LogMessageType type,
                                   long messageId,
                                   String message);

    /**
     * Notifies the application of a specific event. See [SLE-API] Section
     * 6.9.2.
     */
    public abstract void notify(SLE_Alarm alarm, SLE_Component component, ISLE_SII psii, long messageId, String message);

    /**
     * Creates and returns a pointer to a MT-safe ISLE_Reporter interface
     * suitable for configuration of SLE API. The interface is implemented in a
     * MT-safe way using the methods of the given object. The object passed with
     * the argument is assumed to be owned by the MT-safe Reporter and will be
     * eventually deleted by that object.
     */
    public ISLE_Reporter create_ISLE_Reporter(ESLE_Reporter preporter)
    {
        EE_BLD_MTSReporter pMTSReporter;
        ISLE_Reporter pIsleReporter;

        pMTSReporter = new EE_BLD_MTSReporter(preporter);
        pIsleReporter = pMTSReporter.queryInterface(ISLE_Reporter.class);
        if (pIsleReporter != null)
        {
            return pIsleReporter;
        }
        else
        {
            pMTSReporter = null;
            return null;
        }
    }
}
