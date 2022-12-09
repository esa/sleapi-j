package ccsds.sle.api.isrv.ifsp;

import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isrv.ifsp.types.FSP_ThrowEventDiagnostic;

/**
 * The interface provides access to the parameters of the confirmed operation
 * FSP THROW-EVENT.
 * 
 * @version: 1.0, October 2015
 */
public interface IFSP_ThrowEvent extends ISLE_ConfirmedOperation
{
    /**
     * Gets the event id.
     * 
     * @return
     */
    int getEventId();

    /**
     * Gets the event invocation id.
     * 
     * @return
     */
    long getEventInvocationId();

    /**
     * Gets the expected event invocation id.
     * 
     * @return
     */
    long getExpectedEventInvocationId();

    /**
     * Gets the event qualifier.
     * 
     * @return
     */
    byte[] getEventQualifier();

    /**
     * Gets the throw event diagnostic.
     * 
     * @return
     */
    FSP_ThrowEventDiagnostic getThrowEventDiagnostic();

    /**
     * Sets the event id.
     * 
     * @param id
     */
    void setEventId(int id);

    /**
     * Sets the event invocation id.
     * 
     * @param id
     */
    void setEventInvocationId(long id);

    /**
     * Sets the expected event invocation id.
     * 
     * @param id
     */
    void setExpectedEventInvocationId(long id);

    /**
     * Sets the event qualifier.
     * 
     * @param parg
     */
    void setEventQualifier(byte[] parg);

    /**
     * Sets the throw event diagnostic.
     * 
     * @param diagnostic
     */
    void setThrowEventDiagnostic(FSP_ThrowEventDiagnostic diagnostic);

}
