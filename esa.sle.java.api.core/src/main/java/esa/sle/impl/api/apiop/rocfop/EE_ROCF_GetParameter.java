/**
 * @(#) EE_ROCF_GetParameter.java
 */

package esa.sle.impl.api.apiop.rocfop;

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
import ccsds.sle.api.isrv.irocf.IROCF_GetParameter;
import ccsds.sle.api.isrv.irocf.types.ROCF_ChannelType;
import ccsds.sle.api.isrv.irocf.types.ROCF_ControlWordType;
import ccsds.sle.api.isrv.irocf.types.ROCF_DeliveryMode;
import ccsds.sle.api.isrv.irocf.types.ROCF_GetParameterDiagnostic;
import ccsds.sle.api.isrv.irocf.types.ROCF_Gvcid;
import ccsds.sle.api.isrv.irocf.types.ROCF_ParameterName;
import ccsds.sle.api.isrv.irocf.types.ROCF_UpdateMode;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_ConfirmedOperation;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * The class implements the ROCF specific GetParameter operation.
 */
public class EE_ROCF_GetParameter extends IEE_SLE_ConfirmedOperation implements IROCF_GetParameter
{
    /**
     * The requested parameter.
     */
    private ROCF_ParameterName requestedParam = ROCF_ParameterName.rocfPN_invalid;

    /**
     * The returned parameter.
     */
    private ROCF_ParameterName returnedParam = ROCF_ParameterName.rocfPN_invalid;

    /**
     * The delivery mode.
     */
    private ROCF_DeliveryMode deliveryMode = ROCF_DeliveryMode.rocfDM_invalid;

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
    private ROCF_Gvcid gvcid = null;

    /**
     * The list of global VC identifier.
     */
    private ROCF_Gvcid[] gvcidList = null;

    private ROCF_ControlWordType requestedControlWordType = ROCF_ControlWordType.rocfCWT_invalid;

    private ROCF_ControlWordType[] permittedControlWordTypeList = null;

    private boolean tcVcidUsed = false;

    private long requestedTcVcid = 0;

    /**
     * The GetParameter diagnostic.
     */
    private ROCF_GetParameterDiagnostic paramDiagnostic = ROCF_GetParameterDiagnostic.rocfGP_invalid;

    private long[] permittedTcVcidList = null;

    // private long permittedTcVcidListSize = 0;

    private ROCF_UpdateMode requestedUpdateMode = ROCF_UpdateMode.rocfUM_invalid;

    private ROCF_UpdateMode[] permittedUpdateModeList = null;

    /**
     * The reporting cycle.
     */
    private long reportingCycle = 0;

    /**
     * The return timeout period.
     */
    private long returnTimeoutPeriod = 0;
    
    private long minimumReportingCycle = 0;


