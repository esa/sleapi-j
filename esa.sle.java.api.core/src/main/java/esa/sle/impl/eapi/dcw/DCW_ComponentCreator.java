package esa.sle.impl.eapi.dcw;

import ccsds.sle.api.isle.iscm.IUnknown;
import esa.sle.impl.api.apiut.EE_SLE_LibraryInstance;

/**
 * The class creates an instance of the Down Call Wrapper via the function
 * ESLE_CreateDownCall Wrapper(), which creates an object of the class EE_DCW_
 * DownCallWrapper. Note that the created object is a singleton. Every
 * subsequent function call uses the originally created object to request the
 * desired interface.
 */
public class DCW_ComponentCreator
{
	/**
	 * Kept for backward compatibility.
	 * 
	 * @param iid
	 * @return
	 */
	public static <T extends IUnknown> T createDownCallWrapper(Class<T> iid)
    {
        return createDownCallWrapper(EE_SLE_LibraryInstance.LIBRARY_INSTANCE_KEY, iid);
    }
	
    public static <T extends IUnknown> T createDownCallWrapper(String instanceKey, Class<T> iid)
    {
        EE_DCW_DownCallWrapper pdcw = EE_DCW_DownCallWrapper.getDCW(instanceKey);
        return pdcw.queryInterface(iid);
    }
}
