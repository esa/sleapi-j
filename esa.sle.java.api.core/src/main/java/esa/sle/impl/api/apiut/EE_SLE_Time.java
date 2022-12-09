/**
 * @(#) EE_SLE_Time.java
 */

package esa.sle.impl.api.apiut;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_TimeSource;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_TimeFmt;
import ccsds.sle.api.isle.it.SLE_TimeRes;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import esa.sle.impl.ifs.gen.EE_IntegralEncoder;
import esa.sle.impl.ifs.time.EE_TIME_Fmt;
import esa.sle.impl.ifs.time.EE_TIME_Prec;
import esa.sle.impl.ifs.time.EE_Time;

/**
 * The class provides an implementation of a set of time handling functions as
 * specified in reference [SLE-API] for the component class 'Time' The object is
 * created by delivering a time-source interface to the constructor.
 */

public class EE_SLE_Time implements ISLE_Time
{
    private static final Logger LOG = Logger.getLogger(EE_SLE_Time.class.getName());

    private final ISLE_TimeSource source;

    private long picoseconds;

    private boolean picosecondsUsed;

    private EE_Time ee_time;


    /**
     * Constructor with no arguments.
     * 
     * @param source
     */
    public EE_SLE_Time()
    {
        // source can not be null, otherwise update give error.
        this.source = new EE_SLE_TimeSource();
        this.picoseconds = 0;
        this.picosecondsUsed = false;
        this.ee_time = new EE_Time();
        update();
    }

    /**
     * Constructor which must be used when creating a EE_SLE_Time object.
     * 
     * @param source
     */
    public EE_SLE_Time(ISLE_TimeSource source)
    {
        this.source = source;
        this.picoseconds = 0;
        this.picosecondsUsed = false;
        this.ee_time = new EE_Time();
        update();
    }

    /**
     * Constructor which must be used when creating a EE_SLE_Time object.
     * 
     * @param source
     * @param cdsTime The CDS coded time for initialisation
     * @throws SleApiException 
     */
    public EE_SLE_Time(ISLE_TimeSource source, byte[] cdsTime) throws SleApiException
    {
        this.source = source;
        this.picoseconds = 0;
        this.picosecondsUsed = false;
        this.ee_time = new EE_Time(0, 0); // avoid expensive update, CDS time will be set below
        
        if(cdsTime == null)
        {
        	throw new SleApiException(HRESULT.E_INVALIDARG, "Invalid CDS time argument: null");
        }
        
        if(cdsTime.length == 8)
        {
        	setCDS(cdsTime);
        }
        else if(cdsTime.length == 10)
        {
        	setCDSToPicosecondsRes(cdsTime);
        } else
        {
        	throw new SleApiException(HRESULT.E_INVALIDARG, "Invalid CDS time argument length: " + cdsTime.length);
        }
    }
    
    /**
     * Copy constructor.
     * 
     * @param right
     */
    private EE_SLE_Time(EE_SLE_Time right)
    {
        this.source = right.source;
        this.picoseconds = right.picoseconds;
        this.picosecondsUsed = right.picosecondsUsed;
        this.ee_time = new EE_Time(right.ee_time);
    }

    /**
     * @param iid
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Time.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    /**
     * @param time
     * @throws SleApiException
     */
    @Override
    public void setCDS(byte[] time) throws SleApiException
    {
        byte[] tmp = time;
        // Decode microseconds
        long uSec = EE_IntegralEncoder.decodeUnsignedMSBFirst(tmp, 6, 2);
        // Save microseconds as picoseconds
        this.picoseconds = uSec * 1000000;
        // Set the picoseconds flag to NOT USED
        this.picosecondsUsed = false;
        // Save the complete information inside the time object,
        // but use only millisecond information from that
        this.ee_time.setCDSlevel1(time);
    }

    /**
     * @return
     */
    @Override
    public byte[] getCDS()
    {
        int ci_encodeBLen = 8;
        byte[] time = new byte[ci_encodeBLen];
        try
        {
            this.ee_time.getCDSlevel1(time);
            // Retrieve microseconds from picoseconds
            long uSec = this.picoseconds / 1000000;
            // Encode in 2 bytes
            EE_IntegralEncoder.encodeUnsignedMSBFirst(time, 6, 2, uSec);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }
        return time;
    }

