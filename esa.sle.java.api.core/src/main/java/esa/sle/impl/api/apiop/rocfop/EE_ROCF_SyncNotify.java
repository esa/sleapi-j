/**
 * @(#) EE_ROCF_SyncNotify.java
 */

package esa.sle.impl.api.apiop.rocfop;

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
import ccsds.sle.api.isrv.irocf.IROCF_SyncNotify;
import ccsds.sle.api.isrv.irocf.types.ROCF_LockStatus;
import ccsds.sle.api.isrv.irocf.types.ROCF_NotificationType;
import ccsds.sle.api.isrv.irocf.types.ROCF_ProductionStatus;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_Operation;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * @NameROCF SyncNotify Operation@EndName
 * @ResponsibilityThe class implements the ROCF specific SyncNotify operation.@EndResponsibility
 */
public class EE_ROCF_SyncNotify extends IEE_SLE_Operation implements IROCF_SyncNotify
{
    /**
     * The type of notification.
     */
    private ROCF_NotificationType notificationType = ROCF_NotificationType.rocfNT_invalid;

    /**
     * The time at which the frame synchroniser lost lock.
     */
    private ISLE_Time lossOfLockTime = null;

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


    private EE_ROCF_SyncNotify(final EE_ROCF_SyncNotify right)
    {
        super(right);

        if (right.lossOfLockTime != null)
        {
            this.lossOfLockTime = right.lossOfLockTime.copy();
        }
        this.symbolSyncLock = right.symbolSyncLock;
        this.subCarrierDemodLock = right.subCarrierDemodLock;
        this.carrierDemodLock = right.carrierDemodLock;
        this.notificationType = right.notificationType;
        this.productionStatus = right.productionStatus;

    }

    public EE_ROCF_SyncNotify(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnChOcf, SLE_OpType.sleOT_syncNotify, version, false, preporter);
        this.notificationType = ROCF_NotificationType.rocfNT_invalid;
        this.lossOfLockTime = null;
        this.carrierDemodLock = ROCF_LockStatus.rocfLS_invalid;
        this.subCarrierDemodLock = ROCF_LockStatus.rocfLS_invalid;
        this.symbolSyncLock = ROCF_LockStatus.rocfLS_invalid;
        this.productionStatus = ROCF_ProductionStatus.rocfPS_invalid;
    }

    @Override
    public synchronized ROCF_NotificationType getNotificationType()
    {
        return this.notificationType;
    }

    @Override
    public synchronized ISLE_Time getLossOfLockTime()
    {
        assert (this.notificationType == ROCF_NotificationType.rocfNT_lossFrameSync) : "incorrect notification type";
        return this.lossOfLockTime;
    }

    @Override
    public synchronized ROCF_LockStatus getCarrierDemodLock()
    {
        assert (this.notificationType == ROCF_NotificationType.rocfNT_lossFrameSync) : "incorrect notification type";
        return this.carrierDemodLock;
    }

    @Override
    public synchronized ROCF_LockStatus getSubCarrierDemodLock()
    {
        assert (this.notificationType == ROCF_NotificationType.rocfNT_lossFrameSync) : "incorrect notification type";
        return this.subCarrierDemodLock;
    }

    @Override
    public synchronized ROCF_LockStatus getSymbolSyncLock()
    {
        assert (this.notificationType == ROCF_NotificationType.rocfNT_lossFrameSync) : "incorrect notification type";
        return this.symbolSyncLock;
    }

    @Override
    public synchronized ROCF_ProductionStatus getProductionStatus()
    {
        assert (this.notificationType == ROCF_NotificationType.rocfNT_productionStatusChange) : "incorrect notification type ";
        return this.productionStatus;
    }

    @Override
    public synchronized void setLossOfFrameSync(ISLE_Time time,
                                                ROCF_LockStatus symbolSyncLock,
                                                ROCF_LockStatus subCarrierDemodLock,
                                                ROCF_LockStatus carrierDemodLock)
    {
        if (this.lossOfLockTime != null)
        {
            this.lossOfLockTime = null;
        }
        this.lossOfLockTime = time.copy();
        this.symbolSyncLock = symbolSyncLock;
        this.subCarrierDemodLock = subCarrierDemodLock;
        this.carrierDemodLock = carrierDemodLock;
        this.notificationType = ROCF_NotificationType.rocfNT_lossFrameSync;
    }

    @Override
    public synchronized void setProductionStatusChange(ROCF_ProductionStatus status)
    {
        this.notificationType = ROCF_NotificationType.rocfNT_productionStatusChange;
        this.productionStatus = status;
    }

    @Override
    public synchronized void setDataDiscarded()
    {
        this.notificationType = ROCF_NotificationType.rocfNT_excessiveDataBacklog;
    }

    @Override
    public synchronized void setEndOfData()
    {
        this.notificationType = ROCF_NotificationType.rocfNT_endOfData;
    }

    /**
     * @throws SleApiException
     * @FunctionSee specification of ISLE_Operation.@EndFunction
     */
    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        super.verifyInvocationArguments();
        if (this.notificationType == ROCF_NotificationType.rocfNT_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                               "Notification type"));
        }
        else if (this.notificationType == ROCF_NotificationType.rocfNT_lossFrameSync)
        {
            if (this.lossOfLockTime == null)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Loss of lock time"));
            }
            if (this.symbolSyncLock == ROCF_LockStatus.rocfLS_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Symbol synchronisation lock"));
            }
            if (this.symbolSyncLock == ROCF_LockStatus.rocfLS_notInUse)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                                   EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                   "Symbol synchronisation lock"));
            }
            if (this.subCarrierDemodLock == ROCF_LockStatus.rocfLS_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Sub-carrier demodulation lock"));
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
        }
        else if (this.notificationType == ROCF_NotificationType.rocfNT_productionStatusChange)
        {
            if (this.productionStatus == ROCF_ProductionStatus.rocfPS_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Notification type"));
            }
        }

    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_ROCF_SyncNotify ptmp = new EE_ROCF_SyncNotify(this);
        return ptmp;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder("");
        printOn(oss, maxDumpLength);

        oss.append("Notification Type      : " + this.notificationType + "\n");
        oss.append("Symbol Sync Lock       : " + this.symbolSyncLock + "\n");
        oss.append("Sub Carrier Demod Lock : " + this.subCarrierDemodLock + "\n");
        oss.append("Carrier Demod Lock     : " + this.carrierDemodLock + "\n");
        oss.append("Production Status      : " + this.productionStatus + "\n");
        oss.append("Loss Of Lock Time      : ");
        if (this.lossOfLockTime != null)
        {
            String str = this.lossOfLockTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_microSec);
            oss.append(str);
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
        else if (iid == IROCF_SyncNotify.class)
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
        return "EE_ROCF_SyncNotify [notificationType=" + this.notificationType + ", lossOfLockTime="
               + ((this.lossOfLockTime != null) ? this.lossOfLockTime : "") + ", carrierDemodLock="
               + this.carrierDemodLock + ", subCarrierDemodLock=" + this.subCarrierDemodLock + ", symbolSyncLock="
               + this.symbolSyncLock + ", productionStatus=" + this.productionStatus + "]";
    }

}
