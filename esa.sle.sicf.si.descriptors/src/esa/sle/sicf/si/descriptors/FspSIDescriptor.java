package esa.sle.sicf.si.descriptors;

import java.util.Arrays;

import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isrv.icltu.types.CLTU_ClcwGvcId;
import ccsds.sle.api.isrv.ifsp.types.FSP_BlockingUsage;
import ccsds.sle.api.isrv.ifsp.types.FSP_ClcwGvcId;
import ccsds.sle.api.isrv.ifsp.types.FSP_MuxScheme;
import ccsds.sle.api.isrv.ifsp.types.FSP_PermittedTransmissionMode;
import ccsds.sle.api.isrv.ifsp.types.FSP_TimeoutType;

public class FspSIDescriptor extends SIDescriptor
{

    private SLE_YesNo directiveInvocationEnabled;

    private long permittedTcvc;

    private long[] permittedMaps;

    private long[] permittedApis;

    private FSP_PermittedTransmissionMode permittedTransmissionMode = FSP_PermittedTransmissionMode.fspPTM_invalid;

    private SLE_YesNo rfa;

    private SLE_YesNo blr;
    
    private int minimumReportingCycle;

    // not present in sicf file, but supported in the standard
    private long maximumPacketDataLength;

    private FSP_MuxScheme vcMultiplexingScheme;

    private long blockingTimeoutPeriod;

    private FSP_BlockingUsage blockingUsage;

    private long fopSlidingWindow;

    private FSP_MuxScheme mapMultiplexingScheme;

    private long maximumFrameLength;

    private SLE_YesNo segmentHeader;

    private FSP_TimeoutType timeoutType;

    private long timerInitial;

    private long transmissionLimit;

    private long transmitterFrameSequenceNumber;

    private long reportingCycle;
    
    private FSP_ClcwGvcId clcwGlobalVcid;
    
    private String clcwPhysicalChannel;
    
    private SLE_YesNo throwingOfEventsEnabled;

    public SLE_YesNo getThrowingOfEventsEnabled() {
		return throwingOfEventsEnabled;
	}

	public void setThrowingOfEventsEnabled(SLE_YesNo throwingOfEventsEnabled) {
		this.throwingOfEventsEnabled = throwingOfEventsEnabled;
	}

	public FSP_ClcwGvcId getClcwGlobalVcid() {
		return clcwGlobalVcid;
	}

	public void setClcwGlobalVcid(FSP_ClcwGvcId clcwGlobalVcid) {
		this.clcwGlobalVcid = clcwGlobalVcid;
	}
	
    public void setClcwPhysicalChannel(String clcwPhysicalChannel)
    {
        this.clcwPhysicalChannel = clcwPhysicalChannel;

    }

    public String getClcwPhysicalChannel()
    {
        return this.clcwPhysicalChannel;

    }

	public long[] getPermittedApis()
    {
        return this.permittedApis;
    }

    public SLE_YesNo getDirectiveInvocationEnabled()
    {
        return this.directiveInvocationEnabled;
    }

    public long[] getPermittedMaps()
    {
        return this.permittedMaps;
    }

    public FSP_PermittedTransmissionMode getPermittedTransmissionMode()
    {
        return this.permittedTransmissionMode;
    }

    public long getPermittedTcvc()
    {
        return this.permittedTcvc;
    }

    public void setPermittedApis(long[] plist)
    {
        if (plist != null && plist.length > 0)
        {
            this.permittedApis = new long[plist.length];
            System.arraycopy(plist, 0, this.permittedApis, 0, plist.length);
        }

    }

    /**
     * Setter for minimum-reporting-cycle
     * @since SLES v5
     * @param minimumReportingCycle
     */
    public void setMinimumReportingCycle(int minimumReportingCycle)
    {
        this.minimumReportingCycle = minimumReportingCycle;
    }
    
    public void setDirectiveInvocationEnabled(SLE_YesNo directiveInvocationEnabled)
    {
        this.directiveInvocationEnabled = directiveInvocationEnabled;

    }

    public void setPermittedMaps(long[] plist)
    {
        if (plist != null && plist.length > 0)
        {
            this.permittedMaps = new long[plist.length];
            System.arraycopy(plist, 0, this.permittedMaps, 0, plist.length);
        }
    }

