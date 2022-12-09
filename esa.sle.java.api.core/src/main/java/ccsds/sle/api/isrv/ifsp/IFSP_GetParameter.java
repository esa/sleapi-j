package ccsds.sle.api.isrv.ifsp;

import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.it.SLE_DeliveryMode;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isrv.ifsp.types.FSP_AbsolutePriority;
import ccsds.sle.api.isrv.ifsp.types.FSP_BlockingUsage;
import ccsds.sle.api.isrv.ifsp.types.FSP_ClcwGvcId;
import ccsds.sle.api.isrv.ifsp.types.FSP_ClcwPhysicalChannel;
import ccsds.sle.api.isrv.ifsp.types.FSP_FopState;
import ccsds.sle.api.isrv.ifsp.types.FSP_GetParameterDiagnostic;
import ccsds.sle.api.isrv.ifsp.types.FSP_MuxScheme;
import ccsds.sle.api.isrv.ifsp.types.FSP_ParameterName;
import ccsds.sle.api.isrv.ifsp.types.FSP_PermittedTransmissionMode;
import ccsds.sle.api.isrv.ifsp.types.FSP_TimeoutType;

/**
 * Inheritance: IUnknown - ISLE_Operation - ISLE_ConfirmedOperation File
 * IFSP_GetParameter.h The interface provides access to the parameters of the
 * confirmed operation FSP GET PARAMETER.
 * 
 * @version: 1.0, October 2015
 */
public interface IFSP_GetParameter extends ISLE_ConfirmedOperation
{
    /**
     * Gets requested parameter.
     * 
     * @return
     */
    FSP_ParameterName getRequestedParameter();

    /**
     * Gets returned parameter.
     * 
     * @return
     */
    FSP_ParameterName getReturnedParameter();

    /**
     * Gets application identifier list.
     * 
     * @return
     */
    long[] getApIdList();

    /**
     * Gets the blocking timeout.
     * 
     * @return
     */
    long getBlockingTimeout();

    /**
     * Gets the blocking usage.
     * 
     * @return
     */
    FSP_BlockingUsage getBlockingUsage();

    /**
     * Gets the delivery mode.
     * 
     * @return
     */
    SLE_DeliveryMode getDeliveryMode();

    /**
     * Gets the enabled directive invocation.
     * 
     * @return
     */
    SLE_YesNo getDirectiveInvocationEnabled();

    /**
     * Gets the online directive invocation.
     * 
     * @return
     */
    SLE_YesNo getDirectiveInvocationOnline();

    /**
     * Gets the expected directive id.
     * 
     * @return
     */
    long getExpectedDirectiveId();

    /**
     * Gets the Expected event invocation id.
     * 
     * @return
     */
    long getExpectedEventInvocationId();

    /**
     * Gets the expected sldu id.
     * 
     * @return
     */
    long getExpectedSlduId();

    /**
     * Gets the fop sliding window.
     * 
     * @return
     */
    long getFopSlidingWindow();

    /**
     * Gets the fop state.
     * 
     * @return
     */
    FSP_FopState getFopState();

    /**
     * Gets the map list.
     * 
     * @return
     */
    long[] getMapList();

    /**
     * Gets the priority list map.
     * 
     * @return
     */
    FSP_AbsolutePriority[] getMapPriorityList();

    /**
     * Gets polling vector map.
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

    /**
     * Gets the max frame length.
     * 
     * @return
     */
    long getMaxFrameLength();

    /**
     * Gets the max packet length.
     * 
     * @return
     */
    long getMaxPacketLength();

    /**
     * Gets the reporting cycle.
     * 
     * @return
     */
    long getReportingCycle();

    /**
     * Gets the return timeout period.
     * 
     * @return
     */
    long getReturnTimeoutPeriod();

    /**
     * Gets the present segment header.
     * 
     * @return
     */
    SLE_YesNo getSegmentHeaderPresent();

