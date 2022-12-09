/**
 * @(#) EE_RAF_TransferData.java
 */

package esa.sle.impl.api.apiop.rafop;

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
import ccsds.sle.api.isrv.iraf.IRAF_TransferData;
import ccsds.sle.api.isrv.iraf.types.RAF_AntennaIdFormat;
import ccsds.sle.api.isrv.iraf.types.RAF_FrameQuality;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_Operation;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * @NameRAF TransferData Operation@EndName
 * @ResponsibilityThe class implements the RAF specific TransferData operation.@EndResponsibility
 */
public class EE_RAF_TransferData extends IEE_SLE_Operation implements IRAF_TransferData
{
    /**
     * The earth receive time of the frame delivered.
     */
    private ISLE_Time earthRCVTime = null;

    /**
     * The format of the antenna identifier.
     */
    private RAF_AntennaIdFormat antennaIDFormat = RAF_AntennaIdFormat.rafAF_invalid;

    /**
     * The antenna identifier in the local form.
     */
    private byte[] antennaIDLF = null;

    /**
     * The number of octets in the antenna identifier in the local form.
     */
    // private long antennaIDLFSize = 0;

    /**
     * The antenna identifier in the global form.
     */
    private int[] antennaIDGF = null;

    /**
     * The number of elements in the antenna identifier in the global form.
     */
    // private long antennaIDGFSize = 0;

    /**
     * The data link continuity.
     */
    private int dataLinkContinuity = -2;

    /**
     * The frame quality.
     */
    private RAF_FrameQuality frameQuality = RAF_FrameQuality.rafFQ_invalid;

    /**
     * The private annotation.
     */
    private byte[] privateAnnotation = null;

    /**
     * The length of the private annotation in bytes.
     */
    // private long privateAnnotationSize = 0;

    /**
     * The frame.
     */
    private byte[] data = null;


    /**
     * The length of the frame in bytes.
     */
    // private long dataSize = 0;

    private EE_RAF_TransferData(final EE_RAF_TransferData right)
    {
        super(right);
        if (right.earthRCVTime != null)
        {
            this.earthRCVTime = right.earthRCVTime.copy();
        }
        this.antennaIDFormat = right.antennaIDFormat;
        if (right.antennaIDLF != null)
        {
            for (int i = 0; i < right.antennaIDLF.length; i++)
            {
                this.antennaIDLF[i] = right.antennaIDLF[i];
            }
        }
        if (right.antennaIDGF != null)
        {
            for (int i = 0; i < right.antennaIDGF.length; i++)
            {
                this.antennaIDGF[i] = right.antennaIDGF[i];
            }
        }
        this.dataLinkContinuity = right.dataLinkContinuity;
        this.frameQuality = right.frameQuality;
        if (right.privateAnnotation != null)
        {
            for (int i = 0; i < right.privateAnnotation.length; i++)
            {
                this.privateAnnotation[i] = right.privateAnnotation[i];
            }
        }

        if (right.data != null)
        {
            for (int i = 0; i < right.data.length; i++)
            {
                this.data[i] = right.data[i];
            }
        }
    }

