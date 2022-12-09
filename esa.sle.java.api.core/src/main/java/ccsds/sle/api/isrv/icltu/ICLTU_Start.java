package ccsds.sle.api.isrv.icltu;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.icltu.types.CLTU_StartDiagnostic;

public interface ICLTU_Start extends ISLE_ConfirmedOperation
{
    /**
     * Gets first cltu id used.
     * 
     * @return
     */
    boolean getFirstCltuIdUsed();

    /**
     * Gets cltu id.
     * 
     * @return
     * @throws SleApiException
     */
    long getFirstCltuId() throws SleApiException;

    /**
     * Gets start production time.
     * 
     * @return
     */
    ISLE_Time getStartProductionTime();

    /**
     * Gets stop production time.
     * 
     * @return
     */
    ISLE_Time getStopProductionTime();

    /**
     * Gets start diagnostic.
     * 
     * @return
     * @throws SleApiException
     */
    CLTU_StartDiagnostic getStartDiagnostic() throws SleApiException;

    /**
     * Sets first cltu id.
     * 
     * @param id
     */
    void setFirstCltuId(long id);

    /**
     * Sets start production time.
     * 
     * @param startTime
     */
    void setStartProductionTime(ISLE_Time startTime);

    /**
     * Puts start production time.
     * 
     * @param pstartTime
     */
    void putStartProductionTime(ISLE_Time pstartTime);

    /**
     * Sets stop production time.
     * 
     * @param stopTime
     */
    void setStopProductionTime(ISLE_Time stopTime);

    /**
     * Puts stop production time.
     * 
     * @param pstopTime
     */
    void putStopProductionTime(ISLE_Time pstopTime);

    /**
     * Sets start diagnostic.
     * 
     * @param diag
     */
    void setStartDiagnostic(CLTU_StartDiagnostic diag);

}
