package esa.sle.impl.api.apipx.pxdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_APIPX_LocalApplDataTest
{

    EE_APIPX_LocalApplData localAppData;

    EE_Database db;

    EE_APIPX_Database pxd;

    EE_Reference<String> diagnostic;


    @Before
    public void setUp()
    {
        this.localAppData = new EE_APIPX_LocalApplData();
        this.db = new EE_Database();
        this.pxd = new EE_APIPX_Database();
        this.diagnostic = new EE_Reference<String>();
        this.diagnostic.setReference("initial diagnostic");
    }

    @Test
    public void testAcceptValue()
    {
        boolean result = this.localAppData.acceptValue("", "", this.db);
        assertFalse(result);

        result = this.localAppData.acceptValue("LOCAL_ID", "", this.db);
        assertEquals("the value for LOCAL_ID must be between 3 and 16 characters long.", this.db.getCurrentError());
        assertFalse(result);

        setUp();
        result = this.localAppData.acceptValue("LOCAL_ID", "1", this.db);
        assertEquals("the value for LOCAL_ID must be between 3 and 16 characters long.", this.db.getCurrentError());
        assertFalse(result);

        setUp();
        result = this.localAppData.acceptValue("LOCAL_ID", "174326527346592837456287346529378456923847562", this.db);
        assertEquals("the value for LOCAL_ID must be between 3 and 16 characters long.", this.db.getCurrentError());
        assertFalse(result);

        setUp();
        result = this.localAppData.acceptValue("LOCAL_ID", "4444", this.db);
        assertTrue(result);

        setUp();
        result = this.localAppData.acceptValue("LOCAL_ID", "222", this.db);
        assertTrue(result);
        result = this.localAppData.acceptValue("LOCAL_ID", "222", this.db);
        assertFalse(result);
        assertEquals("Value for LOCAL_ID already set", this.db.getCurrentError());

        setUp();
        result = this.localAppData.acceptValue("LOCAL_PASSWORD", "61626364656568", this.db);
        assertTrue(result);
        result = this.localAppData.acceptValue("LOCAL_PASSWORD", "61626364656568", this.db);
        assertFalse(result);

        setUp();
        result = this.localAppData.acceptValue("LOCAL_PASSWORD", "6162636465", this.db);
        assertEquals("the value for LOCAL_PASSWORD has a not acceptable size - the length must be between 6 and 16 characters (or between 12 and 32 hexadecimal 'nibbles').",
                     this.db.getCurrentError());

        setUp();
        result = this.localAppData.acceptValue("LOCAL_PASSWORD", "$&§§", this.db);
        assertFalse(result);
        assertEquals("the value for LOCAL_PASSWORD is not a valid hexadecimal string - the length must be divisible by 2 and the digits must be 0-9, or a-f or A-F.",
                     this.db.getCurrentError());
    }

    @Test
    public void testIsFullyLoadedStringEE_APIPX_Database()
    {

        boolean result = this.localAppData.isFullyLoaded(this.diagnostic, this.pxd);
        assertFalse(result);
        assertEquals("Value for LOCAL_ID missing", this.diagnostic.getReference());

        setUp();
        this.localAppData.acceptValue("LOCAL_ID", "4444", this.db);
        this.localAppData.acceptValue("LOCAL_PASSWORD", "4444", this.db);
        result = this.localAppData.isFullyLoaded(this.diagnostic, this.pxd);
        assertTrue(result);
        assertEquals("initial diagnostic", this.diagnostic.getReference());

        setUp();
        this.localAppData.acceptValue("LOCAL_ID", "4444", this.db);
        this.localAppData.acceptValue("LOCAL_PASSWORD", "", this.db);
        result = this.localAppData.isFullyLoaded(this.diagnostic, this.pxd);
        assertFalse(result);
        assertEquals("Value for LOCAL_PASSWORD missing", this.diagnostic.getReference());
    }

}
