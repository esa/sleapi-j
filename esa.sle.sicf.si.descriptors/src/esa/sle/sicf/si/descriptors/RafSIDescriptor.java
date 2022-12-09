package esa.sle.sicf.si.descriptors;

import java.util.Arrays;

import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isrv.iraf.types.RAF_DeliveryMode;
import ccsds.sle.api.isrv.iraf.types.RAF_ParFrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_RequestedFrameQuality;

public class RafSIDescriptor extends SIDescriptor
{

    private RAF_DeliveryMode deliveryMode = RAF_DeliveryMode.rafDM_invalid;

    private long bufferSize;

    private int latencyLimit;

    private String initiator;

    private RAF_ParFrameQuality[] permittedFrameQuality;

	private int minimumReportingCycle;


    public RAF_DeliveryMode getDeliveryMode()
    {
        return this.deliveryMode;
    }

    public int getLatencyLimit()
    {
        return this.latencyLimit;
    }

    public long getTransferBufferSize()
    {
        return this.bufferSize;
    }

    public void setDeliveryMode(RAF_DeliveryMode deliveryMode)
    {
        this.deliveryMode = deliveryMode;

    }

    public void setLatencyLimit(int latencyLimit)
    {
        this.latencyLimit = latencyLimit;

    }

    public void setTransferBufferSize(long bufferSize)
    {
        this.bufferSize = bufferSize;

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
    
    /**
     * Getter for the minimum-reporting-cycle
     * @since SLES v5.
     * @return
     */
    public int getMinimumReportingCycle()
    {
        return this.minimumReportingCycle;
    }
        
    public String getInitiator()
    {
        return this.initiator;
    }

    public void setInitiator(String initiator)
    {
        this.initiator = initiator;
    }

    public RAF_ParFrameQuality[] getFrameQuality()
    {
        return this.permittedFrameQuality;
    }

    public void setFrameQualitySet(RAF_ParFrameQuality[] frameQuality)
    {
        this.permittedFrameQuality = frameQuality;
    }

    @Override
    public SLE_ApplicationIdentifier getApplicationIdentifier()
    {
        return SLE_ApplicationIdentifier.sleAI_rtnAllFrames;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (this.bufferSize ^ (this.bufferSize >>> 32));
        result = prime * result + ((this.deliveryMode == null) ? 0 : this.deliveryMode.hashCode());
        result = prime * result + Arrays.hashCode(this.permittedFrameQuality);
        result = prime * result + ((this.initiator == null) ? 0 : this.initiator.hashCode());
        result = prime * result + this.latencyLimit;
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
        RafSIDescriptor other = (RafSIDescriptor) obj;
        if (this.bufferSize != other.bufferSize)
        {
            return false;
        }
        if (this.deliveryMode != other.deliveryMode)
        {
            return false;
        }
        if (!Arrays.equals(this.permittedFrameQuality, other.permittedFrameQuality))
        {
            return false;
        }
        if (this.initiator == null)
        {
            if (other.initiator != null)
            {
                return false;
            }
        }
        else if (!this.initiator.equals(other.initiator))
        {
            return false;
        }
        if (this.latencyLimit != other.latencyLimit)
        {
            return false;
        }
        return true;
    }

}
