package ccsds.sle.api.isrv.icltu;

import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.icltu.types.CLTU_NotificationType;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProductionStatus;
import ccsds.sle.api.isrv.icltu.types.CLTU_Status;
import ccsds.sle.api.isrv.icltu.types.CLTU_UplinkStatus;

public interface ICLTU_AsyncNotify extends ISLE_Operation
{
    /**
     * Gets notification type.
     * 
     * @return
     */
    CLTU_NotificationType getNotificationType();

    /**
     * Gets event thrown id.
     * 
     * @return
     */
    long getEventThrownId();

    /**
     * Gets processed cltu.
     * 
     * @return
     */
    boolean getCltusProcessed();

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
     * Gets cltu radiated.
     * 
     * @return
     */
    boolean getCltusRadiated();

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
     * Sets notification type.
     * 
     * @param notifyType
     */
    void setNotificationType(CLTU_NotificationType notifyType);

    /**
     * Sets event thrown event id.
     * 
     * @param id
     */
    void setEventThrownId(long id);

    /**
     * Sets last cltu processed.
     * 
     * @param id
     */
    void setCltuLastProcessed(long id);

    /**
     * Sets radiation start time.
     * 
     * @param startTime
     */
    void setRadiationStartTime(ISLE_Time startTime);

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
     * Sets cltu last ok.
     * 
     * @param id
     */
    void setCltuLastOk(long id);

    /**
     * Sets radiation stop time.
     * 
     * @param stopTime
     */
    void setRadiationStopTime(ISLE_Time stopTime);

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

}
