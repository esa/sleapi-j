package ccsds.sle.api.isle.ise;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;

/**
 * The interface is provided to the Proxy for transfer of operation invocations
 * and returns on a single association. In addition, it provides a method to
 * signal transfer of a PDU if that has been requested via the complementary
 * interface ISLE_SrvProxyInitiate. The PDUs passed via this interface are
 * generally unchecked. The only checks performed by the Proxy are that the PDU
 * is supported by the service type and is properly coded. Reception of an
 * invalid PDU via this interface shall not cause the function to be rejected.
 * The provider of the interface must either generate the appropriate operation
 * return or abort the association. Calls to this interface shall only be
 * rejected when the client misbehaves. For instance, passing of an invocation
 * other than BIND in the state unbound is such an error.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_SrvProxyInform extends IUnknown
{
    /**
     * Inform operation invoke.
     * 
     * @param poperation
     * @param seqCount
     * @throws SleApiException
     */
    void informOpInvoke(ISLE_Operation poperation, long seqCount) throws SleApiException;

    /**
     * Inform operation return.
     * 
     * @param poperation
     * @param seqCount
     * @throws SleApiException
     */
    void informOpReturn(ISLE_ConfirmedOperation poperation, long seqCount) throws SleApiException;

    /**
     * Pdu transmitted.
     * 
     * @param poperation
     * @throws SleApiException
     */
    void pduTransmitted(ISLE_Operation poperation) throws SleApiException;

    /**
     * Protocol abort.
     * 
     * @param diagnostic
     * @throws SleApiException
     */
    void protocolAbort(byte[] diagnostic) throws SleApiException;
}
