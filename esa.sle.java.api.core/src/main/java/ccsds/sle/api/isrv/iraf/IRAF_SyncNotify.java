package ccsds.sle.api.isrv.iraf;

import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.iraf.types.RAF_LockStatus;
import ccsds.sle.api.isrv.iraf.types.RAF_NotificationType;
import ccsds.sle.api.isrv.iraf.types.RAF_ProductionStatus;

public interface IRAF_SyncNotify extends ISLE_Operation
{
    /**
     * Gets the notification type.
     * 
     * @return
     */
    RAF_NotificationType getNotificationType();

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
    RAF_LockStatus getCarrierDemodLock();

    /**
     * Gets the sub-carrier demodulation lock.
     * 
     * @return
     */
    RAF_LockStatus getSubCarrierDemodLock();

    /**
     * Gets the symbol sync lock.
     * 
     * @return
     */
    RAF_LockStatus getSymbolSyncLock();

    /**
     * Gets the production status.
     * 
     * @return
     */
    RAF_ProductionStatus getProductionStatus();

    /**
     * Sets the loss of frame sync.
     * 
     * @param time
     * @param symbolSyncLock
     * @param subCarrierDemodLock
     * @param carrierDemodLock
     */
    void setLossOfFrameSync(ISLE_Time time,
                            RAF_LockStatus symbolSyncLock,
                            RAF_LockStatus subCarrierDemodLock,
                            RAF_LockStatus carrierDemodLock);

    /**
     * Sets the production status change.
     * 
     * @param status
     */
    void setProductionStatusChange(RAF_ProductionStatus status);

    /**
     * Sets the discarded data.
     */
    void setDataDiscarded();

    /**
     * Sets the end of data.
     */
    void setEndOfData();

}
