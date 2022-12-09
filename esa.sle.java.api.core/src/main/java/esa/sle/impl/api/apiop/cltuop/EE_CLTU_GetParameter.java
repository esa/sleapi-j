/**
 * @(#) EE_CLTU_GetParameter.java
 */

package esa.sle.impl.api.apiop.cltuop;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_DeliveryMode;
import ccsds.sle.api.isle.it.SLE_DiagnosticType;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isrv.icltu.ICLTU_GetParameter;
import ccsds.sle.api.isrv.icltu.types.CLTU_ChannelType;
import ccsds.sle.api.isrv.icltu.types.CLTU_ClcwGvcId;
import ccsds.sle.api.isrv.icltu.types.CLTU_GetParameterDiagnostic;
import ccsds.sle.api.isrv.icltu.types.CLTU_ConfType;
import ccsds.sle.api.isrv.icltu.types.CLTU_NotificationMode;
import ccsds.sle.api.isrv.icltu.types.CLTU_ParameterName;
import ccsds.sle.api.isrv.icltu.types.CLTU_ClcwPhysicalChannel;
import ccsds.sle.api.isrv.icltu.types.CLTU_PlopInEffect;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProtocolAbortMode;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_ConfirmedOperation;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * The class implements the CLTU specific GetParameter operation.
 */
public class EE_CLTU_GetParameter extends IEE_SLE_ConfirmedOperation implements ICLTU_GetParameter
{

    /**
     * The requested parameter.
     */
    private CLTU_ParameterName requestedParameter = CLTU_ParameterName.cltuPN_invalid;

    /**
     * The return parameter.
     */
    private CLTU_ParameterName returnedParameter = CLTU_ParameterName.cltuPN_invalid;

    /**
     * The value of the parameter "bit lock required".
     */
    private SLE_YesNo bitLockRequired = SLE_YesNo.sleYN_invalid;

    /**
     * The delivery mode.
     */
    private SLE_DeliveryMode deliveryMode = SLE_DeliveryMode.sleDM_invalid;

    /**
     * The next expected CLTU identification.
     */
    private long expectedCLTUID = 0;

    /**
     * The next expected event invocation identifier.
     */
    private long expectedEventInvocationID = 0;

    /**
     * The maximum length in bytes of a CLTU.
     */
    private long maxSlduLength = 0;

    /**
     * The modulation frequency.
     */
    private long modFrequency = 0;

    /**
     * The modulation index.
     */
    private int modIndex = 0;

    /**
     * The PLOP used.
     */
    private CLTU_PlopInEffect plopInEffect = CLTU_PlopInEffect.cltuPIE_invalid;

    /**
     * The reporting cycle.
     */
    private long reportingCycle = 0;

    /**
     * The return timeout period.
     */
    private long returnTimeoutPeriod = 0;

    /**
     * The value of the parameter "RF available required".
     */
    private SLE_YesNo rfAvailableRequired = SLE_YesNo.sleYN_invalid;

    /**
     * The value of the parameter "sub-carrier to bit-rate ratio".
     */
    private int subCarrierToBitRateRatio;

    /**
     * The CLTU GetParameter diagnostic.
     */
    private CLTU_GetParameterDiagnostic parameterDiagnostic = CLTU_GetParameterDiagnostic.cltuGP_invalid;

    private int acquisitionSequenceLength;

    private int plop1IdleSequenceLength;

    private CLTU_ProtocolAbortMode protocolAbortMode;

    private CLTU_NotificationMode notificationMode;

    //private CLTU_GvcId clcwGlobalVcid;
    private CLTU_ClcwGvcId clcwGlobalVcid;

    //private String clcwPhysicalChannel;
    private CLTU_ClcwPhysicalChannel clcwPhysicalChannel;

    private long minimumDelayTime;
    
    /**
     * minimumReportingCycle type long, integrated since SLES V5
     */
    private long minimumReportingCycle;


