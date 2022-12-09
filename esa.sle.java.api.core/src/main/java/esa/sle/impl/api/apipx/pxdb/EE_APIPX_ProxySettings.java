/**
 * @(#) EE_APIPX_ProxySettings.java
 */

package esa.sle.impl.api.apipx.pxdb;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.it.SLE_AppRole;
import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * This class contains attributes for the maximum authentication delay, the
 * transmission queue size, and the role.
 */
public class EE_APIPX_ProxySettings extends EE_APIPX_LoadableElement
{
    /**
     * The acceptable delay between the time credentials have been created and
     * the time of authentication.
     */
    private int authentAccDelay = -1;

    private int maxTraceLength = -1;

    /**
     * The maximum number of PDU's that shall be queued for transmission.
     */
    private int transmissionQueueSize = -1;

    /**
     * The role (initiator/responder)
     */
    private SLE_AppRole role;

    /**
     * This is set once the role attribute has been set.
     */
    private boolean roleSet = false;

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_ProxyRoleKeyword = "PROXY_ROLE";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_DelayKeyword = "AUTHENTICATION_DELAY";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_TransmissionQueueSizeKeyword = "TRANSMIT_QUEUE_SIZE";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_ResponderKeyword = "RESPONDER";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_InitiatorKeyword = "INITIATOR";

    /**
     * This is the keyword for the maximum trace length.
     */
    private final static String CI_MaxTraceLengthKeyword = "MAX_TRACE_LENGTH";


    @SuppressWarnings("unused")
    private EE_APIPX_ProxySettings(final EE_APIPX_ProxySettings right)
    {
        this.authentAccDelay = right.authentAccDelay;
        this.maxTraceLength = right.maxTraceLength;
        this.transmissionQueueSize = right.transmissionQueueSize;
        this.roleSet = right.roleSet;
    }

    public EE_APIPX_ProxySettings()
    {
        this.authentAccDelay = -1;
        this.maxTraceLength = -1;
        this.transmissionQueueSize = -1;
        this.roleSet = false;
    }

    /**
     * Returns the acceptable authentication delay, returned from the database
     * (in seconds).
     */
    public int getAuthentAccDelay()
    {
        return this.authentAccDelay;
    }

    /**
     * Returns the transmission Queue size read from the database
     */
    public int getTransmissionQueueSize()
    {
        return this.transmissionQueueSize;
    }

    /**
     * Returns the role read from the database
     */
    public SLE_AppRole getRole()
    {
        return this.role;
    }

