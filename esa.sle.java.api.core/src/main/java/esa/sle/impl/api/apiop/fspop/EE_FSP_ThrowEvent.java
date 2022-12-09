/**
 * @(#) EE_FSP_ThrowEvent.java
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
import ccsds.sle.api.isrv.ifsp.IFSP_ThrowEvent;
import ccsds.sle.api.isrv.ifsp.types.FSP_ThrowEventDiagnostic;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_ConfirmedOperation;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_LogMsg;

public class EE_FSP_ThrowEvent extends IEE_SLE_ConfirmedOperation implements IFSP_ThrowEvent
{
    /**
     * The identification of the event.
     */
    private int eventID = 0;

    private byte[] eventQualifier = null;

    private long eventInvocationId = 0;

    private long expectedEventInvocationId = 0;

    private FSP_ThrowEventDiagnostic throwEventDiagnostic = FSP_ThrowEventDiagnostic.fspTED_invalid;


    private EE_FSP_ThrowEvent(final EE_FSP_ThrowEvent right)
    {
        super(right);
        this.eventID = right.eventID;
        this.eventInvocationId = right.eventInvocationId;
        this.expectedEventInvocationId = right.expectedEventInvocationId;
        this.throwEventDiagnostic = right.throwEventDiagnostic;
        setEventQualifier(right.eventQualifier);
    }

    public EE_FSP_ThrowEvent(int version)
    {
        this(version, null);
    }

    /**
     * See specification of IFSP_GetParameter.
     */
    public EE_FSP_ThrowEvent(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt, SLE_OpType.sleOT_throwEvent, version, preporter);
    }

    @Override
    public synchronized int getEventId()
    {
        return this.eventID;
    }

    @Override
    public synchronized long getEventInvocationId()
    {
        return this.eventInvocationId;
    }

    @Override
    public synchronized long getExpectedEventInvocationId()
    {
        return this.expectedEventInvocationId;
    }

    @Override
    public synchronized byte[] getEventQualifier()
    {
        return this.eventQualifier;
    }

    @Override
    public synchronized FSP_ThrowEventDiagnostic getThrowEventDiagnostic()
    {
        return this.throwEventDiagnostic;
    }

    @Override
    public synchronized void setEventId(int id)
    {
        this.eventID = id;
    }

    @Override
    public synchronized void setEventInvocationId(long id)
    {
        this.eventInvocationId = id;
    }

    @Override
    public synchronized void setExpectedEventInvocationId(long id)
    {
        this.expectedEventInvocationId = id;
    }

    @Override
    public synchronized void setEventQualifier(byte[] parg)
    {
        this.eventQualifier = new byte[parg.length];
        System.arraycopy(parg, 0, this.eventQualifier, 0, parg.length);
    }

    @Override
    public synchronized void setThrowEventDiagnostic(FSP_ThrowEventDiagnostic diagnostic)
    {
        this.throwEventDiagnostic = diagnostic;
        setSpecificDiagnostics();
    }

    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        HRESULT rc = HRESULT.S_OK;
        try
        {
            super.verifyInvocationArguments();
        }
        catch (SleApiException e)
        {
            rc = e.getHResult();
        }
        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }

        if (this.eventQualifier == null)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Event Qualifier"));
        }

        if (this.eventQualifier.length < 1 || this.eventQualifier.length > 128)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                               EE_LogMsg.EE_OP_LM_Range.getCode(),
                                               "Event Qualifier",
                                               "1..128"));
        }

    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_FSP_ThrowEvent ptmp = new EE_FSP_ThrowEvent(this);
        ISLE_Operation pop = null;
        pop = ptmp.queryInterface(IFSP_ThrowEvent.class);
        return pop;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder();
        printOn(oss, maxDumpLength);
        oss.append("Event identifier       : " + this.eventID + "\n");
        oss.append("Event invocation ID    : " + this.eventInvocationId + "\n");
        oss.append("Expected evt invoke id : " + this.expectedEventInvocationId + "\n");
        boolean event = false;
        if (this.eventQualifier != null)
        {
            event = true;
            long inlen = this.eventQualifier.length;
            if (maxDumpLength < inlen)
            {
                inlen = maxDumpLength;
            }
            String str = EE_GenStrUtil.convAscii(this.eventQualifier, inlen);
            oss.append("Event Qualifier        : " + str + "\n");
        }
        else
        {
            oss.append("Event Qualifier        : \n");
        }
        oss.append("Event Qualifier Length : " + (!event ? 0 : this.eventQualifier.length) + "\n");
        oss.append("Throw Event Diagnostic : " + this.throwEventDiagnostic + "\n");
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

        if (getResult() == SLE_Result.sleRES_negative)
        {
            if (getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                if (this.throwEventDiagnostic == FSP_ThrowEventDiagnostic.fspTED_invalid)
                {
                    throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                       EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                       "Throw event diagnostic"));
                }
            }
        }
        else if (getResult() == SLE_Result.sleRES_positive)
        {
            if (this.expectedEventInvocationId != this.eventInvocationId + 1)
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
        else if (iid == IFSP_ThrowEvent.class)
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
        return "EE_FSP_ThrowEvent [eventID=" + this.eventID + ", eventQualifier="
               + ((this.eventQualifier != null) ? Arrays.toString(this.eventQualifier) : "") + ", eventInvocationId="
               + this.eventInvocationId + ", expectedEventInvocationId=" + this.expectedEventInvocationId
               + ", throwEventDiagnostic=" + this.throwEventDiagnostic + "]";
    }

}
