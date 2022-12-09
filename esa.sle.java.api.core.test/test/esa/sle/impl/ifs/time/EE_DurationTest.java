package esa.sle.impl.ifs.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class EE_DurationTest
{

    public static int lenghtTest = EE_TIME_Prec.values().length - 1;

    public static long[] secs = new long[lenghtTest];

    static EE_Duration[] durations = new EE_Duration[lenghtTest];

    public static long[] frac = new long[lenghtTest];

    public int[] precision = new int[lenghtTest];

    static List<EE_TIME_Prec> precs = new ArrayList<EE_TIME_Prec>();

    static Random r = new Random();


    @BeforeClass
    public static void beforeClass()
    {
        int i = 0;
        EE_TIME_Prec[] auxprec = EE_TIME_Prec.values();
        int k = 0;
        for (EE_TIME_Prec p : auxprec)
        {
            if (k == auxprec.length - 1)
            {
                break;
            }
            precs.add(p);
            k++;
        }

        for (EE_TIME_Prec precision : precs)
        {
            System.out.println("NEXT object " + i);
            System.out.println("CODE precision " + precision.getCode());
            secs[i] = ((i == 1) ? 0L : ((i == 2) ? 1L : (long) r.nextInt(Integer.MAX_VALUE)));
            System.out.println("generated secs[" + i + "] " + secs[i]);
            frac[i] = generateFracInBounds(precision);
            System.out.println("generated frac[" + i + "] " + frac[i]);
            System.out.println("precision " + precision);
            durations[i] = new EE_Duration(secs[i], frac[i], precision);
            i++;
            System.out.println();
        }
    }

    /**
     * Dereference
     */
    @AfterClass
    public static void afterClass()
    {
        for (int i = 0; i < durations.length; i++)
        {
            durations[i] = null;
        }
        precs = null;
    }

    /**
     * Test method for
     * {@link esa.sle.impl.ifs.time.EE_Duration#fractions(esa.sle.impl.ifs.time.EE_TIME_Prec)}
     * .
     */
    @Test
    public final void testFractions()
    {
        for (int i = 0; i < durations.length; i++)
        {
            EE_TIME_Prec prec = durations[i].getPrecision();
            System.out.println("testFractions prec:" + prec);
            String strDurNanos = getFracNanoSecondsRepresentation(String.valueOf(frac[i]), i);
            long durNanos = Long.parseLong(strDurNanos);
            System.out.println("testFractions durNanos:" + durNanos);
            durNanos %= (long) Math.pow(10, EE_TIME_Prec.eeTIME_PrecNANOSEC.getCode());
            System.out.println("testFractions durNanos %:" + durNanos);
            long factor = (long) Math.pow(10, (EE_TIME_Prec.eeTIME_PrecNANOSEC.getCode() - prec.getCode()));
            // not 100% real test here, probably this mehtod should be deleted.
            long unit = (durNanos + (factor / 2)) / factor;
            assertEquals(unit, durations[i].fractions(prec));
        }
    }

    /**
     * Genarates a frac value number such that the number of digits of this
     * number + precision don exceed the size of Long.MaxValue.
     * 
     * @param eetimePrecseconds
     * @return
     */
    private static long generateFracInBounds(EE_TIME_Prec prec)
    {
        long result;
        int numberOfDigitsDurNanos = EE_TIME_Prec.eeTIME_PrecNANOSEC.getCode() - prec.getCode();
        int boundLimitMax = getNumberOfDigits(Long.MAX_VALUE);
        int maxBoundForFrac = boundLimitMax - numberOfDigitsDurNanos - 1;
        result = (long) (r.nextDouble() * (Math.pow(10, maxBoundForFrac))) - 1;
        ;
        return result;
    }

    /**
     * Gets the number of digits of the parameter.
     * 
     * @param a
     * @return
     */
    private static int getNumberOfDigits(long a)
    {
        int result = 1;
        a = Math.abs(a);
        while (a / 10 != 0)
        {
            a /= 10;
            result++;
        }
        return result;
    }

    /**
     * Test method for {@link esa.sle.impl.ifs.time.EE_Duration#getSeconds()}.
     */
    @Test
    public final void testGetSeconds()
    {
        for (int i = 0; i < durations.length; i++)
        {
            long secondsNew = getNewSecondsTest(i);
            assertEquals(secondsNew, durations[i].getSeconds());
        }
    }

    private long getNewSecondsTest(int i)
    {
        String strFrac = "";
        long secondsNew = secs[i];
        strFrac = getFracNanoSecondsRepresentation(String.valueOf(frac[i]), i);
        long nanosGranularity = Long.parseLong(strFrac);
        secondsNew += nanosGranularity / Math.pow(10, EE_TIME_Prec.eeTIME_PrecNANOSEC.getCode());
        return secondsNew;
    }

    private String getFracNanoSecondsRepresentation(String strFracCP, int i)
    {
        String strFrac = new String(strFracCP);
        int lenghtStr = strFrac.length();
        int factorNorm = (EE_TIME_Prec.eeTIME_PrecNANOSEC.getCode() - durations[i].getPrecision().getCode());
        System.out.println("factorNorm " + factorNorm);
        int boudSize = lenghtStr + factorNorm;
        if (lenghtStr > boudSize)
        {
            System.out.println("cut precision " + lenghtStr + " > " + boudSize);
            strFrac = strFrac.substring(0, boudSize);
        }
        else if (lenghtStr < boudSize)
        {
            System.out.println("add precision " + lenghtStr + " < " + boudSize);
            String extra = getExtraZeros(boudSize - lenghtStr);
            strFrac += extra;
        }
        return strFrac;
    }

    private String getExtraZeros(int i)
    {
        String result = "";
        for (int j = 0; j < i; j++)
        {
            result += "0";
        }
        return result;
    }

    /**
     * Test method for {@link esa.sle.impl.ifs.time.EE_Duration#getPrecision()}.
     */
    @Test
    public final void testGetPrecision()
    {
        for (int i = 0; i < durations.length; i++)
        {
            assertEquals(precs.get(i), durations[i].getPrecision());
        }
    }

    /**
     * Test method for
     * {@link esa.sle.impl.ifs.time.EE_Duration#setPrecision(esa.sle.impl.ifs.time.EE_TIME_Prec)}
     * .
     */
    @Test
    public final void testSetPrecision()
    {
        durations[0].setPrecision(EE_TIME_Prec.eeTIME_PrecMILLISEC);
        assertEquals(EE_TIME_Prec.eeTIME_PrecMILLISEC, durations[0].getPrecision());
    }

    /**
     * Test method for
     * {@link esa.sle.impl.ifs.time.EE_Duration#equals(java.lang.Object)}.
     */
    @Test
    public final void testEqualsObject()
    {
        assertFalse(durations[0].equals(durations[4]));
        EE_Duration durationA = new EE_Duration(500, 12, EE_TIME_Prec.eeTIME_PrecMICROSEC);
        EE_Duration durationB = new EE_Duration(500, 12, EE_TIME_Prec.eeTIME_PrecMICROSEC);
        assertTrue(durationA.isEqualTo(durationB));
        assertTrue(durationA.equals(durationA));
    }

    /**
     * Test method for
     * {@link esa.sle.impl.ifs.time.EE_Duration#isEqualTo(esa.sle.impl.ifs.time.EE_Duration)}
     * .
     */
    @Test
    public final void testIsEqualTo()
    {
        EE_Duration durationA = new EE_Duration(500, 12, EE_TIME_Prec.eeTIME_PrecMICROSEC);
        EE_Duration durationB = new EE_Duration(500, 12, EE_TIME_Prec.eeTIME_PrecMICROSEC);
        assertTrue(durationA.isEqualTo(durationB));
    }

    /**
     * Test method for
     * {@link esa.sle.impl.ifs.time.EE_Duration#add(esa.sle.impl.ifs.time.EE_Duration)}
     * .
     */
    @Test
    public final void testAdd()
    {
        EE_Duration a = new EE_Duration(500, 12, EE_TIME_Prec.eeTIME_PrecMICROSEC);
        EE_Duration b = new EE_Duration(500, 12, EE_TIME_Prec.eeTIME_PrecNANOSEC);
        EE_Duration c = new EE_Duration(0, 0, EE_TIME_Prec.eeTIME_PrecMICROSEC);
        c = a.add(b);
        assertEquals(500, a.getSeconds());
        assertEquals(1000, c.getSeconds());
    }

    /**
     * Test method for
     * {@link esa.sle.impl.ifs.time.EE_Duration#subtract(esa.sle.impl.ifs.time.EE_Duration)}
     * .
     */
    @Test
    public final void testSubtract()
    {
        EE_Duration a = new EE_Duration(500, 0, EE_TIME_Prec.eeTIME_PrecTENMILLISEC);
        EE_Duration b = new EE_Duration(200, 0, EE_TIME_Prec.eeTIME_PrecSECONDS);
        EE_Duration c = new EE_Duration(0, 0, EE_TIME_Prec.eeTIME_PrecMILLISEC);
        c = a.subtract(b);
        assertEquals(500, a.getSeconds());
        assertEquals(300, c.getSeconds());
        assertEquals(EE_TIME_Prec.eeTIME_PrecTENMILLISEC, a.getPrecision());
        assertEquals(EE_TIME_Prec.eeTIME_PrecSECONDS, b.getPrecision());
        assertEquals(EE_TIME_Prec.eeTIME_PrecSECONDS, c.getPrecision());

    }

    /**
     * Test method for {@link esa.sle.impl.ifs.time.EE_Duration#divide(long)}.
     */
    @Test
    public final void testDivide()
    {
        EE_Duration a = new EE_Duration(800, 0, EE_TIME_Prec.eeTIME_PrecTENMILLISEC);
        EE_Duration b = new EE_Duration(200, 0, EE_TIME_Prec.eeTIME_PrecSECONDS);
        EE_Duration c = new EE_Duration(0, 0, EE_TIME_Prec.eeTIME_PrecMILLISEC);
        c = a.divide(b.getSeconds());
        assertEquals(800, a.getSeconds());
        assertEquals(4, c.getSeconds());
    }

    /**
     * Test method for {@link esa.sle.impl.ifs.time.EE_Duration#multiply(long)}.
     */
    @Test
    public final void testMultiply()
    {
        EE_Duration a = new EE_Duration(800, 0, EE_TIME_Prec.eeTIME_PrecTENMILLISEC);
        EE_Duration b = new EE_Duration(200, 0, EE_TIME_Prec.eeTIME_PrecSECONDS);
        EE_Duration c = new EE_Duration(0, 0, EE_TIME_Prec.eeTIME_PrecMILLISEC);
        c = a.multiply(2);
        assertEquals(800, a.getSeconds());
        assertEquals(1600, c.getSeconds());
        assertEquals(200, b.getSeconds());
    }

    /**
     * Test method for
     * {@link esa.sle.impl.ifs.time.EE_Duration#compareTo(esa.sle.impl.ifs.time.EE_Duration)}
     * . Still to implement <= and >=
     */
    @Test
    public final void testCompareTo()
    {
        EE_Duration a = new EE_Duration(800, 1, EE_TIME_Prec.eeTIME_PrecTENMILLISEC);
        EE_Duration aa = new EE_Duration(800, 1, EE_TIME_Prec.eeTIME_PrecTENMILLISEC);
        EE_Duration b = new EE_Duration(200, 2, EE_TIME_Prec.eeTIME_PrecSECONDS);

        assertTrue(a.compareTo(b) == 1);
        assertTrue(aa.compareTo(aa) == 0);
        assertFalse(aa.compareTo(b) == 0);
        assertTrue(b.compareTo(a) == -1);
    }

}
