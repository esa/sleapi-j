/**
 * @(#) EE_APISE_PRSI.java
 */

package esa.sle.impl.api.apise.slese;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.icc.ISLE_TimeoutProcessor;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iop.ISLE_TransferBuffer;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_DeliveryMode;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_PeerAbortDiagnostic;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.it.SLE_SIState;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import esa.sle.impl.api.apise.slese.types.EE_TI_SLESE_Event;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * This class provides the functionality that is specific to return service
 * instances for provider applications. It is responsible for state processing
 * defined in reference [SLE-API] for return service instances in the provider
 * role handling the concatenation buffer handling the latency limit and
 * controlled discarding of data for the online timely delivery mode handling
 * flow control processing as specified in reference [SLE-API] for the delivery
 * modes online-complete and offline The class implements buffering of PDUs
 * based on the delivery-mode to be supported. The delivery-mode is obtained by
 * a call to the abstract method get_Configuration(), which has to be
 * implemented by the derived class. State Processing (doStateProcessing()) is
 * performed according to the specifications for provider service instances for
 * the return-link. The state-processing functions forwards specific tasks to
 * the functions processLatencyTimer(), processPduTransmitted(),
 * processStopPdu(), processEndOfData(). These functions complete
 * state-processing where appropriate. When the service instance is informed of
 * the transmission of a TRANSFER-BUFFER-PDU by the proxy, the most derived
 * class is informed via a call to the abstract function updateStatusInfo(). The
 * derived class has then to update its status information by extracting data
 * from the PDUs (e.g. number of good frames delivered) in the TRANSFER-BUFFER
 * operation. When the service instance needs to prepend a 'data-discarded'
 * notification to the TRANSFER-BUFFER operation, it calls
 * prependNotification(). This function must be implemented by the most derived
 * class, as the SYNC-NOTIFY operation is a service type specific operation. For
 * every SYNC-NOTIFY operation the service instance receives, it checks if it is
 * an 'end-of-data' notification by a call to isEndOfData(), which also has to
 * be implemented by the most derived class. The class holds a set of data
 * members maintained by this class itself, the configuration parameters are
 * obtained (whenever needed) via a call to get_Configuration(), which must be
 * implemented by the derived class.
 */
public abstract class EE_APISE_PRSI extends EE_APISE_PSI
{
    private static final Logger LOG = Logger.getLogger(EE_APISE_PRSI.class.getName());

    /**
     * The information whether or not the data transfer is suspended.
     */
    private boolean suspended;

    /**
     * The information whether or not a transfer-buffer is queued for
     * transmission.
     */
    private boolean bufferQueued;

    /**
     * The transfer buffer.
     */
    private ISLE_TransferBuffer transferBuffer;

    /**
     * The transfer buffer for which the service instance is waiting on a
     * transmission notification. This is needed in the delivery modes
     * online-complete and offline.
     */
    private ISLE_TransferBuffer notifyBuffer;

    private boolean transferBufferDue;

    /**
     * The buffer that was sent although there was already one buffer queued in
     * the proxy. This is only needed during processing of a stop operation.
     */
    private ISLE_TransferBuffer flushedBuffer;

    private final RestartableTimer latencyTimer = new RestartableTimer();


    /**
     * The protected constructor with no arguments
     */
    protected EE_APISE_PRSI(String instanceKey)
    {
    	super(instanceKey);
        this.suspended = false;
        this.bufferQueued = false;
        this.transferBuffer = null;
        this.notifyBuffer = null;
        this.transferBufferDue = false;
        this.flushedBuffer = null;

    }

    /**
     * The protected constructor, to be used for service instance creation,
     * passes the supplied arguments to the base-class.
     */
    protected EE_APISE_PRSI(String instanceKey, SLE_ApplicationIdentifier srvType, ISLE_ServiceInform clientIf)
    {
        super(instanceKey, srvType, clientIf);
        this.suspended = false;
        this.bufferQueued = false;
        this.transferBuffer = null;
        this.notifyBuffer = null;
        this.transferBufferDue = false;
        this.flushedBuffer = null;
    }

    /**
     * If the supplied timer Id is the latency timer, the function invokes
     * latency-timer processing. If the timer-id indicates that it is a
     * different timer, the call is passed to the base-class.
     */
    @Override
    protected void doProcessTimeout(Object timer, int invocationId)
    {

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("doProcessTimeout get in");
        }

