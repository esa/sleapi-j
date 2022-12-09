package esa.sle.impl.api.apise.fspse;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isrv.ifsp.IFSP_GetParameter;
import ccsds.sle.api.isrv.ifsp.IFSP_SIAdmin;
import ccsds.sle.api.isrv.ifsp.types.FSP_AbsolutePriority;
import ccsds.sle.api.isrv.ifsp.types.FSP_BlockingUsage;
import ccsds.sle.api.isrv.ifsp.types.FSP_ClcwGvcId;
import ccsds.sle.api.isrv.ifsp.types.FSP_ClcwPhysicalChannel;
import ccsds.sle.api.isrv.ifsp.types.FSP_MuxScheme;
import ccsds.sle.api.isrv.ifsp.types.FSP_ParameterName;
import ccsds.sle.api.isrv.ifsp.types.FSP_PermittedTransmissionMode;
import ccsds.sle.api.isrv.ifsp.types.FSP_ProductionStatus;
import esa.sle.impl.api.apise.slese.EE_APISE_PConfiguration;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * The class implements IFSP_SISadmin. The class holds all FSP configuration
 * parameters.
 */

public class EE_APISE_FSP_Configuration extends EE_APISE_PConfiguration implements IFSP_SIAdmin
{

    private static final Logger LOG = Logger.getLogger(EE_APISE_FSP_Configuration.class.getName());

    /**
     * The mission maximum TC transfer frame length in octets.
     */

    private long maximumFrameLength = 0;

    /**
     * The mission maximum telecommand packet length in octets.
     */
    public long maximumPacketLength = 0;

    /**
     * The VC multiplexing scheme in effect.
     */

    private FSP_MuxScheme vcMuxScheme = FSP_MuxScheme.fspMS_invalid;

    /**
     * The priority list for the VC multiplexing scheme 'absolute priority'.
     */

    private FSP_AbsolutePriority[] vcPriorityList = null;

    /**
     * The polling vector for the VC multiplexing scheme 'polling vector'.
     */

    private long[] vcPollingVector = null;

    /**
     * The blocking timeout in microseconds.
     */

    private long blockingTimeout = 0;

    /**
     * Defines whether blocking is permitted on the VC..
     */

    private FSP_BlockingUsage blockingUsage = FSP_BlockingUsage.fspAU_invalid;

    /**
     * Defines if the SI being configured is allowd to invoke directives.
     */
    private SLE_YesNo directiveInvocationEnabled = SLE_YesNo.sleYN_invalid;

    /**
     * Defines if a segment header is present in the TC transfer frame..
     */
    private SLE_YesNo segmentHeaderPresent = SLE_YesNo.sleYN_invalid;

    /**
     * The list of ApId's the SI is authorised to access.
     */

    private long[] apIdList = null;

    /**
     * The list of MapId's permitted to be used by the SI if MAPs are used.
     */

    private long[] mapIdList = null;

    /**
     * The VC used by the SI.
     */

    private long vcId = 0;

    /**
     * The permitted transmission mode.
     */
    public FSP_PermittedTransmissionMode permittedTransmissionMode = FSP_PermittedTransmissionMode.fspPTM_invalid;

    /**
     * The maximum packet buffer size in octets.
     */
    public long maxBufferSize = 0;
    

	/**
     * The CLCW global virtual channel ID structure for FSP: FSP ID = 29 - SLES ID = 202 
     */
    private FSP_ClcwGvcId gvcId;
    
    /**
     * The CLCW physical Channel as type string: FSP ID = 30 - SLES ID = 203
     */
    private FSP_ClcwPhysicalChannel clcwPhysicalChannel;
    
    /**
     * The cop-control-frames-repetition: FSP ID = 31 - SLES ID = 300 
     */
    private int copCntrFramesRepetition = 1; // if not configured, value shall be 1 (B5.4)
    
    /**
     * The seq-control-frames-repetition: FSP ID = 33 - SLES ID = 303 
     */
    private int seqCntrFramesRepetition = 1; // if not configured, value shall be 1 (B5.4)
    
