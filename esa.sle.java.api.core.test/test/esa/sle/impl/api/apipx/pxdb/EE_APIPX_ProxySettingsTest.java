package esa.sle.impl.api.apipx.pxdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ccsds.sle.api.isle.it.SLE_AppRole;
import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_APIPX_ProxySettingsTest
{
    EE_APIPX_ProxySettings proxySettings;

    EE_Database db;

    EE_APIPX_Database pxdb;

    EE_Reference<String> diagnostic;


    @Before
    public void setUp()
    {
        this.proxySettings = new EE_APIPX_ProxySettings();
        this.db = new EE_Database();
        this.pxdb = new EE_APIPX_Database();
        this.diagnostic = new EE_Reference<String>();
        this.diagnostic.setReference("initial diagnostic");
    }

    @Test
    public void testAcceptValue()
    {
        assertTrue(this.proxySettings.acceptValue("PROXY_ROLE", "INITIATOR", this.db));
        assertEquals(SLE_AppRole.sleAR_user, this.proxySettings.getRole());
        assertFalse(this.proxySettings.acceptValue("PROXY_ROLE", "INITIATOR", this.db));
        assertEquals("Value for PROXY_ROLE already set", this.db.getCurrentError());

        setUp();
        assertTrue(this.proxySettings.acceptValue("PROXY_ROLE", "RESPONDER", this.db));
        setUp();
        assertFalse(this.proxySettings.acceptValue("PROXY_ROLE", "random", this.db));
        setUp();
        assertTrue(this.proxySettings.acceptValue("AUTHENTICATION_DELAY", "656665", this.db));
        setUp();
        assertFalse(this.proxySettings.acceptValue("AUTHENTICATION_DELAY", "543674567456745633", this.db));
        setUp();
        assertTrue(this.proxySettings.acceptValue("TRANSMIT_QUEUE_SIZE", "656665", this.db));
        assertEquals(656665, this.proxySettings.getTransmissionQueueSize());
        setUp();
        assertTrue(this.proxySettings.acceptValue("MAX_TRACE_LENGTH", "656665", this.db));
        assertFalse(this.proxySettings.acceptValue("MAX_TRACE_LENGTH", "543674567456745633", this.db));
    }

    @Test
    public void isFullyLoaded()
    {
        boolean result = this.proxySettings.isFullyLoaded(this.diagnostic, this.pxdb);
        assertFalse(result);
        assertEquals("Value for PROXY_ROLE missing", this.diagnostic.getReference());

        setUp();
        assertTrue(this.proxySettings.acceptValue("PROXY_ROLE", "INITIATOR", this.db));
        result = this.proxySettings.isFullyLoaded(this.diagnostic, this.pxdb);
        assertFalse(result);
        assertEquals("Value for AUTHENTICATION_DELAY missing", this.diagnostic.getReference());

        setUp();
        assertTrue(this.proxySettings.acceptValue("PROXY_ROLE", "INITIATOR", this.db));
        assertTrue(this.proxySettings.acceptValue("AUTHENTICATION_DELAY", "656665", this.db));
        result = this.proxySettings.isFullyLoaded(this.diagnostic, this.pxdb);
        assertFalse(result);
        assertEquals("Value for TRANSMIT_QUEUE_SIZE missing", this.diagnostic.getReference());

        setUp();
        assertTrue(this.proxySettings.acceptValue("PROXY_ROLE", "INITIATOR", this.db));
        assertTrue(this.proxySettings.acceptValue("AUTHENTICATION_DELAY", "656665", this.db));
        assertTrue(this.proxySettings.acceptValue("TRANSMIT_QUEUE_SIZE", "656665", this.db));
        result = this.proxySettings.isFullyLoaded(this.diagnostic, this.pxdb);
        assertFalse(result);
        assertEquals("Value for MAX_TRACE_LENGTH missing", this.diagnostic.getReference());

        setUp();
        assertTrue(this.proxySettings.acceptValue("PROXY_ROLE", "INITIATOR", this.db));
        assertTrue(this.proxySettings.acceptValue("AUTHENTICATION_DELAY", "656665", this.db));
        assertTrue(this.proxySettings.acceptValue("TRANSMIT_QUEUE_SIZE", "656665", this.db));
        assertTrue(this.proxySettings.acceptValue("MAX_TRACE_LENGTH", "656665", this.db));
        result = this.proxySettings.isFullyLoaded(this.diagnostic, this.pxdb);
        assertTrue(result);
        assertEquals("initial diagnostic", this.diagnostic.getReference());
    }

}
