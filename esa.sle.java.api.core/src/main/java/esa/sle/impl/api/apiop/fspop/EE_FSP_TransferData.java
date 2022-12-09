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
import ccsds.sle.api.isle.it.SLE_SlduStatusNotification;
import ccsds.sle.api.isle.it.SLE_TimeFmt;
import ccsds.sle.api.isle.it.SLE_TimeRes;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.ifsp.IFSP_TransferData;
import ccsds.sle.api.isrv.ifsp.types.FSP_TransferDataDiagnostic;
import ccsds.sle.api.isrv.ifsp.types.FSP_TransmissionMode;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_ConfirmedOperation;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_LogMsg;

public class EE_FSP_TransferData extends IEE_SLE_ConfirmedOperation implements IFSP_TransferData
{
    private long packetID = 0;

    private long expectedPacketID = 0;

    /**
     * The earliest radiation time.
     */
    private ISLE_Time earliestProdTime = null;

    /**
     * The latest radiation time.
     */
    private ISLE_Time latestProdTime = null;

    /**
     * The delay time.
     */
    private long delayTime = 0;

    private boolean mapIdUsed = false;

    private long mapId = 0;

    private FSP_TransmissionMode transmissionMode = FSP_TransmissionMode.fspTM_invalid;

    private SLE_YesNo blocking = SLE_YesNo.sleYN_invalid;

    /**
     * Indicates whether a notification shall be returned when the CLTU has been
     * radiated.
     */

    private SLE_SlduStatusNotification processingStartedNotification = SLE_SlduStatusNotification.sleSN_invalid;

    /**
     * Indicates whether a notification shall be returned when the CLTU has been
     * radiated.
     */

    private SLE_SlduStatusNotification radiationNotification = SLE_SlduStatusNotification.sleSN_invalid;

    /**
     * Indicates whether a notification shall be returned when the CLTU has been
     * radiated.
     */

    private SLE_SlduStatusNotification acknowledgedNotification = SLE_SlduStatusNotification.sleSN_invalid;

    /**
     * The CLTU data.
     */
    private byte[] data = null;

    /**
     * The available FSP buffer size in bytes.
     */

    private long packetBufferAvailable = 0;

    /**
     * The FSP Transfer Data diagnostic.
     */

    private FSP_TransferDataDiagnostic transferDataDiagnostic = FSP_TransferDataDiagnostic.fspXFD_invalid;


    private EE_FSP_TransferData(final EE_FSP_TransferData right)
    {
        super(right);
        if (right.data != null)
        {
            this.data = new byte[right.data.length];
            System.arraycopy(this.data, 0, right.data, 0, this.data.length);
        }
        if (right.earliestProdTime != null)
        {
            this.earliestProdTime = right.earliestProdTime.copy();
        }
        if (right.latestProdTime != null)
        {
            this.latestProdTime = right.latestProdTime.copy();
        }
        this.delayTime = right.delayTime;
        this.transmissionMode = right.transmissionMode;
        this.mapIdUsed = right.mapIdUsed;
        this.mapId = right.mapId;
        this.packetID = right.packetID;
        this.expectedPacketID = right.expectedPacketID;
        this.blocking = right.blocking;
        this.processingStartedNotification = right.processingStartedNotification;
        this.radiationNotification = right.radiationNotification;
        this.acknowledgedNotification = right.acknowledgedNotification;
        this.packetBufferAvailable = right.packetBufferAvailable;
        this.transferDataDiagnostic = right.transferDataDiagnostic;

    }

    public EE_FSP_TransferData(int version)
    {
        this(version, null);
    }

