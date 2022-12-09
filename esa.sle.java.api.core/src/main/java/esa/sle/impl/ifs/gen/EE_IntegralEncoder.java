/**
 * @(#) EE_IntegralEncoder.java
 */

package esa.sle.impl.ifs.gen;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;

/**
 * Simple Encoder/Decoder. Provides correct Encoding and Decoding of integral
 * types that is independent of the underlying hardware byte order. Not designed
 * to act as an actual instantiated class - more as a repository for Byte Order
 * Independent Encoding/Decoding functions. Designed to enable byte order
 * independent encoding/Decoding of integers.
 */
public class EE_IntegralEncoder
{
    private static int ci_sizeLong = 8;


    /**
     * Rationale for passing in an unsigned long is that all fundamental
     * integral types are guaranteed to be smaller or the same size as a long.
     * The function encodes as Most Significant byte first into the buffer. The
     * caller should either pass in the unsigned long, or assign a number such
     * as a short to a long and pass this in. The number of bytes is for the
     * encoding, eg 2 for a short, 3 bytes etc. The encoding will check the
     * number of bytes, and whether the unsigned long value can be encoded in
     * this. PRECOND2, argbytes <= sizeof (long) PRECOND3, argbuffer != NULL If
     * length argbuffer4 < argbytes then a Access Violation error will probably
     * occur, or memory will be overwritten. E_INVALIDAR is returned If any of
     * the preconditions fail
     *
     * @param offset
     * @param cntBytesToDecode
     * @param argval
     * @return
     * @throws SleApiException
     */
    public static byte[] encodeUnsignedMSBFirst(byte[] argbuffer, int offset, int argnumbytes, long argval) throws SleApiException
    {
        if (argbuffer == null)
        {
            throw new SleApiException(HRESULT.E_INVALIDARG, "the byte array in input is null");
        }

        if (argnumbytes > ci_sizeLong || argnumbytes < 0)
        {
            throw new SleApiException(HRESULT.E_INVALIDARG, "the number of bytes to encode is not valid");
        }
        else if (argnumbytes < ci_sizeLong && argval > Long.MAX_VALUE)
        {
            throw new SleApiException(HRESULT.E_INVALIDARG,
                                      "the long number cannot be encoded into the provided number of bytes");
        }

        for (int i = offset, k = offset + argnumbytes - 1; i < offset + argnumbytes; ++i, --k)
        {
            argbuffer[k] = (byte) (argval & 0x00000000000000FF);
            argval >>= 8;
        }
        return argbuffer;
    }

    /**
     * This takes a buffer, and the size in bytes of the integral type. The
     * result is a long. The value of the integer decoded.
     *
     * @param argbuf
     * @param offset
     * @param cntBytesToDecode
     * @return
     * @throws SleApiException
     */
    public static long decodeUnsignedMSBFirst(byte[] argbuf, int offset, int cntBytesToDecode) throws SleApiException
    {
        if (cntBytesToDecode > ci_sizeLong)
        {
            throw new SleApiException(HRESULT.E_INVALIDARG, "the number of bytes to decode is not valid");
        }

        if (cntBytesToDecode <= 0)
        {
            return 0;
        }

        long result = 0;

        for (int i = offset; i < offset + cntBytesToDecode; ++i)
        {
            long tmp = (argbuf[i] & 0x00000000000000FF);
            tmp <<= 8 * (cntBytesToDecode + offset - i) - 8;
            result |= tmp;
        }

        return result;
    }
}
