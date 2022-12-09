package esa.sle.impl.api.apiop.rcfop;

import java.util.Arrays;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_DiagnosticType;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isrv.ifsp.types.FSP_ParameterName;
import ccsds.sle.api.isrv.ircf.IRCF_GetParameter;
import ccsds.sle.api.isrv.ircf.types.RCF_ChannelType;
import ccsds.sle.api.isrv.ircf.types.RCF_DeliveryMode;
import ccsds.sle.api.isrv.ircf.types.RCF_GetParameterDiagnostic;
import ccsds.sle.api.isrv.ircf.types.RCF_Gvcid;
import ccsds.sle.api.isrv.ircf.types.RCF_ParameterName;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_ConfirmedOperation;

/**
 * @NameRAF GetParameter Operation@EndName
 * @ResponsibilityThe class implements the RCF specific GetParameter operation.@EndResponsibility
 */
public class EE_RCF_GetParameter extends IEE_SLE_ConfirmedOperation implements IRCF_GetParameter
{
    /**
     * The requested parameter.
     */
    private RCF_ParameterName requestedParam = RCF_ParameterName.rcfPN_invalid;

    /**
     * The returned parameter.
     */
    private RCF_ParameterName returnedParam = RCF_ParameterName.rcfPN_invalid;

    /**
     * The delivery mode.
     */
    private RCF_DeliveryMode deliveryMode = RCF_DeliveryMode.rcfDM_invalid;

    /**
     * The latency limit.
     */
    private int latencyLimit = 0;

    /**
     * The transfer buffer size.
     */
    private long transferBufferSize = 0;

    /**
     * The global VC identifier.
     */
    private RCF_Gvcid gvcid = null;

    /**
     * The list of global VC identifier.
     */
    private RCF_Gvcid[] gvcidList = null;

    /**
     * The reporting cycle.
     */
    private long reportingCycle = 0;

    /**
     * The return timeout period.
     */
    private long returnTimeoutPeriod = 0;

    /**
     * The GetParameter diagnostic.
     */
    private RCF_GetParameterDiagnostic paramDiagnostic = RCF_GetParameterDiagnostic.rcfGP_invalid;
    
    private long minimumReportingCycle = 0;


    private EE_RCF_GetParameter(final EE_RCF_GetParameter right)
    {
        super(right);
        this.requestedParam = right.requestedParam;
        this.returnedParam = right.returnedParam;
        this.deliveryMode = right.deliveryMode;
        this.latencyLimit = right.latencyLimit;
        this.transferBufferSize = right.transferBufferSize;
        if (right.gvcid != null)
        {
            this.gvcid = right.gvcid;
        }
        if (right.gvcidList != null)
        {
            this.gvcidList = new RCF_Gvcid[right.gvcidList.length];
            for (int i = 0; i < this.gvcidList.length; i++)
            {
                this.gvcidList[i] = right.gvcidList[i];
            }
        }
        this.reportingCycle = right.reportingCycle;
        this.returnTimeoutPeriod = right.returnTimeoutPeriod;
        this.paramDiagnostic = right.paramDiagnostic;
        this.minimumReportingCycle = right.minimumReportingCycle;
    }

