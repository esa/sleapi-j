package esa.sle.impl.api.apiop.cltuop;

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
import ccsds.sle.api.isrv.icltu.ICLTU_Start;
import ccsds.sle.api.isrv.icltu.types.CLTU_StartDiagnostic;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_ConfirmedOperation;

/**
 * The class implements the CLTU specific Start operation.
 */
public class EE_CLTU_Start extends IEE_SLE_ConfirmedOperation implements ICLTU_Start
{
    /**
     * Indicates if the first CLTU to be expected is specified.
     */
    private boolean firstCLTUUsed = false;

    /**
     * The first CLTU identification.
     */
    private long firstCLTUID = 0;

    /**
     * The production start time.
     */
    private ISLE_Time startProductionTime;

    /**
     * The production stop time.
     */
    private ISLE_Time stopProductionTime;

    /**
     * The CLTU Start diagnostic.
     */
    private CLTU_StartDiagnostic startDiagnostic;


    /**
     * @FunctionCreator of the CLTU Start Operation.@EndFunction
     */
    public EE_CLTU_Start(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_fwdCltu, SLE_OpType.sleOT_start, version, preporter);
        this.firstCLTUUsed = false;
        this.firstCLTUID = 0;
        this.startProductionTime = null;
        this.stopProductionTime = null;
        this.startDiagnostic = CLTU_StartDiagnostic.cltuSTD_invalid;
    }

    private EE_CLTU_Start(final EE_CLTU_Start right)
    {
        super(right);
        this.firstCLTUUsed = false;
        this.firstCLTUID = 0;
        this.startProductionTime = null;
        this.stopProductionTime = null;
        this.startDiagnostic = CLTU_StartDiagnostic.cltuSTD_invalid;

        if (right.startProductionTime != null)
        {
            this.startProductionTime = right.startProductionTime.copy();
        }
        if (right.stopProductionTime != null)
        {
            this.stopProductionTime = right.stopProductionTime.copy();
        }
        this.startDiagnostic = right.startDiagnostic;
        this.firstCLTUID = right.firstCLTUID;
        this.firstCLTUUsed = right.firstCLTUUsed;
    }

    @Override
    public synchronized boolean getFirstCltuIdUsed()
    {
        return this.firstCLTUUsed;
    }

    @Override
    public synchronized long getFirstCltuId() throws SleApiException
    {
        assert (this.firstCLTUUsed == true) : "error";
        return this.firstCLTUID;
    }

    /**
     * See specification of ICLTU_Start.
     */
    @Override
    public synchronized final ISLE_Time getStartProductionTime()
    {
        return this.startProductionTime;
    }

    /**
     * See specification of ICLTU_Start.
     */
    @Override
    public synchronized final ISLE_Time getStopProductionTime()
    {
        return this.stopProductionTime;

    }

    /**
     * See specification of ICLTU_Start.
     * 
     * @throws SleApiException
     */
    @Override
    public synchronized CLTU_StartDiagnostic getStartDiagnostic() throws SleApiException
    {
        assert (getResult() == SLE_Result.sleRES_negative && getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics) : "error with getxxx function";
        return this.startDiagnostic;
    }

    /**
     * See specification of ICLTU_Start.
     */
    @Override
    public synchronized void setFirstCltuId(long id)
    {
        this.firstCLTUID = id;
        this.firstCLTUUsed = true;
    }

    /**
     * @FunctionSee specification of ICLTU_Start.@EndFunction
     */
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
    public synchronized void setStartDiagnostic(CLTU_StartDiagnostic diag)
    {
        this.startDiagnostic = diag;
        setSpecificDiagnostics();
    }

    /**
     * ///////////////////////////////////////////////////////
     * 
     * @throws SleApiException
     * @FunctionSee specification of ISLE_Operation.@EndFunction
     *              ///////////////////////////////////////////////////////
     */
    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        super.verifyInvocationArguments();
        if (getOpVersionNumber() == 2 && !this.firstCLTUUsed)
        {
            throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
        }
    }

    @Override
    public synchronized final ISLE_Operation copy()
    {
        EE_CLTU_Start ptmp = new EE_CLTU_Start(this);
        return ptmp;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder("");
        printOn(oss, maxDumpLength);
        oss.append("First CLTU used        : " + this.firstCLTUUsed + "\n");
        oss.append("First CLTUID           : " + this.firstCLTUID + "\n");
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
        oss.append("Start diagnostic       : "
                   + CLTU_StartDiagnostic.getStartDiagnosticByCode(this.startDiagnostic.getCode()) + " \n");
        oss.append("\n");

        String ret = new String(oss.toString());
        return ret;
    }

    /**
     * @throws SleApiException See specification of ISLE_ConfirmedOperation.
     */
    @Override
    public synchronized void verifyReturnArguments() throws SleApiException
    {
        super.verifyReturnArguments();

        if (this.startProductionTime == null && getResult() == SLE_Result.sleRES_positive)
        {
            throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
        }

        if (this.startProductionTime != null && this.stopProductionTime != null)
        {
            if (!(this.startProductionTime.compareTo(this.stopProductionTime) < 0))
            {
                throw new SleApiException(HRESULT.SLE_E_TIMERANGE);
            }
        }
        if (this.startDiagnostic == CLTU_StartDiagnostic.cltuSTD_invalid)
        {
            if (getResult() == SLE_Result.sleRES_negative
                && getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
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
        else if (iid == ISLE_Operation.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_ConfirmedOperation.class)
        {
            return (T) this;
        }
        else if (iid == ICLTU_Start.class)
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
        return "EE_CLTU_Start [firstCLTUUsed=" + this.firstCLTUUsed + ", firstCLTUID=" + this.firstCLTUID
               + ", startProductionTime=" + ((this.startProductionTime != null) ? this.startProductionTime : "")
               + ", stopProductionTime=" + ((this.stopProductionTime != null) ? this.stopProductionTime : "")
               + ", startDiagnostic=" + this.startDiagnostic + "]";
    }

}
