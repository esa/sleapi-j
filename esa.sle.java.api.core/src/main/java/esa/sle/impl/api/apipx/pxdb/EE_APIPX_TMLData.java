/**
 * @(#) EE_APIPX_TMLData.java
 */

package esa.sle.impl.api.apipx.pxdb;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.it.SLE_AppRole;
import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class holds all configuration parameters needed for operation of the
 * Transport Mapping Layer
 */
public class EE_APIPX_TMLData extends EE_APIPX_LoadableElement
{
    /**
     * The TML start-up timer, in seconds.
     */
    private int startupTimer = -1;

    /**
     * Indicates whether a connection without the heartbeat mechanism is
     * acceptable.
     */
    private boolean nonUseHB = false;

    /**
     * Indicates whether the heartbeat attribute has been set.
     */
    private boolean nonUseHBSet = false;

    /**
     * Minimum value of the heartbeat timer interval, in seconds.
     */
    private int minHB = -1;

    /**
     * Maximum value of the heartbeat timer interval, in seconds.
     */
    private int maxHB = -1;

    /**
     * Minimum value of the dead-factor (has no units).
     */
    private int minDeadFactor = -1;

    /**
     * Maximum value of the dead-factor (has no units).
     */
    private int maxDeadFactor = -1;

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private static final String CI_StartupTimerKeyword = "STARTUP_TIMER";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private static final String CI_nonUseHBKeyword = "NON_USEHEARTBEAT";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private static final String CI_minHBKeyword = "MIN_HEARTBEAT";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private static final String CI_maxHBKeyword = "MAX_HEARTBEAT";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private static final String CI_minDeadFactor = "MIN_DEADFACTOR";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private static final String CI_maxDeadFactor = "MAX_DEADFACTOR";


    @SuppressWarnings("unused")
    private EE_APIPX_TMLData(final EE_APIPX_TMLData right)
    {
        this.startupTimer = right.startupTimer;
        this.nonUseHB = right.nonUseHB;
        this.nonUseHBSet = right.nonUseHBSet;
        this.minHB = right.minHB;
        this.maxHB = right.maxHB;
        this.minDeadFactor = right.minDeadFactor;
        this.maxDeadFactor = right.maxDeadFactor;
    }

    public EE_APIPX_TMLData()
    {
        this.startupTimer = -1;
        this.nonUseHB = false;
        this.nonUseHBSet = false;
        this.minHB = -1;
        this.maxHB = -1;
        this.minDeadFactor = -1;
        this.maxDeadFactor = -1;
    }

    /**
     * Returns the TML startup timer duration in seconds.
     */
    public int getStartupTimer()
    {
        return this.startupTimer;
    }

    /**
     * Returns whether the heartbeat mechanisms should be used or not.
     */
    public boolean getNonUseHB()
    {
        return this.nonUseHB;
    }

    /**
     * Returns the minimum acceptable heartbeat in seconds.
     */
    public int getMinHB()
    {
        return this.minHB;
    }

    /**
     * Returns the maximum acceptable heartbeat in seconds.
     */
    public int getMaxHB()
    {
        return this.maxHB;
    }

    /**
     * Returns the minimum acceptable dead factor.
     */
    public int getMinDeadFactor()
    {
        return this.minDeadFactor;
    }

