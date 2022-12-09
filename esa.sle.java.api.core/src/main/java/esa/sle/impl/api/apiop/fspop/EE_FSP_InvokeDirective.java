/**
 * @(#) EE_FSP_InvokeDirective.java
 */

package esa.sle.impl.api.apiop.fspop;

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
import ccsds.sle.api.isrv.ifsp.IFSP_InvokeDirective;
import ccsds.sle.api.isrv.ifsp.types.FSP_AbsolutePriority;
import ccsds.sle.api.isrv.ifsp.types.FSP_Directive;
import ccsds.sle.api.isrv.ifsp.types.FSP_DirectiveTimeoutType;
import ccsds.sle.api.isrv.ifsp.types.FSP_InvokeDirectiveDiagnostic;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_ConfirmedOperation;
import esa.sle.impl.ifs.gen.EE_LogMsg;

public class EE_FSP_InvokeDirective extends IEE_SLE_ConfirmedOperation implements IFSP_InvokeDirective
{

    private long directiveId = 0;

    private long expectedDirectiveId = 0;

    private FSP_Directive directive = FSP_Directive.fspDV_invalid;

    private long vr = 0;

    private long vs = 0;

    private long fopSlidingWindowWidth = 0;

    private long timerInitial = 0;

    private long transmissionLimit = 0;

    private FSP_DirectiveTimeoutType timeoutType = FSP_DirectiveTimeoutType.fspDTT_invalid;

    private FSP_AbsolutePriority[] priority = null;

    private long[] pollingVector = null;

    private FSP_InvokeDirectiveDiagnostic invokeDirectiveDiagnostic = FSP_InvokeDirectiveDiagnostic.fspID_invalid;


    private EE_FSP_InvokeDirective(final EE_FSP_InvokeDirective right)
    {
        super(right);
        this.directiveId = right.directiveId;
        this.expectedDirectiveId = right.expectedDirectiveId;
        this.directive = right.directive;
        this.vr = right.vr;
        this.vs = right.vs;
        this.fopSlidingWindowWidth = right.fopSlidingWindowWidth;
        this.timerInitial = right.timerInitial;
        this.transmissionLimit = right.transmissionLimit;
        this.timeoutType = right.timeoutType;
        this.invokeDirectiveDiagnostic = right.invokeDirectiveDiagnostic;
        setModifyMapPriorityList(right.priority);
        setModifyMapPollingVector(right.pollingVector);
    }

    public EE_FSP_InvokeDirective(int version)
    {
        this(version, null);
    }

