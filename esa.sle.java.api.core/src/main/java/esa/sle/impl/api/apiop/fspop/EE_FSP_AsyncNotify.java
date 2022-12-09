/**
 * @(#) EE_FSP_AsyncNotify.java
 */

package esa.sle.impl.api.apiop.fspop;

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
import ccsds.sle.api.isrv.ifsp.IFSP_AsyncNotify;
import ccsds.sle.api.isrv.ifsp.types.FSP_FopAlert;
import ccsds.sle.api.isrv.ifsp.types.FSP_NotificationType;
import ccsds.sle.api.isrv.ifsp.types.FSP_PacketStatus;
import ccsds.sle.api.isrv.ifsp.types.FSP_ProductionStatus;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_Operation;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * The class implements the FSP specific AsyncNotify operation.
 */
public class EE_FSP_AsyncNotify extends IEE_SLE_Operation implements IFSP_AsyncNotify
{
    /**
     * The notificatin type.
     */

    private FSP_NotificationType notificationType = FSP_NotificationType.fspNT_invalid;

    /**
     * The identification of the thrown event.
     */

    private long eventThrownID = 0;

    private long directiveExecutedId = 0;

    private long[] packetIdentificationList = null;

    private boolean packetIdentificationListPresent = false;

    private FSP_FopAlert fopAlert = FSP_FopAlert.fspFA_invalid;

    private boolean packetsProcessed = false;

    private long packetLastProcessed = 0;

    /**
     * The radiation start time of the last CLTU processed.
     */

    private ISLE_Time productionStartTime = null;

    private FSP_PacketStatus packetStatus = FSP_PacketStatus.fspST_invalid;

    private boolean packetsCompleted = false;

    private long packetLastOk = 0;

    /**
     * The radiation stop time of the last CLTU radiated.
     */

    private ISLE_Time productionStopTime = null;

    private FSP_ProductionStatus productionStatus = FSP_ProductionStatus.fspPS_invalid;
    
    /**
     * Since SLES V5
     */
    private long frameSequenceNumber;


    private EE_FSP_AsyncNotify(final EE_FSP_AsyncNotify right)
    {
        super(right);
        this.notificationType = right.notificationType;
        this.eventThrownID = right.eventThrownID;
        this.directiveExecutedId = right.directiveExecutedId;
        if (right.packetIdentificationList != null)
        {
            this.packetIdentificationList = new long[right.packetIdentificationList.length];
        }
        this.fopAlert = right.fopAlert;
        this.packetsProcessed = right.packetsProcessed;
        this.packetLastProcessed = right.packetLastProcessed;
        if (right.productionStartTime != null)
        {
            this.productionStartTime = right.productionStartTime.copy();
        }
        this.packetStatus = right.packetStatus;
        this.packetsCompleted = right.packetsCompleted;
        this.packetLastOk = right.packetLastOk;
        if (right.productionStopTime != null)
        {
            this.productionStopTime = right.productionStopTime.copy();
        }
        // New for SLES V5
        this.frameSequenceNumber = right.frameSequenceNumber;
    }

