/**
 * @(#) EE_APIPX_Asn1RcfTranslator.java
 */

package esa.sle.impl.api.apipx.pxdel;

import isp1.rcf.pdus.RcfGetParameterInvocationPdu;
import isp1.rcf.pdus.RcfGetParameterReturnPdu;
import isp1.rcf.pdus.RcfGetParameterReturnPduV2To4;
import isp1.rcf.pdus.RcfGetParameterReturnV1Pdu;
import isp1.rcf.pdus.RcfScheduleStatusReportInvocationPdu;
import isp1.rcf.pdus.RcfScheduleStatusReportReturnPdu;
import isp1.rcf.pdus.RcfStartInvocationPdu;
import isp1.rcf.pdus.RcfStartReturnPdu;
import isp1.rcf.pdus.RcfStatusReportInvocationPdu;
import isp1.rcf.pdus.RcfStatusReportInvocationV1Pdu;
import isp1.rcf.pdus.RcfStopInvocationPdu;
import isp1.rcf.pdus.RcfStopReturnPdu;
import isp1.rcf.pdus.RcfTransferBufferPdu;
import isp1.sle.bind.pdus.SleBindInvocationPdu;
import isp1.sle.bind.pdus.SleBindReturnPdu;
import isp1.sle.bind.pdus.SleUnbindInvocationPdu;
import isp1.sle.bind.pdus.SleUnbindReturnPdu;

import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.beanit.jasn1.ber.ReverseByteArrayOutputStream ;
//import com.beanit.jasn1.ber.BerIdentifier;
import com.beanit.jasn1.ber.BerTag;
import com.beanit.jasn1.ber.types.BerInteger;
import com.beanit.jasn1.ber.types.BerNull;
import com.beanit.jasn1.ber.types.BerObjectIdentifier;
//import com.beanit.jasn1.ber.types.BerOctetString;
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
import ccsds.sle.api.isrv.iraf.types.RAF_ParameterName;
import ccsds.sle.api.isrv.ircf.IRCF_GetParameter;
import ccsds.sle.api.isrv.ircf.IRCF_Start;
import ccsds.sle.api.isrv.ircf.IRCF_StatusReport;
import ccsds.sle.api.isrv.ircf.IRCF_SyncNotify;
import ccsds.sle.api.isrv.ircf.IRCF_TransferData;
import ccsds.sle.api.isrv.ircf.types.RCF_AntennaIdFormat;
import ccsds.sle.api.isrv.ircf.types.RCF_ChannelType;
import ccsds.sle.api.isrv.ircf.types.RCF_DeliveryMode;
import ccsds.sle.api.isrv.ircf.types.RCF_GetParameterDiagnostic;
import ccsds.sle.api.isrv.ircf.types.RCF_Gvcid;
import ccsds.sle.api.isrv.ircf.types.RCF_LockStatus;
import ccsds.sle.api.isrv.ircf.types.RCF_ParameterName;
import ccsds.sle.api.isrv.ircf.types.RCF_ProductionStatus;
import ccsds.sle.api.isrv.ircf.types.RCF_StartDiagnostic;
import ccsds.sle.transfer.service.common.pdus.ReportingCycle;
import ccsds.sle.transfer.service.common.types.Diagnostics;
import ccsds.sle.transfer.service.common.types.IntPosShort;
import ccsds.sle.transfer.service.common.types.IntUnsignedLong;
import ccsds.sle.transfer.service.common.types.InvokeId;
import ccsds.sle.transfer.service.common.types.ParameterName;
import ccsds.sle.transfer.service.common.types.SpaceLinkDataUnit;
import ccsds.sle.transfer.service.rcf.outgoing.pdus.FrameOrNotification;
import ccsds.sle.transfer.service.rcf.outgoing.pdus.RcfGetParameterReturnV2To4;
import ccsds.sle.transfer.service.rcf.outgoing.pdus.RcfStartReturn.Result;
import ccsds.sle.transfer.service.rcf.outgoing.pdus.RcfSyncNotifyInvocation;
import ccsds.sle.transfer.service.rcf.outgoing.pdus.RcfTransferDataInvocation;
import ccsds.sle.transfer.service.rcf.outgoing.pdus.RcfGetParameterReturn;
import ccsds.sle.transfer.service.rcf.structures.AntennaId;
import ccsds.sle.transfer.service.rcf.structures.CarrierLockStatus;
import ccsds.sle.transfer.service.rcf.structures.CurrentReportingCycle;
import ccsds.sle.transfer.service.rcf.structures.DiagnosticRcfGet;
import ccsds.sle.transfer.service.rcf.structures.DiagnosticRcfStart;
import ccsds.sle.transfer.service.rcf.structures.FrameSyncLockStatus;
import ccsds.sle.transfer.service.rcf.structures.GvcId;
import ccsds.sle.transfer.service.rcf.structures.GvcId.VcId;
import ccsds.sle.transfer.service.rcf.structures.GvcIdSet;
import ccsds.sle.transfer.service.rcf.structures.GvcIdSetV1To4;
import ccsds.sle.transfer.service.rcf.structures.LockStatus;
import ccsds.sle.transfer.service.rcf.structures.LockStatusReport;
import ccsds.sle.transfer.service.rcf.structures.MasterChannelComposition;
import ccsds.sle.transfer.service.rcf.structures.MasterChannelComposition.McOrVcList;
import ccsds.sle.transfer.service.rcf.structures.MasterChannelComposition.McOrVcList.VcList;
import ccsds.sle.transfer.service.rcf.structures.MasterChannelCompositionV1To4;
import ccsds.sle.transfer.service.rcf.structures.Notification;
import ccsds.sle.transfer.service.rcf.structures.RcfDeliveryMode;
import ccsds.sle.transfer.service.rcf.structures.RcfGetParameter;
import ccsds.sle.transfer.service.rcf.structures.RcfGetParameterV1;
import ccsds.sle.transfer.service.rcf.structures.RcfGetParameterV1.ParBufferSize;
import ccsds.sle.transfer.service.rcf.structures.RcfGetParameterV1.ParDeliveryMode;
import ccsds.sle.transfer.service.rcf.structures.RcfGetParameterV1.ParLatencyLimit;
import ccsds.sle.transfer.service.rcf.structures.RcfGetParameterV1.ParLatencyLimit.ParameterValue;
import ccsds.sle.transfer.service.rcf.structures.RcfGetParameterV1.ParPermittedGvcidSet;
import ccsds.sle.transfer.service.rcf.structures.RcfGetParameterV1.ParReportingCycle;
import ccsds.sle.transfer.service.rcf.structures.RcfGetParameterV1.ParReqGvcId;
import ccsds.sle.transfer.service.rcf.structures.RcfGetParameterV1.ParReturnTimeout;
import ccsds.sle.transfer.service.rcf.structures.RcfGetParameterV2To4;
import ccsds.sle.transfer.service.rcf.structures.RcfParameterName;
import ccsds.sle.transfer.service.rcf.structures.RcfProductionStatus;
import ccsds.sle.transfer.service.rcf.structures.RequestedGvcId;
import ccsds.sle.transfer.service.rcf.structures.SymbolLockStatus;
import ccsds.sle.transfer.service.rcf.structures.TimeoutPeriod;
import ccsds.sle.transfer.service.rocf.structures.RequestedGvcIdV1To4;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class encodes and decodes RCF PDU's. When decoding, the decoded RCF
 * operation is instantiated.
 */
public class EE_APIPX_Asn1RcfTranslator extends EE_APIPX_Asn1SleTranslator
{
    /**
     * Constructor of the class which takes the ASNSDK context object as
     * parameter.
     */
    public EE_APIPX_Asn1RcfTranslator(ISLE_OperationFactory pOpFactory,
                                      ISLE_UtilFactory pUtilFactory,
                                      EE_APIPX_PDUTranslator pdutranslator,
                                      int sleVersionNumber)
    {
        super(pOpFactory, pUtilFactory, pdutranslator, sleVersionNumber);
        this.serviceType = SLE_ApplicationIdentifier.sleAI_rtnChFrames;
    }

