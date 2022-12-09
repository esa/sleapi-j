package esa.sle.impl.api.apipx.pxdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_APIPX_TMLDataTest
{
    EE_APIPX_TMLData tmlData;

    EE_Database db;

    EE_Reference<String> diagnostic;

    EE_APIPX_Database pxdb;


    @Before
    public void setUp()
    {
        this.tmlData = new EE_APIPX_TMLData();
        this.db = new EE_Database();
        this.diagnostic = new EE_Reference<String>();
        this.diagnostic.setReference("initial diagnostic");
        this.pxdb = new EE_APIPX_Database();
    }

    @Test
    public void testAcceptValue()
    {
        assertTrue(this.tmlData.acceptValue("NON_USEHEARTBEAT", "TRUE", this.db));
        assertFalse(this.tmlData.acceptValue("NON_USEHEARTBEAT", "TRUE", this.db));
        setUp();
        assertTrue(this.tmlData.acceptValue("NON_USEHEARTBEAT", "FALSE", this.db));

        assertTrue(this.tmlData.acceptValue("STARTUP_TIMER", "123", this.db));
        assertEquals(123, this.tmlData.getStartupTimer());
        assertTrue(this.tmlData.acceptValue("MIN_HEARTBEAT", "124", this.db));
        assertEquals(124, this.tmlData.getMinHB());
        assertTrue(this.tmlData.acceptValue("MAX_HEARTBEAT", "125", this.db));
        assertEquals(125, this.tmlData.getMaxHB());
        assertTrue(this.tmlData.acceptValue("MIN_DEADFACTOR", "163", this.db));
        assertEquals(163, this.tmlData.getMinDeadFactor());
        assertTrue(this.tmlData.acceptValue("MAX_DEADFACTOR", "227", this.db));
        assertEquals(227, this.tmlData.getMaxDeadFactor());
    }

    @Test
    public void testIsFullyLoadedEE_ReferenceOfStringEE_APIPX_Database()
    {
        testAcceptValue();
        assertTrue(this.tmlData.isFullyLoaded(this.diagnostic, this.pxdb));
    }

}
