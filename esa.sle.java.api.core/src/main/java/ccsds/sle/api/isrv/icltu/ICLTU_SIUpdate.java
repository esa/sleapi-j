package ccsds.sle.api.isrv.icltu;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.icltu.types.CLTU_EventResult;
import ccsds.sle.api.isrv.icltu.types.CLTU_Failure;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProductionStatus;
import ccsds.sle.api.isrv.icltu.types.CLTU_Status;
import ccsds.sle.api.isrv.icltu.types.CLTU_UplinkStatus;

public interface ICLTU_SIUpdate extends IUnknown
{
    /**
     * Cltu started.
     * 
     * @param id
     * @param radiationStartTime
     * @param bufferAvailable
     */
    void cltuStarted(long id, ISLE_Time radiationStartTime, long bufferAvailable);

    /**
     * Cltu radiated.
     * 
     * @param radiationStopTime
     * @param radiationStartTime
     * @param notify
     */
    void cltuRadiated(ISLE_Time radiationStopTime, ISLE_Time radiationStartTime, boolean notify);

    /**
     * Cltu not started.
     * 
     * @param id
     * @param reason
     * @param bufferAvailable
     * @param notify
     * @throws SleApiException
     */
    void cltuNotStarted(long id, CLTU_Failure reason, long bufferAvailable, boolean notify) throws SleApiException;

    /**
     * Production status change.
     * 
     * @param newStatus
     * @param bufferAvailable
     * @param notify
     * @throws SleApiException
     */
    void productionStatusChange(CLTU_ProductionStatus newStatus, long bufferAvailable, boolean notify) throws SleApiException;

    /**
     * Buffer empty.
     * 
     * @param notify
     */
    void bufferEmpty(boolean notify);

    /**
     * Event process completed.
     * 
     * @param id
     * @param result
     * @param notify
     */
    void eventProcCompleted(long id, CLTU_EventResult result, boolean notify);

    /**
     * Sets uplink status.
     * 
     * @param status
     */
    void setUplinkStatus(CLTU_UplinkStatus status);

    /**
     * Gets production status.
     * 
     * @return
     */
    CLTU_ProductionStatus getProductionStatus();

    /**
     * Gets cltu buffer available.
     * 
     * @return
     */
    long getCltuBufferAvailable();

    /**
     * Gets number of cltus received.
     * 
     * @return
     */
    long getNumberOfCltusReceived();

    /**
     * Gets number of cltus processed.
     * 
     * @return
     */
    long getNumberOfCltusProcessed();

    /**
     * Gets number of cltus radiated.
     * 
     * @return
     */
    long getNumberOfCltusRadiated();

    /**
     * gets Cltu last processed.
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
     * Gets uplink status.
     * 
     * @return
     */
    CLTU_UplinkStatus getUplinkStatus();

    /**
     * Gets expected cltu id.
     * 
     * @return
     */
    long getExpectedCltuId();

    /**
     * gets expected event invocation id.
     * 
     * @return
     */
    long getExpectedEventInvocationId();

}
