package esa.sle.impl.api.apise.fspse;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iop.ISLE_Stop;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SIOpFactory;
import ccsds.sle.api.isle.it.SLE_AbortOriginator;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_DeliveryMode;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.it.SLE_SIState;
import ccsds.sle.api.isle.it.SLE_SlduStatusNotification;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.ifsp.IFSP_AsyncNotify;
import ccsds.sle.api.isrv.ifsp.IFSP_FOPMonitor;
import ccsds.sle.api.isrv.ifsp.IFSP_GetParameter;
import ccsds.sle.api.isrv.ifsp.IFSP_InvokeDirective;
import ccsds.sle.api.isrv.ifsp.IFSP_SIAdmin;
import ccsds.sle.api.isrv.ifsp.IFSP_SIUpdate;
import ccsds.sle.api.isrv.ifsp.IFSP_Start;
import ccsds.sle.api.isrv.ifsp.IFSP_StatusReport;
import ccsds.sle.api.isrv.ifsp.IFSP_ThrowEvent;
import ccsds.sle.api.isrv.ifsp.IFSP_TransferData;
import ccsds.sle.api.isrv.ifsp.types.FSP_EventResult;
import ccsds.sle.api.isrv.ifsp.types.FSP_Failure;
import ccsds.sle.api.isrv.ifsp.types.FSP_FopAlert;
import ccsds.sle.api.isrv.ifsp.types.FSP_GetParameterDiagnostic;
import ccsds.sle.api.isrv.ifsp.types.FSP_InvokeDirectiveDiagnostic;
import ccsds.sle.api.isrv.ifsp.types.FSP_NotificationType;
import ccsds.sle.api.isrv.ifsp.types.FSP_PacketStatus;
import ccsds.sle.api.isrv.ifsp.types.FSP_ParameterName;
import ccsds.sle.api.isrv.ifsp.types.FSP_PermittedTransmissionMode;
import ccsds.sle.api.isrv.ifsp.types.FSP_ProductionStatus;
import ccsds.sle.api.isrv.ifsp.types.FSP_TransferDataDiagnostic;
import ccsds.sle.api.isrv.ifsp.types.FSP_TransmissionMode;
import esa.sle.impl.api.apise.slese.EE_APISE_PConfiguration;
import esa.sle.impl.api.apise.slese.EE_APISE_PFSI;
import esa.sle.impl.api.apise.slese.types.EE_TI_SLESE_Event;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * FSP Provider Forward Service Instance This class provides the functionality
 * that is specific to FSP forward service instances for provider applications.
 * It is responsible for the creation of FSP service specific operation objects
 * for a provider application, and for checking of the compatibility of
 * invocation and return PDUs with the FSP provider role. Furthermore the class
 * collects statistical information to be compiled in the status report, which
 * is set-up and passed to the proxy by this class. The class implements the
 * interfaces IFSP_SIAdmin and IFSP_SIUpdate. The class implements the
 * update-mechanism and automatic sending of notifications as specified in the
 * FSP supplement, section 3.1.4. For a GET-PARAMETER invocation the class is
 * responsible to set the value of the desired parameter. If the class is
 * requested to perform a status report ( doStatusReport() ), it creates and
 * initializes a FSP-STATUS-REPORT operation using the data collected for the
 * status information. For PDUs received from the application, the class looks
 * at TRANSFER-DATA and THROW-EVENT return PDU's in order to update the status
 * information parameters from the PDUs received. If a FSP-GET-PARAMETER is
 * received from the proxy, the class sets the desired parameter-value (function
 * setUpGetParameter()) and passes the return-PDU back to the proxy. Note that
 * the class applies the following approach for FSP-TRANSFER-DATA and
 * FSP-INVOKE-DIRECTIVE and FSP-THROW-EVENT invocations: if the check of the
 * invocation fails, the diagnostic code is set and the PDU is passed to the
 * application (which checks the result and initiates the return-PDU). See
 * FSP-supplement, sections 3.1.5.1 and 3.1.5.2, and 3.1.5.3 for more
 * information.
 */
public class EE_APISE_FSP_PFSI extends EE_APISE_PFSI implements IFSP_SIUpdate, ISLE_SIOpFactory
{
    private static final Logger LOG = Logger.getLogger(EE_APISE_FSP_PFSI.class.getName());

    /**
     * The last notification sent to the user.
     */
    private FSP_NotificationType lastSentNotification = FSP_NotificationType.fspNT_invalid;

    private final EE_APISE_FSP_Configuration config;

    private final EE_APISE_FSP_FOPMonitor fopMon;

    private final EE_APISE_FSP_StatusInformation statusInfo;

    private final EE_APISE_FSP_LastProcessed lastProcessed;

    private final EE_APISE_FSP_LastOK lastOK;


    public EE_APISE_FSP_PFSI(String instanceKey, ISLE_ServiceInform clientIf)
    {
        super(instanceKey, SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt, clientIf);

        this.config = new EE_APISE_FSP_Configuration(this);
        this.fopMon = new EE_APISE_FSP_FOPMonitor(this);
        this.lastOK = new EE_APISE_FSP_LastOK();
        this.lastProcessed = new EE_APISE_FSP_LastProcessed();
        this.statusInfo = new EE_APISE_FSP_StatusInformation();
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
            if (iid == IFSP_SIAdmin.class)
            {
                ppv = this.config.queryInterface(iid);
            }
            else if (iid == IFSP_FOPMonitor.class)
            {
                ppv = this.fopMon.queryInterface(iid);
            }
            else if (iid == IFSP_SIUpdate.class)
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
        return ppv;
    }

    @Override
    public FSP_ProductionStatus getProductionStatus()
    {
        return this.statusInfo.getProductionStatus();
    }

    @Override
    public SLE_YesNo getDirectiveInvocationOnline()
    {
        return this.statusInfo.getDirectiveInvocationOnline();
    }

    @Override
    public long getPacketBufferAvailable()
    {

        this.statusInfo.lock();
        long bs = this.statusInfo.getPacketBufferAvailable();
        this.statusInfo.unlock();
        return bs;

    }

    @Override
    public long getNumberOfADPacketsReceived()
    {

        this.statusInfo.lock();
        long num = this.statusInfo.getNumADPacketsReceived();
        this.statusInfo.unlock();
        return num;
    }

    @Override
    public long getNumberOfBDPacketsReceived()
    {

        this.statusInfo.lock();
        long num = this.statusInfo.getNumBDPacketsReceived();
        this.statusInfo.unlock();
        return num;

    }

    @Override
    public long getNumberOfADPacketsProcessed()
    {

        this.statusInfo.lock();
        long num = this.statusInfo.getNumADPacketsProcessed();
        this.statusInfo.unlock();
        return num;

    }

    @Override
    public long getNumberOfBDPacketsProcessed()
    {
        this.statusInfo.lock();
        long num = this.statusInfo.getNumBDPacketsProcessed();
        this.statusInfo.unlock();
        return num;
    }

