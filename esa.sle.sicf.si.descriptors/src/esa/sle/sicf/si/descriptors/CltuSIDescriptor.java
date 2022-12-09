package esa.sle.sicf.si.descriptors;

import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isrv.icltu.types.CLTU_ClcwGvcId;
import ccsds.sle.api.isrv.icltu.types.CLTU_NotificationMode;
import ccsds.sle.api.isrv.icltu.types.CLTU_PlopInEffect;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProtocolAbortMode;

public class CltuSIDescriptor extends SIDescriptor
{

    // present in sicf
    private long maximumClduLength;

    private long minimumCltuDelay;

    private long maximumCltuDelay;

    private SLE_YesNo bitLockRequired;

    private SLE_YesNo rfAvailRequired = SLE_YesNo.sleYN_No; // SLEAPIJ-42

    private CLTU_ProtocolAbortMode protocolAbortClearEnabled;
    
    private int minimumReportingCycle;

    // not present in sicf
    private long acquisitionSequenceLength;

    private String clcwPhysicalChannel;

    private CLTU_ClcwGvcId clcwGlobalVcid;

    private int maximumReportingCycle;

    private long modulationFrequency = Long.MAX_VALUE; // SLEAPIJ-42

    private int modulationIndex = Integer.MAX_VALUE; // SLEAPIJ-42

    private CLTU_NotificationMode cldtuNotificationMode;

    private int plop1idleSequenceLength;

    private CLTU_PlopInEffect plopInEffect;

    private long reportingCycle;

    private int subcarrierToBitRateRatio = 1; // SLEAPIJ-42

    public CltuSIDescriptor() {
    	
    }

    public SLE_YesNo getBitLockRequired()
    {
        return this.bitLockRequired;
    }

    public long getMaximumClduLength()
    {
        return this.maximumClduLength;
    }

    public long getMinimumCltuDelay()
    {
        return this.minimumCltuDelay;
    }

    public CLTU_ProtocolAbortMode getProtocolAbortMode()
    {
        return this.protocolAbortClearEnabled;
    }

    public SLE_YesNo getRfAvailableRequired()
    {
        return this.rfAvailRequired;
    }

    public void setBitLockRequired(SLE_YesNo bitLockRequired)
    {
        this.bitLockRequired = bitLockRequired;

    }

    public void setMaximumCltuLength(long arg0)
    {
        this.maximumClduLength = arg0;

    }

    public void setMinimumCltuDelay(long minimumCltuDelay)
    {
        this.minimumCltuDelay = minimumCltuDelay;

    }

    public void setProtocolAbortMode(CLTU_ProtocolAbortMode protocolAbortClearEnabled)
    {
        this.protocolAbortClearEnabled = protocolAbortClearEnabled;

    }

    public void setRfAvailableRequired(SLE_YesNo rfAvailRequired)
    {
        this.rfAvailRequired = rfAvailRequired;

    }

    public long getMaximumCltuDelay()
    {
        return this.maximumCltuDelay;
    }

    public void setMaximumCltuDelay(long maximumCltuDelay)
    {
        this.maximumCltuDelay = maximumCltuDelay;
    }

    public void setAcquisitionSequenceLength(Long acquisitionSequenceLength)
    {
        this.acquisitionSequenceLength = acquisitionSequenceLength;

    }

    public long getAcquisitionSequenceLength()
    {
        return this.acquisitionSequenceLength;

    }

    public void setClcwPhysicalChannel(String clcwPhysicalChannel)
    {
        this.clcwPhysicalChannel = clcwPhysicalChannel;

    }

    public String getClcwPhysicalChannel()
    {
        return this.clcwPhysicalChannel;

    }

    public void setClcwGlobalVCID(CLTU_ClcwGvcId clcwGlobalVcid)
    {
        this.clcwGlobalVcid = clcwGlobalVcid;
    }

    public CLTU_ClcwGvcId getClcwGlobalVCID()
    {
        return this.clcwGlobalVcid;
    }

    public void setMaximumReportingCycle(int maximumReportingCycle)
    {
        this.maximumReportingCycle = maximumReportingCycle;
    }

    public int getMaximumReportingCycle()
    {
        return this.maximumReportingCycle;
    }

    /**
     * Setter for minimum-reporting-cycle
     * @since SLES V5
     * @param minimumReportingCycle
     */
    public void setMinimumReportingCycle(int minimumReportingCycle)
    {
        this.minimumReportingCycle = minimumReportingCycle;
    }

    /**
     * Getter for minimum-reporting-cycle
     * @since SLES V5
     * @param minimumReportingCycle
     */
    public int getMinimumReportingCycle()
    {
        return this.minimumReportingCycle;
    }

    public void setModulationFrequency(long modulationFrequency)
    {
        this.modulationFrequency = modulationFrequency;
    }

    public long getModulationFrequency()
    {
        return this.modulationFrequency;
    }

    public void setModulationIndex(int modulationIndex)
    {
        this.modulationIndex = modulationIndex;

    }

    public int getModulationIndex()
    {
        return this.modulationIndex;
    }

    public void setNotificationMode(CLTU_NotificationMode cldtuNotificationMode)
    {
        this.cldtuNotificationMode = cldtuNotificationMode;
    }

