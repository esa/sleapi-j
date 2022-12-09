/**
 * @(#) EE_APISE_UFSI.java
 */

package esa.sle.impl.api.apise.slese;

import java.util.concurrent.locks.ReentrantLock;
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
 * instances for user applications. It performs state processing defined in
 * reference [SLE-API] for user forward service instances flow control
 * processing for TRANSFER-DATA operations as specified in reference [SLE-API],
 * which includes requesting the application to suspend data transfer (if
 * necessary) informing the application when data transfer can be resumed (the
 * interface is obtained from the service instance base-class) Flow-control
 * processing is performed in the function doStateProcessing(), which on the one
 * hand requests the application to suspend the data transfer when the proxy has
 * queued the data. On the other hand it informs the application when data
 * transfer can be resumed (proxy-event: PDU transmitted).
 */
public abstract class EE_APISE_UFSI extends EE_APISE_USI
{
    private static final Logger LOG = Logger.getLogger(EE_APISE_UFSI.class.getName());

    /**
     * Holds the information whether or not the data transfer is suspended.
     */
    private boolean suspended;

    /**
     * The PDU for which the service instance waits until the transmission
     * notification is received from the proxy.
     */
    private ISLE_Operation waitNotifyPDU;

    /**
     * The mutex protects the suspension of data transfer against multiple
     * thread access.
     */
    private final ReentrantLock tdMutex = new ReentrantLock();


    /**
     * The constructor with no arguments.
     */
    protected EE_APISE_UFSI(String instanceKey)
    {
    	super(instanceKey);
        this.suspended = false;
        this.waitNotifyPDU = null;
    }

    /**
     * The protected constructor, to be used for service instance creation,
     * passes the supplied arguments to the base-class.
     */
    protected EE_APISE_UFSI(String instanceKey, SLE_ApplicationIdentifier srvType, ISLE_ServiceInform clientIf)
    {
        super(instanceKey, srvType, clientIf);
        this.suspended = false;
        this.waitNotifyPDU = null;
    }

    /**
     * Resets data-transfer-suspended to false and resets all other internally
     * used data members. Implementation: The base-class is called first.
     */
    @Override
    protected void cleanup()
    {
        super.cleanup();
        this.suspended = false;
        if (this.waitNotifyPDU != null)
        {
            this.waitNotifyPDU = null;
        }
    }

    /**
     * Performs state processing for operations on the user side for forward
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
                    if (rc.getCode() >= 0)
                    {
                        this.suspended = false;
                    }
                    return rc;
                }
                return protocolError(event, originator, state);

            } // case StopInv

            case eeSLESE_TransferDataInv:
            {
                if (state == SLE_SIState.sleSIS_active)
                {
                    return processTransferData(poperation);
                }
                else
                {
                    return protocolError(event, originator, state);
                }

            } // case TransferDataInv

            case eeSLESE_InvokeDirectiveInv:
            {
                if (state == SLE_SIState.sleSIS_active)
                {
                    return initiatePxyOpInv(poperation, false);
                }
                else
                {
                    return protocolError(event, originator, state);
                }

            } // case InvokeDirectiveInv

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

            case eeSLESE_TransferDataRtn:
            case eeSLESE_InvokeDirectiveRtn:
            {
                if (state == SLE_SIState.sleSIS_active || state == SLE_SIState.sleSIS_stopPending)
                {
                    return informAplOpRtn(cop);
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
            } // case TransferDataRtn, InvokeDirectiveRtn

            case eeSLESE_PduTransmitted:
            {
                if (state == SLE_SIState.sleSIS_active || state == SLE_SIState.sleSIS_stopPending)
                {
                    return processPduTransmitted(poperation);
                }
                else
                {
                    return protocolError(event, originator, state);
                }
            } // end PduTransmitted

            default:
            {
                return HRESULT.EE_E_NOSUCHEVENT;
            }

            } // end switch (event)

        } // end (originator == sleCP_proxy)

        return HRESULT.S_OK;
    }

    /**
     * The function performs processing of TRANSFER-DATA operation. It performs
     * flow control processing as defined in [SLE-API].
     */
    private HRESULT processTransferData(ISLE_Operation poperation)
    {
        // this function must be protected as lock
        // is given up in initiatePxyOpInv:
        // no other TD operation must enter this function
        // until previous current thread is back

        this.tdMutex.lock();

        if (this.suspended)
        {
            this.tdMutex.unlock();
            return HRESULT.SLE_E_SUSPENDED;
        }

        HRESULT rc = initiatePxyOpInv(poperation, true); // report transmission

        if (rc == HRESULT.SLE_S_TRANSMITTED)
        {
            this.tdMutex.unlock();
            return HRESULT.S_OK;
        }
        else
        {
            if (rc == HRESULT.SLE_S_QUEUED)
            {
                if (this.waitNotifyPDU != null)
                {
                    this.waitNotifyPDU = null;
                    this.suspended = true;
                    this.waitNotifyPDU = poperation.queryInterface(ISLE_Operation.class);
                    this.tdMutex.unlock();
                    return HRESULT.SLE_S_SUSPEND;
                }
            }
        }

        this.tdMutex.unlock();

        return rc;
    }

    /**
     * Requests the application to resume data transfer.
     */
    private HRESULT processPduTransmitted(ISLE_Operation poperation)
    {
        this.objMutex.unlock();
        this.tdMutex.lock();

        ISLE_Operation op = poperation.queryInterface(ISLE_Operation.class);

        if (!this.waitNotifyPDU.equals(op))
        {
            this.tdMutex.unlock();
            this.objMutex.lock();
            return HRESULT.SLE_E_UNSOLICITED;
        }

        this.suspended = false;

        if (this.waitNotifyPDU != null)
        {
            this.waitNotifyPDU = null;
        }

        this.tdMutex.unlock();
        this.objMutex.lock();

        informAplResumeDT();

        return HRESULT.S_OK;
    }
}
