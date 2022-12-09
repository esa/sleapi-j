package ccsds.sle.api.isle.iapl;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iscm.IUnknown;

/**
 * Give support to obtain the current time from an external time source. The
 * time source may contain an offset (positive or negative) to the current
 * system time.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_TimeSource extends IUnknown
{

    /**
     * Get the Current Time.
     * 
     * @return current time in CCSDS CDS format.
     * @throws SleApiException
     */
    byte[] getCurrentTime() throws SleApiException;
}
