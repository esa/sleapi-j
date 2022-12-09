/**
 * @(#) IEE_Binder.java
 */

package esa.sle.impl.api.apipx.pxspl;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The interface is provided to the client for port registration and
 * de-registration.
 */
public interface IEE_Binder extends IUnknown
{
    /**
     * Registers the port. S_OK The port has been registered. SLE_E_DUPLICATE
     * Duplicate registration. E_FAIL The registration fails due to a further
     * unspecified error.
     */
    HRESULT registerPort(ISLE_SII ssid, String portId, EE_Reference<Integer> regId);

    /**
     * Deregisters the port.@EndFunction S_OK The port has been deregistered.
     * SLE_E_UNKNOWN The port was not registered. E_FAIL The deregistration
     * fails due to a further unspecified error.
     */
    HRESULT deregisterPort(int regId);

}
