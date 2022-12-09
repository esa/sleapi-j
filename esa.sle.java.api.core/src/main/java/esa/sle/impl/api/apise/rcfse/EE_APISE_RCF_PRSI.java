package esa.sle.impl.api.apise.rcfse;

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
import ccsds.sle.api.isrv.ircf.IRCF_GetParameter;
import ccsds.sle.api.isrv.ircf.IRCF_SIAdmin;
import ccsds.sle.api.isrv.ircf.IRCF_SITransferBufferControl;
import ccsds.sle.api.isrv.ircf.IRCF_SIUpdate;
import ccsds.sle.api.isrv.ircf.IRCF_Start;
import ccsds.sle.api.isrv.ircf.IRCF_StatusReport;
import ccsds.sle.api.isrv.ircf.IRCF_SyncNotify;
import ccsds.sle.api.isrv.ircf.types.RCF_ChannelType;
import ccsds.sle.api.isrv.ircf.types.RCF_DeliveryMode;
import ccsds.sle.api.isrv.ircf.types.RCF_GetParameterDiagnostic;
import ccsds.sle.api.isrv.ircf.types.RCF_Gvcid;
import ccsds.sle.api.isrv.ircf.types.RCF_LockStatus;
import ccsds.sle.api.isrv.ircf.types.RCF_NotificationType;
import ccsds.sle.api.isrv.ircf.types.RCF_ParameterName;
import ccsds.sle.api.isrv.ircf.types.RCF_ProductionStatus;
import ccsds.sle.api.isrv.ircf.types.RCF_StartDiagnostic;
import esa.sle.impl.api.apise.slese.EE_APISE_PConfiguration;
import esa.sle.impl.api.apise.slese.EE_APISE_PRSI;
import esa.sle.impl.api.apise.slese.types.EE_TI_SLESE_Event;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * RCF Provider Return Service Instance This class provides the functionality
 * that is specific to RCF return service instances for provider applications.
 * It is responsible for the RCF specific configuration of the service instance
 * and the update of the service parameters by implementing the interfaces
 * IRCF_SIAdmin and IRCF_SIUpdate. The class also implements the interface
 * ISLE_SIOpFactory, which allows the client to obtain pre-configured operation
 * objects supported by the RCF service. The RCF-PRSI provides the following
 * functionality: - it checks the operation invocations and returns to be
 * compatible with the RCF service - processes and sets-up GET-PARAMETER
 * operations according to the configuration and status information parameters -
 * it updates status information on request - it creates, initialises and sends
 * STATUS-REPORT operations - it forwards operation objects to the base-class
 * for further processing (if not supported by this class) The class performs
 * RCF service specific checks of operation objects received from the
 * application and the proxy. The class implements do<Initiate/Inform>OpInvoke()
 * and do<Initiate/Inform>OpReturn exported by the base-class. These functions
 * look at the operation-type and pass the operation invocation to the
 * appropriate function (<opType>Inv() or <opType>Rtn()), which performs the
 * specific checks. After successful checking the operation invocations/returns
 * state-processing is executed by a call to doStateProcessing(). The base-class
 * calls updateStatusInfo() whenever a TRANSFER-BUFFER has been transmitted to
 * the service user. The function updateStatusInfo() obtains the
 * 'number-of-frames-delivered' parameter for status-updates from the supplied
 * operation. The RCF-PRSI-class sets the parameter for the GET-PARAMETER
 * invocation (doGetParameter() method) and passes the return-PDU back to the
 * proxy.
 */

