package esa.sle.impl.api.apiut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_TimeSource;
import ccsds.sle.api.isle.it.SLE_TimeFmt;
import ccsds.sle.api.isle.it.SLE_TimeRes;
import ccsds.sle.api.isle.iutl.ISLE_Time;

public class EE_SLE_TimeTest
{

    private static final Logger LOG = Logger.getLogger(EE_SLE_TimeTest.class.getName());

    private static ISLE_Time time = null;

    private static ISLE_Time timeAux = null;

    private static ISLE_Time timeAux2 = null;


    @BeforeClass
    public static void beforeClass()
    {

        ISLE_TimeSource timeSource = new EE_SLE_TimeSource();
        EE_SLE_Time sleTIme = new EE_SLE_Time(timeSource);

        EE_SLE_Time sleTImeAux = new EE_SLE_Time(timeSource);
        EE_SLE_Time sleTImeAux2 = new EE_SLE_Time(timeSource);

        time = sleTIme.queryInterface(ISLE_Time.class);
        timeAux = sleTImeAux.queryInterface(ISLE_Time.class);
        timeAux2 = sleTImeAux2.queryInterface(ISLE_Time.class);
    }

    @Test
    public void testSetDateAndTime()
    {

        try
        {
            time.setDateAndTime("2008-08-11T17:50:00");
            time.setDateAndTime("2002-126T00:00");
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            assertEquals(e, HRESULT.S_OK);
        }

    }

    @Test
    public void testSetTime()
    {
        try
        {
            time.setTime("10:12:41");
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            assertEquals(e, HRESULT.S_OK);
        }
    }

    @Test
    public void testGetDate()
    {
        try
        {
            time.setDateAndTime("2008-08-11T17:50:00");
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            assertEquals(e, HRESULT.S_OK);
        }
        assertTrue(time.getDate(SLE_TimeFmt.sleTF_dayOfMonth).equals("2008-08-11"));
        assertTrue(time.getDate(SLE_TimeFmt.sleTF_dayOfYear).equals("2008-224"));
        try
        {
            time.setDateAndTime("2002-126T00:00");
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            assertEquals(e, HRESULT.S_OK);
        }
        assertTrue(time.getDate(SLE_TimeFmt.sleTF_dayOfMonth).equals("2002-05-06"));
        assertTrue(time.getDate(SLE_TimeFmt.sleTF_dayOfYear).equals("2002-126"));
    }

    @Test
    public void testGetTime()
    {
        try
        {
            time.setDateAndTime("2008-08-11T17:50:00.327372321");
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            assertEquals(e, HRESULT.S_OK);
        }
        assertTrue(time.getTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_minutes).equals("17:50:00Z"));
        assertTrue(time.getTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_milliSec).equals("17:50:00.327Z"));
        assertTrue(time.getTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_hundredMilliSec).equals("17:50:00.3Z"));
        assertTrue(time.getTime(SLE_TimeFmt.sleTF_dayOfYear, SLE_TimeRes.sleTR_minutes).equals("17:50:00Z"));
        assertTrue(time.getTime(SLE_TimeFmt.sleTF_dayOfYear, SLE_TimeRes.sleTR_milliSec).equals("17:50:00.327Z"));

        try
        {
            time.setDateAndTime("2002-126T11:32:11.333");
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            assertEquals(e, HRESULT.S_OK);
        }
        assertTrue(time.getTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_minutes).equals("11:32:11Z"));
        assertTrue(time.getTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_milliSec).equals("11:32:11.333Z"));
        assertTrue(time.getTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_hundredMilliSec).equals("11:32:11.3Z"));
        assertTrue(time.getTime(SLE_TimeFmt.sleTF_dayOfYear, SLE_TimeRes.sleTR_minutes).equals("11:32:11Z"));
        assertTrue(time.getTime(SLE_TimeFmt.sleTF_dayOfYear, SLE_TimeRes.sleTR_milliSec).equals("11:32:11.333Z"));

    }

    @Test
    public void testGetDateAndTime()
    {
        try
        {
            time.setDateAndTime("2008-08-11T17:50:00.327372321");
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            assertEquals(e, HRESULT.S_OK);
        }
        assertTrue(time.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth).equals("2008-08-11T17:50:00Z"));
        assertTrue(time.getDateAndTime(SLE_TimeFmt.sleTF_dayOfYear).equals("2008-224T17:50:00Z"));

        try
        {
            time.setDateAndTime("2002-126T11:32:11.333");
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            assertEquals(e, HRESULT.S_OK);
        }
        assertTrue(time.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth).equals("2002-05-06T11:32:11Z"));
        assertTrue(time.getDateAndTime(SLE_TimeFmt.sleTF_dayOfYear).equals("2002-126T11:32:11Z"));
    }

    @Test
    public void testCopy()
    {
        ISLE_Time timecpy = time.copy();
        assertTrue(timecpy.equals(time));
    }

    @Test
    public void testCompareTo()
    {
        try
        {
            time.setDateAndTime("2008-08-11T17:50:00.327372321");
            timeAux.setDateAndTime("2008-02-12T17:23:12.32");
            timeAux2.setDateAndTime("2008-02-12T17:23:12.32");
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            assertEquals(e, HRESULT.S_OK);
        }
        assertFalse(time.equals(timeAux));
        assertEquals(time.compareTo(timeAux), 1);
        assertEquals(timeAux.compareTo(time), -1);
        assertEquals(timeAux.compareTo(timeAux2), 0);
    }
}
