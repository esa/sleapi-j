package ccsds.sle.api.isle.icc;

import ccsds.sle.api.isle.iscm.IUnknown;

/**
 * The timeout processor is called when a timer expires.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_TimeoutProcessor extends IUnknown
{
    /**
     * Invoked when the timer expires
     * 
     * @param timer the elapsed timer
     * @param invocationId the invocation identifier
     */
    void processTimeout(Object timer, int invocationId);

    /**
     * Invoked when a timer can no longer provide timer notifications
     * 
     * @param timer the elapsed timer
     */
    void handlerAbort(Object timer);
}