    /**
     * See specification of IFSP_AsyncNotify.
     */
    public EE_FSP_AsyncNotify(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt, SLE_OpType.sleOT_asyncNotify, version, false, preporter);
    }

    @Override
    public synchronized FSP_NotificationType getNotificationType()
    {
        return this.notificationType;
    }

    @Override
    public synchronized long getDirectiveExecutedId()
    {
        return this.directiveExecutedId;
    }

    @Override
    public synchronized long getEventThrownId()
    {
        return this.eventThrownID;
    }

    @Override
    public synchronized long[] getPacketIdentificationList()
    {
        return this.packetIdentificationList;
    }

    @Override
    public synchronized FSP_FopAlert getFopAlert()
    {
        return this.fopAlert;
    }

    @Override
    public synchronized boolean getPacketsProcessed()
    {
        return this.packetsProcessed;
    }

    @Override
    public synchronized long getPacketLastProcessed()
    {
        return this.packetLastProcessed;
    }

    @Override
    public synchronized ISLE_Time getProductionStartTime()
    {
        return this.productionStartTime;
    }

    @Override
    public synchronized FSP_PacketStatus getPacketStatus()
    {
        return this.packetStatus;
    }

    @Override
    public synchronized boolean getPacketsCompleted()
    {
        return this.packetsCompleted;
    }

    @Override
    public synchronized long getPacketLastOk()
    {
        return this.packetLastOk;
    }

    @Override
    public synchronized ISLE_Time getProductionStopTime()
    {
        return this.productionStopTime;
    }

    @Override
    public synchronized FSP_ProductionStatus getProductionStatus()
    {
        return this.productionStatus;
    }

    @Override
    public synchronized void setNotificationType(FSP_NotificationType notifyType)
    {
        this.notificationType = notifyType;
    }

    @Override
    public synchronized void setDirectiveExecutedId(long id)
    {
        this.directiveExecutedId = id;
    }

    @Override
    public synchronized void setEventThrownId(long id)
    {
        this.eventThrownID = id;
    }

    @Override
    public synchronized void setFopAlert(FSP_FopAlert alert)
    {
        this.fopAlert = alert;
    }

    @Override
    public synchronized void setPacketLastProcessed(long id)
    {
        this.packetsProcessed = true;
        this.packetLastProcessed = id;
    }

    @Override
    public synchronized void setProductionStartTime(ISLE_Time startTime)
    {
        this.productionStartTime = startTime.copy();
    }

    @Override
    public synchronized void putProductionStartTime(ISLE_Time pstartTime)
    {
        this.productionStartTime = pstartTime;
    }

    @Override
    public synchronized void setPacketStatus(FSP_PacketStatus status)
    {
        this.packetStatus = status;
    }

    @Override
    public synchronized void setPacketLastOk(long id)
    {
        this.packetsCompleted = true;
        this.packetLastOk = id;
    }

    @Override
    public synchronized void setProductionStopTime(ISLE_Time stopTime)
    {
        this.productionStopTime = stopTime.copy();
    }

    @Override
    public synchronized void putProductionStopTime(ISLE_Time pstopTime)
    {
        this.productionStopTime = pstopTime;
    }

    @Override
    public synchronized void setProductionStatus(FSP_ProductionStatus status)
    {
        this.productionStatus = status;
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

        // notification type
        if (this.notificationType == FSP_NotificationType.fspNT_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Notification type"));
        }

        // packet identification list
        switch (this.notificationType)
        {
        case fspNT_packetProcessingStarted:
        case fspNT_packetRadiated:
        case fspNT_packetAcknowledged:
            if (!this.packetIdentificationListPresent)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Packet identification list"));
            }
            if (this.packetIdentificationList.length != 1)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                                   EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                   "Packet identification list"));
            }
            break;
        case fspNT_slduExpired:
        case fspNT_productionInterrupted:
            if (!this.packetIdentificationListPresent)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Packet identification list"));
            }
            if (this.packetIdentificationList.length < 1)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                                   EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                   "Packet identification list"));
            }
            break;
        case fspNT_packetTransmissionModeMismatch:
        case fspNT_vcAborted:
        case fspNT_productionHalted:
            if (!this.packetIdentificationListPresent)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Packet identification list"));
            }
            break;
        default:
            if (this.packetIdentificationListPresent)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                                   EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                   "Packet identification list"));
            }
        }

        // FOP alert
        switch (this.notificationType)
        {
        case fspNT_transmissionModeCapabilityChange:
        case fspNT_negativeConfirmResponseToDirective:
            if (this.fopAlert == FSP_FopAlert.fspFA_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "FOP Alert"));
            }
            break;
        default:
            if (this.fopAlert != FSP_FopAlert.fspFA_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                                   EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                   "FOP Alert"));
            }
        }

        // packets processed
        switch (this.notificationType)
        {
        case fspNT_packetProcessingStarted:
        case fspNT_packetRadiated:
        case fspNT_packetAcknowledged:
        case fspNT_slduExpired:
        case fspNT_productionInterrupted:
            if (!this.packetsProcessed)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                                   EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                   "Packets processed"));
            }
            break;
        default:
            break;
        }
        if (!this.packetsProcessed && this.packetsCompleted)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                               EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                               "Packets processed"));
        }

        // production start time
        if (this.packetsProcessed)
        {
            if (this.productionStartTime == null)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Production start time"));
            }
        }

        // packet status
        if (this.packetsProcessed)
        {
            if (this.packetStatus == FSP_PacketStatus.fspST_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Packet status"));
            }
        }

        // packets complete
        if (this.notificationType == FSP_NotificationType.fspNT_packetAcknowledged)
        {
            if (!this.packetsCompleted)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                                   EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                   "Packets complete"));
            }
        }

        // production stop time
        if (this.packetsCompleted)
        {
            if (this.productionStopTime == null)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Production stop time"));
            }
        }

        // production status
        if (this.productionStatus == FSP_ProductionStatus.fspPS_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Production status"));
        }

    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_FSP_AsyncNotify ptmp = new EE_FSP_AsyncNotify(this);
        ISLE_Operation pop = null;
        pop = ptmp.queryInterface(IFSP_AsyncNotify.class);
        return pop;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder();
        printOn(oss, maxDumpLength);
        oss.append("Notification type      : " + this.notificationType + "\n");
        oss.append("Production status      : " + this.productionStatus + "\n");
        oss.append("Directive executed ID  : " + this.directiveExecutedId + "\n");
        oss.append("Event thrown ID        : " + this.eventThrownID + "\n");

        oss.append("Packet identification list: ");
        if (this.packetIdentificationListPresent)
        {
            if (this.packetIdentificationList != null)
            {
                for (long element : this.packetIdentificationList)
                {
                    oss.append(element + " ");
                }
            }
        }
        else
        {
            oss.append("<not set>");
        }
        oss.append("\n");

        oss.append("FOP alert              : " + this.fopAlert + "\n");
        oss.append("Packets processed      : " + this.packetsProcessed + "\n");
        oss.append("Packet last processed  : " + this.packetLastProcessed + "\n");
        if (this.productionStartTime != null)
        {
            oss.append("Production start time  : ");
            String str = this.productionStartTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth,
                                                                 SLE_TimeRes.sleTR_microSec);
            oss.append(str + "\n");
        }
        else
        {
            oss.append("Production start time  : \n");
        }
        oss.append("Packet status          : " + this.packetStatus + "\n");
        oss.append("Packets completed      : " + this.packetsCompleted + "\n");
        oss.append("Packet last ok         : " + this.packetLastOk + "\n");
        if (this.productionStopTime != null)
        {
            oss.append("Production stop time   : ");
            String str = this.productionStopTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth,
                                                                SLE_TimeRes.sleTR_microSec);
            oss.append(str + "\n");
        }
        else
        {
            oss.append("Production stop time   : \n");
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
        else if (iid == IFSP_AsyncNotify.class)
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
    public void setPacketIdentificationList(long[] list)
    {
        synchronized (this)
        {
            if (list.length > 0)
            {
                this.packetIdentificationList = new long[list.length];
                System.arraycopy(list, 0, this.packetIdentificationList, 0, list.length);
            }
            this.packetIdentificationListPresent = true;
        }
    }

    @Override
    public void putPacketIdentificationList(long[] list)
    {
        synchronized (this)
        {
            if (list.length > 0)
            {
                this.packetIdentificationList = list;
            }
            this.packetIdentificationListPresent = true;
        }
    }

    @Override
    public String toString()
    {
        return "EE_FSP_AsyncNotify [notificationType=" + this.notificationType + ", eventThrownID="
               + this.eventThrownID + ", directiveExecutedId=" + this.directiveExecutedId
               + ", packetIdentificationList="
               + ((this.packetIdentificationList != null) ? Arrays.toString(this.packetIdentificationList) : "")
               + ", packetIdentificationListPresent=" + this.packetIdentificationListPresent + ", fopAlert="
               + this.fopAlert + ", packetsProcessed=" + this.packetsProcessed + ", packetLastProcessed="
               + this.packetLastProcessed + ", productionStartTime="
               + ((this.productionStartTime != null) ? this.productionStartTime : "") + ", packetStatus="
               + this.packetStatus + ", packetsCompleted=" + this.packetsCompleted + ", packetLastOk="
               + this.packetLastOk + ", productionStopTime="
               + ((this.productionStopTime != null) ? this.productionStopTime : "") + ", productionStatus="
               + this.productionStatus + "]";
    }

	@Override
	public long getFrameSequenceNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setFrameSequenceNumber(long fsc) {
		// TODO Auto-generated method stub
		
	}

}
