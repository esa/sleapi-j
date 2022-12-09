package ccsds.sle.api.isle.ise;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import ccsds.sle.api.isle.iutl.ISLE_Time;

/**
 * The interface is provided for configuration of service instances. It can be
 * used for instances supporting the provider role or the user role. For
 * instances supporting the user role not all parameters need to be set. Clients
 * must specify the individual parameters using the method foreseen for the
 * parameter. Depending on the service type, further parameters may have to be
 * supplied using the service type specific configuration interface. When all
 * parameters have been supplied, the method ConfigCompleted() must be called.
 * The service instance then verifies that the configuration is complete and
 * consistent and performs all actions required to start nominal operation. If
 * the method ConfigCompleted() returns with success, the service instance is
 * ready for operation. As a general precondition configuration parameters must
 * not be modified after a successful return of the method ConfigCompleted. The
 * effect of an attempt to set a parameter when the initial configuration has
 * completed is undefined. The interface provides read access to all
 * configuration parameters. The value returned by a call to the read methods
 * before configuration has been completed is undefined.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_SIAdmin extends IUnknown
{
    /**
     * Sets the service instance id.
     * 
     * @param id
     */
    void setServiceInstanceId(ISLE_SII id);

    /**
     * Puts the service instance Id.
     * 
     * @param id
     */
    void putServiceInstanceId(ISLE_SII id);

    /**
     * Sets peer identifier.
     * 
     * @param id
     */
    void setPeerIdentifier(String id);

    /**
     * Sets the provision period.
     * 
     * @param start
     * @param stop
     */
    void setProvisionPeriod(ISLE_Time start, ISLE_Time stop);

    /**
     * Sets bind initiative.
     * 
     * @param role
     */
    void setBindInitiative(SLE_AppRole role);

    /**
     * Sets responder port identifier.
     * 
     * @param portId
     */
    void setResponderPortIdentifier(String portId);

    /**
     * Set return timeout.
     * 
     * @param timeout
     */
    void setReturnTimeout(int timeout);

    /**
     * Verifies that the configuration is complete and consistent.
     * 
     * @throws SleApiException
     */
    void configCompleted() throws SleApiException;

    /**
     * Gets the service type.
     * 
     * @return
     */
    SLE_ApplicationIdentifier getServiceType();

    /**
     * Gets the version.
     * 
     * @return
     */
    int getVersion();

    /**
     * Gets the role.
     * 
     * @return
     */
    SLE_AppRole getRole();

    /**
     * Gets service instance identifier.
     * 
     * @return
     */
    ISLE_SII getServiceInstanceIdentifier();

    /**
     * Gets peer identifier.
     * 
     * @return
     */
    String getPeerIdentifier();

    /**
     * Gets the start provision time.
     * 
     * @return
     */
    ISLE_Time getProvisionPeriodStart();

    /**
     * Gets the stop provision period.
     * 
     * @return
     */
    ISLE_Time getProvisionPeriodStop();

    /**
     * Gets bind initiative.
     * 
     * @return
     */
    SLE_AppRole getBindInitiative();

    /**
     * Gets responder port identifier.
     * 
     * @return
     */
    String getResponderPortIdentifier();

    /**
     * Gets return timeout.
     * 
     * @return
     */
    int getReturnTimeout();
}
