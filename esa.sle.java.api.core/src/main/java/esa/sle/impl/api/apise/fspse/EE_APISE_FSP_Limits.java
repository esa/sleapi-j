package esa.sle.impl.api.apise.fspse;

/**
 * The class holds all FSP service specific limit values of configuration
 * parameters as specified in [CCSDS 912.3] Annex F.
 */
public class EE_APISE_FSP_Limits
{
    /**
     * The minimum size is 1024 maximum length FSP-TRANSFER-DATA invocations.
     */
    private final static long minBufferSize = 1024;

    /**
     * The maximum Map or Vc Identifier (unsigned int 0 .. 63).
     */
    private final static long maxMapOrVcId = 63;

    /**
     * The maximum Application Identifier (unsigned long 0 .. 2047).
     */
    private final static long maxApId = 2047;

    /**
     * The minimum value of the absolute vc priority (highest).
     */
    private final static long minAbsolutePriority = 1;

    /**
     * The maximum value of the absolute vc priority (lowest).
     */
    private final static long maxAbsolutePriority = 64;

    /**
     * The minimum packet length.
     */
    private final static long minPacketLength = 7;

    /**
     * The maximum packet length.
     */
    private final static long maxPacketLength = 65542;

    /**
     * The minimum FOP sliding window width.
     */
    private final static int minFopSlidingWindow = 1;

    /**
     * The maximum FOP sliding window width.
     */
    private final static long maxFopSlidingWindow = 255;

    /**
     * The minimum Transmission Limit.
     */
    private final static int minTransmissionLimit = 1;

    /**
     * The maximum Transmission Limit.
     */
    private final static long maxTransmissionLimit = 255;

    /**
     * The minimum transmitter frame sequence number.
     */
    private final static long minTransmitterFrameSequenceNum = 1;

    /**
     * The maximum transmitter frame sequence number.
     */
    private final static long maxTransmitterFrameSequenceNum = 255;

    /**
     * The minimum frame length.
     */
    private final static long minFrameLength = 12;

    /**
     * The maximum frame length.
     */
    private final static long maxFrameLength = 1024;


    public final static long getMinBufferSize()
    {
        return minBufferSize;
    }

    public final static long getMaxMapOrVcId()
    {
        return maxMapOrVcId;
    }

    public final static long getMaxApId()
    {
        return maxApId;
    }

    public final static long getMinAbsolutePriority()
    {
        return minAbsolutePriority;
    }

    public final static long getMaxAbsolutePriority()
    {
        return maxAbsolutePriority;
    }

    public final static long getMinPacketLength()
    {
        return minPacketLength;
    }

    public final static long getMaxPacketLength()
    {
        return maxPacketLength;
    }

    public final static long getMinFopSlidingWindow()
    {
        return minFopSlidingWindow;
    }

    public final static long getMaxFopSlidingWindow()
    {
        return maxFopSlidingWindow;
    }

    public final static long getMinTransmissionLimit()
    {
        return minTransmissionLimit;
    }

    public final static long getMaxTransmissionLimit()
    {
        return maxTransmissionLimit;
    }

    public final static long getMinTransmitterFrameSequenceNum()
    {
        return minTransmitterFrameSequenceNum;
    }

    public final static long getMaxTransmitterFrameSequenceNum()
    {
        return maxTransmitterFrameSequenceNum;
    }

    public final static long getMinFrameLength()
    {
        return minFrameLength;
    }

    public final static long getMaxFrameLength()
    {
        return maxFrameLength;
    }
}