    /**
     * Gets the timeout type.
     * 
     * @return
     */
    FSP_TimeoutType getTimeoutType();

    /**
     * Gets initial timer.
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
     * Gets the Vc priority list.
     * 
     * @return
     */
    FSP_AbsolutePriority[] getVcPriorityList();

    /**
     * Gets the Vc pooling vector.
     * 
     * @return
     */
    long[] getVcPollingVector();

    /**
     * Gets the Vc mux Scheme.
     * 
     * @return
     */
    FSP_MuxScheme getVcMuxScheme();

    /**
     * Gets the virtual channel.
     * 
     * @return
     */
    long getVirtualChannel();

    /**
     * Gets the parameter diagnostic.
     * 
     * @return
     */
    FSP_GetParameterDiagnostic getGetParameterDiagnostic();

    /**
     * Sets the requested parameter
     * 
     * @param name
     */
    void setRequestedParameter(FSP_ParameterName name);

    /**
     * Sets the application id list.
     * 
     * @param plist
     */
    void setApIdList(long[] plist);

    /**
     * Puts the application id list.
     * 
     * @param plist
     */
    void putApIdList(long[] plist);

    /**
     * Sets blocking timeout.
     * 
     * @param timeout
     */
    void setBlockingTimeout(long timeout);

    /**
     * Sets the blocking usage.
     * 
     * @param usage
     */
    void setBlockingUsage(FSP_BlockingUsage usage);

    /**
     * Sets the delivery mode.
     */
    void setDeliveryMode();

    /**
     * Sets the directive invocation enabled.
     * 
     * @param yesNo
     */
    void setDirectiveInvocationEnabled(SLE_YesNo yesNo);

    /**
     * Sets the directive invocation online.
     * 
     * @param yesNo
     */
    void setDirectiveInvocationOnline(SLE_YesNo yesNo);

    /**
     * Sets the expected directive id.
     * 
     * @param id
     */
    void setExpectedDirectiveId(long id);

    /**
     * Sets the expected event invocation id.
     * 
     * @param id
     */
    void setExpectedEventInvocationId(long id);

    /**
     * Sets the expected sldu id.
     * 
     * @param id
     */
    void setExpectedSlduId(long id);

    /**
     * Sets the fop sliding window.
     * 
     * @param window
     */
    void setFopSlidingWindow(long window);

    /**
     * Sets the fop state.
     * 
     * @param state
     */
    void setFopState(FSP_FopState state);

    /**
     * Sets the map list.
     * 
     * @param plist
     */
    void setMapList(long[] plist);

    /**
     * Puts the map list.
     * 
     * @param plist
     */
    void putMapList(long[] plist);

    /**
     * Sets the map priority list.
     * 
     * @param priorities
     */
    void setMapPriorityList(FSP_AbsolutePriority[] priorities);

    /**
     * Puts the map priority list.
     * 
     * @param priorities
     */
    void putMapPriorityList(FSP_AbsolutePriority[] priorities);

    /**
     * Sets the map pooling vector.
     * 
     * @param pvec
     */
    void setMapPollingVector(long[] pvec);

    /**
     * Puts the map polling vector.
     * 
     * @param pvec
     */
    void putMapPollingVector(long[] pvec);

    /**
     * Sets the map mux scheme.
     * 
     * @param scheme
     */
    void setMapMuxScheme(FSP_MuxScheme scheme);

    /**
     * Sets the max frame lenght.
     * 
     * @param length
     */
    void setMaxFrameLength(long length);

    /**
     * Sets the max packet length.
     * 
     * @param length
     */
    void setMaxPacketLength(long length);

    /**
     * Sets reporting cycle.
     * 
     * @param cycle
     */
    void setReportingCycle(long cycle);

    /**
     * Sets the returning timeout period.
     * 
     * @param period
     */
    void setReturnTimeoutPeriod(long period);

    /**
     * Sets the present segment header.
     * 
     * @param yesNo
     */
    void setSegmentHeaderPresent(SLE_YesNo yesNo);