    private EE_CLTU_GetParameter(final EE_CLTU_GetParameter right)
    {
        super(right);
        this.requestedParameter = right.requestedParameter;
        this.returnedParameter = right.returnedParameter;
        this.bitLockRequired = right.bitLockRequired;
        this.deliveryMode = right.deliveryMode;
        this.expectedCLTUID = right.expectedCLTUID;
        this.expectedEventInvocationID = right.expectedEventInvocationID;
        this.maxSlduLength = right.maxSlduLength;
        this.modFrequency = right.modFrequency;
        this.modIndex = right.modIndex;
        this.plopInEffect = right.plopInEffect;
        this.reportingCycle = right.reportingCycle;
        this.returnTimeoutPeriod = right.returnTimeoutPeriod;
        this.rfAvailableRequired = right.rfAvailableRequired;
        this.subCarrierToBitRateRatio = right.subCarrierToBitRateRatio;
        this.parameterDiagnostic = right.parameterDiagnostic;
        this.acquisitionSequenceLength = right.acquisitionSequenceLength;
        this.plop1IdleSequenceLength = right.plop1IdleSequenceLength;
        this.protocolAbortMode = right.protocolAbortMode;
        this.notificationMode = right.notificationMode;
        this.clcwGlobalVcid = right.clcwGlobalVcid;
        this.clcwPhysicalChannel = right.clcwPhysicalChannel;
        this.minimumDelayTime = right.minimumDelayTime;
        this.minimumReportingCycle = right.minimumReportingCycle;
        
    }

