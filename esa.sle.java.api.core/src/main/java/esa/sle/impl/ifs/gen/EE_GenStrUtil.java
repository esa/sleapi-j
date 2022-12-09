package esa.sle.impl.ifs.gen;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains string utility functions used by the API.
 */
public class EE_GenStrUtil
{

    final static String errorText = "*ERROR*";

    private static final Logger LOG = Logger.getLogger(EE_GenStrUtil.class.getName());


    /**
     * Converts a binary array of characters into a null terminated ascii hex
     * dump, and returns a pointer which it is the reponsibility of the caller
     * to deallocate, and the length of the string returned, not including the
     * null terminator.
     * 
     * @param binary
     * @param sizeInBound is smaller or equal to the size of the vector binary.
     *            The argument is important to impose limits on the byte vector.
     *            In most cases the sizeInBound is equal with the size of binary
     *            vector. The client of this method checks if the sizeInBound is
     *            smaller or equal to the size of binary.
     * @return
     */
    public static String convAscii(byte[] binary, long sizeInBound)
    {
        final String HEXES = "0123456789abcdef";
        char[] retVal = new char[0];

        if (sizeInBound == 0 || binary == null)
        {
            retVal = new char[1];
            String resultSTR = "";
            for (char element : retVal)
            {
                resultSTR += element;
            }

            return resultSTR.trim();
        }
        else
        {
            int sizeNeeded = (int) (2 * sizeInBound);
            final StringBuilder hex = new StringBuilder(sizeNeeded);
            int k = 0;
            for (final byte b : binary)
            {
                if (k < sizeInBound)
                {
                    hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
                }
                else
                {
                    break;
                }
                k++;
            }
            return hex.toString();

        }

    }

    /**
     * This function takes in a hexadecimal string, and converts it into binary
     * data (SLE_Octet*). This function will fail if there is a non hexadecimal
     * character or the length of the string is not divisible by 2, or the
     * string is blank. Failure in this context means return a blank SLE_Octet*.
     * The length is set as an ouptut parameter.
     */
    public static byte[] hexToBin(String value)
    {
        if (value.length() % 2 != 0)
        {
            return null;
        }
        byte[] password = new byte[value.length() / 2];
        for (int i = 0; i < value.length(); i += 2)
        {
            try
            {
                Integer.parseInt(String.valueOf(value.charAt(i)), 16);
                Integer.parseInt(String.valueOf(value.charAt(i + 1)), 16);
            }
            catch (NumberFormatException e)
            {
                return new byte[0];
            }

            if (value.charAt(i) <= '9')
            {
                password[i / 2] = (byte) ((value.charAt(i) - '0') * 16);
            }
            else if (value.charAt(i) <= 'F')
            {
                password[i / 2] = (byte) ((value.charAt(i) - 'A' + 10) * 16);
            }
            else if (value.charAt(i) <= 'f')
            {
                password[i / 2] = (byte) ((value.charAt(i) - 'a' + 10) * 16);
            }

            if (value.charAt(i + 1) <= '9')
            {
                password[i / 2] += value.charAt(i + 1) - '0';
            }
            else if (value.charAt(i + 1) <= 'F')
            {
                password[i / 2] += value.charAt(i + 1) - 'A' + 10;
            }
            else if (value.charAt(i + 1) <= 'f')
            {
                password[i / 2] += value.charAt(i + 1) - 'a' + 10;
            }
        }
        return password;
    }

    public static void print(String string, byte[] b)
    {
        if (LOG.isLoggable(Level.FINEST))
        {

            String str = "";
            for (byte element : b)
            {
                if (element >= 0 && element <= 15)
                {
                    str += "0" + Integer.toHexString(element);
                }
                else
                {
                    str += Integer.toHexString(element & 0xFF);
                }
            }
            LOG.finest(string + " " + str);
        }
    }

}
