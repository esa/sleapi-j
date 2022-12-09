/**
 * @(#) EE_ROCF_Start.java
 */

package esa.sle.impl.api.apiop.rocfop;

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
import ccsds.sle.api.isle.it.SLE_TimeFmt;
import ccsds.sle.api.isle.it.SLE_TimeRes;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.irocf.IROCF_Start;
import ccsds.sle.api.isrv.irocf.types.ROCF_ChannelType;
import ccsds.sle.api.isrv.irocf.types.ROCF_ControlWordType;
import ccsds.sle.api.isrv.irocf.types.ROCF_Gvcid;
import ccsds.sle.api.isrv.irocf.types.ROCF_StartDiagnostic;
import ccsds.sle.api.isrv.irocf.types.ROCF_UpdateMode;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_ConfirmedOperation;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * @NameROCF Start Operation@EndName
 * @ResponsibilityThe class implements the ROCF specific Start operation.@EndResponsibility
 */
public class EE_ROCF_Start extends IEE_SLE_ConfirmedOperation implements IROCF_Start
{
    /**
     * The start diagnostic.
     */
    private ROCF_StartDiagnostic startDiagnostic = ROCF_StartDiagnostic.rocfSD_invalid;

    /**
     * The time of the first frame to be delivered.
     */
    private ISLE_Time startTime = null;

    /**
     * The time of the last frame to be delivered.
     */
    private ISLE_Time stopTime = null;

    /**
     * The global VC identifier.
     */
    private ROCF_Gvcid gvcid = null;

    /**
     * The control word type.
     */
    private ROCF_ControlWordType controlWordType = ROCF_ControlWordType.rocfCWT_invalid;

    private boolean tcVcidUsed = false;

    private long tcVcid;

    private ROCF_UpdateMode updateMode = ROCF_UpdateMode.rocfUM_invalid;


    private EE_ROCF_Start(final EE_ROCF_Start right)
    {
        super(right);
        if (right.startTime != null)
        {
            this.startTime = right.startTime.copy();
        }
        if (right.stopTime != null)
        {
            this.stopTime = right.stopTime.copy();
        }
        if (right.gvcid != null)
        {
            this.gvcid = right.gvcid;
        }
        this.startDiagnostic = right.startDiagnostic;
        this.controlWordType = right.controlWordType;
        this.tcVcidUsed = right.tcVcidUsed;
        this.tcVcid = right.tcVcid;
    }

