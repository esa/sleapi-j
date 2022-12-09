package ccsds.sle.api.isrv.ifsp;

import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.it.SLE_SlduStatusNotification;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.ifsp.types.FSP_TransferDataDiagnostic;
import ccsds.sle.api.isrv.ifsp.types.FSP_TransmissionMode;

/**
 * The interface provides access to the parameters of the confirmed operation
 * FSP TRANSFER DATA.
 * 
 * @version: 1.0, October 2015
 */
public interface IFSP_TransferData extends ISLE_ConfirmedOperation
{
    /**
     * Gets the packet id.
     * 
     * @return
     */
    long getPacketId();

    /**
     * Gets the expected packet id.
     * 
     * @return
     */
    long getExpectedPacketId();

    /**
     * Gets the earliest prod time.
     *
     * @return
     */
    ISLE_Time getEarliestProdTime();

    /**
     * Gets the latest prod time.
     * 
     * @return
     */
    ISLE_Time getLatestProdTime();

    /**
     * Gets the delay time.
     * 
     * @return
     */
    long getDelayTime();

    /**
     * Gets the transmission mode.
     * 
     * @return
     */
    FSP_TransmissionMode getTransmissionMode();

    /**
     * Gets the used map id.
     * 
     * @return
     */
    boolean getMapIdUsed();

    /**
     * Gets map id.
     * 
     * @return
     */
    long getMapId();

    /**
     * Gets the blocking.
     * 
     * @return
     */
    SLE_YesNo getBlocking();

    /**
     * Gets the processing started notification.
     * 
     * @return
     */
    SLE_SlduStatusNotification getProcessingStartedNotification();

    /**
     * Gets the radiated notification.
     * 
     * @return
     */
    SLE_SlduStatusNotification getRadiatedNotification();

    /**
     * Gets the acknoledged notification.
     * 
     * @return
     */
    SLE_SlduStatusNotification getAcknowledgedNotification();

    /**
     * Gets the data.
     * 
     * @return
     */
    byte[] getData();

    /**
     * Removes the data.
     * 
     * @return
     */
    byte[] removeData();

    /**
     * Gets the available packet buffer.
     * 
     * @return
     */
    long getPacketBufferAvailable();

    /**
     * Gets the transfer data diagnostic.
     * 
     * @return
     */
    FSP_TransferDataDiagnostic getTransferDataDiagnostic();

    /**
     * Sets the packet id.
     * 
     * @param id
     */
    void setPacketId(long id);

    /**
     * Sets expected packet id.
     * 
     * @param id
     */
    void setExpectedPacketId(long id);

    /**
     * Sets earliest production time.
     * 
     * @param earliestTime
     */
    void setEarliestProdTime(ISLE_Time earliestTime);

    /**
     * Puts the earliest prod time.
     * 
     * @param pearliestTime
     */
    void putEarliestProdTime(ISLE_Time pearliestTime);

    /**
     * Sets the lates production time.
     * 
     * @param latestTime
     */
    void setLatestProdTime(ISLE_Time latestTime);

    /**
     * Puts the latest prod time.
     * 
     * @param platestTime
     */
    void putLatestProdTime(ISLE_Time platestTime);

    /**
     * Sets the delay time.
     * 
     * @param delay
     */
    void setDelayTime(long delay);

    /**
     * Sets the transmission mode.
     * 
     * @param mode
     */
    void setTransmissionMode(FSP_TransmissionMode mode);

    /**
     * Sets the map id.
     * 
     * @param id
     */
    void setMapId(long id);

    /**
     * Sets the blocking .
     * 
     * @param blocking
     */
    void setBlocking(SLE_YesNo blocking);

    /**
     * Sets the processing started notification.
     * 
     * @param ntf
     */
    void setProcessingStartedNotification(SLE_SlduStatusNotification ntf);

    /**
     * Sets the radiated notification.
     * 
     * @param ntf
     */
    void setRadiatedNotification(SLE_SlduStatusNotification ntf);

    /**
     * Sets the acknowledged notification.
     * 
     * @param ntf
     */
    void setAcknowledgedNotification(SLE_SlduStatusNotification ntf);

    /**
     * Sets the data.
     * 
     * @param pdata
     */
    void setData(byte[] pdata);

    /**
     * Puts the data.
     * 
     * @param pdata
     */
    void putData(byte[] pdata);

    /**
     * Sets the packet buffer available.
     * 
     * @param bufAvail
     */
    void setPacketBufferAvailable(long bufAvail);

    /**
     * Sets the transfer data diagnostic.
     * 
     * @param diagnostic
     */
    void setTransferDataDiagnostic(FSP_TransferDataDiagnostic diagnostic);

}