    private EE_ROCF_GetParameter(final EE_ROCF_GetParameter right)
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
            for (int i = 0; i < right.gvcidList.length; i++)
            {
                this.gvcidList[i] = right.gvcidList[i];
            }
        }

        if (right.permittedControlWordTypeList != null)
        {
            this.permittedControlWordTypeList = right.permittedControlWordTypeList;
        }
        this.tcVcidUsed = right.tcVcidUsed;
        this.requestedTcVcid = right.requestedTcVcid;

        if (right.permittedTcVcidList != null)
        {
            int n = right.permittedTcVcidList.length;
            this.permittedTcVcidList = new long[n];
            for (int i = 0; i < n; i++)
            {
                this.permittedTcVcidList[i] = right.permittedTcVcidList[i];
            }
        }
        this.reportingCycle = right.reportingCycle;
        this.returnTimeoutPeriod = right.returnTimeoutPeriod;
        this.paramDiagnostic = right.paramDiagnostic;
        this.minimumReportingCycle = right.minimumReportingCycle;
    }

    public EE_ROCF_GetParameter(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnChOcf, SLE_OpType.sleOT_getParameter, version, preporter);
        this.requestedParam = ROCF_ParameterName.rocfPN_invalid;
        this.returnedParam = ROCF_ParameterName.rocfPN_invalid;
        this.deliveryMode = ROCF_DeliveryMode.rocfDM_invalid;
        this.latencyLimit = 0;
        this.transferBufferSize = 0;
        this.gvcid = null;
        this.gvcidList = null;
        this.requestedControlWordType = ROCF_ControlWordType.rocfCWT_invalid;
        this.permittedControlWordTypeList = null;
        this.tcVcidUsed = false;
        this.requestedTcVcid = 0;
        this.paramDiagnostic = ROCF_GetParameterDiagnostic.rocfGP_invalid;
        this.permittedTcVcidList = null;
        this.requestedUpdateMode = ROCF_UpdateMode.rocfUM_invalid;
        this.permittedUpdateModeList = null;
        this.reportingCycle = 0;
        this.returnTimeoutPeriod = 0;
        this.minimumReportingCycle = 0;
    }

    @Override
    public synchronized ROCF_ParameterName getRequestedParameter()
    {
        return this.requestedParam;
    }

    @Override
    public synchronized ROCF_ParameterName getReturnedParameter()
    {
        return this.returnedParam;
    }

    @Override
    public synchronized ROCF_DeliveryMode getDeliveryMode()
    {
        assert (this.returnedParam == ROCF_ParameterName.rocfPN_deliveryMode) : "invalid getXXX call";
        return this.deliveryMode;
    }

    @Override
    public synchronized int getLatencyLimit()
    {
        assert (this.returnedParam == ROCF_ParameterName.rocfPN_latencyLimit) : "invalid getXXX call";
        if (this.deliveryMode == ROCF_DeliveryMode.rocfDM_offline)
        {
            return 0;
        }
        return this.latencyLimit;
    }
    
    @Override
    public synchronized long getMinimumReportingCycle()
    {
        assert (this.returnedParam == ROCF_ParameterName.rocfPN_minReportingCycle) : "invalid getXXX call";
        return this.minimumReportingCycle;
    }

    @Override
    public synchronized long getTransferBufferSize()
    {
        assert (this.returnedParam == ROCF_ParameterName.rocfPN_bufferSize) : "invalid getXXX call";
        return this.transferBufferSize;

    }

    @Override
    public synchronized ROCF_Gvcid getRequestedGvcid()
    {
        assert (this.returnedParam == ROCF_ParameterName.rocfPN_requestedGvcid) : "invalid getxxx call";
        return this.gvcid;
    }

    @Override
    public synchronized ROCF_Gvcid[] getPermittedGvcidSet()
    {
        assert (this.returnedParam == ROCF_ParameterName.rocfPN_permittedGvcidSet) : "invalid getXXX call";
        return this.gvcidList;
    }

    @Override
    public synchronized ROCF_Gvcid[] removePermittedGvcidSet(long size)
    {
        assert (this.returnedParam == ROCF_ParameterName.rocfPN_permittedGvcidSet) : "invalid getXXX call";
        ROCF_Gvcid[] ptmp = this.gvcidList;
        this.gvcidList = null;
        return ptmp;
    }

    @Override
    public synchronized ROCF_ControlWordType getRequestedControlWordType()
    {
        assert (this.returnedParam == ROCF_ParameterName.rocfPN_requestedControlWordType) : "invalid getxxx call";
        return this.requestedControlWordType;
    }

    @Override
    public synchronized ROCF_ControlWordType[] removePermittedControlWordTypeSet(long size)
    {
        assert (this.returnedParam == ROCF_ParameterName.rocfPN_permittedControlWordTypeSet) : "invalid getXXX call";
        ROCF_ControlWordType[] tmp = this.permittedControlWordTypeList;
        this.permittedControlWordTypeList = null;
        return tmp;
    }

    @Override
    public synchronized ROCF_ControlWordType[] getPermittedControlWordTypeSet()
    {
        assert (this.returnedParam == ROCF_ParameterName.rocfPN_permittedControlWordTypeSet) : "invalid getXXX call";
        return this.permittedControlWordTypeList;
    }

    @Override
    public synchronized boolean getTcVcidUsed()
    {
        return this.tcVcidUsed;
    }

    @Override
    public synchronized long getRequestedTcVcid()
    {
        assert (this.returnedParam == ROCF_ParameterName.rocfPN_requestedTcVcid) : "invalid getXXX call";
        return this.requestedTcVcid;
    }

    @Override
    public synchronized long[] getPermittedTcVcidSet()
    {
        assert (this.returnedParam == ROCF_ParameterName.rocfPN_permittedTcVcidSet) : "invalid getXXX call";
        return this.permittedTcVcidList;
    }

    @Override
    public synchronized long[] removePermittedTcVcidSet()
    {
        assert (this.returnedParam == ROCF_ParameterName.rocfPN_permittedTcVcidSet) : "invalid getXXX call";
        long[] tmp = this.permittedTcVcidList;
        this.permittedTcVcidList = null;
        return tmp;
    }

    @Override
    public synchronized ROCF_UpdateMode getRequestedUpdateMode()
    {
        assert (this.returnedParam == ROCF_ParameterName.rocfPN_requestedUpdateMode) : "invalid getXXX call";
        return this.requestedUpdateMode;
    }

    @Override
    public synchronized ROCF_UpdateMode[] getPermittedUpdateModeSet()
    {
        assert (this.returnedParam == ROCF_ParameterName.rocfPN_permittedUpdateModeSet) : "invalid getXXX call";
        return this.permittedUpdateModeList;
    }

    @Override
    public synchronized ROCF_UpdateMode[] removePermittedUpdateModeSet()
    {
        assert (this.returnedParam == ROCF_ParameterName.rocfPN_permittedUpdateModeSet) : "invalid getXXX call";
        ROCF_UpdateMode[] tmp = this.permittedUpdateModeList;
        this.permittedUpdateModeList = null;
        return tmp;
    }

    @Override
    public synchronized long getReportingCycle()
    {
        assert (this.returnedParam == ROCF_ParameterName.rocfPN_reportingCycle) : "invalid getXXX call";
        return this.reportingCycle;
    }

    @Override
    public synchronized long getReturnTimeoutPeriod()
    {
        assert (this.returnedParam == ROCF_ParameterName.rocfPN_returnTimeoutPeriod) : "invalid getXXX call";
        return this.returnTimeoutPeriod;
    }

    @Override
    public synchronized ROCF_GetParameterDiagnostic getGetParameterDiagnostic()
    {
        assert (getResult() == SLE_Result.sleRES_negative && getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics) : "invalid getxxx call";
        return this.paramDiagnostic;
    }

    @Override
    public synchronized void setRequestedParameter(ROCF_ParameterName name)
    {
        this.requestedParam = name;
    }

    @Override
    public synchronized void setDeliveryMode(ROCF_DeliveryMode mode)
    {
        this.returnedParam = ROCF_ParameterName.rocfPN_deliveryMode;
        this.deliveryMode = mode;
    }

    @Override
    public synchronized void setLatencyLimit(int limit)
    {
        this.returnedParam = ROCF_ParameterName.rocfPN_latencyLimit;
        this.latencyLimit = limit;
    }    
    
	@Override
	public void setMinimumReportingCycle(long mrc) {
		this.returnedParam = ROCF_ParameterName.rocfPN_minReportingCycle;
		this.minimumReportingCycle = mrc;	
	}

    @Override
    public synchronized void setTransferBufferSize(long size)
    {
        this.returnedParam = ROCF_ParameterName.rocfPN_bufferSize;
        this.transferBufferSize = size;
    }

    @Override
    public synchronized void setRequestedGvcid(ROCF_Gvcid id)
    {
        this.returnedParam = ROCF_ParameterName.rocfPN_requestedGvcid;
        if (id != null)
        {
            if (!(this.gvcid != null))
            {
                this.gvcid = id;
            }
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
    public synchronized void putRequestedGvcid(ROCF_Gvcid pid)
    {
        this.returnedParam = ROCF_ParameterName.rocfPN_requestedGvcid;
        this.gvcid = null;
        this.gvcid = pid;
    }

    @Override
    public synchronized void setPermittedGvcidSet(ROCF_Gvcid[] idList)
    {
        this.returnedParam = ROCF_ParameterName.rocfPN_permittedGvcidSet;
        this.gvcidList = null;

        if (idList.length > 0)
        {
            this.gvcidList = new ROCF_Gvcid[idList.length];
            for (int i = 0; i < idList.length; i++)
            {
                this.gvcidList[i] = idList[i];
            }
        }
    }

    @Override
    public synchronized void putPermittedGvcidSet(ROCF_Gvcid[] idList)
    {
        this.returnedParam = ROCF_ParameterName.rocfPN_permittedGvcidSet;
        this.gvcidList = null;
        this.gvcidList = idList;
    }

    @Override
    public synchronized void setRequestedControlWordType(ROCF_ControlWordType type)
    {
        this.returnedParam = ROCF_ParameterName.rocfPN_requestedControlWordType;
        this.requestedControlWordType = type;
    }

    @Override
    public synchronized void setPermittedControlWordTypeSet(final ROCF_ControlWordType[] typeSet)
    {
        this.returnedParam = ROCF_ParameterName.rocfPN_permittedControlWordTypeSet;
        this.permittedControlWordTypeList = new ROCF_ControlWordType[typeSet.length];

        if (typeSet.length > 0)
        {
            for (int i = 0; i < typeSet.length; i++)
            {
                this.permittedControlWordTypeList[i] = typeSet[i];
            }
        }
    }

    @Override
    public synchronized void putPermittedControlWordTypeSet(ROCF_ControlWordType[] typeSet)
    {
        this.returnedParam = ROCF_ParameterName.rocfPN_permittedControlWordTypeSet;
        this.permittedControlWordTypeList = null;
        this.permittedControlWordTypeList = typeSet;
    }

    @Override
    public synchronized void setRequestedTcVcid(long id)
    {
        this.returnedParam = ROCF_ParameterName.rocfPN_requestedTcVcid;
        this.requestedTcVcid = id;
        this.tcVcidUsed = true;
    }

    @Override
    public synchronized void setPermittedTcVcidSet(long[] idSet)
    {
        this.returnedParam = ROCF_ParameterName.rocfPN_permittedTcVcidSet;
        this.permittedTcVcidList = null;

        if (idSet.length > 0)
        {
            this.permittedTcVcidList = new long[idSet.length];
            for (int i = 0; i < idSet.length; i++)
            {
                this.permittedTcVcidList[i] = idSet[i];
            }
        }
    }

    @Override
    public synchronized void putPermittedTcVcidSet(long[] idSet)
    {
        this.returnedParam = ROCF_ParameterName.rocfPN_permittedTcVcidSet;
        this.permittedTcVcidList = null;
        this.permittedTcVcidList = idSet;
    }

    @Override
    public synchronized void setRequestedUpdateMode(ROCF_UpdateMode mode)
    {
        this.returnedParam = ROCF_ParameterName.rocfPN_requestedUpdateMode;
        this.requestedUpdateMode = mode;
    }

    @Override
    public synchronized void setPermittedUpdateModeSet(ROCF_UpdateMode[] modeSet)
    {
        this.returnedParam = ROCF_ParameterName.rocfPN_permittedUpdateModeSet;
        this.permittedUpdateModeList = null;

        if (modeSet.length > 0)
        {
            this.permittedUpdateModeList = new ROCF_UpdateMode[modeSet.length];
            for (int i = 0; i < modeSet.length; i++)
            {
                this.permittedUpdateModeList[i] = modeSet[i];
            }
        }
    }

    @Override
    public synchronized void putPermittedUpdateModeSet(ROCF_UpdateMode[] modeSet)
    {
        this.returnedParam = ROCF_ParameterName.rocfPN_permittedUpdateModeSet;
        this.permittedUpdateModeList = null;
        this.permittedUpdateModeList = modeSet;
    }

    @Override
    public synchronized void setReportingCycle(long cycle)
    {
        this.returnedParam = ROCF_ParameterName.rocfPN_reportingCycle;
        this.reportingCycle = cycle;
    }

    @Override
    public synchronized void setReturnTimeoutPeriod(long period)
    {
        this.returnedParam = ROCF_ParameterName.rocfPN_returnTimeoutPeriod;
        this.returnTimeoutPeriod = period;
    }

    @Override
    public synchronized void setGetParameterDiagnostic(ROCF_GetParameterDiagnostic diagostic)
    {
        this.paramDiagnostic = diagostic;
        setSpecificDiagnostics();
    }

    /**
     * @throws SleApiException
     * @FunctionSee specification of ISLE_ConfirmedOperation.@EndFunction
     */
    @Override
    public synchronized void verifyReturnArguments() throws SleApiException
    {
        super.verifyReturnArguments();

        if (this.returnedParam != this.requestedParam)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                               EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                               "Requested parameter type"));
        }

        switch (this.returnedParam)
        {
        case rocfPN_deliveryMode:
            if (this.deliveryMode == ROCF_DeliveryMode.rocfDM_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Delivery mode"));
            }
            break;

        case rocfPN_bufferSize:
            if (this.transferBufferSize == 0)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Transfer buffer size"));
            }
            break;

        case rocfPN_requestedGvcid:
            if (this.gvcid != null)
            {
                if (this.gvcid.getType() == ROCF_ChannelType.rocfCT_invalid)
                {
                    throw new SleApiException(logAlarm(HRESULT.SLE_E_INVALIDID,
                                                       EE_LogMsg.EE_OP_LM_InvalidID.getCode(),
                                                       "Global VCID type"));
                }
                if (this.gvcid.getVersion() == 0)
                {
                    if ((this.gvcid.getScid() < 0) || (this.gvcid.getScid() > 1023))
                    {
                        throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                           EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                           "Spacecraft ID",
                                                           "0..1023"));
                    }
                    if (this.gvcid.getType() == ROCF_ChannelType.rocfCT_VirtualChannel)
                    {
                        if ((this.gvcid.getVcid() < 0) || (this.gvcid.getVcid() > 7))
                        {
                            throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                               EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                               "VC ID",
                                                               "0..7"));
                        }
                    }
                }
                else if (this.gvcid.getVersion() == 1)
                {
                    if ((this.gvcid.getScid() < 0) || (this.gvcid.getScid() > 255))
                    {
                        throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                           EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                           "Spacecraft ID",
                                                           "0..255"));
                    }
                    if (this.gvcid.getType() == ROCF_ChannelType.rocfCT_VirtualChannel)
                    {
                        if ((this.gvcid.getVcid() < 0) || (this.gvcid.getVcid() > 63))
                        {
                            throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                               EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                               "VC ID",
                                                               "0..63"));
                        }
                    }
                }
                else
                {
                    throw new SleApiException(logAlarm(HRESULT.SLE_E_INVALIDID,
                                                       EE_LogMsg.EE_OP_LM_InvalidID.getCode(),
                                                       "Global VCID version"));
                }
            }
            break;

        case rocfPN_permittedGvcidSet:
            if (this.gvcidList == null)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "permitted global VCID set"));
            }
            break;

        case rocfPN_permittedControlWordTypeSet:
            if (this.permittedControlWordTypeList == null)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Permitted control word type set"));
            }
            break;

        case rocfPN_permittedTcVcidSet:
            if (this.tcVcidUsed)
            {
                for (ROCF_ControlWordType element : this.permittedControlWordTypeList)
                {
                    if (element != ROCF_ControlWordType.rocfCWT_notClcw)
                    {
                        if (this.permittedTcVcidList == null)
                        {
                            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                               "Permitted TC VCID set"));
                        }
                    }
                }
            }
            break;

        case rocfPN_permittedUpdateModeSet:
            if (this.permittedUpdateModeList == null)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Permitted update mode set"));
            }
            break;

        case rocfPN_returnTimeoutPeriod:
            if (this.returnTimeoutPeriod == 0)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                                   EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                   "Return timeout period"));
            }
            break;
        case rocfPN_minReportingCycle:
            if (this.minimumReportingCycle < 1 || this.minimumReportingCycle > 600 )
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                   EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                   "Return Min Reporting Cycle (1..600)"));
            }
            break;
        default:
            break;
        }

        if ((getResult() == SLE_Result.sleRES_negative)
            && (getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics))
        {
            if (this.paramDiagnostic == ROCF_GetParameterDiagnostic.rocfGP_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                                   EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                   "Get parameter diagnostic"));
            }
        }

    }

    public synchronized void VerifyInvocationArguments() throws SleApiException
    {
        verifyInvocationArguments();
        if (this.requestedParam == ROCF_ParameterName.rocfPN_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Requested parameter"));
        }

    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_ROCF_GetParameter ptmp = new EE_ROCF_GetParameter(this);
        return ptmp;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder("");
        printOn(oss, maxDumpLength);

        oss.append("Requested parameter     : " + this.requestedParam + "\n");
        oss.append("Returned parameter      : " + this.returnedParam + "\n");
        oss.append("Delivery mode           : " + this.deliveryMode + "\n");
        oss.append("Latency limit           : " + this.latencyLimit + "\n");
        oss.append("Transfer buffer size    : " + this.transferBufferSize + "\n");
        if (this.gvcid != null)
        {
            oss.append("Global VC type          : " + this.gvcid.getType() + "\n");
            oss.append("Global VC scId          : " + this.gvcid.getScid() + "\n");
            oss.append("Global VC version       : " + this.gvcid.getVersion() + "\n");
            oss.append("Global VC channel ID    : " + this.gvcid.getVcid() + "\n");
        }
        else
        {
            oss.append("Global VC               : \n");
        }
        if (this.gvcidList != null)
        {
            oss.append("Permitted VC set       : \n");
            for (int i = 0; i < this.gvcidList.length; i++)
            {
                if (i > 0)
                {
                    oss.append("\n");
                }
                oss.append("Permitted VC type          : " + this.gvcidList[i].getType() + "\n");
                oss.append("Permitted VC scId          : " + this.gvcidList[i].getScid() + "\n");
                oss.append("Permitted VC version       : " + this.gvcidList[i].getVersion() + "\n");
                oss.append("Permitted VC channel ID    : " + this.gvcidList[i].getVcid() + "\n");
            }
            if (this.gvcidList.length > 1)
            {
                oss.append("\n");
            }
        }
        oss.append("Req. control word type  : " + this.requestedControlWordType + "\n");
        if (this.permittedControlWordTypeList != null)
        {
            oss.append("Perm. control word set  : ");
            for (int i = 0; i < this.permittedControlWordTypeList.length; i++)
            {
                if (i > 0)
                {
                    oss.append(", ");
                }
                oss.append(this.permittedControlWordTypeList[i]);
            }
            oss.append("\n");
        }
        if (this.tcVcidUsed)
        {
            oss.append("Requested TC VCID       : " + this.requestedTcVcid + "\n");
        }
        else
        {
            oss.append("Requested TC VCID       : " + "\n");
        }
        if (this.permittedTcVcidList != null)
        {
            oss.append("Permitted TC VCID set   : ");
            for (int i = 0; i < this.permittedTcVcidList.length; i++)
            {
                if (i > 0)
                {
                    oss.append(", ");
                }
                oss.append(this.permittedTcVcidList[i]);
            }
            oss.append("\n");
        }
        oss.append("Requested update mode   : " + this.requestedUpdateMode + "\n");
        if (this.permittedUpdateModeList != null)
        {
            oss.append("Perm. update mode set  : ");
            for (int i = 0; i < this.permittedUpdateModeList.length; i++)
            {
                if (i > 0)
                {
                    oss.append(", ");
                }
                oss.append(this.permittedUpdateModeList[i]);
            }
            oss.append("\n");
        }
        oss.append("Reporting cycle         : " + this.reportingCycle + "\n");
        oss.append("Return timeout period   : " + this.returnTimeoutPeriod + "\n");
        oss.append("Get parameter diag.     : " + this.paramDiagnostic + "\n");
        oss.append("Minimum reporting cycle : " + this.minimumReportingCycle + "\n");
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
        else if (iid == IROCF_GetParameter.class)
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
        return "EE_ROCF_GetParameter [requestedParam="
               + this.requestedParam
               + ", returnedParam="
               + this.returnedParam
               + ", deliveryMode="
               + this.deliveryMode
               + ", latencyLimit="
               + this.latencyLimit
               + ", transferBufferSize="
               + this.transferBufferSize
               + ", gvcid="
               + ((this.gvcid != null) ? this.gvcid : "")
               + ", gvcidList="
               + ((this.gvcidList != null) ? Arrays.toString(this.gvcidList) : "")
               + ", requestedControlWordType="
               + this.requestedControlWordType
               + ", permittedControlWordTypeList="
               + ((this.permittedControlWordTypeList != null) ? Arrays.toString(this.permittedControlWordTypeList) : "")
               + ", tcVcidUsed=" + this.tcVcidUsed + ", requestedTcVcid=" + this.requestedTcVcid + ", paramDiagnostic="
               + this.paramDiagnostic + ", permittedTcVcidList="
               + ((this.permittedTcVcidList != null) ? Arrays.toString(this.permittedTcVcidList) : "")
               + ", requestedUpdateMode=" + this.requestedUpdateMode + ", permittedUpdateModeList="
               + ((this.permittedUpdateModeList != null) ? Arrays.toString(this.permittedUpdateModeList) : "")
               + ", reportingCycle=" + this.reportingCycle + ", returnTimeoutPeriod=" + this.returnTimeoutPeriod + "]";
    }

}
