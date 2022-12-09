/**
 * @(#) EE_APIPX_Asn1SleTranslator.java
 */

package esa.sle.impl.api.apipx.pxdel;

import isp1.sle.bind.pdus.SleUnbindInvocationPdu;
import isp1.sle.bind.pdus.SleUnbindReturnPdu;

import com.beanit.jasn1.ber.types.BerInteger;
//import com.beanit.jasn1.ber.types.BerNull;

import com.beanit.jasn1.ber.types.BerNull;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iop.ISLE_ScheduleStatusReport;
import ccsds.sle.api.isle.iop.ISLE_Stop;
import ccsds.sle.api.isle.iop.ISLE_Unbind;
import ccsds.sle.api.isle.it.SLE_DiagnosticType;
import ccsds.sle.api.isle.it.SLE_Diagnostics;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_ReportRequestType;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.it.SLE_ScheduleStatusReportDiagnostic;
import ccsds.sle.api.isle.it.SLE_UnbindReason;
import ccsds.sle.api.isle.iutl.ISLE_Credentials;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import ccsds.sle.transfer.service.bind.types.UnbindReason;
import ccsds.sle.transfer.service.common.pdus.DiagnosticScheduleStatusReport;
import ccsds.sle.transfer.service.common.pdus.ReportRequestType;
import ccsds.sle.transfer.service.common.pdus.ReportingCycle;
import ccsds.sle.transfer.service.common.pdus.SleAcknowledgement;
import ccsds.sle.transfer.service.common.pdus.SleAcknowledgement.Result;
import ccsds.sle.transfer.service.common.pdus.SleScheduleStatusReportInvocation;
import ccsds.sle.transfer.service.common.pdus.SleScheduleStatusReportReturn;
import ccsds.sle.transfer.service.common.pdus.SleStopInvocation;
import ccsds.sle.transfer.service.bind.types.SleUnbindReturn;
import ccsds.sle.transfer.service.common.types.Diagnostics;
import ccsds.sle.transfer.service.common.types.InvokeId;

/**
 * ASN.1 Sle Translator The class contains methods that can be used to encode
 * and decode common Sle PDUs (exept the Sle Bind Pdu)
 */
public class EE_APIPX_Asn1SleTranslator extends EE_APIPX_Asn1BindTranslator
{
    /**
     * nConstructor of the class which takes the ASNSDK context object as
     * parameter.
     */
    public EE_APIPX_Asn1SleTranslator(ISLE_OperationFactory pOpFactory,
                                      ISLE_UtilFactory pUtilFactory,
                                      EE_APIPX_PDUTranslator pdutranslator,
                                      int sleVersionNumber)
    {
        super(pOpFactory, pUtilFactory, pdutranslator, sleVersionNumber);
    }

    /**
     * Fills the object used for the encoding of Stop invoke operation. S_OK The
     * Stop operation has been encoded. E_FAIL Unable to encode the Stop
     * operation.
     * 
     * @throws SleApiException
     */
    protected void encodeStopInvokeOp(ISLE_Operation pOperation, SleStopInvocation eea_stop_o) throws SleApiException
    {
        ISLE_Stop pStopOperation = pOperation.queryInterface(ISLE_Stop.class);
        if (pStopOperation != null)
        {
            // the invoker credentials
            ISLE_Credentials pCredentials = null;
            pCredentials = pStopOperation.getInvokerCredentials();
            eea_stop_o.setInvokerCredentials(encodeCredentials(pCredentials));

            // the invoke id
            eea_stop_o.setInvokeId(new InvokeId(pStopOperation.getInvokeId()));
        }
    }

    /**
     * Instantiates and fills the STOP invoke operation from the object. S_OK A
     * new STOP operation has been instantiated. E_FAIL Unable to instantiate a
     * STOP operation.
     * 
     * @throws SleApiException
     */
    protected ISLE_Operation decodeStopInvokeOp(SleStopInvocation eea_stop_o) throws SleApiException
    {
        ISLE_Operation pOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_stop;

        ISLE_Stop pStopOperation = null;
        pStopOperation = this.operationFactory.createOperation(ISLE_Stop.class,
                                                               opType,
                                                               this.serviceType,
                                                               this.sleVersionNumber);
        if (pStopOperation != null)
        {
            pOperation = pStopOperation.queryInterface(ISLE_Operation.class);
            if (pOperation != null)
            {
                // the invoker credentials
                ISLE_Credentials pcredentials = null;
                pcredentials = decodeCredentials(eea_stop_o.getInvokerCredentials());
                if (pcredentials != null)
                {
                    pStopOperation.putInvokerCredentials(pcredentials);
                }

                // the invoker id
                pStopOperation.setInvokeId((int) eea_stop_o.getInvokeId().value.intValue());
            }
        }

        return pOperation;
    }

