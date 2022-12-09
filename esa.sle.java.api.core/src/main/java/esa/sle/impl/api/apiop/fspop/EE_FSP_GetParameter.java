/**
 * @(#) EE_FSP_GetParameter.java
 */

package esa.sle.impl.api.apiop.fspop;

import java.util.Arrays;

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
import ccsds.sle.api.isrv.ifsp.IFSP_GetParameter;
import ccsds.sle.api.isrv.ifsp.types.FSP_AbsolutePriority;
import ccsds.sle.api.isrv.ifsp.types.FSP_BlockingUsage;
import ccsds.sle.api.isrv.ifsp.types.FSP_ChannelType;
import ccsds.sle.api.isrv.ifsp.types.FSP_ClcwGvcId;
import ccsds.sle.api.isrv.ifsp.types.FSP_ClcwPhysicalChannel;
import ccsds.sle.api.isrv.ifsp.types.FSP_FopState;
import ccsds.sle.api.isrv.ifsp.types.FSP_GetParameterDiagnostic;
import ccsds.sle.api.isrv.ifsp.types.FSP_ConfType;
import ccsds.sle.api.isrv.ifsp.types.FSP_MuxScheme;
import ccsds.sle.api.isrv.ifsp.types.FSP_ParameterName;
import ccsds.sle.api.isrv.ifsp.types.FSP_PermittedTransmissionMode;
import ccsds.sle.api.isrv.ifsp.types.FSP_TimeoutType;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_ConfirmedOperation;
import esa.sle.impl.ifs.gen.EE_LogMsg;

public class EE_FSP_GetParameter extends IEE_SLE_ConfirmedOperation implements IFSP_GetParameter
{

    private FSP_ParameterName requestedParameter = FSP_ParameterName.fspPN_invalid;

    private FSP_ParameterName returnedParameter = FSP_ParameterName.fspPN_invalid;

    private long[] apIdList = null;

    private long blockingTimeout = 0;

    private FSP_BlockingUsage blockingUsage = FSP_BlockingUsage.fspAU_invalid;

    /**
     * The delivery mode.
     */

    private SLE_DeliveryMode deliveryMode = SLE_DeliveryMode.sleDM_invalid;

    private SLE_YesNo directiveInvocationEnabled = SLE_YesNo.sleYN_invalid;

    private SLE_YesNo directiveInvocationOnline = SLE_YesNo.sleYN_invalid;

    private long expectedDirectiveId = 0;

    private long expectedEventInvocationId = 0;

    private long expectedSlduId = 0;

    private long fopSlidingWindow = 0;

    private FSP_FopState fopState = FSP_FopState.fspFS_invalid;

    private long[] mapList = null;

    private FSP_AbsolutePriority[] mapPriorityList = null;

    private long[] mapPollingVector = null;

    private FSP_MuxScheme mapMuxScheme = FSP_MuxScheme.fspMS_invalid;

    private long maxFrameLength = 0;

    private long maxPacketLength = 0;

    private long reportingCycle = 0;

    private long returnTimeoutPeriod = 0;

    private SLE_YesNo segmentHeaderPresent = SLE_YesNo.sleYN_invalid;

    private FSP_TimeoutType timeoutType = FSP_TimeoutType.fspTT_invalid;

    private long timerInitial = 0;

    private long transmissionLimit = 0;

    private long transmitterFrameSequenceNumber = 0;

    private FSP_AbsolutePriority[] vcPriorityList = null;

    private long[] vcPollingVector = null;

    private FSP_MuxScheme vcMuxScheme = FSP_MuxScheme.fspMS_invalid;

    private long virtualChannel = 0;

    private FSP_GetParameterDiagnostic parameterDiagnostic = FSP_GetParameterDiagnostic.fspGP_invalid;

    private FSP_PermittedTransmissionMode permittedTransmissionMode = FSP_PermittedTransmissionMode.fspPTM_invalid;

    private SLE_YesNo bitLockRequired = SLE_YesNo.sleYN_invalid;

    private SLE_YesNo rfAvailableRequired = SLE_YesNo.sleYN_invalid;
    
    private FSP_ClcwGvcId clcwGlobalVcid = null;

    private FSP_ClcwPhysicalChannel clcwPhysicalChannel = null;
    
    private long copCntrFramesRepetition = 0;
    
    private long minReportingCycle = 1;
    
    private long seqCntrFramesRepetition = 0;
    
    private SLE_YesNo throwEventOperation = SLE_YesNo.sleYN_No;