    public CLTU_NotificationMode getNotificationMode()
    {
        return this.cldtuNotificationMode;
    }

    public void setPlop1idleSequenceLength(int plop1idleSequenceLength)
    {
        this.plop1idleSequenceLength = plop1idleSequenceLength;
    }

    public int getPlop1idleSequenceLength()
    {
        return this.plop1idleSequenceLength;
    }

    public void setPlopInEffect(CLTU_PlopInEffect plopInEffect)
    {
        this.plopInEffect = plopInEffect;

    }

    public CLTU_PlopInEffect getPlopInEffect()
    {
        return this.plopInEffect;
    }

    public void setReportingCycle(long reportingCycle)
    {
        this.reportingCycle = reportingCycle;
    }

    public long getReportingCycle()
    {
        return this.reportingCycle;
    }

    public void setSubcarrierToBitRateRatio(int subcarrierToBitRateRatio)
    {
        this.subcarrierToBitRateRatio = subcarrierToBitRateRatio;
    }

    public int getSubcarrierToBitRateRatio()
    {
        return this.subcarrierToBitRateRatio;
    }

    @Override
    public SLE_ApplicationIdentifier getApplicationIdentifier()
    {
        return SLE_ApplicationIdentifier.sleAI_fwdCltu;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (this.acquisitionSequenceLength ^ (this.acquisitionSequenceLength >>> 32));
        result = prime * result + ((this.bitLockRequired == null) ? 0 : this.bitLockRequired.hashCode());
        result = prime * result + ((this.clcwGlobalVcid == null) ? 0 : this.clcwGlobalVcid.hashCode());
        result = prime * result + ((this.clcwPhysicalChannel == null) ? 0 : this.clcwPhysicalChannel.hashCode());
        result = prime * result + ((this.cldtuNotificationMode == null) ? 0 : this.cldtuNotificationMode.hashCode());
        result = prime * result + (int) (this.maximumClduLength ^ (this.maximumClduLength >>> 32));
        result = prime * result + (int) (this.maximumCltuDelay ^ (this.maximumCltuDelay >>> 32));
        result = prime * result + this.maximumReportingCycle;
        result = prime * result + (int) (this.minimumCltuDelay ^ (this.minimumCltuDelay >>> 32));
        result = prime * result + this.minimumReportingCycle;
        result = prime * result + (int) (this.modulationFrequency ^ (this.modulationFrequency >>> 32));
        result = prime * result + this.modulationIndex;
        result = prime * result + this.plop1idleSequenceLength;
        result = prime * result + ((this.plopInEffect == null) ? 0 : this.plopInEffect.hashCode());
        result = prime * result
                 + ((this.protocolAbortClearEnabled == null) ? 0 : this.protocolAbortClearEnabled.hashCode());
        result = prime * result + (int) (this.reportingCycle ^ (this.reportingCycle >>> 32));
        result = prime * result + ((this.rfAvailRequired == null) ? 0 : this.rfAvailRequired.hashCode());
        result = prime * result + this.subcarrierToBitRateRatio;
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
        CltuSIDescriptor other = (CltuSIDescriptor) obj;
        if (this.acquisitionSequenceLength != other.acquisitionSequenceLength)
        {
            return false;
        }
        if (this.bitLockRequired != other.bitLockRequired)
        {
            return false;
        }
        if (this.clcwGlobalVcid == null)
        {
            if (other.clcwGlobalVcid != null)
            {
                return false;
            }
        }
        else if (!this.clcwGlobalVcid.equals(other.clcwGlobalVcid))
        {
            return false;
        }
        if (this.clcwPhysicalChannel == null)
        {
            if (other.clcwPhysicalChannel != null)
            {
                return false;
            }
        }
        else if (!this.clcwPhysicalChannel.equals(other.clcwPhysicalChannel))
        {
            return false;
        }
        if (this.cldtuNotificationMode != other.cldtuNotificationMode)
        {
            return false;
        }
        if (this.maximumClduLength != other.maximumClduLength)
        {
            return false;
        }
        if (this.maximumCltuDelay != other.maximumCltuDelay)
        {
            return false;
        }
        if (this.maximumReportingCycle != other.maximumReportingCycle)
        {
            return false;
        }
        if (this.minimumCltuDelay != other.minimumCltuDelay)
        {
            return false;
        }
        if (this.minimumReportingCycle != other.minimumReportingCycle)
        {
            return false;
        }
        if (this.modulationFrequency != other.modulationFrequency)
        {
            return false;
        }
        if (this.modulationIndex != other.modulationIndex)
        {
            return false;
        }
        if (this.plop1idleSequenceLength != other.plop1idleSequenceLength)
        {
            return false;
        }
        if (this.plopInEffect != other.plopInEffect)
        {
            return false;
        }
        if (this.protocolAbortClearEnabled != other.protocolAbortClearEnabled)
        {
            return false;
        }
        if (this.reportingCycle != other.reportingCycle)
        {
            return false;
        }
        if (this.rfAvailRequired != other.rfAvailRequired)
        {
            return false;
        }
        if (this.subcarrierToBitRateRatio != other.subcarrierToBitRateRatio)
        {
            return false;
        }
        return true;
    }

}
