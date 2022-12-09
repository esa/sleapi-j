package ccsds.sle.api.isle.iop;

import ccsds.sle.api.isle.exception.SleApiException;

/**
 * The interface defines the ISLE Transfer Buffer.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_TransferBuffer extends ISLE_Operation
{
    /**
     * Returns the Transfer Buffer maximum size
     * 
     * @return the Transfer Buffer maximum size
     */
    long getMaximumSize();

    /**
     * Sets the Transfer Buffer maximum size
     * 
     * @param size the Transfer Buffer maximum size
     * @throws SleApiException
     */
    void setMaximumSize(long size) throws SleApiException;

    /**
     * Returns the actual Transfer Buffer size
     * 
     * @return the actual Transfer Buffer size
     */
    long getSize();

    /**
     * Returns true if the Transfer Buffer size equals the maximum Transfer
     * buffer size
     * 
     * @return true if the Transfer Buffer size equals the maximum Transfer
     *         buffer size
     */
    boolean full();

    /**
     * Returns true if the Transfer Buffer is empty
     * 
     * @return true if the Transfer Buffer is empty
     */
    boolean empty();

    /**
     * Appends the operation to the Transfer Buffer
     * 
     * @param poperation the operation to append
     */
    void append(ISLE_Operation poperation);

    /**
     * Prepends the operation to the Transfer Buffer
     * 
     * @param poperation the operation to prepend
     * @param extend true if the Transfer Buffer can be extended
     */

    void prepend(ISLE_Operation poperation, boolean extend);

    /**
     * Removes the first element from the Transfer Buffer and returns it
     * 
     * @return the first element of the Transfer Buffer
     */
    ISLE_Operation removeFront();

    /**
     * Removes the last element from the Transfer Buffer returns it
     * 
     * @return the last operation of the Transfer Buffer
     */
    ISLE_Operation removeRear();

    /**
     * Returns the first element of the Transfer Buffer
     * 
     * @return the first element of the Transfer Buffer
     */
    ISLE_Operation front();

    /**
     * Clears the Transfer Buffer
     */
    void clear();

    /**
     * Resets the Transfer Buffer
     */
    void reset();

    /**
     * Returns true if the Transfer Buffer contains more data
     * 
     * @return true if the Transfer Buffer contains more data
     */
    boolean moreData();

    /**
     * Returns the next element of the Transfer Buffer
     * 
     * @return the next element of the Transfer Buffer
     */
    ISLE_Operation next();
}
