/**
 * @(#) EE_APIPX_LocalApplData.java
 */

package esa.sle.impl.api.apipx.pxdb;

import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class holds all configuration parameters that are needed to identify the
 * local application.
 */
public class EE_APIPX_LocalApplData extends EE_APIPX_LoadableElement
{
    /**
     * The identifier of the local application
     */
    private String id = "";

    /**
     * The password of the local application
     */
    private byte[] password = null;

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_LocalIDKeyword = "LOCAL_ID";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_LocalPasswordKeyword = "LOCAL_PASSWORD";


    @SuppressWarnings("unused")
    private EE_APIPX_LocalApplData(final EE_APIPX_LocalApplData right)
    {
        this.password = right.password;
    }

    public EE_APIPX_LocalApplData()
    {
        this.password = null;
    }

    /**
     * Returns the id of the local Application.
     */
    public String getID()
    {
        return this.id;
    }

    /**
     * Returns the password of the Application. This is not null terminated. The
     * return value should not be deallocated.
     */
    public byte[] getPassword()
    {
        return this.password;
    }

    /**
     * nRefer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public boolean acceptValue(final String name, final String value, EE_Database db)
    {
        if (name.equals(CI_LocalIDKeyword))
        {
            if (!this.id.isEmpty())
            {
                db.setCurrentError(getAlreadyLoadedMsg(name));
                return false;
            }
            if ((value.length() < 3) || (value.length() > 16))
            {
                db.setCurrentError("the value for " + CI_LocalIDKeyword + " must be between 3 and 16 characters long.");
                return false;
            }
            this.id = value;
            return true;
        }
        else if (name.equals(CI_LocalPasswordKeyword))
        {
            if (this.password != null)
            {
                db.setCurrentError(getAlreadyLoadedMsg(name));
                return false;
            }
            this.password = EE_GenStrUtil.hexToBin(value);
            if (this.password == null || this.password.length == 0)
            {
                db.setCurrentError("the value for "
                                   + CI_LocalPasswordKeyword
                                   + " is not a valid hexadecimal string - the length must be divisible by 2 and the digits must be 0-9, or a-f or A-F.");
                return false;
            }
            if ((this.password.length < 6) || (this.password.length > 16))
            {
                db.setCurrentError("the value for "
                                   + CI_LocalPasswordKeyword
                                   + " has a not acceptable size - the length must be between 6 and 16 characters (or between 12 and 32 hexadecimal 'nibbles').");
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
        if (this.id.isEmpty())
        {
            diagnostic.setReference(getNotLoadedMsg(CI_LocalIDKeyword));
            return false;
        }
        else if (this.password.length <= 0)
        {
            diagnostic.setReference(getNotLoadedMsg(CI_LocalPasswordKeyword));
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public void registerKeywords(EE_APIPX_Database argdb)
    {
        argdb.registerOuterKeyword(CI_LocalIDKeyword, this);
        argdb.registerOuterKeyword(CI_LocalPasswordKeyword, this);
    }

}
