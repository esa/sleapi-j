/**
 * @(#) EE_OpSequencer.java
 */

package esa.sle.impl.ifs.gen;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_Result;

/**
 * Provides functionality for sequencing of operation objects, and invokes
 * Release() on any cached operation objects it is forced to discard by release.
 * In the following four situations, the next expected is set to 1: 1) Before
 * any Operations have been sequenced 2) After an Unbind operation is returned
 * from sequenceIt (which is NOT synonymous with an unbind operation being
 * passed in to sequenceIt) 3) After reset has been called on the object. 4)
 * After an abort operation is returned from sequenceIt (which is NOT synonymous
 * with an abort operation being passed in to sequenceIt) When next expected is
 * 1, any operations that are not bind will be ignored by sequenceIt, which will
 * return SLE_E_SEQUENCE. Whenever reset is called on an EE_OpSequencer object,
 * Release will be invoke on all cached operations.
 */
public class EE_OpSequencer
{
    /**
     * Determines the maximum difference between a sequence number and the next
     * expected sequence number.
     */
    private long windowSize = 0;

    /**
     * The next expected attribute will be reset to 1 every time a bind is
     * passed in to SequenceIt . It will also be reset to 1 when a peer-Abort is
     * passed in, or when Reset is called, or when an unbind is passed out of
     * the sequenceIt method. The following applies when a number comes in that
     * is LESS than next expected: calculate disttoWrap = maxULong -
     * _nextExpected + 1 calcluate maxcount = window - disttowrap if (maxcount >
     * 0) then the sequence count being examined is acceptable if if it is <=
     * maxcount. If a sequence count is examined that is greater than next
     * expected it must be <= min(nextexpected + window,maxuint)
     */
    private long nextExpected = 1;

    /**
     * Flag that states whether the object is processing elements (in the
     * processing state), or not (ie is awaiting a bind).
     */
    private boolean processing = false;

    /**
     * After an abort or unbind has been cached, the Sequencer enters the
     * stopping sub state and rejects all operations with a higher number
     * sequence count than _stopping.
     */
    private long stopping = 0;

    LinkedList<EE_GenSeqElem> cache = new LinkedList<EE_GenSeqElem>();

    LinkedList<EE_GenSeqElem> pending = new LinkedList<EE_GenSeqElem>();

