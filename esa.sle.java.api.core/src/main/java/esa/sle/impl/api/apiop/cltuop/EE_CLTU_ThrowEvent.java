/**
 * @(#) EE_CLTU_ThrowEvent.java
 */

package esa.sle.impl.api.apiop.cltuop;

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
import ccsds.sle.api.isrv.icltu.ICLTU_ThrowEvent;
import ccsds.sle.api.isrv.icltu.types.CLTU_ThrowEventDiagnostic;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_ConfirmedOperation;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * @NameCLTU ThrowEvent Operation@EndName
 * @ResponsibilityThe class implements the CLTU specific ThrowEvent operation.@EndResponsibility
 */
public class EE_CLTU_ThrowEvent extends IEE_SLE_ConfirmedOperation implements ICLTU_ThrowEvent
{
    /**
     * The identification of the event.
     */
    private int eventID = 0;

    /**
     * The invocation identifier of the event.
     */
    private long eventInvocationID = 0;

    /**
     * The next expected invocation identifier of the event.
     */
    private long expectedEventInvocationID = 0;

    /**
     * The CLTU Throw Event diagnostic.
     */
    private CLTU_ThrowEventDiagnostic throwEventDiagnostic = CLTU_ThrowEventDiagnostic.cltuTED_invalid;

    private byte[] eventQualifier;

    private long eventQualifierLength = 0;


    private EE_CLTU_ThrowEvent(EE_CLTU_ThrowEvent right)
    {
        super(right);
        this.eventID = right.eventID;
        this.eventInvocationID = right.eventInvocationID;
        this.expectedEventInvocationID = right.expectedEventInvocationID;
        this.throwEventDiagnostic = right.throwEventDiagnostic;

        setEventQualifier(right.eventQualifier);
    }

    public EE_CLTU_ThrowEvent(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_fwdCltu, SLE_OpType.sleOT_throwEvent, version, preporter);
        this.eventID = 0;
        this.eventInvocationID = 0;
        this.expectedEventInvocationID = 0;
        this.throwEventDiagnostic = CLTU_ThrowEventDiagnostic.cltuTED_invalid;
        this.eventQualifier = null;
        this.eventQualifierLength = 0;
    }

    @Override
    public synchronized int getEventId()
    {
        return this.eventID;
    }

    @Override
    public synchronized long getEventInvocationId()
    {
        return this.eventInvocationID;
    }

    @Override
    public synchronized long getExpectedEventInvocationId()
    {
        return this.expectedEventInvocationID;
    }

    @Override
    public synchronized byte[] getEventQualifier()
    {
        return this.eventQualifier;
    }

    @Override
    public synchronized CLTU_ThrowEventDiagnostic getThrowEventDiagnostic()
    {
        assert ((getResult() == SLE_Result.sleRES_negative) && (getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)) : "error";
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
        this.eventInvocationID = id;
    }

    @Override
    public synchronized void setExpectedEventInvocationId(long id)
    {
        this.expectedEventInvocationID = id;
    }

    @Override
    public synchronized void setEventQualifier(byte[] pdata)
    {

        if (this.eventQualifier != null)
        {
            this.eventQualifier = null;
        }
        this.eventQualifier = null;
        this.eventQualifierLength = 0;

        if (pdata.length > 0 && pdata != null)
        {
            this.eventQualifierLength = pdata.length;
            this.eventQualifier = new byte[pdata.length];
            for (int i = 0; i < pdata.length; i++)
            {
                this.eventQualifier[i] = pdata[i];
            }

        }

    }

    @Override
    public synchronized void setThrowEventDiagnostic(CLTU_ThrowEventDiagnostic diagnostic)
    {
        this.throwEventDiagnostic = diagnostic;
        setSpecificDiagnostics();
    }

    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {

        super.verifyInvocationArguments();

        if (this.eventQualifier == null)
        {
            HRESULT code = logAlarm(HRESULT.SLE_E_MISSINGARG,
                                    EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                    "Event Qualifier",
                                    "");
            throw new SleApiException(code);
        }
        if ((this.eventQualifier != null) && (this.eventQualifierLength < 1 || this.eventQualifierLength > 128))
        {
            HRESULT code = logAlarm(HRESULT.SLE_E_RANGE,
                                    EE_LogMsg.EE_OP_LM_Range.getCode(),
                                    "Event Qualifier",
                                    "1..128");
            throw new SleApiException(code);
        }
    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_CLTU_ThrowEvent ptmp = new EE_CLTU_ThrowEvent(this);
        return ptmp;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder();
        printOn(oss, maxDumpLength);
        // oss.append(maxDumpLength);
        oss.append("Event identifier       : " + this.eventID + "\n");
        oss.append("Event invocation ID    : " + this.eventInvocationID + "\n");
        oss.append("Expected evt invoke id : " + this.expectedEventInvocationID + "\n");
        oss.append("Throw Event Diagnostic : " + this.throwEventDiagnostic + "\n");

        oss.append("Event Qualifier        : ");
        if (this.eventQualifier != null)
        {
            long inlen = (maxDumpLength < this.eventQualifier.length) ? maxDumpLength : this.eventQualifier.length;
            String str = EE_GenStrUtil.convAscii(this.eventQualifier, inlen);
            oss.append(str);
        }
        oss.append("\n");
        String ret = oss.toString();
        return ret;

    }

    public synchronized void VerifyReturnArguments() throws SleApiException
    {
        verifyReturnArguments();
        if (getResult() == SLE_Result.sleRES_positive)
        {
            if (this.expectedEventInvocationID != this.eventInvocationID + 1)
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
        else if (iid == ICLTU_ThrowEvent.class)
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
        return "EE_CLTU_ThrowEvent [eventID=" + this.eventID + ", eventInvocationID=" + this.eventInvocationID
               + ", expectedEventInvocationID=" + this.expectedEventInvocationID + ", throwEventDiagnostic="
               + this.throwEventDiagnostic + ", eventQualifier=" + Arrays.toString(this.eventQualifier)
               + ", eventQualifierLength=" + this.eventQualifierLength + "]";
    }

}
