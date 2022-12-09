/**
 * @(#) EE_RCF_TransferData.java
 */

package esa.sle.impl.api.apiop.rcfop;

import java.util.Arrays;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_TimeFmt;
import ccsds.sle.api.isle.it.SLE_TimeRes;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.ircf.IRCF_TransferData;
import ccsds.sle.api.isrv.ircf.types.RCF_AntennaIdFormat;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_Operation;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * RCF TransferData Operation@EndName The class implements the RCF specific
 * TransferData operation.
 */
public class EE_RCF_TransferData extends IEE_SLE_Operation implements IRCF_TransferData
{
    /**
     * The earth receive time of the frame delivered.
     */
    private ISLE_Time earthReceiveTime = null;

    /**
     * The format of the antenna identifier.
     */
    private RCF_AntennaIdFormat antennaIDFormat = RCF_AntennaIdFormat.rcfAF_invalid;

    /**
     * The antenna identifier in the local form.
     */
    private byte[] antennaIDLF = null;

    /**
     * The antenna identifier in the global form.
     */
    private int[] antennaIDGF = null;

    /**
     * The data link continuity.
     */
    private int dataLinkContinuity = -2;

    /**
     * The private annotation.
     */
    private byte[] privateAnnotation = null;

    /**
     * The frame.
     */
    private byte[] data = null;


    EE_RCF_TransferData(final EE_RCF_TransferData right)
    {
        super(right);
        if (right.earthReceiveTime != null)
        {
            this.earthReceiveTime = right.earthReceiveTime.copy();
        }
        this.antennaIDFormat = right.antennaIDFormat;
        if (right.antennaIDLF != null)
        {
            this.antennaIDLF = new byte[right.antennaIDLF.length];
            for (int i = 0; i < this.antennaIDLF.length; i++)
            {
                this.antennaIDLF[i] = right.antennaIDLF[i];
            }
        }
        if (right.antennaIDGF != null)
        {
            this.antennaIDGF = new int[right.antennaIDGF.length];
            for (int i = 0; i < this.antennaIDGF.length; i++)
            {
                this.antennaIDGF[i] = right.antennaIDGF[i];
            }
        }
        this.dataLinkContinuity = right.dataLinkContinuity;
        if (right.data != null)
        {
            this.data = new byte[right.data.length];
            for (int i = 0; i < this.data.length; i++)
            {
                this.data[i] = right.data[i];
            }
        }
        if (right.privateAnnotation != null)
        {
            this.privateAnnotation = new byte[right.privateAnnotation.length];
            for (int i = 0; i < this.privateAnnotation.length; i++)
            {
                this.privateAnnotation[i] = right.privateAnnotation[i];
            }
        }
    }

