/**
 * @(#) EE_APIPX_SrvTypeList.java
 */

package esa.sle.impl.api.apipx.pxdb;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * @NameServer Type List@EndName
 * @ResponsibilityContains all information relating to server types and
 *                         versions.@EndResponsibility
 */
public class EE_APIPX_SrvTypeList extends EE_APIPX_LoadableElement
{

    /**
     * One item is always buffered, until the values for it have been set -
     * otherwise it can not be mapped.
     */
    private EE_APIPX_SrvType bufItem;

    private final TreeMap<SLE_ApplicationIdentifier, EE_APIPX_SrvType> eeSrvType = new TreeMap<SLE_ApplicationIdentifier, EE_APIPX_SrvType>();

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_ServerTypesKeyword = "SERVER_TYPES";


    public EE_APIPX_SrvTypeList(final EE_APIPX_SrvTypeList right)
    {
        this.bufItem = right.bufItem;
    }

    public EE_APIPX_SrvTypeList()
    {
        this.bufItem = null;
    }

    /**
     * Returns the number of aggregated EE_APIPX_SrvTypes
     */
    public int getNumSrvTypes()
    {
        return this.eeSrvType.size();
    }   

    /**
     * Returns the EE_APIPX_SrvType that corresponds to the list index passed
     * in, or null if the index is incorrect.
     */
    public EE_APIPX_SrvType getSrvTypeByPos(int index)
    {
        int pos = 0;
        Iterator<Entry<SLE_ApplicationIdentifier, EE_APIPX_SrvType>> itr = this.eeSrvType.entrySet().iterator();
        while (pos != index && itr.hasNext())
        {
            pos++;
            itr.next();
        }
        if (pos == index)
        {
            Entry<SLE_ApplicationIdentifier, EE_APIPX_SrvType> entry = itr.next();
            return entry.getValue();
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns the EE_APIPX_SrvType that corresponds to the application
     * identifier passed in, or Null if the index is incorrect.
     */
    public EE_APIPX_SrvType getSrvTypeByType(SLE_ApplicationIdentifier index)
    {

        if (this.eeSrvType.containsKey(index))
        {
            EE_APIPX_SrvType result = this.eeSrvType.get(index);
            return result;
        }
        else
        {
            return null;
        }

    }

    /**
     * Adds a server type to the list.
     */

    @SuppressWarnings("unused")
    private HRESULT addSrvType(EE_APIPX_SrvType srvType)
    {
        if (this.eeSrvType.containsKey(srvType.getServiceType()))
        {
            return HRESULT.E_FAIL;
        }
        this.eeSrvType.put(srvType.getServiceType(), srvType);
        return HRESULT.S_OK;
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public boolean acceptValue(final String name, final String value, EE_Database db)
    {
        return super.acceptValue(name, value, db);
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public EE_APIPX_LoadableElement acceptListItem(final String name, EE_Database db)
    {
        if (CI_ServerTypesKeyword.equals(name))
        {
            if (this.bufItem == null)
            {
                this.bufItem = new EE_APIPX_SrvType(this);
                return this.bufItem;
            }
            else
            {
                this.eeSrvType.put(this.bufItem.getServiceType(), this.bufItem);
                this.bufItem = new EE_APIPX_SrvType(this);
                return this.bufItem;
            }
        }
        else
        {
            return super.acceptListItem(name, db);
        }
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public boolean isFullyLoaded(EE_Reference<String> diagnostic, EE_APIPX_Database db)
    {
        if (this.bufItem != null)
        {
            this.eeSrvType.put(this.bufItem.getServiceType(), this.bufItem);
            this.bufItem = null;
            return true;
        }
        else
        {
            if (this.eeSrvType.size() <= 0)
            {
                diagnostic.setReference(getNotLoadedMsg(CI_ServerTypesKeyword));
                return false;
            }
            return true;
        }
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public void registerKeywords(EE_APIPX_Database argdb)
    {
        argdb.registerOuterKeyword(CI_ServerTypesKeyword, this);
    }

    /**
     * Refer to EE_APIPX_LoadableElement documentation.
     */
    @Override
    public boolean listIsKnown(String listName)
    {
        if (listName.equals(CI_ServerTypesKeyword))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}
