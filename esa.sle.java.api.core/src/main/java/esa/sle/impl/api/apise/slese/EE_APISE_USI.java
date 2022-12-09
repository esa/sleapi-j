/**
 * @(#) EE_APISE_USI.java
 */

package esa.sle.impl.api.apise.slese;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_PeerAbortDiagnostic;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.it.SLE_SIState;
import esa.sle.impl.api.apise.slese.types.EE_TI_SLESE_Event;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * The User Service Instance provides common functionality for derived service
 * instance classes in the user role. The class is responsible to initiate BIND
 * and UNBIND operations on request by the application create/release
 * association objects via the interface ISLE_AssocFactory perform state
 * processing according to reference [SLE-API] for user service instances in the
 * bind initiator role
 */
public abstract class EE_APISE_USI extends EE_APISE_ServiceInstance
{
    private static final Logger LOG = Logger.getLogger(EE_APISE_USI.class.getName());

    /**
     * The information whether or not an association object has been created
     */
    private boolean assocCreated;

    /**
     * The version number from the Bind Invocation.
     */
    private int bindInvVersion;


    /**
     * The protected constructor without arguments.
     */
    protected EE_APISE_USI(String instanceKey)
    {
    	super(instanceKey);
        this.assocCreated = false;
        this.bindInvVersion = 0;
    }

    /**
     * The protected constructor, to be used for service instance creation,
     * passes the supplied arguments to the base-class.
     */
    protected EE_APISE_USI(String instanceKey, SLE_ApplicationIdentifier srvType, ISLE_ServiceInform clientIf)
    {
        super(instanceKey, srvType, clientIf, SLE_AppRole.sleAR_user);
        this.assocCreated = false;
        this.bindInvVersion = 0;
    }

