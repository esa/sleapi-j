package ccsds.sle.api.isle.iop;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.iutl.ISLE_Credentials;

/**
 * The interface defines basic characteristics supported by all operation
 * objects.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_Operation extends IUnknown
{
    /**
     * Returns the operation service type
     * 
     * @return the operation service type
     */
    SLE_ApplicationIdentifier getOpServiceType();

    /**
     * Returns the operation service number
     * 
     * @return the operation service number
     */
    int getOpVersionNumber();

    /**
     * Returns the operation type
     * 
     * @return the operation type
     */
    SLE_OpType getOperationType();

    /**
     * Returns true if the operation implements the ISLE_ConfirmedOperation
     * interface, false otherwise
     * 
     * @return true if the operation implements the ISLE_ConfirmedOperation
     *         interface, false otherwise
     */
    boolean isConfirmed();

    /**
     * Returns the invoker credentials
     * 
     * @return the invoker credentials
     */
    ISLE_Credentials getInvokerCredentials();

    /**
     * Sets the invoker credentials
     * 
     * @param credentials the invoker credentials
     */
    void setInvokerCredentials(ISLE_Credentials credentials);

    /**
     * Puts the invoker credentials
     * 
     * @param pcredentials the invoker credentials
     */
    void putInvokerCredentials(ISLE_Credentials pcredentials);

    /**
     * Checks the Operation invocation arguments
     * 
     * @throws SleApiException
     */
    void verifyInvocationArguments() throws SleApiException;

    /**
     * Locks the mutex if not already locked
     */
    void lock();

    /**
     * Attempts to lock, but it doesn't block
     * 
     * @throws SleApiException
     */
    void tryLock() throws SleApiException;

    /**
     * Unlocks the mutex
     */
    void unlock();

    /**
     * Returns a copy of the operation
     * 
     * @return a copy of the operation
     */
    ISLE_Operation copy();

    /**
     * Prints the contents of the object.
     * 
     * @param maxDumpLength
     * @return a string of given length
     */
    String print(int maxDumpLength);
}
