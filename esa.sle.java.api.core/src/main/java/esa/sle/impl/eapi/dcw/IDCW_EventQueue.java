package esa.sle.impl.eapi.dcw;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;
import esa.sle.impl.eapi.dcw.type.DCW_Event_Type;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * This interface is used to detect and fetch events from the DCW. The client
 * reads and removes the event using either the nextEvent (blocking) or
 * pollEvent (non-blocking) method. The client uses the Event Type to determine
 * which up-call was received. If events are occurring faster than the
 * application can process them they will accumulate in the queue, of a Service
 * Instance. If the configured maximum queue-size is exceeded then the Service
 * Instance adding the event will block until there is space. This in turn will
 * cause the association to block which, if blocked for long enough, would
 * time-out leading to peer abort by the peer application. Note that, depending
 * on the implementation of the SLE-API, it is possible that all Service
 * Instances will block. The flushQueue method must be used immediately after
 * the client has invoked a PEER-ABORT Operation on a Service Instance to remove
 * any events pending for that Service Instance. The EventType identifies the
 * up-call received. dcwEVT_no Event represents absence of any event.
 */

public interface IDCW_EventQueue extends IUnknown
{

    /**
     * @param eventType type of event
     * @param psi reference to the Service Instance related to the event
     * @param ppop reference to the interface of the Operation related to the
     *            event, if applicable with eventType, else NULL
     * @param timeoutSec
     * @param timeoutMilliSec timeout specifies the maximum time for method to
     *            complete
     * @return
     * @throws SleApiException
     */

    public <T extends ISLE_Operation> T nextEvent(EE_Reference<DCW_Event_Type> eventType,
                                                  EE_Reference<IUnknown> psi,
                                                  EE_Reference<ISLE_Operation> ppop,
                                                  int timeoutSec,
                                                  int timeoutMilliSec) throws SleApiException;

    /**
     * @param eventType type of event
     * @param psi reference to the Service Instance related to the event
     * @param ppop reference to the interface of the Operation related to the
     *            event, if applicable with eventType, else NULL
     * @param timeoutSec
     * @param timeoutMilliSec timeout specifies the maximum time for method to
     *            complete
     * @return
     * @throws SleApiException
     */
    public <T extends ISLE_Operation> T pollEvent(EE_Reference<DCW_Event_Type> eventType,
                                                  EE_Reference<IUnknown> psi,
                                                  EE_Reference<ISLE_Operation> ppop,
                                                  int timeoutSec,
                                                  int timeoutMilliSec) throws SleApiException;

    /**
     * Flushes the queue.
     * 
     * @param psi reference to the Service Instance
     * @throws SleApiException
     */
    public void flushQueue(IUnknown psi) throws SleApiException;

    /**
     * Suspends the activity of working with the queue of the service instance.
     * 
     * @throws SleApiException
     */
    public void suspend() throws SleApiException;

    /**
     * Resumes the activity.
     * 
     * @throws SleApiException
     */
    public void resume() throws SleApiException;

}
