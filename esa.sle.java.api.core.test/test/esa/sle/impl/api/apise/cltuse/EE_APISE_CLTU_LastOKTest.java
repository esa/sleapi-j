package esa.sle.impl.api.apise.cltuse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isrv.icltu.ICLTU_GetParameter;
import ccsds.sle.api.isrv.icltu.ICLTU_StatusReport;
import esa.sle.impl.api.apiop.cltuop.EE_CLTU_GetParameter;
import esa.sle.impl.api.apiop.cltuop.EE_CLTU_StatusReport;
import esa.sle.impl.ifs.gen.EE_StubReporter;

public class EE_APISE_CLTU_LastOKTest
{

    EE_APISE_CLTU_StatusInformation eecltu;

    int version;

    ISLE_Reporter preporter;

    EE_CLTU_GetParameter poperation;

    ICLTU_GetParameter icltuParm;

    EE_APISE_CLTU_LastOK lastOk = null;


    @Before
    public void setUp() throws Exception
    {
        this.lastOk = new EE_APISE_CLTU_LastOK();
        this.preporter = new EE_StubReporter();
        this.eecltu = new EE_APISE_CLTU_StatusInformation();
        this.poperation = new EE_CLTU_GetParameter(this.version, this.preporter);

    }

    @Test
    public void testSetUpReport()
    {
        ICLTU_StatusReport sr = new EE_CLTU_StatusReport(this.version, this.preporter);
        this.lastOk.setUpReport(sr);
        assertNull(sr.getRadiationStopTime());
        assertEquals(0, sr.getCltuLastOk());
    }

}
