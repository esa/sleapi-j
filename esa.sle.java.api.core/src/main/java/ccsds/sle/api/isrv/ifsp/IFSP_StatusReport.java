package ccsds.sle.api.isrv.ifsp;

import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.ifsp.types.FSP_PacketStatus;
import ccsds.sle.api.isrv.ifsp.types.FSP_ProductionStatus;

/**
 * The interface provides access to the parameters of the unconfirmed operation
 * FSP STATUS REPORT.
 * 
 * @version: 1.0, October 2015
 */
public interface IFSP_StatusReport extends ISLE_Operation
{
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
     * Gets the packets completed.
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
     * Gets production stop time.
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
     * Gets the number of ad packets received.
     * 
     * @return
     */
    long getNumberOfADPacketsReceived();

    /**
     * Gets the number of received bd packets.
     * 
     * @return
     */
    long getNumberOfBDPacketsReceived();

    /**
     * Gets the number of processed ad packets.
     * 
     * @return
     */
    long getNumberOfADPacketsProcessed();

    /**
     * Gets the number of processed bd packets.
     * 
     * @return
     */
    long getNumberOfBDPacketsProcessed();

    /**
     * Gets the number of radiated ad packets.
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
     * Gets the packet available buffer.
     * 
     * @return
     */
    long getPacketBufferAvailable();

    /**
     * Sets the last packet processed.
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
     * Sets the number of received ad packets.
     * 
     * @param numRecv
     */
    void setNumberOfADPacketsReceived(long numRecv);

    /**
     * Sets the number of received bd packets.
     * 
     * @param numRecv
     */
    void setNumberOfBDPacketsReceived(long numRecv);

    /**
     * Sets the number of processed ad packets.
     * 
     * @param numRecv
     */
    void setNumberOfADPacketsProcessed(long numRecv);

    /**
     * Sets the number of processed bd packets.
     * 
     * @param numRecv
     */
    void setNumberOfBDPacketsProcessed(long numRecv);

    /**
     * Sets the number of radiated ad packets.
     * 
     * @param numRecv
     */
    void setNumberOfADPacketsRadiated(long numRecv);

    /**
     * Sets the number of radiated bd packets.
     * 
     * @param numRecv
     */
    void setNumberOfBDPacketsRadiated(long numRecv);

    /**
     * Sets the number of radiated bd packets.
     * 
     * @param numRecv
     */
    void setNumberOfPacketsAcknowledged(long numRecv);

    /**
     * Sets the packet buffer available.
     * 
     * @param size
     */
    void setPacketBufferAvailable(long size);

}
