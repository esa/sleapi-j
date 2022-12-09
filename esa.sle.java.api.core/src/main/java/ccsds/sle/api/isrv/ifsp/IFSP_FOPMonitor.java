package ccsds.sle.api.isrv.ifsp;

import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isrv.ifsp.types.FSP_AbsolutePriority;
import ccsds.sle.api.isrv.ifsp.types.FSP_FopState;
import ccsds.sle.api.isrv.ifsp.types.FSP_MuxScheme;
import ccsds.sle.api.isrv.ifsp.types.FSP_TimeoutType;

/**
 * The interface provides access to the FSP parameters related to the FOP
 * machine of the VC on which the service instance operates including -
 * parameters controlling operation the FOP machine; and - parameters monitoring
 * the FOP state and variables. The API service instance uses these parameters
 * only to respond to GET-PARAMETER invocations. All parameters must be set when
 * the service instance is being configured before the method ConfigCompleted()
 * is called on the interface ISLE_SIAdmin. The service instance verifies
 * completeness and consistency of the parameters within the method
 * ConfigCompleted(). During the lifetime of the service instance, FOP related
 * parameters must be updated whenever they change. Changes might occur due to
 * directives invoked by a service user on the same or on a different service
 * instance, due to events detected by the FOP machine, or due to management
 * action. In order to ensure that the service instance always reports the
 * correct parameter value, updates must be reported independent of the service
 * instance state. The parameters 'map multiplexing scheme' and 'map
 * multiplexing control' are included in this interface because 'map
 * multiplexing control' can be modified by the service user via a directive.
 * 
 * @version: 1.0, October 2015
 */

public interface IFSP_FOPMonitor extends IUnknown
{
    /**
     * Sets the fop sliding window.
     * 
     * @param width
     */
    void setFopSlidingWindow(long width);

    /**
     * Sets the timeout.
     * 
     * @param type
     */
    void setTimeoutType(FSP_TimeoutType type);

    /**
     * Sets the initial timer.
     * 
     * @param timer
     */
    void setTimerInitial(long timer);

    /**
     * Sets the transmission limit.
     * 
     * @param limit
     */
    void setTransmissionLimit(long limit);

    /**
     * Sets the transmitter frame sequence number.
     * 
     * @param number
     */
    void setTransmitterFrameSequenceNumber(long number);

    /**
     * Sets the fop state.
     * 
     * @param state
     */
    void setFopState(FSP_FopState state);

    /**
     * Sets the map priority list.
     * 
     * @param priorities
     */
    void setMapPriorityList(FSP_AbsolutePriority[] priorities);

    /**
     * Sets the polling vector map.
     * 
     * @param pvec
     */
    void setMapPollingVector(long[] pvec);

    /**
     * Sets the mux scheme map.
     * 
     * @param scheme
     */
    void setMapMuxScheme(FSP_MuxScheme scheme);

    /**
     * Gets the fop sliding window width.
     * 
     * @return
     */
    long getFopSlidingWindowWidth();

    /**
     * Gets the timeout type.
     * 
     * @return
     */
    FSP_TimeoutType getTimeoutType();

    /**
     * Gets the initial timer.
     * 
     * @return
     */
    long getTimerInitial();

    /**
     * Gets the transmission limit.
     * 
     * @return
     */
    long getTransmissionLimit();

    /**
     * Gets the transmitter frame sequence number.
     * 
     * @return
     */
    long getTransmitterFrameSequenceNumber();

    /**
     * Gets the fop state.
     * 
     * @return
     */
    FSP_FopState getFopState();

    /**
     * Gets the priority list map.
     * 
     * @return
     */
    FSP_AbsolutePriority[] getMapPriorityList();

    /**
     * Gets the polling vector map.
     * 
     * @return
     */
    long[] getMapPollingVector();

    /**
     * Gets the mux scheme map.
     * 
     * @return
     */
    FSP_MuxScheme getMapMuxScheme();

}
