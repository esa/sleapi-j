package ccsds.sle.api.isle.ipx;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_AssocState;

/**
 * The interface allows a client to pass SLE Operation Invocations and Returns
 * to an association in the Proxy for transmission to the peer system. The
 * association accepts any operation that is valid for the given service type -
 * independent of the service instance state and whether the clients acts as a
 * SLE service user or provider. The only checks applied are related to the
 * state of the association. For a description of the associated state table of
 * an association see chapter 5.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_SrvProxyInitiate extends IUnknown
{
    /**
     * Initiates operation invoke.
     * 
     * @param poperation operation as parameter
     * @param reportTransmission report transmission
     * @param seqCount sequence counter
     * @throws SleApiException
     */
    void initiateOpInvoke(ISLE_Operation poperation, boolean reportTransmission, long seqCount) throws SleApiException;

    /**
     * Initiates operation return.
     * 
     * @param poperation operation as parameter
     * @param report report
     * @param seqCount sequence counter
     * @throws SleApiException
     */
    void initiateOpReturn(ISLE_ConfirmedOperation poperation, boolean report, long seqCount) throws SleApiException;

    /**
     * Discards the buffer.
     * 
     * @throws SleApiException
     */
    void discardBuffer() throws SleApiException;

    /**
     * Get the State.
     * 
     * @return
     */
    SLE_AssocState getAssocState();
}
