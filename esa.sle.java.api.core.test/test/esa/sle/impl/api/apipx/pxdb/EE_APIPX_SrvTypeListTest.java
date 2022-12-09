package esa.sle.impl.api.apipx.pxdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_APIPX_SrvTypeListTest
{
    EE_APIPX_SrvTypeList srvTypeList;

    EE_Database db;

    EE_APIPX_SrvType srvTypeA;

    EE_APIPX_SrvType srvTypeB;

    EE_APIPX_Database pxpDb;

    EE_Reference<String> diagnostic;

    EE_APIPX_SrvType bufItem;


    @Before
    public void setUp()
    {
        this.srvTypeList = new EE_APIPX_SrvTypeList();
        this.srvTypeA = new EE_APIPX_SrvType(this.srvTypeList);
        this.srvTypeA.setServiceType(SLE_ApplicationIdentifier.sleAI_rtnAllFrames);
        this.srvTypeB = new EE_APIPX_SrvType(this.srvTypeList);
        this.srvTypeB.setServiceType(SLE_ApplicationIdentifier.sleAI_rtnChFrames);
        this.db = new EE_Database();
        this.pxpDb = new EE_APIPX_Database();
        this.diagnostic = new EE_Reference<String>();
        this.diagnostic.setReference("initial diagnostic");
        this.bufItem = (EE_APIPX_SrvType) this.srvTypeList.acceptListItem("SERVER_TYPES", this.db);
    }

   

    @Test
    public void testAcceptValue()
    {
        assertFalse(this.srvTypeList.acceptValue("test", "valueTest", this.db));
        assertEquals("Unknown keyword test", this.db.getCurrentError());
    }

    @Test
    public void testAcceptListItem()
    {
        this.bufItem = (EE_APIPX_SrvType) this.srvTypeList.acceptListItem("SERVER_TYPES", this.db);
        assertEquals(1, this.srvTypeList.getNumSrvTypes());
        this.bufItem.acceptValue("SRV_ID", "RAF", this.db);
        this.bufItem = (EE_APIPX_SrvType) this.srvTypeList.acceptListItem("SERVER_TYPES", this.db);
        assertEquals(2, this.srvTypeList.getNumSrvTypes());
        this.bufItem.acceptValue("SRV_ID", "RCF", this.db);
        this.bufItem = (EE_APIPX_SrvType) this.srvTypeList.acceptListItem("SERVER_TYPES", this.db);
        assertEquals(3, this.srvTypeList.getNumSrvTypes());
    }

    @Test
    public void testIsFullyLoaded()
    {

        testAcceptListItem();
        assertTrue(this.srvTypeList.isFullyLoaded(this.diagnostic, this.pxpDb));
        assertEquals(3, this.srvTypeList.getNumSrvTypes());

    }

    @Test
    public void testGetSrvTypeByPos() throws Throwable
    {
        testAcceptListItem();
        EE_APIPX_SrvType srvTypeLocalA = this.srvTypeList.getSrvTypeByPos(0);
        assertNotNull(srvTypeLocalA);
        assertEquals(SLE_ApplicationIdentifier.sleAI_rtnAllFrames, srvTypeLocalA.getServiceType());

        EE_APIPX_SrvType srvTypeLocalB = this.srvTypeList.getSrvTypeByPos(1);
        assertNotNull(srvTypeLocalB);
        assertEquals(SLE_ApplicationIdentifier.sleAI_rtnChFrames, srvTypeLocalB.getServiceType());

    }

    @Test
    public void testGetSrvTypeByType() throws Throwable
    {
    	testAcceptListItem();   
        EE_APIPX_SrvType srvTypeLocalA = this.srvTypeList.getSrvTypeByType(SLE_ApplicationIdentifier.sleAI_rtnAllFrames);
        assertNotNull(srvTypeLocalA);
        assertEquals(SLE_ApplicationIdentifier.sleAI_rtnAllFrames, srvTypeLocalA.getServiceType());

        EE_APIPX_SrvType srvTypeLocalB = this.srvTypeList.getSrvTypeByType(SLE_ApplicationIdentifier.sleAI_rtnChFrames);
        assertNotNull(srvTypeLocalB);
        assertEquals(SLE_ApplicationIdentifier.sleAI_rtnChFrames, srvTypeLocalB.getServiceType());

    }

}
