/**
 * @(#) EE_APIPX_Asn1RafTranslator.java
 */

package esa.sle.impl.api.apipx.pxdel;

import isp1.raf.pdus.RafGetParameterInvocationPdu;
import isp1.raf.pdus.RafGetParameterReturnPdu;
import isp1.raf.pdus.RafScheduleStatusReportInvocationPdu;
import isp1.raf.pdus.RafScheduleStatusReportReturnPdu;
import isp1.raf.pdus.RafStartInvocationPdu;
import isp1.raf.pdus.RafStartReturnPdu;
import isp1.raf.pdus.RafStatusReportInvocationPdu;
import isp1.raf.pdus.RafStatusReportInvocationV1Pdu;
import isp1.raf.pdus.RafStopInvocationPdu;
import isp1.raf.pdus.RafStopReturnPdu;
import isp1.raf.pdus.RafTransferBufferPdu;
import isp1.sle.bind.pdus.SleBindInvocationPdu;
import isp1.sle.bind.pdus.SleBindReturnPdu;
import isp1.sle.bind.pdus.SleUnbindInvocationPdu;
import isp1.sle.bind.pdus.SleUnbindReturnPdu;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import com.beanit.jasn1.ber.ReverseByteArrayOutputStream ;
import com.beanit.jasn1.ber.BerTag;
import com.beanit.jasn1.ber.types.BerInteger;
import com.beanit.jasn1.ber.types.BerNull;
import com.beanit.jasn1.ber.types.BerObjectIdentifier;
import com.beanit.jasn1.ber.types.BerOctetString;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iop.ISLE_TransferBuffer;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_DiagnosticType;
import ccsds.sle.api.isle.it.SLE_Diagnostics;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.iutl.ISLE_Credentials;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import ccsds.sle.api.isrv.iraf.IRAF_GetParameter;
import ccsds.sle.api.isrv.iraf.IRAF_Start;
import ccsds.sle.api.isrv.iraf.IRAF_StatusReport;
import ccsds.sle.api.isrv.iraf.IRAF_SyncNotify;
import ccsds.sle.api.isrv.iraf.IRAF_TransferData;
import ccsds.sle.api.isrv.iraf.types.RAF_AntennaIdFormat;
import ccsds.sle.api.isrv.iraf.types.RAF_DeliveryMode;
import ccsds.sle.api.isrv.iraf.types.RAF_FrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_GetParameterDiagnostic;
import ccsds.sle.api.isrv.iraf.types.RAF_LockStatus;
import ccsds.sle.api.isrv.iraf.types.RAF_ParFrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_ParameterName;
import ccsds.sle.api.isrv.iraf.types.RAF_ProductionStatus;
import ccsds.sle.api.isrv.iraf.types.RAF_RequestedFrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_StartDiagnostic;
import ccsds.sle.transfer.service.raf.structures.RafGetParameter.ParMinReportingCycle;
import ccsds.sle.transfer.service.common.pdus.ReportingCycle;
import ccsds.sle.transfer.service.common.types.Diagnostics;
import ccsds.sle.transfer.service.common.types.IntPosShort;
import ccsds.sle.transfer.service.common.types.IntUnsignedLong;
import ccsds.sle.transfer.service.common.types.InvokeId;
import ccsds.sle.transfer.service.common.types.ParameterName;
import ccsds.sle.transfer.service.common.types.SpaceLinkDataUnit;
import ccsds.sle.transfer.service.raf.outgoing.pdus.FrameOrNotification;
import ccsds.sle.transfer.service.raf.outgoing.pdus.RafStartReturn.Result;
import ccsds.sle.transfer.service.raf.outgoing.pdus.RafSyncNotifyInvocation;
import ccsds.sle.transfer.service.raf.outgoing.pdus.RafTransferDataInvocation;
import ccsds.sle.transfer.service.raf.structures.AntennaId;
import ccsds.sle.transfer.service.raf.structures.CarrierLockStatus;
import ccsds.sle.transfer.service.raf.structures.CurrentReportingCycle;
import ccsds.sle.transfer.service.raf.structures.DiagnosticRafGet;
import ccsds.sle.transfer.service.raf.structures.DiagnosticRafStart;
import ccsds.sle.transfer.service.raf.structures.FrameQuality;
import ccsds.sle.transfer.service.raf.structures.FrameSyncLockStatus;
import ccsds.sle.transfer.service.raf.structures.LockStatus;
import ccsds.sle.transfer.service.raf.structures.LockStatusReport;
import ccsds.sle.transfer.service.raf.structures.Notification;
import ccsds.sle.transfer.service.raf.structures.PermittedFrameQualitySet;
import ccsds.sle.transfer.service.raf.structures.RafDeliveryMode;
import ccsds.sle.transfer.service.raf.structures.RafGetParameter;
import ccsds.sle.transfer.service.raf.structures.RafGetParameter.ParBufferSize;
import ccsds.sle.transfer.service.raf.structures.RafGetParameter.ParDeliveryMode;
import ccsds.sle.transfer.service.raf.structures.RafGetParameter.ParLatencyLimit;
import ccsds.sle.transfer.service.raf.structures.RafGetParameter.ParLatencyLimit.ParameterValue;
import ccsds.sle.transfer.service.raf.structures.RafGetParameter.ParPermittedFrameQuality;
import ccsds.sle.transfer.service.raf.structures.RafGetParameter.ParReportingCycle;
import ccsds.sle.transfer.service.raf.structures.RafGetParameter.ParReqFrameQuality;
import ccsds.sle.transfer.service.raf.structures.RafGetParameter.ParReturnTimeout;
import ccsds.sle.transfer.service.raf.structures.RafParameterName;
import ccsds.sle.transfer.service.raf.structures.RafProductionStatus;
import ccsds.sle.transfer.service.raf.structures.RequestedFrameQuality;
import ccsds.sle.transfer.service.raf.structures.SymbolLockStatus;
import ccsds.sle.transfer.service.raf.structures.TimeoutPeriod;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * ASN.1 Raf Translator The class encodes and decodes RAF PDU's. When decoding,
 * the decoded RAF operation is instantiated. The class contains several private
 * methods used to encode and decode some parts of the RAF operations.
 */
public class EE_APIPX_Asn1RafTranslator extends EE_APIPX_Asn1SleTranslator
{
    /**
     * Constructor of the class which takes the ASNSDK context object as
     * parameter.
     */
    public EE_APIPX_Asn1RafTranslator(ISLE_OperationFactory pOpFactory,
                                      ISLE_UtilFactory pUtilFactory,
                                      EE_APIPX_PDUTranslator pdutranslator,
                                      int sleVersionNumber)
    {
        super(pOpFactory, pUtilFactory, pdutranslator, sleVersionNumber);
        this.serviceType = SLE_ApplicationIdentifier.sleAI_rtnAllFrames;
    }

