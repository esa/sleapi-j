package esa.sle.impl.api.apise.cltuse;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isrv.icltu.ICLTU_GetParameter;
import ccsds.sle.api.isrv.icltu.types.CLTU_ParameterName;
import esa.sle.impl.api.apiop.cltuop.EE_CLTU_GetParameter;
import esa.sle.impl.ifs.gen.EE_StubReporter;

public class EE_APISE_CLTU_ConfigurationTest
{
    EE_APISE_CLTU_Configuration obj;

    ISLE_Reporter preporter;

    EE_CLTU_GetParameter poperation;

    int version;

    ICLTU_GetParameter a;


    @Before
    public void setUp() throws Exception
    {

        this.version = 1;
        this.obj = new EE_APISE_CLTU_Configuration();
        this.preporter = new EE_StubReporter();
        this.poperation = new EE_CLTU_GetParameter(this.version, this.preporter);
    }

    @Test
    public void testSetUpGetParameter()
    {
        this.poperation.setRequestedParameter(CLTU_ParameterName.cltuPN_bitLockRequired);
        this.a = this.poperation;
        HRESULT b = this.obj.setUpGetParameter(this.a);
        assertEquals(HRESULT.S_OK, b);

        this.poperation.setRequestedParameter(CLTU_ParameterName.cltuPN_deliveryMode);
        this.a = this.poperation;
        b = this.obj.setUpGetParameter(this.a);
        assertEquals(HRESULT.S_OK, b);

        this.poperation.setRequestedParameter(CLTU_ParameterName.cltuPN_maximumSlduLength);
        this.a = this.poperation;
        b = this.obj.setUpGetParameter(this.a);
        assertEquals(HRESULT.S_OK, b);

        this.poperation.setRequestedParameter(CLTU_ParameterName.cltuPN_modulationFrequency);
        this.a = this.poperation;
        b = this.obj.setUpGetParameter(this.a);
        assertEquals(HRESULT.S_OK, b);

        this.poperation.setRequestedParameter(CLTU_ParameterName.cltuPN_modulationIndex);
        this.a = this.poperation;
        b = this.obj.setUpGetParameter(this.a);
        assertEquals(HRESULT.S_OK, b);

        this.poperation.setRequestedParameter(CLTU_ParameterName.cltuPN_plopInEffect);
        this.a = this.poperation;
        b = this.obj.setUpGetParameter(this.a);
        assertEquals(HRESULT.S_OK, b);

        this.poperation.setRequestedParameter(CLTU_ParameterName.cltuPN_rfAvailableRequired);
        this.a = this.poperation;
        b = this.obj.setUpGetParameter(this.a);
        assertEquals(HRESULT.S_OK, b);

        this.poperation.setRequestedParameter(CLTU_ParameterName.cltuPN_subcarrierToBitRateRatio);
        this.a = this.poperation;
        b = this.obj.setUpGetParameter(this.a);
        assertEquals(HRESULT.S_OK, b);

        this.poperation.setRequestedParameter(CLTU_ParameterName.cltuPN_invalid);
        this.a = this.poperation;
        b = this.obj.setUpGetParameter(this.a);
        assertEquals(HRESULT.SLE_E_UNKNOWN, b);
    }

}
