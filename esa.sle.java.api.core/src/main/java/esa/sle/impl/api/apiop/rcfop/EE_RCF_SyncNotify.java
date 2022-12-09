/**
 * @(#) EE_RCF_SyncNotify.java
 */

package esa.sle.impl.api.apiop.rcfop;

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
import ccsds.sle.api.isrv.ircf.IRCF_SyncNotify;
import ccsds.sle.api.isrv.ircf.types.RCF_LockStatus;
import ccsds.sle.api.isrv.ircf.types.RCF_NotificationType;
import ccsds.sle.api.isrv.ircf.types.RCF_ProductionStatus;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_Operation;

/**
 * @ResponsibilityThe class implements the RCF specific SyncNotify operation.@EndResponsibility
 */
public class EE_RCF_SyncNotify extends IEE_SLE_Operation implements IRCF_SyncNotify
{
    /**
     * The type of notification.
     */
    private RCF_NotificationType notificationType = RCF_NotificationType.rcfNT_invalid;

    /**
     * The time at which the frame synchroniser lost lock.
     */
    private ISLE_Time lossOfLockTime = null;

    /**
     * The lock status of the carrier demodulation process.
     */
    private RCF_LockStatus carrierDemodLock = RCF_LockStatus.rcfLS_invalid;

    /**
     * The lock status of the sub-carrier demodulation process.
     */
    private RCF_LockStatus subCarrierDemodLock = RCF_LockStatus.rcfLS_invalid;

    /**
     * The lock status of the symbol synchronisation process.
     */
    private RCF_LockStatus symbolSyncLock = RCF_LockStatus.rcfLS_invalid;

    /**
     * The production status.
     */
    private RCF_ProductionStatus productionStatus = RCF_ProductionStatus.rcfPS_invalid;


    private EE_RCF_SyncNotify(final EE_RCF_SyncNotify right)
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

    public EE_RCF_SyncNotify(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnChFrames, SLE_OpType.sleOT_syncNotify, version, false, preporter);
        this.notificationType = RCF_NotificationType.rcfNT_invalid;
        this.lossOfLockTime = null;
        this.carrierDemodLock = RCF_LockStatus.rcfLS_invalid;
        this.subCarrierDemodLock = RCF_LockStatus.rcfLS_invalid;
        this.symbolSyncLock = RCF_LockStatus.rcfLS_invalid;
        this.productionStatus = RCF_ProductionStatus.rcfPS_invalid;
    }

    @Override
    public synchronized RCF_NotificationType getNotificationType()
    {
        return this.notificationType;
    }

    @Override
    public synchronized ISLE_Time getLossOfLockTime()
    {
        assert (this.notificationType == RCF_NotificationType.rcfNT_lossFrameSync) : "incorrect notifytype";
        return this.lossOfLockTime;
    }

    @Override
    public synchronized RCF_LockStatus getCarrierDemodLock()
    {
        assert (this.notificationType == RCF_NotificationType.rcfNT_lossFrameSync) : "incorrect notifytype";
        return this.carrierDemodLock;
    }

    @Override
    public synchronized RCF_LockStatus getSubCarrierDemodLock()
    {
        assert (this.notificationType == RCF_NotificationType.rcfNT_lossFrameSync) : "incorrect notifytype";
        return this.subCarrierDemodLock;
    }

    @Override
    public synchronized RCF_LockStatus getSymbolSyncLock()
    {
        assert (this.notificationType == RCF_NotificationType.rcfNT_lossFrameSync) : "incorrect notifytype";
        return this.symbolSyncLock;
    }

    @Override
    public synchronized RCF_ProductionStatus getProductionStatus()
    {
        assert (this.notificationType == RCF_NotificationType.rcfNT_productionStatusChange) : "incorrect notificationType ";
        return this.productionStatus;
    }

    @Override
    public synchronized void setLossOfFrameSync(ISLE_Time time,
                                                RCF_LockStatus symbolSyncLock,
                                                RCF_LockStatus subCarrierDemodLock,
                                                RCF_LockStatus carrierDemodLock)
    {
        if (this.lossOfLockTime != null)
        {
            this.lossOfLockTime = null;
        }
        this.lossOfLockTime = time.copy();
        this.symbolSyncLock = symbolSyncLock;
        this.subCarrierDemodLock = subCarrierDemodLock;
        this.carrierDemodLock = carrierDemodLock;
        this.notificationType = RCF_NotificationType.rcfNT_lossFrameSync;
    }

    @Override
    public synchronized void setProductionStatusChange(RCF_ProductionStatus status)
    {
        this.notificationType = RCF_NotificationType.rcfNT_productionStatusChange;
        this.productionStatus = status;
    }

    @Override
    public synchronized void setDataDiscarded()
    {
        this.notificationType = RCF_NotificationType.rcfNT_excessiveDataBacklog;
    }

    @Override
    public synchronized void setEndOfData()
    {
        this.notificationType = RCF_NotificationType.rcfNT_endOfData;
    }

    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        super.verifyInvocationArguments();
        if (this.notificationType == RCF_NotificationType.rcfNT_invalid)
        {
            throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
        }
        else if (this.notificationType == RCF_NotificationType.rcfNT_lossFrameSync)
        {
            if (this.lossOfLockTime == null)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.symbolSyncLock == RCF_LockStatus.rcfLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.symbolSyncLock == RCF_LockStatus.rcfLS_notInUse)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            if (this.subCarrierDemodLock == RCF_LockStatus.rcfLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.carrierDemodLock == RCF_LockStatus.rcfLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.carrierDemodLock == RCF_LockStatus.rcfLS_notInUse)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
        }
        else if (this.notificationType == RCF_NotificationType.rcfNT_productionStatusChange)
        {
            if (this.productionStatus == RCF_ProductionStatus.rcfPS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
        }

    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_RCF_SyncNotify ptmp = new EE_RCF_SyncNotify(this);
        return ptmp;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder();
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
        else if (iid == IRCF_SyncNotify.class)
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
        return "EE_RCF_SyncNotify [notificationType=" + this.notificationType + ", lossOfLockTime="
               + ((this.lossOfLockTime != null) ? this.lossOfLockTime : "") + ", carrierDemodLock="
               + this.carrierDemodLock + ", subCarrierDemodLock=" + this.subCarrierDemodLock + ", symbolSyncLock="
               + this.symbolSyncLock + ", productionStatus=" + this.productionStatus + "]";
    }

}
