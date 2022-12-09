package esa.sle.impl.eapi.dcw;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SIAdmin;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import esa.sle.impl.eapi.dcw.type.DCW_Event_Type;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_MessageRepository;
import esa.sle.impl.ifs.gen.EE_OpSequencer;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class implements the functionality of the component class 'DCW Service
 * Instance' specified in reference [API-RM]. It is responsible for implementing
 * the interface ISLE_ServiceInform for reception of up-calls from the service
 * instance queuing up-calls received from the service instance in the service
 * element linking of service instances with the DCW logging and tracing on
 * request of the application Upcalls are passed to the dispatcher (until
 * stopHandling notification is received) and sequence counting is implemented
 * for operations received. Protocol abort will reset the sequence count and
 * cause events on the event queue to be discarded. Calls to the
 * EE_DCW_EventDispatcher are made from instances of this class while the
 * instance is still locked. This is fine as those 2 calls do not call any of
 * this classes methods.
 */

public class EE_DCW_ServiceInstance implements ISLE_ServiceInform, Comparable<EE_DCW_ServiceInstance>
{
    private static final Logger LOG = Logger.getLogger(EE_DCW_ServiceInstance.class.getName());

    /**
     * contains the maximum events allowed to be enqueued before failure occurs.
     */
    private int maxPending = 0;

    /**
     * Set by the constructor argument, needed for Protocol Abort handling.
     */
    private ISLE_Reporter pReporter = null;

    private boolean flushingState = false;

    private BlockingDeque<EE_DCW_Event> eventQ = new LinkedBlockingDeque<EE_DCW_Event>();

    private EE_DCW_EventDispatcher pDispatcher = new EE_DCW_EventDispatcher();

    private EE_OpSequencer sequencer = null;

    private ISLE_SIAdmin source = null;

    private Semaphore spacesAvailable = new Semaphore(0);

    private ReentrantLock outerLock = new ReentrantLock();

    private ReentrantLock objMutex = new ReentrantLock();


    public void setSource(ISLE_SIAdmin source)
    {
        this.source = source;
    }

    public EE_DCW_ServiceInstance(final EE_DCW_ServiceInstance right)
    {
        this.maxPending = right.maxPending;
        this.pReporter = right.pReporter;
        this.flushingState = right.flushingState;
        this.source = right.source;
        this.sequencer = new EE_OpSequencer(1);

    }

    public EE_DCW_ServiceInstance(long windowSize,
                                  EE_DCW_EventDispatcher pDispatcher,
                                  ISLE_Reporter pReporter,
                                  int maxPending)
    {
        this.maxPending = maxPending;
        this.pReporter = pReporter;
        this.source = null;
        this.flushingState = false;
        this.spacesAvailable = new Semaphore(maxPending);
        this.sequencer = new EE_OpSequencer(windowSize);
        this.pDispatcher = pDispatcher;
        this.eventQ = new LinkedBlockingDeque<EE_DCW_Event>();
        this.objMutex = new ReentrantLock();
        this.outerLock = new ReentrantLock();
    }

