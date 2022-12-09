package esa.sle.impl.api.apise.rafse;

import java.util.ArrayList;
import java.util.List;
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
import ccsds.sle.api.isrv.iraf.IRAF_GetParameter;
import ccsds.sle.api.isrv.iraf.IRAF_SIAdmin;
import ccsds.sle.api.isrv.iraf.IRAF_SITransferBufferControl;
import ccsds.sle.api.isrv.iraf.IRAF_SIUpdate;
import ccsds.sle.api.isrv.iraf.IRAF_Start;
import ccsds.sle.api.isrv.iraf.IRAF_StatusReport;
import ccsds.sle.api.isrv.iraf.IRAF_SyncNotify;
import ccsds.sle.api.isrv.iraf.IRAF_TransferData;
import ccsds.sle.api.isrv.iraf.types.RAF_DeliveryMode;
import ccsds.sle.api.isrv.iraf.types.RAF_FrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_GetParameterDiagnostic;
import ccsds.sle.api.isrv.iraf.types.RAF_LockStatus;
import ccsds.sle.api.isrv.iraf.types.RAF_NotificationType;
import ccsds.sle.api.isrv.iraf.types.RAF_ParFrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_ParameterName;
import ccsds.sle.api.isrv.iraf.types.RAF_ProductionStatus;
import ccsds.sle.api.isrv.iraf.types.RAF_RequestedFrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_StartDiagnostic;
import esa.sle.impl.api.apise.slese.EE_APISE_PConfiguration;
import esa.sle.impl.api.apise.slese.EE_APISE_PRSI;
import esa.sle.impl.api.apise.slese.types.EE_TI_SLESE_Event;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * RAF Provider Return Service Instance This class provides the functionality
 * that is specific to RAF return service instances for provider applications.
 * It is responsible for the RAF specific configuration of the service instance
 * and the update of the service parameters by implementing the interfaces
 * IRAF_SIAdmin and IRAF_SIUpdate. The class also implements the interface
 * ISLE_SIOpFactory, which allows the client to obtain pre-configured operation
 * objects supported by the RAF service. The RAF-PRSI provides the following
 * functionality: - it checks the operation invocations and returns to be
 * compatible with the RAF service - processes and responds to GET-PARAMETER
 * invocations according to the configuration and status information parameters
 * - it updates status information on request - it creates, initialises and
 * sends STATSUS-REPORT operations - it forwards operation objects to the
 * base-class for further processing (if not supported by this class) The class
 * performs RAF service specific checks of operation objects received from the
 * application and the proxy. The class implements do<Initiate/Inform>OpInvoke()
 * and do<Initiate/Inform>OpReturn exported by the base-class. These functions
 * look at the operation-type and pass the operation invocation to the
 * appropriate function (<opType>Inv() or <opType>Rtn()), which performs the
 * specific checks. After successful checking the operation invocations/returns
 * state-processing is executed by a call to doStateProcessing(). The base-class
 * calls updateStatusInfo() whenever a TRANSFER-BUFFER invocation has been
 * transmitted to the service user, updateStatusInfo() obtains the parameters
 * 'number-of-frames-delivered' and 'number-error-free-frames' parameters for
 * status-updates from the contents of the supplied operation. The
 * RAF-PRSI-class sets the parameter for the GET-PARAMETER invocation
 * (doGetParameter() method) and passes the return-PDU back to the proxy.
 */
public class EE_APISE_RAF_PRSI extends EE_APISE_PRSI implements IRAF_SIAdmin, IRAF_SIUpdate,
                                                    IRAF_SITransferBufferControl, ISLE_SIOpFactory

