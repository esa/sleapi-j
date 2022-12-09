/**
 * @(#) EE_APISE_ROCF_Limits.java
 */

package esa.sle.impl.api.apise.rocfse;

/**
 * The class holds all ROCF service specific limit values of configuration
 * parameters as specified in [CCSDS 911.5]
 */
public class EE_APISE_ROCF_Limits
{
    /**
     * The minimum transfer buffer size. The size is meant to be the number of
     * TRANSFER-DATA or SYNC-NOTIFY operations.
     */
    private static final int minBufferSize = 1;

    /**
     * The minimum value of the latency limit.
     */
    private static final int minLatencyLimit = 1;

    /**
     * The maximum value of the SC-Id belonging to the GvcId.
     */
    private static final int gvcIdMaxScId = 1023;

    /**
     * The maximum value of the version belonging to the GvcId.
     */
    private static final int gvcIdMaxVersion = 3;

    /**
     * The maximum value of the VC-Id belonging to the GvcId.
     */
    private static final int gvcIdMaxVcId = 63;

    /**
     * The maximum value of the TC VC-Id.
     */
    private static final int maxTcVcId = 63;


    public static int getMinBufferSize()
    {
        return minBufferSize;
    }

    public static int getMinLatencyLimit()
    {
        return minLatencyLimit;
    }

    public static int getGvcIdMaxScId()
    {
        return gvcIdMaxScId;
    }

    public static int getGvcIdMaxVersion()
    {
        return gvcIdMaxVersion;
    }

    public static int getGvcIdMaxVcId()
    {
        return gvcIdMaxVcId;
    }

    public static int getMaxTcVcId()
    {
        return maxTcVcId;
    }
}
