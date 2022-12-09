/**
 * @(#) EE_APIOpSequencer.java
 */

package esa.sle.impl.ifs.gen;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.it.SLE_OpType;

/**
 * the serialising of threads propagating into an API component (e.g. service
 * element or proxy) based on the sequence-count for operation objects. entry of
 * an operation object into a component (e.g. service element or proxy) the
 * Sequencer has to be called first using the function 'serialise()'. When that
 * function returns S_OK, the component (client) shall continue processing of
 * the supplied operation object (under lock()). When processing is finished,
 * the client MUST call 'continue()' in order to inform the Sequencer that
 * processing of the previous operation object is completed and the sequencer
 * can supply the next operation object in sequence and due for further
 * processing. The client has to call reset() using the reason-argument when an
 * association has been aborted and the Sequencer does not get the operation
 * object (This is e.g. the case in the service element, when on the prxy side a
 * peer-abort is received, the sequencer on the application side does not know
 * anything about the peer-abort), when a protocol abort occurres, or after an
 * orderly release (UNBIND) of the association. If a peer-abort operation is
 * passed to the sequencer via serialise(), every subsequently arriving
 * operation object is discarded (retun SLE_E_ABORTED) until the processing of
 * the peer-abort has finished, This also resets the next expected sequence
 * number to 1. sequencer holds a queue of sequence elements, which are all
 * pre-set with their own sequence number. At creation-time (or after reset()
 * has been finished), the sequence number of the first sequence object is one,
 * every following sequence object holds the sequence number 'previous count +
 * 1'. The queue is initialised with '_windowSize * 2' elements. If an operation
 * invocation with a certain sequence number is passed to the serialise()
 * function, the operation object is given to the sequence object that holds the
 * sequence number of the operation and the supplying thread has to wait
 * (condition variable) if the sequence count indicates that it is not the next
 * expected. Note that the next expected sequence count is always the count
 * which holds the first free element in the queue. 'Free' in this context means
 * that the sequence element in the queue has not yet a reference to an
 * operation object. The sequencer has a status attribute in order to ensure
 * sequential thread processing. If the state is 'processing' (an operation is
 * currently processed by the hosting component), the supplied operation is
 * queued even if it holds the next expected sequence number. The queue is
 * extended, if there is not enough space. This can happen that the queue fills
 * up while a previous operation is currently processed. When the client has
 * finished processing of an operation object, it calls continue(), which in
 * turn looks at the first element of the queue. If that contains an operation
 * object, it removes the element from the queue and signals the waiting thread
 * that he can continue. If no operation object is referenced by the first queue
 * element, continue() returns immediately. If the client resets the sequencer
 * object, the queue is stepped through and if any thread it waiting, its
 * condition variable is signalled. The state is first set according to the
 * reason-argument the client has supplied to the call of the reset() function.
 * After every thread is informed about the reset, the sequencer initialises its
 * queue, where the first element holds again the sequence-count 1. Note that
 * the queing mechanism of using sequence objects (that reference operation
 * objects) within the queue handles wrapping of an unsigned long from ULONG_MAX
 * to 0 implicitly, as the last element of the queue is created such that it
 * holds the sequence number of the previous element + 1. This wraps
 * automatically to zero if the previous count is ULONG_MAX. Therefore no extra
 * algorithms have to be invented for that wrapping mechanism. Whenever a new
 * operation object is passed to the corresponding queue element, it is analysed
 * and determined if its sequence number is within the acceptable window size.
 * This is done by stepping through the queue until the first non-pre-set
 * element is found. Its index is memorised. Then the queue is further stepped
 * through up to the element holding the new operation object. If the current
 * index minus the index of the first empty element is greater than the window
 * size, the new sequence number is considered out of the acceptable window size
 * and the operation is rejected with SLE_E_SEQUENCE. Note that in the special
 * case when an operation object that holds the next expected sequence number is
 * passed to the sequencer (in this case the next expected is the first element
 * in the queue and does not reference an operation object), and the state is
 * idle, the sequencer removes the first element from the list and appends it at
 * the end of the queue assigning it the new sequence number (sequence number of
 * last element +1). The function serialise() returns immediately S_OK. Note
 * that the sequencer does not make any assumptions on the operation objects
 * passed to the sequencer besides the PEER-ABORT operation. If that is
 * received, the sequencer sets its state to 'aborting' and reports it to all
 * waiting threads. Finally the queue is initialised again, which means that the
 * first sequence element in the queue holds the sequence count 1. For any other
 * situation the client has to inform the sequencer when it shall reset the
 * queue and start sequence counting from the begining.
 */
