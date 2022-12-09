package ccsds.sle.api.isrv.iraf;

import java.util.List;

import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isrv.iraf.types.RAF_DeliveryMode;
import ccsds.sle.api.isrv.iraf.types.RAF_LockStatus;
import ccsds.sle.api.isrv.iraf.types.RAF_ParFrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_ProductionStatus;
import ccsds.sle.api.isrv.iraf.types.RAF_RequestedFrameQuality;

public interface IRAF_SIAdmin extends IUnknown
{
    /**
     * Sets the delivery mode.
     * 
     * @param mode
     */
    void setDeliveryMode(RAF_DeliveryMode mode);

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
     * Sets the initial production status.
     * 
     * @param status
     */
    void setInitialProductionStatus(RAF_ProductionStatus status);

    /**
     * Sets the initial frame sync lock.
     * 
     * @param status
     */
    void setInitialFrameSyncLock(RAF_LockStatus status);

    /**
     * Sets the initial carrier demodulation lock.
     * 
     * @param status
     */
    void setInitialCarrierDemodLock(RAF_LockStatus status);

    /**
     * Sets the initial sub- carrier demodulation lock.
     * 
     * @param status
     */
    void setInitialSubCarrierDemodLock(RAF_LockStatus status);

    /**
     * Sets the initial symbol sync lock.
     * 
     * @param status
     */
    void setInitialSymbolSyncLock(RAF_LockStatus status);

    /**
     * Gets the delivery mode.
     * 
     * @return
     */
    RAF_DeliveryMode getDeliveryMode();

    /**
     * Gets the latency limit.
     * 
     * @return
     */
    int getLatencyLimit();

    /**
     * Gets the transfer buffer size.
     * 
     * @return
     */
    long getTransferBufferSize();

    /**
     * Set the configured permitted frame quality
     * @since SLES V5.
     */
    void setPermittedFrameQuality(RAF_ParFrameQuality[] frameQualities);
    
    /**
     * Set the configured minimum reporting cycle
     * @since SLES V5.
     */
    void setMinimumReportCycle(long mrc);
    
    /**
     * Get the configured permitted frame quality
     * @since SLES V5.
     * @return
     */
    RAF_ParFrameQuality[] getPermittedFrameQuality();
    
    /**
     * Get the configured minimum reporting cycle
     * @since SLES V5.
     */
    long getMinimumReportCycle();
}
