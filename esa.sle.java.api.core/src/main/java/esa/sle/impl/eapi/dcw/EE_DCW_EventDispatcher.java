package esa.sle.impl.eapi.dcw;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import esa.sle.impl.eapi.dcw.type.DCW_Event_Type;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class notifies the client of the presence of events as they arrive, and
 * provides blocking (NextEvent) and nonblocking (PollEvent) mechanisms to
 * obtain those events. Note that instances of this class are not reference
 * counted. The client of this class should organise the synchronisation of the
 * instance of this class with instances of the class EE_DCW_Service Instance to
 * avoid any problems. This class contains and maintains a dispatch-queue that
 * consists of references to DCW service instances. It contains and uses an
 * instance of the class EE_DCW_EventNotifier to notify the client of the
 * presence of events. A condition variable is used internally to provide
 * blocking semantics for the call to NextEvent to cancelDispatch and
 * needDispatch do not (and indeed must not) leave the execution context of the
 * EE_DCW_EventDispatcher object.
 */

public class EE_DCW_EventDispatcher
{
    private static final Logger LOG = Logger.getLogger(EE_DCW_EventDispatcher.class.getName());

    private boolean suspended;

    private final LinkedBlockingQueue<EE_DCW_ServiceInstance> dispatchQ = new LinkedBlockingQueue<EE_DCW_ServiceInstance>();

    private final ReentrantLock lock = new ReentrantLock();


    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.dispatchQ == null) ? 0 : this.dispatchQ.hashCode());
        result = prime * result + (this.suspended ? 1231 : 1237);
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
        EE_DCW_EventDispatcher other = (EE_DCW_EventDispatcher) obj;
        if (this.dispatchQ == null)
        {
            if (other.dispatchQ != null)
            {
                return false;
            }
        }
        else if (!this.dispatchQ.equals(other.dispatchQ))
        {
            return false;
        }
        if (this.suspended != other.suspended)
        {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unused")
    private boolean getSuspended()
    {
        return this.suspended;
    }

    void setSuspended(boolean value)
    {
        this.suspended = value;
    }

    public EE_DCW_EventDispatcher()
    {
        this.suspended = false;
    }

    /**
     * This function provides a notification mechanism by the
     * EE_DCW_EventDispatcher to the client that the DCW Service Instance given
     * in the argument has events pending
     * 
     * @param pSI
     * @param aborted
     */
    public void needDispatch(EE_DCW_ServiceInstance pSI, boolean aborted)
    {
        boolean bFound = false;
        if (this.dispatchQ.contains(pSI))
        {
            bFound = true;
        }
        if (!bFound)
        {
            this.dispatchQ.add(pSI);
        }
        if (this.suspended == false || aborted == true)
        {
            this.suspended = false;
        }
    }

    /**
     * This function is used to revoke any notification (if any) previously
     * given by needDispatch that the EE_DCW_ServiceInstance (passed in by the
     * argument) had pending events
     * 
     * @param pSI
     */
    public void cancelDispatch(EE_DCW_ServiceInstance pSI)
    {
        removeSI(pSI);
    }

    /**
     * Removes the next pending event and returns its type, the related Service
     * Instance and Operation pointer (if any). The call blocks (i.e. does not
     * return) until an event is available.
     * 
     * @param eventType
     * @param ppdcwsi
     * @param ppop
     * @return
     */
    HRESULT nextEvent(EE_Reference<DCW_Event_Type> eventType,
                      EE_Reference<EE_DCW_ServiceInstance> ppdcwsi,
                      EE_Reference<ISLE_Operation> ppop,
                      int timeoutSec,
                      int timeoutMilliSec)
    {

        if (this.suspended)
        {
            return HRESULT.SLE_E_SUSPENDED;
        }

        EE_DCW_ServiceInstance pSI = null;
        try
        {
            pSI = this.dispatchQ.poll(timeoutSec * 1000 + timeoutMilliSec, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            LOG.log(Level.FINE, "InterruptedException ", e);
        }

        if (pSI != null)
        {
            EE_DCW_Event ptmp = pSI.getEvent();

            if (ptmp == null)
            {
                // only way this returns is with an event.
                return nextEvent(eventType, ppdcwsi, ppop, timeoutSec, timeoutMilliSec);
            }
            else
            {
                eventType.setReference(ptmp.getEventType());
                ppop.setReference(ptmp.getOperation());
                ppdcwsi.setReference(pSI);
                ptmp = null;
                return HRESULT.S_OK;
            }
        }

        return HRESULT.S_OK;
    }

    /**
     * Checks for a pending event and, if present, removes it from the queue,
     * returning its type, Service Instance pointer and Operation pointer if
     * applicable. If no event is present or if the call fails the event Type
     * will be dcwEVT_noEvent
     * 
     * @param eventType
     * @param ppdcwsi
     * @param ppop
     * @return
     */
    public HRESULT pollEvent(EE_Reference<DCW_Event_Type> eventType,
                             EE_Reference<EE_DCW_ServiceInstance> ppdcwsi,
                             EE_Reference<ISLE_Operation> ppop,
                             int timeoutSec,
                             int timeoutMilliSec)
    {
        if (this.suspended)
        {
            return HRESULT.S_FALSE;
        }

        if (this.dispatchQ.isEmpty())
        {
            eventType.setReference(DCW_Event_Type.dcwEVT_noEvent);
            ppdcwsi = null;
            ppop = null;
            return HRESULT.S_FALSE;// no event but not a "failure".
        }
        else
        {
            try
            {
                ppdcwsi.setReference(this.dispatchQ.poll(timeoutSec * 1000 + timeoutMilliSec, TimeUnit.MILLISECONDS));
            }
            catch (InterruptedException e)
            {
                LOG.log(Level.FINE, "InterruptedException ", e);
            }

            EE_DCW_Event ptmp = ppdcwsi.getReference().getEvent();
            if (ptmp != null)
            {
                ppop.setReference(ptmp.getOperation());
                eventType.setReference(ptmp.getEventType());
                ptmp = null;
                return HRESULT.S_OK;
            }
            else
            {
                eventType.setReference(DCW_Event_Type.dcwEVT_noEvent);
                return HRESULT.S_FALSE;
            }
        }
    }

    /**
     * Suspend the reception of operations except PEER ABORT operations for this
     * queue. This applies to all service instances.
     */
    public HRESULT suspend()
    {
        HRESULT res = HRESULT.E_FAIL;
        this.lock.lock();
        if (this.suspended == false)
        {
            this.suspended = true;
            res = HRESULT.S_OK;
        }
        this.lock.unlock();
        return res;
    }

    /**
     * Resume the reception of operations for this queue. This applies to all
     * service instances.
     */
    public HRESULT resume()
    {
        HRESULT res = HRESULT.E_FAIL;
        this.lock.lock();
        if (this.suspended == true)
        {
            this.suspended = false;
            if (!this.dispatchQ.isEmpty())
            {
                this.lock.notifyAll();
            }
            res = HRESULT.S_OK;
        }
        this.lock.unlock();
        return res;
    }

    /**
     * This removes a DCW Service Instance ( if present) from the dispatch
     * queue. This function is private therefore does not perform any locking.
     * 
     * @param pSI
     */
    private void removeSI(EE_DCW_ServiceInstance pSI)
    {
        this.dispatchQ.remove(pSI);
    }

}