    /**
     * Performs state processing for common operations on the user side as
     * specified in the state-table. The member-function performs a state change
     * if necessary, and initiates all necessary actions e.g. the invocation of
     * returns, aborting an association, etc. Note that this member-function
     * shall only be called after a successful pre-processing of the received
     * operation objects. Derived classes have to re-implement this
     * member-function for more specific state processing.
     */
    @Override
    protected HRESULT doStateProcessing(SLE_Component originator, EE_TI_SLESE_Event event, ISLE_Operation poperation)
    {
        traceStateEvent(originator, event, poperation);

        SLE_SIState state = getSIState();
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Current state: " + state);
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
            case eeSLESE_BindInv:
            {
                if (state == SLE_SIState.sleSIS_unbound)
                {
                    stateTransition(SLE_SIState.sleSIS_bindPending);
                    rc = initiatePxyOpInv(poperation, false);
                    return rc;
                }
                return protocolError(event, originator, state);
            } // case BindInv
            case eeSLESE_UnbindInv:
            {
                if (state == SLE_SIState.sleSIS_bound)
                {
                    stateTransition(SLE_SIState.sleSIS_unbindPending);
                    rc = initiatePxyOpInv(poperation, false);
                    clearLocalReturns();
                    return rc;
                }
                return protocolError(event, originator, state);
            } // case UnbindInv
            case eeSLESE_GetPrmInv:
            case eeSLESE_ThrowEventInv:
            case eeSLESE_SsrInv:
            {
                if (state == SLE_SIState.sleSIS_bound || state == SLE_SIState.sleSIS_startPending
                    || state == SLE_SIState.sleSIS_active || state == SLE_SIState.sleSIS_stopPending)
                {
                    return initiatePxyOpInv(poperation, false);
                }
                return protocolError(event, originator, state);
            } // case GetPrmInv, ThrowEventInv, SsrInv
            case eeSLESE_PeerAbortInv:
            {
                if (state == SLE_SIState.sleSIS_unbound)
                {
                    return protocolError(event, originator, state);
                }
                stateTransition(SLE_SIState.sleSIS_unbound);
                rc = initiatePxyOpInv(poperation, false);
                cleanup();
                return rc;
            } // end PeerAbortInv
            default:
            {
                return HRESULT.EE_E_NOSUCHEVENT;
            }
            } // end switch(event)
        } // end originator == sleCP_application

        // ///////////////////////////////
        // Events received from the Proxy
        // ///////////////////////////////

        if (originator == SLE_Component.sleCP_proxy)
        {
            switch (event)
            {
            case eeSLESE_BindRtn:
            {
                if (state == SLE_SIState.sleSIS_bindPending)
                {
                    if (cop.getResult() == SLE_Result.sleRES_positive)
                    {
                        stateTransition(SLE_SIState.sleSIS_bound);
                    }
                    else
                    {
                        stateTransition(SLE_SIState.sleSIS_unbound);
                    }
                    rc = informAplOpRtn(cop);
                    return rc;
                }
                return protocolError(event, originator, state);
            } // case BindRtn
            case eeSLESE_UnbindRtn:
            {
                if (state == SLE_SIState.sleSIS_unbindPending)
                {
                    if (cop.getResult() == SLE_Result.sleRES_positive)
                    {
                        stateTransition(SLE_SIState.sleSIS_unbound);
                    }
                    rc = informAplOpRtn(cop);
                    cleanup();
                    return rc;
                }
                return protocolError(event, originator, state);
            } // case UnindRtn
            case eeSLESE_GetPrmRtn:
            case eeSLESE_SsrRtn:
            case eeSLESE_ThrowEventRtn:
            {
                if (state == SLE_SIState.sleSIS_unbound || state == SLE_SIState.sleSIS_bindPending)
                {
                    return protocolError(event, originator, state);
                }
                return informAplOpRtn(cop);
            } // case GetPrmRtn, ThrowEventRtn, SsrRtn
            case eeSLESE_AsyncNotifyInv:
            case eeSLESE_StatusReportInv:
            {
                if (state == SLE_SIState.sleSIS_unbound || state == SLE_SIState.sleSIS_bindPending)
                {
                    return protocolError(event, originator, state);
                }
                return informAplOpInv(poperation);
            } // case AsyncNotifyInv
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
            } // end protocolAbort
            default:
            {
                return HRESULT.EE_E_NOSUCHEVENT;
            }
            } // end switch (event)
        } // end (originator == sleCp_proxy)

        // /////////////////////////////
        // Internal Events
        // /////////////////////////////

        if (originator == SLE_Component.sleCP_serviceElement) // internal event
        {
            switch (event)
            {
            case eeSLESE_ReturnTimeout:
            {
                if (state != SLE_SIState.sleSIS_unbound)
                {
                    abort(SLE_PeerAbortDiagnostic.slePAD_returnTimeout);
                    return HRESULT.S_OK;
                }
                return HRESULT.S_OK; // ignore, as it is a N/A
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
            default:
            {
                return HRESULT.EE_E_NOSUCHEVENT;
            }
            } // end switch (event)
        } // end (originator == sleCP_serviceElement)

        return HRESULT.EE_E_NOSUCHEVENT;
    }

    /**
     * Checks BIND invocation received from the application and creates a new
     * association object via the interface ISLE_AssocFactory. If that succeeds,
     * it forwards the BIND invocation to the state-processing routine.
     */
    @Override
    protected HRESULT bindInv(ISLE_Operation poperation)
    {
        ISLE_Bind ibind = (ISLE_Bind) poperation;

        if (ibind.getServiceType() != getServiceType())
        {
            return HRESULT.SLE_E_INVALIDPDU;
        }

        if (!this.assocCreated)
        {
            HRESULT rc = createAssoc();
            if (rc != HRESULT.S_OK)
            {
                return rc;
            }
            this.assocCreated = true;
        }

        this.bindInvVersion = ibind.getVersionNumber();
        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_BindInv, poperation);
    }

    /**
     * Processes the BIND-return received from the proxy and invokes
     * state-processing.
     */
    @Override
    protected HRESULT bindRtn(ISLE_ConfirmedOperation poperation)
    {
        ISLE_Bind ibind = (ISLE_Bind) poperation;
        int bindRtnVersion = ibind.getVersionNumber();

        // A different version in the Bind Return would indicate
        // version negotiation, which is not supported by this API.
        // It was agreed to issue a peer-abort(reason other)
        // instead of an UNBIND(version not supported). The specification
        // might change later in the CCSDS book.
        if (bindRtnVersion != this.bindInvVersion)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_BindVersionMismatch.getCode());
            abort(SLE_PeerAbortDiagnostic.slePAD_otherReason);
            return HRESULT.S_OK;
        }

        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_BindRtn, poperation);
    }

    /**
     * Processes the UNBIND invocation received from the application. The
     * function also invokes state -processing, which in turn clears all local
     * returns.
     */
    @Override
    protected HRESULT unbindInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_UnbindInv, poperation);
    }

    /**
     * Processes the UNBIND-return received from the proxy. The function also
     * invokes state - processing, which performs an internal cleanup.
     */
    @Override
    protected HRESULT unbindRtn(ISLE_ConfirmedOperation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_UnbindRtn, poperation);
    }

    /**
     * Performs all checks on the SCHEDULE-STATUS-REPORT operation supplied by
     * the application. When the checks are completed successfully,
     * state-processing is initiated.
     */
    @Override
    protected HRESULT scheduleStatusReportInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_SsrInv, poperation);
    }

    /**
     * Performs all checks on the SCHEDULE-STATUS-REPORT return PDU supplied by
     * the proxy. When the checks are completed successfully, state-processing
     * is initiated.
     */
    @Override
    protected HRESULT scheduleStatusReportRtn(ISLE_ConfirmedOperation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_SsrRtn, poperation);
    }

    /**
     * Requests the service instance to prepare for being released. The USI
     * implementation destroys the association.
     */
    @Override
    public void prepareRelease()
    {
        if (this.assocCreated)
        {
            releaseAssoc();
        }

        super.prepareRelease();
    }
}