public class EE_APIOpSequencer
{
    private static final Logger LOG = Logger.getLogger(EE_APIOpSequencer.class.getName());

    private static int windowSize = 5;

    private static int queueLength = windowSize * 2;

    private final EE_CondVar thrCompletedCv = new EE_CondVar();

    private long thrCount;

    private EE_TI_GEN_PState state;

    private final LinkedList<EE_APIOpSeqElement> opList = new LinkedList<>();

    private final ReentrantLock objMutex = new ReentrantLock();


    public EE_APIOpSequencer()
    {

        this.thrCount = 0;
        this.state = EE_TI_GEN_PState.eeGEN_idle;
        initialiseQueue();

    }

    @SuppressWarnings("unused")
    private EE_APIOpSequencer(final EE_APIOpSequencer right)
    {
        this.thrCount = 0;
        this.state = right.state;
    }

    /**
     * Initializes the queue of operation objects. Note that the queue must be
     * empty before this function can be called, otherwise this function has no
     * effect.
     */
    private void initialiseQueue()
    {

        if (!this.opList.isEmpty())
        {
            return;
        }
        for (long i = 0; i < queueLength; i++)
        {
            EE_APIOpSeqElement elem = new EE_APIOpSeqElement();
            elem.setSeqCount(i + 1);
            this.opList.add(elem);
        }
    }

