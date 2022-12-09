package ccsds.sle.api.isrv.icltu;

import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProductionStatus;
import ccsds.sle.api.isrv.icltu.types.CLTU_Status;
import ccsds.sle.api.isrv.icltu.types.CLTU_UplinkStatus;

public interface ICLTU_StatusReport extends ISLE_Operation
{
    /**
     * Gets last processed cltu.
     * 
     * @return
     */
    long getCltuLastProcessed();

    /**
     * Gets radiation start time.
     * 
     * @return
     */
    ISLE_Time getRadiationStartTime();

    /**
     * Gets cltu status.
     * 
     * @return
     */
    CLTU_Status getCltuStatus();

    /**
     * Gets cltu last ok.
     * 
     * @return
     */
    long getCltuLastOk();

    /**
     * Gets radiation stop time.
     * 
     * @return
     */
    ISLE_Time getRadiationStopTime();

    /**
     * Gets production status.
     * 
     * @return
     */
    CLTU_ProductionStatus getProductionStatus();

    /**
     * Gets uplink status.
     * 
     * @return
     */
    CLTU_UplinkStatus getUplinkStatus();

    /**
     * Gets the number of cltus received.
     * 
     * @return
     */
    long getNumberOfCltusReceived();

    /**
     * Gets the number of cltus processed.
     * 
     * @return
     */
    long getNumberOfCltusProcessed();

    /**
     * Gets the number of cltus radiated.
     * 
     * @return
     */
    long getNumberOfCltusRadiated();

    /**
     * Gets the number of cltu buffer available.
     * 
     * @return
     */
    long getCltuBufferAvailable();

    /**
     * Sets last processed cltu.
     * 
     * @param id
     */
    void setCltuLastProcessed(long id);

    /**
     * Sets radiation start time.
     * 
     * @param startTime
     */
    void setRadiationStartTime(final ISLE_Time startTime);

    /**
     * Puts radiation start time.
     * 
     * @param pstartTime
     */
    void putRadiationStartTime(ISLE_Time pstartTime);

    /**
     * Sets cltu status.
     * 
     * @param status
     */
    void setCltuStatus(CLTU_Status status);

    /**
     * Sets Cltu last ok.
     * 
     * @param id
     */
    void setCltuLastOk(long id);

    /**
     * Sets radiation stop time.
     * 
     * @param stopTime
     */
    void setRadiationStopTime(final ISLE_Time stopTime);

    /**
     * Puts radiation stop time.
     * 
     * @param pstopTime
     */
    void putRadiationStopTime(ISLE_Time pstopTime);

    /**
     * Sets production status.
     * 
     * @param status
     */
    void setProductionStatus(CLTU_ProductionStatus status);

    /**
     * Sets uplink status.
     * 
     * @param status
     */
    void setUplinkStatus(CLTU_UplinkStatus status);

    /**
     * Sets the number of received cltus.
     * 
     * @param numRecv
     */
    void setNumberOfCltusReceived(long numRecv);

    /**
     * Sets the number of processed cltus.
     * 
     * @param numProc
     */
    void setNumberOfCltusProcessed(long numProc);

    /**
     * Sets the number of radiated cltus.
     * 
     * @param numRad
     */
    void setNumberOfCltusRadiated(long numRad);

    /**
     * Sets the cltu buffer available.
     * 
     * @param size
     */
    void setCltuBufferAvailable(long size);

}
