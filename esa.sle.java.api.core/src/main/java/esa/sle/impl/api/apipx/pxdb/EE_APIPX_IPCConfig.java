/**
 * @(#) EE_APIPX_IPCConfig.java
 */

package esa.sle.impl.api.apipx.pxdb;

import ccsds.sle.api.isle.it.SLE_AppRole;
import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class holds all attributes that are needed for the configuration of the
 * inter process communication. For a unix system these attributes would be pipe
 * names.
 */
public class EE_APIPX_IPCConfig extends EE_APIPX_LoadableElement
{
    /**
     * The address, which is used to transfer encoded SLE PDUs to/from the
     * Communication Server from/to the SLE application.
     */
    private String serviceAddress;

    /**
     * This is set when the service address is parsed in.
     */
    private boolean serviceAddressSet = false;

    /**
     * The addres, which is used to transfer encoded reporting and tracing
     * messages to the process registered for the reception of default messages.
     */
    private String defaultReportingAddress;

    /**
     * This is set when the default reporting address is set.
     */
    private boolean defaultReportingAddressSet = false;

    private boolean useNagleFlag;

    private boolean useNagleSet;

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_ServiceAddressKeyword = "CS_ADDRESS";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_DefaultReportingAddressKeyword = "DEFAULT_REPORTING_ADDRESS";

    public final static String CI_UseNagleKeyWord = "USE_NAGLE";


    @SuppressWarnings("unused")
    private EE_APIPX_IPCConfig(final EE_APIPX_IPCConfig right)
    {
        this.serviceAddressSet = right.serviceAddressSet;
        this.defaultReportingAddressSet = right.defaultReportingAddressSet;
        this.useNagleFlag = right.useNagleFlag;
        this.useNagleSet = right.useNagleSet;
    }

    public EE_APIPX_IPCConfig()
    {
        this.serviceAddressSet = false;
        this.defaultReportingAddressSet = false;
        this.useNagleFlag = true;
        this.useNagleSet = false;
    }

    /**
     * Used in Inter process communication.
     */
    public String getDefaultReportingAddress()
    {
        return this.defaultReportingAddress;
    }

    /**
     * Used in inter process communication.
     */
    public String getServiceAddress()
    {
        return this.serviceAddress;
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public boolean acceptValue(final String name, final String value, EE_Database db)
    {
        if (name.equals(CI_ServiceAddressKeyword))
        {
            if (this.serviceAddressSet)
            {
                db.setCurrentError(getAlreadyLoadedMsg(name));
                return false;
            }
        }
        else if (name.equals(CI_DefaultReportingAddressKeyword))
        {
            if (this.defaultReportingAddressSet)
            {
                db.setCurrentError(getAlreadyLoadedMsg(name));
                return false;
            }
        }
        else if (name.equals(CI_UseNagleKeyWord))
        {
            if (this.useNagleSet)
            {
                db.setCurrentError(getAlreadyLoadedMsg(name));
                return false;
            }
        }
        else
        {
            return super.acceptValue(name, value, db);
        }
        if (name.equals(CI_ServiceAddressKeyword))
        {
            this.serviceAddress = value;
            this.serviceAddressSet = true;
            return true;
        }
        else if (name.equals(CI_DefaultReportingAddressKeyword))
        {
            this.defaultReportingAddress = value;
            this.defaultReportingAddressSet = true;
            return true;
        }
        else if (name.equals(CI_UseNagleKeyWord))
        {
            if (value.equals(EE_Database.getcBooltruekeyword()))
            {
                this.useNagleFlag = true;
                this.useNagleSet = true;
                return true;
            }
            else if (value.equals(EE_Database.getcBoolfalsekeyword()))
            {
                this.useNagleFlag = false;
                this.useNagleSet = true;
                return true;
            }
            else
            {
                db.setCurrentError("expected " + EE_Database.getcBooltruekeyword() + " or "
                                   + EE_Database.getcBoolfalsekeyword() + "but was given " + value);
                return false;
            }
        }
        return false;

    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public EE_APIPX_LoadableElement acceptListItem(final String name, EE_Database db)
    {
        return super.acceptListItem(name, db);
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public void registerKeywords(EE_APIPX_Database argdb)
    {
        argdb.registerOuterKeyword(CI_ServiceAddressKeyword, this);
        argdb.registerOuterKeyword(CI_DefaultReportingAddressKeyword, this);
        argdb.registerOuterKeyword(CI_UseNagleKeyWord, this);
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public boolean isFullyLoaded(EE_Reference<String> diagnostic, EE_APIPX_Database pxDb)
    {
        EE_APIPX_ProxySettings ppxy = pxDb.getProxySettings();
        SLE_AppRole lrole = ppxy.getRole();
        if (lrole == SLE_AppRole.sleAR_user)
        {
            return true;
        }
        else if (!this.defaultReportingAddressSet)
        {
            diagnostic.setReference(getNotLoadedMsg(CI_DefaultReportingAddressKeyword));
            return false;
        }
        else if (!this.serviceAddressSet)
        {
            diagnostic.setReference(getNotLoadedMsg(CI_ServiceAddressKeyword));
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean getUseNagleFlag()
    {
        return this.useNagleFlag;
    }
}
