/**
 * @(#) EE_APIPX_PeerApplData.java
 */

package esa.sle.impl.api.apipx.pxdb;

import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class holds all configuration parameters that are needed to identify and
 * authenticate a peer application.
 */
public class EE_APIPX_PeerApplData extends EE_APIPX_LoadableElement
{
    /**
     * The identifier of the peer application
     */
    private String id = "";

    /**
     * The authentication mode used for the association serving the peer
     * application.
     */
    private SLE_AuthenticationMode authenticationMode;

    /**
     * Indicates whether the authentication mode has been set or not.
     */
    private boolean modeSet = false;

    /**
     * The password of the user.
     */
    private byte[] password = new byte[0];

    /**
     * Pointer to the aggregatng list.
     */
    private EE_APIPX_PeerApplDataList parentList = null;

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_IDKeyword = "ID";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_AuthModeKeyword = "AUTHENTICATION_MODE";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_passwordKeyword = "PASSWORD";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_AUTHMODEBINDKeyword = "BIND";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_AUTHMODENONEKeyword = "NONE";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_AUTHMODEALLKeyword = "ALL";


    public EE_APIPX_PeerApplData(final EE_APIPX_PeerApplData right)
    {
        this.modeSet = right.modeSet;
        this.password = right.password;
        this.parentList = right.parentList;
    }

    public EE_APIPX_PeerApplData(EE_APIPX_PeerApplDataList parentList)
    {
        this.modeSet = false;
        this.password = new byte[0];
        ;
        this.parentList = parentList;
    }

    /**
     * Returns the id of the peer Application.
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * @FunctionReturns the authentication mode of the peer application.@EndFunction
     */
    public SLE_AuthenticationMode getAuthenticationMode()
    {
        return this.authenticationMode;
    }

    /**
     * Returns the password of the Peer Application. This is not null
     * terminated. The return value should not be deallocated.
     */
    public byte[] getPassword()
    {
        return this.password;
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public boolean acceptValue(final String name, final String value, EE_Database db)
    {
        if (name.equals(CI_IDKeyword))
        {
            if (!this.id.isEmpty())
            {
                db.setCurrentError(getAlreadyLoadedMsg(name));
                return false;
            }
            if ((value.length() < 3) || (value.length() > 16))
            {
                db.setCurrentError("the value for " + CI_IDKeyword + " must be between 3 and 16 characters long.");
                return false;
            }
            this.id = value;
            if (this.parentList.getPeerApplDataItemByID(this.id) != null)
            {
                // id is already there
                db.setCurrentError(getDuplicateMsg(CI_IDKeyword, this.id));
                return false;
            }
            return true;
        }
        else if (name.equals(CI_AuthModeKeyword))
        {
            if (this.modeSet)
            {
                db.setCurrentError(getAlreadyLoadedMsg(name));
                return false;
            }
            this.modeSet = true;
            if (value.equals(CI_AUTHMODEBINDKeyword))
            {
                this.authenticationMode = SLE_AuthenticationMode.sleAM_bindOnly;
                return true;
            }
            else if (value.equals(CI_AUTHMODENONEKeyword))
            {
                this.authenticationMode = SLE_AuthenticationMode.sleAM_none;
                return true;
            }
            else if (value.equals(CI_AUTHMODEALLKeyword))
            {
                this.authenticationMode = SLE_AuthenticationMode.sleAM_all;
                return true;
            }
            else
            {
                this.modeSet = false;
                db.setCurrentError(name + " must be either " + CI_AUTHMODEBINDKeyword + " or " + CI_AUTHMODENONEKeyword
                                   + " or " + CI_AUTHMODEALLKeyword + " (currently set to )" + value);
                return false;
            }

        }
        else if (name.equals(CI_passwordKeyword))
        {
            if (this.password.length > 0)
            {
                db.setCurrentError(getAlreadyLoadedMsg(name));
                return false;
            }
            this.password = EE_GenStrUtil.hexToBin(value);
            if (this.password.length == 0)
            {
                db.setCurrentError("the value for "
                                   + CI_passwordKeyword
                                   + " is not a valid hexadecimal string - the length must be divisible by 2 and the digits must be 0-9, or a-f or A-F.");
                return false;
            }
            if ((this.password.length < 6) || (this.password.length > 16))
            {
                db.setCurrentError("the value for "
                                   + CI_passwordKeyword
                                   + " has a not acceptable size - the length must be between 6 and 16 characters (or between 12 and 32 hexadecimal 'nibbles').");
                return false;
            }
            return true;
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

        if (this.id.isEmpty())
        {
            diagnostic.setReference(getNotLoadedMsg(CI_IDKeyword));
            return false;
        }
        else if (!this.modeSet)
        {
            diagnostic.setReference(getNotLoadedMsg(CI_AuthModeKeyword) + " for " + this.id);
            return false;
        }
        else if (this.password.length <= 0)
        {
            diagnostic.setReference(getNotLoadedMsg(CI_passwordKeyword) + " for " + this.id);
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

    }

}