    /**
     * Allocates and fills the object used for the encoding of Raf Operation for
     * version 1 PDUs. S_OK The RAF operation has been encoded. E_FAIL Unable to
     * encode the RAF operation.
     * 
     * @throws SleApiException
     * @throws IOException
     */
    public byte[] encodeRafOp(ISLE_Operation pRafOperation, boolean isInvoke) throws SleApiException, IOException
    {
        ReverseByteArrayOutputStream  berBAOStream = new ReverseByteArrayOutputStream (10, true);

        switch (pRafOperation.getOperationType())
        {
        case sleOT_bind:
        {
            if (isInvoke)
            {
                SleBindInvocationPdu obj = new SleBindInvocationPdu();
                encodeBindInvokeOp(pRafOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                SleBindReturnPdu obj = new SleBindReturnPdu();
                encodeBindReturnOp(pRafOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_unbind:
        {
            if (isInvoke)
            {
                SleUnbindInvocationPdu obj = new SleUnbindInvocationPdu();
                encodeUnbindInvokeOp(pRafOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                SleUnbindReturnPdu obj = new SleUnbindReturnPdu();
                encodeUnbindReturnOp(pRafOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_stop:
        {
            if (isInvoke)
            {
                RafStopInvocationPdu obj = new RafStopInvocationPdu();
                encodeStopInvokeOp(pRafOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                RafStopReturnPdu obj = new RafStopReturnPdu();
                encodeStopReturnOp(pRafOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_scheduleStatusReport:
        {
            if (isInvoke)
            {
                RafScheduleStatusReportInvocationPdu obj = new RafScheduleStatusReportInvocationPdu();
                encodeScheduleSRInvokeOp(pRafOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                RafScheduleStatusReportReturnPdu obj = new RafScheduleStatusReportReturnPdu();
                encodeScheduleSRReturnOp(pRafOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_start:
        {
            IRAF_Start pOp = null;
            pOp = pRafOperation.queryInterface(IRAF_Start.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    RafStartInvocationPdu obj = new RafStartInvocationPdu();
                    encodeStartInvokeOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
                else
                {
                    RafStartReturnPdu obj = new RafStartReturnPdu();
                    encodeStartReturnOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
            }

            break;
        }
        case sleOT_getParameter:
        {
            IRAF_GetParameter pOp = null;
            pOp = pRafOperation.queryInterface(IRAF_GetParameter.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    RafGetParameterInvocationPdu obj = new RafGetParameterInvocationPdu();
                    encodeGetParameterInvokeOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
                else
                {
                	if (this.sleVersionNumber <= 4)
                    {
                		RafGetParameterReturnPdu obj = new RafGetParameterReturnPdu();
                		encodeGetParameterReturnOpV1to4(pOp, obj);
                		obj.encode(berBAOStream, true);
                    }
                	else
                	{
                		RafGetParameterReturnPdu obj = new RafGetParameterReturnPdu();
                		encodeGetParameterReturnOp(pOp, obj);
                		obj.encode(berBAOStream, true);
                	}
                }
            }

            break;
        }
        case sleOT_statusReport:
        {
            if (!isInvoke)
            {
                berBAOStream.close();
                throw new SleApiException(HRESULT.E_FAIL, "No status report return operation");
            }
            IRAF_StatusReport pOp = null;
            pOp = pRafOperation.queryInterface(IRAF_StatusReport.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    if (this.sleVersionNumber == 1)
                    {
                        RafStatusReportInvocationV1Pdu obj = new RafStatusReportInvocationV1Pdu();
                        encodeStatusReportOp(pOp, obj);
                        obj.encode(berBAOStream, true);
                    }
                    else
                    {
                        RafStatusReportInvocationPdu obj = new RafStatusReportInvocationPdu();
                        encodeStatusReportOp(pOp, obj);
                        obj.encode(berBAOStream, true);
                    }
                }
            }

            break;
        }
        case sleOT_transferData:
        {
            // not to be called directly --> must come from transfer buffer
            // encoding
            berBAOStream.close();
            throw new SleApiException(HRESULT.E_FAIL, "not to be called directly");
        }
        case sleOT_syncNotify:
        {
            // not to be called directly --> must come from transfer buffer
            // encoding
            berBAOStream.close();
            throw new SleApiException(HRESULT.E_FAIL, "not to be called directly");
        }
        case sleOT_transferBuffer:
        {
            ISLE_TransferBuffer pOp = null;
            pOp = pRafOperation.queryInterface(ISLE_TransferBuffer.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    RafTransferBufferPdu obj = new RafTransferBufferPdu();
                	encodeTransferBufferOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
            }

            break;
        }
        default:
        {
            berBAOStream.close();
            throw new SleApiException(HRESULT.E_FAIL, "Operation type not supported");
        }
        }

        return berBAOStream.getArray();
    }

    /**
     * Instantiates a new RAF operation from the version 1 object given as
     * parameter, and releases the object. S_OK A new RAF operation has been
     * Instantiated. E_FAIL Unable to instantiate a RAF operation.
     * 
     * @throws SleApiException
     * @throws IOException
     */
    public ISLE_Operation decodeRafOp(byte[] buffer, EE_Reference<Boolean> isInvoke) throws SleApiException,
                                                                                    IOException
    {
        InputStream is = new ByteArrayInputStream(buffer);
        ISLE_Operation pOperation = null;

        BerTag tag = new BerTag();
        tag.decode(is);
        if (tag.equals(SleBindInvocationPdu.tag))
        {
            ISLE_Bind pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_bind;
            pOp = this.operationFactory.createOperation(ISLE_Bind.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                SleBindInvocationPdu obj = new SleBindInvocationPdu();
                obj.decode(is, false);
                decodeBindInvokeOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(SleBindReturnPdu.tag))
        {
            ISLE_Bind pOp = null;
            SleBindReturnPdu obj = new SleBindReturnPdu();
            obj.decode(is, false); 
            pOp = decodeBindReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
            if (pOp != null)
            {
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(SleUnbindInvocationPdu.tag))
        {
            SleUnbindInvocationPdu obj = new SleUnbindInvocationPdu();
            obj.decode(is, false);
            pOperation = decodeUnbindInvokeOp(obj);
            isInvoke.setReference(new Boolean(true));
        }
        else if (tag.equals(SleUnbindReturnPdu.tag))
        {
            SleUnbindReturnPdu obj = new SleUnbindReturnPdu();
            obj.decode(is, false);
            pOperation = decodeUnbindReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
        }
        else if (tag.equals(RafStartInvocationPdu.tag))
        {
            IRAF_Start pOp = null;
            SLE_OpType opTye = SLE_OpType.sleOT_start;
            pOp = this.operationFactory.createOperation(IRAF_Start.class,
                                                        opTye,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                RafStartInvocationPdu obj = new RafStartInvocationPdu();
                obj.decode(is, false);
                decodeStartInvokeOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(RafStartReturnPdu.tag))
        {
            IRAF_Start pOp = null;
            RafStartReturnPdu obj = new RafStartReturnPdu();
            obj.decode(is, false);
            pOp = decodeStartReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
            if (pOp != null)
            {
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(RafStopInvocationPdu.tag))
        {
            RafStopInvocationPdu obj = new RafStopInvocationPdu();
            obj.decode(is, false);
            pOperation = decodeStopInvokeOp(obj);
            isInvoke.setReference(new Boolean(true));
        }
        else if (tag.equals(RafStopReturnPdu.tag))
        {
            RafStopReturnPdu obj = new RafStopReturnPdu();
            obj.decode(is, false);
            pOperation = decodeStopReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
        }
        else if (tag.equals(RafScheduleStatusReportInvocationPdu.tag))
        {
            RafScheduleStatusReportInvocationPdu obj = new RafScheduleStatusReportInvocationPdu();
            obj.decode(is, false);
            pOperation = decodeScheduleSRInvokeOp(obj);
            isInvoke.setReference(new Boolean(true));
        }
        else if (tag.equals(RafScheduleStatusReportReturnPdu.tag))
        {
            RafScheduleStatusReportReturnPdu obj = new RafScheduleStatusReportReturnPdu();
            obj.decode(is, false);
            pOperation = decodeScheduleSRReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
        }
        else if (tag.equals(RafGetParameterInvocationPdu.tag))
        {
            IRAF_GetParameter pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_getParameter;
            pOp = this.operationFactory.createOperation(IRAF_GetParameter.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                RafGetParameterInvocationPdu obj = new RafGetParameterInvocationPdu();
                obj.decode(is, false);
                decodeGetParameterInvokeOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(RafGetParameterReturnPdu.tag))
        {
            IRAF_GetParameter pOp = null;
            RafGetParameterReturnPdu obj = new RafGetParameterReturnPdu();
            obj.decode(is, false);
            pOp = decodeGetParameterReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
            if (pOp != null)
            {
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(RafTransferBufferPdu.tag))
        {
            ISLE_TransferBuffer pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_transferBuffer;
            pOp = this.operationFactory.createOperation(ISLE_TransferBuffer.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                RafTransferBufferPdu obj = new RafTransferBufferPdu();
                obj.decode(is, false);
                decodeTransferBufferOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(RafStatusReportInvocationPdu.tag))
        {
            IRAF_StatusReport pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_statusReport;
            pOp = this.operationFactory.createOperation(IRAF_StatusReport.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                if (this.sleVersionNumber == 1)
                {
                    RafStatusReportInvocationV1Pdu obj = new RafStatusReportInvocationV1Pdu();
                    obj.decode(is, false);
                    decodeStatusReportOp(obj, pOp);
                }
                else
                {
                    RafStatusReportInvocationPdu obj = new RafStatusReportInvocationPdu();
                    obj.decode(is, false);
                    decodeStatusReportOp(obj, pOp);
                }

                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else
        {
            throw new SleApiException(HRESULT.E_FAIL, "Unknown operation tag");
        }

        return pOperation;
    }

    /**
     * Fills the object used for the encoding of Raf Start invoke operation.
     * S_OK The RAF Start operation has been encoded. E_FAIL Unable to encode
     * the RAF Start operation.
     * 
     * @throws SleApiException
     */
    private void encodeStartInvokeOp(IRAF_Start pStartOperation, RafStartInvocationPdu eeaRafO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pStartOperation.getInvokerCredentials();
        eeaRafO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaRafO.setInvokeId(new InvokeId(pStartOperation.getInvokeId()));

        // the start time
        ISLE_Time pTime = null;
        pTime = pStartOperation.getStartTime();
        eeaRafO.setStartTime(encodeConditionalTime(pTime));

        // the stop time
        pTime = pStartOperation.getStopTime();
        eeaRafO.setStopTime(encodeConditionalTime(pTime));

        // the frame quality
        eeaRafO.setRequestedFrameQuality(new RequestedFrameQuality(pStartOperation.getRequestedFrameQuality().getCode()));
        //eeaRafO.getRequestedFrameQuality().value = pStartOperation.getRequestedFrameQuality().getCode();
    }

    /**
     * Fills the RAF START invoke operation from the object. S_OK The RAF Start
     * operation has been decoded. E_FAIL Unable to decode the RAF Start
     * operation.
     * 
     * @throws SleApiException
     */
    private void decodeStartInvokeOp(RafStartInvocationPdu eeaRafO, IRAF_Start pStartOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaRafO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pStartOperation.putInvokerCredentials(pCredentials);
        }

        // the invoker id
        pStartOperation.setInvokeId((int) eeaRafO.getInvokeId().value.intValue());

        // the start time
        ISLE_Time pTime = null;
        pTime = decodeConditionalTime(eeaRafO.getStartTime());

        if (pTime != null)
        {
            pStartOperation.putStartTime(pTime);
        }

        // the stop time
        pTime = null;
        pTime = decodeConditionalTime(eeaRafO.getStopTime());

        if (pTime != null)
        {
            pStartOperation.putStopTime(pTime);
        }

        // the frame quality
        pStartOperation.setRequestedFrameQuality(RAF_RequestedFrameQuality
                .getRequestedFrameQualityByCode((int) eeaRafO.getRequestedFrameQuality().value.intValue()));
    }

    /**
     * Fills the object used for the encoding of Raf Start return operation.
     * S_OK The RAF Start operation has been encoded. E_FAIL Unable to encode
     * the RAF Start operation.
     * 
     * @throws SleApiException
     */
    private void encodeStartReturnOp(IRAF_Start pStartOperation, RafStartReturnPdu eeaRafO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pStartOperation.getPerformerCredentials();
        eeaRafO.setPerformerCredentials(encodeCredentials(pCredentials));

        // the invoker id
        eeaRafO.setInvokeId(new InvokeId(pStartOperation.getInvokeId()));

        // the result
        if (pStartOperation.getResult() == SLE_Result.sleRES_positive)
        {
            Result posResult = new Result();
            posResult.setPositiveResult(new BerNull());
            eeaRafO.setResult(posResult);
        }
        else
        {
            Result negResult = new Result();

            if (pStartOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                DiagnosticRafStart repSpecific = new DiagnosticRafStart();

                switch (pStartOperation.getStartDiagnostic())
                {
                case rafSD_outOfService:
                {
                    repSpecific.setSpecific(new BerInteger(RAF_StartDiagnostic.rafSD_outOfService.getCode()));
                    break;
                }
                case rafSD_unableToComply:
                {
                    repSpecific.setSpecific(new BerInteger(RAF_StartDiagnostic.rafSD_unableToComply.getCode()));
                    break;
                }
                case rafSD_invalidStartTime:
                {
                    repSpecific.setSpecific(new BerInteger(RAF_StartDiagnostic.rafSD_invalidStartTime.getCode()));
                    break;
                }
                case rafSD_invalidStopTime:
                {
                    repSpecific.setSpecific(new BerInteger(RAF_StartDiagnostic.rafSD_invalidStopTime.getCode()));
                    break;
                }
                case rafSD_missingTimeValue:
                {
                    repSpecific.setSpecific(new BerInteger(RAF_StartDiagnostic.rafSD_missingTimeValue.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(RAF_StartDiagnostic.rafSD_invalid.getCode()));
                    break;
                }
                }

                negResult.setNegativeResult(repSpecific);
            }
            else
            {
                // common diagnostic
                DiagnosticRafStart repCommon = new DiagnosticRafStart();
                repCommon.setCommon(new Diagnostics(pStartOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }

            eeaRafO.setResult(negResult);
        }
    }

    /**
     * Fills the RAF START return operation from the object. S_OK The RAF Start
     * operation has been decoded. E_FAIL Unable to decode the RAF Start
     * operation.
     * 
     * @throws SleApiException
     */
    private IRAF_Start decodeStartReturnOp(RafStartReturnPdu eeaRafO) throws SleApiException
    {
        IRAF_Start pStartOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_start;
        ISLE_Operation pOperation = null;

        pOperation = this.pduTranslator.getReturnOp(eeaRafO.getInvokeId(), opType);
        if (pOperation != null)
        {
            pStartOperation = pOperation.queryInterface(IRAF_Start.class);
            if (pStartOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaRafO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pStartOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pStartOperation.setInvokeId((int) eeaRafO.getInvokeId().value.intValue());

                // the result
                if (eeaRafO.getResult().getPositiveResult() != null)
                {
                    pStartOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eeaRafO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        int specValue = (int) eeaRafO.getResult().getNegativeResult().getSpecific().value.intValue();
                        pStartOperation.setStartDiagnostic(RAF_StartDiagnostic.getStartDiagnosticByCode(specValue));
                    }
                    else
                    {
                        // common diagnostic
                        int commValue = (int) eeaRafO.getResult().getNegativeResult().getCommon().value.intValue();
                        pStartOperation.setDiagnostics(SLE_Diagnostics.getDiagnosticsByCode(commValue));
                    }
                }
            }
            else
            {
                throw new SleApiException(HRESULT.E_FAIL);
            }
        }

        return pStartOperation;
    }

    /**
     * Fills the object used for the encoding of Raf GetParameter invoke
     * operation. S_OK The RAF GetParameter operation has been encoded. E_FAIL
     * Unable to encode the RAF GetParameter operation.
     * 
     * @throws SleApiException
     */
    private void encodeGetParameterInvokeOp(IRAF_GetParameter pGetParameterOperation,
                                            RafGetParameterInvocationPdu eeaRafO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pGetParameterOperation.getInvokerCredentials();
        eeaRafO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaRafO.setInvokeId(new InvokeId(pGetParameterOperation.getInvokeId()));

        // the parameter
        eeaRafO.setRafParameter(new RafParameterName(pGetParameterOperation.getRequestedParameter().getCode()));
    }

    /**
     * Fills the RAF GET-PARAMETER invoke operation from the object. S_OK The
     * RAF GetParameter operation has been decoded. E_FAIL Unable to decode the
     * RAF GetParameter operation.
     * 
     * @throws SleApiException
     */
    private void decodeGetParameterInvokeOp(RafGetParameterInvocationPdu eeaRafO,
                                            IRAF_GetParameter pGetParameterOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaRafO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pGetParameterOperation.putInvokerCredentials(pCredentials);
        }

        // the invoke id
        pGetParameterOperation.setInvokeId((int) eeaRafO.getInvokeId().value.intValue());

        // the parameter
        pGetParameterOperation.setRequestedParameter(RAF_ParameterName
                .getRAFParamNameByCode((int) eeaRafO.getRafParameter().value.intValue()));
    }

    /**
     * Fills the object used for the encoding of Raf GetParameter return
     * operation. S_OK The RAF GetParameter operation has been encoded. E_FAIL
     * Unable to encode the RAF GetParameter operation.
     * 
     * @throws SleApiException
     */
    private void encodeGetParameterReturnOpV1to4(IRAF_GetParameter pGetParameterOperation, RafGetParameterReturnPdu eeaRafO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pGetParameterOperation.getPerformerCredentials();
        eeaRafO.setPerformerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaRafO.setInvokeId(new InvokeId(pGetParameterOperation.getInvokeId()));

        // the result
        if (pGetParameterOperation.getResult() == SLE_Result.sleRES_positive)
        {
            RafGetParameter rafGetParam = encodeParameterV1to4(pGetParameterOperation);
            
            ccsds.sle.transfer.service.raf.outgoing.pdus.RafGetParameterReturn.Result posR = new ccsds.sle.transfer.service.raf.outgoing.pdus.RafGetParameterReturn.Result();
            posR.setPositiveResult(rafGetParam);
            eeaRafO.setResult(posR);
        }
        else
        {
            ccsds.sle.transfer.service.raf.outgoing.pdus.RafGetParameterReturn.Result negResult = new ccsds.sle.transfer.service.raf.outgoing.pdus.RafGetParameterReturn.Result();

            if (pGetParameterOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                DiagnosticRafGet repSpecific = new DiagnosticRafGet();

                switch (pGetParameterOperation.getGetParameterDiagnostic())
                {
                case rafGP_unknownParameter:
                {
                    repSpecific.setSpecific(new BerInteger(RAF_GetParameterDiagnostic.rafGP_unknownParameter.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(RAF_GetParameterDiagnostic.rafGP_invalid.getCode()));
                    break;
                }
                }

                negResult.setNegativeResult(repSpecific);
            }
            else
            {
                // common diagnostic
                DiagnosticRafGet repCommon = new DiagnosticRafGet();
                repCommon.setCommon(new Diagnostics(pGetParameterOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }

            eeaRafO.setResult(negResult);
        }
    }

    /**
     * Fills the object used for the encoding of Raf GetParameter return
     * operation. S_OK The RAF GetParameter operation has been encoded. E_FAIL
     * Unable to encode the RAF GetParameter operation.
     * 
     * @throws SleApiException
     */
    private void encodeGetParameterReturnOp(IRAF_GetParameter pGetParameterOperation, RafGetParameterReturnPdu eeaRafO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pGetParameterOperation.getPerformerCredentials();
        eeaRafO.setPerformerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaRafO.setInvokeId(new InvokeId(pGetParameterOperation.getInvokeId()));

        // the result
        if (pGetParameterOperation.getResult() == SLE_Result.sleRES_positive)
        {
            RafGetParameter rafGetParam = encodeParameter(pGetParameterOperation);
            
            ccsds.sle.transfer.service.raf.outgoing.pdus.RafGetParameterReturn.Result posR = new ccsds.sle.transfer.service.raf.outgoing.pdus.RafGetParameterReturn.Result();
            posR.setPositiveResult(rafGetParam);
            eeaRafO.setResult(posR);
        }
        else
        {
            ccsds.sle.transfer.service.raf.outgoing.pdus.RafGetParameterReturn.Result negResult = new ccsds.sle.transfer.service.raf.outgoing.pdus.RafGetParameterReturn.Result();

            if (pGetParameterOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                DiagnosticRafGet repSpecific = new DiagnosticRafGet();

                switch (pGetParameterOperation.getGetParameterDiagnostic())
                {
                case rafGP_unknownParameter:
                {
                    repSpecific.setSpecific(new BerInteger(RAF_GetParameterDiagnostic.rafGP_unknownParameter.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(RAF_GetParameterDiagnostic.rafGP_invalid.getCode()));
                    break;
                }
                }

                negResult.setNegativeResult(repSpecific);
            }
            else
            {
                // common diagnostic
                DiagnosticRafGet repCommon = new DiagnosticRafGet();
                repCommon.setCommon(new Diagnostics(pGetParameterOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }

            eeaRafO.setResult(negResult);
        }
    }
    /**
     * Fills the RAF GET-PARAMETER return operation from the object. S_OK The
     * RAF GetParameter operation has been decoded. E_FAIL Unable to decode the
     * RAF GetParameter operation.
     * 
     * @throws SleApiException
     */
    private IRAF_GetParameter decodeGetParameterReturnOp(RafGetParameterReturnPdu eeaRafO) throws SleApiException
    {
        IRAF_GetParameter pGetParameterOperation = null;
        ISLE_Operation pOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_getParameter;

        pOperation = this.pduTranslator.getReturnOp(eeaRafO.getInvokeId(), opType);
        if (pOperation != null)
        {
            pGetParameterOperation = pOperation.queryInterface(IRAF_GetParameter.class);
            if (pGetParameterOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaRafO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pGetParameterOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pGetParameterOperation.setInvokeId((int) eeaRafO.getInvokeId().value.intValue());

                // the result
                if (eeaRafO.getResult().getPositiveResult() != null)
                {
                    decodeParameter(eeaRafO.getResult().getPositiveResult(), pGetParameterOperation);
                    pGetParameterOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eeaRafO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        int specValue = (int) eeaRafO.getResult().getNegativeResult().getSpecific().value.intValue();
                        pGetParameterOperation.setGetParameterDiagnostic(RAF_GetParameterDiagnostic
                                .getGetParamDiagByCode(specValue));
                    }
                    else
                    {
                        // common diagnostic
                        int commValue = (int) eeaRafO.getResult().getNegativeResult().getCommon().value.intValue();
                        pGetParameterOperation.setDiagnostics(SLE_Diagnostics.getDiagnosticsByCode(commValue));
                    }
                }
            }
            else
            {
                throw new SleApiException(HRESULT.E_FAIL);
            }
        }

        return pGetParameterOperation;
    }

    /**
     * Fills the object used for the encoding of Raf StatusReport operation.
     * S_OK The RAF StatusReport operation has been encoded. E_FAIL Unable to
     * encode the RAF StatusReport operation.
     * 
     * @throws SleApiException
     */
    private void encodeStatusReportOp(IRAF_StatusReport pStatusReportOperation, RafStatusReportInvocationPdu eeaRafO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pStatusReportOperation.getInvokerCredentials();
        eeaRafO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the error free frame number
        eeaRafO.setErrorFreeFrameNumber(new IntUnsignedLong(pStatusReportOperation.getNumErrorFreeFrames()));
        // the delivered frame number
        eeaRafO.setDeliveredFrameNumber(new IntUnsignedLong(pStatusReportOperation.getNumFrames()));
        // the frame sync lock status
        eeaRafO.setFrameSyncLockStatus(new FrameSyncLockStatus(pStatusReportOperation.getFrameSyncLock().getCode()));
        // the symbol sync lock status
        eeaRafO.setSymbolSyncLockStatus(new SymbolLockStatus(pStatusReportOperation.getSymbolSyncLock().getCode()));
        // the sub carrier lock status
        eeaRafO.setSubcarrierLockStatus(new LockStatus(pStatusReportOperation.getSubCarrierDemodLock().getCode()));
        // the carrier lock status
        eeaRafO.setCarrierLockStatus(new CarrierLockStatus(pStatusReportOperation.getCarrierDemodLock().getCode()));
        // the production status
        eeaRafO.setProductionStatus(new RafProductionStatus(pStatusReportOperation.getProductionStatus().getCode()));
    }

    /**
     * Fills the object used for the encoding of Raf StatusReport V1 operation.
     * S_OK The RAF StatusReport operation has been encoded. E_FAIL Unable to
     * version encode the RAF StatusReport operation.
     * 
     * @throws SleApiException
     */
    private void encodeStatusReportOp(IRAF_StatusReport pStatusReportOperation, RafStatusReportInvocationV1Pdu eeaRafO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pStatusReportOperation.getInvokerCredentials();
        eeaRafO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the error free frame number
        eeaRafO.setErrorFreeFrameNumber(new IntUnsignedLong(pStatusReportOperation.getNumErrorFreeFrames()));
        // the delivered frame number
        eeaRafO.setDeliveredFrameNumber(new IntUnsignedLong(pStatusReportOperation.getNumFrames()));
        // the frame sync lock status
        eeaRafO.setFrameSyncLockStatus(new FrameSyncLockStatus(pStatusReportOperation.getFrameSyncLock().getCode()));
        // the symbol sync lock status
        eeaRafO.setSymbolSyncLockStatus(new SymbolLockStatus(pStatusReportOperation.getSymbolSyncLock().getCode()));
        // the sub carrier lock status
        eeaRafO.setSubcarrierLockStatus(new LockStatus(pStatusReportOperation.getSubCarrierDemodLock().getCode()));
        // the carrier lock status
        eeaRafO.setCarrierLockStatus(new CarrierLockStatus(pStatusReportOperation.getCarrierDemodLock().getCode()));
        // the production status
        eeaRafO.setProductionStatus(new RafProductionStatus(pStatusReportOperation.getProductionStatus().getCode()));
    }

    /**
     * Fills the RAF STATUS-REPORT operation from the object. S_OK The RAF
     * StatusReport operation has been decoded. E_FAIL Unable to decode the RAF
     * StatusReport operation.
     * 
     * @throws SleApiException
     */
    private void decodeStatusReportOp(RafStatusReportInvocationPdu eeaRafO, IRAF_StatusReport pStatusReportOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaRafO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pStatusReportOperation.putInvokerCredentials(pCredentials);
        }

        // the error free frame number
        pStatusReportOperation.setNumErrorFreeFrames(eeaRafO.getErrorFreeFrameNumber().value.longValue());
        // the delivered frame number
        pStatusReportOperation.setNumFrames(eeaRafO.getDeliveredFrameNumber().value.longValue());
        // the frame sync lock status
        pStatusReportOperation.setFrameSyncLock(RAF_LockStatus
                .getLockStatusByCode((int) eeaRafO.getFrameSyncLockStatus().value.intValue()));
        // the symbol sync lock status
        pStatusReportOperation.setSymbolSyncLock(RAF_LockStatus
                .getLockStatusByCode((int) eeaRafO.getSymbolSyncLockStatus().value.intValue()));
        // the sub carrier lock status
        pStatusReportOperation.setSubCarrierDemodLock(RAF_LockStatus
                .getLockStatusByCode((int) eeaRafO.getSubcarrierLockStatus().value.intValue()));
        // the carrier lock status
        pStatusReportOperation.setCarrierDemodLock(RAF_LockStatus
                .getLockStatusByCode((int) eeaRafO.getCarrierLockStatus().value.intValue()));
        // the production status
        pStatusReportOperation.setProductionStatus(RAF_ProductionStatus
                .getProductionStatusByCode((int) eeaRafO.getProductionStatus().value.intValue()));
    }

    /**
     * Fills the RAF STATUS-REPORT V1 operation from the object. S_OK The RAF
     * StatusReport operation has been decoded. E_FAIL Unable to decode the RAF
     * StatusReport operation.
     * 
     * @throws SleApiException
     */
    private void decodeStatusReportOp(RafStatusReportInvocationV1Pdu eeaRafO, IRAF_StatusReport pStatusReportOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaRafO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pStatusReportOperation.putInvokerCredentials(pCredentials);
        }

        // the error free frame number
        pStatusReportOperation.setNumErrorFreeFrames(eeaRafO.getErrorFreeFrameNumber().value.longValue());
        // the delivered frame number
        pStatusReportOperation.setNumFrames(eeaRafO.getDeliveredFrameNumber().value.longValue());
        // the frame sync lock status
        pStatusReportOperation.setFrameSyncLock(RAF_LockStatus
                .getLockStatusByCode((int) eeaRafO.getFrameSyncLockStatus().value.intValue()));
        // the symbol sync lock status
        pStatusReportOperation.setSymbolSyncLock(RAF_LockStatus
                .getLockStatusByCode((int) eeaRafO.getSymbolSyncLockStatus().value.intValue()));
        // the sub carrier lock status
        pStatusReportOperation.setSubCarrierDemodLock(RAF_LockStatus
                .getLockStatusByCode((int) eeaRafO.getSubcarrierLockStatus().value.intValue()));
        // the carrier lock status
        pStatusReportOperation.setCarrierDemodLock(RAF_LockStatus
                .getLockStatusByCode((int) eeaRafO.getCarrierLockStatus().value.intValue()));
        // the production status
        pStatusReportOperation.setProductionStatus(RAF_ProductionStatus
                .getProductionStatusByCode((int) eeaRafO.getProductionStatus().value.intValue()));
    }

    /**
     * Fills the object used for the encoding of Raf TransferData operation.
     * S_OK The RAF TransferData operation has been encoded. E_FAIL Unable to
     * encode the RAF TransferData operation.
     * 
     * @throws SleApiException
     */
    private void encodeTransferDataOp(IRAF_TransferData pTransferDataOperation, FrameOrNotification eeaRafO) throws SleApiException
    {
        RafTransferDataInvocation annotatedFrame = new RafTransferDataInvocation();

        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pTransferDataOperation.getInvokerCredentials();
        annotatedFrame.setInvokerCredentials(encodeCredentials(pCredentials));

        // the earth receive time
        ISLE_Time pTime = null;
        pTime = pTransferDataOperation.getEarthReceiveTime();
        annotatedFrame.setEarthReceiveTime(encodeEarthReceiveTime(pTime));

        // the antenna id
        if (pTransferDataOperation.getAntennaIdFormat() == RAF_AntennaIdFormat.rafAF_global)
        {
            // global form
        	BerObjectIdentifier objectId = new BerObjectIdentifier(pTransferDataOperation.getAntennaIdGF());
        	AntennaId id = new AntennaId();
        	id.setGlobalForm(objectId);
            annotatedFrame.setAntennaId(id);
        }
        else
        {
            // local form
            byte[] poctet = pTransferDataOperation.getAntennaIdLF();
            if (poctet.length <= C_MaxLengthAntennaLocalForm)
            {
            	AntennaId id = new AntennaId();
            	id.setLocalForm(new BerOctetString(poctet));
                annotatedFrame.setAntennaId(id);
            }
        }

        // the data link continuity
        annotatedFrame.setDataLinkContinuity(new BerInteger(pTransferDataOperation.getDataLinkContinuity()));

        // the delivered frame quality
        if (pTransferDataOperation.getFrameQuality() == RAF_FrameQuality.rafFQ_good)
        {
            annotatedFrame.setDeliveredFrameQuality(new FrameQuality(RAF_FrameQuality.rafFQ_good.getCode()));
        }
        else if (pTransferDataOperation.getFrameQuality() == RAF_FrameQuality.rafFQ_undetermined)
        {
            annotatedFrame.setDeliveredFrameQuality(new FrameQuality(RAF_FrameQuality.rafFQ_undetermined.getCode()));
        }
        else if (pTransferDataOperation.getFrameQuality() == RAF_FrameQuality.rafFQ_erred)
        {
            annotatedFrame.setDeliveredFrameQuality(new FrameQuality(RAF_FrameQuality.rafFQ_erred.getCode()));
        }
        else
        {
            annotatedFrame.setDeliveredFrameQuality(new FrameQuality(RAF_FrameQuality.rafFQ_invalid.getCode()));
        }

        // the private annotation
        byte[] pa = pTransferDataOperation.getPrivateAnnotation();
        if (pa == null)
        {
        	RafTransferDataInvocation.PrivateAnnotation paPA = new RafTransferDataInvocation.PrivateAnnotation();
        	paPA.setNull(new BerNull());
            annotatedFrame.setPrivateAnnotation(paPA);
        }
        else
        {
            if (pa.length <= C_MaxLengthPrivateAnnotation)
            {
            	RafTransferDataInvocation.PrivateAnnotation paPA = new RafTransferDataInvocation.PrivateAnnotation();
            	paPA.setNotNull(new BerOctetString(pa));
                annotatedFrame.setPrivateAnnotation(paPA);
            }
        }

        // the space link data unit
        byte[] pdata = pTransferDataOperation.getData();
        SpaceLinkDataUnit data = new SpaceLinkDataUnit(pdata);
        annotatedFrame.setData(data);

        eeaRafO.setAnnotatedFrame(annotatedFrame);
    }

    /**
     * Fills the RAF TRANSFER-DATA operation from the object. S_OK The RAF
     * TransferData operation has been decoded. E_FAIL Unable to decode the RAF
     * TransferData operation.
     * 
     * @throws SleApiException
     */
    private void decodeTransferDataOp(RafTransferDataInvocation eeaRafO, IRAF_TransferData pTransferDataOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaRafO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pTransferDataOperation.putInvokerCredentials(pCredentials);
        }

        // the earth receive time
        ISLE_Time pTime = null;
        pTime = decodeEarthReceiveTime(eeaRafO.getEarthReceiveTime());
        if (pTime != null)
        {
            pTransferDataOperation.putEarthReceiveTime(pTime);
        }

        // the antenna id
        AntennaId eeaAntenna = eeaRafO.getAntennaId();
        if (eeaAntenna.getGlobalForm() != null)
        {
            // global form
            int[] objectId = eeaAntenna.getGlobalForm().value;
            if (objectId != null)
            {
                pTransferDataOperation.setAntennaIdGF(objectId);
            }
        }
        else
        {
            // local form
            if (eeaAntenna.getLocalForm().value.length <= C_MaxLengthAntennaLocalForm)
            {
                pTransferDataOperation.setAntennaIdLF(eeaAntenna.getLocalForm().value);
            }
        }

        // the data link continuity
        pTransferDataOperation.setDataLinkContinuity((int) eeaRafO.getDataLinkContinuity().value.intValue());

        // the delivered frame quality
        pTransferDataOperation.setFrameQuality(RAF_FrameQuality
                .getFrameQualityByCode((int) eeaRafO.getDeliveredFrameQuality().value.intValue()));

        // the private annotation
        if (eeaRafO.getPrivateAnnotation().getNull() != null)
        {
            pTransferDataOperation.putPrivateAnnotation(null);
        }
        else
        {
            if (eeaRafO.getPrivateAnnotation().getNotNull().value.length <= C_MaxLengthPrivateAnnotation)
            {
                pTransferDataOperation.putPrivateAnnotation(eeaRafO.getPrivateAnnotation().getNotNull().value);
            }
        }

        // the space link data unit
        byte[] pdata = null;
        pdata = eeaRafO.getData().value;
        pTransferDataOperation.putData(pdata);
    }

    /**
     * Fills the object used for the encoding of Raf SyncNotify operation. S_OK
     * The RAF SyncNotify operation has been encoded. E_FAIL Unable to encode
     * the RAF SyncNotify operation.
     * 
     * @throws SleApiException
     */
    private void encodeSyncNotifyOp(IRAF_SyncNotify pSyncNotifyOperation, FrameOrNotification eeaRafO) throws SleApiException
    {
        RafSyncNotifyInvocation syncNotify = new RafSyncNotifyInvocation();

        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pSyncNotifyOperation.getInvokerCredentials();
        syncNotify.setInvokerCredentials(encodeCredentials(pCredentials));

        // the raf notification
        syncNotify.setNotification(new Notification());

        switch (pSyncNotifyOperation.getNotificationType())
        {
        case rafNT_lossFrameSync:
        {
            syncNotify.getNotification().setLossFrameSync(new LockStatusReport());

            // the time
            ISLE_Time pTime = null;
            pTime = pSyncNotifyOperation.getLossOfLockTime();
            syncNotify.getNotification().getLossFrameSync().setTime(encodeTime(pTime));

            // the carrier lock status
            syncNotify.getNotification().getLossFrameSync().setCarrierLockStatus(new CarrierLockStatus(pSyncNotifyOperation
                    .getCarrierDemodLock().getCode()));
            // the sub carrier lock status
            syncNotify.getNotification().getLossFrameSync().setSubcarrierLockStatus(new LockStatus(pSyncNotifyOperation
                    .getSubCarrierDemodLock().getCode()));
            // the symbol sync lock status
            syncNotify.getNotification().getLossFrameSync().setSymbolSyncLockStatus(new SymbolLockStatus(pSyncNotifyOperation
                    .getSymbolSyncLock().getCode()));

            break;
        }
        case rafNT_productionStatusChange:
        {
            syncNotify.getNotification().setProductionStatusChange(new RafProductionStatus(pSyncNotifyOperation
                    .getProductionStatus().getCode()));
            break;
        }
        case rafNT_excessiveDataBacklog:
        {
            syncNotify.getNotification().setExcessiveDataBacklog(new BerNull());
            break;
        }
        case rafNT_endOfData:
        {
            syncNotify.getNotification().setEndOfData(new BerNull());
            break;
        }
        default:
        {
            break;
        }
        }

        eeaRafO.setSyncNotification(syncNotify);
    }

    /**
     * Fills the RAF SYNC-NOTIFY operation from the object. S_OK The RAF
     * SyncNotify operation has been decoded. E_FAIL Unable to decode the RAF
     * SyncNotify operation.
     * 
     * @throws SleApiException
     */
    private void decodeSyncNotifyOp(RafSyncNotifyInvocation eeaRafO, IRAF_SyncNotify pSyncNotifyOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaRafO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pSyncNotifyOperation.setInvokerCredentials(pCredentials);
        }

        // the raf notification
        if (eeaRafO.getNotification().getLossFrameSync() != null)
        {
            // the time
            ISLE_Time pTime = null;
            pTime = decodeTime(eeaRafO.getNotification().getLossFrameSync().getTime());

            // the carrier lock status
            RAF_LockStatus carrierDemodLock = RAF_LockStatus
                    .getLockStatusByCode((int) eeaRafO.getNotification().getLossFrameSync().getCarrierLockStatus().value.intValue());
            // the sub carrier lock status
            RAF_LockStatus subCarrierDemodLock = RAF_LockStatus
                    .getLockStatusByCode((int) eeaRafO.getNotification().getLossFrameSync().getSubcarrierLockStatus().value.intValue());
            // the symbol sync lock status
            RAF_LockStatus symbolSyncLock = RAF_LockStatus
                    .getLockStatusByCode((int) eeaRafO.getNotification().getLossFrameSync().getSymbolSyncLockStatus().value.intValue());

            pSyncNotifyOperation.setLossOfFrameSync(pTime, symbolSyncLock, subCarrierDemodLock, carrierDemodLock);
        }
        else if (eeaRafO.getNotification().getProductionStatusChange() != null)
        {
            RAF_ProductionStatus productionStatus = RAF_ProductionStatus
                    .getProductionStatusByCode((int) eeaRafO.getNotification().getProductionStatusChange().value.intValue());
            pSyncNotifyOperation.setProductionStatusChange(productionStatus);
        }
        else if (eeaRafO.getNotification().getExcessiveDataBacklog() != null)
        {
            pSyncNotifyOperation.setDataDiscarded();
        }
        else if (eeaRafO.getNotification().getEndOfData() != null)
        {
            pSyncNotifyOperation.setEndOfData();
        }
    }

    /**
     * Fills the object used for the encoding of Raf TransferBuffer operation.
     * S_OK The RAF TransferBuffer operation has been encoded. E_FAIL Unable to
     * encode the RAF TransferBuffer operation.
     * 
     * @throws SleApiException
     */
    private void encodeTransferBufferOp(ISLE_TransferBuffer pTransferBufferOperation, RafTransferBufferPdu eeaRafO) throws SleApiException
    {
        ISLE_Operation pCurrentOp = null;

        if (pTransferBufferOperation.getSize() == 0)
        {
            return;
        }

        pTransferBufferOperation.reset();
        // for all the operations of the transfer buffer operation

        while (pTransferBufferOperation.moreData())
        {

            pCurrentOp = pTransferBufferOperation.next();
            SLE_OpType opType = pCurrentOp.getOperationType();

            FrameOrNotification currentElement = new FrameOrNotification();

            if (opType == SLE_OpType.sleOT_transferData)
            {
                IRAF_TransferData pOp = null;
                pOp = pCurrentOp.queryInterface(IRAF_TransferData.class);
                if (pOp != null)
                {
                    encodeTransferDataOp(pOp, currentElement);
                    // add the current element to the list
                    eeaRafO.getFrameOrNotification().add(currentElement);
                }
                else
                {
                    // cannot get the interface
                    throw new SleApiException(HRESULT.E_FAIL, "No interface");
                }
            }
            else if (opType == SLE_OpType.sleOT_syncNotify)
            {
                IRAF_SyncNotify pOp = null;
                pOp = pCurrentOp.queryInterface(IRAF_SyncNotify.class);
                if (pOp != null)
                {
                    encodeSyncNotifyOp(pOp, currentElement);
                    // add the current element to the list
                    eeaRafO.getFrameOrNotification().add(currentElement);
                }
                else
                {
                    // cannot get the interface
                    throw new SleApiException(HRESULT.E_FAIL, "No interface");
                }
            }
            else
            {
                // bad operation type
                throw new SleApiException(HRESULT.E_FAIL, "Operation type not supported");
            }
        }// while
    }

    /**
     * Fills the TRANSFER-BUFFER operation from the object. S_OK A new RAF
     * TransferBuffer operation has been instantiated. E_FAIL Unable to
     * Instantiate a RAF TransferBuffer operation.
     * 
     * @throws SleApiException
     */
    private void decodeTransferBufferOp(RafTransferBufferPdu eeaRafO, ISLE_TransferBuffer pTransferBufferOperation) throws SleApiException
    {
        ISLE_Operation pOp = null;
        Iterator<FrameOrNotification> it = eeaRafO.getFrameOrNotification().iterator();

        while (it.hasNext())
        {
            FrameOrNotification currentElement = it.next();
            if (currentElement.getAnnotatedFrame() != null)
            {
                // instantiate a new transfer data operation
                IRAF_TransferData pTransferData = null;
                pTransferData = this.operationFactory.createOperation(IRAF_TransferData.class,
                                                                      SLE_OpType.sleOT_transferData,
                                                                      this.serviceType,
                                                                      this.sleVersionNumber);
                if (pTransferData != null)
                {
                    decodeTransferDataOp(currentElement.getAnnotatedFrame(), pTransferData);
                    pOp = pTransferData.queryInterface(ISLE_Operation.class);
                }
            }
            else if (currentElement.getSyncNotification() != null)
            {
                // instantiate a new sync notify operation
                IRAF_SyncNotify pSyncNotify = null;
                pSyncNotify = this.operationFactory.createOperation(IRAF_SyncNotify.class,
                                                                    SLE_OpType.sleOT_syncNotify,
                                                                    this.serviceType,
                                                                    this.sleVersionNumber);
                if (pSyncNotify != null)
                {
                    decodeSyncNotifyOp(currentElement.getSyncNotification(), pSyncNotify);
                    pOp = pSyncNotify.queryInterface(ISLE_Operation.class);
                }
            }
            else
            {
                throw new SleApiException(HRESULT.E_FAIL);
            }

            // add the operation to the transfer buffer operation
            pTransferBufferOperation.append(pOp);
        }
    }

    /**
     * Fills the RAF Parameter of the Asn1 object.
     * Called for encoding parameter to return GetParameterOperation
     */
    private RafGetParameter encodeParameter(IRAF_GetParameter pGetParameterOperation)
    {
        RafGetParameter eeaO = new RafGetParameter();

        switch (pGetParameterOperation.getReturnedParameter())
        {
        case rafPN_bufferSize:
        {
            ParBufferSize bufferSize = new ParBufferSize();
            bufferSize.setParameterName(new ParameterName(RAF_ParameterName.rafPN_bufferSize.getCode()));
            bufferSize.setParameterValue(new IntPosShort(pGetParameterOperation.getTransferBufferSize()));
            eeaO.setParBufferSize(bufferSize);
            break;
        }
        case rafPN_deliveryMode:
        {
            ParDeliveryMode parDeliveryMode = new ParDeliveryMode();
            parDeliveryMode.setParameterName(new ParameterName(RAF_ParameterName.rafPN_deliveryMode.getCode()));
            parDeliveryMode.setParameterValue(new RafDeliveryMode(pGetParameterOperation.getDeliveryMode().getCode()));
            eeaO.setParDeliveryMode(parDeliveryMode);
            break;
        }
        case rafPN_latencyLimit:
        {
            int latencyLimit = pGetParameterOperation.getLatencyLimit();
            ParLatencyLimit parLatencyLimit = new ParLatencyLimit();
            parLatencyLimit.setParameterName(new ParameterName(RAF_ParameterName.rafPN_latencyLimit.getCode()));
            if (latencyLimit == 0)
            {
            	ParameterValue pv = new ParameterValue();
            	pv.setOffline(new BerNull());
                parLatencyLimit.setParameterValue(pv);
            }
            else
            {
            	ParameterValue pv = new ParameterValue();
            	pv.setOnline(new IntPosShort(latencyLimit));
                parLatencyLimit.setParameterValue(pv);
            }
            eeaO.setParLatencyLimit(parLatencyLimit);
            break;
        }
        case rafPN_reportingCycle:
        {
            long reportingCycle = pGetParameterOperation.getReportingCycle();
            ParReportingCycle parReportingCycle = new ParReportingCycle();
            parReportingCycle.setParameterName(new ParameterName(RAF_ParameterName.rafPN_reportingCycle.getCode()));
            if (reportingCycle == 0)
            {
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOff(new BerNull());
                parReportingCycle.setParameterValue(crc);
            }
            else
            {
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOn( new ReportingCycle(reportingCycle));
                parReportingCycle.setParameterValue(crc);
            }
            eeaO.setParReportingCycle(parReportingCycle);
            break;
        }
        case rafPN_requestFrameQuality:
        {
            ParReqFrameQuality parReqFrameQuality = new ParReqFrameQuality();
            parReqFrameQuality.setParameterName(new ParameterName(RAF_ParameterName.rafPN_requestFrameQuality.getCode()));
            parReqFrameQuality.setParameterValue(new BerInteger(pGetParameterOperation.getRequestedFrameQuality().getCode()));
            eeaO.setParReqFrameQuality(parReqFrameQuality);
            break;
        }
        case rafPN_returnTimeoutPeriod:
        {
            ParReturnTimeout parRetTimeout = new ParReturnTimeout();
            parRetTimeout.setParameterName(new ParameterName(RAF_ParameterName.rafPN_returnTimeoutPeriod.getCode()));
            parRetTimeout.setParameterValue(new TimeoutPeriod(pGetParameterOperation.getReturnTimeoutPeriod()));
            eeaO.setParReturnTimeout(parRetTimeout);
            break;
        }
        // New with SLES V5
        case rafPN_permittedFrameQuality:
        {
        	// SLE parameter id 302 - RAF parameter id 6
            ParPermittedFrameQuality parPermittedFrameQuality = new ParPermittedFrameQuality();
            parPermittedFrameQuality.setParameterName(new ParameterName(RAF_ParameterName.rafPN_permittedFrameQuality.getCode()));
            PermittedFrameQualitySet permFrameQualitySet = new PermittedFrameQualitySet();
            java.util.List<RAF_RequestedFrameQuality> permFrameQuality = pGetParameterOperation.getPermittedFrameQuality();
            if(permFrameQuality != null)
            {
            	for(RAF_RequestedFrameQuality qual : permFrameQuality)
            	{
            		permFrameQualitySet.getRequestedFrameQuality().add(new RequestedFrameQuality(qual.getCode()));
            	}
            }
            parPermittedFrameQuality.setParameterValue(permFrameQualitySet);            
            eeaO.setParPermittedFrameQuality(parPermittedFrameQuality);
            break;
        }
        case rafPN_minReportingCycle:
        {
        	// SLE parameter id 301 - RAF parameter id 7
            ParMinReportingCycle parMinRepCycle = new ParMinReportingCycle();
            parMinRepCycle.setParameterName(new ParameterName(RAF_ParameterName.rafPN_minReportingCycle.getCode()));
            parMinRepCycle.setParameterValue(new IntPosShort(pGetParameterOperation.getMinimumReportingCycle()));
            eeaO.setParMinReportingCycle(parMinRepCycle);
            break;
        }

        default:
        {
            break;
        }
        }

        return eeaO;
    }

    /**
     * Fills the RAF Parameter of the Asn1 object.
     */
    private RafGetParameter encodeParameterV1to4(IRAF_GetParameter pGetParameterOperation)
    {
        RafGetParameter eeaO = new RafGetParameter();

        switch (pGetParameterOperation.getReturnedParameter())
        {
        case rafPN_bufferSize:
        {
            ParBufferSize bufferSize = new ParBufferSize();
            bufferSize.setParameterName(new ParameterName(RAF_ParameterName.rafPN_bufferSize.getCode()));
            bufferSize.setParameterValue(new IntPosShort(pGetParameterOperation.getTransferBufferSize()));
            eeaO.setParBufferSize(bufferSize);
            break;
        }
        case rafPN_deliveryMode:
        {
            ParDeliveryMode parDeliveryMode = new ParDeliveryMode();
            parDeliveryMode.setParameterName(new ParameterName(RAF_ParameterName.rafPN_deliveryMode.getCode()));
            parDeliveryMode.setParameterValue(new RafDeliveryMode(pGetParameterOperation.getDeliveryMode().getCode()));
            eeaO.setParDeliveryMode(parDeliveryMode);
            break;
        }
        case rafPN_latencyLimit:
        {
            int latencyLimit = pGetParameterOperation.getLatencyLimit();
            ParLatencyLimit parLatencyLimit = new ParLatencyLimit();
            parLatencyLimit.setParameterName(new ParameterName(RAF_ParameterName.rafPN_latencyLimit.getCode()));
            if (latencyLimit == 0)
            {
            	ParameterValue pv = new ParameterValue();
            	pv.setOffline(new BerNull());
                parLatencyLimit.setParameterValue(pv);
            }
            else
            {
            	ParameterValue pv = new ParameterValue();
            	pv.setOnline(new IntPosShort(latencyLimit));
                parLatencyLimit.setParameterValue(pv);
            }
            eeaO.setParLatencyLimit(parLatencyLimit);
            break;
        }
        case rafPN_reportingCycle:
        {
            long reportingCycle = pGetParameterOperation.getReportingCycle();
            ParReportingCycle parReportingCycle = new ParReportingCycle();
            parReportingCycle.setParameterName(new ParameterName(RAF_ParameterName.rafPN_reportingCycle.getCode()));
            if (reportingCycle == 0)
            {
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOff(new BerNull());
                parReportingCycle.setParameterValue(crc);
            }
            else
            {
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOn( new ReportingCycle(reportingCycle));
                parReportingCycle.setParameterValue(crc);
            }
            eeaO.setParReportingCycle(parReportingCycle);
            break;
        }
        case rafPN_requestFrameQuality:
        {
            ParReqFrameQuality parReqFrameQuality = new ParReqFrameQuality();
            parReqFrameQuality.setParameterName(new ParameterName(RAF_ParameterName.rafPN_requestFrameQuality.getCode()));
            parReqFrameQuality.setParameterValue(new BerInteger(pGetParameterOperation.getRequestedFrameQuality().getCode()));
            eeaO.setParReqFrameQuality(parReqFrameQuality);
            break;
        }
        case rafPN_returnTimeoutPeriod:
        {
            ParReturnTimeout parRetTimeout = new ParReturnTimeout();
            parRetTimeout.setParameterName(new ParameterName(RAF_ParameterName.rafPN_returnTimeoutPeriod.getCode()));
            parRetTimeout.setParameterValue(new TimeoutPeriod(pGetParameterOperation.getReturnTimeoutPeriod()));
            eeaO.setParReturnTimeout(parRetTimeout);
            break;
        }
        default:
        {
            break;
        }
        }

        return eeaO;
    }
    
    /**
     * Fills the parameter of the GetParameter Return operation from the Asn1
     * object.
     */
    private void decodeParameter(RafGetParameter eeaO, IRAF_GetParameter pGetParameterOperation)
    {
        if (eeaO.getParBufferSize() != null)
        {
            pGetParameterOperation.setTransferBufferSize(eeaO.getParBufferSize().getParameterValue().value.longValue());
        }
        else if (eeaO.getParDeliveryMode() != null)
        {
            pGetParameterOperation.setDeliveryMode(RAF_DeliveryMode
                    .getRAFDelModeByCode((int) eeaO.getParDeliveryMode().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParLatencyLimit() != null)
        {
            if (eeaO.getParLatencyLimit().getParameterValue().getOnline() != null)
            {
                pGetParameterOperation.setLatencyLimit((int) eeaO.getParLatencyLimit().getParameterValue().getOnline().value.intValue());
            }
            else
            {
                pGetParameterOperation.setLatencyLimit(0);
            }
        }
        else if (eeaO.getParReportingCycle() != null)
        {
            if (eeaO.getParReportingCycle().getParameterValue().getPeriodicReportingOn() != null)
            {
                pGetParameterOperation
                        .setReportingCycle(eeaO.getParReportingCycle().getParameterValue().getPeriodicReportingOn().value.longValue());
            }
            else
            {
                pGetParameterOperation.setReportingCycle(0);
            }
        }
        else if (eeaO.getParReqFrameQuality() != null)
        {
            pGetParameterOperation.setRequestedFrameQuality(RAF_ParFrameQuality
                    .getRAFParFrameQualByCode((int) eeaO.getParReqFrameQuality().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParReturnTimeout() != null)
        {
            pGetParameterOperation.setReturnTimeoutPeriod(eeaO.getParReturnTimeout().getParameterValue().value.longValue());
        }
        else if (eeaO.getParMinReportingCycle() != null)
        {
        	pGetParameterOperation.setMinimumReportingCycle(eeaO.getParMinReportingCycle().getParameterValue().value.longValue());
        }
        else if (eeaO.getParPermittedFrameQuality() != null)
        {
        	if(eeaO.getParPermittedFrameQuality().getParameterValue().getRequestedFrameQuality() != null)
        	{
        		List<RequestedFrameQuality> list = eeaO.getParPermittedFrameQuality().getParameterValue().getRequestedFrameQuality();
        		RAF_ParFrameQuality[] permFrameQuality = new RAF_ParFrameQuality[list.size()];
        		int i = 0;
        		for(RequestedFrameQuality qual : list)
        		{
        			if(qual.value.intValue() == RAF_ParFrameQuality.rafPQ_goodFramesOnly.getCode())
        			{
        				permFrameQuality[i] = RAF_ParFrameQuality.rafPQ_goodFramesOnly;
        			}
        			else if(qual.value.intValue() == RAF_ParFrameQuality.rafPQ_erredFramesOnly.getCode())
        			{
        				permFrameQuality[i] = RAF_ParFrameQuality.rafPQ_erredFramesOnly;
        			}
        			else if(qual.value.intValue() == RAF_ParFrameQuality.rafPQ_allFrames.getCode())
        			{
        				permFrameQuality[i] = RAF_ParFrameQuality.rafPQ_allFrames;
        			}
        			else
        			{
        				permFrameQuality[i] = RAF_ParFrameQuality.rafPQ_invalid;
        			}
        			i++;
        		}
        		pGetParameterOperation.setPermittedFrameQuality(permFrameQuality);
        	}
        	else
        	{
        		RAF_ParFrameQuality[] permFrameQuality = {RAF_ParFrameQuality.rafPQ_invalid};
        		pGetParameterOperation.setPermittedFrameQuality(permFrameQuality);
        	}	
        }
    }
}
