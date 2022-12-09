/**
 * @(#) EE_APISE_PFSI.java
 */

package esa.sle.impl.api.apise.slese;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_PeerAbortDiagnostic;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.it.SLE_SIState;
import esa.sle.impl.api.apise.slese.types.EE_TI_SLESE_Event;

/**
 * This class provides the functionality that is specific to forward service
 * instances for provider applications. It performs state processing as defined
 * in reference [SLE-API] for forward service instances in the provider role
 */
public abstract class EE_APISE_PFSI extends EE_APISE_PSI
{
    private static final Logger LOG = Logger.getLogger(EE_APISE_PFSI.class.getName());


    /**
     * Constructor with no arguments.
     */
    protected EE_APISE_PFSI(String instanceKey)
    {
    	super(instanceKey);
    }

    /**
     * The protected constructor, to be used for service instance creation,
     * passes the supplied arguments to the base-class.
     */
    protected EE_APISE_PFSI(String instanceKey, SLE_ApplicationIdentifier srvType, ISLE_ServiceInform clientIf)
    {
        super(instanceKey, srvType, clientIf);
    }

    /**
     * Performs state processing for operations on the provider side for forward
     * services as specified in the state-table. The member-function performs a
     * state change if necessary, and initiates all necessary actions e.g. the
     * invocation of returns, aborting an association, etc. Note that this
     * member-function is only called after a successful pre-processing of the
     * received operation objects.
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
                    rc = initiatePxyOpRtn(cop, false);
                    return rc;
                }
                else
                {
                    return protocolError(event, originator, state);
                }
            } // StartRtn

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
                    rc = initiatePxyOpRtn(cop, false);
                    return rc;
                }
                else
                {
                    return protocolError(event, originator, state);
                }
            } // StopRtn

            case eeSLESE_TransferDataRtn:
            case eeSLESE_InvokeDirectiveRtn:
            {
                if (state == SLE_SIState.sleSIS_active || state == SLE_SIState.sleSIS_stopPending)
                {
                    rc = initiatePxyOpRtn(cop, false);
                    return rc;
                }
                else
                {
                    return protocolError(event, originator, state);
                }
            } // TransferDataRtn

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
            case eeSLESE_StartInv:
            {
                if (state == SLE_SIState.sleSIS_bound)
                {
                    stateTransition(SLE_SIState.sleSIS_startPending);
                    rc = informAplOpInv(poperation);
                    return rc;
                }
                else if (state == SLE_SIState.sleSIS_startPending || state == SLE_SIState.sleSIS_active
                         || state == SLE_SIState.sleSIS_stopPending)
                {
                    abort(SLE_PeerAbortDiagnostic.slePAD_protocolError);
                    return HRESULT.S_OK;
                }
                else
                {
                    return protocolError(event, originator, state);
                }
            } // StartInv

            case eeSLESE_StopInv:
            {
                if (state == SLE_SIState.sleSIS_active)
                {
                    stateTransition(SLE_SIState.sleSIS_stopPending);
                    rc = informAplOpInv(poperation);
                    return rc;
                }
                else if (state == SLE_SIState.sleSIS_bound || state == SLE_SIState.sleSIS_startPending
                         || state == SLE_SIState.sleSIS_stopPending)
                {
                    abort(SLE_PeerAbortDiagnostic.slePAD_protocolError);
                    return HRESULT.S_OK;
                }
                else
                {
                    return protocolError(event, originator, state);
                }
            } // StopInv

            case eeSLESE_TransferDataInv:
            case eeSLESE_InvokeDirectiveInv:
            {
                if (state == SLE_SIState.sleSIS_active)
                {
                    return informAplOpInv(poperation);
                }
                else if (state == SLE_SIState.sleSIS_bound || state == SLE_SIState.sleSIS_startPending
                         || state == SLE_SIState.sleSIS_stopPending)
                {
                    abort(SLE_PeerAbortDiagnostic.slePAD_protocolError);
                    return HRESULT.S_OK;
                }
                else
                {
                    return protocolError(event, originator, state);
                }
            } // TransferDataInv

            default:
            {
                return HRESULT.EE_E_NOSUCHEVENT;
            }
            } // end switch (event)
        } // end originator == sleCP_proxy
          // no internal event

        return HRESULT.EE_E_NOSUCHEVENT;
    }
}
