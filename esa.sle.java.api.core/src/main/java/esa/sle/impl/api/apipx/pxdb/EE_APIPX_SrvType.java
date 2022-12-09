/**
 * @(#) EE_APIPX_SrvType.java
 */

package esa.sle.impl.api.apipx.pxdb;

import java.util.TreeMap;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class EE_APIPX_SrvType holds the information needed in the database for
 * one supported service type.
 */
public class EE_APIPX_SrvType extends EE_APIPX_LoadableElement
{
    /**
     * The supported transfer service type.
     */
    private SLE_ApplicationIdentifier serviceType = SLE_ApplicationIdentifier.sleAI_invalid;

    /**
     * Used to determine if the object is fully loaded or not.
     */
    private boolean serviceTypeSet = false;

    /**
     * The versions supported for the service type.
     */

    TreeMap<Integer, Integer> supportedVersions = new TreeMap<Integer, Integer>();

    /**
     * Provides navigation to the aggregating list.
     */
    private EE_APIPX_SrvTypeList parentList = new EE_APIPX_SrvTypeList();

    /**
     * This is incremented every time a list item is returned (that points to
     * self) and decremented whenever isFullyLoaded is called. Only when this is
     * 0 again, can isFullyLoaded perform a proper check.
     */
    private int loadingVar = 0;

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_ServerIDKeyword = "SRV_ID";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_ServerVersionKeyword = "SRV_VERSION";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_SRVIDRCFKeyword = "RCF";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_SRVIDRAFKeyword = "RAF";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_SRVIDCLTUKeyword = "CLTU";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_SRVIDFSPKeyword = "FSP";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_SRVIDROCFKeyword = "ROCF";


    @SuppressWarnings("unused")
    private EE_APIPX_SrvType(final EE_APIPX_SrvType right)
    {
        this.serviceTypeSet = right.serviceTypeSet;
        this.parentList = right.getParentList();
        this.loadingVar = right.loadingVar;
    }

    public EE_APIPX_SrvTypeList getParentList()
    {
        return this.parentList;
    }

    public EE_APIPX_SrvType()
    {

    }

    public EE_APIPX_SrvType(EE_APIPX_SrvTypeList parentList)
    {

        this.serviceTypeSet = false;
        this.parentList = parentList;
        this.loadingVar = 0;
    }

    public final int getLoadingVar()
    {
        return this.loadingVar;
    }

    @SuppressWarnings("unused")
    private void setLoadingVar(int value)
    {
        this.loadingVar = value;
    }

    /**
     * Returns the service type
     */
    public SLE_ApplicationIdentifier getServiceType()
    {
        return this.serviceType;
    }

    /**
     * The versions are accessed using getVersion(index). The index is a zero
     * based array indexing parameter, where 0 <= index < get_numVersions. The
     * versions are sorted in ascending order.
     */
    public int getNumVersions()
    {
        return this.supportedVersions.size();
    }

    /**
     * The versions are accessed using getVersion(index).
     * 
     * @param index The index is a zero based array indexing parameter, where 0
     *            <= index < get_numVersions. The versions are sorted in
     *            ascending order. If successful return the index, otherwise
     *            returns -1; /**
     * @param index
     * @return
     */
    public int getVersion(int key)
    {
        int version = -1;
        if (this.supportedVersions.containsKey(key))
        {
            version = this.supportedVersions.get(key);
            return version;
        }
        return version;

    }

    /**
     * Adds another version to the list of versions supported by the object
     * (which are held internally in ascending order.). S_OK The version has
     * been added. E_FAIL The object already contains the version
     */
    public HRESULT addVersion(int version)
    {
        boolean versionExist = false;
        if (this.supportedVersions.containsValue(version))
        {
            versionExist = true;
        }

        if (!versionExist)
        {
            int key = 0;
            if (!this.supportedVersions.isEmpty())
            {
                key = this.supportedVersions.size();
            }
            this.supportedVersions.put(key, version);
            return HRESULT.S_OK;
        }
        else
        {
            return HRESULT.E_FAIL;
        }
    }

