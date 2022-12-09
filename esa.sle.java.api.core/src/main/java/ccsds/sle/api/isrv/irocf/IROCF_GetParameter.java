package ccsds.sle.api.isrv.irocf;

import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isrv.irocf.types.ROCF_ControlWordType;
import ccsds.sle.api.isrv.irocf.types.ROCF_DeliveryMode;
import ccsds.sle.api.isrv.irocf.types.ROCF_GetParameterDiagnostic;
import ccsds.sle.api.isrv.irocf.types.ROCF_Gvcid;
import ccsds.sle.api.isrv.irocf.types.ROCF_ParameterName;
import ccsds.sle.api.isrv.irocf.types.ROCF_UpdateMode;

/**
 * The interface provides access to the parameters of the confirmed operation
 * ROCF GET PARAMETER.
 * 
 * @version: 1.0, October 2015
 */
public interface IROCF_GetParameter extends ISLE_ConfirmedOperation
{
    /**
     * Gets the requested parameter.
     * 
     * @return
     */
    ROCF_ParameterName getRequestedParameter();

    /**
     * Gets the returned parameter.
     * 
     * @return
     */
    ROCF_ParameterName getReturnedParameter();

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
     * Gets the min reporting cycle (para ID 301).
     * @since SLES V.5
     */
	long getMinimumReportingCycle();

    /**
     * Gets the transfer buffer size.
     * 
     * @return
     */
    long getTransferBufferSize();

    /**
     * Gets the requested gvcid.
     * 
     * @return
     */
    ROCF_Gvcid getRequestedGvcid();

    /**
     * Gets the permitted gvcid set.
     * 
     * @return
     */
    ROCF_Gvcid[] getPermittedGvcidSet();

    /**
     * Removes the permitted gvcid set.
     * 
     * @param size
     * @return
     */
    ROCF_Gvcid[] removePermittedGvcidSet(long size);

    /**
     * Gets requested control word type.
     * 
     * @return
     */
    ROCF_ControlWordType getRequestedControlWordType();

    /**
     * Removes the permitted control word type set.
     * 
     * @param size
     * @return
     */
    ROCF_ControlWordType[] removePermittedControlWordTypeSet(long size);

    /**
     * Gets the permitted control word type set.
     * 
     * @return
     */
    ROCF_ControlWordType[] getPermittedControlWordTypeSet();

    /**
     * Gets the used tc vcit.
     * 
     * @return
     */
    boolean getTcVcidUsed();

    /**
     * Gets requested tc vcid.
     * 
     * @return
     */
    long getRequestedTcVcid();

    /**
     * Gets the permitted tc vcid set.
     * 
     * @return
     */
    long[] getPermittedTcVcidSet();

    /**
     * Removes the permitted tc vcid set.
     * 
     * @return
     */
    long[] removePermittedTcVcidSet();

    /**
     * Gets the requested update mode.
     * 
     * @return
     */
    ROCF_UpdateMode getRequestedUpdateMode();

    /**
     * Gets the permitted update mode set.
     * 
     * @return
     */
    ROCF_UpdateMode[] getPermittedUpdateModeSet();

    /**
     * Removes permitted update mode set.
     * 
     * @return
     */
    ROCF_UpdateMode[] removePermittedUpdateModeSet();

    /**
     * Gets the reporting cycle.
     * 
     * @return
     */
    long getReportingCycle();

    /**
     * Gets the return timeout period.
     * 
     * @return
     */
    long getReturnTimeoutPeriod();

    /**
     * Gets the get parameter diagnostic.
     * 
     * @return
     */
    ROCF_GetParameterDiagnostic getGetParameterDiagnostic();

    /**
     * Sets the requested parameter.
     * 
     * @param name
     */
    void setRequestedParameter(ROCF_ParameterName name);

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
     * Sets the min reporting cycle (para ID 301).
     * @since SLES V.5
     * @param mrc
     */
	void setMinimumReportingCycle(long mrc);

    /**
     * Sets the transfer buffer size.
     * 
     * @param size
     */
    void setTransferBufferSize(long size);

    /**
     * Sets the requested gvcid.
     * 
     * @param id
     */
    void setRequestedGvcid(ROCF_Gvcid id);

    /**
     * Puts the requested gvcid.
     * 
     * @param pid
     */
    void putRequestedGvcid(ROCF_Gvcid pid);

    /**
     * Sets the permitted gvcid set.
     * 
     * @param idList
     */
    void setPermittedGvcidSet(ROCF_Gvcid[] idList);

    /**
     * Puts the permitted gvcid set.
     * 
     * @param idList
     */
    void putPermittedGvcidSet(ROCF_Gvcid[] idList);

    /**
     * Sets the requested control word type.
     * 
     * @param type
     */
    void setRequestedControlWordType(ROCF_ControlWordType type);

    /**
     * Sets the permitted control word type set.
     * 
     * @param typeSet
     */
    void setPermittedControlWordTypeSet(ROCF_ControlWordType[] typeSet);

    /**
     * Puts the permitted control word type set.
     * 
     * @param typeSet
     */
    void putPermittedControlWordTypeSet(ROCF_ControlWordType[] typeSet);

    /**
     * Sets the requested tc vcid.
     * 
     * @param id
     */
    void setRequestedTcVcid(long id);

    /**
     * Sets the permitted tc vcid set.
     * 
     * @param idSet
     */
    void setPermittedTcVcidSet(long[] idSet);

    /**
     * Puts the permitted tc vcid set.
     * 
     * @param idSet
     */
    void putPermittedTcVcidSet(long[] idSet);

    /**
     * Sets requested update mode.
     * 
     * @param mode
     */
    void setRequestedUpdateMode(ROCF_UpdateMode mode);

    /**
     * Sets the permitted update mode set.
     * 
     * @param modeSet
     */
    void setPermittedUpdateModeSet(ROCF_UpdateMode[] modeSet);

    /**
     * Puts the permitted update mode set.
     * 
     * @param modeSet
     */
    void putPermittedUpdateModeSet(ROCF_UpdateMode[] modeSet);

    /**
     * Sets the reporting cycle.
     * 
     * @param cycle
     */
    void setReportingCycle(long cycle);

    /**
     * Sets the return timeout period.
     * 
     * @param period
     */
    void setReturnTimeoutPeriod(long period);

    /**
     * Sets the get parameter diagnostic.
     * 
     * @param diagostic
     */
    void setGetParameterDiagnostic(ROCF_GetParameterDiagnostic diagostic);

}
