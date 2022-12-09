package esa.sle.impl.eapi.dcw;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;

/**
 * This interface is used to create and destroy Service Instances when using the
 * DCW. The CreateServiceInstance method creates a Service Instance of the
 * specified service-type and role and returns a reference to its specified
 * interface. It is the responsibility of the client to configure the Service
 * Instance using the ISLE_SIAdmin interface and its service-type specific
 * configuration interface (see [SLE-API] Section 6.8.5).
 */
public interface IDCW_SIFactory extends IUnknown
{
    /**
     * Creates a Service Instance of the specified service-type and role and
     * returns a reference to its specified interface.
     * 
     * @param iid
     * @param srvType service type
     * @param version version
     * @param role role
     * @param maxPendingEvents
     * @return
     * @throws SleApiException
     */
    public <T extends IUnknown> T createServiceInstance(Class<T> iid,
                                                        SLE_ApplicationIdentifier srvType,
                                                        int version,
                                                        SLE_AppRole role,
                                                        int maxPendingEvents) throws SleApiException;

    /**
     * Destroys the Service Instance.
     * 
     * @param psi
     * @throws SleApiException
     */
    public void destroyServiceInstance(IUnknown psi) throws SleApiException;
}
