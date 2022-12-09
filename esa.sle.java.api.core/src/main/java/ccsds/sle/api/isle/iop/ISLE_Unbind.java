package ccsds.sle.api.isle.iop;

import ccsds.sle.api.isle.it.SLE_UnbindReason;

/**
 * The interface provides access to the parameters of the operation UNBIND.
 * Through its inheritance, it provides access to the parameters "result" and
 * "invocation identifier". These parameters are not defined for UNBIND
 * operation and must not be used. The API Proxy and the API Service Element
 * must exclude this operation from the checks related to invocation
 * identifiers.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_Unbind extends ISLE_ConfirmedOperation
{
    /**
     * Returns the UNBIND reason
     * 
     * @return the UNBIND reason
     */
    SLE_UnbindReason getUnbindReason();

    /**
     * Sets the UNBIND reason
     * 
     * @param reason the UNBIND reason
     */
    void setUnbindReason(SLE_UnbindReason reason);
}