    /**
     * Allocates and fills the object used for the encoding of Rcf Operation for
     * version 1 PDUs. S_OK The RCF operation has been encoded. E_FAIL Unable to
     * encode the RCF operation.
     * 
     * @throws SleApiException
     * @throws IOException
     */
    public byte[] encodeRcfOp(ISLE_Operation pRcfOperation, boolean isInvoke) throws SleApiException, IOException
    {
        ReverseByteArrayOutputStream  berBAOStream = new ReverseByteArrayOutputStream (10, true);

        switch (pRcfOperation.getOperationType())
        {
        case sleOT_bind:
        {
            if (isInvoke)
            {
                SleBindInvocationPdu obj = new SleBindInvocationPdu();
                encodeBindInvokeOp(pRcfOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                SleBindReturnPdu obj = new SleBindReturnPdu();
                encodeBindReturnOp(pRcfOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_unbind:
        {
            if (isInvoke)
            {
                SleUnbindInvocationPdu obj = new SleUnbindInvocationPdu();
                encodeUnbindInvokeOp(pRcfOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                SleUnbindReturnPdu obj = new SleUnbindReturnPdu();
                encodeUnbindReturnOp(pRcfOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_stop:
        {
            if (isInvoke)
            {
                RcfStopInvocationPdu obj = new RcfStopInvocationPdu();
                encodeStopInvokeOp(pRcfOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                RcfStopReturnPdu obj = new RcfStopReturnPdu();
                encodeStopReturnOp(pRcfOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_scheduleStatusReport:
        {
            if (isInvoke)
            {
                RcfScheduleStatusReportInvocationPdu obj = new RcfScheduleStatusReportInvocationPdu();
                encodeScheduleSRInvokeOp(pRcfOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                RcfScheduleStatusReportReturnPdu obj = new RcfScheduleStatusReportReturnPdu();
                encodeScheduleSRReturnOp(pRcfOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_start:
        {
            IRCF_Start pOp = null;
            pOp = pRcfOperation.queryInterface(IRCF_Start.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    RcfStartInvocationPdu obj = new RcfStartInvocationPdu();
                    encodeStartInvokeOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
                else
                {
                    RcfStartReturnPdu obj = new RcfStartReturnPdu();
                    encodeStartReturnOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
            }

            break;
        }
        case sleOT_getParameter:
        {
            IRCF_GetParameter pOp = null;
            pOp = pRcfOperation.queryInterface(IRCF_GetParameter.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    RcfGetParameterInvocationPdu obj = new RcfGetParameterInvocationPdu();
                    encodeGetParameterInvokeOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
                else
                {
                    if (this.sleVersionNumber == 1)
                    {
                        RcfGetParameterReturnV1Pdu obj = new RcfGetParameterReturnV1Pdu();
                        encodeGetParameterReturnOp(pOp, obj);
                        obj.encode(berBAOStream, true);
                    }
                    else if (this.sleVersionNumber <=4)
                    {
                    	RcfGetParameterReturnPduV2To4 obj = new RcfGetParameterReturnPduV2To4();
                        encodeGetParameterReturnOpV2To4(pOp, obj);
                        obj.encode(berBAOStream, true);
                    }
                    else
                    {
                    	RcfGetParameterReturnPdu obj = new RcfGetParameterReturnPdu();
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
            IRCF_StatusReport pOp = null;
            pOp = pRcfOperation.queryInterface(IRCF_StatusReport.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    if (this.sleVersionNumber == 1)
                    {
                        RcfStatusReportInvocationV1Pdu obj = new RcfStatusReportInvocationV1Pdu();
                        encodeStatusReportOp(pOp, obj);
                        obj.encode(berBAOStream, true);
                    }
                    else
                    {
                        RcfStatusReportInvocationPdu obj = new RcfStatusReportInvocationPdu();
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
            pOp = pRcfOperation.queryInterface(ISLE_TransferBuffer.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    RcfTransferBufferPdu obj = new RcfTransferBufferPdu();
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
     * Instantiates a new RCF operation from the version 1 object given as
     * parameter, and releases the object. S_OK A new RCF operation has been
     * Instantiated. E_FAIL Unable to instantiate a RCF operation.
     * 
     * @throws IOException
     * @throws SleApiException
     */
    public ISLE_Operation decodeRcfOp(byte[] buffer, EE_Reference<Boolean> isInvoke) throws IOException,
                                                                                    SleApiException
    {
        InputStream is = new ByteArrayInputStream(buffer);
        ISLE_Operation pOperation = null;

        BerTag identifier = new BerTag();
        identifier.decode(is);
        if (identifier.equals(SleBindInvocationPdu.tag))
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
        else if (identifier.equals(SleBindReturnPdu.tag))
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
        else if (identifier.equals(SleUnbindInvocationPdu.tag))
        {
            SleUnbindInvocationPdu obj = new SleUnbindInvocationPdu();
            obj.decode(is, false);
            pOperation = decodeUnbindInvokeOp(obj);
            isInvoke.setReference(new Boolean(true));
        }
        else if (identifier.equals(SleUnbindReturnPdu.tag))
        {
            SleUnbindReturnPdu obj = new SleUnbindReturnPdu();
            obj.decode(is, false);
            pOperation = decodeUnbindReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
        }
        else if (identifier.equals(RcfStartInvocationPdu.tag))
        {
            IRCF_Start pOp = null;
            SLE_OpType opTye = SLE_OpType.sleOT_start;
            pOp = this.operationFactory.createOperation(IRCF_Start.class,
                                                        opTye,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                RcfStartInvocationPdu obj = new RcfStartInvocationPdu();
                obj.decode(is, false);
                decodeStartInvokeOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (identifier.equals(RcfStartReturnPdu.tag))
        {
            IRCF_Start pOp = null;
            RcfStartReturnPdu obj = new RcfStartReturnPdu();
            obj.decode(is, false);
            pOp = decodeStartReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
            if (pOp != null)
            {
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (identifier.equals(RcfStopInvocationPdu.tag))
        {
            RcfStopInvocationPdu obj = new RcfStopInvocationPdu();
            obj.decode(is, false);
            pOperation = decodeStopInvokeOp(obj);
            isInvoke.setReference(new Boolean(true));
        }
        else if (identifier.equals(RcfStopReturnPdu.tag))
        {
            RcfStopReturnPdu obj = new RcfStopReturnPdu();
            obj.decode(is, false);
            pOperation = decodeStopReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
        }
        else if (identifier.equals(RcfScheduleStatusReportInvocationPdu.tag))
        {
            RcfScheduleStatusReportInvocationPdu obj = new RcfScheduleStatusReportInvocationPdu();
            obj.decode(is, false);
            pOperation = decodeScheduleSRInvokeOp(obj);
            isInvoke.setReference(new Boolean(true));
        }
        else if (identifier.equals(RcfScheduleStatusReportReturnPdu.tag))
        {
            RcfScheduleStatusReportReturnPdu obj = new RcfScheduleStatusReportReturnPdu();
            obj.decode(is, false);
            pOperation = decodeScheduleSRReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
        }
        else if (identifier.equals(RcfGetParameterInvocationPdu.tag))
        {
            IRCF_GetParameter pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_getParameter;
            pOp = this.operationFactory.createOperation(IRCF_GetParameter.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                RcfGetParameterInvocationPdu obj = new RcfGetParameterInvocationPdu();
                obj.decode(is, false);
                decodeGetParameterInvokeOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (identifier.equals(RcfGetParameterReturnPdu.tag))
        {
            IRCF_GetParameter pOp = null;
            if (this.sleVersionNumber == 1)
            {
                RcfGetParameterReturnV1Pdu obj = new RcfGetParameterReturnV1Pdu();
                obj.decode(is, false);
                pOp = decodeGetParameterReturnOp(obj);
            }
            else if (this.sleVersionNumber <= 4)
            {
                RcfGetParameterReturnPduV2To4 obj = new RcfGetParameterReturnPduV2To4();
                obj.decode(is, false);
                pOp = decodeGetParameterReturnOpV2To4(obj);
            }
            else
            {
                RcfGetParameterReturnPdu obj = new RcfGetParameterReturnPdu();
                obj.decode(is, false);
                pOp = decodeGetParameterReturnOp(obj);
            }

            isInvoke.setReference(new Boolean(false));
            if (pOp != null)
            {
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (identifier.equals(RcfTransferBufferPdu.tag))
        {
            ISLE_TransferBuffer pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_transferBuffer;
            pOp = this.operationFactory.createOperation(ISLE_TransferBuffer.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                RcfTransferBufferPdu obj = new RcfTransferBufferPdu();
                obj.decode(is, false);
                decodeTransferBufferOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (identifier.equals(RcfStatusReportInvocationPdu.tag))
        {
            IRCF_StatusReport pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_statusReport;
            pOp = this.operationFactory.createOperation(IRCF_StatusReport.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                if (this.sleVersionNumber == 1)
                {
                    RcfStatusReportInvocationV1Pdu obj = new RcfStatusReportInvocationV1Pdu();
                    obj.decode(is, false);
                    decodeStatusReportOp(obj, pOp);
                }
                else
                {
                    RcfStatusReportInvocationPdu obj = new RcfStatusReportInvocationPdu();
                    obj.decode(is, false);
                    decodeStatusReportOp(obj, pOp);
                }

                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else
        {
            throw new SleApiException(HRESULT.E_FAIL, "Unknown operation identifier");
        }

        return pOperation;
    }

    /**
     * Fills the object used for the encoding of Rcf Start invoke operation.
     * S_OK The RCF Start operation has been encoded. E_FAIL Unable to encode
     * the RCF Start operation.
     * 
     * @throws SleApiException
     */
    private void encodeStartInvokeOp(IRCF_Start pStartOperation, RcfStartInvocationPdu eeaRcfO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pStartOperation.getInvokerCredentials();
        eeaRcfO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaRcfO.setInvokeId(new InvokeId(pStartOperation.getInvokeId()));

        // the start time
        ISLE_Time pTime = null;
        pTime = pStartOperation.getStartTime();
        eeaRcfO.setStartTime(encodeConditionalTime(pTime));

        // the stop time
        pTime = pStartOperation.getStopTime();
        eeaRcfO.setStopTime(encodeConditionalTime(pTime));

        // the gvcid
        RCF_Gvcid pGvcId = null;
        pGvcId = pStartOperation.getGvcid();
        eeaRcfO.setRequestedGvcId(encodeGvcid(pGvcId));
    }

    /**
     * Fills the RCF START invoke operation from the object. S_OK The RCF Start
     * operation has been decoded. E_FAIL Unable to decode the RCF Start
     * operation.
     * 
     * @throws SleApiException
     */
    private void decodeStartInvokeOp(RcfStartInvocationPdu eeaRcfO, IRCF_Start pStartOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaRcfO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pStartOperation.putInvokerCredentials(pCredentials);
        }

        // the invoker id
        pStartOperation.setInvokeId((int) eeaRcfO.getInvokeId().value.intValue());

        // the start time
        ISLE_Time pTime = null;
        pTime = decodeConditionalTime(eeaRcfO.getStartTime());

        if (pTime != null)
        {
            pStartOperation.putStartTime(pTime);
        }

        // the stop time
        pTime = null;
        pTime = decodeConditionalTime(eeaRcfO.getStopTime());

        if (pTime != null)
        {
            pStartOperation.putStopTime(pTime);
        }

        // the gvcid
        RCF_Gvcid pGvcId = decodeGvcid(eeaRcfO.getRequestedGvcId());
        pStartOperation.putGvcid(pGvcId);
    }

    /**
     * Fills the object used for the encoding of Rcf Start return operation.
     * S_OK The RCF Start operation has been encoded. E_FAIL Unable to encode
     * the RCF Start operation.
     * 
     * @throws SleApiException
     */
    private void encodeStartReturnOp(IRCF_Start pStartOperation, RcfStartReturnPdu eeaRcfO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pStartOperation.getPerformerCredentials();
        eeaRcfO.setPerformerCredentials(encodeCredentials(pCredentials));

        // the invoker id
        eeaRcfO.setInvokeId(new InvokeId(pStartOperation.getInvokeId()));

        // the result
        if (pStartOperation.getResult() == SLE_Result.sleRES_positive)
        {
            Result posResult = new Result();
            posResult.setPositiveResult(new BerNull());
            eeaRcfO.setResult (posResult);
        }
        else
        {
            Result negResult = new Result();

            if (pStartOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                DiagnosticRcfStart repSpecific = new DiagnosticRcfStart();

                switch (pStartOperation.getStartDiagnostic())
                {
                case rcfSD_outOfService:
                {
                    repSpecific.setSpecific(new BerInteger(RCF_StartDiagnostic.rcfSD_outOfService.getCode()));
                    break;
                }
                case rcfSD_unableToComply:
                {
                    repSpecific.setSpecific( new BerInteger(RCF_StartDiagnostic.rcfSD_unableToComply.getCode()));
                    break;
                }
                case rcfSD_invalidStartTime:
                {
                    repSpecific.setSpecific( new BerInteger(RCF_StartDiagnostic.rcfSD_invalidStartTime.getCode()));
                    break;
                }
                case rcfSD_invalidStopTime:
                {
                    repSpecific.setSpecific(new BerInteger(RCF_StartDiagnostic.rcfSD_invalidStopTime.getCode()));
                    break;
                }
                case rcfSD_missingTimeValue:
                {
                    repSpecific.setSpecific(new BerInteger(RCF_StartDiagnostic.rcfSD_missingTimeValue.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific( new BerInteger(RCF_StartDiagnostic.rcfSD_invalid.getCode()));
                    break;
                }
                }

                negResult.setNegativeResult(repSpecific);
            }
            else if (pStartOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_commonDiagnostics)
            {
                // common diagnostic
                DiagnosticRcfStart repCommon = new DiagnosticRcfStart();
                repCommon.setCommon (new Diagnostics(pStartOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }
            else
            {
            	negResult.setNegativeResult(new DiagnosticRcfStart());
            }

            eeaRcfO.setResult(negResult);
        }
    }

    /**
     * Fills the RCF START return operation from the object. S_OK The RCF Start
     * operation has been decoded. E_FAIL Unable to decode the RCF Start
     * operation.
     * 
     * @throws SleApiException
     */
    private IRCF_Start decodeStartReturnOp(RcfStartReturnPdu eeaRcfO) throws SleApiException
    {
        IRCF_Start pStartOperation = null;
        ISLE_Operation pOperation = null;

        pOperation = this.pduTranslator.getReturnOp(eeaRcfO.getInvokeId(), SLE_OpType.sleOT_start);
        if (pOperation != null)
        {
            pStartOperation = pOperation.queryInterface(IRCF_Start.class);
            if (pStartOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaRcfO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pStartOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pStartOperation.setInvokeId((int) eeaRcfO.getInvokeId().value.intValue());

                // the result
                if (eeaRcfO.getResult().getPositiveResult() != null)
                {
                    pStartOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eeaRcfO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        int specValue = eeaRcfO.getResult().getNegativeResult().getSpecific().value.intValue();
                        pStartOperation.setStartDiagnostic(RCF_StartDiagnostic.getStartDiagnosticByCode(specValue));
                    }
                    else
                    {
                        // common diagnostic
                        int commValue = eeaRcfO.getResult().getNegativeResult().getCommon().value.intValue();
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
     * Fills the object used for the encoding of Rcf GetParameter invoke
     * operation. S_OK The RCF GetParameter operation has been encoded. E_FAIL
     * Unable to encode the RCF GetParameter operation.
     * 
     * @throws SleApiException
     */
    private void encodeGetParameterInvokeOp(IRCF_GetParameter pGetParameterOperation,
                                            RcfGetParameterInvocationPdu eeaRcfO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pGetParameterOperation.getInvokerCredentials();
        eeaRcfO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaRcfO.setInvokeId(new InvokeId(pGetParameterOperation.getInvokeId()));

        // the parameter
        eeaRcfO.setRcfParameter(new RcfParameterName(pGetParameterOperation.getRequestedParameter().getCode()));
    }

    /**
     * Fills the RCF GET-PARAMETER invoke operation from the object. S_OK The
     * RCF GetParameter operation has been decoded. E_FAIL Unable to decode the
     * RCF GetParameter operation.
     * 
     * @throws SleApiException
     */
    private void decodeGetParameterInvokeOp(RcfGetParameterInvocationPdu eeaRcfO,
                                            IRCF_GetParameter pGetParameterOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaRcfO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pGetParameterOperation.putInvokerCredentials(pCredentials);
        }

        // the invoke id
        pGetParameterOperation.setInvokeId( eeaRcfO.getInvokeId().value.intValue());

        // the parameter
        pGetParameterOperation.setRequestedParameter(RCF_ParameterName
                .getRCFParamNameByCode(eeaRcfO.getRcfParameter().value.intValue()));
    }

    /**
     * Fills the RCF GET-PARAMETER return operation from the object for version
     * 2. S_OK The RCF GetParameter operation has been decoded. E_FAIL Unable to
     * decode the RCF GetParameter operation.
     * 
     * @throws SleApiException
     */
    private IRCF_GetParameter decodeGetParameterReturnOp(RcfGetParameterReturnV1Pdu eeaRcfO) throws SleApiException
    {
        IRCF_GetParameter pGetParameterOperation = null;
        ISLE_Operation pOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_getParameter;

        pOperation = this.pduTranslator.getReturnOp(eeaRcfO.getInvokeId(), opType);
        if (pOperation != null)
        {
            pGetParameterOperation = pOperation.queryInterface(IRCF_GetParameter.class);
            if (pGetParameterOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaRcfO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pGetParameterOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pGetParameterOperation.setInvokeId(eeaRcfO.getInvokeId().value.intValue());

                // the result
                if (eeaRcfO.getResult().getPositiveResult() != null)
                {
                    decodeParameter(eeaRcfO.getResult().getPositiveResult(), pGetParameterOperation);
                    pGetParameterOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eeaRcfO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        int specValue = eeaRcfO.getResult().getNegativeResult().getSpecific().value.intValue();
                        pGetParameterOperation.setGetParameterDiagnostic(RCF_GetParameterDiagnostic
                                .getGetParamDiagByCode(specValue));
                    }
                    else
                    {
                        // common diagnostic
                        int commValue =  eeaRcfO.getResult().getNegativeResult().getCommon().value.intValue();
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
     * Fills the object used for the encoding of Rcf StatusReport operation.
     * S_OK The RCF StatusReport operation has been encoded. E_FAIL Unable to
     * encode the RCF StatusReport operation.
     * 
     * @throws SleApiException
     */
    private void encodeStatusReportOp(IRCF_StatusReport pStatusReportOperation, RcfStatusReportInvocationPdu eeaRcfO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pStatusReportOperation.getInvokerCredentials();
        eeaRcfO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the delivered frame number
        eeaRcfO.setDeliveredFrameNumber(new IntUnsignedLong(pStatusReportOperation.getNumFrames()));
        // the frame sync lock status
        eeaRcfO.setFrameSyncLockStatus(new FrameSyncLockStatus(pStatusReportOperation.getFrameSyncLock().getCode()));
        // the symbol sync lock status
        eeaRcfO.setSymbolSyncLockStatus(new SymbolLockStatus(pStatusReportOperation.getSymbolSyncLock().getCode()));
        // the sub carrier lock status
        eeaRcfO.setSubcarrierLockStatus(new LockStatus(pStatusReportOperation.getSubCarrierDemodLock().getCode()));
        // the carrier lock status
        eeaRcfO.setCarrierLockStatus(new CarrierLockStatus(pStatusReportOperation.getCarrierDemodLock().getCode()));
        // the production status
        eeaRcfO.setProductionStatus(new RcfProductionStatus(pStatusReportOperation.getProductionStatus().getCode()));
    }

    /**
     * Fills the object used for the encoding of Rcf StatusReport V1 operation.
     * S_OK The RCF StatusReport operation has been encoded. E_FAIL Unable to
     * encode the RCF StatusReport operation.
     * 
     * @throws SleApiException
     */
    private void encodeStatusReportOp(IRCF_StatusReport pStatusReportOperation, RcfStatusReportInvocationV1Pdu eeaRcfO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pStatusReportOperation.getInvokerCredentials();
        eeaRcfO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the delivered frame number
        eeaRcfO.setDeliveredFrameNumber(new IntUnsignedLong(pStatusReportOperation.getNumFrames()));
        // the frame sync lock status
        eeaRcfO.setFrameSyncLockStatus(new FrameSyncLockStatus(pStatusReportOperation.getFrameSyncLock().getCode()));
        // the symbol sync lock status
        eeaRcfO.setSymbolSyncLockStatus(new SymbolLockStatus(pStatusReportOperation.getSymbolSyncLock().getCode()));
        // the sub carrier lock status
        eeaRcfO.setSubcarrierLockStatus(new LockStatus(pStatusReportOperation.getSubCarrierDemodLock().getCode()));
        // the carrier lock status
        eeaRcfO.setCarrierLockStatus(new CarrierLockStatus(pStatusReportOperation.getCarrierDemodLock().getCode()));
        // the production status
        eeaRcfO.setProductionStatus(new RcfProductionStatus(pStatusReportOperation.getProductionStatus().getCode()));
    }

    /**
     * Fills the RCF STATUS-REPORT operation from the object. S_OK The RCF
     * StatusReport operation has been decoded. E_FAIL Unable to decode the RCF
     * StatusReport operation.
     * 
     * @throws SleApiException
     */
    private void decodeStatusReportOp(RcfStatusReportInvocationPdu eeaRcfO, IRCF_StatusReport pStatusReportOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaRcfO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pStatusReportOperation.putInvokerCredentials(pCredentials);
        }

        // the delivered frame number
        pStatusReportOperation.setNumFrames(eeaRcfO.getDeliveredFrameNumber().value.longValue());
        // the frame sync lock status
        pStatusReportOperation.setFrameSyncLock(RCF_LockStatus
                .getLockStatusByCode(eeaRcfO.getFrameSyncLockStatus().value.intValue()));
        // the symbol sync lock status
        pStatusReportOperation.setSymbolSyncLock(RCF_LockStatus
                .getLockStatusByCode(eeaRcfO.getSymbolSyncLockStatus().value.intValue()));
        // the sub carrier lock status
        pStatusReportOperation.setSubCarrierDemodLock(RCF_LockStatus
                .getLockStatusByCode(eeaRcfO.getSubcarrierLockStatus().value.intValue()));
        // the carrier lock status
        pStatusReportOperation.setCarrierDemodLock(RCF_LockStatus
                .getLockStatusByCode(eeaRcfO.getCarrierLockStatus().value.intValue()));
        // the production status
        pStatusReportOperation.setProductionStatus(RCF_ProductionStatus
                .getProductionStatusByCode(eeaRcfO.getProductionStatus().value.intValue()));
    }

    /**
     * Fills the RCF STATUS-REPORT V1 operation from the object. S_OK The RCF
     * StatusReport operation has been decoded. E_FAIL Unable to decode the RCF
     * StatusReport operation.
     * 
     * @throws SleApiException
     */
    private void decodeStatusReportOp(RcfStatusReportInvocationV1Pdu eeaRcfO, IRCF_StatusReport pStatusReportOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaRcfO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pStatusReportOperation.putInvokerCredentials(pCredentials);
        }

        // the delivered frame number
        pStatusReportOperation.setNumFrames(eeaRcfO.getDeliveredFrameNumber().value.longValue());
        // the frame sync lock status
        pStatusReportOperation.setFrameSyncLock(RCF_LockStatus
                .getLockStatusByCode(eeaRcfO.getFrameSyncLockStatus().value.intValue()));
        // the symbol sync lock status
        pStatusReportOperation.setSymbolSyncLock(RCF_LockStatus
                .getLockStatusByCode(eeaRcfO.getSymbolSyncLockStatus().value.intValue()));
        // the sub carrier lock status
        pStatusReportOperation.setSubCarrierDemodLock(RCF_LockStatus
                .getLockStatusByCode((int) eeaRcfO.getSubcarrierLockStatus().value.intValue()));
        // the carrier lock status
        pStatusReportOperation.setCarrierDemodLock(RCF_LockStatus
                .getLockStatusByCode((int) eeaRcfO.getCarrierLockStatus().value.intValue()));
        // the production status
        pStatusReportOperation.setProductionStatus(RCF_ProductionStatus
                .getProductionStatusByCode((int) eeaRcfO.getProductionStatus().value.intValue()));
    }

    /**
     * Fills the object used for the encoding of Rcf TransferData operation.
     * S_OK The RCF TransferData operation has been encoded. E_FAIL Unable to
     * encode the RCF TransferData operation.
     * 
     * @throws SleApiException
     */
    private void encodeTransferDataOp(IRCF_TransferData pTransferDataOperation, FrameOrNotification eeaRcfO) throws SleApiException
    {
        RcfTransferDataInvocation annotatedFrame = new RcfTransferDataInvocation();

        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pTransferDataOperation.getInvokerCredentials();
        annotatedFrame.setInvokerCredentials(encodeCredentials(pCredentials));

        // the earth receive time
        ISLE_Time pTime = null;
        pTime = pTransferDataOperation.getEarthReceiveTime();
        annotatedFrame.setEarthReceiveTime(encodeEarthReceiveTime(pTime));

        // the antenna id
        if (pTransferDataOperation.getAntennaIdFormat() == RCF_AntennaIdFormat.rcfAF_global)
        {
            // global form
            BerObjectIdentifier objectId = new BerObjectIdentifier(pTransferDataOperation.getAntennaIdGF());
            AntennaId aid = new AntennaId();
            aid.setGlobalForm(objectId);
            annotatedFrame.setAntennaId(aid);
        }
        else
        {
            // local form
            byte[] poctet = pTransferDataOperation.getAntennaIdLF();
            if (poctet.length <= C_MaxLengthAntennaLocalForm)
            {
            	AntennaId aid = new AntennaId();
            	aid.setLocalForm(new BerOctetString(poctet));
                annotatedFrame.setAntennaId(aid);
            }
        }

        // the data link continuity
        annotatedFrame.setDataLinkContinuity(new BerInteger(pTransferDataOperation.getDataLinkContinuity()));

        // the private annotation
        byte[] pa = pTransferDataOperation.getPrivateAnnotation();
        if (pa == null)
        {
        	RcfTransferDataInvocation.PrivateAnnotation paPA = new RcfTransferDataInvocation.PrivateAnnotation();
        	paPA.setNull(new BerNull());
            annotatedFrame.setPrivateAnnotation(paPA);
        }
        else
        {
            if (pa.length <= C_MaxLengthPrivateAnnotation)
            {
                annotatedFrame.setPrivateAnnotation(new RcfTransferDataInvocation.PrivateAnnotation(pa));
            }
        }

        // the space link data unit
        byte[] pdata = pTransferDataOperation.getData();
        SpaceLinkDataUnit data = new SpaceLinkDataUnit(pdata);
        annotatedFrame.setData(data);

        eeaRcfO.setAnnotatedFrame(annotatedFrame);
    }

    /**
     * Fills the RCF TRANSFER-DATA operation from the object. S_OK The RCF
     * TransferData operation has been decoded. E_FAIL Unable to decode the RCF
     * TransferData operation.
     * 
     * @throws SleApiException
     */
    private void decodeTransferDataOp(RcfTransferDataInvocation eeaRcfO, IRCF_TransferData pTransferDataOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaRcfO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pTransferDataOperation.putInvokerCredentials(pCredentials);
        }

        // the earth receive time
        ISLE_Time pTime = null;
        pTime = decodeEarthReceiveTime(eeaRcfO.getEarthReceiveTime());
        if (pTime != null)
        {
            pTransferDataOperation.putEarthReceiveTime(pTime);
        }

        // the antenna id
        AntennaId eeaAntenna = eeaRcfO.getAntennaId();
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
        pTransferDataOperation.setDataLinkContinuity((int) eeaRcfO.getDataLinkContinuity().value.intValue());

        // the private annotation
        if (eeaRcfO.getPrivateAnnotation().getNull() != null)
        {
            pTransferDataOperation.putPrivateAnnotation(null);
        }
        else
        {
            if (eeaRcfO.getPrivateAnnotation().getNotNull().value.length <= C_MaxLengthPrivateAnnotation)
            {
                pTransferDataOperation.putPrivateAnnotation(eeaRcfO.getPrivateAnnotation().getNotNull().value);
            }
        }

        // the space link data unit
        byte[] pdata = null;
        pdata = eeaRcfO.getData().value;
        pTransferDataOperation.putData(pdata);
    }

    /**
     * Fills the object used for the encoding of Rcf SyncNotify operation. S_OK
     * The RCF SyncNotify operation has been encoded. E_FAIL Unable to encode
     * the RCF SyncNotify operation.
     * 
     * @throws SleApiException
     */
    private void encodeSyncNotifyOp(IRCF_SyncNotify pSyncNotifyOperation, FrameOrNotification eeaRcfO) throws SleApiException
    {
        RcfSyncNotifyInvocation syncNotify = new RcfSyncNotifyInvocation();

        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pSyncNotifyOperation.getInvokerCredentials();
        syncNotify.setInvokerCredentials(encodeCredentials(pCredentials));

        // the rcf notification
        syncNotify.setNotification(new Notification());

        switch (pSyncNotifyOperation.getNotificationType())
        {
        case rcfNT_lossFrameSync:
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
        case rcfNT_productionStatusChange:
        {
            syncNotify.getNotification().setProductionStatusChange(new RcfProductionStatus(pSyncNotifyOperation
                    .getProductionStatus().getCode()));
            break;
        }
        case rcfNT_excessiveDataBacklog:
        {
            syncNotify.getNotification().setExcessiveDataBacklog(new BerNull());
            break;
        }
        case rcfNT_endOfData:
        {
            syncNotify.getNotification().setEndOfData(new BerNull());
            break;
        }
        default:
        {
            break;
        }
        }

        eeaRcfO.setSyncNotification(syncNotify);
    }

    /**
     * Fills the RCF SYNC-NOTIFY operation from the object. S_OK The RCF
     * SyncNotify operation has been decoded. E_FAIL Unable to decode the RCF
     * SyncNotify operation.
     * 
     * @throws SleApiException
     */
    private void decodeSyncNotifyOp(RcfSyncNotifyInvocation eeaRcfO, IRCF_SyncNotify pSyncNotifyOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaRcfO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pSyncNotifyOperation.setInvokerCredentials(pCredentials);
        }

        // the rcf notification
        if (eeaRcfO.getNotification().getLossFrameSync() != null)
        {
            // the time
            ISLE_Time pTime = null;
            pTime = decodeTime(eeaRcfO.getNotification().getLossFrameSync().getTime());

            // the carrier lock status
            RCF_LockStatus carrierDemodLock = RCF_LockStatus
                    .getLockStatusByCode(eeaRcfO.getNotification().getLossFrameSync().getCarrierLockStatus().value.intValue());
            // the sub carrier lock status
            RCF_LockStatus subCarrierDemodLock = RCF_LockStatus
                    .getLockStatusByCode(eeaRcfO.getNotification().getLossFrameSync().getSubcarrierLockStatus().value.intValue());
            // the symbol sync lock status
            RCF_LockStatus symbolSyncLock = RCF_LockStatus
                    .getLockStatusByCode(eeaRcfO.getNotification().getLossFrameSync().getSymbolSyncLockStatus().value.intValue());

            pSyncNotifyOperation.setLossOfFrameSync(pTime, symbolSyncLock, subCarrierDemodLock, carrierDemodLock);
        }
        else if (eeaRcfO.getNotification().getProductionStatusChange() != null)
        {
            RCF_ProductionStatus productionStatus = RCF_ProductionStatus
                    .getProductionStatusByCode(eeaRcfO.getNotification().getProductionStatusChange().value.intValue());
            pSyncNotifyOperation.setProductionStatusChange(productionStatus);
        }
        else if (eeaRcfO.getNotification().getExcessiveDataBacklog() != null)
        {
            pSyncNotifyOperation.setDataDiscarded();
        }
        else if (eeaRcfO.getNotification().getEndOfData() != null)
        {
            pSyncNotifyOperation.setEndOfData();
        }
    }

    /**
     * Fills the object used for the encoding of Rcf TransferBuffer operation.
     * S_OK The RCF TransferBuffer operation has been encoded. E_FAIL Unable to
     * encode the RCF TransferBuffer operation.
     * 
     * @throws SleApiException
     */
    private void encodeTransferBufferOp(ISLE_TransferBuffer pTransferBufferOperation, RcfTransferBufferPdu eeaRcfO) throws SleApiException
    {
        ISLE_Operation pCurrentOp = null;
        // Added for v5
        //ByteArrayOutputStream os = new ByteArrayOutputStream();

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
                IRCF_TransferData pOp = null;
                pOp = pCurrentOp.queryInterface(IRCF_TransferData.class);
                if (pOp != null)
                {
                    encodeTransferDataOp(pOp, currentElement);
                    // add the current element to the list
                    eeaRcfO.getFrameOrNotification().add(currentElement);
                }
                else
                {
                    // cannot get the interface
                    throw new SleApiException(HRESULT.E_FAIL, "No interface");
                }
            }
            else if (opType == SLE_OpType.sleOT_syncNotify)
            {
                IRCF_SyncNotify pOp = null;
                pOp = pCurrentOp.queryInterface(IRCF_SyncNotify.class);
                if (pOp != null)
                {
                    encodeSyncNotifyOp(pOp, currentElement);
                    // add the current element to the list
                    eeaRcfO.getFrameOrNotification().add(currentElement);
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
     * Fills the TRANSFER-BUFFER operation from the object. S_OK A new RCF
     * TransferBuffer operation has been instantiated. E_FAIL Unable to
     * Instantiate a RCF TransferBuffer operation.
     * 
     * @throws SleApiException
     */
    private void decodeTransferBufferOp(RcfTransferBufferPdu eeaRcfO, ISLE_TransferBuffer pTransferBufferOperation) throws SleApiException
    {
        ISLE_Operation pOp = null;
        //Iterator<FrameOrNotification> it = eeaRcfO.seqOf.iterator();
        Iterator<FrameOrNotification> it = eeaRcfO.getFrameOrNotification().iterator();

        while (it.hasNext())
        {
            FrameOrNotification currentElement = it.next();
            if (currentElement.getAnnotatedFrame() != null)
            {
                // instantiate a new transfer data operation
                IRCF_TransferData pTransferData = null;
                pTransferData = this.operationFactory.createOperation(IRCF_TransferData.class,
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
                IRCF_SyncNotify pSyncNotify = null;
                pSyncNotify = this.operationFactory.createOperation(IRCF_SyncNotify.class,
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
     * Fills the RCF global VCID of the Asn1 object.
     */
    private GvcId encodeGvcid(RCF_Gvcid gvcid)
    {
        GvcId eeaO = new GvcId();
        eeaO.setSpacecraftId(new BerInteger(gvcid.getScid()));
        eeaO.setVersionNumber(new BerInteger(gvcid.getVersion()));
        switch (gvcid.getType())
        {
        case rcfCT_MasterChannel:
        {
        	VcId mc = new VcId();
        	mc.setMasterChannel(new BerNull());
            eeaO.setVcId(mc);
            break;
        }
        case rcfCT_VirtualChannel:
        {
        	VcId vc = new VcId();
        	vc.setVirtualChannel(new ccsds.sle.transfer.service.rcf.structures.VcId(gvcid.getVcid()));
        	eeaO.setVcId(vc);      	
        	//eeaO.vcId = new VcId(null, new VcId(gvcid.getVcid()));
            break;
        }
        default:
        {
            eeaO.setVcId(new VcId());
            break;
        }
        }

        return eeaO;
    }

    /**
     * Fills the RCF global VCID from the Asn1 object.
     */
    private RCF_Gvcid decodeGvcid(GvcId eeaO)
    {
        RCF_Gvcid gvcid = new RCF_Gvcid();
        gvcid.setScid(eeaO.getSpacecraftId().value.intValue());
        gvcid.setVersion(eeaO.getVersionNumber().value.intValue());

        if (eeaO.getVcId().getMasterChannel() != null)
        {
            gvcid.setType(RCF_ChannelType.rcfCT_MasterChannel);
            gvcid.setVcid(0);
        }
        else if (eeaO.getVcId().getVirtualChannel() != null)
        {
            gvcid.setType(RCF_ChannelType.rcfCT_VirtualChannel);
            gvcid.setVcid((int) eeaO.getVcId().getVirtualChannel().value.intValue());
        }
        else
        {
            gvcid.setType(RCF_ChannelType.rcfCT_invalid);
        }

        return gvcid;
    }

    /**
     * Fills the RCF Parameter of the Asn1 object for SLE V1
     * @throws IOException 
     */
    private void encodeParameter(IRCF_GetParameter pGetParameterOperation, RcfGetParameterV1 eeaO) throws IOException
    {
        switch (pGetParameterOperation.getReturnedParameter())
        {
        case rcfPN_bufferSize:
        {
            ParBufferSize bufferSize = new ParBufferSize();
            bufferSize.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_bufferSize.getCode()));
            bufferSize.setParameterValue(new IntPosShort(pGetParameterOperation.getTransferBufferSize()));
            eeaO.setParBufferSize(bufferSize);
            break;
        }
        case rcfPN_deliveryMode:
        {
            ParDeliveryMode parDeliveryMode = new ParDeliveryMode();
            parDeliveryMode.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_deliveryMode.getCode()));
            parDeliveryMode.setParameterValue(new RcfDeliveryMode(pGetParameterOperation.getDeliveryMode().getCode()));
            eeaO.setParDeliveryMode(parDeliveryMode);
            break;
        }
        case rcfPN_latencyLimit:
        {
            int latencyLimit = pGetParameterOperation.getLatencyLimit();
            ParLatencyLimit parLatencyLimit = new ParLatencyLimit();
            parLatencyLimit.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_latencyLimit.getCode()));
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
        case rcfPN_permittedGvcidSet:
        {
            ParPermittedGvcidSet parPermittedGvcidSet = new ParPermittedGvcidSet();
            parPermittedGvcidSet.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_permittedGvcidSet.getCode()));
            GvcIdSetV1To4 parSet = new GvcIdSetV1To4();
            encodeGvcidSetV1To4(pGetParameterOperation, parSet);
            parPermittedGvcidSet.setParameterValue(parSet);
            eeaO.setParPermittedGvcidSet(parPermittedGvcidSet);
            break;
        }
        case rcfPN_reportingCycle:
        {
            long reportingCycle = pGetParameterOperation.getReportingCycle();
            ParReportingCycle parReportingCycle = new ParReportingCycle();
            parReportingCycle.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_reportingCycle.getCode()));
            if (reportingCycle == 0)
            {
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOff(new BerNull());
                parReportingCycle.setParameterValue(crc);
            }
            else
            {
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOn(new ReportingCycle(reportingCycle));
            	parReportingCycle.setParameterValue(crc);
                //parReportingCycle.setParameterValue(new CurrentReportingCycle(null, new ReportingCycle(reportingCycle));
            }
            eeaO.setParReportingCycle(parReportingCycle);
            break;
        }
        case rcfPN_requestedGvcid:
        {
            ParReqGvcId parReqGvcid = new ParReqGvcId();
            parReqGvcid.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_requestedGvcid.getCode()));
            parReqGvcid.setParameterValue(encodeGvcid(pGetParameterOperation.getRequestedGvcid()));
            eeaO.setParReqGvcId(parReqGvcid);
            break;
        }
        case rcfPN_returnTimeoutPeriod:
        {
            ParReturnTimeout parRetTimeout = new ParReturnTimeout();
            parRetTimeout.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_returnTimeoutPeriod.getCode()));
            parRetTimeout.setParameterValue(new TimeoutPeriod(pGetParameterOperation.getReturnTimeoutPeriod()));
            eeaO.setParReturnTimeout(parRetTimeout);
            break;
        }
        default:
        {
            break;
        }
        }
    }

    /**
     * Fills the parameter of the GetParameter Return operation from the Asn1
     * object for version 1.
     */
    private void decodeParameter(RcfGetParameterV1 eeaO, IRCF_GetParameter pGetParameterOperation)
    {
        if (eeaO.getParBufferSize() != null)
        {
            pGetParameterOperation.setTransferBufferSize(eeaO.getParBufferSize().getParameterValue().value.longValue());
        }
        else if (eeaO.getParDeliveryMode() != null)
        {
            pGetParameterOperation.setDeliveryMode(RCF_DeliveryMode
                    .getRCFDelModeByCode( eeaO.getParDeliveryMode().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParLatencyLimit() != null)
        {
            if (eeaO.getParLatencyLimit().getParameterValue().getOnline() != null)
            {
                pGetParameterOperation.setLatencyLimit( eeaO.getParLatencyLimit().getParameterValue().getOnline().value.intValue());
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
        else if (eeaO.getParPermittedGvcidSet() != null)
        {
        	decodeGvcidSetV1To4(eeaO.getParPermittedGvcidSet().getParameterValue(), pGetParameterOperation);
        }
        else if (eeaO.getParReqGvcId() != null)
        {
            pGetParameterOperation.setRequestedGvcid(decodeGvcid(eeaO.getParReqGvcId().getParameterValue()));
        }
        else if (eeaO.getParReturnTimeout() != null)
        {
            pGetParameterOperation.setReturnTimeoutPeriod(eeaO.getParReturnTimeout().getParameterValue().value.longValue());
        }
    }

    /**
     * Fills the RCF global VCID list of the Asn1 object.
     * @throws IOException 
     */
    private void encodeGvcidSet(IRCF_GetParameter pGetParameterOperation, GvcIdSet eeaO)
    {
        RCF_Gvcid[] pGvcidList = pGetParameterOperation.getPermittedGvcidSet();
        List<PXDEL_Gvcid> delGvcidList = new ArrayList<PXDEL_Gvcid>();

        PXDEL_Gvcid delGvcid = null;

        // build a list which contains all the information organized to be
        // easily encoded
        for (RCF_Gvcid pGvcid : pGvcidList)
        {
            boolean exist = false;
            if (pGvcid.getType() == RCF_ChannelType.rcfCT_VirtualChannel)
            {
                // check if an object with the same scId and version already
                // exists in the list
                for (PXDEL_Gvcid li : delGvcidList)
                {
                    delGvcid = li;
                    if (delGvcid.getScid() == pGvcid.getScid() && delGvcid.getVersion() == pGvcid.getVersion())
                    {
                        exist = true;
                        break;
                    }
                }
            }

            if (exist)
            {
                // update the vc list
                delGvcid.getVcid().add((long) pGvcid.getVcid());
            }
            else
            {
                // create a new object
                delGvcid = new PXDEL_Gvcid();
                delGvcid.getVcid().clear();
                // fill the new object
                if (pGvcid.getType() == RCF_ChannelType.rcfCT_MasterChannel)
                {
                    delGvcid.setMasterChannel(true);
                }
                else
                {
                    delGvcid.setMasterChannel(false);
                }

                delGvcid.setScid(pGvcid.getScid());
                delGvcid.setVersion(pGvcid.getVersion());
                delGvcid.getVcid().add((long) pGvcid.getVcid());
                // insert the new object in the list
                delGvcidList.add(delGvcid);
            }
        }
        //ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        while (!delGvcidList.isEmpty())
        {
            MasterChannelComposition pmcc = new MasterChannelComposition();
            delGvcid = delGvcidList.get(0);
            delGvcidList.remove(0);
            pmcc.setSpacecraftId(new BerInteger(delGvcid.getScid()));
            pmcc.setVersionNumber(new BerInteger(delGvcid.getVersion()));
            if (delGvcid.isMasterChannel())
            {
            	McOrVcList mcOrVcList = new McOrVcList();
            	mcOrVcList.setMasterChannel(new BerNull());
                pmcc.setMcOrVcList(mcOrVcList);
            }
            else
            {
                VcList vcList = new VcList();
                int countVc = delGvcid.getVcid().size(); // count is always >= 1
                for (int i = 0; i < countVc; i++)
                {
                    List<Long> list = new ArrayList<Long>(delGvcid.getVcid());
                    ccsds.sle.transfer.service.rcf.structures.VcId vcEntry = new ccsds.sle.transfer.service.rcf.structures.VcId(list.get(0));
                    delGvcid.getVcid().remove(list.get(0));
                    vcList.getVcId().add(vcEntry);
                }

                McOrVcList mcvcList = new McOrVcList();
                mcvcList.setVcList(vcList);
                pmcc.setMcOrVcList(mcvcList);
            }

            eeaO.getMasterChannelComposition().add(pmcc);
        }
    }

    
    /**
     * Fills the RCF global VCID list of the Asn1 object.
     * @throws IOException 
     */
    private void encodeGvcidSetV1To4(IRCF_GetParameter pGetParameterOperation, GvcIdSetV1To4 eeaO)
    {
        RCF_Gvcid[] pGvcidList = pGetParameterOperation.getPermittedGvcidSet();
        List<PXDEL_Gvcid> delGvcidList = new ArrayList<PXDEL_Gvcid>();

        PXDEL_Gvcid delGvcid = null;

        // build a list which contains all the information organized to be
        // easily encoded
        for (RCF_Gvcid pGvcid : pGvcidList)
        {
            boolean exist = false;
            if (pGvcid.getType() == RCF_ChannelType.rcfCT_VirtualChannel)
            {
                // check if an object with the same scId and version already
                // exists in the list
                for (PXDEL_Gvcid li : delGvcidList)
                {
                    delGvcid = li;
                    if (delGvcid.getScid() == pGvcid.getScid() && delGvcid.getVersion() == pGvcid.getVersion())
                    {
                        exist = true;
                        break;
                    }
                }
            }

            if (exist)
            {
                // update the vc list
                delGvcid.getVcid().add((long) pGvcid.getVcid());
            }
            else
            {
                // create a new object
                delGvcid = new PXDEL_Gvcid();
                delGvcid.getVcid().clear();
                // fill the new object
                if (pGvcid.getType() == RCF_ChannelType.rcfCT_MasterChannel)
                {
                    delGvcid.setMasterChannel(true);
                }
                else
                {
                    delGvcid.setMasterChannel(false);
                }

                delGvcid.setScid(pGvcid.getScid());
                delGvcid.setVersion(pGvcid.getVersion());
                delGvcid.getVcid().add((long) pGvcid.getVcid());
                // insert the new object in the list
                delGvcidList.add(delGvcid);
            }
        }
        //ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        while (!delGvcidList.isEmpty())
        {
            MasterChannelCompositionV1To4 pmcc = new MasterChannelCompositionV1To4();
            delGvcid = delGvcidList.get(0);
            delGvcidList.remove(0);
            pmcc.setSpacecraftId(new BerInteger(delGvcid.getScid()));
            pmcc.setVersionNumber(new BerInteger(delGvcid.getVersion()));
            if (delGvcid.isMasterChannel())
            {
            	MasterChannelCompositionV1To4.McOrVcList mcOrVcList = new MasterChannelCompositionV1To4.McOrVcList();
            	mcOrVcList.setMasterChannel(new BerNull());
                pmcc.setMcOrVcList(mcOrVcList);
            }
            else
            {
            	MasterChannelCompositionV1To4.McOrVcList.VcList vcList = new MasterChannelCompositionV1To4.McOrVcList.VcList();
                int countVc = delGvcid.getVcid().size(); // count is always >= 1
                for (int i = 0; i < countVc; i++)
                {
                    List<Long> list = new ArrayList<Long>(delGvcid.getVcid());
                    ccsds.sle.transfer.service.rcf.structures.VcId vcEntry = new ccsds.sle.transfer.service.rcf.structures.VcId(list.get(0));
                    delGvcid.getVcid().remove(list.get(0));
                    vcList.getVcId().add(vcEntry);
                }

                MasterChannelCompositionV1To4.McOrVcList mcvcList = new MasterChannelCompositionV1To4.McOrVcList();
                mcvcList.setVcList(vcList);
                pmcc.setMcOrVcList(mcvcList);
            }

            eeaO.getMasterChannelCompositionV1To4().add(pmcc);
        }
    }
 
    /**
     * Fills the RCF global VCID list from the Asn1 object.
     */
    private void decodeGvcidSetV1To4(GvcIdSetV1To4 eeaO, IRCF_GetParameter pGetParameterOperation)
    {
        int nbelem = 0;
        RCF_Gvcid pGvcid;

        // calculate the total number rcf gvcid
        for (MasterChannelCompositionV1To4 pmcc : eeaO.getMasterChannelCompositionV1To4())
        {
            if (pmcc.getMcOrVcList().getMasterChannel() != null)
            {
                nbelem++;
            }
            else
            {
                MasterChannelCompositionV1To4.McOrVcList.VcList vcList = pmcc.getMcOrVcList().getVcList();
                nbelem += vcList.getVcId().size();
            }
        }

        if (nbelem == 0)
        {
            return;
        }

        RCF_Gvcid[] pGvcidList = new RCF_Gvcid[nbelem];
        for (int i = 0; i < nbelem; i++)
        {
            pGvcidList[i] = new RCF_Gvcid();
        }

        // for all the master channel composition
        nbelem = 0;
        for (MasterChannelCompositionV1To4 pmcc : eeaO.getMasterChannelCompositionV1To4())
        {
            if (pmcc.getMcOrVcList().getMasterChannel() != null)
            {
                pGvcid = pGvcidList[nbelem++];
                pGvcid.setScid(pmcc.getSpacecraftId().value.intValue());
                pGvcid.setVersion(pmcc.getVersionNumber().value.intValue());
                pGvcid.setType(RCF_ChannelType.rcfCT_MasterChannel);
                pGvcid.setVcid(0);
            }
            else
            {
            	MasterChannelCompositionV1To4.McOrVcList vcList = pmcc.getMcOrVcList();
                // for all the vc
                for (ccsds.sle.transfer.service.rcf.structures.VcId vc : vcList.getVcList().getVcId() )
                {
                    pGvcid = pGvcidList[nbelem++];
                    pGvcid.setScid(pmcc.getSpacecraftId().value.intValue());
                    pGvcid.setVersion( pmcc.getVersionNumber().value.intValue());
                    pGvcid.setType(RCF_ChannelType.rcfCT_VirtualChannel);
                    pGvcid.setVcid(vc.value.intValue());
                }
            }
        }
        pGetParameterOperation.putPermittedGvcidSet(pGvcidList);
    }

    
    
    /**
     * Fills the RCF global VCID list from the Asn1 object.
     */
    private void decodeGvcidSet(GvcIdSet eeaO, IRCF_GetParameter pGetParameterOperation)
    {
        int nbelem = 0;
        RCF_Gvcid pGvcid;

        // calculate the total number rcf gvcid
        for (MasterChannelComposition pmcc : eeaO.getMasterChannelComposition())
        {
            if (pmcc.getMcOrVcList().getMasterChannel() != null)
            {
                nbelem++;
            }
            else
            {
                VcList vcList = pmcc.getMcOrVcList().getVcList();
                nbelem += vcList.getVcId().size();
            }
        }

        if (nbelem == 0)
        {
            return;
        }

        RCF_Gvcid[] pGvcidList = new RCF_Gvcid[nbelem];
        for (int i = 0; i < nbelem; i++)
        {
            pGvcidList[i] = new RCF_Gvcid();
        }

        // for all the master channel composition
        nbelem = 0;
        for (MasterChannelComposition pmcc : eeaO.getMasterChannelComposition())
        {
            if (pmcc.getMcOrVcList().getMasterChannel() != null)
            {
                pGvcid = pGvcidList[nbelem++];
                pGvcid.setScid(pmcc.getSpacecraftId().value.intValue());
                pGvcid.setVersion(pmcc.getVersionNumber().value.intValue());
                pGvcid.setType(RCF_ChannelType.rcfCT_MasterChannel);
                pGvcid.setVcid(0);
            }
            else
            {
                McOrVcList vcList = pmcc.getMcOrVcList();
                // for all the vc
                for (ccsds.sle.transfer.service.rcf.structures.VcId vc : vcList.getVcList().getVcId() )
                {
                    pGvcid = pGvcidList[nbelem++];
                    pGvcid.setScid(pmcc.getSpacecraftId().value.intValue());
                    pGvcid.setVersion( pmcc.getVersionNumber().value.intValue());
                    pGvcid.setType(RCF_ChannelType.rcfCT_VirtualChannel);
                    pGvcid.setVcid(vc.value.intValue());
                }
            }
        }

        pGetParameterOperation.putPermittedGvcidSet(pGvcidList);
    }

    /**
     * Fills the object used for the encoding of Rcf GetParameter return
     * operation for version 2. S_OK The RCF GetParameter operation has been
     * encoded. E_FAIL Unable to encode the RCF GetParameter operation.
     * 
     * @throws SleApiException
     * @throws IOException 
     */
    private void encodeGetParameterReturnOpV2To4(IRCF_GetParameter pGetParameterOperation, 
    											 RcfGetParameterReturnPduV2To4 eeaRcfO) throws SleApiException, IOException
    {																					   
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pGetParameterOperation.getPerformerCredentials();
        eeaRcfO.setPerformerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaRcfO.setInvokeId(new InvokeId(pGetParameterOperation.getInvokeId()));

        // the result
        if (pGetParameterOperation.getResult() == SLE_Result.sleRES_positive)
        {
            // positive result
            RcfGetParameterReturnV2To4.Result positiveResult = new RcfGetParameterReturnV2To4.Result();
            positiveResult.setPositiveResult(new RcfGetParameterV2To4());
            encodeParameterV2To4(pGetParameterOperation, positiveResult.getPositiveResult());
            eeaRcfO.setResult(positiveResult);
        }
        else
        {
        	RcfGetParameterReturnV2To4.Result negResult = new RcfGetParameterReturnV2To4.Result();

            if (pGetParameterOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                DiagnosticRcfGet repSpecific = new DiagnosticRcfGet();

                switch (pGetParameterOperation.getGetParameterDiagnostic())
                {
                case rcfGP_unknownParameter:
                {
                    repSpecific.setSpecific(new BerInteger(RCF_GetParameterDiagnostic.rcfGP_unknownParameter.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(RCF_GetParameterDiagnostic.rcfGP_invalid.getCode()));
                    break;
                }
                }

                negResult.setNegativeResult(repSpecific);
            }
            else
            {
                // common diagnostic
                DiagnosticRcfGet repCommon = new DiagnosticRcfGet();
                repCommon.setSpecific (null);
                repCommon.setCommon(new Diagnostics(pGetParameterOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }

            eeaRcfO.setResult(negResult);
        }
    }
    
    /**
     * Fills the object used for the encoding of Rcf GetParameter return
     * operation for version 5 and later. S_OK The RCF GetParameter operation has been
     * encoded. E_FAIL Unable to encode the RCF GetParameter operation.
     * 
     * @throws SleApiException
     * @throws IOException 
     */
    private void encodeGetParameterReturnOp(IRCF_GetParameter pGetParameterOperation, 
    											 RcfGetParameterReturnPdu eeaRcfO) throws SleApiException, IOException
    {																					   
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pGetParameterOperation.getPerformerCredentials();
        eeaRcfO.setPerformerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaRcfO.setInvokeId(new InvokeId(pGetParameterOperation.getInvokeId()));

        // the result
        if (pGetParameterOperation.getResult() == SLE_Result.sleRES_positive)
        {
            // positive result
            RcfGetParameterReturn.Result positiveResult = new RcfGetParameterReturn.Result();
            positiveResult.setPositiveResult(new RcfGetParameter());
            encodeParameter(pGetParameterOperation, positiveResult.getPositiveResult());
            eeaRcfO.setResult(positiveResult);
        }
        else
        {
            RcfGetParameterReturn.Result negResult = new RcfGetParameterReturn.Result();

            if (pGetParameterOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                DiagnosticRcfGet repSpecific = new DiagnosticRcfGet();

                switch (pGetParameterOperation.getGetParameterDiagnostic())
                {
                case rcfGP_unknownParameter:
                {
                    repSpecific.setSpecific(new BerInteger(RCF_GetParameterDiagnostic.rcfGP_unknownParameter.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(RCF_GetParameterDiagnostic.rcfGP_invalid.getCode()));
                    break;
                }
                }

                negResult.setNegativeResult(repSpecific);
            }
            else
            {
                // common diagnostic
                DiagnosticRcfGet repCommon = new DiagnosticRcfGet();
                repCommon.setSpecific (null);
                repCommon.setCommon(new Diagnostics(pGetParameterOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }

            eeaRcfO.setResult(negResult);
        }
    }


    /**
     * Fills the object used for the encoding of Rcf GetParameter return
     * operation for version 1. S_OK The RCF GetParameter operation has been
     * encoded. E_FAIL Unable to encode the RCF GetParameter operation.
     * 
     * @throws SleApiException
     * @throws IOException 
     */
    private void encodeGetParameterReturnOp(IRCF_GetParameter pGetParameterOperation, RcfGetParameterReturnV1Pdu eeaRcfO) throws SleApiException, IOException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pGetParameterOperation.getPerformerCredentials();
        eeaRcfO.setPerformerCredentials (encodeCredentials(pCredentials));

        // the invoke id
        eeaRcfO.setInvokeId(new InvokeId(pGetParameterOperation.getInvokeId()));

        // the result
        if (pGetParameterOperation.getResult() == SLE_Result.sleRES_positive)
        {
            // positive result
            ccsds.sle.transfer.service.rcf.outgoing.pdus.RcfGetParameterReturnV1.Result positiveResult = new ccsds.sle.transfer.service.rcf.outgoing.pdus.RcfGetParameterReturnV1.Result();
            positiveResult.setPositiveResult(new RcfGetParameterV1());
            encodeParameter(pGetParameterOperation, positiveResult.getPositiveResult());
            eeaRcfO.setResult(positiveResult);
        }
        else
        {
            ccsds.sle.transfer.service.rcf.outgoing.pdus.RcfGetParameterReturnV1.Result negResult = new ccsds.sle.transfer.service.rcf.outgoing.pdus.RcfGetParameterReturnV1.Result();

            if (pGetParameterOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                DiagnosticRcfGet repSpecific = new DiagnosticRcfGet();

                switch (pGetParameterOperation.getGetParameterDiagnostic())
                {
                case rcfGP_unknownParameter:
                {
                    repSpecific.setSpecific(new BerInteger(RCF_GetParameterDiagnostic.rcfGP_unknownParameter.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(RCF_GetParameterDiagnostic.rcfGP_invalid.getCode()));
                    break;
                }
                }

                negResult.setNegativeResult(repSpecific);
            }
            else
            {
                // common diagnostic
                DiagnosticRcfGet repCommon = new DiagnosticRcfGet();
                repCommon.setSpecific (null);
                repCommon.setCommon(new Diagnostics(pGetParameterOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }

            eeaRcfO.setResult(negResult);
        }
    }
    
    /**
     * Fills the RCF GET-PARAMETER return operation from the object for version
     * 1. S_OK The RCF GetParameter operation has been decoded. E_FAIL Unable to
     * decode the RCF GetParameter operation.
     * 
     * @throws SleApiException
     */
    private IRCF_GetParameter decodeGetParameterReturnOp(RcfGetParameterReturnPdu eeaRcfO) throws SleApiException
    {
        IRCF_GetParameter pGetParameterOperation = null;
        ISLE_Operation pOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_getParameter;

        pOperation = this.pduTranslator.getReturnOp(eeaRcfO.getInvokeId(), opType);
        if (pOperation != null)
        {
            pGetParameterOperation = pOperation.queryInterface(IRCF_GetParameter.class);
            if (pGetParameterOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaRcfO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pGetParameterOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pGetParameterOperation.setInvokeId(eeaRcfO.getInvokeId().value.intValue());

                // the result
                if (eeaRcfO.getResult().getPositiveResult() != null)
                {
                    decodeParameter(eeaRcfO.getResult().getPositiveResult(), pGetParameterOperation);
                    pGetParameterOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eeaRcfO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        int specValue = eeaRcfO.getResult().getNegativeResult().getSpecific().value.intValue();
                        pGetParameterOperation.setGetParameterDiagnostic(RCF_GetParameterDiagnostic
                                .getGetParamDiagByCode(specValue));
                    }
                    else
                    {
                        // common diagnostic
                        int commValue = eeaRcfO.getResult().getNegativeResult().getCommon().value.intValue();
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
     * Fills the RCF GET-PARAMETER return operation from the object for version
     * 1. S_OK The RCF GetParameter operation has been decoded. E_FAIL Unable to
     * decode the RCF GetParameter operation.
     * 
     * @throws SleApiException
     */
    private IRCF_GetParameter decodeGetParameterReturnOpV2To4(RcfGetParameterReturnPduV2To4 eeaRcfO) throws SleApiException
    {
        IRCF_GetParameter pGetParameterOperation = null;
        ISLE_Operation pOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_getParameter;

        pOperation = this.pduTranslator.getReturnOp(eeaRcfO.getInvokeId(), opType);
        if (pOperation != null)
        {
            pGetParameterOperation = pOperation.queryInterface(IRCF_GetParameter.class);
            if (pGetParameterOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaRcfO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pGetParameterOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pGetParameterOperation.setInvokeId(eeaRcfO.getInvokeId().value.intValue());

                // the result
                if (eeaRcfO.getResult().getPositiveResult() != null)
                {
                    decodeParameterV2To4(eeaRcfO.getResult().getPositiveResult(), pGetParameterOperation);
                    pGetParameterOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eeaRcfO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        int specValue = eeaRcfO.getResult().getNegativeResult().getSpecific().value.intValue();
                        pGetParameterOperation.setGetParameterDiagnostic(RCF_GetParameterDiagnostic
                                .getGetParamDiagByCode(specValue));
                    }
                    else
                    {
                        // common diagnostic
                        int commValue = eeaRcfO.getResult().getNegativeResult().getCommon().value.intValue();
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
     * Fills the RCF Parameter of the Asn1 object for SLE V2 .. V4
     * @throws IOException 
     */
    private void encodeParameterV2To4(IRCF_GetParameter pGetParameterOperation, RcfGetParameterV2To4 eeaO) throws IOException
    {
        switch (pGetParameterOperation.getReturnedParameter())
        {
        case rcfPN_bufferSize:
        {
            RcfGetParameterV2To4.ParBufferSize bufferSize = new RcfGetParameterV2To4.ParBufferSize();
            bufferSize.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_bufferSize.getCode()));
            bufferSize.setParameterValue(new IntPosShort(pGetParameterOperation.getTransferBufferSize()));
            eeaO.setParBufferSize(bufferSize);
            break;
        }
        case rcfPN_deliveryMode:
        {
        	RcfGetParameterV2To4.ParDeliveryMode parDeliveryMode = new RcfGetParameterV2To4.ParDeliveryMode();
            parDeliveryMode.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_deliveryMode.getCode()));
            parDeliveryMode.setParameterValue(new RcfDeliveryMode(pGetParameterOperation.getDeliveryMode().getCode()));
            eeaO.setParDeliveryMode(parDeliveryMode);
            break;
        }
        case rcfPN_latencyLimit:
        {
            int latencyLimit = pGetParameterOperation.getLatencyLimit();
            RcfGetParameterV2To4.ParLatencyLimit parLatencyLimit = new RcfGetParameterV2To4.ParLatencyLimit();
            parLatencyLimit.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_latencyLimit.getCode()));
            if (latencyLimit == 0)
            {
                parLatencyLimit.setParameterValue(new RcfGetParameterV2To4.ParLatencyLimit.ParameterValue());
            }
            else
            {
            	RcfGetParameterV2To4.ParLatencyLimit.ParameterValue pv = new RcfGetParameterV2To4.ParLatencyLimit.ParameterValue();
            	pv.setOnline(new IntPosShort(latencyLimit));
                parLatencyLimit.setParameterValue(pv);
            }
            eeaO.setParLatencyLimit(parLatencyLimit);
            break;
        }
        case rcfPN_permittedGvcidSet:
        {
        	RcfGetParameterV2To4.ParPermittedGvcidSet parPermittedGvcidSet = new RcfGetParameterV2To4.ParPermittedGvcidSet();
            parPermittedGvcidSet.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_permittedGvcidSet.getCode()));
            GvcIdSetV1To4 parSet = new GvcIdSetV1To4();
            encodeGvcidSetV1To4(pGetParameterOperation, parSet);
            parPermittedGvcidSet.setParameterValue(parSet);
            eeaO.setParPermittedGvcidSet(parPermittedGvcidSet);
            break;
        }
        case rcfPN_reportingCycle:
        {
            long reportingCycle = pGetParameterOperation.getReportingCycle();
            RcfGetParameterV2To4.ParReportingCycle parReportingCycle = new RcfGetParameterV2To4.ParReportingCycle();
            parReportingCycle.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_reportingCycle.getCode()));
            if (reportingCycle == 0)
            {
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOff(new BerNull());
                parReportingCycle.setParameterValue(crc);
            }
            else
            {
                //parReportingCycle.setParameterValue(new CurrentReportingCycle(null, new ReportingCycle(reportingCycle));
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOn(new ReportingCycle(reportingCycle));
            	parReportingCycle.setParameterValue(crc);
            }
            eeaO.setParReportingCycle(parReportingCycle);
            break;
        }
        case rcfPN_requestedGvcid:
        {
        	RcfGetParameterV2To4.ParReqGvcId parReqGvcid = new RcfGetParameterV2To4.ParReqGvcId();
            parReqGvcid.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_requestedGvcid.getCode()));
            RequestedGvcId rgvcId = new RequestedGvcId();
            rgvcId.setGvcid(encodeGvcid(pGetParameterOperation.getRequestedGvcid()));
            parReqGvcid.setParameterValue(rgvcId);
            //parReqGvcid.setParameterValue(new RequestedGvcId(encodeGvcid(pGetParameterOperation.getRequestedGvcid()),
            //                                                null);
            eeaO.setParReqGvcId(parReqGvcid);
            break;
        }
        case rcfPN_returnTimeoutPeriod:
        {
        	RcfGetParameterV2To4.ParReturnTimeout parRetTimeout = new RcfGetParameterV2To4.ParReturnTimeout();
            parRetTimeout.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_returnTimeoutPeriod.getCode()));
            parRetTimeout.setParameterValue(new TimeoutPeriod(pGetParameterOperation.getReturnTimeoutPeriod()));
            eeaO.setParReturnTimeout(parRetTimeout);
            break;
        }
        default:
        {
            break;
        }
        }
    }
    
    /**
     * Fills the RCF Parameter of the Asn1 object for SLE version V5 and later.
     * @throws IOException 
     */
    private void encodeParameter(IRCF_GetParameter pGetParameterOperation, RcfGetParameter eeaO) throws IOException
    {
        switch (pGetParameterOperation.getReturnedParameter())
        {
        case rcfPN_bufferSize:
        {
            RcfGetParameter.ParBufferSize bufferSize = new RcfGetParameter.ParBufferSize();
            bufferSize.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_bufferSize.getCode()));
            bufferSize.setParameterValue(new IntPosShort(pGetParameterOperation.getTransferBufferSize()));
            eeaO.setParBufferSize(bufferSize);
            break;
        }
        case rcfPN_deliveryMode:
        {
            RcfGetParameter.ParDeliveryMode parDeliveryMode = new RcfGetParameter.ParDeliveryMode();
            parDeliveryMode.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_deliveryMode.getCode()));
            parDeliveryMode.setParameterValue(new RcfDeliveryMode(pGetParameterOperation.getDeliveryMode().getCode()));
            eeaO.setParDeliveryMode(parDeliveryMode);
            break;
        }
        case rcfPN_latencyLimit:
        {
            int latencyLimit = pGetParameterOperation.getLatencyLimit();
            RcfGetParameter.ParLatencyLimit parLatencyLimit = new RcfGetParameter.ParLatencyLimit();
            parLatencyLimit.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_latencyLimit.getCode()));
            if (latencyLimit == 0)
            {
                parLatencyLimit.setParameterValue(new RcfGetParameter.ParLatencyLimit.ParameterValue());
            }
            else
            {
            	RcfGetParameter.ParLatencyLimit.ParameterValue pv = new RcfGetParameter.ParLatencyLimit.ParameterValue();
            	pv.setOnline(new IntPosShort(latencyLimit));
                parLatencyLimit.setParameterValue(pv);
            }
            eeaO.setParLatencyLimit(parLatencyLimit);
            break;
        }
        case rcfPN_permittedGvcidSet:
        {
            RcfGetParameter.ParPermittedGvcidSet parPermittedGvcidSet = new RcfGetParameter.ParPermittedGvcidSet();
            parPermittedGvcidSet.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_permittedGvcidSet.getCode()));
            GvcIdSet parSet = new GvcIdSet();
            encodeGvcidSet(pGetParameterOperation, parSet);
            parPermittedGvcidSet.setParameterValue(parSet);
            eeaO.setParPermittedGvcidSet(parPermittedGvcidSet);
            break;
        }
        case rcfPN_reportingCycle:
        {
            long reportingCycle = pGetParameterOperation.getReportingCycle();
            RcfGetParameter.ParReportingCycle parReportingCycle = new RcfGetParameter.ParReportingCycle();
            parReportingCycle.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_reportingCycle.getCode()));
            if (reportingCycle == 0)
            {
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOff(new BerNull());
                parReportingCycle.setParameterValue(crc);
            }
            else
            {
                //parReportingCycle.setParameterValue(new CurrentReportingCycle(null, new ReportingCycle(reportingCycle));
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOn(new ReportingCycle(reportingCycle));
            	parReportingCycle.setParameterValue(crc);
            }
            eeaO.setParReportingCycle(parReportingCycle);
            break;
        }
        case rcfPN_requestedGvcid:
        {
            RcfGetParameter.ParReqGvcId parReqGvcid = new RcfGetParameter.ParReqGvcId();
            parReqGvcid.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_requestedGvcid.getCode()));
            RequestedGvcId rgvcId = new RequestedGvcId();
            rgvcId.setGvcid(encodeGvcid(pGetParameterOperation.getRequestedGvcid()));
            parReqGvcid.setParameterValue(rgvcId);
            //parReqGvcid.setParameterValue(new RequestedGvcId(encodeGvcid(pGetParameterOperation.getRequestedGvcid()),
            //                                                null);
            eeaO.setParReqGvcId(parReqGvcid);
            break;
        }
        case rcfPN_returnTimeoutPeriod:
        {
            RcfGetParameter.ParReturnTimeout parRetTimeout = new RcfGetParameter.ParReturnTimeout();
            parRetTimeout.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_returnTimeoutPeriod.getCode()));
            parRetTimeout.setParameterValue(new TimeoutPeriod(pGetParameterOperation.getReturnTimeoutPeriod()));
            eeaO.setParReturnTimeout(parRetTimeout);
            break;
        }
        case rcfPN_minReportingCycle:
        {
        	// SLE parameter id 301 - RF parameter id 7
        	RcfGetParameter.ParMinReportingCycle parMinRepCycle = new RcfGetParameter.ParMinReportingCycle();
            parMinRepCycle.setParameterName(new ParameterName(RCF_ParameterName.rcfPN_minReportingCycle.getCode()));
            parMinRepCycle.setParameterValue(new IntPosShort(pGetParameterOperation.getMinimumReportingCycle()));
            eeaO.setParMinReportingCycle(parMinRepCycle);
            break;
        }
        default:
        {
            break;
        }
        }
    }

    /**
     * Fills the parameter of the GetParameter Return operation from the Asn1
     * object for version 2..4.
     */
    private void decodeParameterV2To4(RcfGetParameterV2To4 eeaO, IRCF_GetParameter pGetParameterOperation)
    {
        if (eeaO.getParBufferSize() != null)
        {
            pGetParameterOperation.setTransferBufferSize(eeaO.getParBufferSize().getParameterValue().value.longValue());
        }
        else if (eeaO.getParDeliveryMode() != null)
        {
            pGetParameterOperation.setDeliveryMode(RCF_DeliveryMode
                    .getRCFDelModeByCode(eeaO.getParDeliveryMode().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParLatencyLimit() != null)
        {
            if (eeaO.getParLatencyLimit().getParameterValue().getOnline() != null)
            {
                pGetParameterOperation.setLatencyLimit(eeaO.getParLatencyLimit().getParameterValue().getOnline().value.intValue());
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
        else if (eeaO.getParPermittedGvcidSet() != null)
        {
            decodeGvcidSetV1To4(eeaO.getParPermittedGvcidSet().getParameterValue(), pGetParameterOperation);
        }
        else if (eeaO.getParReqGvcId() != null)
        {
            pGetParameterOperation.setRequestedGvcid(decodeGvcid(eeaO.getParReqGvcId().getParameterValue().getGvcid()));
        }
        else if (eeaO.getParReturnTimeout() != null)
        {
            pGetParameterOperation.setReturnTimeoutPeriod(eeaO.getParReturnTimeout().getParameterValue().value.longValue());
        }
    }
    
    
    /**
     * Fills the parameter of the GetParameter Return operation from the Asn1
     * object for version >=5.
     */
    private void decodeParameter(RcfGetParameter eeaO, IRCF_GetParameter pGetParameterOperation)
    {
        if (eeaO.getParBufferSize() != null)
        {
            pGetParameterOperation.setTransferBufferSize(eeaO.getParBufferSize().getParameterValue().value.longValue());
        }
        else if (eeaO.getParDeliveryMode() != null)
        {
            pGetParameterOperation.setDeliveryMode(RCF_DeliveryMode
                    .getRCFDelModeByCode(eeaO.getParDeliveryMode().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParLatencyLimit() != null)
        {
            if (eeaO.getParLatencyLimit().getParameterValue().getOnline() != null)
            {
                pGetParameterOperation.setLatencyLimit(eeaO.getParLatencyLimit().getParameterValue().getOnline().value.intValue());
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
        else if (eeaO.getParPermittedGvcidSet() != null)
        {
            decodeGvcidSet(eeaO.getParPermittedGvcidSet().getParameterValue(), pGetParameterOperation);
        }
        else if (eeaO.getParReqGvcId() != null)
        {
            pGetParameterOperation.setRequestedGvcid(decodeGvcid(eeaO.getParReqGvcId().getParameterValue().getGvcid()));
        }
        else if (eeaO.getParReturnTimeout() != null)
        {
            pGetParameterOperation.setReturnTimeoutPeriod(eeaO.getParReturnTimeout().getParameterValue().value.longValue());
        }
        else if (eeaO.getParMinReportingCycle() != null)
        {
            pGetParameterOperation.setMinimumReportingCycle(eeaO.getParMinReportingCycle().getParameterValue().value.longValue());
        }
    }
}
