package esa.sle.impl.api.apipx.pxdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_APIPX_PeerApplDataListTest
{

    EE_APIPX_PeerApplDataList applDataList;

    EE_Database db;

    EE_APIPX_Database pxdb;

    EE_Reference<String> diagnostic;


    @Before
    public void setUp()
    {
        this.applDataList = new EE_APIPX_PeerApplDataList();
        this.pxdb = new EE_APIPX_Database();
        this.db = new EE_Database();
        this.diagnostic = new EE_Reference<String>();
        this.diagnostic.setReference("initial diagnostic");
    }

    @Test
    public void testAcceptListItem()
    {
        EE_APIPX_PeerApplData bufItem = new EE_APIPX_PeerApplData(this.applDataList);
        // get a buffItem.
        bufItem = (EE_APIPX_PeerApplData) this.applDataList.acceptListItem("REMOTE_PEERS", this.db);
        // set the id for that buffItem
        bufItem.acceptValue("ID", "4444", this.db);
        // set the password for that buffItem
        assertTrue(bufItem.acceptValue("PASSWORD", "62636465676869", this.db));
        // set the authentication mode for that buffItem
        assertTrue(bufItem.acceptValue("AUTHENTICATION_MODE", "BIND", this.db));
        // add bufferItem to the list;
        bufItem = (EE_APIPX_PeerApplData) this.applDataList.acceptListItem("REMOTE_PEERS", this.db);
        assertEquals(1, this.applDataList.getNumPeerApplDataItems());

        bufItem.acceptValue("ID", "55555", this.db);
        bufItem = (EE_APIPX_PeerApplData) this.applDataList.acceptListItem("REMOTE_PEERS", this.db);
        assertEquals(2, this.applDataList.getNumPeerApplDataItems());

        bufItem.acceptValue("ID", "666666", this.db);
        bufItem = (EE_APIPX_PeerApplData) this.applDataList.acceptListItem("REMOTE_PEERS", this.db);
        assertEquals(3, this.applDataList.getNumPeerApplDataItems());

        bufItem.acceptValue("ID", "7777777", this.db);
        bufItem = (EE_APIPX_PeerApplData) this.applDataList.acceptListItem("REMOTE_PEERS", this.db);
        assertEquals(4, this.applDataList.getNumPeerApplDataItems());

        bufItem.acceptValue("ID", "88888888", this.db);
        bufItem = (EE_APIPX_PeerApplData) this.applDataList.acceptListItem("REMOTE_PEERS", this.db);
        assertEquals(5, this.applDataList.getNumPeerApplDataItems());

        bufItem.acceptValue("ID", "999999999", this.db);
        bufItem = (EE_APIPX_PeerApplData) this.applDataList.acceptListItem("REMOTE_PEERS", this.db);
        assertEquals(6, this.applDataList.getNumPeerApplDataItems());

        bufItem.acceptValue("ID", "10000000000", this.db);
        bufItem = (EE_APIPX_PeerApplData) this.applDataList.acceptListItem("REMOTE_PEERS", this.db);
        assertEquals(7, this.applDataList.getNumPeerApplDataItems());

        bufItem.acceptValue("ID", "111111111111", this.db);
        bufItem = (EE_APIPX_PeerApplData) this.applDataList.acceptListItem("REMOTE_PEERS", this.db);
        assertEquals(8, this.applDataList.getNumPeerApplDataItems());

        bufItem.acceptValue("ID", "111111111111", this.db);
        bufItem = (EE_APIPX_PeerApplData) this.applDataList.acceptListItem("REMOTE_PEERS", this.db);
        assertEquals("Duplicate value 111111111111 for REMOTE_PEERS", this.db.getCurrentError());
        this.applDataList.acceptListItem("REMOTE_PEERS2", this.db);
        assertEquals("Unexpected list element REMOTE_PEERS2", this.db.getCurrentError());
    }

    @Test
    public void testListIsKnown()
    {
        assertTrue(this.applDataList.listIsKnown("REMOTE_PEERS"));
        assertFalse(this.applDataList.listIsKnown("Random text"));
    }

    @Test
    public void testIsFullyLoadedStringEE_APIPX_Database()
    {

        testAcceptListItem();
        this.db = new EE_Database();

        boolean result = this.applDataList.isFullyLoaded(this.diagnostic, this.pxdb);
        assertFalse(result);
        assertEquals("Duplicate value 111111111111 for REMOTE_PEERS", this.diagnostic.getReference());

        // create a new bufItem
        EE_APIPX_PeerApplData bufItem = new EE_APIPX_PeerApplData(this.applDataList);
        // get a buffItem.
        bufItem = (EE_APIPX_PeerApplData) this.applDataList.acceptListItem("REMOTE_PEERS", this.db);
        // set the id for that buffItem
        bufItem.acceptValue("ID", "2222222222222", this.db);
        // add bufferItem to the list;
        bufItem = (EE_APIPX_PeerApplData) this.applDataList.acceptListItem("REMOTE_PEERS", this.db);

        EE_APIPX_PeerApplData a = this.applDataList.getPeerApplDataItemByID("2222222222222");
        assertEquals("2222222222222", a.getId());

        bufItem.acceptValue("ID", "33333333333333333", this.db);
        this.diagnostic = new EE_Reference<String>();
        this.diagnostic.setReference("initial diagnostic");

        result = this.applDataList.isFullyLoaded(this.diagnostic, this.pxdb);
        assertTrue(result);
        System.out.println(this.diagnostic.getReference());
        assertEquals("initial diagnostic", this.diagnostic.getReference());

    }

    @Test
    public void testGetPeerApplDataItemByPos()
    {
        testAcceptListItem();
        EE_APIPX_PeerApplData result = this.applDataList.getPeerApplDataItemByPos(0);
        assertEquals("10000000000", result.getId());
        result = this.applDataList.getPeerApplDataItemByPos(1);
        assertEquals("111111111111", result.getId());
        result = this.applDataList.getPeerApplDataItemByPos(2);
        assertEquals("4444", result.getId());
        result = this.applDataList.getPeerApplDataItemByPos(3);
        assertEquals("55555", result.getId());
        result = this.applDataList.getPeerApplDataItemByPos(4);
        assertEquals("666666", result.getId());
        result = this.applDataList.getPeerApplDataItemByPos(5);
        assertEquals("7777777", result.getId());
        result = this.applDataList.getPeerApplDataItemByPos(6);
        assertEquals("88888888", result.getId());
        result = this.applDataList.getPeerApplDataItemByPos(7);
        assertEquals("999999999", result.getId());
    }

    @Test
    public void testGetPeerApplDataItemByID()
    {
        testAcceptListItem();
        EE_APIPX_PeerApplData result = this.applDataList.getPeerApplDataItemByID("4444");
        assertEquals("4444", result.getId());
        assertEquals(7, result.getPassword().length);

        assertEquals(SLE_AuthenticationMode.sleAM_bindOnly, result.getAuthenticationMode());
        result = this.applDataList.getPeerApplDataItemByID("111111111111");
        assertEquals("111111111111", result.getId());
    }

}
