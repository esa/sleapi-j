/**
 * @(#) EE_APIPX_Asn1RocfTranslator.java
 */

package esa.sle.impl.api.apipx.pxdel;

import isp1.rocf.pdus.RocfGetParameterInvocationPdu;
import isp1.rocf.pdus.RocfGetParameterReturnPdu;
import isp1.rocf.pdus.RocfGetParameterReturnPduV1To4;
import isp1.rocf.pdus.RocfScheduleStatusReportInvocationPdu;
import isp1.rocf.pdus.RocfScheduleStatusReportReturnPdu;
import isp1.rocf.pdus.RocfStartInvocationPdu;
import isp1.rocf.pdus.RocfStartReturnPdu;
import isp1.rocf.pdus.RocfStatusReportInvocationPdu;
import isp1.rocf.pdus.RocfStopInvocationPdu;
import isp1.rocf.pdus.RocfStopReturnPdu;
import isp1.rocf.pdus.RocfTransferBufferPdu;
import isp1.sle.bind.pdus.SleBindInvocationPdu;
import isp1.sle.bind.pdus.SleBindReturnPdu;
import isp1.sle.bind.pdus.SleUnbindInvocationPdu;
import isp1.sle.bind.pdus.SleUnbindReturnPdu;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
import ccsds.sle.api.isrv.irocf.IROCF_GetParameter;
import ccsds.sle.api.isrv.irocf.IROCF_Start;
import ccsds.sle.api.isrv.irocf.IROCF_StatusReport;
import ccsds.sle.api.isrv.irocf.IROCF_SyncNotify;
import ccsds.sle.api.isrv.irocf.IROCF_TransferData;
import ccsds.sle.api.isrv.irocf.types.ROCF_AntennaIdFormat;
import ccsds.sle.api.isrv.irocf.types.ROCF_ChannelType;
import ccsds.sle.api.isrv.irocf.types.ROCF_ControlWordType;
import ccsds.sle.api.isrv.irocf.types.ROCF_DeliveryMode;
import ccsds.sle.api.isrv.irocf.types.ROCF_GetParameterDiagnostic;
import ccsds.sle.api.isrv.irocf.types.ROCF_Gvcid;
import ccsds.sle.api.isrv.irocf.types.ROCF_LockStatus;
import ccsds.sle.api.isrv.irocf.types.ROCF_ParameterName;
import ccsds.sle.api.isrv.irocf.types.ROCF_ProductionStatus;
import ccsds.sle.api.isrv.irocf.types.ROCF_StartDiagnostic;
import ccsds.sle.api.isrv.irocf.types.ROCF_UpdateMode;
import ccsds.sle.transfer.service.common.pdus.ReportingCycle;
import ccsds.sle.transfer.service.common.types.Diagnostics;
import ccsds.sle.transfer.service.common.types.IntPosShort;
import ccsds.sle.transfer.service.common.types.IntUnsignedLong;
import ccsds.sle.transfer.service.common.types.InvokeId;
import ccsds.sle.transfer.service.common.types.ParameterName;
import ccsds.sle.transfer.service.common.types.SpaceLinkDataUnit;
import ccsds.sle.transfer.service.rcf.structures.RcfGetParameter;
import ccsds.sle.transfer.service.rocf.outgoing.pdus.OcfOrNotification;
import ccsds.sle.transfer.service.rocf.outgoing.pdus.RocfStartReturn.Result;
import ccsds.sle.transfer.service.rocf.outgoing.pdus.RocfGetParameterReturnV1To4;
import ccsds.sle.transfer.service.rocf.outgoing.pdus.RocfSyncNotifyInvocation;
import ccsds.sle.transfer.service.rocf.outgoing.pdus.RocfTransferDataInvocation;
import ccsds.sle.transfer.service.rocf.outgoing.pdus.RocfGetParameterReturn;
import ccsds.sle.transfer.service.rocf.structures.AntennaId;
import ccsds.sle.transfer.service.rocf.structures.CarrierLockStatus;
import ccsds.sle.transfer.service.rocf.structures.ControlWordType;
import ccsds.sle.transfer.service.rocf.structures.ControlWordTypeNumber;
import ccsds.sle.transfer.service.rocf.structures.CurrentReportingCycle;
import ccsds.sle.transfer.service.rocf.structures.DiagnosticRocfGet;
import ccsds.sle.transfer.service.rocf.structures.DiagnosticRocfStart;
import ccsds.sle.transfer.service.rocf.structures.FrameSyncLockStatus;
import ccsds.sle.transfer.service.rocf.structures.GvcId;
import ccsds.sle.transfer.service.rocf.structures.GvcIdSet;
import ccsds.sle.transfer.service.rocf.structures.GvcIdSetV1To4;
import ccsds.sle.transfer.service.rocf.structures.LockStatus;
import ccsds.sle.transfer.service.rocf.structures.LockStatusReport;
import ccsds.sle.transfer.service.rocf.structures.MasterChannelComposition;
import ccsds.sle.transfer.service.rocf.structures.RequestedControlWordTypeNumberV1To4;
import ccsds.sle.transfer.service.rocf.structures.RequestedGvcIdV1To4;
import ccsds.sle.transfer.service.rocf.structures.MasterChannelComposition.McOrVcList;
import ccsds.sle.transfer.service.rocf.structures.MasterChannelComposition.McOrVcList.VcList;
import ccsds.sle.transfer.service.rocf.structures.MasterChannelCompositionV1To4;
import ccsds.sle.transfer.service.rocf.structures.Notification;
import ccsds.sle.transfer.service.rocf.structures.RequestedControlWordTypeNumber;
import ccsds.sle.transfer.service.rocf.structures.RequestedGvcId;
import ccsds.sle.transfer.service.rocf.structures.RequestedTcVcid;
import ccsds.sle.transfer.service.rocf.structures.RequestedTcVcidV1To4;
import ccsds.sle.transfer.service.rocf.structures.RequestedUpdateMode;
import ccsds.sle.transfer.service.rocf.structures.RequestedUpdateModeV1To4;
import ccsds.sle.transfer.service.rocf.structures.RocfDeliveryMode;
import ccsds.sle.transfer.service.rocf.structures.RocfGetParameter;
import ccsds.sle.transfer.service.rocf.structures.RocfGetParameter.ParBufferSize;
import ccsds.sle.transfer.service.rocf.structures.RocfGetParameter.ParDeliveryMode;
import ccsds.sle.transfer.service.rocf.structures.RocfGetParameter.ParLatencyLimit;
import ccsds.sle.transfer.service.rocf.structures.RocfGetParameter.ParLatencyLimit.ParameterValue;
import ccsds.sle.transfer.service.rocf.structures.RocfGetParameter.ParPermittedGvcidSet;
import ccsds.sle.transfer.service.rocf.structures.RocfGetParameter.ParPermittedRprtTypeSet;
//mport ccsds.sle.transfer.service.rocf.structures.RocfGetParameter.ParPermittedRprtTypeSet.ParameterValue;
import ccsds.sle.transfer.service.rocf.structures.RocfGetParameter.ParPermittedTcVcidSet;
import ccsds.sle.transfer.service.rocf.structures.RocfGetParameter.ParPermittedUpdModeSet;
import ccsds.sle.transfer.service.rocf.structures.RocfGetParameter.ParReqControlWordType;
import ccsds.sle.transfer.service.rocf.structures.RocfGetParameter.ParReqTcVcid;
import ccsds.sle.transfer.service.rocf.structures.RocfGetParameter.ParReqUpdateMode;
import ccsds.sle.transfer.service.rocf.structures.RocfGetParameterV1To4;
import ccsds.sle.transfer.service.rocf.structures.RocfParameterName;
import ccsds.sle.transfer.service.rocf.structures.RocfProductionStatus;
import ccsds.sle.transfer.service.rocf.structures.SymbolLockStatus;
import ccsds.sle.transfer.service.rocf.structures.VcId;
import ccsds.sle.transfer.service.rocf.structures.TcVcid;
import ccsds.sle.transfer.service.rocf.structures.TcVcidSet;
import ccsds.sle.transfer.service.rocf.structures.TcVcidSet.TcVcids;
import ccsds.sle.transfer.service.rocf.structures.TimeoutPeriod;
import ccsds.sle.transfer.service.rocf.structures.UpdateMode;
import ccsds.sle.transfer.service.rocf.structures.VcId;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class encodes and decodes ROCF PDU's. When decoding, the decoded ROCF
 * operation is instantiated.
 */
public class EE_APIPX_Asn1RocfTranslator extends EE_APIPX_Asn1SleTranslator
{
    /**
     * Constructor of the class which takes the ASNSDK context object as
     * parameter.
     */
    public EE_APIPX_Asn1RocfTranslator(ISLE_OperationFactory pOpFactory,
                                       ISLE_UtilFactory pUtilFactory,
                                       EE_APIPX_PDUTranslator pdutranslator,
                                       int sleVersionNumber)
    {
        super(pOpFactory, pUtilFactory, pdutranslator, sleVersionNumber);
        this.serviceType = SLE_ApplicationIdentifier.sleAI_rtnChOcf;
    }