    public void setPermittedTransmissionMode(FSP_PermittedTransmissionMode mode)
    {
        this.permittedTransmissionMode = mode;
    }

    public void setPermittedTcvc(long vcid)
    {
        this.permittedTcvc = vcid;

    }

    public void setRfAvailableRequired(SLE_YesNo rfa)
    {
        this.rfa = rfa;

    }

    public SLE_YesNo getRfAvailableRequired()
    {
        return this.rfa;
    }

    public void setBitLockRequired(SLE_YesNo blr)
    {
        this.blr = blr;

    }

    public SLE_YesNo getBitLockRequired()
    {
        return this.blr;
    }

    public void setMaximumPacketDataLength(long maximumPacketDataLength)
    {
        this.maximumPacketDataLength = maximumPacketDataLength;
    }

    public long getMaximumPacketDataLength()
    {
        return this.maximumPacketDataLength;
    }
    
    /**
     * getter for the minimum-reporting-cycle
     * @since SLES v5.
     * @return
     */
    public int getMinimumReportingCycle()
    {
        return this.minimumReportingCycle;
    }

    public void setVCMultiplexingScheme(FSP_MuxScheme vcMultiplexingScheme)
    {
        this.vcMultiplexingScheme = vcMultiplexingScheme;

    }

    public FSP_MuxScheme getVCMultiplexingScheme()
    {
        return this.vcMultiplexingScheme;
    }

    public void setBlockingTimeoutPeriod(long blockingTimeoutPeriod)
    {
        this.blockingTimeoutPeriod = blockingTimeoutPeriod;
    }

    public long getBlockingTimeoutPeriod()
    {
        return this.blockingTimeoutPeriod;
    }

    public void setBlockingUsage(FSP_BlockingUsage blockingUsage)
    {
        this.blockingUsage = blockingUsage;
    }

    public FSP_BlockingUsage getBlockingUsage()
    {
        return this.blockingUsage;
    }

    public void setFopSlidingWindow(long fopSlidingWindow)
    {
        this.fopSlidingWindow = fopSlidingWindow;
    }

    public long getFopSlidingWindow()
    {
        return this.fopSlidingWindow;
    }

    public void setMapMultiplexingScheme(FSP_MuxScheme mapMultiplexingControl)
    {
        this.mapMultiplexingScheme = mapMultiplexingControl;
    }

    public FSP_MuxScheme getMapMultiplexingScheme()
    {
        return this.mapMultiplexingScheme;
    }

    public void setMaximumFrameLength(long maximumFrameLength)
    {
        this.maximumFrameLength = maximumFrameLength;
    }

    public long getMaximumFrameLength()
    {
        return this.maximumFrameLength;
    }

    public void setSegmentHeader(SLE_YesNo segmentHeader)
    {
        this.segmentHeader = segmentHeader;
    }

    public SLE_YesNo getSegmentHeader()
    {
        return this.segmentHeader;
    }

    public void setTimeoutType(FSP_TimeoutType timeoutType)
    {
        this.timeoutType = timeoutType;
    }

    public FSP_TimeoutType getTimeoutType()
    {
        return this.timeoutType;
    }

    public void setTimerInitial(long timerInitial)
    {
        this.timerInitial = timerInitial;
    }

    public long getTimerInitial()
    {
        return this.timerInitial;
    }

    public void setTransmissionLimit(long transmissionLimit)
    {
        this.transmissionLimit = transmissionLimit;
    }

    public long getTransmissionLimit()
    {
        return this.transmissionLimit;
    }

    public void setTransmitterFrameSequenceNumber(long transmitterFrameSequenceNumber)
    {
        this.transmitterFrameSequenceNumber = transmitterFrameSequenceNumber;
    }

    public long getTransmitterFrameSequenceNumber()
    {
        return this.transmitterFrameSequenceNumber;
    }

    public void setReportingCycle(long reportingCycle)
    {
        this.reportingCycle = reportingCycle;
    }

    public long getReportingCycle()
    {
        return this.reportingCycle;
    }