    /**
     * Serialises the thread stepping into the Sequencer based on the supplied
     * sequence number. If the supplied sequence number is not the next expected
     * number, the thread waits and the function is blocked. It returns when the
     * sequence is due for further processing
     *
     * @param pop
     * @param seqCount
     * @return S_OK - The thread can continue processing, sequence is preserved
     *         SLE_E_SEQUENCE - the supplied sequence count is out of the
     *         acceptable window size. SLE_E_ABORTED - serialisation of the
     *         supplied operation is rejected, as an ABORT operation is
     *         currently processed by the component.
     */
    public HRESULT serialise(ISLE_Operation pop, long seqCount)
    {
        this.objMutex.lock();

        if (this.state == EE_TI_GEN_PState.eeGEN_aborting)
        {
            this.objMutex.unlock();
            return HRESULT.SLE_E_ABORTED;
        }

        if (pop.getOperationType() == SLE_OpType.sleOT_peerAbort)
        {
            this.state = EE_TI_GEN_PState.eeGEN_aborting;

            // CS 2013-04-16 sleapi#1448
            if (this.thrCount > 0)
            {
                this.thrCompletedCv.lock();
            }

            this.objMutex.unlock();
            clearOpList();
            if (this.thrCount > 0)
            {
                try
                {
                    this.thrCompletedCv.wait();
                }
                catch (InterruptedException e)
                {
                    LOG.log(Level.FINE, "InterruptedException ", e);
                }
                this.thrCompletedCv.unlock();

            }
            //

            this.state = EE_TI_GEN_PState.eeGEN_idle;
            return HRESULT.S_OK;
        }

        switch (this.state)
        {
        case eeGEN_idle:
        case eeGEN_processing:
        {
            EE_Reference<EE_CondVar> cv = new EE_Reference<EE_CondVar>();
            HRESULT rc = queueOperation(pop, seqCount, cv);

            if (rc == HRESULT.SLE_S_SUSPEND)
            {
                this.thrCount++; // increase number of waiting threads
                // CS - 2013-04-04 sleapi#1448 inversion of 2 instructions
                // (SLEAPI deadlock)
                // this.objMutex.unlock();
                // cv.getReference().lock();
                cv.getReference().lock();
                this.objMutex.unlock();
                //
                try
                {
                    cv.getReference().await();
                }
                catch (InterruptedException e)
                {
                    LOG.log(Level.FINE, "SleApiException ", e);
                }
                finally
                {
                    cv.getReference().unlock();
                }

                this.objMutex.lock();
                cv = null;
                rc = HRESULT.S_OK;
                // check state after waiting has finished
                if (this.state == EE_TI_GEN_PState.eeGEN_idle)
                {
                    this.state = EE_TI_GEN_PState.eeGEN_processing;
                }
                else if (this.state == EE_TI_GEN_PState.eeGEN_aborting)
                {
                    rc = HRESULT.SLE_E_ABORTED;
                }
                else if (this.state == EE_TI_GEN_PState.eeGEN_unbinding)
                {
                    rc = HRESULT.SLE_E_UNBINDING;
                }

                // the state processing must not occure because the cv is not
                // signalled if processing is not yet finished. State is idle
                this.thrCount--; // decrease count of waiting threads
                if (this.thrCount == 0)
                {
                    this.thrCompletedCv.lock();
                    this.thrCompletedCv.signalAll();
                    this.thrCompletedCv.unlock();
                }
                this.objMutex.unlock();
                return rc;
            }
            else
            {
                // SLE_E_SEQUENCE or S_OK has been returned:
                if (rc == HRESULT.S_OK)
                {
                    this.state = EE_TI_GEN_PState.eeGEN_processing;
                }
                this.objMutex.unlock();
                return rc;
            }
        }
        case eeGEN_unbinding:
        {
            // discard the PDU in state terminating
            this.objMutex.unlock();
            return HRESULT.SLE_E_UNBINDING;
        }
        case eeGEN_aborting:
        {
            // discard the PDU in state aborting
            this.objMutex.unlock();
            return HRESULT.SLE_E_ABORTED;
        }
        }
        return HRESULT.S_OK;
    }

    /**
     * Requests the Sequencer to continue with supplying the next operation (in
     * sequence) which is due for processing. This function must be called by
     * the client when the processing of the previous operation is completed.
     */
    public void cont()
    {
        this.objMutex.lock();
        if (this.state == EE_TI_GEN_PState.eeGEN_aborting)
        {
            this.objMutex.unlock();
            return;
        }
        if (this.state == EE_TI_GEN_PState.eeGEN_processing)
        {
            this.state = EE_TI_GEN_PState.eeGEN_idle;
        }

        if (!this.opList.isEmpty())
        {
            EE_APIOpSeqElement firstElem = this.opList.getFirst();
            if (firstElem.getOp() != null)
            {
                this.opList.remove();
                EE_APIOpSeqElement lastElem = this.opList.getLast();
                long lastCount = lastElem.getSeqCount();
                EE_APIOpSeqElement newElem = new EE_APIOpSeqElement();
                newElem.setSeqCount(lastCount + 1);
                this.opList.add((newElem));
                firstElem.lock();
                firstElem.signalAll(); // elem is deleted by signalled thread
                firstElem.unlock();
                this.objMutex.unlock();
                return;
            }

        }
        this.objMutex.unlock();
    }

