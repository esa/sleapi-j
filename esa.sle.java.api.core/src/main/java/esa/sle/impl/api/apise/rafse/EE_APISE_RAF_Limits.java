/**
 * @(#) EE_APISE_RAF_Limits.java
 */

package esa.sle.impl.api.apise.rafse;

/**
 * The class holds all RAF service specific limit values of configuration
 * parameters as specified in [CCSDS 911.1].
 */
public class EE_APISE_RAF_Limits
{
    /**
     * The minimum transfer buffer size. The size is meant to be the number of
     * TRANSFER-DATA or SYNC-NOTIFY operations.
     */
    private static long minBufferSize = 1;

    /**
     * The minimum value of the latency limit.
     */
    private static int minLatencyLimit = 1;


    public static long getMinBufferSize()
    {
        return minBufferSize;
    }

    public static int getMinLatencyLimit()
    {
        return minLatencyLimit;
    }

}