    /**
     * Creator of the CLTU GetParameter Operation.
     */
    public EE_CLTU_GetParameter(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_fwdCltu, SLE_OpType.sleOT_getParameter, version, preporter);
        this.requestedParameter = CLTU_ParameterName.cltuPN_invalid;
        this.returnedParameter = CLTU_ParameterName.cltuPN_invalid;
        this.bitLockRequired = SLE_YesNo.sleYN_invalid;
        this.deliveryMode = SLE_DeliveryMode.sleDM_invalid;
        this.expectedCLTUID = 0;
        this.expectedEventInvocationID = 0;
        this.maxSlduLength = 0;
        this.modFrequency = 0;
        this.modIndex = 0;
        this.plopInEffect = CLTU_PlopInEffect.cltuPIE_invalid;
        this.reportingCycle = 0;
        this.returnTimeoutPeriod = 0;
        this.rfAvailableRequired = SLE_YesNo.sleYN_invalid;
        this.subCarrierToBitRateRatio = 0;
        this.parameterDiagnostic = CLTU_GetParameterDiagnostic.cltuGP_invalid;
        this.acquisitionSequenceLength = 65535;
        this.plop1IdleSequenceLength = 65535;
        this.protocolAbortMode = CLTU_ProtocolAbortMode.cltuPAM_invalid;
        this.notificationMode = CLTU_NotificationMode.cltuNM_invalid;
        this.clcwGlobalVcid = null;
        this.clcwPhysicalChannel = null;
        this.minimumDelayTime = 0;
        this.minimumReportingCycle = 0;
    }

    /**
     * specification of ICLTU_GetParameter.
     */
    @Override
    public synchronized CLTU_ParameterName getRequestedParameter()
    {
        return this.requestedParameter;
    }

    /**
     * @FunctionSee specification of ICLTU_GetParameter.@EndFunction
     */
    @Override
    public synchronized CLTU_ParameterName getReturnedParameter()
    {
        return this.returnedParameter;
    }

    /**
     * @FunctionSee specification of ICLTU_GetParameter.@EndFunction
     */
    @Override
    public synchronized SLE_YesNo getBitLockRequired()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_bitLockRequired) : "precond";
        return this.bitLockRequired;
    }

    /**
     * See specification of ICLTU_GetParameter.
     */
    @Override
    public synchronized SLE_DeliveryMode getDeliveryMode()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_deliveryMode) : "precond";
        return this.deliveryMode;
    }

    /**
     * See specification of ICLTU_GetParameter
     */
    @Override
    public synchronized long getExpectedCltuId()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_expectedSlduIdentification) : "precond";
        return this.expectedCLTUID;
    }

    /**
     * @FunctionSee specification of ICLTU_GetParameter.@EndFunction
     */
    @Override
    public synchronized long getExpectedEventInvocationId()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_expectedEventInvocationId) : "check";
        return this.expectedEventInvocationID;
    }

    /**
     * @FunctionSee specification of ICLTU_GetParameter.@EndFunction
     */
    @Override
    public synchronized long getMaximumSlduLength()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_maximumSlduLength) : "precond";
        return this.maxSlduLength;
    }

    /**
     * @FunctionSee specification of ICLTU_GetParameter.@EndFunction
     */
    @Override
    public synchronized long getModulationFrequency()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_modulationFrequency) : "precond";
        return this.modFrequency;
    }

    /**
     * @FunctionSee specification of ICLTU_GetParameter.@EndFunction
     */
    @Override
    public synchronized int getModulationIndex()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_modulationIndex) : "precond";
        return this.modIndex;
    }

    /**
     * @FunctionSee specification of ICLTU_GetParameter.@EndFunction
     */
    @Override
    public synchronized CLTU_PlopInEffect getPlopInEffect()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_plopInEffect) : "precond";
        return this.plopInEffect;
    }

    /**
     * @FunctionSee specification of ICLTU_GetParameter.@EndFunction
     */
    @Override
    public synchronized long getReportingCycle()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_reportingCycle) : "precond";
        return this.reportingCycle;
    }

    /**
     * @FunctionSee specification of ICLTU_GetParameter.@EndFunction
     */
    @Override
    public synchronized long getReturnTimeoutPeriod()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_returnTimeoutPeriod) : "precond";
        return this.returnTimeoutPeriod;
    }

    /**
     * See specification of ICLTU_GetParameter
     */
    @Override
    public synchronized SLE_YesNo getRfAvailableRequired()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_rfAvailableRequired) : "precond";
        return this.rfAvailableRequired;
    }

    /**
     * See specification of ICLTU_GetParameter.
     */
    @Override
    public synchronized int getSubcarrierToBitRateRatio()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_subcarrierToBitRateRatio) : "precond";
        return this.subCarrierToBitRateRatio;
    }

    /**
     * See specification of ICLTU_GetParameter.
     */
    @Override
    public synchronized CLTU_GetParameterDiagnostic getGetParameterDiagnostic()
    {
        assert (getResult() == SLE_Result.sleRES_negative && getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics) : "precond";
        return this.parameterDiagnostic;
    }

    /**
     * See specification of ICLTU_GetParameter.
     */
    @Override
    public synchronized void setRequestedParameter(CLTU_ParameterName name)
    {
        this.requestedParameter = name;
    }

    /**
     * @FunctionSee specification of ICLTU_GetParameter.@EndFunction
     */
    @Override
    public synchronized void setBitLockRequired(SLE_YesNo yesno)
    {
        this.bitLockRequired = yesno;
        this.returnedParameter = CLTU_ParameterName.cltuPN_bitLockRequired;
    }

    /**
     * See specification of ICLTU_GetParameter.
     */
    @Override
    public synchronized void setDeliveryMode()
    {
        this.returnedParameter = CLTU_ParameterName.cltuPN_deliveryMode;
        this.deliveryMode = SLE_DeliveryMode.sleDM_fwdOnline;
    }

    /**
     * See specification of ICLTU_GetParameter.
     */
    @Override
    public synchronized void setExpectedCltuId(long id)
    {
        this.expectedCLTUID = id;
        this.returnedParameter = CLTU_ParameterName.cltuPN_expectedSlduIdentification;
    }

    /**
     * @FunctionSee specification of ICLTU_GetParameter.@EndFunction
     */
    @Override
    public synchronized void setExpectedEventInvocationId(long id)
    {
        this.expectedEventInvocationID = id;
        this.returnedParameter = CLTU_ParameterName.cltuPN_expectedEventInvocationId;
    }

    /**
     * @FunctionSee specification of ICLTU_GetParameter.@EndFunction
     */
    @Override
    public synchronized void setMaximumSlduLength(long length)
    {
        this.maxSlduLength = length;
        this.returnedParameter = CLTU_ParameterName.cltuPN_maximumSlduLength;
    }

    /**
     * See specification of ICLTU_GetParameter.
     */
    @Override
    public synchronized void setModulationFrequency(long frequency)
    {
        this.modFrequency = frequency;
        this.returnedParameter = CLTU_ParameterName.cltuPN_modulationFrequency;
    }

    /**
     * @FunctionSee specification of ICLTU_GetParameter.@EndFunction
     */
    @Override
    public synchronized void setModulationIndex(int index)
    {
        this.modIndex = index;
        this.returnedParameter = CLTU_ParameterName.cltuPN_modulationIndex;
    }

    /**
     * See specification of ICLTU_GetParameter.
     */
    @Override
    public synchronized void setPlopInEffect(CLTU_PlopInEffect plop)
    {
        this.plopInEffect = plop;
        this.returnedParameter = CLTU_ParameterName.cltuPN_plopInEffect;
    }

    /**
     * @FunctionSee specification of ICLTU_GetParameter.@EndFunction
     */
    @Override
    public synchronized void setReportingCycle(long cycle)
    {
        this.reportingCycle = cycle;
        this.returnedParameter = CLTU_ParameterName.cltuPN_reportingCycle;
    }

    /**
     * @FunctionSee specification of ICLTU_GetParameter.@EndFunction
     */
    @Override
    public synchronized void setReturnTimeoutPeriod(long period)
    {
        this.returnedParameter = CLTU_ParameterName.cltuPN_returnTimeoutPeriod;
        this.returnTimeoutPeriod = period;
    }

    /**
     * See specification of ICLTU_GetParameter.
     */
    @Override
    public synchronized void setRfAvailableRequired(SLE_YesNo yesno)
    {
        this.rfAvailableRequired = yesno;
        this.returnedParameter = CLTU_ParameterName.cltuPN_rfAvailableRequired;
    }

    /**
     * See specification of ICLTU_GetParameter.
     */
    @Override
    public synchronized void setSubcarrierToBitRateRatio(int divisor)
    {
        this.returnedParameter = CLTU_ParameterName.cltuPN_subcarrierToBitRateRatio;
        this.subCarrierToBitRateRatio = divisor;
    }

    /**
     * @FunctionSee specification of ICLTU_GetParameter.@EndFunction
     */
    @Override
    public synchronized void setGetParameterDiagnostic(CLTU_GetParameterDiagnostic diagnostic)
    {
        setSpecificDiagnostics();
        this.parameterDiagnostic = diagnostic;
    }

    /**
     * @throws SleApiException See specification of ISLE_Operation.@EndFunction
     */
    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        super.verifyInvocationArguments();
        if (this.requestedParameter == CLTU_ParameterName.cltuPN_invalid)
        {
            throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
        }
    }

    /**
     * See specification of ISLE_Operation
     */
    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_CLTU_GetParameter ptmp = new EE_CLTU_GetParameter(this);
        return ptmp;
    }

    /**
     * See specification of ISLE_Operation
     */
    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder("");
        printOn(oss, maxDumpLength);
        oss.append("Requested parameter         : " + this.requestedParameter + "\n");
        oss.append("Returned parameter          : " + this.returnedParameter + "\n");
        oss.append("Bit lock required           : " + this.bitLockRequired + "\n");
        oss.append("Delivery mode               : " + this.deliveryMode + "\n");
        oss.append("Expected CLTUID             : " + this.expectedCLTUID + "\n");
        oss.append("Expected evt invoke id      : " + this.expectedEventInvocationID + "\n");
        oss.append("Maximum Sldu length         : " + this.maxSlduLength + "\n");
        oss.append("Minimum Reporting Cycle     : " + this.minimumReportingCycle + "\n");
        oss.append("Modulation frequency        : " + this.modFrequency + "\n");
        oss.append("Modulation index            : " + this.modIndex + "\n");
        oss.append("PLOP in effect              : " + this.plopInEffect + "\n");
        oss.append("Reporting cycle             : " + this.reportingCycle + "\n");
        oss.append("Return timeout period       : " + this.returnTimeoutPeriod + "\n");
        oss.append("Rf Available required       : " + this.rfAvailableRequired + "\n");
        oss.append("SubCarr. bitrate ratio      : " + this.subCarrierToBitRateRatio + "\n");
        oss.append("Parameter diagnostic        : " + this.parameterDiagnostic + "\n");
        oss.append("Acquisition Sequence Length : " + this.acquisitionSequenceLength + "\n");
        oss.append("Plop1 Idle Sequence Length  : " + this.plop1IdleSequenceLength + "\n");
        oss.append("Protocol Abort Mode         : " + this.protocolAbortMode.toString() + "\n");
        oss.append("Notification Mode           : " + this.notificationMode.toString() + "\n");
        if (this.clcwGlobalVcid != null && this.clcwGlobalVcid.getCltuGvcId() != null)
        {
            oss.append("CLCW Global VC type         : " + this.clcwGlobalVcid.getCltuGvcId().getType().toString() + "\n");
            oss.append("CLCW Global VC scid         : " + this.clcwGlobalVcid.getCltuGvcId().getScid() + "\n");
            oss.append("CLCW Global VC version      : " + this.clcwGlobalVcid.getCltuGvcId().getVersion() + "\n");
            oss.append("CLCW Global VC channel ID   : " + this.clcwGlobalVcid.getCltuGvcId().getVcid() + "\n");
        }
        else
        {
            oss.append("CLCW Global VC              : Not initialised" + "\n");
        }
        if (this.clcwPhysicalChannel != null && this.clcwPhysicalChannel.getCltuPhyChannel() != null)
        {
            oss.append("CLCW Physical Channel       : " + this.clcwPhysicalChannel.getCltuPhyChannel() + "\n");
        }
        else 
        {
            oss.append("CLCW Physical Channel       : Not configured " + "\n");
        }
        oss.append("Minimum Delay Time          : " + this.minimumDelayTime + "\n");
        String ret = oss.toString();
        return ret;
    }

    @Override
    public synchronized void verifyReturnArguments() throws SleApiException
    {
        super.verifyReturnArguments();
        if (this.returnedParameter != this.requestedParameter)
        {
            throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
        }
        switch (this.returnedParameter)
        {
        case cltuPN_bitLockRequired:
            if (this.bitLockRequired == SLE_YesNo.sleYN_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case cltuPN_deliveryMode:
            if (this.deliveryMode != SLE_DeliveryMode.sleDM_fwdOnline)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case cltuPN_maximumSlduLength:
            if (this.maxSlduLength == 0)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case cltuPN_modulationIndex:
            if (this.modIndex == 0)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case cltuPN_plopInEffect:
            if (this.plopInEffect == CLTU_PlopInEffect.cltuPIE_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case cltuPN_returnTimeoutPeriod:
            if (this.returnTimeoutPeriod == 0)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case cltuPN_rfAvailableRequired:
            if (this.rfAvailableRequired == SLE_YesNo.sleYN_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case cltuPN_subcarrierToBitRateRatio:
            if (this.subCarrierToBitRateRatio == 0)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case cltuPN_acquisitionSequenceLength:
            if (this.acquisitionSequenceLength > 65535)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case cltuPN_plop1IdleSequenceLength:
            if (this.plop1IdleSequenceLength > 65535)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case cltuPN_protocolAbortMode:
            if (this.protocolAbortMode == CLTU_ProtocolAbortMode.cltuPAM_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case cltuPN_notificationMode:
            if (this.notificationMode == CLTU_NotificationMode.cltuNM_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case cltuPN_clcwGlobalVcid:
            if (this.clcwGlobalVcid != null)
            {
                if (this.clcwGlobalVcid.getConfigType() == CLTU_ConfType.cltuCT_invalid)
                {
                    throw new SleApiException(logAlarm(HRESULT.SLE_E_INVALIDPDU,
                                                       EE_LogMsg.EE_OP_LM_InvalidID.getCode(),
                                                       "Global VCID type"));
                }
                if(this.clcwGlobalVcid.getCltuGvcId() == null)
                {
                	throw new SleApiException(logAlarm(HRESULT.SLE_E_INVALIDPDU,
                            EE_LogMsg.EE_OP_LM_InvalidID.getCode(),
                            "Global VCID type"));
                }
                if (this.clcwGlobalVcid.getCltuGvcId().getVersion() == 0)
                {
                    // This is the PTM supported version, which permits 0 .. 1023 (10 bits)
                    if ((this.clcwGlobalVcid.getCltuGvcId().getScid() < 0) || (this.clcwGlobalVcid.getCltuGvcId().getScid() > 1023))
                    {
                        throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                           EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                           "Spacecraft ID",
                                                           "0..1023"));
                    }
                    if (this.clcwGlobalVcid.getCltuGvcId().getType() == CLTU_ChannelType.cltuCT_VirtualChannel)
                    {
                        if ((this.clcwGlobalVcid.getCltuGvcId().getVcid() < 0) || (this.clcwGlobalVcid.getCltuGvcId().getVcid() > 7))
                        {
                            throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                               EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                               "VC ID",
                                                               "0..7"));
                        }
                    }
                }
                else if (this.clcwGlobalVcid.getCltuGvcId().getVersion() == 1)
                {
                	// This is the AOS suported version which permits 0 .. 255 (8 bits)
                    if ((this.clcwGlobalVcid.getCltuGvcId().getScid() < 0) || (this.clcwGlobalVcid.getCltuGvcId().getScid() > 255))
                    {
                        throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                           EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                           "Spacecraft ID",
                                                           "0..255"));
                    }
                    if (this.clcwGlobalVcid.getCltuGvcId().getType() == CLTU_ChannelType.cltuCT_VirtualChannel)
                    {
                        if ((this.clcwGlobalVcid.getCltuGvcId().getVcid() < 0) || (this.clcwGlobalVcid.getCltuGvcId().getVcid() > 63))
                        {
                            throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                               EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                               "VC ID",
                                                               "0..63"));
                        }
                    }
                }
                else
                {
                    throw new SleApiException(logAlarm(HRESULT.SLE_E_INVALIDID,
                                                       EE_LogMsg.EE_OP_LM_InvalidID.getCode(),
                                                       "Global VCID version"));
                }
            }
            break;
        case cltuPN_clcwPhysicalChannel:
            //if (this.clcwPhysicalChannel.isEmpty())
        	if (this.clcwPhysicalChannel == null)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case cltuPN_minimumDelayTime:
            if (this.minimumDelayTime == 0)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case cltuPN_minimumReportingCycle:
            if (this.minimumReportingCycle < 1 || this.minimumReportingCycle > 600)
            {
                throw new SleApiException(HRESULT.SLE_E_RANGE);
            }
            break;
        case cltuPN_expectedSlduIdentification:
        case cltuPN_expectedEventInvocationId:
        case cltuPN_modulationFrequency:
        case cltuPN_reportingCycle:
        case cltuPN_invalid:
        {
            break;
        }
        default:
            throw new RuntimeException("Unsupported CLTU parameter " + this.returnedParameter);
        }
        if (getResult() == SLE_Result.sleRES_negative)
        {
            if ((getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
                && (this.parameterDiagnostic == CLTU_GetParameterDiagnostic.cltuGP_invalid))
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Operation.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_ConfirmedOperation.class)
        {
            return (T) this;
        }
        else if (iid == ICLTU_GetParameter.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }

    }

    @Override
    public synchronized int getAcquisitionSequenceLength()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_acquisitionSequenceLength) : "precond";
        return this.acquisitionSequenceLength;
    }

    @Override
    public synchronized void setAcquisitionSequenceLength(int length)
    {
        this.acquisitionSequenceLength = length;
        this.returnedParameter = CLTU_ParameterName.cltuPN_acquisitionSequenceLength;
    }

    @Override
    public synchronized int getPlop1IdleSequenceLength()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_plop1IdleSequenceLength) : "precond";
        return this.plop1IdleSequenceLength;
    }

    @Override
    public synchronized void setPlop1IdleSequenceLength(int length)
    {
        this.plop1IdleSequenceLength = length;
        this.returnedParameter = CLTU_ParameterName.cltuPN_plop1IdleSequenceLength;
    }

    @Override
    public synchronized CLTU_ProtocolAbortMode getProtocolAbortMode()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_protocolAbortMode) : "precond";
        return this.protocolAbortMode;
    }

    @Override
    public synchronized void setProtocolAbortMode(CLTU_ProtocolAbortMode pam)
    {
        this.protocolAbortMode = pam;
        this.returnedParameter = CLTU_ParameterName.cltuPN_protocolAbortMode;
    }

    @Override
    public synchronized CLTU_NotificationMode getNotificationMode()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_notificationMode) : "precond";
        return this.notificationMode;
    }

    @Override
    public synchronized void setNotificationMode(CLTU_NotificationMode nm)
    {
        this.notificationMode = nm;
        this.returnedParameter = CLTU_ParameterName.cltuPN_notificationMode;
    }
