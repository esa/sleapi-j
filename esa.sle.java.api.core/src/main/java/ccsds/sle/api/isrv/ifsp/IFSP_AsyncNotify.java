package ccsds.sle.api.isrv.ifsp;

import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.ifsp.types.FSP_FopAlert;
import ccsds.sle.api.isrv.ifsp.types.FSP_NotificationType;
import ccsds.sle.api.isrv.ifsp.types.FSP_PacketStatus;
import ccsds.sle.api.isrv.ifsp.types.FSP_ProductionStatus;

/**
 * The interface provides access to the parameters of the unconfirmed operation
 * FSP ASYNC NOTIFY.
 * 
 * @version 1.0, October 2015
 */

public interface IFSP_AsyncNotify extends ISLE_Operation
{
    /**
     * Gets the notification type.
     * 
     * @return
     */
    FSP_NotificationType getNotificationType();

    /**
     * Gets the directive executed id.
     * 
     * @return
     */
    long getDirectiveExecutedId();

    /**
     * Gets the event thrown id.
     * 
     * @return
     */
    long getEventThrownId();

    /**
     * Gets the packet identification list.
     * 
     * @return
     */
    long[] getPacketIdentificationList();

    /**
     * Gets the fop alert.
     * 
     * @return
     */
    FSP_FopAlert getFopAlert();

    /**
     * Gets the processed packets.
     * 
     * @return
     */
    boolean getPacketsProcessed();

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
     * Gets the completed packets.
     * 
     * @return
     */
    boolean getPacketsCompleted();

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
     * Gets the production status.
     * 
     * @return
     */
    FSP_ProductionStatus getProductionStatus();

    /**
     * Sets the notification type.
     * 
     * @param notifyType
     */
    void setNotificationType(FSP_NotificationType notifyType);

    /**
     * Sets the directive executed id.
     * 
     * @param id
     */
    void setDirectiveExecutedId(long id);

    /**
     * Sets the event thrown id.
     * 
     * @param id
     */
    void setEventThrownId(long id);

    /**
     * Sets the packet identification list.
     * 
     * @param list
     */
    void setPacketIdentificationList(long[] list);

    /**
     * Puts the packet identification list.
     * 
     * @param list
     */
    void putPacketIdentificationList(long[] list);

    /**
     * Sets the fop alert.
     * 
     * @param alert
     */
    void setFopAlert(FSP_FopAlert alert);

    /**
     * Sets the last processed packet.
     * 
     * @param id
     */
    void setPacketLastProcessed(long id);

    /**
     * Sets the production start time.
     * 
     * @param startTime
     */
    void setProductionStartTime(ISLE_Time startTime);

    /**
     * Puts the production start time.
     * 
     * @param pstartTime
     */
    void putProductionStartTime(ISLE_Time pstartTime);

    /**
     * Sets the packet status.
     * 
     * @param status
     */
    void setPacketStatus(FSP_PacketStatus status);

    /**
     * Sets the packet last ok.
     * 
     * @param id
     */
    void setPacketLastOk(long id);

    /**
     * Sets the production stop time.
     * 
     * @param stopTime
     */
    void setProductionStopTime(ISLE_Time stopTime);

    /**
     * Puts the production stop time.
     * 
     * @param pstopTime
     */
    void putProductionStopTime(ISLE_Time pstopTime);

    /**
     * Sets the production status.
     * 
     * @param status
     */
    void setProductionStatus(FSP_ProductionStatus status);

    /**
     * Gets the frame sequence number.
     * @since SLES V5
     * 
     */
    long getFrameSequenceNumber();

    /**
     * Sets the frame sequence number.
     * @since SLES V5
     */
    void setFrameSequenceNumber(long fsc);
}
