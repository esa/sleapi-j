package esa.sle.impl.ifs.gen;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EE_GenStrUtilTest
{

    @Test
    public void testConvAscii()
    {
        byte[] input = new byte[6];
        input[0] = 'a';
        input[1] = 'b';
        input[2] = 'c';
        input[3] = 'd';
        input[4] = 'e';
        input[5] = 'f';
        assertEquals("616263646566", EE_GenStrUtil.convAscii(input, input.length));
        assertEquals("61", EE_GenStrUtil.convAscii(input, 1));
        assertEquals("6162", EE_GenStrUtil.convAscii(input, 2));
    }

    @Test
    public void testHexToBin()
    {
        byte[] output = EE_GenStrUtil.hexToBin("6162");
        String resultSTR = "";
        for (byte element : output)
        {
            resultSTR += (char) element;
        }
        assertEquals(resultSTR, "ab");

    }

}