    /**
     * @param dateAndTime
     * @throws SleApiException
     */
    @Override
    public void setDateAndTime(String dateAndTime) throws SleApiException
    {
        setEETime(dateAndTime, true);
    }

    /**
     * @param time
     * @throws SleApiException
     */
    @Override
    public void setTime(String time) throws SleApiException
    {
        setEETime(time, false);
    }

    /**
     * @param time
     * @param isDateAndTime
     * @throws SleApiException
     */
    private void setEETime(String timeOrDateAndTime, boolean isDateAndTime) throws SleApiException
    {
        // Picodigits
        String picoDigits = "";
        // Check if the resolution is more than nanoseconds (how many digits
        // there are
        // after the . point, if any)
        int dotIdx = timeOrDateAndTime.indexOf('.');
        if (dotIdx != -1)
        {
            // Count decimals
            int decNum = 0;
            int idxStart = dotIdx + 1;
            boolean continueCheck = true;
            long alreadyCounted = 0;
            while (idxStart < timeOrDateAndTime.length() && continueCheck)
            {
                if (Character.isDigit(timeOrDateAndTime.charAt(idxStart)))
                {
                    // Increment count
                    decNum++;
                    // Split on milliseconds
                    if (alreadyCounted > 2)
                    {
                        picoDigits += timeOrDateAndTime.charAt(idxStart);
                    }
                }
                else
                {
                    // Stop
                    continueCheck = false;
                }
                alreadyCounted++;
                idxStart++;
            }

            // If more than nanosecond resolution, truncate the string before
            // passing it
            // to the EE_Time object
            if (decNum > 9)
            {
                // Is a picosecond resolution number
                this.picosecondsUsed = true;
                String resultingString = timeOrDateAndTime.substring(0, dotIdx + 9 + 1);
                // Skip all digits starting from dotIdx + nanoseconds resolution
                // length + 1
                int dIdx = dotIdx + 9 + 1;
                while (dIdx < timeOrDateAndTime.length() && Character.isDigit(timeOrDateAndTime.charAt(dIdx)))
                {
                    picoDigits += timeOrDateAndTime.charAt(dIdx);
                    dIdx++;
                }
                // Add the rest
                if (dIdx < timeOrDateAndTime.length())
                {
                    resultingString += timeOrDateAndTime.substring(dIdx);
                }
                // Substitute the string
                timeOrDateAndTime = resultingString;
            }
            else
            {
                if (decNum > 6)
                {
                    // use more than microsecond resolution
                    this.picosecondsUsed = true;
                }
                else
                {
                    this.picosecondsUsed = false;
                }
            }
        }

        // Pass the string (original or truncated) to the EE_Time object

        if (isDateAndTime)
        {
            this.ee_time.setCCSDSDateAndTime(timeOrDateAndTime);
        }
        else
        {
            this.ee_time.setCCSDSTime(timeOrDateAndTime);
        }

        // Detect how many picoseconds there are in the string (picoDigits
        // variable)
        if (picoDigits.length() > 0)
        {
            if (picoDigits.length() > 9)
            {
                // Truncate to pico
                picoDigits = picoDigits.substring(0, 9);
            }
            else if (picoDigits.length() < 9)
            {
                // Fill with zeroes at the end
                String.format("%-" + picoDigits.length() + "s", picoDigits).replace(' ', '0');
            }
            // Save them in the picosecond variable
            this.picoseconds = Long.parseLong(picoDigits);
        }
        else
        {
            // No picoseconds
            this.picoseconds = 0;
        }
    }

    /**
     * @param fmt
     * @throws SleApiException
     */
    @Override
    public String getDate(SLE_TimeFmt fmt)
    {
        String ret = "";
        try
        {
            if (fmt == SLE_TimeFmt.sleTF_dayOfMonth)
            {
                ret = this.ee_time.getDateCCSDS(EE_TIME_Fmt.eeTIME_FmtA);
            }
            else
            {
                ret = this.ee_time.getDateCCSDS(EE_TIME_Fmt.eeTIME_FmtB);
            }
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }

        return ret;
    }

