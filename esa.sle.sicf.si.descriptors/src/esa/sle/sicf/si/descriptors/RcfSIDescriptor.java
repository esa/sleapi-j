package esa.sle.sicf.si.descriptors;

import java.util.Arrays;

import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isrv.ircf.types.RCF_DeliveryMode;
import ccsds.sle.api.isrv.ircf.types.RCF_Gvcid;

public class RcfSIDescriptor extends SIDescriptor
{

    private RCF_DeliveryMode dm;

    private String initiator;

    private long transferBufferSize;

    private int latencyLimit;

    private RCF_Gvcid[] permittedGvcidSet;

    private SLE_AppRole bindInitiative;
    
	private int minimumReportingCycle;

    @Override
    public SLE_AppRole getBindInitiative()
    {
        return this.bindInitiative;
    }

    @Override
    public void setBindInitiative(SLE_AppRole bindInitiative)
    {
        this.bindInitiative = bindInitiative;
    }

    public RCF_DeliveryMode getDeliveryMode()
    {
        return this.dm;
    }

    public int getLatencyLimit()
    {
        return this.latencyLimit;
    }

    public RCF_Gvcid[] getPermittedGvcidSet()
    {
        return this.permittedGvcidSet;
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

    public void setDeliveryMode(RCF_DeliveryMode dm)
    {
        this.dm = dm;

    }

    public void setLatencyLimit(int latencyLimit)
    {
        this.latencyLimit = latencyLimit;

    }

    public void setPermittedGvcidSet(RCF_Gvcid[] permittedGvcidSet)
    {
        this.permittedGvcidSet = permittedGvcidSet;

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
        return SLE_ApplicationIdentifier.sleAI_rtnChFrames;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.bindInitiative == null) ? 0 : this.bindInitiative.hashCode());
        result = prime * result + ((this.dm == null) ? 0 : this.dm.hashCode());
        result = prime * result + ((this.initiator == null) ? 0 : this.initiator.hashCode());
        result = prime * result + this.latencyLimit;
        result = prime * result + Arrays.hashCode(this.permittedGvcidSet);
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
        RcfSIDescriptor other = (RcfSIDescriptor) obj;
        if (this.bindInitiative != other.bindInitiative)
        {
            return false;
        }
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
        if (!Arrays.equals(this.permittedGvcidSet, other.permittedGvcidSet))
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