/*
    @Override
    public synchronized CLTU_GvcId getClcwGlobalVcid()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_clcwGlobalVcid) : "precond";
        return this.clcwGlobalVcid;
    }

    @Override
    public synchronized void setClcwGlobalVcid(CLTU_GvcId cgv)
    {
        this.returnedParameter = CLTU_ParameterName.cltuPN_clcwGlobalVcid;
        if (cgv != null)
        {
            if (this.clcwGlobalVcid == null)
            {
                this.clcwGlobalVcid = new CLTU_GvcId(cgv);
            }
        }
        else
        {
            if (this.clcwGlobalVcid != null)
            {
                this.clcwGlobalVcid = null;
            }
        }
    }

    @Override
    public synchronized void putClcwGlobalVcid(CLTU_GvcId cgv)
    {
        this.returnedParameter = CLTU_ParameterName.cltuPN_clcwGlobalVcid;
        if (cgv != null)
        {
            if (this.clcwGlobalVcid == null)
            {
                this.clcwGlobalVcid = new CLTU_GvcId(cgv);
            }
        }
        else
        {
            if (this.clcwGlobalVcid != null)
            {
                this.clcwGlobalVcid = null;
            }
        }
    }
*/
    @Override
    public synchronized CLTU_ClcwGvcId getClcwGlobalVcid()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_clcwGlobalVcid) : "precond";
        return this.clcwGlobalVcid;
    }
    
    @Override
    public synchronized void setClcwGlobalVcid(CLTU_ClcwGvcId cgv)
    {
    	this.returnedParameter = CLTU_ParameterName.cltuPN_clcwGlobalVcid;
    	if (cgv != null)
    	{
    		this.clcwGlobalVcid = cgv;
    	}
    	else
    	{
    		this.clcwGlobalVcid = new CLTU_ClcwGvcId(null, CLTU_ConfType.cltuCT_invalid);
    	}    	
    }
    
    @Override
    public synchronized CLTU_ClcwPhysicalChannel getClcwPhysicalChannel()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_clcwPhysicalChannel) : "precond";
        return this.clcwPhysicalChannel;
    }

    @Override
    //public synchronized void setClcwPhysicalChannel(String cpc)
    public synchronized void setClcwPhysicalChannel(CLTU_ClcwPhysicalChannel cpc)
    {
        this.returnedParameter = CLTU_ParameterName.cltuPN_clcwPhysicalChannel;
        this.clcwPhysicalChannel = cpc;
    }

    @Override
    public synchronized long getMinimumDelayTime()
    {
        assert (this.returnedParameter == CLTU_ParameterName.cltuPN_minimumDelayTime) : "precond";
        return this.minimumDelayTime;
    }

    @Override
    public synchronized void setMinimumDelayTime(long mdt)
    {
        this.minimumDelayTime = mdt;
        this.returnedParameter = CLTU_ParameterName.cltuPN_minimumDelayTime;
    }
    
    /**
     * @FunctionSee specification of ICLTU_GetParameter.@EndFunction
     */
	@Override
	public synchronized long getMinimumReportingCycle() {
		assert (this.returnedParameter == CLTU_ParameterName.cltuPN_minimumReportingCycle) : "precond";
		return this.minimumReportingCycle;
	}

	/**
     * @FunctionSee specification of ICLTU_GetParameter.@EndFunction
     */
	@Override
	public synchronized void setMinimumReportingCycle(long mrc) {
		this.minimumReportingCycle = mrc;	
		this.returnedParameter = CLTU_ParameterName.cltuPN_minimumReportingCycle;
	}

    @Override
    public synchronized String toString()
    {
        return "EE_CLTU_GetParameter [requestedParameter=" + this.requestedParameter + ", returnedParameter="
               + this.returnedParameter + ", bitLockRequired=" + this.bitLockRequired + ", deliveryMode="
               + this.deliveryMode + ", expectedCLTUID=" + this.expectedCLTUID + ", expectedEventInvocationID="
               + this.expectedEventInvocationID + ", maxSlduLength=" + this.maxSlduLength + ", modFrequency="
               + this.modFrequency + ", modIndex=" + this.modIndex + ", plopInEffect=" + this.plopInEffect
               + ", reportingCycle=" + this.reportingCycle + ", returnTimeoutPeriod=" + this.returnTimeoutPeriod
               + ", rfAvailableRequired=" + this.rfAvailableRequired + ", subCarrierToBitRateRatio="
               + this.subCarrierToBitRateRatio + ", parameterDiagnostic=" + this.parameterDiagnostic
               + ", acquisitionSequenceLength=" + this.acquisitionSequenceLength + ", plop1IdleSequenceLength="
               + this.plop1IdleSequenceLength + ", protocolAbortMode=" + this.protocolAbortMode + ", notificationMode="
               + this.notificationMode + ", clcwGlobalVcid="
               + ((this.clcwGlobalVcid != null) ? this.clcwGlobalVcid.toString() : "Not Configured") + ", clcwPhysicalChannel="
               + ((this.clcwPhysicalChannel != null && this.clcwPhysicalChannel.getCltuPhyChannel() != null) ? 
            		   this.clcwPhysicalChannel.getCltuPhyChannel() : "")
               + ", minimumDelayTime=" + this.minimumDelayTime 
               + ", minimumReportingCycle=" + this.minimumReportingCycle + "]";
    }

}