    public EE_RCF_TransferData(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnChFrames, SLE_OpType.sleOT_transferData, version, false, preporter);
        this.earthReceiveTime = null;
        this.antennaIDFormat = RCF_AntennaIdFormat.rcfAF_invalid;
        this.antennaIDLF = null;
        this.antennaIDGF = null;
        this.dataLinkContinuity = -2;
        this.privateAnnotation = null;
        this.data = null;
    }

    @Override
    public synchronized ISLE_Time getEarthReceiveTime()
    {
        return this.earthReceiveTime;
    }

    @Override
    public synchronized RCF_AntennaIdFormat getAntennaIdFormat()
    {
        return this.antennaIDFormat;
    }

    @Override
    public synchronized byte[] getAntennaIdLF()
    {
        assert (this.antennaIDFormat == RCF_AntennaIdFormat.rcfAF_local) : "wrong antenna format";
        return this.antennaIDLF;
    }

    @Override
    public synchronized int[] getAntennaIdGF()
    {
        assert (this.antennaIDFormat == RCF_AntennaIdFormat.rcfAF_global) : "wrong antennaFormat";
        return this.antennaIDGF;
    }

    @Override
    public synchronized String getAntennaIdGFString()
    {
        assert (this.antennaIDFormat == RCF_AntennaIdFormat.rcfAF_global) : "antenna id format not global";

        StringBuilder oss = new StringBuilder("");
        for (int i = 0; i < this.antennaIDGF.length; i++)
        {
            if (i != 0)
            {
                oss.append(".");
            }
            oss.append(this.antennaIDGF[i]);
        }

        String ret = oss.toString();
        return ret;
    }

    @Override
    public synchronized int getDataLinkContinuity()
    {
        return this.dataLinkContinuity;
    }

    @Override
    public synchronized byte[] getPrivateAnnotation()
    {
        return this.privateAnnotation;
    }

    @Override
    public synchronized byte[] removePrivateAnnotation()
    {
        byte[] ptmp = this.privateAnnotation;
        this.privateAnnotation = null;
        return ptmp;
    }

    @Override
    public synchronized byte[] getData()
    {
        return this.data;
    }

    @Override
    public synchronized byte[] removeData()
    {
        byte[] ptmp = this.data;
        this.data = null;
        return ptmp;
    }

    @Override
    public synchronized void setEarthReceiveTime(ISLE_Time time)
    {
        if (this.earthReceiveTime != null)
        {
            this.earthReceiveTime = null;
        }
        this.earthReceiveTime = time.copy();
    }

    @Override
    public synchronized void putEarthReceiveTime(ISLE_Time ptime)
    {
        if (this.earthReceiveTime != null)
        {
            this.earthReceiveTime = null;
        }
        this.earthReceiveTime = ptime;
    }

    @Override
    public synchronized void setAntennaIdLF(byte[] id)
    {
        this.antennaIDLF = new byte[id.length];
        this.antennaIDFormat = RCF_AntennaIdFormat.rcfAF_local;
        for (int i = 0; i < this.antennaIDLF.length; i++)
        {
            this.antennaIDLF[i] = id[i];
        }
    }

    @Override
    public synchronized void setAntennaIdGF(int[] id)
    {
        this.antennaIDGF = new int[id.length];
        this.antennaIDFormat = RCF_AntennaIdFormat.rcfAF_global;
        for (int i = 0; i < this.antennaIDGF.length; i++)
        {
            this.antennaIDGF[i] = id[i];
        }
    }

    @Override
    public synchronized void setAntennaIdGFString(String id)
    {
        int iNumInts = 0;
        char last = 0;
        for (int i = 0; i < id.length(); i++)
        {
            last = id.charAt(i);
            if (id.charAt(i) == '.')
            {
                iNumInts++;
            }
            else if (!Character.isDigit((id.charAt(i))))
            {
                // spec says to not do anything if the argument is badly
                // formatted.
                return;
            }
        }
        if (last == '.')
        {
            return;
        }
        iNumInts++;// one more int than there is dots ...
        int[] pNew = new int[iNumInts];

        int sepPos = id.indexOf('.');
        for (int j = 0; j < iNumInts; j++)
        {
            pNew[j] = Integer.parseInt(id.substring(0, sepPos));
            id = id.substring(sepPos + 1);
            sepPos = id.indexOf('.');
            if (sepPos < 0)
            {
                pNew[j + 1] = Integer.parseInt(id);
                break;
            }
        }
        this.antennaIDGF = null;
        this.antennaIDFormat = RCF_AntennaIdFormat.rcfAF_global;
        this.antennaIDGF = pNew;
    }

    @Override
    public synchronized void setDataLinkContinuity(int numFrames)
    {
        this.dataLinkContinuity = numFrames;
    }

    @Override
    public synchronized void setPrivateAnnotation(byte[] pannotation)
    {
        if (this.privateAnnotation != null)
        {
            this.privateAnnotation = null;
        }
        this.privateAnnotation = new byte[pannotation.length];
        for (int i = 0; i < pannotation.length; i++)
        {
            this.privateAnnotation[i] = pannotation[i];
        }
    }

    @Override
    public synchronized void putPrivateAnnotation(byte[] pannotation)
    {
        if (this.privateAnnotation != null)
        {
            this.privateAnnotation = null;
        }
        this.privateAnnotation = pannotation;
    }

    @Override
    public synchronized void setData(byte[] pdata)
    {
        if (this.data != null)
        {
            this.data = null;
        }
        this.data = new byte[pdata.length];
        for (int i = 0; i < this.data.length; i++)
        {
            this.data[i] = pdata[i];
        }
    }

    @Override
    public synchronized void putData(byte[] pdata)
    {
        if (this.data != null)
        {
            this.data = null;
        }
        this.data = pdata;
    }

    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        super.verifyInvocationArguments();
        if (!(this.antennaIDLF != null || this.antennaIDGF != null))
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Antenna ID"));
        }
        if (!(this.earthReceiveTime != null))
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Earth receive time"));
        }
        if (this.dataLinkContinuity <= -2)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                               EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                               "Data link continuity"));
        }
        if ((this.privateAnnotation != null)
            && ((this.privateAnnotation.length < 0) || (this.privateAnnotation.length > 128)))
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                               EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                               "Private annotation"));
        }
        if (!(this.data != null))
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Data"));
        }
    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_RCF_TransferData ptrans = new EE_RCF_TransferData(this);
        return ptrans;

    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder();
        String sTime = null;
        String sAntenna = null;
        String sDumpData = null;
        String sDumpAnnotation = null;

        printOn(oss, maxDumpLength);

        if (this.earthReceiveTime != null)
        {
            if (this.earthReceiveTime.getPicosecondsResUsed())
            {
                sTime = this.earthReceiveTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_picoSec);
                oss.append("Earth Receive Time (p) : " + sTime + "\n");
            }
            else
            {
                sTime = this.earthReceiveTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_microSec);
                oss.append("Earth Receive Time     : " + sTime + "\n");
            }
        }
        else
        {
            oss.append("Earth Receive Time     : " + "\n");
        }
        oss.append("Antenna Format         : " + this.antennaIDFormat + "\n");
        switch (this.antennaIDFormat)
        {
        case rcfAF_local:
        {
            sAntenna = EE_GenStrUtil.convAscii(this.antennaIDLF, this.antennaIDLF.length);
            oss.append("Antenna ID             : " + sAntenna + "\n");
        }
            break;
        case rcfAF_global:
        {
            oss.append("Antenna ID             : ");
            for (int i = 0; i < this.antennaIDGF.length; i++)
            {
                if (i != 0)
                {
                    oss.append(".");
                }
                oss.append(this.antennaIDGF[i]);
            }
            oss.append("\n");
            break;
        }
        default:
            oss.append("Antenna ID             : " + "\n");
        }
        oss.append("Data Link Continuity   : " + this.dataLinkContinuity + "\n");
        if (this.privateAnnotation != null)
        {
            oss.append("Private Annotation Len : " + this.privateAnnotation.length + "\n");
        }
        else
        {
            oss.append("Private Annotation Len : 0" + "\n");
        }
        oss.append("Private Annotation     : ");
        if (this.privateAnnotation != null)
        {
            long iRequestDump = (this.privateAnnotation.length < maxDumpLength) ? this.privateAnnotation.length
                                                                               : maxDumpLength;
            sDumpAnnotation = EE_GenStrUtil.convAscii(this.privateAnnotation, iRequestDump);
            oss.append(sDumpAnnotation);
        }

        if (this.data != null)
        {
            oss.append("\n");
            oss.append("Data Length             : " + this.data.length + "\n");
            oss.append("Data                    : ");

            long iRequestDump = (this.data.length < maxDumpLength) ? this.data.length : maxDumpLength;
            sDumpData = EE_GenStrUtil.convAscii(this.data, iRequestDump);
            oss.append(sDumpData);
        }
        else
        {
            oss.append("\n");
            oss.append("Data Length             : " + "\n");
            oss.append("Data                    : ");
        }

        oss.append("\n");
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
        else if (iid == ISLE_Operation.class)
        {
            return (T) this;
        }
        else if (iid == IRCF_TransferData.class)
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
        return "EE_RCF_TransferData [earthReceiveTime="
               + ((this.earthReceiveTime != null) ? this.earthReceiveTime : "") + ", antennaIDFormat="
               + this.antennaIDFormat + ", antennaIDLF="
               + ((this.antennaIDLF != null) ? Arrays.toString(this.antennaIDLF) : "") + ", antennaIDGF="
               + ((this.antennaIDGF != null) ? Arrays.toString(this.antennaIDGF) : "") + ", dataLinkContinuity="
               + this.dataLinkContinuity + ", privateAnnotation="
               + ((this.privateAnnotation != null) ? Arrays.toString(this.privateAnnotation) : null) + ", data="
               + ((this.data != null) ? Arrays.toString(this.data) : "") + "]";
    }

}