    /**
     * Constructor of the FSP Transfer Data Operation.
     * 
     * @param version
     * @param preporter
     */
    public EE_FSP_TransferData(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt, SLE_OpType.sleOT_transferData, version, preporter);
    }

    @Override
    public synchronized long getPacketId()
    {
        return this.packetID;
    }

    @Override
    public synchronized long getExpectedPacketId()
    {
        return this.expectedPacketID;
    }

    @Override
    public synchronized ISLE_Time getEarliestProdTime()
    {
        return this.earliestProdTime;
    }

    @Override
    public synchronized ISLE_Time getLatestProdTime()
    {
        return this.latestProdTime;
    }

    @Override
    public synchronized long getDelayTime()
    {
        return this.delayTime;
    }

    @Override
    public synchronized FSP_TransmissionMode getTransmissionMode()
    {
        return this.transmissionMode;
    }

    @Override
    public synchronized boolean getMapIdUsed()
    {
        return this.mapIdUsed;
    }

    @Override
    public synchronized long getMapId()
    {
        return this.mapId;
    }

    @Override
    public synchronized SLE_YesNo getBlocking()
    {
        return this.blocking;
    }

    @Override
    public synchronized SLE_SlduStatusNotification getProcessingStartedNotification()
    {
        return this.processingStartedNotification;
    }

    @Override
    public synchronized SLE_SlduStatusNotification getRadiatedNotification()
    {
        return this.radiationNotification;
    }

    @Override
    public synchronized SLE_SlduStatusNotification getAcknowledgedNotification()
    {
        return this.acknowledgedNotification;
    }

    @Override
    public synchronized long getPacketBufferAvailable()
    {
        return this.packetBufferAvailable;
    }

    @Override
    public synchronized FSP_TransferDataDiagnostic getTransferDataDiagnostic()
    {
        return this.transferDataDiagnostic;
    }

    @Override
    public synchronized void setPacketId(long id)
    {
        this.packetID = id;
    }

    @Override
    public synchronized void setExpectedPacketId(long id)
    {
        this.expectedPacketID = id;
    }

    @Override
    public synchronized void setEarliestProdTime(ISLE_Time earliestTime)
    {
        this.earliestProdTime = earliestTime.copy();
    }

    @Override
    public synchronized void putEarliestProdTime(ISLE_Time pearliestTime)
    {
        this.earliestProdTime = pearliestTime;
    }

    @Override
    public synchronized void setLatestProdTime(ISLE_Time latestTime)
    {
        this.latestProdTime = latestTime.copy();
    }

    @Override
    public synchronized void putLatestProdTime(ISLE_Time platestTime)
    {
        this.latestProdTime = platestTime;
    }

    @Override
    public synchronized void setDelayTime(long delay)
    {
        this.delayTime = delay;
    }

    @Override
    public synchronized void setTransmissionMode(FSP_TransmissionMode mode)
    {
        this.transmissionMode = mode;
    }

    @Override
    public synchronized void setMapId(long id)
    {
        this.mapIdUsed = true;
        this.mapId = id;
    }

    @Override
    public synchronized void setBlocking(SLE_YesNo blocking)
    {
        this.blocking = blocking;
    }

    @Override
    public synchronized void setProcessingStartedNotification(SLE_SlduStatusNotification ntf)
    {
        this.processingStartedNotification = ntf;
    }

    @Override
    public synchronized void setRadiatedNotification(SLE_SlduStatusNotification ntf)
    {
        this.radiationNotification = ntf;
    }

    @Override
    public synchronized void setAcknowledgedNotification(SLE_SlduStatusNotification ntf)
    {
        this.acknowledgedNotification = ntf;
    }

    @Override
    public synchronized void setPacketBufferAvailable(long bufAvail)
    {
        this.packetBufferAvailable = bufAvail;
    }

    @Override
    public synchronized void setTransferDataDiagnostic(FSP_TransferDataDiagnostic diagnostic)
    {
        setSpecificDiagnostics();
        this.transferDataDiagnostic = diagnostic;
    }

    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        HRESULT retval = HRESULT.S_OK;
        try
        {
            super.verifyInvocationArguments();
        }
        catch (SleApiException e)
        {
            retval = e.getHResult();
        }
        if (retval != HRESULT.S_OK)
        {
            throw new SleApiException(retval);
        }

        if (this.earliestProdTime != null && this.latestProdTime != null)
        {
            if (!(this.earliestProdTime.compareTo(this.latestProdTime) < 0))
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_TIMERANGE, EE_LogMsg.EE_OP_LM_TimeRange.getCode()));
            }
        }
        if (this.transmissionMode == FSP_TransmissionMode.fspTM_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Transmission mode"));
        }
        if (this.transmissionMode == FSP_TransmissionMode.fspTM_expedited
            && this.acknowledgedNotification == SLE_SlduStatusNotification.sleSN_produceNotification)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                               EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                               "Transmission mode and Acknowledged notification"));
        }
        if (this.mapIdUsed == true && (this.mapId < 0 || this.mapId > 63))
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                               EE_LogMsg.EE_OP_LM_Range.getCode(),
                                               "MAP ID",
                                               "0..63"));
        }
        if (this.blocking == SLE_YesNo.sleYN_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Blocking"));
        }
        if (this.processingStartedNotification == SLE_SlduStatusNotification.sleSN_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Processing started notification"));
        }
        if (this.radiationNotification == SLE_SlduStatusNotification.sleSN_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Radiated notification"));
        }
        if (this.acknowledgedNotification == SLE_SlduStatusNotification.sleSN_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Acknowledged notification"));
        }
        if (this.data == null)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Data"));
        }
    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_FSP_TransferData ptmp = new EE_FSP_TransferData(this);
        ISLE_Operation pop = null;
        pop = ptmp.queryInterface(IFSP_TransferData.class);
        return pop;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder();
        printOn(oss, maxDumpLength);
        oss.append("Packet ID              : " + this.packetID + "\n");
        oss.append("Expected Packet ID     : " + this.expectedPacketID + "\n");

        if (this.earliestProdTime != null)
        {
            String str = this.earliestProdTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_microSec);
            oss.append("Earliest Production time: " + str + "\n");
        }
        else
        {
            oss.append("Earliest Production time: \n");
        }
        if (this.latestProdTime != null)
        {
            String str = this.latestProdTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_microSec);
            oss.append("Latest Production time : " + str + "\n");
        }
        else
        {
            oss.append("Latest Production time : \n");
        }
        oss.append("Delay time             : " + this.delayTime + "\n");
        if (this.mapIdUsed)
        {
            oss.append("MAP ID                 : " + this.mapId + "\n");
        }
        else
        {
            oss.append("MAP ID                 : " + this.mapId + "\n");
        }
        oss.append("Transmission mode      : " + this.transmissionMode + "\n");
        oss.append("Blocking               : " + this.blocking + "\n");
        oss.append("Processing started Not.: " + this.processingStartedNotification + "\n");
        oss.append("Radiation Notification : " + this.radiationNotification + "\n");
        oss.append("Acknowledged Not.      : " + this.acknowledgedNotification + "\n");

        if (this.data != null)
        {
            long inlen = this.data.length;
            if (maxDumpLength < inlen)
            {
                inlen = maxDumpLength;
            }
            String str = EE_GenStrUtil.convAscii(this.data, inlen);
            oss.append("Data                   : " + str + "\n");
        }
        else
        {
            oss.append("Data                  : \n");
        }
        oss.append("Packet Buffer Available: " + this.packetBufferAvailable + "\n");
        oss.append("Transfer Diagnostic    : " + this.transferDataDiagnostic + "\n");
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
            if (this.expectedPacketID != this.packetID + 1)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                                   EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                   "Expected packet ID"));
            }

        }
        else if (getResult() == SLE_Result.sleRES_negative)
        {
            if (getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                if (this.transferDataDiagnostic == FSP_TransferDataDiagnostic.fspXFD_invalid)
                {
                    throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                                       EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                       "Transfer data diagnostic"));
                }
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
        else if (iid == ISLE_ConfirmedOperation.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Operation.class)
        {
            return (T) this;

        }
        else if (iid == IFSP_TransferData.class)
        {
            return (T) this;
        }

        else
        {
            return null;
        }
    }

    @Override
    public synchronized byte[] getData()
    {
        return this.data;
    }

    @Override
    public synchronized byte[] removeData()
    {
        byte[] pretval = new byte[this.data.length];
        System.arraycopy(this.data, 0, pretval, 0, this.data.length);
        this.data = null;
        return pretval;
    }

    @Override
    public synchronized void setData(byte[] pdata)
    {
        this.data = new byte[pdata.length];
        System.arraycopy(pdata, 0, this.data, 0, pdata.length);
    }

    @Override
    public synchronized void putData(byte[] pdata)
    {
        this.data = pdata;
    }

    @Override
    public synchronized String toString()
    {
        return "EE_FSP_TransferData [packetID=" + this.packetID + ", expectedPacketID=" + this.expectedPacketID
               + ", earliestProdTime=" + ((this.earliestProdTime != null) ? this.earliestProdTime : "")
               + ", latestProdTime=" + ((this.latestProdTime != null) ? this.latestProdTime : "") + ", delayTime="
               + this.delayTime + ", mapIdUsed=" + this.mapIdUsed + ", mapId=" + this.mapId + ", transmissionMode="
               + this.transmissionMode + ", blocking=" + this.blocking + ", processingStartedNotification="
               + this.processingStartedNotification + ", radiationNotification=" + this.radiationNotification
               + ", acknowledgedNotification=" + this.acknowledgedNotification + ", data="
               + ((this.data != null) ? Arrays.toString(this.data) : "") + ", packetBufferAvailable="
               + this.packetBufferAvailable + ", transferDataDiagnostic=" + this.transferDataDiagnostic + "]";
    }

}