    /**
     * Fills the object used for the encoding of Stop return operation. S_OK The
     * Stop operation has been encoded. E_FAIL Unable to encode the Stop
     * operation.
     * 
     * @throws SleApiException
     */
    protected void encodeStopReturnOp(ISLE_Operation pOperation, SleAcknowledgement eea_stop_o) throws SleApiException
    {
        ISLE_Stop pStopOperation = null;
        pStopOperation = pOperation.queryInterface(ISLE_Stop.class);
        if (pStopOperation != null)
        {
            // the credentials
            ISLE_Credentials pcredentials = null;
            pcredentials = pStopOperation.getPerformerCredentials();
            eea_stop_o.setCredentials(encodeCredentials(pcredentials));

            // the invoke id
            eea_stop_o.setInvokeId(new InvokeId(pStopOperation.getInvokeId()));

            // the result
            if (pStopOperation.getResult() == SLE_Result.sleRES_positive)
            {
            	Result res = new Result();
            	res.setPositiveResult(new BerNull());
                eea_stop_o.setResult(res);
            }
            else
            {
                // negative result
            	Result res = new Result();
            	res.setNegativeResult(new Diagnostics(pStopOperation.getResult().getCode()));
                eea_stop_o.setResult(res);
            }
        }
    }

    /**
     * Instantiates and fills the STOP return operation from the object. S_OK A
     * new STOP operation has been instantiated. E_FAIL Unable to instantiate a
     * STOP operation.
     * 
     * @throws SleApiException
     */
    protected ISLE_Operation decodeStopReturnOp(SleAcknowledgement eea_stop_o) throws SleApiException
    {
        ISLE_Operation pOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_stop;

        pOperation = this.pduTranslator.getReturnOp(eea_stop_o.getInvokeId(), opType);
        if (pOperation != null)
        {
            ISLE_Stop pStopOperation = null;
            pStopOperation = pOperation.queryInterface(ISLE_Stop.class);
            if (pStopOperation != null)
            {
                // the credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eea_stop_o.getCredentials());
                if (pCredentials != null)
                {
                    pStopOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pStopOperation.setInvokeId((int) eea_stop_o.getInvokeId().value.intValue());

                if (eea_stop_o.getResult().getPositiveResult() != null)
                {
                    pStopOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    SLE_Diagnostics diag = SLE_Diagnostics
                            .getDiagnosticsByCode(eea_stop_o.getResult().getNegativeResult().value.intValue());
                    pStopOperation.setDiagnostics(diag);
                }
            }
            else
            {
                throw new SleApiException(HRESULT.E_FAIL);
            }
        }

        return pOperation;
    }

    /**
     * Fills the object used for the encoding of Unbind invoke operation. S_OK
     * The Unbind operation has been encoded. E_FAIL Unable to encode the Unbind
     * operation.
     * 
     * @throws SleApiException
     */
    protected void encodeUnbindInvokeOp(ISLE_Operation pOperation, SleUnbindInvocationPdu eea_unbind_o) throws SleApiException
    {
        ISLE_Unbind pUnbindOperation = null;
        pUnbindOperation = pOperation.queryInterface(ISLE_Unbind.class);
        if (pUnbindOperation != null)
        {
            // the invoker credentials
            ISLE_Credentials pCredentials = null;
            pCredentials = pUnbindOperation.getInvokerCredentials();
            eea_unbind_o.setInvokerCredentials(encodeCredentials(pCredentials));

            // the unbind reason
            eea_unbind_o.setUnbindReason(new UnbindReason(pUnbindOperation.getUnbindReason().getCode()));
        }
    }

    /**
     * Instantiates and fills the UNBIND invoke operation from the object. S_OK
     * A new UNBIND operation has been instantiated. E_FAIL Unable to
     * Instantiate a UNBIND operation.
     * 
     * @throws SleApiException
     */
    protected ISLE_Operation decodeUnbindInvokeOp(SleUnbindInvocationPdu eea_unbind_o) throws SleApiException
    {
        ISLE_Operation pOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_unbind;
        ISLE_Unbind pUnbindOperation = null;

        pUnbindOperation = this.operationFactory.createOperation(ISLE_Unbind.class,
                                                                 opType,
                                                                 this.serviceType,
                                                                 this.sleVersionNumber);
        if (pUnbindOperation != null)
        {
            pOperation = pUnbindOperation.queryInterface(ISLE_Operation.class);
            if (pOperation != null)
            {
                // the invoker credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eea_unbind_o.getInvokerCredentials());
                if (pCredentials != null)
                {
                    pUnbindOperation.putInvokerCredentials(pCredentials);
                }

                // the unbind reason
                SLE_UnbindReason unbindReason = SLE_UnbindReason
                        .getUnbindReasonByCode(eea_unbind_o.getUnbindReason().value.intValue());
                pUnbindOperation.setUnbindReason(unbindReason);
            }
        }

        return pOperation;
    }

    /**
     * Fills the object used for the encoding of Unbind return operation. S_OK
     * The Unbind operation has been encoded. E_FAIL Unable to encode the Unbind
     * operation.
     * 
     * @throws SleApiException
     */
    protected void encodeUnbindReturnOp(ISLE_Operation pOperation, SleUnbindReturnPdu eea_unbind_o) throws SleApiException
    {
        ISLE_Unbind pUnbindOperation = null;
        pUnbindOperation = pOperation.queryInterface(ISLE_Unbind.class);
        if (pUnbindOperation != null)
        {
            // the performer credentials
            ISLE_Credentials pCredentials = null;
            pCredentials = pUnbindOperation.getPerformerCredentials();
            eea_unbind_o.setResponderCredentials(encodeCredentials(pCredentials));

            // the result
            SleUnbindReturn.Result res = new SleUnbindReturn.Result();
            res.setPositive(new BerNull());
            eea_unbind_o.setResult(res);
        }
    }

    /**
     * Instantiates and fills the UNBIND return operation from the object. S_OK
     * A new UNBIND operation has been instantiated. E_FAIL Unable to
     * Instantiate a UNBIND operation.
     * 
     * @throws SleApiException
     */
    protected ISLE_Operation decodeUnbindReturnOp(SleUnbindReturnPdu eea_unbind_o) throws SleApiException
    {
        ISLE_Operation pOperation = null;
        ISLE_Unbind pUnbindOperation = null;

        pUnbindOperation = this.pduTranslator.getUnbindReturnOp();
        if (pUnbindOperation != null)
        {
            pOperation = pUnbindOperation.queryInterface(ISLE_Operation.class);
            if (pOperation != null)
            {
                // the responder credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eea_unbind_o.getResponderCredentials());
                if (pCredentials != null)
                {
                    pUnbindOperation.putPerformerCredentials(pCredentials);
                }

                // the result
                if (eea_unbind_o.getResult().getPositive() != null)
                {
                    pUnbindOperation.setPositiveResult();
                }
            }
            else
            {
                throw new SleApiException(HRESULT.E_FAIL);
            }
        }

        return pOperation;
    }

    /**
     * Fills the object used for the encoding of ScheduleStatusReport invoke
     * operation. S_OK The ScheduleStatusReport operation has been encoded.
     * E_FAIL Unable to encode the ScheduleStatusReport operation.
     * 
     * @throws SleApiException
     */
    protected void encodeScheduleSRInvokeOp(ISLE_Operation pOperation,
                                            SleScheduleStatusReportInvocation eea_scheduleSR_o) throws SleApiException
    {
        ISLE_ScheduleStatusReport pSSROperation = null;
        pSSROperation = pOperation.queryInterface(ISLE_ScheduleStatusReport.class);
        if (pSSROperation != null)
        {
            // the invoker credentials
            ISLE_Credentials pCredentials = null;
            pCredentials = pSSROperation.getInvokerCredentials();
            eea_scheduleSR_o.setInvokerCredentials(encodeCredentials(pCredentials));

            // the invoke id
            eea_scheduleSR_o.setInvokeId(new InvokeId(pSSROperation.getInvokeId()));

            // the report request type
            switch (pSSROperation.getReportRequestType())
            {
            case sleRRT_immediately:
            {
            	ReportRequestType rrt = new ReportRequestType();
            	rrt.setImmediately(new BerNull());
                eea_scheduleSR_o.setReportRequestType(rrt);
                break;
            }
            case sleRRT_periodically:
            {
            	ReportRequestType rrt = new ReportRequestType();
            	rrt.setPeriodically(new ReportingCycle(pSSROperation.getReportingCycle()));
                eea_scheduleSR_o.setReportRequestType(rrt);
                break;
            }
            case sleRRT_stop:
            {
            	ReportRequestType rrt = new ReportRequestType();
            	rrt.setStop(new BerNull());
                eea_scheduleSR_o.setReportRequestType(rrt);
                break;
            }
            default:
            {
                eea_scheduleSR_o.setReportRequestType(new ReportRequestType());
            }
            }
        }
    }

    /**
     * Instantiates and fills the SCHEDULE-STATUS-REPORT invoke operation from
     * the object. S_OK A new SCHEDULE-STATUS-REPORT operation has been
     * Instantiated. E_FAIL Unable to instantiate a SCHEDULE-STATUS-REPORT
     * operation.
     * 
     * @throws SleApiException
     */
    protected ISLE_Operation decodeScheduleSRInvokeOp(SleScheduleStatusReportInvocation eea_scheduleSR_o) throws SleApiException
    {
        ISLE_Operation pOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_scheduleStatusReport;
        ISLE_ScheduleStatusReport pSSROperation = null;
        pSSROperation = this.operationFactory.createOperation(ISLE_ScheduleStatusReport.class,
                                                              opType,
                                                              this.serviceType,
                                                              this.sleVersionNumber);
        if (pSSROperation != null)
        {
            pOperation = pSSROperation.queryInterface(ISLE_Operation.class);
            if (pOperation != null)
            {
                // the invoker credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eea_scheduleSR_o.getInvokerCredentials());
                if (pCredentials != null)
                {
                    pSSROperation.putInvokerCredentials(pCredentials);
                }

                // the invoke id
                pSSROperation.setInvokeId((int) eea_scheduleSR_o.getInvokeId().value.intValue());

                // the report request
                if (eea_scheduleSR_o.getReportRequestType().getImmediately() != null)
                {
                    pSSROperation.setReportRequestType(SLE_ReportRequestType.sleRRT_immediately);
                }
                else if (eea_scheduleSR_o.getReportRequestType().getPeriodically() != null)
                {
                    pSSROperation.setReportRequestType(SLE_ReportRequestType.sleRRT_periodically);
                    pSSROperation.setReportingCycle( eea_scheduleSR_o.getReportRequestType().getPeriodically().value.intValue());
                }
                else if (eea_scheduleSR_o.getReportRequestType().getStop() != null)
                {
                    pSSROperation.setReportRequestType(SLE_ReportRequestType.sleRRT_stop);
                }
                else
                {
                    pSSROperation.setReportRequestType(SLE_ReportRequestType.sleRRT_invalid);
                }
            }
        }

        return pOperation;
    }

    /**
     * Fills the object used for the encoding of SchedulestatusReport return
     * operation. S_OK The ScheduleStatusReport operation has been encoded.
     * E_FAIL Unable to encode the ScheduleStatusReport operation.
     * 
     * @throws SleApiException
     */
    protected void encodeScheduleSRReturnOp(ISLE_Operation pOperation, SleScheduleStatusReportReturn eea_scheduleSR_o) throws SleApiException
    {
        ISLE_ScheduleStatusReport pSSROperation = null;
        pSSROperation = pOperation.queryInterface(ISLE_ScheduleStatusReport.class);
        if (pSSROperation != null)
        {
            // the performer credentials
            ISLE_Credentials pCredentials = null;
            pCredentials = pSSROperation.getPerformerCredentials();
            eea_scheduleSR_o.setPerformerCredentials(encodeCredentials(pCredentials));

            // the invoke id
            eea_scheduleSR_o.setInvokeId(new InvokeId(pSSROperation.getInvokeId()));

            // the result
            if (pSSROperation.getResult() == SLE_Result.sleRES_positive)
            {
                ccsds.sle.transfer.service.common.pdus.SleScheduleStatusReportReturn.Result posResult = new ccsds.sle.transfer.service.common.pdus.SleScheduleStatusReportReturn.Result();
                posResult.setPositiveResult(new BerNull());
                eea_scheduleSR_o.setResult(posResult);
            }
            else
            {
                ccsds.sle.transfer.service.common.pdus.SleScheduleStatusReportReturn.Result negResult = new ccsds.sle.transfer.service.common.pdus.SleScheduleStatusReportReturn.Result();
                negResult.setPositiveResult(null);

                if (pSSROperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
                {
                    DiagnosticScheduleStatusReport repSpecific = new DiagnosticScheduleStatusReport();
                    repSpecific.setCommon(null);

                    switch (pSSROperation.getSSRDiagnostic())
                    {
                    case sleSSD_notSupportedInThisDeliveryMode:
                    {
                        repSpecific.setSpecific( new BerInteger(SLE_ScheduleStatusReportDiagnostic.sleSSD_notSupportedInThisDeliveryMode
                                .getCode()));
                        break;
                    }
                    case sleSSD_invalidReportingCycle:
                    {
                        repSpecific.setSpecific( new BerInteger(SLE_ScheduleStatusReportDiagnostic.sleSSD_invalidReportingCycle
                                .getCode()));
                        break;
                    }
                    case sleSSD_alreadyStopped:
                    {
                        repSpecific.setSpecific( new BerInteger(SLE_ScheduleStatusReportDiagnostic.sleSSD_alreadyStopped.getCode()));
                        break;
                    }
                    default:
                    {
                        repSpecific.setSpecific( new BerInteger(SLE_ScheduleStatusReportDiagnostic.sleSSD_invalid.getCode()));
                        break;
                    }
                    }

                    negResult.setNegativeResult(repSpecific);
                }
                else
                {
                    // common diagnostic
                    DiagnosticScheduleStatusReport repCommon = new DiagnosticScheduleStatusReport();
                    repCommon.setSpecific( null);
                    repCommon.setCommon(new Diagnostics(pSSROperation.getDiagnostics().getCode()));
                    negResult.setNegativeResult(repCommon);
                }
            }
        }
    }

    /**
     * Encodes and fills the SCHEDULE-STATUS-REPORT return operation from the
     * object. S_OK A new SCHEDULE-STATUS-REPORT operation has been
     * Instantiated. E_FAIL Unable to instantiate a SCHEDULE-STATUS-REPORT
     * operation.
     * 
     * @throws SleApiException
     */
    protected ISLE_Operation decodeScheduleSRReturnOp(SleScheduleStatusReportReturn eea_scheduleSR_o) throws SleApiException
    {
        ISLE_Operation pOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_scheduleStatusReport;
        ISLE_ScheduleStatusReport pSSROperation = null;

        pOperation = this.pduTranslator.getReturnOp(eea_scheduleSR_o.getInvokeId(), opType);
        if (pOperation != null)
        {
            pSSROperation = pOperation.queryInterface(ISLE_ScheduleStatusReport.class);
            if (pSSROperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eea_scheduleSR_o.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pSSROperation.setPerformerCredentials(pCredentials);
                }

                // the invoke id
                pSSROperation.setInvokeId((int) eea_scheduleSR_o.getInvokeId().value.intValue());

                // the result
                if (eea_scheduleSR_o.getResult().getPositiveResult() != null)
                {
                    pSSROperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eea_scheduleSR_o.getResult().getNegativeResult().getSpecific() != null)
                    {
                        int specValue = (int) eea_scheduleSR_o.getResult().getNegativeResult().getSpecific().value.intValue();
                        pSSROperation.setSSRDiagnostic(SLE_ScheduleStatusReportDiagnostic
                                .getSSRDiagnosticsByCode(specValue));
                    }
                    else
                    {
                        // common diagnostic
                        int commValue = (int) eea_scheduleSR_o.getResult().getNegativeResult().getCommon().value.intValue();
                        pSSROperation.setDiagnostics(SLE_Diagnostics.getDiagnosticsByCode(commValue));
                    }
                }
            }
        }

        return pOperation;
    }
}
