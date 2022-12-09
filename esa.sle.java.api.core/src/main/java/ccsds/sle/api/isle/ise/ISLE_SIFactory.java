package ccsds.sle.api.isle.ise;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;

/**
 * The interface allows creation of service instances for a specified service
 * type and for a specified role (SLE Service Provider or SLE Service User).
 * Following creation, the service instance must be configured using its
 * administrative interface. When the association is no longer needed, the
 * service element component must be instructed to destroy the service instance.
 * In addition, clients must make sure that all references on all interfaces of
 * the service instance have been released.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_SIFactory extends IUnknown
{
    /**
     * Create service instance.
     * 
     * @param iid service instance id
     * @param srvType service type
     * @param version version to be used
     * @param role application role
     * @param pclientIf service inform
     * @return
     * @throws SleApiException
     */
    <T extends IUnknown> T createServiceInstance(Class<T> iid,
                                                 SLE_ApplicationIdentifier srvType,
                                                 int version,
                                                 SLE_AppRole role,
                                                 ISLE_ServiceInform pclientIf) throws SleApiException;

    /**
     * Destroy the Service Instance.
     * 
     * @param psi
     * @throws SleApiException
     */
    void destroyServiceInstance(IUnknown psi) throws SleApiException;
}
