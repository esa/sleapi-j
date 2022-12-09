/**
 * @(#) EE_APISE_PSI.java
 */

package esa.sle.impl.api.apise.slese;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_ScheduleStatusReport;
import ccsds.sle.api.isle.iop.ISLE_Unbind;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_DeliveryMode;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_PeerAbortDiagnostic;
import ccsds.sle.api.isle.it.SLE_ReportRequestType;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.it.SLE_SIState;
import ccsds.sle.api.isle.it.SLE_ScheduleStatusReportDiagnostic;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import ccsds.sle.api.isle.it.SLE_UnbindReason;
import esa.sle.impl.api.apise.slese.EE_APISE_PRSI.RestartableTimer;
import esa.sle.impl.api.apise.slese.types.EE_TI_SLESE_Event;
import esa.sle.impl.ifs.gen.EE_LogMsg;
import esa.sle.impl.ifs.time.EE_Duration;
import esa.sle.impl.ifs.time.EE_ElapsedTimer;

/**
 * The Provider Service Instance provides the functionality, which is common to
 * all derived service instances in the provider role. It is responsible to
 * perform access control for BIND operations process BIND (after service
 * instance location) and UNBIND operations perform state processing according
 * to the common provider state table defined in reference [SLE-API] control of
 * the scheduled provision period process SCHEDULE-STATUS-REPORT operations and
 * to start the report-timer for periodic status reports perform status
 * reporting (calls derived class to generate a service-type specific status
 * report) Status Reporting: If a SCHEDULE-STATUS-REPORT operation is received
 * from the proxy, the PSI performs processing according to the
 * report-request-type. For an immediate report it calls doStatusReport(), which
 * is abstract and must be implemented by the service-type specific service
 * instance (most derived class). If the report-request-type is 'periodically',
 * a report-timer is started. If the report-timer expires (function
 * ProcessTimeout()) , the function dotStatusReport() is called. When the
 * report-request-type is 'stop', the report-timer is cancelled. If a
 * GET-PARAMETER invocation is received, the request is forwarded to the
 * function doGetParameter(), which must be implemented by the most derived
 * class. After a successful pre-processing check the client (derived class or
 * self) has to initiate State Processing, which is performed by this class
 * (doStateProcessing()) according to the state-tables specified for the common
 * state table on the provider side. @EndBehaviour
 */
public abstract class EE_APISE_PSI extends EE_APISE_ServiceInstance
{
    private static final Logger LOG = Logger.getLogger(EE_APISE_PSI.class.getName());

    /**
     * The timeout value for the status-report timer (reporting cycle).
     */
    private int reportCycle;

    /**
     * The report request type
     */
    private SLE_ReportRequestType reportRequestType;

    private EE_ElapsedTimer reportTimer;


    /**
     * The protected constructor, to be used for service instance creation,
     * passes the supplied arguments to the base-class.
     */
    protected EE_APISE_PSI(String instanceKey, SLE_ApplicationIdentifier srvType, ISLE_ServiceInform clientIf)
    {
        super(instanceKey, srvType, clientIf, SLE_AppRole.sleAR_provider);
        this.reportCycle = 0;
        this.reportRequestType = SLE_ReportRequestType.sleRRT_invalid;
        this.reportTimer = null;
    }

    /**
     * The protected constructor without arguments.
     */
    protected EE_APISE_PSI(String instanceKey)
    {
    	super(instanceKey);
        this.reportCycle = 0;
        this.reportRequestType = SLE_ReportRequestType.sleRRT_invalid;
        this.reportTimer = null;
    }

