/**
 * @(#) EE_BLD_MTSTimeSource.java
 */

package esa.sle.impl.eapi.bld;

import ccsds.sle.api.isle.iapl.ISLE_TimeSource;
import ccsds.sle.api.isle.iscm.IUnknown;

/**
 * Multi Thread Save Time Source The class implements a multi thread safe
 * wrapper around the ESLE_TimeSource.
 */
public class EE_BLD_MTSTimeSource implements ISLE_TimeSource
{
    /**
     * The pointer to the time source object to be used.
     */
    private ESLE_TimeSource pEsleTimeSource = null;


    /**
     * Constructor which sets the pointer to the ESLE_TimeSource.
     */
    public EE_BLD_MTSTimeSource(ESLE_TimeSource ptimesource)
    {
        assert (ptimesource != null) : "ptimesource is NULL";
        this.pEsleTimeSource = ptimesource;
    }

    /**
     * Returns the current time in CCSDS CDS format.
     */
    @Override
    public byte[] getCurrentTime()
    {
        byte[] octet = null;
        synchronized (this)
        {
            if (this.pEsleTimeSource != null)
            {
                octet = this.pEsleTimeSource.getCurrentTime();
            }
        }
        ;
        return octet;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (ISLE_TimeSource.class == iid)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

}
