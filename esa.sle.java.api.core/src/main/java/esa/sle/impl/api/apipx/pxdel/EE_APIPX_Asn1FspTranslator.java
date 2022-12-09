/**
 * @(#) EE_APIPX_Asn1FspTranslator.java
 */

package esa.sle.impl.api.apipx.pxdel;

import isp1.fsp.pdus.FspAsyncNotifyInvocationPdu;
import isp1.fsp.pdus.FspAsyncNotifyInvocationPduV1To4;
import isp1.fsp.pdus.FspGetParameterInvocationPdu;
import isp1.fsp.pdus.FspGetParameterReturnPdu;
import isp1.fsp.pdus.FspGetParameterReturnPduV2to4;
import isp1.fsp.pdus.FspGetParameterReturnV1Pdu;
import isp1.fsp.pdus.FspInvokeDirectiveInvocationPdu;
import isp1.fsp.pdus.FspInvokeDirectiveReturnPdu;
import isp1.fsp.pdus.FspScheduleStatusReportInvocationPdu;
import isp1.fsp.pdus.FspScheduleStatusReportReturnPdu;
import isp1.fsp.pdus.FspStartInvocationPdu;
import isp1.fsp.pdus.FspStartReturnPdu;
import isp1.fsp.pdus.FspStatusReportInvocationPdu;
import isp1.fsp.pdus.FspStopInvocationPdu;
import isp1.fsp.pdus.FspStopReturnPdu;
import isp1.fsp.pdus.FspThrowEventInvocationPdu;
import isp1.fsp.pdus.FspThrowEventReturnPdu;
import isp1.fsp.pdus.FspTransferDataInvocationPdu;
import isp1.fsp.pdus.FspTransferDataReturnPdu;
import isp1.sle.bind.pdus.SleBindInvocationPdu;
import isp1.sle.bind.pdus.SleBindReturnPdu;
import isp1.sle.bind.pdus.SleUnbindInvocationPdu;
import isp1.sle.bind.pdus.SleUnbindReturnPdu;

import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.beanit.jasn1.ber.BerTag;
import com.beanit.jasn1.ber.ReverseByteArrayOutputStream ;
//import com.beanit.jasn1.ber.BerIdentifier;
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
import ccsds.sle.api.isrv.ifsp.IFSP_AsyncNotify;
import ccsds.sle.api.isrv.ifsp.IFSP_GetParameter;
import ccsds.sle.api.isrv.ifsp.IFSP_InvokeDirective;
import ccsds.sle.api.isrv.ifsp.IFSP_Start;
import ccsds.sle.api.isrv.ifsp.IFSP_StatusReport;
import ccsds.sle.api.isrv.ifsp.IFSP_ThrowEvent;
import ccsds.sle.api.isrv.ifsp.IFSP_TransferData;
import ccsds.sle.api.isrv.ifsp.types.FSP_AbsolutePriority;
import ccsds.sle.api.isrv.ifsp.types.FSP_BlockingUsage;
import ccsds.sle.api.isrv.ifsp.types.FSP_ChannelType;
import ccsds.sle.api.isrv.ifsp.types.FSP_ClcwGvcId;
import ccsds.sle.api.isrv.ifsp.types.FSP_ClcwPhysicalChannel;
import ccsds.sle.api.isrv.ifsp.types.FSP_DirectiveTimeoutType;
import ccsds.sle.api.isrv.ifsp.types.FSP_FopAlert;
import ccsds.sle.api.isrv.ifsp.types.FSP_FopState;
import ccsds.sle.api.isrv.ifsp.types.FSP_GetParameterDiagnostic;
import ccsds.sle.api.isrv.ifsp.types.FSP_GvcId;
import ccsds.sle.api.isrv.ifsp.types.FSP_ConfType;
import ccsds.sle.api.isrv.ifsp.types.FSP_InvokeDirectiveDiagnostic;
import ccsds.sle.api.isrv.ifsp.types.FSP_MuxScheme;
import ccsds.sle.api.isrv.ifsp.types.FSP_NotificationType;
import ccsds.sle.api.isrv.ifsp.types.FSP_PacketStatus;
import ccsds.sle.api.isrv.ifsp.types.FSP_ParameterName;
import ccsds.sle.api.isrv.ifsp.types.FSP_PermittedTransmissionMode;
import ccsds.sle.api.isrv.ifsp.types.FSP_ProductionStatus;
import ccsds.sle.api.isrv.ifsp.types.FSP_StartDiagnostic;
import ccsds.sle.api.isrv.ifsp.types.FSP_ThrowEventDiagnostic;
import ccsds.sle.api.isrv.ifsp.types.FSP_TimeoutType;
import ccsds.sle.api.isrv.ifsp.types.FSP_TransferDataDiagnostic;
import ccsds.sle.api.isrv.ifsp.types.FSP_TransmissionMode;
import ccsds.sle.transfer.service.fsp.structures.GvcId;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParMinReportingCycle;
import ccsds.sle.transfer.service.common.pdus.ReportingCycle;
import ccsds.sle.transfer.service.common.types.ConditionalTime;
import ccsds.sle.transfer.service.common.types.Diagnostics;
import ccsds.sle.transfer.service.common.types.Duration;
import ccsds.sle.transfer.service.common.types.IntPosLong;
import ccsds.sle.transfer.service.common.types.IntPosShort;
import ccsds.sle.transfer.service.common.types.IntUnsignedLong;
import ccsds.sle.transfer.service.common.types.InvokeId;
import ccsds.sle.transfer.service.common.types.ParameterName;
import ccsds.sle.transfer.service.common.types.SlduStatusNotification;
import ccsds.sle.transfer.service.common.types.Time;
import ccsds.sle.transfer.service.fsp.incoming.pdus.FspInvokeDirectiveInvocation.Directive;
import ccsds.sle.transfer.service.fsp.outgoing.pdus.FspStartReturn;
import ccsds.sle.transfer.service.fsp.outgoing.pdus.FspGetParameterReturn;
import ccsds.sle.transfer.service.fsp.outgoing.pdus.FspTransferDataReturn;
import ccsds.sle.transfer.service.fsp.outgoing.pdus.FspGetParameterReturnV1;
import ccsds.sle.transfer.service.fsp.outgoing.pdus.FspInvokeDirectiveReturn;
import ccsds.sle.transfer.service.fsp.outgoing.pdus.FspThrowEventReturn;
import ccsds.sle.transfer.service.fsp.structures.AbsolutePriority;
import ccsds.sle.transfer.service.fsp.structures.Apid;
import ccsds.sle.transfer.service.fsp.structures.ApidList;
import ccsds.sle.transfer.service.fsp.structures.ApidListV1to4;
import ccsds.sle.transfer.service.fsp.structures.BlockingTimeoutPeriod;
import ccsds.sle.transfer.service.fsp.structures.BlockingUsage;
import ccsds.sle.transfer.service.fsp.structures.BufferSize;
import ccsds.sle.transfer.service.fsp.structures.ClcwGvcId;
import ccsds.sle.transfer.service.fsp.structures.ClcwPhysicalChannel;
import ccsds.sle.transfer.service.fsp.structures.CurrentReportingCycle;
import ccsds.sle.transfer.service.fsp.structures.DiagnosticFspGet;
import ccsds.sle.transfer.service.fsp.structures.DiagnosticFspInvokeDirective;
import ccsds.sle.transfer.service.fsp.structures.DiagnosticFspStart;
import ccsds.sle.transfer.service.fsp.structures.DiagnosticFspThrowEvent;
import ccsds.sle.transfer.service.fsp.structures.DiagnosticFspTransferData;
import ccsds.sle.transfer.service.fsp.structures.DirectiveExecutedId;
import ccsds.sle.transfer.service.fsp.structures.EventInvocationId;
import ccsds.sle.transfer.service.fsp.structures.FopAlert;
import ccsds.sle.transfer.service.fsp.structures.FspData;
import ccsds.sle.transfer.service.fsp.structures.FspDeliveryMode;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParApidList;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParBitLockRequired;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParBlockingTimeout;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParBlockingTimeout.ParameterValue;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParBlockingUsage;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParClcwGlobalVcId;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParClcwPhysicalChannel;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParCopCntrFramesRepetition;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParDeliveryMode;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParDirInvocOnl;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParDirectiveInvoc;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParExpectDirectiveId;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParExpectEventInvId;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParExpectSlduId;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParFopSlidWindow;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParFopState;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParMapList;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParMapMuxControl;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParMapMuxScheme;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParMaxFrameLength;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParMaxPacketLength;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParPermTransMode;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParReportingCycle;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParReturnTimeout;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParRfAvailableRequired;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParSegmHeader;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParSequCntrFramesRepetition;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParThrowEventOperation;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParTimeoutType;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParTimerInitial;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParTrFrSeqNumber;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParTransmissLimit;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParVcMuxControl;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParVcMuxScheme;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameter.ParVirtualChannel;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameterV1;
import ccsds.sle.transfer.service.fsp.structures.FspGetParameterV2to4;
import ccsds.sle.transfer.service.fsp.structures.FspNotification;
import ccsds.sle.transfer.service.fsp.structures.FspNotificationV1To4;
import ccsds.sle.transfer.service.fsp.structures.FspPacketCount;
import ccsds.sle.transfer.service.fsp.structures.FspPacketLastOk;
import ccsds.sle.transfer.service.fsp.structures.FspPacketLastOk.PacketOk;
import ccsds.sle.transfer.service.fsp.structures.FspPacketLastProcessed;
import ccsds.sle.transfer.service.fsp.structures.FspPacketLastProcessed.PacketProcessed;
import ccsds.sle.transfer.service.fsp.structures.FspPacketStatus;
import ccsds.sle.transfer.service.fsp.structures.FspParameterName;
import ccsds.sle.transfer.service.fsp.structures.FspProductionStatus;
import ccsds.sle.transfer.service.fsp.structures.Map;
import ccsds.sle.transfer.service.fsp.structures.MapId;
import ccsds.sle.transfer.service.fsp.structures.MapList;
import ccsds.sle.transfer.service.fsp.structures.MapList.MapsUsed;
import ccsds.sle.transfer.service.fsp.structures.MapMuxControl;
//import ccsds.sle.transfer.service.fsp.structures.AbsolutePriority;
import ccsds.sle.transfer.service.fsp.structures.MapMuxControl.PollingVector;
import ccsds.sle.transfer.service.fsp.structures.MapMuxSchemeV1;
import ccsds.sle.transfer.service.fsp.structures.MuxControl;
import ccsds.sle.transfer.service.fsp.structures.MuxControl.MuxSchemeIsPriority;
import ccsds.sle.transfer.service.fsp.structures.MuxControl.MuxSchemeIsVector;
import ccsds.sle.transfer.service.fsp.structures.MuxScheme;
import ccsds.sle.transfer.service.fsp.structures.NegativeConfirmResponseToDirective;
import ccsds.sle.transfer.service.fsp.structures.PacketIdentification;
import ccsds.sle.transfer.service.fsp.structures.PacketIdentificationList;
import ccsds.sle.transfer.service.fsp.structures.PacketRadiatedInfo;
import ccsds.sle.transfer.service.fsp.structures.PermittedTransmissionMode;
import ccsds.sle.transfer.service.fsp.structures.Priority;
import ccsds.sle.transfer.service.fsp.structures.ProductionTime;
import ccsds.sle.transfer.service.fsp.structures.TimeoutPeriod;
import ccsds.sle.transfer.service.fsp.structures.TransmissionMode;
import ccsds.sle.transfer.service.fsp.structures.VcOrMapId;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class encodes and decodes FSP PDU's. When decoding, the decoded FSP
 * operation is instantiated.
 */
public class EE_APIPX_Asn1FspTranslator extends EE_APIPX_Asn1SleTranslator
{

    private static final Logger LOG = Logger.getLogger(EE_APIPX_Asn1FspTranslator.class.getName());


    /**
     * Constructor of the class which takes the ASNSDK context object as
     * parameter.
     */
    public EE_APIPX_Asn1FspTranslator(ISLE_OperationFactory pOpFactory,
                                      ISLE_UtilFactory pUtilFactory,
                                      EE_APIPX_PDUTranslator pdutranslator,
                                      int sleVersionNumber)
    {
        super(pOpFactory, pUtilFactory, pdutranslator, sleVersionNumber);
        this.serviceType = SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt;
    }

