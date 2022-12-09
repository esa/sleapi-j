package esa.sle.impl.api.apise.rocfse;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iop.ISLE_Stop;
import ccsds.sle.api.isle.iop.ISLE_TransferBuffer;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SIOpFactory;
import ccsds.sle.api.isle.it.SLE_AbortOriginator;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.it.SLE_SIState;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.irocf.IROCF_GetParameter;
import ccsds.sle.api.isrv.irocf.IROCF_SIAdmin;
import ccsds.sle.api.isrv.irocf.IROCF_SIUpdate;
import ccsds.sle.api.isrv.irocf.IROCF_Start;
import ccsds.sle.api.isrv.irocf.IROCF_StatusReport;
import ccsds.sle.api.isrv.irocf.IROCF_SyncNotify;
import ccsds.sle.api.isrv.irocf.types.ROCF_ChannelType;
import ccsds.sle.api.isrv.irocf.types.ROCF_ControlWordType;
import ccsds.sle.api.isrv.irocf.types.ROCF_DeliveryMode;
import ccsds.sle.api.isrv.irocf.types.ROCF_GetParameterDiagnostic;
import ccsds.sle.api.isrv.irocf.types.ROCF_Gvcid;
import ccsds.sle.api.isrv.irocf.types.ROCF_LockStatus;
import ccsds.sle.api.isrv.irocf.types.ROCF_NotificationType;
import ccsds.sle.api.isrv.irocf.types.ROCF_ParameterName;
import ccsds.sle.api.isrv.irocf.types.ROCF_ProductionStatus;
import ccsds.sle.api.isrv.irocf.types.ROCF_StartDiagnostic;
import ccsds.sle.api.isrv.irocf.types.ROCF_UpdateMode;
import esa.sle.impl.api.apise.slese.EE_APISE_PConfiguration;
import esa.sle.impl.api.apise.slese.EE_APISE_PRSI;
import esa.sle.impl.api.apise.slese.types.EE_TI_SLESE_Event;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * This class provides the functionality that is specific to ROCF return service
 * instances for provider applications. It is responsible for the ROCF specific
 * configuration of the service instance and the update of the service
 * parameters by implementing the interfaces IROCF_SIAdmin and IROCF_SIUpdate.
 * The class also implements the interface ISLE_SIOpFactory, which allows the
 * client to obtain pre-configured operation objects supported by the ROCF
 * service. The ROCF-PRSI provides the following functionality: - it checks the
 * operation invocations and returns to be compatible with the ROCF service -
 * processes and sets-up GET-PARAMETER operations according to the configuration
 * and status information parameters - it updates status information on request
 * - it creates, initializes and sends STATUS-REPORT operations - it forwards
 * operation objects to the base-class for further processing (if not supported
 * by this class)@EndResponsibility The class performs ROCF service specific
 * checks of operation objects received from the application and the proxy. The
 * class implements do<Initiate/Inform>OpInvoke() and
 * do<Initiate/Inform>OpReturn exported by the base-class. These functions look
 * at the operation-type and pass the operation invocation to the appropriate
 * function (<opType>Inv() or <opType>Rtn()), which performs the specific
 * checks. After successful checking the operation invocations/returns
 * state-processing is executed by a call to doStateProcessing(). The base-class
 * calls updateStatusInfo() whenever a TRANSFER-BUFFER has been transmitted to
 * the service user. The function updateStatusInfo() obtains the
 * 'number-of-frames-processed' parameter for status-updates from the supplied
 * operation. The ROCF-PRSI-class sets the parameter for the GET-PARAMETER
 * invocation (doGetParameter() method) and passes the return-PDU back to the
 * proxy.
 */
public class EE_APISE_ROCF_PRSI extends EE_APISE_PRSI implements ISLE_SIOpFactory, IROCF_SIAdmin, IROCF_SIUpdate
{

    private static final Logger LOG = Logger.getLogger(EE_APISE_ROCF_PRSI.class.getName());

    private final EE_APISE_ROCF_Configuration config = new EE_APISE_ROCF_Configuration();

    private final EE_APISE_ROCF_StatusInformation statusInfo = new EE_APISE_ROCF_StatusInformation();

    private final ReentrantLock obj = new ReentrantLock();


    /**
     * The constructor, to be used for service instance creation, it passes the
     * supplied arguments to the base-class.
     */
    public EE_APISE_ROCF_PRSI(String instanceKey, ISLE_ServiceInform clientIf)
    {
        super(instanceKey, SLE_ApplicationIdentifier.sleAI_rtnChOcf, clientIf);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        T ppv = super.queryInterface(iid);
        if (ppv != null)
        {
            return ppv;
        }
        else
        {
            if (iid == IROCF_SIAdmin.class)
            {
                return (T) this;
            }
            else if (iid == IROCF_SIUpdate.class)
            {
                return (T) this;
            }
            else if (iid == ISLE_SIOpFactory.class)
            {
                return (T) this;
            }
            else
            {
                return null;
            }
        }

    }

