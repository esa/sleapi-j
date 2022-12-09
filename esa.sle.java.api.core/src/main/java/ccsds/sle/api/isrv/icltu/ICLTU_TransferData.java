package ccsds.sle.api.isrv.icltu;

import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.it.SLE_SlduStatusNotification;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.icltu.types.CLTU_TransferDataDiagnostic;

public interface ICLTU_TransferData extends ISLE_ConfirmedOperation
{
    /**
     * Gets cltu id.
     * 
     * @return
     */
    long getCltuId();

    /**
     * Gets the expected cltu id.
     * 
     * @return
     */
    long getExpectedCltuId();

    /**
     * Gets the earliest rad time.
     * 
     * @return
     */
    ISLE_Time getEarliestRadTime();

    /**
     * Gets the last rad time.
     * 
     * @return
     */
    ISLE_Time getLatestRadTime();

    /**
     * Gets the delay time.
     * 
     * @return
     */
    long getDelayTime();

    /**
     * Gets radiation notification.
     * 
     * @return
     */
    SLE_SlduStatusNotification getRadiationNotification();

    /**
     * Gets data.
     * 
     * @return
     */
    byte[] getData();

    /**
     * Removes data.
     * 
     * @return
     */
    byte[] removeData();

    /**
     * Gets cltu buffer available.
     * 
     * @return
     */
    long getCltuBufferAvailable();

    /**
     * Gets transfer data diagnostic.
     * 
     * @return
     */
    CLTU_TransferDataDiagnostic getTransferDataDiagnostic();

    /**
     * Sets cltu id.
     * 
     * @param id
     */
    void setCltuId(long id);

    /**
     * Sets expected cltu id.
     * 
     * @param id
     */
    void setExpectedCltuId(long id);

    /**
     * Sets the earliest rad time.
     * 
     * @param earliestTime
     */
    void setEarliestRadTime(ISLE_Time earliestTime);

    /**
     * Puts the earliest rad time.
     * 
     * @param pearliestTime
     */
    void putEarliestRadTime(ISLE_Time pearliestTime);

    /**
     * Sets the last rad time.
     * 
     * @param latestTime
     */
    void setLatestRadTime(ISLE_Time latestTime);

    /**
     * Puts the latest rad time.
     * 
     * @param platestTime
     */
    void putLatestRadTime(ISLE_Time platestTime);

    /**
     * Sets the delay time.
     * 
     * @param delay
     */
    void setDelayTime(long delay);

    /**
     * Sets the radiation notification.
     * 
     * @param ntf
     */
    void setRadiationNotification(SLE_SlduStatusNotification ntf);

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
     * Sets the clut buffer available.
     * 
     * @param bufAvail
     */
    void setCltuBufferAvailable(long bufAvail);

    /**
     * Sets the transfer data diagnostic.
     * 
     * @param diagnostic
     */
    void setTransferDataDiagnostic(CLTU_TransferDataDiagnostic diagnostic);

}