    @Override
    public long getNumberOfADPacketsRadiated()
    {
        this.statusInfo.lock();
        long num = this.statusInfo.getNumADPacketsRadiated();
        this.statusInfo.unlock();
        return num;
    }

    @Override
    public long getNumberOfBDPacketsRadiated()
    {
        this.statusInfo.lock();
        long num = this.statusInfo.getNumBDPacketsRadiated();
        this.statusInfo.unlock();
        return num;
    }

    @Override
    public long getNumberOfPacketsAcknowledged()
    {

        this.statusInfo.lock();
        long num = this.statusInfo.getNumPacketsAcknowledged();
        this.statusInfo.unlock();
        return num;

    }

    @Override
    public long getPacketLastProcessed()
    {

        this.lastProcessed.lock();
        long id = this.lastProcessed.getPacketId();
        this.lastProcessed.unlock();
        return id;
    }

    @Override
    public ISLE_Time getProductionStartTime()
    {

        this.statusInfo.lock();
        this.lastProcessed.lock();

        ISLE_Time pst = null;

        if (this.statusInfo.getNumADPacketsProcessed() != 0 || this.statusInfo.getNumBDPacketsProcessed() != 0)
        {
            pst = this.lastProcessed.getProductionStartTime();
        }

        this.statusInfo.unlock();
        this.lastProcessed.unlock();
        return pst;

    }

    @Override
    public FSP_PacketStatus getPacketStatus()
    {
        this.statusInfo.lock();
        this.lastProcessed.lock();

        FSP_PacketStatus st = FSP_PacketStatus.fspST_invalid;

        if (this.statusInfo.getNumADPacketsProcessed() != 0 || this.statusInfo.getNumBDPacketsProcessed() != 0)
        {
            st = this.lastProcessed.getPacketStatus();
        }

        this.statusInfo.unlock();
        this.lastProcessed.unlock();
        return st;
    }

    @Override
    public long getPacketLastOk()
    {
        this.lastOK.lock();
        long id = this.lastOK.getPacketId();
        this.lastOK.unlock();
        return id;
    }

    @Override
    public ISLE_Time getProductionStopTime()
    {

        this.statusInfo.lock();
        this.lastOK.lock();

        ISLE_Time pst = null;

        if (this.statusInfo.getNumADPacketsRadiated() != 0 || this.statusInfo.getNumBDPacketsRadiated() != 0)
        {
            pst = this.lastOK.getProductionStopTime();
        }

        this.statusInfo.unlock();
        this.lastOK.unlock();
        return pst;

    }

    @Override
    public long getExpectedPacketId()
    {
        this.statusInfo.lock();
        long id = this.statusInfo.getExpectedSlduId();
        this.statusInfo.unlock();
        return id;
    }

    @Override
    public long getExpectedDirectiveInvocationId()
    {

        this.statusInfo.lock();
        long id = this.statusInfo.getExpectedDirectiveId();
        this.statusInfo.unlock();
        return id;
    }

    @Override
    public long getExpectedEventInvocationId()
    {
        this.statusInfo.lock();
        long id = this.statusInfo.getExpectedEventInvId();
        this.statusInfo.unlock();
        return id;
    }
    
    /**
     * Sets the CLTU config parameter minimum-reporting-cycle in seconds
     * SLES parameter ID 301
     * @since SLES V5
     */
    public void setMinimumReportingCycle(long mrc)
    {
    	this.config.setMinimumReportingCycle(mrc);
    }
    
    /**
     * Gets the CLTU config parameter minimum-reporting-cycle in seconds
     * SLES parameter ID 301
     * @since SLES V5
     * @return minimum-reporting-cycle in seconds as type long
     */
    public long getMinimumReportingCycle()
    {
    	return this.config.getMinimumReportingCycle();
    }

    @Override
    public <T extends ISLE_Operation> T createOperation(Class<T> iid, SLE_OpType optype) throws SleApiException
    {

        if (isConfigured() == false)
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        // check operation type supported for a FSP service provider SI:
        if (optype != SLE_OpType.sleOT_asyncNotify && optype != SLE_OpType.sleOT_peerAbort)
        {
            throw new SleApiException(HRESULT.SLE_E_TYPE);
        }

        ISLE_OperationFactory opf = getOpFactory();
        int vn = getVersion();
        T ppv = null;
        if(optype == SLE_OpType.sleOT_scheduleStatusReport)
        {
        	ppv = opf.createOperation(iid, optype, SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt, vn, getMinimumReportingCycle());
        }
        else
        {
        	ppv = opf.createOperation(iid, optype, SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt, vn);
        }
        //T ppv = opf.createOperation(iid, optype, SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt, vn);

        if (ppv == null)
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        if (optype == SLE_OpType.sleOT_asyncNotify)
        {
            this.lastProcessed.lock();
            this.lastOK.lock();
            this.statusInfo.lock();

            IFSP_AsyncNotify an = (IFSP_AsyncNotify) (ppv);

            an.setNotificationType(FSP_NotificationType.fspNT_invalid);
            an.setDirectiveExecutedId(0);
            an.setEventThrownId(0);
            an.setFopAlert(FSP_FopAlert.fspFA_invalid);

            an.setPacketLastProcessed(this.lastProcessed.getPacketId());

            ISLE_Time pst = this.lastProcessed.getProductionStartTime();
            if (pst != null)
            {
                an.setProductionStartTime(pst);
            }

            an.setPacketStatus(this.lastProcessed.getPacketStatus());
            an.setPacketLastOk(this.lastOK.getPacketId());

            ISLE_Time t = this.lastOK.getProductionStopTime();
            if (t != null)
            {
                an.setProductionStopTime(t);
            }

            an.setProductionStatus(this.statusInfo.getProductionStatus());

            this.lastProcessed.unlock();
            this.lastOK.unlock();
            this.statusInfo.unlock();

        }
        setUpOperation(optype, ppv);
        return ppv;
    }

    public void setInitialProductionStatus(FSP_ProductionStatus status)
    {
        this.statusInfo.setProductionStatus(status);
    }

    public void setInitialDirectiveInvocationOnline(SLE_YesNo yesNo)
    {
        this.statusInfo.setDirectiveInvocationOnline(yesNo);
    }

    @Override
    protected EE_APISE_PConfiguration getConfiguration()
    {
        return this.config;
    }