    public EE_ROCF_Start(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnChOcf, SLE_OpType.sleOT_start, version, preporter);
        this.startDiagnostic = ROCF_StartDiagnostic.rocfSD_invalid;
        this.startTime = null;
        this.stopTime = null;
        this.gvcid = null;
        this.controlWordType = ROCF_ControlWordType.rocfCWT_invalid;
        this.tcVcidUsed = false;
        this.updateMode = ROCF_UpdateMode.rocfUM_invalid;
    }

    @Override
    public synchronized ISLE_Time getStartTime()
    {
        return this.startTime;
    }

    @Override
    public synchronized ISLE_Time getStopTime()
    {
        return this.stopTime;
    }

    @Override
    public synchronized ROCF_Gvcid getGvcid()
    {
        return this.gvcid;
    }

    @Override
    public synchronized ROCF_ControlWordType getControlWordType()
    {
        return this.controlWordType;
    }

    @Override
    public synchronized boolean getTcVcidUsed()
    {
        return this.tcVcidUsed;
    }

    @Override
    public synchronized long getTcVcid()
    {
        assert (getTcVcidUsed() == true) : "error";
        return this.tcVcid;
    }

    @Override
    public synchronized ROCF_UpdateMode getUpdateMode()
    {
        return this.updateMode;
    }

    @Override
    public synchronized ROCF_StartDiagnostic getStartDiagnostic()
    {
        assert (getResult() == SLE_Result.sleRES_negative && getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics) : "error";
        return this.startDiagnostic;
    }

    @Override
    public synchronized void setStartTime(ISLE_Time time)
    {
        if (this.startTime != null)
        {
            this.startTime = null;
        }
        this.startTime = time.copy();
    }

    @Override
    public synchronized void putStartTime(ISLE_Time ptime)
    {
        if (this.startTime != null)
        {
            this.startTime = null;
        }
        this.startTime = ptime;
    }

    @Override
    public synchronized void setStopTime(ISLE_Time time)
    {
        if (this.stopTime != null)
        {
            this.stopTime = null;
        }
        this.stopTime = time.copy();
    }

    @Override
    public synchronized void putStopTime(ISLE_Time ptime)
    {
        if (this.stopTime != null)
        {
            this.stopTime = null;
        }
        this.stopTime = ptime;
    }

    @Override
    public synchronized void setGvcid(ROCF_Gvcid id)
    {
        if (this.gvcid != null)
        {
            this.gvcid = null;
        }
        this.gvcid = id;
    }

    @Override
    public synchronized void putGvcid(ROCF_Gvcid pid)
    {
        this.gvcid = null;
        this.gvcid = pid;
    }

    @Override
    public synchronized void setControlWordType(ROCF_ControlWordType type)
    {
        this.controlWordType = type;
    }

    @Override
    public synchronized void setTcVcid(long id)
    {
        this.tcVcid = id;
        this.tcVcidUsed = true;
    }

    @Override
    public synchronized void setUpdateMode(ROCF_UpdateMode mode)
    {
        this.updateMode = mode;
    }

    @Override
    public synchronized void setStartDiagnostic(ROCF_StartDiagnostic diagnostic)
    {
        this.startDiagnostic = diagnostic;
        setSpecificDiagnostics();
    }

    @Override
    public synchronized void verifyReturnArguments() throws SleApiException
    {
        super.verifyReturnArguments();
        if (getResult() == SLE_Result.sleRES_negative)
        {
            if ((getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
                && (this.startDiagnostic == ROCF_StartDiagnostic.rocfSD_invalid))
            {
                HRESULT code = logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                        EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                        "Start diagnostic");
                throw new SleApiException(code);
            }
        }
    }

    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        super.verifyInvocationArguments();

        if (this.startTime != null && this.stopTime != null)
        {
            if (!(this.startTime.compareTo(this.stopTime) < 0))
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_TIMERANGE, EE_LogMsg.EE_OP_LM_TimeRange.getCode()));
            }
        }
        if (this.gvcid == null)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Channel ID"));
        }
        if (this.gvcid.getType() == ROCF_ChannelType.rocfCT_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_INVALIDID,
                                               EE_LogMsg.EE_OP_LM_InvalidID.getCode(),
                                               "Channel ID"));
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
                                                       "Virtual channel ID",
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
                                                       "Virtual channel ID",
                                                       "0..63"));
                }
            }
        }
        else
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_INVALIDID,
                                               EE_LogMsg.EE_OP_LM_InvalidID.getCode(),
                                               "Channel ID"));
        }

        if (this.controlWordType == ROCF_ControlWordType.rocfCWT_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Control word type"));
        }
        if (this.tcVcidUsed)
        {
            if (this.controlWordType == ROCF_ControlWordType.rocfCWT_clcw)
            {
                if ((this.tcVcid < 0) || (this.tcVcid > 63))
                {
                    throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                       EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                       "TC channel ID",
                                                       "0..63"));
                }
            }
            else
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_INVALIDID,
                                                   EE_LogMsg.EE_OP_LM_InvalidMode.getCode(),
                                                   "Control word type",
                                                   "not CLCW"));
            }
        }
        if (this.updateMode == ROCF_UpdateMode.rocfUM_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Update mode"));
        }

    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_ROCF_Start ptmp = new EE_ROCF_Start(this);
        return ptmp;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder("");
        printOn(oss, maxDumpLength);

        oss.append("Start time             : ");
        if (this.startTime != null)
        {
            String start = this.startTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_microSec);
            oss.append(start);
        }
        oss.append("\n Stop time              : \n");
        if (this.stopTime != null)
        {
            String stop = this.stopTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_microSec);
            oss.append(stop);
        }
        oss.append("\n Start diagnostic       : " + this.startDiagnostic + "\n");
        if (this.gvcid != null)
        {
            oss.append("Type                   : " + this.gvcid.getType() + "\n");
            oss.append("Spacecraft identifier  : " + this.gvcid.getScid() + "\n");
            oss.append("Version number         : " + this.gvcid.getVersion() + "\n");
            oss.append("Virtual channel id     : " + this.gvcid.getVcid() + "\n");
        }
        else
        {
            oss.append("Virtual channel id     : \n");
        }
        oss.append("Control word           : " + this.controlWordType + "\n");
        if (this.tcVcidUsed)
        {
            oss.append("TC virtual channel id  : " + this.tcVcid + "\n");
        }
        else
        {
            oss.append("TC virtual channel id  : \n");
        }
        oss.append("Update mode            : " + this.updateMode + "\n");
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
        else if (iid == IROCF_Start.class)
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
        else
        {
            return null;
        }

    }

    @Override
    public synchronized String toString()
    {
        return "EE_ROCF_Start [startDiagnostic=" + this.startDiagnostic + ", startTime="
               + ((this.startTime != null) ? this.startTime : "") + ", stopTime="
               + ((this.stopTime != null) ? this.stopTime : "") + ", gvcid=" + ((this.gvcid != null) ? this.gvcid : "")
               + ", controlWordType=" + this.controlWordType + ", tcVcidUsed=" + this.tcVcidUsed + ", tcVcid="
               + this.tcVcid + ", updateMode=" + this.updateMode + "]";
    }

}