    public EE_RCF_GetParameter(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnChFrames, SLE_OpType.sleOT_getParameter, version, preporter);
        this.requestedParam = RCF_ParameterName.rcfPN_invalid;
        this.returnedParam = RCF_ParameterName.rcfPN_invalid;
        this.deliveryMode = RCF_DeliveryMode.rcfDM_invalid;
        this.latencyLimit = 0;
        this.transferBufferSize = 0;
        this.gvcid = null;
        this.gvcidList = null;
        this.reportingCycle = 0;
        this.returnTimeoutPeriod = 0;
        this.paramDiagnostic = RCF_GetParameterDiagnostic.rcfGP_invalid;
        this.minimumReportingCycle = 0;
    }

    @Override
    public synchronized RCF_ParameterName getRequestedParameter()
    {
        return this.requestedParam;
    }

    @Override
    public synchronized RCF_ParameterName getReturnedParameter()
    {
        return this.returnedParam;
    }

    @Override
    public synchronized RCF_DeliveryMode getDeliveryMode()
    {
        assert (this.returnedParam == RCF_ParameterName.rcfPN_deliveryMode) : "invalid getXXX call";
        return this.deliveryMode;
    }

    @Override
    public synchronized int getLatencyLimit()
    {
        assert (this.returnedParam == RCF_ParameterName.rcfPN_latencyLimit) : "invalid getXXX call";
        if (this.deliveryMode == RCF_DeliveryMode.rcfDM_offline)
        {
            return 0;
        }
        return this.latencyLimit;
    }

    @Override
    public synchronized long getTransferBufferSize()
    {
        assert (this.returnedParam == RCF_ParameterName.rcfPN_bufferSize) : "invalid getXXX call";
        return this.transferBufferSize;
    }

    @Override
    public synchronized RCF_Gvcid getRequestedGvcid()
    {
        assert (this.returnedParam == RCF_ParameterName.rcfPN_requestedGvcid) : "invalid getxxx call";
        return this.gvcid;
    }

    @Override
    public synchronized RCF_Gvcid[] getPermittedGvcidSet()
    {
        assert (this.returnedParam == RCF_ParameterName.rcfPN_permittedGvcidSet) : "invalid getXXX call";
        return this.gvcidList;
    }

    @Override
    public synchronized RCF_Gvcid[] removePermittedGvcidSet()
    {
        assert (this.returnedParam == RCF_ParameterName.rcfPN_permittedGvcidSet) : "invalid getXXX call";
        RCF_Gvcid[] ptmp = this.gvcidList;
        this.gvcidList = null;
        return ptmp;
    }

    @Override
    public synchronized long getReportingCycle()
    {
        assert (this.returnedParam == RCF_ParameterName.rcfPN_reportingCycle) : "invalid getXXX call";
        return this.reportingCycle;
    }

    @Override
    public synchronized long getReturnTimeoutPeriod()
    {
        assert (this.returnedParam == RCF_ParameterName.rcfPN_returnTimeoutPeriod) : "invalid get call";
        return this.returnTimeoutPeriod;
    }

    @Override
    public synchronized RCF_GetParameterDiagnostic getGetParameterDiagnostic()
    {
        assert (getResult() == SLE_Result.sleRES_negative && getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics) : "invalid getxxx call";
        return this.paramDiagnostic;
    }

	@Override
	public long getMinimumReportingCycle() {
		
		return minimumReportingCycle;
	}
	
    @Override
    public synchronized void setRequestedParameter(RCF_ParameterName name)
    {
        this.requestedParam = name;
    }

    @Override
    public synchronized void setDeliveryMode(RCF_DeliveryMode mode)
    {
        this.returnedParam = RCF_ParameterName.rcfPN_deliveryMode;
        this.deliveryMode = mode;
    }

    @Override
    public synchronized void setLatencyLimit(int limit)
    {
        this.latencyLimit = limit;
        this.returnedParam = RCF_ParameterName.rcfPN_latencyLimit;
    }

    @Override
    public synchronized void setTransferBufferSize(long size)
    {
        this.transferBufferSize = size;
        this.returnedParam = RCF_ParameterName.rcfPN_bufferSize;
    }

    @Override
    public synchronized void setRequestedGvcid(RCF_Gvcid id)
    {
        this.returnedParam = RCF_ParameterName.rcfPN_requestedGvcid;
        if (id != null)
        {
            this.gvcid = id;
        }
        else
        {
            if (this.gvcid != null)
            {
                this.gvcid = null;
            }
        }
    }

	@Override
	public void setMinimumReportingCycle(long mrc) {
		this.returnedParam = RCF_ParameterName.rcfPN_minReportingCycle;
		this.minimumReportingCycle = mrc;	
	}

    @Override
    public synchronized void putRequestedGvcid(RCF_Gvcid pid)
    {
        this.returnedParam = RCF_ParameterName.rcfPN_requestedGvcid;
        this.gvcid = pid;
    }

    @Override
    public synchronized void setPermittedGvcidSet(RCF_Gvcid[] idList)
    {
        this.returnedParam = RCF_ParameterName.rcfPN_permittedGvcidSet;
        this.gvcidList = new RCF_Gvcid[idList.length];
        for (int i = 0; i < this.gvcidList.length; i++)
        {
            this.gvcidList[i] = idList[i];
        }
    }

    @Override
    public synchronized void putPermittedGvcidSet(RCF_Gvcid[] idList)
    {
        this.returnedParam = RCF_ParameterName.rcfPN_permittedGvcidSet;
        this.gvcidList = idList;
    }

    @Override
    public synchronized void setReportingCycle(long cycle)
    {
        this.reportingCycle = cycle;
        this.returnedParam = RCF_ParameterName.rcfPN_reportingCycle;
    }

    @Override
    public synchronized void setReturnTimeoutPeriod(long period)
    {
        this.returnTimeoutPeriod = period;
        this.returnedParam = RCF_ParameterName.rcfPN_returnTimeoutPeriod;
    }

    @Override
    public synchronized void setGetParameterDiagnostic(RCF_GetParameterDiagnostic diagostic)
    {
        this.paramDiagnostic = diagostic;
        setSpecificDiagnostics();
    }

    @Override
    public synchronized void verifyReturnArguments() throws SleApiException
    {
        super.verifyReturnArguments();
        if (this.returnedParam != this.requestedParam)
        {
            throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
        }
        switch (this.returnedParam)
        {
        case rcfPN_bufferSize:
            if (this.transferBufferSize == 0)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case rcfPN_deliveryMode:
            if (this.deliveryMode == RCF_DeliveryMode.rcfDM_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case rcfPN_latencyLimit:
            break;
        case rcfPN_permittedGvcidSet:
            if (this.gvcidList == null)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case rcfPN_reportingCycle:
            break;
        case rcfPN_requestedGvcid:
            if (!(this.gvcid != null))
            {
                if (getOpVersionNumber() < 2)
                {
                    throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
                }
            }
            else
            {
                if (this.gvcid.getType() == RCF_ChannelType.rcfCT_invalid)
                {
                    throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
                }
                if (this.gvcid.getVersion() == 0)
                {
                    if ((this.gvcid.getScid() < 0) || (this.gvcid.getScid() > 1023))
                    {
                        throw new SleApiException(HRESULT.SLE_E_INVALIDID);
                    }
                    if (this.gvcid.getType() == RCF_ChannelType.rcfCT_VirtualChannel)
                    {
                        if ((this.gvcid.getVcid() < 0) || (this.gvcid.getVcid() > 7))
                        {
                            throw new SleApiException(HRESULT.SLE_E_INVALIDID);
                        }
                    }
                }
                else if (this.gvcid.getVersion() == 1)
                {
                    if ((this.gvcid.getScid() < 0) || (this.gvcid.getScid() > 255))
                    {
                        throw new SleApiException(HRESULT.SLE_E_INVALIDID);
                    }
                    if (this.gvcid.getType() == RCF_ChannelType.rcfCT_VirtualChannel)
                    {
                        if ((this.gvcid.getVcid() < 0) || (this.gvcid.getVcid() > 63))
                        {
                            throw new SleApiException(HRESULT.SLE_E_INVALIDID);
                        }
                    }
                }
                else
                {
                    throw new SleApiException(HRESULT.SLE_E_INVALIDID);
                }
            }
            break;
        case rcfPN_returnTimeoutPeriod:
            if (this.returnTimeoutPeriod == 0)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case rcfPN_minReportingCycle:
            if (this.minimumReportingCycle < 1 || this.minimumReportingCycle > 600)
            {
                throw new SleApiException(HRESULT.SLE_E_RANGE);
            }
            break;
        case rcfPN_invalid:
        default:
            break;
        }
        if ((getResult() == SLE_Result.sleRES_negative)
            && (getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics))
        {
            if (this.paramDiagnostic == RCF_GetParameterDiagnostic.rcfGP_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
        }

    }

    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        super.verifyInvocationArguments();
        if (this.requestedParam == RCF_ParameterName.rcfPN_invalid)
        {
            throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
        }
    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_RCF_GetParameter ptmp = new EE_RCF_GetParameter(this);
        return ptmp;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder("");
        printOn(oss, maxDumpLength);

        oss.append("Requested parameter    : " + this.requestedParam + "\n");
        oss.append("Returned parameter     : " + this.returnedParam + "\n");
        oss.append("Delivery mode          : " + this.deliveryMode + "\n");
        if (this.gvcid != null)
        {
            oss.append("Global VC type         : " + this.gvcid.getType() + "\n");
            oss.append("Global VC scId         : " + this.gvcid.getScid() + "\n");
            oss.append("Global VC version      : " + this.gvcid.getVersion() + "\n");
            oss.append("Global VC channel ID   : " + this.gvcid.getVcid() + "\n");
        }
        else
        {
            oss.append("Global VC              : \n");
        }
        if (this.gvcidList != null)
        {
            oss.append("Permitted VC list      : \n");
            if (this.gvcidList.length > 1)
            {
                oss.append("\n");
            }
            for (int i = 0; i < this.gvcidList.length; i++)
            {
                if (i > 0)
                {
                    oss.append("\n");// separate previous from current with
                                     // newline
                }
                oss.append("Permitted VC type         : " + this.gvcidList[i].getType() + "\n");
                oss.append("Permitted VC scId         : " + this.gvcidList[i].getScid() + "\n");
                oss.append("Permitted VC version      : " + this.gvcidList[i].getVersion() + "\n");
                oss.append("Permitted VC channel ID   : " + this.gvcidList[i].getVcid() + "\n");
            }
            // add an end of line to separate
            // last element from other parameter values.
            if (this.gvcidList.length > 1)
            {
                oss.append("\n");
            }
        }
        oss.append("Latency limit          : " + this.latencyLimit + "\n");
        oss.append("Transfer buffer size   : " + this.transferBufferSize + "\n");
        oss.append("Reporting cycle        : " + this.reportingCycle + "\n");
        oss.append("Return timeout period  : " + this.returnTimeoutPeriod + "\n");
        oss.append("Get parameter diag.    : " + this.paramDiagnostic + "\n");
        oss.append("Minimum reporting cycle: " + this.getMinimumReportingCycle() + "\n");
        String ret = oss.toString();
        return ret;

    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_ConfirmedOperation.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Operation.class)
        {
            return (T) this;
        }
        else if (iid == IRCF_GetParameter.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    @Override
    public synchronized String toString()
    {
        return "EE_RCF_GetParameter [requestedParam=" + this.requestedParam + ", returnedParam=" + this.returnedParam
               + ", deliveryMode=" + this.deliveryMode + ", latencyLimit=" + this.latencyLimit
               + ", transferBufferSize=" + this.transferBufferSize + ", gvcid="
               + ((this.gvcid != null) ? this.gvcid : "") + ", gvcidList="
               + ((this.gvcidList != null) ? Arrays.toString(this.gvcidList) : "") + ", reportingCycle="
               + this.reportingCycle + ", returnTimeoutPeriod=" + this.returnTimeoutPeriod + ", paramDiagnostic="
               + this.paramDiagnostic + "]";
    }
}
