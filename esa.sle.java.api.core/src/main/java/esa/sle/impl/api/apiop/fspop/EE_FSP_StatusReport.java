package esa.sle.impl.api.apiop.fspop;

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
import ccsds.sle.api.isrv.ifsp.IFSP_StatusReport;
import ccsds.sle.api.isrv.ifsp.types.FSP_PacketStatus;
import ccsds.sle.api.isrv.ifsp.types.FSP_ProductionStatus;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_Operation;
import esa.sle.impl.ifs.gen.EE_LogMsg;

public class EE_FSP_StatusReport extends IEE_SLE_Operation implements IFSP_StatusReport
{

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

    private long numberOfADPacketsReceived = 0;

    private long numberOfBDPacketsReceived = 0;

    private long numberOfADPacketsProcessed = 0;

    private long numberOfBDPacketsProcessed = 0;

    private long numberOfADPacketsRadiated = 0;

    private long numberOfBDPacketsRadiated = 0;

    private long numberOfPacketsAcknowledged = 0;

    private long packetBufferAvailable = 0;


    private EE_FSP_StatusReport(final EE_FSP_StatusReport right)
    {
        super(right);
        this.packetsProcessed = right.packetsProcessed;
        this.packetLastProcessed = right.packetLastProcessed;
        if (right.productionStartTime != null)
        {
            this.productionStartTime = right.productionStartTime.copy();
        }
        this.packetStatus = right.packetStatus;
        this.packetsCompleted = right.packetsCompleted;
        if (right.productionStopTime != null)
        {
            this.productionStopTime = right.productionStopTime.copy();
        }
        this.productionStatus = right.productionStatus;
        this.numberOfADPacketsReceived = right.numberOfADPacketsReceived;
        this.numberOfBDPacketsReceived = right.numberOfBDPacketsReceived;
        this.numberOfADPacketsProcessed = right.numberOfADPacketsProcessed;
        this.numberOfBDPacketsProcessed = right.numberOfBDPacketsProcessed;
        this.numberOfADPacketsRadiated = right.numberOfADPacketsRadiated;
        this.numberOfBDPacketsRadiated = right.numberOfBDPacketsRadiated;
        this.numberOfPacketsAcknowledged = right.numberOfPacketsAcknowledged;
        this.packetBufferAvailable = right.packetBufferAvailable;
    }

    public EE_FSP_StatusReport(int version)
    {
        this(version, null);
    }

    public EE_FSP_StatusReport(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt, SLE_OpType.sleOT_statusReport, version, false, preporter);
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
    public synchronized long getNumberOfADPacketsReceived()
    {
        return this.numberOfADPacketsReceived;
    }

    @Override
    public synchronized long getNumberOfBDPacketsReceived()
    {
        return this.numberOfBDPacketsReceived;
    }

    @Override
    public synchronized long getNumberOfADPacketsProcessed()
    {
        return this.numberOfADPacketsProcessed;
    }

    @Override
    public synchronized long getNumberOfBDPacketsProcessed()
    {
        return this.numberOfBDPacketsProcessed;
    }

    @Override
    public synchronized long getNumberOfADPacketsRadiated()
    {
        return this.numberOfADPacketsRadiated;
    }

    @Override
    public synchronized long getNumberOfBDPacketsRadiated()
    {
        return this.numberOfBDPacketsRadiated;
    }

    @Override
    public synchronized long getNumberOfPacketsAcknowledged()
    {
        return this.numberOfPacketsAcknowledged;
    }

    @Override
    public synchronized long getPacketBufferAvailable()
    {
        return this.packetBufferAvailable;
    }

