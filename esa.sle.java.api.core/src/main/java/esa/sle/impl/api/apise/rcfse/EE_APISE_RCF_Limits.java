/**
 * @(#) EE_APISE_RCF_Limits.java
 */

package esa.sle.impl.api.apise.rcfse;

/**
 * The class holds all RCF service specific limit values of configuration
 * parameters as specified in [CCSDS 911.2].
 */
public class EE_APISE_RCF_Limits
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

    /**
     * The maximum value of the SC-Id belonging to the GvcId.
     */
    private static long gvcIdMaxScId = 1023;

    /**
     * The maximum value of the version belonging to the GvcId.
     */
    private static long gvcIdMaxVersion = 3;

    /**
     * The maximum value of the VC-Id belonging to the GvcId.
     */
    private static long gvcIdMaxVcId = 63;


    public static final long getMinBufferSize()
    {
        return minBufferSize;
    }

    public static final int getMinLatencyLimit()
    {
        return minLatencyLimit;
    }

    public static final long getGvcIdMaxScId()
    {
        return gvcIdMaxScId;
    }

    public static final long getGvcIdMaxVersion()
    {
        return gvcIdMaxVersion;
    }

    public static final long getGvcIdMaxVcId()
    {
        return gvcIdMaxVcId;
    }
}
