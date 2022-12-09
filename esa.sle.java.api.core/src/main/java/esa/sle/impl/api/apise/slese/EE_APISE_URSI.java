/**
 * @(#) EE_APISE_URSI.java
 */

package esa.sle.impl.api.apise.slese;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_TransferBuffer;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_PeerAbortDiagnostic;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.it.SLE_SIState;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import esa.sle.impl.api.apise.slese.types.EE_TI_SLESE_Event;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * This class provides common functionality for return service instances in the
 * user role. It is responsible to accept TRANSFER-BUFFER operations from the
 * association read the TRANSFER-DATA and SYNC-NOTIFY operations from the
 * concatenation buffer and to deliver them to the application perform state
 * processing as defined in [SLE-API] for user service instances for the return
 * link services If state-processing indicates that a TRANSFER-BUFFER invocation
 * has to be read, the class starts reading the buffer and transfers all
 * TRANSFER-DATA and SYNC-NOTIFY invocations to the application. If the
 * extracted operation is not of the correct type the association is aborted.
 */
public abstract class EE_APISE_URSI extends EE_APISE_USI
{
    private static final Logger LOG = Logger.getLogger(EE_APISE_URSI.class.getName());


    /**
     * The protected constructor with no arguments.
     */
    protected EE_APISE_URSI(String instanceKey)
    {
    	super(instanceKey);
    }

    /**
     * The protected constructor, to be used for service instance creation,
     * passes the supplied arguments to the base-class.
     */
    protected EE_APISE_URSI(String instanceKey, SLE_ApplicationIdentifier srvType, ISLE_ServiceInform clientIf)
    {
        super(instanceKey, srvType, clientIf);
    }

    /**
     * Performs state processing for operations on the user side for return
     * services as specified in the state-table. The member-function performs a
     * state change if necessary, and initiates all necessary actions e.g. the
     * invocation of returns, aborting an association, etc. Note that this
     * member-function is only called after a successful pre-processing of the
     * received operation objects. Derived classes have to re-implement this
     * member-function for more specific state processing.
     */
    @Override
    protected HRESULT doStateProcessing(SLE_Component originator, EE_TI_SLESE_Event event, ISLE_Operation poperation)
    {
        HRESULT rc = super.doStateProcessing(originator, event, poperation);
        if (rc != HRESULT.EE_E_NOSUCHEVENT)
        {
            return rc;
        }

        rc = HRESULT.S_OK;

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

        // //////////////////////////////////////
        // Events received from the Application
        // //////////////////////////////////////

        if (originator == SLE_Component.sleCP_application)
        {
            switch (event)
            {
            case eeSLESE_StartInv:
            {
                if (state == SLE_SIState.sleSIS_bound)
                {
                    stateTransition(SLE_SIState.sleSIS_startPending);
                    rc = initiatePxyOpInv(poperation, false);
                    return rc;
                }
                return protocolError(event, originator, state);

            } // case StartInv

            case eeSLESE_StopInv:
            {
                if (state == SLE_SIState.sleSIS_active)
                {
                    stateTransition(SLE_SIState.sleSIS_stopPending);
                    rc = initiatePxyOpInv(poperation, false);
                    return rc;
                }
                return protocolError(event, originator, state);

            } // case StopInv

            default:
            {
                return HRESULT.EE_E_NOSUCHEVENT;
            }

            } // end switch (event)

        } // end (originator == sleCP_application)

        // ///////////////////////////////
        // Events received from the Proxy
        // ///////////////////////////////

        if (originator == SLE_Component.sleCP_proxy)
        {

            switch (event)
            {
            case eeSLESE_StartRtn:
            {
                if (state == SLE_SIState.sleSIS_startPending)
                {
                    if (cop.getResult() == SLE_Result.sleRES_positive)
                    {
                        stateTransition(SLE_SIState.sleSIS_active);
                    }
                    else
                    {
                        stateTransition(SLE_SIState.sleSIS_bound);
                    }
                    rc = informAplOpRtn(cop);
                    return rc;
                }
                else if (state == SLE_SIState.sleSIS_unbound || state == SLE_SIState.sleSIS_bindPending)
                {
                    return protocolError(event, originator, state);
                }
                else
                {
                    abort(SLE_PeerAbortDiagnostic.slePAD_protocolError);
                    return HRESULT.S_OK;
                }
            } // case StartRtn

            case eeSLESE_StopRtn:
            {
                if (state == SLE_SIState.sleSIS_stopPending)
                {
                    if (cop.getResult() == SLE_Result.sleRES_positive)
                    {
                        stateTransition(SLE_SIState.sleSIS_bound);
                    }
                    else
                    {
                        stateTransition(SLE_SIState.sleSIS_active);
                    }
                    rc = informAplOpRtn(cop);
                    return rc;
                }
                else if (state == SLE_SIState.sleSIS_unbound || state == SLE_SIState.sleSIS_bindPending)
                {
                    return protocolError(event, originator, state);
                }
                else
                {
                    abort(SLE_PeerAbortDiagnostic.slePAD_protocolError);
                    return HRESULT.S_OK;
                }
            } // case StopRtn

            case eeSLESE_TransferBufferInv:
            {
                if (state == SLE_SIState.sleSIS_active || state == SLE_SIState.sleSIS_stopPending)
                {
                    return processBuffer(poperation);
                }
                else if (state == SLE_SIState.sleSIS_unbound || state == SLE_SIState.sleSIS_bindPending)
                {
                    return protocolError(event, originator, state);
                }
                else
                {
                    abort(SLE_PeerAbortDiagnostic.slePAD_protocolError);
                    return HRESULT.S_OK;
                }
            } // case TransferBufferInv

            default:
            {
                return HRESULT.EE_E_NOSUCHEVENT;
            }

            } // end switch (event)

        } // end (originator == sleCP_proxy)

        // ///////////////////////////////
        // No internal event for a URSI
        // ///////////////////////////////

        return HRESULT.EE_E_NOSUCHEVENT;
    }

