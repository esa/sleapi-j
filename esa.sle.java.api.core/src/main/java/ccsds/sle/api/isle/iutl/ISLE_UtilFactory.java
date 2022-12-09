package ccsds.sle.api.isle.iutl;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iscm.IUnknown;

/**
 * The Utility Factory provides the means to create a SLE Utility object with a
 * default initialization. The factory uses the interface identifier to verify
 * that it can create the requested version of the object. If the IID is
 * unknown, the factory returns an error.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_UtilFactory extends IUnknown
{

    /**
     * Creates time.
     * 
     * @param iid
     * @return
     * @throws SleApiException
     */
    <T extends ISLE_Time> T createTime(Class<T> iid) throws SleApiException;

    /**
     * Creates time.
     * 
     * @param iid
     * @param cdsTime The CDS coded time with usec or pico second precision used for initialisation
     * @return
     * @throws SleApiException
     */
    <T extends ISLE_Time> T createTime(Class<T> iid, byte[] cdsTime) throws SleApiException;
    
    /**
     * Creates service instance identifier.
     * 
     * @param iid
     * @return
     * @throws SleApiException
     */
    <T extends ISLE_SII> T createSII(Class<T> iid) throws SleApiException;

    /**
     * Creates credentials.
     * 
     * @param iid
     * @return
     * @throws SleApiException
     */
    <T extends ISLE_Credentials> T createCredentials(Class<T> iid) throws SleApiException;

    /**
     * Creates sec attributes.
     * 
     * @param iid
     * @return
     * @throws SleApiException
     */
    <T extends ISLE_SecAttributes> T createSecAttributes(Class<T> iid) throws SleApiException;
}