    /**
     * Constructor of the FSP Transfer Data Operation.
     */
    public EE_FSP_InvokeDirective(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt, SLE_OpType.sleOT_invokeDirective, version, preporter);
    }

    @Override
    public synchronized long getDirectiveId()
    {
        return this.directiveId;
    }

    @Override
    public synchronized long getExpectedDirectiveId()
    {
        return this.expectedDirectiveId;
    }

    @Override
    public synchronized FSP_Directive getDirective()
    {
        return this.directive;
    }

    @Override
    public synchronized long getVR()
    {
        return this.vr;
    }

    @Override
    public synchronized long getVS()
    {
        return this.vs;
    }

    @Override
    public synchronized long getFopSlidingWindowWidth()
    {
        return this.fopSlidingWindowWidth;
    }

    @Override
    public synchronized long getTimerInitial()
    {
        return this.timerInitial;
    }

    @Override
    public synchronized long getTransmissionLimit()
    {
        return this.transmissionLimit;
    }

    @Override
    public synchronized FSP_DirectiveTimeoutType getTimeoutType()
    {
        return this.timeoutType;
    }

    @Override
    public synchronized FSP_AbsolutePriority[] getPriority()
    {
        return this.priority;
    }

    @Override
    public synchronized long[] getPollingVector()
    {
        return this.pollingVector;
    }

    @Override
    public synchronized FSP_InvokeDirectiveDiagnostic getInvokeDirectiveDiagnostic()
    {
        return this.invokeDirectiveDiagnostic;
    }

    @Override
    public synchronized void setDirectiveId(long id)
    {
        this.directiveId = id;
    }

    @Override
    public synchronized void setExpectedDirectiveId(long id)
    {
        this.expectedDirectiveId = id;
    }

    @Override
    public synchronized void setInitiateADwithoutCLCW()
    {
        this.directive = FSP_Directive.fspDV_initiateADwithoutCLCW;
    }

    @Override
    public synchronized void setInitiateADwithCLCW()
    {
        this.directive = FSP_Directive.fspDV_initiateADwithCLCW;
    }

    @Override
    public synchronized void setInitiateADwithUnlock()
    {
        this.directive = FSP_Directive.fspDV_initiateADwithUnlock;
    }

    @Override
    public synchronized void setInitiateADwithSetVR(long vr)
    {
        this.directive = FSP_Directive.fspDV_initiateADwithSetVR;
        this.vr = vr;
    }

    @Override
    public synchronized void setTerminateAD()
    {
        this.directive = FSP_Directive.fspDV_terminateAD;
    }

    @Override
    public synchronized void setResumeAD()
    {
        this.directive = FSP_Directive.fspDV_resumeAD;
    }

    @Override
    public synchronized void setVS(long vs)
    {
        this.directive = FSP_Directive.fspDV_setVS;
        this.vs = vs;
    }

    @Override
    public synchronized void setFopSlidingWindow(long width)
    {
        this.directive = FSP_Directive.fspDV_setFopSlidingWindow;
        this.fopSlidingWindowWidth = width;
    }

    @Override
    public synchronized void setTimerInitial(long timer)
    {
        this.directive = FSP_Directive.fspDV_setT1Initial;
        this.timerInitial = timer;
    }

    @Override
    public synchronized void setTransmissionLimit(long limit)
    {
        this.directive = FSP_Directive.fspDV_setTransmissionLimit;
        this.transmissionLimit = limit;
    }

    @Override
    public synchronized void setTimeoutType(FSP_DirectiveTimeoutType type)
    {
        this.directive = FSP_Directive.fspDV_setTimeoutType;
        this.timeoutType = type;
    }

    @Override
    public synchronized void setAbortVC()
    {
        this.directive = FSP_Directive.fspDV_abortVC;
    }

    @Override
    public synchronized void setModifyMapPriorityList(FSP_AbsolutePriority[] plist)
    {
        this.directive = FSP_Directive.fspDV_modifyMapMuxControl;
        if (this.priority != null)
        {
            this.priority = null;
        }
        if (this.pollingVector != null)
        {
            this.pollingVector = null;
        }
        if (plist != null && plist.length > 0)
        {
            this.priority = new FSP_AbsolutePriority[plist.length];
            System.arraycopy(plist, 0, this.priority, 0, plist.length);
        }
    }

    @Override
    public synchronized void setInvokeDirectiveDiagnostic(FSP_InvokeDirectiveDiagnostic diag)
    {
        this.invokeDirectiveDiagnostic = diag;
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

        switch (this.directive)
        {
        case fspDV_invalid:
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Directive"));

        case fspDV_initiateADwithSetVR:
            if (this.vr < 0 || this.vr > 255)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                   EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                   "VR",
                                                   "0..255"));
            }
            break;

        case fspDV_setVS:
            if (this.vs < 0 || this.vs > 255)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                   EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                   "VS",
                                                   "0..255"));
            }
            break;

        case fspDV_setFopSlidingWindow:
            if (this.fopSlidingWindowWidth < 1 || this.fopSlidingWindowWidth > 255)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                   EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                   "FOP sliding window width",
                                                   "1..255"));
            }
            break;

        case fspDV_setT1Initial:
            if (this.timerInitial == 0)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "T1 initial"));
            }
            break;

        case fspDV_setTransmissionLimit:
            if (this.transmissionLimit == 0)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Transmission limit"));
            }
            break;

        case fspDV_setTimeoutType:
            if (this.timeoutType == FSP_DirectiveTimeoutType.fspDTT_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Timeout type"));
            }
            break;

        case fspDV_modifyMapMuxControl:
            if (this.priority != null)
            {
                if (this.priority.length < 1 || this.priority.length > 64)
                {
                    throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                       EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                       "No. of priority list entries",
                                                       "1..64"));
                }

                for (FSP_AbsolutePriority element : this.priority)
                {
                    if (element.getMapOrVc() < 0 || element.getMapOrVc() > 63)
                    {
                        throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                           EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                           "ID in priority list",
                                                           "0..63"));
                    }
                    if (element.getPriority() < 1 || element.getPriority() > 64)
                    {
                        throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                           EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                           "Priority in priority list",
                                                           "1..64"));
                    }
                }
            }
            if (this.pollingVector != null)
            {
                if (this.pollingVector.length < 1 || this.pollingVector.length > 192)
                {
                    throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                       EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                       "No. of polling vector entries",
                                                       "1..192"));
                }
                for (long element : this.pollingVector)
                {
                    if (element < 0 || element > 63)
                    {
                        throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                           EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                           "ID in polling vector",
                                                           "0..63"));
                    }
                }
            }
            break;
        default:
            break;
        }

    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_FSP_InvokeDirective ptmp = new EE_FSP_InvokeDirective(this);
        ISLE_Operation pop = null;
        pop = ptmp.queryInterface(IFSP_InvokeDirective.class);
        return pop;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder();
        printOn(oss, maxDumpLength);
        oss.append("Directive ID           : " + this.directiveId + "\n");
        oss.append("Expected directive ID  : " + this.expectedDirectiveId + "\n");
        oss.append("Directive              : " + this.directive + "\n");
        oss.append("V(R)                   : " + this.vr + "\n");
        oss.append("V(S)                   : " + this.vs + "\n");
        oss.append("FOP sliding window wdth: " + this.fopSlidingWindowWidth + "\n");
        oss.append("T1 initial             : " + this.timerInitial + "\n");
        oss.append("Transmission Limit     : " + this.transmissionLimit + "\n");
        oss.append("Timeout type           : " + this.timeoutType + "\n");
        oss.append("Priority list (map/pri): ");
        if (this.priority != null)
        {
            int i;
            for (i = 0; i < this.priority.length; i++)
            {
                oss.append(this.priority[i].getMapOrVc() + "/" + this.priority[i].getPriority() + " ");
            }
        }
        oss.append("\n");
        oss.append("Polling vector         : ");
        if (this.pollingVector != null)
        {
            int i;
            for (i = 0; i < this.pollingVector.length; i++)
            {
                oss.append(this.pollingVector[i] + " ");
            }
        }
        oss.append("\n");
        oss.append("Diagnostic             : " + this.invokeDirectiveDiagnostic + "\n");
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
            if (this.expectedDirectiveId != this.directiveId + 1)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                                   EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                   "Expected directive id"));
            }
        }
        if (this.invokeDirectiveDiagnostic == FSP_InvokeDirectiveDiagnostic.fspID_invalid)
        {
            if (getResult() == SLE_Result.sleRES_negative
                && getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                                   EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                   "Invoke directive diagnostic"));
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
        else if (iid == IFSP_InvokeDirective.class)
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
    public synchronized void setModifyMapPollingVector(long[] pvec)
    {
        this.directive = FSP_Directive.fspDV_modifyMapMuxControl;
        if (this.priority != null)
        {
            this.priority = null;
        }
        if (this.pollingVector != null)
        {
            this.pollingVector = null;
        }
        if (pvec != null && pvec.length > 0)
        {

            this.pollingVector = new long[pvec.length];

            System.arraycopy(pvec, 0, this.pollingVector, 0, pvec.length);

        }
    }

    @Override
    public synchronized String toString()
    {
        return "EE_FSP_InvokeDirective [directiveId=" + this.directiveId + ", expectedDirectiveId="
               + this.expectedDirectiveId + ", directive=" + this.directive + ", vr=" + this.vr + ", vs=" + this.vs
               + ", fopSlidingWindowWidth=" + this.fopSlidingWindowWidth + ", timerInitial=" + this.timerInitial
               + ", transmissionLimit=" + this.transmissionLimit + ", timeoutType=" + this.timeoutType + ", priority="
               + ((this.priority != null) ? Arrays.toString(this.priority) : "") + ", pollingVector="
               + ((this.pollingVector != null) ? Arrays.toString(this.pollingVector) : "")
               + ", invokeDirectiveDiagnostic=" + this.invokeDirectiveDiagnostic + "]";
    }

}