    /**
     * Reads all contained operations from the supplied TRANSFER-BUFFER
     * operation and passes them to the application. If a contained operation is
     * neither a TRANSFER-DATA nor a SYNC-NOTIFY operation, the association is
     * aborted.
     * 
     * @param poperation
     * @return
     */
    private HRESULT processBuffer(ISLE_Operation poperation)
    {
        ISLE_TransferBuffer tb = (ISLE_TransferBuffer) poperation;

        if (tb.empty())
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_EmptyBufferRec.getCode());
            return HRESULT.S_OK;
        }

        while (!tb.empty() && getSIState().getCode() >= SLE_SIState.sleSIS_active.getCode())
        {
            ISLE_Operation op = tb.removeFront();
            // SLEAPIJ-14
            if(this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_medium.getCode()) {
            	traceOperation(op, " operation extracted from TRANSFER-BUFFER");
            }
            SLE_OpType ot = op.getOperationType();
            if (ot != SLE_OpType.sleOT_transferData && ot != SLE_OpType.sleOT_syncNotify)
            {
                String otS = ot.toString();
                logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_UnexpectedTbPdu.getCode(), otS);
                abort(SLE_PeerAbortDiagnostic.slePAD_protocolError);
                return HRESULT.S_OK;
            }

            informAplOpInv(op);
            //Thread.yield();
        }

        return HRESULT.S_OK;
    }

    /**
     * Performs all checks on the TRANSFER-BUFFER operation supplied by the
     * proxy. When the checks are completed successfully, state-processing is
     * initiated, which extracts the operation objects from the buffer and
     * forwards them to the application.
     * 
     * @param poperation
     * @return
     */
    protected HRESULT transferBufferInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_TransferBufferInv, poperation);
    }
}
