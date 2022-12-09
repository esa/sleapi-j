package esa.sle.service.loader;

import java.util.List;

import esa.sle.sicf.si.descriptors.SIDescriptor;

/**
 * This interface manages the Service Instance Repository used to handle the
 * list of existing service instances descriptors. The service descriptors are
 * objects that contains data necessary to configure a service instance once it
 * is created.
 */
public interface ISLE_SIRepository
{
    /**
     * Subscribes to the SI Repository.
     * 
     * @param subscriber
     */
    void subscribe(ISLE_SIRepositoryInform subscriber);

    /**
     * Unsubscribes to the SI Repository.
     * 
     * @param subscriber
     */
    void unsubscribe(ISLE_SIRepositoryInform subscriber);

    /**
     * Returns the list of Service Instance descriptors currently present in the
     * repository.
     * 
     * @return the list of Service Instance descriptors
     */
    List<SIDescriptor> getServiceInstanceDescriptors();
}
