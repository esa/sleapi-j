package esa.sle.impl.api.apise.rocfse;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isrv.irocf.IROCF_GetParameter;
import ccsds.sle.api.isrv.irocf.types.ROCF_ParameterName;
import esa.sle.impl.api.apiop.rocfop.EE_ROCF_GetParameter;
import esa.sle.impl.ifs.gen.EE_StubReporter;

public class EE_APISE_ROCF_StatusInformationTest
{

    EE_APISE_ROCF_StatusInformation eerocf;

    int version;

    ISLE_Reporter preporter;

    EE_ROCF_GetParameter poperation;

    IROCF_GetParameter irocfParm;


    @Before
    public void setUp() throws Exception
    {
        this.version = 1;
        this.preporter = new EE_StubReporter();
        this.eerocf = new EE_APISE_ROCF_StatusInformation();
        this.poperation = new EE_ROCF_GetParameter(this.version, this.preporter);
    }

    @Test
    public void testSetUpGetParameter()
    {
        this.poperation.setRequestedParameter(ROCF_ParameterName.rocfPN_requestedGvcid);
        HRESULT hr = this.eerocf.setUpGetParameter(this.poperation);
        assertEquals(HRESULT.S_OK, hr);

        this.poperation.setRequestedParameter(ROCF_ParameterName.rocfPN_requestedControlWordType);
        hr = this.eerocf.setUpGetParameter(this.poperation);
        assertEquals(HRESULT.S_OK, hr);

        this.poperation.setRequestedParameter(ROCF_ParameterName.rocfPN_requestedTcVcid);
        hr = this.eerocf.setUpGetParameter(this.poperation);
        assertEquals(HRESULT.S_OK, hr);

        this.poperation.setRequestedParameter(ROCF_ParameterName.rocfPN_requestedUpdateMode);
        hr = this.eerocf.setUpGetParameter(this.poperation);
        assertEquals(HRESULT.S_OK, hr);
    }

}
