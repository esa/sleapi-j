package ccsds.sle.api.isle.ipx;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_Locator;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;

/**
 * The interface provides the means to configure the proxy component and to pass
 * it the interfaces needed operationally. All static configuration parameters
 * needed by the proxy are defined in a configuration file. The path name of
 * that file is supplied to the proxy via this interface. In addition, the
 * interface provides methods to register and de-register ports for a specific
 * service instance. These methods are used by the service element when a
 * service instance is created and deleted. Port registration is described in
 * chapter 4. The interface finally provides a method for shutdown of the Proxy.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_ProxyAdmin extends IUnknown
{
    /**
     * Used for configuration.
     * 
     * @param configFilePath file path
     * @param plocator locator
     * @param popFactory operation factory
     * @param putilFactory util factory
     * @param preporter reporter
     * @throws SleApiException
     */
    void configure(String configFilePath,
                   ISLE_Locator plocator,
                   ISLE_OperationFactory popFactory,
                   ISLE_UtilFactory putilFactory,
                   ISLE_Reporter preporter) throws SleApiException;

    /**
     * @throws SleApiException
     */
    void shutDown() throws SleApiException;

    /**
     * Register a Port.
     * 
     * @param sii service instance identifier
     * @param responderPort responder port
     * @throws SleApiException
     */
    int registerPort(ISLE_SII sii, String responderPort) throws SleApiException;

    /**
     * Unregister Port.
     * 
     * @param regId registered port id
     * @throws SleApiException
     */
    void deregisterPort(int regId) throws SleApiException;

    /**
     * Gets the protocol Id.
     * 
     * @return
     */
    String getProtocolId();
}
