/**
 * @(#) EE_SLE_UtilityFactory.java
 */

package esa.sle.impl.api.apiut;

import java.util.HashMap;
import java.util.Map;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_TimeSource;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.iutl.ISLE_Credentials;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import ccsds.sle.api.isle.iutl.ISLE_SecAttributes;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;

/**
 * The class provides an implementation of the interface ISLE_UtilityFactory as
 * specified in reference [SLE-API] for the component class 'Utility Factory'.
 * The class creates an instance of the Utility Factory. If the pointer to the
 * supplied time-source interface ISLE_TimeSource is not NULL, the
 * implementation of the ISLE_Time interface uses the supplied time source.
 * Otherwise the function creates an object of the class EE_SLE_TimeSource,
 * which is used instead of the external time source. Note that the created
 * object is a singleton and as such only created once.
 */
public class EE_SLE_UtilityFactory implements ISLE_UtilFactory
{

    /**
     * The unique instance of this class
     */
    private static Map<String, EE_SLE_UtilityFactory> instanceMap = new HashMap<>();

    /**
     * The time-source interface to be used for the creation of EE_SLE_Time.
     */
    private final ISLE_TimeSource timeSource;


    /**
     * This method is called once to create the EE_SLE_UtilityFactory instance
     */
    public static synchronized void initialiseInstance(String instanceKey, ISLE_TimeSource source)
    {
    	EE_SLE_UtilityFactory instance = instanceMap.get(instanceKey);
        // If the provided source is null, create an internal time source
        ISLE_TimeSource its = source;
        if (its == null)
        {
            its = new EE_SLE_TimeSource();
        }
        if (instance == null)
        {
            instance = new EE_SLE_UtilityFactory(its);
            instanceMap.put(instanceKey, instance);
        }
    }

    /**
     * This method is called every time the EE_SLE_Utilityfactory instance is
     * needed
     * 
     * @return
     */
    public static EE_SLE_UtilityFactory getInstance(String instanceKey)
    {
    	EE_SLE_UtilityFactory instance = instanceMap.get(instanceKey);
        if (instance == null)
        {
            throw new IllegalStateException("The initialise method has never been called and the instance never created for instance " + instanceKey);
        }
        return instance;
    }

    /**
     * Constructor with no arguments.
     */
    private EE_SLE_UtilityFactory()
    {
        this.timeSource = null;
    }

    /**
     * Private constructor called by the initialiseInstance method.
     * 
     * @param source
     */
    private EE_SLE_UtilityFactory(ISLE_TimeSource source)
    {
        this.timeSource = source;
    }

    /**
     * Copy constructor.
     * 
     * @param ufactory
     */
    private EE_SLE_UtilityFactory(EE_SLE_UtilityFactory right)
    {
        this.timeSource = right.timeSource;
    }

    /**
     * @param iid
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_UtilFactory.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    @Override
    public <T extends ISLE_Time> T createTime(Class<T> iid) throws SleApiException
    {
        EE_SLE_Time t = new EE_SLE_Time(this.timeSource);
        T res = t.queryInterface(iid);
        if (res == null)
        {
            throw new SleApiException(HRESULT.E_NOINTERFACE, "Specified interface not found when creating time.");
        }
        return res;
    }
    
    @Override
    public <T extends ISLE_Time> T createTime(Class<T> iid, byte[] cdsTime) throws SleApiException
    {
        EE_SLE_Time t = new EE_SLE_Time(this.timeSource, cdsTime);
        T res = t.queryInterface(iid);
        if (res == null)
        {
            throw new SleApiException(HRESULT.E_NOINTERFACE, "Specified interface not found when creating time.");
        }
        return res;
    }
    
    @Override
    public <T extends ISLE_SII> T createSII(Class<T> iid) throws SleApiException
    {
        EE_SLE_SII sii = new EE_SLE_SII();
        T res = sii.queryInterface(iid);
        if (res == null)
        {
            throw new SleApiException(HRESULT.E_NOINTERFACE, "Specified interface not found when creating sii");
        }
        return res;
    }

    @Override
    public <T extends ISLE_Credentials> T createCredentials(Class<T> iid) throws SleApiException
    {
        EE_SLE_Credentials c = new EE_SLE_Credentials();
        T res = c.queryInterface(iid);
        if (res == null)
        {
            throw new SleApiException(HRESULT.E_NOINTERFACE, "Specified interface not found when creating credentials");
        }
        return res;
    }

    /**
     * @throws SleApiException
     */
    @Override
    public <T extends ISLE_SecAttributes> T createSecAttributes(Class<T> iid) throws SleApiException
    {
        EE_SLE_SecAttributes s = new EE_SLE_SecAttributes(this);
        T res = s.queryInterface(iid);
        if (res == null)
        {
            throw new SleApiException(HRESULT.E_NOINTERFACE,
                                      "Specified interface not found when creating secAttributes");
        }
        return res;
    }
}
