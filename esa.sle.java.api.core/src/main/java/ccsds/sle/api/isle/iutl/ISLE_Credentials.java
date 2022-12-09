package ccsds.sle.api.isle.iutl;

import ccsds.sle.api.isle.iscm.IUnknown;

/**
 * Objects implementing this interface hold the credentials used for
 * authentication of the peer identity. The credentials comprise a message
 * digest (the protected), a random number, and the time when the message digest
 * was generated. For the message digest the object does not make any
 * assumptions on the format, size, or encoding. It simply stores the sequence
 * of bytes passed to it.
 * 
 * @version: 1.0, October 2015
 */

public interface ISLE_Credentials extends IUnknown
{
    /**
     * Gets random number.
     * 
     * @return
     */
    long getRandomNumber();

    /**
     * Gets protected.
     * 
     * @return
     */
    byte[] getProtected();

    /**
     * Gets the time ref.
     * 
     * @return
     */
    ISLE_Time getTimeRef();

    /**
     * Sets random number.
     * 
     * @param number
     */
    void setRandomNumber(long number);

    /**
     * Sets protected.
     * 
     * @param hashCode
     */
    void setProtected(byte[] hashCode);

    /**
     * Sets time ref.
     * 
     * @param time
     */
    void setTimeRef(ISLE_Time time);

    /**
     * Copies the instance object.
     * 
     * @return
     */
    ISLE_Credentials copy();

    /**
     * Dumps
     * 
     * @return
     */
    String dump();

    /**
     * @param o
     * @return
     */
    @Override
    boolean equals(Object o);

    /**
     * @return
     */
    @Override
    int hashCode();
}
