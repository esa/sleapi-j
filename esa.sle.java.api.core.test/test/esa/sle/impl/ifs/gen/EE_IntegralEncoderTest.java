package esa.sle.impl.ifs.gen;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import ccsds.sle.api.isle.exception.SleApiException;

public class EE_IntegralEncoderTest
{

    private static final Logger LOG = Logger.getLogger(EE_IntegralEncoderTest.class.getName());


    @Test
    public void testEncodeDecode()
    {

        long days = 21201;
        long millisec = 46104000;
        long microsec = 544;
        byte[] a = new byte[9];
        try
        {
            System.out.println("ulMicroSecs decoded: " + days);
            System.out.println("ulMicroSecs decoded: " + millisec);
            System.out.println("ulMicroSecs decoded: " + microsec);

            System.out.println("***************************************** ");
            a = EE_IntegralEncoder.encodeUnsignedMSBFirst(a, 0, 2, days);
            a = EE_IntegralEncoder.encodeUnsignedMSBFirst(a, 2, 4, millisec);
            a = EE_IntegralEncoder.encodeUnsignedMSBFirst(a, 6, 2, microsec);

            for (byte element : a)
            {
                System.out.print(element + " ");
            }

            System.out.println("\n***************************************** ");

            long ulDays = EE_IntegralEncoder.decodeUnsignedMSBFirst(a, 0, 2);
            long ulMilliSecs = EE_IntegralEncoder.decodeUnsignedMSBFirst(a, 2, 4);
            long ulMicroSecs = EE_IntegralEncoder.decodeUnsignedMSBFirst(a, 6, 2);

            System.out.println("ulDays decoded: " + ulDays);
            System.out.println("ulMilliSecs decoded: " + ulMilliSecs);
            System.out.println("ulMicroSecs decoded: " + ulMicroSecs);

            for (byte element : a)
            {
                System.out.print(element + " ");
            }

            System.out.println("\n***************************************** ");
            assertEquals(days, ulDays);
            assertEquals(millisec, ulMilliSecs);
            assertEquals(microsec, ulMicroSecs);

        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }

    }

    @SuppressWarnings("unused")
    private String getString(byte[] barray)
    {
        StringBuilder sb = new StringBuilder();
        for (byte b : barray)
        {
            sb.append(String.format("0x%02X ", b));
        }
        return sb.toString();
    }

    @Test
    public void testDecodeUnsignedMSBFirstLong() throws IOException, SleApiException
    {
        System.out.println("---------- testDecodeUnsignedMSBFirstLong()");
        byte[] toDecode = { (byte) 0xFA, 0x02, 0x03, 0x04, (byte) 0xFA, 0x12, (byte) 0x86, 0x45 };

        long result = 0;
        int offset = 0;
        int cntBytesToDecode = 8;

        // Same algorithm used in the method under test but with traces
        for (int i = offset; i < offset + cntBytesToDecode; ++i)
        {
            long tmp = (toDecode[i] & 0x00000000000000FF);
            int shift = 8 * (cntBytesToDecode + offset - i) - 8;
            tmp <<= shift;
            System.out.println(i + " tmp - " + Long.toBinaryString(tmp) + " shift - " + shift);
            result |= tmp;
            System.out.println(i + " res - " + Long.toBinaryString(result) + " shift - " + shift);
        }

        System.out.println("res=" + result);

        BigInteger bigI = new BigInteger(toDecode);
        System.out.println("bigI=" + bigI.longValue());

        long res3 = EE_IntegralEncoder.decodeUnsignedMSBFirst(toDecode, 0, 8);
        System.out.println("res3=" + res3);

        assertEquals(bigI.longValue(), res3);
    }

    @Test
    public void testDecodeUnsignedMSBFirstInt() throws IOException, SleApiException
    {
        System.out.println("---------- testDecodeUnsignedMSBFirstInt()");
        byte[] toDecode = { (byte) 0xFA, 0x02, 0x03, 0x04, (byte) 0xFA, 0x12, (byte) 0x86, 0x45 };

        long result = 0;
        int offset = 2;
        int cntBytesToDecode = 4;

        // Same algorithm used in the method under test but with traces
        for (int i = offset; i < offset + cntBytesToDecode; ++i)
        {
            long tmp = (toDecode[i] & 0x00000000000000FF);
            int shift = 8 * (cntBytesToDecode + offset - i) - 8;
            tmp <<= shift;
            System.out.println(i + " tmp - " + Long.toBinaryString(tmp) + " shift - " + shift);
            result |= tmp;
            System.out.println(i + " res - " + Long.toBinaryString(result) + " shift - " + shift);
        }

        System.out.println("res=" + result);

        byte[] toDecodeSub = Arrays.copyOfRange(toDecode, offset, cntBytesToDecode + offset);

        BigInteger bigI = new BigInteger(toDecodeSub);
        System.out.println("bigI=" + bigI.longValue());

        long res2 = EE_IntegralEncoder.decodeUnsignedMSBFirst(toDecode, offset, cntBytesToDecode);
        System.out.println("res2=" + res2);

        assertEquals(bigI.longValue(), res2);
    }

    @Test
    public void testDecodeUnsignedMSBFirstShort() throws IOException, SleApiException
    {
        System.out.println("---------- testDecodeUnsignedMSBFirstShort()");
        byte[] toDecode = { (byte) 0xFA, 0x02, 0x03, 0x04, (byte) 0xFA, 0x12, (byte) 0x86, 0x45 };

        long result = 0;
        int offset = 1;
        int cntBytesToDecode = 2;

        // Same algorithm used in the method under test but with traces
        for (int i = offset; i < offset + cntBytesToDecode; ++i)
        {
            long tmp = (toDecode[i] & 0x00000000000000FF);
            int shift = 8 * (cntBytesToDecode + offset - i) - 8;
            tmp <<= shift;
            System.out.println(i + " tmp - " + Long.toBinaryString(tmp) + " shift - " + shift);
            result |= tmp;
            System.out.println(i + " res - " + Long.toBinaryString(result) + " shift - " + shift);
        }

        System.out.println("res=" + result);

        byte[] toDecodeSub = Arrays.copyOfRange(toDecode, offset, cntBytesToDecode + offset);

        BigInteger bigI = new BigInteger(toDecodeSub);
        System.out.println("bigI=" + bigI.longValue());

        long res2 = EE_IntegralEncoder.decodeUnsignedMSBFirst(toDecode, offset, cntBytesToDecode);
        System.out.println("res2=" + res2);

        assertEquals(bigI.longValue(), res2);
    }
}
