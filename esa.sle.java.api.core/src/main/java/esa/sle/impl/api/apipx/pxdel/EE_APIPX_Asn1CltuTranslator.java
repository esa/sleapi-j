/**
 * @(#) EE_APIPX_Asn1CltuTranslator.java
 */

package esa.sle.impl.api.apipx.pxdel;

import isp1.cltu.pdus.CltuAsyncNotifyInvocationPdu;
import isp1.cltu.pdus.CltuGetParameterInvocationPdu;
import isp1.cltu.pdus.CltuGetParameterReturnPdu;
import isp1.cltu.pdus.CltuGetParameterReturnPduV1to3;
import isp1.cltu.pdus.CltuGetParameterReturnPduV4;
import isp1.cltu.pdus.CltuScheduleStatusReportInvocationPdu;
import isp1.cltu.pdus.CltuScheduleStatusReportReturnPdu;
import isp1.cltu.pdus.CltuStartInvocationPdu;
import isp1.cltu.pdus.CltuStartInvocationV1Pdu;
import isp1.cltu.pdus.CltuStartReturnPdu;
import isp1.cltu.pdus.CltuStatusReportInvocationPdu;
import isp1.cltu.pdus.CltuStopInvocationPdu;
import isp1.cltu.pdus.CltuStopReturnPdu;
import isp1.cltu.pdus.CltuThrowEventInvocationPdu;
import isp1.cltu.pdus.CltuThrowEventReturnPdu;
import isp1.cltu.pdus.CltuTransferDataInvocationPdu;
import isp1.cltu.pdus.CltuTransferDataReturnPdu;
import isp1.sle.bind.pdus.SleBindInvocationPdu;
import isp1.sle.bind.pdus.SleBindReturnPdu;
import isp1.sle.bind.pdus.SleUnbindInvocationPdu;
import isp1.sle.bind.pdus.SleUnbindReturnPdu;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.beanit.jasn1.ber.ReverseByteArrayOutputStream ;
import com.beanit.jasn1.ber.BerTag;
import com.beanit.jasn1.ber.types.BerInteger;
import com.beanit.jasn1.ber.types.BerNull;
import com.beanit.jasn1.ber.types.BerOctetString;
import com.beanit.jasn1.ber.types.string.BerVisibleString;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_DeliveryMode;
import ccsds.sle.api.isle.it.SLE_DiagnosticType;
import ccsds.sle.api.isle.it.SLE_Diagnostics;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.it.SLE_SlduStatusNotification;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isle.iutl.ISLE_Credentials;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import ccsds.sle.api.isrv.icltu.ICLTU_AsyncNotify;
import ccsds.sle.api.isrv.icltu.ICLTU_GetParameter;
import ccsds.sle.api.isrv.icltu.ICLTU_Start;
import ccsds.sle.api.isrv.icltu.ICLTU_StatusReport;
import ccsds.sle.api.isrv.icltu.ICLTU_ThrowEvent;
import ccsds.sle.api.isrv.icltu.ICLTU_TransferData;
import ccsds.sle.api.isrv.icltu.types.CLTU_ChannelType;
import ccsds.sle.api.isrv.icltu.types.CLTU_ClcwGvcId;
import ccsds.sle.api.isrv.icltu.types.CLTU_ClcwPhysicalChannel;
import ccsds.sle.api.isrv.icltu.types.CLTU_GetParameterDiagnostic;
import ccsds.sle.api.isrv.icltu.types.CLTU_GvcId;
import ccsds.sle.api.isrv.icltu.types.CLTU_ConfType;
import ccsds.sle.api.isrv.icltu.types.CLTU_NotificationMode;
import ccsds.sle.api.isrv.icltu.types.CLTU_NotificationType;
import ccsds.sle.api.isrv.icltu.types.CLTU_ParameterName;
import ccsds.sle.api.isrv.icltu.types.CLTU_PlopInEffect;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProductionStatus;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProtocolAbortMode;
import ccsds.sle.api.isrv.icltu.types.CLTU_StartDiagnostic;
import ccsds.sle.api.isrv.icltu.types.CLTU_Status;
import ccsds.sle.api.isrv.icltu.types.CLTU_ThrowEventDiagnostic;
import ccsds.sle.api.isrv.icltu.types.CLTU_TransferDataDiagnostic;
import ccsds.sle.api.isrv.icltu.types.CLTU_UplinkStatus;
import ccsds.sle.transfer.service.cltu.outgoing.pdus.CltuStartReturn;
import ccsds.sle.transfer.service.cltu.outgoing.pdus.CltuGetParameterReturn;
import ccsds.sle.transfer.service.cltu.outgoing.pdus.CltuGetParameterReturnV4;
import ccsds.sle.transfer.service.cltu.outgoing.pdus.CltuGetParameterReturnV1To3;
import ccsds.sle.transfer.service.cltu.structures.BufferSize;
import ccsds.sle.transfer.service.cltu.structures.ClcwGvcId;
import ccsds.sle.transfer.service.cltu.structures.ClcwPhysicalChannel;
import ccsds.sle.transfer.service.cltu.structures.CltuData;
import ccsds.sle.transfer.service.cltu.structures.CltuDeliveryMode;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter;
//import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.SubSeq_parAcquisitionSequenceLength;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParAcquisitionSequenceLength;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParBitLockRequired;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParClcwGlobalVcId;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParClcwPhysicalChannel;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParCltuIdentification;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParDeliveryMode;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParEventInvocationIdentification;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParMaximumCltuLength;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParMinReportingCycle;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParMinimumDelayTime;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParModulationFrequency;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParModulationIndex;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParNotificationMode;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParPlop1IdleSequenceLength;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParPlopInEffect;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParProtocolAbortMode;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParReportingCycle;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParReturnTimeout;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParRfAvailableRequired;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameter.ParSubcarrierToBitRateRatio;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV4;
import ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3;
import ccsds.sle.transfer.service.cltu.structures.CltuIdentification;
import ccsds.sle.transfer.service.cltu.structures.CltuLastOk;
import ccsds.sle.transfer.service.cltu.structures.CltuLastOk.CltuOk;
import ccsds.sle.transfer.service.cltu.structures.CltuLastProcessed;
import ccsds.sle.transfer.service.cltu.structures.CltuLastProcessed.CltuProcessed;
import ccsds.sle.transfer.service.cltu.structures.CltuNotification;
import ccsds.sle.transfer.service.cltu.structures.CltuParameterName;
import ccsds.sle.transfer.service.cltu.structures.CltuStatus;
import ccsds.sle.transfer.service.cltu.structures.ConditionalCltuIdentificationV1;
import ccsds.sle.transfer.service.cltu.structures.CurrentReportingCycle;
import ccsds.sle.transfer.service.cltu.structures.DiagnosticCltuGetParameter;
import ccsds.sle.transfer.service.cltu.structures.DiagnosticCltuStart;
import ccsds.sle.transfer.service.cltu.structures.DiagnosticCltuThrowEvent;
import ccsds.sle.transfer.service.cltu.structures.DiagnosticCltuTransferData;
import ccsds.sle.transfer.service.cltu.structures.EventInvocationId;
import ccsds.sle.transfer.service.cltu.structures.GvcId;
//import ccsds.sle.transfer.service.cltu.structures.GvcId.*;
//import ccsds.sle.transfer.service.cltu.structures.GvcId.VcId;
import ccsds.sle.transfer.service.cltu.structures.ModulationFrequency;
import ccsds.sle.transfer.service.cltu.structures.ModulationIndex;
import ccsds.sle.transfer.service.cltu.structures.NumberOfCltusProcessed;
import ccsds.sle.transfer.service.cltu.structures.NumberOfCltusRadiated;
import ccsds.sle.transfer.service.cltu.structures.NumberOfCltusReceived;
import ccsds.sle.transfer.service.cltu.structures.ProductionStatus;
import ccsds.sle.transfer.service.cltu.structures.SubcarrierDivisor;
import ccsds.sle.transfer.service.cltu.structures.TimeoutPeriod;
import ccsds.sle.transfer.service.cltu.structures.UplinkStatus;
//import ccsds.sle.transfer.service.cltu.structures.VcId;
import ccsds.sle.transfer.service.common.pdus.ReportingCycle;
import ccsds.sle.transfer.service.common.types.ConditionalTime;
import ccsds.sle.transfer.service.common.types.Diagnostics;
import ccsds.sle.transfer.service.common.types.Duration;
import ccsds.sle.transfer.service.common.types.IntPosShort;
import ccsds.sle.transfer.service.common.types.IntUnsignedShort;
import ccsds.sle.transfer.service.common.types.InvokeId;
import ccsds.sle.transfer.service.common.types.ParameterName;
import ccsds.sle.transfer.service.common.types.SlduStatusNotification;
import ccsds.sle.transfer.service.common.types.Time;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * ASN.1 Cltu Translator The class encodes and decodes CLTU PDU's. When
 * decoding, the decoded CLTU operation is instantiated.
 */
public class EE_APIPX_Asn1CltuTranslator extends EE_APIPX_Asn1SleTranslator
{
    /**
     * Constructor of the class which takes the ASNSDK context object as
     * parameter.
     */
    public EE_APIPX_Asn1CltuTranslator(ISLE_OperationFactory pOpFactory,
                                       ISLE_UtilFactory pUtilFactory,
                                       EE_APIPX_PDUTranslator pdutranslator,
                                       int sleVersionNumber)
    {
        super(pOpFactory, pUtilFactory, pdutranslator, sleVersionNumber);
        this.serviceType = SLE_ApplicationIdentifier.sleAI_fwdCltu;
    }