    /**
     * More of a validation and error setting. There are 2 cases based on the
     * name and accordingly on value.
     */
    @Override
    public boolean acceptValue(final String name, final String value, EE_Database db)
    {
        if (name.equals(CI_ServerVersionKeyword))
        {
            if (!this.serviceTypeSet)
            {
                db.setCurrentError(CI_ServerIDKeyword + " must be defined before " + CI_ServerVersionKeyword);
                return false;
            }

            String maxver = Integer.MAX_VALUE + "";

            if (value.length() > maxver.length())
            {
                db.setCurrentError("version is too large " + value);
                return false;
            }
            else if (value.length() == maxver.length() && value.compareTo(maxver) > 0)
            {
                db.setCurrentError("version is too large " + value);
                return false;
            }
            else
            {
                HRESULT isNum = HRESULT.S_OK;
                int version = -1;
                try
                {
                    version = EE_Database.convIntegral(value);
                }
                catch (SleApiException e)
                {
                    isNum = e.getHResult();
                }
                if (isNum != HRESULT.S_OK)
                {
                    db.setCurrentError("setting " + name + " - could not convert " + value + " to a number.");
                    return false;
                }
                switch (this.serviceType)
                {
                case sleAI_rtnAllFrames:
                case sleAI_rtnChFrames:
                case sleAI_fwdCltu:
                    // the old services can have versions 1 or 2 or 3 or 4
                    if (version > 5)
                    {
                        db.setCurrentError("version is too high. " + value);
                        return false;
                    }
                    break;
                case sleAI_rtnChOcf:
                case sleAI_fwdTcSpacePkt:
                    // the new services can have version 1 or 2 or 4 // SLEAPIJ-49
                    if (version != 1 && version != 2 && version != 4 && version != 5)
                    {
                        db.setCurrentError("version is not supported " + value);
                        return false;
                    }
                    break;
                default:
                    break;

                }
                if (addVersion(version) != HRESULT.S_OK)
                {
                    db.setCurrentError(getDuplicateMsg(name, value));
                    return false;
                }
                return true;
            }
        }
        else if (name.equals(CI_ServerIDKeyword))
        {
            if (this.serviceTypeSet)
            {
                db.setCurrentError(getAlreadyLoadedMsg(name));
                return false;
            }
            this.serviceTypeSet = true;
            if (value.equals(CI_SRVIDRAFKeyword))
            {
                this.serviceType = SLE_ApplicationIdentifier.sleAI_rtnAllFrames;
            }
            else if (value.equals(CI_SRVIDRCFKeyword))
            {
                this.serviceType = SLE_ApplicationIdentifier.sleAI_rtnChFrames;
            }
            else if (value.equals(CI_SRVIDROCFKeyword))
            {
                this.serviceType = SLE_ApplicationIdentifier.sleAI_rtnChOcf;
            }
            else if (value.equals(CI_SRVIDFSPKeyword))
            {
                this.serviceType = SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt;
            }
            else if (value.equals(CI_SRVIDCLTUKeyword))
            {
                this.serviceType = SLE_ApplicationIdentifier.sleAI_fwdCltu;
            }
            else
            {
                db.setCurrentError("unknown service id type " + value);
                return false;
            }
            final EE_APIPX_SrvType ptmp = this.parentList.getSrvTypeByType(this.serviceType);
            if (ptmp != null)
            {
                db.setCurrentError(getDuplicateMsg(name, value));
                return false;
            }
            return true;
        }
        else
        {
            return super.acceptValue(name, value, db);
        }

    }

    @Override
    public EE_APIPX_LoadableElement acceptListItem(String name, EE_Database db)
    {
        if (name.equals(CI_ServerVersionKeyword))
        {
            this.loadingVar++;
            return this;
        }
        else if (name.equals(CI_ServerIDKeyword))
        {
            db.setCurrentError(name + " cannot be given in list format.");
            return null;
        }
        return super.acceptListItem(name, db);
    }

    @Override
    public boolean isFullyLoaded(EE_Reference<String> diagnostic, EE_APIPX_Database db)
    {
        if (this.loadingVar > 0)
        {
            --this.loadingVar;
            // only check when checking overall item, and not the list element
            // aliases to self.
            return true;
        }
        if (!this.serviceTypeSet)
        {
            diagnostic.setReference(getNotLoadedMsg(CI_ServerIDKeyword));
            return false;
        }
        /*
         * else if (_supportedVersions.size() <= 0){ diagnostic =
         * getNotLoadedMsg(CI_ServerVersionKeyword); return false; }
         */
        return true;
    }

    /**
     * Sets the service type
     */
    public void setServiceType(SLE_ApplicationIdentifier type1)
    {
        this.serviceType = type1;
    }

    /**
     * Refer to EE_APIPX_LoadableElement documentation.
     */
    @Override
    public boolean listIsKnown(final String listName)
    {
        if (listName.equals(CI_ServerVersionKeyword))
        {
            return true;
        }
        else
        {
            return false;
        }

    }

}
