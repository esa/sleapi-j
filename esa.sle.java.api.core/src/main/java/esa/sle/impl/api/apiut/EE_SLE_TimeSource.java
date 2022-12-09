/**
 * @(#) EE_SLE_TimeSource.java
 */

package esa.sle.impl.api.apiut;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_TimeSource;
import ccsds.sle.api.isle.iscm.IUnknown;
import esa.sle.impl.ifs.time.EE_TIME_Prec;
import esa.sle.impl.ifs.time.EE_Time;

/**
 * ////////////////////////////////////////////////// The class implements the
 * interface ISLE_TimeSource and provides the current system time. This class
 * has been designed for use when the external time source interface has not
 * been delivered by the SLE application. This class enables a unique internal
 * handling of the time source.
 * //////////////////////////////////////////////////
 */
public class EE_SLE_TimeSource implements ISLE_TimeSource
{

    /**
     * Constructor
     */
    public EE_SLE_TimeSource()
    {

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_TimeSource.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    /**
     * @return
     * @throws SleApiException
     */
    @Override
    public byte[] getCurrentTime() throws SleApiException
    {
        int ci_encodeBLen = 8;
        // Get the current time from EE_Time
        byte[] time_cds = new byte[ci_encodeBLen];
        EE_Time time = new EE_Time(EE_TIME_Prec.eeTIME_PrecMILLISEC);
        time.getCDSlevel1(time_cds);
        return time_cds;
    }
}
