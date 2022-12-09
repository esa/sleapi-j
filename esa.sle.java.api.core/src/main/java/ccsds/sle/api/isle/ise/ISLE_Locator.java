package ccsds.sle.api.isle.ise;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.ipx.ISLE_SrvProxyInitiate;
import ccsds.sle.api.isle.iscm.IUnknown;

/**
 * The interface is provided to the Proxy to obtain an interface of the type
 * ISLE_SrvProxyInform when a BIND Invocation has been received. When an error
 * is returned, the Proxy is expected to reject the BIND Invocation.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_Locator extends IUnknown
{
    /**
     * Locate instance
     * 
     * @param passociation association
     * @param pbindop bind operation
     * @return ISLE_SrvProxyInform
     * @throws SleApiException
     */
    ISLE_SrvProxyInform locateInstance(ISLE_SrvProxyInitiate passociation, ISLE_Bind pbindop) throws SleApiException;
}
