/**
 * @(#) EE_RCF_Start.java
 */

package esa.sle.impl.api.apiop.rcfop;

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
import ccsds.sle.api.isrv.ircf.IRCF_Start;
import ccsds.sle.api.isrv.ircf.types.RCF_ChannelType;
import ccsds.sle.api.isrv.ircf.types.RCF_Gvcid;
import ccsds.sle.api.isrv.ircf.types.RCF_StartDiagnostic;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_ConfirmedOperation;

/**
 * The class implements the RCF specific Start operation
 */
public class EE_RCF_Start extends IEE_SLE_ConfirmedOperation implements IRCF_Start
{
    /**
     * The start diagnostic.
     */
    private RCF_StartDiagnostic startDiagnostic = RCF_StartDiagnostic.rcfSD_invalid;

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
    private RCF_Gvcid gvcid = null;


    public EE_RCF_Start(final EE_RCF_Start right)
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
    }

    public EE_RCF_Start(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnChFrames, SLE_OpType.sleOT_start, version, preporter);
        this.startDiagnostic = RCF_StartDiagnostic.rcfSD_invalid;
        this.startTime = null;
        this.stopTime = null;
        this.gvcid = null;
    }

    @Override
    public synchronized ISLE_Time getStartTime()
    {
        if (this.startTime != null)
        {
            return this.startTime;
        }
        return null;
    }

    @Override
    public synchronized ISLE_Time getStopTime()
    {
        if (this.stopTime != null)
        {
            return this.stopTime;
        }
        return null;
    }

    @Override
    public synchronized RCF_Gvcid getGvcid()
    {
        return this.gvcid;
    }

    @Override
    public synchronized RCF_StartDiagnostic getStartDiagnostic()
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
    public synchronized void setGvcid(RCF_Gvcid id)
    {
        if (this.gvcid == null)
        {
            this.gvcid = id;
        }
    }

    @Override
    public synchronized void putGvcid(RCF_Gvcid pid)
    {
        this.gvcid = null;
        this.gvcid = pid;
    }

    @Override
    public synchronized void setStartDiagnostic(RCF_StartDiagnostic diagnostic)
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
                && (this.startDiagnostic == RCF_StartDiagnostic.rcfSD_invalid))
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
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
                throw new SleApiException(HRESULT.SLE_E_TIMERANGE);
            }
        }
        if (this.gvcid == null)
        {
            throw new SleApiException(HRESULT.SLE_E_INVALIDID);
        }
        if (this.gvcid.getType() == RCF_ChannelType.rcfCT_invalid)
        {
            throw new SleApiException(HRESULT.SLE_E_INVALIDID);
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

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_RCF_Start ptmp = new EE_RCF_Start(this);
        return ptmp;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder("");
        printOn(oss, maxDumpLength);
        if (this.startTime != null)
        {
            String start = this.startTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_microSec);
            oss.append("Start time             : " + start + "\n");
        }
        else
        {
            oss.append("Start time             : \n");
        }
        if (this.stopTime != null)
        {
            String stop = this.stopTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_microSec);
            oss.append("Stop time              : " + stop + "\n");
        }
        else
        {
            oss.append("Stop time              : \n");
        }
        oss.append("Start diagnostic       : " + this.startDiagnostic + "\n");
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
        else if (iid == IRCF_Start.class)
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
        return "EE_RCF_Start [startDiagnostic=" + ((this.startDiagnostic != null) ? this.startDiagnostic : "")
               + ", startTime=" + ((this.startTime != null) ? this.startTime : "") + ", stopTime="
               + ((this.stopTime != null) ? this.stopTime : "") + ", gvcid=" + ((this.gvcid != null) ? this.gvcid : "")
               + "]";
    }

}
