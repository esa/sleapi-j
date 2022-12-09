package ccsds.sle.api.isrv.irocf;

import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isrv.irocf.types.ROCF_LockStatus;
import ccsds.sle.api.isrv.irocf.types.ROCF_ProductionStatus;

/**
 * The interface provides access to the parameters of the unconfirmed operation
 * ROCF STATUS REPORT.
 * 
 * @version: 1.0, October 2015
 */
public interface IROCF_StatusReport extends ISLE_Operation
{
    /**
     * Gets the number of frames.
     * 
     * @return
     */
    long getNumFrames();

    /**
     * Gets the number ocf delivered.
     * 
     * @return
     */
    long getNumOcfDelivered();

    /**
     * Gets the frame sync lock.
     * 
     * @return
     */
    ROCF_LockStatus getFrameSyncLock();

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
     * Sets the number of frames.
     * 
     * @param count
     */
    void setNumFrames(long count);

    /**
     * Sets the number ocf delivered.
     * 
     * @param count
     */
    void setNumOcfDelivered(long count);

    /**
     * Sets the frame sync lock.
     * 
     * @param status
     */
    void setFrameSyncLock(ROCF_LockStatus status);

    /**
     * Sets the carrier demodulation lock.
     * 
     * @param status
     */
    void setCarrierDemodLock(ROCF_LockStatus status);

    /**
     * Sets sub-carrier demodulation lock.
     * 
     * @param status
     */
    void setSubCarrierDemodLock(ROCF_LockStatus status);

    /**
     * Sets the symbol sync lock.
     * 
     * @param status
     */
    void setSymbolSyncLock(ROCF_LockStatus status);

    /**
     * Sets the production status.
     * 
     * @param status
     */
    void setProductionStatus(ROCF_ProductionStatus status);

}