        if (!this.latencyTimer.equals(timer))
        {
            super.doProcessTimeout(timer, invocationId);
        }
        else
        {
            // no log but a trace message
            trace(SLE_TraceLevel.sleTL_medium, "Latency limit reached");
            doStateProcessing(SLE_Component.sleCP_serviceElement, EE_TI_SLESE_Event.eeSLESE_LatencyTimerExpired, null);
            // the timer is re-started in the state-processing function
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("doProcessTimeout get out");
        }
    }

    @Override
    protected HRESULT doConfigCompleted()
    {
        return super.doConfigCompleted();
    }

    /**
     * If the supplied timer-id is not the latency timer, it forwards the
     * request to the base-class. Otherwise a logging message is issued.
     */
    @Override
    public void HandlerAbort(RestartableTimer timer)
    {
        if (!this.latencyTimer.equals(timer))
        {
            super.HandlerAbort(timer);
        }
        else
        {
            logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_TimerAborted.getCode(), "Latency Timer");
        }
    }

    /**
     * Updates the status information parameters based on the supplied transfer
     * buffer operation. This function is pure virtual and must be implemented
     * by derived classes. The base-class calls that function whenever the proxy
     * has reported transmission of the supplied transfer-buffer operation.
     */
    protected abstract void updateStatusInfo(ISLE_TransferBuffer transmittedBuffer);

    /**
     * Resets the data needed for the transfer-buffer handling and discards any
     * non-empty transfer-buffer operation. Implementation: The base-class must
     * be called first.
     */
    @Override
    protected void cleanup()
    {
        super.cleanup();

        this.suspended = false;
        this.bufferQueued = false;

        if (this.transferBuffer != null)
        {
            this.transferBuffer.clear();
            this.transferBuffer = null;
        }

        cancelLatencyTimer();
        //this.latencyTimer.terminate();

        if (this.notifyBuffer != null)
        {
            this.notifyBuffer = null;
        }

        if (this.flushedBuffer != null)
        {
            this.flushedBuffer = null;
        }
    }

    @Override
    public void prepareRelease()
    {    	
    	this.latencyTimer.terminate();
    	super.prepareRelease();
    }
    
    /**
     * Performs state processing for operations on the provider side for return
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
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Current state: " + state + "  originator: " + originator + "  event: " + event
                       + "  poperation: " + poperation);
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
                    rc = initiatePxyOpRtn(cop, false);
                    if (rc.getCode() >= 0 && cop.getResult() == SLE_Result.sleRES_positive)
                    {
                        createNewBuffer();
                        stateTransition(SLE_SIState.sleSIS_active);
                        return rc;
                    }
                    stateTransition(SLE_SIState.sleSIS_bound);
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
                        processStopPdu(poperation);
                        rc = initiatePxyOpRtn(cop, false);
                        stateTransition(SLE_SIState.sleSIS_bound);
                        return rc;
                    }
                    rc = initiatePxyOpRtn(cop, false);
                    stateTransition(SLE_SIState.sleSIS_active);
                    return rc;
                }
                else
                {
                    return protocolError(event, originator, state);
                }
            } // StopRtn

            case eeSLESE_TransferDataInv:
            {
                if (state == SLE_SIState.sleSIS_active)
                {
                    if (this.suspended == true)
                    {
                        return HRESULT.SLE_E_SUSPENDED;
                    }
                    else
                    {
                        rc = bufferData(poperation);
                    }
                    return rc;
                }
                else if (state == SLE_SIState.sleSIS_stopPending)
                {
                    return HRESULT.SLE_E_STOPPING;
                }
                else
                {
                    return protocolError(event, originator, state);
                }
            } // eeSLESE_TransferDataInv

            case eeSLESE_SyncNotifyInv:
            {
                if (state == SLE_SIState.sleSIS_active || state == SLE_SIState.sleSIS_stopPending)
                {
                    if (this.suspended == true)
                    {
                        return HRESULT.SLE_E_SUSPENDED;
                    }
                    if (isEndOfData(poperation) == true)
                    {
                        rc = processEndOfData(poperation);
                    }
                    else
                    {
                        rc = bufferData(poperation);
                    }
                    return rc;
                }
                else
                {
                    return protocolError(event, originator, state);
                }
            } // SyncNotifyInv

            default:
            {
                return HRESULT.EE_E_NOSUCHEVENT;
            }
            } // end switch(event)
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
                    informAplOpInv(poperation);
                    return HRESULT.S_OK;
                }
                else if (state == SLE_SIState.sleSIS_startPending || state == SLE_SIState.sleSIS_active
                         || state == SLE_SIState.sleSIS_stopPending)
                {
                    abort(SLE_PeerAbortDiagnostic.slePAD_protocolError);
                    stateTransition(SLE_SIState.sleSIS_unbound);
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
                    informAplOpInv(poperation);
                    return HRESULT.S_OK;
                }
                else if (state == SLE_SIState.sleSIS_bound || state == SLE_SIState.sleSIS_startPending
                         || state == SLE_SIState.sleSIS_stopPending)
                {
                    abort(SLE_PeerAbortDiagnostic.slePAD_protocolError);
                    stateTransition(SLE_SIState.sleSIS_unbound);
                    return HRESULT.S_OK;
                }
                else
                {
                    return protocolError(event, originator, state);
                }
            } // StopInv

            case eeSLESE_PduTransmitted:
            {
                if (state == SLE_SIState.sleSIS_unbound || state == SLE_SIState.sleSIS_bindPending)
                {
                    return protocolError(event, originator, state);
                }
                else
                {
                    processPduTransmitted(poperation);
                    return HRESULT.S_OK;
                }
            } // PduTransmitted

            default:
            {
                return HRESULT.EE_E_NOSUCHEVENT;
            }
            } // end switch (event)
        } // end (originator == sleCP_proxy)

        // /////////////////////////////
        // Internal Events
        // /////////////////////////////

        if (originator == SLE_Component.sleCP_serviceElement) // internal event
        {
            switch (event)
            {
            case eeSLESE_LatencyTimerExpired:
            {
                if (state == SLE_SIState.sleSIS_active || state == SLE_SIState.sleSIS_stopPending)
                {
                    processLatencyTimer();
                    return HRESULT.S_OK;
                }
                else
                {
                    return protocolError(event, originator, state); // actually
                                                                    // N/A
                }
            }

            default:
            {
                return HRESULT.EE_E_NOSUCHEVENT;
            }
            } // end switch (event)
        } // end (originator == sleCP_serviceElement)

        return HRESULT.EE_E_NOSUCHEVENT;
    }

    /**
     * The function starts processing the latency timer. If a buffer is queued,
     * it requests the proxy to discard the buffer and transfers the new buffer
     * to the proxy. Finally a new buffer is created.
     */
    private HRESULT processLatencyTimer()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("processLatencyTimer get in");
        }

        if (this.bufferQueued)
        {
            SLE_DeliveryMode theDeliveryMode = getConfiguration().getDeliveryMode();
            if (theDeliveryMode == SLE_DeliveryMode.sleDM_rtnTimelyOnline)
            {
                HRESULT rc = discardBuffer();
                if (rc == HRESULT.SLE_S_DISCARDED)
                {
                    prependNotification(this.transferBuffer);
                }
            }
            else if (theDeliveryMode == SLE_DeliveryMode.sleDM_rtnCompleteOnline)
            {
                this.transferBufferDue = true; // buffer will be sent as soon as
                                               // processPduTransmitted() is
                                               // called

                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("processLatencyTimer out");
                }

                return HRESULT.S_OK;
            }
        }

        HRESULT pxyRc = HRESULT.S_FALSE;
        if (!this.transferBuffer.empty())
        {
            pxyRc = transmitBuffer();
        }
        if (pxyRc == HRESULT.SLE_S_QUEUED)
        {
            this.bufferQueued = true;
        }
        else
        {
            this.bufferQueued = false;
        }
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("processLatencyTimer get out");
        }
        return HRESULT.S_OK;
    }

    /**
     * The function resets 'buffer queued', resumes data-transfer (if needed),
     * and informs the derived class that an update of the status-info can be
     * done (for a TRANSFER-BUFFER operation.
     */
    private HRESULT processPduTransmitted(ISLE_Operation poperation)
    {
        ISLE_TransferBuffer tb = poperation.queryInterface(ISLE_TransferBuffer.class);
        if (tb != null)
        {
            updateStatusInfo(tb);
            if (this.notifyBuffer != null && this.notifyBuffer.equals(tb)) // SLEAPIJ-48
            {
                this.notifyBuffer = null;
            }
            else if (this.flushedBuffer.equals(tb))
            {
                this.flushedBuffer = null;
            }
            else
            {
                return HRESULT.SLE_E_UNSOLICITED;
            }
        }
        else
        {
            return HRESULT.E_FAIL;
        }

        // delivery modes on-line complete and offline:
        // If a full buffer is available at
        // this point transmit it to the
        // proxy.
        // Concerning state variables: _bufferQueued must be true when getting
        // here, and because a buffer is passed to the proxy now it must stay
        // true. if _suspended is true, it will be cleared on the next callback
        // when no more data is waiting to be sent.

        SLE_DeliveryMode theDeliveryMode = getConfiguration().getDeliveryMode();
        if (theDeliveryMode != SLE_DeliveryMode.sleDM_rtnTimelyOnline
            && (this.transferBuffer.full() || this.transferBufferDue))
        {
            // no need to check for SLE_S_TRANSMITTED, can not happen when
            // sending data from a callback
            transmitBuffer();
            return HRESULT.S_OK;
        }

        this.bufferQueued = false;

        if (this.suspended)
        {
            this.suspended = false;
            informAplResumeDT();
        }
        return HRESULT.S_OK;
    }

    /**
     * The function discards a queued buffer and starts transmitting the current
     * buffer.
     */
    private HRESULT processStopPdu(ISLE_Operation poperation)
    {
        SLE_DeliveryMode theDeliveryMode = getConfiguration().getDeliveryMode();

        if (!this.transferBuffer.empty())
        {
            if (theDeliveryMode == SLE_DeliveryMode.sleDM_rtnTimelyOnline)
            {
                cancelLatencyTimer();
                if (this.bufferQueued)
                {
                    HRESULT dRc = discardBuffer();
                    if (dRc == HRESULT.SLE_S_DISCARDED)
                    {
                        prependNotification(this.transferBuffer);
                    }
                }
            } // latency timer for online complete
            else if (theDeliveryMode == SLE_DeliveryMode.sleDM_rtnCompleteOnline)
            {
                cancelLatencyTimer();
            }

            HRESULT pxyRc = transmitBuffer(this.bufferQueued);
            if (pxyRc == HRESULT.SLE_S_QUEUED)
            {
                this.bufferQueued = true;
            }
            else if (pxyRc == HRESULT.SLE_S_TRANSMITTED)
            {
                this.bufferQueued = false;
            }
        }
        return HRESULT.S_OK;
    }

    /**
     * Processes an end-of-data notification. The function discards (in
     * online-timely delivery mode) a queued buffer and starts transmitting the
     * current buffer.
     */
    private HRESULT processEndOfData(ISLE_Operation poperation)
    {
        SLE_DeliveryMode theDeliveryMode = getConfiguration().getDeliveryMode();

        boolean bufferWasEmpty = this.transferBuffer.empty();
        this.transferBuffer.append(poperation);

        if (theDeliveryMode == SLE_DeliveryMode.sleDM_rtnTimelyOnline)
        {
            cancelLatencyTimer();
            if (this.bufferQueued)
            {
                HRESULT dRc = discardBuffer();
                if (dRc == HRESULT.SLE_S_DISCARDED)
                {
                    prependNotification(this.transferBuffer);
                }
            }
        }
        else if (theDeliveryMode == SLE_DeliveryMode.sleDM_rtnCompleteOnline && bufferWasEmpty && this.bufferQueued)
        {
            // start latency timer in case this is the first operation enqueued
            // and it's
            // not possible to send immediately
            startLatencyTimer();
        }

        // if not in timely online mode and there is still a transfer
        // buffer in the proxy, set the 'transfer buffer due' flag
        // (which means the current buffer will be sent during the next
        // processPduTransmitted() invocation) and return
        if (this.bufferQueued && theDeliveryMode != SLE_DeliveryMode.sleDM_rtnTimelyOnline)
        {
            this.transferBufferDue = true;
            if (this.transferBuffer.full())
            {
                this.suspended = true;
                return HRESULT.SLE_S_SUSPEND;
            }
            else
            {
                return HRESULT.S_OK;
            }
        }

        HRESULT pxyRc = transmitBuffer();
        if (pxyRc == HRESULT.SLE_S_QUEUED)
        {
            this.bufferQueued = true;
        }
        else
        {
            this.bufferQueued = false;
        }
        return HRESULT.S_OK;
    }

    /**
     * Performs the transfer-buffer processing of TRANSFER-DATA and SYNC-NOTIFY
     * invocations for all delivery modes.
     */
    private HRESULT bufferData(ISLE_Operation poperation)
    {
        //Thread.yield();

        HRESULT rc = HRESULT.S_OK;

        SLE_DeliveryMode theDeliveryMode = getConfiguration().getDeliveryMode();
        if ((theDeliveryMode == SLE_DeliveryMode.sleDM_rtnTimelyOnline || theDeliveryMode == SLE_DeliveryMode.sleDM_rtnCompleteOnline)
            && this.transferBuffer.empty())
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("startLatencyTimer, the buffer is null");
            }
            startLatencyTimer();
        }

        this.transferBuffer.append(poperation);

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("this.transferBuffer.size(): " + this.transferBuffer.getSize());
        }

        if (this.transferBuffer.full())
        {
            if (this.bufferQueued)
            {
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("bufferQueued: " + this.bufferQueued);
                }

                if (theDeliveryMode == SLE_DeliveryMode.sleDM_rtnTimelyOnline)
                {
                    cancelLatencyTimer();

                    HRESULT dRc = discardBuffer();
                    if (dRc == HRESULT.SLE_S_DISCARDED)
                    {
                        prependNotification(this.transferBuffer);
                    }

                    HRESULT pxyRc = transmitBuffer();
                    if (pxyRc == HRESULT.SLE_S_QUEUED)
                    {
                        this.bufferQueued = true;
                    }
                    else
                    {
                        this.bufferQueued = false;
                    }
                }
                else
                {
                    // delivery-mode is not online timely
                    if (theDeliveryMode == SLE_DeliveryMode.sleDM_rtnCompleteOnline)
                    {
                        cancelLatencyTimer(); // latency timer for onlc
                    }

                    this.suspended = true;
                    rc = HRESULT.SLE_S_SUSPEND;
                }
            }
            else
            {
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("bufferQueued: " + this.bufferQueued);
                }
                if (theDeliveryMode == SLE_DeliveryMode.sleDM_rtnTimelyOnline
                    || theDeliveryMode == SLE_DeliveryMode.sleDM_rtnCompleteOnline)
                {
                    cancelLatencyTimer();
                }

                // no buffer queued, transfer immediately
                HRESULT pxyRc = transmitBuffer();
                if (pxyRc == HRESULT.SLE_S_QUEUED)
                {
                    this.bufferQueued = true;
                }
                else
                {
                    this.bufferQueued = false;
                }
            }
        }
        return rc;
    }

    /**
     * Prepends a 'buffer-discarded' notification to the supplied
     * transfer-buffer. The function has to be implemented by derived classes,
     * as the SYNC-NOTIFY operation is a service-type specific operation.
     */
    protected abstract void prependNotification(ISLE_TransferBuffer buffer);

    /**
     * Returns true if the supplied operation is a 'EndOfData' notification. The
     * derived classes have to implement this function, because the SYNC-NOTIFY
     * operation is service-type specific.
     */
    protected abstract boolean isEndOfData(ISLE_Operation poperation);

    /**
     * Creates a new TransferBuffer operation.
     */
    private void createNewBuffer()
    {
        ISLE_OperationFactory opf = getOpFactory();
        int vn = getVersion();
        try
        {
            this.transferBuffer = opf.createOperation(ISLE_TransferBuffer.class,
                                                      SLE_OpType.sleOT_transferBuffer,
                                                      getServiceType(),
                                                      vn);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }

        EE_APISE_RSConfiguration rsc = (EE_APISE_RSConfiguration) getConfiguration();
        long bufferSize = rsc.getTransferBufferSize();

        // The maximum buffer size is set to maximum-buffer-size.
        // In the delivery mode on-line timely a possible Sync-notify
        // (data discarded) is pre-pended, which leads to a buffer-size
        // of max-size + 1.
        try
        {
            this.transferBuffer.setMaximumSize(bufferSize);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }

        // clear the 'buffer due' flag, as this (at least conceptually) belongs
        // to the old frame
        this.transferBufferDue = false;
    }

    /**
     * Passes the TRANSFER-BUFFER operation (private data) to the proxy. If the
     * result indicates that the buffer has been transmitted, it informs the
     * client (derived class) to update the status information. Before the
     * buffer is transmitted, a new buffer is created (to avoid problems in an
     * MT environment)
     */
    private HRESULT transmitBuffer(boolean forceSend)
    {
        // a new buffer has to be created before the current
        // buffer can be sent to the proxy, to prevent from
        // problems within a MT environment:

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("transmitBuffer in");
        }

        ISLE_TransferBuffer currentBuffer = this.transferBuffer;
        createNewBuffer();
        if (forceSend)
        {
            if (this.flushedBuffer != null)
            {
                this.flushedBuffer = null;
            }
            this.flushedBuffer = currentBuffer;
        }
        else
        {
            if (this.notifyBuffer != null)
            {
                this.notifyBuffer = null;
            }
            this.notifyBuffer = currentBuffer;
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("currentBuffer size " + currentBuffer.getSize());
        }
        HRESULT pxyRc = initiatePxyOpInv(currentBuffer, true); // reportTransmission

        if (pxyRc != HRESULT.SLE_S_QUEUED)
        {
            if (forceSend)
            {
                if (this.flushedBuffer != null)
                {
                    this.flushedBuffer = null;
                }
            }
            else
            {
                if (this.notifyBuffer != null)
                {
                    this.notifyBuffer = null;
                }
            }

            // in case the buffer was transmitted immediately, PDUTransmitted()
            // does not get called, so update counters here
            if (pxyRc == HRESULT.SLE_S_TRANSMITTED)
            {
                updateStatusInfo(currentBuffer);
            }
        }
        else if (pxyRc == HRESULT.SLE_S_QUEUED)
        {
            // In this case a different thread coming from the proxy
            // could have called processPduTransmitted already, which
            // has released the buffer, therefore set rc to transmitted.
            if (forceSend)
            {
                if (this.flushedBuffer == null)
                {
                    pxyRc = HRESULT.SLE_S_TRANSMITTED;
                }
            }
            else
            {
                if (this.notifyBuffer == null)
                {
                    pxyRc = HRESULT.SLE_S_TRANSMITTED;
                }
            }
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("transmitBuffer out");
        }

        return pxyRc;
    }

    /**
     * Calls the transmitBuffer method passing the default value false for force
     * send
     * 
     * @return
     */
    private HRESULT transmitBuffer()
    {
        return transmitBuffer(false);
    }

    /**
     * Passes the duscard-buffer request to the proxy. If the result indicates
     * that the buffer has been discarded, it releases the previous
     * TRANSFER-BUFFER operation that waits for the transmission-report from the
     * proxy.
     */
    private HRESULT discardBuffer()
    {
        HRESULT dRc = initiatePxyDiscardBuffer();
        if (dRc == HRESULT.SLE_S_DISCARDED)
        {
            if (this.notifyBuffer != null)
            {
                this.notifyBuffer = null;
            }
        }
        return dRc;
    }

    /**
     * Starts the latency timer.
     */
    private void startLatencyTimer()
    {
        EE_APISE_RSConfiguration rsc = (EE_APISE_RSConfiguration) getConfiguration();
        int latencyLimit = rsc.getLatencyLimit();
 
        final boolean started = this.latencyTimer.start(latencyLimit, TimeUnit.SECONDS, this, 0);
        if(started == false) {
        	LOG.log(Level.SEVERE, "RCF: Failed to start latency timer");
        }
    }

    /**
     * Cancels and releases the latency timer.
     */
    private void cancelLatencyTimer()
    {
    	this.latencyTimer.cancel();
//        if (this.latencyTimer != null)
//        {
//            this.latencyTimer.cancel();
//            this.latencyTimer = null;
//        }
    }

    /**
     * Forces the sending of the transfer buffer to the application, regardless
     * of the buffer size and of the latency limit.
     */
    protected HRESULT sendBuffer(boolean withNotification)
    {
        SLE_DeliveryMode theDeliveryMode = getConfiguration().getDeliveryMode();

        if (theDeliveryMode != SLE_DeliveryMode.sleDM_rtnTimelyOnline)
        {
            return HRESULT.E_FAIL;
        }

        cancelLatencyTimer();

        if (this.bufferQueued)
        {
            HRESULT rc = discardBuffer();
            if (rc == HRESULT.SLE_S_DISCARDED)
            {
                // notify that a buffer has been discarded
                prependNotification(this.transferBuffer);
            }
        }

        // send the buffer to the proxy, and create a new empty one
        HRESULT pxyRc = transmitBuffer();
        if (pxyRc == HRESULT.SLE_S_QUEUED)
        {
            this.bufferQueued = true;
        }
        else
        {
            this.bufferQueued = false;
        }

        if (withNotification)
        {
            // notify requested by the application
            prependNotification(this.transferBuffer);
        }

        return HRESULT.S_OK;
    }
    
    /**
     * Timer class with the ability to restart.
     */
    class RestartableTimer implements Runnable {

    	private static final int DEFAULT_TIMEOUT = 1;
		private final ReentrantLock lock;
    	private final Condition condition;
    	private final Thread timerThread;
    	private final AtomicBoolean terminate;
    	
    	// members below must be accessed under lock
    	private boolean armTimer;    	
    	private long timeout;
    	private TimeUnit unit;
		private ISLE_TimeoutProcessor processor;
		private int invocationId;
    	
		/**
		 * Constructs the timer object
		 */
    	public RestartableTimer() {
    		this.lock = new ReentrantLock();
    		this.condition = lock.newCondition();
    		this.timerThread = new Thread(this); // still safe publication, thread does not use this until Thread#start
    		this.timeout = DEFAULT_TIMEOUT;
    		this.unit = TimeUnit.MINUTES;
    		this.terminate = new AtomicBoolean(false);
    		this.invocationId = -1;
    	}
    	
    	/**
    	 * Start the timer.
    	 * @param timeout	the timeout value
    	 * @param unit		the timeout unit
    	 */
    	public boolean start(long timeout, TimeUnit unit, ISLE_TimeoutProcessor processor, int invocationId) {
    		if(this.terminate.get() == true) {
    			return false;
    		}
    		
    		if(this.timerThread.isAlive() == false) {
    			this.timerThread.start();
    		}
    		
    		lock.lock();
    		try {
    			if(this.armTimer == true) {
    				return false;
    			}
    			this.timeout = timeout;
    			this.unit = unit;
    			this.armTimer = true;
    			this.processor = processor;
    			this.invocationId = invocationId;
    			this.condition.signal(); // wake up our thread to go for the timeout
    			
    			return true;
    		} finally {
    			lock.unlock(); // always called
    		}
    	}
    	
    	/**
    	 * Cancels an on-going timer
    	 */
    	public void cancel() {
    		lock.lock();
    		try {
    			this.timeout = DEFAULT_TIMEOUT;
    			this.unit = TimeUnit.MINUTES;
    			this.armTimer = false;
    			this.processor = null;
    			this.invocationId = 0;
    			//this.condition.signal(); no need to signal, amrTimer == false is enough
    		} finally {
    			lock.unlock();
    		}
    	}
    	
    	/**
    	 * Terminates the internal timer thread
    	 */
    	public void terminate() {
    		this.terminate.set(true);
    		lock.lock();
    		try {
    			this.armTimer = false;
    			this.condition.signal();
    		} finally {
    			lock.unlock();
    		}
    		
    		try {
				this.timerThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	
		@Override
		public void run() {
			
			while(this.terminate.get() == false) {
				ISLE_TimeoutProcessor tempProc = null;
				int tempInvocId = 0;
	    		lock.lock();
	    		try {
	    			try {
						final boolean signalled = this.condition.await(this.timeout, this.unit);
						if(this.armTimer == true && signalled == false) {
							tempProc = this.processor;
							tempInvocId = this.invocationId;
							this.timeout = DEFAULT_TIMEOUT;
							this.unit = TimeUnit.MINUTES;
							this.armTimer = false;
							this.processor = null;
							this.invocationId = 0;
						}						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	    		} finally {
	    			lock.unlock();
	    		}
	    		
	    		// do the actual call outside lock
	    		if(tempProc != null) {
	    			tempProc.processTimeout(this, tempInvocId);
	    		}
			}			
		}
    	
    }
}
