package ccsds.sle.api.isrv.ircf;

import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.ircf.types.RCF_LockStatus;
import ccsds.sle.api.isrv.ircf.types.RCF_NotificationType;
import ccsds.sle.api.isrv.ircf.types.RCF_ProductionStatus;

public interface IRCF_SyncNotify extends ISLE_Operation
{
    /**
     * Gets the notification type.
     * 
     * @return
     */
    RCF_NotificationType getNotificationType();

    /**
     * Gets the loss of lock time.
     * 
     * @return
     */
    ISLE_Time getLossOfLockTime();

    /**
     * Gets the carrier demodulation lock.
     * 
     * @return
     */
    RCF_LockStatus getCarrierDemodLock();

    /**
     * Gets the sub-carrier demodulation lock.
     * 
     * @return
     */
    RCF_LockStatus getSubCarrierDemodLock();

    /**
     * Get symbol sync lock.
     * 
     * @return
     */
    RCF_LockStatus getSymbolSyncLock();

    /**
     * Gets production status.
     * 
     * @return
     */
    RCF_ProductionStatus getProductionStatus();

    /**
     * Sets the loss of frame sync.
     * 
     * @param time
     * @param symbolSyncLock
     * @param subCarrierDemodLock
     * @param carrierDemodLock
     */
    void setLossOfFrameSync(ISLE_Time time,
                            RCF_LockStatus symbolSyncLock,
                            RCF_LockStatus subCarrierDemodLock,
                            RCF_LockStatus carrierDemodLock);

    /**
     * Sets the production status change.
     * 
     * @param status
     */
    void setProductionStatusChange(RCF_ProductionStatus status);

    /**
     * Sets the discarded data.
     */
    void setDataDiscarded();

    /**
     * Sets the end of data.
     */
    void setEndOfData();

}