    /**
     * Sets the timeout type.
     * 
     * @param type
     */
    void setTimeoutType(FSP_TimeoutType type);

    /**
     * Sets the initial timer.
     * 
     * @param timeout
     */
    void setTimerInitial(long timeout);

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
     * Sets the vc priority list.
     * 
     * @param priorities
     */
    void setVcPriorityList(FSP_AbsolutePriority[] priorities);

    /**
     * Puts the vc priority list.
     * 
     * @param priorities
     */
    void putVcPriorityList(FSP_AbsolutePriority[] priorities);

    /**
     * Sets the vc polling vector.
     * 
     * @param pvec
     */
    void setVcPollingVector(long[] pvec);

    /**
     * Puts the vc polling vector.
     * 
     * @param pvec
     */
    void putVcPollingVector(long[] pvec);

    /**
     * Sets the vc mux scheme.
     * 
     * @param scheme
     */
    void setVcMuxScheme(FSP_MuxScheme scheme);

    /**
     * Sets the virtual channel.
     * 
     * @param id
     */
    void setVirtualChannel(long id);

    /**
     * Sets the get parameter diagnostic.
     * 
     * @param diagnostic
     */
    void setGetParameterDiagnostic(FSP_GetParameterDiagnostic diagnostic);

    /**
     * Sets the permitted transmission mode.
     * 
     * @param mode
     */
    void setPermittedTransmissionMode(FSP_PermittedTransmissionMode mode);

    /**
     * Gets the permitted transmission mode.
     * 
     * @return
     */
    FSP_PermittedTransmissionMode getPermittedTransmissionMode();

    /**
     * Gets the required bit lock.
     * 
     * @return
     */
    SLE_YesNo getBitLockRequired();

    /**
     * Gets the available rf required.
     * 
     * @return
     */
    SLE_YesNo getRfAvailableRequired();

    /**
     * Sets the required bit lock.
     * 
     * @param yesNo
     */
    void setBitLockRequired(SLE_YesNo yesNo);

    /**
     * Sets the available rf required.
     * 
     * @param yesNo
     */
    void setRfAvailableRequired(SLE_YesNo yesNo);
    
    /**
     * returns the clcwGlobalVcid for FSP (added for SLES V5 support)
     * @return
     */
    public  FSP_ClcwGvcId getClcwGlobalVcid();
    
    /**
     * returns the clcwphysicalChannel for FSP (added for SLES V5 support)
     * @return
     */
    public  FSP_ClcwPhysicalChannel getClcwPhysicalChannel();

    /**
     * sets the clcwGlobalVcid for FSP (added for SLES V5 support)
     */
    public void setClcwGlobalVcid(FSP_ClcwGvcId clcwGvcId);
    
    /**
     * sets the clcwphysicalChannel for FSP (added for SLES V5 support)
     */
    public void setClcwPhysicalChannel(FSP_ClcwPhysicalChannel pch);


    /**
     * sets the copCntrFramesRepetition for FSP (added for SLES V5 support)
     * @param counter
     */
    public void setCopCntrFramesRepetition(long counter);
    
    public void setMinReportingCycle(long mrc);
    
    public void setSeqCntrFramesRepetition(long scfr);
    
    public void setThrowEventOperation(SLE_YesNo yesNo);
    

    /**
     * @return returns the copCntrFramesRepetition for FSP (added for SLES V5 support)
     */
    public long getCopCntrFramesRepetition();
    
    /**
     * @return returns the minReportingCycle for FSP (added for SLES V5 support)
     */
    public long getMinReportingCycle();
    
    /**
     * @return returns the seqCntrFramesRepetition for FSP (added for SLES V5 support)
     */
    public long getSeqCntrFramesRepetition();
    
    /**
     * @return returns the throwEventOperation for FSP (added for SLES V5 support)
     */
    public SLE_YesNo getThrowEventOperation();
}
