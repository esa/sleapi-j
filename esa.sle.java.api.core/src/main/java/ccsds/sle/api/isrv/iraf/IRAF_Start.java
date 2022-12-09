package ccsds.sle.api.isrv.iraf;

import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.iraf.types.RAF_RequestedFrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_StartDiagnostic;

public interface IRAF_Start extends ISLE_ConfirmedOperation
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
     * Gets the requested frame quality.
     * 
     * @return
     */
    RAF_RequestedFrameQuality getRequestedFrameQuality();

    /**
     * Gets the start diagnostic.
     * 
     * @return
     */
    RAF_StartDiagnostic getStartDiagnostic();

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
     * Sets the requested frame quality.
     * 
     * @param quality
     */
    void setRequestedFrameQuality(RAF_RequestedFrameQuality quality);

    /**
     * Sets the start diagnostic.
     * 
     * @param diagnostic
     */
    void setStartDiagnostic(RAF_StartDiagnostic diagnostic);

}
