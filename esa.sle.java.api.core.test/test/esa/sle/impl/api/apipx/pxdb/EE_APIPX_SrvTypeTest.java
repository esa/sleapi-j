package esa.sle.impl.api.apipx.pxdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ccsds.sle.api.isle.exception.HRESULT;
import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_APIPX_SrvTypeTest
{
    EE_APIPX_SrvType srvType;

    EE_APIPX_SrvTypeList parentList;

    EE_Database db;

    EE_APIPX_Database eePipDB;

    EE_Reference<String> diagnostic;


    @Before
    public void setUp()
    {
        this.parentList = new EE_APIPX_SrvTypeList();
        this.srvType = new EE_APIPX_SrvType(this.parentList);
        this.db = new EE_Database();
        this.eePipDB = new EE_APIPX_Database();
        this.diagnostic = new EE_Reference<String>();
        this.diagnostic.setReference("initial diagnostic");
    }

    @Test
    public void testAddVersion()
    {
        assertEquals(HRESULT.S_OK, this.srvType.addVersion(2));
        assertEquals(HRESULT.S_OK, this.srvType.addVersion(11));
        assertEquals(HRESULT.E_FAIL, this.srvType.addVersion(11));
    }

    @Test
    public void testGetVerision()
    {
        testAddVersion();
        int version = this.srvType.getVersion(1);
        assertEquals(11, version);
        version = this.srvType.getVersion(0);
        assertEquals(2, version);
        assertEquals(-1, this.srvType.getVersion(2));
    }

    @Test
    public void testAcceptValue()
    {
        assertEquals(0, this.srvType.getNumVersions());
        assertTrue(this.srvType.acceptValue("SRV_ID", "RAF", this.db));
        assertTrue(this.db.getCurrentError().isEmpty());
        assertTrue(this.srvType.acceptValue("SRV_VERSION", "1", this.db));
        assertEquals("", this.db.getCurrentError());
        assertEquals(1, this.srvType.getNumVersions());
        assertFalse(this.srvType.acceptValue("SRV_VERSION", "RAF", this.db));
        assertTrue(this.srvType.acceptValue("SRV_VERSION", "5", this.db));

        setUp();
        assertTrue(this.srvType.acceptValue("SRV_ID", "FSP", this.db));
        assertTrue(this.srvType.acceptValue("SRV_VERSION", "1", this.db));
        assertTrue(this.srvType.acceptValue("SRV_VERSION", "2", this.db));
        assertFalse(this.srvType.acceptValue("SRV_VERSION", "3", this.db));
        assertTrue(this.srvType.acceptValue("SRV_VERSION", "4", this.db));
        assertTrue(this.srvType.acceptValue("SRV_VERSION", "5", this.db));
        

        setUp();
        assertFalse(this.srvType.acceptValue("test", "test", this.db));
        assertEquals("Unknown keyword test", this.db.getCurrentError());
    }

    @Test
    public void testAcceptListItem()
    {
        assertEquals(0, this.srvType.getLoadingVar());
        this.srvType.acceptListItem("SRV_VERSION", this.db);
        assertEquals(1, this.srvType.getLoadingVar());
        this.srvType.acceptListItem("SRV_ID", this.db);
        assertEquals("SRV_ID cannot be given in list format.", this.db.getCurrentError());
        this.srvType.acceptListItem("test", this.db);
        assertEquals("Unexpected list element test", this.db.getCurrentError());
    }

    @Test
    public void testIsFullyLoaded()
    {
        this.srvType.acceptValue("SRV_ID", "RAF", this.db);
        boolean result = this.srvType.isFullyLoaded(this.diagnostic, this.eePipDB);
        assertTrue(result);
        assertEquals("initial diagnostic", this.diagnostic.getReference());

        setUp();
        result = this.srvType.isFullyLoaded(this.diagnostic, this.eePipDB);
        assertFalse(result);
        assertEquals("Value for SRV_ID missing", this.diagnostic.getReference());

        setUp();
        assertEquals(0, this.srvType.getLoadingVar());
        this.srvType.acceptListItem("SRV_VERSION", this.db);
        assertEquals(1, this.srvType.getLoadingVar());
        result = this.srvType.isFullyLoaded(this.diagnostic, this.eePipDB);
        assertTrue(result);
        assertEquals("initial diagnostic", this.diagnostic.getReference());
    }

    @Test
    public void testListIsKnown()
    {
        assertFalse(this.srvType.listIsKnown("test"));
        assertTrue(this.srvType.listIsKnown("SRV_VERSION"));
    }
}
