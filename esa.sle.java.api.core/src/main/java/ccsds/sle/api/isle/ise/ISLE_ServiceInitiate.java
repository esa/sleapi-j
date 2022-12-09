package ccsds.sle.api.isle.ise;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_SIState;

/**
 * The interface provides the methods to pass operation invocations and returns
 * to a service instance in the API Service Element. The requests are checked
 * and an error is returned in case of incomplete or inconsistent definitions or
 * if the PDU is not valid in the current state of the service instance. For the
 * definition of the state table see chapter 5. A positive return code of the
 * methods ensures that the PDU has been queued for transmission. It does not
 * indicate that the PDU has been actually transmitted. For the following
 * special PDUs, the interface may return the code SLE_S_SUSPEND indicating that
 * further transfer of data shall be suspended. * TRANSFER DATA Invocation for
 * forward Services * TRANSFER DATA Invocation for Return Services when the
 * delivery mode is either online complete or offline. The method
 * ResumeDataTransfer() will be called on the complementary interface
 * ISLE_ServiceInform when data transfer is again possible.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_ServiceInitiate extends IUnknown
{
    /**
     * Initiate operation invoke.
     * 
     * @param poperation operation as parameter
     * @param seqCount sequence counter
     * @throws SleApiException
     */
    void initiateOpInvoke(ISLE_Operation poperation, long seqCount) throws SleApiException;

    /**
     * Initiate operation return.
     * 
     * @param poperation operation
     * @param seqCount sequence counter
     * @throws SleApiException
     */
    void initiateOpReturn(ISLE_ConfirmedOperation poperation, long seqCount) throws SleApiException;

    /**
     * Get the Service Instance State.
     * 
     * @return
     */
    SLE_SIState getSIState();
}
