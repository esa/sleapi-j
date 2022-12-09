package ccsds.sle.api.isrv.icltu;

import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isrv.icltu.types.CLTU_ThrowEventDiagnostic;

public interface ICLTU_ThrowEvent extends ISLE_ConfirmedOperation
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
     * Gets expected event invocation id.
     * 
     * @return
     */
    long getExpectedEventInvocationId();

    /**
     * Gets event qualifier.
     * 
     * @return
     */
    byte[] getEventQualifier();

    /**
     * Gets throw event diagnostic.
     * 
     * @return
     */
    CLTU_ThrowEventDiagnostic getThrowEventDiagnostic();

    /**
     * Sets event id.
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
     * @param pdata
     */
    void setEventQualifier(byte[] pdata);

    /**
     * Sets the throw event diagnostic.
     * 
     * @param diagnostic
     */
    void setThrowEventDiagnostic(CLTU_ThrowEventDiagnostic diagnostic);

}
