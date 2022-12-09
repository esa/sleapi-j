package esa.sle.impl.api.apise.rcfse;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isrv.ircf.IRCF_GetParameter;
import ccsds.sle.api.isrv.ircf.types.RCF_ParameterName;
import esa.sle.impl.api.apiop.rcfop.EE_RCF_GetParameter;
import esa.sle.impl.ifs.gen.EE_StubReporter;

public class EE_APISE_RCF_ConfigurationTest
{
    EE_APISE_RCF_Configuration obj;

    int version;

    ISLE_Reporter preporter;

    ISLE_ServiceInform serviceInfo;

    ISLE_Reporter reporter;

    EE_RCF_GetParameter operation;

    IRCF_GetParameter ircfGetParam;


    @Before
    public void setUp() throws Exception
    {
        this.version = 1;
        this.obj = new EE_APISE_RCF_Configuration();
        this.reporter = new EE_StubReporter();
        this.operation = new EE_RCF_GetParameter(this.version, this.preporter);
    }

    @Test
    public void testSetUpGetParameter()
    {
        this.operation.setRequestedParameter(RCF_ParameterName.rcfPN_bufferSize);
        this.ircfGetParam = this.operation;
        HRESULT hr = this.obj.setUpGetParameter(this.ircfGetParam);
        assertEquals(HRESULT.S_OK, hr);

        this.operation.setRequestedParameter(RCF_ParameterName.rcfPN_deliveryMode);
        this.ircfGetParam = this.operation;
        hr = this.obj.setUpGetParameter(this.ircfGetParam);
        assertEquals(HRESULT.S_OK, hr);

        this.operation.setRequestedParameter(RCF_ParameterName.rcfPN_latencyLimit);
        this.ircfGetParam = this.operation;
        hr = this.obj.setUpGetParameter(this.ircfGetParam);
        assertEquals(HRESULT.S_OK, hr);

        this.operation.setRequestedParameter(RCF_ParameterName.rcfPN_permittedGvcidSet);
        this.ircfGetParam = this.operation;
        hr = this.obj.setUpGetParameter(this.ircfGetParam);
        assertEquals(HRESULT.S_OK, hr);
    }

}
