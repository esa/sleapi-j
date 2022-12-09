/**
 * @(#) EE_APIPX_ResponderPortList.java
 */

package esa.sle.impl.api.apipx.pxdb;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import ccsds.sle.api.isle.exception.HRESULT;
import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * Ports Data Contains all the information relating to ports in the
 * configuration database
 */
public class EE_APIPX_ResponderPortList extends EE_APIPX_LoadableElement
{

    /**
     * One item is always buffered, until the values for it have been set -
     * otherwise it can not be mapped.
     */
    private EE_APIPX_ResponderPort bufItem = null;

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_ForeignPortsKeyword = "FOREIGN_LOGICAL_PORTS";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_LocalPortsKeyword = "LOCAL_LOGICAL_PORTS";

    private final TreeMap<String, EE_APIPX_ResponderPort> portList = new TreeMap<String, EE_APIPX_ResponderPort>();


    public EE_APIPX_ResponderPortList(final EE_APIPX_ResponderPortList right)
    {
        this.bufItem = right.bufItem;
    }

    public EE_APIPX_ResponderPortList()
    {
        this.bufItem = null;
    }

    public EE_APIPX_ResponderPort getBufItem()
    {
        return this.bufItem;
    }

    /**
     * Returns the number of ports contained by the list.
     */
    public int getNumResponderPorts()
    {
        return this.portList.size();
    }

    /**
     * Sets the output parameter to point to the port indexed by the index
     * passed in. The index passed in should be greater than or equal to zero,
     * and less than get_numPorts
     */
    public EE_APIPX_ResponderPort getResponderPort(int index)
    {
        int pos = 0;
        Iterator<Entry<String, EE_APIPX_ResponderPort>> itr = this.portList.entrySet().iterator();
        while (pos != index && itr.hasNext())
        {
            pos++;
            itr.next();
        }
        if (pos == index)
        {
            Entry<String, EE_APIPX_ResponderPort> entry = itr.next();
            return entry.getValue();
        }
        else
        {
            return null;
        }

    }

    public HRESULT getResponderPort(final String logicalPort, EE_Reference<EE_APIPX_ResponderPort> retVal)
    {
        if (this.portList.containsKey(logicalPort))
        {
            retVal.setReference(this.portList.get(logicalPort));
            return HRESULT.S_OK;
        }
        retVal.setReference(null);
        return HRESULT.E_FAIL;
    }

    /**
     * Returns the number of responder ports contained by the object.
     */
    public int getResponderPortCount()
    {
        return this.portList.size();
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public boolean acceptValue(final String name, final String value, EE_Database db)
    {
        return super.acceptValue(name, value, db);
    }

    @Override
    public EE_APIPX_LoadableElement acceptListItem(final String name, EE_Database db)
    {
        boolean bIsForeign = false;
        if (name.equals(CI_ForeignPortsKeyword))
        {
            bIsForeign = true;
        }
        else if (!name.equals(CI_LocalPortsKeyword))
        {
            return super.acceptListItem(name, db);
        }
        if (this.bufItem == null)
        {
            this.bufItem = new EE_APIPX_ResponderPort(bIsForeign, this);
            return this.bufItem;
        }
        else
        {
            if (!this.portList.containsKey(this.bufItem.getLogicalID()))
            {
                this.portList.put(this.bufItem.getLogicalID(), this.bufItem);
                this.bufItem = new EE_APIPX_ResponderPort(bIsForeign, this);
                return this.bufItem;
            }
            else
            {
                db.setCurrentError(getDuplicateMsg(EE_APIPX_ResponderPort.C_PortName, this.bufItem.getLogicalID()));
                return null;
            }
        }

    }

    @Override
    public boolean isFullyLoaded(EE_Reference<String> diagnostic, EE_APIPX_Database db)
    {

        if (this.bufItem != null && this.bufItem.getLogicalID() != null)
        {
            if (!this.portList.containsKey(this.bufItem.getLogicalID()))
            {
                this.portList.put(this.bufItem.getLogicalID(), this.bufItem);
                this.bufItem = null;
                return true;
            }
            else
            {
                diagnostic.setReference("duplicate port found " + this.bufItem.getLogicalID());
                return false;
            }
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
        argdb.registerOuterKeyword(CI_ForeignPortsKeyword, this);
        argdb.registerOuterKeyword(CI_LocalPortsKeyword, this);
    }

    /**
     * Refer to EE_APIPX_LoadableElement documentation.
     */
    @Override
    public boolean listIsKnown(String listName)
    {
        if (listName.equals(CI_ForeignPortsKeyword))
        {
            return true;
        }
        else if (listName.equals(CI_LocalPortsKeyword))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}
