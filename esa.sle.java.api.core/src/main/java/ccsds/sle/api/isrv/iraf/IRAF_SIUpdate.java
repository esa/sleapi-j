package ccsds.sle.api.isrv.iraf;


import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isrv.iraf.types.RAF_LockStatus;
import ccsds.sle.api.isrv.iraf.types.RAF_ParFrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_ProductionStatus;

public interface IRAF_SIUpdate extends IUnknown
{
    /**
     * Sets the production status.
     * 
     * @param status
     */
    void setProductionStatus(RAF_ProductionStatus status);

    /**
     * Sets the frame sync lock.
     * 
     * @param status
     */
    void setFrameSyncLock(RAF_LockStatus status);

    /**
     * Sets the carrier demodulation lock.
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
     * Gets the production status.
     * 
     * @return
     */
    RAF_ProductionStatus getProductionStatus();

    /**
     * Gets the frame sync lock.
     * 
     * @return
     */
    RAF_LockStatus getFrameSyncLock();

    /**
     * Gets the carrier demodulation lock.
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
     * Gets the number of error free frames.
     * 
     * @return
     */
    long getNumErrorFreeFrames();

    /**
     * Gets the number of frames.
     * 
     * @return
     */
    long getNumFrames();

    /**
     * Gets the requested frame quality.
     * 
     * @return
     */
    RAF_ParFrameQuality getRequestedFrameQuality();

}
