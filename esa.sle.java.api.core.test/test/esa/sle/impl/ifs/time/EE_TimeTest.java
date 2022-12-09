package esa.sle.impl.ifs.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Calendar;
import java.util.regex.Pattern;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ccsds.sle.api.isle.exception.SleApiException;
import esa.sle.impl.ifs.gen.EE_IntegralEncoder;

public class EE_TimeTest
{

    public final static int n = 6;

    static EE_Time[] ee_time = new EE_Time[n];

    static EE_Duration duration;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        duration = new EE_Duration(500, 12, EE_TIME_Prec.eeTIME_PrecSECONDS);
        ee_time[0] = new EE_Time(500, 12);
        ee_time[1] = new EE_Time(500, 12);
        ee_time[2] = new EE_Time(EE_TIME_Prec.eeTIME_PrecMICROSEC);
        ee_time[3] = new EE_Time(EE_TIME_Prec.eeTIME_PrecTENMILLISEC);
        ee_time[4] = new EE_Time(EE_TIME_Prec.eeTIME_PrecMICROSEC);
        ee_time[5] = new EE_Time(600, 134354);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        for (int i = 0; i < n; i++)
        {
            ee_time[i] = null;
        }
        duration = null;
    }

    @Before
    public void before()
    {
        duration = new EE_Duration(500, 12, EE_TIME_Prec.eeTIME_PrecSECONDS);
        ee_time[0] = new EE_Time(500, 12);
        ee_time[1] = new EE_Time(500, 12);
        ee_time[2] = new EE_Time(EE_TIME_Prec.eeTIME_PrecMICROSEC);
        ee_time[3] = new EE_Time(EE_TIME_Prec.eeTIME_PrecTENMILLISEC);
        ee_time[4] = new EE_Time(EE_TIME_Prec.eeTIME_PrecMICROSEC);
        ee_time[5] = new EE_Time(600, 13);
    }

    @Test
    public final void testEqualsObject()
    {
        assertTrue(ee_time[0].equals(ee_time[1]));
    }

    @Test
    public final void testCompareTo()
    {
        assertTrue(ee_time[0].compareTo(ee_time[1]) == 0);
        assertTrue(ee_time[0].compareTo(ee_time[2]) < 0);
        assertTrue(ee_time[3].compareTo(ee_time[1]) > 0);
    }

    @Test
    public final void testAdd()
    {
        EE_Time a = new EE_Time(500, 12);
        a = a.add(duration);
        assertTrue(a.equals(new EE_Time(1000, 24, EE_TIME_Prec.eeTIME_PrecSECONDS)));
    }

    @Test
    public final void testSubtractTime()
    {
        EE_Time a = new EE_Time(500, 12);
        EE_Duration durationAux = new EE_Duration(0, 0, EE_TIME_Prec.eeTIME_PrecSECONDS);
        duration = a.subtractTime(ee_time[0]);
        assertTrue(duration.equals(durationAux));
        assertTrue(duration.isEqualTo(durationAux));
    }

    @Test
    public final void testPrecision()
    {
        assertEquals(EE_TIME_Prec.eeTIME_PrecSECONDS, ee_time[0].precision());
        assertEquals(EE_TIME_Prec.eeTIME_PrecMICROSEC, ee_time[2].precision());
        assertEquals(EE_TIME_Prec.eeTIME_PrecMICROSEC, ee_time[4].precision());
        assertEquals(EE_TIME_Prec.eeTIME_PrecTENMILLISEC, ee_time[3].precision());
    }

    @Test
    public final void testSubtractDuration()
    {
        EE_Time a = ee_time[5].subtractDuration(duration);
        EE_Time b = new EE_Time(100, 1);
        assertTrue(a.equals(b));
    }

    @Test
    public final void testGet_DateAndTimeCCSDS() throws SleApiException
    {
        String strAA = ee_time[0].getDateAndTimeCCSDS(EE_TIME_Fmt.eeTIME_FmtA, EE_TIME_Prec.eeTIME_PrecSECONDS);
        String strBA = ee_time[1].getDateAndTimeCCSDS(EE_TIME_Fmt.eeTIME_FmtA, EE_TIME_Prec.eeTIME_PrecHUNDRMILLISEC);
        String strCA = ee_time[2].getDateAndTimeCCSDS(EE_TIME_Fmt.eeTIME_FmtA, EE_TIME_Prec.eeTIME_PrecTENMILLISEC);
        String strAB = ee_time[0].getDateAndTimeCCSDS(EE_TIME_Fmt.eeTIME_FmtB, EE_TIME_Prec.eeTIME_PrecSECONDS);
        String strBB = ee_time[3].getDateAndTimeCCSDS(EE_TIME_Fmt.eeTIME_FmtB, EE_TIME_Prec.eeTIME_PrecHUNDRMILLISEC);
        String strCB = ee_time[1].getDateAndTimeCCSDS(EE_TIME_Fmt.eeTIME_FmtB, EE_TIME_Prec.eeTIME_PrecTENMILLISEC);
        Pattern pFmtA = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.?[0-9]*");
        Pattern pFmtB = Pattern.compile("\\d{4}-\\d{3}T\\d{2}:\\d{2}:\\d{2}\\.?[0-9]*");
        assertTrue(pFmtA.matcher(strAA).matches());
        assertTrue(pFmtA.matcher(strBA).matches());
        assertTrue(pFmtA.matcher(strCA).matches());
        assertTrue(pFmtB.matcher(strAB).matches());
        assertTrue(pFmtB.matcher(strBB).matches());
        assertTrue(pFmtB.matcher(strCB).matches());
    }

    @Test
    public final void testGet_TimeCCSDS() throws SleApiException
    {
        String strAA = ee_time[0].getTimeCCSDS(EE_TIME_Fmt.eeTIME_FmtA, EE_TIME_Prec.eeTIME_PrecSECONDS);
        Pattern localPattern = Pattern.compile("\\d{2}:\\d{2}:\\d{2}");
        assertTrue(localPattern.matcher(strAA).matches());
    }

    @Test
    public final void testGet_DateCCSDS() throws SleApiException
    {
        String a = ee_time[0].getDateCCSDS(EE_TIME_Fmt.eeTIME_FmtA);
        String b = ee_time[5].getDateCCSDS(EE_TIME_Fmt.eeTIME_FmtB);
        Pattern fmtA = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        Pattern fmtB = Pattern.compile("\\d{4}-\\d{3}");
        assertTrue(fmtA.matcher(a).matches());
        assertTrue(fmtB.matcher(b).matches());
    }
    
    @Test
    public final void test_setCDSToPicosecondsRes() throws SleApiException
    {
        int ci_encodeBLen = 10;
        byte[] time_cds = new byte[ci_encodeBLen];
        ee_time[5].getCDSlevel1(time_cds);
              
        long pSec = EE_IntegralEncoder.decodeUnsignedMSBFirst(time_cds, 6, 4);
        System.out.println(pSec);
        byte[] tmp = EE_IntegralEncoder.encodeUnsignedMSBFirst(ee_time[5].getCDSlevel1(time_cds), 6, 4, pSec);
        assertEquals(17, tmp[0]);
        assertEquals(31, tmp[1]);
        assertEquals(0, tmp[2]);
        assertEquals(9, tmp[3]);
        assertEquals(90, tmp[4]);
        assertEquals(-120, tmp[5]);
        assertEquals(0, tmp[6]);
        assertEquals(0, tmp[7]);
        assertEquals(0, tmp[8]);
        assertEquals(0, tmp[9]);
    }

    @Test
    public final void testSet_CCSDSDateAndTime() throws SleApiException, ParseException
    {
        String a = ee_time[5].getDateAndTimeCCSDS(EE_TIME_Fmt.eeTIME_FmtA, EE_TIME_Prec.eeTIME_PrecHUNDRNANOSEC);
        String b = ee_time[0].getDateAndTimeCCSDS(EE_TIME_Fmt.eeTIME_FmtB, EE_TIME_Prec.eeTIME_PrecHUNDRNANOSEC);
        EE_Time timeA = new EE_Time(0, 0);
        EE_Time timeB = new EE_Time(0, 0);
        timeA.setCCSDSDateAndTime(a);
        timeB.setCCSDSDateAndTime(b);
        assertTrue(ee_time[5].equals(timeA));
        assertTrue(ee_time[0].equals(timeB));
    }

    @Test
    public final void testSet_CCSDSTime() throws SleApiException
    {
        EE_Time timeA = new EE_Time(1, 0);
        String b = "00:10:00.0000000";
        timeA.setCCSDSTime(b);
        Calendar gregCal = Calendar.getInstance();
        gregCal.set(Calendar.HOUR_OF_DAY, 0);
        gregCal.set(Calendar.MINUTE, 0);
        gregCal.set(Calendar.SECOND, 0);
        gregCal.set(Calendar.SECOND, 600);
        EE_Time timeB = new EE_Time(gregCal.getTimeInMillis() / 1000, 0);
        assertTrue(timeB.equals(timeA));
    }

    @Test
    public final void getCDSlevel1() throws SleApiException
    {
        int ci_encodeBLen = 8;
        byte[] time_cds = new byte[ci_encodeBLen];
        ee_time[5].getCDSlevel1(time_cds);
        assertEquals(17, time_cds[0]);
        assertEquals(31, time_cds[1]);
        assertEquals(0, time_cds[2]);
        assertEquals(9, time_cds[3]);
        assertEquals(90, time_cds[4]);
        assertEquals(-120, time_cds[5]);
        assertEquals(0, time_cds[6]);
        assertEquals(0, time_cds[7]);

    }
}
