/**
 * @(#) EE_APIPX_PeerApplDataList.java
 */

package esa.sle.impl.api.apipx.pxdb;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * Contains the list of peer application data objects and appropriate accessor
 * functions.
 */
public class EE_APIPX_PeerApplDataList extends EE_APIPX_LoadableElement
{
    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    private final static String CI_RemotePeersKeyword = "REMOTE_PEERS";

    /**
     * One item is always buffered, until the values for it have been set -
     * otherwise it can not be mapped.
     */
    private EE_APIPX_PeerApplData bufItem = null;

    private final TreeMap<String, EE_APIPX_PeerApplData> peerAplDataList = new TreeMap<String, EE_APIPX_PeerApplData>();


    @SuppressWarnings("unused")
    private EE_APIPX_PeerApplDataList(final EE_APIPX_PeerApplDataList right)
    {
        this.bufItem = right.bufItem;
    }

    public EE_APIPX_PeerApplDataList()
    {
        this.bufItem = null;
    }

    /**
     * Returns the cardinality of the Peer Application Data items set. This can
     * then be used as an index bound when iterating over the list.
     */
    public int getNumPeerApplDataItems()
    {
        return this.peerAplDataList.size();
    }

    public TreeMap<String, EE_APIPX_PeerApplData> getPeerAplDataList()
    {
        return this.peerAplDataList;
    }

    /**
     * Returns a Peer application data item, based on position. The value of pos
     * must range from 0 to getnumPeerApplDataItems - 1. Null will be returned
     * if the argument is incorrect.
     */
    public EE_APIPX_PeerApplData getPeerApplDataItemByPos(int pos)
    {
        Iterator<Entry<String, EE_APIPX_PeerApplData>> itr = this.peerAplDataList.entrySet().iterator();
        int i = 0;
        while (itr.hasNext())
        {
            if (i == pos)
            {
                Entry<String, EE_APIPX_PeerApplData> entry = itr.next();
                return entry.getValue();
            }
            itr.next();
            i++;
        }
        return null;
    }

    /**
     * Returns the Peer Application Data corresponding to the ID passed in as
     * parameter. This will return NULL if no peer data item exists.
     */
    public EE_APIPX_PeerApplData getPeerApplDataItemByID(String peerID)
    {
        if (this.peerAplDataList.containsKey(peerID))
        {
            EE_APIPX_PeerApplData value = this.peerAplDataList.get(peerID);
            return value;
        }
        return null;
    }

    @Override
    public boolean acceptValue(final String name, final String value, EE_Database db)
    {
        return super.acceptValue(name, value, db);
    }

    @Override
    public EE_APIPX_LoadableElement acceptListItem(final String name, EE_Database db)
    {
        if (name.equals(CI_RemotePeersKeyword))
        {

            if (this.bufItem == null)
            {
                this.bufItem = new EE_APIPX_PeerApplData(this);
                return this.bufItem;
            }
            else if (!this.peerAplDataList.containsKey(this.bufItem.getId()))
            {
                this.peerAplDataList.put(this.bufItem.getId(), this.bufItem);
                this.bufItem = new EE_APIPX_PeerApplData(this);
                return this.bufItem;
            }
            else
            {
                db.setCurrentError(getDuplicateMsg(name, this.bufItem.getId()));
                return null;
            }
        }

        return super.acceptListItem(name, db);

    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public boolean isFullyLoaded(EE_Reference<String> diagnostic, EE_APIPX_Database db)
    {
        if (this.bufItem == null)
        {
            return true;
        }
        else if (!this.peerAplDataList.containsKey(this.bufItem.getId()))
        {
            this.peerAplDataList.put(this.bufItem.getId(), this.bufItem);
            this.bufItem = null;
            return true;
        }
        diagnostic.setReference(getDuplicateMsg(CI_RemotePeersKeyword, this.bufItem.getId()));
        this.bufItem = null;
        return false;
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public void registerKeywords(EE_APIPX_Database argdb)
    {
        argdb.registerOuterKeyword(CI_RemotePeersKeyword, this);
    }

    /**
     * Refer to EE_APIPX_LoadableElement documentation.
     */
    @Override
    public boolean listIsKnown(String listName)
    {
        if (listName.equals(CI_RemotePeersKeyword))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}
