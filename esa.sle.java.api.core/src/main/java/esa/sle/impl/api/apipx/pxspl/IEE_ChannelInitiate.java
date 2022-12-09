/**
 * @(#) IEE_ChannelInitiate.java
 */

package esa.sle.impl.api.apipx.pxspl;

import java.net.ServerSocket;
import java.net.Socket;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iscm.IUnknown;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_Database;

/**
 * The interface is provided to the client for transfer of encoded PDUs from a
 * single association to a single channel, residing in the Transport Mapping
 * Layer. In addition it provides methods that comprise the TCP primitives for
 * the Transport Mapping Layer as specified in reference [TCP-PROXY].
 */
public interface IEE_ChannelInitiate extends IUnknown
{
    /**
     * Sends an encoded PDU.
     * 
     * @param data
     * @param lg
     * @param last
     */
    HRESULT sendSLEPDU(byte[] data, boolean last);

    /**
     * Sends a DISCONNECT request.
     */
    void sendDisconnect();

    /**
     * Sends a CONNECT request.
     * 
     * @param rspPortId
     */
    void sendConnect(String rspPortId);

    /**
     * Sends a PEER_ABORT request.
     * 
     * @param diagnostic
     */
    void sendPeerAbort(int diagnostic);

    /**
     * Sends a RESET request.
     */
    void sendReset();

    /**
     * Request the suspension of the receiving.
     */
    void suspendReceive();

    /**
     * Request a resumption of the receiving.
     */
    void resumeReceive();

    /**
     * Set the ChannelInform interface.
     * 
     * @param channelInform
     */
    void setChannelInform(IEE_ChannelInform channelInform);

    /**
     * Configure the ChannelInitiate interface.
     * 
     * @param preporter
     * @param pdatabase
     */
    void configure(ISLE_Reporter preporter, EE_APIPX_Database pdatabase);

    /**
     * Initialise the TCP Socket in the Channel.
     * 
     * @param pSock
     */
    void initialise(Socket pSock, ServerSocket sSock);

    /**
     * 
     */
    void dispose();

}