    @Override
    public int compareTo(EE_DCW_ServiceInstance o)
    {
        if (hashCode() < o.hashCode())
        {
            return -1;
        }
        else if (hashCode() > o.hashCode())
        {
            return 1;
        }
        return 0;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.eventQ == null) ? 0 : this.eventQ.hashCode());
        result = prime * result + (this.flushingState ? 1231 : 1237);
        result = prime * result + this.maxPending;
        result = prime * result + ((this.pDispatcher == null) ? 0 : this.pDispatcher.hashCode());
        result = prime * result + ((this.pReporter == null) ? 0 : this.pReporter.hashCode());
        result = prime * result + ((this.sequencer == null) ? 0 : this.sequencer.hashCode());
        result = prime * result + ((this.source == null) ? 0 : this.source.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        EE_DCW_ServiceInstance other = (EE_DCW_ServiceInstance) obj;
        if (this.eventQ == null)
        {
            if (other.eventQ != null)
            {
                return false;
            }
        }
        else if (!this.eventQ.equals(other.eventQ))
        {
            return false;
        }
        if (this.flushingState != other.flushingState)
        {
            return false;
        }
        if (this.maxPending != other.maxPending)
        {
            return false;
        }

        if (this.pDispatcher == null)
        {
            if (other.pDispatcher != null)
            {
                return false;
            }
        }
        else if (!this.pDispatcher.equals(other.pDispatcher))
        {
            return false;
        }
        if (this.pReporter == null)
        {
            if (other.pReporter != null)
            {
                return false;
            }
        }
        else if (!this.pReporter.equals(other.pReporter))
        {
            return false;
        }
        if (this.sequencer == null)
        {
            if (other.sequencer != null)
            {
                return false;
            }
        }
        else if (!this.sequencer.equals(other.sequencer))
        {
            return false;
        }
        if (this.source == null)
        {
            if (other.source != null)
            {
                return false;
            }
        }
        else if (!this.source.equals(other.source))
        {
            return false;
        }
        return true;
    }


    final int DCW_PROTOABORT = 1001;

    final int DCW_BIND_REMOVED = 1014;

    final int DCW_PEERABORT = 1015;


    /**
     * Internal implementation of flushQueue, which does not lock. If the
     * argument all_operations is set to true, all the operations are removed
     * from the queue, and the method return true if at least one operation was
     * removed. If the argument all_operations is set to false, all the
     * operations of the queue are removed till a bind invoke operation is
     * founded. If a bind invoke operation is founded, it is also removed and
     * the method return true.
     */
    private boolean unsafeFlushQueue(boolean all_operations)
    {
        boolean retval = false;
        ISLE_Operation pBind = null;
        if (this.pDispatcher != null)
        {
            this.pDispatcher.cancelDispatch(this);
        }

        // all queued events in the sequencer are released.
        // the element are removed from the back to the top
        this.sequencer.reset();
        while (!this.eventQ.isEmpty())
        {
            EE_DCW_Event ptmp = this.eventQ.removeLast();
            if (all_operations == true)
            {
                retval = true;
            }
            this.spacesAvailable.release();

            ISLE_Operation pop = ptmp.getOperation();

            if (pop != null)
            {
                if (all_operations == false)
                {
                    // check if it is a bind operation
                    if ((pop.getOperationType() == SLE_OpType.sleOT_bind)
                        && (ptmp.getEventType() == DCW_Event_Type.dcwEVT_informOpInvoke))
                    {
                        // a bind operation is found, and removed
                        retval = true;
                        pBind = pop;
                        ptmp = null;
                        break;
                    }
                }
            }
            ptmp = null;
        }

        if ((all_operations == false) && (retval == true))
        {
            // a bind has been removed instanciate a report
            if (this.pReporter != null)
            {
                ISLE_SII pII = null;
                if (this.source != null)
                {
                    pII = this.source.getServiceInstanceIdentifier();
                }
                String mess_op = pBind.print(512);
                String mess = EE_MessageRepository.getMessage(this.DCW_BIND_REMOVED, mess_op, null, null);
                this.pReporter.logRecord(SLE_Component.sleCP_application,
                                         pII,
                                         SLE_LogMessageType.sleLM_alarm,
                                         this.DCW_BIND_REMOVED,
                                         mess);
            }
        }
        return retval;
    }

    /**
     * Instructs the Service Instance to flush its Queue
     */
    public HRESULT flushQueue()
    {
        this.objMutex.lock();
        this.flushingState = true;
        boolean iRemoved = unsafeFlushQueue(true);
        this.objMutex.unlock();
        if (iRemoved == true)
        {
            return HRESULT.S_OK;
        }
        else
        {
            return HRESULT.S_FALSE;
        }
    }

    /**
     * Constructs and enqueues a new event of type DCW_ EventType in a MT safe
     * and sequence counting compliant manner
     * 
     * @param poperation
     * @param seqCount
     * @param bIsInvoke
     * @return
     */
    private HRESULT InformOp(ISLE_Operation poperation, long seqCount, boolean bIsInvoke)
    {
        boolean bind_removed = false;
        if (poperation.getOperationType() == SLE_OpType.sleOT_peerAbort)
        {
            this.objMutex.lock();
            this.flushingState = true;
            bind_removed = unsafeFlushQueue(false); // empties the queue and
                                                    // releases the wait #hd#
            this.objMutex.unlock();
        }

        // lock to preserve atomicity of sequenceIt and push_back
        this.outerLock.lock();
        this.objMutex.lock();
        EE_Reference<HRESULT> hres = new EE_Reference<>();
        hres.setReference(HRESULT.SLE_E_SEQUENCE);
        boolean aborted = false;

        if (this.pDispatcher != null)
        {
            // operation has entered DCW. Reference is kept as long as the
            // operation remains in the DCW, and if the operation is passed out
            // of the DCW, the reference is left in place. only if
            // the DCW discards an operation is it released.
            EE_DCW_Event ptmp = null;
            ISLE_Operation pop = poperation;

            EE_Reference<Boolean> bIsInvokeReference = new EE_Reference<>();
            bIsInvokeReference.setReference(bIsInvoke);

            pop = this.sequencer.sequenceIt(poperation, seqCount, bIsInvokeReference, hres);

            while (hres.getReference() == HRESULT.EE_S_OKPENDING)
            {
                boolean deliver_op = true;
                if (pop.getOperationType() == SLE_OpType.sleOT_peerAbort)
                {
                    aborted = true; // #hd#
                    peerAbort();
                    if (bind_removed == true)
                    {
                        deliver_op = false;
                    }
                }
                else if (pop.getOperationType() == SLE_OpType.sleOT_bind)
                {
                    this.flushingState = false;
                }
                else if (this.flushingState == true)
                {
                    deliver_op = false;
                }

                if (deliver_op)
                {
                    // now we put it on the queue.
                    if (bIsInvokeReference.getReference())
                    {
                        ptmp = new EE_DCW_Event(DCW_Event_Type.dcwEVT_informOpInvoke, pop);
                    }
                    else
                    {
                        ptmp = new EE_DCW_Event(DCW_Event_Type.dcwEVT_informOpReturn, pop);
                    }
                    // if a peer abort, do not wait for space, the queue has
                    // been flushed !
                    if (pop.getOperationType() != SLE_OpType.sleOT_peerAbort)
                    {
                        this.objMutex.unlock();
                        this.spacesAvailable.release();
                        this.objMutex.lock();
                        if (this.flushingState == false)
                        {
                            this.eventQ.add(ptmp);
                            if (this.pDispatcher != null)
                            {
                                this.pDispatcher.needDispatch(this, aborted);
                            }
                        }
                        else
                        {
                            ptmp = null;
                        }
                    }
                    else
                    {
                        this.eventQ.add(ptmp);
                        // must always give dispatcher possibility to clear Q
                        // especially if we block in next round of loop
                        if (this.pDispatcher != null)
                        {
                            this.pDispatcher.needDispatch(this, aborted);
                        }
                    }
                }
                seqCount = 0;
                pop = this.sequencer.sequenceIt(null, seqCount, bIsInvokeReference, hres, true);
            }

            // need to queue this one as well.
            if (hres.getReference() == HRESULT.S_OK)
            {

                boolean deliver_op = true;

                if (pop.getOperationType() == SLE_OpType.sleOT_peerAbort)
                {
                    aborted = true; // #hd#
                    // peer_abort operation
                    peerAbort();
                    if (bind_removed == true)
                    {
                        deliver_op = false;
                    }
                }
                else if (pop.getOperationType() == SLE_OpType.sleOT_bind)
                {
                    this.flushingState = false;
                }
                else if (this.flushingState == true)
                {
                    deliver_op = false;
                }

                if (deliver_op == true)
                {
                    if (bIsInvokeReference.getReference())
                    {
                        ptmp = new EE_DCW_Event(DCW_Event_Type.dcwEVT_informOpInvoke, pop);
                    }
                    else
                    {
                        ptmp = new EE_DCW_Event(DCW_Event_Type.dcwEVT_informOpReturn, pop);
                    }

                    // if a peer abort, do not wait for space, the queue has
                    // been flushed !
                    if (pop.getOperationType() != SLE_OpType.sleOT_peerAbort)
                    {
                        this.objMutex.unlock();
                        this.spacesAvailable.release();
                        this.objMutex.lock();
                        if (this.flushingState == false)
                        {
                            this.eventQ.add(ptmp);

                            // must always give dispatcher possibility to clear
                            // Q
                            // especially if we block in next round of loop
                            if (this.pDispatcher != null)
                            {
                                this.pDispatcher.needDispatch(this, aborted);
                            }
                        }
                        else
                        {
                            ptmp = null;
                        }
                    }
                    else
                    {
                        this.eventQ.add(ptmp);

                        // must always give dispatcher possibility to clear Q
                        // especially if we block in next round of loop
                        if (this.pDispatcher != null)
                        {
                            this.pDispatcher.needDispatch(this, aborted);
                        }
                    }
                }

            }
            else if (hres.getReference() == HRESULT.EE_S_SEQUENCEWAIT)
            {
                hres.setReference(HRESULT.S_OK);
            }
            else
            {
                hres.setReference(HRESULT.SLE_E_SEQUENCE);
            }
            if (this.pDispatcher == null)
            {
                // StopHandling events must have occurred during wait on
                // semaphore.
                unsafeFlushQueue(true);
                hres.setReference(HRESULT.SLE_E_SEQUENCE);
            }
        }
        this.objMutex.unlock();
        this.outerLock.unlock();
        return hres.getReference();
    }

    @SuppressWarnings("unused")
    private final boolean getFlushingState()
    {
        return this.flushingState;
    }

    @SuppressWarnings("unused")
    private void setFlushingState(boolean value)
    {
        this.flushingState = value;
    }

    /**
     * The operation received from the service element is a peerAbort. The event
     * is logged.
     */
    private void peerAbort()
    {
        if (this.pReporter != null)
        {
            ISLE_SII pII = null;
            if (this.source != null)
            {
                pII = this.source.getServiceInstanceIdentifier();
            }
            String msg = EE_MessageRepository.getMessage(this.DCW_PEERABORT);
            this.pReporter.logRecord(SLE_Component.sleCP_application,
                                     pII,
                                     SLE_LogMessageType.sleLM_alarm,
                                     this.DCW_PEERABORT,
                                     msg);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_ServiceInform.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }

    }

    /**
     * Constructs and enqueues a new event of type DCW_ EventType
     * (dcwEVT_informOpInvoke) in a MT safe and sequence counting compliant
     * manner
     */
    @Override
    public void informOpInvoke(ISLE_Operation poperation, long seqCount) throws SleApiException
    {
        HRESULT res = HRESULT.S_OK;
        res = InformOp(poperation, seqCount, true);
        if (res != HRESULT.S_OK)
        {
            throw new SleApiException(res);
        }
    }

    public void informOpInvoke(ISLE_Operation poperation) throws SleApiException
    {
        informOpInvoke(poperation, 0);
    }

    /**
     * Constructs and enqueues a new event of type DCW_ EventType
     * (dcwEVT_informOpInvoke) in a MT safe and sequence counting compliant
     * manner.
     */
    @Override
    public void informOpReturn(ISLE_ConfirmedOperation poperation, long seqCount) throws SleApiException
    {
        ISLE_Operation pop = poperation;
        HRESULT res = HRESULT.S_OK;
        res = InformOp(pop, seqCount, false);
        if (res != HRESULT.S_OK)
        {
            throw new SleApiException(res);
        }
    }

    public void informOpReturn(ISLE_ConfirmedOperation poperation) throws SleApiException
    {
        informOpReturn(poperation, 0);
    }

    /**
     * Enqueues the corresponding DCW_EventType (dcwEVT resumeDataTransfer)
     * event in a MT safe manner.
     */
    @Override
    public void resumeDataTransfer()
    {
        this.outerLock.lock();
        this.objMutex.lock();

        if (this.flushingState == true)
        {
            this.objMutex.unlock();
            this.outerLock.unlock();
            return;
        }

        if (this.pDispatcher != null)
        {
            this.objMutex.unlock();
            try
            {
                this.spacesAvailable.wait();
            }
            catch (InterruptedException e)
            {
                LOG.log(Level.FINE, "InterruptedException ", e);
            }
            this.objMutex.lock();
            this.eventQ.add(new EE_DCW_Event(DCW_Event_Type.dcwEVT_resumeDataTransfer, null));
            if (this.pDispatcher != null)
            {
                this.pDispatcher.needDispatch(this, false);
            }
            else
            {
                // must have been set to Null during spacesAvailable.wait
                unsafeFlushQueue(true);
            }
        }
        this.objMutex.unlock();
        this.outerLock.unlock();

    }

    @Override
    public void provisionPeriodEnds()
    {

        this.outerLock.lock();
        this.objMutex.lock();

        // do not check for flushing state if PP ends,
        // this event shall always be delivered to the application !

        if (this.pDispatcher != null)
        {
            this.objMutex.unlock();
            try
            {
                this.spacesAvailable.acquire();
            }
            catch (InterruptedException e)
            {
                LOG.log(Level.FINE, "InterruptedException ", e);
            }
            this.objMutex.lock();
            this.eventQ.add(new EE_DCW_Event(DCW_Event_Type.dcwEVT_provisionPeriodEnds, null));
            if (this.pDispatcher != null)
            {
                this.pDispatcher.needDispatch(this, false);
            }
            else
            {
                unsafeFlushQueue(true);
            }
        }
        this.objMutex.unlock();
        this.outerLock.unlock();
    }

    /**
     * The application will be notified, passing a string which represents the
     * const SLE_Octet* converted to an Ascii string. An event of type
     * dcwEVT_protocol Abort will be enqueued.
     */
    @Override
    public void protocolAbort(final byte[] diagnostic) throws SleApiException
    {
        this.objMutex.lock();
        this.flushingState = true;
        boolean bind_removed = unsafeFlushQueue(false); // empties the queue and
                                                        // releases the wait
        this.objMutex.unlock();

        this.outerLock.lock();
        this.objMutex.lock();

        // if a bind invoke has been removed from the queue
        if (bind_removed == true)
        {
            this.objMutex.unlock();
            this.outerLock.unlock();
            throw new SleApiException(HRESULT.S_OK);
        }

        if (this.pDispatcher != null)
        {
            // no need to wait for space because the queue has been flushed !!
            if (this.pDispatcher != null)
            {
                this.eventQ.add(new EE_DCW_Event(DCW_Event_Type.dcwEVT_protocolAbort, null));
                this.pDispatcher.needDispatch(this, true); // added true
            }
            ISLE_SII pII = null;
            if (this.source != null)
            {
                pII = this.source.getServiceInstanceIdentifier();
            }

            String Diag = EE_GenStrUtil.convAscii(diagnostic, diagnostic.length);

            String logMsg = "";
            HRESULT hres = HRESULT.S_OK;
            try
            {
                logMsg = EE_MessageRepository.getMessageText(this.DCW_PROTOABORT);
            }
            catch (SleApiException e)
            {
                hres = e.getHResult();
            }

            String protMsg = "";
            if (hres == HRESULT.S_OK)
            {
                protMsg = logMsg;
            }
            protMsg += " ";
            protMsg += Diag;

            // unlock before I leave the class
            this.objMutex.unlock();

            // should always be valid, non-null.
            if (this.pReporter != null)
            {
                this.pReporter.logRecord(SLE_Component.sleCP_application,
                                         pII,
                                         SLE_LogMessageType.sleLM_alarm,
                                         this.DCW_PROTOABORT,
                                         protMsg);
            }

        }
        else
        {
            this.objMutex.unlock();
        }

        this.outerLock.unlock();

    }

    /**
     * Notification mechanism, notifies the object that any invocations on the
     * ServiceInform interface should no longer be handled (return failure code
     * on that interface).
     */
    public void stopHandlingEvents()
    {
        this.objMutex.lock();
        if (this.pDispatcher != null)
        {
            this.source = null;
            unsafeFlushQueue(true);
            this.pDispatcher = null;
            this.objMutex.unlock();
        }
        else
        {
            this.objMutex.unlock();
        }
    }

    /**
     * This function returns a pointer to the next pending event, which must be
     * deleted by the caller. If there are no events then NULL is returned. Note
     * that this function is MT safe.
     */
    public EE_DCW_Event getEvent()
    {
        this.objMutex.lock();
        EE_DCW_Event pRetVal = null;
        if (this.pDispatcher != null)
        {
            if (!this.eventQ.isEmpty())
            {
                pRetVal = this.eventQ.removeFirst();
                this.spacesAvailable.release();
            }
            if (!this.eventQ.isEmpty())
            {
                this.pDispatcher.needDispatch(this, false);
            }
        }
        this.objMutex.unlock();
        return pRetVal;
    }

}