    @Override
    public SLE_ApplicationIdentifier getApplicationIdentifier()
    {
        return SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (this.blockingTimeoutPeriod ^ (this.blockingTimeoutPeriod >>> 32));
        result = prime * result + ((this.blockingUsage == null) ? 0 : this.blockingUsage.hashCode());
        result = prime * result + ((this.blr == null) ? 0 : this.blr.hashCode());
        result = prime * result
                 + ((this.directiveInvocationEnabled == null) ? 0 : this.directiveInvocationEnabled.hashCode());
        result = prime * result + (int) (this.fopSlidingWindow ^ (this.fopSlidingWindow >>> 32));
        result = prime * result + ((this.mapMultiplexingScheme == null) ? 0 : this.mapMultiplexingScheme.hashCode());
        result = prime * result + (int) (this.maximumFrameLength ^ (this.maximumFrameLength >>> 32));
        result = prime * result + (int) (this.maximumPacketDataLength ^ (this.maximumPacketDataLength >>> 32));
        result = prime * result + Arrays.hashCode(this.permittedApis);
        result = prime * result + Arrays.hashCode(this.permittedMaps);
        result = prime * result + (int) (this.permittedTcvc ^ (this.permittedTcvc >>> 32));
        result = prime * result
                 + ((this.permittedTransmissionMode == null) ? 0 : this.permittedTransmissionMode.hashCode());
        result = prime * result + (int) (this.reportingCycle ^ (this.reportingCycle >>> 32));
        result = prime * result + ((this.rfa == null) ? 0 : this.rfa.hashCode());
        result = prime * result + ((this.segmentHeader == null) ? 0 : this.segmentHeader.hashCode());
        result = prime * result + ((this.timeoutType == null) ? 0 : this.timeoutType.hashCode());
        result = prime * result + (int) (this.timerInitial ^ (this.timerInitial >>> 32));
        result = prime * result + (int) (this.transmissionLimit ^ (this.transmissionLimit >>> 32));
        result = prime * result
                 + (int) (this.transmitterFrameSequenceNumber ^ (this.transmitterFrameSequenceNumber >>> 32));
        result = prime * result + ((this.vcMultiplexingScheme == null) ? 0 : this.vcMultiplexingScheme.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        FspSIDescriptor other = (FspSIDescriptor) obj;
        if (this.blockingTimeoutPeriod != other.blockingTimeoutPeriod)
        {
            return false;
        }
        if (this.blockingUsage != other.blockingUsage)
        {
            return false;
        }
        if (this.blr != other.blr)
        {
            return false;
        }
        if (this.directiveInvocationEnabled != other.directiveInvocationEnabled)
        {
            return false;
        }
        if (this.fopSlidingWindow != other.fopSlidingWindow)
        {
            return false;
        }
        if (this.mapMultiplexingScheme != other.mapMultiplexingScheme)
        {
            return false;
        }
        if (this.maximumFrameLength != other.maximumFrameLength)
        {
            return false;
        }
        if (this.maximumPacketDataLength != other.maximumPacketDataLength)
        {
            return false;
        }
        if (!Arrays.equals(this.permittedApis, other.permittedApis))
        {
            return false;
        }
        if (!Arrays.equals(this.permittedMaps, other.permittedMaps))
        {
            return false;
        }
        if (this.permittedTcvc != other.permittedTcvc)
        {
            return false;
        }
        if (this.permittedTransmissionMode != other.permittedTransmissionMode)
        {
            return false;
        }
        if (this.reportingCycle != other.reportingCycle)
        {
            return false;
        }
        if (this.rfa != other.rfa)
        {
            return false;
        }
        if (this.segmentHeader != other.segmentHeader)
        {
            return false;
        }
        if (this.timeoutType != other.timeoutType)
        {
            return false;
        }
        if (this.timerInitial != other.timerInitial)
        {
            return false;
        }
        if (this.transmissionLimit != other.transmissionLimit)
        {
            return false;
        }
        if (this.transmitterFrameSequenceNumber != other.transmitterFrameSequenceNumber)
        {
            return false;
        }
        if (this.vcMultiplexingScheme != other.vcMultiplexingScheme)
        {
            return false;
        }
        return true;
    }

}
