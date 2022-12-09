package esa.sle.impl.tst.systst;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_TimeSource;
import ccsds.sle.api.isle.iscm.IUnknown;
import esa.sle.impl.ifs.time.EE_Duration;
import esa.sle.impl.ifs.time.EE_Time;

public class EE_SYSTST_TimeSource implements ISLE_TimeSource
{
    private boolean positive;

    private EE_Duration offset;


    @SuppressWarnings("unused")
    private EE_SYSTST_TimeSource(final EE_SYSTST_TimeSource right)
    {
        this.offset = right.offset;
        this.positive = right.positive;
    }

    public EE_SYSTST_TimeSource()
    {
        this.positive = true;
        this.offset = new EE_Duration(0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == ISLE_TimeSource.class)
        {
            return (T) this;
        }
        else if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns the current time in CCSDS CDS format.
     */
    @Override
    public byte[] getCurrentTime() throws SleApiException
    {
        EE_Time tmpTime = new EE_Time();
        byte[] time;
        int ciEncodedBLen = 8;

        // get the current time from EE_Time
        tmpTime.update();

        // add the time offset for simulated time
        if (this.positive)
        {
            tmpTime = tmpTime.add(this.offset);
        }
        else
        {
            tmpTime = tmpTime.subtractDuration(this.offset);
        }

        time = new byte[ciEncodedBLen + 1];
        tmpTime.getCDSlevel1(time);
        time[ciEncodedBLen] = '\0';
        return time;
    }

    public void setOffset(boolean positive, long offset)
    {
        this.positive = positive;
        this.offset = new EE_Duration(offset);
    }
}