{

    private static final Logger LOG = Logger.getLogger(EE_APISE_RAF_PRSI.class.getName());

    private final EE_APISE_RAF_Configuration config = new EE_APISE_RAF_Configuration();

    private final EE_APISE_RAF_StatusInformation statusInfo = new EE_APISE_RAF_StatusInformation();


    /**
     * The public constructor, to be used for service instance creation, passes
     * the supplied arguments to the base-class.
     */
    public EE_APISE_RAF_PRSI(String instanceKey, ISLE_ServiceInform clientIf)
    {
        super(instanceKey, SLE_ApplicationIdentifier.sleAI_rtnAllFrames, clientIf);
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
            if (iid == IRAF_SIAdmin.class)
            {
                return (T) this;
            }
            else if (iid == IRAF_SIUpdate.class)
            {
                return (T) this;
            }
            else if (iid == ISLE_SIOpFactory.class)
            {
                return (T) this;
            }
            else if (iid == IRAF_SITransferBufferControl.class)
            {
                return (T) this;
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * See specification of IRAF_SIAdmin.
     */
    @Override
    public void setDeliveryMode(RAF_DeliveryMode mode)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.config.setDeliveryMode(mode.asSLE_DeliveryMode());
    }

    /**
     * See specification of IRAF_SIAdmin.
     */
    @Override
    public void setLatencyLimit(int limit)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.config.setLatencyLimit(limit);
    }
    
    

    /**
     * See specification of IRAF_SIAdmin.
     */
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
     * See specification of IRAF_SIAdmin.
     */
    @Override
    public void setInitialProductionStatus(RAF_ProductionStatus status)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.statusInfo.setProductionStatus(status);
    }

    /**
     * See specification of IRAF_SIAdmin.
     */
    @Override
    public void setInitialFrameSyncLock(RAF_LockStatus status)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.statusInfo.setFrameSyncLock(status);
    }

    /**
     * See specification of IRAF_SIAdmin.
     */
    @Override
    public void setInitialCarrierDemodLock(RAF_LockStatus status)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.statusInfo.setCarrierDemodLock(status);
    }

    /**
     * See specification of IRAF_SIAdmin.
     */
    @Override
    public void setInitialSubCarrierDemodLock(RAF_LockStatus status)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.statusInfo.setSubCarrDemodLock(status);
    }

    /**
     * See specification of IRAF_SIAdmin.
     */
    @Override
    public void setInitialSymbolSyncLock(RAF_LockStatus status)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.statusInfo.setSymbolSyncLock(status);
    }

    /**
     * See specification of IRAF_SIAdmin.
     */
    @Override
    public RAF_DeliveryMode getDeliveryMode()
    {
        return this.config.getDeliveryMode().asRAF_DeliveryMode();
    }

    /**
     * See specification of IRAF_SIAdmin.
     */
    @Override
    public int getLatencyLimit()
    {
        return this.config.getLatencyLimit();
    }

    /**
     * See specification of IRAF_SIAdmin.
     */
    @Override
    public long getTransferBufferSize()
    {
        return this.config.getTransferBufferSize();
    }

    /**
     * Set the configured permitted frame quality
     * @since SLES V5.
     */
    public void setPermittedFrameQuality(RAF_ParFrameQuality[] frameQualities)
    {
    	if (isConfigured() == true)
        {
            return;
        }
        this.config.setPermittedFrameQuality(frameQualities);
    }
    
    /**
     * Set the configured minimum reporting cycle
     * @since SLES V5.
     */
    public void setMinimumReportCycle(long mrc)
    {
    	if (isConfigured() == true)
        {
            return;
        }
        this.config.setMinimumReportingCycle(mrc);
    }
    
    
    /**
     * See specification of IRAF_SIUpdate.
     */
    @Override
    public void setProductionStatus(RAF_ProductionStatus status)
    {
        this.statusInfo.lock();
        this.statusInfo.setProductionStatus(status);
        this.statusInfo.unlock();
    }

    /**
     * See specification of IRAF_SIUpdate.
     */
    @Override
    public void setFrameSyncLock(RAF_LockStatus status)
    {
        this.statusInfo.lock();
        this.statusInfo.setFrameSyncLock(status);
        this.statusInfo.unlock();
    }

    /**
     * See specification of IRAF_SIUpdate.
     */
    @Override
    public void setCarrierDemodLock(RAF_LockStatus status)
    {
        this.statusInfo.lock();
        this.statusInfo.setCarrierDemodLock(status);
        this.statusInfo.unlock();
    }

    /**
     * See specification of IRAF_SIUpdate.
     */
    @Override
    public void setSubCarrierDemodLock(RAF_LockStatus status)
    {
        this.statusInfo.lock();
        this.statusInfo.setSubCarrDemodLock(status);
        this.statusInfo.unlock();
    }

    /**
     * See specification of IRAF_SIUpdate.
     */
    @Override
    public void setSymbolSyncLock(RAF_LockStatus status)
    {
        this.statusInfo.lock();
        this.statusInfo.setSymbolSyncLock(status);
        this.statusInfo.unlock();
    }

    /**
     * See specification of IRAF_SIUpdate.
     */
    @Override
    public RAF_ProductionStatus getProductionStatus()
    {

        this.statusInfo.lock();
        RAF_ProductionStatus ps = this.statusInfo.getProductionStatus();
        this.statusInfo.unlock();
        return ps;

    }

    /**
     * See specification of IRAF_SIUpdate.
     */
    @Override
    public RAF_LockStatus getFrameSyncLock()
    {
        this.statusInfo.lock();
        RAF_LockStatus ls = this.statusInfo.getFrameSyncLock();
        this.statusInfo.unlock();
        return ls;
    }

    /**
     * See specification of IRAF_SIUpdate.
     */
    @Override
    public RAF_LockStatus getCarrierDemodLock()
    {
        this.statusInfo.lock();
        RAF_LockStatus ls = this.statusInfo.getCarrierDemodLock();
        this.statusInfo.unlock();
        return ls;
    }

    /**
     * See specification of IRAF_SIUpdate.
     */
    @Override
    public RAF_LockStatus getSubCarrierDemodLock()
    {

        this.statusInfo.lock();
        RAF_LockStatus ls = this.statusInfo.getSubCarrDemodLock();
        this.statusInfo.unlock();
        return ls;
    }

    /**
     * See specification of IRAF_SIUpdate.
     */
    @Override
    public RAF_LockStatus getSymbolSyncLock()
    {
        this.statusInfo.lock();
        RAF_LockStatus ls = this.statusInfo.getSymbolSyncLock();
        this.statusInfo.unlock();
        return ls;

    }

    /**
     * See specification of IRAF_SIUpdate.
     */
    @Override
    public long getNumErrorFreeFrames()
    {
        this.statusInfo.lock();
        long eff = this.statusInfo.getNumErrorFreeFrames();
        this.statusInfo.unlock();
        return eff;
    }
    
    

    /**
     * See specification of IRAF_SIUpdate.
     */
    @Override
    public long getNumFrames()
    {
        this.statusInfo.lock();
        long nf = this.statusInfo.getNumFrames();
        this.statusInfo.unlock();
        return nf;
    }

    /**
     * See specification of IRAF_SIUpdate.
     */
    @Override
    public RAF_ParFrameQuality getRequestedFrameQuality()
    {

        if (getSIState() != SLE_SIState.sleSIS_active)
        {
            return RAF_ParFrameQuality.rafPQ_undefined;
        }

        this.statusInfo.lock();
        RAF_ParFrameQuality fq = this.statusInfo.getReqFrameQuality();
        this.statusInfo.unlock();
        return fq;

    }
    /**
     * Get the configured permitted frame quality
     * @return
     */
    @Override
    public RAF_ParFrameQuality[] getPermittedFrameQuality()
    {
    	return this.config.getPermittedFrameQuality();
    }
    
    /**
     * Implementation of the configured minimum reporting cycle
     * @since SLES V5.
     * @return the value
     */
    public long getMinimumReportCycle()
    {
    	return this.config.getMinimumReportingCycle();
    }
    
    /**
     * See specification of ISLE_SIOpFactory.
     */
    @Override
    public <T extends ISLE_Operation> T createOperation(Class<T> iid, SLE_OpType optype) throws SleApiException
    {

        if (isConfigured() == false)
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        // check operation type supported for a RAF service provider SI:
        if (optype != SLE_OpType.sleOT_transferData && optype != SLE_OpType.sleOT_syncNotify
            && optype != SLE_OpType.sleOT_peerAbort)
        {
            throw new SleApiException(HRESULT.SLE_E_TYPE);
        }

        ISLE_OperationFactory opf = getOpFactory();

        HRESULT rc = HRESULT.S_OK;
        T ppv = null;
        try
        {
        	if(optype == SLE_OpType.sleOT_scheduleStatusReport)
            {
            	ppv = opf.createOperation(iid, optype, SLE_ApplicationIdentifier.sleAI_rtnAllFrames, getVersion(), getMinimumReportCycle());
            }
            else
            {
            	ppv = opf.createOperation(iid, optype, SLE_ApplicationIdentifier.sleAI_rtnAllFrames, getVersion());
            }
            //ppv = opf.createOperation(iid, optype, SLE_ApplicationIdentifier.sleAI_rtnAllFrames, getVersion());
        }
        catch (SleApiException e)
        {
            rc = e.getHResult();
        }

        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }

        // no specific set-up for TRANSFER-DATA required
        // no specific set-up for SYNC-NOTIFY required

        // PEER-ABOR-specific set-up supported by base-class:
        setUpOperation(optype, ppv);

        return ppv;

    }

    /**
     * Returns a pointer to the configuration object.
     */
    @Override
    protected EE_APISE_PConfiguration getConfiguration()
    {
        return this.config;
    }

    /**
     * Performs RAF provider service instance specific configuration checks.
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
        RAF_DeliveryMode dm = this.config.getDeliveryMode().asRAF_DeliveryMode();
        if (dm == RAF_DeliveryMode.rafDM_invalid)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                    EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                    "Invalid RAF Delivery Mode");
            return HRESULT.SLE_E_CONFIG;
        }

        long bufferSize = this.config.getTransferBufferSize();

        if (bufferSize < EE_APISE_RAF_Limits.getMinBufferSize())
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                    EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                    "Invalid Minimum Buffer Size");
            return HRESULT.SLE_E_CONFIG;
        }

        // check the latency-limit
        if ((dm == RAF_DeliveryMode.rafDM_timelyOnline) || (dm == RAF_DeliveryMode.rafDM_completeOnline))
        {
            int ll = this.config.getLatencyLimit();
            if (ll < EE_APISE_RAF_Limits.getMinLatencyLimit())
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

        // set-ip according to SE-2.1 and sect. 4.3.2 of RAF-spec:
        // set values for delivery mode offline, if not set vis
        // IRAF_SIAdmin:
        this.statusInfo.lock();
        if (dm == RAF_DeliveryMode.rafDM_offline)
        {
            if (this.statusInfo.getFrameSyncLock() == RAF_LockStatus.rafLS_invalid)
            {
                this.statusInfo.setFrameSyncLock(RAF_LockStatus.rafLS_unknown);
            }
            if (this.statusInfo.getSymbolSyncLock() == RAF_LockStatus.rafLS_invalid)
            {
                this.statusInfo.setSymbolSyncLock(RAF_LockStatus.rafLS_unknown);
            }
            if (this.statusInfo.getSubCarrDemodLock() == RAF_LockStatus.rafLS_invalid)
            {
                this.statusInfo.setSubCarrDemodLock(RAF_LockStatus.rafLS_unknown);
            }
            if (this.statusInfo.getCarrierDemodLock() == RAF_LockStatus.rafLS_invalid)
            {
                this.statusInfo.setCarrierDemodLock(RAF_LockStatus.rafLS_unknown);
            }
        }
        else
        {
            if (this.statusInfo.getProductionStatus() == RAF_ProductionStatus.rafPS_invalid
                || this.statusInfo.getFrameSyncLock() == RAF_LockStatus.rafLS_invalid
                || this.statusInfo.getSymbolSyncLock() == RAF_LockStatus.rafLS_invalid
                || this.statusInfo.getSubCarrDemodLock() == RAF_LockStatus.rafLS_invalid
                || this.statusInfo.getCarrierDemodLock() == RAF_LockStatus.rafLS_invalid)
            {
                this.statusInfo.unlock();
                logRecord(SLE_LogMessageType.sleLM_alarm,
                        EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                        "Invalid RAF Status Rnfo");
                return HRESULT.SLE_E_CONFIG;
            }
        }
        
        // set-up remaining initial values acc. to SE2.2 and SE2.3, SE2.4 and
        // 4.3.2 of RAF-spec:

        this.statusInfo.setNumErrorFreeFrames(0);
        this.statusInfo.setNumFrames(0);
        this.statusInfo.setReqFrameQuality(RAF_ParFrameQuality.rafPQ_undefined);
        this.statusInfo.unlock();

        return HRESULT.S_OK;
    }

    /**
     * Obtains all TRANSFER-DATA operations from the transfer-buffer, increments
     * the number of delivered frames. In addition, the number of
     * error-free-frames-delivered is incremented if the frame-quality in the
     * TRANSFER-DATA operation indicates 'good frame'.
     */
    @Override
    protected void updateStatusInfo(ISLE_TransferBuffer transmittedBuffer)
    {

        ISLE_Operation op = null;
        transmittedBuffer.reset();

        while (transmittedBuffer.moreData() == true)
        {
            op = transmittedBuffer.next();
            if (op.getOperationType() == SLE_OpType.sleOT_transferData)
            {
                IRAF_TransferData tdOp = (IRAF_TransferData) op;
                this.statusInfo.lock();
                long nf = this.statusInfo.getNumFrames();
                this.statusInfo.setNumFrames(nf + 1);

                if (tdOp.getFrameQuality() == RAF_FrameQuality.rafFQ_good)
                {
                    long frames = this.statusInfo.getNumErrorFreeFrames();
                    this.statusInfo.setNumErrorFreeFrames(frames + 1);
                }
                this.statusInfo.unlock();
            }
        }

    }

    /**
     * Creates a new RAF specific status report operation and initialises it.
     * When all values have been set, it passes the operation to the interface
     * ISLE_SrvProxyInitiate for transmission to the user.
     */
    @Override
    protected HRESULT doStatusReport()
    {

        ISLE_OperationFactory opf = getOpFactory();
        HRESULT rc = HRESULT.S_OK;
        IRAF_StatusReport sr = null;
        try
        {
            sr = opf.createOperation(IRAF_StatusReport.class,
                                     SLE_OpType.sleOT_statusReport,
                                     SLE_ApplicationIdentifier.sleAI_rtnAllFrames,
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
     * RAF-GET-PARAMETER operation.. When the value has been set, it passes the
     * operation to the interface ISLE_SrvProxyInitiate for transmission to the
     * user.
     */
    @Override
    protected HRESULT doGetParameter(ISLE_Operation poperation)
    {

        IRAF_GetParameter gp = (IRAF_GetParameter) poperation;

        RAF_ParameterName pname = gp.getRequestedParameter();

        HRESULT rc = HRESULT.SLE_E_UNKNOWN;

        if (pname == RAF_ParameterName.rafPN_reportingCycle)
        {
            gp.setReportingCycle(getReportingCycle());
            rc = HRESULT.S_OK;
        }
        else if (pname == RAF_ParameterName.rafPN_returnTimeoutPeriod)
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
            gp.setGetParameterDiagnostic(RAF_GetParameterDiagnostic.rafGP_unknownParameter);
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

        // cleanup is called fpollowing peer-abort and protocol-abort,
        // therefore the req-frame-quality is reset here
        this.statusInfo.lock();
       	this.statusInfo.setReqFrameQuality(RAF_ParFrameQuality.rafPQ_undefined);
        this.statusInfo.unlock();

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
        HRESULT rc = super.doInformOpReturn(poperation);
        if (rc != HRESULT.S_OK)
        {
            return rc;
        }

        return HRESULT.SLE_E_ROLE;
    }

    /**
     * Performs all checks on the RAF-START operation supplied by the Proxy.
     * When the checks are completed successfully, state-processing is
     * initiated.
     * 
     * @throws SleApiException
     */

    private HRESULT startInv(ISLE_Operation poperation)
    {

        IRAF_Start s = (IRAF_Start) poperation;

        ISLE_Time startt = s.getStartTime();
        ISLE_Time stopt = s.getStopTime();

        RAF_DeliveryMode dm = this.config.getDeliveryMode().asRAF_DeliveryMode();

        HRESULT rc = HRESULT.S_OK;

        if (dm == RAF_DeliveryMode.rafDM_offline)
        {
            if (startt == null)
            {
                s.setStartDiagnostic(RAF_StartDiagnostic.rafSD_missingTimeValue);
                rc = HRESULT.E_FAIL;
            }
            if (stopt == null)
            {
                s.setStartDiagnostic(RAF_StartDiagnostic.rafSD_missingTimeValue);
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
                currentTime.update();

                // check if stop time is in the future
                if (!(stopt.compareTo(currentTime) < 0))
                {
                    s.setStartDiagnostic(RAF_StartDiagnostic.rafSD_invalidStopTime);
                    rc = HRESULT.E_FAIL;
                }
            }
        } // end offline delivery mode
        else
        {
            // delivery-mode is online
            final ISLE_Time ppstart = getProvisionPeriodStart();
            final ISLE_Time ppstop = getProvisionPeriodStop();
            if (startt != null)
            {
                if (!(startt.compareTo(ppstart) >= 0) || !(startt.compareTo(ppstop) < 0))
                {
                    s.setStartDiagnostic(RAF_StartDiagnostic.rafSD_invalidStartTime);
                    rc = HRESULT.E_FAIL;
                }
            }
            if (rc == HRESULT.S_OK && stopt != null)
            {
                if (!(stopt.compareTo(ppstop) <= 0))
                {
                    s.setStartDiagnostic(RAF_StartDiagnostic.rafSD_invalidStopTime);
                    rc = HRESULT.E_FAIL;
                }
            }

        } // end online delivery mode

        if (startt != null)
        {
            startt = null;
        }
        if (stopt != null)
        {
            stopt = null;
        }

        if (rc != HRESULT.S_OK)
        {
            initiatePxyOpRtn(s, false);
            return HRESULT.S_OK;
        }
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_StartInv, poperation);
    }

    /**
     * Performs all checks on the RAF-START return supplied by the Appliaction.
     * When the checks are completed successfully, state-processing is
     * initiated. The status parameter 'requested-frame-quality' is updated
     * according to the value obtained from the return-PDU.
     * ///////////////////////////////////////////////////////
     */

    private HRESULT startRtn(ISLE_ConfirmedOperation poperation)
    {

        IRAF_Start s = (IRAF_Start) poperation;

        // checks: nothing left to check here

        // update status info
        if (s.getResult() == SLE_Result.sleRES_positive)
        {
            RAF_RequestedFrameQuality rfq = s.getRequestedFrameQuality();
            RAF_ParFrameQuality pfq = RAF_ParFrameQuality.rafPQ_undefined;
            if (rfq == RAF_RequestedFrameQuality.rafRQ_goodFramesOnly)
            {
                pfq = RAF_ParFrameQuality.rafPQ_goodFramesOnly;
            }
            else if (rfq == RAF_RequestedFrameQuality.rafRQ_erredFramesOnly)
            {
                pfq = RAF_ParFrameQuality.rafPQ_erredFramesOnly;
            }
            else if (rfq == RAF_RequestedFrameQuality.rafRQ_allFrames)
            {
                pfq = RAF_ParFrameQuality.rafPQ_allFrames;
            }

            this.statusInfo.lock();
            this.statusInfo.setReqFrameQuality(pfq);
            this.statusInfo.unlock();

        }

        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_StartRtn, poperation);

    }

    /**
     * Performs all checks on the RAF-SYNC-NOTIFY invocation supplied by the
     * application(see section 3.1.3.3 in the RAF supplement). When the checks
     * are completed successfully, state-processing is initiated.
     */

    private HRESULT syncNotifyInv(ISLE_Operation poperation)
    {

        IRAF_SyncNotify sn = (IRAF_SyncNotify) poperation;
        RAF_NotificationType nt = sn.getNotificationType();

        if (nt == RAF_NotificationType.rafNT_invalid)
        {
            return HRESULT.SLE_E_MISSINGARG;
        }

        RAF_DeliveryMode dm = this.config.getDeliveryMode().asRAF_DeliveryMode();

        if (dm == RAF_DeliveryMode.rafDM_offline && nt != RAF_NotificationType.rafNT_endOfData)
        {
            return HRESULT.SLE_E_INCONSISTENT;
        }
        else if (dm == RAF_DeliveryMode.rafDM_timelyOnline && nt == RAF_NotificationType.rafNT_excessiveDataBacklog)
        {
            return HRESULT.SLE_E_INCONSISTENT;
        }

        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_SyncNotifyInv, poperation);

    }

    /**
     * Performs all checks on the RAF-TRANSFER-DATA operation supplied by the
     * application. When the checks are completed successfully, state-processing
     * is initiated.
     */

    private HRESULT transferDataInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_TransferDataInv, poperation);
    }

    /**
     * Performs all checks on the RAF-GET-PARAMETER operation supplied by the
     * Proxy. When the checks are completed successfully, state-processing is
     * initiated.
     */

    private HRESULT getParameterInv(ISLE_Operation poperation)
    {

        IRAF_GetParameter gp = (IRAF_GetParameter) poperation;
        RAF_ParameterName pname = gp.getRequestedParameter();

        if (pname == RAF_ParameterName.rafPN_invalid)
        {
            gp.setGetParameterDiagnostic(RAF_GetParameterDiagnostic.rafGP_unknownParameter);
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
     * undefined.
     */

    private HRESULT stopRtn(ISLE_ConfirmedOperation poperation)
    {

        ISLE_Stop s = (ISLE_Stop) poperation;
        if (s.getResult() == SLE_Result.sleRES_positive)
        {
            this.statusInfo.lock();
            this.statusInfo.setReqFrameQuality(RAF_ParFrameQuality.rafPQ_undefined);
            this.statusInfo.unlock();
        }

        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_StopRtn, poperation);
    }

    /**
     * Prepends a 'buffer-discarded' RAF-SYNC-NOTIFY operation to the supplied
     * transfer-buffer.
     */
    @Override
    protected void prependNotification(ISLE_TransferBuffer buffer)
    {
        IRAF_SyncNotify sn = null;
        try
        {
            sn = createOperation(IRAF_SyncNotify.class, SLE_OpType.sleOT_syncNotify);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }
        sn.setDataDiscarded();
        buffer.prepend(sn, true);
    }

    /**
     * Returns true if the supplied operation is a 'EndOfData' RAF-SYNC-NOTIFY
     * operation.
     */
    @Override
    protected boolean isEndOfData(ISLE_Operation poperation)
    {
        if (poperation.getOperationType() != SLE_OpType.sleOT_syncNotify)
        {
            return false;
        }

        IRAF_SyncNotify sn = (IRAF_SyncNotify) poperation;
        if (sn.getNotificationType() == RAF_NotificationType.rafNT_endOfData)
        {
            return true;
        }

        return false;

    }

    @Override
    public void sendBufferTransfer(boolean withNotification)
    {
        this.objMutex.lock();
        super.sendBuffer(withNotification);
        this.objMutex.unlock();
    }



}
