package ccsds.sle.api.isle.icc;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iscm.IUnknown;

/**
 * The interface is used to control processing of a component providing the
 * behavior "Concurrent Flows of Control" as defined in chapter 4. Processing of
 * the component is started with the method StartConcurent The function checks
 * the configuration and returns as soon as processing within the component has
 * been started.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_Concurrent extends IUnknown
{
    /**
     * Starts the processing of the component
     * 
     * @throws SleApiException
     */
    void startConcurrent() throws SleApiException;

    /**
     * Terminates the processing of the component
     * 
     * @throws SleApiException
     */
    void terminateConcurrent() throws SleApiException;
}