public class EE_APISE_RCF_PRSI extends EE_APISE_PRSI implements ISLE_SIOpFactory, IRCF_SIUpdate,
                                                    IRCF_SITransferBufferControl, IRCF_SIAdmin
{

    private static final Logger LOG = Logger.getLogger(EE_APISE_RCF_PRSI.class.getName());

    private final EE_APISE_RCF_Configuration config;

    private final EE_APISE_RCF_StatusInformation statusInfo;

    private final ReentrantLock obj = new ReentrantLock();


    /**
     * The constructor, to be used for service instance creation, it passes the
     * supplied arguments to the base-class.
     */
    public EE_APISE_RCF_PRSI(String instanceKey, ISLE_ServiceInform clientIf)
    {
        super(instanceKey, SLE_ApplicationIdentifier.sleAI_rtnChFrames, clientIf);
        this.config = new EE_APISE_RCF_Configuration();
        this.statusInfo = new EE_APISE_RCF_StatusInformation();
    }

    /**
     * See specification of IUnknown.
     */

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
            if (iid == IRCF_SIAdmin.class)
            {
                return (T) this;
            }
            else if (iid == IRCF_SIUpdate.class)
            {
                return (T) this;
            }
            else if (iid == ISLE_SIOpFactory.class)
            {
                return (T) this;
            }
            else if (iid == IRCF_SITransferBufferControl.class)
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
    public void setDeliveryMode(RCF_DeliveryMode mode)
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
    public void setPermittedGvcidSet(RCF_Gvcid[] idList)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.config.setPermittedGvcIdSet(idList);
    }

    @Override
    public void setInitialProductionStatus(RCF_ProductionStatus status)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.statusInfo.setProductionStatus(status);
    }

    @Override
    public void setInitialFrameSyncLock(RCF_LockStatus status)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.statusInfo.setFrameSyncLock(status);
    }

    @Override
    public void setInitialCarrierDemodLock(RCF_LockStatus status)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.statusInfo.setCarrierDemodLock(status);
    }

    @Override
    public void setInitialSubCarrierDemodLock(RCF_LockStatus status)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.statusInfo.setSubCarrDemodLock(status);
    }

    @Override
    public void setInitialSymbolSyncLock(RCF_LockStatus status)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.statusInfo.setSymbolSyncLock(status);
    }

    @Override
    public RCF_DeliveryMode getDeliveryMode()
    {
        return this.config.getDeliveryMode().asRCF_DeliveryMode();
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
    public RCF_Gvcid[] getPermittedGvcidSet()
    {
        return this.config.getPermittedGvcIdSet();
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

    @Override
    public void setProductionStatus(RCF_ProductionStatus status)
    {
        this.statusInfo.lock();
        this.statusInfo.setProductionStatus(status);
        this.statusInfo.unlock();
    }

    @Override
    public void setFrameSyncLock(RCF_LockStatus status)
    {
        this.statusInfo.lock();
        this.statusInfo.setFrameSyncLock(status);
        this.statusInfo.unlock();
    }

    @Override
    public void setCarrierDemodLock(RCF_LockStatus status)
    {
        this.statusInfo.lock();
        this.statusInfo.setCarrierDemodLock(status);
        this.statusInfo.unlock();
    }

    @Override
    public void setSubCarrierDemodLock(RCF_LockStatus status)
    {
        this.statusInfo.lock();
        this.statusInfo.setSubCarrDemodLock(status);
        this.statusInfo.unlock();
    }

    @Override
    public void setSymbolSyncLock(RCF_LockStatus status)
    {
        this.statusInfo.lock();
        this.statusInfo.setSymbolSyncLock(status);
        this.statusInfo.unlock();
    }

    @Override
    public RCF_ProductionStatus getProductionStatus()
    {

        this.statusInfo.lock();
        RCF_ProductionStatus ps = this.statusInfo.getProductionStatus();
        this.statusInfo.unlock();
        return ps;
    }

    @Override
    public RCF_LockStatus getFrameSyncLock()
    {
        this.statusInfo.lock();
        RCF_LockStatus ls = this.statusInfo.getFrameSyncLock();
        this.statusInfo.unlock();
        return ls;
    }

    @Override
    public RCF_LockStatus getCarrierDemodLock()
    {

        this.statusInfo.lock();
        RCF_LockStatus ls = this.statusInfo.getCarrierDemodLock();
        this.statusInfo.unlock();
        return ls;
    }

    @Override
    public RCF_LockStatus getSubCarrierDemodLock()
    {
        this.statusInfo.lock();
        RCF_LockStatus ls = this.statusInfo.getSubCarrDemodLock();
        this.statusInfo.unlock();
        return ls;
    }

    @Override
    public RCF_LockStatus getSymbolSyncLock()
    {
        this.statusInfo.lock();
        RCF_LockStatus ls = this.statusInfo.getSymbolSyncLock();
        this.statusInfo.unlock();
        return ls;
    }

    @Override
    public long getNumFrames()
    {

        this.statusInfo.lock();
        long nf = this.statusInfo.getNumFrames();
        this.statusInfo.unlock();
        return nf;

    }

    @Override
    public RCF_Gvcid getRequestedGvcid()
    {

        // according to spec the return value is dependent of the SI state.
        if (getSIState() != SLE_SIState.sleSIS_active)
        {
            //return null;
            // SLES V5 (Page3-51): Changed from 'invalid/undefined' to 
        	// first element of permitted-global-VCID-set
        	return getPermittedGvcidSet()[0];
        }
        else
        {
            this.statusInfo.lock();
            RCF_Gvcid gvcId = this.statusInfo.getReqGlobalVcId();
            if (gvcId != null)
            {
                this.statusInfo.unlock();
                return null;
            }
            RCF_Gvcid newGvcId = new RCF_Gvcid();
            newGvcId = gvcId;
            this.statusInfo.unlock();
            return newGvcId;
        }

    }

    @Override
    public <T extends ISLE_Operation> T createOperation(Class<T> iid, SLE_OpType optype) throws SleApiException
    {

        if (isConfigured() == false)
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        // check operation type supported for a RCF service provider SI:
        if (optype != SLE_OpType.sleOT_transferData && optype != SLE_OpType.sleOT_syncNotify
            && optype != SLE_OpType.sleOT_peerAbort)
        {
            throw new SleApiException(HRESULT.SLE_E_TYPE);
        }

        ISLE_OperationFactory opf = getOpFactory();

        T ppv = null;
        if(optype == SLE_OpType.sleOT_scheduleStatusReport)
        {
        	ppv = opf.createOperation(iid, optype, SLE_ApplicationIdentifier.sleAI_rtnChFrames, getVersion(), getMinimumReportCycle());
        }
        else
        {
        	ppv = opf.createOperation(iid, optype, SLE_ApplicationIdentifier.sleAI_rtnChFrames, getVersion());
        }
        //T ppv = opf.createOperation(iid, optype, SLE_ApplicationIdentifier.sleAI_rtnChFrames, getVersion());
        // no specific set-up for TRANSFER-DATA required
        // no specific set-up for SYNC-NOTIFY required

        // PEER-ABOR-specific set-up supported by base-class:
        setUpOperation(optype, ppv);
        return ppv;

    }

    @Override
    public void sendBufferTransfer(boolean withNotification)
    {
        this.obj.lock();
        super.sendBuffer(withNotification);
        this.obj.unlock();
    }

    @Override
    protected EE_APISE_PConfiguration getConfiguration()
    {
        return this.config;
    }

    @Override
    protected HRESULT doConfigCompleted()
    {

        HRESULT rc = super.doConfigCompleted();
        if (rc != HRESULT.S_OK)
        {
            return rc;
        }

        // check delivery mode

        RCF_DeliveryMode dm = this.config.getDeliveryMode().asRCF_DeliveryMode();
        if (dm == RCF_DeliveryMode.rcfDM_invalid)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                    EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                    "Invalid RCF Delivery Mode");
            return HRESULT.SLE_E_CONFIG;
        }

        long bufferSize = this.config.getTransferBufferSize();

        if (bufferSize < EE_APISE_RCF_Limits.getMinBufferSize())
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                    EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                    "Invalid RCF Minimum Buffer Size");
            return HRESULT.SLE_E_CONFIG;
        }

        RCF_Gvcid[] permGvcIdList = this.config.getPermittedGvcIdSet();

        if (permGvcIdList == null)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                    EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                    "Missing GvcId List");
            return HRESULT.SLE_E_CONFIG;
        }
        else
        {
            for (RCF_Gvcid element : permGvcIdList)
            {
                if (element.getType() == RCF_ChannelType.rcfCT_invalid
                    || element.getScid() > EE_APISE_RCF_Limits.getGvcIdMaxScId()
                    || element.getVersion() > EE_APISE_RCF_Limits.getGvcIdMaxVersion()
                    || element.getVcid() > EE_APISE_RCF_Limits.getGvcIdMaxVcId())
                {
                    logRecord(SLE_LogMessageType.sleLM_alarm,
                            EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                            "Invalid Permitted GvcId List");
                    return HRESULT.SLE_E_CONFIG;
                }
            }
        }

        // check the latency-limit
        if ((dm == RCF_DeliveryMode.rcfDM_timelyOnline) || (dm == RCF_DeliveryMode.rcfDM_completeOnline))
        {
            int ll = this.config.getLatencyLimit();
            if (ll < EE_APISE_RCF_Limits.getMinLatencyLimit())
            {
                logRecord(SLE_LogMessageType.sleLM_alarm,
                        EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                        "Invalid Minimum Latency Limit");
                return HRESULT.SLE_E_CONFIG;
            }
        }
        else
        {
            this.config.setLatencyLimit(0);
        }

        // set-ip according to SE-2.1 and sect. 4.3.2 of RCF-spec:
        // set values for delivery mode offline, if not set via
        // IRCF_SIAdmin:
        this.statusInfo.lock();
        if (dm == RCF_DeliveryMode.rcfDM_offline)
        {
            if (this.statusInfo.getFrameSyncLock() == RCF_LockStatus.rcfLS_invalid)
            {
                this.statusInfo.setFrameSyncLock(RCF_LockStatus.rcfLS_unknown);
            }
            if (this.statusInfo.getSymbolSyncLock() == RCF_LockStatus.rcfLS_invalid)
            {
                this.statusInfo.setSymbolSyncLock(RCF_LockStatus.rcfLS_unknown);
            }
            if (this.statusInfo.getSubCarrDemodLock() == RCF_LockStatus.rcfLS_invalid)
            {
                this.statusInfo.setSubCarrDemodLock(RCF_LockStatus.rcfLS_unknown);
            }
            if (this.statusInfo.getCarrierDemodLock() == RCF_LockStatus.rcfLS_invalid)
            {
                this.statusInfo.setCarrierDemodLock(RCF_LockStatus.rcfLS_unknown);
            }
        }
        else
        {
            if (this.statusInfo.getProductionStatus() == RCF_ProductionStatus.rcfPS_invalid
                || this.statusInfo.getFrameSyncLock() == RCF_LockStatus.rcfLS_invalid
                || this.statusInfo.getSymbolSyncLock() == RCF_LockStatus.rcfLS_invalid
                || this.statusInfo.getSubCarrDemodLock() == RCF_LockStatus.rcfLS_invalid
                || this.statusInfo.getCarrierDemodLock() == RCF_LockStatus.rcfLS_invalid)
            {
                this.statusInfo.unlock();
                logRecord(SLE_LogMessageType.sleLM_alarm,
                        EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                        "Invalid RCF Status Info");
                return HRESULT.SLE_E_CONFIG;
            }

        }

        // set-up remaining initial values acc. to SE2.2 and SE2.3, SE2.4 and
        // 4.3.2 of RCF-spec:

        this.statusInfo.setNumFrames(0);
        this.statusInfo.setReqGlobalVcId(null);
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
                long nf = this.statusInfo.getNumFrames();
                this.statusInfo.setNumFrames(nf + 1);
                this.statusInfo.unlock();
            }
        }
    }

    /**
     * Creates a new RCF specific status report operation and initialises it.
     * When all values have been set, it passes the operation to the interface
     * ISLE_SrvProxyInitiate for transmission to the user.
     */
    @Override
    protected HRESULT doStatusReport()
    {

        ISLE_OperationFactory opf = getOpFactory();
        HRESULT rc = HRESULT.S_OK;
        IRCF_StatusReport sr = null;
        try
        {
            sr = opf.createOperation(IRCF_StatusReport.class,
                                     SLE_OpType.sleOT_statusReport,
                                     SLE_ApplicationIdentifier.sleAI_rtnChFrames,
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
     * RCF-GET-PARAMETER operation.. When the value has been set, it passes the
     * operation to the interface ISLE_SrvProxyInitiate for transmission to the
     * user.
     */
    @Override
    protected HRESULT doGetParameter(ISLE_Operation poperation)
    {

        IRCF_GetParameter gp = (IRCF_GetParameter) poperation;
        RCF_ParameterName pname = gp.getRequestedParameter();
        HRESULT rc = HRESULT.SLE_E_UNKNOWN;

        if (pname == RCF_ParameterName.rcfPN_reportingCycle)
        {
            gp.setReportingCycle(getReportingCycle());
            rc = HRESULT.S_OK;
        }
        else if (pname == RCF_ParameterName.rcfPN_returnTimeoutPeriod)
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
            rc = this.statusInfo.setUpGetParameter(gp, this.config.getPermittedGvcIdSet());
            this.statusInfo.unlock();
        }

        if (rc == HRESULT.S_OK)
        {
            gp.setPositiveResult();
        }
        else
        {
            gp.setGetParameterDiagnostic(RCF_GetParameterDiagnostic.rcfGP_unknownParameter);
        }
        return initiatePxyOpRtn(gp, false);
    }

    /**
     * Resets the status information parameters to the initial values.
     */
    @Override
    protected void cleanup()
    {
        super.cleanup();
        this.statusInfo.setReqGlobalVcId(null);
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
     * Performs all checks on the RCF-START operation supplied by the Proxy (see
     * also 3.1.3.1 in the RCF supplement). When the checks are completed
     * successfully, state-processing is initiated.
     */

    private HRESULT startInv(ISLE_Operation poperation)
    {

        IRCF_Start s = (IRCF_Start) poperation;

        final ISLE_Time startt = s.getStartTime();
        final ISLE_Time stopt = s.getStopTime();

        RCF_DeliveryMode dm = this.config.getDeliveryMode().asRCF_DeliveryMode();

        HRESULT rc = HRESULT.S_OK;

        if (dm == RCF_DeliveryMode.rcfDM_offline)
        {
            if (startt == null)
            {
                s.setStartDiagnostic(RCF_StartDiagnostic.rcfSD_missingTimeValue);
                rc = HRESULT.E_FAIL;
            }
            if (stopt == null)
            {
                s.setStartDiagnostic(RCF_StartDiagnostic.rcfSD_missingTimeValue);
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
                        s.setStartDiagnostic(RCF_StartDiagnostic.rcfSD_invalidStopTime);
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
                    s.setStartDiagnostic(RCF_StartDiagnostic.rcfSD_invalidStartTime);
                    rc = HRESULT.E_FAIL;
                }
            }
            if (rc == HRESULT.S_OK && stopt != null)
            {
                if (!(stopt.compareTo(ppstop) <= 0))
                {
                    s.setStartDiagnostic(RCF_StartDiagnostic.rcfSD_invalidStopTime);
                    rc = HRESULT.E_FAIL;
                }
            }

        } // end online delivery mode

        // perform the checks on the global VcId:
        if (rc == HRESULT.S_OK)
        {
            rc = this.config.checkGvcId(s.getGvcid());
            if (rc != HRESULT.S_OK)
            {
                s.setStartDiagnostic(RCF_StartDiagnostic.rcfSD_invalidGvcId);
            }
        }

        if (rc != HRESULT.S_OK)
        {
            initiatePxyOpRtn(s, false);
            return HRESULT.S_OK;
        }

        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_StartInv, poperation);
    }

    /**
     * Performs all checks on the RCF-START return supplied by the Appliaction.
     * When the checks are completed successfully, state-processing is
     * initiated. The status parameter 'requested-global-VCID' is updated
     * according to the value obtained from the return-PDU.
     */

    private HRESULT startRtn(ISLE_ConfirmedOperation poperation)
    {
        IRCF_Start s = (IRCF_Start) poperation;

        // checks: nothing left to check here

        // update status info for requested GvcId
        if (s.getResult() == SLE_Result.sleRES_positive)
        {
            RCF_Gvcid gvcId = s.getGvcid();
            this.statusInfo.lock();
            this.statusInfo.setReqGlobalVcId(gvcId);
            this.statusInfo.unlock();
        }

        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_StartRtn, poperation);

    }

    /**
     * Performs all checks on the RCF-SYNC-NOTIFY invocation supplied by the
     * application(see section 3.1.3.2 in the RCF supplement). When the checks
     * are completed successfully, state-processing is initiated.
     */

    private HRESULT syncNotifyInv(ISLE_Operation poperation)
    {

        IRCF_SyncNotify sn = (IRCF_SyncNotify) poperation;
        RCF_NotificationType nt = sn.getNotificationType();

        if (nt == RCF_NotificationType.rcfNT_invalid)
        {
            return HRESULT.SLE_E_MISSINGARG;
        }

        RCF_DeliveryMode dm = this.config.getDeliveryMode().asRCF_DeliveryMode();

        if (dm == RCF_DeliveryMode.rcfDM_offline && nt != RCF_NotificationType.rcfNT_endOfData)
        {
            return HRESULT.SLE_E_INCONSISTENT;
        }
        else
        {
            if (dm == RCF_DeliveryMode.rcfDM_timelyOnline && nt == RCF_NotificationType.rcfNT_excessiveDataBacklog)
            {
                return HRESULT.SLE_E_INCONSISTENT;
            }
        }

        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_SyncNotifyInv, poperation);

    }

    /**
     * Performs all checks on the RCF-TRANSFER-DATA operation supplied by the
     * application. When the checks are completed successfully, state-processing
     * is initiated.
     */

    private HRESULT transferDataInv(ISLE_Operation poperation)
    {

        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_TransferDataInv, poperation);

    }

    /**
     * Performs all checks on the RCF-GET-PARAMETER operation supplied by the
     * Proxy. When the checks are completed successfully, state-processing is
     * initiated.
     */

    private HRESULT getParameterInv(ISLE_Operation poperation)
    {

        IRCF_GetParameter gp = (IRCF_GetParameter) poperation;
        RCF_ParameterName pname = gp.getRequestedParameter();

        if (pname == RCF_ParameterName.rcfPN_invalid)
        {
            gp.setGetParameterDiagnostic(RCF_GetParameterDiagnostic.rcfGP_unknownParameter);
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
            this.statusInfo.unlock();
        }

        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_StopRtn, poperation);

    }

    /**
     * Prepends a 'buffer-discarded' RCF-SYNC-NOTIFY operation to the supplied
     * transfer-buffer.
     */
    @Override
    protected void prependNotification(ISLE_TransferBuffer buffer)
    {

        IRCF_SyncNotify sn = null;
        try
        {
            sn = createOperation(IRCF_SyncNotify.class, SLE_OpType.sleOT_syncNotify);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }
        if (sn != null)
        {
            sn.setDataDiscarded();
            buffer.prepend(sn, true);
        }
    }

    /**
     * Returns true if the supplied operation is a 'EndOfData' RCF-SYNC-NOTIFY
     * operation.
     */
    @Override
    protected boolean isEndOfData(ISLE_Operation poperation)
    {

        if (poperation.getOperationType() != SLE_OpType.sleOT_syncNotify)
        {
            return false;
        }

        IRCF_SyncNotify sn = (IRCF_SyncNotify) poperation;
        if (sn.getNotificationType() == RCF_NotificationType.rcfNT_endOfData)
        {
            return true;
        }

        return false;

    }

}
