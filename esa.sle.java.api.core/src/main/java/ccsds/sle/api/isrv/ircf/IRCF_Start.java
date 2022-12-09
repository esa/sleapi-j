package ccsds.sle.api.isrv.ircf;

import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.ircf.types.RCF_Gvcid;
import ccsds.sle.api.isrv.ircf.types.RCF_StartDiagnostic;

public interface IRCF_Start extends ISLE_ConfirmedOperation
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
    RCF_Gvcid getGvcid();

    /**
     * Gets the start diagnostic.
     * 
     * @return
     */
    RCF_StartDiagnostic getStartDiagnostic();

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
    void setGvcid(RCF_Gvcid id);

    /**
     * Puts the gvcid.
     * 
     * @param pid
     */
    void putGvcid(RCF_Gvcid pid);

    /**
     * Sets the start diagnostic.
     * 
     * @param diagnostic
     */
    void setStartDiagnostic(RCF_StartDiagnostic diagnostic);

}
