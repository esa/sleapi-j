package esa.sle.service.loader;

import esa.sle.osgi.ISLE_LibraryInstance;

/**
 * This interface defines the service loader. Objects implementing this
 * interface collect all the necessary data to create the service instances. The
 * predefined implementation of this interface is the class
 * EE_SLE_ServiceLoader. This class requires the property "name" to be set when
 * publishing it using declarative services.
 */
public interface ISLE_ServiceLoader
{
    public static final String NAME_PROPERTY = "name";


    /**
     * Returns the Service Loader name as provided in the declarative service.
     * 
     * @return the Service Loader name
     */
    String getName();

    /**
     * Returns the Library Instance that contains the information from the
     * configuration files and the SLE administration interfaces.
     * 
     * @return the Library instance reference
     */
    ISLE_LibraryInstance getLibraryInstance();

    /**
     * Returns the SI Repository that contains the information of the available
     * service instances.
     * 
     * @return the SI Repository reference
     */
    ISLE_SIRepository getSIRepository();

}
