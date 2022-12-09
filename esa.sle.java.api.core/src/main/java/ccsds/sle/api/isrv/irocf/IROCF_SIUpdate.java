package ccsds.sle.api.isrv.irocf;

import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isrv.irocf.types.ROCF_ControlWordType;
import ccsds.sle.api.isrv.irocf.types.ROCF_Gvcid;
import ccsds.sle.api.isrv.irocf.types.ROCF_LockStatus;
import ccsds.sle.api.isrv.irocf.types.ROCF_ProductionStatus;
import ccsds.sle.api.isrv.irocf.types.ROCF_UpdateMode;

/**
 * The interface provides methods to update parameters that shall be reported to
 * the service user via the operation STATUS REPORT. In order to keep this
 * information up to date the appropriate methods of this interface must be
 * called whenever the information changes, independent of the state of the
 * service instance. The interface provides read access to the parameters set
 * via this interface and to parameters accumulated or derived by the API
 * according to the specifications in section 3.1. The API sets the parameters
 * to the initial values specified at the end of this section when the service
 * instance is configured. Parameter values retrieved before configuration are
 * undefined. In the delivery mode 'offline', status reporting is not supported.
 * Therefore configuration parameters used to initialise the status report need
 * not be supplied and the status information need not be updated. If the
 * initial values and updates are not supplied, the retrieval methods return the
 * values defined at the end of this section. Values accumulated by the service
 * element are kept up to date for all delivery modes, including the mode
 * 'offline'.
 * 
 * @version: 1.0, October 2015
 */
public interface IROCF_SIUpdate extends IUnknown
{
    /**
     * Sets the number of frames processed.
     * 
     * @param count
     */
    void setNumFramesProcessed(long count);

    /**
     * Sets the production status.
     * 
     * @param status
     */
    void setProductionStatus(ROCF_ProductionStatus status);

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
     * Sets the sub-carrier demodulation lock.
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
     * Gets the production status.
     * 
     * @return
     */
    ROCF_ProductionStatus getProductionStatus();

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
     * Gets the number of frames processed.
     * 
     * @return
     */
    long getNumFramesProcessed();

    /**
     * Gets the number ocf delivered.
     * 
     * @return
     */
    long getNumOcfDelivered();

    /**
     * Gets requested gvcid.
     * 
     * @return
     */
    ROCF_Gvcid getRequestedGvcid();

    /**
     * Gets the requested control word type.
     * 
     * @return
     */
    ROCF_ControlWordType getRequestedControlWordType();

    /**
     * Gets the used tc vcid.
     * 
     * @return
     */
    boolean getTcVcidUsed();

    /**
     * Gets the requested tc vcid.
     * 
     * @return
     */
    long getRequestedTcVcid();

    /**
     * Sets the requested update mode.
     * 
     * @return
     */
    ROCF_UpdateMode getRequestedUpdateMode();

}