    /**
     * Flushes the list of pending operation objects and sets the next expected
     * sequence number to 1. The client must set the argument <reason> either to
     * SLE_E_ ABORTED or SLE_E_UNBINDING The argument reason is used internally
     * to signal all waiting therads the reason of termination.
     * 
     * @param reason
     */
    public void reset(HRESULT reason)
    {
        this.objMutex.lock();
        if (this.state == EE_TI_GEN_PState.eeGEN_aborting)
        {
            this.objMutex.unlock();
            return;
        }

        if (reason == HRESULT.SLE_E_UNBINDING)
        {
            this.state = EE_TI_GEN_PState.eeGEN_unbinding;
        }
        else
        {
            this.state = EE_TI_GEN_PState.eeGEN_aborting;
        }

        while (!this.opList.isEmpty())
        {
            EE_APIOpSeqElement firstElem = this.opList.removeFirst();
            if (firstElem.getOp() != null)
            {
                this.objMutex.unlock();
                firstElem.lock();
                firstElem.signalAll(); // elem is deleted by signalled thread
                firstElem.unlock();
                this.objMutex.lock();
            }
            else
            {
                firstElem = null;
            }
        }
        initialiseQueue();

        // the state is not changed before every thread has left the object
        if (this.thrCount > 0)
        {
            // CS 2013-04-16 sleapi#1448
            // this.objMutex.unlock();
            // this.thrCompletedCv.lock();
            this.thrCompletedCv.lock();
            this.objMutex.unlock();
            //
            try
            {
                this.thrCompletedCv.wait();
            }
            catch (InterruptedException e)
            {
                LOG.log(Level.FINE, "InterruptedException ", e);
            }
            this.thrCompletedCv.unlock();
            this.objMutex.lock();
        }
        this.state = EE_TI_GEN_PState.eeGEN_idle;
        this.objMutex.unlock();
    }

    /**
     * Queues the supplied operation object with its sequence count and returns
     * a newly allocated condition-variable, on which the caller HAS TO wait. It
     * is the responsibility of the caller to delete the condition variable.
     * S_OK The processing of the operation shall continue SLE_S_SUSPEND The
     * processing of the operation shall wait until the condition variable is
     * signalled SLE_E_SEQUENCE The supplied sequence number is out of the
     * acceptable window size
     * 
     * @param pop
     * @param seqCount
     * @return
     */
    private HRESULT queueOperation(ISLE_Operation pop, // The operation object
                                                       // for which sequencing
                                                       // is requested
                                   long seqCount, // The sequence count
                                                  // belonging to the operation
                                                  // object
                                   EE_Reference<EE_CondVar> cv // The condition
                                                               // variable on
                                                               // which the
                                                               // calling thread
                                                               // shall wait
    )
    {
        HRESULT rc = HRESULT.S_OK;

        cv.setReference(null);

        EE_APIOpSeqElement firstElem = this.opList.getFirst();

        if (seqCount == firstElem.getSeqCount())
        {

            if (firstElem.getOp() != null)
            {
                return HRESULT.SLE_E_SEQUENCE;
            }

            if (this.state == EE_TI_GEN_PState.eeGEN_processing)
            {
                firstElem.setOp(pop);
                cv.setReference(firstElem);
                return HRESULT.SLE_S_SUSPEND;
            }
            else
            {

                // state is idle, processing can start immediately
                this.opList.removeFirst();

                EE_APIOpSeqElement lastElem = this.opList.getLast();
                long lastSc = lastElem.getSeqCount();
                lastSc++;
                EE_APIOpSeqElement newElem = firstElem;
                newElem.setSeqCount(lastSc);
                this.opList.add(newElem);
                return HRESULT.S_OK; // processing can start
                                     // immediately,
                // the next expected sequence count is now the sequence count
                // helt by the first element in the queue
            }
        }

        Iterator<EE_APIOpSeqElement> i = this.opList.iterator();
        while (i.hasNext())
        {
            EE_APIOpSeqElement se = i.next();

            if (seqCount == se.getSeqCount())
            {
                // check for duplicate sequence count
                if (se.getOp() != null)
                {
                    return HRESULT.SLE_E_SEQUENCE;
                }
                se.setOp(pop);

                if (analyseMember(se) != HRESULT.S_OK)
                {
                    se = null;// removed from list by analyseMember()
                    return HRESULT.SLE_E_SEQUENCE;
                }
                cv.setReference(se);
                return HRESULT.SLE_S_SUSPEND;
            }
        }

        // no entry yet foreseen, extend the queue
        EE_APIOpSeqElement anotherNewElem = new EE_APIOpSeqElement();
        anotherNewElem.setOp(pop);
        anotherNewElem.setSeqCount(seqCount);
        rc = extendQueue(anotherNewElem);
        if (rc == HRESULT.S_OK)
        {
            if (analyseMember(anotherNewElem) != HRESULT.S_OK)
            {
                // the empty element can be kept in the queue, so
                // set pointer to operation object to zero.
                anotherNewElem.setOp(null);
                return HRESULT.SLE_E_SEQUENCE;
            }
            cv.setReference(anotherNewElem);
            return HRESULT.SLE_S_SUSPEND;
        }
        if (rc != HRESULT.S_OK)
        {
            anotherNewElem = null;
        }
        return rc;
    }

