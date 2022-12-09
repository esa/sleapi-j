package ccsds.sle.api.isle.iop;

import ccsds.sle.api.isle.it.SLE_AbortOriginator;
import ccsds.sle.api.isle.it.SLE_PeerAbortDiagnostic;

/**
 * The interface provides access to the parameters of the operation PEER-ABORT.
 * Through its inheritance, it provides access to the parameter
 * "invoker credentials". This parameter is not defined for the PEER-ABORT
 * operation and must not be used. The Proxy must ensure that authentication is
 * not applied to the PEER-ABORT operation, even if the parameter is set in the
 * operation object by mistake. In addition to the parameters defined for the
 * SLE operation, objects exporting this interface store the originator of the
 * abort, which can be the peer system, the local Proxy, the local Service
 * Element, or the local Application. This information is not forwarded across
 * the association.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_PeerAbort extends ISLE_Operation
{
    /**
     * Returns the PEER ABORT diagnostic
     * 
     * @return the PEER ABORT diagnostic
     */
    SLE_PeerAbortDiagnostic getPeerAbortDiagnostic();

    /**
     * Sets the PEER ABORT diagnostic
     * 
     * @param diagnostic the PEER ABORT diagnostic
     */
    void setPeerAbortDiagnostic(SLE_PeerAbortDiagnostic diagnostic);

    /**
     * Returns the PEER ABORT originator
     * 
     * @return the PEER ABORT originator
     */
    SLE_AbortOriginator getAbortOriginator();

    /**
     * Sets the PEER ABORT originator
     * 
     * @param originator the PEER ABORT originator
     */
    void setAbortOriginator(SLE_AbortOriginator originator);
}
