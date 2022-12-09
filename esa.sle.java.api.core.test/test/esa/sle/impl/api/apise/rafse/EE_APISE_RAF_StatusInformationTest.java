package esa.sle.impl.api.apise.rafse;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isrv.iraf.IRAF_GetParameter;
import ccsds.sle.api.isrv.iraf.IRAF_StatusReport;
import ccsds.sle.api.isrv.iraf.types.RAF_ParameterName;
import esa.sle.impl.api.apiop.rafop.EE_RAF_GetParameter;
import esa.sle.impl.api.apiop.rafop.EE_RAF_StatusReport;
import esa.sle.impl.ifs.gen.EE_StubReporter;

public class EE_APISE_RAF_StatusInformationTest
{
    EE_APISE_RAF_StatusInformation eeraf;

    int version;

    ISLE_Reporter preporter;

    EE_RAF_GetParameter poperation;

    IRAF_GetParameter irafParm;


    @Before
    public void setUp() throws Exception
    {
        this.version = 1;
        this.preporter = new EE_StubReporter();
        this.eeraf = new EE_APISE_RAF_StatusInformation();
        this.poperation = new EE_RAF_GetParameter(this.version, this.preporter);
    }

    @Test
    public void testSetUpReport()
    {
        IRAF_StatusReport sr = new EE_RAF_StatusReport(this.version, this.preporter);
        this.eeraf.setUpReport(sr);
        assertEquals(this.eeraf.getNumErrorFreeFrames(), sr.getNumErrorFreeFrames());
        assertEquals(this.eeraf.getNumFrames(), sr.getNumFrames());
        assertEquals(this.eeraf.getFrameSyncLock(), sr.getFrameSyncLock());
        assertEquals(this.eeraf.getCarrierDemodLock(), sr.getCarrierDemodLock());
        assertEquals(this.eeraf.getSymbolSyncLock(), sr.getSymbolSyncLock());
        assertEquals(this.eeraf.getProductionStatus(), sr.getProductionStatus());
    }

    @Test
    public void testSetUpGetParameter()
    {
        this.poperation.setRequestedParameter(RAF_ParameterName.rafPN_requestFrameQuality);
        this.irafParm = this.poperation;
        assertEquals(HRESULT.S_OK, this.eeraf.setUpGetParameter(this.irafParm));
    }

}