    private EE_FSP_GetParameter(final EE_FSP_GetParameter right)
    {
        super(right);
        this.requestedParameter = right.requestedParameter;
        this.returnedParameter = right.returnedParameter;
        if (right.apIdList != null && right.apIdList.length > 0)
        {
            this.apIdList = new long[right.apIdList.length];
            System.arraycopy(right.apIdList, 0, this.apIdList, 0, right.apIdList.length);
        }
        this.blockingTimeout = right.blockingTimeout;
        this.blockingUsage = right.blockingUsage;
        this.deliveryMode = right.deliveryMode;
        this.directiveInvocationEnabled = right.directiveInvocationEnabled;
        this.directiveInvocationOnline = right.directiveInvocationOnline;
        this.expectedDirectiveId = right.expectedDirectiveId;
        this.expectedEventInvocationId = right.expectedEventInvocationId;
        this.expectedSlduId = right.expectedSlduId;
        this.fopSlidingWindow = right.fopSlidingWindow;
        this.fopState = right.fopState;
        if (right.mapList != null && right.mapList.length > 0)
        {
            this.mapList = new long[right.mapList.length];
            System.arraycopy(right.mapList, 0, this.mapList, 0, right.mapList.length);
        }
        if (right.mapPriorityList != null && right.mapPriorityList.length > 0)
        {
            this.mapPriorityList = new FSP_AbsolutePriority[right.mapPriorityList.length];
            System.arraycopy(right.mapPriorityList, 0, this.mapPriorityList, 0, right.mapPriorityList.length);
        }
        if (right.mapPollingVector != null && right.mapPollingVector.length > 0)
        {
            this.mapPollingVector = new long[right.mapPollingVector.length];
            System.arraycopy(right.mapPollingVector, 0, this.mapPollingVector, 0, right.mapPollingVector.length);
        }
        this.mapMuxScheme = right.mapMuxScheme;
        this.maxFrameLength = right.maxFrameLength;
        this.maxPacketLength = right.maxPacketLength;
        this.reportingCycle = right.reportingCycle;
        this.returnTimeoutPeriod = right.returnTimeoutPeriod;
        this.segmentHeaderPresent = right.segmentHeaderPresent;
        this.timeoutType = right.timeoutType;
        this.timerInitial = right.timerInitial;
        this.transmissionLimit = right.transmissionLimit;
        this.transmitterFrameSequenceNumber = right.transmitterFrameSequenceNumber;
        if (right.vcPriorityList != null && right.vcPriorityList.length > 0)
        {
            this.vcPriorityList = new FSP_AbsolutePriority[right.vcPriorityList.length];
            System.arraycopy(right.vcPriorityList, 0, this.vcPriorityList, 0, right.vcPriorityList.length);
        }
        if (right.vcPollingVector != null && right.vcPollingVector.length > 0)
        {
            this.vcPollingVector = new long[right.vcPollingVector.length];
            System.arraycopy(right.vcPollingVector, 0, this.vcPollingVector, 0, right.vcPollingVector.length);
        }
        this.vcMuxScheme = right.vcMuxScheme;
        this.virtualChannel = right.virtualChannel;
        this.parameterDiagnostic = right.parameterDiagnostic;
        
        // Added for SLES V5
        this.clcwGlobalVcid = right.clcwGlobalVcid;
        this.clcwPhysicalChannel = right.clcwPhysicalChannel;
        this.copCntrFramesRepetition = right.copCntrFramesRepetition;
        this.minReportingCycle = right.minReportingCycle;
        this.seqCntrFramesRepetition = right.seqCntrFramesRepetition;
        this.throwEventOperation = right.throwEventOperation;
        
        
        
    }

    public EE_FSP_GetParameter(int version)
    {
        this(version, null);

    }