    /**
     * Allocates and fills the object used for the encoding of Rocf Operation PDUs. 
     * S_OK The ROCF operation has been encoded. E_FAIL
     * Unable to encode the ROCF operation.
     */
    public byte[] encodeRocfOp(ISLE_Operation pRocfOperation, boolean isInvoke) throws SleApiException, IOException
    {
        ReverseByteArrayOutputStream  berBAOStream = new ReverseByteArrayOutputStream (10, true);

        switch (pRocfOperation.getOperationType())
        {
        case sleOT_bind:
        {
            if (isInvoke)
            {
                SleBindInvocationPdu obj = new SleBindInvocationPdu();
                encodeBindInvokeOp(pRocfOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                SleBindReturnPdu obj = new SleBindReturnPdu();
                encodeBindReturnOp(pRocfOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_unbind:
        {
            if (isInvoke)
            {
                SleUnbindInvocationPdu obj = new SleUnbindInvocationPdu();
                encodeUnbindInvokeOp(pRocfOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                SleUnbindReturnPdu obj = new SleUnbindReturnPdu();
                encodeUnbindReturnOp(pRocfOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_stop:
        {
            if (isInvoke)
            {
                RocfStopInvocationPdu obj = new RocfStopInvocationPdu();
                encodeStopInvokeOp(pRocfOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                RocfStopReturnPdu obj = new RocfStopReturnPdu();
                encodeStopReturnOp(pRocfOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_scheduleStatusReport:
        {
            if (isInvoke)
            {
                RocfScheduleStatusReportInvocationPdu obj = new RocfScheduleStatusReportInvocationPdu();
                encodeScheduleSRInvokeOp(pRocfOperation, obj);
                obj.encode(berBAOStream, true);
            }
            else
            {
                RocfScheduleStatusReportReturnPdu obj = new RocfScheduleStatusReportReturnPdu();
                encodeScheduleSRReturnOp(pRocfOperation, obj);
                obj.encode(berBAOStream, true);
            }

            break;
        }
        case sleOT_start:
        {
            IROCF_Start pOp = null;
            pOp = pRocfOperation.queryInterface(IROCF_Start.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    RocfStartInvocationPdu obj = new RocfStartInvocationPdu();
                    encodeStartInvokeOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
                else
                {
                    RocfStartReturnPdu obj = new RocfStartReturnPdu();
                    encodeStartReturnOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
            }

            break;
        }
        case sleOT_getParameter:
        {
            IROCF_GetParameter pOp = null;
            pOp = pRocfOperation.queryInterface(IROCF_GetParameter.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    RocfGetParameterInvocationPdu obj = new RocfGetParameterInvocationPdu();
                    encodeGetParameterInvokeOp(pOp, obj);
                    obj.encode(berBAOStream, true);
                }
                else
                {
                	if(this.sleVersionNumber <=4)
                	{
                		RocfGetParameterReturnPduV1To4 obj = new RocfGetParameterReturnPduV1To4();
                		encodeGetParameterReturnOpV1To4(pOp, obj);
                		obj.encode(berBAOStream, true);
                	}
                	else
                	{
                		// New since SLE V5
                		RocfGetParameterReturnPdu obj = new RocfGetParameterReturnPdu();
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
            IROCF_StatusReport pOp = null;
            pOp = pRocfOperation.queryInterface(IROCF_StatusReport.class);
            if (pOp != null)
            {
                RocfStatusReportInvocationPdu obj = new RocfStatusReportInvocationPdu();
                encodeStatusReportOp(pOp, obj);
                obj.encode(berBAOStream, true);
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
            pOp = pRocfOperation.queryInterface(ISLE_TransferBuffer.class);
            if (pOp != null)
            {
                if (isInvoke)
                {
                    RocfTransferBufferPdu obj = new RocfTransferBufferPdu();
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
     * Instantiates a new ROCF operation from the version 1 object given as
     * parameter, and releases the object. S_OK A new ROCF operation has been
     * Instantiated. E_FAIL Unable to instantiate a ROCF operation.
     * 
     * @throws SleApiException
     * @throws IOException
     */
    public ISLE_Operation decodeRocfOp(byte[] buffer, EE_Reference<Boolean> isInvoke) throws SleApiException,
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
        else if (tag.equals(RocfStartInvocationPdu.tag))
        {
            IROCF_Start pOp = null;
            SLE_OpType opTye = SLE_OpType.sleOT_start;
            pOp = this.operationFactory.createOperation(IROCF_Start.class,
                                                        opTye,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                RocfStartInvocationPdu obj = new RocfStartInvocationPdu();
                obj.decode(is, false);
                decodeStartInvokeOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(RocfStartReturnPdu.tag))
        {
            IROCF_Start pOp = null;
            RocfStartReturnPdu obj = new RocfStartReturnPdu();
            obj.decode(is, false);
            pOp = decodeStartReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
            if (pOp != null)
            {
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(RocfStopInvocationPdu.tag))
        {
            RocfStopInvocationPdu obj = new RocfStopInvocationPdu();
            obj.decode(is, false);
            pOperation = decodeStopInvokeOp(obj);
            isInvoke.setReference(new Boolean(true));
        }
        else if (tag.equals(RocfStopReturnPdu.tag))
        {
            RocfStopReturnPdu obj = new RocfStopReturnPdu();
            obj.decode(is, false);
            pOperation = decodeStopReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
        }
        else if (tag.equals(RocfScheduleStatusReportInvocationPdu.tag))
        {
            RocfScheduleStatusReportInvocationPdu obj = new RocfScheduleStatusReportInvocationPdu();
            obj.decode(is, false);
            pOperation = decodeScheduleSRInvokeOp(obj);
            isInvoke.setReference(new Boolean(true));
        }
        else if (tag.equals(RocfScheduleStatusReportReturnPdu.tag))
        {
            RocfScheduleStatusReportReturnPdu obj = new RocfScheduleStatusReportReturnPdu();
            obj.decode(is, false);
            pOperation = decodeScheduleSRReturnOp(obj);
            isInvoke.setReference(new Boolean(false));
        }
        else if (tag.equals(RocfGetParameterInvocationPdu.tag))
        {
            IROCF_GetParameter pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_getParameter;
            pOp = this.operationFactory.createOperation(IROCF_GetParameter.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                RocfGetParameterInvocationPdu obj = new RocfGetParameterInvocationPdu();
                obj.decode(is, false);
                decodeGetParameterInvokeOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(RocfGetParameterReturnPdu.tag))
        {
            IROCF_GetParameter pOp = null;
            if(this.sleVersionNumber <= 4)
            {
                RocfGetParameterReturnPduV1To4 obj = new RocfGetParameterReturnPduV1To4();
                obj.decode(is, false);
                pOp = decodeGetParameterReturnOpV1To4(obj);
            }
            else{
            	// New with SLES V5
                RocfGetParameterReturnPdu obj = new RocfGetParameterReturnPdu();
                obj.decode(is, false);
                pOp = decodeGetParameterReturnOp(obj);
            }


            isInvoke.setReference(new Boolean(false));
            if (pOp != null)
            {
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(RocfTransferBufferPdu.tag))
        {
            ISLE_TransferBuffer pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_transferBuffer;
            pOp = this.operationFactory.createOperation(ISLE_TransferBuffer.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                RocfTransferBufferPdu obj = new RocfTransferBufferPdu();
                obj.decode(is, false);
                decodeTransferBufferOp(obj, pOp);
                isInvoke.setReference(new Boolean(true));
                pOperation = pOp.queryInterface(ISLE_Operation.class);
            }
        }
        else if (tag.equals(RocfStatusReportInvocationPdu.tag))
        {
            IROCF_StatusReport pOp = null;
            SLE_OpType opType = SLE_OpType.sleOT_statusReport;
            pOp = this.operationFactory.createOperation(IROCF_StatusReport.class,
                                                        opType,
                                                        this.serviceType,
                                                        this.sleVersionNumber);
            if (pOp != null)
            {
                RocfStatusReportInvocationPdu obj = new RocfStatusReportInvocationPdu();
                obj.decode(is, false);
                decodeStatusReportOp(obj, pOp);

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
     * Fills the object used for the encoding of Rocf Start invoke operation.
     * S_OK The ROCF Start operation has been encoded. E_FAIL Unable to encode
     * the ROCF Start operation.
     * 
     * @throws SleApiException
     */
    private void encodeStartInvokeOp(IROCF_Start pStartOperation, RocfStartInvocationPdu eeaRocfO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pStartOperation.getInvokerCredentials();
        eeaRocfO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaRocfO.setInvokeId(new InvokeId(pStartOperation.getInvokeId()));

        // the start time
        ISLE_Time pTime = null;
        pTime = pStartOperation.getStartTime();
        eeaRocfO.setStartTime(encodeConditionalTime(pTime));

        // the stop time
        pTime = pStartOperation.getStopTime();
        eeaRocfO.setStopTime(encodeConditionalTime(pTime));

        // the gvcid
        ROCF_Gvcid pGvcId = null;
        pGvcId = pStartOperation.getGvcid();
        eeaRocfO.setRequestedGvcId(encodeGvcid(pGvcId));

        // the control word type
        ROCF_ControlWordType controlWordType = pStartOperation.getControlWordType();
        switch (controlWordType)
        {
        case rocfCWT_allControlWords:
        {
        	ControlWordType cwt = new ControlWordType();
        	cwt.setAllControlWords(new BerNull());
            eeaRocfO.setControlWordType(cwt);
            break;
        }
        case rocfCWT_clcw:
        {
            ControlWordType clcw = new ControlWordType();
            TcVcid tcVcid = new TcVcid();
            if (pStartOperation.getTcVcidUsed())
            {
                tcVcid.setTcVcid(new VcId(pStartOperation.getTcVcid()));
                clcw.setClcw(tcVcid);
            }
            else
            {
                tcVcid.setNoTcVC(new BerNull());
                clcw.setClcw(tcVcid);
            }

            eeaRocfO.setControlWordType(clcw);
            break;
        }
        case rocfCWT_notClcw:
        {
        	ControlWordType cwt = new ControlWordType();
        	cwt.setNotClcw(new BerNull());
            eeaRocfO.setControlWordType(cwt);
            break;
        }
        default:
        {
            break;
        }
        }

        // the update mode
        ROCF_UpdateMode updateMode = pStartOperation.getUpdateMode();
        eeaRocfO.setUpdateMode (new UpdateMode(updateMode.getCode()));
    }

    /**
     * Fills the ROCF START invoke operation from the object. S_OK The ROCF
     * Start operation has been decoded. E_FAIL Unable to decode the ROCF Start
     * operation.
     * 
     * @throws SleApiException
     */
    private void decodeStartInvokeOp(RocfStartInvocationPdu eeaRocfO, IROCF_Start pStartOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaRocfO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pStartOperation.putInvokerCredentials(pCredentials);
        }

        // the invoker id
        pStartOperation.setInvokeId(eeaRocfO.getInvokeId().value.intValue());

        // the start time
        ISLE_Time pTime = null;
        pTime = decodeConditionalTime(eeaRocfO.getStartTime());

        if (pTime != null)
        {
            pStartOperation.putStartTime(pTime);
        }

        // the stop time
        pTime = null;
        pTime = decodeConditionalTime(eeaRocfO.getStopTime());

        if (pTime != null)
        {
            pStartOperation.putStopTime(pTime);
        }

        // the gvcid
        ROCF_Gvcid pGvcId = decodeGvcid(eeaRocfO.getRequestedGvcId());
        pStartOperation.putGvcid(pGvcId);

        // the control word type
        ROCF_ControlWordType controlWordType = ROCF_ControlWordType.rocfCWT_invalid;
        if (eeaRocfO.getControlWordType().getAllControlWords() != null)
        {
            controlWordType = ROCF_ControlWordType.rocfCWT_allControlWords;
        }
        else if (eeaRocfO.getControlWordType().getClcw() != null)
        {
            controlWordType = ROCF_ControlWordType.rocfCWT_clcw;
            // the tcvcid
            if (eeaRocfO.getControlWordType().getClcw().getTcVcid() != null)
            {
                pStartOperation.setTcVcid(eeaRocfO.getControlWordType().getClcw().getTcVcid().value.longValue());
            }
        }
        else if (eeaRocfO.getControlWordType().getNotClcw() != null)
        {
            controlWordType = ROCF_ControlWordType.rocfCWT_notClcw;
        }

        pStartOperation.setControlWordType(controlWordType);

        // the update mode
        ROCF_UpdateMode updateMode = ROCF_UpdateMode.getROCFUpdateModeByCode(eeaRocfO.getUpdateMode().value.intValue());
        pStartOperation.setUpdateMode(updateMode);
    }

    /**
     * Fills the object used for the encoding of Rocf Start return operation.
     * S_OK The ROCF Start operation has been encoded. E_FAIL Unable to encode
     * the ROCF Start operation.
     * 
     * @throws SleApiException
     */
    private void encodeStartReturnOp(IROCF_Start pStartOperation, RocfStartReturnPdu eeaRocfO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pStartOperation.getPerformerCredentials();
        eeaRocfO.setPerformerCredentials(encodeCredentials(pCredentials));

        // the invoker id
        eeaRocfO.setInvokeId(new InvokeId(pStartOperation.getInvokeId()));

        // the result
        if (pStartOperation.getResult() == SLE_Result.sleRES_positive)
        {
            Result posResult = new Result();
            posResult.setPositiveResult(new BerNull());
            eeaRocfO.setResult(posResult);
        }
        else
        {
            Result negResult = new Result();

            if (pStartOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                DiagnosticRocfStart repSpecific = new DiagnosticRocfStart();

                switch (pStartOperation.getStartDiagnostic())
                {
                case rocfSD_outOfService:
                {
                    repSpecific.setSpecific(new BerInteger(ROCF_StartDiagnostic.rocfSD_outOfService.getCode()));
                    break;
                }
                case rocfSD_unableToComply:
                {
                    repSpecific.setSpecific(new BerInteger(ROCF_StartDiagnostic.rocfSD_unableToComply.getCode()));
                    break;
                }
                case rocfSD_invalidStartTime:
                {
                    repSpecific.setSpecific(new BerInteger(ROCF_StartDiagnostic.rocfSD_invalidStartTime.getCode()));
                    break;
                }
                case rocfSD_invalidStopTime:
                {
                    repSpecific.setSpecific(new BerInteger(ROCF_StartDiagnostic.rocfSD_invalidStopTime.getCode()));
                    break;
                }
                case rocfSD_missingTimeValue:
                {
                    repSpecific.setSpecific(new BerInteger(ROCF_StartDiagnostic.rocfSD_missingTimeValue.getCode()));
                    break;
                }
                case rocfSD_invalidGvcId:
                {
                    repSpecific.setSpecific(new BerInteger(ROCF_StartDiagnostic.rocfSD_invalidGvcId.getCode()));
                    break;
                }
                case rocfSD_invalidControlWordType:
                {
                    repSpecific.setSpecific(new BerInteger(ROCF_StartDiagnostic.rocfSD_invalidControlWordType.getCode()));
                    break;
                }
                case rocfSD_invalidTcVcid:
                {
                    repSpecific.setSpecific(new BerInteger(ROCF_StartDiagnostic.rocfSD_invalidTcVcid.getCode()));
                    break;
                }
                case rocfSD_invalidUpdateMode:
                {
                    repSpecific.setSpecific(new BerInteger(ROCF_StartDiagnostic.rocfSD_invalidUpdateMode.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(ROCF_StartDiagnostic.rocfSD_invalid.getCode()));
                    break;
                }
                }

                negResult.setNegativeResult(repSpecific);
            }
            else if (pStartOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_commonDiagnostics)
            {
                // common diagnostic
                DiagnosticRocfStart repCommon = new DiagnosticRocfStart();
                repCommon.setCommon(new Diagnostics(pStartOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }
            else
            {
                negResult.setNegativeResult(new DiagnosticRocfStart());
            }

            eeaRocfO.setResult(negResult);
        }
    }

    /**
     * Fills the ROCF START return operation from the object. S_OK The ROCF
     * Start operation has been decoded. E_FAIL Unable to decode the ROCF Start
     * operation.
     * 
     * @throws SleApiException
     */
    private IROCF_Start decodeStartReturnOp(RocfStartReturnPdu eeaRocfO) throws SleApiException
    {
        IROCF_Start pStartOperation = null;
        ISLE_Operation pOperation = null;

        pOperation = this.pduTranslator.getReturnOp(eeaRocfO.getInvokeId(), SLE_OpType.sleOT_start);
        if (pOperation != null)
        {
            pStartOperation = pOperation.queryInterface(IROCF_Start.class);
            if (pStartOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaRocfO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pStartOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pStartOperation.setInvokeId(eeaRocfO.getInvokeId().value.intValue());

                // the result
                if (eeaRocfO.getResult().getPositiveResult() != null)
                {
                    pStartOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eeaRocfO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        int specValue = eeaRocfO.getResult().getNegativeResult().getSpecific().value.intValue();
                        pStartOperation.setStartDiagnostic(ROCF_StartDiagnostic.getStartDiagnosticByCode(specValue));
                    }
                    else
                    {
                        // common diagnostic
                        int commValue = eeaRocfO.getResult().getNegativeResult().getCommon().value.intValue();
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
     * Fills the object used for the encoding of Rocf GetParameter invoke
     * operation. S_OK The ROCF GetParameter operation has been encoded. E_FAIL
     * Unable to encode the ROCF GetParameter operation.
     * 
     * @throws SleApiException
     */
    private void encodeGetParameterInvokeOp(IROCF_GetParameter pGetParameterOperation,
                                            RocfGetParameterInvocationPdu eeaRocfO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pGetParameterOperation.getInvokerCredentials();
        eeaRocfO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaRocfO.setInvokeId(new InvokeId(pGetParameterOperation.getInvokeId()));

        // the parameter
        eeaRocfO.setRocfParameter(new RocfParameterName(pGetParameterOperation.getRequestedParameter().getCode()));
    }

    /**
     * Fills the ROCF GET-PARAMETER invoke operation from the object. S_OK The
     * ROCF GetParameter operation has been decoded. E_FAIL Unable to decode the
     * ROCF GetParameter operation.
     * 
     * @throws SleApiException
     */
    private void decodeGetParameterInvokeOp(RocfGetParameterInvocationPdu eeaRocfO,
                                            IROCF_GetParameter pGetParameterOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaRocfO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pGetParameterOperation.putInvokerCredentials(pCredentials);
        }

        // the invoke id
        pGetParameterOperation.setInvokeId(eeaRocfO.getInvokeId().value.intValue());

        // the parameter
        pGetParameterOperation.setRequestedParameter(ROCF_ParameterName
                .getROCFParamNameByCode(eeaRocfO.getRocfParameter().value.intValue()));

    }

    /**
     * Fills the object used for the encoding of Rocf GetParameter return
     * operation. S_OK The ROCF GetParameter operation has been encoded. E_FAIL
     * Unable to encode the ROCF GetParameter operation.
     * 
     * @throws SleApiException
     */
    private void encodeGetParameterReturnOpV1To4(IROCF_GetParameter pGetParameterOperation,
                                            RocfGetParameterReturnPduV1To4 eeaRocfO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pGetParameterOperation.getPerformerCredentials();
        eeaRocfO.setPerformerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaRocfO.setInvokeId(new InvokeId(pGetParameterOperation.getInvokeId()));

        // the result
        if (pGetParameterOperation.getResult() == SLE_Result.sleRES_positive)
        {
            // positive result
            RocfGetParameterReturnV1To4.Result positiveResult = new RocfGetParameterReturnV1To4.Result();
            positiveResult.setPositiveResult(new RocfGetParameterV1To4());
            encodeParameterV1To4(pGetParameterOperation, positiveResult.getPositiveResult());
            eeaRocfO.setResult(positiveResult);
        }
        else
        {
            RocfGetParameterReturnV1To4.Result negResult = new RocfGetParameterReturnV1To4.Result();

            if (pGetParameterOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                DiagnosticRocfGet repSpecific = new DiagnosticRocfGet();

                switch (pGetParameterOperation.getGetParameterDiagnostic())
                {
                case rocfGP_unknownParameter:
                {
                    repSpecific.setSpecific(new BerInteger(ROCF_GetParameterDiagnostic.rocfGP_unknownParameter.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(ROCF_GetParameterDiagnostic.rocfGP_invalid.getCode()));
                    break;
                }
                }

                negResult.setNegativeResult(repSpecific);
            }
            else
            {
                // common diagnostic
                DiagnosticRocfGet repCommon = new DiagnosticRocfGet();
                repCommon.setSpecific(null);
                repCommon.setCommon(new Diagnostics(pGetParameterOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }

            eeaRocfO.setResult(negResult);
        }
    }
    
    
    /**
     * Fills the object used for the encoding of Rocf GetParameter return
     * operation. S_OK The ROCF GetParameter operation has been encoded. E_FAIL
     * Unable to encode the ROCF GetParameter operation.
     * 
     * @throws SleApiException
     */
    private void encodeGetParameterReturnOp(IROCF_GetParameter pGetParameterOperation,
                                            RocfGetParameterReturnPdu eeaRocfO) throws SleApiException
    {
        // the performer credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pGetParameterOperation.getPerformerCredentials();
        eeaRocfO.setPerformerCredentials(encodeCredentials(pCredentials));

        // the invoke id
        eeaRocfO.setInvokeId(new InvokeId(pGetParameterOperation.getInvokeId()));

        // the result
        if (pGetParameterOperation.getResult() == SLE_Result.sleRES_positive)
        {
            // positive result
            RocfGetParameterReturn.Result positiveResult = new RocfGetParameterReturn.Result();
            positiveResult.setPositiveResult(new RocfGetParameter());
            encodeParameter(pGetParameterOperation, positiveResult.getPositiveResult());
            eeaRocfO.setResult(positiveResult);
        }
        else
        {
            RocfGetParameterReturn.Result negResult = new RocfGetParameterReturn.Result();

            if (pGetParameterOperation.getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                DiagnosticRocfGet repSpecific = new DiagnosticRocfGet();

                switch (pGetParameterOperation.getGetParameterDiagnostic())
                {
                case rocfGP_unknownParameter:
                {
                    repSpecific.setSpecific(new BerInteger(ROCF_GetParameterDiagnostic.rocfGP_unknownParameter.getCode()));
                    break;
                }
                default:
                {
                    repSpecific.setSpecific(new BerInteger(ROCF_GetParameterDiagnostic.rocfGP_invalid.getCode()));
                    break;
                }
                }

                negResult.setNegativeResult(repSpecific);
            }
            else
            {
                // common diagnostic
                DiagnosticRocfGet repCommon = new DiagnosticRocfGet();
                repCommon.setSpecific(null);
                repCommon.setCommon(new Diagnostics(pGetParameterOperation.getDiagnostics().getCode()));
                negResult.setNegativeResult(repCommon);
            }

            eeaRocfO.setResult(negResult);
        }
    }

    /**
     * Fills the ROCF GET-PARAMETER return operation from the object. S_OK The
     * ROCF GetParameter operation has been decoded. E_FAIL Unable to decode the
     * ROCF GetParameter operation.
     * 
     * @throws SleApiException
     */
    private IROCF_GetParameter decodeGetParameterReturnOpV1To4(RocfGetParameterReturnPduV1To4 eeaRocfO) throws SleApiException
    {
        IROCF_GetParameter pGetParameterOperation = null;
        ISLE_Operation pOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_getParameter;

        pOperation = this.pduTranslator.getReturnOp(eeaRocfO.getInvokeId(), opType);
        if (pOperation != null)
        {
            pGetParameterOperation = pOperation.queryInterface(IROCF_GetParameter.class);
            if (pGetParameterOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaRocfO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pGetParameterOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pGetParameterOperation.setInvokeId(eeaRocfO.getInvokeId().value.intValue());

                // the result
                if (eeaRocfO.getResult().getPositiveResult() != null)
                {
                    decodeParameterv1To4(eeaRocfO.getResult().getPositiveResult(), pGetParameterOperation);
                    pGetParameterOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eeaRocfO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        int specValue = eeaRocfO.getResult().getNegativeResult().getSpecific().value.intValue();
                        pGetParameterOperation.setGetParameterDiagnostic(ROCF_GetParameterDiagnostic
                                .getGetParamDiagByCode(specValue));
                    }
                    else
                    {
                        // common diagnostic
                        int commValue = eeaRocfO.getResult().getNegativeResult().getCommon().value.intValue();
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
     * Fills the ROCF GET-PARAMETER return operation from the object. S_OK The
     * ROCF GetParameter operation has been decoded. E_FAIL Unable to decode the
     * ROCF GetParameter operation.
     * 
     * @throws SleApiException
     */
    private IROCF_GetParameter decodeGetParameterReturnOp(RocfGetParameterReturnPdu eeaRocfO) throws SleApiException
    {
        IROCF_GetParameter pGetParameterOperation = null;
        ISLE_Operation pOperation = null;
        SLE_OpType opType = SLE_OpType.sleOT_getParameter;

        pOperation = this.pduTranslator.getReturnOp(eeaRocfO.getInvokeId(), opType);
        if (pOperation != null)
        {
            pGetParameterOperation = pOperation.queryInterface(IROCF_GetParameter.class);
            if (pGetParameterOperation != null)
            {
                // the performer credentials
                ISLE_Credentials pCredentials = null;
                pCredentials = decodeCredentials(eeaRocfO.getPerformerCredentials());
                if (pCredentials != null)
                {
                    pGetParameterOperation.putPerformerCredentials(pCredentials);
                }

                // the invoke id
                pGetParameterOperation.setInvokeId(eeaRocfO.getInvokeId().value.intValue());

                // the result
                if (eeaRocfO.getResult().getPositiveResult() != null)
                {
                    decodeParameter(eeaRocfO.getResult().getPositiveResult(), pGetParameterOperation);
                    pGetParameterOperation.setPositiveResult();
                }
                else
                {
                    // negative result
                    if (eeaRocfO.getResult().getNegativeResult().getSpecific() != null)
                    {
                        int specValue = eeaRocfO.getResult().getNegativeResult().getSpecific().value.intValue();
                        pGetParameterOperation.setGetParameterDiagnostic(ROCF_GetParameterDiagnostic
                                .getGetParamDiagByCode(specValue));
                    }
                    else
                    {
                        // common diagnostic
                        int commValue = eeaRocfO.getResult().getNegativeResult().getCommon().value.intValue();
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
     * Fills the object used for the encoding of Rocf StatusReport operation.
     * S_OK The ROCF StatusReport operation has been encoded. E_FAIL Unable to
     * encode the ROCF StatusReport operation.
     * 
     * @throws SleApiException
     */
    private void encodeStatusReportOp(IROCF_StatusReport pStatusReportOperation, RocfStatusReportInvocationPdu eeaRocfO) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pStatusReportOperation.getInvokerCredentials();
        eeaRocfO.setInvokerCredentials(encodeCredentials(pCredentials));

        // the delivered frame number
        eeaRocfO.setDeliveredOcfsNumber(new IntUnsignedLong(pStatusReportOperation.getNumOcfDelivered()));
        eeaRocfO.setProcessedFrameNumber(new IntUnsignedLong(pStatusReportOperation.getNumFrames()));
        // the frame sync lock status
        eeaRocfO.setFrameSyncLockStatus(new FrameSyncLockStatus(pStatusReportOperation.getFrameSyncLock().getCode()));
        // the symbol sync lock status
        eeaRocfO.setSymbolSyncLockStatus(new SymbolLockStatus(pStatusReportOperation.getSymbolSyncLock().getCode()));
        // the sub carrier lock status
        eeaRocfO.setSubcarrierLockStatus(new LockStatus(pStatusReportOperation.getSubCarrierDemodLock().getCode()));
        // the carrier lock status
        eeaRocfO.setCarrierLockStatus(new CarrierLockStatus(pStatusReportOperation.getCarrierDemodLock().getCode()));
        // the production status
        eeaRocfO.setProductionStatus(new RocfProductionStatus(pStatusReportOperation.getProductionStatus().getCode()));
    }

    /**
     * Fills the ROCF STATUS-REPORT operation from the object. S_OK The ROCF
     * StatusReport operation has been decoded. E_FAIL Unable to decode the ROCF
     * StatusReport operation.
     * 
     * @throws SleApiException
     */
    private void decodeStatusReportOp(RocfStatusReportInvocationPdu eeaRocfO, IROCF_StatusReport pStatusReportOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaRocfO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pStatusReportOperation.putInvokerCredentials(pCredentials);
        }

        // the delivered frame number
        pStatusReportOperation.setNumFrames(eeaRocfO.getProcessedFrameNumber().value.longValue());
        pStatusReportOperation.setNumOcfDelivered(eeaRocfO.getDeliveredOcfsNumber().value.longValue());
        // the frame sync lock status
        pStatusReportOperation.setFrameSyncLock(ROCF_LockStatus
                .getLockStatusByCode(eeaRocfO.getFrameSyncLockStatus().value.intValue()));
        // the symbol sync lock status
        pStatusReportOperation.setSymbolSyncLock(ROCF_LockStatus
                .getLockStatusByCode(eeaRocfO.getSymbolSyncLockStatus().value.intValue()));
        // the sub carrier lock status
        pStatusReportOperation.setSubCarrierDemodLock(ROCF_LockStatus
                .getLockStatusByCode(eeaRocfO.getSubcarrierLockStatus().value.intValue()));
        // the carrier lock status
        pStatusReportOperation.setCarrierDemodLock(ROCF_LockStatus
                .getLockStatusByCode(eeaRocfO.getCarrierLockStatus().value.intValue()));
        // the production status
        pStatusReportOperation.setProductionStatus(ROCF_ProductionStatus
                .getProductionStatusByCode(eeaRocfO.getProductionStatus().value.intValue()));
    }

    /**
     * fills the object used for the encoding of Rocf TransferData operation.
     * CodesS_OK The ROCF TransferData operation has been encoded. E_FAIL Unable
     * to encode the ROCF TransferData operation.
     * 
     * @throws SleApiException
     */
    private void encodeTransferDataOp(IROCF_TransferData pTransferDataOperation, OcfOrNotification eeaRocfO) throws SleApiException
    {
        RocfTransferDataInvocation annotatedOcf = new RocfTransferDataInvocation();

        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pTransferDataOperation.getInvokerCredentials();
        annotatedOcf.setInvokerCredentials(encodeCredentials(pCredentials));

        // the earth receive time
        ISLE_Time pTime = null;
        pTime = pTransferDataOperation.getEarthReceiveTime();
        annotatedOcf.setEarthReceiveTime(encodeEarthReceiveTime(pTime));

        // the antenna id
        if (pTransferDataOperation.getAntennaIdFormat() == ROCF_AntennaIdFormat.rocfAF_global)
        {
            // global form
            BerObjectIdentifier objectId = new BerObjectIdentifier(pTransferDataOperation.getAntennaIdGF());
            AntennaId aid = new AntennaId();
            aid.setGlobalForm(objectId);
            annotatedOcf.setAntennaId(aid);
        }
        else
        {
            // local form
            byte[] poctet = pTransferDataOperation.getAntennaIdLF();
            if (poctet.length <= C_MaxLengthAntennaLocalForm)
            {
            	AntennaId aid = new AntennaId();
                aid.setLocalForm(new BerOctetString(poctet));
                annotatedOcf.setAntennaId(aid);
            }
        }

        // the data link continuity
        annotatedOcf.setDataLinkContinuity(new BerInteger(pTransferDataOperation.getDataLinkContinuity()));

        // the private annotation
        byte[] pa = pTransferDataOperation.getPrivateAnnotation();
        if (pa == null)
        {
        	RocfTransferDataInvocation.PrivateAnnotation paPA = new RocfTransferDataInvocation.PrivateAnnotation();
        	paPA.setNull(new BerNull());
            annotatedOcf.setPrivateAnnotation(paPA);
        }
        else
        {
            if (pa.length <= C_MaxLengthPrivateAnnotation)
            {
            	RocfTransferDataInvocation.PrivateAnnotation paPA = new RocfTransferDataInvocation.PrivateAnnotation();
            	paPA.setNotNull(new BerOctetString(pa));
                annotatedOcf.setPrivateAnnotation(paPA);
            }
        }

        // the space link data unit
        byte[] pdata = pTransferDataOperation.getData();
        SpaceLinkDataUnit data = new SpaceLinkDataUnit(pdata);
        annotatedOcf.setData(data);

        eeaRocfO.setAnnotatedOcf(annotatedOcf);
    }

    /**
     * Fills the ROCF TRANSFER-DATA operation from the object. S_OK The ROCF
     * TransferData operation has been decoded. E_FAIL Unable to decode the ROCF
     * TransferData operation.
     * 
     * @throws SleApiException
     */
    private void decodeTransferDataOp(RocfTransferDataInvocation eeaRocfO, IROCF_TransferData pTransferDataOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaRocfO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pTransferDataOperation.putInvokerCredentials(pCredentials);
        }

        // the earth receive time
        ISLE_Time pTime = null;
        pTime = decodeEarthReceiveTime(eeaRocfO.getEarthReceiveTime());
        if (pTime != null)
        {
            pTransferDataOperation.putEarthReceiveTime(pTime);
        }

        // the antenna id
        AntennaId eeaAntenna = eeaRocfO.getAntennaId();
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
        pTransferDataOperation.setDataLinkContinuity(eeaRocfO.getDataLinkContinuity().value.intValue());

        // the private annotation
        if (eeaRocfO.getPrivateAnnotation().getNull() != null)
        {
            pTransferDataOperation.putPrivateAnnotation(null);
        }
        else
        {
            if (eeaRocfO.getPrivateAnnotation().getNotNull().value.length <= C_MaxLengthPrivateAnnotation)
            {
                pTransferDataOperation.putPrivateAnnotation(eeaRocfO.getPrivateAnnotation().getNotNull().value);
            }
        }

        // the space link data unit
        byte[] pdata = null;
        pdata = eeaRocfO.getData().value;
        pTransferDataOperation.putData(pdata);
    }

    /**
     * Fills the object used for the encoding of Rocf SyncNotify operation. S_OK
     * The ROCF SyncNotify operation has been encoded. E_FAIL Unable to encode
     * the ROCF SyncNotify operation.
     * 
     * @throws SleApiException
     */
    private void encodeSyncNotifyOp(IROCF_SyncNotify pSyncNotifyOperation, OcfOrNotification eeaRocfO) throws SleApiException
    {
        RocfSyncNotifyInvocation syncNotify = new RocfSyncNotifyInvocation();

        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = pSyncNotifyOperation.getInvokerCredentials();
        syncNotify.setInvokerCredentials(encodeCredentials(pCredentials));

        // the rocf notification
        syncNotify.setNotification(new Notification());

        switch (pSyncNotifyOperation.getNotificationType())
        {
        case rocfNT_lossFrameSync:
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
        case rocfNT_productionStatusChange:
        {
            syncNotify.getNotification().setProductionStatusChange(new RocfProductionStatus(pSyncNotifyOperation
                    .getProductionStatus().getCode()));
            break;
        }
        case rocfNT_excessiveDataBacklog:
        {
            syncNotify.getNotification().setExcessiveDataBacklog(new BerNull());
            break;
        }
        case rocfNT_endOfData:
        {
            syncNotify.getNotification().setEndOfData(new BerNull());
            break;
        }
        default:
        {
            break;
        }
        }

        eeaRocfO.setSyncNotification(syncNotify);
    }

    /**
     * Fills the ROCF SYNC-NOTIFY operation from the object. S_OK The ROCF
     * SyncNotify operation has been decoded. E_FAIL Unable to decode the ROCF
     * SyncNotify operation.
     * 
     * @throws SleApiException
     */
    private void decodeSyncNotifyOp(RocfSyncNotifyInvocation eeaRocfO, IROCF_SyncNotify pSyncNotifyOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = null;
        pCredentials = decodeCredentials(eeaRocfO.getInvokerCredentials());
        if (pCredentials != null)
        {
            pSyncNotifyOperation.setInvokerCredentials(pCredentials);
        }

        // the rocf notification
        if (eeaRocfO.getNotification().getLossFrameSync() != null)
        {
            // the time
            ISLE_Time pTime = null;
            pTime = decodeTime(eeaRocfO.getNotification().getLossFrameSync().getTime());

            // the carrier lock status
            ROCF_LockStatus carrierDemodLock = ROCF_LockStatus
                    .getLockStatusByCode(eeaRocfO.getNotification().getLossFrameSync().getCarrierLockStatus().value.intValue());
            // the sub carrier lock status
            ROCF_LockStatus subCarrierDemodLock = ROCF_LockStatus
                    .getLockStatusByCode(eeaRocfO.getNotification().getLossFrameSync().getSubcarrierLockStatus().value.intValue());
            // the symbol sync lock status
            ROCF_LockStatus symbolSyncLock = ROCF_LockStatus
                    .getLockStatusByCode(eeaRocfO.getNotification().getLossFrameSync().getSymbolSyncLockStatus().value.intValue());

            pSyncNotifyOperation.setLossOfFrameSync(pTime, symbolSyncLock, subCarrierDemodLock, carrierDemodLock);
        }
        else if (eeaRocfO.getNotification().getProductionStatusChange() != null)
        {
            ROCF_ProductionStatus productionStatus = ROCF_ProductionStatus
                    .getProductionStatusByCode(eeaRocfO.getNotification().getProductionStatusChange().value.intValue());
            pSyncNotifyOperation.setProductionStatusChange(productionStatus);
        }
        else if (eeaRocfO.getNotification().getExcessiveDataBacklog() != null)
        {
            pSyncNotifyOperation.setDataDiscarded();
        }
        else if (eeaRocfO.getNotification().getEndOfData() != null)
        {
            pSyncNotifyOperation.setEndOfData();
        }
    }

    /**
     * Fills the object used for the encoding of Rocf TransferBuffer operation.
     * S_OK The ROCF TransferBuffer operation has been encoded. E_FAIL Unable to
     * encode the ROCF TransferBuffer operation.
     * 
     * @throws SleApiException
     */
    private void encodeTransferBufferOp(ISLE_TransferBuffer pTransferBufferOperation, RocfTransferBufferPdu eeaRocfO) throws SleApiException
    {
        ISLE_Operation pCurrentOp = null;

        if (pTransferBufferOperation.getSize() == 0)
        {
            return ;
        }

        pTransferBufferOperation.reset();

        // for all the operations of the transfer buffer operation
        while (pTransferBufferOperation.moreData())
        {
            pCurrentOp = pTransferBufferOperation.next();
            SLE_OpType opType = pCurrentOp.getOperationType();

            OcfOrNotification currentElement = new OcfOrNotification();

            if (opType == SLE_OpType.sleOT_transferData)
            {
                IROCF_TransferData pOp = null;
                pOp = pCurrentOp.queryInterface(IROCF_TransferData.class);
                if (pOp != null)
                {
                    encodeTransferDataOp(pOp, currentElement);
                    // add the current element to the list
                    eeaRocfO.getOcfOrNotification().add(currentElement);
                }
                else
                {
                    // cannot get the interface
                    throw new SleApiException(HRESULT.E_FAIL, "No interface");
                }
            }
            else if (opType == SLE_OpType.sleOT_syncNotify)
            {
                IROCF_SyncNotify pOp = null;
                pOp = pCurrentOp.queryInterface(IROCF_SyncNotify.class);
                if (pOp != null)
                {
                    encodeSyncNotifyOp(pOp, currentElement);
                    // add the current element to the list
                    eeaRocfO.getOcfOrNotification().add(currentElement);
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
     * fills the TRANSFER-BUFFER operation from the object. S_OK A new ROCF
     * TransferBuffer operation has been instantiated. E_FAIL Unable to
     * Instantiate a ROCF TransferBuffer operation.
     * 
     * @throws SleApiException
     */
    private void decodeTransferBufferOp(RocfTransferBufferPdu eeaRocfO, ISLE_TransferBuffer pTransferBufferOperation) throws SleApiException
    {
        ISLE_Operation pOp = null;
        Iterator<OcfOrNotification> it = eeaRocfO.getOcfOrNotification().iterator();
        int nbOp = 0;

        while (it.hasNext())
        {
            OcfOrNotification currentElement = it.next();
            if (currentElement.getAnnotatedOcf() != null)
            {
                // instantiate a new transfer data operation
                IROCF_TransferData pTransferData = null;
                pTransferData = this.operationFactory.createOperation(IROCF_TransferData.class,
                                                                      SLE_OpType.sleOT_transferData,
                                                                      this.serviceType,
                                                                      this.sleVersionNumber);
                if (pTransferData != null)
                {
                    decodeTransferDataOp(currentElement.getAnnotatedOcf(), pTransferData);
                    pOp = pTransferData.queryInterface(ISLE_Operation.class);
                }
            }
            else if (currentElement.getSyncNotification() != null)
            {
                // instantiate a new sync notify operation
                IROCF_SyncNotify pSyncNotify = null;
                pSyncNotify = this.operationFactory.createOperation(IROCF_SyncNotify.class,
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
            nbOp++;
            pTransferBufferOperation.setMaximumSize(nbOp);
            pTransferBufferOperation.append(pOp);
        }
    }

    private void encodeRequestedTcVcidV1To4(IROCF_GetParameter pGetParameterOperation, RequestedTcVcidV1To4 eeaO)
    {
        if (pGetParameterOperation.getTcVcidUsed())
        {
            TcVcid reqTcVcid = new TcVcid();
            VcId tcVcid = new VcId(pGetParameterOperation.getRequestedTcVcid());
            reqTcVcid.setTcVcid(tcVcid);
            eeaO.setTcVcid(reqTcVcid);
        }
        else
        {
            eeaO.setUndefined(new BerNull());
        }
    }
    
    private void encodeRequestedTcVcid(IROCF_GetParameter pGetParameterOperation, RequestedTcVcid eeaO)
    {
        if (pGetParameterOperation.getTcVcidUsed())
        {
            //TcVcid reqTcVcid = new TcVcid();
            VcId tcVcid = new VcId(pGetParameterOperation.getRequestedTcVcid());
            //reqTcVcid.setTcVcid(tcVcid);
            eeaO.setTcVcid(tcVcid);
        }
        else
        {
            eeaO.setNoTcVC(new BerNull());
        }
    }

    /**
     * Fills the ROCF global VCID of the Asn1 object.
     */
    private GvcId encodeGvcid(ROCF_Gvcid gvcid)
    {
        GvcId eeaO = new GvcId();
        eeaO.setSpacecraftId(new BerInteger(gvcid.getScid()));
        eeaO.setVersionNumber(new BerInteger(gvcid.getVersion()));
        switch (gvcid.getType())
        {
        case rocfCT_MasterChannel:
        {
        	GvcId.VcId gvcId = new GvcId.VcId();
        	gvcId.setMasterChannel(new BerNull());
            eeaO.setVcId(gvcId);
            break;
        }
        case rocfCT_VirtualChannel:
        {
        	GvcId.VcId gvcId = new GvcId.VcId();
        	ccsds.sle.transfer.service.rocf.structures.VcId id = new ccsds.sle.transfer.service.rocf.structures.VcId(gvcid.getVcid());
        	gvcId.setVirtualChannel(id);
            eeaO.setVcId(gvcId);
            break;
        }
        default:
        {
            eeaO.setVcId(new GvcId.VcId());
            break;
        }
        }
        return eeaO;
    }

    /**
     * Fills the ROCF global VCID from the Asn1 object.
     */
    private ROCF_Gvcid decodeGvcid(GvcId eeaO)
    {
        ROCF_Gvcid gvcid = new ROCF_Gvcid();
        gvcid.setScid(eeaO.getSpacecraftId().value.intValue());
        gvcid.setVersion(eeaO.getVersionNumber().value.intValue());
        if (eeaO.getVcId().getMasterChannel() != null)
        {
            gvcid.setType(ROCF_ChannelType.rocfCT_MasterChannel);
            gvcid.setVcid(0);
        }
        else if (eeaO.getVcId().getVirtualChannel() != null)
        {
            gvcid.setType(ROCF_ChannelType.rocfCT_VirtualChannel);
            gvcid.setVcid(eeaO.getVcId().getVirtualChannel().value.intValue());
        }
        else
        {
            gvcid.setType(ROCF_ChannelType.rocfCT_invalid);
        }
        return gvcid;
    }

    /**
     * Fills the ROCF Parameter of the Asn1 object.
     */
    private void encodeParameterV1To4(IROCF_GetParameter pGetParameterOperation, RocfGetParameterV1To4 eeaO)
    {
        switch (pGetParameterOperation.getReturnedParameter())
        {
        case rocfPN_bufferSize:
        {
        	RocfGetParameterV1To4.ParBufferSize parBufferSize = new RocfGetParameterV1To4.ParBufferSize();
            parBufferSize.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_bufferSize.getCode()));
            parBufferSize.setParameterValue(new IntPosShort(pGetParameterOperation.getTransferBufferSize()));
            eeaO.setParBufferSize(parBufferSize);
            break;
        }
        case rocfPN_deliveryMode:
        {
        	RocfGetParameterV1To4.ParDeliveryMode parDeliveryMode = new RocfGetParameterV1To4.ParDeliveryMode();
            parDeliveryMode.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_deliveryMode.getCode()));
            parDeliveryMode.setParameterValue(new RocfDeliveryMode(pGetParameterOperation.getDeliveryMode().getCode()));
            eeaO.setParDeliveryMode(parDeliveryMode);
            break;
        }
        case rocfPN_latencyLimit:
        {
            int latencyLimit = pGetParameterOperation.getLatencyLimit();
            RocfGetParameterV1To4.ParLatencyLimit parLatencyLimit = new RocfGetParameterV1To4.ParLatencyLimit();
            parLatencyLimit.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_latencyLimit.getCode()));
            if (latencyLimit == 0)
            {
            	RocfGetParameterV1To4.ParLatencyLimit.ParameterValue pv = new RocfGetParameterV1To4.ParLatencyLimit.ParameterValue();
            	pv.setOffline(new BerNull());
                parLatencyLimit.setParameterValue(pv);
            }
            else
            {
            	RocfGetParameterV1To4.ParLatencyLimit.ParameterValue pv = new RocfGetParameterV1To4.ParLatencyLimit.ParameterValue();
            	pv.setOnline(new IntPosShort(latencyLimit));
                parLatencyLimit.setParameterValue(pv);
            }
            eeaO.setParLatencyLimit(parLatencyLimit);
            break;

        }
        case rocfPN_permittedGvcidSet:
        {
        	RocfGetParameterV1To4.ParPermittedGvcidSet parPermittedGvcidSet = new RocfGetParameterV1To4.ParPermittedGvcidSet();
            parPermittedGvcidSet.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_permittedGvcidSet.getCode()));
            GvcIdSetV1To4 parSet = new GvcIdSetV1To4();
            encodeGvcidSetV1To4(pGetParameterOperation, parSet);
            parPermittedGvcidSet.setParameterValue(parSet);
            eeaO.setParPermittedGvcidSet(parPermittedGvcidSet);
            break;
        }
        case rocfPN_permittedControlWordTypeSet:
        {
        	RocfGetParameterV1To4.ParPermittedRprtTypeSet parPermittedRprtTypeSet = new RocfGetParameterV1To4.ParPermittedRprtTypeSet();
            parPermittedRprtTypeSet.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_permittedControlWordTypeSet
                    .getCode()));
            RocfGetParameterV1To4.ParPermittedRprtTypeSet.ParameterValue parValue = new RocfGetParameterV1To4.ParPermittedRprtTypeSet.ParameterValue();
            encodeControlWordTypeSetV1To4(pGetParameterOperation, parValue);
            parPermittedRprtTypeSet.setParameterValue(parValue);
            eeaO.setParPermittedRprtTypeSet(parPermittedRprtTypeSet);
            break;
        }
        case rocfPN_permittedTcVcidSet:
        {
        	RocfGetParameterV1To4.ParPermittedTcVcidSet parPermittedTcVcidSet = new RocfGetParameterV1To4.ParPermittedTcVcidSet();
            parPermittedTcVcidSet.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_permittedTcVcidSet.getCode()));
            TcVcidSet parSet = new TcVcidSet();
            encodeTcVcidSet(pGetParameterOperation, parSet);
            parPermittedTcVcidSet.setParameterValue(parSet);
            eeaO.setParPermittedTcVcidSet(parPermittedTcVcidSet);
            break;
        }
        case rocfPN_permittedUpdateModeSet:
        {
        	RocfGetParameterV1To4.ParPermittedUpdModeSet parPermittedUpdModeSet = new RocfGetParameterV1To4.ParPermittedUpdModeSet();
            parPermittedUpdModeSet.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_permittedUpdateModeSet.getCode()));
            RocfGetParameterV1To4.ParPermittedUpdModeSet.ParameterValue parValue = new RocfGetParameterV1To4.ParPermittedUpdModeSet.ParameterValue();
            encodePermittedUpdateModeSetV1To4(pGetParameterOperation, parValue);
            parPermittedUpdModeSet.setParameterValue(parValue);
            eeaO.setParPermittedUpdModeSet(parPermittedUpdModeSet);
            break;
        }
        case rocfPN_reportingCycle:
        {
            long reportingCycle = pGetParameterOperation.getReportingCycle();
            RocfGetParameterV1To4.ParReportingCycle parReportingCycle = new RocfGetParameterV1To4.ParReportingCycle();
            parReportingCycle.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_reportingCycle.getCode()));
            if (reportingCycle == 0)
            {
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOff(new BerNull());
                parReportingCycle.setParameterValue(crc);
            }
            else
            {
            	ReportingCycle rc = new ReportingCycle(reportingCycle);
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOn(rc);
                parReportingCycle.setParameterValue(crc);
            }
            eeaO.setParReportingCycle(parReportingCycle);
            break;
        }
        case rocfPN_requestedGvcid:
        {
        	RocfGetParameterV1To4.ParReqGvcId parReqGvcid = new RocfGetParameterV1To4.ParReqGvcId();
            parReqGvcid.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_requestedGvcid.getCode()));
            RequestedGvcIdV1To4 reqGvcid = new RequestedGvcIdV1To4();
            encodeRequestedGvcidV1To4(pGetParameterOperation.getRequestedGvcid(), reqGvcid);
            parReqGvcid.setParameterValue(reqGvcid);
            eeaO.setParReqGvcId(parReqGvcid);
            break;
        }
        case rocfPN_requestedControlWordType:
        {
        	RocfGetParameterV1To4.ParReqControlWordType parReqControlWordType = new RocfGetParameterV1To4.ParReqControlWordType();
            parReqControlWordType.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_requestedControlWordType.getCode()));
            parReqControlWordType.setParameterValue(new RequestedControlWordTypeNumberV1To4(pGetParameterOperation
                    .getRequestedControlWordType().getCode()));
            eeaO.setParReqControlWordType(parReqControlWordType);
            break;
        }
        case rocfPN_requestedTcVcid:
        {
        	RocfGetParameterV1To4.ParReqTcVcid parReqTcVcid = new RocfGetParameterV1To4.ParReqTcVcid();
            parReqTcVcid.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_requestedTcVcid.getCode()));
            RequestedTcVcidV1To4 reqTcVcid = new RequestedTcVcidV1To4();
            encodeRequestedTcVcidV1To4(pGetParameterOperation, reqTcVcid);
            parReqTcVcid.setParameterValue(reqTcVcid);
            eeaO.setParReqTcVcid(parReqTcVcid);
            break;
        }
        case rocfPN_requestedUpdateMode:
        {
        	RocfGetParameterV1To4.ParReqUpdateMode parReqUpdateMode = new RocfGetParameterV1To4.ParReqUpdateMode();
            parReqUpdateMode.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_requestedUpdateMode.getCode()));
            parReqUpdateMode.setParameterValue(new RequestedUpdateModeV1To4(pGetParameterOperation.getRequestedUpdateMode()
                    .getCode()))	;
            eeaO.setParReqUpdateMode(parReqUpdateMode);
            break;
        }
        case rocfPN_returnTimeoutPeriod:
        {
        	RocfGetParameterV1To4.ParReturnTimeout parRetTimeout = new RocfGetParameterV1To4.ParReturnTimeout();
            parRetTimeout.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_returnTimeoutPeriod.getCode()));
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
     * Fills the ROCF Parameter of the Asn1 object.
     */
    private void encodeParameter(IROCF_GetParameter pGetParameterOperation, RocfGetParameter eeaO)
    {
        switch (pGetParameterOperation.getReturnedParameter())
        {
        case rocfPN_bufferSize:
        {
            ParBufferSize parBufferSize = new ParBufferSize();
            parBufferSize.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_bufferSize.getCode()));
            parBufferSize.setParameterValue(new IntPosShort(pGetParameterOperation.getTransferBufferSize()));
            eeaO.setParBufferSize(parBufferSize);
            break;
        }
        case rocfPN_deliveryMode:
        {
            ParDeliveryMode parDeliveryMode = new ParDeliveryMode();
            parDeliveryMode.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_deliveryMode.getCode()));
            parDeliveryMode.setParameterValue(new RocfDeliveryMode(pGetParameterOperation.getDeliveryMode().getCode()));
            eeaO.setParDeliveryMode(parDeliveryMode);
            break;
        }
        case rocfPN_latencyLimit:
        {
            int latencyLimit = pGetParameterOperation.getLatencyLimit();
            ParLatencyLimit parLatencyLimit = new ParLatencyLimit();
            parLatencyLimit.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_latencyLimit.getCode()));
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
        case rocfPN_permittedGvcidSet:
        {
            ParPermittedGvcidSet parPermittedGvcidSet = new ParPermittedGvcidSet();
            parPermittedGvcidSet.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_permittedGvcidSet.getCode()));
            GvcIdSet parSet = new GvcIdSet();
            encodeGvcidSet(pGetParameterOperation, parSet);
            parPermittedGvcidSet.setParameterValue(parSet);
            eeaO.setParPermittedGvcidSet(parPermittedGvcidSet);
            break;
        }
        case rocfPN_permittedControlWordTypeSet:
        {
            ParPermittedRprtTypeSet parPermittedRprtTypeSet = new ParPermittedRprtTypeSet();
            parPermittedRprtTypeSet.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_permittedControlWordTypeSet
                    .getCode()));
            ParPermittedRprtTypeSet.ParameterValue parValue = new ParPermittedRprtTypeSet.ParameterValue();
            encodeControlWordTypeSet(pGetParameterOperation, parValue);
            parPermittedRprtTypeSet.setParameterValue(parValue);
            eeaO.setParPermittedRprtTypeSet(parPermittedRprtTypeSet);
            break;
        }
        case rocfPN_permittedTcVcidSet:
        {
            ParPermittedTcVcidSet parPermittedTcVcidSet = new ParPermittedTcVcidSet();
            parPermittedTcVcidSet.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_permittedTcVcidSet.getCode()));
            TcVcidSet parSet = new TcVcidSet();
            encodeTcVcidSet(pGetParameterOperation, parSet);
            parPermittedTcVcidSet.setParameterValue(parSet);
            eeaO.setParPermittedTcVcidSet(parPermittedTcVcidSet);
            break;
        }
        case rocfPN_permittedUpdateModeSet:
        {
            ParPermittedUpdModeSet parPermittedUpdModeSet = new ParPermittedUpdModeSet();
            parPermittedUpdModeSet.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_permittedUpdateModeSet.getCode()));
            ccsds.sle.transfer.service.rocf.structures.RocfGetParameter.ParPermittedUpdModeSet.ParameterValue parValue = new ccsds.sle.transfer.service.rocf.structures.RocfGetParameter.ParPermittedUpdModeSet.ParameterValue();
            encodePermittedUpdateModeSet(pGetParameterOperation, parValue);
            parPermittedUpdModeSet.setParameterValue(parValue);
            eeaO.setParPermittedUpdModeSet(parPermittedUpdModeSet);
            break;
        }
        case rocfPN_reportingCycle:
        {
            long reportingCycle = pGetParameterOperation.getReportingCycle();
            ccsds.sle.transfer.service.rocf.structures.RocfGetParameter.ParReportingCycle parReportingCycle = new ccsds.sle.transfer.service.rocf.structures.RocfGetParameter.ParReportingCycle();
            parReportingCycle.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_reportingCycle.getCode()));
            if (reportingCycle == 0)
            {
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOff(new BerNull());
                parReportingCycle.setParameterValue(crc);
            }
            else
            {
            	ReportingCycle rc = new ReportingCycle(reportingCycle);
            	CurrentReportingCycle crc = new CurrentReportingCycle();
            	crc.setPeriodicReportingOn(rc);
                parReportingCycle.setParameterValue(crc);
            }
            eeaO.setParReportingCycle(parReportingCycle);
            break;
        }
        case rocfPN_requestedGvcid:
        {
            ccsds.sle.transfer.service.rocf.structures.RocfGetParameter.ParReqGvcId parReqGvcid = new ccsds.sle.transfer.service.rocf.structures.RocfGetParameter.ParReqGvcId();
            parReqGvcid.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_requestedGvcid.getCode()));
            RequestedGvcId reqGvcid = new RequestedGvcId();
            encodeRequestedGvcid(pGetParameterOperation.getRequestedGvcid(), reqGvcid);
            parReqGvcid.setParameterValue(reqGvcid);
            eeaO.setParReqGvcId(parReqGvcid);
            break;
        }
        case rocfPN_requestedControlWordType:
        {
            ParReqControlWordType parReqControlWordType = new ParReqControlWordType();
            parReqControlWordType.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_requestedControlWordType.getCode()));
            parReqControlWordType.setParameterValue(new RequestedControlWordTypeNumber(pGetParameterOperation
                    .getRequestedControlWordType().getCode()));
            eeaO.setParReqControlWordType(parReqControlWordType);
            break;
        }
        case rocfPN_requestedTcVcid:
        {
            ParReqTcVcid parReqTcVcid = new ParReqTcVcid();
            parReqTcVcid.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_requestedTcVcid.getCode()));
            RequestedTcVcid reqTcVcid = new RequestedTcVcid();
            encodeRequestedTcVcid(pGetParameterOperation, reqTcVcid);
            parReqTcVcid.setParameterValue(reqTcVcid);
            eeaO.setParReqTcVcid(parReqTcVcid);
            break;
        }
        case rocfPN_requestedUpdateMode:
        {
            ParReqUpdateMode parReqUpdateMode = new ParReqUpdateMode();
            parReqUpdateMode.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_requestedUpdateMode.getCode()));
            parReqUpdateMode.setParameterValue(new RequestedUpdateMode(pGetParameterOperation.getRequestedUpdateMode()
                    .getCode()))	;
            eeaO.setParReqUpdateMode(parReqUpdateMode);
            break;
        }
        case rocfPN_returnTimeoutPeriod:
        {
            RocfGetParameter.ParReturnTimeout parRetTimeout = new RocfGetParameter.ParReturnTimeout();
            parRetTimeout.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_returnTimeoutPeriod.getCode()));
            parRetTimeout.setParameterValue(new TimeoutPeriod(pGetParameterOperation.getReturnTimeoutPeriod()));
            eeaO.setParReturnTimeout(parRetTimeout);
            break;
        }
        case rocfPN_minReportingCycle:
        {
        	RocfGetParameter.ParMinReportingCycle parMinRepCycle = new RocfGetParameter.ParMinReportingCycle();
            parMinRepCycle.setParameterName(new ParameterName(ROCF_ParameterName.rocfPN_minReportingCycle.getCode()));
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
     * object.
     */
    private void decodeParameterv1To4(RocfGetParameterV1To4 eeaO, IROCF_GetParameter pGetParameterOperation)
    {

        if (eeaO.getParBufferSize() != null)
        {
            pGetParameterOperation.setTransferBufferSize(eeaO.getParBufferSize().getParameterValue().value.longValue());
        }
        else if (eeaO.getParDeliveryMode() != null)
        {
            pGetParameterOperation.setDeliveryMode(ROCF_DeliveryMode
                    .getROCFDeliveryMode(eeaO.getParDeliveryMode().getParameterValue().value.intValue()));

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
        else if (eeaO.getParPermittedGvcidSet() != null)
        {
        	decodeGvcidSetV1To4(eeaO.getParPermittedGvcidSet().getParameterValue(), pGetParameterOperation);
        }
        else if (eeaO.getParPermittedRprtTypeSet() != null)
        {
        	decodePermittedRprtTypeSetV1To4(eeaO.getParPermittedRprtTypeSet().getParameterValue(), pGetParameterOperation);
        }
        else if (eeaO.getParPermittedTcVcidSet() != null)
        {
            decodePermittedTcVcidSet(eeaO.getParPermittedTcVcidSet().getParameterValue(), pGetParameterOperation);
        }
        else if (eeaO.getParPermittedUpdModeSet() != null)
        {
        	decodePermittedUpdateModeSetV1To4(eeaO.getParPermittedUpdModeSet().getParameterValue(), pGetParameterOperation);
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
        else if (eeaO.getParReqGvcId() != null)
        {
            ROCF_Gvcid pGvcId = null;
            if (eeaO.getParReqGvcId().getParameterValue().getGvcid() != null)
            {
                pGvcId = new ROCF_Gvcid();
                decodeRequestedGvcidV1To4(eeaO.getParReqGvcId().getParameterValue(), pGvcId);
            }
            pGetParameterOperation.putRequestedGvcid(pGvcId);
        }
        else if (eeaO.getParReqControlWordType() != null)
        {
            ROCF_ControlWordType rocfControlWordType = ROCF_ControlWordType
                    .getControlWordTypeByCode(eeaO.getParReqControlWordType().getParameterValue().value.intValue());
            pGetParameterOperation.setRequestedControlWordType(rocfControlWordType);
        }
        else if (eeaO.getParReqTcVcid() != null)
        {
            if (eeaO.getParReqTcVcid().getParameterValue().getTcVcid() != null)
            {
                //TcVcid tcVcid = eeaO.getParReqTcVcid().getParameterValue().getTcVcid();
                VcId vcid = eeaO.getParReqTcVcid().getParameterValue().getTcVcid().getTcVcid();
                if (vcid != null)
                {
                    pGetParameterOperation.setRequestedTcVcid(vcid.value.longValue());
                }
            }
        }
        else if (eeaO.getParReqUpdateMode() != null)
        {
            pGetParameterOperation.setRequestedUpdateMode(ROCF_UpdateMode
                    .getROCFUpdateModeByCode(eeaO.getParReqUpdateMode().getParameterValue().value.longValue()));
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
    private void decodeParameter(RocfGetParameter eeaO, IROCF_GetParameter pGetParameterOperation)
    {

        if (eeaO.getParBufferSize() != null)
        {
            pGetParameterOperation.setTransferBufferSize(eeaO.getParBufferSize().getParameterValue().value.longValue());
        }
        else if (eeaO.getParDeliveryMode() != null)
        {
            pGetParameterOperation.setDeliveryMode(ROCF_DeliveryMode
                    .getROCFDeliveryMode(eeaO.getParDeliveryMode().getParameterValue().value.intValue()));

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
        else if (eeaO.getParPermittedGvcidSet() != null)
        {
            decodeGvcidSet(eeaO.getParPermittedGvcidSet().getParameterValue(), pGetParameterOperation);
        }
        else if (eeaO.getParPermittedRprtTypeSet() != null)
        {
            decodePermittedRprtTypeSet(eeaO.getParPermittedRprtTypeSet().getParameterValue(), pGetParameterOperation);
        }
        else if (eeaO.getParPermittedTcVcidSet() != null)
        {
            decodePermittedTcVcidSet(eeaO.getParPermittedTcVcidSet().getParameterValue(), pGetParameterOperation);
        }
        else if (eeaO.getParPermittedUpdModeSet() != null)
        {
            decodePermittedUpdateModeSet(eeaO.getParPermittedUpdModeSet().getParameterValue(), pGetParameterOperation);
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
        else if (eeaO.getParReqGvcId() != null)
        {
            ROCF_Gvcid pGvcId = null;
            if (eeaO.getParReqGvcId().getParameterValue().getVcId() != null)
            {
                pGvcId = new ROCF_Gvcid();
                decodeRequestedGvcid(eeaO.getParReqGvcId().getParameterValue(), pGvcId);
            }
            pGetParameterOperation.putRequestedGvcid(pGvcId);
        }
        else if (eeaO.getParReqControlWordType() != null)
        {
            ROCF_ControlWordType rocfControlWordType = ROCF_ControlWordType
                    .getControlWordTypeByCode(eeaO.getParReqControlWordType().getParameterValue().value.intValue());
            pGetParameterOperation.setRequestedControlWordType(rocfControlWordType);
        }
        else if (eeaO.getParReqTcVcid() != null)
        {
            if (eeaO.getParReqTcVcid().getParameterValue().getTcVcid() != null)
            {
                //TcVcid tcVcid = eeaO.getParReqTcVcid().getParameterValue().getTcVcid();
                VcId tcVcid = eeaO.getParReqTcVcid().getParameterValue().getTcVcid();
                if (tcVcid != null)
                {
                    pGetParameterOperation.setRequestedTcVcid(tcVcid.value.longValue());
                }
            }
        }
        else if (eeaO.getParReqUpdateMode() != null)
        {
            pGetParameterOperation.setRequestedUpdateMode(ROCF_UpdateMode
                    .getROCFUpdateModeByCode(eeaO.getParReqUpdateMode().getParameterValue().value.longValue()));
        }
        else if (eeaO.getParReturnTimeout() != null)
        {
            pGetParameterOperation.setReturnTimeoutPeriod(eeaO.getParReturnTimeout().getParameterValue().value.longValue());
        }
        // New for SLES V5
        else if (eeaO.getParMinReportingCycle() != null)
        {
        	pGetParameterOperation.setMinimumReportingCycle(eeaO.getParMinReportingCycle().getParameterValue().value.longValue());
        }
    }

    /**
     * Fills the ROCF global VCID list of the Asn1 object.
     */
    private void encodeGvcidSetV1To4(IROCF_GetParameter pGetParameterOperation, GvcIdSetV1To4 eeaO) 
    {
        ROCF_Gvcid[] pGvcidList = pGetParameterOperation.getPermittedGvcidSet();
        List<PXDEL_Gvcid> delGvcidList = new ArrayList<PXDEL_Gvcid>();

        PXDEL_Gvcid delGvcid = null;

        // build a list which contains all the information organized to be
        // easily encoded
        for (ROCF_Gvcid pGvcid : pGvcidList)
        {
            boolean exist = false;
            if (pGvcid.getType() == ROCF_ChannelType.rocfCT_VirtualChannel)
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

            if (!exist)
            {
                // create a new object
                delGvcid = new PXDEL_Gvcid();
                delGvcid.getVcid().clear();
                // fill the new object
                if (pGvcid.getType() == ROCF_ChannelType.rocfCT_MasterChannel)
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

            if (!delGvcid.isMasterChannel())
            {
                // add vc to the vc list
                delGvcid.getVcid().add((long) pGvcid.getVcid());
            }
        }

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
                    VcId vcEntry = new VcId(list.get(0));
                    delGvcid.getVcid().remove(list.get(0));
                    vcList.getVcId().add(vcEntry);
                    
                }
                MasterChannelCompositionV1To4.McOrVcList mcvclist = new MasterChannelCompositionV1To4.McOrVcList();
                mcvclist.setVcList(vcList);
                pmcc.setMcOrVcList(mcvclist);
            }
            eeaO.getMasterChannelCompositionV1To4().add(pmcc);
        }
    }
    
    /**
     * Fills the ROCF global VCID list of the Asn1 object.
     */
    private void encodeGvcidSet(IROCF_GetParameter pGetParameterOperation, GvcIdSet eeaO) 
    {
        ROCF_Gvcid[] pGvcidList = pGetParameterOperation.getPermittedGvcidSet();
        List<PXDEL_Gvcid> delGvcidList = new ArrayList<PXDEL_Gvcid>();

        PXDEL_Gvcid delGvcid = null;

        // build a list which contains all the information organized to be
        // easily encoded
        for (ROCF_Gvcid pGvcid : pGvcidList)
        {
            boolean exist = false;
            if (pGvcid.getType() == ROCF_ChannelType.rocfCT_VirtualChannel)
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

            if (!exist)
            {
                // create a new object
                delGvcid = new PXDEL_Gvcid();
                delGvcid.getVcid().clear();
                // fill the new object
                if (pGvcid.getType() == ROCF_ChannelType.rocfCT_MasterChannel)
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

            if (!delGvcid.isMasterChannel())
            {
                // add vc to the vc list
                delGvcid.getVcid().add((long) pGvcid.getVcid());
            }
        }

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
                    VcId vcEntry = new VcId(list.get(0));
                    delGvcid.getVcid().remove(list.get(0));
                    vcList.getVcId().add(vcEntry);
                    
                }
                McOrVcList mcvclist = new McOrVcList();
                mcvclist.setVcList(vcList);
                pmcc.setMcOrVcList(mcvclist);
            }
            //os.write(pmcc.code);
            eeaO.getMasterChannelComposition().add(pmcc);
            
        }
    }
    /**
     * Fills the ROCF global VCID list from the Asn1 object.
     */
    private void decodeGvcidSetV1To4(GvcIdSetV1To4 eeaO, IROCF_GetParameter pGetParameterOperation)
    {
        int nbelem = 0;
        ROCF_Gvcid pGvcid;

        // calculate the total number rocf gvcid
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

        ROCF_Gvcid[] pGvcidList = new ROCF_Gvcid[nbelem];

        for (int i = 0; i < nbelem; i++)
        {
            pGvcidList[i] = new ROCF_Gvcid();
        }
        // for all the master channel composition
        nbelem = 0;
        for (MasterChannelCompositionV1To4 pmcc : eeaO.getMasterChannelCompositionV1To4())
        {
            if (pmcc != null)
            {
                if (pmcc.getMcOrVcList().getMasterChannel() != null)
                {
                    pGvcid = pGvcidList[nbelem++];
                    pGvcid.setScid(pmcc.getSpacecraftId().value.intValue());
                    pGvcid.setVersion(pmcc.getVersionNumber().value.intValue());
                    pGvcid.setType(ROCF_ChannelType.rocfCT_MasterChannel);
                    pGvcid.setVcid(0);
                }
                else
                {
                	MasterChannelCompositionV1To4.McOrVcList vcList = pmcc.getMcOrVcList();
                    // for all the vc
                    for (VcId vc : vcList.getVcList().getVcId())
                    {
                        pGvcid = pGvcidList[nbelem++];
                        pGvcid.setScid(pmcc.getSpacecraftId().value.intValue());
                        pGvcid.setVersion(pmcc.getVersionNumber().value.intValue());
                        pGvcid.setType(ROCF_ChannelType.rocfCT_VirtualChannel);
                        pGvcid.setVcid(vc.value.intValue());
                    }
                }
            }
        }

        pGetParameterOperation.putPermittedGvcidSet(pGvcidList);
    }
    
    /**
     * Fills the ROCF global VCID list from the Asn1 object.
     */
    private void decodeGvcidSet(GvcIdSet eeaO, IROCF_GetParameter pGetParameterOperation)
    {
        int nbelem = 0;
        ROCF_Gvcid pGvcid;

        // calculate the total number rocf gvcid
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

        ROCF_Gvcid[] pGvcidList = new ROCF_Gvcid[nbelem];

        for (int i = 0; i < nbelem; i++)
        {
            pGvcidList[i] = new ROCF_Gvcid();
        }
        // for all the master channel composition
        nbelem = 0;
        for (MasterChannelComposition pmcc : eeaO.getMasterChannelComposition())
        {
            if (pmcc != null)
            {
                if (pmcc.getMcOrVcList().getMasterChannel() != null)
                {
                    pGvcid = pGvcidList[nbelem++];
                    pGvcid.setScid(pmcc.getSpacecraftId().value.intValue());
                    pGvcid.setVersion(pmcc.getVersionNumber().value.intValue());
                    pGvcid.setType(ROCF_ChannelType.rocfCT_MasterChannel);
                    pGvcid.setVcid(0);
                }
                else
                {
                    McOrVcList vcList = pmcc.getMcOrVcList();
                    // for all the vc
                    for (VcId vc : vcList.getVcList().getVcId())
                    {
                        pGvcid = pGvcidList[nbelem++];
                        pGvcid.setScid(pmcc.getSpacecraftId().value.intValue());
                        pGvcid.setVersion(pmcc.getVersionNumber().value.intValue());
                        pGvcid.setType(ROCF_ChannelType.rocfCT_VirtualChannel);
                        pGvcid.setVcid(vc.value.intValue());
                    }
                }
            }
        }

        pGetParameterOperation.putPermittedGvcidSet(pGvcidList);
    }

    /**
     * Fills the ROCF requested global VCID of the Asn1 object.
     */
    private void encodeRequestedGvcidV1To4(ROCF_Gvcid gvcid, RequestedGvcIdV1To4 eeaO)
    {
        if (gvcid != null)
        {
            GvcId gVcid = new GvcId();
            gVcid.setSpacecraftId(new BerInteger(gvcid.getScid()));
            gVcid.setVersionNumber(new BerInteger(gvcid.getVersion()));
            switch (gvcid.getType())
            {
            case rocfCT_MasterChannel:
            {
            	GvcId.VcId vcId = new GvcId.VcId();
            	vcId.setMasterChannel(new BerNull());
            	gVcid.setVcId(vcId);
                break;
            }
            case rocfCT_VirtualChannel:
            {
                VcId vc = new VcId(gvcid.getVcid());
                GvcId.VcId vcId = new GvcId.VcId();
                vcId.setVirtualChannel(vc);
                gVcid.setVcId(vcId);
                break;
            }
            default:
            {
                gVcid.setVcId(new GvcId.VcId());
                break;
            }
            }   
            eeaO.setGvcid(gVcid);
        }
    }

    /**
     * Fills the ROCF requested global VCID of the Asn1 object.
     */
    private void encodeRequestedGvcid(ROCF_Gvcid gvcid, RequestedGvcId eeaO)
    {
        if (gvcid != null)
        {
        	eeaO.setSpacecraftId(new BerInteger(gvcid.getScid()));
        	eeaO.setVersionNumber(new BerInteger(gvcid.getVersion()));
            switch (gvcid.getType())
            {
            case rocfCT_MasterChannel:
            {
            	GvcId.VcId mc = new GvcId.VcId();
            	mc.setMasterChannel(new BerNull());               
            	eeaO.setVcId(mc);;
                break;
            }
            case rocfCT_VirtualChannel:
            {
                VcId vc = new VcId(gvcid.getVcid());
                GvcId.VcId gvcId = new GvcId.VcId();
                gvcId.setVirtualChannel(vc);
                eeaO.setVcId(gvcId);
                break;
            }
            default:
            {
                eeaO.setVcId(new GvcId.VcId());
                break;
            }
            }   
        }
    }
 
    /**
     * Decodes the ROCF requested global VCID of the Asn1 object.
     */
    private void decodeRequestedGvcidV1To4(RequestedGvcIdV1To4 eeaO, ROCF_Gvcid gvcid)
    {
        if (eeaO.getGvcid().getVcId() != null)
        {
        	
            gvcid.setScid(eeaO.getGvcid().getSpacecraftId().value.intValue());
            gvcid.setVersion(eeaO.getGvcid().getVersionNumber().value.intValue());
            if (eeaO.getGvcid().getVcId().getMasterChannel() != null)
            {
                gvcid.setType(ROCF_ChannelType.rocfCT_MasterChannel);
                gvcid.setVcid(0);
            }
            else if (eeaO.getGvcid().getVcId().getVirtualChannel() != null)
            {
                gvcid.setType(ROCF_ChannelType.rocfCT_VirtualChannel);
                gvcid.setVcid(eeaO.getGvcid().getVcId().getVirtualChannel().value.intValue());
            }
            else
            {
                gvcid.setType(ROCF_ChannelType.rocfCT_invalid);
            }
        }
    }
    
    /**
     * Decodes the ROCF requested global VCID of the Asn1 object.
     */
    private void decodeRequestedGvcid(RequestedGvcId eeaO, ROCF_Gvcid gvcid)
    {
        if (eeaO.getVcId() != null)
        {
            gvcid.setScid(eeaO.getSpacecraftId().value.intValue());
            gvcid.setVersion(eeaO.getVersionNumber().value.intValue());
            if (eeaO.getVcId().getMasterChannel() != null)
            {
                gvcid.setType(ROCF_ChannelType.rocfCT_MasterChannel);
                gvcid.setVcid(0);
            }
            else if (eeaO.getVcId().getVirtualChannel() != null)
            {
                gvcid.setType(ROCF_ChannelType.rocfCT_VirtualChannel);
                gvcid.setVcid(eeaO.getVcId().getVirtualChannel().value.intValue());
            }
            else
            {
                gvcid.setType(ROCF_ChannelType.rocfCT_invalid);
            }
        }
    }

    private void encodeTcVcidSet(IROCF_GetParameter pGetParameterOperation, TcVcidSet eeaO)
    {
        long[] pTcVcid = pGetParameterOperation.getPermittedTcVcidSet();
        if (pTcVcid.length != 0)
        {
            TcVcids tcVcid = new TcVcids();
            for (long i : pTcVcid)
            {
                VcId e = new VcId(i);
                tcVcid.getVcId().add(e);
            }
            eeaO.setTcVcids(tcVcid);
        }
        else
        {
            eeaO.setNoTcVC(new BerNull());
        }
    }

    private void decodePermittedTcVcidSet(TcVcidSet eeaO, IROCF_GetParameter pGetParameterOperation)
    {
        if (eeaO.getTcVcids() != null)
        {
            int count = 0;
            int size = eeaO.getTcVcids().getVcId().size();
            long[] pTcVcid = null;

            if (size > 0)
            {
                pTcVcid = new long[size];
                for (VcId tcvc : eeaO.getTcVcids().getVcId())
                {
                    pTcVcid[count++] = tcvc.value.longValue();
                }
            }

            pGetParameterOperation.putPermittedTcVcidSet(pTcVcid);
        }
    }

    private void encodePermittedUpdateModeSetV1To4(IROCF_GetParameter pGetParameterOperation,
            RocfGetParameterV1To4.ParPermittedUpdModeSet.ParameterValue eeaO)
    {
    	ROCF_UpdateMode[] pUpdateMode = pGetParameterOperation.getPermittedUpdateModeSet();
    	for (ROCF_UpdateMode i : pUpdateMode)
    	{
    		UpdateMode e = new UpdateMode(i.getCode());
    		//eeaO.seqOf.add(e);
    		eeaO.getUpdateMode().add(e);
    	}
	}

    
    private void encodePermittedUpdateModeSet(IROCF_GetParameter pGetParameterOperation,
                                              RocfGetParameter.ParPermittedUpdModeSet.ParameterValue eeaO)
    {
        ROCF_UpdateMode[] pUpdateMode = pGetParameterOperation.getPermittedUpdateModeSet();
        for (ROCF_UpdateMode i : pUpdateMode)
        {
            UpdateMode e = new UpdateMode(i.getCode());
            //eeaO.seqOf.add(e);
            eeaO.getUpdateMode().add(e);
        }
    }

    
    private void decodePermittedUpdateModeSetV1To4(RocfGetParameterV1To4.ParPermittedUpdModeSet.ParameterValue eeaO,
            IROCF_GetParameter pGetParameterOperation)
    {
    	ROCF_UpdateMode[] pUpdateMode = null;
    	if (!eeaO.getUpdateMode().isEmpty())
    	{
    		int size = eeaO.getUpdateMode().size();
    		pUpdateMode = new ROCF_UpdateMode[size];
    		int count = 0;
    		for (UpdateMode um : eeaO.getUpdateMode())
    		{
    			pUpdateMode[count++] = ROCF_UpdateMode.getROCFUpdateModeByCode(um.value.longValue());
    		}

    		pGetParameterOperation.putPermittedUpdateModeSet(pUpdateMode);
    	}
    }   

    private void decodePermittedUpdateModeSet(RocfGetParameter.ParPermittedUpdModeSet.ParameterValue eeaO,
                                              IROCF_GetParameter pGetParameterOperation)
    {
        ROCF_UpdateMode[] pUpdateMode = null;
        if (!eeaO.getUpdateMode().isEmpty())
        {
            int size = eeaO.getUpdateMode().size();
            pUpdateMode = new ROCF_UpdateMode[size];
            int count = 0;
            for (UpdateMode um : eeaO.getUpdateMode())
            {
                pUpdateMode[count++] = ROCF_UpdateMode.getROCFUpdateModeByCode(um.value.longValue());
            }

            pGetParameterOperation.putPermittedUpdateModeSet(pUpdateMode);
        }
    }

    private void decodePermittedRprtTypeSetV1To4(RocfGetParameterV1To4.ParPermittedRprtTypeSet.ParameterValue eeaO, IROCF_GetParameter pGetParameterOperation)
    {
        ROCF_ControlWordType[] pControlWordType = null;
        if (!eeaO.getControlWordTypeNumber().isEmpty())
        {
            pControlWordType = new ROCF_ControlWordType[eeaO.getControlWordTypeNumber().size()];
            int count = 0;
            for (ControlWordTypeNumber cwtn : eeaO.getControlWordTypeNumber())
            {
                pControlWordType[count++] = ROCF_ControlWordType.getControlWordTypeByCode(cwtn.value.intValue());
            }
        }

        pGetParameterOperation.putPermittedControlWordTypeSet(pControlWordType);
    }
    
    private void decodePermittedRprtTypeSet(ParPermittedRprtTypeSet.ParameterValue eeaO, IROCF_GetParameter pGetParameterOperation)
    {
        ROCF_ControlWordType[] pControlWordType = null;
        if (!eeaO.getControlWordTypeNumber().isEmpty())
        {
            pControlWordType = new ROCF_ControlWordType[eeaO.getControlWordTypeNumber().size()];
            int count = 0;
            for (ControlWordTypeNumber cwtn : eeaO.getControlWordTypeNumber())
            {
                pControlWordType[count++] = ROCF_ControlWordType.getControlWordTypeByCode(cwtn.value.intValue());
            }
        }

        pGetParameterOperation.putPermittedControlWordTypeSet(pControlWordType);
    }

    private void encodeControlWordTypeSetV1To4(IROCF_GetParameter pGetParameterOperation, RocfGetParameterV1To4.ParPermittedRprtTypeSet.ParameterValue eeaO)
    {
        ROCF_ControlWordType[] pControlWordtype = pGetParameterOperation.getPermittedControlWordTypeSet();
        for (ROCF_ControlWordType i : pControlWordtype)
        {
            ControlWordTypeNumber e = new ControlWordTypeNumber(i.getCode());
            eeaO.getControlWordTypeNumber().add(e);
        }
    }
    private void encodeControlWordTypeSet(IROCF_GetParameter pGetParameterOperation, ParPermittedRprtTypeSet.ParameterValue eeaO)
    {
        ROCF_ControlWordType[] pControlWordtype = pGetParameterOperation.getPermittedControlWordTypeSet();
        for (ROCF_ControlWordType i : pControlWordtype)
        {
            ControlWordTypeNumber e = new ControlWordTypeNumber(i.getCode());
            eeaO.getControlWordTypeNumber().add(e);
        }
    }
}