    /**
     * Performs FSP provider service instance specific configuration checks.
     */
    @Override
    protected HRESULT doConfigCompleted()
    {

        HRESULT rc = HRESULT.S_OK;
        rc = super.doConfigCompleted();

        if (rc != HRESULT.S_OK)
        {
            return rc;
        }

        HRESULT ccrc = HRESULT.S_OK;

        // check specific configuration parameters

        rc = this.config.doConfigCompleted();
        if (rc != HRESULT.S_OK)
        {
            ccrc = HRESULT.SLE_E_CONFIG;
        }

        rc = this.fopMon.doConfigCompleted();
        if (rc != HRESULT.S_OK)
        {
            ccrc = HRESULT.SLE_E_CONFIG;
        }

        // check initial status parameters

        if (this.statusInfo.getProductionStatus() == FSP_ProductionStatus.fspPS_invalid)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                      EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                      "Invalid or missing Initial Production Status");
            ccrc = HRESULT.SLE_E_CONFIG;
        }
        if (this.config.getDirectiveInvocationEnabled() != SLE_YesNo.sleYN_Yes &&
        		this.statusInfo.getDirectiveInvocationOnline() == SLE_YesNo.sleYN_invalid)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                      EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                      "Invalid or missing Directive Invocation Online");
            ccrc = HRESULT.SLE_E_CONFIG;
        }
        if (ccrc != HRESULT.S_OK)
        {
            return HRESULT.SLE_E_CONFIG;
        }

        // set-up config values according to section 4.2.3 of the FSP
        // supplement:

        // currently the only possible value for FSP is on-line
        this.config.setDeliveryMode(SLE_DeliveryMode.sleDM_fwdOnline);

        this.lastProcessed.setPacketId(0);
        this.lastProcessed.setProductionStartTime(null);
        this.lastProcessed.setPacketStatus(FSP_PacketStatus.fspST_invalid);

        this.lastOK.setPacketId(0);
        this.lastOK.setProductionStopTime(null);

        this.statusInfo.setPacketBufferAvailable(this.config.getMaximumBufferSize());

        this.statusInfo.setNumADPacketsReceived(0);
        this.statusInfo.setNumBDPacketsReceived(0);
        this.statusInfo.setNumADPacketsProcessed(0);
        this.statusInfo.setNumBDPacketsProcessed(0);
        this.statusInfo.setNumADPacketsRadiated(0);
        this.statusInfo.setNumBDPacketsRadiated(0);
        this.statusInfo.setNumPacketsAcknowledged(0);
        this.statusInfo.setExpectedSlduId(0);
        this.statusInfo.setExpectedEventInvId(0);
        this.statusInfo.setExpectedDirectiveId(0);

        return HRESULT.S_OK;

    }

    /**
     * Creates a new FSP specific status report operation and initializes it.
     * When all values have been set, it passes the operation to the interface
     * ISLE_SrvProxyInitiate for transmission to the user.
     */
    @Override
    protected HRESULT doStatusReport()
    {
        ISLE_OperationFactory opf = getOpFactory();

        int vn = getVersion();
        HRESULT rc = HRESULT.S_OK;
        IFSP_StatusReport sr = null;
        try
        {
            sr = opf.createOperation(IFSP_StatusReport.class,
                                     SLE_OpType.sleOT_statusReport,
                                     SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt,
                                     vn);
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
        this.lastProcessed.lock();
        this.lastOK.lock();

        this.lastProcessed.setUpReport(sr);
        this.lastOK.setUpReport(sr);
        this.statusInfo.setUpReport(sr);

        this.statusInfo.unlock();
        this.lastProcessed.unlock();
        this.lastOK.unlock();

        rc = initiatePxyOpInv(sr, false);
        return rc;
    }

    /**
     * Performs setting of the required parameter to the supplied GetParameter
     * operation.. When the value has been set, it passes the operation to the
     * interface ISLE_SrvProxyInitiate for transmission to the user.
     */
    @Override
    protected HRESULT doGetParameter(ISLE_Operation poperation)
    {

        IFSP_GetParameter gp = (IFSP_GetParameter) poperation;

        FSP_ParameterName pname = gp.getRequestedParameter();

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(" pname: " + pname);
        }

        HRESULT rc = HRESULT.SLE_E_UNKNOWN;

        if (pname == FSP_ParameterName.fspPN_reportingCycle)
        {
            gp.setReportingCycle(getReportingCycle());
            rc = HRESULT.S_OK;
        }
        else if (pname == FSP_ParameterName.fspPN_returnTimeoutPeriod)
        {
            gp.setReturnTimeoutPeriod(getReturnTimeout());
            rc = HRESULT.S_OK;
        }
        else
        {
            synchronized (this.config)
            {
                synchronized (this.config)
                {
                    rc = this.config.setUpGetParameter(gp);
                    if (rc != HRESULT.S_OK)
                    {
                        rc = this.fopMon.setUpGetParameter(gp);
                    }
                }
            }
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
            gp.setGetParameterDiagnostic(FSP_GetParameterDiagnostic.fspGP_unknownParameter);
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
        if (this.statusInfo != null && this.config != null)
        {
            this.statusInfo.lock();
            this.statusInfo.setPacketBufferAvailable(this.config.getMaximumBufferSize());
            this.statusInfo.setDirectiveInvocationOnline(SLE_YesNo.sleYN_No);
            this.statusInfo.unlock();
        }
        this.lastSentNotification = FSP_NotificationType.fspNT_invalid;
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
        case sleOT_asyncNotify:
            return asyncNotifyInv(poperation);
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
        case sleOT_transferData:
            return transferDataRtn(poperation);
        case sleOT_throwEvent:
            return throwEventRtn(poperation);
        case sleOT_invokeDirective:
            return invokeDirectiveRtn(poperation);

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

        SLE_OpType opType = poperation.getOperationType();
        HRESULT rc = HRESULT.S_OK;

        // do not make checks for TRANSFER-DATA,
        // THROW-EVENT, and INVOKE-DIRECTIVE;
        // these PDUs have special error-handling, see
        // transferDataInv() and throwEventInv()

        if (opType != SLE_OpType.sleOT_transferData && opType != SLE_OpType.sleOT_throwEvent
            && opType != SLE_OpType.sleOT_invokeDirective)
        {
            rc = super.doInformOpInvoke(poperation);
            if (rc != HRESULT.S_OK)
            {
                return rc;
            }
        }

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
        case sleOT_transferData:
            return transferDataInv(poperation);
        case sleOT_throwEvent:
            return throwEventInv(poperation);
        case sleOT_invokeDirective:
            return invokeDirectiveInv(poperation);
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
     * Performs all checks on the FSP-TRANSFER-DATA operation supplied by the
     * Proxy. When the checks are completed successfully, state-processing is
     * initiated.
     */

    private HRESULT transferDataInv(ISLE_Operation poperation)
    {

        HRESULT rc = HRESULT.S_OK;
        rc = checkInformOpInvoke(poperation, false); // do not send return on
                                                     // failure

        IFSP_TransferData td = (IFSP_TransferData) poperation;

        ISLE_Time ept = td.getEarliestProdTime();
        ISLE_Time lpt = td.getLatestProdTime();

        // always check the result code in order to guarantee
        // the defined sequence of diagnostic code.

        if ((rc == HRESULT.S_OK) && (ept != null && lpt != null))
        {
            if (!(ept.compareTo(lpt) < 0))
            {
                td.setTransferDataDiagnostic(FSP_TransferDataDiagnostic.fspXFD_inconsistentTimeRange);
                rc = HRESULT.E_FAIL;
            }
            // check if prod start/end overlap provision period
            if (rc == HRESULT.S_OK)
            {
                ISLE_Time ppstart = getProvisionPeriodStart();
                ISLE_Time ppstop = getProvisionPeriodStop();
                if (ept.compareTo(ppstart) < 0 || lpt.compareTo(ppstop) > 0)
                {
                    td.setTransferDataDiagnostic(FSP_TransferDataDiagnostic.fspXFD_invalidTime);
                    rc = HRESULT.E_FAIL;
                }
            }
        }

        // check correct usage of MapIds:
        SLE_YesNo segmentationUsed = this.config.getSegmentHeaderPresent();
        if (rc == HRESULT.S_OK && segmentationUsed == SLE_YesNo.sleYN_Yes)
        {
            long mapId = td.getMapId();
            if (!this.config.isMapIdListMember(mapId))
            {
                td.setTransferDataDiagnostic(FSP_TransferDataDiagnostic.fspXFD_invalidMap);
                rc = HRESULT.E_FAIL;
            }
        }
        else if (rc == HRESULT.S_OK && segmentationUsed == SLE_YesNo.sleYN_No)
        {
            // if segmentation is not used, no MapId shall be set
            if (td.getMapIdUsed() == true)
            {
                td.setTransferDataDiagnostic(FSP_TransferDataDiagnostic.fspXFD_invalidMap);
                rc = HRESULT.E_FAIL;
            }
        }

        FSP_TransmissionMode transmissionMode = td.getTransmissionMode();
        FSP_PermittedTransmissionMode permTransMode = this.config.getPermittedTransmissionMode();

        if (permTransMode != FSP_PermittedTransmissionMode.fspPTM_any)
        {
            if ((transmissionMode == FSP_TransmissionMode.fspTM_sequenceControlled)
                && (permTransMode != FSP_PermittedTransmissionMode.fspPTM_sequenceControlled))
            {
                rc = HRESULT.E_FAIL;
            }
            else if ((transmissionMode == FSP_TransmissionMode.fspTM_sequenceControlledUnblock)
                     && (permTransMode != FSP_PermittedTransmissionMode.fspPTM_sequenceControlled))
            {
                rc = HRESULT.E_FAIL;
            }
            else if ((transmissionMode == FSP_TransmissionMode.fspTM_expedited)
                     && (permTransMode != FSP_PermittedTransmissionMode.fspPTM_expedited))
            {
                rc = HRESULT.E_FAIL;
            }
            if (rc == HRESULT.E_FAIL)
            {
                td.setTransferDataDiagnostic(FSP_TransferDataDiagnostic.fspXFD_invalidTransmissionMode);
            }
        }

        if (rc == HRESULT.S_OK
            && (transmissionMode == FSP_TransmissionMode.fspTM_expedited && td.getAcknowledgedNotification() == SLE_SlduStatusNotification.sleSN_produceNotification))
        {
            td.setTransferDataDiagnostic(FSP_TransferDataDiagnostic.fspXFD_invalidNotificationRequest);
            rc = HRESULT.E_FAIL;
        }

        if (rc == HRESULT.S_OK)
        {

            byte[] data = td.getData();
            if (data.length > this.config.getMaximumPacketLength())
            {
                td.setTransferDataDiagnostic(FSP_TransferDataDiagnostic.fspXFD_packetTooLong);
                rc = HRESULT.E_FAIL;
            }
        }

        if (rc == HRESULT.S_OK)
        {
            td.setPositiveResult(); // according to SE-21.3
        }

        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_TransferDataInv, poperation);

    }

    /**
     * Performs all checks on the FSP-TRANSFER-DATA return-PDU supplied by the
     * Application. When the checks are completed successfully, state-processing
     * is initiated. The function also obtains status-parameters from the
     * supplied operation object and copies them to the internal status
     * parameters
     */

    private HRESULT transferDataRtn(ISLE_ConfirmedOperation poperation)
    {

        IFSP_TransferData td = (IFSP_TransferData) poperation;
        this.statusInfo.lock();

        this.statusInfo.setExpectedSlduId(td.getExpectedPacketId());
        this.statusInfo.setPacketBufferAvailable(td.getPacketBufferAvailable());

        if (td.getResult() == SLE_Result.sleRES_positive)
        {
            FSP_TransmissionMode tm = td.getTransmissionMode();

            if (tm == FSP_TransmissionMode.fspTM_sequenceControlled)
            {

                this.statusInfo.incrNumReceivedAD();
            }
            else if (tm == FSP_TransmissionMode.fspTM_expedited)
            {

                this.statusInfo.incrNumReceivedBD();
            }
        }

        this.statusInfo.unlock();

        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_TransferDataRtn, poperation);

    }

    /**
     * Performs all checks on the FSP-THROW-EVENT operation supplied by the
     * Proxy. When the checks are completed successfully, state-processing is
     * initiated. If any error is encountered, the result is set to 'negative',
     * the appropriate diagnostic code is set and the PDU is forwarded to the
     * application after state-processing.
     */

    private HRESULT throwEventInv(ISLE_Operation poperation)
    {

        HRESULT rc = HRESULT.S_OK;
        rc = checkInformOpInvoke(poperation, false); // do not send return on
                                                     // failure

        IFSP_ThrowEvent te = (IFSP_ThrowEvent) poperation;

        if (rc == HRESULT.S_OK)
        {
            te.setPositiveResult(); // according to SE-21.3
        }

        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_ThrowEventInv, poperation);

    }

    /**
     * Performs all checks on the FSP-THROW-EVENT return-PDU supplied by the
     * Application. When the checks are completed successfully, state-processing
     * is initiated. The function also obtains status-parameters from the
     * supplied operation object and copies them to the internal status
     * parameters
     */

    private HRESULT throwEventRtn(ISLE_ConfirmedOperation poperation)
    {

        IFSP_ThrowEvent te = (IFSP_ThrowEvent) poperation;

        this.statusInfo.lock();
        this.statusInfo.setExpectedEventInvId(te.getExpectedEventInvocationId());
        this.statusInfo.unlock();

        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_ThrowEventRtn, poperation);

    }

    /**
     * Performs all checks on the FSP-INVOKE-DIRECTIVE operation supplied by the
     * Proxy. When the checks are completed successfully, state-processing is
     * initiated. If any error is encountered, the result is set to 'negative',
     * the appropriate diagnostic code is set and the PDU is forwarded to the
     * application after state-processing.
     */

    private HRESULT invokeDirectiveInv(ISLE_Operation poperation)
    {

        HRESULT rc = HRESULT.S_OK;
        rc = checkInformOpInvoke(poperation, false); // do not send return on
                                                     // failure

        IFSP_InvokeDirective te = (IFSP_InvokeDirective) poperation;

        if (rc == HRESULT.S_OK && this.config.getDirectiveInvocationEnabled() == SLE_YesNo.sleYN_No)
        {
            te.setInvokeDirectiveDiagnostic(FSP_InvokeDirectiveDiagnostic.fspID_directiveInvocationNotAllowed);
            initiatePxyOpRtn(te, false);
            return HRESULT.S_OK;
        }
        if (rc == HRESULT.S_OK)
        {
            te.setPositiveResult(); // according to SE-21.3
        }

        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_InvokeDirectiveInv, poperation);
    }

    /**
     * Performs all checks on the FSP-INVOKE-DIRECTIVE return-PDU supplied by
     * the Application. When the checks are completed successfully,
     * state-processing is initiated. The function also obtains
     * status-parameters from the supplied operation object and copies them to
     * the internal status parameters.
     */

    private HRESULT invokeDirectiveRtn(ISLE_ConfirmedOperation poperation)
    {

        IFSP_InvokeDirective te = (IFSP_InvokeDirective) poperation;

        this.statusInfo.lock();
        this.statusInfo.setExpectedDirectiveId(te.getExpectedDirectiveId());
        this.statusInfo.unlock();

        return doStateProcessing(SLE_Component.sleCP_application,
                                 EE_TI_SLESE_Event.eeSLESE_InvokeDirectiveRtn,
                                 poperation);

    }

    /**
     * Performs all checks on the FSP-GET-PARAMETER operation supplied by the
     * Proxy. When the checks are completed successfully, state-processing is
     * initiated.
     */

    private HRESULT getParameterInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_GetPrmInv, poperation);
    }

    /**
     * Performs all checks on the FSP-START operation supplied by the Proxy.
     * When the checks are completed successfully, state-processing is
     * initiated.
     */

    private HRESULT startInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_StartInv, poperation);
    }

    /**
     * Performs all checks on the FSP-START return supplied by the Appliaction.
     * When the checks are completed successfully, state-processing is
     * initiated. Status parameters are also updated by reading them from the
     * return-PDU.
     */

    private HRESULT startRtn(ISLE_ConfirmedOperation poperation)
    {
        IFSP_Start s = (IFSP_Start) poperation;

        if (s.getResult() == SLE_Result.sleRES_positive)
        {
            this.statusInfo.lock();
            this.statusInfo.setExpectedSlduId(s.getFirstPacketId());
            this.statusInfo.unlock();
        }
        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_StartRtn, poperation);
    }

    /**
     * Performs all checks on the FSP-ASYNC-NOTIFY invocation supplied by the
     * application. When the checks are completed successfully, state-processing
     * is initiated.
     */

    private HRESULT asyncNotifyInv(ISLE_Operation poperation)
    {

        IFSP_AsyncNotify an = (IFSP_AsyncNotify) poperation;

        if (an.getNotificationType() == FSP_NotificationType.fspNT_bufferEmpty)
        {
            this.statusInfo.lock();
            this.statusInfo.setPacketBufferAvailable(this.config.getMaximumBufferSize());
            this.statusInfo.unlock();
        }
        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_AsyncNotifyInv, poperation);
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
     * function also updates the status-parameter 'packet-buffer-available' from
     * the configuration parameter 'maximum-buffer-size'.
     */

    private HRESULT stopRtn(ISLE_ConfirmedOperation poperation)
    {
        ISLE_Stop s = (ISLE_Stop) poperation;

        if (s.getResult() == SLE_Result.sleRES_positive)
        {
            this.statusInfo.lock();
            this.statusInfo.setPacketBufferAvailable(this.config.getMaximumBufferSize());
            this.statusInfo.unlock();
        }
        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_StopRtn, poperation);
    }

    private void sendNotification(FSP_NotificationType type)
    {
        sendNotification(type, null, FSP_FopAlert.fspFA_invalid);
    }

    private void sendNotification(FSP_NotificationType type, long[] packetIdList)
    {
        sendNotification(type, packetIdList, FSP_FopAlert.fspFA_invalid);
    }

    /**
     * Creates and passes a notification with the supplied type to the proxy.
     */

    private void sendNotification(FSP_NotificationType type, long[] packetIdList, FSP_FopAlert fopAlert)
    {

        SLE_SIState state = getSIState();
        if (state == SLE_SIState.sleSIS_unbound || state == SLE_SIState.sleSIS_bindPending)
        {
            return;
        }

        IFSP_AsyncNotify an = null;
        HRESULT rc = HRESULT.S_OK;
        try
        {
            an = createOperation(IFSP_AsyncNotify.class, SLE_OpType.sleOT_asyncNotify);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            rc = e.getHResult();
        }
        if (an != null && rc == HRESULT.S_OK)
        {
            an.setNotificationType(type);
            if (packetIdList != null)
            {
                // Put... does not make a copy, so the packetId list must be
                // deleted by the async notify object.
                an.putPacketIdentificationList(packetIdList);
            }
            if (fopAlert != null && fopAlert != FSP_FopAlert.fspFA_invalid)
            {
                an.setFopAlert(fopAlert);
            }
            
            try
            {
            	this.objMutex.lock();// SLEAPIJ-86
            	initiatePxyOpInv(an, false);
            }
            finally
            {
            	this.objMutex.unlock();// SLEAPIJ-86 Part II
            }
        }
    }

    /**
     * Creates and passes a notification for completed directive invocations to
     * the proxy.
     */

    private void sendNotification(FSP_NotificationType type, long dirInvId, FSP_FopAlert fopAlert)
    {

        if (type != FSP_NotificationType.fspNT_positiveConfirmResponseToDirective
            && type != FSP_NotificationType.fspNT_negativeConfirmResponseToDirective)
        {
            return;
        }

        SLE_SIState state = getSIState();
        if (state == SLE_SIState.sleSIS_unbound || state == SLE_SIState.sleSIS_bindPending)
        {
            return;
        }

        IFSP_AsyncNotify an = null;
        HRESULT rc = HRESULT.S_OK;
        try
        {
            an = createOperation(IFSP_AsyncNotify.class, SLE_OpType.sleOT_asyncNotify);
        }
        catch (SleApiException e)
        {
            rc = e.getHResult();
        }
        if (an != null && rc == HRESULT.S_OK)
        {
            an.setNotificationType(type);
            an.setDirectiveExecutedId(dirInvId);
            an.setFopAlert(fopAlert);
            initiatePxyOpInv(an, false);
        }
    }

    /**
     * Creates and passes a notification for completed event invocations to the
     * proxy.
     */

    private void sendNotification(FSP_NotificationType type, long eventInvId, FSP_EventResult result)
    {

        if (type != FSP_NotificationType.fspNT_actionListCompleted
            && type != FSP_NotificationType.fspNT_actionListNotCompleted
            && type != FSP_NotificationType.fspNT_eventConditionEvFalse)
        {
            return;
        }

        SLE_SIState state = getSIState();
        if (state == SLE_SIState.sleSIS_unbound || state == SLE_SIState.sleSIS_bindPending)
        {
            return;
        }

        IFSP_AsyncNotify an = null;
        HRESULT rc = HRESULT.S_OK;
        try
        {
            an = createOperation(IFSP_AsyncNotify.class, SLE_OpType.sleOT_asyncNotify);
        }
        catch (SleApiException e)
        {
            rc = e.getHResult();
        }
        if (an != null && rc == HRESULT.S_OK)
        {
            an.setNotificationType(type);
            an.setEventThrownId(eventInvId);
            initiatePxyOpInv(an, false);
        }

    }

    /**
     * Creates the packet id list combining the supplied packet id with the
     * supplied packet list. The returned list must be deleted by the client.
     */

    private long[] combinePacketIdList(long packetId, long[] packetIdList)
    {
        if (packetIdList != null)
        {
            long[] pId = new long[packetIdList.length + 1];
            pId[0] = packetId;
            for (int i = 0; i < packetIdList.length; i++)
            {
                pId[i + 1] = packetIdList[i];
            }
            return pId;
        }
        else
        {
            long[] pId = new long[1];
            pId[0] = packetId;
            return pId;
        }
    }

    /**
     * Requests the service instance to prepare for being released. The function
     * can e.g. perform port deregistration or can destroy the corresponding
     * association, if applicable. The client shall call this function before
     * Release() is called. The baseclass implementation performs port
     * deregistration in the responder role.
     */
    @Override
    public void prepareRelease()
    {
        super.prepareRelease();
    }

    @Override
    public void packetStarted(long packetId,
                              FSP_TransmissionMode mode,
                              ISLE_Time startTime,
                              long bufferAvailable,
                              boolean notify)
    {

        this.objMutex.lock();
        this.statusInfo.lock();
        this.lastProcessed.lock();

        if (mode == FSP_TransmissionMode.fspTM_sequenceControlled)
        {
            this.statusInfo.incrNumProcessedAD();
        }
        else
        {
            this.statusInfo.incrNumProcessedBD();
        }

        this.lastProcessed.setPacketId(packetId);
        this.lastProcessed.setProductionStartTime(startTime);
        this.lastProcessed.setPacketStatus(FSP_PacketStatus.fspST_productionStarted);

        this.statusInfo.setPacketBufferAvailable(bufferAvailable);

        this.lastProcessed.unlock();
        this.statusInfo.unlock();

        if (notify == true)
        {
            long[] pIdList = combinePacketIdList(packetId, null);
            sendNotification(FSP_NotificationType.fspNT_packetProcessingStarted, pIdList);
        }

        this.objMutex.unlock();

    }

    @Override
    public void packetRadiated(long packetId, FSP_TransmissionMode mode, ISLE_Time radiationTime, boolean notify)
    {

        this.objMutex.lock();

        this.statusInfo.lock();
        this.lastProcessed.lock();
        this.lastOK.lock();

        if (mode == FSP_TransmissionMode.fspTM_sequenceControlled)
        {
            this.statusInfo.incrNumRadiatedAD();
        }
        else
        {
            this.statusInfo.incrNumRadiatedBD();
        }

        if (mode == FSP_TransmissionMode.fspTM_expedited)
        {
            this.lastOK.setPacketId(packetId);
            this.lastOK.setProductionStopTime(radiationTime);
        }

        if (packetId == this.lastProcessed.getPacketId())
        {
            this.lastProcessed.setPacketStatus(FSP_PacketStatus.fspST_radiated);
        }

        this.statusInfo.unlock();
        this.lastProcessed.unlock();
        this.lastOK.unlock();

        this.objMutex.unlock();

        if (notify == true)
        {
            this.objMutex.lock();
            long[] pIdList = combinePacketIdList(packetId, null);
            sendNotification(FSP_NotificationType.fspNT_packetRadiated, pIdList);
            this.objMutex.unlock();
        }

    }

    @Override
    public void packetAcknowledged(long packetId, ISLE_Time ackTime, boolean notify)
    {

        this.objMutex.lock();

        this.statusInfo.lock();
        this.lastProcessed.lock();
        this.lastOK.lock();

        this.statusInfo.incrNumAcknowledged();
        this.lastOK.setPacketId(packetId);
        this.lastOK.setProductionStopTime(ackTime);

        if (packetId == this.lastProcessed.getPacketId())
        {
            this.lastProcessed.setPacketStatus(FSP_PacketStatus.fspST_acknowledged);
        }

        this.statusInfo.unlock();
        this.lastProcessed.unlock();
        this.lastOK.unlock();

        this.objMutex.unlock();

        if (notify == true)
        {
            this.objMutex.lock();
            long[] pIdList = combinePacketIdList(packetId, null);
            sendNotification(FSP_NotificationType.fspNT_packetAcknowledged, pIdList);
            this.objMutex.unlock();
        }
    }

    @Override
    public void bufferEmpty(boolean notify)
    {

        this.objMutex.lock();
        this.statusInfo.lock();
        this.statusInfo.setPacketBufferAvailable(this.config.getMaximumBufferSize());
        this.statusInfo.unlock();
        this.objMutex.unlock();

        if (notify == true)
        {
            this.objMutex.lock();
            sendNotification(FSP_NotificationType.fspNT_bufferEmpty);
            this.objMutex.unlock();
        }
    }

    @Override
    public void packetNotStarted(long packetId,
                                 FSP_TransmissionMode mode,
                                 final ISLE_Time startTime,
                                 FSP_Failure reason,
                                 long bufferAvailable,
                                 boolean notify,
                                 final long[] affectedPackets) throws SleApiException
    {

        HRESULT rc = HRESULT.S_OK;
        this.objMutex.lock();

        FSP_ProductionStatus ps = this.statusInfo.getProductionStatus();
        if (reason == FSP_Failure.fspF_interrupted
            && (ps != FSP_ProductionStatus.fspPS_interrupted && ps != FSP_ProductionStatus.fspPS_halted))
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
        }

        if (reason == FSP_Failure.fspF_modeMismatch && mode == FSP_TransmissionMode.fspTM_expedited)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
        }

        if (getSIState() == SLE_SIState.sleSIS_unbound)
        {
            rc = HRESULT.SLE_E_STATE;
        }
        // updates have to be performed when rc = SLE_E_STATE

        this.statusInfo.lock();
        this.lastProcessed.lock();

        if (mode == FSP_TransmissionMode.fspTM_sequenceControlled)
        {
            this.statusInfo.incrNumProcessedAD();
        }
        else
        {
            this.statusInfo.incrNumProcessedBD();
        }

        this.lastProcessed.setPacketId(packetId);
        this.lastProcessed.setProductionStartTime(startTime);

        FSP_PacketStatus packetStatus = FSP_PacketStatus.fspST_invalid;
        FSP_NotificationType nt = FSP_NotificationType.fspNT_invalid;
        if (reason == FSP_Failure.fspF_expired)
        {
            packetStatus = FSP_PacketStatus.fspST_expired;
            nt = FSP_NotificationType.fspNT_slduExpired;
        }
        else if (reason == FSP_Failure.fspF_interrupted)
        {
            packetStatus = FSP_PacketStatus.fspST_interrupted;
            nt = FSP_NotificationType.fspNT_productionInterrupted;
        }
        else if (reason == FSP_Failure.fspF_modeMismatch)
        {
            packetStatus = FSP_PacketStatus.fspST_unsupportedTransmissionMode;
            nt = FSP_NotificationType.fspNT_packetTransmissionModeMismatch;
        }

        this.lastProcessed.setPacketStatus(packetStatus);
        this.statusInfo.setPacketBufferAvailable(bufferAvailable);

        this.statusInfo.unlock();
        this.lastProcessed.unlock();

        if (notify == true && nt != FSP_NotificationType.fspNT_invalid && rc != HRESULT.SLE_E_STATE)
        {
            long[] pIdList = combinePacketIdList(packetId, affectedPackets);
            sendNotification(nt, pIdList);
        }

        this.objMutex.unlock();
        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }
    }

    @Override
    public void productionStatusChange(FSP_ProductionStatus newStatus,
                                       long[] affectedPackets,
                                       FSP_FopAlert fopAlert,
                                       long bufferAvailable,
                                       boolean notify) throws SleApiException
    {

        HRESULT rc = HRESULT.S_OK;

        this.objMutex.lock();

        FSP_ProductionStatus oldProductionStatus = this.statusInfo.getProductionStatus();

        // ---------------- first perform consistency checks
        // ------------------------
        if (oldProductionStatus == newStatus)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_S_IGNORED);
        }
        // check for inconsistent arguments:
        if (affectedPackets != null)
        {
            // in the following cases the affectedPackets must be 0
            if (newStatus == FSP_ProductionStatus.fspPS_configured
                || newStatus == FSP_ProductionStatus.fspPS_operationalAdAndBd)
            {
                rc = HRESULT.SLE_E_INCONSISTENT;
            }
            if (this.statusInfo.isOperational(oldProductionStatus) == false)
            {
                rc = HRESULT.SLE_E_INCONSISTENT;
            }
            if (oldProductionStatus == FSP_ProductionStatus.fspPS_operationalAdSuspended
                && newStatus == FSP_ProductionStatus.fspPS_operationalBd)
            {
                rc = HRESULT.SLE_E_INCONSISTENT;
            }
            if (rc == HRESULT.SLE_E_INCONSISTENT)
            {
                this.objMutex.unlock();
                throw new SleApiException(rc);
            }
        }
        if ((newStatus == FSP_ProductionStatus.fspPS_interrupted || newStatus == FSP_ProductionStatus.fspPS_halted)
            && this.lastProcessed.getPacketStatus() == FSP_PacketStatus.fspST_productionStarted)
        {
            if (affectedPackets == null)
            {
                this.objMutex.unlock();
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            // check if packet id last processed is in the affected packets list
            if (this.lastProcessed.isContainedIn(affectedPackets) == false)
            {
                this.objMutex.unlock();
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
        }
        if (oldProductionStatus == FSP_ProductionStatus.fspPS_configured
            || oldProductionStatus == FSP_ProductionStatus.fspPS_interrupted)
        {
            if (this.statusInfo.isOperational(newStatus) && newStatus != FSP_ProductionStatus.fspPS_operationalBd)
            {
                this.objMutex.unlock();
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
        }

        if (this.statusInfo.checkTransition(oldProductionStatus, newStatus) != HRESULT.S_OK)
        {
            rc = HRESULT.SLE_E_SEQUENCE;
        }
        // updates have to be performed when rc = SLE_E_SEQUENCE

        if (getSIState() == SLE_SIState.sleSIS_unbound)
        {
            rc = HRESULT.SLE_E_STATE;
        }
        // updates have to be performed when rc = SLE_E_STATE

        // ----------------now set the values ------------------------

        this.statusInfo.lock();
        this.lastProcessed.lock();

        this.statusInfo.setProductionStatus(newStatus);
        this.statusInfo.setPacketBufferAvailable(bufferAvailable);

        if (affectedPackets != null)
        {
            boolean packetIsContained = this.lastProcessed.isContainedIn(affectedPackets);
            if (packetIsContained
                && (newStatus == FSP_ProductionStatus.fspPS_interrupted || newStatus == FSP_ProductionStatus.fspPS_halted))
            {
                this.lastProcessed.setPacketStatus(FSP_PacketStatus.fspST_interrupted);
            }
            else if (packetIsContained && this.statusInfo.isOperational(newStatus))
            {
                this.lastProcessed.setPacketStatus(FSP_PacketStatus.fspST_unsupportedTransmissionMode);
            }
        }

        this.lastProcessed.unlock();
        this.statusInfo.unlock();

        // ----------------now send the notification ------------------------
        if (notify == true && newStatus != oldProductionStatus && newStatus != FSP_ProductionStatus.fspPS_configured
            && rc != HRESULT.SLE_E_STATE)
        {

            long[] pId = null;
            if (affectedPackets != null)
            {

                pId = new long[affectedPackets.length];
                for (int i = 0; i < pId.length; i++)
                {
                    pId[i] = affectedPackets[i];
                }
            }

            if (newStatus == FSP_ProductionStatus.fspPS_halted)
            {
                sendNotification(FSP_NotificationType.fspNT_productionHalted, pId);
                this.lastSentNotification = FSP_NotificationType.fspNT_productionHalted;
            }
            else if (newStatus == FSP_ProductionStatus.fspPS_interrupted && affectedPackets != null)
            {
                sendNotification(FSP_NotificationType.fspNT_productionInterrupted, pId);
                this.lastSentNotification = FSP_NotificationType.fspNT_productionInterrupted;
            }
            else if (oldProductionStatus == FSP_ProductionStatus.fspPS_operationalAdAndBd
                     && newStatus == FSP_ProductionStatus.fspPS_interrupted
                     && (affectedPackets == null || affectedPackets.length == 0))
            {
                sendNotification(FSP_NotificationType.fspNT_transmissionModeCapabilityChange);
            }
            else if (this.statusInfo.isOperational(newStatus)
                     && this.lastSentNotification != FSP_NotificationType.fspNT_productionOperational)
            {
                sendNotification(FSP_NotificationType.fspNT_productionOperational, pId);
                this.lastSentNotification = FSP_NotificationType.fspNT_productionOperational;
            }
            else if ((newStatus == FSP_ProductionStatus.fspPS_operationalBd || newStatus == FSP_ProductionStatus.fspPS_operationalAdSuspended)
                     && oldProductionStatus == FSP_ProductionStatus.fspPS_operationalAdAndBd)
            {

                // We need a copy of the pId list, because the lists will be
                // deleted
                // inside of sendNotification->AsyncNotify->...

                long[] pId2 = null;
                if (affectedPackets != null)
                {
                    pId2 = new long[affectedPackets.length];
                    System.arraycopy(pId, 0, pId2, 0, pId.length);
                }

                sendNotification(FSP_NotificationType.fspNT_transmissionModeCapabilityChange, pId, fopAlert);

                if (affectedPackets != null)
                {
                    sendNotification(FSP_NotificationType.fspNT_packetTransmissionModeMismatch, pId2);
                }

            }
            else if (newStatus == FSP_ProductionStatus.fspPS_operationalAdAndBd
                     && (oldProductionStatus == FSP_ProductionStatus.fspPS_operationalBd || oldProductionStatus == FSP_ProductionStatus.fspPS_operationalAdSuspended))
            {
                sendNotification(FSP_NotificationType.fspNT_transmissionModeCapabilityChange, pId, fopAlert);
            }

        }

        this.objMutex.unlock();
        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }

    }

    @Override
    public void vcAborted(long[] affectedPackets, long bufferAvailable, boolean notify) throws SleApiException
    {

        HRESULT rc = HRESULT.S_OK;
        this.objMutex.lock();

        FSP_ProductionStatus ps = this.statusInfo.getProductionStatus();

        if (this.statusInfo.isOperational(ps) == false)
        {
            this.objMutex.unlock();
            // no updates have to be performed when rc = SLE_E_SEQUENCE
            throw new SleApiException(HRESULT.SLE_E_SEQUENCE);
        }

        if (this.lastProcessed.getPacketStatus() == FSP_PacketStatus.fspST_productionStarted)
        {
            if (bufferAvailable == 0)
            {
                this.objMutex.unlock();
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            // check if packet id last processed is in the affected packets list
            if (this.lastProcessed.isContainedIn(affectedPackets) == false)
            {
                this.objMutex.unlock();
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }

        }

        if (getSIState() == SLE_SIState.sleSIS_unbound)
        {
            rc = HRESULT.SLE_E_STATE;
        }
        // updates have to be performed when rc = SLE_E_STATE

        // ------------------------ now set the parameters:
        // -----------------------------
        this.statusInfo.lock();
        this.lastProcessed.lock();

        this.statusInfo.setProductionStatus(FSP_ProductionStatus.fspPS_operationalBd);
        this.statusInfo.setPacketBufferAvailable(bufferAvailable);

        if (affectedPackets != null)
        {
            boolean packetIsContained = this.lastProcessed.isContainedIn(affectedPackets);
            if (packetIsContained)
            {
                this.lastProcessed.setPacketStatus(FSP_PacketStatus.fspST_interrupted);
            }
        }

        this.lastProcessed.unlock();
        this.statusInfo.unlock();

        // ----------------now send the notification ------------------------
        if (notify == true && rc != HRESULT.SLE_E_STATE)
        {
            long[] pId = null;
            if (affectedPackets != null && affectedPackets.length != 0)
            {

                pId = new long[affectedPackets.length];
                for (int i = 0; i < affectedPackets.length; i++)
                {
                    pId[i] = affectedPackets[i];
                }

            }

            sendNotification(FSP_NotificationType.fspNT_vcAborted, pId);

        }

        this.objMutex.unlock();
        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }

    }

    @Override
    public void noDirectiveCapability(boolean notify) throws SleApiException
    {

        HRESULT rc = HRESULT.S_OK;
        this.objMutex.lock();

        SLE_YesNo dirInvEnabled = this.config.getDirectiveInvocationEnabled();
        if (dirInvEnabled == SLE_YesNo.sleYN_Yes)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_S_IGNORED);
        }

        if (getSIState() == SLE_SIState.sleSIS_unbound)
        {
            rc = HRESULT.SLE_E_STATE;
        }

        this.statusInfo.lock();
        this.statusInfo.setDirectiveInvocationOnline(SLE_YesNo.sleYN_No);
        this.statusInfo.unlock();

        if (notify == true && rc != HRESULT.SLE_E_STATE)
        {
            sendNotification(FSP_NotificationType.fspNT_noInvokeDirectiveCapabilityOnThisVc);
        }

        this.objMutex.unlock();
        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }
    }

    @Override
    public void directiveCapabilityOnline(boolean notify) throws SleApiException
    {

        HRESULT rc = HRESULT.S_OK;
        this.objMutex.lock();

        SLE_YesNo dirInvEnabled = this.config.getDirectiveInvocationEnabled();
        if (dirInvEnabled == SLE_YesNo.sleYN_Yes)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_S_IGNORED);
        }

        if (getSIState() == SLE_SIState.sleSIS_unbound)
        {
            rc = HRESULT.SLE_E_STATE;
        }

        this.statusInfo.lock();
        this.statusInfo.setDirectiveInvocationOnline(SLE_YesNo.sleYN_Yes);
        this.statusInfo.unlock();

        if (notify == true && rc != HRESULT.SLE_E_STATE)
        {
            sendNotification(FSP_NotificationType.fspNT_invokeDirectiveCapabilityOnThisVC);
        }

        this.objMutex.unlock();
        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }
    }

    @Override
    public void directiveCompleted(long directiveId, SLE_Result result, FSP_FopAlert fopAlert, boolean notify) throws SleApiException
    {

        HRESULT rc = HRESULT.S_OK;
        this.objMutex.lock();

        if (getSIState() == SLE_SIState.sleSIS_unbound)
        {
            rc = HRESULT.SLE_E_STATE;
        }

        if (notify == true && rc != HRESULT.SLE_E_STATE)
        {
            if (result == SLE_Result.sleRES_positive)
            {
                sendNotification(FSP_NotificationType.fspNT_positiveConfirmResponseToDirective, directiveId, fopAlert);
            }
            else if (result == SLE_Result.sleRES_negative)
            {
                sendNotification(FSP_NotificationType.fspNT_negativeConfirmResponseToDirective, directiveId, fopAlert);
            }
        }
        this.objMutex.unlock();
        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }

    }

    @Override
    public void eventProcCompleted(long eventId, FSP_EventResult result, boolean notify) throws SleApiException
    {

        HRESULT rc = HRESULT.S_OK;
        this.objMutex.lock();

        if (getSIState() == SLE_SIState.sleSIS_unbound)
        {
            rc = HRESULT.SLE_E_STATE;
        }

        if (notify == true && rc != HRESULT.SLE_E_STATE)
        {
            if (result == FSP_EventResult.fspER_completed)
            {
                sendNotification(FSP_NotificationType.fspNT_actionListCompleted, eventId, result);
            }
            else if (result == FSP_EventResult.fspER_notCompleted)
            {
                sendNotification(FSP_NotificationType.fspNT_actionListNotCompleted, eventId, result);
            }
            else if (result == FSP_EventResult.fspER_conditionFalse)
            {
                sendNotification(FSP_NotificationType.fspNT_eventConditionEvFalse, eventId, result);
            }
        }

        this.objMutex.unlock();
        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }
        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }

    }

}
