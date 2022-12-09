package esa.sle.service.loader;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.ise.ISLE_SIAdmin;
import esa.sle.sicf.si.descriptors.SIDescriptor;

/**
 * This interface defines an OSGi provided service. Objects implementing this
 * interface are provided as services by the service loaded. The class that
 * implements this interface has all the information necessary to create a
 * service instance.
 */
public interface ISLE_LoadedServiceInstance
{

    public static final String SIID_PROPERTY = "siid";


    /**
     * Returns the service instance identifier as string.
     * 
     * @return the service instance identifier
     */
    String getServiceInstanceIdentifier();

    /**
     * Returns the Service Instance administration interface. This method must
     * be called only after registration to the service instance using the
     * register(ISLE_ServiceInform,int) method.
     * 
     * @return the Service Instance Admin interface
     */
    ISLE_SIAdmin getServiceInstance();

    /**
     * Returns the Service Loader that published this service instance.
     * 
     * @return the Service Loader
     */
    ISLE_ServiceLoader getServiceLoader();

    /**
     * Registers the ISLE_ServiceInform to the service instance. This operation
     * causes the creation of the Service Instance by the SLE library.
     * 
     * @param servInform
     */
    void register(ISLE_ServiceInform servInform, int version);

    /**
     * Deregisters the ISLE_ServiceInform. If the service instance has been
     * marked by the service loader to be deallocated, then the service instance
     * is destroyed.
     * @throws SleApiException Thrown if the SI is not in the correct state for destruction (READ or ACTIVE)
     */
    void deregister() throws SleApiException;

    /**
     * Updates the descriptor of the service instance. This method is used
     * internally by the service loader to inform a service instance that some
     * configuration parameters have been updated. If the service instance was
     * already accessed by the application, this method does not succeed: it
     * raises a message on the reporter interface and return.
     */
    void updateDescriptor(SIDescriptor siDescriptor);

}
