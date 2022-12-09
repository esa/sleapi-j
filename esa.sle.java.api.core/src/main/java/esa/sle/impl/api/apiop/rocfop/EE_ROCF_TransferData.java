/**
 * @(#) EE_ROCF_TransferData.java
 */

package esa.sle.impl.api.apiop.rocfop;

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
import ccsds.sle.api.isrv.irocf.IROCF_TransferData;
import ccsds.sle.api.isrv.irocf.types.ROCF_AntennaIdFormat;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_Operation;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * ROCF TransferData Operation The class implements the ROCF specific
 * TransferData operation
 */
public class EE_ROCF_TransferData extends IEE_SLE_Operation implements IROCF_TransferData
{
    /**
     * The earth receive time of the frame delivered.
     */
    private ISLE_Time earthReceiveTime = null;

    /**
     * The format of the antenna identifier.
     */
    private ROCF_AntennaIdFormat antennaIDFormat = ROCF_AntennaIdFormat.rocfAF_invalid;

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


    private EE_ROCF_TransferData(final EE_ROCF_TransferData right)
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
            for (int i = 0; i < right.antennaIDLF.length; i++)
            {
                this.antennaIDLF[i] = right.antennaIDLF[i];
            }
        }
        if (right.antennaIDGF != null)
        {
            this.antennaIDGF = new int[right.antennaIDGF.length];
            for (int i = 0; i < right.antennaIDGF.length; i++)
            {
                this.antennaIDGF[i] = right.antennaIDGF[i];
            }
        }
        this.dataLinkContinuity = right.dataLinkContinuity;
        if (right.data != null)
        {
            this.data = new byte[4];
            for (int i = 0; i < 4; i++)
            {
                this.data[i] = right.data[i];
            }
        }
        if (right.privateAnnotation != null)
        {
            this.privateAnnotation = new byte[right.privateAnnotation.length];
            for (int i = 0; i < right.privateAnnotation.length; i++)
            {
                this.privateAnnotation[i] = right.privateAnnotation[i];
            }
        }

    }

    public EE_ROCF_TransferData(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnChOcf, SLE_OpType.sleOT_transferData, version, false, preporter);
        this.earthReceiveTime = null;
        this.antennaIDFormat = ROCF_AntennaIdFormat.rocfAF_invalid;
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
    public synchronized ROCF_AntennaIdFormat getAntennaIdFormat()
    {
        return this.antennaIDFormat;
    }

    @Override
    public synchronized byte[] getAntennaIdLF()
    {
        assert (this.antennaIDFormat == ROCF_AntennaIdFormat.rocfAF_local) : "Antenna id not in local format";
        return this.antennaIDLF;
    }

    @Override
    public synchronized int[] getAntennaIdGF()
    {
        assert (this.antennaIDFormat == ROCF_AntennaIdFormat.rocfAF_global) : "Antenna id not in global format";
        return this.antennaIDGF;
    }

    @Override
    public synchronized String getAntennaIdGFString()
    {
        assert (this.antennaIDFormat == ROCF_AntennaIdFormat.rocfAF_global) : "Antenna id not in global format";
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
        this.antennaIDFormat = ROCF_AntennaIdFormat.rocfAF_local;
        for (int i = 0; i < this.antennaIDLF.length; i++)
        {
            this.antennaIDLF[i] = id[i];
        }
    }

    @Override
    public synchronized void setAntennaIdGF(int[] id)
    {
        this.antennaIDGF = new int[id.length];
        this.antennaIDFormat = ROCF_AntennaIdFormat.rocfAF_global;
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
            else if (!Character.isDigit(id.charAt(i)))
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
        this.antennaIDFormat = ROCF_AntennaIdFormat.rocfAF_global;
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
        this.privateAnnotation = new byte[pannotation.length];
        for (int i = 0; i < pannotation.length; i++)
        {
            this.privateAnnotation[i] = pannotation[i];
        }
    }

    @Override
    public synchronized void putPrivateAnnotation(byte[] pannotation)
    {
        this.privateAnnotation = null;
        this.privateAnnotation = pannotation;
    }

    @Override
    public synchronized void setData(byte[] pdata)
    {
        if (pdata == null)
        {
            if (this.data != null)
            {
                this.data = null;
            }
        }
        else
        {
            if (this.data == null)
            {
                this.data = new byte[4];
            }
            for (int i = 0; i < 4; i++)
            {
                this.data[i] = pdata[i];
            }
        }
    }

    @Override
    public synchronized void putData(byte[] pdata)
    {
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
        EE_ROCF_TransferData ptrans = new EE_ROCF_TransferData(this);
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
        oss.append("Antenna ID             : ");
        switch (this.antennaIDFormat)
        {
        case rocfAF_local:
            if (this.antennaIDLF != null)
            {
                sAntenna = EE_GenStrUtil.convAscii(this.antennaIDLF, this.antennaIDLF.length);
                oss.append(sAntenna);
            }
            break;
        case rocfAF_global:
            if (this.antennaIDGF != null)
            {
                for (int i = 0; i < this.antennaIDGF.length; i++)
                {
                    if (i != 0)
                    {
                        oss.append(".");
                    }
                    oss.append(this.antennaIDGF[i]);
                }
            }
            break;
        default:
            break;
        }
        oss.append("\n");
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
        oss.append("\n");
        oss.append("Data                    : ");
        if (this.data != null)
        {
            long iRequestDump = (4 < maxDumpLength) ? 4 : maxDumpLength;
            sDumpData = EE_GenStrUtil.convAscii(this.data, iRequestDump);
            oss.append(sDumpData);
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
        else if (iid == IROCF_TransferData.class)
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
        return "EE_ROCF_TransferData [earthReceiveTime="
               + ((this.earthReceiveTime != null) ? this.earthReceiveTime : "") + ", antennaIDFormat="
               + this.antennaIDFormat + ", antennaIDLF="
               + ((this.antennaIDLF != null) ? Arrays.toString(this.antennaIDLF) : "") + ", antennaIDGF="
               + ((this.antennaIDGF != null) ? Arrays.toString(this.antennaIDGF) : "") + ", dataLinkContinuity="
               + this.dataLinkContinuity + ", privateAnnotation="
               + ((this.privateAnnotation != null) ? Arrays.toString(this.privateAnnotation) : "") + ", data="
               + ((this.data != null) ? Arrays.toString(this.data) : "") + "]";
    }

}
