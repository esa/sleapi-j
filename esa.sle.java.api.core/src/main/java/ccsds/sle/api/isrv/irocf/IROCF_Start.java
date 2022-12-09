/**
 * @(#) IROCF_Start.java
 */

package ccsds.sle.api.isrv.irocf;

import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.irocf.types.ROCF_ControlWordType;
import ccsds.sle.api.isrv.irocf.types.ROCF_Gvcid;
import ccsds.sle.api.isrv.irocf.types.ROCF_StartDiagnostic;
import ccsds.sle.api.isrv.irocf.types.ROCF_UpdateMode;

/**
 * The interface provides access to the parameters of the confirmed operation
 * ROCF- START.
 * 
 * @version: 1.0, October 2015
 */
public interface IROCF_Start extends ISLE_ConfirmedOperation
{
    /**
     * Gets the start time.
     * 
     * @return
     */
    ISLE_Time getStartTime();

    /**
     * Gets the stop time.
     * 
     * @return
     */
    ISLE_Time getStopTime();

    /**
     * Gets the gvcid.
     * 
     * @return
     */
    ROCF_Gvcid getGvcid();

    /**
     * Gets the control word type.
     * 
     * @return
     */
    ROCF_ControlWordType getControlWordType();

    /**
     * gets the used tc vcid.
     * 
     * @return
     */
    boolean getTcVcidUsed();

    /**
     * Gets the tc vcid.
     * 
     * @return
     */
    long getTcVcid();

    /**
     * Gets the update mode.
     * 
     * @return
     */
    ROCF_UpdateMode getUpdateMode();

    /**
     * Gets the start diagnostic.
     * 
     * @return
     */
    ROCF_StartDiagnostic getStartDiagnostic();

    /**
     * Sets the start time.
     * 
     * @param time
     */
    void setStartTime(ISLE_Time time);

    /**
     * Puts the start time.
     * 
     * @param ptime
     */
    void putStartTime(ISLE_Time ptime);

    /**
     * Sets the stop time.
     * 
     * @param time
     */
    void setStopTime(ISLE_Time time);

    /**
     * Puts the stop time.
     * 
     * @param ptime
     */
    void putStopTime(ISLE_Time ptime);

    /**
     * Sets the gvcid.
     * 
     * @param id
     */
    void setGvcid(ROCF_Gvcid id);

    /**
     * Puts the gvcid.
     * 
     * @param pid
     */
    void putGvcid(ROCF_Gvcid pid);

    /**
     * Sets the control word type.
     * 
     * @param type
     */
    void setControlWordType(ROCF_ControlWordType type);

    /**
     * Sets the tc vcid.
     * 
     * @param id
     */
    void setTcVcid(long id);

    /**
     * Sets the update mode.
     * 
     * @param mode
     */
    void setUpdateMode(ROCF_UpdateMode mode);

    /**
     * Sets the start diagnostic.
     * 
     * @param diagnostic
     */
    void setStartDiagnostic(ROCF_StartDiagnostic diagnostic);

}
