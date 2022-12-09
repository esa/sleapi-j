package esa.sle.service.loader;

import esa.sle.sicf.si.descriptors.SIDescriptor;

/**
 * This interface is used by the ISLE_SIRepository implementation to notify
 * changes in the repository. One implementor of this interface is the service
 * loader.
 */
public interface ISLE_SIRepositoryInform
{
    /**
     * Notifies that a new service instance has been added to the repository.
     * 
     * @param serviceInstance
     */
    void onServiceAdded(SIDescriptor serviceInstanceDescr);

    /**
     * Notifies that a service instance has been removed from the repository.
     * 
     * @param serviceInstance identifier
     */
    void onServiceRemoved(String serviceInstanceId);

    /**
     * Notifies that a service instance has been updated.
     * 
     * @param serviceInstance
     */
    void onServiceUpdated(SIDescriptor serviceInstanceDescr);
}
