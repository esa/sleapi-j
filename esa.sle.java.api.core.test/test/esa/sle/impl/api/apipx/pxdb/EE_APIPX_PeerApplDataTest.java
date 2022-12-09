package esa.sle.impl.api.apipx.pxdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_APIPX_PeerApplDataTest
{

    EE_APIPX_PeerApplData peer;

    EE_APIPX_PeerApplDataList parentList;

    EE_Database db;

    EE_APIPX_Database pxd;

    EE_Reference<String> diagnostic;


    @Before
    public void setUp()
    {
        this.parentList = new EE_APIPX_PeerApplDataList();
        this.peer = new EE_APIPX_PeerApplData(this.parentList);
        this.db = new EE_Database();
        this.pxd = new EE_APIPX_Database();
        this.diagnostic = new EE_Reference<String>();
        this.diagnostic.setReference("initial Diagnostic");
    }

    @Test
    public void testAcceptValue()
    {
        assertTrue(this.peer.acceptValue("ID", "4444", this.db));
        assertFalse(this.peer.acceptValue("ID", "333", this.db));
        assertEquals("Value for ID already set", this.db.getCurrentError());

        setUp();
        assertFalse(this.peer.acceptValue("ID", "22", this.db));
        assertEquals("the value for ID must be between 3 and 16 characters long.", this.db.getCurrentError());

        setUp();
        assertTrue(this.peer.acceptValue("AUTHENTICATION_MODE", "BIND", this.db));
        assertFalse(this.peer.acceptValue("AUTHENTICATION_MODE", "BIND", this.db));
        assertEquals("Value for AUTHENTICATION_MODE already set", this.db.getCurrentError());

        setUp();
        assertTrue(this.peer.acceptValue("AUTHENTICATION_MODE", "NONE", this.db));
        setUp();
        assertTrue(this.peer.acceptValue("AUTHENTICATION_MODE", "ALL", this.db));
        setUp();
        assertFalse(this.peer.acceptValue("AUTHENTICATION_MODE", "test", this.db));

        setUp();
        assertFalse(this.peer.acceptValue("PASSWORD", "test", this.db));
        assertEquals("the value for PASSWORD is not a valid hexadecimal string - the length must be divisible by 2 and the digits must be 0-9, or a-f or A-F.",
                     this.db.getCurrentError());

        setUp();
        assertFalse(this.peer.acceptValue("PASSWORD", "62", this.db));
        setUp();
        assertTrue(this.peer.acceptValue("PASSWORD", "62636465676869", this.db));
        assertFalse(this.peer.acceptValue("PASSWORD", "62636465676869", this.db));

        setUp();
        assertFalse(this.peer.acceptValue("anything_else", "anything_else", this.db));
    }

    @Test
    public void testIsFullyLoadedStringEE_APIPX_Database()
    {
        boolean result = this.peer.isFullyLoaded(this.diagnostic, this.pxd);
        assertFalse(result);

        assertTrue(this.peer.acceptValue("ID", "4444", this.db));
        result = this.peer.isFullyLoaded(this.diagnostic, this.pxd);
        assertFalse(result);

        assertTrue(this.peer.acceptValue("AUTHENTICATION_MODE", "BIND", this.db));
        result = this.peer.isFullyLoaded(this.diagnostic, this.pxd);
        assertFalse(result);
        assertEquals("Value for PASSWORD missing for 4444", this.diagnostic.getReference());

        assertTrue(this.peer.acceptValue("PASSWORD", "62636465676869", this.db));
        result = this.peer.isFullyLoaded(this.diagnostic, this.pxd);
        assertTrue(result);

    }

}