    /**
     * The throw-event-operation: FSP ID = 34 - SLES ID = 304 
     */
    private SLE_YesNo throwEventOperation = SLE_YesNo.sleYN_No;
    
    /**
     * The repetition-limit parameter 
     */
    private int repetitionLimit = 1;  // if not configured, value shall be 1 (B5.4 & B5.5) 

    /**
     * The pointer to the SI.
     */
    private EE_APISE_FSP_PFSI fspSI = null;

    /**
     * Defines if a segment header is present in the TC transfer frame..
     */
    private SLE_YesNo bitLockRequired = SLE_YesNo.sleYN_invalid;

    /**
     * Defines if a segment header is present in the TC transfer frame..
     */
    private SLE_YesNo rfAvailableRequired = SLE_YesNo.sleYN_invalid;

    private final ReentrantLock obj = new ReentrantLock();


    @SuppressWarnings("unused")
    private EE_APISE_FSP_Configuration(final EE_APISE_FSP_Configuration right)
    {
        this.maximumFrameLength = right.maximumFrameLength;
        this.maximumPacketLength = right.maximumPacketLength;
        this.vcMuxScheme = right.vcMuxScheme;

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
        this.blockingTimeout = right.blockingTimeout;
        this.blockingUsage = right.blockingUsage;
        this.directiveInvocationEnabled = right.directiveInvocationEnabled;
        this.segmentHeaderPresent = right.segmentHeaderPresent;

        if (right.apIdList != null && right.apIdList.length > 0)
        {
            this.apIdList = new long[right.apIdList.length];
            System.arraycopy(right.apIdList, 0, this.apIdList, 0, right.apIdList.length);
        }

        if (right.mapIdList != null && right.mapIdList.length > 0)
        {
            this.mapIdList = new long[right.mapIdList.length];
            System.arraycopy(right.mapIdList, 0, this.mapIdList, 0, right.mapIdList.length);
        }

        this.vcId = right.vcId;
        this.permittedTransmissionMode = right.permittedTransmissionMode;
        this.maxBufferSize = right.maxBufferSize;
        this.fspSI = right.fspSI;

        this.bitLockRequired = right.bitLockRequired;
        this.rfAvailableRequired = right.rfAvailableRequired;
        this.clcwPhysicalChannel = right.clcwPhysicalChannel;
        this.gvcId = right.gvcId;
        this.copCntrFramesRepetition = right.copCntrFramesRepetition;
        this.seqCntrFramesRepetition = right.seqCntrFramesRepetition;
        this.throwEventOperation = right.throwEventOperation;
        this.repetitionLimit = right.repetitionLimit;
        this.setMinimumReportingCycle(right.getMinimumReportingCycle());
    }

