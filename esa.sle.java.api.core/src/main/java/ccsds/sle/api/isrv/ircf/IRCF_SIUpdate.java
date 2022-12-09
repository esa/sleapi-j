package ccsds.sle.api.isrv.ircf;

import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isrv.ircf.types.RCF_Gvcid;
import ccsds.sle.api.isrv.ircf.types.RCF_LockStatus;
import ccsds.sle.api.isrv.ircf.types.RCF_ProductionStatus;

public interface IRCF_SIUpdate extends IUnknown
{
    /**
     * Sets the production status.
     * 
     * @param status obj
     */
    void setProductionStatus(RCF_ProductionStatus status);

    /**
     * Sets the frame sync lock.
     * 
     * @param status obj
     */
    void setFrameSyncLock(RCF_LockStatus status);

    /**
     * Sets the carrier demodulation lock.
     * 
     * @param status obj
     */
    void setCarrierDemodLock(RCF_LockStatus status);

    /**
     * Sets the sub-carrier demodulation lock.
     * 
     * @param status obj
     */
    void setSubCarrierDemodLock(RCF_LockStatus status);

    /**
     * Sets the symbol sync lock.
     * 
     * @param status obj
     */
    void setSymbolSyncLock(RCF_LockStatus status);

    /**
     * Gets the production status.
     * 
     * @return
     */
    RCF_ProductionStatus getProductionStatus();

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
     * Gets the sub-currier demodulation lock.
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
     * Gets the number of frames.
     * 
     * @return
     */
    long getNumFrames();

    /**
     * Gets the requested gvcid.
     * 
     * @return
     */
    RCF_Gvcid getRequestedGvcid();

}
