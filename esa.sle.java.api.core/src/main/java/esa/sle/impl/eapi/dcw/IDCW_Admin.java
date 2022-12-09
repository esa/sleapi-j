package esa.sle.impl.eapi.dcw;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SEAdmin;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;

/**
 * This interface provides the means to configure and control the DCW component.
 * Clients use the method configure() to register the other components with the
 * DCW. Processing in the DCW and Service Element is initiated using the start()
 * method, and terminated using the terminate() method. The Service Element
 * should be started and terminated via this interface only.
 */

public interface IDCW_Admin extends IUnknown
{
    /**
     * Registers the other components to the DCW.
     * 
     * @param pse Service element Admin
     * @param putilFactory Utility factory
     * @param preporter reporter
     * @throws SleApiException
     */
    public void configure(ISLE_SEAdmin pse, ISLE_UtilFactory putilFactory, ISLE_Reporter preporter) throws SleApiException;

    /**
     * Initiate the processing in the DCW and Service Element.
     * 
     * @throws SleApiException
     */
    public void start() throws SleApiException;

    /**
     * Terminates the processing from the DCW and Service Element.
     * 
     * @throws SleApiException
     */
    public void terminate() throws SleApiException;

    /**
     * ShutDown the processing.
     * 
     * @throws SleApiException
     */
    public void shutdown() throws SleApiException;
}
