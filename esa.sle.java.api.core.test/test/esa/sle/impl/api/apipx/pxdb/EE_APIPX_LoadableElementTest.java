package esa.sle.impl.api.apipx.pxdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import esa.sle.impl.ifs.gen.EE_Database;

public class EE_APIPX_LoadableElementTest
{

    EE_APIPX_LoadableElement loadebleElement;

    EE_Database db;


    @Before
    public void setUp()
    {
        this.db = EE_Database.getInstance();
        this.loadebleElement = new EE_APIPX_LoadableElement();
    }

    @Test
    public void testAcceptValue()
    {
        String name = "name";
        String value = "value";
        boolean result = this.loadebleElement.acceptValue(name, value, this.db);
        assertEquals("Unknown keyword name", this.db.getCurrentError());
        assertFalse(result);
    }

    @Test
    public void testAcceptListItem()
    {
        String name = "name";
        EE_APIPX_LoadableElement result = this.loadebleElement.acceptListItem(name, this.db);
        assertEquals("Unexpected list element " + name, this.db.getCurrentError());
        assertNull(result);
    }

}