    /**
     * If the supplied timer Id is the periodic status report timer, the
     * function invokes a status-report and re-starts the timer. If the timer-id
     * indicates that it is a different timer, the call is passed to the
     * base-class.
     */
    @Override
    protected void doProcessTimeout(Object timer, int invocationId)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("in doProcessTimeout");
        }

        if (!this.reportTimer.equals(timer))
        {
            super.doProcessTimeout(timer, invocationId);
        }
        else
        {
            trace(SLE_TraceLevel.sleTL_medium, "sending periodic status report");
            doStateProcessing(SLE_Component.sleCP_serviceElement, EE_TI_SLESE_Event.eeSLESE_ReportingTimerExpired, null);
            // the timer is re-started in the state-processing function
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("out doProcessTimeout");
        }
    }

    @Override
    protected HRESULT doConfigCompleted()
    {
        return super.doConfigCompleted();
    }

    /**
     * Resets status reporting and issues a logging message saying that periodic
     * status reporting has terminated abnormally.
     */
    public void HandlerAbort(RestartableTimer timer)
    {
        if (!this.reportTimer.equals(timer))
        {
            super.handlerAbort(timer);
        }
        else
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_TimerAborted.getCode());
        }
    }

    /**
     * Creates a new service-type specific status report operation and
     * Initializes it. When all values have been set, it passes the operation to
     * the interface ISLE_SrvProxyInitiate for transmission to the user. This is
     * an abstract method and must be implemented by the most derived class.
     */
    protected abstract HRESULT doStatusReport();

    /**
     * Performs setting of the required parameter to the supplied GetParameter
     * operation.. When the value has been set, it passes the operation to the
     * interface ISLE_SrvProxyInitiate for transmission to the user. The most
     * derived class has to implement the function, as the GetParameter
     * operation is service-type specific.
     */
    protected abstract HRESULT doGetParameter(ISLE_Operation poperation);

    /**
     * Returns the reporting cycle in seconds.
     */
    protected int getReportingCycle()
    {
        return this.reportCycle;
    }

    /**
     * Returns the report-request-type set by the service user in the last
     * SCHEDULE-STATUS-REPORT operation.
     */
    protected SLE_ReportRequestType getReportRequestType()
    {
        return this.reportRequestType;
    }

    /**
     * Resets the reporting cycle, the report-request-type and cancels an active
     * reporting-timer. Implementation: The base-class is called first.
     */
    @Override
    protected void cleanup()
    {
        super.cleanup();

        this.reportCycle = 0;
        this.reportRequestType = SLE_ReportRequestType.sleRRT_invalid;
        cancelReportTimer();
    }

    /**
     * Performs state processing for common operations on the provider side as
     * specified in the state-table. The member-function performs a state change
     * if necessary, and initiates all necessary actions e.g. the invocation of
     * returns, aborting an association, etc. Note that this member-function is
     * only called after a successful pre-processing of the received operation
     * objects. Derived classes have to re-implement this member-function for
     * more specific state processing.
     */
    @Override
    protected HRESULT doStateProcessing(SLE_Component originator, EE_TI_SLESE_Event event, ISLE_Operation poperation)
    {
        traceStateEvent(originator, event, poperation);

        SLE_SIState state = getSIState();
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Current state: " + state + " originator: " + originator + "  event: " + event + " " + poperation);
        }
        ISLE_ConfirmedOperation cop = null;
        if (poperation != null && poperation.isConfirmed())
        {
            cop = (ISLE_ConfirmedOperation) poperation;
        }

        HRESULT rc = HRESULT.S_OK;

        // //////////////////////////////////////
        // Events received from the Application
        // //////////////////////////////////////

        if (originator == SLE_Component.sleCP_application)
        {
            switch (event)
            {
            case eeSLESE_BindRtn:
            {
                switch (state)
                {
                case sleSIS_bindPending:
                {
                    if (cop.getResult() == SLE_Result.sleRES_positive)
                    {
                        stateTransition(SLE_SIState.sleSIS_bound);
                    }
                    else
                    {
                        stateTransition(SLE_SIState.sleSIS_unbound);
                    }
                    rc = initiatePxyOpRtn(cop, false);
                    return rc;
                }
                default:
                {
                    return protocolError(event, originator, state);
                }
                } // switch state
            } // case BindRtn

            case eeSLESE_UnbindRtn:
            {
                switch (state)
                {
                case sleSIS_unbindPending:
                {
                    stateTransition(SLE_SIState.sleSIS_unbound);
                    rc = initiatePxyOpRtn(cop, false);
                    cleanup();

                    ISLE_Unbind ub = poperation.queryInterface(ISLE_Unbind.class);
                    if (ub != null)
                    {
                        // generate endOfPP event
                        if (ub.getUnbindReason() == SLE_UnbindReason.sleUBR_end)
                        {
                            logRecord(SLE_LogMessageType.sleLM_information,
                                      EE_LogMsg.EE_SE_LM_PpEndsOnRequest.getCode());
                            doStateProcessing(SLE_Component.sleCP_serviceElement,
                                              EE_TI_SLESE_Event.eeSLESE_ProvisionPeriodEnds,
                                              null);
                        }
                    }
                    return rc;
                }
                default:
                {
                    return protocolError(event, originator, state);
                }
                }
            } // end UnbindRtn

            case eeSLESE_ThrowEventRtn:
            {
                switch (state)
                {
                case sleSIS_unbindPending:
                {
                    return HRESULT.SLE_E_UNBINDING;
                }

                case sleSIS_bound:
                case sleSIS_startPending:
                case sleSIS_active:
                case sleSIS_stopPending:
                {
                    return initiatePxyOpRtn(cop, false);
                }

                default:
                {
                    return protocolError(event, originator, state);
                }
                }
            } // end ThrowEvent Rtn

            case eeSLESE_AsyncNotifyInv:
            {
                switch (state)
                {
                case sleSIS_unbindPending:
                {
                    return HRESULT.SLE_E_UNBINDING;
                }

                case sleSIS_bound:
                case sleSIS_startPending:
                case sleSIS_active:
                case sleSIS_stopPending:
                {
                    return initiatePxyOpInv(poperation, false);
                }

                default:
                {
                    return protocolError(event, originator, state);
                }
                }
            } // end AsyncNotifyInv

            case eeSLESE_PeerAbortInv:
            {
                switch (state)
                {
                case sleSIS_unbound:
                {
                    return protocolError(event, originator, state);
                }

                default:
                {
                    stateTransition(SLE_SIState.sleSIS_unbound);
                    rc = initiatePxyOpInv(poperation, false);
                    cleanup();
                    return rc;
                }
                }
            } // end PeerAbortInv

            default:
                return HRESULT.EE_E_NOSUCHEVENT;

            } // switch event
        }

        // ///////////////////////////////
        // Events received from the Proxy
        // ///////////////////////////////

        if (originator == SLE_Component.sleCP_proxy)
        {
            switch (event)
            {
            case eeSLESE_BindInv:
            {
                if (state == SLE_SIState.sleSIS_unbound)
                {
                    stateTransition(SLE_SIState.sleSIS_bindPending);
                    rc = informAplOpInv(poperation);
                    return rc;
                }
                else
                {
                    return protocolError(event, originator, state);
                }
            }

            case eeSLESE_UnbindInv:
            {
                switch (state)
                {
                case sleSIS_startPending:
                case sleSIS_active:
                case sleSIS_stopPending:
                {
                    abort(SLE_PeerAbortDiagnostic.slePAD_protocolError);
                    return HRESULT.S_OK;
                }
                case sleSIS_bound:
                {
                    clearRemoteReturns();
                    cancelReportTimer();
                    stateTransition(SLE_SIState.sleSIS_unbindPending);
                    rc = informAplOpInv(poperation);
                    return rc;
                }
                default:
                {
                    // unbound, bindPending and unbindPending
                    return protocolError(event, originator, state);
                }
                }
            } // case eeSLESE_UnbindInv

            case eeSLESE_GetPrmInv:
            {
                switch (state)
                {
                case sleSIS_bound:
                case sleSIS_startPending:
                case sleSIS_active:
                case sleSIS_stopPending:
                {
                    return doGetParameter(poperation);
                }

                default:
                {
                    return protocolError(event, originator, state);
                }
                }
            } // end GetPrmInv

            case eeSLESE_SsrInv:
            {
                switch (state)
                {
                case sleSIS_bound:
                case sleSIS_startPending:
                case sleSIS_active:
                case sleSIS_stopPending:
                {
                    return processSSR(poperation);
                }

                default:
                {
                    return protocolError(event, originator, state);
                }
                }
            } // end SsrInv

            case eeSLESE_ThrowEventInv:
            {
                switch (state)
                {
                case sleSIS_bound:
                case sleSIS_startPending:
                case sleSIS_active:
                case sleSIS_stopPending:
                {
                    return informAplOpInv(poperation);
                }

                default:
                {
                    return protocolError(event, originator, state);
                }
                }

            } // end ThrowEventInv

            case eeSLESE_PeerAbortInv:
            {
                if (state == SLE_SIState.sleSIS_unbound)
                {
                    return protocolError(event, originator, state);
                }
                else
                {
                    stateTransition(SLE_SIState.sleSIS_unbound);
                    rc = informAplOpInv(poperation);
                    cleanup();
                    return HRESULT.S_OK;
                }
            } // end PeerAbortInv

            case eeSLESE_ProtocolAbort:
            {
                if (state == SLE_SIState.sleSIS_unbound)
                {
                    return protocolError(event, originator, state);
                }
                else
                {
                    stateTransition(SLE_SIState.sleSIS_unbound);
                    // forwarding the protocol abort to the application
                    // must be done by the caller in this special case.
                    return HRESULT.S_OK;
                }
            } // end ProtocolAbort

            default:
            {
                return HRESULT.EE_E_NOSUCHEVENT;
            }

            } // switch (event)
        } // end if originator is proxy

        // /////////////////////////////
        // Internal Events
        // /////////////////////////////

        if (originator == SLE_Component.sleCP_serviceElement) // internal event
        {
            switch (event)
            {
            case eeSLESE_ReportingTimerExpired:
            {
                if (state == SLE_SIState.sleSIS_bound || state == SLE_SIState.sleSIS_startPending
                    || state == SLE_SIState.sleSIS_active || state == SLE_SIState.sleSIS_stopPending)
                {
                    doStatusReport();
                    EE_Duration dur = new EE_Duration(this.reportCycle);
                    if (this.reportTimer != null)
                    {
                        try
                        {
                            this.reportTimer.restart(dur, 0);
                        }
                        catch (SleApiException e)
                        {
                            if (LOG.isLoggable(Level.FINE))
                            {
                                LOG.fine("restart throes a " + e.getHResult());
                            }
                        }
                    }
                    return HRESULT.S_OK;
                }
                return HRESULT.S_OK; // ignore, as it is N/A

            } // end ReportingTimerExpired

            case eeSLESE_ReturnTimeout:
            {
                if (state == SLE_SIState.sleSIS_bound || state == SLE_SIState.sleSIS_startPending
                    || state == SLE_SIState.sleSIS_active || state == SLE_SIState.sleSIS_stopPending)
                {
                    abort(SLE_PeerAbortDiagnostic.slePAD_returnTimeout);
                    return HRESULT.S_OK;
                }
                return HRESULT.S_OK; // ignore, as it is N/A

            } // end ReportingTimerExpired

            case eeSLESE_PeerAbortInv:
            {
                if (state != SLE_SIState.sleSIS_unbound)
                {
                    stateTransition(SLE_SIState.sleSIS_unbound);
                    rc = initiatePxyOpInv(poperation, false);
                    rc = informAplOpInv(poperation);
                    cleanup();
                }
                return HRESULT.S_OK; // for other states ignore, as N/A

            } // end PeerAbortInv

            case eeSLESE_ProvisionPeriodEnds:
            {
                if (state != SLE_SIState.sleSIS_unbound)
                {
                    abort(SLE_PeerAbortDiagnostic.slePAD_endOfServiceProvisionPeriod);
                }
                informAplPpEnds();
                setEndOfProvisionPeriod();
                return HRESULT.S_OK;
            }

            default:
            {
                return HRESULT.EE_E_NOSUCHEVENT;
            }
            } // end switch (event)
        } // end originator == sleCP_serviceElement

        return HRESULT.EE_E_NOSUCHEVENT;
    }

    /**
     * Performs the processing of a SCHEDULE-STATUS-REPORT invocation including
     * the call to doStatusReport() if required (for immediate and for starting
     * a periodic report).
     *
     * @param poperation
     * @return
     */
    private HRESULT processSSR(ISLE_Operation poperation)
    {
        ISLE_ScheduleStatusReport ssr = (ISLE_ScheduleStatusReport) poperation;

        SLE_ReportRequestType rrt = ssr.getReportRequestType();

        if (rrt == SLE_ReportRequestType.sleRRT_stop)
        {
            if (this.reportTimer != null)
            {
                cancelReportTimer();
                this.reportRequestType = SLE_ReportRequestType.sleRRT_stop;
                this.reportCycle = 0;
                ssr.setPositiveResult();
            }
            else
            {
                ssr.setSSRDiagnostic(SLE_ScheduleStatusReportDiagnostic.sleSSD_alreadyStopped);
            }

            return initiatePxyOpRtn(ssr, false);
        }

        cancelReportTimer();

        this.reportRequestType = rrt;
        ssr.setPositiveResult();
        initiatePxyOpRtn(ssr, false);

        doStatusReport(); // calls most derived class

        if (rrt == SLE_ReportRequestType.sleRRT_periodically)
        {
            this.reportCycle = ssr.getReportingCycle();
            this.reportTimer = new EE_ElapsedTimer();
            EE_Duration tmo = new EE_Duration(this.reportCycle);
            try
            {
                this.reportTimer.start(tmo, this, 0);
            }
            catch (SleApiException e)
            {
                LOG.log(Level.FINE, "SleApiException ", e);
            }
        }
        return HRESULT.S_OK;
    }

    /**
     * Cancels the report-timer if it is active.
     */
    private void cancelReportTimer()
    {
        if (this.reportTimer != null)
        {
            this.reportTimer.cancel();
            this.reportTimer = null;
        }
    }

    /**
     * Performs all checks on the SCHEDULE-STATUS-REPORT operation supplied by
     * the Proxy. The function checks the cycle period against the value in the
     * database. If the request indicates to stop status-reporting, it is
     * checked whether status-reporting is active at all. When the checks are
     * completed successfully, state-processing is initiated.
     */
    @Override
    protected HRESULT scheduleStatusReportInv(ISLE_Operation poperation)
    {
        ISLE_ScheduleStatusReport ssr = (ISLE_ScheduleStatusReport) poperation;
        SLE_DeliveryMode deliveryMode = getConfiguration().getDeliveryMode();

        if (deliveryMode == SLE_DeliveryMode.sleDM_rtnOffline || deliveryMode == SLE_DeliveryMode.sleDM_fwdOffline)
        {
            ssr.setSSRDiagnostic(SLE_ScheduleStatusReportDiagnostic.sleSSD_notSupportedInThisDeliveryMode);
            initiatePxyOpRtn(ssr, false);
            return HRESULT.S_OK;
        }

        int cycle = ssr.getReportingCycle();

        EE_APISE_Database db = EE_APISE_Database.getDb(super.instanceId);
        int minRepCycleDB = db.getMinReportingCycle(); // defined in SE Database
        int maxRepCycleDB = db.getMaxReportingCycle(); // defined in SE Database
        long minRepCycleSC = getConfiguration().getMinimumReportingCycle(); // defined in SI Configuration since SLES V5 (default is 1).

        SLE_ReportRequestType rt = ssr.getReportRequestType();
        if (rt == SLE_ReportRequestType.sleRRT_periodically)
        {
            if (!(cycle >= minRepCycleDB && cycle <= maxRepCycleDB && cycle >= minRepCycleSC))
            {
                ssr.setSSRDiagnostic(SLE_ScheduleStatusReportDiagnostic.sleSSD_invalidReportingCycle);
                initiatePxyOpRtn(ssr, false);
                return HRESULT.S_OK;
            }
        }

        // pre-processing check passed. now to state machine
        String ssrType = "Immediate";
        if (rt == SLE_ReportRequestType.sleRRT_periodically)
        {
            ssrType = "Periodical";
        }
        else if (rt == SLE_ReportRequestType.sleRRT_stop)
        {
            ssrType = "Stop";
        }

        logRecord(SLE_LogMessageType.sleLM_information, EE_LogMsg.EE_SE_LM_SSRRequested.getCode(), ssrType);

        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_SsrInv, poperation);
    }

    /**
     * Implements EE_APISE_ServiceInstance. A SCHEDULE-STATUS-REPORT return is
     * not allowed for a provider service instance, therefore it returns E_ROLE.
     */
    @Override
    protected HRESULT scheduleStatusReportRtn(ISLE_ConfirmedOperation poperation)
    {
        return HRESULT.SLE_E_ROLE;
    }

    /**
     * Processes the arrived BIND invocation from the proxy. It is assumed that
     * all checks have already been performed by service instance location. The
     * function also invokes state-processing
     */
    @Override
    protected HRESULT bindInv(ISLE_Operation poperation)
    {
        if (((ISLE_Bind) poperation).getServiceType() != getServiceType())
        {
            return HRESULT.SLE_E_INVALIDPDU;
        }
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_BindInv, poperation);
    }

    /**
     * Processes the BIND-return received from the application and invokes
     * state-processing.
     */
    @Override
    protected HRESULT bindRtn(ISLE_ConfirmedOperation poperation)
    {
        // The provider SI has to set the version number from the
        // BIND invocation
        if (poperation.getResult() == SLE_Result.sleRES_positive)
        {
            int version = poperation.getOpVersionNumber();
            setVersion(version);
        }
        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_BindRtn, poperation);
    }

    /**
     * Processes the UNBIND received from the proxy. The function also invokes
     * state -processing, which also clears all remote returns and cancels an
     * active report-timer
     */
    @Override
    protected HRESULT unbindInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_UnbindInv, poperation);
    }

    /**
     * Processes the UNBIND-return received from the application. The function
     * also invokes state - processing, which performs an internal cleanup and
     * generates an 'end of provision period' report if the unbind-reason
     * indicates 'end of provision period'.
     */
    @Override
    protected HRESULT unbindRtn(ISLE_ConfirmedOperation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_UnbindRtn, poperation);
    }

    /**
     * Returns a pointer to the configuration object. This protected pure
     * virtual function must be implemented by the most derived class.
     */
    protected abstract EE_APISE_PConfiguration getConfiguration();
}