    public EE_RAF_TransferData(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnAllFrames, SLE_OpType.sleOT_transferData, version, false, preporter);
        this.earthRCVTime = null;
        this.antennaIDFormat = RAF_AntennaIdFormat.rafAF_invalid;
        this.antennaIDLF = null;
        this.antennaIDGF = null;
        this.dataLinkContinuity = -2;
        this.frameQuality = RAF_FrameQuality.rafFQ_invalid;
        this.privateAnnotation = null;
        this.data = null;
    }

    @Override
    public synchronized ISLE_Time getEarthReceiveTime()
    {
        if (this.earthRCVTime != null)
        {
            return this.earthRCVTime;
        }
        return null;
    }

    @Override
    public synchronized RAF_AntennaIdFormat getAntennaIdFormat()
    {
        return this.antennaIDFormat;
    }

    @Override
    public synchronized byte[] getAntennaIdLF()
    {
        assert (this.antennaIDFormat == RAF_AntennaIdFormat.rafAF_local) : "antenna id format not local";
        return this.antennaIDLF;
    }

    @Override
    public synchronized int[] getAntennaIdGF()
    {
        assert (this.antennaIDFormat == RAF_AntennaIdFormat.rafAF_global) : "antenna id format not global";
        return this.antennaIDGF;
    }

    @Override
    public synchronized String getAntennaIdGFString()
    {  	
        if (this.antennaIDFormat != RAF_AntennaIdFormat.rafAF_global)
        {
        	logAlarm(HRESULT.SLE_E_MISSINGARG,
                    EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                    "Antenna ID format not global");
        	return "";
        }
        StringBuilder oss = new StringBuilder();
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
    public synchronized RAF_FrameQuality getFrameQuality()
    {
        return this.frameQuality;
    }

    @Override
    public synchronized byte[] getPrivateAnnotation()
    {
        return this.privateAnnotation;
    }

    @Override
    public synchronized byte[] removePrivateAnnotation()
    {
        byte[] ptmp = null;
        if (this.privateAnnotation != null)
        {
            ptmp = new byte[this.privateAnnotation.length];
            for (int i = 0; i < this.privateAnnotation.length; i++)
            {
                ptmp[i] = this.privateAnnotation[i];
            }
            this.privateAnnotation = null;
        }
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
        byte[] ptmp = new byte[this.data.length];
        for (int i = 0; i < this.data.length; i++)
        {
            ptmp[i] = this.data[i];
        }
        this.data = null;
        return ptmp;
    }

    @Override
    public synchronized void setEarthReceiveTime(ISLE_Time time)
    {
        if (this.earthRCVTime != null)
        {
            this.earthRCVTime = null;
        }
        this.earthRCVTime = time.copy();
    }

    @Override
    public synchronized void putEarthReceiveTime(ISLE_Time ptime)
    {
        if (this.earthRCVTime != null)
        {
            this.earthRCVTime = null;
        }
        this.earthRCVTime = ptime;
    }

    @Override
    public synchronized void setAntennaIdLF(byte[] id)
    {
        this.antennaIDLF = null;

        this.antennaIDFormat = RAF_AntennaIdFormat.rafAF_local;
        this.antennaIDLF = new byte[id.length];
        for (int i = 0; i < id.length; i++)
        {
            this.antennaIDLF[i] = id[i];
        }
    }

    @Override
    public synchronized void setAntennaIdGF(int[] id)
    {
        this.antennaIDFormat = RAF_AntennaIdFormat.rafAF_global;
        this.antennaIDGF = new int[id.length];
        for (int i = 0; i < id.length; i++)
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
                return;
            }
        }
        if (last == '.')
        {
            return;
        }
        iNumInts++;// one more int, than there is dots ...
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
        this.antennaIDFormat = RAF_AntennaIdFormat.rafAF_global;
        this.antennaIDGF = pNew;
    }

    @Override
    public synchronized void setDataLinkContinuity(int numFrames)
    {
        this.dataLinkContinuity = numFrames;
    }

    @Override
    public synchronized void setFrameQuality(RAF_FrameQuality quality)
    {
        this.frameQuality = quality;
    }

    @Override
    public synchronized void setPrivateAnnotation(byte[] pannotation)
    {
        int size = 0;
        if (pannotation != null)
        {
            size = pannotation.length;
        }

        this.privateAnnotation = new byte[size];
        for (int i = 0; i < size; i++)
        {
            this.privateAnnotation[i] = pannotation[i];
        }
    }

    @Override
    public synchronized void putPrivateAnnotation(byte[] pannotation)
    {
        this.privateAnnotation = pannotation;
    }

    @Override
    public synchronized void setData(byte[] pdata)
    {
        int size = pdata.length;
        this.data = new byte[size];
        for (int i = 0; i < size; i++)
        {
            this.data[i] = pdata[i];
        }
    }

    @Override
    public synchronized void putData(byte[] pdata)
    {
        this.data = pdata;
    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_RAF_TransferData ptrans = new EE_RAF_TransferData(this);
        return ptrans;

    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder("");
        String sTime = null;
        String sAntenna = null;
        String sDumpData = null;
        String sDumpAnnotation = null;

        printOn(oss, maxDumpLength);

        if (this.earthRCVTime != null)
        {
            if (this.earthRCVTime.getPicosecondsResUsed())
            {
                sTime = this.earthRCVTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_picoSec);
                oss.append("Earth Receive Time (p) : " + sTime + "\n");
            }
            else
            {
                sTime = this.earthRCVTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_microSec);
                oss.append("Earth Receive Time     : " + sTime + "\n");
            }
        }
        else
        {
            oss.append("Earth Receive Time     : \n");
        }
        oss.append("Antenna Format         : " + this.antennaIDFormat + "\n");
        switch (this.antennaIDFormat)
        {
        case rafAF_local:
        {
            sAntenna = EE_GenStrUtil.convAscii(this.antennaIDLF, this.antennaIDLF.length);
            oss.append("Antenna ID             : " + sAntenna + "\n");
        }
            break;
        case rafAF_global:
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
            oss.append("Antenna ID             : \n");
        }
        oss.append("Data Link Continuity   : " + this.dataLinkContinuity + "\n");
        oss.append("Frame Quality          : " + this.frameQuality + "\n");
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
        if (!(this.earthRCVTime != null))
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
        if (this.frameQuality == RAF_FrameQuality.rafFQ_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Frame quality"));
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
        else if (iid == IRAF_TransferData.class)
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
        return "EE_RAF_TransferData [earthRCVTime=" + ((this.earthRCVTime != null) ? this.earthRCVTime : "")
               + ", antennaIDFormat=" + ((this.antennaIDFormat != null) ? this.antennaIDFormat : "") + ", antennaIDLF="
               + ((this.antennaIDLF != null) ? Arrays.toString(this.antennaIDLF) : "") + ", antennaIDGF="
               + ((this.antennaIDGF != null) ? Arrays.toString(this.antennaIDGF) : "") + ", dataLinkContinuity="
               + this.dataLinkContinuity + ", frameQuality=" + this.frameQuality + ", privateAnnotation="
               + ((this.privateAnnotation != null) ? Arrays.toString(this.privateAnnotation) : "") + ", data="
               + ((this.data != null) ? Arrays.toString(this.data) : "") + "]";
    }

}