    /**
     * @param fmt
     * @param res
     * @return
     */
    @Override
    public String getTime(SLE_TimeFmt fmt, SLE_TimeRes res)
    {
        return getEETime(fmt, res, false);
    }

    /**
     * @param fmt
     * @param res
     * @return
     */
    @Override
    public String getDateAndTime(SLE_TimeFmt fmt, SLE_TimeRes res)
    {
        return getEETime(fmt, res, true);
    }

    /**
     * @param fmt
     * @return
     */
    @Override
    public String getDateAndTime(SLE_TimeFmt fmt)
    {
        return getDateAndTime(fmt, SLE_TimeRes.sleTR_seconds);
    }

    /**
     * @param fmt
     * @param res
     * @param isDateAndTime
     * @return
     */
    private String getEETime(SLE_TimeFmt fmt, SLE_TimeRes res, boolean isDateAndTime)
    {
        String ret = "";
        String tmp1 = "";

        EE_TIME_Prec argres = EE_TIME_Prec.eeTIME_PrecSECONDS;

        if ((res == SLE_TimeRes.sleTR_seconds) || (res == SLE_TimeRes.sleTR_minutes))
        {
            argres = EE_TIME_Prec.eeTIME_PrecSECONDS;
        }
        else if (res == SLE_TimeRes.sleTR_hundredMilliSec)
        {
            argres = EE_TIME_Prec.eeTIME_PrecHUNDRMILLISEC;
        }
        else if (res == SLE_TimeRes.sleTR_tenMilliSec)
        {
            argres = EE_TIME_Prec.eeTIME_PrecTENMILLISEC;
        }
        else if (res == SLE_TimeRes.sleTR_milliSec)
        {
            argres = EE_TIME_Prec.eeTIME_PrecMILLISEC;
        }
        else
        {
            argres = EE_TIME_Prec.eeTIME_PrecMILLISEC;
        }

        try
        {
            if (fmt == SLE_TimeFmt.sleTF_dayOfMonth)
            {
                if (isDateAndTime)
                {
                    tmp1 = this.ee_time.getDateAndTimeCCSDS(EE_TIME_Fmt.eeTIME_FmtA, argres);
                }
                else
                {
                    tmp1 = this.ee_time.getTimeCCSDS(EE_TIME_Fmt.eeTIME_FmtA, argres);
                }
            }
            else
            {
                if (isDateAndTime)
                {
                    tmp1 = this.ee_time.getDateAndTimeCCSDS(EE_TIME_Fmt.eeTIME_FmtB, argres);
                }
                else
                {
                    tmp1 = this.ee_time.getTimeCCSDS(EE_TIME_Fmt.eeTIME_FmtB, argres);
                }
            }
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }

        int len = tmp1.length();
        // If resolution is minutes, must remove the seconds
        if (res == SLE_TimeRes.sleTR_minutes)
        {
            // remove the seconds: remove ":00"
            if (len >= 3)
            {
                len -= 3;
            }
        }
        else
        {
            if ((res != SLE_TimeRes.sleTR_hundredMilliSec) && (res != SLE_TimeRes.sleTR_tenMilliSec)
                && (res != SLE_TimeRes.sleTR_milliSec) && (res != SLE_TimeRes.sleTR_seconds))
            {
                // Fill string with picoseconds informations, depending on the
                // required
                // resolution
                String picos = Long.toString(this.picoseconds);
                // Pad with zeroes in the front
                if (picos.length() < 9)
                {
                    String picoTmp = "";
                    int k = 9 - picos.length();
                    for (int i = 0; i < k; i++)
                    {
                        picoTmp += '0';
                    }

                    picoTmp += picos;
                    // Replace
                    picos = picoTmp;
                }

                String fullTime = tmp1;
                switch (res)
                {
                case sleTR_hundredMicroSec:
                {
                    fullTime += picos.subSequence(0, 1);
                }
                    break;
                case sleTR_tenMicroSec:
                {
                    fullTime += picos.subSequence(0, 2);
                }
                    break;
                case sleTR_microSec:
                {

                    fullTime += picos.subSequence(0, 3);
                }
                    break;
                case sleTR_hundredNanoSec:
                {
                    fullTime += picos.subSequence(0, 4);
                }
                    break;
                case sleTR_tenNanoSec:
                {
                    fullTime += picos.subSequence(0, 5);
                }
                    break;
                case sleTR_nanoSec:
                {
                    fullTime += picos.subSequence(0, 6);
                }
                    break;
                case sleTR_hundredPicoSec:
                {
                    fullTime += picos.subSequence(0, 7);
                }
                    break;
                case sleTR_tenPicoSec:
                {
                    fullTime += picos.subSequence(0, 8);
                }
                    break;
                case sleTR_picoSec:
                {
                    fullTime += picos.subSequence(0, 9);
                }
                    break;
                default:
                    break;
                }

                len = fullTime.length();
                tmp1 = fullTime;

            }
        }
        // must add 'Z' at end of timestamp information
        ret = tmp1 + 'Z';
        return ret;
    }