    /**
     * Initializes the FSP Configuration object with the supplied data.
     * 
     * @param fspSI
     */
    public EE_APISE_FSP_Configuration(EE_APISE_FSP_PFSI fspSI)
    {
        this.fspSI = fspSI;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {

        if (iid == IFSP_SIAdmin.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }

    }

    @Override
    public void setMaximumFrameLength(long length)
    {
        this.obj.lock();
        this.maximumFrameLength = length;
        this.obj.unlock();
    }

    @Override
    public void setMaximumPacketLength(long length)
    {
        this.obj.lock();
        this.maximumPacketLength = length;
        this.obj.unlock();
    }

    @Override
    public void setVcMuxScheme(FSP_MuxScheme scheme)
    {
        this.obj.lock();
        this.vcMuxScheme = scheme;
        this.obj.unlock();
    }

    @Override
    public void setBlockingTimeout(long timeout)
    {
        this.obj.lock();
        this.blockingTimeout = timeout;
        this.obj.unlock();
    }

    @Override
    public void setBlockingUsage(FSP_BlockingUsage usage)
    {
        this.obj.lock();
        this.blockingUsage = usage;
        this.obj.unlock();

    }

    @Override
    public void setDirectiveInvocationEnabled(SLE_YesNo yesNo)
    {
        this.obj.lock();
        this.directiveInvocationEnabled = yesNo;
        this.obj.unlock();
    }

    @Override
    public void setSegmentHeaderPresent(SLE_YesNo yesNo)
    {
        this.obj.lock();
        this.segmentHeaderPresent = yesNo;
        this.obj.unlock();
    }

    @Override
    public void setVirtualChannel(long id)
    {
        this.obj.lock();
        this.vcId = id;
        this.obj.unlock();
    }

    @Override
    public void setPermittedTransmissionMode(FSP_PermittedTransmissionMode mode)
    {

        this.obj.lock();
        this.permittedTransmissionMode = mode;
        this.obj.unlock();
    }

    @Override
    public void setMaximumBufferSize(long size)
    {

        this.obj.lock();
        this.maxBufferSize = size;
        this.obj.unlock();
    }

    @Override
    public void setInitialProductionStatus(FSP_ProductionStatus status)
    {
        boolean configurationCompleted = this.fspSI.isConfigured();

        // this parameter must not be changed after configCompleted()
        if (configurationCompleted == true)
        {
            return;
        }
        this.obj.lock();
        this.fspSI.setInitialProductionStatus(status);
        this.obj.unlock();
    }

    @Override
    public void setInitialDirectiveInvocationOnline(SLE_YesNo yesNo)
    {
        boolean configurationCompleted = this.fspSI.isConfigured();
        // this parameter must not be changed after configCompleted()
        if (configurationCompleted == true)
        {
            return;
        }
        this.obj.lock();
        // ignore if directive invocation is true (SE-3.2)
        if (this.directiveInvocationEnabled == SLE_YesNo.sleYN_Yes)
        {
            this.obj.unlock();
            return;
        }
        this.fspSI.setInitialDirectiveInvocationOnline(yesNo);
        this.obj.unlock();
    }
    public FSP_ClcwGvcId getClcwGvcId() {
		return gvcId;
	}

	public void setClcwGvcId(FSP_ClcwGvcId gvcId) {
        if (gvcId != null)
        {
            this.gvcId = gvcId;
        }
        else
        {
            if (this.gvcId != null)
            {
                this.gvcId = null;
            }
        }
	}

	public FSP_ClcwPhysicalChannel getClcwPhysicalChannel() {
		return clcwPhysicalChannel;
	}

	public void setClcwPhysicalChannel(FSP_ClcwPhysicalChannel clcwPhysicalChannel) {
		this.clcwPhysicalChannel = clcwPhysicalChannel;
	}

	public int getCopCntrFramesRepetition() {
		return copCntrFramesRepetition;
	}

	public void setCopCntrFramesRepetition(int copCntrFramesRepetition) {
		this.copCntrFramesRepetition = copCntrFramesRepetition;
	}

	public int getSeqCntrFramesRepetition() {
		return seqCntrFramesRepetition;
	}

	public void setSeqCntrFramesRepetition(int seqCntrFramesRepetition) {
		this.seqCntrFramesRepetition = seqCntrFramesRepetition;
	}

	public SLE_YesNo getThrowEventOperation() {
		return throwEventOperation;
	}

	public void setThrowEventOperation(SLE_YesNo throwEventOperation) {
		this.throwEventOperation = throwEventOperation;
	}

    @Override
    public long getMaximumFrameLength()
    {
        return this.maximumFrameLength;
    }

    @Override
    public long getMaximumPacketLength()
    {
        return this.maximumPacketLength;
    }

    @Override
    public FSP_MuxScheme getVcMuxScheme()
    {
        return this.vcMuxScheme;
    }

    @Override
    public long getBlockingTimeout()
    {
        return this.blockingTimeout;
    }

    @Override
    public FSP_BlockingUsage getBlockingUsage()
    {
        return this.blockingUsage;
    }

    @Override
    public SLE_YesNo getDirectiveInvocationEnabled()
    {
        return this.directiveInvocationEnabled;
    }

    @Override
    public SLE_YesNo getSegmentHeaderPresent()
    {
        return this.segmentHeaderPresent;
    }

    @Override
    public long getVirtualChannel()
    {
        return this.vcId;
    }

    @Override
    public FSP_PermittedTransmissionMode getPermittedTransmissionMode()
    {
        return this.permittedTransmissionMode;
    }

    @Override
    public long getMaximumBufferSize()
    {
        return this.maxBufferSize;
    }

    /**
     * This method checks whether all parameters are set up to the latest
     * version. Earlier versions need to have the parameter set, too.
     * @return
     */
    public HRESULT doConfigCompleted()
    {

        HRESULT rc = HRESULT.S_OK;

        if (this.maximumFrameLength < EE_APISE_FSP_Limits.getMinFrameLength()
            || this.maximumFrameLength > EE_APISE_FSP_Limits.getMaxFrameLength())
        {
            logAlarm("Maximum Frame Length out of range");
            rc = HRESULT.SLE_E_CONFIG;
        }
        if (this.maximumPacketLength < EE_APISE_FSP_Limits.getMinPacketLength()
            || this.maximumPacketLength > EE_APISE_FSP_Limits.getMaxPacketLength())
        {
            logAlarm("Maximum Packet Length out of range");
            rc = HRESULT.SLE_E_CONFIG;
        }
        if (this.directiveInvocationEnabled == SLE_YesNo.sleYN_invalid)
        {
            logAlarm("Invalid or missing Directive Invocation Enabled");
            rc = HRESULT.SLE_E_CONFIG;
        }

        // API Version 3.4 modification - FSP Version 2
        if (this.bitLockRequired == SLE_YesNo.sleYN_invalid)
        {
            logAlarm("Invalid or missing Bit Lock Required");
            rc = HRESULT.SLE_E_CONFIG;
        }
        if (this.rfAvailableRequired == SLE_YesNo.sleYN_invalid)
        {
            logAlarm("Invalid or missing Rf Available Required");
            rc = HRESULT.SLE_E_CONFIG;
        }
        // End modification for API Version 3.4 - FSP Version 2

        // check the mux scheme and the corresponding vectors:
        if (this.vcMuxScheme == FSP_MuxScheme.fspMS_invalid)
        {
            logAlarm("Invalid or missing VC Mux Scheme");
            rc = HRESULT.SLE_E_CONFIG;
        }
        if (this.vcMuxScheme == FSP_MuxScheme.fspMS_absolutePriority)
        {
            if (this.vcPriorityList == null || this.vcPriorityList.length == 0)
            {
                logAlarm("Empty VC Priority List");
                rc = HRESULT.SLE_E_CONFIG;
            }
            // check the values to be in limits
            for (FSP_AbsolutePriority element : this.vcPriorityList)
            {
                if (element.getMapOrVc() > EE_APISE_FSP_Limits.getMaxMapOrVcId())
                {
                    logAlarm("VC Priority List: VC Id out of range");
                    rc = HRESULT.SLE_E_CONFIG;
                }
                if (element.getPriority() < EE_APISE_FSP_Limits.getMinAbsolutePriority()
                    || element.getPriority() > EE_APISE_FSP_Limits.getMaxAbsolutePriority())
                {
                    logAlarm("VC Priority List: priority out of range");
                    rc = HRESULT.SLE_E_CONFIG;
                }
            }
        }

        else if (this.vcMuxScheme == FSP_MuxScheme.fspMS_pollingVector)
        {
            if (this.vcPollingVector == null || this.vcPollingVector.length == 0)
            {
                logAlarm("Empty VC Polling Vector");
                rc = HRESULT.SLE_E_CONFIG;
            }
            // check the vc polling vector values to be in limits
            for (long element : this.vcPollingVector)
            {
                if (element > EE_APISE_FSP_Limits.getMaxMapOrVcId())
                {
                    logAlarm("VC Polling Vector: VC Id out of range");
                    rc = HRESULT.SLE_E_CONFIG;
                }
            }
        }
        // if vcMuxScheme is fifo: nothing more to check.

        // check the application Id List
        if (this.apIdList == null || this.apIdList.length == 0)
        {
            logAlarm("Empty Application Id List");
            rc = HRESULT.SLE_E_CONFIG;
        }
        for (long element : this.apIdList)
        {
            if (element > EE_APISE_FSP_Limits.getMaxApId())
            {
                logAlarm("Application Id List: Application Id out of range");
                rc = HRESULT.SLE_E_CONFIG;
            }
        }

        // check if the segment header is present:
        if (this.segmentHeaderPresent == SLE_YesNo.sleYN_invalid)
        {
            logAlarm("Parameter 'Segment Header Present' invalid or missing");
            rc = HRESULT.SLE_E_CONFIG;
        }
        else if (this.segmentHeaderPresent == SLE_YesNo.sleYN_Yes)
        {
            // the MAP usage is defined in the segment header,
            // therefore check in this case if MAP list is valid
            if (this.mapIdList == null || this.mapIdList.length == 0)
            {
                logAlarm("Empty MAP Id List (Segment Header present)");
                rc = HRESULT.SLE_E_CONFIG;
            }
            // check the map Id List values to be in limits
            for (long element : this.mapIdList)
            {
                if (element > EE_APISE_FSP_Limits.getMaxMapOrVcId())
                {
                    logAlarm("MAP Id List: MAP Id out of range");
                    rc = HRESULT.SLE_E_CONFIG;
                }
            }
        }
        // if the segment header is not present, no check on
        // the MAPs need to be performed.

        if (this.maxBufferSize == 0)
        {
            logAlarm("Invalid or missing Max Packet Buffer Size");
            rc = HRESULT.SLE_E_CONFIG;
        }
        // no further checks on the max buffer size shall be made

        if (this.vcId > EE_APISE_FSP_Limits.getMaxMapOrVcId())
        {
            logAlarm("VC Id out of range");
            rc = HRESULT.SLE_E_CONFIG;
        }

        // check blocking usage:
        if (this.blockingUsage == FSP_BlockingUsage.fspAU_invalid)
        {
            logAlarm("Invalid or missing 'Blocking Usage' parameter");
            rc = HRESULT.SLE_E_CONFIG;
        }
        else if (this.blockingUsage == FSP_BlockingUsage.fspAU_permitted && this.blockingTimeout == 0)
        {
            logAlarm("Invalid or missing Blocking Timeout (Blocking Usage permitted)");
            rc = HRESULT.SLE_E_CONFIG;
        }

        // check if the transmission mode is set
        if (this.permittedTransmissionMode == FSP_PermittedTransmissionMode.fspPTM_invalid)
        {
            logAlarm("Invalid or missing Permitted Transmission Mode");
            rc = HRESULT.SLE_E_CONFIG;
        }
        
//        // For earlier versions of SLES V5 the values are set in
//        // TestTool
//        // check the ClcwGvcId
//        if (this.gvcId == null || this.gvcId.getConfigType() == FSP_ConfType.fspCT_invalid)
//        {
//        	logAlarm("Invalid or missing ClcwGvcId");
//            rc = HRESULT.SLE_E_CONFIG;
//        }
//        // check the ClcwPhysicalChannel
//        if (this.clcwPhysicalChannel == null || this.clcwPhysicalChannel.getConfigType() == FSP_ConfType.fspCT_invalid)
//        {
//        	logAlarm("Invalid or missing ClcwPhysicalChannel");
//            rc = HRESULT.SLE_E_CONFIG;
//        }
//        // check the copCntrFramesRepetition
//        if (this.copCntrFramesRepetition == 0 || this.copCntrFramesRepetition > this.repetitionLimit)
//        {
//        	logAlarm("Invalid or missing COP Control Frames Repetition");
//            rc = HRESULT.SLE_E_CONFIG;
//        }
//        // check the seqCntrFramesRepetition
//        if (this.seqCntrFramesRepetition == 0 || this.copCntrFramesRepetition > this.repetitionLimit)
//        {
//        	logAlarm("Invalid or missing COP Control Frames Repetition");
//            rc = HRESULT.SLE_E_CONFIG;
//        }
//        // check the throwEventOperation
//        if (this.throwEventOperation == SLE_YesNo.sleYN_invalid)
//        {
//            logAlarm("Invalid or missing Throw Event Operation Enabled");
//            rc = HRESULT.SLE_E_CONFIG;
//        }
        return rc;

    }

    public HRESULT setUpGetParameter(IFSP_GetParameter prm)
    {

        FSP_ParameterName pname = prm.getRequestedParameter();

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(" pname: " + pname);
        }

        switch (pname)
        {
        case fspPN_bitLockRequired:
        {
            prm.setBitLockRequired(this.bitLockRequired);
            return HRESULT.S_OK;
        }
        case fspPN_rfAvailableRequired:
        {
            prm.setRfAvailableRequired(this.rfAvailableRequired);
            return HRESULT.S_OK;
        }
        case fspPN_blockingTimeoutPeriod:
        {
            prm.setBlockingTimeout(this.blockingTimeout);
            return HRESULT.S_OK;
        }
        case fspPN_blockingUsage:
        {
            prm.setBlockingUsage(this.blockingUsage);
            return HRESULT.S_OK;
        }
        case fspPN_apidList:
        {
            prm.setApIdList(this.apIdList);
            return HRESULT.S_OK;
        }
        case fspPN_deliveryMode:
        {
            prm.setDeliveryMode();
            return HRESULT.S_OK;
        }
        case fspPN_directiveInvocationEnabled:
        {
            prm.setDirectiveInvocationEnabled(this.directiveInvocationEnabled);
            return HRESULT.S_OK;
        }
        case fspPN_mapList:
        {
            prm.setMapList(this.mapIdList);
            return HRESULT.S_OK;
        }
        case fspPN_maximumFrameLength:
        {
            prm.setMaxFrameLength(this.maximumFrameLength);
            return HRESULT.S_OK;
        }
        case fspPN_maximumPacketLength:
        {
            prm.setMaxPacketLength(this.maximumPacketLength);
            return HRESULT.S_OK;
        }
        case fspPN_segmentHeader:
        {
            prm.setSegmentHeaderPresent(this.segmentHeaderPresent);
            return HRESULT.S_OK;
        }
        case fspPN_vcMuxControl:
        {
            if (this.vcMuxScheme == FSP_MuxScheme.fspMS_absolutePriority)
            {
                prm.setVcPriorityList(this.vcPriorityList);
            }
            else if (this.vcMuxScheme == FSP_MuxScheme.fspMS_pollingVector)
            {
                prm.setVcPollingVector(this.vcPollingVector);
            }
            else
            {
                // map mux scheme is fifo, nothing to set.
                prm.setVcPriorityList(null);
            }
            return HRESULT.S_OK;
        }
        case fspPN_vcMuxScheme:
        {
            prm.setVcMuxScheme(this.vcMuxScheme);
            return HRESULT.S_OK;
        }
        case fspPN_virtualChannel:
        {
            prm.setVirtualChannel(this.vcId);
            return HRESULT.S_OK;
        }
        case fspPN_permittedTransmissionMode:
        {
            prm.setPermittedTransmissionMode(this.permittedTransmissionMode);
            return HRESULT.S_OK;
        }
        case fspPN_clcwGlobalVcId:
        {
            prm.setClcwGlobalVcid(this.gvcId);
            return HRESULT.S_OK;
        }
        case fspPN_clcwPhysicalChannel:
        {
            prm.setClcwPhysicalChannel(this.clcwPhysicalChannel);
            return HRESULT.S_OK;
        }
        case fspPN_copCntrFramesRepetion:
        {
            prm.setCopCntrFramesRepetition(this.copCntrFramesRepetition);
            return HRESULT.S_OK;
        }
        case fspPN_seqCntrFramesRepetition:
        {
            prm.setSeqCntrFramesRepetition(this.seqCntrFramesRepetition);
            return HRESULT.S_OK;
        }
        case fspPN_throwEventOperation:
        {
            prm.setThrowEventOperation(this.throwEventOperation);
            return HRESULT.S_OK;
        }
        case fspPN_minReportingCycle:
        {
        	prm.setMinReportingCycle(this.getMinimumReportingCycle());
        	return HRESULT.S_OK;
        }
        default:
            return HRESULT.SLE_E_UNKNOWN;
        }

    }