    /**
     * Returns the maximum acceptable dead factor.
     */
    public int getMaxDeadFactor()
    {
        return this.maxDeadFactor;
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public boolean acceptValue(final String name, final String value, EE_Database db)
    {

        if (name.equals(CI_nonUseHBKeyword))
        {
            if (this.nonUseHBSet)
            {
                db.setCurrentError(getAlreadyLoadedMsg(name));
                return false;
            }
            this.nonUseHBSet = true;
            if (value.equals(EE_Database.getcBooltruekeyword()))
            {
                this.nonUseHB = true;
                return true;
            }
            else if (value.equals(EE_Database.getcBoolfalsekeyword()))
            {
                this.nonUseHB = false;
                return true;
            }
            else
            {
                db.setCurrentError("expected " + EE_Database.getcBooltruekeyword() + " or "
                                   + EE_Database.getcBoolfalsekeyword() + " but was given " + value);
                return false;
            }
        }
        String maxvalue = Integer.MAX_VALUE + "";

        if (value.length() > maxvalue.length())
        {
            db.setCurrentError("value of " + name + " was too long ... " + value);
            return false;
        }
        else if (value.length() == maxvalue.length() && value.compareTo(maxvalue) > 0)
        {
            db.setCurrentError("value of " + name + " was too large ... " + value);
            return false;
        }
        HRESULT convok = HRESULT.S_OK;
        try
        {
            if (name.equals(CI_StartupTimerKeyword))
            {
                if (this.startupTimer != -1)
                {
                    db.setCurrentError(getAlreadyLoadedMsg(name));
                    return false;
                }
                this.startupTimer = EE_Database.convIntegral(value);
            }
            else if (name.equals(CI_minHBKeyword))
            {
                if (this.minHB != -1)
                {
                    db.setCurrentError(getAlreadyLoadedMsg(name));
                    return false;
                }
                this.minHB = EE_Database.convIntegral(value);
            }
            else if (name.equals(CI_maxHBKeyword))
            {
                if (this.maxHB != -1)
                {
                    db.setCurrentError(getAlreadyLoadedMsg(name));
                    return false;
                }
                this.maxHB = EE_Database.convIntegral(value);
            }
            else if (name.equals(CI_minDeadFactor))
            {
                if (this.minDeadFactor != -1)
                {
                    db.setCurrentError(getAlreadyLoadedMsg(name));
                    return false;
                }
                this.minDeadFactor = EE_Database.convIntegral(value);
            }
            else if (name.equals(CI_maxDeadFactor))
            {
                if (this.maxDeadFactor != -1)
                {
                    db.setCurrentError(getAlreadyLoadedMsg(name));
                    return false;
                }
                this.maxDeadFactor = EE_Database.convIntegral(value);
            }
            else
            {
                return acceptValue(name, value, db);
            }
        }
        catch (SleApiException e)
        {
            convok = e.getHResult();
        }
        if (convok != HRESULT.S_OK)
        {
            db.setCurrentError("value of " + name + " = " + value + " was not able to be converted to a number.");
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
    public EE_APIPX_LoadableElement acceptListItem(String name, EE_Database db)
    {
        return super.acceptListItem(name, db);
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public void registerKeywords(EE_APIPX_Database argdb)
    {
        argdb.registerOuterKeyword(CI_StartupTimerKeyword, this);
        argdb.registerOuterKeyword(CI_nonUseHBKeyword, this);
        argdb.registerOuterKeyword(CI_minHBKeyword, this);
        argdb.registerOuterKeyword(CI_maxHBKeyword, this);
        argdb.registerOuterKeyword(CI_minDeadFactor, this);
        argdb.registerOuterKeyword(CI_maxDeadFactor, this);
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public boolean isFullyLoaded(EE_Reference<String> diagnostic, EE_APIPX_Database db)
    {
        EE_APIPX_Database pdb = db;
        EE_APIPX_ProxySettings ppxy = pdb.getProxySettings();
        SLE_AppRole lrole = ppxy.getRole();
        ppxy = null;
        if (lrole == SLE_AppRole.sleAR_user)
        {
            return true;
        }
        else if (!this.nonUseHBSet)
        {
            diagnostic.setReference(getNotLoadedMsg(CI_nonUseHBKeyword));
            return false;
        }
        else if (this.startupTimer < 0)
        {
            diagnostic.setReference(getNotLoadedMsg(CI_StartupTimerKeyword));
            return false;
        }
        else if (this.minHB < 0)
        {
            diagnostic.setReference(getNotLoadedMsg(CI_minHBKeyword));
            return false;
        }
        else if (this.maxHB < 0)
        {
            diagnostic.setReference(getNotLoadedMsg(CI_maxHBKeyword));
            return false;
        }
        else if (this.minDeadFactor < 0)
        {
            diagnostic.setReference(getNotLoadedMsg(CI_minDeadFactor));
            return false;
        }
        else if (this.maxDeadFactor < 0)
        {
            diagnostic.setReference(getNotLoadedMsg(CI_maxDeadFactor));
            return false;
        }
        return true;
    }

}
