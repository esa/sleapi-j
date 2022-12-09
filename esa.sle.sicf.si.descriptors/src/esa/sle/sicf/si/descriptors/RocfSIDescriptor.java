package esa.sle.sicf.si.descriptors;

import java.util.Arrays;

import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isrv.irocf.types.ROCF_ControlWordType;
import ccsds.sle.api.isrv.irocf.types.ROCF_DeliveryMode;
import ccsds.sle.api.isrv.irocf.types.ROCF_Gvcid;
import ccsds.sle.api.isrv.irocf.types.ROCF_UpdateMode;

public class RocfSIDescriptor extends SIDescriptor
{

    private long transferBufferSize;

    private int latencyLimit;

    private ROCF_UpdateMode[] permittedUpdateModeSet;

    private ROCF_ControlWordType[] permittedControlWordTypeSet;

    private long[] permittedTcVcidSet;

    private ROCF_Gvcid[] permittedGvcidSet;

    private ROCF_DeliveryMode dm;

    private String initiator;
    
	private int minimumReportingCycle;

    public ROCF_DeliveryMode getDeliveryMode()
    {
        return this.dm;
    }

    public int getLatencyLimit()
    {
        return this.latencyLimit;
    }

    public ROCF_ControlWordType[] getPermittedControlWordTypeSet()
    {
        return this.permittedControlWordTypeSet;
    }

    public ROCF_Gvcid[] getPermittedGvcidSet()
    {
        return this.permittedGvcidSet;
    }

    public long[] getPermittedTcVcidSet()
    {
        return this.permittedTcVcidSet;
    }

    public ROCF_UpdateMode[] getPermittedUpdateModeSet()
    {
        return this.permittedUpdateModeSet;
    }

    public long getTransferBufferSize()
    {
        return this.transferBufferSize;
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

    public void setDeliveryMode(ROCF_DeliveryMode dm)
    {
        this.dm = dm;

    }

    public void setLatencyLimit(int latencyLimit)
    {
        this.latencyLimit = latencyLimit;

    }

    public void setPermittedControlWordTypeSet(ROCF_ControlWordType[] permittedControlWordTypeSet)
    {
        this.permittedControlWordTypeSet = permittedControlWordTypeSet;

    }

    public void setPermittedGvcidSet(ROCF_Gvcid[] permittedGvcidSet)
    {
        this.permittedGvcidSet = permittedGvcidSet;

    }

    public void setPermittedTcVcidSet(long[] permittedTcVcidSet)
    {
        this.permittedTcVcidSet = permittedTcVcidSet;

    }

    public void setPermittedUpdateModeSet(ROCF_UpdateMode[] permittedUpdateModeSet)
    {
        this.permittedUpdateModeSet = permittedUpdateModeSet;

    }

    public void setTransferBufferSize(long transferBufferSize)
    {
        this.transferBufferSize = transferBufferSize;

    }

    public String getInitiator()
    {
        return this.initiator;
    }

    public void setInitiator(String initiator)
    {
        this.initiator = initiator;
    }

    @Override
    public SLE_ApplicationIdentifier getApplicationIdentifier()
    {
        return SLE_ApplicationIdentifier.sleAI_rtnChOcf;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.dm == null) ? 0 : this.dm.hashCode());
        result = prime * result + ((this.initiator == null) ? 0 : this.initiator.hashCode());
        result = prime * result + this.latencyLimit;
        result = prime * result + Arrays.hashCode(this.permittedControlWordTypeSet);
        result = prime * result + Arrays.hashCode(this.permittedGvcidSet);
        result = prime * result + Arrays.hashCode(this.permittedTcVcidSet);
        result = prime * result + Arrays.hashCode(this.permittedUpdateModeSet);
        result = prime * result + (int) (this.getReturnTimeoutPeriod() ^ (this.getReturnTimeoutPeriod() >>> 32));
        result = prime * result + (int) (this.transferBufferSize ^ (this.transferBufferSize >>> 32));
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
        RocfSIDescriptor other = (RocfSIDescriptor) obj;
        if (this.dm != other.dm)
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
        if (!Arrays.equals(this.permittedControlWordTypeSet, other.permittedControlWordTypeSet))
        {
            return false;
        }
        if (!Arrays.equals(this.permittedGvcidSet, other.permittedGvcidSet))
        {
            return false;
        }
        if (!Arrays.equals(this.permittedTcVcidSet, other.permittedTcVcidSet))
        {
            return false;
        }
        if (!Arrays.equals(this.permittedUpdateModeSet, other.permittedUpdateModeSet))
        {
            return false;
        }
        if (this.transferBufferSize != other.transferBufferSize)
        {
            return false;
        }
        return true;
    }

}
