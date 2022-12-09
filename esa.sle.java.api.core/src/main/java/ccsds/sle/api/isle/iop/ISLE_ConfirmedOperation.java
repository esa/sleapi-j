package ccsds.sle.api.isle.iop;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.it.SLE_DiagnosticType;
import ccsds.sle.api.isle.it.SLE_Diagnostics;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.iutl.ISLE_Credentials;

/**
 * The interface defines characteristics supported by all confirmed operation
 * objects.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_ConfirmedOperation extends ISLE_Operation
{
    /**
     * Returns the result
     * 
     * @return the result
     */
    SLE_Result getResult();

    /**
     * Returns the diagnostic type
     * 
     * @return the diagnostic type
     */
    SLE_DiagnosticType getDiagnosticType();

    /**
     * Returns the diagnostic
     * 
     * @return the diagnostic
     */
    SLE_Diagnostics getDiagnostics();

    /**
     * Returns the invocation identifier
     * 
     * @return the invocation identifier
     */
    int getInvokeId();

    /**
     * Returns the performer credentials
     * 
     * @return the performer credentials
     */
    ISLE_Credentials getPerformerCredentials();

    /**
     * Sets the positive result
     */
    void setPositiveResult();

    /**
     * Sets the diagnostic
     * 
     * @param diagnostic the diagnostic
     */
    void setDiagnostics(SLE_Diagnostics diagnostic);

    /**
     * Sets the invocation identifier
     * 
     * @param id invocation identifier
     */
    void setInvokeId(int id);

    /**
     * Sets the performer credentials
     * 
     * @param credentials the performer credentials
     */
    void setPerformerCredentials(ISLE_Credentials credentials);

    /**
     * Puts the performer credentials
     * 
     * @param pcredentials the performer credentials
     */
    void putPerformerCredentials(ISLE_Credentials pcredentials);

    /**
     * Checks the operation return arguments
     * 
     * @throws SleApiException
     */
    void verifyReturnArguments() throws SleApiException;
}
