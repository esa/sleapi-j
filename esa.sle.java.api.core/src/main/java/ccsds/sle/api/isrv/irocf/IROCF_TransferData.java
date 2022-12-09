package ccsds.sle.api.isrv.irocf;

import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.irocf.types.ROCF_AntennaIdFormat;

/**
 * The interface provides access to the parameters of the operation ROCF
 * TRANSFER DATA.
 * 
 * @version: 1.0, October 2015
 */
public interface IROCF_TransferData extends ISLE_Operation
{
    /**
     * Gets the earth receive time.
     * 
     * @return
     */
    ISLE_Time getEarthReceiveTime();

    /**
     * Gets the antenna id format.
     * 
     * @return
     */
    ROCF_AntennaIdFormat getAntennaIdFormat();

    /**
     * Gets the antenna id lf.
     * 
     * @return
     */
    byte[] getAntennaIdLF();

    /**
     * Gets the antenna id gf.
     * 
     * @return
     */
    int[] getAntennaIdGF();

    /**
     * Gets the antenna id gf in String format.
     * 
     * @return
     */
    String getAntennaIdGFString();

    /**
     * Gets the Data link continuity.
     * 
     * @return
     */
    int getDataLinkContinuity();

    /**
     * Gets the private annotation.
     * 
     * @return
     */
    byte[] getPrivateAnnotation();

    /**
     * Removes the private anotation.
     * 
     * @return
     */
    byte[] removePrivateAnnotation();

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
     * Sets the earth receive time.
     * 
     * @param time
     */
    void setEarthReceiveTime(final ISLE_Time time);

    /**
     * Puts the earth receive time.
     * 
     * @param ptime
     */
    void putEarthReceiveTime(ISLE_Time ptime);

    /**
     * Sets the antenna id lf.
     * 
     * @param id
     */
    void setAntennaIdLF(final byte[] id);

    /**
     * Sets the antenna id gf.
     * 
     * @param id
     */
    void setAntennaIdGF(final int[] id);

    /**
     * Sets the antenna id gf in String format.
     * 
     * @param id
     */
    void setAntennaIdGFString(final String id);

    /**
     * Sets the data link continuity.
     * 
     * @param numFrames
     */
    void setDataLinkContinuity(int numFrames);

    /**
     * Sets the private annotation.
     * 
     * @param pannotation
     */
    void setPrivateAnnotation(final byte[] pannotation);

    /**
     * Puts the private annotation.
     * 
     * @param pannotation
     */
    void putPrivateAnnotation(byte[] pannotation);

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

}
