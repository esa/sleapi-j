/**
 * @(#) IFSP_SIUpdate.java
 */

package ccsds.sle.api.isrv.ifsp;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.ifsp.types.FSP_EventResult;
import ccsds.sle.api.isrv.ifsp.types.FSP_Failure;
import ccsds.sle.api.isrv.ifsp.types.FSP_FopAlert;
import ccsds.sle.api.isrv.ifsp.types.FSP_PacketStatus;
import ccsds.sle.api.isrv.ifsp.types.FSP_ProductionStatus;
import ccsds.sle.api.isrv.ifsp.types.FSP_TransmissionMode;

/**
 * The interface provides methods to update parameters that shall be reported to
 * the service user via the operations FSP-STATUS-REPORT, FSP-ASYNC-NOTIFY, and
 * FSP-GET-PARAMETER. In order to keep this information up to date the
 * appropriate methods of this interface must be called whenever certain events
 * occur (see the specification in section 3.1). If these events must be
 * reported to the FSP service user via a notification, the API can be requested
 * to send the notification. Alternatively the application can generate and send
 * the notification itself. The methods of this interface must always be called
 * when one of the relevant events occurs, independent of the state of the
 * service instance. Notifications to the user will only be sent, if the service
 * instance state is either 'ready' or 'active'. Failure to inform the API of an
 * event can result in incorrect and inconsistent parameters in the status
 * report. Because of performance considerations, methods processing nominal
 * events perform no plausibility checks, but completely rely on the application
 * to provide correct and consistent arguments. The interface provides read
 * access to the parameters set via this interface and to parameters accumulated
 * or derived by the API according to the specifications in section 3.1. The API
 * sets the parameters to the initial values specified at the end of this
 * section when the service instance is configured. Parameter values retrieved
 * before configuration are undefined.
 * 
 * @version: 1.0, October 2015
 */
public interface IFSP_SIUpdate extends IUnknown
{
    /**
     * Packet started.
     * 
     * @param packedId
     * @param mode
     * @param startTime
     * @param bufferAvailable
     * @param notify
     */
    void packetStarted(long packedId,
                       FSP_TransmissionMode mode,
                       ISLE_Time startTime,
                       long bufferAvailable,
                       boolean notify);

    /**
     * Packet radiated.
     * 
     * @param packedId
     * @param mode
     * @param radiationTime
     * @param notify
     */
    void packetRadiated(long packedId, FSP_TransmissionMode mode, ISLE_Time radiationTime, boolean notify);

    /**
     * Packed acknowledged.
     * 
     * @param packedId
     * @param ackTime
     * @param notify
     */
    void packetAcknowledged(long packedId, ISLE_Time ackTime, boolean notify);

    /**
     * Buffer empty.
     * 
     * @param notify
     */
    void bufferEmpty(boolean notify);

    /**
     * Packet is not started.
     * 
     * @param packedId
     * @param mode
     * @param startTime
     * @param reason
     * @param bufferAvailable
     * @param notify
     * @param affectedPackets
     * @throws SleApiException
     */
    void packetNotStarted(long packedId,
                          FSP_TransmissionMode mode,
                          ISLE_Time startTime,
                          FSP_Failure reason,
                          long bufferAvailable,
                          boolean notify,
                          long[] affectedPackets) throws SleApiException;

    /**
     * Production status change.
     * 
     * @param newStatus
     * @param affectedPackets
     * @param fopAlert
     * @param bufferAvailable
     * @param notify
     * @throws SleApiException
     */
    void productionStatusChange(FSP_ProductionStatus newStatus,
                                long[] affectedPackets,
                                FSP_FopAlert fopAlert,
                                long bufferAvailable,
                                boolean notify) throws SleApiException;

    /**
     * Vc aborted.
     * 
     * @param affectedPackets
     * @param bufferAvailable
     * @param notify
     * @throws SleApiException
     */
    void vcAborted(long[] affectedPackets, long bufferAvailable, boolean notify) throws SleApiException;

    /**
     * No directive capability.
     * 
     * @param notify
     * @throws SleApiException
     */
    void noDirectiveCapability(boolean notify) throws SleApiException;

    /**
     * Directive online capability.
     * 
     * @param notify
     * @throws SleApiException
     */
    void directiveCapabilityOnline(boolean notify) throws SleApiException;

    /**
     * Completed directive.
     * 
     * @param directiveId
     * @param result
     * @param fopAlert
     * @param notify
     * @throws SleApiException
     */
    void directiveCompleted(long directiveId, SLE_Result result, FSP_FopAlert fopAlert, boolean notify) throws SleApiException;

    /**
     * Event completed.
     * 
     * @param eventId
     * @param result
     * @param notify
     * @throws SleApiException
     */
    void eventProcCompleted(long eventId, FSP_EventResult result, boolean notify) throws SleApiException;

    /**
     * Gets the production status.
     * 
     * @return
     */
    FSP_ProductionStatus getProductionStatus();

    /**
     * Gets the online directive invocation.
     * 
     * @return
     */
    SLE_YesNo getDirectiveInvocationOnline();

    /**
     * Gets the available packet buffer.
     * 
     * @return
     */
    long getPacketBufferAvailable();

    /**
     * Gets the number of ad received packets.
     * 
     * @return
     */
    long getNumberOfADPacketsReceived();

    /**
     * Gets the number of bd received packets.
     * 
     * @return
     */
    long getNumberOfBDPacketsReceived();

    /**
     * Gets the number of ad processed packets.
     * 
     * @return
     */
    long getNumberOfADPacketsProcessed();

    /**
     * Gets the number of bd processed packets.
     * 
     * @return
     */
    long getNumberOfBDPacketsProcessed();

    /**
     * Gets the number of ad radiated packets.
     * 
     * @return
     */
    long getNumberOfADPacketsRadiated();

    /**
     * Gets the number of bd radiated packets.
     * 
     * @return
     */
    long getNumberOfBDPacketsRadiated();

    /**
     * Gets the number of acknowledged packets.
     * 
     * @return
     */
    long getNumberOfPacketsAcknowledged();

    /**
     * Gets the last processed packet.
     * 
     * @return
     */
    long getPacketLastProcessed();

    /**
     * Gets the production start time.
     * 
     * @return
     */
    ISLE_Time getProductionStartTime();

    /**
     * Gets the packet status.
     * 
     * @return
     */
    FSP_PacketStatus getPacketStatus();

    /**
     * Gets the packet last ok.
     * 
     * @return
     */
    long getPacketLastOk();

    /**
     * Gets the production stop time.
     * 
     * @return
     */
    ISLE_Time getProductionStopTime();

    /**
     * Gets the expected packet id.
     * 
     * @return
     */
    long getExpectedPacketId();

    /**
     * Gets the expected directive invocation id.
     * 
     * @return
     */
    long getExpectedDirectiveInvocationId();

    /**
     * Gets the expected event invocation id.
     * 
     * @return
     */
    long getExpectedEventInvocationId();

}
