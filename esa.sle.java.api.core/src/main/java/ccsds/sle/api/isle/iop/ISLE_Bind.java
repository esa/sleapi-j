package ccsds.sle.api.isle.iop;

import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_BindDiagnostic;
import ccsds.sle.api.isle.iutl.ISLE_SII;

/**
 * The interface provides access to the parameters of the operation BIND.Through
 * its inheritance, it provides access to the parameter "invocation identifier".
 * This parameter is not defined for the BIND operation and must not be used.
 * The API Proxy and the API Service Element must exclude this operation from
 * the checks related to invocation identifiers.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_Bind extends ISLE_ConfirmedOperation
{
    /**
     * Returns the initiator identifier
     * 
     * @return the initiator identifier
     */
    String getInitiatorIdentifier();

    /**
     * Returns the responder identifier
     * 
     * @return the responder identifier
     */
    String getResponderIdentifier();

    /**
     * Returns the responder port identifier
     * 
     * @return the responder port identifier
     */
    String getResponderPortIdentifier();

    /**
     * Returns the Service Instance identifier
     * 
     * @return the Service Instance identifier
     */
    ISLE_SII getServiceInstanceId();

    /**
     * Sets the initiator identifier
     * 
     * @param id the initiator identifier
     */
    void setInitiatorIdentifier(String id);

    /**
     * Sets the responder identifier
     * 
     * @param id the responder identifier
     */
    void setResponderIdentifier(String id);

    /**
     * Sets the responder port identifier
     * 
     * @param port the responder port identifier
     */
    void setResponderPortIdentifier(String port);

    /**
     * Sets the Service Instance identifier
     * 
     * @param siid the Service Instance identifier
     */
    void setServiceInstanceId(ISLE_SII siid);

    /**
     * Puts the Service Instance identifier
     * 
     * @param psiid the Service Instance identifier
     */
    void putServiceInstanceId(ISLE_SII psiid);

    /**
     * Returns the Service type
     * 
     * @return the Service Type
     */
    SLE_ApplicationIdentifier getServiceType();

    /**
     * Returns the version number
     * 
     * @return the version number
     */
    int getVersionNumber();

    /**
     * Sets the Service type
     * 
     * @param serviceType the service type
     */
    void setServiceType(SLE_ApplicationIdentifier serviceType);

    /**
     * Sets the version number
     * 
     * @param version the version number
     */
    void setVersionNumber(int version);

    /**
     * Returns the BIND diagnostic
     * 
     * @return the BIND diagnostic
     */
    SLE_BindDiagnostic getBindDiagnostic();

    /**
     * Sets the BIND diagnostic
     * 
     * @param diagnostic the BIND diagnostic
     */
    void setBindDiagnostic(SLE_BindDiagnostic diagnostic);
}
