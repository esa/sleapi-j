package esa.sle.impl.api.apise.rocfse;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isrv.irocf.IROCF_GetParameter;
import ccsds.sle.api.isrv.irocf.types.ROCF_ChannelType;
import ccsds.sle.api.isrv.irocf.types.ROCF_ControlWordType;
import ccsds.sle.api.isrv.irocf.types.ROCF_Gvcid;
import ccsds.sle.api.isrv.irocf.types.ROCF_ParameterName;
import esa.sle.impl.api.apiop.rocfop.EE_ROCF_GetParameter;
import esa.sle.impl.ifs.gen.EE_StubReporter;

public class EE_APISE_ROCF_ConfigurationTest
{

    EE_APISE_ROCF_Configuration obj;

    int version;

    ISLE_Reporter preporter;

    ISLE_ServiceInform serviceInfo;

    ISLE_Reporter reporter;

    EE_ROCF_GetParameter operation;

    IROCF_GetParameter ircfGetParam;

    ROCF_Gvcid gvcIdM;

    ROCF_Gvcid gvcIdV;


    @Before
    public void setUp() throws Exception
    {
        this.version = 1;
        this.obj = new EE_APISE_ROCF_Configuration();
        this.reporter = new EE_StubReporter();
        this.operation = new EE_ROCF_GetParameter(this.version, this.preporter);
        this.gvcIdM = new ROCF_Gvcid(ROCF_ChannelType.rocfCT_MasterChannel, 1, 1, 1);
        this.gvcIdV = new ROCF_Gvcid(ROCF_ChannelType.rocfCT_VirtualChannel, 1, 1, 1);
    }

    @Test
    public void testSetUpGetParameter()
    {
        this.operation.setRequestedParameter(ROCF_ParameterName.rocfPN_bufferSize);
        this.ircfGetParam = this.operation;
        HRESULT hr = this.obj.setUpGetParameter(this.ircfGetParam);
        assertEquals(HRESULT.S_OK, hr);

        this.operation.setRequestedParameter(ROCF_ParameterName.rocfPN_deliveryMode);
        this.ircfGetParam = this.operation;
        hr = this.obj.setUpGetParameter(this.ircfGetParam);
        assertEquals(HRESULT.S_OK, hr);

        this.operation.setRequestedParameter(ROCF_ParameterName.rocfPN_latencyLimit);
        this.ircfGetParam = this.operation;
        hr = this.obj.setUpGetParameter(this.ircfGetParam);
        assertEquals(HRESULT.S_OK, hr);

        this.operation.setRequestedParameter(ROCF_ParameterName.rocfPN_permittedGvcidSet);
        this.ircfGetParam = this.operation;
        hr = this.obj.setUpGetParameter(this.ircfGetParam);
        assertEquals(HRESULT.S_OK, hr);

        this.operation.setRequestedParameter(ROCF_ParameterName.rocfPN_permittedControlWordTypeSet);
        this.ircfGetParam = this.operation;
        hr = this.obj.setUpGetParameter(this.ircfGetParam);
        assertEquals(HRESULT.S_OK, hr);

        this.operation.setRequestedParameter(ROCF_ParameterName.rocfPN_permittedTcVcidSet);
        this.ircfGetParam = this.operation;
        hr = this.obj.setUpGetParameter(this.ircfGetParam);
        assertEquals(HRESULT.S_OK, hr);

        this.operation.setRequestedParameter(ROCF_ParameterName.rocfPN_permittedUpdateModeSet);
        this.ircfGetParam = this.operation;
        hr = this.obj.setUpGetParameter(this.ircfGetParam);
        assertEquals(HRESULT.S_OK, hr);
    }

    @Test
    public void testCheckGvcId()
    {
        ROCF_Gvcid[] list = new ROCF_Gvcid[3];
        list[0] = new ROCF_Gvcid(ROCF_ChannelType.rocfCT_MasterChannel, 0, 0, 0);
        list[1] = new ROCF_Gvcid(ROCF_ChannelType.rocfCT_MasterChannel, 1, 1, 1);
        list[2] = new ROCF_Gvcid(ROCF_ChannelType.rocfCT_MasterChannel, 2, 2, 2);
        this.obj.setPermittedGvcIdSet(list);
        HRESULT hr = this.obj.checkGvcId(this.gvcIdM);
        assertEquals(HRESULT.S_OK, hr);

        list = new ROCF_Gvcid[3];
        list[0] = new ROCF_Gvcid(ROCF_ChannelType.rocfCT_VirtualChannel, 0, 0, 0);
        list[1] = new ROCF_Gvcid(ROCF_ChannelType.rocfCT_VirtualChannel, 1, 1, 1);
        list[2] = new ROCF_Gvcid(ROCF_ChannelType.rocfCT_VirtualChannel, 2, 2, 2);
        this.obj.setPermittedGvcIdSet(list);
        hr = this.obj.checkGvcId(this.gvcIdV);
        assertEquals(HRESULT.S_OK, hr);
    }

    @Test
    public void testCheckControlWordType()
    {
        ROCF_ControlWordType cwType = ROCF_ControlWordType.rocfCWT_allControlWords;
        ROCF_ControlWordType[] list = new ROCF_ControlWordType[2];
        list[0] = ROCF_ControlWordType.rocfCWT_allControlWords;
        list[1] = ROCF_ControlWordType.rocfCWT_allControlWords;
        this.obj.setPermittedControlWordTypeSet(list);
        HRESULT hr = this.obj.checkControlWordType(cwType);
        System.out.println(hr);
    }

}
