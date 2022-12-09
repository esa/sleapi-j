package ccsds.sle.api.isrv.ircf;

import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isrv.ircf.types.RCF_LockStatus;
import ccsds.sle.api.isrv.ircf.types.RCF_ProductionStatus;

public interface IRCF_StatusReport extends ISLE_Operation
{
    /**
     * Gets the number of frames.
     * 
     * @return
     */
    long getNumFrames();

    /**
     * Gets the frame sync lock.
     * 
     * @return
     */
    RCF_LockStatus getFrameSyncLock();

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
     * Gets the symbol sync lock.
     * 
     * @return
     */
    RCF_LockStatus getSymbolSyncLock();

    /**
     * Gets the production status.
     * 
     * @return
     */
    RCF_ProductionStatus getProductionStatus();

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
    void setFrameSyncLock(RCF_LockStatus status);

    /**
     * Sets carrier demodulation lock.
     * 
     * @param status
     */
    void setCarrierDemodLock(RCF_LockStatus status);

    /**
     * Sets the sub-carrier demodulation lock.
     * 
     * @param status
     */
    void setSubCarrierDemodLock(RCF_LockStatus status);

    /**
     * Sets the symbol sync lock.
     * 
     * @param status
     */
    void setSymbolSyncLock(RCF_LockStatus status);

    /**
     * Sets the production status.
     * 
     * @param status
     */
    void setProductionStatus(RCF_ProductionStatus status);

}
