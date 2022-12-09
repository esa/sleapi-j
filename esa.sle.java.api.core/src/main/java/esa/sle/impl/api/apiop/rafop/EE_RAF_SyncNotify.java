/**
 * @(#) EE_RAF_SyncNotify.java
 */

package esa.sle.impl.api.apiop.rafop;

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
import ccsds.sle.api.isrv.iraf.IRAF_SyncNotify;
import ccsds.sle.api.isrv.iraf.types.RAF_LockStatus;
import ccsds.sle.api.isrv.iraf.types.RAF_NotificationType;
import ccsds.sle.api.isrv.iraf.types.RAF_ProductionStatus;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_Operation;

/**
 * @NameRAF SyncNotify Operation@EndName
 * @ResponsibilityThe class implements the RAF specific SyncNotify operation.@EndResponsibility
 */
public class EE_RAF_SyncNotify extends IEE_SLE_Operation implements IRAF_SyncNotify
{
    /**
     * The lock status of the symbol synchronisation process.
     */
    private RAF_LockStatus symbolSyncLock = RAF_LockStatus.rafLS_invalid;

    /**
     * The lock status of the sub-carrier demodulation process.
     */
    private RAF_LockStatus subCarrierDemodLock = RAF_LockStatus.rafLS_invalid;

    /**
     * The lock status of the carrier demodulation process.
     */
    private RAF_LockStatus carrierDemodLock = RAF_LockStatus.rafLS_invalid;

    /**
     * The type of notification.
     */
    private RAF_NotificationType notificationType = RAF_NotificationType.rafNT_invalid;

    /**
     * The time at which the frame synchronizer lost lock.
     */
    private ISLE_Time lossOfLockTime = null;

    /**
     * The production status.
     */
    private RAF_ProductionStatus productionStatus = RAF_ProductionStatus.rafPS_invalid;


    private EE_RAF_SyncNotify(final EE_RAF_SyncNotify right)
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

    public EE_RAF_SyncNotify(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnAllFrames, SLE_OpType.sleOT_syncNotify, version, false, preporter);
        this.symbolSyncLock = RAF_LockStatus.rafLS_invalid;
        this.subCarrierDemodLock = RAF_LockStatus.rafLS_invalid;
        this.carrierDemodLock = RAF_LockStatus.rafLS_invalid;
        this.notificationType = RAF_NotificationType.rafNT_invalid;
        this.lossOfLockTime = null;
        this.productionStatus = RAF_ProductionStatus.rafPS_invalid;
    }

    @Override
    public synchronized RAF_NotificationType getNotificationType()
    {
        return this.notificationType;
    }

    @Override
    public synchronized ISLE_Time getLossOfLockTime()
    {
        assert (this.notificationType == RAF_NotificationType.rafNT_lossFrameSync) : "incorrect notifytype";
        if (this.lossOfLockTime != null)
        {
            return this.lossOfLockTime;
        }
        return null;
    }

    @Override
    public synchronized RAF_LockStatus getCarrierDemodLock()
    {
        assert (this.notificationType == RAF_NotificationType.rafNT_lossFrameSync) : "incorrect notifytype";
        return this.carrierDemodLock;
    }

    @Override
    public synchronized RAF_LockStatus getSubCarrierDemodLock()
    {
        assert (this.notificationType == RAF_NotificationType.rafNT_lossFrameSync) : "incorrect notifytype";
        return this.subCarrierDemodLock;

    }

    @Override
    public synchronized RAF_LockStatus getSymbolSyncLock()
    {
        assert (this.notificationType == RAF_NotificationType.rafNT_lossFrameSync) : "incorrect notifytype";
        return this.symbolSyncLock;
    }

    @Override
    public synchronized RAF_ProductionStatus getProductionStatus()
    {
        assert (this.notificationType == RAF_NotificationType.rafNT_productionStatusChange) : "incorrect notificationType ";
        return this.productionStatus;
    }

    @Override
    public synchronized void setLossOfFrameSync(ISLE_Time time,
                                                RAF_LockStatus symbolSyncLock,
                                                RAF_LockStatus subCarrierDemodLock,
                                                RAF_LockStatus carrierDemodLock)
    {
        if (this.lossOfLockTime != null)
        {
            this.lossOfLockTime = null;
        }
        this.lossOfLockTime = time.copy();
        this.symbolSyncLock = symbolSyncLock;
        this.subCarrierDemodLock = subCarrierDemodLock;
        this.carrierDemodLock = carrierDemodLock;
        this.notificationType = RAF_NotificationType.rafNT_lossFrameSync;
    }

    @Override
    public synchronized void setProductionStatusChange(RAF_ProductionStatus status)
    {
        this.notificationType = RAF_NotificationType.rafNT_productionStatusChange;
        this.productionStatus = status;
    }

    @Override
    public synchronized void setDataDiscarded()
    {
        this.notificationType = RAF_NotificationType.rafNT_excessiveDataBacklog;
    }

    @Override
    public synchronized void setEndOfData()
    {
        this.notificationType = RAF_NotificationType.rafNT_endOfData;
    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_RAF_SyncNotify ptmp = new EE_RAF_SyncNotify(this);
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

    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {

        super.verifyInvocationArguments();

        if (this.notificationType == RAF_NotificationType.rafNT_invalid)
        {
            throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
        }
        else if (this.notificationType == RAF_NotificationType.rafNT_lossFrameSync)
        {
            if (this.lossOfLockTime == null)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.symbolSyncLock == RAF_LockStatus.rafLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.symbolSyncLock == RAF_LockStatus.rafLS_notInUse)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            if (this.subCarrierDemodLock == RAF_LockStatus.rafLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.carrierDemodLock == RAF_LockStatus.rafLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.carrierDemodLock == RAF_LockStatus.rafLS_notInUse)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
        }
        else if (this.notificationType == RAF_NotificationType.rafNT_productionStatusChange)
        {
            if (this.productionStatus == RAF_ProductionStatus.rafPS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
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
        else if (iid == IRAF_SyncNotify.class)
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
        return "EE_RAF_SyncNotify [symbolSyncLock=" + this.symbolSyncLock + ", subCarrierDemodLock="
               + this.subCarrierDemodLock + ", carrierDemodLock=" + this.carrierDemodLock + ", notificationType="
               + this.notificationType + ", lossOfLockTime="
               + ((this.lossOfLockTime != null) ? this.lossOfLockTime : "") + ", productionStatus="
               + this.productionStatus + "]";
    }

}