    /**
     * Allocates and fills the object used for the encoding of Fsp Operation for
     * version 1 PDUs. S_OK The FSP operation has been encoded. E_FAIL Unable to
     * encode the FSP operation.
     * 
     * @throws SleApiException
     * @throws IOException
     */
    public byte[] encodeFspOp(ISLE_Operation pFspOperation, boolean isInvoke) throws SleApiException, IOException
    {
        ReverseByteArrayOutputStream  berBAOStream = new ReverseByteArrayOutputStream (10, true);

        switch (pFspOperation.getOperationType())
        {
        case sleOT_bind:
        {
            if (isInvoke)
            {
                SleBindInvocationPdu obj = new SleBindInvocationPdu();
                encodeBindInvokeOp(pFspOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                SleBindReturnPdu obj = new SleBindReturnPdu();
                encodeBindReturnOp(pFspOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_unbind:
        {
            if (isInvoke)
            {
                SleUnbindInvocationPdu obj = new SleUnbindInvocationPdu();
                encodeUnbindInvokeOp(pFspOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                SleUnbindReturnPdu obj = new SleUnbindReturnPdu();
                encodeUnbindReturnOp(pFspOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_stop:
        {
            if (isInvoke)
            {
                FspStopInvocationPdu obj = new FspStopInvocationPdu();
                encodeStopInvokeOp(pFspOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                FspStopReturnPdu obj = new FspStopReturnPdu();
                encodeStopReturnOp(pFspOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_scheduleStatusReport:
        {
            if (isInvoke)
            {
                FspScheduleStatusReportInvocationPdu obj = new FspScheduleStatusReportInvocationPdu();
                encodeScheduleSRInvokeOp(pFspOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                FspScheduleStatusReportReturnPdu obj = new FspScheduleStatusReportReturnPdu();
                encodeScheduleSRReturnOp(pFspOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_start:
        {
            IFSP_Start pOp = null;
            pOp = pFspOperation.queryInterface(IFSP_Start.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    FspStartInvocationPdu obj = new FspStartInvocationPdu();
                    encodeStartInvokeOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
                else
                {
                    FspStartReturnPdu obj = new FspStartReturnPdu();
                    encodeStartReturnOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
            }

            break;
        }
        case sleOT_getParameter:
        {
            IFSP_GetParameter pOp = null;
            pOp = pFspOperation.queryInterface(IFSP_GetParameter.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    FspGetParameterInvocationPdu obj = new FspGetParameterInvocationPdu();
                    encodeGetParameterInvokeOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
                else
                {
                    if (this.sleVersionNumber == 1)
                    {
                        FspGetParameterReturnV1Pdu obj = new FspGetParameterReturnV1Pdu();
                        encodeGetParameterReturnOpV1(pOp, obj);
                        obj.encode(berBAOStream, true);
                    }
                    else if (this.sleVersionNumber <= 4)
                    {
                        FspGetParameterReturnPdu obj = new FspGetParameterReturnPdu();
                        encodeGetParameterReturnOpV4(pOp, obj);
                        obj.encode(berBAOStream, true);
                    }
                    else{
                    	// SLES V5 or later
                    	FspGetParameterReturnPdu obj = new FspGetParameterReturnPdu();
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
            IFSP_StatusReport pOp = null;
            pOp = pFspOperation.queryInterface(IFSP_StatusReport.class);
            if (pOp != null)
            {
                FspStatusReportInvocationPdu obj = new FspStatusReportInvocationPdu();
                encodeStatusReportOp(pOp, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_transferData:
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("sleOT_transferData encoding start");
            }
            IFSP_TransferData pOp = null;
            pOp = pFspOperation.queryInterface(IFSP_TransferData.class);
            if (pOp != null)
            {

                if (isInvoke)
                {
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("FspTransferDataInvocationPdu start");
                    }
                    FspTransferDataInvocationPdu obj = new FspTransferDataInvocationPdu();
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("FspTransferDataInvocationPdu stop");
                    }
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("encodeTransferDataInvokeOp start");
                    }
                    encodeTransferDataInvokeOp(pOp, obj);
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("encodeTransferDataInvokeOp stop");
                    }
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("encode start");
                    }
                    obj.encode(berBAOStream, true);
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("encode stop");
                    }
                }
                else
                {
                    FspTransferDataReturnPdu obj = new FspTransferDataReturnPdu();
                    encodeTransferDataReturnOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
            }
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("sleOT_transferData encoding end");
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
                IFSP_AsyncNotify pOp = null;
                pOp = pFspOperation.queryInterface(IFSP_AsyncNotify.class);
                if (pOp != null)
                {
                	if(this.sleVersionNumber > 4)
                	{
	                    FspAsyncNotifyInvocationPdu obj = new FspAsyncNotifyInvocationPdu();
	                    encodeAsyncNotifyOp(pOp, obj);
	                    obj.encode(berBAOStream, true);
                	}
                	else // SLEAPIJ-79
                	{
	                    FspAsyncNotifyInvocationPduV1To4 obj = new FspAsyncNotifyInvocationPduV1To4();
	                    encodeAsyncNotifyOpV1To4(pOp, obj);
	                    obj.encode(berBAOStream, true);
                	}                		
                }
            }

            break;
        }
        case sleOT_throwEvent:
        {
            IFSP_ThrowEvent pOp = null;
            pOp = pFspOperation.queryInterface(IFSP_ThrowEvent.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    FspThrowEventInvocationPdu obj = new FspThrowEventInvocationPdu();
                    encodeThrowEventInvokeOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
                else
                {
                    FspThrowEventReturnPdu obj = new FspThrowEventReturnPdu();
                    encodeThrowEventReturnOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
            }

            break;
        }
        case sleOT_transferBuffer:
        {
            berBAOStream.close();
            throw new SleApiException(HRESULT.E_FAIL, "No async notify return operation");
        }
        case sleOT_invokeDirective:
        {
            IFSP_InvokeDirective pOp = null;
            pOp = pFspOperation.queryInterface(IFSP_InvokeDirective.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    FspInvokeDirectiveInvocationPdu obj = new FspInvokeDirectiveInvocationPdu();
                    encodeInvokeDirectiveInvokeOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
                else
                {
                    FspInvokeDirectiveReturnPdu obj = new FspInvokeDirectiveReturnPdu();
                    encodeInvokeDirectiveReturnOp(pOp, obj);
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
     * Instantiates a new FSP operation from the version 1 object given as
     * parameter, and releases the object. S_OK A new FSP operation has been
     * Instantiated. E_FAIL Unable to instantiate a FSP operation.
     * 
     * @throws SleApiException
     * @throws IOException
     */
    public ISLE_Operation decodeFspOp(byte[] buffer, EE_Reference<Boolean> isInvoke) throws SleApiException,
                                                                                    IOException
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
        else if (identifier.equals(FspStartInvocationPdu.tag))
        {
            IFSP_Start pOp = null;
            SLE_OpType opTye = SLE_OpType.sleOT_start;
            pOp = this.operationFactory.createOperation(IFSP_Start.class,
                                                        opTye,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                FspStartInvocationPdu obj = new FspStartInvocationPdu();
                obj.decode(is, false);
                decodeStartInvokeOp(obj, pOp);

                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (identifier.equals(FspStartReturnPdu.tag))
        {

            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("FspStartReturnPdu: " + FspStartReturnPdu.tag);
            }
            IFSP_Start pOp = null;
            FspStartReturnPdu obj = new FspStartReturnPdu();
            obj.decode(is, false);
            pOp = decodeStartReturnOp(obj);
            isInvoke.setReference(new Boolean(false));

            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("FspStartReturnPdu pOp: " + pOp);
            }

            if (pOp != null)
            {
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (identifier.equals(FspStopInvocationPdu.tag))
        {
            FspStopInvocationPdu obj = new FspStopInvocationPdu();
            obj.decode(is, false);
            pOperation = decodeStopInvokeOp(obj);
            isInvoke.setReference(new Boolean(true));
        }
        else if (identifier.equals(FspStopReturnPdu.tag))
        {
            FspStopReturnPdu obj = new FspStopReturnPdu();
            obj.decode(is, false);
            pOperation = decodeStopReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
        }
        else if (identifier.equals(FspScheduleStatusReportInvocationPdu.tag))
        {
            FspScheduleStatusReportInvocationPdu obj = new FspScheduleStatusReportInvocationPdu();
            obj.decode(is, false);
            pOperation = decodeScheduleSRInvokeOp(obj);
            isInvoke.setReference(new Boolean(true));
        }
        else if (identifier.equals(FspScheduleStatusReportReturnPdu.tag))
        {
            FspScheduleStatusReportReturnPdu obj = new FspScheduleStatusReportReturnPdu();
            obj.decode(is, false);
            pOperation = decodeScheduleSRReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
        }
        else if (identifier.equals(FspGetParameterInvocationPdu.tag))
        {
            IFSP_GetParameter pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_getParameter;
            pOp = this.operationFactory.createOperation(IFSP_GetParameter.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                FspGetParameterInvocationPdu obj = new FspGetParameterInvocationPdu();
                obj.decode(is, false);
                decodeGetParameterInvokeOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (identifier.equals(FspGetParameterReturnPdu.tag))
        {
            IFSP_GetParameter pOp = null;
            if (this.sleVersionNumber == 1)
            {
            	// SLES V1
                FspGetParameterReturnV1Pdu obj = new FspGetParameterReturnV1Pdu();
                obj.decode(is, false);
                pOp = decodeGetParameterReturnOp(obj);
            }
            else if (this.sleVersionNumber <= 4)
            {
            	// SLES V2 .. V4
                FspGetParameterReturnPduV2to4 obj = new FspGetParameterReturnPduV2to4();
                obj.decode(is, false);
                pOp = decodeGetParameterReturnOpV2to4(obj);
            }
            else
            {
            	// SLES V5 and later
            	FspGetParameterReturnPdu obj = new FspGetParameterReturnPdu();
                obj.decode(is, false);
                pOp = decodeGetParameterReturnOp(obj);
            }

            isInvoke.setReference(new Boolean(false));
            if (pOp != null)
            {
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (identifier.equals(FspStatusReportInvocationPdu.tag))
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("FspStatusReportInvocationPdu: " + FspStatusReportInvocationPdu.tag);
            }
            IFSP_StatusReport pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_statusReport;
            pOp = this.operationFactory.createOperation(IFSP_StatusReport.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("FspStatusReportInvocationPdu: pOp" + pOp);
            }
            if (pOp != null)
            {
                FspStatusReportInvocationPdu obj = new FspStatusReportInvocationPdu();
                obj.decode(is, false);
                decodeStatusReportOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (identifier.equals(FspAsyncNotifyInvocationPdu.tag))
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("FspAsyncNotifyInvocationPdu: " + FspAsyncNotifyInvocationPdu.tag);
            }
            IFSP_AsyncNotify pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_asyncNotify;
            pOp = this.operationFactory.createOperation(IFSP_AsyncNotify.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);

            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("FspAsyncNotifyInvocationPdu pOp: " + pOp);
            }

            if (pOp != null)
            {            
            	if(this.sleVersionNumber > 4)
            	{
            		FspAsyncNotifyInvocationPdu obj = new FspAsyncNotifyInvocationPdu();
	                obj.decode(is, false);
	                decodeAsyncNotifyOp(obj, pOp);
            	}
            	else // SLEAPIJ-79
            	{
            		FspAsyncNotifyInvocationPduV1To4 obj = new FspAsyncNotifyInvocationPduV1To4();
	                obj.decode(is, false);
	                decodeAsyncNotifyOpV1To4(obj, pOp);
            		
            	}
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (identifier.equals(FspThrowEventInvocationPdu.tag))
        {
            IFSP_ThrowEvent pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_throwEvent;
            pOp = this.operationFactory.createOperation(IFSP_ThrowEvent.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                FspThrowEventInvocationPdu obj = new FspThrowEventInvocationPdu();
                obj.decode(is, false);
                decodeThrowEventInvokeOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (identifier.equals(FspThrowEventReturnPdu.tag))
        {
            IFSP_ThrowEvent pOp = null;
            FspThrowEventReturnPdu obj = new FspThrowEventReturnPdu();
            obj.decode(is, false);
            pOp = decodeThrowEventReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
            if (pOp != null)
            {
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (identifier.equals(FspTransferDataInvocationPdu.tag))
        {
            IFSP_TransferData pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_transferData;
            pOp = this.operationFactory.createOperation(IFSP_TransferData.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                FspTransferDataInvocationPdu obj = new FspTransferDataInvocationPdu();
                obj.decode(is, false);
                decodeTransferDataInvokeOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (identifier.equals(FspTransferDataReturnPdu.tag))
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("FspTransferDataReturnPdu: " + FspTransferDataReturnPdu.tag);
            }

            IFSP_TransferData pOp = null;
            FspTransferDataReturnPdu obj = new FspTransferDataReturnPdu();
            obj.decode(is, false);
            pOp = decodeTransferDataReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("pOp: " + pOp);
            }

            if (pOp != null)
            {
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (identifier.equals(FspInvokeDirectiveInvocationPdu.tag))
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("FspInvokeDirectiveInvocationPdu: " + FspInvokeDirectiveInvocationPdu.tag);
            }
            IFSP_InvokeDirective pOp = null;
            SLE_OpType optype = SLE_OpType.sleOT_invokeDirective;
            pOp = this.operationFactory.createOperation(IFSP_InvokeDirective.class,
                                                        optype,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                FspInvokeDirectiveInvocationPdu obj = new FspInvokeDirectiveInvocationPdu();
                obj.decode(is, false);
                decodeInvokeDirectiveInvokeOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (identifier.equals(FspInvokeDirectiveReturnPdu.tag))
        {
            IFSP_InvokeDirective pOp = null;
            FspInvokeDirectiveReturnPdu obj = new FspInvokeDirectiveReturnPdu();
            obj.decode(is, false);
            pOp = decodeInvokeDirectiveReturnOp(obj);
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
     * Fills the object used for the encoding of Fsp Start invoke operation.
     * S_OK The FSP Start operation has been encoded. E_FAIL Unable to encode
     * the FSP Start operation.
     * 
     * @throws SleApiException
     */
    private void encodeStartInvokeOp(IFSP_Start pStartOperation, FspStartInvocationPdu eeaFspO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pStartOperation.getInvokerCredentials();
        eeaFspO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaFspO.setInvokeId(new InvokeId(pStartOperation.getInvokeId()));

        // the first packet identification
        eeaFspO.setFirstPacketIdentification(new PacketIdentification(pStartOperation.getFirstPacketId()));
    }

    /**
     * Fills the FSP START invoke operation from the object. S_OK The FSP Start
     * operation has been decoded. E_FAIL Unable to decode the FSP Start
     * operation.
     * 
     * @throws SleApiException
     */
    private void decodeStartInvokeOp(FspStartInvocationPdu eeaFspO, IFSP_Start pStartOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaFspO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pStartOperation.putInvokerCredentials(pCredentials);
        }

        // the invoker id
        pStartOperation.setInvokeId((int) eeaFspO.getInvokeId().value.intValue());

        // the first packet identification
        pStartOperation.setFirstPacketId(eeaFspO.getFirstPacketIdentification().value.longValue());
    }

    /**
     * Fills the object used for the encoding of Fsp Start return operation.
     * S_OK The FSP Start operation has been encoded. E_FAIL Unable to encode
     * the FSP Start operation.
     * 
     * @throws SleApiException
     */
    private void encodeStartReturnOp(IFSP_Start pStartOperation, FspStartReturnPdu eeaFspO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pStartOperation.getPerformerCredentials();
        eeaFspO.setPerformerCredentials(encodeCredentials(pCredentials));

        // the invoker id
        eeaFspO.setInvokeId(new InvokeId(pStartOperation.getInvokeId()));

        // the result
        if (pStartOperation.getResult() == SLE_Result.sleRES_positive)
        {
            // positive result
        	FspStartReturn.Result.PositiveResult pRes = new FspStartReturn.Result.PositiveResult();

            // the production start time
            ISLE_Time pTime = null;
            pTime = pStartOperation.getStartProductionTime();
            Time startT = encodeTime(pTime);
            if (startT == null)
            {
                throw new SleApiException(HRESULT.E_FAIL);
            }

            pRes.setStartProductionTime(startT);

            // the production stop time
            pTime = pStartOperation.getStopProductionTime();
            ConditionalTime stopT = encodeConditionalTime(pTime);
            if (stopT == null)
            {
                throw new SleApiException(HRESULT.E_FAIL);
            }

            pRes.setStopProductionTime(stopT);
            FspStartReturn.Result result = new FspStartReturn.Result();
            result.setPositiveResult(pRes);
            eeaFspO.setResult(result);
            //eeaFspO.result = new Result(pRes, null);
        }
        else
        {
        	FspStartReturn.Result negResult = new FspStartReturn.Result();

            if (pStartOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                DiagnosticFspStart repSpecific = new DiagnosticFspStart();

                switch (pStartOperation.getStartDiagnostic())
                {
                case fspSTD_outOfService:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_StartDiagnostic.fspSTD_outOfService.getCode()));
                    break;
                }
                case fspSTD_unableToComply:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_StartDiagnostic.fspSTD_unableToComply.getCode()));
                    break;
                }
                case fspSTD_productionTimeExpired:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_StartDiagnostic.fspSTD_productionTimeExpired.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_StartDiagnostic.fspSTD_invalid.getCode()));
                    break;
                }
                }
                negResult.setNegativeResult(repSpecific);
                //negResult.negativeResult = repSpecific;
            }
            else
            {
                // common diagnostic
                DiagnosticFspStart repCommon = new DiagnosticFspStart();
                repCommon.setCommon(new Diagnostics(pStartOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }

            eeaFspO.setResult(negResult);
        }
    }

    /**
     * Fills the FSP START return operation from the object. S_OK The FSP Start
     * operation has been decoded. E_FAIL Unable to decode the FSP Start
     * operation.
     * 
     * @throws SleApiException
     */
    private IFSP_Start decodeStartReturnOp(FspStartReturnPdu eeaFspO) throws SleApiException
    {
        IFSP_Start pStartOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_start;
        ISLE_Operation pOperation = null;

        pOperation = this.pduTranslator.getReturnOp(eeaFspO.getInvokeId(), opType);
        if (pOperation != null)
        {
            pStartOperation = pOperation.queryInterface(IFSP_Start.class);
            if (pStartOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaFspO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pStartOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pStartOperation.setInvokeId((int) eeaFspO.getInvokeId().value.intValue());

                // the result
                if (eeaFspO.getResult().getPositiveResult() != null)
                {
                    pStartOperation.setPositiveResult();

                    // the production start time
                    ISLE_Time pTime = null;
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("eeaFspO.getResult().getPositiveResult().getStartProductionTime() "
                                   + eeaFspO.getResult().getPositiveResult().getStartProductionTime());
                    }
                    pTime = decodeTime(eeaFspO.getResult().getPositiveResult().getStartProductionTime());
                    if (pTime == null)
                    {
                        throw new SleApiException(HRESULT.E_FAIL);
                    }
                    else
                    {
                        pStartOperation.putStartProductionTime(pTime);
                    }

                    // the production stop time
                    pTime = null;
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("eeaFspO.getResult().getPositiveResult().getStopProductionTime() "
                                   + eeaFspO.getResult().getPositiveResult().getStopProductionTime());
                    }

                    pTime = decodeConditionalTime(eeaFspO.getResult().getPositiveResult().getStopProductionTime());
                    if (pTime != null)
                    {
                        pStartOperation.putStopProductionTime(pTime);
                    }

                }
                else
                {
                    // negative result
                    if (eeaFspO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        int specValue = (int) eeaFspO.getResult().getNegativeResult().getSpecific().value.intValue();
                        pStartOperation.setStartDiagnostic(FSP_StartDiagnostic.getStartDiagnosticByCode(specValue));
                    }
                    else
                    {
                        // common diagnostic
                        int commValue = (int) eeaFspO.getResult().getNegativeResult().getCommon().value.intValue();
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
     * Fills the object used for the encoding of Fsp GetParameter invoke
     * operation. S_OK The FSP GetParameter operation has been encoded. E_FAIL
     * Unable to encode the FSP GetParameter operation.
     * 
     * @throws SleApiException
     */
    private void encodeGetParameterInvokeOp(IFSP_GetParameter pGetParameterOperation,
                                            FspGetParameterInvocationPdu eeaFspO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pGetParameterOperation.getInvokerCredentials();
        eeaFspO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaFspO.setInvokeId(new InvokeId(pGetParameterOperation.getInvokeId()));

        // the parameter
        eeaFspO.setFspParameterName(new FspParameterName(pGetParameterOperation.getRequestedParameter().getCode()));

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(" eeaFspO.getFspParameterName(): " + eeaFspO.getFspParameterName());
        }
    }

    /**
     * Fills the FSP GET-PARAMETER invoke operation from the object. S_OK The
     * FSP GetParameter operation has been decoded. E_FAIL Unable to decode the
     * FSP GetParameter operation.
     * 
     * @throws SleApiException
     */
    private void decodeGetParameterInvokeOp(FspGetParameterInvocationPdu eeaFspO,
                                            IFSP_GetParameter pGetParameterOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaFspO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pGetParameterOperation.putInvokerCredentials(pCredentials);
        }

        // the invoke id
        pGetParameterOperation.setInvokeId((int) eeaFspO.getInvokeId().value.intValue());

        // the parameter
        pGetParameterOperation.setRequestedParameter(FSP_ParameterName
                .getParameterNameByCode((int) eeaFspO.getFspParameterName().value.intValue()));
    }
    
    /**
     * Fills the object used for the encoding of Fsp GetParameter return
     * operation. S_OK The FSP GetParameter operation has been encoded. E_FAIL
     * Unable to encode the FSP GetParameter operation.
     * 
     * This method is applicable for SLES V5 and later.
     * 
     * @throws SleApiException
     * @throws IOException 
     */
    private void encodeGetParameterReturnOp(IFSP_GetParameter pGetParameterOperation, FspGetParameterReturnPdu eeaFspO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pGetParameterOperation.getPerformerCredentials();
        eeaFspO.setPerformerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaFspO.setInvokeId(new InvokeId(pGetParameterOperation.getInvokeId()));

        // the result
        if (pGetParameterOperation.getResult() == SLE_Result.sleRES_positive)
        {
            FspGetParameter fspGetParam = new FspGetParameter();
            encodeParameter(pGetParameterOperation, fspGetParam);
            
            FspGetParameterReturn.Result posResult = new FspGetParameterReturn.Result();
            posResult.setPositiveResult(fspGetParam);
            eeaFspO.setResult(posResult);
            //eeaFspO.setResult(new ccsds.sle.transfer.service.fsp.outgoing.pdus.FspGetParameterReturn.SubChoice_result(fspGetParam, null);
        }
        else
        {
            ccsds.sle.transfer.service.fsp.outgoing.pdus.FspGetParameterReturn.Result negResult = new ccsds.sle.transfer.service.fsp.outgoing.pdus.FspGetParameterReturn.Result();

            if (pGetParameterOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                DiagnosticFspGet repSpecific = new DiagnosticFspGet();

                switch (pGetParameterOperation.getGetParameterDiagnostic())
                {
                case fspGP_unknownParameter:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_GetParameterDiagnostic.fspGP_unknownParameter.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_GetParameterDiagnostic.fspGP_invalid.getCode()));
                    break;
                }
                }

                negResult.setNegativeResult(repSpecific);
            }
            else
            {
                // common diagnostic
                DiagnosticFspGet repCommon = new DiagnosticFspGet();
                repCommon.setCommon(new Diagnostics(pGetParameterOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }

            eeaFspO.setResult(negResult);
        }
    }

    /**
     * Fills the object used for the encoding of Fsp GetParameter return
     * operation. S_OK The FSP GetParameter operation has been encoded. E_FAIL
     * Unable to encode the FSP GetParameter operation.
     * 
     * This method is applicable for SLES V2 .. V4
     * 
     * @throws SleApiException
     * @throws IOException 
     */
    private void encodeGetParameterReturnOpV4(IFSP_GetParameter pGetParameterOperation, FspGetParameterReturnPdu eeaFspO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pGetParameterOperation.getPerformerCredentials();
        eeaFspO.setPerformerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaFspO.setInvokeId(new InvokeId(pGetParameterOperation.getInvokeId()));

        // the result
        if (pGetParameterOperation.getResult() == SLE_Result.sleRES_positive)
        {
            FspGetParameter fspGetParam = new FspGetParameter();
            encodeParameterV4(pGetParameterOperation, fspGetParam);
            
            FspGetParameterReturn.Result posResult = new FspGetParameterReturn.Result();
            posResult.setPositiveResult(fspGetParam);
            eeaFspO.setResult(posResult);
            //eeaFspO.setResult(new ccsds.sle.transfer.service.fsp.outgoing.pdus.FspGetParameterReturn.SubChoice_result(fspGetParam, null);
        }
        else
        {
            ccsds.sle.transfer.service.fsp.outgoing.pdus.FspGetParameterReturn.Result negResult = new ccsds.sle.transfer.service.fsp.outgoing.pdus.FspGetParameterReturn.Result();

            if (pGetParameterOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                DiagnosticFspGet repSpecific = new DiagnosticFspGet();

                switch (pGetParameterOperation.getGetParameterDiagnostic())
                {
                case fspGP_unknownParameter:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_GetParameterDiagnostic.fspGP_unknownParameter.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_GetParameterDiagnostic.fspGP_invalid.getCode()));
                    break;
                }
                }

                negResult.setNegativeResult(repSpecific);
            }
            else
            {
                // common diagnostic
                DiagnosticFspGet repCommon = new DiagnosticFspGet();
                repCommon.setCommon(new Diagnostics(pGetParameterOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }

            eeaFspO.setResult(negResult);
        }
    }

    /**
     * Fills the FSP GET-PARAMETER return operation from the object. S_OK The
     * FSP GetParameter operation has been decoded. E_FAIL Unable to decode the
     * FSP GetParameter operation.
     * 
     * @throws SleApiException
     */
    private IFSP_GetParameter decodeGetParameterReturnOp(FspGetParameterReturnPdu eeaFspO) throws SleApiException
    {
        IFSP_GetParameter pGetParameterOperation = null;
        ISLE_Operation pOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_getParameter;

        pOperation = this.pduTranslator.getReturnOp(eeaFspO.getInvokeId(), opType);
        if (pOperation != null)
        {
            pGetParameterOperation = pOperation.queryInterface(IFSP_GetParameter.class);
            if (pGetParameterOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaFspO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pGetParameterOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pGetParameterOperation.setInvokeId((int) eeaFspO.getInvokeId().value.intValue());

                // the result
                if (eeaFspO.getResult().getPositiveResult() != null)
                {
                    decodeParameter(eeaFspO.getResult().getPositiveResult(), pGetParameterOperation);
                    pGetParameterOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eeaFspO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        int specValue = (int) eeaFspO.getResult().getNegativeResult().getSpecific().value.intValue();
                        pGetParameterOperation.setGetParameterDiagnostic(FSP_GetParameterDiagnostic
                                .getGetParamDiagByCode(specValue));
                    }
                    else
                    {
                        // common diagnostic
                        int commValue = (int) eeaFspO.getResult().getNegativeResult().getCommon().value.intValue();
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
     * Fills the FSP GET-PARAMETER return operation from the object. S_OK The
     * FSP GetParameter operation has been decoded. E_FAIL Unable to decode the
     * FSP GetParameter operation.
     * 
     * @throws SleApiException
     */
    private IFSP_GetParameter decodeGetParameterReturnOpV2to4(FspGetParameterReturnPduV2to4 eeaFspO) throws SleApiException
    {
        IFSP_GetParameter pGetParameterOperation = null;
        ISLE_Operation pOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_getParameter;

        pOperation = this.pduTranslator.getReturnOp(eeaFspO.getInvokeId(), opType);
        if (pOperation != null)
        {
            pGetParameterOperation = pOperation.queryInterface(IFSP_GetParameter.class);
            if (pGetParameterOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaFspO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pGetParameterOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pGetParameterOperation.setInvokeId((int) eeaFspO.getInvokeId().value.intValue());

                // the result
                if (eeaFspO.getResult().getPositiveResult() != null)
                {
                    decodeParameterV2to4(eeaFspO.getResult().getPositiveResult(), pGetParameterOperation);
                    pGetParameterOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eeaFspO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        int specValue = (int) eeaFspO.getResult().getNegativeResult().getSpecific().value.intValue();
                        pGetParameterOperation.setGetParameterDiagnostic(FSP_GetParameterDiagnostic
                                .getGetParamDiagByCode(specValue));
                    }
                    else
                    {
                        // common diagnostic
                        int commValue = (int) eeaFspO.getResult().getNegativeResult().getCommon().value.intValue();
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
     * Fills the object used for the encoding of Fsp TransferData invoke
     * operation. S_OK The FSP TransferData operation has been encoded. E_FAIL
     * Unable to encode the FSP TransferData operation.
     * 
     * @throws SleApiException
     */
    private void encodeTransferDataInvokeOp(IFSP_TransferData pTransferDataOperation,
                                            FspTransferDataInvocationPdu eeaFspO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pTransferDataOperation.getInvokerCredentials();
        eeaFspO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaFspO.setInvokeId(new InvokeId(pTransferDataOperation.getInvokeId()));

        // the fsp identification
        eeaFspO.setPacketIdentification(new PacketIdentification(pTransferDataOperation.getPacketId()));

        // the earliest production time
        ISLE_Time pTime = null;
        pTime = pTransferDataOperation.getEarliestProdTime();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("pTime: 1: " + pTime);
        }

        ConditionalTime cTime = encodeConditionalTime(pTime);
        if (cTime == null)
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }

        if (cTime.getKnown() != null)
        {
        	ProductionTime pt = new ProductionTime();
        	pt.setSpecified(cTime.getKnown());
            eeaFspO.setEarliestProductionTime(pt);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("eeaFspO.getEarliestProductionTime().getSpecified(): " + eeaFspO.getEarliestProductionTime().getSpecified());
            }

        }
        else
        {
        	ProductionTime pt = new ProductionTime();
        	pt.setUnspecified(new BerNull());
            eeaFspO.setEarliestProductionTime(pt);
        }

        // the latest production time
        pTime = null;
        pTime = pTransferDataOperation.getLatestProdTime();

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("pTime: 2: " + pTime);
        }

        cTime = encodeConditionalTime(pTime);

        if (cTime == null)
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }

        if (cTime.getKnown() != null)
        {
        	ProductionTime pt = new ProductionTime();
        	pt.setSpecified(cTime.getKnown());
            eeaFspO.setLatestProductionTime(pt);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("eeaFspO.getLatestProductionTime().getSpecified(): " + eeaFspO.getLatestProductionTime().getSpecified());
            }

        }
        else
        {
        	ProductionTime pt = new ProductionTime();
        	pt.setUnspecified(new BerNull());
            eeaFspO.setLatestProductionTime(pt);
        }

        // the delay time
        eeaFspO.setDelayTime(new Duration(pTransferDataOperation.getDelayTime()));

        // transmission mode
        eeaFspO.setTransmissionMode(new TransmissionMode(pTransferDataOperation.getTransmissionMode().getCode()));

        // map
        if (pTransferDataOperation.getMapIdUsed())
        {
        	Map map = new Map();
        	map.setMapUsed(new MapId(pTransferDataOperation.getMapId()));
        	eeaFspO.setMap(map);
            //eeaFspO.map = new Map(null, new MapId(pTransferDataOperation.getMapId()));
        }
        else
        {
        	Map map = new Map();
        	map.setNone(new BerNull());
            eeaFspO.setMap(map);
        }

        // blocking
        if (pTransferDataOperation.getBlocking() == SLE_YesNo.sleYN_No)
        {
            eeaFspO.setBlocking(new BlockingUsage(0));
        }
        else
        {
            eeaFspO.setBlocking(new BlockingUsage(1));
        }

        // processing started notification
        eeaFspO.setProcessingStartedNotification(new SlduStatusNotification(pTransferDataOperation
                .getProcessingStartedNotification().getCode()));

        // radiated notification
        eeaFspO.setRadiatedNotification(new SlduStatusNotification(pTransferDataOperation.getRadiatedNotification()
                .getCode()));

        // acknowledged notification
        eeaFspO.setAcknowledgedNotification(new SlduStatusNotification(pTransferDataOperation
                .getAcknowledgedNotification().getCode()));

        // the space link data unit
        byte[] pdata = pTransferDataOperation.getData();
        eeaFspO.setFspData(new FspData(pdata));
    }

    /**
     * Fills the FSP TRANSFER-DATA invoke operation from the object. S_OK The
     * FSP TransferData operation has been decoded. E_FAIL Unable to decode the
     * FSP TransferData operation.
     * 
     * @throws SleApiException
     */
    private void decodeTransferDataInvokeOp(FspTransferDataInvocationPdu eeaFspO,
                                            IFSP_TransferData pTransferDataOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaFspO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pTransferDataOperation.putInvokerCredentials(pCredentials);
        }

        // the invoke id
        pTransferDataOperation.setInvokeId((int) eeaFspO.getInvokeId().value.intValue());

        // the fsp identification
        pTransferDataOperation.setPacketId(eeaFspO.getPacketIdentification().value.longValue());

        // the earliest production time
        ISLE_Time pTime = null;
        if (eeaFspO.getEarliestProductionTime().getSpecified() != null)
        {
            pTime = decodeTime(eeaFspO.getEarliestProductionTime().getSpecified());
        }

        if (pTime != null)
        {
            pTransferDataOperation.putEarliestProdTime(pTime);
        }
        // the latest production time
        pTime = null;
        if (eeaFspO.getLatestProductionTime().getSpecified() != null)
        {
            pTime = decodeTime(eeaFspO.getLatestProductionTime().getSpecified());
        }

        if (pTime != null)
        {
            pTransferDataOperation.putLatestProdTime(pTime);
        }

        // the delay time
        pTransferDataOperation.setDelayTime(eeaFspO.getDelayTime().value.longValue());

        // transmission mode
        FSP_TransmissionMode transmissionMode = FSP_TransmissionMode
                .getTransmissionModeByCode(eeaFspO.getTransmissionMode().value.intValue());
        pTransferDataOperation.setTransmissionMode(transmissionMode);

        // map
        if (eeaFspO.getMap().getMapUsed() != null)
        {
            pTransferDataOperation.setMapId(eeaFspO.getMap().getMapUsed().value.longValue());
        }

        // blocking
        if (eeaFspO.getBlocking().value.intValue() == 0)
        {
            pTransferDataOperation.setBlocking(SLE_YesNo.sleYN_Yes);
        }
        else
        {
            pTransferDataOperation.setBlocking(SLE_YesNo.sleYN_No);
        }

        // the sldu status notification
        SLE_SlduStatusNotification notify = SLE_SlduStatusNotification
                .getSlduStatusNotificationByCode((int) eeaFspO.getProcessingStartedNotification().value.intValue());
        pTransferDataOperation.setProcessingStartedNotification(notify);

        notify = SLE_SlduStatusNotification.getSlduStatusNotificationByCode((int) eeaFspO.getRadiatedNotification().value.intValue());
        pTransferDataOperation.setRadiatedNotification(notify);

        notify = SLE_SlduStatusNotification
                .getSlduStatusNotificationByCode((int) eeaFspO.getAcknowledgedNotification().value.intValue());
        pTransferDataOperation.setAcknowledgedNotification(notify);

        // the space link data unit
        byte[] pdata = null;
        pdata = eeaFspO.getFspData().value;
        pTransferDataOperation.putData(pdata);
    }

    /**
     * Fills the object used for the encoding of Fsp TransferData return
     * operation. S_OK The FSP TransferData operation has been encoded. E_FAIL
     * Unable to encode the FSP TransferData operation.
     * 
     * @throws SleApiException
     */
    private void encodeTransferDataReturnOp(IFSP_TransferData pTransferDataOperation, FspTransferDataReturnPdu eeaFspO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pTransferDataOperation.getPerformerCredentials();
        eeaFspO.setPerformerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaFspO.setInvokeId(new InvokeId(pTransferDataOperation.getInvokeId()));

        // the fsp identification
        eeaFspO.setExpectedPacketIdentification(new PacketIdentification(pTransferDataOperation.getExpectedPacketId()));

        // the fsp buffer available
        eeaFspO.setPacketBufferAvailable(new BufferSize(pTransferDataOperation.getPacketBufferAvailable()));

        // the result
        if (pTransferDataOperation.getResult() == SLE_Result.sleRES_positive)
        {
            // positive result
        	FspTransferDataReturn.Result posResult = new FspTransferDataReturn.Result();
        	posResult.setPositiveResult(new BerNull());
            eeaFspO.setResult(posResult);
        }
        else
        {
            FspTransferDataReturn.Result negResult = new FspTransferDataReturn.Result();

            // negative result
            if (pTransferDataOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                // specific diagnostic
                DiagnosticFspTransferData repSpecific = new DiagnosticFspTransferData();

                switch (pTransferDataOperation.getTransferDataDiagnostic())
                {
                case fspXFD_unableToProcess:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_TransferDataDiagnostic.fspXFD_unableToProcess.getCode()));
                    break;
                }
                case fspXFD_unableToStore:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_TransferDataDiagnostic.fspXFD_unableToStore.getCode()));
                    break;
                }
                case fspXFD_packetIdOutOfSequence:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_TransferDataDiagnostic.fspXFD_packetIdOutOfSequence.getCode()));
                    break;
                }
                case fspXFD_duplicatePacketIdentification:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_TransferDataDiagnostic.fspXFD_duplicatePacketIdentification
                            .getCode()));
                    break;
                }
                case fspXFD_inconsistentTimeRange:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_TransferDataDiagnostic.fspXFD_inconsistentTimeRange.getCode()));
                    break;
                }
                case fspXFD_invalidTime:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_TransferDataDiagnostic.fspXFD_invalidTime.getCode()));
                    break;
                }
                case fspXFD_conflictingProductionTimeIntervals:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_TransferDataDiagnostic.fspXFD_conflictingProductionTimeIntervals
                            .getCode()));
                    break;
                }
                case fspXFD_lateSldu:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_TransferDataDiagnostic.fspXFD_lateSldu.getCode()));
                    break;
                }
                case fspXFD_invalidDelayTime:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_TransferDataDiagnostic.fspXFD_invalidDelayTime.getCode()));
                    break;
                }
                case fspXFD_invalidTransmissionMode:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_TransferDataDiagnostic.fspXFD_invalidTransmissionMode.getCode()));
                    break;
                }
                case fspXFD_invalidMap:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_TransferDataDiagnostic.fspXFD_invalidMap.getCode()));
                    break;
                }
                case fspXFD_invalidNotificationRequest:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_TransferDataDiagnostic.fspXFD_invalidNotificationRequest.getCode()));
                    break;
                }
                case fspXFD_packetTooLong:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_TransferDataDiagnostic.fspXFD_packetTooLong.getCode()));
                    break;
                }
                case fspXFD_unsupportedPacketVersion:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_TransferDataDiagnostic.fspXFD_unsupportedPacketVersion.getCode()));
                    break;
                }
                case fspXFD_incorrectPacketType:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_TransferDataDiagnostic.fspXFD_incorrectPacketType.getCode()));
                    break;
                }
                case fspXFD_invalidPacketApid:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_TransferDataDiagnostic.fspXFD_invalidPacketApid.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_GetParameterDiagnostic.fspGP_invalid.getCode()));
                    break;
                }
                }

                negResult.setNegativeResult(repSpecific);
            }
            else
            {
                // common diagnostic
                DiagnosticFspTransferData repCommon = new DiagnosticFspTransferData();
                repCommon.setCommon(new Diagnostics(pTransferDataOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }

            eeaFspO.setResult(negResult);
        }
    }

    /**
     * Fills the FSP TRANSFER-DATA return operation from the object. S_OK The
     * FSP TransferData operation has been decoded. E_FAIL Unable to decode the
     * FSP TransferData operation.
     * 
     * @throws SleApiException
     */
    private IFSP_TransferData decodeTransferDataReturnOp(FspTransferDataReturnPdu eeaFspO) throws SleApiException
    {
        ISLE_Operation pOperation = null;
        IFSP_TransferData pTransferDataOperation = null;
        pOperation = this.pduTranslator.getReturnOp(eeaFspO.getInvokeId(), SLE_OpType.sleOT_transferData);
        if (pOperation != null)
        {
            pTransferDataOperation = pOperation.queryInterface(IFSP_TransferData.class);

            // the performer credentials
            ISLE_Credentials pCredentials = null;
            pCredentials = decodeCredentials(eeaFspO.getPerformerCredentials());
            if (pCredentials != null)
            {
                pTransferDataOperation.putPerformerCredentials(pCredentials);
            }

            // the invoke id
            pTransferDataOperation.setInvokeId((int) eeaFspO.getInvokeId().value.intValue());

            // the fsp identification
            pTransferDataOperation.setExpectedPacketId(eeaFspO.getExpectedPacketIdentification().value.longValue());

            // the fsp buffer available
            pTransferDataOperation.setPacketBufferAvailable(eeaFspO.getPacketBufferAvailable().value.longValue());

            // the result
            if (eeaFspO.getResult().getPositiveResult() != null)
            {
                // positive result
                pTransferDataOperation.setPositiveResult();
            }
            else
            {
                // negative result
                if (eeaFspO.getResult().getNegativeResult().getSpecific() != null)
                {
                    // specific
                    int specDiag = (int) eeaFspO.getResult().getNegativeResult().getSpecific().value.intValue();
                    pTransferDataOperation.setTransferDataDiagnostic(FSP_TransferDataDiagnostic
                            .getTransferDataDiagnosticByCode(specDiag));
                }
                else
                {
                    // common
                    int commDiag = (int) eeaFspO.getResult().getNegativeResult().getCommon().value.intValue();
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
     * Decodes ASN1 parameter ClcwGvcId to FSP_ClcwGvcId
     * @param eeaO - type ClcwGvcId new for SLES V5
     * @return FSP_ClcwGvcId 
     */
    private FSP_ClcwGvcId decodeClcwGlobalVcid(ClcwGvcId eeaO)
    {
    	FSP_ClcwGvcId clcwgvcid = null;
    	FSP_GvcId gvcid = new FSP_GvcId();
        //gvcid.setScid((int) eeaO.getSpacecraftId().value.intValue());
        //gvcid.setVersion((int) eeaO.getVersionNumber().value.intValue());
    	if(eeaO.getCongigured() != null){
    		if (eeaO.getCongigured().getVcId().getMasterChannel() != null)
    		{
    			gvcid.setType(FSP_ChannelType.fspCT_MasterChannel);
    			gvcid.setVcid(0);
    			gvcid.setScid((int) eeaO.getCongigured().getSpacecraftId().value.intValue());
    			gvcid.setVersion((int) eeaO.getCongigured().getVersionNumber().value.intValue());
    			clcwgvcid = new FSP_ClcwGvcId(gvcid, FSP_ConfType.fspCT_configured);
    		}
    		else if (eeaO.getCongigured().getVcId().getVirtualChannel() != null)
    		{
    			gvcid.setType(FSP_ChannelType.fspCT_VirtualChannel);
    			gvcid.setVcid((int) eeaO.getCongigured().getVcId().getVirtualChannel().value.intValue());
    			gvcid.setScid((int) eeaO.getCongigured().getSpacecraftId().value.intValue());
    			gvcid.setVersion((int) eeaO.getCongigured().getVersionNumber().value.intValue());
    			clcwgvcid = new FSP_ClcwGvcId(gvcid, FSP_ConfType.fspCT_configured);
    		}
    		else
    		{
    			// Set to invalid
    			clcwgvcid = new FSP_ClcwGvcId(null, FSP_ConfType.fspCT_invalid);
    		}
    	}
    	else if(eeaO.getNotConfigured() != null)
    	{
    		// Set to notConfigured 		
    		clcwgvcid = new FSP_ClcwGvcId(null, FSP_ConfType.fspCT_notConfigured);
    	}
    	else
    	{
    		// Set to invalid
    		clcwgvcid = new FSP_ClcwGvcId(null, FSP_ConfType.fspCT_invalid);
    	}

        return clcwgvcid;
    }
    
    /**
     * Fills the object used for the encoding of Fsp AsyncNotify operation. S_OK
     * The FSP AsyncNotify operation has been encoded. E_FAIL Unable to encode
     * the FSP AsyncNotify operation.
     * 
     * @throws SleApiException
     * @throws IOException 
     */
    private void encodeAsyncNotifyOp(IFSP_AsyncNotify pAsyncNotifyOperation, FspAsyncNotifyInvocationPdu eeaFspO) throws SleApiException, IOException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pAsyncNotifyOperation.getInvokerCredentials();
        eeaFspO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the fsp notification
        eeaFspO.setFspNotification(new FspNotification());

        switch (pAsyncNotifyOperation.getNotificationType())
        {
        case fspNT_slduExpired:
        {
            PacketIdentificationList pil = new PacketIdentificationList();
        	encodePacketIdentificationList(pAsyncNotifyOperation, pil);
            eeaFspO.getFspNotification().setSlduExpired (pil);
            break;
        }
        case fspNT_productionInterrupted:
        {
            PacketIdentificationList pil = new PacketIdentificationList();
        	encodePacketIdentificationList(pAsyncNotifyOperation, pil);
            eeaFspO.getFspNotification().setProductionInterrupted(pil);
            break;
        }
        case fspNT_productionHalted:
        {
            PacketIdentificationList pil = new PacketIdentificationList();
        	encodePacketIdentificationList(pAsyncNotifyOperation, pil);
            eeaFspO.getFspNotification().setProductionHalted(pil);
            break;
        }
        case fspNT_productionOperational:
        {
            eeaFspO.getFspNotification().setProductionOperational(new BerNull());
            break;
        }
        case fspNT_bufferEmpty:
        {
            eeaFspO.getFspNotification().setBufferEmpty(new BerNull());
            break;
        }
        case fspNT_actionListCompleted:
        {
            eeaFspO.getFspNotification().setActionListCompleted(new EventInvocationId(pAsyncNotifyOperation.getEventThrownId()));
            break;
        }
        case fspNT_actionListNotCompleted:
        {
            eeaFspO.getFspNotification().setActionListNotCompleted(new EventInvocationId(pAsyncNotifyOperation.getEventThrownId()));
            break;
        }
        case fspNT_packetProcessingStarted:
        {
            eeaFspO.getFspNotification().setActionListNotCompleted(new EventInvocationId(pAsyncNotifyOperation.getEventThrownId()));
            break;
        }
        case fspNT_packetRadiated:
        {
            PacketIdentificationList pil = new PacketIdentificationList();
            encodePacketIdentificationList(pAsyncNotifyOperation, pil);
            PacketRadiatedInfo pri = new PacketRadiatedInfo();
            pri.setPacketIdentificationList(pil);
            pri.setFrameSequenceNumber(new BerInteger(pAsyncNotifyOperation.getFrameSequenceNumber()));
            eeaFspO.getFspNotification().setPacketRadiated(pri);
            break;
        }
        case fspNT_packetAcknowledged:
        {
            PacketIdentificationList pil = new PacketIdentificationList();
            encodePacketIdentificationList(pAsyncNotifyOperation, pil);
            eeaFspO.getFspNotification().setPacketAcknowledged(pil);
            break;
        }
        case fspNT_packetTransmissionModeMismatch:
        {
            PacketIdentificationList pil = new PacketIdentificationList();
        	encodePacketIdentificationList(pAsyncNotifyOperation, pil);
            eeaFspO.getFspNotification().setPacketTransmissionModeMismatch(pil);
            break;
        }
        case fspNT_transmissionModeCapabilityChange:
        {
            FSP_FopAlert alert = pAsyncNotifyOperation.getFopAlert();
            FopAlert tmcc = new FopAlert(alert.getCode());
            eeaFspO.getFspNotification().setTransmissionModeCapabilityChange(tmcc);
            break;
        }
        case fspNT_noInvokeDirectiveCapabilityOnThisVc:
        {
            eeaFspO.getFspNotification().setInvokeDirectiveCapabilityOnthisVC(new BerNull());
            break;
        }
        case fspNT_positiveConfirmResponseToDirective:
        {
            DirectiveExecutedId diId = new DirectiveExecutedId(pAsyncNotifyOperation.getDirectiveExecutedId());
            eeaFspO.getFspNotification().setPositiveConfirmResponceToDirective(diId);
            break;
        }
        case fspNT_negativeConfirmResponseToDirective:
        {
            FSP_FopAlert alert = pAsyncNotifyOperation.getFopAlert();
            FopAlert fa = new FopAlert(alert.getCode());
            DirectiveExecutedId deId = new DirectiveExecutedId(pAsyncNotifyOperation.getDirectiveExecutedId());
            NegativeConfirmResponseToDirective ncrtd = new NegativeConfirmResponseToDirective();
            ncrtd.setDirectiveExecutedId(deId);
            ncrtd.setFopAlert(fa);
            //NegativeConfirmResponseToDirective ncrtd = new NegativeConfirmResponseToDirective(deId, fa);
            eeaFspO.getFspNotification().setNegativeConfirmResponseToDirective(ncrtd);
            break;
        }
        case fspNT_vcAborted:
        {
            PacketIdentificationList pil = new PacketIdentificationList();
        	encodePacketIdentificationList(pAsyncNotifyOperation, pil);
            eeaFspO.getFspNotification().setVcAborted(pil);
            break;
        }
        case fspNT_eventConditionEvFalse:
        {
            EventInvocationId eiId = new EventInvocationId(pAsyncNotifyOperation.getEventThrownId());
            eeaFspO.getFspNotification().setEventConditionEvaluatedToFalse(eiId);
        }
        case fspNT_invokeDirectiveCapabilityOnThisVC:
        {
            eeaFspO.getFspNotification().setInvokeDirectiveCapabilityOnthisVC(new BerNull());
        }
        default:
        {
            break;
        }
        }

        // the fsp last processed
        ISLE_Time pTimeLastProcessed = pAsyncNotifyOperation.getProductionStartTime();
        if (pTimeLastProcessed != null)
        {
            PacketIdentification pId = new PacketIdentification(pAsyncNotifyOperation.getPacketLastProcessed());
            Time time = encodeTime(pTimeLastProcessed);
            FspPacketStatus fspPS = new FspPacketStatus(pAsyncNotifyOperation.getPacketStatus().getCode());
            //PacketProcessed packetProcessed = new PacketProcessed(pId, time, fspPS);
            PacketProcessed packetProcessed = new PacketProcessed();
            packetProcessed.setPacketIdentification(pId);
            packetProcessed.setPacketStatus(fspPS);
            packetProcessed.setProcessingStartTime(time);
            //FspPacketLastProcessed fspPaketLastProcessed = new FspPacketLastProcessed(null, packetProcessed);
            FspPacketLastProcessed fspPaketLastProcessed = new FspPacketLastProcessed();
            fspPaketLastProcessed.setPacketProcessed(packetProcessed);
            eeaFspO.setFspPacketLastProc(fspPaketLastProcessed);
        }
        else
        {
            eeaFspO.setFspPacketLastProc(new FspPacketLastProcessed());
        }

        // the fsp last ok
        ISLE_Time pTimeLastOk = pAsyncNotifyOperation.getProductionStopTime();
        if (pTimeLastOk != null)
        {
            PacketOk packetOk = new PacketOk();
            packetOk.setPacketIdentification(new PacketIdentification(pAsyncNotifyOperation.getPacketLastOk()));
            Time time = encodeTime(pTimeLastOk);
            packetOk.setProcessingStopTime(time);
            FspPacketLastOk lastOk = new FspPacketLastOk();
            lastOk.setPacketOk(packetOk);
            eeaFspO.setFspPacketLastOk(lastOk);
        }
        else
        {
        	FspPacketLastOk lastOk = new FspPacketLastOk();
            lastOk.setNoPacketOk(new BerNull());
            eeaFspO.setFspPacketLastOk(lastOk);
        }

        // the production status
        eeaFspO.setProductionStatus(new FspProductionStatus(pAsyncNotifyOperation.getProductionStatus().getCode()));
    }

    /**
     * Fills the object used for the encoding of Fsp AsyncNotifyV1To4 operation. S_OK SLEAPIJ-79
     * The FSP AsyncNotify operation has been encoded. E_FAIL Unable to encode
     * the FSP AsyncNotify operation.
     * 
     * @throws SleApiException
     * @throws IOException 
     */
    private void encodeAsyncNotifyOpV1To4(IFSP_AsyncNotify pAsyncNotifyOperation, FspAsyncNotifyInvocationPduV1To4 eeaFspO) throws SleApiException, IOException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pAsyncNotifyOperation.getInvokerCredentials();
        eeaFspO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the fsp notification
        eeaFspO.setFspNotification(new FspNotificationV1To4());

        switch (pAsyncNotifyOperation.getNotificationType())
        {
        case fspNT_slduExpired:
        {
            PacketIdentificationList pil = new PacketIdentificationList();
        	encodePacketIdentificationList(pAsyncNotifyOperation, pil);
            eeaFspO.getFspNotification().setSlduExpired (pil);
            break;
        }
        case fspNT_productionInterrupted:
        {
            PacketIdentificationList pil = new PacketIdentificationList();
        	encodePacketIdentificationList(pAsyncNotifyOperation, pil);
            eeaFspO.getFspNotification().setProductionInterrupted(pil);
            break;
        }
        case fspNT_productionHalted:
        {
            PacketIdentificationList pil = new PacketIdentificationList();
        	encodePacketIdentificationList(pAsyncNotifyOperation, pil);
            eeaFspO.getFspNotification().setProductionHalted(pil);
            break;
        }
        case fspNT_productionOperational:
        {
            eeaFspO.getFspNotification().setProductionOperational(new BerNull());
            break;
        }
        case fspNT_bufferEmpty:
        {
            eeaFspO.getFspNotification().setBufferEmpty(new BerNull());
            break;
        }
        case fspNT_actionListCompleted:
        {
            eeaFspO.getFspNotification().setActionListCompleted(new EventInvocationId(pAsyncNotifyOperation.getEventThrownId()));
            break;
        }
        case fspNT_actionListNotCompleted:
        {
            eeaFspO.getFspNotification().setActionListNotCompleted(new EventInvocationId(pAsyncNotifyOperation.getEventThrownId()));
            break;
        }
        case fspNT_packetProcessingStarted:
        {
            eeaFspO.getFspNotification().setActionListNotCompleted(new EventInvocationId(pAsyncNotifyOperation.getEventThrownId()));
            break;
        }
        case fspNT_packetRadiated:
        {
            PacketIdentificationList pil = new PacketIdentificationList();
            encodePacketIdentificationList(pAsyncNotifyOperation, pil);
            eeaFspO.getFspNotification().setPacketRadiated(pil);
            break;
        }
        case fspNT_packetAcknowledged:
        {
            PacketIdentificationList pil = new PacketIdentificationList();
            encodePacketIdentificationList(pAsyncNotifyOperation, pil);
            eeaFspO.getFspNotification().setPacketAcknowledged(pil);
            break;
        }
        case fspNT_packetTransmissionModeMismatch:
        {
            PacketIdentificationList pil = new PacketIdentificationList();
        	encodePacketIdentificationList(pAsyncNotifyOperation, pil);
            eeaFspO.getFspNotification().setPacketTransmissionModeMismatch(pil);
            break;
        }
        case fspNT_transmissionModeCapabilityChange:
        {
            FSP_FopAlert alert = pAsyncNotifyOperation.getFopAlert();
            FopAlert tmcc = new FopAlert(alert.getCode());
            eeaFspO.getFspNotification().setTransmissionModeCapabilityChange(tmcc);
            break;
        }
        case fspNT_noInvokeDirectiveCapabilityOnThisVc:
        {
            eeaFspO.getFspNotification().setInvokeDirectiveCapabilityOnthisVC(new BerNull());
            break;
        }
        case fspNT_positiveConfirmResponseToDirective:
        {
            DirectiveExecutedId diId = new DirectiveExecutedId(pAsyncNotifyOperation.getDirectiveExecutedId());
            eeaFspO.getFspNotification().setPositiveConfirmResponceToDirective(diId);
            break;
        }
        case fspNT_negativeConfirmResponseToDirective:
        {
            FSP_FopAlert alert = pAsyncNotifyOperation.getFopAlert();
            FopAlert fa = new FopAlert(alert.getCode());
            DirectiveExecutedId deId = new DirectiveExecutedId(pAsyncNotifyOperation.getDirectiveExecutedId());
            NegativeConfirmResponseToDirective ncrtd = new NegativeConfirmResponseToDirective();
            ncrtd.setDirectiveExecutedId(deId);
            ncrtd.setFopAlert(fa);
            //NegativeConfirmResponseToDirective ncrtd = new NegativeConfirmResponseToDirective(deId, fa);
            eeaFspO.getFspNotification().setNegativeConfirmResponseToDirective(ncrtd);
            break;
        }
        case fspNT_vcAborted:
        {
            PacketIdentificationList pil = new PacketIdentificationList();
        	encodePacketIdentificationList(pAsyncNotifyOperation, pil);
            eeaFspO.getFspNotification().setVcAborted(pil);
            break;
        }
        case fspNT_eventConditionEvFalse:
        {
            EventInvocationId eiId = new EventInvocationId(pAsyncNotifyOperation.getEventThrownId());
            eeaFspO.getFspNotification().setEventConditionEvaluatedToFalse(eiId);
        }
        case fspNT_invokeDirectiveCapabilityOnThisVC:
        {
            eeaFspO.getFspNotification().setInvokeDirectiveCapabilityOnthisVC(new BerNull());
        }
        default:
        {
            break;
        }
        }

        // the fsp last processed
        ISLE_Time pTimeLastProcessed = pAsyncNotifyOperation.getProductionStartTime();
        if (pTimeLastProcessed != null)
        {
            PacketIdentification pId = new PacketIdentification(pAsyncNotifyOperation.getPacketLastProcessed());
            Time time = encodeTime(pTimeLastProcessed);
            FspPacketStatus fspPS = new FspPacketStatus(pAsyncNotifyOperation.getPacketStatus().getCode());
            //PacketProcessed packetProcessed = new PacketProcessed(pId, time, fspPS);
            PacketProcessed packetProcessed = new PacketProcessed();
            packetProcessed.setPacketIdentification(pId);
            packetProcessed.setPacketStatus(fspPS);
            packetProcessed.setProcessingStartTime(time);
            //FspPacketLastProcessed fspPaketLastProcessed = new FspPacketLastProcessed(null, packetProcessed);
            FspPacketLastProcessed fspPaketLastProcessed = new FspPacketLastProcessed();
            fspPaketLastProcessed.setPacketProcessed(packetProcessed);
            eeaFspO.setFspPacketLastProc(fspPaketLastProcessed);
        }
        else
        {
            eeaFspO.setFspPacketLastProc(new FspPacketLastProcessed());
        }

        // the fsp last ok
        ISLE_Time pTimeLastOk = pAsyncNotifyOperation.getProductionStopTime();
        if (pTimeLastOk != null)
        {
            PacketOk packetOk = new PacketOk();
            packetOk.setPacketIdentification(new PacketIdentification(pAsyncNotifyOperation.getPacketLastOk()));
            Time time = encodeTime(pTimeLastOk);
            packetOk.setProcessingStopTime(time);
            FspPacketLastOk lastOk = new FspPacketLastOk();
            lastOk.setPacketOk(packetOk);
            eeaFspO.setFspPacketLastOk(lastOk);
        }
        else
        {
        	FspPacketLastOk lastOk = new FspPacketLastOk();
            lastOk.setNoPacketOk(new BerNull());
            eeaFspO.setFspPacketLastOk(lastOk);
        }

        // the production status
        eeaFspO.setProductionStatus(new FspProductionStatus(pAsyncNotifyOperation.getProductionStatus().getCode()));
    }
    
    
    /**
     * Fills the FSP ASYNC-NOTIFY operation from the object. S_OK The FSP
     * AsyncNotify operation has been decoded. E_FAIL Unable to decode the FSP
     * AsyncNotify operation.
     */
    private void decodeAsyncNotifyOp(FspAsyncNotifyInvocationPdu eeaFspO, IFSP_AsyncNotify pAsyncNotifyOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaFspO.getInvokerCredentials());

        // the production status
        pAsyncNotifyOperation.setProductionStatus(FSP_ProductionStatus
                .getProductionStatusByCode((int) eeaFspO.getProductionStatus().value.intValue()));

        if (pCredentials != null)
        {
            pAsyncNotifyOperation.putInvokerCredentials(pCredentials);
        }

        // the fsp notification
        if (eeaFspO.getFspNotification().getSlduExpired() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_slduExpired);
            decodePacketIdentificationList(eeaFspO.getFspNotification().getSlduExpired().getPacketIdentification(), pAsyncNotifyOperation); // SLEAPIJ-86
        }
        else if (eeaFspO.getFspNotification().getProductionInterrupted() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_productionInterrupted);
            decodePacketIdentificationList(eeaFspO.getFspNotification().getProductionInterrupted().getPacketIdentification(), pAsyncNotifyOperation); // SLEAPIJ-86
        }
        else if (eeaFspO.getFspNotification().getProductionHalted() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_productionHalted);
            decodePacketIdentificationList(eeaFspO.getFspNotification().getProductionHalted().getPacketIdentification(), pAsyncNotifyOperation); // SLEAPIJ-86
        }
        else if (eeaFspO.getFspNotification().getProductionOperational() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_productionOperational);
        }
        else if (eeaFspO.getFspNotification().getBufferEmpty() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_bufferEmpty);
        }
        else if (eeaFspO.getFspNotification().getActionListCompleted() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_actionListCompleted);
            pAsyncNotifyOperation.setEventThrownId(eeaFspO.getFspNotification().getActionListCompleted().value.longValue());
        }
        else if (eeaFspO.getFspNotification().getActionListNotCompleted() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_actionListNotCompleted);
            pAsyncNotifyOperation.setEventThrownId(eeaFspO.getFspNotification().getActionListNotCompleted().value.longValue());
        }
        else if (eeaFspO.getFspNotification().getPacketProcessingStarted() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_packetProcessingStarted);
            decodePacketIdentificationList(eeaFspO.getFspNotification().getPacketProcessingStarted(), pAsyncNotifyOperation);
        }
        else if (eeaFspO.getFspNotification().getPacketRadiated() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_packetRadiated);
            decodePacketIdentificationList(eeaFspO.getFspNotification().getPacketRadiated().getPacketIdentificationList(), pAsyncNotifyOperation);
            decodeFrameSequenceNumber(eeaFspO.getFspNotification().getPacketRadiated().getFrameSequenceNumber(), pAsyncNotifyOperation);
        }
        else if (eeaFspO.getFspNotification().getPacketAcknowledged() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_packetAcknowledged);
            decodePacketIdentificationList(eeaFspO.getFspNotification().getPacketAcknowledged(), pAsyncNotifyOperation);
        }
        else if (eeaFspO.getFspNotification().getPacketTransmissionModeMismatch() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_packetTransmissionModeMismatch);
            decodePacketIdentificationList(eeaFspO.getFspNotification().getPacketTransmissionModeMismatch(),
                                           pAsyncNotifyOperation);
        }
        else if (eeaFspO.getFspNotification().getTransmissionModeCapabilityChange() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_transmissionModeCapabilityChange);
            FSP_FopAlert alert = FSP_FopAlert
                    .getFopAlertByCode((int) eeaFspO.getFspNotification().getTransmissionModeCapabilityChange().value.intValue());
            pAsyncNotifyOperation.setFopAlert(alert);
        }
        else if (eeaFspO.getFspNotification().getNoInvokeDirectiveCapabilityOnthisVC() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_noInvokeDirectiveCapabilityOnThisVc);
        }
        else if (eeaFspO.getFspNotification().getInvokeDirectiveCapabilityOnthisVC() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_invokeDirectiveCapabilityOnThisVC);
        }
        else if (eeaFspO.getFspNotification().getPositiveConfirmResponceToDirective() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_positiveConfirmResponseToDirective);
            FSP_FopAlert alert = FSP_FopAlert
                    .getFopAlertByCode((int) eeaFspO.getFspNotification().getPositiveConfirmResponceToDirective().value.intValue());
            pAsyncNotifyOperation.setFopAlert(alert);
            pAsyncNotifyOperation
                    .setDirectiveExecutedId(eeaFspO.getFspNotification().getPositiveConfirmResponceToDirective().value.longValue());
        }
        else if (eeaFspO.getFspNotification().getNegativeConfirmResponseToDirective() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_negativeConfirmResponseToDirective);
            FSP_FopAlert alert = FSP_FopAlert
                    .getFopAlertByCode((int) eeaFspO.getFspNotification().getNegativeConfirmResponseToDirective().getFopAlert().value.intValue());
            pAsyncNotifyOperation.setFopAlert(alert);
            pAsyncNotifyOperation
                    .setDirectiveExecutedId(eeaFspO.getFspNotification().getNegativeConfirmResponseToDirective().getDirectiveExecutedId().value.longValue());
        }
        else if (eeaFspO.getFspNotification().getVcAborted() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_vcAborted);
            decodePacketIdentificationList(eeaFspO.getFspNotification().getVcAborted(), pAsyncNotifyOperation);
        }
        else if (eeaFspO.getFspNotification().getEventConditionEvaluatedToFalse() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_eventConditionEvFalse);
            pAsyncNotifyOperation.setEventThrownId(eeaFspO.getFspNotification().getEventConditionEvaluatedToFalse().value.longValue());
        }
        else
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_invalid);
        }

        // the fsp last processed
        //if (eeaFspO.fspPacketLastProc.packetProcessed != null)// UMW: SLEAPIJ-47
        if (eeaFspO.getFspPacketLastProc().getPacketProcessed() != null) // JC: SLEAPIJ-47 merge result. 
        {
            // the fsp identification
            pAsyncNotifyOperation
                    .setPacketLastProcessed(eeaFspO.getFspPacketLastProc().getPacketProcessed().getPacketIdentification().value.longValue());
            // the production start time
            ISLE_Time pTime = null;
            pTime = decodeTime(eeaFspO.getFspPacketLastProc().getPacketProcessed().getProcessingStartTime());
            if (pTime != null)
            {
                pAsyncNotifyOperation.putProductionStartTime(pTime);
            }
            // the fsp status
            FSP_PacketStatus fspStatus = FSP_PacketStatus
                    .getPacketStatusByCode((int) eeaFspO.getFspPacketLastProc().getPacketProcessed().getPacketStatus().value.intValue());
            pAsyncNotifyOperation.setPacketStatus(fspStatus);
        }

        // the fsp last ok
        if (eeaFspO.getFspPacketLastOk().getPacketOk() != null)
        {
            pAsyncNotifyOperation.setPacketLastOk(eeaFspO.getFspPacketLastOk().getPacketOk().getPacketIdentification().value.longValue());

            ISLE_Time ptime = null;
            ptime = decodeTime(eeaFspO.getFspPacketLastOk().getPacketOk().getProcessingStopTime());

            if (ptime != null)
            {
                pAsyncNotifyOperation.putProductionStopTime(ptime);
            }

        }

    }

    /**
     * SLEAPIJ-79
     * Fills the FSP ASYNC-NOTIFY V1 to 4 operation from the object. S_OK The FSP
     * AsyncNotify operation has been decoded. E_FAIL Unable to decode the FSP
     * AsyncNotify operation.
     */
    private void decodeAsyncNotifyOpV1To4(FspAsyncNotifyInvocationPduV1To4 eeaFspO, IFSP_AsyncNotify pAsyncNotifyOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaFspO.getInvokerCredentials());

        // the production status
        pAsyncNotifyOperation.setProductionStatus(FSP_ProductionStatus
                .getProductionStatusByCode((int) eeaFspO.getProductionStatus().value.intValue()));

        if (pCredentials != null)
        {
            pAsyncNotifyOperation.putInvokerCredentials(pCredentials);
        }

        // the fsp notification
        if (eeaFspO.getFspNotification().getSlduExpired() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_slduExpired);
            decodePacketIdentificationList(eeaFspO.getFspNotification().getSlduExpired().getPacketIdentification(), pAsyncNotifyOperation); // SLEAPIJ-86
        }
        else if (eeaFspO.getFspNotification().getProductionInterrupted() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_productionInterrupted);
            decodePacketIdentificationList(eeaFspO.getFspNotification().getProductionInterrupted().getPacketIdentification(), pAsyncNotifyOperation); // SLEAPIJ-86
        }
        else if (eeaFspO.getFspNotification().getProductionHalted() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_productionHalted);
            decodePacketIdentificationList(eeaFspO.getFspNotification().getProductionHalted().getPacketIdentification(), pAsyncNotifyOperation); // SLEAPIJ-86
        }
        else if (eeaFspO.getFspNotification().getProductionOperational() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_productionOperational);
        }
        else if (eeaFspO.getFspNotification().getBufferEmpty() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_bufferEmpty);
        }
        else if (eeaFspO.getFspNotification().getActionListCompleted() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_actionListCompleted);
            pAsyncNotifyOperation.setEventThrownId(eeaFspO.getFspNotification().getActionListCompleted().value.longValue());
        }
        else if (eeaFspO.getFspNotification().getActionListNotCompleted() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_actionListNotCompleted);
            pAsyncNotifyOperation.setEventThrownId(eeaFspO.getFspNotification().getActionListNotCompleted().value.longValue());
        }
        else if (eeaFspO.getFspNotification().getPacketProcessingStarted() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_packetProcessingStarted);
            decodePacketIdentificationList(eeaFspO.getFspNotification().getPacketProcessingStarted(), pAsyncNotifyOperation);
        }
        else if (eeaFspO.getFspNotification().getPacketRadiated() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_packetRadiated);
            decodePacketIdentificationList(eeaFspO.getFspNotification().getPacketRadiated(), pAsyncNotifyOperation);
        }
        else if (eeaFspO.getFspNotification().getPacketAcknowledged() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_packetAcknowledged);
            decodePacketIdentificationList(eeaFspO.getFspNotification().getPacketAcknowledged(), pAsyncNotifyOperation);
        }
        else if (eeaFspO.getFspNotification().getPacketTransmissionModeMismatch() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_packetTransmissionModeMismatch);
            decodePacketIdentificationList(eeaFspO.getFspNotification().getPacketTransmissionModeMismatch(),
                                           pAsyncNotifyOperation);
        }
        else if (eeaFspO.getFspNotification().getTransmissionModeCapabilityChange() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_transmissionModeCapabilityChange);
            FSP_FopAlert alert = FSP_FopAlert
                    .getFopAlertByCode((int) eeaFspO.getFspNotification().getTransmissionModeCapabilityChange().value.intValue());
            pAsyncNotifyOperation.setFopAlert(alert);
        }
        else if (eeaFspO.getFspNotification().getNoInvokeDirectiveCapabilityOnthisVC() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_noInvokeDirectiveCapabilityOnThisVc);
        }
        else if (eeaFspO.getFspNotification().getInvokeDirectiveCapabilityOnthisVC() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_invokeDirectiveCapabilityOnThisVC);
        }
        else if (eeaFspO.getFspNotification().getPositiveConfirmResponceToDirective() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_positiveConfirmResponseToDirective);
            FSP_FopAlert alert = FSP_FopAlert
                    .getFopAlertByCode((int) eeaFspO.getFspNotification().getPositiveConfirmResponceToDirective().value.intValue());
            pAsyncNotifyOperation.setFopAlert(alert);
            pAsyncNotifyOperation
                    .setDirectiveExecutedId(eeaFspO.getFspNotification().getPositiveConfirmResponceToDirective().value.longValue());
        }
        else if (eeaFspO.getFspNotification().getNegativeConfirmResponseToDirective() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_negativeConfirmResponseToDirective);
            FSP_FopAlert alert = FSP_FopAlert
                    .getFopAlertByCode((int) eeaFspO.getFspNotification().getNegativeConfirmResponseToDirective().getFopAlert().value.intValue());
            pAsyncNotifyOperation.setFopAlert(alert);
            pAsyncNotifyOperation
                    .setDirectiveExecutedId(eeaFspO.getFspNotification().getNegativeConfirmResponseToDirective().getDirectiveExecutedId().value.longValue());
        }
        else if (eeaFspO.getFspNotification().getVcAborted() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_vcAborted);
            decodePacketIdentificationList(eeaFspO.getFspNotification().getVcAborted(), pAsyncNotifyOperation);
        }
        else if (eeaFspO.getFspNotification().getEventConditionEvaluatedToFalse() != null)
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_eventConditionEvFalse);
            pAsyncNotifyOperation.setEventThrownId(eeaFspO.getFspNotification().getEventConditionEvaluatedToFalse().value.longValue());
        }
        else
        {
            pAsyncNotifyOperation.setNotificationType(FSP_NotificationType.fspNT_invalid);
        }

        // the fsp last processed
        //if (eeaFspO.fspPacketLastProc.packetProcessed != null)// UMW: SLEAPIJ-47
        if (eeaFspO.getFspPacketLastProc().getPacketProcessed() != null) // JC: SLEAPIJ-47 merge result. 
        {
            // the fsp identification
            pAsyncNotifyOperation
                    .setPacketLastProcessed(eeaFspO.getFspPacketLastProc().getPacketProcessed().getPacketIdentification().value.longValue());
            // the production start time
            ISLE_Time pTime = null;
            pTime = decodeTime(eeaFspO.getFspPacketLastProc().getPacketProcessed().getProcessingStartTime());
            if (pTime != null)
            {
                pAsyncNotifyOperation.putProductionStartTime(pTime);
            }
            // the fsp status
            FSP_PacketStatus fspStatus = FSP_PacketStatus
                    .getPacketStatusByCode((int) eeaFspO.getFspPacketLastProc().getPacketProcessed().getPacketStatus().value.intValue());
            pAsyncNotifyOperation.setPacketStatus(fspStatus);
        }

        // the fsp last ok
        if (eeaFspO.getFspPacketLastOk().getPacketOk() != null)
        {
            pAsyncNotifyOperation.setPacketLastOk(eeaFspO.getFspPacketLastOk().getPacketOk().getPacketIdentification().value.longValue());

            ISLE_Time ptime = null;
            ptime = decodeTime(eeaFspO.getFspPacketLastOk().getPacketOk().getProcessingStopTime());

            if (ptime != null)
            {
                pAsyncNotifyOperation.putProductionStopTime(ptime);
            }

        }

    }
    
    
    /**
     * Fills the object used for the encoding of Fsp StatusReport operation.
     * S_OK The FSP StatusReport operation has been encoded. E_FAIL Unable to
     * encode the FSP StatusReport operation.
     * 
     * @throws SleApiException
     */
    private void encodeStatusReportOp(IFSP_StatusReport pStatusReportOperation, FspStatusReportInvocationPdu eeaFspO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pStatusReportOperation.getInvokerCredentials();
        eeaFspO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the fsp last processed
        if (pStatusReportOperation.getPacketsProcessed())
        {
            FspPacketLastProcessed fspPacketLastProcessed = new FspPacketLastProcessed();
            PacketProcessed packetProcessed = new PacketProcessed();
            // the fsp identification
            packetProcessed.setPacketIdentification(new PacketIdentification(pStatusReportOperation.getPacketLastProcessed()));
            // the production start time
            ISLE_Time pTime = null;
            pTime = pStatusReportOperation.getProductionStartTime();
            packetProcessed.setProcessingStartTime(encodeTime(pTime));
            // the fsp status
            packetProcessed.setPacketStatus(new FspPacketStatus(pStatusReportOperation.getPacketStatus().getCode()));
            eeaFspO.setFspPacketLastProcessed(fspPacketLastProcessed);
            eeaFspO.getFspPacketLastProcessed().setPacketProcessed(packetProcessed);
        }
        else
        {
        	FspPacketLastProcessed packetProcessed = new FspPacketLastProcessed();
        	packetProcessed.setNoPacketProcessed(new BerNull());
            eeaFspO.setFspPacketLastProcessed(packetProcessed);
        }

        // the fsp last ok
        ISLE_Time pTimeLastOk = pStatusReportOperation.getProductionStopTime();

        if (pTimeLastOk != null)
        {
            FspPacketLastOk fspPacketLastOk = new FspPacketLastOk();
            PacketOk packetOk = new PacketOk();
            packetOk.setPacketIdentification(new PacketIdentification(pStatusReportOperation.getPacketLastOk()));
            packetOk.setProcessingStopTime(encodeTime(pTimeLastOk));
            eeaFspO.setFspPacketLastOk(fspPacketLastOk);
            eeaFspO.getFspPacketLastOk().setPacketOk(packetOk);
        }
        else
        {
        	FspPacketLastOk lastOk = new FspPacketLastOk();
        	lastOk.setNoPacketOk(new BerNull());
            eeaFspO.setFspPacketLastOk(lastOk);
        }

        // the fsp production status
        eeaFspO.setProductionStatus(new FspProductionStatus(pStatusReportOperation.getProductionStatus().getCode()));
        // the number of fsp received
        FspPacketCount fspC = new FspPacketCount();
        fspC.setAdCount(new IntUnsignedLong(pStatusReportOperation.getNumberOfADPacketsReceived()));
        fspC.setBdCount(new IntUnsignedLong(pStatusReportOperation.getNumberOfBDPacketsReceived()));
        eeaFspO.setNumberOfPacketsReceived(fspC);
        // the number of fsp radiated
        FspPacketCount fspR = new FspPacketCount();
        fspR.setAdCount(new IntUnsignedLong(pStatusReportOperation.getNumberOfADPacketsRadiated()));
        fspR.setBdCount(new IntUnsignedLong(pStatusReportOperation.getNumberOfBDPacketsRadiated()));
        eeaFspO.setNumberOfPacketsRadiated(fspR);
        // the number of fsp processed
        FspPacketCount fspP = new FspPacketCount();
        fspP.setAdCount(new IntUnsignedLong(pStatusReportOperation.getNumberOfADPacketsProcessed()));
        fspP.setBdCount(new IntUnsignedLong(pStatusReportOperation.getNumberOfBDPacketsProcessed()));
        eeaFspO.setNumberOfPacketsProcessed(fspP);

        // the number of acknowledged
        eeaFspO.setNumberOfPacketsAcknowledged(new IntUnsignedLong(pStatusReportOperation.getNumberOfPacketsAcknowledged()));

        // the fsp buffer available
        eeaFspO.setFspBufferAvailable(new BufferSize(pStatusReportOperation.getPacketBufferAvailable()));
    }

    /**
     * Fills the FSP STATUS-REPORT operation from the object. S_OK The FSP
     * StatusReport operation has been decoded. E_FAIL Unable to decode the FSP
     * StatusReport operation.
     * 
     * @throws SleApiException
     */
    private void decodeStatusReportOp(FspStatusReportInvocationPdu eeaFspO, IFSP_StatusReport pStatusReportOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaFspO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pStatusReportOperation.putInvokerCredentials(pCredentials);
        }

        // the fsp last processed
        if (eeaFspO.getFspPacketLastProcessed() != null)
        {
            // the fsp identification
            pStatusReportOperation
                    .setPacketLastProcessed(eeaFspO.getFspPacketLastProcessed().getPacketProcessed().getPacketIdentification().value.longValue());
            // the production start time
            ISLE_Time pTime = null;
            pTime = decodeTime(eeaFspO.getFspPacketLastProcessed().getPacketProcessed().getProcessingStartTime());
            if (pTime != null)
            {
                pStatusReportOperation.putProductionStartTime(pTime);
            }
            // the fsp status
            pStatusReportOperation.setPacketStatus(FSP_PacketStatus
                    .getPacketStatusByCode((int) eeaFspO.getFspPacketLastProcessed().getPacketProcessed().getPacketStatus().value.intValue()));
        }

        // the fsp last ok
        if (eeaFspO.getFspPacketLastOk() != null)
        {
            // the fsp identification
            pStatusReportOperation.setPacketLastOk(eeaFspO.getFspPacketLastOk().getPacketOk().getPacketIdentification().value.longValue());
            // the production stop time
            ISLE_Time pTime = null;
            pTime = decodeTime(eeaFspO.getFspPacketLastOk().getPacketOk().getProcessingStopTime());
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("pTIme should be not null: " + pTime);
            }
            if (pTime != null)
            {
                pStatusReportOperation.putProductionStopTime(pTime);
            }
        }

        // the production status
        pStatusReportOperation.setProductionStatus(FSP_ProductionStatus
                .getProductionStatusByCode((int) eeaFspO.getProductionStatus().value.intValue()));
        // the number of fsp received
        pStatusReportOperation.setNumberOfADPacketsReceived(eeaFspO.getNumberOfPacketsReceived().getAdCount().value.longValue());
        pStatusReportOperation.setNumberOfBDPacketsReceived(eeaFspO.getNumberOfPacketsReceived().getBdCount().value.longValue());
        // the number of fsp processed
        pStatusReportOperation.setNumberOfADPacketsProcessed(eeaFspO.getNumberOfPacketsProcessed().getAdCount().value.longValue());
        pStatusReportOperation.setNumberOfBDPacketsProcessed(eeaFspO.getNumberOfPacketsProcessed().getBdCount().value.longValue());
        // the number of fsp radiated
        pStatusReportOperation.setNumberOfADPacketsRadiated(eeaFspO.getNumberOfPacketsRadiated().getAdCount().value.longValue());
        pStatusReportOperation.setNumberOfBDPacketsRadiated(eeaFspO.getNumberOfPacketsRadiated().getBdCount().value.longValue());
        // the number of fsp acknowledged
        pStatusReportOperation.setNumberOfPacketsAcknowledged(eeaFspO.getNumberOfPacketsAcknowledged().value.longValue());
        // the fsp buffer available
        pStatusReportOperation.setPacketBufferAvailable(eeaFspO.getFspBufferAvailable().value.longValue());

    }

    /**
     * Fills the object used for the encoding of Fsp ThrowEvent invoke
     * operation. S_OK The FSP ThrowEvent operation has been encoded. E_FAIL
     * Unable to encode the FSP ThrowEvent operation.
     * 
     * @throws SleApiException
     */
    private void encodeThrowEventInvokeOp(IFSP_ThrowEvent pThrowEventOperation, FspThrowEventInvocationPdu eeaFspO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pThrowEventOperation.getInvokerCredentials();
        eeaFspO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the invoker id
        eeaFspO.setInvokeId(new InvokeId(pThrowEventOperation.getInvokeId()));

        // the event thrown id
        eeaFspO.setEventInvocationIdentification(new EventInvocationId(pThrowEventOperation.getEventInvocationId()));

        // the event identification
        eeaFspO.setEventIdentifier(new IntPosShort(pThrowEventOperation.getEventId()));

        // the event argument
        byte[] pData = pThrowEventOperation.getEventQualifier();
        eeaFspO.setEventQualifier(new BerOctetString(pData));
    }

    /**
     * Fills the FSP THROW-EVENT invoke operation from the object. S_OK The FSP
     * ThrowEvent operation has been decoded. E_FAIL Unable to decode the FSP
     * ThrowEvent operation.
     * 
     * @throws SleApiException
     */
    private void decodeThrowEventInvokeOp(FspThrowEventInvocationPdu eeaFspO, IFSP_ThrowEvent pThrowEventOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaFspO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pThrowEventOperation.putInvokerCredentials(pCredentials);
        }

        // the invoke id
        pThrowEventOperation.setInvokeId((int) eeaFspO.getInvokeId().value.intValue());

        // the event thrown id
        pThrowEventOperation.setEventInvocationId(eeaFspO.getEventInvocationIdentification().value.intValue());

        // the event identification
        pThrowEventOperation.setEventId((int) eeaFspO.getEventIdentifier().value.intValue());

        // the event argument
        pThrowEventOperation.setEventQualifier(eeaFspO.getEventQualifier().value);
    }

    /**
     * Fills the object used for the encoding of Fsp ThrowEvent return
     * operation. S_OK The FSP ThrowEvent operation has been encoded. E_FAIL
     * Unable to encode the FSP ThrowEvent operation.
     * 
     * @throws SleApiException
     */
    private void encodeThrowEventReturnOp(IFSP_ThrowEvent pThrowEventOperation, FspThrowEventReturnPdu eeaFspO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pThrowEventOperation.getPerformerCredentials();
        eeaFspO.setPerformerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaFspO.setInvokeId(new InvokeId(pThrowEventOperation.getInvokeId()));

        // the event invocation id
        eeaFspO.setEventInvokeId(new IntUnsignedLong(pThrowEventOperation.getExpectedEventInvocationId()));

        // the result
        if (pThrowEventOperation.getResult() == SLE_Result.sleRES_positive)
        {
            // positive result
        	FspThrowEventReturn.Result posResult = new FspThrowEventReturn.Result();
        	posResult.setPositiveResult(new BerNull());
            eeaFspO.setResult(posResult);
        }
        else
        {
            DiagnosticFspThrowEvent negResult = new DiagnosticFspThrowEvent();

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
            ccsds.sle.transfer.service.fsp.outgoing.pdus.FspThrowEventReturn.Result negR = new ccsds.sle.transfer.service.fsp.outgoing.pdus.FspThrowEventReturn.Result();
            negR.setNegativeResult(negResult);
            eeaFspO.setResult(negR);
        }
    }

    /**
     * Fills the FSP THROW-EVENT return operation from the object. S_OK The FSP
     * ThrowEvent operation has been decoded. E_FAIL Unable to decode the FSP
     * ThrowEvent operation.
     * 
     * @throws SleApiException
     */
    private IFSP_ThrowEvent decodeThrowEventReturnOp(FspThrowEventReturnPdu eeaFspO) throws SleApiException
    {
        ISLE_Operation pOperation = null;
        IFSP_ThrowEvent pThrowEventOperation = null;

        pOperation = this.pduTranslator.getReturnOp(eeaFspO.getInvokeId(), SLE_OpType.sleOT_throwEvent);
        if (pOperation != null)
        {
            pThrowEventOperation = pOperation.queryInterface(IFSP_ThrowEvent.class);
            if (pThrowEventOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaFspO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pThrowEventOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pThrowEventOperation.setInvokeId((int) eeaFspO.getInvokeId().value.intValue());

                // the event invocation id
                pThrowEventOperation.setExpectedEventInvocationId(eeaFspO.getEventInvokeId().value.longValue());

                // the result
                if (eeaFspO.getResult().getPositiveResult() != null)
                {
                    // positive result
                    pThrowEventOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eeaFspO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        // specific
                        pThrowEventOperation.setThrowEventDiagnostic(FSP_ThrowEventDiagnostic
                                .getThrowEventDiagnosticByCode((int) eeaFspO.getResult().getNegativeResult().getSpecific().value.intValue()));
                    }
                    else
                    {
                        // common
                        pThrowEventOperation.setDiagnostics(SLE_Diagnostics
                                .getDiagnosticsByCode((int) eeaFspO.getResult().getNegativeResult().getCommon().value.intValue()));
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

    private void encodeInvokeDirectiveInvokeOp(IFSP_InvokeDirective pInvokeDirectiveOperation,
                                               FspInvokeDirectiveInvocationPdu eeaFspO) throws SleApiException, IOException
    {
        // encode the credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pInvokeDirectiveOperation.getInvokerCredentials();
        eeaFspO.setInvokerCredentials(encodeCredentials(pCredentials));

        // encode the invocation id
        eeaFspO.setInvokeId(new InvokeId(pInvokeDirectiveOperation.getInvokeId()));

        // encode the directive id
        eeaFspO.setDirective(new Directive());

        switch (pInvokeDirectiveOperation.getDirective())
        {
        case fspDV_initiateADwithoutCLCW:
        {
            eeaFspO.getDirective().setInitiateADserviceWithoutCLCW(new BerNull());
            break;
        }
        case fspDV_initiateADwithCLCW:
        {
            eeaFspO.getDirective().setInitiateADserviceWithCLCW(new BerNull());
            break;
        }
        case fspDV_initiateADwithUnlock:
        {
            eeaFspO.getDirective().setInitiateADserviceWithUnlock(new BerNull());
            break;
        }
        case fspDV_initiateADwithSetVR:
        {
            eeaFspO.getDirective().setInitiateADserviceWithSetVR(new BerInteger(pInvokeDirectiveOperation.getVR()));
            break;
        }
        case fspDV_terminateAD:
        {
            eeaFspO.getDirective().setTerminateADservice(new BerNull());
            break;
        }
        case fspDV_resumeAD:
        {
            eeaFspO.getDirective().setResumeADservice(new BerNull());
            break;
        }
        case fspDV_setVS:
        {
            eeaFspO.getDirective().setSetVS(new BerInteger(pInvokeDirectiveOperation.getVS()));
            break;
        }
        case fspDV_setFopSlidingWindow:
        {
            eeaFspO.getDirective().setSetFOPslidingWindowWidth(new BerInteger(pInvokeDirectiveOperation.getFopSlidingWindowWidth()));
            break;
        }
        case fspDV_setT1Initial:
        {
            eeaFspO.getDirective().setSetT1Initial(new IntPosLong(pInvokeDirectiveOperation.getTimerInitial()));
            break;
        }
        case fspDV_setTransmissionLimit:
        {
            eeaFspO.getDirective().setSetTransmissionLimit(new IntPosShort(pInvokeDirectiveOperation.getTransmissionLimit()));
            break;
        }
        case fspDV_setTimeoutType:
        {
            switch (pInvokeDirectiveOperation.getTimeoutType())
            {
            case fspDTT_terminateAD:
            {
                eeaFspO.getDirective().setSetTimeoutType(new BerInteger(0));
                break;
            }
            case fspDTT_suspendAD:
            {
                eeaFspO.getDirective().setSetTimeoutType(new BerInteger(1));
                break;
            }
            default:
            {
                eeaFspO.getDirective().setSetTimeoutType(new BerInteger(-1));
                break;
            }
            }

            break;
        }
        case fspDV_abortVC:
        {
            eeaFspO.getDirective().setAbortVC(new BerNull());
            break;
        }
        case fspDV_modifyMapMuxControl:
        {
            MapMuxControl mmc = new MapMuxControl();
            if (pInvokeDirectiveOperation.getPriority() != null)
            {
                mmc.setAbsolutePriority(new MapMuxControl.AbsolutePriority());
                FSP_AbsolutePriority[] list = pInvokeDirectiveOperation.getPriority();
                for (FSP_AbsolutePriority i : list)
                {
                    AbsolutePriority e = new AbsolutePriority();
                    e.setVcOrMapId(new VcOrMapId(i.getMapOrVc()));
                    e.setPriority(new Priority(i.getPriority()));
                    //os.write(e.code);
                    mmc.getAbsolutePriority().getAbsolutePriority().add(e);
                }
            }
            else
            {
                mmc.setPollingVector(new PollingVector());
                long[] list = pInvokeDirectiveOperation.getPollingVector();
                for (long i : list)
                {
                    MapId e = new MapId(i);
                    mmc.getPollingVector().getMapId().add(e);
                }
            }
            eeaFspO.getDirective().setModifyMapMuxControl(mmc);
            break;
        }
        default:
        {
            eeaFspO.setDirective(new Directive());
        }
        }

        // encode directive identification
        eeaFspO.setDirectiveIdentification(new IntUnsignedLong(pInvokeDirectiveOperation.getDirectiveId()));

    }

    private void decodeInvokeDirectiveInvokeOp(FspInvokeDirectiveInvocationPdu eeaFspO,
                                               IFSP_InvokeDirective pInvokeDirectiveOperation) throws SleApiException
    {
        // decode the credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaFspO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pInvokeDirectiveOperation.putInvokerCredentials(pCredentials);
        }

        // decode the invocation id
        pInvokeDirectiveOperation.setInvokeId((int) eeaFspO.getInvokeId().value.intValue());

        // decode the directive id
        pInvokeDirectiveOperation.setDirectiveId(eeaFspO.getDirectiveIdentification().value.longValue());

        // decode the directive
        if (eeaFspO.getDirective().getInitiateADserviceWithoutCLCW() != null)
        {
            pInvokeDirectiveOperation.setInitiateADwithoutCLCW();
        }
        else if (eeaFspO.getDirective().getInitiateADserviceWithCLCW() != null)
        {
            pInvokeDirectiveOperation.setInitiateADwithCLCW();
        }
        else if (eeaFspO.getDirective().getInitiateADserviceWithUnlock() != null)
        {
            pInvokeDirectiveOperation.setInitiateADwithUnlock();
        }
        else if (eeaFspO.getDirective().getInitiateADserviceWithSetVR() != null)
        {
            pInvokeDirectiveOperation.setInitiateADwithSetVR(eeaFspO.getDirective().getInitiateADserviceWithSetVR().value.longValue());
        }
        else if (eeaFspO.getDirective().getTerminateADservice() != null)
        {
            pInvokeDirectiveOperation.setTerminateAD();
        }
        else if (eeaFspO.getDirective().getResumeADservice() != null)
        {
            pInvokeDirectiveOperation.setResumeAD();
        }
        else if (eeaFspO.getDirective().getSetVS() != null)
        {
            pInvokeDirectiveOperation.setVS(eeaFspO.getDirective().getSetVS().value.longValue());
        }
        else if (eeaFspO.getDirective().getSetFOPslidingWindowWidth() != null)
        {
            pInvokeDirectiveOperation.setFopSlidingWindow(eeaFspO.getDirective().getSetFOPslidingWindowWidth().value.longValue());
        }
        else if (eeaFspO.getDirective().getSetT1Initial() != null)
        {
            pInvokeDirectiveOperation.setTimerInitial(eeaFspO.getDirective().getSetT1Initial().value.longValue());
        }
        else if (eeaFspO.getDirective().getSetTransmissionLimit() != null)
        {
            pInvokeDirectiveOperation.setTransmissionLimit(eeaFspO.getDirective().getSetTransmissionLimit().value.longValue());
        }
        else if (eeaFspO.getDirective().getSetTimeoutType() != null)
        {
            if (eeaFspO.getDirective().getSetTimeoutType().value.intValue() == 0)
            {
                pInvokeDirectiveOperation.setTimeoutType(FSP_DirectiveTimeoutType.fspDTT_terminateAD);
            }
            else if (eeaFspO.getDirective().getSetTimeoutType().value.intValue() == 1)
            {
                pInvokeDirectiveOperation.setTimeoutType(FSP_DirectiveTimeoutType.fspDTT_suspendAD);
            }
            else
            {
                pInvokeDirectiveOperation.setTimeoutType(FSP_DirectiveTimeoutType.fspDTT_invalid);
            }
        }
        else if (eeaFspO.getDirective().getAbortVC() != null)
        {
            pInvokeDirectiveOperation.setAbortVC();
        }
        else if (eeaFspO.getDirective().getModifyMapMuxControl() != null)
        {
            if (eeaFspO.getDirective().getModifyMapMuxControl().getAbsolutePriority() != null)
            {
                int size = eeaFspO.getDirective().getModifyMapMuxControl().getAbsolutePriority().getAbsolutePriority().size();
                int count = 0;
                FSP_AbsolutePriority[] list = new FSP_AbsolutePriority[size];
                for (AbsolutePriority i : eeaFspO.getDirective().getModifyMapMuxControl().getAbsolutePriority().getAbsolutePriority())
                {
                    FSP_AbsolutePriority aps = new FSP_AbsolutePriority();
                    aps.setMapOrVc((int) i.getVcOrMapId().value.intValue());
                    aps.setPriority((int) i.getPriority().value.intValue());
                    list[count++] = aps;
                }
                pInvokeDirectiveOperation.setModifyMapPriorityList(list);
            }
            else
            {
                long[] list = new long[eeaFspO.getDirective().getModifyMapMuxControl().getPollingVector().getMapId().size()];
                int counter = 0;
                for (MapId i : eeaFspO.getDirective().getModifyMapMuxControl().getPollingVector().getMapId())
                {
                    list[counter] = new Long(i.value.longValue());
                    counter++;
                }
                pInvokeDirectiveOperation.setModifyMapPollingVector(list);
            }
        }
        else
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }
    }

    private void encodeInvokeDirectiveReturnOp(IFSP_InvokeDirective pInvokeDirectiveOperation,
                                               FspInvokeDirectiveReturnPdu eeaFspO) throws SleApiException
    {
        // encode the credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pInvokeDirectiveOperation.getPerformerCredentials();
        eeaFspO.setPerformerCredentials(encodeCredentials(pCredentials));

        // encode the invocation id
        eeaFspO.setInvokeId(new InvokeId(pInvokeDirectiveOperation.getInvokeId()));

        // encode the expected directive id
        eeaFspO.setDirectiveIdentification(new IntUnsignedLong(pInvokeDirectiveOperation.getExpectedDirectiveId()));

        // encode the result
        // the result
        if (pInvokeDirectiveOperation.getResult() == SLE_Result.sleRES_positive)
        {
            // positive result
        	FspInvokeDirectiveReturn.Result posResult = new FspInvokeDirectiveReturn.Result();
        	posResult.setPositiveResult(new BerNull());
            eeaFspO.setResult(posResult);
        }
        else
        {
            DiagnosticFspInvokeDirective negResult = new DiagnosticFspInvokeDirective();

            if (pInvokeDirectiveOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                // specific diagnostic
                negResult.setSpecific(new BerInteger(pInvokeDirectiveOperation.getInvokeDirectiveDiagnostic().getCode()));
            }
            else
            {
                // common diagnostic
                negResult.setCommon(new Diagnostics(pInvokeDirectiveOperation.getDiagnostics().getCode()));
            }
            ccsds.sle.transfer.service.fsp.outgoing.pdus.FspInvokeDirectiveReturn.Result negR = new ccsds.sle.transfer.service.fsp.outgoing.pdus.FspInvokeDirectiveReturn.Result();
            negR.setNegativeResult(negResult);
            eeaFspO.setResult(negR);
        }
    }

    private IFSP_InvokeDirective decodeInvokeDirectiveReturnOp(FspInvokeDirectiveReturnPdu eeaFspO) throws SleApiException
    {
        ISLE_Operation pOperation = null;
        IFSP_InvokeDirective pInvokeDirectiveOperation = null;

        pOperation = this.pduTranslator.getReturnOp(eeaFspO.getInvokeId(), SLE_OpType.sleOT_invokeDirective);
        if (pOperation != null)
        {
            pInvokeDirectiveOperation = pOperation.queryInterface(IFSP_InvokeDirective.class);
            if (pInvokeDirectiveOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaFspO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pInvokeDirectiveOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pInvokeDirectiveOperation.setInvokeId((int) eeaFspO.getInvokeId().value.intValue());

                // the expected directive id
                pInvokeDirectiveOperation.setExpectedDirectiveId(eeaFspO.getDirectiveIdentification().value.longValue());

                // the result
                if (eeaFspO.getResult().getPositiveResult() != null)
                {
                    // positive result
                    pInvokeDirectiveOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eeaFspO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        // specific
                        pInvokeDirectiveOperation
                                .setInvokeDirectiveDiagnostic(FSP_InvokeDirectiveDiagnostic
                                        .getInvokeDirectiveDiagnosticByCode(eeaFspO.getResult().getNegativeResult().getSpecific().value.intValue()));
                    }
                    else
                    {
                        // common
                        pInvokeDirectiveOperation.setDiagnostics(SLE_Diagnostics
                                .getDiagnosticsByCode(eeaFspO.getResult().getNegativeResult().getCommon().value.intValue()));
                    }
                }
            }
            else
            {
                throw new SleApiException(HRESULT.E_FAIL);
            }
        }

        return pInvokeDirectiveOperation;
    }
    
    /**
     * 
     *  
     * @param clcwgvcid type ICLTU_ClcwGvcId
     * @return
     */
    private ClcwGvcId encodeClcwGlobalVcid(FSP_ClcwGvcId clcwgvcid)
    {
    	GvcId gvcid = new GvcId();
    	ClcwGvcId eeaO = new ClcwGvcId();
    		
    	if(clcwgvcid != null && clcwgvcid.getConfigType() == FSP_ConfType.fspCT_configured)
    	{
    		FSP_GvcId cltuGvcId = clcwgvcid.getGvcId();	
    	
    		if(cltuGvcId != null){
    			gvcid.setSpacecraftId( new BerInteger(cltuGvcId.getScid()));
    			gvcid.setVersionNumber(new BerInteger(cltuGvcId.getVersion()));

    			switch (cltuGvcId.getType())
    			{
    			case fspCT_MasterChannel:
    			{
    				GvcId.VcId mc = new GvcId.VcId();
    				mc.setMasterChannel(new BerNull());
    				gvcid.setVcId(mc);
    				eeaO.setCongigured(gvcid);
    				eeaO.setNotConfigured(null);
    				break;
    			}
    			case fspCT_VirtualChannel:
    			{
    				//eeaO.vcId = new VcId(null, new VcId(gvcid.getVcid()));
    				ccsds.sle.transfer.service.fsp.structures.VcId id = new ccsds.sle.transfer.service.fsp.structures.VcId(cltuGvcId.getVcid());
    				GvcId.VcId vcid = new GvcId.VcId();
    				vcid.setVirtualChannel(id);
    				gvcid.setVcId(vcid);
    				eeaO.setCongigured(gvcid);
    				eeaO.setNotConfigured(null);
    				break;
    			}
    			default:
    			{
    				// GvcId is Not Configured
    				gvcid = null;
    				eeaO.setCongigured(gvcid);
    				eeaO.setNotConfigured(new BerNull());
    				break;
    			}
    			}
    		}
    	}
    	else
    	{
    		// GvcId is Not-Configured or invalid
    		eeaO.setNotConfigured(new BerNull());    		
    	}

        return eeaO;
    }
    
    /**
     * Fills the FSPGetParameter of the Asn1 object according to the input object pGetParameterOperation
     * Applicable for SLES V5 and later.
     * 
     * @param pGetParameterOperation - input
     * @param eeaO - output 
     */
    private void encodeParameter(IFSP_GetParameter pGetParameterOperation, FspGetParameter eeaO) 
    {
        switch (pGetParameterOperation.getReturnedParameter())
        {
        case fspPN_blockingTimeoutPeriod:
        {
            ParBlockingTimeout parBlockTOPeriod = new ParBlockingTimeout();
            parBlockTOPeriod.setParameterName(new ParameterName(FSP_ParameterName.fspPN_blockingTimeoutPeriod.getCode()));
            if (pGetParameterOperation.getBlockingTimeout() == 0)
            {
            	ParameterValue parVal = new ParameterValue();
            	parVal.setBlockingOff(new BerNull());
                parBlockTOPeriod.setParameterValue(new ParameterValue());
            }
            else
            {
            	BlockingTimeoutPeriod btp = new BlockingTimeoutPeriod(pGetParameterOperation.getBlockingTimeout());
            	ParameterValue pv = new ParameterValue();
            	pv.setBlockingOn(btp);
                parBlockTOPeriod.setParameterValue(pv);
            }
            eeaO.setParBlockingTimeout(parBlockTOPeriod);
            break;
        }
        case fspPN_blockingUsage:
        {
            ParBlockingUsage parBlockUsage = new ParBlockingUsage();
            parBlockUsage.setParameterName(new ParameterName(FSP_ParameterName.fspPN_blockingUsage.getCode()));
            switch (pGetParameterOperation.getBlockingUsage())
            {
            case fspAU_permitted:
            {
                parBlockUsage.setParameterValue(new BlockingUsage(FSP_BlockingUsage.fspAU_permitted.getCode()));
                break;
            }
            case fspAU_notPermitted:
            {
                parBlockUsage.setParameterValue(new BlockingUsage(FSP_BlockingUsage.fspAU_notPermitted.getCode()));
                break;
            }
            default:
            {
                parBlockUsage.setParameterValue(new BlockingUsage(FSP_BlockingUsage.fspAU_invalid.getCode()));
            }
            }
            eeaO.setParBlockingUsage(parBlockUsage);
            break;
        }
        case fspPN_apidList:
        {
            ParApidList parApidList = new ParApidList();
            parApidList.setParameterName(new ParameterName(FSP_ParameterName.fspPN_apidList.getCode()));
            ApidList apidList = new ApidList();
            parApidList.setParameterValue(apidList);
            
            long[] list = pGetParameterOperation.getApIdList();

            // A nonzero pGetParameterOperation.getApIdList() of length zero indicates any
            if(list.length == 1 && list[0] == -1)
            {
            	apidList.setAny(new BerNull());
            }
            else
            {
	            for (long i : list)
	            {
	            	if(apidList.getApidListType() == null)
	            	{
	            		apidList.setApidListType(new ApidList.ApidListType());
	            	}
	                Apid e = new Apid(i);
	            	//os.write(e.code);
	                //parApidList.getParameterValue().getApid().add(e);
	                parApidList.getParameterValue().getApidListType().getApid().add(e);
	            }
            }
            
            eeaO.setParApidList(parApidList);
            break;
        }
        case fspPN_deliveryMode:
        {
            ParDeliveryMode parDeliveryMode = new ParDeliveryMode();
            parDeliveryMode.setParameterName(new ParameterName(FSP_ParameterName.fspPN_deliveryMode.getCode()));
            parDeliveryMode.setParameterValue(new FspDeliveryMode(pGetParameterOperation.getDeliveryMode().getCode()));
            eeaO.setParDeliveryMode(parDeliveryMode);
            break;
        }
        case fspPN_directiveInvocationEnabled:
        {
            ParDirectiveInvoc parDirInv = new ParDirectiveInvoc();
            parDirInv.setParameterName(new ParameterName(FSP_ParameterName.fspPN_directiveInvocationEnabled.getCode()));
            switch (pGetParameterOperation.getDirectiveInvocationEnabled())
            {
            case sleYN_Yes:
            {
                parDirInv.setParameterValue(new BerInteger(SLE_YesNo.sleYN_Yes.getCode()));
                break;
            }
            case sleYN_No:
            {
                parDirInv.setParameterValue(new BerInteger(SLE_YesNo.sleYN_No.getCode()));
                break;
            }
            default:
            {
                parDirInv.setParameterValue(new BerInteger(SLE_YesNo.sleYN_invalid.getCode()));
                break;
            }
            }
            eeaO.setParDirectiveInvoc(parDirInv);
            break;
        }
        case fspPN_directiveInvocationOnline:
        {
            ParDirInvocOnl parDirInv = new ParDirInvocOnl();
            parDirInv.setParameterName(new ParameterName(FSP_ParameterName.fspPN_directiveInvocationOnline.getCode()));
            switch (pGetParameterOperation.getDirectiveInvocationOnline())
            {
            case sleYN_Yes:
            {
                parDirInv.setParameterValue(new BerInteger(SLE_YesNo.sleYN_Yes.getCode()));
                break;
            }
            case sleYN_No:
            {
                parDirInv.setParameterValue(new BerInteger(SLE_YesNo.sleYN_No.getCode()));
                break;
            }
            default:
            {
                parDirInv.setParameterValue(new BerInteger(SLE_YesNo.sleYN_invalid.getCode()));
                break;
            }
            }
            eeaO.setParDirInvocOnl(parDirInv);
            break;
        }
        case fspPN_bitLockRequired:
        {
            ParBitLockRequired parBitLockReq = new ParBitLockRequired();
            parBitLockReq.setParameterName(new ParameterName(FSP_ParameterName.fspPN_bitLockRequired.getCode()));
            switch (pGetParameterOperation.getBitLockRequired())
            {
            case sleYN_Yes:
            {
                parBitLockReq.setParameterValue(new BerInteger(SLE_YesNo.sleYN_Yes.getCode()));
                break;
            }
            case sleYN_No:
            {
                parBitLockReq.setParameterValue(new BerInteger(SLE_YesNo.sleYN_No.getCode()));
                break;
            }
            default:
            {
                parBitLockReq.setParameterValue(new BerInteger(SLE_YesNo.sleYN_invalid.getCode()));
                break;
            }
            }
            eeaO.setParBitLockRequired(parBitLockReq);
            break;
        }
        case fspPN_rfAvailableRequired:
        {
            ParRfAvailableRequired parRfAvailReq = new ParRfAvailableRequired();
            parRfAvailReq.setParameterName(new ParameterName(FSP_ParameterName.fspPN_rfAvailableRequired.getCode()));
            switch (pGetParameterOperation.getRfAvailableRequired())
            {
            case sleYN_Yes:
            {
                parRfAvailReq.setParameterValue(new BerInteger(SLE_YesNo.sleYN_Yes.getCode()));
                break;
            }
            case sleYN_No:
            {
                parRfAvailReq.setParameterValue(new BerInteger(SLE_YesNo.sleYN_No.getCode()));
                break;
            }
            default:
            {
                parRfAvailReq.setParameterValue(new BerInteger(SLE_YesNo.sleYN_invalid.getCode()));
                break;
            }
            }
            eeaO.setParRfAvailableRequired(parRfAvailReq);
            break;
        }
        case fspPN_expectedDirectiveId:
        {
            ParExpectDirectiveId parExpDirId = new ParExpectDirectiveId();
            parExpDirId.setParameterName(new ParameterName(FSP_ParameterName.fspPN_expectedDirectiveId.getCode()));
            parExpDirId.setParameterValue(new IntUnsignedLong(pGetParameterOperation.getExpectedDirectiveId()));
            eeaO.setParExpectDirectiveId(parExpDirId);
            break;
        }
        case fspPN_expectedEventInvocationId:
        {
            ParExpectEventInvId parExpEvInvId = new ParExpectEventInvId();
            parExpEvInvId.setParameterName(new ParameterName(FSP_ParameterName.fspPN_expectedEventInvocationId.getCode()));
            parExpEvInvId.setParameterValue(new IntUnsignedLong(pGetParameterOperation.getExpectedEventInvocationId()));
            eeaO.setParExpectEventInvId(parExpEvInvId);
            break;
        }
        case fspPN_expectedSlduIdentification:
        {
            ParExpectSlduId parExpSlduId = new ParExpectSlduId();
            parExpSlduId.setParameterName(new ParameterName(FSP_ParameterName.fspPN_expectedSlduIdentification.getCode()));
            parExpSlduId.setParameterValue(new PacketIdentification(pGetParameterOperation.getExpectedSlduId()));
            eeaO.setParExpectSlduId(parExpSlduId);
            break;
        }
        case fspPN_fopSlidingWindow:
        {
            ParFopSlidWindow parFopSlidWind = new ParFopSlidWindow();
            parFopSlidWind.setParameterName(new ParameterName(FSP_ParameterName.fspPN_fopSlidingWindow.getCode()));
            parFopSlidWind.setParameterValue(new BerInteger(pGetParameterOperation.getFopSlidingWindow()));
            eeaO.setParFopSlidWindow(parFopSlidWind);
            break;
        }
        case fspPN_fopState:
        {
            ParFopState parFopState = new ParFopState();
            parFopState.setParameterName(new ParameterName(FSP_ParameterName.fspPN_fopState.getCode()));
            switch (pGetParameterOperation.getFopState())
            {
            case fspFS_active:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_active.getCode()));
                break;
            }
            case fspFS_retransmitWithoutWait:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_retransmitWithoutWait.getCode()));
                break;
            }
            case fspFS_retransmitWithWait:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_retransmitWithWait.getCode()));
                break;
            }
            case fspFS_initialisingWithoutBCFrame:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_initialisingWithoutBCFrame.getCode()));
                break;
            }
            case fspFS_initialisingWithBCFrame:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_initialisingWithBCFrame.getCode()));
                break;
            }
            case fspFS_initial:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_initial.getCode()));
                break;
            }
            default:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_invalid.getCode()));
                break;
            }
            }
            eeaO.setParFopState(parFopState);
            break;
        }
        case fspPN_mapList:
        {
            ParMapList parMapList = new ParMapList();
            parMapList.setParameterName(new ParameterName(FSP_ParameterName.fspPN_mapList.getCode()));
            long[] list = pGetParameterOperation.getMapList();
            if (list.length != 0)
            {
                MapList mapList = new MapList();
                mapList.setMapsUsed(new MapsUsed());
                for (long i : list)
                {
                    MapId e = new MapId(i);
                    mapList.getMapsUsed().getMapId().add(e);
                }
                parMapList.setMapList(mapList);
            }
            else
            {
                MapList mapList = new MapList();
                parMapList.setMapList(mapList);
            }
            eeaO.setParMapList(parMapList);
            break;
        }
        case fspPN_mapMuxControl:
        {
            ParMapMuxControl parMapMuxControl = new ParMapMuxControl();
            parMapMuxControl.setParameterName(new ParameterName(FSP_ParameterName.fspPN_mapMuxControl.getCode()));
            FSP_AbsolutePriority[] priorityList = pGetParameterOperation.getMapPriorityList();
            long[] pollingVector = pGetParameterOperation.getMapPollingVector();
            if (priorityList != null)
            {
                MuxControl muxControl = new MuxControl();
                MuxSchemeIsPriority elem = new MuxSchemeIsPriority();
                for (FSP_AbsolutePriority i : priorityList)
                {
                    AbsolutePriority ap = new AbsolutePriority();
                    ap.setVcOrMapId(new VcOrMapId(i.getMapOrVc()));
                    ap.setPriority(new Priority(i.getPriority()));
                    elem.getAbsolutePriority().add(ap);
                }
                muxControl.setMuxSchemeIsPriority(elem);
                parMapMuxControl.setParameterValue(muxControl);
            }
            else if (pollingVector != null)
            {
                MuxControl muxControl = new MuxControl();
                MuxSchemeIsVector elem = new MuxSchemeIsVector();
                for (long i : pollingVector)
                {
                    VcOrMapId e = new VcOrMapId(i);
                    elem.getVcOrMapId().add(e);
                }
                muxControl.setMuxSchemeIsVector(elem);
                parMapMuxControl.setParameterValue(muxControl);
            }
            else
            {
            	// setup a null value for successful encoding.
            	MuxControl fifo = new MuxControl();
            	fifo.setMuxSchemeIsFifo(new BerNull());
                parMapMuxControl.setParameterValue(fifo);
            }
            eeaO.setParMapMuxControl(parMapMuxControl);
            break;
        }
        case fspPN_mapMuxScheme:
        {
            ParMapMuxScheme parMapMuxScheme = new ParMapMuxScheme();
            parMapMuxScheme.setParameterName(new ParameterName(FSP_ParameterName.fspPN_mapMuxScheme.getCode()));
            switch (pGetParameterOperation.getMapMuxScheme())
            {
            case fspMS_fifo:
            {
                parMapMuxScheme.setParameterValue(new MuxScheme(FSP_MuxScheme.fspMS_fifo.getCode()));
                break;
            }
            case fspMS_absolutePriority:
            {
                parMapMuxScheme.setParameterValue(new MuxScheme(FSP_MuxScheme.fspMS_absolutePriority.getCode()));
                break;
            }
            case fspMS_pollingVector:
            {
                parMapMuxScheme.setParameterValue(new MuxScheme(FSP_MuxScheme.fspMS_pollingVector.getCode()));
                break;
            }
            default:
            {
                parMapMuxScheme.setParameterValue(new MuxScheme());
                break;
            }
            }
            eeaO.setParMapMuxScheme(parMapMuxScheme);
            break;
        }
        case fspPN_maximumFrameLength:
        {
            ParMaxFrameLength parmaxFrameLength = new ParMaxFrameLength();
            parmaxFrameLength.setParameterName(new ParameterName(FSP_ParameterName.fspPN_maximumFrameLength.getCode()));
            parmaxFrameLength.setParameterValue(new BerInteger(pGetParameterOperation.getMaxFrameLength()));
            eeaO.setParMaxFrameLength(parmaxFrameLength);
            break;
        }
        case fspPN_maximumPacketLength:
        {
            ParMaxPacketLength parmaxPacketLength = new ParMaxPacketLength();
            parmaxPacketLength.setParameterName(new ParameterName(FSP_ParameterName.fspPN_maximumPacketLength.getCode()));
            parmaxPacketLength.setParameterValue(new BerInteger(pGetParameterOperation.getMaxPacketLength()));
            eeaO.setParMaxPacketLength(parmaxPacketLength);
            break;
        }
        case fspPN_permittedTransmissionMode:
        {
            ParPermTransMode parPermTransMode = new ParPermTransMode();
            parPermTransMode.setParameterName(new ParameterName(FSP_ParameterName.fspPN_permittedTransmissionMode.getCode()));
            switch (pGetParameterOperation.getPermittedTransmissionMode())
            {
            case fspPTM_sequenceControlled:
            {
                parPermTransMode.setParameterValue(new PermittedTransmissionMode(FSP_PermittedTransmissionMode.fspPTM_sequenceControlled.getCode()));
                break;
            }
            case fspPTM_expedited:
            {
                parPermTransMode.setParameterValue(new PermittedTransmissionMode(FSP_PermittedTransmissionMode.fspPTM_expedited.getCode()));
                break;
            }
            case fspPTM_any:
            {
                parPermTransMode.setParameterValue(new PermittedTransmissionMode(FSP_PermittedTransmissionMode.fspPTM_any.getCode()));
                break;
            }
            default:
            {
                parPermTransMode.setParameterValue(new PermittedTransmissionMode());
                break;
            }
            }
            eeaO.setParPermTransMode(parPermTransMode);
            break;
        }
        case fspPN_reportingCycle:
        {
            ParReportingCycle parRepCycle = new ParReportingCycle();
            parRepCycle.setParameterName(new ParameterName(FSP_ParameterName.fspPN_reportingCycle.getCode()));
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
            }
            eeaO.setParReportingCycle(parRepCycle);
            break;
        }
        case fspPN_returnTimeoutPeriod:
        {
            ParReturnTimeout parRtnTo = new ParReturnTimeout();
            parRtnTo.setParameterName(new ParameterName(FSP_ParameterName.fspPN_returnTimeoutPeriod.getCode()));
            parRtnTo.setParameterValue(new TimeoutPeriod(pGetParameterOperation.getReturnTimeoutPeriod()));
            eeaO.setParReturnTimeout(parRtnTo);
            break;
        }
        case fspPN_segmentHeader:
        {
            ParSegmHeader parSegmHeader = new ParSegmHeader();
            parSegmHeader.setParameterName(new ParameterName(FSP_ParameterName.fspPN_segmentHeader.getCode()));
            switch (pGetParameterOperation.getSegmentHeaderPresent())
            {
            case sleYN_Yes:
            {
                parSegmHeader.setParameterValue(new BerInteger(SLE_YesNo.sleYN_Yes.getCode()));
                break;
            }
            case sleYN_No:
            {
                parSegmHeader.setParameterValue(new BerInteger(SLE_YesNo.sleYN_No.getCode()));
                break;
            }
            default:
            {
                parSegmHeader.setParameterValue(new BerInteger(-1));
                break;
            }
            }
            eeaO.setParSegmHeader(parSegmHeader);
            break;
        }
        case fspPN_timeoutType:
        {
            ParTimeoutType parTOTime = new ParTimeoutType();
            parTOTime.setParameterName(new ParameterName(FSP_ParameterName.fspPN_timeoutType.getCode()));
            switch (pGetParameterOperation.getTimeoutType())
            {
            case fspTT_generateAlert:
            {
                parTOTime.setParameterValue(new BerInteger(FSP_TimeoutType.fspTT_generateAlert.getCode()));
                break;
            }
            case fspTT_suspendAD:
            {
                parTOTime.setParameterValue(new BerInteger(FSP_TimeoutType.fspTT_suspendAD.getCode()));
                break;
            }
            default:
            {
                parTOTime.setParameterValue(new BerInteger(-1));
                break;
            }
            }
            eeaO.setParTimeoutType(parTOTime);
            break;
        }
        case fspPN_timerInitial:
        {
            ParTimerInitial parTimerInit = new ParTimerInitial();
            parTimerInit.setParameterName(new ParameterName(FSP_ParameterName.fspPN_timerInitial.getCode()));
            parTimerInit.setParameterValue(new IntPosLong(pGetParameterOperation.getTimerInitial()));
            eeaO.setParTimerInitial(parTimerInit);
            break;
        }
        case fspPN_transmissionLimit:
        {
            ParTransmissLimit parTransmLimit = new ParTransmissLimit();
            parTransmLimit.setParameterName(new ParameterName(FSP_ParameterName.fspPN_transmissionLimit.getCode()));
            parTransmLimit.setParameterValue(new IntPosLong(pGetParameterOperation.getTransmissionLimit()));
            eeaO.setParTransmissLimit(parTransmLimit);
            break;
        }
        case fspPN_transmitterFrameSequenceNumber:
        {
            ParTrFrSeqNumber parTransmFrSeqNbr = new ParTrFrSeqNumber();
            parTransmFrSeqNbr.setParameterName(new ParameterName(FSP_ParameterName.fspPN_transmitterFrameSequenceNumber.getCode()));
            parTransmFrSeqNbr.setParameterValue(new IntPosLong(pGetParameterOperation.getTransmitterFrameSequenceNumber()));
            eeaO.setParTrFrSeqNumber(parTransmFrSeqNbr);
            break;
        }
        case fspPN_vcMuxControl:
        {
            ParVcMuxControl parVcMuxControl = new ParVcMuxControl();
            parVcMuxControl.setParameterName(new ParameterName(FSP_ParameterName.fspPN_vcMuxControl.getCode()));
            FSP_AbsolutePriority[] priorityList = pGetParameterOperation.getVcPriorityList();
            long[] pollingVector = pGetParameterOperation.getVcPollingVector();
            if (priorityList != null && priorityList.length != 0)
            {
                MuxControl muxControl = new MuxControl();
                MuxSchemeIsPriority elem = new MuxSchemeIsPriority();
                for (FSP_AbsolutePriority i : priorityList)
                {
                    AbsolutePriority ap = new AbsolutePriority();
                    ap.setVcOrMapId(new VcOrMapId(i.getMapOrVc()));
                    ap.setPriority(new Priority(i.getPriority()));
                    elem.getAbsolutePriority().add(ap);
                }
                muxControl.setMuxSchemeIsPriority(elem);
                parVcMuxControl.setParameterValue(muxControl);
            }
            else if (pollingVector != null && pollingVector.length != 0)
            {
                MuxControl muxControl = new MuxControl();
                MuxSchemeIsVector elem = new MuxSchemeIsVector();
                for (long i : pollingVector)
                {
                    VcOrMapId e = new VcOrMapId(i);
                    elem.getVcOrMapId().add(e);
                }
                muxControl.setMuxSchemeIsVector(elem);
                parVcMuxControl.setParameterValue(muxControl);
            }
            else
            {
            	MuxControl fifo = new MuxControl();
            	fifo.setMuxSchemeIsFifo(new BerNull());
                parVcMuxControl.setParameterValue(fifo);
            }
            eeaO.setParVcMuxControl(parVcMuxControl);
            break;
        }
        case fspPN_vcMuxScheme:
        {
            ParVcMuxScheme parvcMuxScheme = new ParVcMuxScheme();
            parvcMuxScheme.setParameterName(new ParameterName(FSP_ParameterName.fspPN_vcMuxScheme.getCode()));
            parvcMuxScheme.setParameterValue(new MuxScheme(pGetParameterOperation.getVcMuxScheme().getCode()));
            eeaO.setParVcMuxScheme(parvcMuxScheme);
            break;
        }
        case fspPN_virtualChannel:
        {
            ParVirtualChannel parVChannel = new ParVirtualChannel();
            parVChannel.setParameterName(new ParameterName(FSP_ParameterName.fspPN_virtualChannel.getCode()));
            parVChannel.setParameterValue(new VcOrMapId(pGetParameterOperation.getVirtualChannel()));
            eeaO.setParVirtualChannel(parVChannel);
            break;
        }
        
        case fspPN_clcwGlobalVcId:			// ParameterNameID: 202 - FspGetParameterID: 29
        {
        	ParClcwGlobalVcId parClcwGlobalVcid = new ParClcwGlobalVcId();
        	parClcwGlobalVcid.setParameterName(new ParameterName(FSP_ParameterName.fspPN_clcwGlobalVcId.getCode()));
        	parClcwGlobalVcid.setParameterValue(encodeClcwGlobalVcid(pGetParameterOperation.getClcwGlobalVcid()));
        	eeaO.setParClcwGlobalVcId(parClcwGlobalVcid);
        	break;
        }
        case fspPN_clcwPhysicalChannel:		// ParameterNameID: 203 - FspGetParameterID: 30
        {
        	ParClcwPhysicalChannel parClcwPhysicalChannel = new ParClcwPhysicalChannel();
        	parClcwPhysicalChannel.setParameterName(new ParameterName(FSP_ParameterName.fspPN_clcwPhysicalChannel.getCode()));
        	FSP_ClcwPhysicalChannel clcwPhyChannel = pGetParameterOperation.getClcwPhysicalChannel();
        	ClcwPhysicalChannel cpch = new ClcwPhysicalChannel();
        	if(clcwPhyChannel != null && clcwPhyChannel.getConfigType()== FSP_ConfType.fspCT_configured)
        	{
        		cpch.setConfigured(new BerVisibleString(clcwPhyChannel.getClcwPhysicalChannel()));
        	}
        	else
        	{
        		cpch.setNotConfigured(new BerNull());
        	}
        	parClcwPhysicalChannel.setParameterValue(cpch);
        	eeaO.setParClcwPhysicalChannel(parClcwPhysicalChannel);       	
        	break;
        }
        case fspPN_copCntrFramesRepetion:	// ParameterNameID: 300 - FspGetParameterID: 31
        {
        	ParCopCntrFramesRepetition ccfr = new ParCopCntrFramesRepetition();
        	ccfr.setParameterName(new ParameterName(FSP_ParameterName.fspPN_copCntrFramesRepetion.getCode()));
        	ccfr.setParameterValue(new IntPosShort(pGetParameterOperation.getCopCntrFramesRepetition()));
            eeaO.setParCopCntrFramesRepetition(ccfr); 	
        	break;
        }
        case fspPN_minReportingCycle:		// ParameterNameID: 301 - FspGetParameterID: 32
        {
        	ParMinReportingCycle mrc = new ParMinReportingCycle();
        	mrc.setParameterName(new ParameterName(FSP_ParameterName.fspPN_minReportingCycle.getCode()));
        	mrc.setParameterValue(new IntPosShort(pGetParameterOperation.getMinReportingCycle()));
            eeaO.setParMinReportingCycle(mrc); 	
        	break;
        }
        case fspPN_seqCntrFramesRepetition:	// ParameterNameID: 303 - FspGetParameterID: 33
        {
        	ParSequCntrFramesRepetition scfr = new ParSequCntrFramesRepetition();
        	scfr.setParameterName(new ParameterName(FSP_ParameterName.fspPN_seqCntrFramesRepetition.getCode()));
        	scfr.setParameterValue(new IntPosShort(pGetParameterOperation.getSeqCntrFramesRepetition()));
            eeaO.setParSequCntrFramesRepetition(scfr); 	
        	break;
        }
        case fspPN_throwEventOperation:		// ParameterNameID: 304 - FspGetParameterID: 34
        {
        	ParThrowEventOperation teo = new ParThrowEventOperation();
        	teo.setParameterName(new ParameterName(FSP_ParameterName.fspPN_throwEventOperation.getCode()));
        	SLE_YesNo yn = pGetParameterOperation.getThrowEventOperation();
            switch (yn)
            {
            case sleYN_Yes:
            {
            	teo.setParameterValue(new BerInteger(SLE_YesNo.sleYN_Yes.getCode()));
                break;
            }
            case sleYN_No:
            {
            	teo.setParameterValue(new BerInteger(SLE_YesNo.sleYN_No.getCode()));
                break;
            }
            default:
            {
            	teo.setParameterValue(new BerInteger(-1));
                break;
            }
            }
            eeaO.setParThrowEventOperation(teo);
        	
        	break;
        }
        case fspPN_invalid:
        {
            break;
        }
        default:
        {
            break;
        }
        }
    }

    
    
    /**
     * Fills the FSP Parameter of the Asn1 object of SLES V2 .. V4.
     * @throws IOException 
     */
    private void encodeParameterV4(IFSP_GetParameter pGetParameterOperation, FspGetParameter eeaO) 
    {
        switch (pGetParameterOperation.getReturnedParameter())
        {
        case fspPN_blockingTimeoutPeriod:
        {
            ParBlockingTimeout parBlockTOPeriod = new ParBlockingTimeout();
            parBlockTOPeriod.setParameterName(new ParameterName(FSP_ParameterName.fspPN_blockingTimeoutPeriod.getCode()));
            if (pGetParameterOperation.getBlockingTimeout() == 0)
            {
            	ParameterValue parVal = new ParameterValue();
            	parVal.setBlockingOff(new BerNull());
                parBlockTOPeriod.setParameterValue(new ParameterValue());
            }
            else
            {
            	BlockingTimeoutPeriod btp = new BlockingTimeoutPeriod(pGetParameterOperation.getBlockingTimeout());
            	ParameterValue pv = new ParameterValue();
            	pv.setBlockingOn(btp);
                parBlockTOPeriod.setParameterValue(pv);
            }
            eeaO.setParBlockingTimeout(parBlockTOPeriod);
            break;
        }
        case fspPN_blockingUsage:
        {
            ParBlockingUsage parBlockUsage = new ParBlockingUsage();
            parBlockUsage.setParameterName(new ParameterName(FSP_ParameterName.fspPN_blockingUsage.getCode()));
            switch (pGetParameterOperation.getBlockingUsage())
            {
            case fspAU_permitted:
            {
                parBlockUsage.setParameterValue(new BlockingUsage(FSP_BlockingUsage.fspAU_permitted.getCode()));
                break;
            }
            case fspAU_notPermitted:
            {
                parBlockUsage.setParameterValue(new BlockingUsage(FSP_BlockingUsage.fspAU_notPermitted.getCode()));
            }
            default:
            {
                parBlockUsage.setParameterValue(new BlockingUsage(FSP_BlockingUsage.fspAU_invalid.getCode()));
            }
            }
            eeaO.setParBlockingUsage(parBlockUsage);
            break;
        }
        case fspPN_apidList:
        {
            ParApidList parApidList = new ParApidList();
            parApidList.setParameterName(new ParameterName(FSP_ParameterName.fspPN_apidList.getCode()));
            parApidList.setParameterValue(new ApidList());
            long[] list = pGetParameterOperation.getApIdList();
            // A nonzero pGetParameterOperation.getApIdList() of length zero indicates any
            if(list.length == 1 && list[0] == -1)
            {
            	parApidList.getParameterValue().setAny(new BerNull());
            }
            else
            {
	            for (long i : list)
	            {
	                Apid e = new Apid(i);
	            	//os.write(e.code);
	                parApidList.getParameterValue().getApidListType().getApid().add(e);
	            }
            }
            eeaO.setParApidList(parApidList);
            break;
        }
        case fspPN_deliveryMode:
        {
            ParDeliveryMode parDeliveryMode = new ParDeliveryMode();
            parDeliveryMode.setParameterName(new ParameterName(FSP_ParameterName.fspPN_deliveryMode.getCode()));
            parDeliveryMode.setParameterValue(new FspDeliveryMode(pGetParameterOperation.getDeliveryMode().getCode()));
            eeaO.setParDeliveryMode(parDeliveryMode);
            break;
        }
        case fspPN_directiveInvocationEnabled:
        {
            ParDirectiveInvoc parDirInv = new ParDirectiveInvoc();
            parDirInv.setParameterName(new ParameterName(FSP_ParameterName.fspPN_directiveInvocationEnabled.getCode()));
            switch (pGetParameterOperation.getDirectiveInvocationEnabled())
            {
            case sleYN_Yes:
            {
                parDirInv.setParameterValue(new BerInteger(SLE_YesNo.sleYN_Yes.getCode()));
                break;
            }
            case sleYN_No:
            {
                parDirInv.setParameterValue(new BerInteger(SLE_YesNo.sleYN_No.getCode()));
                break;
            }
            default:
            {
                parDirInv.setParameterValue(new BerInteger(SLE_YesNo.sleYN_invalid.getCode()));
                break;
            }
            }
            eeaO.setParDirectiveInvoc(parDirInv);
            break;
        }
        case fspPN_directiveInvocationOnline:
        {
            ParDirInvocOnl parDirInv = new ParDirInvocOnl();
            parDirInv.setParameterName(new ParameterName(FSP_ParameterName.fspPN_directiveInvocationOnline.getCode()));
            switch (pGetParameterOperation.getDirectiveInvocationOnline())
            {
            case sleYN_Yes:
            {
                parDirInv.setParameterValue(new BerInteger(SLE_YesNo.sleYN_Yes.getCode()));
                break;
            }
            case sleYN_No:
            {
                parDirInv.setParameterValue(new BerInteger(SLE_YesNo.sleYN_No.getCode()));
                break;
            }
            default:
            {
                parDirInv.setParameterValue(new BerInteger(SLE_YesNo.sleYN_invalid.getCode()));
                break;
            }
            }
            eeaO.setParDirInvocOnl(parDirInv);
            break;
        }
        case fspPN_bitLockRequired:
        {
            ParBitLockRequired parBitLockReq = new ParBitLockRequired();
            parBitLockReq.setParameterName(new ParameterName(FSP_ParameterName.fspPN_bitLockRequired.getCode()));
            switch (pGetParameterOperation.getBitLockRequired())
            {
            case sleYN_Yes:
            {
                parBitLockReq.setParameterValue(new BerInteger(SLE_YesNo.sleYN_Yes.getCode()));
                break;
            }
            case sleYN_No:
            {
                parBitLockReq.setParameterValue(new BerInteger(SLE_YesNo.sleYN_No.getCode()));
                break;
            }
            default:
            {
                parBitLockReq.setParameterValue(new BerInteger(SLE_YesNo.sleYN_invalid.getCode()));
                break;
            }
            }
            eeaO.setParBitLockRequired(parBitLockReq);
            break;
        }
        case fspPN_rfAvailableRequired:
        {
            ParRfAvailableRequired parRfAvailReq = new ParRfAvailableRequired();
            parRfAvailReq.setParameterName(new ParameterName(FSP_ParameterName.fspPN_rfAvailableRequired.getCode()));
            switch (pGetParameterOperation.getRfAvailableRequired())
            {
            case sleYN_Yes:
            {
                parRfAvailReq.setParameterValue(new BerInteger(SLE_YesNo.sleYN_Yes.getCode()));
                break;
            }
            case sleYN_No:
            {
                parRfAvailReq.setParameterValue(new BerInteger(SLE_YesNo.sleYN_No.getCode()));
                break;
            }
            default:
            {
                parRfAvailReq.setParameterValue(new BerInteger(SLE_YesNo.sleYN_invalid.getCode()));
                break;
            }
            }
            eeaO.setParRfAvailableRequired(parRfAvailReq);
            break;
        }
        case fspPN_expectedDirectiveId:
        {
            ParExpectDirectiveId parExpDirId = new ParExpectDirectiveId();
            parExpDirId.setParameterName(new ParameterName(FSP_ParameterName.fspPN_expectedDirectiveId.getCode()));
            parExpDirId.setParameterValue(new IntUnsignedLong(pGetParameterOperation.getExpectedDirectiveId()));
            eeaO.setParExpectDirectiveId(parExpDirId);
            break;
        }
        case fspPN_expectedEventInvocationId:
        {
            ParExpectEventInvId parExpEvInvId = new ParExpectEventInvId();
            parExpEvInvId.setParameterName(new ParameterName(FSP_ParameterName.fspPN_expectedEventInvocationId.getCode()));
            parExpEvInvId.setParameterValue(new IntUnsignedLong(pGetParameterOperation.getExpectedEventInvocationId()));
            eeaO.setParExpectEventInvId(parExpEvInvId);
            break;
        }
        case fspPN_expectedSlduIdentification:
        {
            ParExpectSlduId parExpSlduId = new ParExpectSlduId();
            parExpSlduId.setParameterName(new ParameterName(FSP_ParameterName.fspPN_expectedSlduIdentification.getCode()));
            parExpSlduId.setParameterValue(new PacketIdentification(pGetParameterOperation.getExpectedSlduId()));
            eeaO.setParExpectSlduId(parExpSlduId);
            break;
        }
        case fspPN_fopSlidingWindow:
        {
            ParFopSlidWindow parFopSlidWind = new ParFopSlidWindow();
            parFopSlidWind.setParameterName(new ParameterName(FSP_ParameterName.fspPN_fopSlidingWindow.getCode()));
            parFopSlidWind.setParameterValue(new BerInteger(pGetParameterOperation.getFopSlidingWindow()));
            eeaO.setParFopSlidWindow(parFopSlidWind);
            break;
        }
        case fspPN_fopState:
        {
            ParFopState parFopState = new ParFopState();
            parFopState.setParameterName(new ParameterName(FSP_ParameterName.fspPN_fopState.getCode()));
            switch (pGetParameterOperation.getFopState())
            {
            case fspFS_active:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_active.getCode()));
                break;
            }
            case fspFS_retransmitWithoutWait:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_retransmitWithoutWait.getCode()));
                break;
            }
            case fspFS_retransmitWithWait:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_retransmitWithWait.getCode()));
                break;
            }
            case fspFS_initialisingWithoutBCFrame:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_initialisingWithoutBCFrame.getCode()));
                break;
            }
            case fspFS_initialisingWithBCFrame:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_initialisingWithBCFrame.getCode()));
                break;
            }
            case fspFS_initial:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_initial.getCode()));
                break;
            }
            default:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_invalid.getCode()));
                break;
            }
            }
            eeaO.setParFopState(parFopState);
            break;
        }
        case fspPN_mapList:
        {
            ParMapList parMapList = new ParMapList();
            parMapList.setParameterName(new ParameterName(FSP_ParameterName.fspPN_mapList.getCode()));
            long[] list = pGetParameterOperation.getMapList();
            if (list.length != 0)
            {
                MapList mapList = new MapList();
                mapList.setMapsUsed(new MapsUsed());
                for (long i : list)
                {
                    MapId e = new MapId(i);
                    mapList.getMapsUsed().getMapId().add(e);
                }
                parMapList.setMapList(mapList);
            }
            else
            {
                MapList mapList = new MapList();
                parMapList.setMapList(mapList);
            }
            eeaO.setParMapList(parMapList);
            break;
        }
        case fspPN_mapMuxControl:
        {
            ParMapMuxControl parMapMuxControl = new ParMapMuxControl();
            parMapMuxControl.setParameterName(new ParameterName(FSP_ParameterName.fspPN_mapMuxControl.getCode()));
            FSP_AbsolutePriority[] priorityList = pGetParameterOperation.getMapPriorityList();
            long[] pollingVector = pGetParameterOperation.getMapPollingVector();
            if (priorityList != null)
            {
                MuxControl muxControl = new MuxControl();
                MuxSchemeIsPriority elem = new MuxSchemeIsPriority();
                for (FSP_AbsolutePriority i : priorityList)
                {
                    AbsolutePriority ap = new AbsolutePriority();
                    ap.setVcOrMapId(new VcOrMapId(i.getMapOrVc()));
                    ap.setPriority(new Priority(i.getPriority()));
                    elem.getAbsolutePriority().add(ap);
                }
                muxControl.setMuxSchemeIsPriority(elem);
                parMapMuxControl.setParameterValue(muxControl);
            }
            else if (pollingVector != null)
            {
                MuxControl muxControl = new MuxControl();
                MuxSchemeIsVector elem = new MuxSchemeIsVector();
                for (long i : pollingVector)
                {
                    VcOrMapId e = new VcOrMapId(i);
                    elem.getVcOrMapId().add(e);
                }
                muxControl.setMuxSchemeIsVector(elem);
                parMapMuxControl.setParameterValue(muxControl);
            }
            else
            {
            	// SLEAPIJ-80
            	MuxControl muxSchemeFifo = new MuxControl();
            	muxSchemeFifo.setMuxSchemeIsFifo(new BerNull());
                parMapMuxControl.setParameterValue(muxSchemeFifo);
            }
            eeaO.setParMapMuxControl(parMapMuxControl);
            break;
        }
        case fspPN_mapMuxScheme:
        {
            ParMapMuxScheme parMapMuxScheme = new ParMapMuxScheme();
            parMapMuxScheme.setParameterName(new ParameterName(FSP_ParameterName.fspPN_mapMuxScheme.getCode()));
            switch (pGetParameterOperation.getMapMuxScheme())
            {
            case fspMS_fifo:
            {
                parMapMuxScheme.setParameterValue(new MuxScheme(FSP_MuxScheme.fspMS_fifo.getCode()));
                break;
            }
            case fspMS_absolutePriority:
            {
                parMapMuxScheme.setParameterValue(new MuxScheme(FSP_MuxScheme.fspMS_absolutePriority.getCode()));
                break;
            }
            case fspMS_pollingVector:
            {
                parMapMuxScheme.setParameterValue(new MuxScheme(FSP_MuxScheme.fspMS_pollingVector.getCode()));
                break;
            }
            default:
            {
                parMapMuxScheme.setParameterValue(new MuxScheme());
                break;
            }
            }
            eeaO.setParMapMuxScheme(parMapMuxScheme);
            break;
        }
        case fspPN_maximumFrameLength:
        {
            ParMaxFrameLength parmaxFrameLength = new ParMaxFrameLength();
            parmaxFrameLength.setParameterName(new ParameterName(FSP_ParameterName.fspPN_maximumFrameLength.getCode()));
            parmaxFrameLength.setParameterValue(new BerInteger(pGetParameterOperation.getMaxFrameLength()));
            eeaO.setParMaxFrameLength(parmaxFrameLength);
            break;
        }
        case fspPN_maximumPacketLength:
        {
            ParMaxPacketLength parmaxPacketLength = new ParMaxPacketLength();
            parmaxPacketLength.setParameterName(new ParameterName(FSP_ParameterName.fspPN_maximumPacketLength.getCode()));
            parmaxPacketLength.setParameterValue(new BerInteger(pGetParameterOperation.getMaxPacketLength()));
            eeaO.setParMaxPacketLength(parmaxPacketLength);
            break;
        }
        case fspPN_permittedTransmissionMode:
        {
            ParPermTransMode parPermTransMode = new ParPermTransMode();
            parPermTransMode.setParameterName(new ParameterName(FSP_ParameterName.fspPN_permittedTransmissionMode.getCode()));
            switch (pGetParameterOperation.getPermittedTransmissionMode())
            {
            case fspPTM_sequenceControlled:
            {
                parPermTransMode.setParameterValue(new PermittedTransmissionMode(FSP_PermittedTransmissionMode.fspPTM_sequenceControlled.getCode()));
                break;
            }
            case fspPTM_expedited:
            {
                parPermTransMode.setParameterValue(new PermittedTransmissionMode(FSP_PermittedTransmissionMode.fspPTM_expedited.getCode()));
                break;
            }
            case fspPTM_any:
            {
                parPermTransMode.setParameterValue(new PermittedTransmissionMode(FSP_PermittedTransmissionMode.fspPTM_any.getCode()));
                break;
            }
            default:
            {
                parPermTransMode.setParameterValue(new PermittedTransmissionMode());
                break;
            }
            }
            eeaO.setParPermTransMode(parPermTransMode);
            break;
        }
        case fspPN_reportingCycle:
        {
            ParReportingCycle parRepCycle = new ParReportingCycle();
            parRepCycle.setParameterName(new ParameterName(FSP_ParameterName.fspPN_reportingCycle.getCode()));
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
            }
            eeaO.setParReportingCycle(parRepCycle);
            break;
        }
        case fspPN_returnTimeoutPeriod:
        {
            ParReturnTimeout parRtnTo = new ParReturnTimeout();
            parRtnTo.setParameterName(new ParameterName(FSP_ParameterName.fspPN_returnTimeoutPeriod.getCode()));
            parRtnTo.setParameterValue(new TimeoutPeriod(pGetParameterOperation.getReturnTimeoutPeriod()));
            eeaO.setParReturnTimeout(parRtnTo);
            break;
        }
        case fspPN_segmentHeader:
        {
            ParSegmHeader parSegmHeader = new ParSegmHeader();
            parSegmHeader.setParameterName(new ParameterName(FSP_ParameterName.fspPN_segmentHeader.getCode()));
            switch (pGetParameterOperation.getSegmentHeaderPresent())
            {
            case sleYN_Yes:
            {
                parSegmHeader.setParameterValue(new BerInteger(SLE_YesNo.sleYN_Yes.getCode()));
                break;
            }
            case sleYN_No:
            {
                parSegmHeader.setParameterValue(new BerInteger(SLE_YesNo.sleYN_No.getCode()));
                break;
            }
            default:
            {
                parSegmHeader.setParameterValue(new BerInteger(-1));
                break;
            }
            }
            eeaO.setParSegmHeader(parSegmHeader);
            break;
        }
        case fspPN_timeoutType:
        {
            ParTimeoutType parTOTime = new ParTimeoutType();
            parTOTime.setParameterName(new ParameterName(FSP_ParameterName.fspPN_timeoutType.getCode()));
            switch (pGetParameterOperation.getTimeoutType())
            {
            case fspTT_generateAlert:
            {
                parTOTime.setParameterValue(new BerInteger(FSP_TimeoutType.fspTT_generateAlert.getCode()));
                break;
            }
            case fspTT_suspendAD:
            {
                parTOTime.setParameterValue(new BerInteger(FSP_TimeoutType.fspTT_suspendAD.getCode()));
                break;
            }
            default:
            {
                parTOTime.setParameterValue(new BerInteger(-1));
                break;
            }
            }
            eeaO.setParTimeoutType(parTOTime);
            break;
        }
        case fspPN_timerInitial:
        {
            ParTimerInitial parTimerInit = new ParTimerInitial();
            parTimerInit.setParameterName(new ParameterName(FSP_ParameterName.fspPN_timerInitial.getCode()));
            parTimerInit.setParameterValue(new IntPosLong(pGetParameterOperation.getTimerInitial()));
            eeaO.setParTimerInitial(parTimerInit);
            break;
        }
        case fspPN_transmissionLimit:
        {
            ParTransmissLimit parTransmLimit = new ParTransmissLimit();
            parTransmLimit.setParameterName(new ParameterName(FSP_ParameterName.fspPN_transmissionLimit.getCode()));
            parTransmLimit.setParameterValue(new IntPosLong(pGetParameterOperation.getTransmissionLimit()));
            eeaO.setParTransmissLimit(parTransmLimit);
            break;
        }
        case fspPN_transmitterFrameSequenceNumber:
        {
            ParTrFrSeqNumber parTransmFrSeqNbr = new ParTrFrSeqNumber();
            parTransmFrSeqNbr.setParameterName(new ParameterName(FSP_ParameterName.fspPN_transmitterFrameSequenceNumber.getCode()));
            parTransmFrSeqNbr.setParameterValue(new IntPosLong(pGetParameterOperation.getTransmitterFrameSequenceNumber()));
            eeaO.setParTrFrSeqNumber(parTransmFrSeqNbr);
            break;
        }
        case fspPN_vcMuxControl:
        {
            ParVcMuxControl parVcMuxControl = new ParVcMuxControl();
            parVcMuxControl.setParameterName(new ParameterName(FSP_ParameterName.fspPN_vcMuxControl.getCode()));
            FSP_AbsolutePriority[] priorityList = pGetParameterOperation.getVcPriorityList();
            long[] pollingVector = pGetParameterOperation.getVcPollingVector();
            if (priorityList.length != 0)
            {
                MuxControl muxControl = new MuxControl();
                MuxSchemeIsPriority elem = new MuxSchemeIsPriority();
                for (FSP_AbsolutePriority i : priorityList)
                {
                    AbsolutePriority ap = new AbsolutePriority();
                    ap.setVcOrMapId(new VcOrMapId(i.getMapOrVc()));
                    ap.setPriority(new Priority(i.getPriority()));
                    elem.getAbsolutePriority().add(ap);
                }
                muxControl.setMuxSchemeIsPriority(elem);
                parVcMuxControl.setParameterValue(muxControl);
            }
            else if (pollingVector.length != 0)
            {
                MuxControl muxControl = new MuxControl();
                MuxSchemeIsVector elem = new MuxSchemeIsVector();
                for (long i : pollingVector)
                {
                    VcOrMapId e = new VcOrMapId(i);
                    elem.getVcOrMapId().add(e);
                }
                muxControl.setMuxSchemeIsVector(elem);
                parVcMuxControl.setParameterValue(muxControl);
            }
            else
            {
                parVcMuxControl.setParameterValue(new MuxControl());
            }
            eeaO.setParVcMuxControl(parVcMuxControl);
            break;
        }
        case fspPN_vcMuxScheme:
        {
            ParVcMuxScheme parvcMuxScheme = new ParVcMuxScheme();
            parvcMuxScheme.setParameterName(new ParameterName(FSP_ParameterName.fspPN_vcMuxScheme.getCode()));
            parvcMuxScheme.setParameterValue(new MuxScheme(pGetParameterOperation.getVcMuxScheme().getCode()));
            eeaO.setParVcMuxScheme(parvcMuxScheme);
            break;
        }
        case fspPN_virtualChannel:
        {
            ParVirtualChannel parVChannel = new ParVirtualChannel();
            parVChannel.setParameterName(new ParameterName(FSP_ParameterName.fspPN_virtualChannel.getCode()));
            parVChannel.setParameterValue(new VcOrMapId(pGetParameterOperation.getVirtualChannel()));
            eeaO.setParVirtualChannel(parVChannel);
            break;
        }
        case fspPN_invalid:
        {
            break;
        }
        default:
        {
            break;
        }
        }
    }

    /**
     * Fills the parameter of the Fsp GetParameter return operation from the
     * object.
     */
    private void decodeParameter(FspGetParameter eeaO, IFSP_GetParameter pGetParameterOperation)
    {
        if (eeaO.getParBlockingTimeout() != null)
        {
            if (eeaO.getParBlockingTimeout().getParameterValue().getBlockingOn() != null)
            {
                pGetParameterOperation.setBlockingTimeout(eeaO.getParBlockingTimeout().getParameterValue().getBlockingOn().value.longValue());
            }
            else
            {
                pGetParameterOperation.setBlockingTimeout(0);
            }
        }
        else if (eeaO.getParBlockingUsage() != null)
        {
            pGetParameterOperation.setBlockingUsage(FSP_BlockingUsage
                    .getFSP_BlockingUsageByCode((int) eeaO.getParBlockingUsage().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParApidList() != null)
        {
        	if(eeaO.getParApidList().getParameterValue().getAny() != null)
        	{
        		long[] any = {-1};
        		pGetParameterOperation.putApIdList(any);
        	}
        	else
        	{
	            int size = eeaO.getParApidList().getParameterValue().getApidListType().getApid().size();
	            long[] plist = new long[size];
	            int count = 0;
	            for (Apid i : eeaO.getParApidList().getParameterValue().getApidListType().getApid())
	            {
	                plist[count++] = i.value.intValue();
	            }
	            pGetParameterOperation.putApIdList(plist);
        	}
        }
        else if (eeaO.getParDeliveryMode() != null)
        {
            SLE_DeliveryMode delMode = SLE_DeliveryMode
                    .getDelModeByCode(eeaO.getParDeliveryMode().getParameterValue().value.intValue());
            if (delMode == SLE_DeliveryMode.sleDM_fwdOnline)
            {
                pGetParameterOperation.setDeliveryMode();
            }
        }
        else if (eeaO.getParDirectiveInvoc() != null)
        {
            pGetParameterOperation.setDirectiveInvocationEnabled(SLE_YesNo
                    .getYesNoByCode(eeaO.getParDirectiveInvoc().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParDirInvocOnl() != null)
        {
            pGetParameterOperation.setDirectiveInvocationOnline(SLE_YesNo
                    .getYesNoByCode( eeaO.getParDirInvocOnl().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParBitLockRequired() != null)
        {
            pGetParameterOperation.setBitLockRequired(SLE_YesNo
                    .getYesNoByCode(eeaO.getParBitLockRequired().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParRfAvailableRequired() != null)
        {
            pGetParameterOperation.setRfAvailableRequired(SLE_YesNo
                    .getYesNoByCode(eeaO.getParRfAvailableRequired().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParExpectDirectiveId() != null)
        {
            pGetParameterOperation.setExpectedDirectiveId(eeaO.getParExpectDirectiveId().getParameterValue().value.longValue());
        }
        else if (eeaO.getParExpectEventInvId() != null)
        {
            pGetParameterOperation.setExpectedEventInvocationId(eeaO.getParExpectEventInvId().getParameterValue().value.longValue());
        }
        else if (eeaO.getParExpectSlduId() != null)
        {
            pGetParameterOperation.setExpectedSlduId(eeaO.getParExpectSlduId().getParameterValue().value.longValue());
        }
        else if (eeaO.getParFopSlidWindow() != null)
        {
            pGetParameterOperation.setFopSlidingWindow(eeaO.getParFopSlidWindow().getParameterValue().value.longValue());
        }
        else if (eeaO.getParFopState() != null)
        {
            pGetParameterOperation
                    .setFopState(FSP_FopState.getFSPFopStateByCode(eeaO.getParFopState().getFopState().value.intValue()));
        }
        else if (eeaO.getParMapList() != null)
        {
            if (eeaO.getParMapList().getMapList().getMapsUsed() != null)
            {
                int size = eeaO.getParMapList().getMapList().getMapsUsed().getMapId().size();
                long[] plist = new long[size];
                int count = 0;
                for (MapId i : eeaO.getParMapList().getMapList().getMapsUsed().getMapId())
                {
                    plist[count++] = i.value.longValue();
                }
                pGetParameterOperation.setMapList(plist);
            }
            else
            {
                pGetParameterOperation.setMapList(null);
            }
        }
        else if (eeaO.getParMapMuxControl() != null)
        {
            if (eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsFifo() != null)
            {
                pGetParameterOperation.setMapPriorityList(null);
                pGetParameterOperation.setMapPollingVector(null);
            }
            else if (eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsPriority() != null)
            {
                int size = eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsPriority().getAbsolutePriority().size();
                FSP_AbsolutePriority[] plist = new FSP_AbsolutePriority[size];
                int count = 0;
                for (AbsolutePriority i : eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsPriority().getAbsolutePriority())
                {
                    FSP_AbsolutePriority ap = new FSP_AbsolutePriority();
                    ap.setMapOrVc(i.getVcOrMapId().value.intValue());
                    ap.setPriority(i.getPriority().value.intValue());
                    plist[count++] = ap;
                }
                pGetParameterOperation.putMapPriorityList(plist);
                pGetParameterOperation.setMapPollingVector(null);
            }
            else if (eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsVector() != null)
            {
                int size = eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsVector().getVcOrMapId().size();
                long[] plist = new long[size];
                int count = 0;
                for (VcOrMapId i : eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsVector().getVcOrMapId())
                {
                    plist[count++] = i.value.longValue();
                }
                pGetParameterOperation.putMapPollingVector(plist);
                pGetParameterOperation.setMapPollingVector(null);
            }
        }
        else if (eeaO.getParMapMuxScheme() != null)
        {
            pGetParameterOperation.setMapMuxScheme(FSP_MuxScheme
                    .getFSP_MuxSchemeByCode(eeaO.getParMapMuxScheme().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParMaxFrameLength() != null)
        {
            pGetParameterOperation.setMaxFrameLength(eeaO.getParMaxFrameLength().getParameterValue().value.longValue());
        }
        else if (eeaO.getParMaxPacketLength() != null)
        {
            pGetParameterOperation.setMaxPacketLength(eeaO.getParMaxPacketLength().getParameterValue().value.longValue());
        }
        else if (eeaO.getParPermTransMode() != null)
        {
            pGetParameterOperation.setPermittedTransmissionMode(FSP_PermittedTransmissionMode
                    .getFSPPermittedTransmissionModeByCode((int) eeaO.getParPermTransMode().getParameterValue().value.intValue()));
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
        else if (eeaO.getParReturnTimeout() != null)
        {
            pGetParameterOperation.setReturnTimeoutPeriod(eeaO.getParReturnTimeout().getParameterValue().value.longValue());
        }
        else if (eeaO.getParSegmHeader() != null)
        {
            pGetParameterOperation.setSegmentHeaderPresent(SLE_YesNo
                    .getYesNoByCode((int) eeaO.getParSegmHeader().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParTimeoutType() != null)
        {
            pGetParameterOperation.setTimeoutType(FSP_TimeoutType
                    .getFSPTimeoutTypeByCode((int) eeaO.getParTimeoutType().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParTimerInitial() != null)
        {
            pGetParameterOperation.setTimerInitial(eeaO.getParTimerInitial().getParameterValue().value.longValue());
        }
        else if (eeaO.getParTransmissLimit() != null)
        {
            pGetParameterOperation.setTransmissionLimit(eeaO.getParTransmissLimit().getParameterValue().value.longValue());
        }
        else if (eeaO.getParTrFrSeqNumber() != null)
        {
            pGetParameterOperation.setTransmitterFrameSequenceNumber(eeaO.getParTrFrSeqNumber().getParameterValue().value.longValue());
        }
        else if (eeaO.getParVcMuxControl() != null)
        {
            if (eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsFifo() != null)
            {
                pGetParameterOperation.setVcPriorityList(null);
                pGetParameterOperation.setVcPollingVector(null);
            }
            else if (eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsPriority() != null)
            {
                int size = eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsPriority().getAbsolutePriority().size();
                FSP_AbsolutePriority[] plist = new FSP_AbsolutePriority[size];
                int count = 0;
                for (AbsolutePriority i : eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsPriority().getAbsolutePriority())
                {
                    FSP_AbsolutePriority ap = new FSP_AbsolutePriority();
                    ap.setMapOrVc((int) i.getVcOrMapId().value.intValue());
                    ap.setPriority((int) i.getPriority().value.intValue());
                    plist[count++] = ap;
                }
                pGetParameterOperation.putVcPriorityList(plist);
                pGetParameterOperation.setVcPollingVector(null);
            }
            else if (eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsVector() != null)
            {
                int size = eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsVector().getVcOrMapId().size();
                long[] plist = new long[size];
                int count = 0;
                for (VcOrMapId i : eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsVector().getVcOrMapId())
                {
                    plist[count++] = i.value.longValue();
                }
                pGetParameterOperation.putVcPollingVector(plist);
                pGetParameterOperation.setVcPriorityList(null);
            }
        }
        else if (eeaO.getParVcMuxScheme() != null)
        {
            pGetParameterOperation.setVcMuxScheme(FSP_MuxScheme
                    .getFSP_MuxSchemeByCode((int) eeaO.getParVcMuxScheme().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParVirtualChannel() != null)
        {
            pGetParameterOperation.setVirtualChannel(eeaO.getParVirtualChannel().getParameterValue().value.longValue());
        }
        /** add support for the six new parameters SLES V5 **/
        else if (eeaO.getParClcwGlobalVcId() != null)
        {
        	pGetParameterOperation.setClcwGlobalVcid(decodeClcwGlobalVcid(eeaO.getParClcwGlobalVcId().getParameterValue()));
        }
        else if (eeaO.getParClcwPhysicalChannel() != null)
        {
        	if(eeaO.getParClcwPhysicalChannel().getParameterValue().getConfigured() != null)
        	{
        		pGetParameterOperation.setClcwPhysicalChannel(new FSP_ClcwPhysicalChannel(
	                new String(eeaO.getParClcwPhysicalChannel().getParameterValue().getConfigured().value)));
        	}
        	else if(eeaO.getParClcwPhysicalChannel().getParameterValue().getNotConfigured() != null)
        	{
        		pGetParameterOperation.setClcwPhysicalChannel(new FSP_ClcwPhysicalChannel(null));
        	}
        }
        else if (eeaO.getParCopCntrFramesRepetition() != null)
        {
        	pGetParameterOperation.setCopCntrFramesRepetition(eeaO.getParCopCntrFramesRepetition().getParameterValue().value.longValue());
        }
        else if (eeaO.getParMinReportingCycle() != null)
        {
        	pGetParameterOperation.setMinReportingCycle(eeaO.getParMinReportingCycle().getParameterValue().longValue());
        }
        else if (eeaO.getParSequCntrFramesRepetition() != null)
        {
        	pGetParameterOperation.setSeqCntrFramesRepetition(eeaO.getParSequCntrFramesRepetition().getParameterValue().longValue());
        }
        else if (eeaO.getParThrowEventOperation() != null)
        {
        	pGetParameterOperation.setThrowEventOperation(SLE_YesNo.getYesNoByCode(
        			eeaO.getParThrowEventOperation().getParameterValue().intValue()));
        }
    }

    
    
    /**
     * Fills the parameter of the Fsp GetParameter return operation from the
     * object.
     */
    private void decodeParameterV2to4(FspGetParameterV2to4 eeaO, IFSP_GetParameter pGetParameterOperation)
    {
        if (eeaO.getParBlockingTimeout() != null)
        {
            if (eeaO.getParBlockingTimeout().getParameterValue().getBlockingOn() != null)
            {
                pGetParameterOperation.setBlockingTimeout(eeaO.getParBlockingTimeout().getParameterValue().getBlockingOn().value.longValue());
            }
            else
            {
                pGetParameterOperation.setBlockingTimeout(0);
            }
        }
        else if (eeaO.getParBlockingUsage() != null)
        {
            pGetParameterOperation.setBlockingUsage(FSP_BlockingUsage
                    .getFSP_BlockingUsageByCode((int) eeaO.getParBlockingUsage().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParApidList() != null)
        {
        	/** Handle ApidListV1to4 **/
            int size = eeaO.getParApidList().getParameterValue().getApid().size();
            long[] plist = new long[size];
            int count = 0;
            for (Apid i : eeaO.getParApidList().getParameterValue().getApid())
            {
                plist[count++] = i.value.intValue();
            }
            pGetParameterOperation.putApIdList(plist);
        }
        else if (eeaO.getParDeliveryMode() != null)
        {
            SLE_DeliveryMode delMode = SLE_DeliveryMode
                    .getDelModeByCode(eeaO.getParDeliveryMode().getParameterValue().value.intValue());
            if (delMode == SLE_DeliveryMode.sleDM_fwdOnline)
            {
                pGetParameterOperation.setDeliveryMode();
            }
        }
        else if (eeaO.getParDirectiveInvoc() != null)
        {
            pGetParameterOperation.setDirectiveInvocationEnabled(SLE_YesNo
                    .getYesNoByCode(eeaO.getParDirectiveInvoc().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParDirInvocOnl() != null)
        {
            pGetParameterOperation.setDirectiveInvocationOnline(SLE_YesNo
                    .getYesNoByCode( eeaO.getParDirInvocOnl().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParBitLockRequired() != null)
        {
            pGetParameterOperation.setBitLockRequired(SLE_YesNo
                    .getYesNoByCode(eeaO.getParBitLockRequired().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParRfAvailableRequired() != null)
        {
            pGetParameterOperation.setRfAvailableRequired(SLE_YesNo
                    .getYesNoByCode(eeaO.getParRfAvailableRequired().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParExpectDirectiveId() != null)
        {
            pGetParameterOperation.setExpectedDirectiveId(eeaO.getParExpectDirectiveId().getParameterValue().value.longValue());
        }
        else if (eeaO.getParExpectEventInvId() != null)
        {
            pGetParameterOperation.setExpectedEventInvocationId(eeaO.getParExpectEventInvId().getParameterValue().value.longValue());
        }
        else if (eeaO.getParExpectSlduId() != null)
        {
            pGetParameterOperation.setExpectedSlduId(eeaO.getParExpectSlduId().getParameterValue().value.longValue());
        }
        else if (eeaO.getParFopSlidWindow() != null)
        {
            pGetParameterOperation.setFopSlidingWindow(eeaO.getParFopSlidWindow().getParameterValue().value.longValue());
        }
        else if (eeaO.getParFopState() != null)
        {
            pGetParameterOperation
                    .setFopState(FSP_FopState.getFSPFopStateByCode(eeaO.getParFopState().getFopState().value.intValue()));
        }
        else if (eeaO.getParMapList() != null)
        {
            if (eeaO.getParMapList().getMapList().getMapsUsed() != null)
            {
                int size = eeaO.getParMapList().getMapList().getMapsUsed().getMapId().size();
                long[] plist = new long[size];
                int count = 0;
                for (MapId i : eeaO.getParMapList().getMapList().getMapsUsed().getMapId())
                {
                    plist[count++] = i.value.longValue();
                }
                pGetParameterOperation.setMapList(plist);
            }
            else
            {
                pGetParameterOperation.setMapList(null);
            }
        }
        else if (eeaO.getParMapMuxControl() != null)
        {
            if (eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsFifo() != null)
            {
                pGetParameterOperation.setMapPriorityList(null);
                pGetParameterOperation.setMapPollingVector(null);
            }
            else if (eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsPriority() != null)
            {
                int size = eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsPriority().getAbsolutePriority().size();
                FSP_AbsolutePriority[] plist = new FSP_AbsolutePriority[size];
                int count = 0;
                for (AbsolutePriority i : eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsPriority().getAbsolutePriority())
                {
                    FSP_AbsolutePriority ap = new FSP_AbsolutePriority();
                    ap.setMapOrVc(i.getVcOrMapId().value.intValue());
                    ap.setPriority(i.getPriority().value.intValue());
                    plist[count++] = ap;
                }
                pGetParameterOperation.putMapPriorityList(plist);
                pGetParameterOperation.setMapPollingVector(null);
            }
            else if (eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsVector() != null)
            {
                int size = eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsVector().getVcOrMapId().size();
                long[] plist = new long[size];
                int count = 0;
                for (VcOrMapId i : eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsVector().getVcOrMapId())
                {
                    plist[count++] = i.value.longValue();
                }
                pGetParameterOperation.putMapPollingVector(plist);
                pGetParameterOperation.setMapPollingVector(null);
            }
        }
        else if (eeaO.getParMapMuxScheme() != null)
        {
            pGetParameterOperation.setMapMuxScheme(FSP_MuxScheme
                    .getFSP_MuxSchemeByCode(eeaO.getParMapMuxScheme().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParMaxFrameLength() != null)
        {
            pGetParameterOperation.setMaxFrameLength(eeaO.getParMaxFrameLength().getParameterValue().value.longValue());
        }
        else if (eeaO.getParMaxPacketLength() != null)
        {
            pGetParameterOperation.setMaxPacketLength(eeaO.getParMaxPacketLength().getParameterValue().value.longValue());
        }
        else if (eeaO.getParPermTransMode() != null)
        {
            pGetParameterOperation.setPermittedTransmissionMode(FSP_PermittedTransmissionMode
                    .getFSPPermittedTransmissionModeByCode((int) eeaO.getParPermTransMode().getParameterValue().value.intValue()));
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
        else if (eeaO.getParReturnTimeout() != null)
        {
            pGetParameterOperation.setReturnTimeoutPeriod(eeaO.getParReturnTimeout().getParameterValue().value.longValue());
        }
        else if (eeaO.getParSegmHeader() != null)
        {
            pGetParameterOperation.setSegmentHeaderPresent(SLE_YesNo
                    .getYesNoByCode((int) eeaO.getParSegmHeader().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParTimeoutType() != null)
        {
            pGetParameterOperation.setTimeoutType(FSP_TimeoutType
                    .getFSPTimeoutTypeByCode((int) eeaO.getParTimeoutType().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParTimerInitial() != null)
        {
            pGetParameterOperation.setTimerInitial(eeaO.getParTimerInitial().getParameterValue().value.longValue());
        }
        else if (eeaO.getParTransmissLimit() != null)
        {
            pGetParameterOperation.setTransmissionLimit(eeaO.getParTransmissLimit().getParameterValue().value.longValue());
        }
        else if (eeaO.getParTrFrSeqNumber() != null)
        {
            pGetParameterOperation.setTransmitterFrameSequenceNumber(eeaO.getParTrFrSeqNumber().getParameterValue().value.longValue());
        }
        else if (eeaO.getParVcMuxControl() != null)
        {
            if (eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsFifo() != null)
            {
                pGetParameterOperation.setVcPriorityList(null);
                pGetParameterOperation.setVcPollingVector(null);
            }
            else if (eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsPriority() != null)
            {
                int size = eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsPriority().getAbsolutePriority().size();
                FSP_AbsolutePriority[] plist = new FSP_AbsolutePriority[size];
                int count = 0;
                for (AbsolutePriority i : eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsPriority().getAbsolutePriority())
                {
                    FSP_AbsolutePriority ap = new FSP_AbsolutePriority();
                    ap.setMapOrVc((int) i.getVcOrMapId().value.intValue());
                    ap.setPriority((int) i.getPriority().value.intValue());
                    plist[count++] = ap;
                }
                pGetParameterOperation.putVcPriorityList(plist);
                pGetParameterOperation.setVcPollingVector(null);
            }
            else if (eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsVector() != null)
            {
                int size = eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsVector().getVcOrMapId().size();
                long[] plist = new long[size];
                int count = 0;
                for (VcOrMapId i : eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsVector().getVcOrMapId())
                {
                    plist[count++] = i.value.longValue();
                }
                pGetParameterOperation.putVcPollingVector(plist);
                pGetParameterOperation.setVcPriorityList(null);
            }
        }
        else if (eeaO.getParVcMuxScheme() != null)
        {
            pGetParameterOperation.setVcMuxScheme(FSP_MuxScheme
                    .getFSP_MuxSchemeByCode((int) eeaO.getParVcMuxScheme().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParVirtualChannel() != null)
        {
            pGetParameterOperation.setVirtualChannel(eeaO.getParVirtualChannel().getParameterValue().value.longValue());
        }
    }    
    
    
    private void encodePacketIdentificationList(IFSP_AsyncNotify pAsyncNotifyOperation, PacketIdentificationList eeaO)
    {
        long[] pIdlist = pAsyncNotifyOperation.getPacketIdentificationList();

        if (pIdlist != null)
        {
            for (long l : pIdlist)
            {
                eeaO.getPacketIdentification().add(new PacketIdentification(l));
            }
        }
    }

    private void decodePacketIdentificationList(PacketIdentificationList eeaO, IFSP_AsyncNotify pAsyncNotifyOperation)
    {
    	decodePacketIdentificationList(eeaO.getPacketIdentification(), pAsyncNotifyOperation);
    }
    
    private void decodePacketIdentificationList(List<PacketIdentification> eeaO, IFSP_AsyncNotify pAsyncNotifyOperation)
    {
        long[] list = new long[eeaO.size()];
        int k = 0;
        for (PacketIdentification i : eeaO)
        {
            list[k] = i.value.longValue();
            k++;
        }
        pAsyncNotifyOperation.putPacketIdentificationList(list);
    }    
    
    /**
     * Decodes the frameSequenceNumber for packet-radiated async-notification types only.
     * @since SLES V5
     * @param frameSequenceNumber
     * @param pAsyncNotifyOperation
     */
    private void decodeFrameSequenceNumber(BerInteger frameSequenceNumber, IFSP_AsyncNotify pAsyncNotifyOperation)
    {
    	pAsyncNotifyOperation.setFrameSequenceNumber(frameSequenceNumber.longValue());
    }

    /**
     * Fills the object used for the encoding of Fsp GetParameter return
     * operation for version 1. S_OK The FSP GetParameter operation has been
     * encoded. E_FAIL Unable to encode the FSP GetParameter operation.
     * 
     * @throws SleApiException
     * @throws IOException 
     */
    private void encodeGetParameterReturnOpV1(IFSP_GetParameter pGetParameterOperation, FspGetParameterReturnV1Pdu eeaFspO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pGetParameterOperation.getPerformerCredentials();
        eeaFspO.setPerformerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaFspO.setInvokeId(new InvokeId(pGetParameterOperation.getInvokeId()));

        // the result
        if (pGetParameterOperation.getResult() == SLE_Result.sleRES_positive)
        {
            FspGetParameterV1 fspGetParam = new FspGetParameterV1();
            encodeParameterV1(pGetParameterOperation, fspGetParam);
            FspGetParameterReturnV1.Result posR = new FspGetParameterReturnV1.Result();
            posR.setPositiveResult(fspGetParam);
            eeaFspO.setResult(posR);
        }
        else
        {
            FspGetParameterReturnV1.Result negResult = new FspGetParameterReturnV1.Result();

            if (pGetParameterOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                DiagnosticFspGet repSpecific = new DiagnosticFspGet();

                switch (pGetParameterOperation.getGetParameterDiagnostic())
                {
                case fspGP_unknownParameter:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_GetParameterDiagnostic.fspGP_unknownParameter.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(FSP_GetParameterDiagnostic.fspGP_invalid.getCode()));
                    break;
                }
                }

                negResult.setNegativeResult(repSpecific);
            }
            else
            {
                // common diagnostic
                DiagnosticFspGet repCommon = new DiagnosticFspGet();
                repCommon.setCommon(new Diagnostics(pGetParameterOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }

            eeaFspO.setResult(negResult);
        }
    }

    /**
     * Fills the FSP GET-PARAMETER return operation from the object for version
     * 1. S_OK The FSP GetParameter operation has been decoded. E_FAIL Unable to
     * decode the FSP GetParameter operation.
     * 
     * @throws SleApiException
     */
    private IFSP_GetParameter decodeGetParameterReturnOp(FspGetParameterReturnV1Pdu eeaFspO) throws SleApiException
    {
        IFSP_GetParameter pGetParameterOperation = null;
        ISLE_Operation pOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_getParameter;

        pOperation = this.pduTranslator.getReturnOp(eeaFspO.getInvokeId(), opType);
        if (pOperation != null)
        {
            pGetParameterOperation = pOperation.queryInterface(IFSP_GetParameter.class);
            if (pGetParameterOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaFspO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pGetParameterOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pGetParameterOperation.setInvokeId((int) eeaFspO.getInvokeId().value.intValue());

                // the result
                if (eeaFspO.getResult().getPositiveResult() != null)
                {
                    decodeParameter(eeaFspO.getResult().getPositiveResult(), pGetParameterOperation);
                    pGetParameterOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eeaFspO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        int specValue = (int) eeaFspO.getResult().getNegativeResult().getSpecific().value.intValue();
                        pGetParameterOperation.setGetParameterDiagnostic(FSP_GetParameterDiagnostic
                                .getGetParamDiagByCode(specValue));
                    }
                    else
                    {
                        // common diagnostic
                        int commValue = (int) eeaFspO.getResult().getNegativeResult().getCommon().value.intValue();
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
     * Fills the FSP Parameter of the Asn1 object for FSP version 1.
     * @throws IOException 
     */
    private void encodeParameterV1(IFSP_GetParameter pGetParameterOperation, FspGetParameterV1 eeaO) 
    {
        switch (pGetParameterOperation.getReturnedParameter())
        {
        case fspPN_blockingTimeoutPeriod:
        {
            FspGetParameterV1.ParBlockingTimeout parBlockTOPeriod = new FspGetParameterV1.ParBlockingTimeout();

            parBlockTOPeriod.setParameterName(new ParameterName(FSP_ParameterName.fspPN_blockingTimeoutPeriod.getCode()));
            if (pGetParameterOperation.getBlockingTimeout() == 0)
            {
            	FspGetParameterV1.ParBlockingTimeout.ParameterValue pv = new FspGetParameterV1.ParBlockingTimeout.ParameterValue();
            	pv.setBlockingOff(new BerNull());
                parBlockTOPeriod.setParameterValue(pv);
            }
            else
            {
            	BlockingTimeoutPeriod btp = new BlockingTimeoutPeriod(pGetParameterOperation.getBlockingTimeout());
            	FspGetParameterV1.ParBlockingTimeout.ParameterValue pv = new FspGetParameterV1.ParBlockingTimeout.ParameterValue();
            	pv.setBlockingOn(btp);
                parBlockTOPeriod.setParameterValue(pv);
            }
            eeaO.setParBlockingTimeout(parBlockTOPeriod);
            break;
        }
        case fspPN_blockingUsage:
        {
            FspGetParameterV1.ParBlockingUsage parBlockUsage = new FspGetParameterV1.ParBlockingUsage();
            parBlockUsage.setParameterName(new ParameterName(FSP_ParameterName.fspPN_blockingUsage.getCode()));
            switch (pGetParameterOperation.getBlockingUsage())
            {
            case fspAU_permitted:
            {
                parBlockUsage.setParameterValue(new BlockingUsage(FSP_BlockingUsage.fspAU_permitted.getCode()));
                break;
            }
            case fspAU_notPermitted:
            {
                parBlockUsage.setParameterValue(new BlockingUsage(FSP_BlockingUsage.fspAU_notPermitted.getCode()));
            }
            default:
            {
                parBlockUsage.setParameterValue(new BlockingUsage(FSP_BlockingUsage.fspAU_invalid.getCode()));
            }
            }
            eeaO.setParBlockingUsage(parBlockUsage);
            break;
        }
        case fspPN_apidList:
        {
            FspGetParameterV1.ParApidList parApidList = new FspGetParameterV1.ParApidList();
            parApidList.setParameterName(new ParameterName(FSP_ParameterName.fspPN_apidList.getCode()));
            parApidList.setParameterValue(new ApidListV1to4());
            long[] list = pGetParameterOperation.getApIdList();
            for (long i : list)
            {
                Apid e = new Apid(i);
                parApidList.getParameterValue().getApid().add(e);
            }
            
            eeaO.setParApidList(parApidList);
            break;
        }
        case fspPN_deliveryMode:
        {
            FspGetParameterV1.ParDeliveryMode parDeliveryMode = new FspGetParameterV1.ParDeliveryMode();
            parDeliveryMode.setParameterName(new ParameterName(FSP_ParameterName.fspPN_deliveryMode.getCode()));
            parDeliveryMode.setParameterValue(new FspDeliveryMode(pGetParameterOperation.getDeliveryMode().getCode()));
            eeaO.setParDeliveryMode(parDeliveryMode);
            break;
        }
        case fspPN_directiveInvocationEnabled:
        {
            FspGetParameterV1.ParDirectiveInvoc parDirInv = new FspGetParameterV1.ParDirectiveInvoc();
            parDirInv.setParameterName(new ParameterName(FSP_ParameterName.fspPN_directiveInvocationEnabled.getCode()));
            switch (pGetParameterOperation.getDirectiveInvocationEnabled())
            {
            case sleYN_Yes:
            {
                parDirInv.setParameterValue(new BerInteger(SLE_YesNo.sleYN_Yes.getCode()));
                break;
            }
            case sleYN_No:
            {
                parDirInv.setParameterValue(new BerInteger(SLE_YesNo.sleYN_No.getCode()));
                break;
            }
            default:
            {
                parDirInv.setParameterValue(new BerInteger(SLE_YesNo.sleYN_invalid.getCode()));
                break;
            }
            }
            eeaO.setParDirectiveInvoc(parDirInv);
            break;
        }
        case fspPN_directiveInvocationOnline:
        {
            FspGetParameterV1.ParDirInvocOnl parDirInv = new FspGetParameterV1.ParDirInvocOnl();
            parDirInv.setParameterName(new ParameterName(FSP_ParameterName.fspPN_directiveInvocationOnline.getCode()));
            switch (pGetParameterOperation.getDirectiveInvocationOnline())
            {
            case sleYN_Yes:
            {
                parDirInv.setParameterValue(new BerInteger(SLE_YesNo.sleYN_Yes.getCode()));
                break;
            }
            case sleYN_No:
            {
                parDirInv.setParameterValue(new BerInteger(SLE_YesNo.sleYN_No.getCode()));
                break;
            }
            default:
            {
                parDirInv.setParameterValue(new BerInteger(SLE_YesNo.sleYN_invalid.getCode()));
                break;
            }
            }
            eeaO.setParDirInvocOnl(parDirInv);
            break;
        }
        case fspPN_expectedDirectiveId:
        {
            FspGetParameterV1.ParExpectDirectiveId parExpDirId = new FspGetParameterV1.ParExpectDirectiveId();
            parExpDirId.setParameterName(new ParameterName(FSP_ParameterName.fspPN_expectedDirectiveId.getCode()));
            parExpDirId.setParameterValue(new IntUnsignedLong(pGetParameterOperation.getExpectedDirectiveId()));
            eeaO.setParExpectDirectiveId(parExpDirId);
            break;
        }
        case fspPN_expectedEventInvocationId:
        {
            FspGetParameterV1.ParExpectEventInvId parExpEvInvId = new FspGetParameterV1.ParExpectEventInvId();
            parExpEvInvId.setParameterName(new ParameterName(FSP_ParameterName.fspPN_expectedEventInvocationId.getCode()));
            parExpEvInvId.setParameterValue(new IntUnsignedLong(pGetParameterOperation.getExpectedEventInvocationId()));
            eeaO.setParExpectEventInvId(parExpEvInvId);
            break;
        }
        case fspPN_expectedSlduIdentification:
        {
            FspGetParameterV1.ParExpectSlduId parExpSlduId = new FspGetParameterV1.ParExpectSlduId();
            parExpSlduId.setParameterName(new ParameterName(FSP_ParameterName.fspPN_expectedSlduIdentification.getCode()));
            parExpSlduId.setParameterValue(new PacketIdentification(pGetParameterOperation.getExpectedSlduId()));
            eeaO.setParExpectSlduId(parExpSlduId);
            break;
        }
        case fspPN_fopSlidingWindow:
        {
            FspGetParameterV1.ParFopSlidWindow parFopSlidWind = new FspGetParameterV1.ParFopSlidWindow();
            parFopSlidWind.setParameterName(new ParameterName(FSP_ParameterName.fspPN_fopSlidingWindow.getCode()));
            parFopSlidWind.setParameterValue(new BerInteger(pGetParameterOperation.getFopSlidingWindow()));
            eeaO.setParFopSlidWindow(parFopSlidWind);
            break;
        }
        case fspPN_fopState:
        {
            FspGetParameterV1.ParFopState parFopState = new FspGetParameterV1.ParFopState();
            parFopState.setParameterName(new ParameterName(FSP_ParameterName.fspPN_fopState.getCode()));
            switch (pGetParameterOperation.getFopState())
            {
            case fspFS_active:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_active.getCode()));
                break;
            }
            case fspFS_retransmitWithoutWait:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_retransmitWithoutWait.getCode()));
                break;
            }
            case fspFS_retransmitWithWait:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_retransmitWithWait.getCode()));
                break;
            }
            case fspFS_initialisingWithoutBCFrame:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_initialisingWithoutBCFrame.getCode()));
                break;
            }
            case fspFS_initialisingWithBCFrame:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_initialisingWithBCFrame.getCode()));
                break;
            }
            case fspFS_initial:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_initial.getCode()));
                break;
            }
            default:
            {
                parFopState.setFopState(new BerInteger(FSP_FopState.fspFS_invalid.getCode()));
                break;
            }
            }
            eeaO.setParFopState(parFopState);
            break;
        }
        case fspPN_mapList:
        {
            FspGetParameterV1.ParMapList parMapList = new FspGetParameterV1.ParMapList();
            parMapList.setParameterName(new ParameterName(FSP_ParameterName.fspPN_mapList.getCode()));
            long[] list = pGetParameterOperation.getMapList();
            if (list.length != 0)
            {
                MapList mapList = new MapList();
                mapList.setMapsUsed(new MapsUsed());
                for (long i : list)
                {
                    MapId e = new MapId(i);
                    mapList.getMapsUsed().getMapId().add(e);
                }
                parMapList.setMapList(mapList);
            }
            else
            {
                MapList mapList = new MapList();
                parMapList.setMapList(mapList);
            }
            eeaO.setParMapList(parMapList);
            break;
        }
        case fspPN_mapMuxControl:
        {
            FspGetParameterV1.ParMapMuxControl parMapMuxControl = new FspGetParameterV1.ParMapMuxControl();
            parMapMuxControl.setParameterName(new ParameterName(FSP_ParameterName.fspPN_mapMuxControl.getCode()));
            FSP_AbsolutePriority[] priorityList = pGetParameterOperation.getMapPriorityList();
            long[] pollingVector = pGetParameterOperation.getMapPollingVector();
            if (priorityList != null)
            {
                MuxControl muxControl = new MuxControl();
                MuxSchemeIsPriority elem = new MuxSchemeIsPriority();
                for (FSP_AbsolutePriority i : priorityList)
                {
                    AbsolutePriority ap = new AbsolutePriority();
                    ap.setVcOrMapId(new VcOrMapId(i.getMapOrVc()));
                    ap.setPriority(new Priority(i.getPriority()));
                    elem.getAbsolutePriority().add(ap);
                }
                muxControl.setMuxSchemeIsPriority(elem);
                parMapMuxControl.setParameterValue(muxControl);
            }
            else if (pollingVector != null)
            {
                MuxControl muxControl = new MuxControl();
                MuxSchemeIsVector elem = new MuxSchemeIsVector();
                for (long i : pollingVector)
                {
                    VcOrMapId e = new VcOrMapId(i);
                    elem.getVcOrMapId().add(e);
                }
                muxControl.setMuxSchemeIsVector(elem);
                parMapMuxControl.setParameterValue(muxControl);
            }
            else
            {
            	MuxControl muxControl = new MuxControl();
            	muxControl.setMuxSchemeIsFifo(new BerNull());
                parMapMuxControl.setParameterValue(muxControl);
            }
            eeaO.setParMapMuxControl(parMapMuxControl);
            break;
        }
        case fspPN_mapMuxScheme:
        {
            FspGetParameterV1.ParMapMuxScheme parMapMuxScheme = new FspGetParameterV1.ParMapMuxScheme();
            parMapMuxScheme.setParameterName(new ParameterName(FSP_ParameterName.fspPN_mapMuxScheme.getCode()));
            MapMuxSchemeV1 mmsv1 = new MapMuxSchemeV1();
            switch (pGetParameterOperation.getMapMuxScheme())
            {
            case fspMS_fifo:
            {
                mmsv1.setMapsUsed(new MuxScheme(FSP_MuxScheme.fspMS_fifo.getCode()));
                parMapMuxScheme.setParameterValue(mmsv1);
                break;
            }
            case fspMS_absolutePriority:
            {
            	mmsv1.setMapsUsed(new MuxScheme(FSP_MuxScheme.fspMS_absolutePriority.getCode()));
                parMapMuxScheme.setParameterValue(mmsv1);
                break;
            }
            case fspMS_pollingVector:
            {
            	mmsv1.setMapsUsed(new MuxScheme(FSP_MuxScheme.fspMS_pollingVector.getCode()));
                parMapMuxScheme.setParameterValue(mmsv1);
                break;
            }
            default:
            {
            	mmsv1.setMapsUsed(new MuxScheme(FSP_MuxScheme.fspMS_invalid.getCode()));
                parMapMuxScheme.setParameterValue(mmsv1);
                break;
            }
            }
            eeaO.setParMapMuxScheme(parMapMuxScheme);
            break;
        }
        case fspPN_maximumFrameLength:
        {
            FspGetParameterV1.ParMaxFrameLength parmaxFrameLength = new FspGetParameterV1.ParMaxFrameLength();
            parmaxFrameLength.setParameterName(new ParameterName(FSP_ParameterName.fspPN_maximumFrameLength.getCode()));
            parmaxFrameLength.setParameterValue(new BerInteger(pGetParameterOperation.getMaxFrameLength()));
            eeaO.setParMaxFrameLength(parmaxFrameLength);
            break;
        }
        case fspPN_maximumPacketLength:
        {
            FspGetParameterV1.ParMaxPacketLength parmaxPacketLength = new FspGetParameterV1.ParMaxPacketLength();
            parmaxPacketLength.setParameterName(new ParameterName(FSP_ParameterName.fspPN_maximumPacketLength.getCode()));
            parmaxPacketLength.setParameterValue(new BerInteger(pGetParameterOperation.getMaxPacketLength()));
            eeaO.setParMaxPacketLength(parmaxPacketLength);
            break;
        }
        case fspPN_permittedTransmissionMode:
        {
            FspGetParameterV1.ParPermTransMode parPermTransMode = new FspGetParameterV1.ParPermTransMode();
            parPermTransMode.setParameterName(new ParameterName(FSP_ParameterName.fspPN_permittedTransmissionMode.getCode()));
            switch (pGetParameterOperation.getPermittedTransmissionMode())
            {
            case fspPTM_sequenceControlled:
            {
                parPermTransMode.setParameterValue(new PermittedTransmissionMode(FSP_PermittedTransmissionMode.fspPTM_sequenceControlled
                        .getCode()));
                break;
            }
            case fspPTM_expedited:
            {
                parPermTransMode.setParameterValue(new PermittedTransmissionMode(FSP_PermittedTransmissionMode.fspPTM_expedited
                        .getCode()));
                break;
            }
            case fspPTM_any:
            {
                parPermTransMode.setParameterValue(new PermittedTransmissionMode(FSP_PermittedTransmissionMode.fspPTM_any
                        .getCode()));
                break;
            }
            default:
            {
                parPermTransMode.setParameterValue(new PermittedTransmissionMode());
                break;
            }
            }
            eeaO.setParPermTransMode(parPermTransMode);
            break;
        }
        case fspPN_reportingCycle:
        {
            FspGetParameterV1.ParReportingCycle parRepCycle = new FspGetParameterV1.ParReportingCycle();
            parRepCycle.setParameterName(new ParameterName(FSP_ParameterName.fspPN_reportingCycle.getCode()));
            long reportingCycle = pGetParameterOperation.getReportingCycle();
            if (reportingCycle == 0)
            {
            	CurrentReportingCycle nullCycle = new CurrentReportingCycle();
            	nullCycle.setPeriodicReportingOff(new BerNull());
                parRepCycle.setParameterValue(nullCycle);
            }
            else
            {
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOn(new ReportingCycle(reportingCycle));
                parRepCycle.setParameterValue(crc);
            }
            eeaO.setParReportingCycle(parRepCycle);
            break;
        }
        case fspPN_returnTimeoutPeriod:
        {
            FspGetParameterV1.ParReturnTimeout parRtnTo = new FspGetParameterV1.ParReturnTimeout();
            parRtnTo.setParameterName(new ParameterName(FSP_ParameterName.fspPN_returnTimeoutPeriod.getCode()));
            parRtnTo.setParameterValue(new TimeoutPeriod(pGetParameterOperation.getReturnTimeoutPeriod()));
            eeaO.setParReturnTimeout(parRtnTo);
            break;
        }
        case fspPN_segmentHeader:
        {
            FspGetParameterV1.ParSegmHeader parSegmHeader = new FspGetParameterV1.ParSegmHeader();
            parSegmHeader.setParameterName(new ParameterName(FSP_ParameterName.fspPN_segmentHeader.getCode()));
            switch (pGetParameterOperation.getSegmentHeaderPresent())
            {
            case sleYN_Yes:
            {
                parSegmHeader.setParameterValue(new BerInteger(SLE_YesNo.sleYN_Yes.getCode()));
                break;
            }
            case sleYN_No:
            {
                parSegmHeader.setParameterValue(new BerInteger(SLE_YesNo.sleYN_No.getCode()));
                break;
            }
            default:
            {
                parSegmHeader.setParameterValue(new BerInteger(-1));
                break;
            }
            }
            eeaO.setParSegmHeader(parSegmHeader);
            break;
        }
        case fspPN_timeoutType:
        {
            FspGetParameterV1.ParTimeoutType parTOTime = new FspGetParameterV1.ParTimeoutType();
            parTOTime.setParameterName(new ParameterName(FSP_ParameterName.fspPN_timeoutType.getCode()));
            switch (pGetParameterOperation.getTimeoutType())
            {
            case fspTT_generateAlert:
            {
                parTOTime.setParameterValue(new BerInteger(FSP_TimeoutType.fspTT_generateAlert.getCode()));
                break;
            }
            case fspTT_suspendAD:
            {
                parTOTime.setParameterValue(new BerInteger(FSP_TimeoutType.fspTT_suspendAD.getCode()));
                break;
            }
            default:
            {
                parTOTime.setParameterValue(new BerInteger(-1));
                break;
            }
            }
            eeaO.setParTimeoutType(parTOTime);
            break;
        }
        case fspPN_timerInitial:
        {
            FspGetParameterV1.ParTimerInitial parTimerInit = new FspGetParameterV1.ParTimerInitial();
            parTimerInit.setParameterName(new ParameterName(FSP_ParameterName.fspPN_timerInitial.getCode()));
            parTimerInit.setParameterValue(new IntPosLong(pGetParameterOperation.getTimerInitial()));
            eeaO.setParTimerInitial(parTimerInit);
            break;
        }
        case fspPN_transmissionLimit:
        {
            FspGetParameterV1.ParTransmissLimit parTransmLimit = new FspGetParameterV1.ParTransmissLimit();
            parTransmLimit.setParameterName(new ParameterName(FSP_ParameterName.fspPN_transmissionLimit.getCode()));
            parTransmLimit.setParameterValue(new IntPosLong(pGetParameterOperation.getTransmissionLimit()));
            eeaO.setParTransmissLimit(parTransmLimit);
            break;
        }
        case fspPN_transmitterFrameSequenceNumber:
        {
            FspGetParameterV1.ParTrFrSeqNumber parTransmFrSeqNbr = new FspGetParameterV1.ParTrFrSeqNumber();
            parTransmFrSeqNbr.setParameterName(new ParameterName(FSP_ParameterName.fspPN_transmitterFrameSequenceNumber.getCode()));
            parTransmFrSeqNbr.setParameterValue(new IntPosLong(pGetParameterOperation.getTransmitterFrameSequenceNumber()));
            eeaO.setParTrFrSeqNumber(parTransmFrSeqNbr);
            break;
        }
        case fspPN_vcMuxControl:
        {
            FspGetParameterV1.ParVcMuxControl parVcMuxControl = new FspGetParameterV1.ParVcMuxControl();
            parVcMuxControl.setParameterName(new ParameterName(FSP_ParameterName.fspPN_vcMuxControl.getCode()));
            FSP_AbsolutePriority[] priorityList = pGetParameterOperation.getVcPriorityList();
            long[] pollingVector = pGetParameterOperation.getVcPollingVector();
            if (priorityList.length != 0)
            {
                MuxControl muxControl = new MuxControl();
                MuxSchemeIsPriority elem = new MuxSchemeIsPriority();
                for (FSP_AbsolutePriority i : priorityList)
                {
                    AbsolutePriority ap = new AbsolutePriority();
                    ap.setVcOrMapId(new VcOrMapId(i.getMapOrVc()));
                    ap.setPriority(new Priority(i.getPriority()));
                    elem.getAbsolutePriority().add(ap);
                }
                muxControl.setMuxSchemeIsPriority(elem);
                parVcMuxControl.setParameterValue(muxControl);
            }
            else if (pollingVector.length != 0)
            {
                MuxControl muxControl = new MuxControl();
                MuxSchemeIsVector elem = new MuxSchemeIsVector();
                for (long i : pollingVector)
                {
                    VcOrMapId e = new VcOrMapId(i);
                    elem.getVcOrMapId().add(e);
                }
                muxControl.setMuxSchemeIsVector(elem);
                parVcMuxControl.setParameterValue(muxControl);
            }
            else
            {
                parVcMuxControl.setParameterValue(new MuxControl());
            }
            eeaO.setParVcMuxControl(parVcMuxControl);
            break;
        }
        case fspPN_vcMuxScheme:
        {
            FspGetParameterV1.ParVcMuxScheme parvcMuxScheme = new FspGetParameterV1.ParVcMuxScheme();
            parvcMuxScheme.setParameterName(new ParameterName(FSP_ParameterName.fspPN_vcMuxScheme.getCode()));
            parvcMuxScheme.setParameterValue(new MuxScheme(pGetParameterOperation.getVcMuxScheme().getCode()));
            eeaO.setParVcMuxScheme(parvcMuxScheme);
            break;
        }
        case fspPN_virtualChannel:
        {
            FspGetParameterV1.ParVirtualChannel parVChannel = new FspGetParameterV1.ParVirtualChannel();
            parVChannel.setParameterName(new ParameterName(FSP_ParameterName.fspPN_virtualChannel.getCode()));
            parVChannel.setParameterValue(new VcOrMapId(pGetParameterOperation.getVirtualChannel()));
            eeaO.setParVirtualChannel(parVChannel);
            break;
        }
        case fspPN_invalid:
        {
            break;
        }
        default:
        {
            break;
        }
        }
    }

    /**
     * Fills the parameter of the Fsp GetParameter return operation from the
     * object for FSP version 1.
     */
    private void decodeParameter(FspGetParameterV1 eeaO, IFSP_GetParameter pGetParameterOperation)
    {
        if (eeaO.getParBlockingTimeout() != null)
        {
            if (eeaO.getParBlockingTimeout().getParameterValue().getBlockingOn() != null)
            {
                pGetParameterOperation.setBlockingTimeout(eeaO.getParBlockingTimeout().getParameterValue().getBlockingOn().value.longValue());
            }
            else
            {
                pGetParameterOperation.setBlockingTimeout(0);
            }
        }
        else if (eeaO.getParBlockingUsage() != null)
        {
            pGetParameterOperation.setBlockingUsage(FSP_BlockingUsage
                    .getFSP_BlockingUsageByCode((int) eeaO.getParBlockingUsage().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParApidList() != null)
        {
        	/** @todo make it compatible to V1 - V4 APID version. **/
            int size = eeaO.getParApidList().getParameterValue().getApid().size();
            long[] plist = new long[size];
            int count = 0;
            for (Apid i : eeaO.getParApidList().getParameterValue().getApid())
            {
                plist[count++] = i.value.longValue();
            }
            pGetParameterOperation.putApIdList(plist);
        }
        else if (eeaO.getParDeliveryMode() != null)
        {
            SLE_DeliveryMode delMode = SLE_DeliveryMode
                    .getDelModeByCode((int) eeaO.getParDeliveryMode().getParameterValue().value.intValue());
            if (delMode == SLE_DeliveryMode.sleDM_fwdOnline)
            {
                pGetParameterOperation.setDeliveryMode();
            }
        }
        else if (eeaO.getParDirectiveInvoc() != null)
        {
            pGetParameterOperation.setDirectiveInvocationEnabled(SLE_YesNo
                    .getYesNoByCode((int) eeaO.getParDirectiveInvoc().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParDirInvocOnl() != null)
        {
            pGetParameterOperation.setDirectiveInvocationOnline(SLE_YesNo
                    .getYesNoByCode((int) eeaO.getParDirInvocOnl().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParExpectDirectiveId() != null)
        {
            pGetParameterOperation.setExpectedDirectiveId(eeaO.getParExpectDirectiveId().getParameterValue().value.longValue());
        }
        else if (eeaO.getParExpectEventInvId() != null)
        {
            pGetParameterOperation.setExpectedEventInvocationId(eeaO.getParExpectEventInvId().getParameterValue().value.longValue());
        }
        else if (eeaO.getParExpectSlduId() != null)
        {
            pGetParameterOperation.setExpectedSlduId(eeaO.getParExpectSlduId().getParameterValue().value.longValue());
        }
        else if (eeaO.getParFopSlidWindow() != null)
        {
            pGetParameterOperation.setFopSlidingWindow(eeaO.getParFopSlidWindow().getParameterValue().value.longValue());
        }
        else if (eeaO.getParFopState() != null)
        {
            pGetParameterOperation
                    .setFopState(FSP_FopState.getFSPFopStateByCode((int) eeaO.getParFopState().getFopState().value.intValue()));
        }
        else if (eeaO.getParMapList() != null)
        {
            if (eeaO.getParMapList().getMapList().getMapsUsed() != null)
            {
                int size = eeaO.getParMapList().getMapList().getMapsUsed().getMapId().size();
                long[] plist = new long[size];
                int count = 0;
                for (MapId i : eeaO.getParMapList().getMapList().getMapsUsed().getMapId())
                {
                    plist[count++] = i.value.longValue();
                }
                pGetParameterOperation.setMapList(plist);
            }
            else
            {
                pGetParameterOperation.setMapList(null);
            }
        }
        else if (eeaO.getParMapMuxControl() != null)
        {
            if (eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsFifo() != null)
            {
                pGetParameterOperation.setMapPriorityList(null);
                pGetParameterOperation.setMapPollingVector(null);
            }
            else if (eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsPriority() != null)
            {
                int size = eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsPriority().getAbsolutePriority().size();
                FSP_AbsolutePriority[] plist = new FSP_AbsolutePriority[size];
                int count = 0;
                for (AbsolutePriority i : eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsPriority().getAbsolutePriority())
                {
                    FSP_AbsolutePriority ap = new FSP_AbsolutePriority();
                    ap.setMapOrVc((int) i.getVcOrMapId().value.intValue());
                    ap.setPriority((int) i.getPriority().value.intValue());
                    plist[count++] = ap;
                }
                pGetParameterOperation.putMapPriorityList(plist);
                pGetParameterOperation.setMapPollingVector(null);
            }
            else if (eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsVector() != null)
            {
                int size = eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsVector().getVcOrMapId().size();
                long[] plist = new long[size];
                int count = 0;
                for (VcOrMapId i : eeaO.getParMapMuxControl().getParameterValue().getMuxSchemeIsVector().getVcOrMapId())
                {
                    plist[count++] = i.value.longValue();
                }
                pGetParameterOperation.putMapPollingVector(plist);
                pGetParameterOperation.setMapPollingVector(null);
            }
        }
        else if (eeaO.getParMapMuxScheme() != null)
        {
            if (eeaO.getParMapMuxScheme().getParameterValue().getMapsUsed() != null)
            {
                pGetParameterOperation.setMapMuxScheme(FSP_MuxScheme
                        .getFSP_MuxSchemeByCode((int) eeaO.getParMapMuxScheme().getParameterValue().getMapsUsed().value.intValue()));
            }
            else
            {
                pGetParameterOperation.setMapMuxScheme(FSP_MuxScheme.fspMS_fifo);
            }
        }
        else if (eeaO.getParMaxFrameLength() != null)
        {
            pGetParameterOperation.setMaxFrameLength(eeaO.getParMaxFrameLength().getParameterValue().value.longValue());
        }
        else if (eeaO.getParMaxPacketLength() != null)
        {
            pGetParameterOperation.setMaxPacketLength(eeaO.getParMaxPacketLength().getParameterValue().value.longValue());
        }
        else if (eeaO.getParPermTransMode() != null)
        {
            pGetParameterOperation.setPermittedTransmissionMode(FSP_PermittedTransmissionMode
                    .getFSPPermittedTransmissionModeByCode((int) eeaO.getParPermTransMode().getParameterValue().value.intValue()));
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
        else if (eeaO.getParReturnTimeout() != null)
        {
            pGetParameterOperation.setReturnTimeoutPeriod(eeaO.getParReturnTimeout().getParameterValue().value.longValue());
        }
        else if (eeaO.getParSegmHeader() != null)
        {
            pGetParameterOperation.setSegmentHeaderPresent(SLE_YesNo
                    .getYesNoByCode((int) eeaO.getParSegmHeader().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParTimeoutType() != null)
        {
            pGetParameterOperation.setTimeoutType(FSP_TimeoutType
                    .getFSPTimeoutTypeByCode((int) eeaO.getParTimeoutType().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParTimerInitial() != null)
        {
            pGetParameterOperation.setTimerInitial(eeaO.getParTimerInitial().getParameterValue().value.longValue());
        }
        else if (eeaO.getParTransmissLimit() != null)
        {
            pGetParameterOperation.setTransmissionLimit(eeaO.getParTransmissLimit().getParameterValue().value.longValue());
        }
        else if (eeaO.getParTrFrSeqNumber() != null)
        {
            pGetParameterOperation.setTransmitterFrameSequenceNumber(eeaO.getParTrFrSeqNumber().getParameterValue().value.longValue());
        }
        else if (eeaO.getParVcMuxControl() != null)
        {
            if (eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsFifo() != null)
            {
                pGetParameterOperation.setVcPriorityList(null);
                pGetParameterOperation.setVcPollingVector(null);
            }
            else if (eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsPriority() != null)
            {
                int size = eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsPriority().getAbsolutePriority().size();
                FSP_AbsolutePriority[] plist = new FSP_AbsolutePriority[size];
                int count = 0;
                for (AbsolutePriority i : eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsPriority().getAbsolutePriority())
                {
                    FSP_AbsolutePriority ap = new FSP_AbsolutePriority();
                    ap.setMapOrVc((int) i.getVcOrMapId().value.intValue());
                    ap.setPriority((int) i.getPriority().value.intValue());
                    plist[count++] = ap;
                }
                pGetParameterOperation.putVcPriorityList(plist);
                pGetParameterOperation.setVcPollingVector(null);
            }
            else if (eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsVector() != null)
            {
                int size = eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsVector().getVcOrMapId().size();
                long[] plist = new long[size];
                int count = 0;
                for (VcOrMapId i : eeaO.getParVcMuxControl().getParameterValue().getMuxSchemeIsVector().getVcOrMapId())
                {
                    plist[count++] = i.value.longValue();
                }
                pGetParameterOperation.putVcPollingVector(plist);
                pGetParameterOperation.setVcPriorityList(null);
            }
        }
        else if (eeaO.getParVcMuxScheme() != null)
        {
            pGetParameterOperation.setVcMuxScheme(FSP_MuxScheme
                    .getFSP_MuxSchemeByCode((int) eeaO.getParVcMuxScheme().getParameterValue().value.intValue()));
        }
        else if (eeaO.getParVirtualChannel() != null)
        {
            pGetParameterOperation.setVirtualChannel(eeaO.getParVirtualChannel().getParameterValue().value.longValue());
        }
    }
}
