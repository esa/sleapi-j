package ccsds.sle.api.isrv.ircf;

import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.ircf.types.RCF_AntennaIdFormat;

public interface IRCF_TransferData extends ISLE_Operation
{
    /**
     * Gets the earth received time.
     * 
     * @return
     */
    ISLE_Time getEarthReceiveTime();

    /**
     * Gets the antenna id format.
     * 
     * @return
     */
    RCF_AntennaIdFormat getAntennaIdFormat();

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
     * Gets the antenna id gf in string format.
     * 
     * @return
     */
    String getAntennaIdGFString();

    /**
     * Gets the data link continuity.
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
     * Removes the private annotation.
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
    void setEarthReceiveTime(ISLE_Time time);

    /**
     * Puts the eatch receive time.
     * 
     * @param ptime
     */
    void putEarthReceiveTime(ISLE_Time ptime);

    /**
     * Sets the antenna id lf.
     * 
     * @param id
     */
    void setAntennaIdLF(byte[] id);

    /**
     * Sets the antenna id gf.
     * 
     * @param id
     */
    void setAntennaIdGF(int[] id);

    /**
     * Sets the antenna id gf in String format.
     * 
     * @param id
     */
    void setAntennaIdGFString(String id);

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
    void setPrivateAnnotation(byte[] pannotation);

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