    public boolean isMapIdListMember(long mapId)
    {

        if (this.mapIdList == null || this.mapIdList.length == 0)
        {
            return false;
        }

        for (long element : this.mapIdList)
        {
            if (element == mapId)
            {
                return true;
            }
        }

        return false;

    }

    /**
     * Issues a configuration alarm with the message supplied as argument.
     */
    public void logAlarm(String msg)
    {
        this.fspSI.logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_ConfigError.getCode(), msg);
    }

    /**
     * See specification of IFSP_SIAdmin.
     */
    @Override
    public void setBitLockRequired(SLE_YesNo yesNo)
    {
        this.obj.lock();
        this.bitLockRequired = yesNo;
        this.obj.unlock();
    }

    @Override
    public void setRfAvailableRequired(SLE_YesNo yesNo)
    {
        this.obj.lock();
        this.rfAvailableRequired = yesNo;
        this.obj.unlock();
    }

    @Override
    public SLE_YesNo getBitLockRequired()
    {
        return this.bitLockRequired;

    }

    @Override
    public SLE_YesNo getRfAvailableRequired()
    {
        return this.rfAvailableRequired;
    }

    @Override
    public void setVcPriorityList(FSP_AbsolutePriority[] priorities)
    {
        this.obj.lock();
        this.vcPriorityList = new FSP_AbsolutePriority[priorities.length];
        for (int i = 0; i < priorities.length; i++)
        {
        	this.vcPriorityList[i] = new FSP_AbsolutePriority();
            this.vcPriorityList[i].setMapOrVc(priorities[i].getMapOrVc());
            this.vcPriorityList[i].setPriority(priorities[i].getPriority());
        }
        this.obj.unlock();
    }

    @Override
    public void setVcPollingVector(long[] pvec)
    {
        this.obj.lock();
        this.vcPollingVector = new long[pvec.length];
        for (int i = 0; i < pvec.length; i++)
        {
            this.vcPollingVector[i] = pvec[i];
        }
        this.obj.unlock();
    }

    @Override
    public void setApIdList(long[] plist)
    {
        this.obj.lock();
        this.apIdList = new long[plist.length];
        for (int i = 0; i < plist.length; i++)
        {
            this.apIdList[i] = plist[i];
        }
        this.obj.unlock();

    }

    @Override
    public void setMapList(long[] plist)
    {
        this.obj.lock();
        this.mapIdList = new long[plist.length];
        for (int i = 0; i < plist.length; i++)
        {
            this.mapIdList[i] = plist[i];
        }
        this.obj.unlock();
    }
    
	@Override
	public void setRepetitionLimit(int repetitionLimit) {
		this.repetitionLimit = repetitionLimit;
	}

    @Override
    public FSP_AbsolutePriority[] getVcPriorityList()
    {
        return this.vcPriorityList;
    }

    @Override
    public long[] getVcPollingVector()
    {

        return this.vcPollingVector;
    }

    @Override
    public long[] getApIdList()
    {
        return this.apIdList;
    }

    @Override
    public long[] getMapList()
    {
        return this.mapIdList;
    }
    @Override
	public int getRepetitionLimit() {
		return repetitionLimit;
	}

}