    @Override
    public void update()
    {
        try
        {
            byte[] tmp = this.source.getCurrentTime();
            setCDS(tmp);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }
    }

    /**
     * @return
     */
    @Override
    public ISLE_Time copy()
    {
        EE_SLE_Time p_sleTime = new EE_SLE_Time(this);
        return p_sleTime;
    }

    /**
     * @param time
     * @throws SleApiException
     */
    @Override
    public void setCDSToPicosecondsRes(byte[] time) throws SleApiException
    {
        byte[] tmp = time;
        // Decode picoseconds
        long pSec = EE_IntegralEncoder.decodeUnsignedMSBFirst(tmp, 6, 4); // SLEAPIJ-62 use the correct index 6 (not 0) for the pico seconds
        // Save picoseconds
        this.picoseconds = pSec;
        this.picosecondsUsed = true;
        // Save the complete information (reduced to usec) inside the time
        // object,
        // but use only millisecond information from that
        byte[] tmp1 = new byte[8];
        // Copy the common information
        for (int i = 0; i <= 6; i++)
        {
            tmp1[i] = time[i];
        }
        // Calculate microseconds
        pSec /= 1000000;
        // Store microseconds information
        EE_IntegralEncoder.encodeUnsignedMSBFirst(tmp1, 6, 2, pSec);
        this.ee_time.setCDSlevel1(tmp1);
    }

    /**
     * @return
     */
    @Override
    public byte[] getCDSToPicosecondsRes()
    {
        int ci_encodeBLen = 10;
        byte[] time = new byte[ci_encodeBLen];
        try
        {
            this.ee_time.getCDSlevel1(time);
            // Retrieve picoseconds
            long pSec = this.picoseconds;
            if (!this.picosecondsUsed)
            {
                // If picoseconds are not used, the resolution has to be
                // microseconds
                pSec /= 1000000;
                // Now back to pico, but with loss of precision
                pSec *= 1000000;
            }
            // Encode in 4 bytes
            EE_IntegralEncoder.encodeUnsignedMSBFirst(time, 6, 4, pSec);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }
        return time;
    }

    /**
     * @return
     */
    @Override
    public boolean getPicosecondsResUsed()
    {
        return this.picosecondsUsed;
    }

    /**
     * @return
     */
    public long getPicoseconds()
    {
        return this.picoseconds;
    }

    /**
     * @param picoseconds
     */
    public void setPicoseconds(long picoseconds)
    {
        this.picoseconds = picoseconds;
    }

    /**
     * @return
     */
    public boolean getPicosecondUsed()
    {
        return this.picosecondsUsed;
    }

    /**
     * @param isUsed
     */
    public void setPicosecondUsed(boolean isUsed)
    {
        this.picosecondsUsed = isUsed;
    }

    /**
     * @param t2
     * @return
     */
    @Override
    public int compareTo(ISLE_Time t2)
    {
        return compareTo(this, t2);
    }

