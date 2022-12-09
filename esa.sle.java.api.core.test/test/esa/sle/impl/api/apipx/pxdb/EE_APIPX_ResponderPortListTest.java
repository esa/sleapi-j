package esa.sle.impl.api.apipx.pxdb;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_APIPX_ResponderPortListTest
{
    EE_APIPX_ResponderPortList rplist;// object to test

    EE_Database db;

    EE_Reference<String> diagnostic;

    EE_Reference<EE_APIPX_ResponderPort> retVal;

    EE_APIPX_Database pxdb;

    EE_APIPX_ResponderPort bufItem;


    @Before
    public void setUp()
    {
        this.rplist = new EE_APIPX_ResponderPortList();
        this.db = new EE_Database();
        this.pxdb = new EE_APIPX_Database();
        this.diagnostic = new EE_Reference<String>();
        this.diagnostic.setReference("initial reference");

        this.retVal = new EE_Reference<EE_APIPX_ResponderPort>();
        this.retVal.setReference(new EE_APIPX_ResponderPort(false, this.rplist));
        this.bufItem = (EE_APIPX_ResponderPort) this.rplist.acceptListItem("FOREIGN_LOGICAL_PORTS", this.db);
    }

    @Test
    public void testAcceptListItem()
    {

        this.bufItem.acceptValue("PORT_NAME", "id1", this.db); // set the id
        this.bufItem = (EE_APIPX_ResponderPort) this.rplist.acceptListItem("FOREIGN_LOGICAL_PORTS", this.db); // insert
        this.bufItem.acceptValue("PORT_NAME", "id2", this.db); // set the id
        this.bufItem = (EE_APIPX_ResponderPort) this.rplist.acceptListItem("FOREIGN_LOGICAL_PORTS", this.db); // insert
        assertEquals(2, this.rplist.getNumResponderPorts());

    }

    @Test
    public void testIsFullyLoaded()
    {
        testAcceptListItem();
        this.rplist.isFullyLoaded(this.diagnostic, this.pxdb);
        assertEquals(3, this.rplist.getNumResponderPorts());
    }

    @Test
    public void intGetResponderPortInt()
    {
        testAcceptListItem();
        EE_APIPX_ResponderPort a = this.rplist.getResponderPort(0);
        assertEquals("id1", a.getLogicalID());
        a = this.rplist.getResponderPort(1);
        assertEquals("id2", a.getLogicalID());
    }

    @Test
    public void testGetResponderPortString()
    {
        testAcceptListItem();
        this.rplist.getResponderPort("id2", this.retVal);
        assertEquals("id2", this.retVal.getReference().getLogicalID());
        this.rplist.getResponderPort("id1", this.retVal);
        assertEquals("id1", this.retVal.getReference().getLogicalID());
    }

}
