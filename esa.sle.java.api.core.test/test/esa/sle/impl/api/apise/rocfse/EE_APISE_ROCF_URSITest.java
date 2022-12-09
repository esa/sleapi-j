package esa.sle.impl.api.apise.rocfse;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_ReportRequestType;
import ccsds.sle.api.isle.it.SLE_UnbindReason;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import esa.sle.impl.api.apiop.rocfop.EE_ROCF_GetParameter;
import esa.sle.impl.api.apiop.rocfop.EE_ROCF_Start;
import esa.sle.impl.api.apiop.sleop.EE_SLE_Bind;
import esa.sle.impl.api.apiop.sleop.EE_SLE_PeerAbort;
import esa.sle.impl.api.apiop.sleop.EE_SLE_ScheduleStatusReport;
import esa.sle.impl.api.apiop.sleop.EE_SLE_Stop;
import esa.sle.impl.api.apiop.sleop.EE_SLE_Unbind;
import esa.sle.impl.api.apise.slese.EE_APISE_ServiceElement;
import esa.sle.impl.api.apiut.EE_SLE_LibraryInstance;
import esa.sle.impl.api.apiut.EE_SLE_SII;
import esa.sle.impl.ifs.gen.EE_StubReporter;

public class EE_APISE_ROCF_URSITest
{

    EE_APISE_ROCF_URSI obj;

    int version;

    ISLE_ServiceInform serviceInfo;

    ISLE_Reporter reporter;


    @Before
    public void setUp() throws Exception
    {
        this.version = 1;
        this.obj = new EE_APISE_ROCF_URSI(EE_SLE_LibraryInstance.LIBRARY_INSTANCE_KEY, this.serviceInfo);
        this.reporter = new EE_StubReporter();
    }

    @Test
    public void testDoInitiateOpInvoke() throws SleApiException
    {

        /************************ Operation: EE_SLE_Bind ****************************/
        EE_SLE_Bind bindOp = new EE_SLE_Bind(SLE_ApplicationIdentifier.sleAI_rtnChOcf, this.version, this.reporter);
        bindOp.setResponderIdentifier("responder");
        bindOp.setResponderPortIdentifier("reponderPortId");

        ISLE_SII sii = new EE_SLE_SII();
        sii.setInitialFormat();
        sii.addLocalRDN(15, "justOneElementtest");

        bindOp.setServiceInstanceId(sii);
        bindOp.setServiceType(SLE_ApplicationIdentifier.sleAI_rtnChOcf);

        EE_APISE_ServiceElement.initialiseInstance(EE_SLE_LibraryInstance.LIBRARY_INSTANCE_KEY); // used also for the test
                                                      // bellow
        this.obj.setBindInitiative(SLE_AppRole.sleAR_user);
        HRESULT hr = this.obj.doInitiateOpInvoke(bindOp);
        assertEquals(HRESULT.SLE_E_CONFIG, hr);

        /************************ Operation: EE_SLE_Unbind ****************************/
        EE_SLE_Unbind unBindOp = new EE_SLE_Unbind(SLE_ApplicationIdentifier.sleAI_rtnChOcf,
                                                   this.version,
                                                   this.reporter);
        unBindOp.setUnbindReason(SLE_UnbindReason.sleUBR_end);
        hr = this.obj.doInitiateOpInvoke(unBindOp);
        assertEquals(HRESULT.SLE_E_PROTOCOL, hr);

        /************************ Operation: EE_RAF_Start ****************************/
        EE_ROCF_Start opStart = new EE_ROCF_Start(this.version, this.reporter);
        hr = this.obj.doInitiateOpInvoke(opStart);
        assertEquals(HRESULT.SLE_E_MISSINGARG, hr);

        /************************ Operation: EE_SLE_Stop ****************************/
        EE_SLE_Stop opStop = new EE_SLE_Stop(SLE_ApplicationIdentifier.sleAI_rtnChOcf, this.version, this.reporter);
        hr = this.obj.doInitiateOpInvoke(opStop);
        assertEquals(HRESULT.SLE_E_PROTOCOL, hr);

        /************************ Operation: EE_SLE_ScheduleStatusReport ****************************/
        EE_SLE_ScheduleStatusReport scheduleStatusOp = new EE_SLE_ScheduleStatusReport(SLE_ApplicationIdentifier.sleAI_rtnChOcf,
                                                                                       this.version,
                                                                                       this.reporter);
        scheduleStatusOp.setReportRequestType(SLE_ReportRequestType.sleRRT_periodically);
        scheduleStatusOp.setReportingCycle(5);
        hr = this.obj.doInitiateOpInvoke(scheduleStatusOp);
        assertEquals(HRESULT.SLE_E_PROTOCOL, hr);

        scheduleStatusOp.setReportingCycle(1);
        hr = this.obj.doInitiateOpInvoke(scheduleStatusOp);
        assertEquals(HRESULT.SLE_E_RANGE, hr);

        /************************ Operation: EE_RAF_GetParameter ****************************/
        EE_ROCF_GetParameter getParamOp = new EE_ROCF_GetParameter(this.version, this.reporter);
        hr = this.obj.doInitiateOpInvoke(getParamOp);
        assertEquals(HRESULT.SLE_E_PROTOCOL, hr);

        /************************ Operation: EE_SLE_PeerAbort ****************************/
        EE_SLE_PeerAbort peerAbortOp = new EE_SLE_PeerAbort(SLE_ApplicationIdentifier.sleAI_rtnChOcf,
                                                            this.version,
                                                            this.reporter);
        hr = this.obj.doInitiateOpInvoke(peerAbortOp);
        assertEquals(HRESULT.SLE_E_PROTOCOL, hr);
    }

   
}
