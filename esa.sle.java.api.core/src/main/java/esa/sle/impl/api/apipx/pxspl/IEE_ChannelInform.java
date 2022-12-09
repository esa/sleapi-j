/**
 * @(#) IEE_ChannelInform.java
 */

package esa.sle.impl.api.apipx.pxspl;

import ccsds.sle.api.isle.iscm.IUnknown;
import esa.sle.impl.api.apipx.pxtml.EE_APIPX_ISP1ProtocolAbortDiagnostics;

/**
 * The interface is provided to the client for transfer of encoded PDUs from a
 * single channel, residing in the Transport Mapping Layer, to a single
 * association. In addition it provides methods that comprise the TCP primitives
 * for the Transport Mapping Layer as specified in reference [TCP-PROXY].
 */
public interface IEE_ChannelInform extends IUnknown
{
    /**
     * Receives an encoded PDU.
     */
    void rcvSLEPDU(byte[] data);

    /**
     * Receives a CONNECT request.
     */
    void rcvConnect();

    /**
     * Receives a PEER_ABORT request.
     */
    void rcvPeerAbort(int diagnostic, boolean originatorIsLocal);

    /**
     * Receives a PROTOCOL_ABORT request.
     */
    void rcvProtocolAbort(EE_APIPX_ISP1ProtocolAbortDiagnostics diagnostic);

    /**
     * Request a resumption of the sending.
     */
    void resumeXmit();

    /**
     * Request the suspension of the sending.
     */
    void suspendXmit();

}
