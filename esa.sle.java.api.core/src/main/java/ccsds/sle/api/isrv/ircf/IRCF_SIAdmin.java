package ccsds.sle.api.isrv.ircf;

import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isrv.ircf.types.RCF_DeliveryMode;
import ccsds.sle.api.isrv.ircf.types.RCF_Gvcid;
import ccsds.sle.api.isrv.ircf.types.RCF_LockStatus;
import ccsds.sle.api.isrv.ircf.types.RCF_ProductionStatus;

public interface IRCF_SIAdmin extends IUnknown
{
	
    //
    // Setter methods
    //
	
    /**
     * Sets delivery mode.
     * 
     * @param mode
     */
    void setDeliveryMode(RCF_DeliveryMode mode);

    /**
     * Sets the latency limit.
     * 
     * @param limit
     */
    void setLatencyLimit(int limit);

    /**
     * Sets the transfer buffer size.
     * 
     * @param size
     */
    void setTransferBufferSize(long size);

    /**
     * Sets the permitted gvcid set.
     * 
     * @param idList
     */
    void setPermittedGvcidSet(RCF_Gvcid[] idList);

    /**
     * Sets the initial production status.
     * 
     * @param status
     */
    void setInitialProductionStatus(RCF_ProductionStatus status);

    /**
     * Sets the initial frame sync lock.
     * 
     * @param status
     */
    void setInitialFrameSyncLock(RCF_LockStatus status);

    /**
     * Sets the initial carrier demodulation lock.
     * 
     * @param status
     */
    void setInitialCarrierDemodLock(RCF_LockStatus status);

    /**
     * Sets the initial sub-carrier demodulation lock.
     * 
     * @param status
     */
    void setInitialSubCarrierDemodLock(RCF_LockStatus status);

    /**
     * Sets the initial symbol sync lock.
     * 
     * @param status
     */
    void setInitialSymbolSyncLock(RCF_LockStatus status);
    
    /**
     * Set the configured minimum reporting cycle
     * @since SLES V5.
     */
    void setMinimumReportCycle(long mrc);
    
    //
    // Getter methods
    //

    /**
     * Gets the delivery mode.
     * 
     * @return deliveryMode
     */
    RCF_DeliveryMode getDeliveryMode();

    /**
     * Gets the latency limit.
     * 
     * @return latency limit
     */
    int getLatencyLimit();

    /**
     * Gets the transfer buffer size.
     * 
     * @return transfer buffer size
     */
    long getTransferBufferSize();

    /**
     * Gets the permitted gvcid set.
     * 
     * @return permitted gvcid set as array.
     */
    RCF_Gvcid[] getPermittedGvcidSet();
    
    /**
     * Get the configured minimum reporting cycle
     * @since SLES V5.
     */
    long getMinimumReportCycle();

}
