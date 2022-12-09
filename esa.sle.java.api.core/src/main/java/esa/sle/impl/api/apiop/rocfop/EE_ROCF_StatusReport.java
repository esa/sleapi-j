/**
 * @(#) EE_ROCF_StatusReport.java
 */

package esa.sle.impl.api.apiop.rocfop;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isrv.irocf.IROCF_StatusReport;
import ccsds.sle.api.isrv.irocf.types.ROCF_LockStatus;
import ccsds.sle.api.isrv.irocf.types.ROCF_ProductionStatus;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_Operation;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * @NameROCF StatusReport Operation@EndName
 * @ResponsibilityThe class implements the ROCF specific StatusReport operation.@EndResponsibility
 */
public class EE_ROCF_StatusReport extends IEE_SLE_Operation implements IROCF_StatusReport
{
    /**
     * The total number of frames delivered.
     */
    private long numFrames = 0;

    /**
     * The total number of frames delivered.
     */
    private long numOcfDelivered = 0;

    /**
     * The lock status of the frame synchronisation process.
     */
    private ROCF_LockStatus frameSyncLock = ROCF_LockStatus.rocfLS_invalid;

    /**
     * The lock status of the carrier demodulation process.
     */
    private ROCF_LockStatus carrierDemodLock = ROCF_LockStatus.rocfLS_invalid;

    /**
     * The lock status of the sub-carrier demodulation process.
     */
    private ROCF_LockStatus subCarrierDemodLock = ROCF_LockStatus.rocfLS_invalid;

    /**
     * The lock status of the symbol synchronisation process.
     */
    private ROCF_LockStatus symbolSyncLock = ROCF_LockStatus.rocfLS_invalid;

    /**
     * The production status.
     */
    private ROCF_ProductionStatus productionStatus = ROCF_ProductionStatus.rocfPS_invalid;


    private EE_ROCF_StatusReport(final EE_ROCF_StatusReport right)
    {
        super(right);
        this.numFrames = right.numFrames;
        this.numOcfDelivered = right.numOcfDelivered;
        this.frameSyncLock = right.frameSyncLock;
        this.carrierDemodLock = right.carrierDemodLock;
        this.subCarrierDemodLock = right.subCarrierDemodLock;
        this.symbolSyncLock = right.symbolSyncLock;
        this.productionStatus = right.productionStatus;
    }

    public EE_ROCF_StatusReport(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnChOcf, SLE_OpType.sleOT_statusReport, version, false, preporter);
        this.numFrames = 0;
        this.numOcfDelivered = 0;
        this.frameSyncLock = ROCF_LockStatus.rocfLS_invalid;
        this.carrierDemodLock = ROCF_LockStatus.rocfLS_invalid;
        this.subCarrierDemodLock = ROCF_LockStatus.rocfLS_invalid;
        this.symbolSyncLock = ROCF_LockStatus.rocfLS_invalid;
        this.productionStatus = ROCF_ProductionStatus.rocfPS_invalid;
    }

    @Override
    public synchronized long getNumFrames()
    {
        return this.numFrames;
    }

    @Override
    public synchronized long getNumOcfDelivered()
    {
        return this.numOcfDelivered;
    }

    @Override
    public synchronized ROCF_LockStatus getFrameSyncLock()
    {
        return this.frameSyncLock;
    }

    @Override
    public synchronized ROCF_LockStatus getCarrierDemodLock()
    {
        return this.carrierDemodLock;
    }

    @Override
    public synchronized ROCF_LockStatus getSubCarrierDemodLock()
    {
        return this.subCarrierDemodLock;
    }

    @Override
    public synchronized ROCF_LockStatus getSymbolSyncLock()
    {
        return this.symbolSyncLock;
    }

    @Override
    public synchronized ROCF_ProductionStatus getProductionStatus()
    {
        return this.productionStatus;
    }

    @Override
    public synchronized void setNumFrames(long count)
    {
        this.numFrames = count;
    }

    @Override
    public synchronized void setNumOcfDelivered(long count)
    {
        this.numOcfDelivered = count;
    }

    @Override
    public synchronized void setFrameSyncLock(ROCF_LockStatus status)
    {
        this.frameSyncLock = status;
    }

    @Override
    public synchronized void setCarrierDemodLock(ROCF_LockStatus status)
    {
        this.carrierDemodLock = status;
    }

    @Override
    public synchronized void setSubCarrierDemodLock(ROCF_LockStatus status)
    {
        this.subCarrierDemodLock = status;
    }

    @Override
    public synchronized void setSymbolSyncLock(ROCF_LockStatus status)
    {
        this.symbolSyncLock = status;
    }

    @Override
    public synchronized void setProductionStatus(ROCF_ProductionStatus status)
    {
        this.productionStatus = status;
    }

    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        super.verifyInvocationArguments();
        if (this.frameSyncLock == ROCF_LockStatus.rocfLS_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "frame synchronisation lock"));
        }
        if (this.frameSyncLock == ROCF_LockStatus.rocfLS_notInUse)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                               EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                               "frame synchronisation lock"));
        }
        if (this.symbolSyncLock == ROCF_LockStatus.rocfLS_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Sybol sychronisation lock"));
        }
        if (this.symbolSyncLock == ROCF_LockStatus.rocfLS_notInUse)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                               EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                               "Sybol sychronisation lock"));
        }
        if (this.carrierDemodLock == ROCF_LockStatus.rocfLS_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Carrier demodulation lock"));
        }
        if (this.carrierDemodLock == ROCF_LockStatus.rocfLS_notInUse)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                               EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                               "Carrier demodulation lock"));
        }
        if (this.subCarrierDemodLock == ROCF_LockStatus.rocfLS_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Sub-carrier demodulation lock"));
        }
        if (this.productionStatus == ROCF_ProductionStatus.rocfPS_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Production status"));
        }
    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_ROCF_StatusReport pobj = new EE_ROCF_StatusReport(this);
        return pobj;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder("");
        printOn(oss, maxDumpLength);
        oss.append("Number of frames       : " + this.numFrames + "\n");
        oss.append("Number of OCF delivered: " + this.numOcfDelivered + "\n");
        oss.append("Frame sync lock        : " + this.frameSyncLock + "\n");
        oss.append("Symbol sync lock       : " + this.symbolSyncLock + "\n");
        oss.append("Carrier demod lock     : " + this.carrierDemodLock + "\n");
        oss.append("Subcarrier demod lock  : " + this.subCarrierDemodLock + "\n");
        oss.append("Production status      : " + this.productionStatus + "\n");
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
        else if (iid == IROCF_StatusReport.class)
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
        return "EE_ROCF_StatusReport [numFrames=" + this.numFrames + ", numOcfDelivered=" + this.numOcfDelivered
               + ", frameSyncLock=" + this.frameSyncLock + ", carrierDemodLock=" + this.carrierDemodLock
               + ", subCarrierDemodLock=" + this.subCarrierDemodLock + ", symbolSyncLock=" + this.symbolSyncLock
               + ", productionStatus=" + this.productionStatus + "]";
    }

}