    /**
     * See specification of IFSP_GetParameter.
     */
    public EE_FSP_GetParameter(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt, SLE_OpType.sleOT_getParameter, version, preporter);
    }

    @Override
    public synchronized FSP_ParameterName getRequestedParameter()
    {
        return this.requestedParameter;
    }

    @Override
    public synchronized FSP_ParameterName getReturnedParameter()
    {
        return this.returnedParameter;
    }

    @Override
    public synchronized long[] getApIdList()
    {
        return this.apIdList;
    }

    @Override
    public synchronized long getBlockingTimeout()
    {
        return this.blockingTimeout;
    }

    @Override
    public synchronized FSP_BlockingUsage getBlockingUsage()
    {
        return this.blockingUsage;
    }

    @Override
    public synchronized SLE_DeliveryMode getDeliveryMode()
    {
        return this.deliveryMode;
    }

    @Override
    public synchronized SLE_YesNo getDirectiveInvocationEnabled()
    {
        return this.directiveInvocationEnabled;
    }

    @Override
    public synchronized SLE_YesNo getDirectiveInvocationOnline()
    {
        return this.directiveInvocationOnline;
    }

    @Override
    public synchronized long getExpectedDirectiveId()
    {
        return this.expectedDirectiveId;
    }

    @Override
    public synchronized long getExpectedEventInvocationId()
    {
        return this.expectedEventInvocationId;
    }

    @Override
    public synchronized long getExpectedSlduId()
    {
        return this.expectedSlduId;
    }

    @Override
    public synchronized long getFopSlidingWindow()
    {
        return this.fopSlidingWindow;
    }

    @Override
    public synchronized FSP_FopState getFopState()
    {
        return this.fopState;
    }

    @Override
    public synchronized long[] getMapList()
    {
        return this.mapList;
    }

    @Override
    public synchronized FSP_AbsolutePriority[] getMapPriorityList()
    {
        return this.mapPriorityList;
    }

    @Override
    public synchronized long[] getMapPollingVector()
    {
        return this.mapPollingVector;
    }

    @Override
    public synchronized FSP_MuxScheme getMapMuxScheme()
    {
        return this.mapMuxScheme;
    }

    @Override
    public synchronized long getMaxFrameLength()
    {
        return this.maxFrameLength;
    }

    @Override
    public synchronized long getMaxPacketLength()
    {
        return this.maxPacketLength;
    }

    @Override
    public synchronized long getReportingCycle()
    {
        return this.reportingCycle;
    }

    @Override
    public synchronized long getReturnTimeoutPeriod()
    {
        return this.returnTimeoutPeriod;
    }

    @Override
    public synchronized SLE_YesNo getSegmentHeaderPresent()
    {
        return this.segmentHeaderPresent;
    }

    @Override
    public synchronized FSP_TimeoutType getTimeoutType()
    {
        return this.timeoutType;
    }

    @Override
    public synchronized long getTimerInitial()
    {
        return this.timerInitial;
    }

    @Override
    public synchronized long getTransmissionLimit()
    {
        return this.transmissionLimit;
    }

    @Override
    public synchronized long getTransmitterFrameSequenceNumber()
    {
        return this.transmitterFrameSequenceNumber;
    }

    @Override
    public synchronized FSP_AbsolutePriority[] getVcPriorityList()
    {
        return this.vcPriorityList;
    }

    @Override
    public synchronized long[] getVcPollingVector()
    {
        return this.vcPollingVector;
    }

    @Override
    public synchronized FSP_MuxScheme getVcMuxScheme()
    {
        return this.vcMuxScheme;
    }

    @Override
    public synchronized long getVirtualChannel()
    {
        return this.virtualChannel;
    }

    @Override
    public synchronized FSP_GetParameterDiagnostic getGetParameterDiagnostic()
    {
        return this.parameterDiagnostic;
    }

    @Override
    public synchronized void setRequestedParameter(FSP_ParameterName name)
    {
        this.requestedParameter = name;
    }

    @Override
    public synchronized void setApIdList(long[] plist)
    {
        if (plist != null && plist.length > 0)
        {
            this.apIdList = new long[plist.length];
            System.arraycopy(plist, 0, this.apIdList, 0, plist.length);
        }
        this.returnedParameter = FSP_ParameterName.fspPN_apidList;
    }

    @Override
    public synchronized void putApIdList(long[] plist)
    {
        this.apIdList = plist;
        this.returnedParameter = FSP_ParameterName.fspPN_apidList;
    }

    @Override
    public synchronized void setBlockingTimeout(long timeout)
    {
        this.blockingTimeout = timeout;
        this.returnedParameter = FSP_ParameterName.fspPN_blockingTimeoutPeriod;
    }

    @Override
    public synchronized void setBlockingUsage(FSP_BlockingUsage usage)
    {
        this.blockingUsage = usage;
        this.returnedParameter = FSP_ParameterName.fspPN_blockingUsage;
    }

    @Override
    public synchronized void setDeliveryMode()
    {
        this.deliveryMode = SLE_DeliveryMode.sleDM_fwdOnline;
        this.returnedParameter = FSP_ParameterName.fspPN_deliveryMode;
    }

    @Override
    public synchronized void setDirectiveInvocationEnabled(SLE_YesNo yesNo)
    {
        this.directiveInvocationEnabled = yesNo;
        this.returnedParameter = FSP_ParameterName.fspPN_directiveInvocationEnabled;
    }

    @Override
    public synchronized void setDirectiveInvocationOnline(SLE_YesNo yesNo)
    {
        this.directiveInvocationOnline = yesNo;
        this.returnedParameter = FSP_ParameterName.fspPN_directiveInvocationOnline;
    }

    @Override
    public synchronized void setExpectedDirectiveId(long id)
    {
        this.expectedDirectiveId = id;
        this.returnedParameter = FSP_ParameterName.fspPN_expectedDirectiveId;
    }

    @Override
    public synchronized void setExpectedEventInvocationId(long id)
    {
        this.expectedEventInvocationId = id;
        this.returnedParameter = FSP_ParameterName.fspPN_expectedEventInvocationId;
    }

    @Override
    public synchronized void setExpectedSlduId(long id)
    {
        this.expectedSlduId = id;
        this.returnedParameter = FSP_ParameterName.fspPN_expectedSlduIdentification;
    }

    @Override
    public synchronized void setFopSlidingWindow(long window)
    {
        this.fopSlidingWindow = window;
        this.returnedParameter = FSP_ParameterName.fspPN_fopSlidingWindow;
    }

    @Override
    public synchronized void setFopState(FSP_FopState state)
    {
        this.fopState = state;
        this.returnedParameter = FSP_ParameterName.fspPN_fopState;
    }

    @Override
    public synchronized void setMapList(long[] plist)
    {
        if (plist != null && plist.length > 0)
        {
            this.mapList = new long[plist.length];
            System.arraycopy(plist, 0, this.mapList, 0, plist.length);
        }
        this.returnedParameter = FSP_ParameterName.fspPN_mapList;
    }

    @Override
    public synchronized void putMapList(long[] plist)
    {
        this.mapList = plist;
        this.returnedParameter = FSP_ParameterName.fspPN_mapList;
    }

    @Override
    public synchronized void setMapPriorityList(FSP_AbsolutePriority[] priorities)
    {
        if (priorities != null && priorities.length > 0)
        {
            this.mapPriorityList = new FSP_AbsolutePriority[priorities.length];
            System.arraycopy(priorities, 0, this.mapPriorityList, 0, priorities.length);
        }
        this.returnedParameter = FSP_ParameterName.fspPN_mapMuxControl;
    }

    @Override
    public synchronized void putMapPriorityList(FSP_AbsolutePriority[] priorities)
    {
        this.mapPriorityList = priorities;
        this.returnedParameter = FSP_ParameterName.fspPN_mapMuxControl;
    }

    @Override
    public synchronized void setMapPollingVector(long[] pvec)
    {
        if (pvec != null && pvec.length > 0)
        {
            this.mapPollingVector = new long[pvec.length];
            System.arraycopy(pvec, 0, this.mapPollingVector, 0, pvec.length);
        }
        this.returnedParameter = FSP_ParameterName.fspPN_mapMuxControl;
    }

    @Override
    public synchronized void putMapPollingVector(long[] pvec)
    {
        this.mapPollingVector = pvec;
        this.returnedParameter = FSP_ParameterName.fspPN_mapMuxControl;
    }

    @Override
    public synchronized void setMapMuxScheme(FSP_MuxScheme scheme)
    {
        this.mapMuxScheme = scheme;
        this.returnedParameter = FSP_ParameterName.fspPN_mapMuxScheme;
    }

    @Override
    public synchronized void setMaxFrameLength(long length)
    {
        this.maxFrameLength = length;
        this.returnedParameter = FSP_ParameterName.fspPN_maximumFrameLength;
    }

    @Override
    public synchronized void setMaxPacketLength(long length)
    {
        this.maxPacketLength = length;
        this.returnedParameter = FSP_ParameterName.fspPN_maximumPacketLength;
    }

    @Override
    public synchronized void setReportingCycle(long cycle)
    {
        this.reportingCycle = cycle;
        this.returnedParameter = FSP_ParameterName.fspPN_reportingCycle;
    }

    @Override
    public synchronized void setReturnTimeoutPeriod(long period)
    {
        this.returnTimeoutPeriod = period;
        this.returnedParameter = FSP_ParameterName.fspPN_returnTimeoutPeriod;
    }

    @Override
    public synchronized void setSegmentHeaderPresent(SLE_YesNo yesNo)
    {
        this.segmentHeaderPresent = yesNo;
        this.returnedParameter = FSP_ParameterName.fspPN_segmentHeader;
    }

    @Override
    public synchronized void setTimeoutType(FSP_TimeoutType type)
    {
        this.timeoutType = type;
        this.returnedParameter = FSP_ParameterName.fspPN_timeoutType;
    }

    @Override
    public synchronized void setTimerInitial(long timeout)
    {
        this.timerInitial = timeout;
        this.returnedParameter = FSP_ParameterName.fspPN_timerInitial;
    }

    @Override
    public synchronized void setTransmissionLimit(long limit)
    {
        this.transmissionLimit = limit;
        this.returnedParameter = FSP_ParameterName.fspPN_transmissionLimit;
    }

    @Override
    public synchronized void setTransmitterFrameSequenceNumber(long number)
    {
        this.transmitterFrameSequenceNumber = number;
        this.returnedParameter = FSP_ParameterName.fspPN_transmitterFrameSequenceNumber;
    }

    @Override
    public synchronized void setVcPriorityList(FSP_AbsolutePriority[] priorities)
    {
        if (priorities != null && priorities.length > 0)
        {
            this.vcPriorityList = new FSP_AbsolutePriority[priorities.length];
            System.arraycopy(priorities, 0, this.vcPriorityList, 0, priorities.length);
        }
        this.returnedParameter = FSP_ParameterName.fspPN_vcMuxControl;
    }

    @Override
    public synchronized void putVcPriorityList(FSP_AbsolutePriority[] priorities)
    {
        this.vcPriorityList = priorities;
        this.returnedParameter = FSP_ParameterName.fspPN_vcMuxControl;
    }

    @Override
    public synchronized void setVcPollingVector(long[] pvec)
    {
        if (pvec != null && pvec.length > 0)
        {
            this.vcPollingVector = new long[pvec.length];
            System.arraycopy(pvec, 0, this.vcPollingVector, 0, pvec.length);
        }
        this.returnedParameter = FSP_ParameterName.fspPN_vcMuxControl;
    }

    @Override
    public synchronized void putVcPollingVector(long[] pvec)
    {
        this.vcPollingVector = pvec;
        this.returnedParameter = FSP_ParameterName.fspPN_vcMuxControl;
    }

    @Override
    public synchronized void setVcMuxScheme(FSP_MuxScheme scheme)
    {
        this.vcMuxScheme = scheme;
        this.returnedParameter = FSP_ParameterName.fspPN_vcMuxScheme;
    }

    @Override
    public synchronized void setVirtualChannel(long id)
    {
        this.virtualChannel = id;
        this.returnedParameter = FSP_ParameterName.fspPN_virtualChannel;
    }

    @Override
    public synchronized void setGetParameterDiagnostic(FSP_GetParameterDiagnostic diagnostic)
    {
        this.parameterDiagnostic = diagnostic;
        setSpecificDiagnostics();
    }

    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        HRESULT baseres = HRESULT.S_OK;
        try
        {
            super.verifyInvocationArguments();
        }
        catch (SleApiException e)
        {
            baseres = e.getHResult();
        }
        if (baseres != HRESULT.S_OK)
        {
            throw new SleApiException(baseres);
        }
        if (this.requestedParameter == FSP_ParameterName.fspPN_invalid)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                               EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                               "Requested parameter"));
        }
    }

    @Override
    public synchronized ISLE_Operation copy()
    {

        EE_FSP_GetParameter ptmp = new EE_FSP_GetParameter(this);
        ISLE_Operation pop = null;
        pop = ptmp.queryInterface(IFSP_GetParameter.class);
        return pop;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder();
        printOn(oss, maxDumpLength);
        oss.append("Requested parameter    : " + this.requestedParameter + "\n");
        oss.append("Returned Paramter      : " + this.returnedParameter + "\n");
        oss.append("Application ID list    : ");
        if (this.apIdList != null)
        {
            int i;
            if(this.apIdList.length == 1 && this.apIdList[0] == -1)
            {
            	oss.append("any");
            }
            else
            {
	            for (i = 0; i < this.apIdList.length; i++)
	            {
	                oss.append(this.apIdList[i] + " ");
	            }
            }
        }
        oss.append("\n");
        oss.append("Bit lock required      : " + this.bitLockRequired + "\n");
        oss.append("Blocking timeout       : " + this.blockingTimeout + "\n");
        oss.append("Blocking usage         : " + this.blockingUsage + "\n");
        oss.append("Dir. invocation enabled: " + this.directiveInvocationEnabled + "\n");
        oss.append("Dir. invocation online : " + this.directiveInvocationOnline + "\n");
        oss.append("Delivery mode          : " + this.deliveryMode + "\n");
        oss.append("Expected directive ID  : " + this.expectedDirectiveId + "\n");
        oss.append("Expected event inv. ID : " + this.expectedEventInvocationId + "\n");
        oss.append("Expected SLDU ID       : " + this.expectedSlduId + "\n");
        oss.append("FOP sliding window     : " + this.fopSlidingWindow + "\n");
        oss.append("FOP state              : " + this.fopState + "\n");
        oss.append("MAP ID list            : ");
        if (this.mapList != null)
        {
            int i;
            for (i = 0; i < this.mapList.length; i++)
            {
                oss.append(this.mapList[i] + " ");
            }
        }
        oss.append("\n");
        oss.append("MAP priority list (map/pri): ");
        if (this.mapPriorityList != null)
        {
            int i;
            for (i = 0; i < this.mapPriorityList.length; i++)
            {
                oss.append(this.mapPriorityList[i].getMapOrVc() + "/" + this.mapPriorityList[i].getPriority() + " ");
            }
        }
        oss.append("\n");
        oss.append("MAP polling vector     : ");
        if (this.mapPollingVector != null)
        {
            int i;
            for (i = 0; i < this.mapPollingVector.length; i++)
            {
                oss.append(this.mapPollingVector[i] + " ");
            }
        }
        oss.append("\n");
        oss.append("MAP mux scheme             : " + this.mapMuxScheme + "\n");
        oss.append("Maximum frame length       : " + this.maxFrameLength + "\n");
        oss.append("Maximum packet length      : " + this.maxPacketLength + "\n");
        oss.append("Perm. transmission mode    : " + this.permittedTransmissionMode + "\n");
        oss.append("Reporting cycle            : " + this.reportingCycle + "\n");
        oss.append("Return timeout period      : " + this.returnTimeoutPeriod + "\n");
        oss.append("Rf available required      : " + this.rfAvailableRequired + "\n");
        oss.append("Segment header present     : " + this.segmentHeaderPresent + "\n");
        oss.append("Timeout type               : " + this.timeoutType + "\n");
        oss.append("Timer initial              : " + this.timerInitial + "\n");
        oss.append("Transmission limit         : " + this.transmissionLimit + "\n");
        oss.append("Trans. frame sequ. no.     : " + this.transmitterFrameSequenceNumber + "\n");
        oss.append("FOP sliding window         : " + this.fopSlidingWindow + "\n");
        oss.append("VC priority list (VC/pri)  : ");
        if (this.vcPriorityList != null)
        {
            int i;
            for (i = 0; i < this.vcPriorityList.length; i++)
            {
                oss.append(this.vcPriorityList[i].getMapOrVc() + "/" + this.vcPriorityList[i].getPriority() + " ");
            }
        }
        oss.append("\n");
        oss.append("VC polling vector          : ");
        if (this.vcPollingVector != null)
        {
            for (long element : this.vcPollingVector)
            {
                oss.append(element + " ");
            }
        }
        oss.append("\n");
        oss.append("VC mux scheme              : " + this.vcMuxScheme + "\n");
        oss.append("Virtual channel            : " + this.virtualChannel + "\n");
        oss.append("Parameter Diagnostic       : " + this.parameterDiagnostic + "\n");
        // Added for SLE V5
        if (this.clcwGlobalVcid != null &&
        	this.clcwGlobalVcid.getConfigType() == FSP_ConfType.fspCT_configured &&
            this.clcwGlobalVcid.getGvcId() != null)
        {
            oss.append("CLCW Global VC type        : " + this.clcwGlobalVcid.getGvcId().getType().toString() + "\n");
            oss.append("CLCW Global VC scid        : " + this.clcwGlobalVcid.getGvcId().getScid() + "\n");
            oss.append("CLCW Global VC version     : " + this.clcwGlobalVcid.getGvcId().getVersion() + "\n");
            oss.append("CLCW Global VC channel ID  : " + this.clcwGlobalVcid.getGvcId().getVcid() + "\n");
        }
        else if(this.clcwGlobalVcid != null && this.clcwGlobalVcid.getConfigType() == FSP_ConfType.fspCT_notConfigured)
        {
        	oss.append("CLCW Global VC             : NotConfigured" + "\n");
        }
        else
        {
            oss.append("CLCW Global VC             : Invalid" + "\n");
        }
        if (this.clcwPhysicalChannel != null && this.clcwPhysicalChannel.getClcwPhysicalChannel() != null)
        {
            oss.append("CLCW Physical Channel      : " + this.clcwPhysicalChannel.getClcwPhysicalChannel() + "\n");
        }
        else 
        {
            oss.append("CLCW Physical Channel      : Not configured " + "\n");
        }
        oss.append("COP ctrl frames rep        : " + this.copCntrFramesRepetition + "\n");
        oss.append("Min report cycle           : " + this.minReportingCycle + "\n");
        oss.append("Seq ctrl frame rep         : " + this.seqCntrFramesRepetition + "\n");
        oss.append("Throw event operation      : " + this.throwEventOperation + "\n");
        
        String ret = oss.toString();
        return ret;

    }

    @Override
    public synchronized void verifyReturnArguments() throws SleApiException
    {
        HRESULT baseres = HRESULT.S_OK;
        try
        {
            super.verifyReturnArguments();
        }
        catch (SleApiException e)
        {
            baseres = e.getHResult();
        }
        if (baseres != HRESULT.S_OK)
        {
            throw new SleApiException(baseres);
        }

        if (this.returnedParameter != this.requestedParameter)
        {
            throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                               EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                               "Requested parameter type"));
        }

        switch (this.returnedParameter)
        {
        case fspPN_apidList:
            if (this.apIdList == null)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "APID list"));
            }
            else
            {
            	if(this.apIdList.length == 1 && this.apIdList[0] == -1)
            	{
            		break; // indicates any
            	}
                for (long element : this.apIdList)
                {
                    if (element < 0 || element > 2047)
                    {
                        throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                           EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                           "APID",
                                                           "0..2947"));
                    }
                }
            }
            break;

        case fspPN_bitLockRequired:
            if (this.bitLockRequired == SLE_YesNo.sleYN_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Bit lock required"));
            }
            break;

        case fspPN_blockingTimeoutPeriod:
            if (this.blockingTimeout != 0)
            {
                if (this.blockingTimeout < 100 || this.blockingTimeout > 100000)
                {
                    throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                       EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                       "blocking timeout period",
                                                       "100..100000"));
                }
            }
            break;

        case fspPN_blockingUsage:
            if (this.blockingUsage == FSP_BlockingUsage.fspAU_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Blocking usage"));
            }
            break;

        case fspPN_directiveInvocationEnabled:
            if (this.directiveInvocationEnabled == SLE_YesNo.sleYN_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Directive invocation enabled"));
            }
            break;

        case fspPN_fopSlidingWindow:
            if (this.fopSlidingWindow < 1 || this.fopSlidingWindow > 255)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                   EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                   "FOP sliding window",
                                                   "1..255"));
            }
            break;

        case fspPN_fopState:
            if (this.fopState == FSP_FopState.fspFS_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "FOP state"));
            }
            break;

        case fspPN_mapList:
            if (this.mapList != null)
            {
                if (this.mapList.length < 1 || this.mapList.length > 64)
                {
                    throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                       EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                       "No. of map list entries",
                                                       "1..64"));
                }
                else
                {
                    for (long element : this.mapList)
                    {
                        if (element < 0 || element > 63)
                        {
                            throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                               EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                               "Map list",
                                                               "0..63"));
                        }
                    }
                }
            }
            break;

        case fspPN_mapMuxControl:
            if (this.mapPriorityList != null)
            {
                if (this.mapPriorityList.length < 1 || this.mapPriorityList.length > 64)
                {
                    throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                       EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                       "Map priority list entries",
                                                       "1..64"));
                }
                else
                {
                    for (FSP_AbsolutePriority element : this.mapPriorityList)
                    {
                        if (element.getMapOrVc() < 0 || element.getMapOrVc() > 63)
                        {
                            throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                               EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                               "ID in map priority list",
                                                               "0..63"));
                        }
                        if (element.getPriority() < 1 || element.getPriority() > 64)
                        {
                            throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                               EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                               "Priority in map priority list",
                                                               "0..63"));
                        }
                    }
                }
            }
            if (this.mapPollingVector != null)
            {
                if (this.mapPollingVector.length < 1 || this.mapPollingVector.length > 192)
                {
                    throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                       EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                       "Map polling vector entries",
                                                       "1..192"));
                }
                else
                {
                    for (long element : this.mapPollingVector)
                    {
                        if (element < 0 || element > 63)
                        {
                            throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                               EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                               "ID in map polling vector",
                                                               "0..63"));
                        }
                    }
                }
            }
            break;

        case fspPN_mapMuxScheme:
            if (this.mapMuxScheme == FSP_MuxScheme.fspMS_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Map multiplexing scheme"));
            }
            break;

        case fspPN_maximumFrameLength:
            if (this.maxFrameLength < 12 || this.maxFrameLength > 1024)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                   EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                   "Maximum frame length",
                                                   "12..1024"));
            }
            break;

        case fspPN_maximumPacketLength:
            if (this.maxPacketLength < 7 || this.maxPacketLength > 65542)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                   EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                   "Maximum packet length",
                                                   "7..65542"));
            }
            break;

        case fspPN_returnTimeoutPeriod:
            if (this.returnTimeoutPeriod == 0)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Return timeout period"));
            }
            break;

        case fspPN_rfAvailableRequired:
            if (this.rfAvailableRequired == SLE_YesNo.sleYN_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Rf available required"));
            }
            break;

        case fspPN_segmentHeader:
            if (this.segmentHeaderPresent == SLE_YesNo.sleYN_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Segment header"));
            }
            break;

        case fspPN_timeoutType:
            if (this.timeoutType == FSP_TimeoutType.fspTT_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Timeout type"));
            }
            break;

        case fspPN_timerInitial:
            if (this.timerInitial == 0)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "Timer initial"));
            }
            break;

        case fspPN_transmissionLimit:
            if (this.transmissionLimit < 1 || this.transmissionLimit > 255)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                   EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                   "Transmission limit",
                                                   "1..255"));
            }
            break;

        case fspPN_transmitterFrameSequenceNumber:
            if (this.transmitterFrameSequenceNumber < 0 || this.transmitterFrameSequenceNumber > 255)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                   EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                   "Transmitter frame sequence number",
                                                   "0..255"));
            }
            break;

        case fspPN_vcMuxControl:
            if (this.vcPriorityList != null)
            {
                if (this.vcPriorityList.length < 1 || this.vcPriorityList.length > 64)
                {
                    throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                       EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                       "No. of VC mux. priority list entries",
                                                       "1..64"));
                }
                else
                {
                    for (FSP_AbsolutePriority element : this.vcPriorityList)
                    {
                        if (element.getMapOrVc() < 0 || element.getMapOrVc() > 63)
                        {
                            throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                               EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                               "ID in VC priority list",
                                                               "0..63"));
                        }
                        if (element.getPriority() < 1 || element.getPriority() > 64)
                        {
                            throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                               EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                               "Priority in VC priority list",
                                                               "0..63"));
                        }
                    }
                }
            }
            if (this.vcPollingVector != null)
            {
                if (this.vcPollingVector.length < 1 || this.vcPollingVector.length > 192)
                {
                    throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                       EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                       "VC polling vector entries",
                                                       "1..192"));
                }
                else
                {
                    for (long element : this.vcPollingVector)
                    {
                        if (element < 0 || element > 63)
                        {
                            throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                               EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                               "ID in VC polling vector",
                                                               "0..63"));
                        }
                    }
                }
            }
            break;

        case fspPN_vcMuxScheme:
            if (this.vcMuxScheme == FSP_MuxScheme.fspMS_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                                                   EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                   "VC multiplexing scheme"));
            }
            break;

        case fspPN_virtualChannel:
            if (this.virtualChannel < 0 || this.virtualChannel > 63)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                                                   EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                   "VC ID",
                                                   "0..63"));
            }
            break;
            
        case fspPN_clcwGlobalVcId:
        	if (this.clcwGlobalVcid != null )
            {
        		if (this.clcwGlobalVcid.getConfigType() == FSP_ConfType.fspCT_configured)
        		{
        			if (this.clcwGlobalVcid.getGvcId().getVersion() == 0)
        			{
        				// This is the PTM supported version, which permits 0 .. 1023 (10 bits)
        				if ((this.clcwGlobalVcid.getGvcId().getScid() < 0) || (this.clcwGlobalVcid.getGvcId().getScid() > 1023))
        				{
        					throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
        							EE_LogMsg.EE_OP_LM_Range.getCode(),
        							"Spacecraft ID",
        							"0..1023"));
        				}
        				if (this.clcwGlobalVcid.getGvcId().getType() == FSP_ChannelType.fspCT_VirtualChannel)
        				{
        					if ((this.clcwGlobalVcid.getGvcId().getVcid() < 0) || (this.clcwGlobalVcid.getGvcId().getVcid() > 7))
        					{
        						throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
        								EE_LogMsg.EE_OP_LM_Range.getCode(),
        								"VC ID",
        								"0..7"));
        					}
        				}
        			}
        			else if (this.clcwGlobalVcid.getGvcId().getVersion() == 1)
        			{
        				// This is the AOS suported version which permits 0 .. 255 (8 bits)
        				if ((this.clcwGlobalVcid.getGvcId().getScid() < 0) || (this.clcwGlobalVcid.getGvcId().getScid() > 255))
        				{
        					throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
        							EE_LogMsg.EE_OP_LM_Range.getCode(),
        							"Spacecraft ID",
        							"0..255 recvd: " + this.clcwGlobalVcid.getGvcId().getScid()));
        				}
        				if (this.clcwGlobalVcid.getGvcId().getType() == FSP_ChannelType.fspCT_VirtualChannel)
        				{
        					if ((this.clcwGlobalVcid.getGvcId().getVcid() < 0) || (this.clcwGlobalVcid.getGvcId().getVcid() > 63))
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
        		else if (this.clcwGlobalVcid.getConfigType() == FSP_ConfType.fspCT_invalid)
        		{
        			throw new SleApiException(logAlarm(HRESULT.SLE_E_BADVALUE,
        					EE_LogMsg.EE_OP_LM_InvalidMode.getCode()));
        		}
            }
        break;      	
        case fspPN_clcwPhysicalChannel:    
        	if(this.clcwPhysicalChannel.getConfigType() == FSP_ConfType.fspCT_configured)
        	{
        		if(this.clcwPhysicalChannel.getClcwPhysicalChannel().length() < 1 || 
        		   this.clcwPhysicalChannel.getClcwPhysicalChannel().length() > 32)
        		{
        			throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                            EE_LogMsg.EE_OP_LM_Range.getCode(),
                            "String lenght of CLCW phy channel",
                            "1..32"));
        		}
        	}
        	else if (this.clcwPhysicalChannel.getConfigType() == FSP_ConfType.fspCT_invalid)
    		{
    			throw new SleApiException(logAlarm(HRESULT.SLE_E_BADVALUE,
    					EE_LogMsg.EE_OP_LM_InvalidMode.getCode()));
    		}
        break;	
        case fspPN_copCntrFramesRepetion:
        	if(this.copCntrFramesRepetition < 1 || this.copCntrFramesRepetition > 65535)
        	{
        		throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                        EE_LogMsg.EE_OP_LM_Range.getCode(),
                        "Cop control frames repetition",
                        "1..65535"));
        	}
        	break;
        case fspPN_minReportingCycle:
        	if(this.minReportingCycle < 1 || this.minReportingCycle > 600)
        	{
        		throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                        EE_LogMsg.EE_OP_LM_Range.getCode(),
                        "Min reporting cycle",
                        "1..600"));
        	}
        	break;
        case fspPN_seqCntrFramesRepetition:
        	if(this.seqCntrFramesRepetition < 1 || this.seqCntrFramesRepetition > 65535)
        	{
        		throw new SleApiException(logAlarm(HRESULT.SLE_E_RANGE,
                        EE_LogMsg.EE_OP_LM_Range.getCode(),
                        "Sequence control frames repetition",
                        "1..65535"));
        	}
        	break;
        case fspPN_throwEventOperation:
        if(this.throwEventOperation != null)
        {
        	if(this.throwEventOperation == SLE_YesNo.sleYN_invalid){
        		throw new SleApiException(logAlarm(HRESULT.SLE_E_MISSINGARG,
                        EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                        "Throw event operation"));
        	}
        }
        break;
            
        default:
            break;
        }

        if ((getResult() == SLE_Result.sleRES_negative)
            && (getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics))
        {
            if (this.parameterDiagnostic == FSP_GetParameterDiagnostic.fspGP_invalid)
            {
                throw new SleApiException(logAlarm(HRESULT.SLE_E_INCONSISTENT,
                                                   EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                   "Get parameter diagnostic"));
            }
        }
    }

    @Override
    public synchronized FSP_PermittedTransmissionMode getPermittedTransmissionMode()
    {
        return this.permittedTransmissionMode;
    }

    @Override
    public synchronized void setPermittedTransmissionMode(FSP_PermittedTransmissionMode mode)
    {
        this.permittedTransmissionMode = mode;
        this.returnedParameter = FSP_ParameterName.fspPN_permittedTransmissionMode;
    }

    @Override
    public synchronized SLE_YesNo getBitLockRequired()
    {
        return this.bitLockRequired;
    }

    @Override
    public synchronized SLE_YesNo getRfAvailableRequired()
    {
        return this.rfAvailableRequired;
    }
    
    @Override
    public synchronized FSP_ClcwGvcId getClcwGlobalVcid()
    {
        return this.clcwGlobalVcid;
    }
    
    @Override
    public synchronized FSP_ClcwPhysicalChannel getClcwPhysicalChannel()
    {
        return this.clcwPhysicalChannel;
    }

    @Override
    public synchronized void setBitLockRequired(SLE_YesNo yesNo)
    {
        this.bitLockRequired = yesNo;
        this.returnedParameter = FSP_ParameterName.fspPN_bitLockRequired;
    }

    @Override
    public synchronized void setRfAvailableRequired(SLE_YesNo yesNo)
    {
        this.rfAvailableRequired = yesNo;
        this.returnedParameter = FSP_ParameterName.fspPN_rfAvailableRequired;
    }

    /**
     * Setter for clcwGlobalVcid for FSP (added for SLES V5 support)
     */
    @Override
    public synchronized void setClcwGlobalVcid(FSP_ClcwGvcId clcwGvcId)
    {
        this.returnedParameter = FSP_ParameterName.fspPN_clcwGlobalVcId;
    	if (clcwGvcId != null)
    	{
    		this.clcwGlobalVcid = clcwGvcId;	
    	}
    	else
    	{
    		this.clcwGlobalVcid = new FSP_ClcwGvcId(null, FSP_ConfType.fspCT_notConfigured);
    	}    
    }
 
    /**
     * Setter for clcwPhysicalChannel for FSP (added for SLES V5 support)
     */
    @Override
    public synchronized void setClcwPhysicalChannel(FSP_ClcwPhysicalChannel pch)
    {
        this.clcwPhysicalChannel = pch;
        this.returnedParameter = FSP_ParameterName.fspPN_clcwPhysicalChannel;
    }
 
    /**
     * Setter for copControlFramesRepetion for FSP (added for SLES V5 support)
     */
    @Override
    public synchronized void setCopCntrFramesRepetition(long counter)
    {
    	this.copCntrFramesRepetition = counter;
        this.returnedParameter = FSP_ParameterName.fspPN_copCntrFramesRepetion;
    }
 
    /**
     * Setter for minimum-reporting-cycle for FSP (added for SLES V5 support)
     */
    @Override
    public synchronized void setMinReportingCycle(long mrc)
    {
        this.minReportingCycle = mrc;
        this.returnedParameter = FSP_ParameterName.fspPN_minReportingCycle;
    }
 
    /**
     * Setter for sequControlFramesRepetion for FSP (added for SLES V5 support)
     */
    @Override
    public synchronized void setSeqCntrFramesRepetition(long scfr)
    {
        this.seqCntrFramesRepetition = scfr;
        this.returnedParameter = FSP_ParameterName.fspPN_seqCntrFramesRepetition;
    }
    
    /**
     * Setter for throw-event-operation for FSP (added for SLES V5 support)
     */
    @Override
    public synchronized void setThrowEventOperation(SLE_YesNo yesNo)
    {
        this.throwEventOperation = yesNo;
        this.returnedParameter = FSP_ParameterName.fspPN_throwEventOperation;
    }  
    
    /**
     * @return returns the copCntrFramesRepetition for FSP (added for SLES V5 support)
     */
    public long getCopCntrFramesRepetition()
    {
    	return this.copCntrFramesRepetition;
    }
    
    /**
     * @return returns the minReportingCycle for FSP (added for SLES V5 support)
     */
    public long getMinReportingCycle()
    {
    	return this.minReportingCycle;
    }
    
    /**
     * @return returns the seqCntrFramesRepetition for FSP (added for SLES V5 support)
     */
    public long getSeqCntrFramesRepetition()
    {
    	return this.seqCntrFramesRepetition;
    }
    
    /**
     * @return returns the throwEventOperation for FSP (added for SLES V5 support)
     */
    public SLE_YesNo getThrowEventOperation()
    {
    	return this.throwEventOperation;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == IFSP_GetParameter.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_ConfirmedOperation.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Operation.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    @Override
    public synchronized String toString()
    {
        return "EE_FSP_GetParameter [requestedParameter=" + this.requestedParameter + ", returnedParameter="
               + this.returnedParameter + ", apIdList=" + Arrays.toString(this.apIdList) + ", blockingTimeout="
               + this.blockingTimeout + ", blockingUsage=" + this.blockingUsage + ", deliveryMode=" + this.deliveryMode
               + ", directiveInvocationEnabled=" + this.directiveInvocationEnabled + ", directiveInvocationOnline="
               + this.directiveInvocationOnline + ", expectedDirectiveId=" + this.expectedDirectiveId
               + ", expectedEventInvocationId=" + this.expectedEventInvocationId + ", expectedSlduId="
               + this.expectedSlduId + ", fopSlidingWindow=" + this.fopSlidingWindow + ", fopState=" + this.fopState
               + ", mapList=" + ((this.mapList != null) ? Arrays.toString(this.mapList) : "") + ", mapPriorityList="
               + ((this.mapPriorityList != null) ? Arrays.toString(this.mapPriorityList) : "") + ", mapPollingVector="
               + ((this.mapPollingVector != null) ? Arrays.toString(this.mapPollingVector) : "") + ", mapMuxScheme="
               + this.mapMuxScheme + ", maxFrameLength=" + this.maxFrameLength + ", maxPacketLength="
               + this.maxPacketLength + ", reportingCycle=" + this.reportingCycle + ", returnTimeoutPeriod="
               + this.returnTimeoutPeriod + ", segmentHeaderPresent=" + this.segmentHeaderPresent + ", timeoutType="
               + this.timeoutType + ", timerInitial=" + this.timerInitial + ", transmissionLimit="
               + this.transmissionLimit + ", transmitterFrameSequenceNumber=" + this.transmitterFrameSequenceNumber
               + ", vcPriorityList=" + Arrays.toString(this.vcPriorityList) + ", vcPollingVector="
               + ((this.vcPollingVector != null) ? Arrays.toString(this.vcPollingVector) : "") + ", vcMuxScheme="
               + this.vcMuxScheme + ", virtualChannel=" + this.virtualChannel + ", parameterDiagnostic="
               + this.parameterDiagnostic + ", permittedTransmissionMode=" + this.permittedTransmissionMode
               + ", bitLockRequired=" + this.bitLockRequired + ", rfAvailableRequired=" + this.rfAvailableRequired
               + ", clcwGlobalVcid="+ ((this.clcwGlobalVcid != null) ? this.clcwGlobalVcid.toString() : "Not Configured") 
               + ", clcwPhysicalChannel="+this.clcwPhysicalChannel 
               + ", minReportingCycle="+this.minReportingCycle + ", copCntrFramesRepetition="+this.copCntrFramesRepetition 
               + ", seqCntrFramesRepetition="+this.seqCntrFramesRepetition + ", throwEventOperation="+this.throwEventOperation 
               
               + "]";
    }

}
