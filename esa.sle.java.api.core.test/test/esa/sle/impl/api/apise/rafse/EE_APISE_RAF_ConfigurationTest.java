package esa.sle.impl.api.apise.rafse;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isrv.iraf.IRAF_GetParameter;
import ccsds.sle.api.isrv.iraf.types.RAF_ParameterName;
import esa.sle.impl.api.apiop.rafop.EE_RAF_GetParameter;
import esa.sle.impl.ifs.gen.EE_StubReporter;

public class EE_APISE_RAF_ConfigurationTest
{
    EE_APISE_RAF_Configuration obj;

    ISLE_Reporter preporter;

    int version;

    EE_RAF_GetParameter poperation;

    IRAF_GetParameter irafGetParam;


    @Before
    public void setUp() throws Exception
    {
        this.version = 1;
        this.obj = new EE_APISE_RAF_Configuration();
        this.preporter = new EE_StubReporter();
        this.poperation = new EE_RAF_GetParameter(this.version, this.preporter);
    }

    @Test
    public void testSetUpGetParameter()
    {
        this.poperation.setRequestedParameter(RAF_ParameterName.rafPN_bufferSize);
        this.irafGetParam = this.poperation;
        HRESULT hr = this.obj.setUpGetParameter(this.irafGetParam);
        assertEquals(HRESULT.S_OK, hr);

        this.poperation.setRequestedParameter(RAF_ParameterName.rafPN_deliveryMode);
        this.irafGetParam = this.poperation;
        hr = this.obj.setUpGetParameter(this.irafGetParam);
        assertEquals(HRESULT.S_OK, hr);

        this.poperation.setRequestedParameter(RAF_ParameterName.rafPN_latencyLimit);
        this.irafGetParam = this.poperation;
        hr = this.obj.setUpGetParameter(this.irafGetParam);
        assertEquals(HRESULT.S_OK, hr);
        
        this.poperation.setRequestedParameter(RAF_ParameterName.rafPN_minReportingCycle);
        this.irafGetParam = this.poperation;
        hr = this.obj.setUpGetParameter(this.irafGetParam);
        assertEquals(HRESULT.S_OK, hr);
        
        this.poperation.setRequestedParameter(RAF_ParameterName.rafPN_permittedFrameQuality);
        this.irafGetParam = this.poperation;
        hr = this.obj.setUpGetParameter(this.irafGetParam);
        assertEquals(HRESULT.S_OK, hr);
    }

}
