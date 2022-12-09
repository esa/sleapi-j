package ccsds.sle.api.isrv.irocf;

import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isrv.irocf.types.ROCF_ControlWordType;
import ccsds.sle.api.isrv.irocf.types.ROCF_DeliveryMode;
import ccsds.sle.api.isrv.irocf.types.ROCF_Gvcid;
import ccsds.sle.api.isrv.irocf.types.ROCF_LockStatus;
import ccsds.sle.api.isrv.irocf.types.ROCF_ProductionStatus;
import ccsds.sle.api.isrv.irocf.types.ROCF_UpdateMode;

/**
 * The interface provides write and read access to the ROCF-specific service
 * instance configuration-parameters. All configuration parameters must be set
 * as part of service instance configuration. When the method ConfigCompleted()
 * is called on the interface ISLE_SIAdmin, the service element checks that all
 * required parameters have been set and returns an error when the configuration
 * is not complete. Configuration parameters must not be set after successful
 * return of the method ConfigCompleted(). The effect of invoking these methods
 * at a later stage is undefined. As a convenience for the application, the
 * interface provides read access to the configuration parameters, except for
 * parameters used to initialize the status report. If retrieval methods are
 * called before configuration, the value returned is undefined.
 * 
 * @version: 1.0, October 2015
 */
public interface IROCF_SIAdmin extends IUnknown
{
    /**
     * Sets the delivery mode.
     * 
     * @param mode
     */
    void setDeliveryMode(ROCF_DeliveryMode mode);

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
    void setPermittedGvcidSet(ROCF_Gvcid[] idList);

    /**
     * Sets the permitted control word type set.
     * 
     * @param typeSet
     */
    void setPermittedControlWordTypeSet(ROCF_ControlWordType[] typeSet);

    /**
     * Sets the permitted tc vcid set.
     * 
     * @param idSet
     */
    void setPermittedTcVcidSet(final long[] idSet);

    /**
     * Sets the permitted update mode set.
     * 
     * @param modeSet
     */
    void setPermittedUpdateModeSet(final ROCF_UpdateMode[] modeSet);

    /**
     * Sets the initial production status.
     * 
     * @param status
     */
    void setInitialProductionStatus(ROCF_ProductionStatus status);

    /**
     * Sets the initial frame sync lock.
     * 
     * @param status
     */
    void setInitialFrameSyncLock(ROCF_LockStatus status);

    /**
     * Sets the initial carrier demodulation lock.
     * 
     * @param status
     */
    void setInitialCarrierDemodLock(ROCF_LockStatus status);

    /**
     * Sets the initial sub-carrier demodulation lock.
     * 
     * @param status
     */
    void setInitialSubCarrierDemodLock(ROCF_LockStatus status);

    /**
     * Sets the initial Symbol sync lock.
     * 
     * @param status
     */
    void setInitialSymbolSyncLock(ROCF_LockStatus status);
    
    /**
     * Set the configured minimum reporting cycle
     * @since SLES V5.
     */
    void setMinimumReportCycle(long mrc);

    /**
     * Gets the delivery mode.
     * 
     * @return
     */
    ROCF_DeliveryMode getDeliveryMode();

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
     * Gets the permitted gvcid set.
     * 
     * @return
     */
    ROCF_Gvcid[] getPermittedGvcidSet();

    /**
     * Gets the permitted word type set.
     * 
     * @return
     */
    ROCF_ControlWordType[] getPermittedControlWordTypeSet();

    /**
     * Gets the permitted tc vcid set.
     * 
     * @return
     */
    long[] getPermittedTcVcidSet();

    /**
     * Gets the permitted update mode set.
     * 
     * @return
     */
    ROCF_UpdateMode[] getPermittedUpdateModeSet();

    /**
     * Get the configured minimum reporting cycle
     * @since SLES V5.
     */
    long getMinimumReportCycle();
}