    @Override
    public void setDeliveryMode(ROCF_DeliveryMode mode)
    {

        if (isConfigured() == true)
        {
            return;
        }
        this.config.setDeliveryMode(mode.asSLE_DeliveryMode());
    }

    @Override
    public void setLatencyLimit(int limit)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.config.setLatencyLimit(limit);
    }

    @Override
    public void setTransferBufferSize(long size)
    {

        if (isConfigured() == true)
        {
            return;
        }
        this.config.setTransferBufferSize(size);

    }

    @Override
    public void setInitialProductionStatus(ROCF_ProductionStatus status)
    {

        if (isConfigured() == true)
        {
            return;
        }
        this.statusInfo.setProductionStatus(status);
    }

    @Override
    public void setInitialFrameSyncLock(ROCF_LockStatus status)
    {

        if (isConfigured() == true)
        {
            return;
        }
        this.statusInfo.setFrameSyncLock(status);
    }

    @Override
    public void setInitialCarrierDemodLock(ROCF_LockStatus status)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.statusInfo.setCarrierDemodLock(status);
    }

    @Override
    public void setInitialSubCarrierDemodLock(ROCF_LockStatus status)
    {

        if (isConfigured() == true)
        {
            return;
        }
        this.statusInfo.setSubCarrDemodLock(status);
    }

    @Override
    public void setInitialSymbolSyncLock(ROCF_LockStatus status)
    {

        if (isConfigured() == true)
        {
            return;
        }
        this.statusInfo.setSymbolSyncLock(status);

    }

    @Override
    public ROCF_DeliveryMode getDeliveryMode()
    {

        return this.config.getDeliveryMode().asROCF_DeliveryMode();
    }

    @Override
    public int getLatencyLimit()
    {
        return this.config.getLatencyLimit();
    }

    @Override
    public long getTransferBufferSize()
    {
        return this.config.getTransferBufferSize();
    }

    @Override
    public void setNumFramesProcessed(long count)
    {
        this.statusInfo.lock();
        this.statusInfo.setNumFramesProcessed(count);
        this.statusInfo.unlock();

    }

    @Override
    public void setProductionStatus(ROCF_ProductionStatus status)
    {
        this.statusInfo.lock();
        this.statusInfo.setProductionStatus(status);
        this.statusInfo.unlock();
    }

    @Override
    public void setFrameSyncLock(ROCF_LockStatus status)
    {

        this.statusInfo.lock();
        this.statusInfo.setFrameSyncLock(status);
        this.statusInfo.unlock();
    }

    @Override
    public void setCarrierDemodLock(ROCF_LockStatus status)
    {
        this.statusInfo.lock();
        this.statusInfo.setCarrierDemodLock(status);
        this.statusInfo.unlock();
    }

    @Override
    public void setSubCarrierDemodLock(ROCF_LockStatus status)
    {
        this.statusInfo.lock();
        this.statusInfo.setSubCarrDemodLock(status);
        this.statusInfo.unlock();
    }

    @Override
    public void setSymbolSyncLock(ROCF_LockStatus status)
    {

        this.statusInfo.lock();
        this.statusInfo.setSymbolSyncLock(status);
        this.statusInfo.unlock();
    }
    
    /**
     * Set the configured minimum reporting cycle
     * @since SLES V5.
     */
    @Override
    public void setMinimumReportCycle(long mrc)
    {
    	if (isConfigured() == true)
        {
            return;
        }
        this.config.setMinimumReportingCycle(mrc);
    }

    @Override
    public ROCF_ProductionStatus getProductionStatus()
    {
        this.statusInfo.lock();
        ROCF_ProductionStatus ps = this.statusInfo.getProductionStatus();
        this.statusInfo.unlock();
        return ps;
    }

    @Override
    public ROCF_LockStatus getFrameSyncLock()
    {
        this.statusInfo.lock();
        ROCF_LockStatus ls = this.statusInfo.getFrameSyncLock();
        this.statusInfo.unlock();
        return ls;
    }

    @Override
    public ROCF_LockStatus getCarrierDemodLock()
    {

        this.statusInfo.lock();
        ROCF_LockStatus ls = this.statusInfo.getCarrierDemodLock();
        this.statusInfo.unlock();
        return ls;

    }

    @Override
    public ROCF_LockStatus getSubCarrierDemodLock()
    {
        this.statusInfo.lock();
        ROCF_LockStatus ls = this.statusInfo.getSubCarrDemodLock();
        this.statusInfo.unlock();
        return ls;
    }

    @Override
    public ROCF_LockStatus getSymbolSyncLock()
    {

        this.statusInfo.lock();
        ROCF_LockStatus ls = this.statusInfo.getSymbolSyncLock();
        this.statusInfo.unlock();
        return ls;

    }

    @Override
    public long getNumFramesProcessed()
    {
        this.statusInfo.lock();
        long nf = this.statusInfo.getNumFramesProcessed();
        this.statusInfo.unlock();
        return nf;
    }

    @Override
    public long getNumOcfDelivered()
    {
        this.statusInfo.lock();
        long nf = this.statusInfo.getNumOcfDelivered();
        this.statusInfo.unlock();
        return nf;
    }

    @Override
    public ROCF_Gvcid getRequestedGvcid()
    {
        // according to spec the return value is dependent of the SI state
        if (getSIState() != SLE_SIState.sleSIS_active)
        {
            //return null;
            // SLES V5 (Page3-54): Changed from 'invalid/undefined' to 
        	// first element of permitted-global-VCID-set
        	return getPermittedGvcidSet()[0];
        }
        else
        {
            this.statusInfo.lock();
            ROCF_Gvcid gvcId = this.statusInfo.getReqGlobalVcId();
            if (gvcId != null)
            {
                this.statusInfo.unlock();
                return null;
            }

            ROCF_Gvcid newGvcId = new ROCF_Gvcid();
            newGvcId = gvcId;
            this.statusInfo.unlock();
            return newGvcId;
        }
    }

    @Override
    public ROCF_ControlWordType getRequestedControlWordType()
    {
        if (getSIState() != SLE_SIState.sleSIS_active)
        {
            // SLES V5(Page3-53): Changed from 'invalid/undefined' to 
        	// first element of permitted-control-word-type-set
        	return ROCF_ControlWordType.getControlWordTypeByCode(0);
        }
        this.statusInfo.lock();
        ROCF_ControlWordType cwt = this.statusInfo.getReqControlWordType();
        this.statusInfo.unlock();
        return cwt;
    }

    @Override
    public boolean getTcVcidUsed()
    {
        this.statusInfo.lock();
        boolean tcVcidUsed = this.statusInfo.getTcVcidUsed();
        this.statusInfo.unlock();
        return tcVcidUsed;
    }

    @Override
    public long getRequestedTcVcid()
    {
    	if (getSIState() != SLE_SIState.sleSIS_active)
        {
            // SLES V5 (Page3-54): Changed from 'invalid/undefined' to 
        	// first element of permitted-tc-vcid-set
    		long pVcSet[] = getPermittedTcVcidSet();
    		if(pVcSet.length == 0){
    			// if the permitted TcVcId set is 'Not Set'
    			// is empty return 0 'noTcVC'
    			return 0;
    		}
        	return pVcSet[0];
        }
        this.statusInfo.lock();
        long tcVcid = this.statusInfo.getReqTcVcid();
        this.statusInfo.unlock();
        return tcVcid;
    }

    @Override
    public ROCF_UpdateMode getRequestedUpdateMode()
    {

        if (getSIState() != SLE_SIState.sleSIS_active)
        {
        	// SLES V5 (Page3-54): Changed from 'invalid/undefined' to 
        	// first element of permitted-update-mode-set
            return getPermittedUpdateModeSet()[0];
        }

        this.statusInfo.lock();
        ROCF_UpdateMode upm = this.statusInfo.getReqUpdateMode();
        this.statusInfo.unlock();
        return upm;

    }

    @Override
    public <T extends ISLE_Operation> T createOperation(Class<T> iid, SLE_OpType optype) throws SleApiException
    {

        if (isConfigured() == false)
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        // check operation type supported for a ROCF service provider SI:
        if (optype != SLE_OpType.sleOT_transferData && optype != SLE_OpType.sleOT_syncNotify
            && optype != SLE_OpType.sleOT_peerAbort)
        {
            throw new SleApiException(HRESULT.SLE_E_TYPE);
        }

        ISLE_OperationFactory opf = getOpFactory();
        T ppv = null;
        if(optype == SLE_OpType.sleOT_scheduleStatusReport)
        {
        	ppv = opf.createOperation(iid, optype, SLE_ApplicationIdentifier.sleAI_rtnChOcf, getVersion(), getMinimumReportCycle());
        }
        else
        {
        	ppv = opf.createOperation(iid, optype, SLE_ApplicationIdentifier.sleAI_rtnChOcf, getVersion());
        }
        //T ppv = opf.createOperation(iid, optype, SLE_ApplicationIdentifier.sleAI_rtnChOcf, getVersion());

        // no specific set-up for TRANSFER-DATA required
        // no specific set-up for SYNC-NOTIFY required

        // PEER-ABOR-specific set-up supported by base-class:
        setUpOperation(optype, ppv);

        return ppv;
    }

    /**
     * Forces the sending of the transfer buffer to the application, regardless
     * of the buffer size and of the latency limit.
     */
    @Override
    protected HRESULT sendBuffer(boolean withNotification)
    {
        HRESULT rc = HRESULT.S_OK;
        this.obj.lock();
        rc = super.sendBuffer(withNotification);
        this.obj.unlock();
        return rc;
    }

    @Override
    protected EE_APISE_PConfiguration getConfiguration()
    {
        return this.config;
    }

    /**
     * Performs ROCF provider service instance specific configuration checks.
     */
    @Override
    protected HRESULT doConfigCompleted()
    {

        HRESULT rc = super.doConfigCompleted();
        if (rc != HRESULT.S_OK)
        {
            return rc;
        }

        // check delivery mode

        ROCF_DeliveryMode dm = this.config.getDeliveryMode().asROCF_DeliveryMode();
        if (dm == ROCF_DeliveryMode.rocfDM_invalid)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                      EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                      "Invalid or missing Delivery Mode");
            return HRESULT.SLE_E_CONFIG;
        }

        long bufferSize = this.config.getTransferBufferSize();
        if (bufferSize < EE_APISE_ROCF_Limits.getMinBufferSize())
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                      EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                      "Transfer Buffer Size invalid or missing");
            return HRESULT.SLE_E_CONFIG;
        }

        ROCF_Gvcid[] permGvcIdList = this.config.getPermittedGvcIdSet();

        if (permGvcIdList == null)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                      EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                      "Permitted GVCID list missing");
            return HRESULT.SLE_E_CONFIG;
        }
        for (ROCF_Gvcid element : permGvcIdList)
        {
            if (element.getType() == ROCF_ChannelType.rocfCT_invalid
                || element.getScid() > EE_APISE_ROCF_Limits.getGvcIdMaxScId()
                || element.getVersion() > EE_APISE_ROCF_Limits.getGvcIdMaxVersion()
                || element.getVcid() > EE_APISE_ROCF_Limits.getGvcIdMaxVcId())
            {
                logRecord(SLE_LogMessageType.sleLM_alarm,
                          EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                          "Permitted GVCID list: value out of range");
                return HRESULT.SLE_E_CONFIG;
            }
        }

        // Check the permitted Control Word Type Set
        ROCF_ControlWordType[] cwtSet = this.config.getPermittedControlWordTypeSet();
        if (cwtSet == null)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                      EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                      "Permitted Control Word Types missing");
            return HRESULT.SLE_E_CONFIG;
        }
        for (ROCF_ControlWordType element : cwtSet)
        {
            if (element == ROCF_ControlWordType.rocfCWT_invalid)
            {
                logRecord(SLE_LogMessageType.sleLM_alarm,
                          EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                          "Permitted Control Word Type invalid");
                return HRESULT.SLE_E_CONFIG;
            }
        }

        // Check the permitted Tc VcId List
        long[] tcVcid = this.config.getPermittedTcVcidSet();
        if (tcVcid == null)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                      EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                      "Permitted Tc VcId Set missing");
            return HRESULT.SLE_E_CONFIG;
        }
        for (long element : tcVcid)
        {
            if (element > EE_APISE_ROCF_Limits.getMaxTcVcId())
            {
                logRecord(SLE_LogMessageType.sleLM_alarm,
                          EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                          "Permitted Tc VcId invalid");
                return HRESULT.SLE_E_CONFIG;
            }
        }

        // Check the permitted Update Mode Set
        ROCF_UpdateMode[] upm = this.config.getPermittedUpdateModeSet();
        if (upm == null)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                      EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                      "Permitted Update Mode Set missing");
            return HRESULT.SLE_E_CONFIG;
        }
        for (ROCF_UpdateMode element : upm)
        {
            if (element == ROCF_UpdateMode.rocfUM_invalid)
            {
                logRecord(SLE_LogMessageType.sleLM_alarm,
                          EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                          "Permitted Update Mode invalid");
                return HRESULT.SLE_E_CONFIG;
            }
        }

        // check the latency-limit
        if ((dm == ROCF_DeliveryMode.rocfDM_timelyOnline) || (dm == ROCF_DeliveryMode.rocfDM_completeOnline))
        {
            int ll = this.config.getLatencyLimit();
            if (ll < EE_APISE_ROCF_Limits.getMinLatencyLimit())
            {
                logRecord(SLE_LogMessageType.sleLM_alarm,
                          EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                          "Latency Limit out of range");
                return HRESULT.SLE_E_CONFIG;
            }
        }
        else
        {
            this.config.setLatencyLimit(0);
        }

        // set-up according to SE-2.1 and sect. 4.3.2 of ROCF-spec:
        // set values for delivery mode offline, if not set via
        // IROCF_SIAdmin:
        this.statusInfo.lock();
        if (dm == ROCF_DeliveryMode.rocfDM_offline)
        {
            if (this.statusInfo.getFrameSyncLock() == ROCF_LockStatus.rocfLS_invalid)
            {
                this.statusInfo.setFrameSyncLock(ROCF_LockStatus.rocfLS_unknown);
            }
            if (this.statusInfo.getSymbolSyncLock() == ROCF_LockStatus.rocfLS_invalid)
            {
                this.statusInfo.setSymbolSyncLock(ROCF_LockStatus.rocfLS_unknown);
            }
            if (this.statusInfo.getSubCarrDemodLock() == ROCF_LockStatus.rocfLS_invalid)
            {
                this.statusInfo.setSubCarrDemodLock(ROCF_LockStatus.rocfLS_unknown);
            }
            if (this.statusInfo.getCarrierDemodLock() == ROCF_LockStatus.rocfLS_invalid)
            {
                this.statusInfo.setCarrierDemodLock(ROCF_LockStatus.rocfLS_unknown);
            }
        }
        else
        {
            if (this.statusInfo.getProductionStatus() == ROCF_ProductionStatus.rocfPS_invalid)
            {
                this.statusInfo.unlock();
                logRecord(SLE_LogMessageType.sleLM_alarm,
                          EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                          "Initial Production Status missing");
                return HRESULT.SLE_E_CONFIG;
            }

            if (this.statusInfo.getFrameSyncLock() == ROCF_LockStatus.rocfLS_invalid
                || this.statusInfo.getSymbolSyncLock() == ROCF_LockStatus.rocfLS_invalid
                || this.statusInfo.getSubCarrDemodLock() == ROCF_LockStatus.rocfLS_invalid
                || this.statusInfo.getCarrierDemodLock() == ROCF_LockStatus.rocfLS_invalid)
            {
                this.statusInfo.unlock();
                logRecord(SLE_LogMessageType.sleLM_alarm,
                          EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                          "Initial Lock Status missing");
                return HRESULT.SLE_E_CONFIG;
            }
        }

        // set-up remaining initial values acc. to SE2.2 and SE2.3, SE2.4 and
        // 4.3.2 of ROCF-spec:

        this.statusInfo.setNumFramesProcessed(0);
        this.statusInfo.setNumOcfDelivered(0);
        this.statusInfo.setReqGlobalVcId(null);
        this.statusInfo.setReqControlWordType(ROCF_ControlWordType.rocfCWT_invalid);
        this.statusInfo.setTcVcidUsed(false);
        this.statusInfo.setReqTcVcid(0);
        this.statusInfo.setReqUpdateMode(ROCF_UpdateMode.rocfUM_invalid);

        this.statusInfo.unlock();

        return HRESULT.S_OK;

    }

    /**
     * Obtains all TRANSFER-DATA operations from the transfer-buffer, increments
     * the number of delivered frames.
     */
    @Override
    protected void updateStatusInfo(ISLE_TransferBuffer transmittedBuffer)
    {

        ISLE_Operation op = null;
        transmittedBuffer.reset();
        while (transmittedBuffer.moreData())
        {
            op = transmittedBuffer.next();
            if (op.getOperationType() == SLE_OpType.sleOT_transferData)
            {
                this.statusInfo.lock();
                long numOcf = this.statusInfo.getNumOcfDelivered();
                this.statusInfo.setNumOcfDelivered(numOcf + 1);
                this.statusInfo.unlock();
            }
        }
    }

    /**
     * Creates a new ROCF specific status report operation and initialises it.
     * When all values have been set, it passes the operation to the interface
     * ISLE_SrvProxyInitiate for transmission to the user.
     */
    @Override
    protected HRESULT doStatusReport()
    {

        ISLE_OperationFactory opf = getOpFactory();

        IROCF_StatusReport sr = null;
        HRESULT rc = HRESULT.S_OK;

        try
        {
            sr = opf.createOperation(IROCF_StatusReport.class,
                                     SLE_OpType.sleOT_statusReport,
                                     SLE_ApplicationIdentifier.sleAI_rtnChOcf,
                                     getVersion());
        }
        catch (SleApiException e)
        {
            rc = e.getHResult();
        }

        if (rc != HRESULT.S_OK)
        {
            return rc;
        }

        this.statusInfo.lock();
        this.statusInfo.setUpReport(sr);
        this.statusInfo.unlock();

        rc = initiatePxyOpInv(sr, false);
        return rc;
    }

    /**
     * Performs setting of the required parameter to the supplied
     * ROCF-GET-PARAMETER operation.. When the value has been set, it passes the
     * operation to the interface ISLE_SrvProxyInitiate for transmission to the
     * user.
     */
    @Override
    protected HRESULT doGetParameter(ISLE_Operation poperation)
    {

        IROCF_GetParameter gp = (IROCF_GetParameter) poperation;
        ROCF_ParameterName pname = gp.getRequestedParameter();
        HRESULT rc = HRESULT.SLE_E_UNKNOWN;

        if (pname == ROCF_ParameterName.rocfPN_reportingCycle)
        {
            gp.setReportingCycle(getReportingCycle());
            rc = HRESULT.S_OK;

        }
        else if (pname == ROCF_ParameterName.rocfPN_returnTimeoutPeriod)
        {
            gp.setReturnTimeoutPeriod(getReturnTimeout());
            rc = HRESULT.S_OK;

        }
        else
        {
            rc = this.config.setUpGetParameter(gp);
        }

        if (rc != HRESULT.S_OK)
        {
            this.statusInfo.lock();
            rc = this.statusInfo.setUpGetParameter(gp);
            this.statusInfo.unlock();
        }
        if (rc == HRESULT.S_OK)
        {
            gp.setPositiveResult();
        }
        else
        {
            gp.setGetParameterDiagnostic(ROCF_GetParameterDiagnostic.rocfGP_unknownParameter);
        }

        return initiatePxyOpRtn(gp, false);
    }

    /**
     * Resets the status information parameters to the initial values.
     * Implementation: The base-class is called first.
     */
    @Override
    protected void cleanup()
    {

        super.cleanup();
        this.statusInfo.setReqGlobalVcId(null);
        this.statusInfo.setReqControlWordType(ROCF_ControlWordType.rocfCWT_invalid);
        this.statusInfo.setReqTcVcid(0);
        this.statusInfo.setReqUpdateMode(ROCF_UpdateMode.rocfUM_invalid);

    }

    /**
     * Starts processing of the operation invocation received from the
     * application.
     */
    @Override
    protected HRESULT doInitiateOpInvoke(ISLE_Operation poperation)
    {

        HRESULT rc = super.doInitiateOpInvoke(poperation);
        if (rc != HRESULT.S_OK)
        {
            return rc;
        }

        SLE_OpType opType = poperation.getOperationType();

        switch (opType)
        {
        case sleOT_transferData:
            return transferDataInv(poperation);
        case sleOT_syncNotify:
            return syncNotifyInv(poperation);
        case sleOT_peerAbort:
            return peerAbortInv(poperation, SLE_AbortOriginator.sleAO_application);

        default:
            return HRESULT.SLE_E_ROLE;
        }

    }

    /**
     * Starts processing of the return-operation received from the application.
     */
    @Override
    protected HRESULT doInitiateOpReturn(ISLE_ConfirmedOperation poperation)
    {

        HRESULT rc = super.doInitiateOpReturn(poperation);
        if (rc != HRESULT.S_OK)
        {
            return rc;
        }

        SLE_OpType opType = poperation.getOperationType();

        switch (opType)
        {
        case sleOT_bind:
            return bindRtn(poperation);
        case sleOT_unbind:
            return unbindRtn(poperation);
        case sleOT_start:
            return startRtn(poperation);
        case sleOT_stop:
            return stopRtn(poperation);

        default:
            return HRESULT.SLE_E_ROLE;
        }
    }

    /**
     * Starts processing of the operation invocation received from the proxy.
     */
    @Override
    protected HRESULT doInformOpInvoke(ISLE_Operation poperation)
    {

        HRESULT rc = super.doInformOpInvoke(poperation);
        if (rc != HRESULT.S_OK)
        {
            return rc;
        }

        SLE_OpType opType = poperation.getOperationType();

        switch (opType)
        {
        case sleOT_bind:
            return bindInv(poperation);
        case sleOT_unbind:
            return unbindInv(poperation);
        case sleOT_start:
            return startInv(poperation);
        case sleOT_stop:
            return stopInv(poperation);
        case sleOT_scheduleStatusReport:
            return scheduleStatusReportInv(poperation);
        case sleOT_getParameter:
            return getParameterInv(poperation);
        case sleOT_peerAbort:
            return peerAbortInv(poperation, SLE_AbortOriginator.sleAO_proxy);

        default:
            return HRESULT.SLE_E_ROLE;
        }
    }

    /**
     * Starts processing of the return-operation received from the proxy.
     */
    @Override
    protected HRESULT doInformOpReturn(ISLE_ConfirmedOperation poperation)
    {

        // we do not expect any return-PDU from the proxy for a provider SI

        HRESULT rc = super.doInformOpReturn(poperation);
        if (rc != HRESULT.S_OK)
        {
            return rc;
        }

        return HRESULT.SLE_E_ROLE;

    }

    /**
     * Performs all checks on the ROCF-START operation supplied by the Proxy
     * (see also 3.1.3.1 in the ROCF supplement). When the checks are completed
     * successfully, state-processing is initiated.
     */

    private HRESULT startInv(ISLE_Operation poperation)
    {

        IROCF_Start start = (IROCF_Start) poperation;

        ISLE_Time startt = start.getStartTime();
        ISLE_Time stopt = start.getStopTime();

        ROCF_DeliveryMode dm = this.config.getDeliveryMode().asROCF_DeliveryMode();

        HRESULT rc = HRESULT.S_OK;

        if (dm == ROCF_DeliveryMode.rocfDM_offline)
        {
            if (startt == null)
            {
                start.setStartDiagnostic(ROCF_StartDiagnostic.rocfSD_missingTimeValue);
                rc = HRESULT.E_FAIL;
            }
            if (stopt == null)
            {
                start.setStartDiagnostic(ROCF_StartDiagnostic.rocfSD_missingTimeValue);
                rc = HRESULT.E_FAIL;
            }
            if (rc == HRESULT.S_OK)
            {
                ISLE_Time currentTime = null;
                try
                {
                    currentTime = getUtilFactory().createTime(ISLE_Time.class);
                }
                catch (SleApiException e)
                {
                    LOG.log(Level.FINE, "SleApiException ", e);
                }
                if (currentTime != null)
                {
                    currentTime.update();

                    // check if stop time is in the future
                    if (!(stopt.compareTo(currentTime) < 0))
                    {
                        start.setStartDiagnostic(ROCF_StartDiagnostic.rocfSD_invalidStopTime);
                        rc = HRESULT.E_FAIL;
                    }
                }
            }
        } // end offline delivery mode
        else
        {
            // delivery-mode is online
            ISLE_Time ppstart = getProvisionPeriodStart();
            ISLE_Time ppstop = getProvisionPeriodStop();
            if (startt != null)
            {
                if (!(startt.compareTo(ppstart) >= 0) || !(startt.compareTo(ppstop) < 0))
                {
                    start.setStartDiagnostic(ROCF_StartDiagnostic.rocfSD_invalidStartTime);
                    rc = HRESULT.E_FAIL;
                }
            }
            if (rc == HRESULT.S_OK && stopt != null)
            {
                if (!(stopt.compareTo(ppstop) <= 0))
                {
                    start.setStartDiagnostic(ROCF_StartDiagnostic.rocfSD_invalidStopTime);
                    rc = HRESULT.E_FAIL;
                }
            }

        } // end online delivery mode

        // perform the checks on the global VcId:
        if (rc == HRESULT.S_OK)
        {
            rc = this.config.checkGvcId(start.getGvcid());
            if (rc != HRESULT.S_OK)
            {
                start.setStartDiagnostic(ROCF_StartDiagnostic.rocfSD_invalidGvcId);
            }
        }

        // perform the checks on the control word type:
        if (rc == HRESULT.S_OK)
        {
            rc = this.config.checkControlWordType(start.getControlWordType());
            if (rc != HRESULT.S_OK)
            {
                start.setStartDiagnostic(ROCF_StartDiagnostic.rocfSD_invalidControlWordType);
            }
        }

        // perform checks on the TcVcid
        if (rc == HRESULT.S_OK)
        {
            boolean tcVcidUsed = start.getTcVcidUsed();
            ROCF_ControlWordType cwt = start.getControlWordType();
            if (tcVcidUsed && cwt == ROCF_ControlWordType.rocfCWT_clcw)
            {
                rc = this.config.checkTcVcid(start.getTcVcid());
            }
            else if (cwt != ROCF_ControlWordType.rocfCWT_clcw && tcVcidUsed)
            {
                rc = HRESULT.E_FAIL;
            }
            if (rc != HRESULT.S_OK)
            {
                start.setStartDiagnostic(ROCF_StartDiagnostic.rocfSD_invalidTcVcid);
            }
        }

        // perform checks on the update mode
        if (rc == HRESULT.S_OK)
        {
            rc = this.config.checkUpdateMode(start.getUpdateMode());
            if (rc != HRESULT.S_OK)
            {
                start.setStartDiagnostic(ROCF_StartDiagnostic.rocfSD_invalidUpdateMode);
            }
        }

        if (rc != HRESULT.S_OK)
        {
            initiatePxyOpRtn(start, false);
            return HRESULT.S_OK;
        }

        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_StartInv, poperation);

    }

    /**
     * Performs all checks on the ROCF-START return supplied by the Appliaction.
     * When the checks are completed successfully, state-processing is
     * initiated. The status parameter 'requested-global-VCID' is updated
     * according to the value obtained from the return-PDU.
     */

    private HRESULT startRtn(ISLE_ConfirmedOperation poperation)
    {

        IROCF_Start s = (IROCF_Start) poperation;

        // checks: nothing left to check here

        // update status info for requested GvcId
        if (s.getResult() == SLE_Result.sleRES_positive)
        {
            long tcVcid = 0;
            ROCF_Gvcid gvcId = s.getGvcid();
            ROCF_ControlWordType cwt = s.getControlWordType();
            boolean tcVcidUsed = s.getTcVcidUsed();
            if (tcVcidUsed)
            {
                tcVcid = s.getTcVcid(); // SPR-1198
            }
            ROCF_UpdateMode updMode = s.getUpdateMode();

            this.statusInfo.lock();
            this.statusInfo.setReqGlobalVcId(gvcId);
            this.statusInfo.setReqControlWordType(cwt);
            this.statusInfo.setTcVcidUsed(tcVcidUsed);
            if (tcVcidUsed)
            {
                this.statusInfo.setReqTcVcid(tcVcid); // SPR-1198
            }
            this.statusInfo.setReqUpdateMode(updMode);
            this.statusInfo.unlock();
        }

        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_StartRtn, poperation);
    }

    /**
     * Performs all checks on the ROCF-SYNC-NOTIFY invocation supplied by the
     * application(see section 3.1.3.2 in the ROCF supplement). When the checks
     * are completed successfully, state-processing is initiated.
     */

    private HRESULT syncNotifyInv(ISLE_Operation poperation)
    {

        IROCF_SyncNotify sn = (IROCF_SyncNotify) poperation;
        ROCF_NotificationType nt = sn.getNotificationType();

        if (nt == ROCF_NotificationType.rocfNT_invalid)
        {
            return HRESULT.SLE_E_MISSINGARG;
        }

        ROCF_DeliveryMode dm = this.config.getDeliveryMode().asROCF_DeliveryMode();

        if (dm == ROCF_DeliveryMode.rocfDM_offline && nt != ROCF_NotificationType.rocfNT_endOfData)
        {
            return HRESULT.SLE_E_INCONSISTENT;
        }
        else
        {
            if (dm == ROCF_DeliveryMode.rocfDM_timelyOnline && nt == ROCF_NotificationType.rocfNT_excessiveDataBacklog)
            {
                return HRESULT.SLE_E_INCONSISTENT;
            }
        }

        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_SyncNotifyInv, poperation);

    }

    /**
     * Performs all checks on the ROCF-TRANSFER-DATA operation supplied by the
     * application. When the checks are completed successfully, state-processing
     * is initiated.
     */

    private HRESULT transferDataInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_TransferDataInv, poperation);
    }

    /**
     * Performs all checks on the ROCF-GET-PARAMETER operation supplied by the
     * Proxy. When the checks are completed successfully, state-processing is
     * initiated.
     */

    private HRESULT getParameterInv(ISLE_Operation poperation)
    {

        IROCF_GetParameter gp = (IROCF_GetParameter) poperation;
        ROCF_ParameterName pname = gp.getRequestedParameter();

        if (pname == ROCF_ParameterName.rocfPN_invalid)
        {
            gp.setGetParameterDiagnostic(ROCF_GetParameterDiagnostic.rocfGP_unknownParameter);
            HRESULT rc = initiatePxyOpRtn(gp, false);
            return rc;
        }

        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_GetPrmInv, poperation);

    }

    /**
     * Performs all checks on the STOP operation supplied by the Proxy. When the
     * checks are completed successfully, state-processing is initiated.
     */

    private HRESULT stopInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_StopInv, poperation);
    }

    /**
     * Performs all checks on the STOP return supplied by the Appliaction. When
     * the checks are completed successfully, state-processing is initiated. The
     * function also sets the status-parameter 'requested-frame-quality' to
     * NULL.
     */

    private HRESULT stopRtn(ISLE_ConfirmedOperation poperation)
    {

        ISLE_Stop s = (ISLE_Stop) poperation;

        // update status info
        if (s.getResult() == SLE_Result.sleRES_positive)
        {
            this.statusInfo.lock();
            this.statusInfo.setReqGlobalVcId(null);
            this.statusInfo.setReqControlWordType(ROCF_ControlWordType.rocfCWT_invalid);
            this.statusInfo.setReqTcVcid(0);
            this.statusInfo.setReqUpdateMode(ROCF_UpdateMode.rocfUM_invalid);
            this.statusInfo.unlock();
        }

        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_StopRtn, poperation);

    }

    /**
     * Prepends a 'buffer-discarded' ROCF-SYNC-NOTIFY operation to the supplied
     * transfer-buffer.
     */
    @Override
    protected void prependNotification(ISLE_TransferBuffer buffer)
    {

        IROCF_SyncNotify sn = null;
        HRESULT rc = HRESULT.S_OK;
        try
        {
            sn = createOperation(IROCF_SyncNotify.class, SLE_OpType.sleOT_syncNotify);
        }
        catch (SleApiException e)
        {
            rc = e.getHResult();
        }
        if (rc == HRESULT.S_OK)
        {
            sn.setDataDiscarded();
            buffer.prepend(sn, true);
        }
    }

    /**
     * Returns true if the supplied operation is a 'EndOfData' ROCF-SYNC-NOTIFY
     * operation.
     */
    @Override
    protected boolean isEndOfData(ISLE_Operation poperation)
    {
        if (poperation.getOperationType() != SLE_OpType.sleOT_syncNotify)
        {
            return false;
        }

        IROCF_SyncNotify sn = (IROCF_SyncNotify) poperation;
        if (sn.getNotificationType() == ROCF_NotificationType.rocfNT_endOfData)
        {
            return true;
        }

        return false;
    }

    @Override
    public void setPermittedGvcidSet(ROCF_Gvcid[] idList)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.config.setPermittedGvcIdSet(idList);
    }

    @Override
    public void setPermittedControlWordTypeSet(ROCF_ControlWordType[] typeSet)
    {

        if (isConfigured() == true)
        {
            return;
        }
        this.config.setPermittedControlWordTypeSet(typeSet);
    }

    @Override
    public void setPermittedTcVcidSet(long[] idSet)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.config.setPermittedTcVcidSet(idSet);
    }

    @Override
    public void setPermittedUpdateModeSet(ROCF_UpdateMode[] modeSet)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.config.setPermittedUpdateModeSet(modeSet);
    }

    @Override
    public ROCF_Gvcid[] getPermittedGvcidSet()
    {
        return this.config.getPermittedGvcIdSet();
    }

    @Override
    public ROCF_ControlWordType[] getPermittedControlWordTypeSet()
    {
        return this.config.getPermittedControlWordTypeSet();
    }

    @Override
    public long[] getPermittedTcVcidSet()
    {
    	// TEst JC
        return this.config.getPermittedTcVcidSet();
    }

    @Override
    public ROCF_UpdateMode[] getPermittedUpdateModeSet()
    {
        return this.config.getPermittedUpdateModeSet();
    }

    /**
     * Implementation of the configured minimum reporting cycle
     * @since SLES V5.
     * @return the value
     */
    @Override
    public long getMinimumReportCycle()
    {
    	return this.config.getMinimumReportingCycle();
    }
}
