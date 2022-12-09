package esa.sle.impl.api.apiop.fspop;

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
import ccsds.sle.api.isrv.ifsp.IFSP_Start;
import ccsds.sle.api.isrv.ifsp.types.FSP_StartDiagnostic;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_ConfirmedOperation;
import esa.sle.impl.ifs.gen.EE_LogMsg;

public class EE_FSP_Start extends IEE_SLE_ConfirmedOperation implements IFSP_Start
{
    /**
     * The first FSP packet identification.
     */
    private long firstPacketId = 0;

    /**
     * The production start time.
     */
    private ISLE_Time startProductionTime = null;

    /**
     * The production stop time.
     */
    private ISLE_Time stopProductionTime = null;

    /**
     * The FSP Start diagnostic.
     */
    private FSP_StartDiagnostic startDiagnostic = FSP_StartDiagnostic.fspSTD_invalid;


    private EE_FSP_Start(final EE_FSP_Start right)
    {
        super(right);
        this.firstPacketId = 0;
        this.startProductionTime = null;
        this.stopProductionTime = null;
        this.startDiagnostic = FSP_StartDiagnostic.fspSTD_invalid;

        if (right.startProductionTime != null)
        {
            this.startProductionTime = right.startProductionTime.copy();
        }
        if (right.stopProductionTime != null)
        {
            this.stopProductionTime = right.stopProductionTime.copy();
        }
        this.startDiagnostic = right.startDiagnostic;
        this.firstPacketId = right.firstPacketId;
    }

    public EE_FSP_Start(int version)
    {
        this(version, null);
    }

    /**
     * Constructor of the FSP Start Operation.
     */
    public EE_FSP_Start(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt, SLE_OpType.sleOT_start, version, preporter);
        this.firstPacketId = 0;
        this.startProductionTime = null;
        this.stopProductionTime = null;
        this.startDiagnostic = FSP_StartDiagnostic.fspSTD_invalid;

    }

    @Override
    public synchronized long getFirstPacketId()
    {
        return this.firstPacketId;
    }

    @Override
    public synchronized ISLE_Time getStartProductionTime()
    {
        return this.startProductionTime;
    }

    @Override
    public synchronized ISLE_Time getStopProductionTime()
    {
        return this.stopProductionTime;
    }

    @Override
    public synchronized FSP_StartDiagnostic getStartDiagnostic()
    {
        return this.startDiagnostic;
    }

    @Override
    public synchronized void setFirstPacketId(long id)
    {
        this.firstPacketId = id;
    }

    @Override
    public synchronized void setStartProductionTime(ISLE_Time startTime)
    {
        this.startProductionTime = startTime.copy();
    }

    @Override
    public synchronized void putStartProductionTime(ISLE_Time pstartTime)
    {
        this.startProductionTime = pstartTime;
    }

    @Override
    public synchronized void setStopProductionTime(ISLE_Time stopTime)
    {
        this.stopProductionTime = stopTime.copy();
    }

    @Override
    public synchronized void putStopProductionTime(ISLE_Time pstopTime)
    {
        this.stopProductionTime = pstopTime;
    }

    @Override
    public synchronized void setStartDiagnostic(FSP_StartDiagnostic diag)
    {
        this.startDiagnostic = diag;
        setSpecificDiagnostics();
    }

    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        HRESULT baseres = HRESULT.S_OK;
        try
        {
            super.verifyInvocationArguments();
        }
        catch (SleApiException e)
        {
            baseres = e.getHResult();
        }
        if (baseres != HRESULT.S_OK)
        {
            throw new SleApiException(baseres);
        }
    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_FSP_Start ptmp = new EE_FSP_Start(this);
        ISLE_Operation pop = null;

        pop = ptmp.queryInterface(IFSP_Start.class);

        if (pop != null)
        {
            return pop;
        }
        return pop;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder();
        printOn(oss, maxDumpLength);
        oss.append("First packet id        : " + this.firstPacketId + "\n");
        if (this.startProductionTime != null)
        {
            String str = this.startProductionTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth,
                                                                 SLE_TimeRes.sleTR_microSec);
            oss.append("Start production time  : " + str + "\n");
        }
        else
        {
            oss.append("Start production time  : \n");
        }
        if (this.stopProductionTime != null)
        {
            String str = this.stopProductionTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth,
                                                                SLE_TimeRes.sleTR_microSec);
            oss.append("Stop production time  : " + str + "\n");
        }
        else
        {
            oss.append("Stop production time  : \n");
        }
        oss.append("Start diagnostic       : " + this.startDiagnostic + "\n");
        oss.append("\n");
        String ret = oss.toString();
        return ret;
    }

    @Override
    public synchronized void verifyReturnArguments() throws SleApiException
    {
        HRESULT baseres = HRESULT.S_OK;
        try
        {
            super.verifyReturnArguments();
        }
        catch (SleApiException e)
        {
            baseres = e.getHResult();
        }
        if (baseres != HRESULT.S_OK)
        {
            throw new SleApiException(baseres);
        }

        if (getResult() == SLE_Result.sleRES_positive)
        {
            if (this.startProductionTime == null)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Start production time"));
            }
        }
        else
        {
            if (this.startProductionTime != null)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                                   EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                   "Start production time with negative result"));
            }
        }

        if (this.startProductionTime != null && this.stopProductionTime != null)
        {
            if (!(this.startProductionTime.compareTo(this.stopProductionTime) < 1))
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_TIMERANGE, EE_LogMsg.EE_OP_LM_TimeRange.getCode()));
            }
        }
        if (this.startDiagnostic == FSP_StartDiagnostic.fspSTD_invalid)
        {
            if (getResult() == SLE_Result.sleRES_negative
                && getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                                   EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                   "Start diagnostic"));
            }
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == IFSP_Start.class)
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
        return "EE_FSP_Start [firstPacketId=" + this.firstPacketId + ", startProductionTime="
               + ((this.startProductionTime != null) ? this.startProductionTime : "") + ", stopProductionTime="
               + ((this.stopProductionTime != null) ? this.stopProductionTime : "") + ", startDiagnostic="
               + this.startDiagnostic + "]";
    }

}
