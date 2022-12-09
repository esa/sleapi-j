package ccsds.sle.api.isle.ise;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.ipx.ISLE_ProxyAdmin;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_BindRole;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;

/**
 * The interface provides the means to configure the service element component
 * and to pass it the interfaces needed operationally. All static configuration
 * parameters needed by the component are defined in a configuration file. The
 * path name of that file is supplied to the proxy via this interface. Clients
 * must first call the method Configure() and then call AddProxy() to pass a
 * pointer to the proxy component for every proxy that shall be supported. The
 * interface finally provides a method for shutdown of the Service Element.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_SEAdmin extends IUnknown
{
    /**
     * Configures the service element component.
     * 
     * @param configFilePath
     * @param popFactory
     * @param putilFactory
     * @param preporter
     * @throws SleApiException
     */
    void configure(String configFilePath,
                   ISLE_OperationFactory popFactory,
                   ISLE_UtilFactory putilFactory,
                   ISLE_Reporter preporter) throws SleApiException;

    /**
     * Pass a pointer to proxy.
     * 
     * @param protocolId
     * @param role
     * @param pproxy
     * @throws SleApiException
     */
    void addProxy(String protocolId, SLE_BindRole role, ISLE_ProxyAdmin pproxy) throws SleApiException;

    /**
     * Shut Down the service element.
     * 
     * @throws SleApiException
     */
    void shutDown() throws SleApiException;
}