    /**
     * Extends the operations queue and enters the supplied list element into
     * the correct place. S_OK The element has been entered into the queue
     * SLE_E_SEQUENCE The element has not been entered into the queue because
     * its sequence count indicates out of the acceptable window size.
     * 
     * @param se
     * @return
     */
    private HRESULT extendQueue(EE_APIOpSeqElement se)
    {

        EE_APIOpSeqElement lastElem = this.opList.getLast();
        long lastElemCount = lastElem.getSeqCount();

        long reqCount = se.getSeqCount();
        long numNewElems = 0;

        // check if extension causes sequence error
        // before extension to increase performance:
        while (lastElemCount != reqCount - 1)
        {
            lastElemCount++;
            numNewElems++;
            if (numNewElems > windowSize)
            {
                return HRESULT.SLE_E_SEQUENCE;
            }
        }

        // queue-extension causes no error, extend queue:
        lastElemCount = lastElem.getSeqCount();
        while (lastElemCount != reqCount - 1)
        {
            EE_APIOpSeqElement newSe = new EE_APIOpSeqElement();
            lastElemCount++;
            newSe.setSeqCount(lastElemCount);
            this.opList.add(newSe);
        }

        // now we can insert the new element at the end of the queue
        this.opList.add(se);

        return HRESULT.S_OK;
    }

    /**
     * Flushes the list of pending operation objects, this includes to signal
     * the waiting therads to continue
     */
    private void clearOpList()
    {
        while (!this.opList.isEmpty())
        {
            EE_APIOpSeqElement elem = this.opList.pop();
            if (elem.getOp() != null) // other thread is waiting
            {
                elem.lock();
                elem.signalAll();
                elem.unlock();
            }
            else
            {
                elem = null;
            }
        }
        initialiseQueue();
    }

    /**
     * Analyzes the supplied queue member to have a sequence number that is
     * within the acceptable window size. The caller must ensure that the
     * supplied element is member of the queue. If SLE_E_SEQUENCE is returned,
     * the caller must make sure to set the pointer of the operation object
     * within the sequence element to 0.@End Function S_OK The elements sequence
     * number is within limits SLE_E_SEQUENCE The elements sequence number is
     * out of the acceptable window size.
     * 
     * @param se
     * @return
     */
    private HRESULT analyseMember(EE_APIOpSeqElement se)
    {
        HRESULT rc = HRESULT.S_OK;
        long idx = 0;
        long firstEmptyIdx = 0;

        Iterator<EE_APIOpSeqElement> i = this.opList.iterator();
        while (i.hasNext())
        {
            idx++;
            EE_APIOpSeqElement theElem = i.next();

            if ((theElem.getOp() == null) && (firstEmptyIdx == 0))
            {
                firstEmptyIdx = idx;
            }
            if (theElem.equals(se))
            {
                if ((idx - firstEmptyIdx) > windowSize)
                {
                    return HRESULT.SLE_E_SEQUENCE;
                }
                else
                {
                    return HRESULT.S_OK;
                }
            }
        }
        return rc;
    }

}