    private final Lock lock = new ReentrantLock();


    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.cache == null) ? 0 : this.cache.hashCode());
        result = prime * result + (int) (this.nextExpected ^ (this.nextExpected >>> 32));
        result = prime * result + ((this.pending == null) ? 0 : this.pending.hashCode());
        result = prime * result + (this.processing ? 1231 : 1237);
        result = prime * result + (int) (this.stopping ^ (this.stopping >>> 32));
        result = prime * result + (int) (this.windowSize ^ (this.windowSize >>> 32));
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
        EE_OpSequencer other = (EE_OpSequencer) obj;
        if (this.cache == null)
        {
            if (other.cache != null)
            {
                return false;
            }
        }
        else if (!this.cache.equals(other.cache))
        {
            return false;
        }
        if (this.nextExpected != other.nextExpected)
        {
            return false;
        }
        if (this.pending == null)
        {
            if (other.pending != null)
            {
                return false;
            }
        }
        else if (!this.pending.equals(other.pending))
        {
            return false;
        }
        if (this.processing != other.processing)
        {
            return false;
        }
        if (this.stopping != other.stopping)
        {
            return false;
        }
        if (this.windowSize != other.windowSize)
        {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unused")
    private EE_OpSequencer()
    {
        this.windowSize = 0;
        this.nextExpected = 1;
        this.processing = false;
        this.stopping = 0;
    }

    public EE_OpSequencer(long windowSize)
    {
        this.windowSize = windowSize;
        this.nextExpected = 1;
        this.processing = false;
        this.stopping = 0;
    }

    public ISLE_Operation sequenceIt(ISLE_Operation pop,
                                     long sequenceCount,
                                     EE_Reference<Boolean> isInvoke,
                                     EE_Reference<HRESULT> retVal)
    {
        return sequenceIt(pop, sequenceCount, isInvoke, retVal, false);
    }

    /**
     * The sequencer takes a (possibly) out of sequence operation, and whether
     * it is an invocation or not, and returns an HRESULT stating whether the
     * returned operation points to a valid operation or not.. There are 5
     * possible result codes: S_OK The operation passed in is in sequence, or
     * the retrievePending argument was set, the output parameters have been set
     * to valid values, and there are no pending operations. I_OKPENDING The
     * operation passed in is in sequence, or the retrievePending argument was
     * set,, the output parameters have been set, and there exist cached
     * operations which can be retrieved immediately. I_SEQUENCEWAIT The input
     * operation has been cached and is valid and the output parameters have not
     * been set to valid values, and no pending operations exist. SLE_E_SEQUENCE
     * The retrievePending flag was set, and no pending operations exist, or was
     * not set and the sequence count passed in was either less than the next
     * expected, or greater than the (next expected + window size) mod MAX_UINT
     * or the operation type was inappropriate for the current state of the
     * E_OpSequencer. this method modifies 3 client values : ISLE_Operation bool
     * isInvoke HREsult code.
     */
    public ISLE_Operation sequenceIt(ISLE_Operation pop,
                                     long sequenceCount,
                                     EE_Reference<Boolean> isInvoke,
                                     EE_Reference<HRESULT> retVal,
                                     boolean retrievePending)
    {
        this.lock.lock();
        ISLE_Operation popReturn = null;

        boolean bBadSequence = false;
        boolean bOutIsIn = false;

        if (!retrievePending)
        {
            SLE_OpType tmpType = pop.getOperationType();
            // bind is valid in ONE, situation - when we are expecting it.
            // abort and unbind are only valid when the sequencer is not
            // stopping
            // _stopping logic is needed to ensure no
            switch (tmpType)
            {
            case sleOT_bind:
                // modif 20-10-00 CL : if the provider has sent a negative
                // bind return, a new bind can comes in with sequence number=1
                // but we are already in the processing state !
                // if (! ((sequenceCount == 1) && (!_processing))){
                if (sequenceCount != 1)
                {
                    this.lock.unlock();
                    retVal.setReference(HRESULT.SLE_E_SEQUENCE);
                    return null;
                }
                // modif 20-10-00 CL : when a bind comes in, the processing must
                // be false.
                this.processing = false;
                break;
            case sleOT_unbind:
            case sleOT_peerAbort:
                // modified, so that peerAbort can follow an unbind
                // AFW, 20/03/2000
                if (this.stopping < sequenceCount)
                {
                    this.stopping = sequenceCount;
                }
                break;
            default:
                break;
            }
            if (!this.processing)
            {
                // only one way to leave the !processing state, must
                // receive a bind with a sequence count of 1.
                // leave the output parameters as they are.
                if ((tmpType == SLE_OpType.sleOT_bind) && (sequenceCount == 1))
                {
                    // transition to the next state.
                    ISLE_ConfirmedOperation ptmpop = (ISLE_ConfirmedOperation) pop;
                    // 9 May, 2000
                    // AFW, negative bind result is equivalent to an unbind ...
                    // it leaves the sequencer in exactly the same state it
                    // found
                    // it. it also passes the bind (negative) out.
                    if (ptmpop.getResult() != SLE_Result.sleRES_negative)
                    {
                        this.processing = true;
                        this.nextExpected = 2;
                        this.stopping = 0;
                    }
                    bOutIsIn = true;
                }
                else if (tmpType == SLE_OpType.sleOT_peerAbort)
                {
                    // modified 20/03/2000,
                    // so that peerabort can preceed a bind.
                    bOutIsIn = true;
                }
                else
                {
                    // just ignore whats been passed.
                    bBadSequence = true;
                }
            }
            // in the processing state.
            else
            {
                // ok, processing.
                // next expected is not necessarily first pending element.
                // need to validate sequence count.
                // unbind and peer abort have already been validated ...
                // so dont revalidate
                boolean bBadStopVal = ((this.stopping != 0) && (tmpType != SLE_OpType.sleOT_unbind)
                                       && (tmpType != SLE_OpType.sleOT_peerAbort) && (sequenceCount >= this.stopping));

                if ((sequenceCount < this.nextExpected) && (!bBadStopVal))
                {
                    long distToWrap = Long.MAX_VALUE - this.nextExpected;
                    distToWrap++;// add one because it is ne -> UINT_MAX
                                 // inclusive.
                    long maxCount;
                    boolean bCanWrap = (distToWrap < this.windowSize);
                    if (bCanWrap)
                    {
                        maxCount = this.windowSize - distToWrap;
                        if (sequenceCount < maxCount)
                        {
                            EE_GenSeqElem ptrNew = new EE_GenSeqElem(isInvoke.getReference(), pop, sequenceCount);
                            bBadSequence = !(addElem(ptrNew));
                        }
                        else
                        {
                            bBadSequence = true;
                        }
                    }
                    else
                    {
                        // cant wrap so must be bad value.
                        bBadSequence = true;
                    }
                }
                else if ((sequenceCount > this.nextExpected) && (!bBadStopVal))
                {
                    if ((sequenceCount - this.nextExpected) <= this.windowSize)
                    {
                        EE_GenSeqElem ptrNew = new EE_GenSeqElem(isInvoke.getReference(), pop, sequenceCount);
                        bBadSequence = !(addElem(ptrNew));
                    }
                    else
                    {
                        bBadSequence = true;
                    }
                }
                else if (!bBadStopVal)
                {
                    if (this.pending.size() != 0)
                    {
                        EE_GenSeqElem ptrNew = new EE_GenSeqElem(isInvoke.getReference(), pop, sequenceCount);
                        bBadSequence = !(addElem(ptrNew));
                    }
                    else
                    {
                        // output parameters stay as they are.
                        bOutIsIn = true;
                    }
                    this.nextExpected = nextSeqCount(this.nextExpected);
                    while (this.cache.size() > 0)
                    {
                        EE_GenSeqElem ptrNext = this.cache.peek();
                        if (this.nextExpected == ptrNext.getSeqCount())
                        {
                            this.pending.addLast(ptrNext);
                            this.nextExpected = nextSeqCount(this.nextExpected);
                            this.cache.removeFirst();
                        }
                        else
                        {
                            break;
                        }
                    }
                }
            }
        }

        retVal.setReference(HRESULT.SLE_E_SEQUENCE);

        if (!bBadSequence)
        {
            if (!bOutIsIn)
            {
                if (this.pending.size() == 0)
                {
                    if (!retrievePending)
                    {
                        // element has been cached, and none is pending.
                        retVal.setReference(HRESULT.EE_S_SEQUENCEWAIT);
                    }
                    // else SLE_E_SEQUENCE.
                }
                else
                {
                    EE_GenSeqElem ptrRetrieve = this.pending.peek();
                    popReturn = ptrRetrieve.getPop();
                    isInvoke.setReference(ptrRetrieve.getIsInvoke());
                    ptrRetrieve = null;
                    this.pending.removeFirst();
                    if (this.pending.size() > 0)
                    {
                        retVal.setReference(HRESULT.EE_S_OKPENDING);
                    }
                    else
                    {
                        retVal.setReference(HRESULT.S_OK);
                    }
                }
            }
            else
            {
                popReturn = pop;
                if (this.pending.size() > 0)
                {
                    retVal.setReference(HRESULT.EE_S_OKPENDING);
                }
                else
                {
                    retVal.setReference(HRESULT.S_OK);
                }

            }
        }
        // else SLE_E_SEQUENCE
        if (this.stopping != 0)
        {
            if ((this.pending.size() == 0) && (this.cache.size() == 0))
            {
                // finished all pending and missing elements.
                // there are no more elements.
                // reset all the values.
                unsafe_reset();
            }
        }
        this.lock.unlock();
        return popReturn;
    }

    /**
     * This should be called when the client wishes to flush the queue (for
     * example after a Protocol Abort has occurred). Note that before any
     * operations are discarded, Release() is invoked on the operation.
     */
    public void reset()
    {
        this.lock.lock();
        unsafe_reset();
        this.lock.unlock();
    }

    /**
     * Calculates what the value of the sequence count subsequent to the
     * argument is.
     */
    private long nextSeqCount(long seqCount)
    {
        if (seqCount == Long.MAX_VALUE)
        {
            return 0;
        }
        return ++seqCount;
    }

    /**
     * Enqueues an element if valid, and deletes the element if not. Returns
     * whether the element was successfully enqueued or not.
     */
    private boolean addElem(EE_GenSeqElem ptrNewElem)
    {
        // ## begin EE_OpSequencer::addElem%37CD9FF002DA.body preserve=yes
        // while attempting to deduce the relationship between
        // the addElem and the listElem, we must ALSO examine the relationships
        // to _nextExpected to determine an ordering.
        // this is to handle wrapping.

        Iterator<EE_GenSeqElem> li = this.cache.iterator();

        boolean bCondX, bCondY, bCondZ;
        bCondY = ptrNewElem.getSeqCount() > this.nextExpected;

        int k = 0;
        while (!li.hasNext())
        {
            EE_GenSeqElem ptrElem = li.next();
            bCondX = ptrNewElem.getSeqCount() > ptrElem.getSeqCount();
            bCondZ = this.nextExpected > ptrElem.getSeqCount();
            if (ptrNewElem.getSeqCount() == ptrElem.getSeqCount())
            {
                ptrNewElem = null;
                return false;
            }
            if (bCondX)
            {
                if (bCondY && bCondZ)
                {
                    break;
                }
            }
            else if (bCondY != bCondZ)
            {
                break;
            }
            k++;
        }
        this.cache.add(k, ptrNewElem);
        return true;

    }

    private void unsafe_reset()
    {
        while (this.cache.size() > 0)
        {
            @SuppressWarnings("unused")
            EE_GenSeqElem pDiscard = this.cache.pop();
            pDiscard = null;
        }
        while (this.pending.size() > 0)
        {
            @SuppressWarnings("unused")
            EE_GenSeqElem pDiscard = this.pending.pop();
            pDiscard = null;
        }
        this.processing = false;
        this.stopping = 0;
        this.nextExpected = 1;
    }

}
