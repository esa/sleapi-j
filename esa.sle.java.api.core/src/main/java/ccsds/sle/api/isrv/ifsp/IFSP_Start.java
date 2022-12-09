package ccsds.sle.api.isrv.ifsp;

import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.ifsp.types.FSP_StartDiagnostic;

/**
 * The interface provides access to the parameters of the confirmed operation
 * FSP START.
 * 
 * @version: 1.0, October 2015
 */
public interface IFSP_Start extends ISLE_ConfirmedOperation
{
    /**
     * Gets the first packet id.
     * 
     * @return
     */
    long getFirstPacketId();

    /**
     * Gets the start production time.
     * 
     * @return
     */
    ISLE_Time getStartProductionTime();

    /**
     * Gets the stop production time.
     * 
     * @return
     */
    ISLE_Time getStopProductionTime();

    /**
     * Gets the start diagnostic.
     * 
     * @return
     */
    FSP_StartDiagnostic getStartDiagnostic();

    /**
     * Sets the first packet id.
     * 
     * @param id
     */
    void setFirstPacketId(long id);

    /**
     * @param startTime
     */
    void setStartProductionTime(ISLE_Time startTime);

    /**
     * Puts the start production on time.
     * 
     * @param pstartTime
     */
    void putStartProductionTime(ISLE_Time pstartTime);

    /**
     * Sets the stop production time.
     * 
     * @param stopTime
     */
    void setStopProductionTime(ISLE_Time stopTime);

    /**
     * Puts the stop production time.
     * 
     * @param pstopTime
     */
    void putStopProductionTime(ISLE_Time pstopTime);

    /**
     * Sets the start diagnostic.
     * 
     * @param diag
     */
    void setStartDiagnostic(FSP_StartDiagnostic diag);

}
