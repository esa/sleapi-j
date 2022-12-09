package ccsds.sle.api.isle.iutl;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_TimeFmt;
import ccsds.sle.api.isle.it.SLE_TimeRes;

/**
 * Objects exporting this interface store time information with a resolution of
 * up to one microsecond. They support input and output in the following
 * formats: * CCSDS day segmented time code (CDS) * CCSDS ASCII Calendar
 * Segmented Time Code. They provide methods for comparison of times and
 * calculation of the difference between two times measured in seconds and
 * fractions of seconds.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_Time extends IUnknown, Comparable<ISLE_Time>
{
    /**
     * Sets CDS.
     * 
     * @param time
     * @throws SleApiException
     */
    void setCDS(byte[] time) throws SleApiException;

    /**
     * Gets CDS.
     * 
     * @return
     */
    byte[] getCDS();

    /**
     * Sets date and time.
     * 
     * @param dateAndTime
     * @throws SleApiException
     */
    void setDateAndTime(String dateAndTime) throws SleApiException;

    /**
     * Sets time.
     * 
     * @param time
     * @throws SleApiException
     */
    void setTime(String time) throws SleApiException;

    /**
     * Gets date.
     * 
     * @param fmt
     * @return
     */
    String getDate(SLE_TimeFmt fmt);

    /**
     * Gets time.
     * 
     * @param fmt
     * @param res
     * @return
     */
    String getTime(SLE_TimeFmt fmt, SLE_TimeRes res);

    /**
     * Gets data and time.
     * 
     * @param fmt
     * @param res
     * @return
     */
    String getDateAndTime(SLE_TimeFmt fmt, SLE_TimeRes res);

    /**
     * Gets data and time.
     * 
     * @param fmt
     * @return
     */
    String getDateAndTime(SLE_TimeFmt fmt);

    /**
     * Updates.
     */
    void update();

    /**
     * Copies the object.
     * 
     * @return
     */
    ISLE_Time copy();

    /**
     * Sets CDS to picoseconds.
     * 
     * @param time
     * @throws SleApiException
     */
    void setCDSToPicosecondsRes(byte[] time) throws SleApiException;

    /**
     * Gets CDS to picoseconds.
     * 
     * @return
     */
    byte[] getCDSToPicosecondsRes();

    /**
     * Gets used picoseconds.
     * 
     * @return
     */
    boolean getPicosecondsResUsed();

    /**
     * Subtraction from this the time object given as argument.
     * 
     * @param time
     * @return
     */
    double subtract(ISLE_Time time);

    /**
     * @param o
     * @return
     */
    @Override
    boolean equals(Object o);

    /**
     * @return
     */
    @Override
    int hashCode();
}
