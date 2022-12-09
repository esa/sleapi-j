package esa.sle.sicf.si.descriptors;

import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import ccsds.sle.api.isle.iutl.ISLE_Time;

public abstract class SIDescriptor
{

    private ISLE_SII serviceInstanceId = null;

    private ISLE_Time serviceInstanceStartTime;

    private ISLE_Time serviceInstanceStopTime;

    private String initiatorId;

    private String responderId;

    private String responderPortId;

    private long returnTimeOutPeriod;

    private int version;

    private SLE_AppRole appRole;


    public SLE_AppRole getBindInitiative()
    {
        return this.appRole;
    }

    public ISLE_Time getProvisionPeriodStart()
    {
        return this.serviceInstanceStartTime;
    }

    public ISLE_Time getProvisionPeriodStop()
    {
        return this.serviceInstanceStopTime;
    }

    public String getResponderPortIdentifier()
    {
        return this.responderPortId;
    }

    public long getReturnTimeoutPeriod()
    {
        return this.returnTimeOutPeriod;
    }

    public ISLE_SII getServiceInstanceId()
    {
        return this.serviceInstanceId;
    }

    public void setBindInitiative(SLE_AppRole appRole)
    {
        this.appRole = appRole;
    }

    public void setServiceInstanceStartTime(ISLE_Time serviceInstanceStartTime)
    {
        this.serviceInstanceStartTime = serviceInstanceStartTime;
    }

    public void setServiceInstanceStopTime(ISLE_Time serviceInstanceStopTime)
    {
        this.serviceInstanceStopTime = serviceInstanceStopTime;
    }

    public void setProvisionPeriod(ISLE_Time serviceInstanceStartTime, ISLE_Time serviceInstanceStoptTime)
    {
        this.serviceInstanceStartTime = serviceInstanceStartTime;
        this.serviceInstanceStopTime = serviceInstanceStoptTime;
    }

    public void setResponderPortIdentifier(String responderPortId)
    {
        this.responderPortId = responderPortId;
    }

    public void setReturnTimeoutPeriod(long returnTimeOutPeriod)
    {
        this.returnTimeOutPeriod = returnTimeOutPeriod;
    }

    public void setServiceInstanceId(ISLE_SII serviceInstanceId)
    {
        this.serviceInstanceId = serviceInstanceId;

    }

    public String getInitiatorId()
    {
        return this.initiatorId;
    }

    public void setInitiatorId(String initiatorId)
    {
        this.initiatorId = initiatorId;
    }

    public String getResponderId()
    {
        return this.responderId;
    }

    public void setResponderId(String responderId)
    {
        this.responderId = responderId;
    }

    public int getVersion()
    {
        return this.version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public abstract SLE_ApplicationIdentifier getApplicationIdentifier();

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.appRole == null) ? 0 : this.appRole.hashCode());
        result = prime * result + ((this.initiatorId == null) ? 0 : this.initiatorId.hashCode());
        result = prime * result + ((this.responderId == null) ? 0 : this.responderId.hashCode());
        result = prime * result + ((this.responderPortId == null) ? 0 : this.responderPortId.hashCode());
        result = prime * result + (int) (this.returnTimeOutPeriod ^ (this.returnTimeOutPeriod >>> 32));
        result = prime * result + ((this.serviceInstanceId == null) ? 0 : this.serviceInstanceId.hashCode());
        result = prime * result
                 + ((this.serviceInstanceStartTime == null) ? 0 : this.serviceInstanceStartTime.hashCode());
        result = prime * result
                 + ((this.serviceInstanceStopTime == null) ? 0 : this.serviceInstanceStopTime.hashCode());
        result = prime * result + this.version;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        SIDescriptor other = (SIDescriptor) obj;
        if (this.appRole != other.appRole)
        {
            return false;
        }
        if (this.initiatorId == null)
        {
            if (other.initiatorId != null)
            {
                return false;
            }
        }
        else if (!this.initiatorId.equals(other.initiatorId))
        {
            return false;
        }
        if (this.responderId == null)
        {
            if (other.responderId != null)
            {
                return false;
            }
        }
        else if (!this.responderId.equals(other.responderId))
        {
            return false;
        }
        if (this.responderPortId == null)
        {
            if (other.responderPortId != null)
            {
                return false;
            }
        }
        else if (!this.responderPortId.equals(other.responderPortId))
        {
            return false;
        }
        if (this.returnTimeOutPeriod != other.returnTimeOutPeriod)
        {
            return false;
        }
        if (this.serviceInstanceId == null)
        {
            if (other.serviceInstanceId != null)
            {
                return false;
            }
        }
        else if (!this.serviceInstanceId.equals(other.serviceInstanceId))
        {
            return false;
        }
        if (this.serviceInstanceStartTime == null)
        {
            if (other.serviceInstanceStartTime != null)
            {
                return false;
            }
        }
        else if (!this.serviceInstanceStartTime.equals(other.serviceInstanceStartTime))
        {
            return false;
        }
        if (this.serviceInstanceStopTime == null)
        {
            if (other.serviceInstanceStopTime != null)
            {
                return false;
            }
        }
        else if (!this.serviceInstanceStopTime.equals(other.serviceInstanceStopTime))
        {
            return false;
        }
        if (this.version != other.version)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "SIDescriptor [serviceInstanceId="
               + ((this.serviceInstanceId == null) ? "null" : this.serviceInstanceId.getAsciiForm()) + "]";
    }

}
