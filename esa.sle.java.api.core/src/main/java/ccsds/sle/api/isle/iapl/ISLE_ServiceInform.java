package ccsds.sle.api.isle.iapl;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;

/**
 * The Interface allows passing of operation invocations and returns to the SLE
 * Application for a single service instance. The client of this interface is
 * expected to have checked all PDUs that pass this interface to the level
 * defined for the API Service Element in chapter 4. For data transfer for
 * forward services and for the online complete and offline return services, the
 * interface additionally provides the means to signal to the application that
 * data transfer may continue. Suspension of data transfer is requested via the
 * complementary interface ISLE_ServiceInitiate. Finally the interface provides
 * a means to inform the application when a protocol abort occurs and when the
 * scheduled provision period of the service instance ends.
 * 
 * @version: 1.0, October 2015
 */

public interface ISLE_ServiceInform extends IUnknown
{
    /**
     * Passes an operation invocation to the application
     * 
     * @param poperation the operation invocation
     * @param seqCount the operation sequence number
     * @throws SleApiException
     */
    void informOpInvoke(ISLE_Operation poperation, long seqCount) throws SleApiException;

    /**
     * Passes an operation return to the application
     * 
     * @param poperation the operation return
     * @param seqCount the operation sequence number
     * @throws SleApiException
     */
    void informOpReturn(ISLE_ConfirmedOperation poperation, long seqCount) throws SleApiException;

    /**
     * Enables the reception of events after a previous suspend
     */
    void resumeDataTransfer();

    /**
     * Ends the Service Instance provision period
     */
    void provisionPeriodEnds();

    /**
     * Informs the application when a PROTOCOL-ABORT occurs
     * 
     * @param diagnostic the PROTOCOl-ABORT diagnostic
     * @throws SleApiException
     */
    void protocolAbort(final byte[] diagnostic) throws SleApiException;

}