    /**
     * Allocates and fills the object used for the encoding of Cltu Operation
     * for version 1 PDUs. S_OK The CLTU operation has been encoded. E_FAIL
     * Unable to encode the CLTU operation.
     * 
     * @throws SleApiException
     * @throws IOException
     */
    public byte[] encodeCltuOp(ISLE_Operation pCltuOperation, boolean isInvoke) throws SleApiException, IOException
    {
        ReverseByteArrayOutputStream berBAOStream = new ReverseByteArrayOutputStream (10, true);

        switch (pCltuOperation.getOperationType())
        {
        case sleOT_bind:
        {
            if (isInvoke)
            {
                SleBindInvocationPdu obj = new SleBindInvocationPdu();
                encodeBindInvokeOp(pCltuOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                SleBindReturnPdu obj = new SleBindReturnPdu();
                encodeBindReturnOp(pCltuOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_unbind:
        {
            if (isInvoke)
            {
                SleUnbindInvocationPdu obj = new SleUnbindInvocationPdu();
                encodeUnbindInvokeOp(pCltuOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                SleUnbindReturnPdu obj = new SleUnbindReturnPdu();
                encodeUnbindReturnOp(pCltuOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_stop:
        {
            if (isInvoke)
            {
                CltuStopInvocationPdu obj = new CltuStopInvocationPdu();
                encodeStopInvokeOp(pCltuOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                CltuStopReturnPdu obj = new CltuStopReturnPdu();
                encodeStopReturnOp(pCltuOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_scheduleStatusReport:
        {
            if (isInvoke)
            {
                CltuScheduleStatusReportInvocationPdu obj = new CltuScheduleStatusReportInvocationPdu();
                encodeScheduleSRInvokeOp(pCltuOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                CltuScheduleStatusReportReturnPdu obj = new CltuScheduleStatusReportReturnPdu();
                encodeScheduleSRReturnOp(pCltuOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_start:
        {
            ICLTU_Start pOp = null;
            pOp = pCltuOperation.queryInterface(ICLTU_Start.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    if (this.sleVersionNumber == 1)
                    {
                        CltuStartInvocationV1Pdu obj = new CltuStartInvocationV1Pdu();
                        encodeStartInvokeOp(pOp, obj);
                        obj.encode(berBAOStream, true);

                    }
                    else
                    {
                        CltuStartInvocationPdu obj = new CltuStartInvocationPdu();
                        encodeStartInvokeOp(pOp, obj);
                        obj.encode(berBAOStream, true);
                    }
                }
                else
                {
                    CltuStartReturnPdu obj = new CltuStartReturnPdu();
                    encodeStartReturnOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
            }

            break;
        }
        case sleOT_getParameter:
        {
            ICLTU_GetParameter pOp = null;
            pOp = pCltuOperation.queryInterface(ICLTU_GetParameter.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    CltuGetParameterInvocationPdu obj = new CltuGetParameterInvocationPdu();
                    encodeGetParameterInvokeOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
                else
                {
                    // The Cltu parameter are encoded/decoded with different ASN
                    // tag between v1to3, v4 and v5
                	if (this.sleVersionNumber > 4)
                    {
                        CltuGetParameterReturnPdu obj = new CltuGetParameterReturnPdu();
                        encodeGetParameterReturnOp(pOp, obj);                       
                        obj.encode(berBAOStream, true);
                    }
                	else if (this.sleVersionNumber > 3)
                    {
                        CltuGetParameterReturnPduV4 obj = new CltuGetParameterReturnPduV4();
                        encodeGetParameterReturnOpV4(pOp, obj);
                        obj.encode(berBAOStream, true);
                    }
                    else
                    {
                        CltuGetParameterReturnPduV1to3 obj = new CltuGetParameterReturnPduV1to3();
                        encodeGetParameterReturnOpV1to3(pOp, obj);
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
            ICLTU_StatusReport pOp = null;
            pOp = pCltuOperation.queryInterface(ICLTU_StatusReport.class);
            if (pOp != null)
            {
                CltuStatusReportInvocationPdu obj = new CltuStatusReportInvocationPdu();
                encodeStatusReportOp(pOp, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_transferData:
        {
            ICLTU_TransferData pOp = null;
            pOp = pCltuOperation.queryInterface(ICLTU_TransferData.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    CltuTransferDataInvocationPdu obj = new CltuTransferDataInvocationPdu();
                    encodeTransferDataInvokeOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
                else
                {
                    CltuTransferDataReturnPdu obj = new CltuTransferDataReturnPdu();
                    encodeTransferDataReturnOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
            }

            break;
        }
        case sleOT_asyncNotify:
        {
            if (!isInvoke)
            {
                berBAOStream.close();
                throw new SleApiException(HRESULT.E_FAIL, "No async notify return operation");
            }
            else
            {
                ICLTU_AsyncNotify pOp = null;
                pOp = pCltuOperation.queryInterface(ICLTU_AsyncNotify.class);
                if (pOp != null)
                {
                    CltuAsyncNotifyInvocationPdu obj = new CltuAsyncNotifyInvocationPdu();
                    encodeAsyncNotifyOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
            }

            break;
        }
        case sleOT_throwEvent:
        {
            ICLTU_ThrowEvent pOp = null;
            pOp = pCltuOperation.queryInterface(ICLTU_ThrowEvent.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    CltuThrowEventInvocationPdu obj = new CltuThrowEventInvocationPdu();
                    encodeThrowEventInvokeOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
                else
                {
                    CltuThrowEventReturnPdu obj = new CltuThrowEventReturnPdu();
                    encodeThrowEventReturnOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
            }

            break;
        }
        case sleOT_transferBuffer:
        {
            // not in cltu
            berBAOStream.close();
            throw new SleApiException(HRESULT.E_FAIL, "Not in cltu!");
        }
        default:
        {
            berBAOStream.close();
            throw new SleApiException(HRESULT.E_FAIL, "Not in cltu!");
        }
        }

        return berBAOStream.getArray();
    }

    /**
     * Instantiates a new CLTU operation from the version 1 object given as
     * parameter, and releases the object. S_OK A new CLTU operation has been
     * Instantiated. E_FAIL Unable to instantiate a CLTU operation.
     * 
     * @throws IOException
     * @throws SleApiException
     */
    public ISLE_Operation decodeCltuOp(byte[] buffer, EE_Reference<Boolean> isInvoke) throws IOException,
                                                                                     SleApiException
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
        else if (tag.equals(CltuStartInvocationPdu.tag))
        {
            ICLTU_Start pOp = null;
            SLE_OpType opTye = SLE_OpType.sleOT_start;
            pOp = this.operationFactory.createOperation(ICLTU_Start.class,
                                                        opTye,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                if (this.sleVersionNumber == 1)
                {
                    CltuStartInvocationV1Pdu obj = new CltuStartInvocationV1Pdu();
                    obj.decode(is, false);
                    decodeStartInvokeOp(obj, pOp);
                }
                else
                {
                    CltuStartInvocationPdu obj = new CltuStartInvocationPdu();
                    obj.decode(is, false);
                    decodeStartInvokeOp(obj, pOp);
                }

                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(CltuStartReturnPdu.tag))
        {
            ICLTU_Start pOp = null;
            CltuStartReturnPdu obj = new CltuStartReturnPdu();
            obj.decode(is, false);
            pOp = decodeStartReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
            if (pOp != null)
            {
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(CltuStopInvocationPdu.tag))
        {
            CltuStopInvocationPdu obj = new CltuStopInvocationPdu();
            obj.decode(is, false);
            pOperation = decodeStopInvokeOp(obj);
            isInvoke.setReference(new Boolean(true));
        }
        else if (tag.equals(CltuStopReturnPdu.tag))
        {
            CltuStopReturnPdu obj = new CltuStopReturnPdu();
            obj.decode(is, false);
            pOperation = decodeStopReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
        }
        else if (tag.equals(CltuScheduleStatusReportInvocationPdu.tag))
        {
            CltuScheduleStatusReportInvocationPdu obj = new CltuScheduleStatusReportInvocationPdu();
            obj.decode(is, false);
            pOperation = decodeScheduleSRInvokeOp(obj);
            isInvoke.setReference(new Boolean(true));
        }
        else if (tag.equals(CltuScheduleStatusReportReturnPdu.tag))
        {
            CltuScheduleStatusReportReturnPdu obj = new CltuScheduleStatusReportReturnPdu();
            obj.decode(is, false);
            pOperation = decodeScheduleSRReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
        }
        else if (tag.equals(CltuGetParameterInvocationPdu.tag))
        {
            ICLTU_GetParameter pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_getParameter;
            pOp = this.operationFactory.createOperation(ICLTU_GetParameter.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                CltuGetParameterInvocationPdu obj = new CltuGetParameterInvocationPdu();
                obj.decode(is, false);
                decodeGetParameterInvokeOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(CltuGetParameterReturnPdu.tag))
        {
            ICLTU_GetParameter pOp = null;
            // The Cltu parameter are encoded/decoded with different ASN tag
            // between v1to3 and v4
            if (this.sleVersionNumber >= 5){
            	CltuGetParameterReturnPdu obj = new CltuGetParameterReturnPdu();
                obj.decode(is, false);
                pOp = decodeGetParameterReturnOp(obj);
            }            	            
            else if (this.sleVersionNumber == 4)
            {
                CltuGetParameterReturnPduV4 obj = new CltuGetParameterReturnPduV4();
                obj.decode(is, false);
                pOp = decodeGetParameterReturnOpV4(obj);
            }
            else
            {
                CltuGetParameterReturnPduV1to3 obj = new CltuGetParameterReturnPduV1to3();
                obj.decode(is, false);
                pOp = decodeGetParameterReturnOpV1to3(obj);
            }
            isInvoke.setReference(new Boolean(false));
            if (pOp != null)
            {
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(CltuStatusReportInvocationPdu.tag))
        {
            ICLTU_StatusReport pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_statusReport;
            pOp = this.operationFactory.createOperation(ICLTU_StatusReport.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                CltuStatusReportInvocationPdu obj = new CltuStatusReportInvocationPdu();
                obj.decode(is, false);
                decodeStatusReportOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(CltuAsyncNotifyInvocationPdu.tag))
        {
            ICLTU_AsyncNotify pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_asyncNotify;
            pOp = this.operationFactory.createOperation(ICLTU_AsyncNotify.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                CltuAsyncNotifyInvocationPdu obj = new CltuAsyncNotifyInvocationPdu();
                obj.decode(is, false);
                decodeAsyncNotifyOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(CltuThrowEventInvocationPdu.tag))
        {
            ICLTU_ThrowEvent pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_throwEvent;
            pOp = this.operationFactory.createOperation(ICLTU_ThrowEvent.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                CltuThrowEventInvocationPdu obj = new CltuThrowEventInvocationPdu();
                obj.decode(is, false);
                decodeThrowEventInvokeOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(CltuThrowEventReturnPdu.tag))
        {
            ICLTU_ThrowEvent pOp = null;
            CltuThrowEventReturnPdu obj = new CltuThrowEventReturnPdu();
            obj.decode(is, false);
            pOp = decodeThrowEventReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
            if (pOp != null)
            {
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(CltuTransferDataInvocationPdu.tag))
        {
            ICLTU_TransferData pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_transferData;
            pOp = this.operationFactory.createOperation(ICLTU_TransferData.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                CltuTransferDataInvocationPdu obj = new CltuTransferDataInvocationPdu();
                obj.decode(is, false);
                decodeTransferDataInvokeOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(CltuTransferDataReturnPdu.tag))
        {
            ICLTU_TransferData pOp = null;
            CltuTransferDataReturnPdu obj = new CltuTransferDataReturnPdu();
            obj.decode(is, false);
            pOp = decodeTransferDataReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
            if (pOp != null)
            {
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }

        return pOperation;
    }

    /**
     * Fills the object used for the encoding of Cltu Start invoke operation.
     * CodesS_OK The CLTU Start operation has been encoded. E_FAIL Unable to
     * encode the CLTU Start operation.
     * 
     * @throws SleApiException
     */
    private void encodeStartInvokeOp(ICLTU_Start pStartOperation, CltuStartInvocationPdu eeaCltuO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pStartOperation.getInvokerCredentials();
        //eeaCltuO.invokerCredentials = encodeCredentials(pCredentials);
        eeaCltuO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaCltuO.setInvokeId (new InvokeId(pStartOperation.getInvokeId()));

        // the first cltu identification
        if (pStartOperation.getFirstCltuIdUsed())
        {
            eeaCltuO.setFirstCltuIdentification(new CltuIdentification(pStartOperation.getFirstCltuId()));
        }
    }

    /**
     * Fills the CLTU START invoke operation from the object. S_OK The CLTU
     * Start operation has been decoded. E_FAIL Unable to decode the CLTU Start
     * operation.
     * 
     * @throws SleApiException
     */
    private void decodeStartInvokeOp(CltuStartInvocationPdu eeaCltuO, ICLTU_Start pStartOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaCltuO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pStartOperation.putInvokerCredentials(pCredentials);
        }

        // the invoker id
        pStartOperation.setInvokeId((int) eeaCltuO.getInvokeId().value.intValue());

        // the first cltu identification
        pStartOperation.setFirstCltuId(eeaCltuO.getFirstCltuIdentification().value.longValue());
    }

    /**
     * Fills the object used for the encoding of Cltu Start return operation.
     * S_OK The CLTU Start operation has been encoded. E_FAIL Unable to encode
     * the CLTU Start operation.
     * 
     * @throws SleApiException
     */
    private void encodeStartReturnOp(ICLTU_Start pStartOperation, CltuStartReturnPdu eeaCltuO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pStartOperation.getPerformerCredentials();
        eeaCltuO.setPerformerCredentials(encodeCredentials(pCredentials));

        // the invoker id
        eeaCltuO.setInvokeId(new InvokeId(pStartOperation.getInvokeId()));

        // the result
        if (pStartOperation.getResult() == SLE_Result.sleRES_positive)
        {
            // positive result
        	CltuStartReturn.Result.PositiveResult pRes = new CltuStartReturn.Result.PositiveResult();

            // the radiation start time
            ISLE_Time pTime = null;
            pTime = pStartOperation.getStartProductionTime();
            Time startT = encodeTime(pTime);
            if (startT == null)
            {
                throw new SleApiException(HRESULT.E_FAIL);
            }

            pRes.setStartRadiationTime( startT);

            // the radiation stop time
            pTime = pStartOperation.getStopProductionTime();
            ConditionalTime stopT = encodeConditionalTime(pTime);
            if (stopT == null)
            {
                throw new SleApiException(HRESULT.E_FAIL);
            }

            pRes.setStopRadiationTime(stopT);
            CltuStartReturn.Result res = new CltuStartReturn.Result();
            res.setPositiveResult(pRes);
            eeaCltuO.setResult( res);
        }
        else
        {
        	CltuStartReturn.Result negResult = new CltuStartReturn.Result();

            if (pStartOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                DiagnosticCltuStart repSpecific = new DiagnosticCltuStart();

                switch (pStartOperation.getStartDiagnostic())
                {
                case cltuSTD_outOfService:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_StartDiagnostic.cltuSTD_outOfService.getCode()));
                    break;
                }
                case cltuSTD_unableToComply:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_StartDiagnostic.cltuSTD_unableToComply.getCode()));
                    break;
                }
                case cltuSTD_productionTimeExpired:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_StartDiagnostic.cltuSTD_productionTimeExpired.getCode()));
                    break;
                }
                case cltuSTD_invalidCltuId:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_StartDiagnostic.cltuSTD_invalidCltuId.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_StartDiagnostic.cltuSTD_invalid.getCode()));
                    break;
                }
                }

                negResult.setNegativeResult(repSpecific);
            }
            else
            {
                // common diagnostic
                DiagnosticCltuStart repCommon = new DiagnosticCltuStart();
                repCommon.setCommon(new Diagnostics(pStartOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }

            eeaCltuO.setResult(negResult);
        }
    }

    /**
     * Fills the CLTU START return operation from the object. S_OK The CLTU
     * Start operation has been decoded. E_FAIL Unable to decode the CLTU Start
     * operation.
     * 
     * @throws SleApiException
     */
    private ICLTU_Start decodeStartReturnOp(CltuStartReturnPdu eeaCltuO) throws SleApiException
    {
        ICLTU_Start pStartOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_start;
        ISLE_Operation pOperation = null;

        pOperation = this.pduTranslator.getReturnOp(eeaCltuO.getInvokeId(), opType);
        if (pOperation != null)
        {
            pStartOperation = pOperation.queryInterface(ICLTU_Start.class);
            if (pStartOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaCltuO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pStartOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pStartOperation.setInvokeId(eeaCltuO.getInvokeId().value.intValue());

                // the result
                if (eeaCltuO.getResult().getPositiveResult() != null)
                {
                    pStartOperation.setPositiveResult();

                    // the radiation start time
                    ISLE_Time pTime = null;
                    pTime = decodeTime(eeaCltuO.getResult().getPositiveResult().getStartRadiationTime());
                    if (pTime == null)
                    {
                        throw new SleApiException(HRESULT.E_FAIL);
                    }
                    else
                    {
                        pStartOperation.putStartProductionTime(pTime);
                    }

                    // the radiation stop time
                    pTime = null;
                    pTime = decodeConditionalTime(eeaCltuO.getResult().getPositiveResult().getStopRadiationTime());
                    if (pTime != null)
                    {
                        pStartOperation.putStopProductionTime(pTime);
                    }
                }
                else
                {
                    // negative result
                    if (eeaCltuO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        int specValue = (int) eeaCltuO.getResult().getNegativeResult().getSpecific().value.intValue();
                        pStartOperation.setStartDiagnostic(CLTU_StartDiagnostic.getStartDiagnosticByCode(specValue));
                    }
                    else
                    {
                        // common diagnostic
                        int commValue = (int) eeaCltuO.getResult().getNegativeResult().getCommon().value.intValue();
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
     * Fills the object used for the encoding of Cltu GetParameter invoke
     * operation. S_OK The CLTU GetParameter operation has been encoded. E_FAIL
     * Unable to encode the CLTU GetParameter operation.
     * 
     * @throws SleApiException
     */
    private void encodeGetParameterInvokeOp(ICLTU_GetParameter pGetParameterOperation,
                                            CltuGetParameterInvocationPdu eeaCltuO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pGetParameterOperation.getInvokerCredentials();
        eeaCltuO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaCltuO.setInvokeId(new InvokeId(pGetParameterOperation.getInvokeId()));

        // the parameter
        eeaCltuO.setCltuParameter (new CltuParameterName(pGetParameterOperation.getRequestedParameter().getCode()));
    }

    /**
     * Fills the CLTU GET-PARAMETER invoke operation from the object. S_OK The
     * CLTU GetParameter operation has been decoded. E_FAIL Unable to decode the
     * CLTU GetParameter operation.
     * 
     * @throws SleApiException
     */
    private void decodeGetParameterInvokeOp(CltuGetParameterInvocationPdu eeaCltuO,
                                            ICLTU_GetParameter pGetParameterOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaCltuO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pGetParameterOperation.putInvokerCredentials(pCredentials);
        }

        // the invoke id
        pGetParameterOperation.setInvokeId(eeaCltuO.getInvokeId().value.intValue());

        // the parameter
        pGetParameterOperation.setRequestedParameter(CLTU_ParameterName
                .getParameterNameByCode(eeaCltuO.getCltuParameter().value.intValue()));
    }

    /**
     * Fills the object used for the encoding of Cltu sleapi#1479: The Cltu
     * parameter are encoded/decoded with different ASN tag between v1to3 and v4
     * GetParameter return operation. S_OK The CLTU GetParameter operation has
     * been encoded. E_FAIL Unable to encode the CLTU GetParameter operation.
     */
    private void encodeGetParameterReturnOpV1to3(ICLTU_GetParameter pGetParameterOperation,
                                                 CltuGetParameterReturnPduV1to3 eeaCltuO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pGetParameterOperation.getPerformerCredentials();
        eeaCltuO.setPerformerCredentials( encodeCredentials(pCredentials));

        // the invoke id
        eeaCltuO.setInvokeId(new InvokeId(pGetParameterOperation.getInvokeId()));

        // the result
        if (pGetParameterOperation.getResult() == SLE_Result.sleRES_positive)
        {

            CltuGetParameterV1To3 cltuGetParam = new CltuGetParameterV1To3();
            encodeParameterV3(pGetParameterOperation, cltuGetParam);
            CltuGetParameterReturnV1To3.Result posResult = new CltuGetParameterReturnV1To3.Result();
            posResult.setPositiveResult(cltuGetParam);
            eeaCltuO.setResult(posResult);
        }
        else
        {
            CltuGetParameterReturnV1To3.Result negResult = new CltuGetParameterReturnV1To3.Result();

            if (pGetParameterOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                DiagnosticCltuGetParameter repSpecific = new DiagnosticCltuGetParameter();

                switch (pGetParameterOperation.getGetParameterDiagnostic())
                {
                case cltuGP_unknownParameter:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_GetParameterDiagnostic.cltuGP_unknownParameter.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_GetParameterDiagnostic.cltuGP_invalid.getCode()));
                    break;
                }
                }

                negResult.setNegativeResult(repSpecific);
            }
            else
            {
                // common diagnostic
                DiagnosticCltuGetParameter repCommon = new DiagnosticCltuGetParameter();
                repCommon.setCommon(new Diagnostics(pGetParameterOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }

            eeaCltuO.setResult(negResult);
        }
    }

    /**
     * Fills the object used for the encoding of Cltu GetParameter return
     * operation. S_OK The CLTU GetParameter operation has been encoded. E_FAIL
     * Unable to encode the CLTU GetParameter operation.
     * 
     * @throws SleApiException
     */
    private void encodeGetParameterReturnOp(ICLTU_GetParameter pGetParameterOperation,
                                            CltuGetParameterReturnPdu eeaCltuO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pGetParameterOperation.getPerformerCredentials();
        eeaCltuO.setPerformerCredentials( encodeCredentials(pCredentials));

        // the invoke id
        eeaCltuO.setInvokeId(new InvokeId(pGetParameterOperation.getInvokeId()));

        // the result
        if (pGetParameterOperation.getResult() == SLE_Result.sleRES_positive)
        {
            CltuGetParameter cltuGetParam = new CltuGetParameter();
            encodeParameter(pGetParameterOperation, cltuGetParam);
            CltuGetParameterReturn.Result posResult = new ccsds.sle.transfer.service.cltu.outgoing.pdus.CltuGetParameterReturn.Result();
            posResult.setPositiveResult(cltuGetParam);
            eeaCltuO.setResult(posResult);
        }
        else
        {
            ccsds.sle.transfer.service.cltu.outgoing.pdus.CltuGetParameterReturn.Result negResult = new ccsds.sle.transfer.service.cltu.outgoing.pdus.CltuGetParameterReturn.Result();
            if (pGetParameterOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                DiagnosticCltuGetParameter repSpecific = new DiagnosticCltuGetParameter();

                switch (pGetParameterOperation.getGetParameterDiagnostic())
                {
                case cltuGP_unknownParameter:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_GetParameterDiagnostic.cltuGP_unknownParameter.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_GetParameterDiagnostic.cltuGP_invalid.getCode()));
                    break;
                }
                }

                negResult.setNegativeResult(repSpecific);
            }
            else
            {
                // common diagnostic
                DiagnosticCltuGetParameter repCommon = new DiagnosticCltuGetParameter();
                repCommon.setCommon(new Diagnostics(pGetParameterOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }

            eeaCltuO.setResult(negResult);
        }
    }

    /**
     * Fills the CLTU GET-PARAMETER return operation sleapi#1479: The Cltu
     * parameter are encoded/decoded with different ASN tag between v1to3 and v4
     * from the object.S_OK The CLTU GetParameter operation has been decoded.
     * E_FAIL Unable to decode the CLTU GetParameter operation.
     */
    private ICLTU_GetParameter decodeGetParameterReturnOpV1to3(CltuGetParameterReturnPduV1to3 eeaCltuO) throws SleApiException
    {
        ICLTU_GetParameter pGetParameterOperation = null;
        ISLE_Operation pOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_getParameter;

        pOperation = this.pduTranslator.getReturnOp(eeaCltuO.getInvokeId(), opType);
        if (pOperation != null)
        {
            pGetParameterOperation = pOperation.queryInterface(ICLTU_GetParameter.class);
            if (pGetParameterOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaCltuO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pGetParameterOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pGetParameterOperation.setInvokeId(eeaCltuO.getInvokeId().value.intValue());

                // the result
                if (eeaCltuO.getResult().getPositiveResult() != null)
                {
                    decodeParameterV3(eeaCltuO.getResult().getPositiveResult(), pGetParameterOperation);
                    pGetParameterOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eeaCltuO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        int specValue = eeaCltuO.getResult().getNegativeResult().getSpecific().value.intValue();
                        pGetParameterOperation.setGetParameterDiagnostic(CLTU_GetParameterDiagnostic
                                .getGetParamDiagByCode(specValue));
                    }
                    else
                    {
                        // common diagnostic
                        int commValue = eeaCltuO.getResult().getNegativeResult().getCommon().value.intValue();
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
     * Fills the CLTU GET-PARAMETER return operation from the object. S_OK The
     * CLTU GetParameter operation has been decoded. E_FAIL Unable to decode the
     * CLTU GetParameter operation.
     * 
     * @throws SleApiException
     */
    private ICLTU_GetParameter decodeGetParameterReturnOp(CltuGetParameterReturnPdu eeaCltuO) throws SleApiException
    {
        ICLTU_GetParameter pGetParameterOperation = null;
        ISLE_Operation pOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_getParameter;

        pOperation = this.pduTranslator.getReturnOp(eeaCltuO.getInvokeId(), opType);
        if (pOperation != null)
        {
            pGetParameterOperation = pOperation.queryInterface(ICLTU_GetParameter.class);
            if (pGetParameterOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaCltuO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pGetParameterOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pGetParameterOperation.setInvokeId(eeaCltuO.getInvokeId().value.intValue());

                // the result
                if (eeaCltuO.getResult().getPositiveResult() != null)
                {
                    decodeParameter(eeaCltuO.getResult().getPositiveResult(), pGetParameterOperation);
                    pGetParameterOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eeaCltuO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        int specValue = (int) eeaCltuO.getResult().getNegativeResult().getSpecific().value.intValue();
                        pGetParameterOperation.setGetParameterDiagnostic(CLTU_GetParameterDiagnostic
                                .getGetParamDiagByCode(specValue));
                    }
                    else
                    {
                        // common diagnostic
                        int commValue = (int) eeaCltuO.getResult().getNegativeResult().getCommon().intValue();
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
     * Fills the CLTU GET-PARAMETER return operation from the object. S_OK The
     * CLTU GetParameter operation has been decoded. E_FAIL Unable to decode the
     * CLTU GetParameter operation.
     * 
     * @throws SleApiException
     */
    private ICLTU_GetParameter decodeGetParameterReturnOpV4(CltuGetParameterReturnPduV4 eeaCltuO) throws SleApiException
    {
        ICLTU_GetParameter pGetParameterOperation = null;
        ISLE_Operation pOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_getParameter;

        pOperation = this.pduTranslator.getReturnOp(eeaCltuO.getInvokeId(), opType);
        if (pOperation != null)
        {
            pGetParameterOperation = pOperation.queryInterface(ICLTU_GetParameter.class);
            if (pGetParameterOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaCltuO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pGetParameterOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pGetParameterOperation.setInvokeId(eeaCltuO.getInvokeId().value.intValue());

                // the result
                if (eeaCltuO.getResult().getPositiveResult() != null)
                {
                    decodeParameterV4(eeaCltuO.getResult().getPositiveResult(), pGetParameterOperation);
                    pGetParameterOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eeaCltuO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        int specValue = (int) eeaCltuO.getResult().getNegativeResult().getSpecific().value.intValue();
                        pGetParameterOperation.setGetParameterDiagnostic(CLTU_GetParameterDiagnostic
                                .getGetParamDiagByCode(specValue));
                    }
                    else
                    {
                        // common diagnostic
                        int commValue = (int) eeaCltuO.getResult().getNegativeResult().getCommon().intValue();
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
     * Fills the object used for the encoding of Cltu V4: The Cltu
     * parameter are encoded/decoded with different ASN tag between v4 and v5
     * GetParameter return operation. S_OK The CLTU GetParameter operation has
     * been encoded. E_FAIL Unable to encode the CLTU GetParameter operation.
     * 
     * @throws SleApiException
     */
    private void encodeGetParameterReturnOpV4(ICLTU_GetParameter pGetParameterOperation,
                                            CltuGetParameterReturnPduV4 eeaCltuO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pGetParameterOperation.getPerformerCredentials();
        eeaCltuO.setPerformerCredentials( encodeCredentials(pCredentials));

        // the invoke id
        eeaCltuO.setInvokeId(new InvokeId(pGetParameterOperation.getInvokeId()));

        // the result
        if (pGetParameterOperation.getResult() == SLE_Result.sleRES_positive)
        {
            CltuGetParameterV4 cltuGetParam = new CltuGetParameterV4();
            encodeParameterV4(pGetParameterOperation, cltuGetParam);
            CltuGetParameterReturnV4.Result posResult = new CltuGetParameterReturnV4.Result();
            posResult.setPositiveResult(cltuGetParam);
            eeaCltuO.setResult(posResult);
        }
        else
        {
            CltuGetParameterReturnV4.Result negResult = new CltuGetParameterReturnV4.Result();

            if (pGetParameterOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                DiagnosticCltuGetParameter repSpecific = new DiagnosticCltuGetParameter();

                switch (pGetParameterOperation.getGetParameterDiagnostic())
                {
                case cltuGP_unknownParameter:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_GetParameterDiagnostic.cltuGP_unknownParameter.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_GetParameterDiagnostic.cltuGP_invalid.getCode()));
                    break;
                }
                }

                negResult.setNegativeResult(repSpecific);
            }
            else
            {
                // common diagnostic
                DiagnosticCltuGetParameter repCommon = new DiagnosticCltuGetParameter();
                repCommon.setCommon(new Diagnostics(pGetParameterOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }

            eeaCltuO.setResult(negResult);
        }
    }

    
    /**
     * Fills the object used for the encoding of Cltu TransferData invoke
     * operation. S_OK The CLTU TransferData operation has been encoded. E_FAIL
     * Unable to encode the CLTU TransferData operation.
     * 
     * @throws SleApiException
     */
    private void encodeTransferDataInvokeOp(ICLTU_TransferData pTransferDataOperation,
                                            CltuTransferDataInvocationPdu eeaCltuO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pTransferDataOperation.getInvokerCredentials();
        eeaCltuO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaCltuO.setInvokeId(new InvokeId(pTransferDataOperation.getInvokeId()));

        // the cltu identification
        eeaCltuO.setCltuIdentification(new CltuIdentification(pTransferDataOperation.getCltuId()));

        // the earliest transmission time
        ISLE_Time pTime = null;
        pTime = pTransferDataOperation.getEarliestRadTime();
        ConditionalTime cTime = encodeConditionalTime(pTime);
        if (cTime == null)
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }
        eeaCltuO.setEarliestTransmissionTime(cTime);

        // the latest transmission time
        pTime = null;
        pTime = pTransferDataOperation.getLatestRadTime();
        cTime = encodeConditionalTime(pTime);
        if (cTime == null)
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }
        eeaCltuO.setLatestTransmissionTime(cTime);

        // the delay time
        eeaCltuO.setDelayTime(new Duration(pTransferDataOperation.getDelayTime()));

        // the sldu status notification
        eeaCltuO.setSlduRadiationNotification(new SlduStatusNotification(pTransferDataOperation
                .getRadiationNotification().getCode()));

        // the space link data unit
        byte[] pdata = pTransferDataOperation.getData();
        eeaCltuO.setCltuData(new CltuData(pdata));
    }

    /**
     * Fills the CLTU TRANSFER-DATA invoke operation from the object. S_OK The
     * CLTU TransferData operation has been decoded. E_FAIL Unable to decode the
     * CLTU TransferData operation.
     * 
     * @throws SleApiException
     */
    private void decodeTransferDataInvokeOp(CltuTransferDataInvocationPdu eeaCltuO,
                                            ICLTU_TransferData pTransferDataOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaCltuO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pTransferDataOperation.putInvokerCredentials(pCredentials);
        }

        // the invoke id
        //pTransferDataOperation.setInvokeId((int) eeaCltuO.invokeId.value);
        pTransferDataOperation.setInvokeId((int) eeaCltuO.getInvokeId().value.intValue());

        // the cltu identification
        //pTransferDataOperation.setCltuId(eeaCltuO.cltuIdentification.value);
        pTransferDataOperation.setCltuId(eeaCltuO.getCltuIdentification().value.longValue());

        // the earliest transmission time
        ISLE_Time pTime = null;
        //pTime = decodeConditionalTime(eeaCltuO.earliestTransmissionTime);
        pTime = decodeConditionalTime(eeaCltuO.getEarliestTransmissionTime());
        if (pTime != null)
        {
            pTransferDataOperation.putEarliestRadTime(pTime);
        }

        // the latest transmission time
        pTime = null;
        pTime = decodeConditionalTime(eeaCltuO.getLatestTransmissionTime());
        if (pTime != null)
        {
            pTransferDataOperation.putLatestRadTime(pTime);
        }

        // the delay time
        pTransferDataOperation.setDelayTime(eeaCltuO.getDelayTime().value.longValue());

        // the sldu status notification
        SLE_SlduStatusNotification productionStatus = SLE_SlduStatusNotification
                //.getSlduStatusNotificationByCode((int) eeaCltuO.slduRadiationNotification.value);
        		.getSlduStatusNotificationByCode((int) eeaCltuO.getSlduRadiationNotification().value.intValue());
        pTransferDataOperation.setRadiationNotification(productionStatus);

        // the space link data unit
        byte[] pdata = null;
        //pdata = eeaCltuO.cltuData.value;
        pdata = eeaCltuO.getCltuData().value;
        pTransferDataOperation.putData(pdata);
    }

    /**
     * Fills the object used for the encoding of Cltu TransferData return
     * operation. S_OK The CLTU TransferData operation has been encoded. E_FAIL
     * Unable to encode the CLTU TransferData operation.
     * 
     * @throws SleApiException
     */
    private void encodeTransferDataReturnOp(ICLTU_TransferData pTransferDataOperation,
                                            CltuTransferDataReturnPdu eeaCltuO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pTransferDataOperation.getPerformerCredentials();
        eeaCltuO.setPerformerCredentials( encodeCredentials(pCredentials));

        // the invoke id
        eeaCltuO.setInvokeId( new InvokeId(pTransferDataOperation.getInvokeId()));

        // the cltu identification
        eeaCltuO.setCltuIdentification(new CltuIdentification(pTransferDataOperation.getExpectedCltuId()));

        // the cltu buffer available
        eeaCltuO.setCltuBufferAvailable(new BufferSize(pTransferDataOperation.getCltuBufferAvailable()));

        // the result
        if (pTransferDataOperation.getResult() == SLE_Result.sleRES_positive)
        {
            // positive result
            //eeaCltuO.result = new ccsds.sle.transfer.service.cltu.outgoing.pdus.CltuTransferDataReturn.SubChoice_result(new BerNull(),
        	//null);
        	ccsds.sle.transfer.service.cltu.outgoing.pdus.CltuTransferDataReturn.Result posResult = new ccsds.sle.transfer.service.cltu.outgoing.pdus.CltuTransferDataReturn.Result();
        	posResult.setPositiveResult(new BerNull());
        	eeaCltuO.setResult(posResult);
        }
        else
        {
            //ccsds.sle.transfer.service.cltu.outgoing.pdus.CltuTransferDataReturn.SubChoice_result negResult = new ccsds.sle.transfer.service.cltu.outgoing.pdus.CltuTransferDataReturn.SubChoice_result();
        	ccsds.sle.transfer.service.cltu.outgoing.pdus.CltuTransferDataReturn.Result negResult = new ccsds.sle.transfer.service.cltu.outgoing.pdus.CltuTransferDataReturn.Result();

        	
            // negative result
            if (pTransferDataOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                // specific diagnostic
                DiagnosticCltuTransferData repSpecific = new DiagnosticCltuTransferData();

                switch (pTransferDataOperation.getTransferDataDiagnostic())
                {
                case cltuXFD_unableToProcess:
                {
                    //repSpecific.specific = new BerInteger(CLTU_TransferDataDiagnostic.cltuXFD_unableToProcess.getCode());
                	repSpecific.setSpecific(new BerInteger(CLTU_TransferDataDiagnostic.cltuXFD_unableToProcess.getCode()));
                    break;
                }
                case cltuXFD_unableToStore:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_TransferDataDiagnostic.cltuXFD_unableToStore.getCode()));
                    break;
                }
                case cltuXFD_outOfSequence:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_TransferDataDiagnostic.cltuXFD_outOfSequence.getCode()));
                    break;
                }
                case cltuXFD_inconsistenceTimeRange:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_TransferDataDiagnostic.cltuXFD_inconsistenceTimeRange.getCode()));
                    break;
                }
                case cltuXFD_invalidTime:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_TransferDataDiagnostic.cltuXFD_invalidTime.getCode()));
                    break;
                }
                case cltuXFD_lateSldu:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_TransferDataDiagnostic.cltuXFD_lateSldu.getCode()));
                    break;
                }
                case cltuXFD_invalidDelayTime:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_TransferDataDiagnostic.cltuXFD_invalidDelayTime.getCode()));
                    break;
                }
                case cltuXFD_cltuError:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_TransferDataDiagnostic.cltuXFD_cltuError.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(CLTU_GetParameterDiagnostic.cltuGP_invalid.getCode()));
                    break;
                }
                }

                //negResult.negativeResult = repSpecific;
                negResult.setNegativeResult(repSpecific);
            }
            else
            {
                // common diagnostic
                DiagnosticCltuTransferData repCommon = new DiagnosticCltuTransferData();
                repCommon.setCommon(new Diagnostics(pTransferDataOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }

            //eeaCltuO.result = negResult;
            eeaCltuO.setResult(negResult);
        }
    }

    /**
     * Fills the CLTU TRANSFER-DATA return operation from the object. S_OK The
     * CLTU TransferData operation has been decoded. E_FAIL Unable to decode the
     * CLTU TransferData operation.
     * 
     * @throws SleApiException
     */
    private ICLTU_TransferData decodeTransferDataReturnOp(CltuTransferDataReturnPdu eeaCltuO) throws SleApiException
    {
        ISLE_Operation pOperation = null;
        ICLTU_TransferData pTransferDataOperation = null;
        pOperation = this.pduTranslator.getReturnOp(eeaCltuO.getInvokeId(), SLE_OpType.sleOT_transferData);
        if (pOperation != null)
        {
            pTransferDataOperation = pOperation.queryInterface(ICLTU_TransferData.class);

            // the performer credentials
            ISLE_Credentials pCredentials = null;
            pCredentials = decodeCredentials(eeaCltuO.getPerformerCredentials());
            if (pCredentials != null)
            {
                pTransferDataOperation.putPerformerCredentials(pCredentials);
            }

            // the invoke id
            pTransferDataOperation.setInvokeId((int) eeaCltuO.getInvokeId().value.intValue());

            // the cltu identification
            pTransferDataOperation.setExpectedCltuId(eeaCltuO.getCltuIdentification().value.longValue());

            // the cltu buffer available
            pTransferDataOperation.setCltuBufferAvailable(eeaCltuO.getCltuBufferAvailable().value.longValue());

            // the result
            //if (eeaCltuO.result.positiveResult != null)
            if (eeaCltuO.getResult().getPositiveResult() != null)
            {
                // positive result
                pTransferDataOperation.setPositiveResult();
            }
            else
            {
                // negative result
                //if (eeaCltuO.result.negativeResult.specific != null)
            	if (eeaCltuO.getResult().getNegativeResult().getSpecific() != null)
                {
                    // specific
                    //int specDiag = (int) eeaCltuO.result.negativeResult.specific.value;
            		int specDiag = eeaCltuO.getResult().getNegativeResult().getSpecific().intValue();
                    pTransferDataOperation.setTransferDataDiagnostic(CLTU_TransferDataDiagnostic
                            .getTransferDataDiagnosticByCode(specDiag));
                }
                else
                {
                    // common
                    //int commDiag = (int) eeaCltuO.result.negativeResult.common.value;
                	int commDiag = eeaCltuO.getResult().getNegativeResult().getCommon().value.intValue();
                    pTransferDataOperation.setDiagnostics(SLE_Diagnostics.getDiagnosticsByCode(commDiag));
                }
            }
        }
        else
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }

        return pTransferDataOperation;
    }

    /**
     * Fills the object used for the encoding of Cltu AsyncNotify operation.
     * S_OK The CLTU AsyncNotify operation has been encoded. E_FAIL Unable to
     * encode the CLTU AsyncNotify operation.
     * 
     * @throws SleApiException
     */
    private void encodeAsyncNotifyOp(ICLTU_AsyncNotify pAsyncNotifyOperation, CltuAsyncNotifyInvocationPdu eeaCltuO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pAsyncNotifyOperation.getInvokerCredentials();
        eeaCltuO.setInvokerCredentials (encodeCredentials(pCredentials));

        // the cltu notification
        eeaCltuO.setCltuNotification (new CltuNotification());

        switch (pAsyncNotifyOperation.getNotificationType())
        {
        case cltuNT_cltuRadiated:
        {
            eeaCltuO.getCltuNotification().setCltuRadiated( new BerNull());
            break;
        }
        case cltuNT_slduExpired:
        {
            eeaCltuO.getCltuNotification().setSlduExpired(new BerNull());
            break;
        }
        case cltuNT_productionInterrupted:
        {
            eeaCltuO.getCltuNotification().setProductionInterrupted(new BerNull());
            break;
        }
        case cltuNT_productionHalted:
        {
            eeaCltuO.getCltuNotification().setProductionHalted(new BerNull());
            break;
        }
        case cltuNT_productionOperational:
        {
            eeaCltuO.getCltuNotification().setProductionOperational(new BerNull());
            break;
        }
        case cltuNT_bufferEmpty:
        {
            eeaCltuO.getCltuNotification().setBufferEmpty( new BerNull());
            break;
        }
        case cltuNT_actionListCompleted:
        {
            eeaCltuO.getCltuNotification().setActionListCompleted(new EventInvocationId(pAsyncNotifyOperation.getEventThrownId()));
            break;
        }
        case cltuNT_actionListNotCompleted:
        {
            eeaCltuO.getCltuNotification().setActionListNotCompleted(new EventInvocationId(pAsyncNotifyOperation.getEventThrownId()));
            break;
        }
        case cltuNT_eventConditionEvFalse:
        {
            eeaCltuO.getCltuNotification().setEventConditionEvFalse(new EventInvocationId(pAsyncNotifyOperation.getEventThrownId()));
            break;
        }
        default:
        {
            break;
        }
        }

        // the cltu last processed
        if (pAsyncNotifyOperation.getCltusProcessed())
        {
            //CltuLastProcessed cltuLastProcessed = new CltuLastProcessed(null, new CltuProcessed());
        	CltuLastProcessed cltuLastProcessed = new CltuLastProcessed();
        	cltuLastProcessed.setCltuProcessed(new CltuProcessed());
            // the cltu identification
            cltuLastProcessed.getCltuProcessed().setCltuIdentification(new CltuIdentification(pAsyncNotifyOperation.getCltuLastProcessed()));
            // the radiation start time
            ISLE_Time ptime = null;
            ptime = pAsyncNotifyOperation.getRadiationStartTime();
            cltuLastProcessed.getCltuProcessed().setStartRadiationTime (encodeConditionalTime(ptime));
            // the cltu status
            cltuLastProcessed.getCltuProcessed().setCltuStatus(new CltuStatus(pAsyncNotifyOperation.getCltuStatus().getCode()));
            eeaCltuO.setCltuLastProcessed(cltuLastProcessed);
        }
        else
        {
        	CltuLastProcessed cltuLastProcessed = new CltuLastProcessed();
        	cltuLastProcessed.setNoCltuProcessed(new BerNull());
            eeaCltuO.setCltuLastProcessed(cltuLastProcessed);
        }

        // the cltu last ok
        if (pAsyncNotifyOperation.getCltusRadiated())
        {
            //CltuLastOk cltuOk = new CltuLastOk(null, new CltuOk());
        	CltuLastOk cltuOk = new CltuLastOk();
        	cltuOk.setCltuOk(new CltuOk());
            // the cltu identification
            cltuOk.getCltuOk().setCltuIdentification(new CltuIdentification(pAsyncNotifyOperation.getCltuLastOk()));
            // the radiation stop time
            ISLE_Time pTime = null;
            pTime = pAsyncNotifyOperation.getRadiationStopTime();
            cltuOk.getCltuOk().setStopRadiationTime(encodeTime(pTime));
            eeaCltuO.setCltuLastOk(cltuOk);
        }
        else
        {
        	CltuLastOk cltuRad = new CltuLastOk();
        	cltuRad.setNoCltuOk(new BerNull());
            eeaCltuO.setCltuLastOk( cltuRad);
        }

        // the production status
        eeaCltuO.setProductionStatus (new ProductionStatus(pAsyncNotifyOperation.getProductionStatus().getCode()));

        // the uplink status
        eeaCltuO.setUplinkStatus( new UplinkStatus(pAsyncNotifyOperation.getUplinkStatus().getCode()));
    }

    /**
     * Fills the CLTU ASYNC-NOTIFY operation from the object. S_OK The CLTU
     * AsyncNotify operation has been decoded. E_FAIL Unable to decode the CLTU
     * AsyncNotify operation.
     * 
     * @throws SleApiException
     */
    private void decodeAsyncNotifyOp(CltuAsyncNotifyInvocationPdu eeaCltuO, ICLTU_AsyncNotify pAsyncNotifyOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaCltuO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pAsyncNotifyOperation.putInvokerCredentials(pCredentials);
        }

        // the cltu notification
        if (eeaCltuO.getCltuNotification().getCltuRadiated() != null)
        {
            pAsyncNotifyOperation.setNotificationType(CLTU_NotificationType.cltuNT_cltuRadiated);
        }
        else if (eeaCltuO.getCltuNotification().getSlduExpired() != null)
        {
            pAsyncNotifyOperation.setNotificationType(CLTU_NotificationType.cltuNT_slduExpired);
        }
        else if (eeaCltuO.getCltuNotification().getProductionInterrupted() != null)
        {
            pAsyncNotifyOperation.setNotificationType(CLTU_NotificationType.cltuNT_productionInterrupted);
        }
        else if (eeaCltuO.getCltuNotification().getProductionHalted() != null)
        {
            pAsyncNotifyOperation.setNotificationType(CLTU_NotificationType.cltuNT_productionHalted);
        }
        else if (eeaCltuO.getCltuNotification().getProductionOperational() != null)
        {
            pAsyncNotifyOperation.setNotificationType(CLTU_NotificationType.cltuNT_productionOperational);
        }
        else if (eeaCltuO.getCltuNotification().getBufferEmpty() != null)
        {
            pAsyncNotifyOperation.setNotificationType(CLTU_NotificationType.cltuNT_bufferEmpty);
        }
        else if (eeaCltuO.getCltuNotification().getActionListCompleted() != null)
        {
            pAsyncNotifyOperation.setNotificationType(CLTU_NotificationType.cltuNT_actionListCompleted);
            pAsyncNotifyOperation.setEventThrownId(eeaCltuO.getCltuNotification().getActionListCompleted().value.longValue());
        }
        else if (eeaCltuO.getCltuNotification().getActionListNotCompleted() != null)
        {
            pAsyncNotifyOperation.setNotificationType(CLTU_NotificationType.cltuNT_actionListNotCompleted);
            pAsyncNotifyOperation.setEventThrownId(eeaCltuO.getCltuNotification().getActionListNotCompleted().value.longValue());
        }
        else if (eeaCltuO.getCltuNotification().getEventConditionEvFalse() != null)
        {
            pAsyncNotifyOperation.setNotificationType(CLTU_NotificationType.cltuNT_eventConditionEvFalse);
            pAsyncNotifyOperation.setEventThrownId(eeaCltuO.getCltuNotification().getEventConditionEvFalse().value.longValue());
        }
        else
        {
            pAsyncNotifyOperation.setNotificationType(CLTU_NotificationType.cltuNT_invalid);
        }

        // the cltu last processed
        if (eeaCltuO.getCltuLastProcessed().getCltuProcessed() != null) // UMW: SLEAPIJ-36 after merge to SLES-V5
        {
            // the cltu identification
            pAsyncNotifyOperation
                    .setCltuLastProcessed(eeaCltuO.getCltuLastProcessed().getCltuProcessed().getCltuIdentification().value.longValue());
            // the radiation start time
            ISLE_Time pTime = null;
            pTime = decodeConditionalTime(eeaCltuO.getCltuLastProcessed().getCltuProcessed().getStartRadiationTime());
            if (pTime != null)
            {
                pAsyncNotifyOperation.putRadiationStartTime(pTime);
            }
            // the cltu status
            pAsyncNotifyOperation.setCltuStatus(CLTU_Status
                    .getStatusByCode( eeaCltuO.getCltuLastProcessed().getCltuProcessed().getCltuStatus().value.intValue()));
        }

        // the cltu last ok
        if (eeaCltuO.getCltuLastOk().getCltuOk() != null)
        {
            // the cltu identification
            pAsyncNotifyOperation.setCltuLastOk(eeaCltuO.getCltuLastOk().getCltuOk().getCltuIdentification().value.longValue());
            // the radiation stop time
            ISLE_Time pTime = null;
            pTime = decodeTime(eeaCltuO.getCltuLastOk().getCltuOk().getStopRadiationTime());
            if (pTime != null)
            {
                pAsyncNotifyOperation.putRadiationStopTime(pTime);
            }
        }

        // the production status
        pAsyncNotifyOperation.setProductionStatus(CLTU_ProductionStatus
                .getProductionStatusByCode( eeaCltuO.getProductionStatus().value.intValue()));

        // the uplink status
        pAsyncNotifyOperation.setUplinkStatus(CLTU_UplinkStatus
                .getUplinkStatusByCode(eeaCltuO.getUplinkStatus().value.intValue()));
    }

    /**
     * Fills the object used for the encoding of Cltu StatusReport operation.
     * S_OK The CLTU StatusReport operation has been encoded. E_FAIL Unable to
     * encode the CLTU StatusReport operation.
     * 
     * @throws SleApiException
     */
    private void encodeStatusReportOp(ICLTU_StatusReport pStatusReportOperation, CltuStatusReportInvocationPdu eeaCltuO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pStatusReportOperation.getInvokerCredentials();
        eeaCltuO.setInvokerCredentials ( encodeCredentials(pCredentials));

        // the cltu last processed
        if (pStatusReportOperation.getNumberOfCltusProcessed() != 0)
        {
            CltuLastProcessed cltuLastProcessed = new CltuLastProcessed();
            CltuProcessed cltuProcessed = new CltuProcessed();
            // the cltu identification
            cltuProcessed.setCltuIdentification(new CltuIdentification(pStatusReportOperation.getCltuLastProcessed()));
            // the radiation start time
            ISLE_Time pTime = null;
            pTime = pStatusReportOperation.getRadiationStartTime();
            cltuProcessed.setStartRadiationTime(encodeConditionalTime(pTime));
            // the cltu status
            cltuProcessed.setCltuStatus(new CltuStatus(pStatusReportOperation.getCltuStatus().getCode()));
            eeaCltuO.setCltuLastProcessed(cltuLastProcessed);
            eeaCltuO.getCltuLastProcessed().setCltuProcessed(cltuProcessed);
        }
        else
        {
        	CltuLastProcessed cltuLastProcessed = new CltuLastProcessed();
        	cltuLastProcessed.setNoCltuProcessed(new BerNull());
        	eeaCltuO.setCltuLastProcessed(cltuLastProcessed);
        }

        // the cltu last ok
        if (pStatusReportOperation.getNumberOfCltusRadiated() != 0)
        {
            CltuLastOk cltuLastOk = new CltuLastOk();
            CltuOk cltuOk = new CltuOk();
            // the cltu identification
            cltuOk.setCltuIdentification(new CltuIdentification(pStatusReportOperation.getCltuLastOk()));
            // the radiation stop time
            ISLE_Time pTime = null;
            pTime = pStatusReportOperation.getRadiationStopTime();
            cltuOk.setStopRadiationTime(encodeTime(pTime));
            cltuLastOk.setCltuOk(cltuOk);
            eeaCltuO.setCltuLastOk(cltuLastOk);
        }
        else
        {
        	CltuLastOk lastCltu = new CltuLastOk();
        	lastCltu.setNoCltuOk(new BerNull());
            eeaCltuO.setCltuLastOk(lastCltu);
        }

        // the cltu production status
        eeaCltuO.setCltuProductionStatus(new ProductionStatus(pStatusReportOperation.getProductionStatus().getCode()));
        // the uplink status
        eeaCltuO.setUplinkStatus(new UplinkStatus(pStatusReportOperation.getUplinkStatus().getCode()));
        // the number of cltu received
        eeaCltuO.setNumberOfCltusReceived(new NumberOfCltusReceived(pStatusReportOperation.getNumberOfCltusReceived()));
        // the number of cltu radiated
        eeaCltuO.setNumberOfCltusRadiated(new NumberOfCltusRadiated(pStatusReportOperation.getNumberOfCltusRadiated()));
        // the number of cltu processed
        eeaCltuO.setNumberOfCltusProcessed(new NumberOfCltusProcessed(pStatusReportOperation.getNumberOfCltusProcessed()));
        // the cltu buffer available
        eeaCltuO.setCltuBufferAvailable(new BufferSize(pStatusReportOperation.getCltuBufferAvailable()));
    }

    /**
     * Fills the CLTU STATUS-REPORT operation from the object. S_OK The CLTU
     * StatusReport operation has been decoded. E_FAIL Unable to decode the CLTU
     * StatusReport operation.
     * 
     * @throws SleApiException
     */
    private void decodeStatusReportOp(CltuStatusReportInvocationPdu eeaCltuO, ICLTU_StatusReport pStatusReportOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaCltuO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pStatusReportOperation.putInvokerCredentials(pCredentials);
        }

        // the cltu last processed
        if (eeaCltuO.getCltuLastProcessed().getCltuProcessed() != null)
        {
            // the cltu identification
            pStatusReportOperation
                    .setCltuLastProcessed(eeaCltuO.getCltuLastProcessed().getCltuProcessed().getCltuIdentification().value.longValue());
            // the radiation start time
            ISLE_Time pTime = null;
            pTime = decodeConditionalTime(eeaCltuO.getCltuLastProcessed().getCltuProcessed().getStartRadiationTime());
            if (pTime != null)
            {
                pStatusReportOperation.putRadiationStartTime(pTime);
            }
            // the cltu status
            pStatusReportOperation.setCltuStatus(CLTU_Status
                    .getStatusByCode(eeaCltuO.getCltuLastProcessed().getCltuProcessed().getCltuStatus().value.intValue()));
        }

        // the cltu last ok
        if (eeaCltuO.getCltuLastOk().getCltuOk() != null)
        {
            // the cltu identifiaction
            pStatusReportOperation.setCltuLastOk(eeaCltuO.getCltuLastOk().getCltuOk().getCltuIdentification().value.longValue());
            // the radiation stop time
            ISLE_Time pTime = null;
            pTime = decodeTime(eeaCltuO.getCltuLastOk().getCltuOk().getStopRadiationTime());
            if (pTime != null)
            {
                pStatusReportOperation.putRadiationStopTime(pTime);
            }
        }

        // the production status
        pStatusReportOperation.setProductionStatus(CLTU_ProductionStatus
                .getProductionStatusByCode((int) eeaCltuO.getCltuProductionStatus().value.intValue()));
        // the uplink status
        pStatusReportOperation.setUplinkStatus(CLTU_UplinkStatus
                .getUplinkStatusByCode((int) eeaCltuO.getUplinkStatus().value.intValue()));
        // the number of cltu received
        pStatusReportOperation.setNumberOfCltusReceived(eeaCltuO.getNumberOfCltusReceived().value.longValue());
        // the number of cltu processed
        pStatusReportOperation.setNumberOfCltusProcessed(eeaCltuO.getNumberOfCltusProcessed().value.longValue());
        // the number of cltu radiated
        pStatusReportOperation.setNumberOfCltusRadiated(eeaCltuO.getNumberOfCltusRadiated().value.longValue());
        // the cltu buffer available
        pStatusReportOperation.setCltuBufferAvailable(eeaCltuO.getCltuBufferAvailable().value.longValue());
    }

    /**
     * Fills the object used for the encoding of Cltu ThrowEvent invoke
     * operation. S_OK The CLTU ThrowEvent operation has been encoded. E_FAIL
     * Unable to encode the CLTU ThrowEvent operation.
     * 
     * @throws SleApiException
     */
    private void encodeThrowEventInvokeOp(ICLTU_ThrowEvent pThrowEventOperation, CltuThrowEventInvocationPdu eeaCltuO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pThrowEventOperation.getInvokerCredentials();
        eeaCltuO.setInvokerCredentials( encodeCredentials(pCredentials));

        // the invoker id
        eeaCltuO.setInvokeId (new InvokeId(pThrowEventOperation.getInvokeId()));

        // the event thrown id
        eeaCltuO.setEventInvocationIdentification ( new EventInvocationId(pThrowEventOperation.getEventInvocationId()));

        // the event identification
        eeaCltuO.setEventIdentifier( new IntPosShort(pThrowEventOperation.getEventId()));

        // the event qualifier
        byte[] pData = pThrowEventOperation.getEventQualifier();
        eeaCltuO.setEventQualifier( new BerOctetString(pData));
    }

    /**
     * Fills the CLTU THROW-EVENT invoke operation from the object. S_OK The
     * CLTU ThrowEvent operation has been decoded. E_FAIL Unable to decode the
     * CLTU ThrowEvent operation.
     * 
     * @throws SleApiException
     */
    private void decodeThrowEventInvokeOp(CltuThrowEventInvocationPdu eeaCltuO, ICLTU_ThrowEvent pThrowEventOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaCltuO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pThrowEventOperation.putInvokerCredentials(pCredentials);
        }

        // the invoke id
        pThrowEventOperation.setInvokeId(eeaCltuO.getInvokeId().value.intValue());

        // the event thrown id
        pThrowEventOperation.setEventInvocationId(eeaCltuO.getEventInvocationIdentification().value.longValue());

        // the event identification
        pThrowEventOperation.setEventId( eeaCltuO.getEventIdentifier().value.intValue());

        // the event qualifier
        pThrowEventOperation.setEventQualifier(eeaCltuO.getEventQualifier().value);
    }

    /**
     * Fills the object used for the encoding of Cltu ThrowEvent return
     * operation. S_OK The CLTU ThrowEvent operation has been encoded. E_FAIL
     * Unable to encode the CLTU ThrowEvent operation.
     * 
     * @throws SleApiException
     */
    private void encodeThrowEventReturnOp(ICLTU_ThrowEvent pThrowEventOperation, CltuThrowEventReturnPdu eeaCltuO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pThrowEventOperation.getPerformerCredentials();
        eeaCltuO.setPerformerCredentials( encodeCredentials(pCredentials));

        // the invoke id
        eeaCltuO.setInvokeId(new InvokeId(pThrowEventOperation.getInvokeId()));

        // the event thrown id
        eeaCltuO.setEventInvocationIdentification(new EventInvocationId(pThrowEventOperation.getExpectedEventInvocationId()));

        // the result
        if (pThrowEventOperation.getResult() == SLE_Result.sleRES_positive)
        {
            // positive result
        	ccsds.sle.transfer.service.cltu.outgoing.pdus.CltuThrowEventReturn.Result posResult= new ccsds.sle.transfer.service.cltu.outgoing.pdus.CltuThrowEventReturn.Result();
        	posResult.setPositiveResult(new BerNull());
            eeaCltuO.setResult(posResult);
        }
        else
        {
            DiagnosticCltuThrowEvent negResult = new DiagnosticCltuThrowEvent();

            if (pThrowEventOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                // specific diagnostic
                negResult.setSpecific(new BerInteger(pThrowEventOperation.getThrowEventDiagnostic().getCode()));
            }
            else
            {
                // common diagnostic
                negResult.setCommon(new Diagnostics(pThrowEventOperation.getDiagnostics().getCode()));
            }
            ccsds.sle.transfer.service.cltu.outgoing.pdus.CltuThrowEventReturn.Result negThrowEventResult = new ccsds.sle.transfer.service.cltu.outgoing.pdus.CltuThrowEventReturn.Result();
            negThrowEventResult.setNegativeResult(negResult);
            eeaCltuO.setResult(negThrowEventResult);
        }
    }

    /**
     * Fills the CLTU THROW-EVENT return operation from the object. S_OK The
     * CLTU ThrowEvent operation has been decoded. E_FAIL Unable to decode the
     * CLTU ThrowEvent operation.
     * 
     * @throws SleApiException
     */
    private ICLTU_ThrowEvent decodeThrowEventReturnOp(CltuThrowEventReturnPdu eeaCltuO) throws SleApiException
    {
        ISLE_Operation pOperation = null;
        ICLTU_ThrowEvent pThrowEventOperation = null;

        pOperation = this.pduTranslator.getReturnOp(eeaCltuO.getInvokeId(), SLE_OpType.sleOT_throwEvent);
        if (pOperation != null)
        {
            pThrowEventOperation = pOperation.queryInterface(ICLTU_ThrowEvent.class);
            if (pThrowEventOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaCltuO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pThrowEventOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pThrowEventOperation.setInvokeId(eeaCltuO.getInvokeId().value.intValue());

                // the event thrown id
                pThrowEventOperation.setExpectedEventInvocationId(eeaCltuO.getEventInvocationIdentification().value.longValue());

                // the result
                if (eeaCltuO.getResult().getPositiveResult() != null)
                {
                    // positive result
                    pThrowEventOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eeaCltuO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        // specific
                        pThrowEventOperation.setThrowEventDiagnostic(CLTU_ThrowEventDiagnostic
                                .getThrowEventDiagnosticByCode(eeaCltuO.getResult().getNegativeResult().getSpecific().value.intValue()));
                    }
                    else
                    {
                        // common
                        pThrowEventOperation.setDiagnostics(SLE_Diagnostics
                                .getDiagnosticsByCode(eeaCltuO.getResult().getNegativeResult().getCommon().value.intValue()));
                    }
                }
            }
            else
            {
                throw new SleApiException(HRESULT.E_FAIL);
            }
        }

        return pThrowEventOperation;
    }

    /**
     * Fills the CLTU Parameter of the Asn1 object.
     */
    private void encodeParameterV3(ICLTU_GetParameter pGetParameterOperation, CltuGetParameterV1To3 eea_o)
    {
        switch (pGetParameterOperation.getReturnedParameter())
        {
        case cltuPN_bitLockRequired:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParBitLockRequired parBitLockRequired = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParBitLockRequired();
            parBitLockRequired.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_bitLockRequired.getCode()));
            parBitLockRequired.setParameterValue(new BerInteger(pGetParameterOperation.getBitLockRequired().getCode()));
            eea_o.setParBitLockRequired (parBitLockRequired);
            break;
        }
        case cltuPN_deliveryMode:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParDeliveryMode parDeliveryMode = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParDeliveryMode();
            parDeliveryMode.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_deliveryMode.getCode()));
            parDeliveryMode.setParameterValue(new CltuDeliveryMode(pGetParameterOperation.getDeliveryMode().getCode()));
            eea_o.setParDeliveryMode(parDeliveryMode);
            break;
        }
        case cltuPN_expectedEventInvocationId:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParEventInvocationIdentification parEventInvocId = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParEventInvocationIdentification();
            parEventInvocId.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_expectedEventInvocationId.getCode()));
            parEventInvocId.setParameterValue(new EventInvocationId(pGetParameterOperation.getExpectedEventInvocationId()));
            eea_o.setParEventInvocationIdentification(parEventInvocId);
            break;
        }
        case cltuPN_expectedSlduIdentification:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParCltuIdentification parCltuId = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParCltuIdentification();
            parCltuId.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_expectedSlduIdentification.getCode()));
            parCltuId.setParameterValue(new CltuIdentification(pGetParameterOperation.getExpectedCltuId()));
            eea_o.setParCltuIdentification(parCltuId);
            break;
        }
        case cltuPN_maximumSlduLength:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParMaximumCltuLength parMaxCltuLength = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParMaximumCltuLength();
            parMaxCltuLength.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_maximumSlduLength.getCode()));
            parMaxCltuLength.setParameterValue(new BerInteger(pGetParameterOperation.getMaximumSlduLength()));
            eea_o.setParMaximumCltuLength(parMaxCltuLength);
            break;
        }
        case cltuPN_modulationFrequency:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParModulationFrequency parModFreq = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParModulationFrequency();
            parModFreq.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_modulationFrequency.getCode()));
            parModFreq.setParameterValue(new ModulationFrequency(pGetParameterOperation.getModulationFrequency()));
            eea_o.setParModulationFrequency(parModFreq);
            break;
        }
        case cltuPN_modulationIndex:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParModulationIndex parModIndex = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParModulationIndex();
            parModIndex.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_modulationIndex.getCode()));
            parModIndex.setParameterValue(new ModulationIndex(pGetParameterOperation.getModulationIndex()));
            eea_o.setParModulationIndex(parModIndex);
            break;
        }
        case cltuPN_plopInEffect:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParPlopInEffect parPlop = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParPlopInEffect();
            parPlop.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_plopInEffect.getCode()));
            parPlop.setParameterValue(new BerInteger(pGetParameterOperation.getPlopInEffect().getCode()));
            eea_o.setParPlopInEffect(parPlop);
            break;
        }
        case cltuPN_reportingCycle:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParReportingCycle parRepCycle = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParReportingCycle();
            parRepCycle.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_reportingCycle.getCode()));
            long reportingCycle = pGetParameterOperation.getReportingCycle();
            if (reportingCycle == 0)
            {
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOff(new BerNull());
                parRepCycle.setParameterValue(crc);
            }
            else
            {
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOn(new ReportingCycle(reportingCycle));
            	parRepCycle.setParameterValue(crc);
                //parRepCycle.setParameterValue(new CurrentReportingCycle(null, new ReportingCycle(reportingCycle));
            }
            eea_o.setParReportingCycle(parRepCycle);
            break;
        }
        case cltuPN_returnTimeoutPeriod:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParReturnTimeout parRtnTo = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParReturnTimeout();
            parRtnTo.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_returnTimeoutPeriod.getCode()));
            parRtnTo.setParameterValue(new TimeoutPeriod(pGetParameterOperation.getReturnTimeoutPeriod()));
            eea_o.setParReturnTimeout(parRtnTo);
            break;
        }
        case cltuPN_rfAvailableRequired:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParRfAvailableRequired parAvReq = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParRfAvailableRequired();
            parAvReq.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_rfAvailableRequired.getCode()));
            parAvReq.setParameterValue(new BerInteger(pGetParameterOperation.getRfAvailableRequired().getCode()));
            eea_o.setParRfAvailableRequired(parAvReq);
            break;
        }
        case cltuPN_subcarrierToBitRateRatio:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParSubcarrierToBitRateRatio parSubCarrToBitRateRatio = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParSubcarrierToBitRateRatio();
            parSubCarrToBitRateRatio.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_subcarrierToBitRateRatio
                    .getCode()));
            parSubCarrToBitRateRatio.setParameterValue(new SubcarrierDivisor(pGetParameterOperation.getSubcarrierToBitRateRatio()));
            eea_o.setParSubcarrierToBitRateRatio(parSubCarrToBitRateRatio);
            break;
        }
        case cltuPN_acquisitionSequenceLength:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParAcquisitionSequenceLength parAcqSeqLength = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParAcquisitionSequenceLength();
            parAcqSeqLength.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_acquisitionSequenceLength.getCode()));
            parAcqSeqLength.setParameterValue(new IntUnsignedShort(pGetParameterOperation.getAcquisitionSequenceLength()));
            eea_o.setParAcquisitionSequenceLength(parAcqSeqLength);
            break;
        }
        case cltuPN_plop1IdleSequenceLength:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParPlop1IdleSequenceLength parPlop1IdleSeqLength = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParPlop1IdleSequenceLength();
            parPlop1IdleSeqLength.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_plop1IdleSequenceLength.getCode()));
            parPlop1IdleSeqLength.setParameterValue(new IntUnsignedShort(pGetParameterOperation.getPlop1IdleSequenceLength()));
            eea_o.setParPlop1IdleSequenceLength(parPlop1IdleSeqLength);
            break;
        }
        case cltuPN_protocolAbortMode:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParProtocolAbortMode parProtAbortMode = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParProtocolAbortMode();
            parProtAbortMode.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_protocolAbortMode.getCode()));
            parProtAbortMode.setParameterValue(new BerInteger(pGetParameterOperation.getProtocolAbortMode().getCode()));
            eea_o.setParProtocolAbortMode(parProtAbortMode);
            break;
        }
        case cltuPN_notificationMode:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParNotificationMode parNotificationMode = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParNotificationMode();
            parNotificationMode.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_notificationMode.getCode()));
            parNotificationMode.setParameterValue(new BerInteger(pGetParameterOperation.getNotificationMode().getCode()));
            eea_o.setParNotificationMode(parNotificationMode);
            break;
        }
        case cltuPN_clcwGlobalVcid:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParGlobalVcid parGlobalVcid = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParGlobalVcid();
            parGlobalVcid.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_clcwGlobalVcid.getCode()));
            parGlobalVcid.setParameterValue(encodeClcwGlobalVcidV4(pGetParameterOperation.getClcwGlobalVcid()));
            eea_o.setParGlobalVcid(parGlobalVcid);
            break;
        }
        case cltuPN_clcwPhysicalChannel:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParClcwPhysicalChannel parPhysicalChannel = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParClcwPhysicalChannel();
            parPhysicalChannel.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_clcwPhysicalChannel.getCode()));
            parPhysicalChannel.setParameterValue(new BerVisibleString(pGetParameterOperation.getClcwPhysicalChannel().getCltuPhyChannel()));
            eea_o.setParClcwPhysicalChannel(parPhysicalChannel);
            break;
        }
        case cltuPN_minimumDelayTime:
        {
            ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParMinimumDelayTime parMinDelayTime = new ccsds.sle.transfer.service.cltu.structures.CltuGetParameterV1To3.ParMinimumDelayTime();
            parMinDelayTime.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_minimumDelayTime.getCode()));
            parMinDelayTime.setParameterValue(new Duration(pGetParameterOperation.getMinimumDelayTime()));
            eea_o.setParMinimumDelayTime(parMinDelayTime);
            break;
        }
        default:
        {
            break;
        }
        }
    }
    
    /**
     * This method encodes the ClcwGlobalVcId parameter of SLES V5
     * @param clcwgvcid
     * @return an object of jASN1 generated class ClcwGvcId, which will be encoded later.
     */
    private ClcwGvcId encodeClcwGlobalVcid(CLTU_ClcwGvcId clcwgvcid)
    {
    	GvcId gvcid = new GvcId();
    	ClcwGvcId eeaO = new ClcwGvcId();
    	if(clcwgvcid.getConfigType() == CLTU_ConfType.cltuCT_configured){
    		
    		CLTU_GvcId cltuGvcId = ((CLTU_ClcwGvcId)clcwgvcid).getCltuGvcId();
    		if(cltuGvcId != null){
    			
    			gvcid.setSpacecraftId( new BerInteger(cltuGvcId.getScid()));
    			gvcid.setVersionNumber(new BerInteger(cltuGvcId.getVersion()));

    			switch (cltuGvcId.getType())
    			{
    			case cltuCT_MasterChannel:
    			{
    				//eeaO.vcId = new VcId(new BerNull(), null));
    				GvcId.VcId mc = new GvcId.VcId();
    				mc.setMasterChannel(new BerNull());
    				gvcid.setVcId(mc);
    				eeaO.setCongigured(gvcid);
    				eeaO.setNotConfigured(null);
    				break;
    			}
    			case cltuCT_VirtualChannel:
    			{
    				//eeaO.vcId = new VcId(null, new VcId(gvcid.getVcid()));
    				ccsds.sle.transfer.service.cltu.structures.VcId id = new ccsds.sle.transfer.service.cltu.structures.VcId(cltuGvcId.getVcid());
    				GvcId.VcId vcid = new GvcId.VcId();
    				vcid.setVirtualChannel(id);
    				gvcid.setVcId(vcid);
    				eeaO.setCongigured(gvcid);
    				eeaO.setNotConfigured(null);
    				break;
    			}
    			default:
    			{
    				//eeaO.vcId = new VcId();
    				//eeaO.setVcId(new GvcId.VcId());
    				gvcid = null;
    				//eeaO.setCongigured(gvcid);
    				eeaO.setNotConfigured(new BerNull());
    				break;
    			}
    			}
    		}
    	}
    	else{
    		// In case it is notConfigured or invalid
    		gvcid = null;
			//eeaO.setCongigured(gvcid);
			eeaO.setNotConfigured(new BerNull());
    	}

        return eeaO;
    }
    
    /**
     * This method encodes the ClcwGlobalVcId parameter of SLES V1 .. V4
     * @param clcwgvcid
     * @return an object of jASN1 generated class GvcId, which will be encoded later.
     */
    private GvcId encodeClcwGlobalVcidV4(CLTU_ClcwGvcId clcwGvcid)
    { 
        GvcId eeaO = new GvcId(); // The output object 
        
        if(clcwGvcid.getConfigType() == CLTU_ConfType.cltuCT_configured)
        {
        	CLTU_GvcId gvcid = clcwGvcid.getCltuGvcId();
        	//eeaO.spacecraftId = new BerInteger(gvcid.getScid());
        	eeaO.setSpacecraftId( new BerInteger(gvcid.getScid()));
        	eeaO.setVersionNumber(new BerInteger(gvcid.getVersion()));
        	switch (gvcid.getType())
        	{
        	case cltuCT_MasterChannel:
        	{
        		//eeaO.vcId = new VcId(new BerNull(), null));
        		GvcId.VcId mc = new GvcId.VcId();
        		mc.setMasterChannel(new BerNull());
        		eeaO.setVcId(mc);
        		break;
        	}
        	case cltuCT_VirtualChannel:
        	{
        		//eeaO.vcId = new VcId(null, new VcId(gvcid.getVcid()));
        		ccsds.sle.transfer.service.cltu.structures.VcId id = new ccsds.sle.transfer.service.cltu.structures.VcId(gvcid.getVcid());
        		GvcId.VcId vcid = new GvcId.VcId();
        		vcid.setVirtualChannel(id);
        		eeaO.setVcId(vcid);
        		break;
        	}
        	default:
        	{
        		//eeaO.vcId = new VcId();
        		eeaO.setVcId(new GvcId.VcId());
        		break;
        	}
        	}
        }
        else
        {
        	eeaO.setVcId(new GvcId.VcId());
        }           

        return eeaO;
    }

    /**
     * Fills the CLTU Parameter of the Asn1 object.
     */
    private void encodeParameter(ICLTU_GetParameter pGetParameterOperation, CltuGetParameter eea_o)
    {
        switch (pGetParameterOperation.getReturnedParameter())
        {
        case cltuPN_bitLockRequired:
        {
            ParBitLockRequired parBitLockRequired = new ParBitLockRequired();
            parBitLockRequired.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_bitLockRequired.getCode()));
            parBitLockRequired.setParameterValue(new BerInteger(pGetParameterOperation.getBitLockRequired().getCode()));
            eea_o.setParBitLockRequired(parBitLockRequired);
            break;
        }
        case cltuPN_deliveryMode:
        {
            ParDeliveryMode parDeliveryMode = new ParDeliveryMode();
            parDeliveryMode.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_deliveryMode.getCode()));
            parDeliveryMode.setParameterValue( new CltuDeliveryMode(pGetParameterOperation.getDeliveryMode().getCode()));
            eea_o.setParDeliveryMode (parDeliveryMode);
            break;
        }
        case cltuPN_expectedEventInvocationId:
        {
            ParEventInvocationIdentification parEventInvocId = new ParEventInvocationIdentification();
            parEventInvocId.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_expectedEventInvocationId.getCode()));
            parEventInvocId.setParameterValue(new EventInvocationId(pGetParameterOperation.getExpectedEventInvocationId()));
            eea_o.setParEventInvocationIdentification(parEventInvocId);
            break;
        }
        case cltuPN_expectedSlduIdentification:
        {
            ParCltuIdentification parCltuId = new ParCltuIdentification();
            parCltuId.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_expectedSlduIdentification.getCode()));
            parCltuId.setParameterValue( new CltuIdentification(pGetParameterOperation.getExpectedCltuId()));
            eea_o.setParCltuIdentification(parCltuId);
            break;
        }
        case cltuPN_maximumSlduLength:
        {
            ParMaximumCltuLength parMaxCltuLength = new ParMaximumCltuLength();
            parMaxCltuLength.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_maximumSlduLength.getCode()));
            parMaxCltuLength.setParameterValue( new BerInteger(pGetParameterOperation.getMaximumSlduLength()));
            eea_o.setParMaximumCltuLength(parMaxCltuLength);
            break;
        }
        case cltuPN_modulationFrequency:
        {
            ParModulationFrequency parModFreq = new ParModulationFrequency();
            parModFreq.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_modulationFrequency.getCode()));
            parModFreq.setParameterValue( new ModulationFrequency(pGetParameterOperation.getModulationFrequency()));
            eea_o.setParModulationFrequency(parModFreq);
            break;
        }
        case cltuPN_modulationIndex:
        {
            ParModulationIndex parModIndex = new ParModulationIndex();
            parModIndex.setParameterName( new ParameterName(CLTU_ParameterName.cltuPN_modulationIndex.getCode()));
            parModIndex.setParameterValue( new ModulationIndex(pGetParameterOperation.getModulationIndex()));
            eea_o.setParModulationIndex(parModIndex);
            break;
        }
        case cltuPN_plopInEffect:
        {
            ParPlopInEffect parPlop = new ParPlopInEffect();
            parPlop.setParameterName( new ParameterName(CLTU_ParameterName.cltuPN_plopInEffect.getCode()));
            parPlop.setParameterValue(new BerInteger(pGetParameterOperation.getPlopInEffect().getCode()));
            eea_o.setParPlopInEffect(parPlop);
            break;
        }
        case cltuPN_reportingCycle:
        {
            ParReportingCycle parRepCycle = new ParReportingCycle();
            parRepCycle.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_reportingCycle.getCode()));
            long reportingCycle = pGetParameterOperation.getReportingCycle();
            if (reportingCycle == 0)
            {
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOff(new BerNull());
                parRepCycle.setParameterValue(crc);
            }
            else
            {
                //parRepCycle.setParameterValue(new CurrentReportingCycle(null, new ReportingCycle(reportingCycle)));
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOn(new ReportingCycle(reportingCycle));
            	parRepCycle.setParameterValue(crc);
            }
            eea_o.setParReportingCycle(parRepCycle);
            break;
        }
        case cltuPN_returnTimeoutPeriod:
        {
            ParReturnTimeout parRtnTo = new ParReturnTimeout();
            parRtnTo.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_returnTimeoutPeriod.getCode()));
            parRtnTo.setParameterValue(new TimeoutPeriod(pGetParameterOperation.getReturnTimeoutPeriod()));
            eea_o.setParReturnTimeout(parRtnTo);
            break;
        }
        case cltuPN_rfAvailableRequired:
        {
            ParRfAvailableRequired parAvReq = new ParRfAvailableRequired();
            parAvReq.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_rfAvailableRequired.getCode()));
            parAvReq.setParameterValue(new BerInteger(pGetParameterOperation.getRfAvailableRequired().getCode()));
            eea_o.setParRfAvailableRequired(parAvReq);
            break;
        }
        case cltuPN_subcarrierToBitRateRatio:
        {
            ParSubcarrierToBitRateRatio parSubCarrToBitRateRatio = new ParSubcarrierToBitRateRatio();
            parSubCarrToBitRateRatio.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_subcarrierToBitRateRatio
                    .getCode()));
            parSubCarrToBitRateRatio.setParameterValue(new SubcarrierDivisor(pGetParameterOperation.getSubcarrierToBitRateRatio()));
            eea_o.setParSubcarrierToBitRateRatio(parSubCarrToBitRateRatio);
            break;
        }
        case cltuPN_acquisitionSequenceLength:
        {
            ParAcquisitionSequenceLength parAcqSeqLength = new ParAcquisitionSequenceLength();
            parAcqSeqLength.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_acquisitionSequenceLength.getCode()));
            parAcqSeqLength.setParameterValue(new IntUnsignedShort(pGetParameterOperation.getAcquisitionSequenceLength()));
            eea_o.setParAcquisitionSequenceLength(parAcqSeqLength);
            break;
        }
        case cltuPN_plop1IdleSequenceLength:
        {
            ParPlop1IdleSequenceLength parPlop1IdleSeqLength = new ParPlop1IdleSequenceLength();
            parPlop1IdleSeqLength.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_plop1IdleSequenceLength.getCode()));
            parPlop1IdleSeqLength.setParameterValue(new IntUnsignedShort(pGetParameterOperation.getPlop1IdleSequenceLength()));
            eea_o.setParPlop1IdleSequenceLength(parPlop1IdleSeqLength);
            break;
        }
        case cltuPN_protocolAbortMode:
        {
            ParProtocolAbortMode parProtAbortMode = new ParProtocolAbortMode();
            parProtAbortMode.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_protocolAbortMode.getCode()));
            parProtAbortMode.setParameterValue(new BerInteger(pGetParameterOperation.getProtocolAbortMode().getCode()));
            eea_o.setParProtocolAbortMode(parProtAbortMode);
            break;
        }
        case cltuPN_notificationMode:
        {
            ParNotificationMode parNotificationMode = new ParNotificationMode();
            parNotificationMode.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_notificationMode.getCode()));
            parNotificationMode.setParameterValue(new BerInteger(pGetParameterOperation.getNotificationMode().getCode()));
            eea_o.setParNotificationMode(parNotificationMode);
            break;
        }
        case cltuPN_clcwGlobalVcid:
        {
            ParClcwGlobalVcId parClcwGlobalVcId = new ParClcwGlobalVcId();
            parClcwGlobalVcId.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_clcwGlobalVcid.getCode()));      
            parClcwGlobalVcId.setParameterValue(encodeClcwGlobalVcid(pGetParameterOperation.getClcwGlobalVcid()));
            eea_o.setParClcwGlobalVcId(parClcwGlobalVcId);
            break;
        }
        case cltuPN_clcwPhysicalChannel:
        {
            ParClcwPhysicalChannel parPhysicalChannel = new ParClcwPhysicalChannel();
            parPhysicalChannel.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_clcwPhysicalChannel.getCode()));
            //parPhysicalChannel.setParameterValue(new BerVisibleString(pGetParameterOperation.getClcwPhysicalChannel()));
            ClcwPhysicalChannel pch = new ClcwPhysicalChannel();
            if(pGetParameterOperation.getClcwPhysicalChannel() != null)
            {
            	if(pGetParameterOperation.getClcwPhysicalChannel().getCltuPhyChannel() != null)
            	{
            		pch.setConfigured(new BerVisibleString(pGetParameterOperation.getClcwPhysicalChannel().getCltuPhyChannel()));
            	}
            	else
            	{
            		pch.setNotConfigured(new BerNull());
            	}
            }
            else
        	{
        		pch.setNotConfigured(new BerNull());
        	}
            parPhysicalChannel.setParameterValue(pch);
            eea_o.setParClcwPhysicalChannel(parPhysicalChannel);
            break;
        }
        case cltuPN_minimumDelayTime:
        {
            ParMinimumDelayTime parMinDelayTime = new ParMinimumDelayTime();
            parMinDelayTime.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_minimumDelayTime.getCode()));
            parMinDelayTime.setParameterValue(new Duration(pGetParameterOperation.getMinimumDelayTime()));
            eea_o.setParMinimumDelayTime(parMinDelayTime);
            break;
        }
        //Added for SLES V5
        case cltuPN_minimumReportingCycle:
        {
        	ParMinReportingCycle parMinRepCycle = new ParMinReportingCycle();
        	parMinRepCycle.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_minimumReportingCycle.getCode()));
        	parMinRepCycle.setParameterValue(new IntPosShort(pGetParameterOperation.getMinimumReportingCycle()));
        	eea_o.setParMinReportingCycle(parMinRepCycle);
        	break;
        }
        default:
        {
            break;
        }
        }
    }

    /**
     * Fills the CLTU Parameter of the Asn1 object.
     */
    private void encodeParameterV4(ICLTU_GetParameter pGetParameterOperation, CltuGetParameterV4 eea_o)
    {
        switch (pGetParameterOperation.getReturnedParameter())
        {
        case cltuPN_bitLockRequired:
        {
            CltuGetParameterV4.ParBitLockRequired parBitLockRequired = new CltuGetParameterV4.ParBitLockRequired();
            parBitLockRequired.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_bitLockRequired.getCode()));
            parBitLockRequired.setParameterValue(new BerInteger(pGetParameterOperation.getBitLockRequired().getCode()));
            eea_o.setParBitLockRequired(parBitLockRequired);
            break;
        }
        case cltuPN_deliveryMode:
        {
        	CltuGetParameterV4.ParDeliveryMode parDeliveryMode = new CltuGetParameterV4.ParDeliveryMode();
            parDeliveryMode.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_deliveryMode.getCode()));
            parDeliveryMode.setParameterValue( new CltuDeliveryMode(pGetParameterOperation.getDeliveryMode().getCode()));
            eea_o.setParDeliveryMode (parDeliveryMode);
            break;
        }
        case cltuPN_expectedEventInvocationId:
        {
        	CltuGetParameterV4.ParEventInvocationIdentification parEventInvocId = new CltuGetParameterV4.ParEventInvocationIdentification();
            parEventInvocId.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_expectedEventInvocationId.getCode()));
            parEventInvocId.setParameterValue(new EventInvocationId(pGetParameterOperation.getExpectedEventInvocationId()));
            eea_o.setParEventInvocationIdentification(parEventInvocId);
            break;
        }
        case cltuPN_expectedSlduIdentification:
        {
        	CltuGetParameterV4.ParCltuIdentification parCltuId = new CltuGetParameterV4.ParCltuIdentification();
            parCltuId.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_expectedSlduIdentification.getCode()));
            parCltuId.setParameterValue( new CltuIdentification(pGetParameterOperation.getExpectedCltuId()));
            eea_o.setParCltuIdentification(parCltuId);
            break;
        }
        case cltuPN_maximumSlduLength:
        {
        	CltuGetParameterV4.ParMaximumCltuLength parMaxCltuLength = new CltuGetParameterV4.ParMaximumCltuLength();
            parMaxCltuLength.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_maximumSlduLength.getCode()));
            parMaxCltuLength.setParameterValue( new BerInteger(pGetParameterOperation.getMaximumSlduLength()));
            eea_o.setParMaximumCltuLength(parMaxCltuLength);
            break;
        }
        case cltuPN_modulationFrequency:
        {
        	CltuGetParameterV4.ParModulationFrequency parModFreq = new CltuGetParameterV4.ParModulationFrequency();
            parModFreq.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_modulationFrequency.getCode()));
            parModFreq.setParameterValue( new ModulationFrequency(pGetParameterOperation.getModulationFrequency()));
            eea_o.setParModulationFrequency(parModFreq);
            break;
        }
        case cltuPN_modulationIndex:
        {
        	CltuGetParameterV4.ParModulationIndex parModIndex = new CltuGetParameterV4.ParModulationIndex();
            parModIndex.setParameterName( new ParameterName(CLTU_ParameterName.cltuPN_modulationIndex.getCode()));
            parModIndex.setParameterValue( new ModulationIndex(pGetParameterOperation.getModulationIndex()));
            eea_o.setParModulationIndex(parModIndex);
            break;
        }
        case cltuPN_plopInEffect:
        {
        	CltuGetParameterV4.ParPlopInEffect parPlop = new CltuGetParameterV4.ParPlopInEffect();
            parPlop.setParameterName( new ParameterName(CLTU_ParameterName.cltuPN_plopInEffect.getCode()));
            parPlop.setParameterValue(new BerInteger(pGetParameterOperation.getPlopInEffect().getCode()));
            eea_o.setParPlopInEffect(parPlop);
            break;
        }
        case cltuPN_reportingCycle:
        {
        	CltuGetParameterV4.ParReportingCycle parRepCycle = new CltuGetParameterV4.ParReportingCycle();
            parRepCycle.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_reportingCycle.getCode()));
            long reportingCycle = pGetParameterOperation.getReportingCycle();
            if (reportingCycle == 0)
            {
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOff(new BerNull());
                parRepCycle.setParameterValue(crc);
            }
            else
            {
                //parRepCycle.setParameterValue(new CurrentReportingCycle(null, new ReportingCycle(reportingCycle)));
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOn(new ReportingCycle(reportingCycle));
            	parRepCycle.setParameterValue(crc);
            }
            eea_o.setParReportingCycle(parRepCycle);
            break;
        }
        case cltuPN_returnTimeoutPeriod:
        {
        	CltuGetParameterV4.ParReturnTimeout parRtnTo = new CltuGetParameterV4.ParReturnTimeout();
            parRtnTo.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_returnTimeoutPeriod.getCode()));
            parRtnTo.setParameterValue(new TimeoutPeriod(pGetParameterOperation.getReturnTimeoutPeriod()));
            eea_o.setParReturnTimeout(parRtnTo);
            break;
        }
        case cltuPN_rfAvailableRequired:
        {
        	CltuGetParameterV4.ParRfAvailableRequired parAvReq = new CltuGetParameterV4.ParRfAvailableRequired();
            parAvReq.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_rfAvailableRequired.getCode()));
            parAvReq.setParameterValue(new BerInteger(pGetParameterOperation.getRfAvailableRequired().getCode()));
            eea_o.setParRfAvailableRequired(parAvReq);
            break;
        }
        case cltuPN_subcarrierToBitRateRatio:
        {
        	CltuGetParameterV4.ParSubcarrierToBitRateRatio parSubCarrToBitRateRatio = new CltuGetParameterV4.ParSubcarrierToBitRateRatio();
            parSubCarrToBitRateRatio.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_subcarrierToBitRateRatio
                    .getCode()));
            parSubCarrToBitRateRatio.setParameterValue(new SubcarrierDivisor(pGetParameterOperation.getSubcarrierToBitRateRatio()));
            eea_o.setParSubcarrierToBitRateRatio(parSubCarrToBitRateRatio);
            break;
        }
        case cltuPN_acquisitionSequenceLength:
        {
        	CltuGetParameterV4.ParAcquisitionSequenceLength parAcqSeqLength = new CltuGetParameterV4.ParAcquisitionSequenceLength();
            parAcqSeqLength.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_acquisitionSequenceLength.getCode()));
            parAcqSeqLength.setParameterValue(new IntUnsignedShort(pGetParameterOperation.getAcquisitionSequenceLength()));
            eea_o.setParAcquisitionSequenceLength(parAcqSeqLength);
            break;
        }
        case cltuPN_plop1IdleSequenceLength:
        {
        	CltuGetParameterV4.ParPlop1IdleSequenceLength parPlop1IdleSeqLength = new CltuGetParameterV4.ParPlop1IdleSequenceLength();
            parPlop1IdleSeqLength.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_plop1IdleSequenceLength.getCode()));
            parPlop1IdleSeqLength.setParameterValue(new IntUnsignedShort(pGetParameterOperation.getPlop1IdleSequenceLength()));
            eea_o.setParPlop1IdleSequenceLength(parPlop1IdleSeqLength);
            break;
        }
        case cltuPN_protocolAbortMode:
        {
        	CltuGetParameterV4.ParProtocolAbortMode parProtAbortMode = new CltuGetParameterV4.ParProtocolAbortMode();
            parProtAbortMode.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_protocolAbortMode.getCode()));
            parProtAbortMode.setParameterValue(new BerInteger(pGetParameterOperation.getProtocolAbortMode().getCode()));
            eea_o.setParProtocolAbortMode(parProtAbortMode);
            break;
        }
        case cltuPN_notificationMode:
        {
        	CltuGetParameterV4.ParNotificationMode parNotificationMode = new CltuGetParameterV4.ParNotificationMode();
            parNotificationMode.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_notificationMode.getCode()));
            parNotificationMode.setParameterValue(new BerInteger(pGetParameterOperation.getNotificationMode().getCode()));
            eea_o.setParNotificationMode(parNotificationMode);
            break;
        }
        case cltuPN_clcwGlobalVcid:
        {
        	CltuGetParameterV4.ParGlobalVcid parGlobalVcid = new CltuGetParameterV4.ParGlobalVcid();
            parGlobalVcid.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_clcwGlobalVcid.getCode()));
            parGlobalVcid.setParameterValue(encodeClcwGlobalVcidV4(pGetParameterOperation.getClcwGlobalVcid()));
            eea_o.setParGlobalVcid(parGlobalVcid);
            break;
        }
        case cltuPN_clcwPhysicalChannel:
        {
        	CltuGetParameterV4.ParClcwPhysicalChannel parPhysicalChannel = new CltuGetParameterV4.ParClcwPhysicalChannel();
            parPhysicalChannel.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_clcwPhysicalChannel.getCode()));
            parPhysicalChannel.setParameterValue(new BerVisibleString(pGetParameterOperation.getClcwPhysicalChannel().getCltuPhyChannel()));
            eea_o.setParClcwPhysicalChannel(parPhysicalChannel);
            break;
        }
        case cltuPN_minimumDelayTime:
        {
        	CltuGetParameterV4.ParMinimumDelayTime parMinDelayTime = new CltuGetParameterV4.ParMinimumDelayTime();
            parMinDelayTime.setParameterName(new ParameterName(CLTU_ParameterName.cltuPN_minimumDelayTime.getCode()));
            parMinDelayTime.setParameterValue(new Duration(pGetParameterOperation.getMinimumDelayTime()));
            eea_o.setParMinimumDelayTime(parMinDelayTime);
            break;
        }
        default:
        {
            break;
        }
        }
    }    
    
    /**
     * Gets the parameter from the getParameterOperation and 
     * fills the parameter for the Cltu GetParameterReturn operation from the
     * object for SLES V1 .. V3.
     */
    private void decodeParameterV3(CltuGetParameterV1To3 eea_o, ICLTU_GetParameter pGetParameterOperation)
    {
        if (eea_o.getParBitLockRequired() != null)
        {
            pGetParameterOperation.setBitLockRequired(SLE_YesNo
                    .getYesNoByCode((int) eea_o.getParBitLockRequired().getParameterValue().value.intValue()));
        }
        else if (eea_o.getParDeliveryMode() != null)
        {
            SLE_DeliveryMode delMode = SLE_DeliveryMode
                    .getDelModeByCode((int) eea_o.getParDeliveryMode().getParameterValue().value.intValue());
            if (delMode == SLE_DeliveryMode.sleDM_fwdOnline)
            {
                pGetParameterOperation.setDeliveryMode();
            }
        }
        else if (eea_o.getParEventInvocationIdentification() != null)
        {
            pGetParameterOperation
                    .setExpectedEventInvocationId(eea_o.getParEventInvocationIdentification().getParameterValue().value.longValue());
        }
        else if (eea_o.getParCltuIdentification() != null)
        {
            pGetParameterOperation.setExpectedCltuId(eea_o.getParCltuIdentification().getParameterValue().value.longValue());
        }
        else if (eea_o.getParMaximumCltuLength() != null)
        {
            pGetParameterOperation.setMaximumSlduLength(eea_o.getParMaximumCltuLength().getParameterValue().value.longValue());
        }
        else if (eea_o.getParModulationFrequency() != null)
        {
            pGetParameterOperation.setModulationFrequency(eea_o.getParModulationFrequency().getParameterValue().value.longValue());
        }
        else if (eea_o.getParModulationIndex() != null)
        {
            pGetParameterOperation.setModulationIndex( eea_o.getParModulationIndex().getParameterValue().value.intValue());
        }
        else if (eea_o.getParPlopInEffect() != null)
        {
            pGetParameterOperation.setPlopInEffect(CLTU_PlopInEffect
                    .getplopInEffectByCode(eea_o.getParPlopInEffect().getParameterValue().value.intValue()));
        }
        else if (eea_o.getParReportingCycle() != null)
        {
            if (eea_o.getParReportingCycle().getParameterValue().getPeriodicReportingOn() != null)
            {
                pGetParameterOperation
                        .setReportingCycle(eea_o.getParReportingCycle().getParameterValue().getPeriodicReportingOn().value.longValue());
            }
            else
            {
                pGetParameterOperation.setReportingCycle(0);
            }
        }
        else if (eea_o.getParReturnTimeout() != null)
        {
            pGetParameterOperation.setReturnTimeoutPeriod(eea_o.getParReturnTimeout().getParameterValue().value.longValue());
        }
        else if (eea_o.getParRfAvailableRequired() != null)
        {
            pGetParameterOperation.setRfAvailableRequired(SLE_YesNo
                    .getYesNoByCode((int) eea_o.getParRfAvailableRequired().getParameterValue().value.intValue()));
        }
        else if (eea_o.getParSubcarrierToBitRateRatio() != null)
        {
            pGetParameterOperation
                    .setSubcarrierToBitRateRatio((int) eea_o.getParSubcarrierToBitRateRatio().getParameterValue().value.intValue());
        }
        else if (eea_o.getParAcquisitionSequenceLength() != null)
        {
            pGetParameterOperation
                    .setAcquisitionSequenceLength((int) eea_o.getParAcquisitionSequenceLength().getParameterValue().value.intValue());
        }
        else if (eea_o.getParPlop1IdleSequenceLength() != null)
        {
            pGetParameterOperation
                    .setPlop1IdleSequenceLength((int) eea_o.getParPlop1IdleSequenceLength().getParameterValue().value.intValue());
        }
        else if (eea_o.getParProtocolAbortMode() != null)
        {
            pGetParameterOperation.setProtocolAbortMode(CLTU_ProtocolAbortMode
                    .getProtAbportModeByCode((int) eea_o.getParProtocolAbortMode().getParameterValue().value.intValue()));
        }
        else if (eea_o.getParNotificationMode() != null)
        {
            pGetParameterOperation.setNotificationMode(CLTU_NotificationMode
                    .getNotificationModeByCode((int) eea_o.getParNotificationMode().getParameterValue().value.intValue()));
        }
        else if (eea_o.getParGlobalVcid() != null)
        {
            //pGetParameterOperation.setClcwGlobalVcid(decodeClcwGlobalVcid(eea_o.parGlobalVcid.parameterValue));
        	CLTU_GvcId gvcid = new CLTU_GvcId();
        	GvcId eeaO = eea_o.getParGlobalVcid().getParameterValue();
        	if(eeaO != null)
        	{
        		
        		gvcid.setScid((int) eeaO.getSpacecraftId().value.intValue());
        		gvcid.setVersion((int) eeaO.getVersionNumber().value.intValue());
        		if (eeaO.getVcId().getMasterChannel() != null)
        		{
        			gvcid.setType(CLTU_ChannelType.cltuCT_MasterChannel);
        			gvcid.setVcid(0);
        		}
        		else if (eeaO.getVcId().getVirtualChannel() != null)
        		{
        			gvcid.setType(CLTU_ChannelType.cltuCT_VirtualChannel);
        			gvcid.setVcid((int) eeaO.getVcId().getVirtualChannel().value.intValue());
        		}
        	}
            else
            {
                gvcid.setType(CLTU_ChannelType.cltuCT_invalid);
            }
        	// Actually, we don't need the ClcwGvcId but rather GvcId for SLES V4,
        	// But as GvcId is member of ClcwGvcId we ecapsulate and reuse the code.
        	CLTU_ClcwGvcId clcwGvcId = new CLTU_ClcwGvcId(gvcid);
        	pGetParameterOperation.setClcwGlobalVcid(clcwGvcId);
        }
        else if (eea_o.getParClcwPhysicalChannel() != null)
        {
            pGetParameterOperation
                    .setClcwPhysicalChannel(new CLTU_ClcwPhysicalChannel(
                    		                new String(eea_o.getParClcwPhysicalChannel().getParameterValue().value)));
        }
        else if (eea_o.getParMinimumDelayTime() != null)
        {
            pGetParameterOperation.setMinimumDelayTime(eea_o.getParMinimumDelayTime().getParameterValue().value.longValue());
        }
    }

    /**
     * Fills the parameter of the Cltu GetParameter return operation from the
     * object for SLES V4.
     */
    private void decodeParameterV4(CltuGetParameterV4 eea_o, ICLTU_GetParameter pGetParameterOperation)
    {
        //if (eea_o.parBitLockRequired != null)
    	if (eea_o.getParBitLockRequired()!= null)
        {
            pGetParameterOperation.setBitLockRequired(SLE_YesNo
                    .getYesNoByCode( eea_o.getParBitLockRequired().getParameterValue().value.intValue()));
        }
        else if (eea_o.getParDeliveryMode() != null)
        {
            SLE_DeliveryMode delMode = SLE_DeliveryMode
                    .getDelModeByCode((int) eea_o.getParDeliveryMode().getParameterValue().value.intValue());
            if (delMode == SLE_DeliveryMode.sleDM_fwdOnline)
            {
                pGetParameterOperation.setDeliveryMode();
            }
        }
        else if (eea_o.getParEventInvocationIdentification() != null)
        {
            pGetParameterOperation
                    .setExpectedEventInvocationId(eea_o.getParEventInvocationIdentification().getParameterValue().value.longValue());
        }
        else if (eea_o.getParCltuIdentification() != null)
        {
            pGetParameterOperation.setExpectedCltuId(eea_o.getParCltuIdentification().getParameterValue().value.longValue());
        }
        else if (eea_o.getParMaximumCltuLength() != null)
        {
            pGetParameterOperation.setMaximumSlduLength(eea_o.getParMaximumCltuLength().getParameterValue().value.longValue());
        }
        else if (eea_o.getParModulationFrequency() != null)
        {
            pGetParameterOperation.setModulationFrequency(eea_o.getParModulationFrequency().getParameterValue().value.longValue());
        }
        else if (eea_o.getParModulationIndex() != null)
        {
            pGetParameterOperation.setModulationIndex(eea_o.getParModulationIndex().getParameterValue().value.intValue());
        }
        else if (eea_o.getParPlopInEffect() != null)
        {
            pGetParameterOperation.setPlopInEffect(CLTU_PlopInEffect
                    .getplopInEffectByCode( eea_o.getParPlopInEffect().getParameterValue().value.intValue()));
        }
        else if (eea_o.getParReportingCycle() != null)
        {
            if (eea_o.getParReportingCycle().getParameterValue().getPeriodicReportingOn() != null)
            {
                pGetParameterOperation
                        .setReportingCycle(eea_o.getParReportingCycle().getParameterValue().getPeriodicReportingOn().value.longValue());
            }
            else
            {
                pGetParameterOperation.setReportingCycle(0);
            }
        }
        else if (eea_o.getParReturnTimeout() != null)
        {
            pGetParameterOperation.setReturnTimeoutPeriod(eea_o.getParReturnTimeout().getParameterValue().value.longValue());
        }
        else if (eea_o.getParRfAvailableRequired() != null)
        {
            pGetParameterOperation.setRfAvailableRequired(SLE_YesNo
                    .getYesNoByCode( eea_o.getParRfAvailableRequired().getParameterValue().value.intValue()));
        }
        else if (eea_o.getParSubcarrierToBitRateRatio() != null)
        {
            pGetParameterOperation
                    .setSubcarrierToBitRateRatio( eea_o.getParSubcarrierToBitRateRatio().getParameterValue().value.intValue());
        }
        else if (eea_o.getParAcquisitionSequenceLength() != null)
        {
            pGetParameterOperation
                    .setAcquisitionSequenceLength(eea_o.getParAcquisitionSequenceLength().getParameterValue().value.intValue());
        }
        else if (eea_o.getParPlop1IdleSequenceLength() != null)
        {
            pGetParameterOperation
                    .setPlop1IdleSequenceLength( eea_o.getParPlop1IdleSequenceLength().getParameterValue().value.intValue());
        }
        else if (eea_o.getParProtocolAbortMode() != null)
        {
            pGetParameterOperation.setProtocolAbortMode(CLTU_ProtocolAbortMode
                    .getProtAbportModeByCode( eea_o.getParProtocolAbortMode().getParameterValue().value.intValue()));
        }
        else if (eea_o.getParNotificationMode() != null)
        {
            pGetParameterOperation.setNotificationMode(CLTU_NotificationMode
                    .getNotificationModeByCode( eea_o.getParNotificationMode().getParameterValue().value.intValue()));
        }
        else if (eea_o.getParGlobalVcid() != null)
        {
            //pGetParameterOperation.setClcwGlobalVcid(decodeClcwGlobalVcid(eea_o.parGlobalVcid.parameterValue));
        	CLTU_GvcId gvcid = new CLTU_GvcId();
        	GvcId eeaO = eea_o.getParGlobalVcid().getParameterValue();
        	if(eeaO != null)
        	{
        		
        		gvcid.setScid((int) eeaO.getSpacecraftId().value.intValue());
        		gvcid.setVersion((int) eeaO.getVersionNumber().value.intValue());
        		if (eeaO.getVcId().getMasterChannel() != null)
        		{
        			gvcid.setType(CLTU_ChannelType.cltuCT_MasterChannel);
        			gvcid.setVcid(0);
        		}
        		else if (eeaO.getVcId().getVirtualChannel() != null)
        		{
        			gvcid.setType(CLTU_ChannelType.cltuCT_VirtualChannel);
        			gvcid.setVcid((int) eeaO.getVcId().getVirtualChannel().value.intValue());
        		}
        	}
            else
            {
                gvcid.setType(CLTU_ChannelType.cltuCT_invalid);
            }
        	// Actually, we don't need the ClcwGvcId but rather GvcId for SLES V4,
        	// But as GvcId is member of ClcwGvcId we ecapsulate and reuse the code.
        	CLTU_ClcwGvcId clcwGvcId = new CLTU_ClcwGvcId(gvcid);
        	pGetParameterOperation.setClcwGlobalVcid(clcwGvcId);
        }
        else if (eea_o.getParClcwPhysicalChannel() != null)
        {
            pGetParameterOperation
                    .setClcwPhysicalChannel(new CLTU_ClcwPhysicalChannel(
                    		                new String(eea_o.getParClcwPhysicalChannel().getParameterValue().value)));
        }
        else if (eea_o.getParMinimumDelayTime() != null)
        {
            pGetParameterOperation.setMinimumDelayTime(eea_o.getParMinimumDelayTime().getParameterValue().value.longValue());
        }
    }

    
    /**
     * Fills the parameter of the Cltu GetParameter return operation from the
     * object for SLES version >=5.
     */
    private void decodeParameter(CltuGetParameter eea_o, ICLTU_GetParameter pGetParameterOperation)
    {
    	if (eea_o.getParBitLockRequired()!= null)
        {
            pGetParameterOperation.setBitLockRequired(SLE_YesNo
                    .getYesNoByCode( eea_o.getParBitLockRequired().getParameterValue().value.intValue()));
        }
        else if (eea_o.getParDeliveryMode() != null)
        {
            SLE_DeliveryMode delMode = SLE_DeliveryMode
                    .getDelModeByCode((int) eea_o.getParDeliveryMode().getParameterValue().value.intValue());
            if (delMode == SLE_DeliveryMode.sleDM_fwdOnline)
            {
                pGetParameterOperation.setDeliveryMode();
            }
        }
        else if (eea_o.getParEventInvocationIdentification() != null)
        {
            pGetParameterOperation
                    .setExpectedEventInvocationId(eea_o.getParEventInvocationIdentification().getParameterValue().value.longValue());
        }
        else if (eea_o.getParCltuIdentification() != null)
        {
            pGetParameterOperation.setExpectedCltuId(eea_o.getParCltuIdentification().getParameterValue().value.longValue());
        }
        else if (eea_o.getParMaximumCltuLength() != null)
        {
            pGetParameterOperation.setMaximumSlduLength(eea_o.getParMaximumCltuLength().getParameterValue().value.longValue());
        }
        else if (eea_o.getParModulationFrequency() != null)
        {
            pGetParameterOperation.setModulationFrequency(eea_o.getParModulationFrequency().getParameterValue().value.longValue());
        }
        else if (eea_o.getParModulationIndex() != null)
        {
            pGetParameterOperation.setModulationIndex(eea_o.getParModulationIndex().getParameterValue().value.intValue());
        }
        else if (eea_o.getParPlopInEffect() != null)
        {
            pGetParameterOperation.setPlopInEffect(CLTU_PlopInEffect
                    .getplopInEffectByCode( eea_o.getParPlopInEffect().getParameterValue().value.intValue()));
        }
        else if (eea_o.getParReportingCycle() != null)
        {
            if (eea_o.getParReportingCycle().getParameterValue().getPeriodicReportingOn() != null)
            {
                pGetParameterOperation
                        .setReportingCycle(eea_o.getParReportingCycle().getParameterValue().getPeriodicReportingOn().value.longValue());
            }
            else
            {
                pGetParameterOperation.setReportingCycle(0);
            }
        }
        else if (eea_o.getParReturnTimeout() != null)
        {
            pGetParameterOperation.setReturnTimeoutPeriod(eea_o.getParReturnTimeout().getParameterValue().value.longValue());
        }
        else if (eea_o.getParRfAvailableRequired() != null)
        {
            pGetParameterOperation.setRfAvailableRequired(SLE_YesNo
                    .getYesNoByCode( eea_o.getParRfAvailableRequired().getParameterValue().value.intValue()));
        }
        else if (eea_o.getParSubcarrierToBitRateRatio() != null)
        {
            pGetParameterOperation
                    .setSubcarrierToBitRateRatio( eea_o.getParSubcarrierToBitRateRatio().getParameterValue().value.intValue());
        }
        else if (eea_o.getParAcquisitionSequenceLength() != null)
        {
            pGetParameterOperation
                    .setAcquisitionSequenceLength(eea_o.getParAcquisitionSequenceLength().getParameterValue().value.intValue());
        }
        else if (eea_o.getParPlop1IdleSequenceLength() != null)
        {
            pGetParameterOperation
                    .setPlop1IdleSequenceLength( eea_o.getParPlop1IdleSequenceLength().getParameterValue().value.intValue());
        }
        else if (eea_o.getParProtocolAbortMode() != null)
        {
            pGetParameterOperation.setProtocolAbortMode(CLTU_ProtocolAbortMode
                    .getProtAbportModeByCode( eea_o.getParProtocolAbortMode().getParameterValue().value.intValue()));
        }
        else if (eea_o.getParNotificationMode() != null)
        {
            pGetParameterOperation.setNotificationMode(CLTU_NotificationMode
                    .getNotificationModeByCode( eea_o.getParNotificationMode().getParameterValue().value.intValue()));
        }
        //else if (eea_o.getParGlobalVcid() != null)
        else if (eea_o.getParClcwGlobalVcId() != null)
        {
        	ClcwGvcId eeaO = eea_o.getParClcwGlobalVcId().getParameterValue();        	
        	CLTU_ClcwGvcId clcwgvcid = null;
        	CLTU_GvcId gvcid = new CLTU_GvcId();
        	
            if(eeaO != null)
            {
            	if(eeaO.getCongigured() != null){
            		if (eeaO.getCongigured().getVcId().getMasterChannel() != null)
            		{
            			gvcid.setType(CLTU_ChannelType.cltuCT_MasterChannel);
            			gvcid.setVcid(0);
            			gvcid.setScid(eeaO.getCongigured().getSpacecraftId().value.intValue());
            			gvcid.setVersion(eeaO.getCongigured().getVersionNumber().value.intValue());
            			clcwgvcid = new CLTU_ClcwGvcId(gvcid, CLTU_ConfType.cltuCT_configured);

            		}
            		else if (eeaO.getCongigured().getVcId().getVirtualChannel() != null)
            		{
            			gvcid.setType(CLTU_ChannelType.cltuCT_VirtualChannel);
            			gvcid.setVcid((int) eeaO.getCongigured().getVcId().getVirtualChannel().value.intValue());
            			gvcid.setScid(eeaO.getCongigured().getSpacecraftId().value.intValue());
            			gvcid.setVersion(eeaO.getCongigured().getVersionNumber().value.intValue());
            			clcwgvcid = new CLTU_ClcwGvcId(gvcid, CLTU_ConfType.cltuCT_configured);
            		}
            		else
            		{
            			// Set to notConfigured
            			//gvcid.setType(CLTU_ChannelType.cltuCT_invalid);
            			clcwgvcid = new CLTU_ClcwGvcId(null, CLTU_ConfType.cltuCT_notConfigured);
            		}
            	}
            	else{
            		// Set to notConfigured
            		clcwgvcid = new CLTU_ClcwGvcId(null, CLTU_ConfType.cltuCT_notConfigured);
            	}
            }
            else
            {
            	// Set to invalid
            	clcwgvcid = new CLTU_ClcwGvcId(null, CLTU_ConfType.cltuCT_invalid);
            }     	
        	pGetParameterOperation.setClcwGlobalVcid(clcwgvcid);
        }
        else if (eea_o.getParClcwPhysicalChannel() != null)
        {
        	if(eea_o.getParClcwPhysicalChannel().getParameterValue() != null)
        	{
        		if(eea_o.getParClcwPhysicalChannel().getParameterValue().getConfigured() != null)
        		{
        			pGetParameterOperation
                    .setClcwPhysicalChannel(new CLTU_ClcwPhysicalChannel(
                    		                new String(eea_o.getParClcwPhysicalChannel().getParameterValue().getConfigured().value)));
        		}
        		else
        		{
        			// Set to NotConfigured
        			pGetParameterOperation.setClcwPhysicalChannel(new CLTU_ClcwPhysicalChannel());
        		}
        	}
        	else
    		{
        		// Set to NotConfigured
    			pGetParameterOperation.setClcwPhysicalChannel(new CLTU_ClcwPhysicalChannel());
    		}
        }
        else if (eea_o.getParMinimumDelayTime() != null)
        {
            pGetParameterOperation.setMinimumDelayTime(eea_o.getParMinimumDelayTime().getParameterValue().value.longValue());
        }
    	// Added for SLES V5
        else if (eea_o.getParMinReportingCycle() != null)
        {
        	pGetParameterOperation.setMinimumReportingCycle(eea_o.getParMinReportingCycle().getParameterValue().value.longValue());
        }
    }

    /**
     * Fills the CLTU START invoke operation from the object. S_OK The CLTU
     * Start operation has been decoded. E_FAIL Unable to decode the CLTU Start
     * operation.
     * 
     * @throws SleApiException
     */
    private void decodeStartInvokeOp(CltuStartInvocationV1Pdu eeaCltuO, ICLTU_Start pStartOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaCltuO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pStartOperation.putInvokerCredentials(pCredentials);
        }

        // the invoke id
        pStartOperation.setInvokeId( eeaCltuO.getInvokeId().value.intValue());

        // the first cltu identification
        if (eeaCltuO.getFirstCltuIdentification().getCltuProcessed() != null)
        {
            pStartOperation.setFirstCltuId(eeaCltuO.getFirstCltuIdentification().getCltuProcessed().value.longValue());
        }
    }

    /**
     * Fills the object used for the encoding of Cltu Start invoke operation.
     * CodesS_OK The CLTU Start operation has been encoded. E_FAIL Unable to
     * encode the CLTU Start operation.
     * 
     * @throws SleApiException
     */
    private void encodeStartInvokeOp(ICLTU_Start pStartOperation, CltuStartInvocationV1Pdu eeaCltuO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pStartOperation.getInvokerCredentials();
        eeaCltuO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaCltuO.setInvokeId( new InvokeId(pStartOperation.getInvokeId()));

        // the first cltu identification
        if (pStartOperation.getFirstCltuIdUsed())
        {
            ConditionalCltuIdentificationV1 cltuId = new ConditionalCltuIdentificationV1();
            cltuId.setCltuProcessed (new CltuIdentification(pStartOperation.getFirstCltuId()));
            eeaCltuO.setFirstCltuIdentification (cltuId);
        }
        else
        {
        	ConditionalCltuIdentificationV1 cltuId = new ConditionalCltuIdentificationV1();
        	cltuId.setNoCltuProcessed(new BerNull());
            eeaCltuO.setFirstCltuIdentification(cltuId);
        }
    }

}
