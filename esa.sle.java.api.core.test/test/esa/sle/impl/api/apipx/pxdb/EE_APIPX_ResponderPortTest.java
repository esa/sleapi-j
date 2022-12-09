package esa.sle.impl.api.apipx.pxdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_APIPX_ResponderPortTest
{
    EE_APIPX_ResponderPort responderPortA;

    EE_APIPX_ResponderPort responderPortB;

    EE_Database db;

    EE_APIPX_Database pxdb;

    EE_APIPX_ResponderPortList parentList;

    EE_Reference<String> diagnostic;


    @Before
    public void setUp()
    {
        this.parentList = new EE_APIPX_ResponderPortList();
        this.responderPortA = new EE_APIPX_ResponderPort(false, this.parentList);
        this.responderPortB = new EE_APIPX_ResponderPort(true, this.parentList);
        this.db = new EE_Database();
        this.pxdb = new EE_APIPX_Database();
        this.diagnostic = new EE_Reference<String>();
        this.diagnostic.setReference("initial diagnostic");
    }

    @Test
    public void testAcceptValue()
    {
        assertTrue(this.responderPortA.acceptValue("PORT_NAME", "value", this.db));
        assertFalse(this.responderPortA.acceptValue("HOST_NAME", "value", this.db));
        assertFalse(this.responderPortA.acceptValue("PORT_HEARTBEAT_TIMER", "value", this.db));
        assertTrue(this.responderPortB.acceptValue("PORT_HEARTBEAT_TIMER", "123", this.db));
        assertTrue(this.responderPortA.acceptValue("TCP_XMIT_BUFFER_SIZE", "12321", this.db));
        assertTrue(this.responderPortA.acceptValue("TCP_RECV_BUFFER_SIZE", "12321", this.db));
    }

    @Test
    public void testAcceptListItem()
    {
        assertNull(this.responderPortA.acceptListItem("name", this.db));
        assertEquals("Unexpected list element name", this.db.getCurrentError());
        setUp();
        assertNotNull(this.responderPortA.acceptListItem("IP_ADDRESS", this.db));
        assertNull(this.responderPortA.acceptListItem("IP_ADDRESS", this.db));
        setUp();
        assertNotNull(this.responderPortA.acceptListItem("HOST_NAME", this.db));
        assertNull(this.responderPortA.acceptListItem("HOST_NAME", this.db));
        setUp();
        assertNull(this.responderPortA.acceptListItem("PORT_NAME", this.db));
    }

    @Test
    public void testIsFullyLoadedStringEE_APIPX_Database()
    {
        assertTrue(this.responderPortA.acceptValue("PORT_NAME", "value", this.db));
        boolean result = this.responderPortA.isFullyLoaded(this.diagnostic, this.pxdb);
        assertFalse(result);

        setUp();
        assertTrue(this.responderPortA.acceptValue("PORT_NAME", "value", this.db));
        assertNotNull(this.responderPortA.acceptListItem("IP_ADDRESS", this.db));
        assertNull(this.responderPortA.acceptListItem("IP_ADDRESS", this.db));
        result = this.responderPortA.isFullyLoaded(this.diagnostic, this.pxdb);
        assertTrue(result);
        assertEquals("initial diagnostic", this.diagnostic.getReference());

        setUp();
        assertTrue(this.responderPortB.acceptValue("PORT_NAME", "value", this.db));
        assertTrue(this.responderPortB.acceptValue("PORT_HEARTBEAT_TIMER", "123", this.db));
        assertNotNull(this.responderPortB.acceptListItem("HOST_NAME", this.db));
        assertFalse(this.responderPortB.isFullyLoaded(this.diagnostic, this.pxdb));
    }

    @Test
    public void testGetPortData()
    {
        this.responderPortB.acceptListItem("IP_ADDRESS", this.db);
        System.out.println(this.responderPortB.getPortDataCardinality());
        EE_APIPX_PortData pd = this.responderPortB.getPortData(0);
        assertFalse(pd.getHostNameSet());
    }

}
