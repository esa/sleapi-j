package ccsds.sle.api.isrv.iraf;

import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isrv.iraf.types.RAF_LockStatus;
import ccsds.sle.api.isrv.iraf.types.RAF_ProductionStatus;

public interface IRAF_StatusReport extends ISLE_Operation
{
    /**
     * Gets the number error free frames.
     * 
     * @return
     */
    long getNumErrorFreeFrames();

    /**
     * Gets the number frames.
     * 
     * @return
     */
    long getNumFrames();

    /**
     * Gets the frame sync lock.
     * 
     * @return
     */
    RAF_LockStatus getFrameSyncLock();

    /**
     * Gets the carrier demodulation lock
     * 
     * @return
     */
    RAF_LockStatus getCarrierDemodLock();

    /**
     * Gets the sub carrier demodulation lock.
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
     * Sets the number of error free frames.
     * 
     * @param count
     */
    void setNumErrorFreeFrames(long count);

    /**
     * Sets the number of frames.
     * 
     * @param count
     */
    void setNumFrames(long count);

    /**
     * Sets the frame sync lock.
     * 
     * @param status
     */
    void setFrameSyncLock(RAF_LockStatus status);

    /**
     * Set the carrier demodulation lock.
     * 
     * @param status
     */
    void setCarrierDemodLock(RAF_LockStatus status);

    /**
     * Sets the sub carrier demodulation lock.
     * 
     * @param status
     */
    void setSubCarrierDemodLock(RAF_LockStatus status);

    /**
     * Sets the symbol sync lock.
     * 
     * @param status
     */
    void setSymbolSyncLock(RAF_LockStatus status);

    /**
     * Sets the production status.
     * 
     * @param status
     */
    void setProductionStatus(RAF_ProductionStatus status);

}