    /**
     * Returns the maximum trace length.
     */
    public int getMaxTraceLength()
    {
        return this.maxTraceLength;
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public boolean acceptValue(final String name, final String value, EE_Database db)
    {
        String maxvalue = Integer.MAX_VALUE + "";
        if (name.equals(CI_ProxyRoleKeyword))
        {
            if (this.roleSet)
            {
                db.setCurrentError(getAlreadyLoadedMsg(name));
                return false;
            }
            if (value.equals(CI_InitiatorKeyword))
            {
                this.role = SLE_AppRole.sleAR_user;
                this.roleSet = true;
                return true;
            }
            else if (value.equals(CI_ResponderKeyword))
            {
                this.roleSet = true;
                this.role = SLE_AppRole.sleAR_provider;
                return true;
            }
            else
            {
                db.setCurrentError("only roles supported are " + CI_InitiatorKeyword + " and " + CI_ResponderKeyword);
                return false;
            }
        }
        else if (name.equals(CI_DelayKeyword))
        {
            if (this.authentAccDelay != -1)
            {
                db.setCurrentError(getAlreadyLoadedMsg(name));
                return false;
            }
            if (value.length() > maxvalue.length())
            {
                db.setCurrentError("the value given for the Authentication delay (" + value + ") is too large ");
                return false;
            }
            else if (value.length() == maxvalue.length() && value.compareTo(maxvalue) > 0)
            {
                db.setCurrentError("the value given for the Authentication delay (" + value + ") is too large ");
                return false;
            }

            HRESULT convOk = HRESULT.S_OK;
            try
            {
                this.authentAccDelay = EE_Database.convIntegral(value);
            }
            catch (SleApiException e)
            {
                convOk = e.getHResult();
            }
            if (convOk != HRESULT.S_OK)
            {
                db.setCurrentError("the value given for the authentication delay (" + value
                                   + ") is not able to be correctly converted into a number");
                return false;
            }
            else
            {
                return true;
            }
        }
        else if (name.equals(CI_TransmissionQueueSizeKeyword))
        {
            if (this.transmissionQueueSize != -1)
            {
                db.setCurrentError(getAlreadyLoadedMsg(name));
                return false;
            }
            if (value.length() > maxvalue.length() && (value.compareTo(maxvalue) > 0))
            {
                db.setCurrentError("the value given for the transmission queue size (" + value + ") is too large.");
                return false;
            }
            else if (value.length() == maxvalue.length() && (value.compareTo(maxvalue) > 0))
            {
                db.setCurrentError("the value given for the transmission queue size (" + value + ") is too large.");
                return false;
            }

            HRESULT convOk = HRESULT.S_OK;
            try
            {
                this.transmissionQueueSize = EE_Database.convIntegral(value);
            }
            catch (SleApiException e)
            {
                convOk = e.getHResult();
            }
            if (convOk != HRESULT.S_OK)
            {
                db.setCurrentError("the value given for the transmission queue size (" + value
                                   + ") is not able to be converted into a number.");
                return false;
            }
            else
            {
                return true;
            }
        }
        else if (name.equals(CI_MaxTraceLengthKeyword))
        {
            if (this.maxTraceLength != -1)
            {
                db.setCurrentError("The value for " + name + " was attempted to be set twice");
                return false;
            }
            else if (value.length() > maxvalue.length() && (value.compareTo(maxvalue) > 0))
            {
                db.setCurrentError("the value given for " + name + " (" + value + ") is too large.");
                return false;
            }
            else if (value.length() == maxvalue.length() && (value.compareTo(maxvalue) > 0))
            {
                db.setCurrentError("the value given for " + name + " (" + value + ") is too large.");
                return false;
            }
            HRESULT convOk = HRESULT.S_OK;
            try
            {
                this.maxTraceLength = EE_Database.convIntegral(value);
            }
            catch (SleApiException e)
            {
                convOk = e.getHResult();
            }
            if (convOk != HRESULT.S_OK)
            {
                db.setCurrentError("the value given for " + name + " (" + value
                                   + ") is not able to be converted into a number.");
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return super.acceptValue(name, value, db);
        }
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public EE_APIPX_LoadableElement acceptListItem(String name, EE_Database db)
    {
        return super.acceptListItem(name, db);
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public boolean isFullyLoaded(EE_Reference<String> diagnostic, EE_APIPX_Database db)
    {

        if (!this.roleSet)
        {

            diagnostic.setReference(getNotLoadedMsg(CI_ProxyRoleKeyword));
            return false;
        }
        else if (this.authentAccDelay <= -1)
        {
            diagnostic.setReference(getNotLoadedMsg(CI_DelayKeyword));
            return false;
        }
        else if (this.transmissionQueueSize <= -1)
        {
            diagnostic.setReference(getNotLoadedMsg(CI_TransmissionQueueSizeKeyword));
            return false;
        }
        else if (this.maxTraceLength <= -1)
        {
            diagnostic.setReference(getNotLoadedMsg(CI_MaxTraceLengthKeyword));
            return false;
        }
        return true;
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public void registerKeywords(EE_APIPX_Database argdb)
    {
        argdb.registerOuterKeyword(CI_ProxyRoleKeyword, this);
        argdb.registerOuterKeyword(CI_DelayKeyword, this);
        argdb.registerOuterKeyword(CI_TransmissionQueueSizeKeyword, this);
        argdb.registerOuterKeyword(CI_MaxTraceLengthKeyword, this);
    }

}
