package ccsds.sle.api.isle.ipx;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SrvProxyInform;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;

/**
 * The interface allows creation of associations that take the initiator role
 * for the BIND operation. Associations created via this interface can be used
 * for several consecutive associations for the same service instance. When the
 * association is no longer needed, the proxy must be instructed to destroy the
 * association. In addition, clients must make sure that all references on the
 * interface have been released.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_AssocFactory extends IUnknown
{
    /**
     * Creates an association.
     * 
     * @param iid the instance that should be created
     * @param srvType service type
     * @param pclientIf service Proxy inform
     * @return association
     * @throws SleApiException
     */
    <T extends IUnknown> T createAssociation(Class<T> iid,
                                             SLE_ApplicationIdentifier srvType,
                                             ISLE_SrvProxyInform pclientIf) throws SleApiException;

    /**
     * Destroys an association
     * 
     * @param passoc
     * @throws SleApiException
     */
    void destroyAssociation(IUnknown passoc) throws SleApiException;
}
