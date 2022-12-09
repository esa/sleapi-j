package esa.sle.osgi;

import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_TimeSource;
import ccsds.sle.api.isle.ise.ISLE_SIFactory;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;

/**
 * This interface gives access to the SLE API interfaces. The class implementing
 * this interface is {@link esa.sle.osgi.impl.EE_SLE_LibraryInstance}, which can
 * be instantiated using OSGi declarative services by specifying the following
 * properties: - seConfigFilePath: path to the service element config file -
 * proxyConfigFilePath: path to the proxy config file - instanceName: name of
 * the instance As service references, the implementation class
 * EE_SLE_LibraryInstance accepts optionally: - a time source - a reporter
 */
public interface ISLE_LibraryInstance
{

    public static final String SE_CONFIG_PROPERTY = "seConfigFilePath";

    public static final String PROXY_CONFIG_PROPERTY = "proxyConfigFilePath";

    public static final String INSTANCE_NAME_PROPERTY = "instanceName";
    
    public static final String SLE_CONFIG_DIR_SYSTEM_PROPERTY = "ccsds.sle.config.dir";


    /**
     * Returns the library instance name.
     * 
     * @return the library instance name
     */
    String getInstanceName();

    /**
     * Returns the SIFactory that is created by the SLE API builder.
     * 
     * @return the SIFactory
     */
    ISLE_SIFactory getSIFactory();

    /**
     * Returns the UtilFactory that is created by the SLE API builder.
     * 
     * @return the UtilFactory
     */
    ISLE_UtilFactory getUtilFactory();

    /**
     * Returns the registered reporter.
     * 
     * @return
     */
    ISLE_Reporter getReporter();

    /**
     * Returns the time source.
     * 
     * @return
     */
    ISLE_TimeSource getTimeSource();

    /**
     * Returns the Application Role that is read from a configuration file (or
     * Database)
     * 
     * @return
     */
    SLE_AppRole getApplRole();
}
