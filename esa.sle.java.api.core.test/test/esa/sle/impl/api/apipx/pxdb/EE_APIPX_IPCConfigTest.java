package esa.sle.impl.api.apipx.pxdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_APIPX_IPCConfigTest
{
    EE_APIPX_IPCConfig ipConfig;

    EE_Database db;

    EE_APIPX_Database eePipDB;

    EE_Reference<String> diagnostic;

    private final String CI_ServiceAddressKeyword = "CS_ADDRESS";
    private final String CI_DefaultReportingAddressKeyword = "DEFAULT_REPORTING_ADDRESS";
    public final String CI_UseNagleKeyWord = "USE_NAGLE";

    @Before
    public void setUp()
    {
        this.db = EE_Database.getInstance();
        this.ipConfig = new EE_APIPX_IPCConfig();
        this.eePipDB = new EE_APIPX_Database();
        this.diagnostic = new EE_Reference<String>();
        this.diagnostic.setReference("initial diagnostic");
    }

    @Test
    public void testAcceptValue()
    {
        String[] name = { "CS_ADDRESS", "DEFAULT_REPORTING_ADDRESS" };
        String[] value = { "valueA", "valueB" };
        boolean result = this.ipConfig.acceptValue(name[0], value[0], this.db);
        assertTrue(result);
        result = this.ipConfig.acceptValue(name[0], value[0], this.db);
        assertFalse(result);
        assertEquals("Value for CS_ADDRESS already set", this.db.getCurrentError());
        result = this.ipConfig.acceptValue(name[1], value[1], this.db);
        assertTrue(result);
        result = this.ipConfig.acceptValue(name[1], value[1], this.db);
        assertFalse(result);
        assertEquals("Value for DEFAULT_REPORTING_ADDRESS already set", this.db.getCurrentError());
        String nameVal = "name";
        String valueVal = "value";
        result = this.ipConfig.acceptValue(nameVal, valueVal, this.db);
        assertEquals("Unknown keyword name", this.db.getCurrentError());
        assertFalse(result);

    }

    @Test
    public void testAcceptListItem()
    {
        String name = "test";
        this.ipConfig.acceptListItem(name, this.db);
        assertEquals("Unexpected list element " + name, this.db.getCurrentError());
    }

    @Test
    public void testRegisterKeywords()
    {
        this.ipConfig.registerKeywords(this.eePipDB);
        this.ipConfig.registerKeywords(this.eePipDB);
                
        if (this.eePipDB.getCurrentError().equals("keyword "+CI_ServiceAddressKeyword+" was attempted to be registered twice ") || 
        		this.eePipDB.getCurrentError().equals("keyword "+CI_DefaultReportingAddressKeyword+" was attempted to be registered twice ") ||
        		this.eePipDB.getCurrentError().equals("keyword "+CI_UseNagleKeyWord+" was attempted to be registered twice ")){
        	fail("register key fails");
        }
    }

    @Test
    public void testIsFullyLoadedStringEE_APIPX_Database()
    {
        int check = 0;
        if (check++ == 0)
        {
            EE_APIPX_Database pxDb = new EE_APIPX_Database();
            pxDb.getProxySettings().acceptValue("PROXY_ROLE", "INITIATOR", pxDb);
            this.ipConfig.acceptValue("DEFAULT_REPORTING_ADDRESS", "", this.db);
            this.ipConfig.acceptValue("DEFAULT_REPORTING_ADDRESS", "", this.db);
            boolean result = this.ipConfig.isFullyLoaded(this.diagnostic, pxDb);
            assertEquals("Value for DEFAULT_REPORTING_ADDRESS already set", this.db.getCurrentError());
            assertTrue(result);
        }
        if (check++ == 1)
        {
            this.ipConfig = new EE_APIPX_IPCConfig();
            EE_APIPX_Database pxDb = new EE_APIPX_Database();
            Boolean result = this.ipConfig.isFullyLoaded(this.diagnostic, pxDb);
            assertEquals("Value for DEFAULT_REPORTING_ADDRESS missing", this.diagnostic.getReference());
            assertFalse(result);
        }
        if (check++ == 2)
        {
            setUp();
            String name = "DEFAULT_REPORTING_ADDRESS";
            String value = "valueA";
            this.ipConfig.acceptValue(name, value, this.db);
            EE_APIPX_Database pxDb = new EE_APIPX_Database();
            boolean result = this.ipConfig.isFullyLoaded(this.diagnostic, pxDb);
            assertFalse(result);
            assertEquals("Value for CS_ADDRESS missing", this.diagnostic.getReference());
        }
        setUp();
        String[] name = { "CS_ADDRESS", "DEFAULT_REPORTING_ADDRESS" };
        String[] value = { "valueA", "valueB" };
        this.ipConfig.acceptValue(name[0], value[0], this.db);
        this.ipConfig.acceptValue(name[1], value[1], this.db);
        EE_APIPX_Database pxDb = new EE_APIPX_Database();
        boolean result = this.ipConfig.isFullyLoaded(this.diagnostic, pxDb);
        assertTrue(result);
        assertEquals("initial diagnostic", this.diagnostic.getReference());

    }

}