    /**
     * @param t1
     * @param t2
     * @return
     */
    private int compareTo(EE_SLE_Time t1, ISLE_Time t2)
    {
        if (t1.equals(t2))
        {
            return 0;
        }

        byte[] thisCDS = t1.getCDSToPicosecondsRes();
        byte[] timeCDS = t2.getCDSToPicosecondsRes();

        try
        {
            // Check for the day
            long timeDays = EE_IntegralEncoder.decodeUnsignedMSBFirst(timeCDS, 0, 2);
            long thisDays = EE_IntegralEncoder.decodeUnsignedMSBFirst(thisCDS, 0, 2);

            if (thisDays < timeDays)
            {
                return -1;
            }
            else if (thisDays > timeDays)
            {
                return 1;
            }
            else
            {
                // Check for milliseconds
                long timeMSec = EE_IntegralEncoder.decodeUnsignedMSBFirst(timeCDS, 2, 4);
                long thisMSec = EE_IntegralEncoder.decodeUnsignedMSBFirst(thisCDS, 2, 4);

                if (thisMSec < timeMSec)
                {
                    return -1;
                }
                else if (thisMSec > timeMSec)
                {
                    return 1;
                }
                else
                {
                    // Check for picoseconds
                    long timePSec = EE_IntegralEncoder.decodeUnsignedMSBFirst(timeCDS, 6, 4);
                    long thisPSec = EE_IntegralEncoder.decodeUnsignedMSBFirst(thisCDS, 6, 4);

                    if (thisPSec < timePSec)
                    {
                        return -1;
                    }
                    else if (thisPSec > timePSec)
                    {
                        return 1;
                    }
                }
            }
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }

        return 0;
    }

    /**
     * @param time
     * @return
     */
    @Override
    public double subtract(ISLE_Time time)
    {
        if (this.compareTo(time) < 0)
        {
            return -(time.subtract(this));
        }

        long sec;
        long frc;
        double res;
        EE_Time tmpTime = new EE_Time();

        byte[] time_cds = time.getCDS();
        try
        {
            tmpTime.setCDSlevel1(time_cds);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }

        sec = (this.ee_time.subtractTime(tmpTime)).getSeconds();

        // Now calculate picoseconds difference
        byte[] timeCDS = time.getCDSToPicosecondsRes();
        byte[] thisCDS = getCDSToPicosecondsRes();

        long timePSec = 0;
        long thisPSec = 0;
        try
        {
            timePSec = EE_IntegralEncoder.decodeUnsignedMSBFirst(timeCDS, 6, 4);
            thisPSec = EE_IntegralEncoder.decodeUnsignedMSBFirst(thisCDS, 6, 4);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }

        // Calculate the picoseconds difference
        thisPSec += 1000000000;
        long psecdiff = thisPSec - timePSec;
        frc = (this.ee_time.subtractTime(tmpTime)).fractions(EE_TIME_Prec.eeTIME_PrecMICROSEC);
        // Avoid inner rounding problems
        frc /= 1000;
        // If the difference is negative, remove one millisecond
        if (psecdiff < 1000000000)
        {
            if (frc > 0)
            {
                frc--;
            }
            else
            {
                frc = 999;
                sec--;
            }
        }
        else
        {
            psecdiff -= 1000000000;
        }

        // in psecdiff there is the number of picoseconds to be divided by 1
        // trillion
        // before the addition to sec
        double psecDouble = psecdiff / 1E+12;

        res = sec + frc / 10E+2 + psecDouble;

        return res;
    }

    @Override
    public String toString()
    {
        return "EE_SLE_Time [picoseconds=" + this.picoseconds + ", picosecondsUsed=" + this.picosecondsUsed
               + ", ee_time=" + this.ee_time + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.ee_time == null) ? 0 : this.ee_time.hashCode());
        result = prime * result + (int) (this.picoseconds ^ (this.picoseconds >>> 32));
        result = prime * result + (this.picosecondsUsed ? 1231 : 1237);
        result = prime * result + ((this.source == null) ? 0 : this.source.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        EE_SLE_Time other = (EE_SLE_Time) obj;
        if (this.ee_time == null)
        {
            if (other.ee_time != null)
            {
                return false;
            }
        }
        else if (!this.ee_time.equals(other.ee_time))
        {
            return false;
        }
        if (this.picoseconds != other.picoseconds)
        {
            return false;
        }
        if (this.picosecondsUsed != other.picosecondsUsed)
        {
            return false;
        }
        if (this.source == null)
        {
            if (other.source != null)
            {
                return false;
            }
        }
        else if (!this.source.equals(other.source))
        {
            return false;
        }
        return true;
    }
}
