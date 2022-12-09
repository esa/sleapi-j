/**
 * @(#) ESLE_TimeSource.java
 */

package esa.sle.impl.eapi.bld;

import ccsds.sle.api.isle.iapl.ISLE_TimeSource;

/**
 * This class provides a base for implementing the method of the ISLE_TimeSource
 * interface that is used by the API for passing trace messages to the
 * application. The application implements the method Get_CurrentTime by
 * specializing this class. The Create_ISLE_TimeSource method is used to obtain
 * a MT-safe TimeSource interface that is passed to the ESLE_Builder object for
 * initialization.
 */
public abstract class ESLE_TimeSource
{
    /**
     * Creates and returns a pointer to a MT-safe interface ISLE_TimeSource
     * suitable for use by the SLE API.
     */
    public ISLE_TimeSource create_ISLE_TimeSource(ESLE_TimeSource ptimeSource)
    {
        EE_BLD_MTSTimeSource pMTSTimeSource;
        ISLE_TimeSource pIsleTimeSource;

        pMTSTimeSource = new EE_BLD_MTSTimeSource(ptimeSource);
        pIsleTimeSource = pMTSTimeSource.queryInterface(ISLE_TimeSource.class);
        if (pIsleTimeSource != null)
        {
            return pIsleTimeSource;
        }
        else
        {
            pMTSTimeSource = null;
            return null;
        }

    }

    /**
     * Returns the current time in CCSDS CDS format.
     */
    public abstract byte[] getCurrentTime();

}
