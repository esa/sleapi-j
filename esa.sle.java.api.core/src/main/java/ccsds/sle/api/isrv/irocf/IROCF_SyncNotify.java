package ccsds.sle.api.isrv.irocf;

import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.irocf.types.ROCF_LockStatus;
import ccsds.sle.api.isrv.irocf.types.ROCF_NotificationType;
import ccsds.sle.api.isrv.irocf.types.ROCF_ProductionStatus;

/**
 * The interface provides access to the parameters of the unconfirmed operation
 * ROCF SYNC NOTIFY.
 * 
 * @version: 1.0, October 2015
 */
public interface IROCF_SyncNotify extends ISLE_Operation
{
    /**
     * Gets the notification type.
     * 
     * @return
     */
    ROCF_NotificationType getNotificationType();

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
    ROCF_LockStatus getCarrierDemodLock();

    /**
     * Gets the sub-carrier demodulation lock.
     * 
     * @return
     */
    ROCF_LockStatus getSubCarrierDemodLock();

    /**
     * Gets the symbol sync lock.
     * 
     * @return
     */
    ROCF_LockStatus getSymbolSyncLock();

    /**
     * Gets the production status.
     * 
     * @return
     */
    ROCF_ProductionStatus getProductionStatus();

    /**
     * Sets the loss of frame sync.
     * 
     * @param time
     * @param symbolSyncLock
     * @param subCarrierDemodLock
     * @param carrierDemodLock
     */
    void setLossOfFrameSync(ISLE_Time time,
                            ROCF_LockStatus symbolSyncLock,
                            ROCF_LockStatus subCarrierDemodLock,
                            ROCF_LockStatus carrierDemodLock);

    /**
     * Sets the production status change.
     * 
     * @param status
     */
    void setProductionStatusChange(ROCF_ProductionStatus status);

    /**
     * Sets the data discarded.
     */
    void setDataDiscarded();

    /**
     * Sets the end of data.
     */
    void setEndOfData();

}