    @Override
    public synchronized void setPacketLastProcessed(long id)
    {
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
    public synchronized void setNumberOfADPacketsReceived(long numRecv)
    {
        this.numberOfADPacketsReceived = numRecv;
    }

    @Override
    public synchronized void setNumberOfBDPacketsReceived(long numRecv)
    {
        this.numberOfBDPacketsReceived = numRecv;
    }

    @Override
    public synchronized void setNumberOfADPacketsProcessed(long numRecv)
    {
        this.numberOfADPacketsProcessed = numRecv;
        this.packetsProcessed = (this.numberOfADPacketsProcessed != 0 || this.numberOfBDPacketsProcessed != 0);
    }

    @Override
    public synchronized void setNumberOfBDPacketsProcessed(long numRecv)
    {
        this.numberOfBDPacketsProcessed = numRecv;
        this.packetsProcessed = (this.numberOfADPacketsProcessed != 0 || this.numberOfBDPacketsProcessed != 0);
    }

    @Override
    public synchronized void setNumberOfADPacketsRadiated(long numRecv)
    {
        this.numberOfADPacketsRadiated = numRecv;
    }

    @Override
    public synchronized void setNumberOfBDPacketsRadiated(long numRecv)
    {
        this.numberOfBDPacketsRadiated = numRecv;
        this.packetsCompleted = (this.numberOfPacketsAcknowledged != 0 || this.numberOfBDPacketsRadiated != 0);
    }

    @Override
    public synchronized void setNumberOfPacketsAcknowledged(long numRecv)
    {
        this.numberOfPacketsAcknowledged = numRecv;
        this.packetsCompleted = (this.numberOfPacketsAcknowledged != 0 || this.numberOfBDPacketsRadiated != 0);
    }

    @Override
    public synchronized void setPacketBufferAvailable(long size)
    {
        this.packetBufferAvailable = size;
    }

    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        if (this.numberOfADPacketsProcessed > 0 || this.numberOfBDPacketsProcessed > 0)
        {
            if (this.productionStartTime == null)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                                   EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                   "Production start time"));
            }
            if (this.packetStatus == FSP_PacketStatus.fspST_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Packet status"));
            }
        }

        if (this.numberOfPacketsAcknowledged > 0 || this.numberOfBDPacketsRadiated > 0)
        {
            if (this.productionStopTime == null)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                                   EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                   "Production stop time"));
            }
        }

        if (this.productionStatus == FSP_ProductionStatus.fspPS_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Production status"));
        }

        if (this.numberOfADPacketsProcessed > this.numberOfADPacketsReceived)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                               EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                               "Number of packets AD processed"));
        }

        if (this.numberOfBDPacketsProcessed > this.numberOfBDPacketsReceived)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                               EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                               "Number of packets BD processed"));
        }

        if (this.numberOfADPacketsProcessed < this.numberOfADPacketsRadiated)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                               EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                               "Number of packets AD radiated"));
        }

        if (this.numberOfBDPacketsProcessed < this.numberOfBDPacketsRadiated)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                               EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                               "Number of packets BD radiated"));
        }

        if (this.numberOfADPacketsRadiated < this.numberOfPacketsAcknowledged)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                               EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                               "Number of packets AD acknowledged"));
        }
    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_FSP_StatusReport ptmp = new EE_FSP_StatusReport(this);
        ISLE_Operation pop = null;
        pop = ptmp.queryInterface(IFSP_StatusReport.class);
        if (pop != null)
        {
            return pop;
        }
        return pop;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder();
        printOn(oss, maxDumpLength);
        oss.append("Packets processed      : " + this.packetsProcessed + "\n");
        oss.append("Packet Last Processed  : " + this.packetLastProcessed + "\n");
        oss.append("Production status      : " + this.productionStatus + "\n");
        oss.append("Production start time  : ");
        if (this.productionStartTime != null)
        {
            String str = this.productionStartTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth,
                                                                 SLE_TimeRes.sleTR_microSec);
            oss.append(str);
        }
        oss.append("\n");
        oss.append("Packet status          : " + this.packetStatus + "\n");
        oss.append("Packets completed      : " + this.packetsCompleted + "\n");
        oss.append("Packet Last OK         : " + this.packetLastOk + "\n");
        oss.append("Production stop time   : ");
        if (this.productionStopTime != null)
        {
            String str = this.productionStopTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth,
                                                                SLE_TimeRes.sleTR_microSec);
            oss.append(str);
        }
        oss.append("\n");
        oss.append("No. AD packets received : " + this.numberOfADPacketsReceived + "\n");
        oss.append("No. BD packets received : " + this.numberOfBDPacketsReceived + "\n");
        oss.append("No. AD packets processed: " + this.numberOfADPacketsProcessed + "\n");
        oss.append("No. BD packets processed: " + this.numberOfBDPacketsProcessed + "\n");
        oss.append("No. AD packets radiated : " + this.numberOfADPacketsRadiated + "\n");
        oss.append("No. BD packets radiated : " + this.numberOfBDPacketsRadiated + "\n");
        oss.append("No. AD packets ack.     : " + this.numberOfPacketsAcknowledged + "\n");
        oss.append("Packet buffer available : " + this.packetBufferAvailable + "\n");
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
        else if (iid == IFSP_StatusReport.class)
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
        return "EE_FSP_StatusReport [packetsProcessed=" + this.packetsProcessed + ", packetLastProcessed="
               + this.packetLastProcessed + ", productionStartTime="
               + ((this.productionStartTime != null) ? this.productionStartTime : "") + ", packetStatus="
               + this.packetStatus + ", packetsCompleted=" + this.packetsCompleted + ", packetLastOk="
               + this.packetLastOk + ", productionStopTime="
               + ((this.productionStopTime != null) ? this.productionStopTime : "") + ", productionStatus="
               + this.productionStatus + ", numberOfADPacketsReceived=" + this.numberOfADPacketsReceived
               + ", numberOfBDPacketsReceived=" + this.numberOfBDPacketsReceived + ", numberOfADPacketsProcessed="
               + this.numberOfADPacketsProcessed + ", numberOfBDPacketsProcessed=" + this.numberOfBDPacketsProcessed
               + ", numberOfADPacketsRadiated=" + this.numberOfADPacketsRadiated + ", numberOfBDPacketsRadiated="
               + this.numberOfBDPacketsRadiated + ", numberOfPacketsAcknowledged=" + this.numberOfPacketsAcknowledged
               + ", packetBufferAvailable=" + this.packetBufferAvailable + "]";
    }

}
