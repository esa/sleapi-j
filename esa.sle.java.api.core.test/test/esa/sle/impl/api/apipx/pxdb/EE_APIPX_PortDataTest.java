package esa.sle.impl.api.apipx.pxdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_APIPX_PortDataTest
{

    EE_APIPX_PortData portDataA;

    EE_APIPX_PortData portDataB;

    EE_APIPX_PortData portDataC;

    EE_APIPX_PortData portDataD;

    EE_Database dbA;

    EE_Database dbB;

    EE_Database dbC;

    EE_Database dbD;

    EE_APIPX_Database pxdb;

    EE_Reference<String> diagnostic;


    @Before
    public void setUp()
    {
        this.portDataA = new EE_APIPX_PortData(true, true);
        this.portDataB = new EE_APIPX_PortData(false, true);
        this.portDataC = new EE_APIPX_PortData(false, false);
        this.portDataD = new EE_APIPX_PortData(false, false);
        this.dbA = new EE_Database();
        this.dbB = new EE_Database();
        this.dbC = new EE_Database();
        this.dbD = new EE_Database();
        this.pxdb = new EE_APIPX_Database();
        this.diagnostic = new EE_Reference<String>();
        this.diagnostic.setReference("initial diagnostic");
    }

    @Test
    public void testAcceptValue()
    {
        assertTrue(this.portDataA.acceptValue("HOST_NAME", "localhost:3050", this.dbA));
        assertFalse(this.portDataB.acceptValue("HOST_NAME", "*:3050", this.dbB));
        assertEquals("cannot specify the any IP address (*) for a foreign port.", this.dbB.getCurrentError());
        assertTrue(this.portDataC.acceptValue("IP_ADDRESS", "*:3050", this.dbC));
        assertTrue(this.portDataD.acceptValue("IP_ADDRESS", "localhost:3050", this.dbC));
        assertEquals(3050, this.portDataD.getTcpPortNumber());
        assertEquals("localhost/127.0.0.1", this.portDataD.getTcpIPAddress() + "");
    }

    @Test
    public void testIsFullyLoaded()
    {
        assertFalse(this.portDataA.isFullyLoaded(this.diagnostic, this.pxdb));

        setUp();
        assertTrue(this.portDataD.acceptValue("IP_ADDRESS", "127.0.0.1:3050", this.dbC));
        assertTrue(this.portDataD.isFullyLoaded(this.diagnostic, this.pxdb));

    }

}
