package esa.sle.impl.api.apiop.rafop;

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
import ccsds.sle.api.isrv.iraf.IRAF_Start;
import ccsds.sle.api.isrv.iraf.types.RAF_RequestedFrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_StartDiagnostic;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_ConfirmedOperation;

/**
 * The class implements the RAF specific Start operation.
 */
public class EE_RAF_Start extends IEE_SLE_ConfirmedOperation implements IRAF_Start
{
    /**
     * The time of the first frame to be delivered.
     */
    private ISLE_Time firstFrameDelivered = null;

    /**
     * The time of the last frame to be delivered.
     */
    private ISLE_Time lastFrameDelivered = null;

    /**
     * The RAF Start diagnostic.
     */
    private RAF_StartDiagnostic diagnostic = RAF_StartDiagnostic.rafSD_invalid;

    /**
     * The RAF Start requested frame quality.
     */
    private RAF_RequestedFrameQuality reqFrameQual = RAF_RequestedFrameQuality.rafRQ_invalid;


    protected EE_RAF_Start(final EE_RAF_Start right)
    {
        super(right);
        if (right.firstFrameDelivered != null)
        {
            this.firstFrameDelivered = right.firstFrameDelivered.copy();
        }
        if (right.lastFrameDelivered != null)
        {
            this.lastFrameDelivered = right.lastFrameDelivered.copy();
        }
        this.diagnostic = right.diagnostic;
        this.reqFrameQual = right.reqFrameQual;

    }

    public EE_RAF_Start(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnAllFrames, SLE_OpType.sleOT_start, version, preporter);
        this.firstFrameDelivered = null;
        this.lastFrameDelivered = null;
        this.diagnostic = RAF_StartDiagnostic.rafSD_invalid;
        this.reqFrameQual = RAF_RequestedFrameQuality.rafRQ_invalid;
    }

    @Override
    public synchronized ISLE_Time getStartTime()
    {
        if (this.firstFrameDelivered != null)
        {
            return this.firstFrameDelivered;
        }
        return null;
    }

    @Override
    public synchronized ISLE_Time getStopTime()
    {
        if (this.lastFrameDelivered != null)
        {
            return this.lastFrameDelivered;
        }
        return null;
    }

    @Override
    public synchronized RAF_RequestedFrameQuality getRequestedFrameQuality()
    {
        return this.reqFrameQual;
    }

    @Override
    public synchronized RAF_StartDiagnostic getStartDiagnostic()
    {
        assert ((getResult() == SLE_Result.sleRES_negative) && (getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)) : "Error, Result not negative, and diagnostic type not specific";
        return this.diagnostic;
    }

    @Override
    public synchronized void setStartTime(final ISLE_Time time)
    {
        this.firstFrameDelivered = time.copy();
    }

    @Override
    public synchronized void putStartTime(ISLE_Time ptime)
    {
        this.firstFrameDelivered = ptime;
    }

    @Override
    public synchronized void setStopTime(ISLE_Time time)
    {
        this.lastFrameDelivered = time.copy();
    }

    @Override
    public synchronized void putStopTime(ISLE_Time ptime)
    {
        this.lastFrameDelivered = ptime;
    }

    @Override
    public synchronized void setRequestedFrameQuality(RAF_RequestedFrameQuality quality)
    {
        this.reqFrameQual = quality;
    }

    @Override
    public synchronized void setStartDiagnostic(RAF_StartDiagnostic diagnostic)
    {
        this.diagnostic = diagnostic;
        setSpecificDiagnostics();
    }

    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {

        super.verifyInvocationArguments();
        if (this.firstFrameDelivered != null && this.lastFrameDelivered != null)
        {
            if (!(this.firstFrameDelivered.compareTo(this.lastFrameDelivered) < 0))
            {
                throw new SleApiException(HRESULT.SLE_E_TIMERANGE);
            }
        }
        if (this.reqFrameQual == RAF_RequestedFrameQuality.rafRQ_invalid)
        {
            throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
        }

    }

    @Override
    public synchronized void verifyReturnArguments() throws SleApiException
    {
        super.verifyReturnArguments();
        if ((getResult() == SLE_Result.sleRES_negative) && (this.diagnostic == RAF_StartDiagnostic.rafSD_invalid)
            && (getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics))
        {
            throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
        }
    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_RAF_Start pstart = new EE_RAF_Start(this);
        return pstart;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder os = new StringBuilder();
        printOn(os, maxDumpLength);
        String firstDelivered = null;
        String lastDelivered = null;
        if (this.firstFrameDelivered != null)
        {
            firstDelivered = this.firstFrameDelivered.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth,
                                                                     SLE_TimeRes.sleTR_microSec);
        }
        if (this.lastFrameDelivered != null)
        {
            lastDelivered = this.lastFrameDelivered.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth,
                                                                   SLE_TimeRes.sleTR_microSec);
        }

        if (firstDelivered != null)
        {
            os.append("First Frame Delivered : " + firstDelivered + "\n");
        }
        else
        {
            os.append("First Frame Delivered : \n");
        }
        if (lastDelivered != null)
        {
            os.append("Last Frame Delivered   : " + lastDelivered + "\n");
        }
        else
        {
            os.append("Last Frame Delivered   : \n");
        }
        os.append("Start Diagnostic       : " + this.diagnostic + "\n");
        os.append("Requested Frame Quality: " + this.reqFrameQual + "\n");

        String ret = os.toString();
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
        else if (iid == IRAF_Start.class)
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
        return "EE_RAF_Start [firstFrameDelivered="
               + ((this.firstFrameDelivered != null) ? this.firstFrameDelivered : "") + ", lastFrameDelivered="
               + ((this.lastFrameDelivered != null) ? this.lastFrameDelivered : "") + ", diagnostic=" + this.diagnostic
               + ", reqFrameQual=" + this.reqFrameQual + "]";
    }

}
