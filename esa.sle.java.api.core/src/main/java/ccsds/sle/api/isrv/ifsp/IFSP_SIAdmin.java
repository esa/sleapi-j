package ccsds.sle.api.isrv.ifsp;

import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isrv.ifsp.types.FSP_AbsolutePriority;
import ccsds.sle.api.isrv.ifsp.types.FSP_BlockingUsage;
import ccsds.sle.api.isrv.ifsp.types.FSP_ClcwGvcId;
import ccsds.sle.api.isrv.ifsp.types.FSP_ClcwPhysicalChannel;
import ccsds.sle.api.isrv.ifsp.types.FSP_MuxScheme;
import ccsds.sle.api.isrv.ifsp.types.FSP_PermittedTransmissionMode;
import ccsds.sle.api.isrv.ifsp.types.FSP_ProductionStatus;

/**
 * The interface provides write and read access to the FSP-specific service
 * instance configuration parameters. All configuration parameters must be set
 * as part of service instance configuration. When the method ConfigCompleted()
 * is called on the interface ISLE_SIAdmin, the service element checks that all
 * parameters have been set and returns an error when the configuration is not
 * complete.
 * 
 * @version: 1.0, April 2015
 */
public interface IFSP_SIAdmin extends IUnknown
{
    /**
     * Sets the maximum frame length.
     * 
     * @param length
     */
    void setMaximumFrameLength(long length);

    /**
     * Sets the maximum packet length.
     * 
     * @param length
     */
    void setMaximumPacketLength(long length);

    /**
     * Sets the vc mux scheme.
     * 
     * @param scheme
     */
    void setVcMuxScheme(FSP_MuxScheme scheme);

    /**
     * Sets the vc priority list.
     * 
     * @param priorities
     */
    void setVcPriorityList(FSP_AbsolutePriority[] priorities);

    /**
     * Sets the vc polling vector.
     * 
     * @param pvec
     */
    void setVcPollingVector(long[] pvec);

    /**
     * Sets the blocking timeout.
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
     * Sets the directive invocation enabled.
     * 
     * @param yesNo
     */
    void setDirectiveInvocationEnabled(SLE_YesNo yesNo);

    /**
     * Sets the present segment header.
     *
     * @param yesNo
     */
    void setSegmentHeaderPresent(SLE_YesNo yesNo);

    /**
     * Sets the application id list.
     * 
     * @param plist
     */
    void setApIdList(long[] plist);

    /**
     * Sets the map list.
     * 
     * @param plist
     */
    void setMapList(long[] plist);

    /**
     * Sets the virtual channel.
     * 
     * @param id
     */
    void setVirtualChannel(long id);

    /**
     * Sets permitted transmission mode.
     * 
     * @param mode
     */
    void setPermittedTransmissionMode(FSP_PermittedTransmissionMode mode);

    /**
     * Sets the maximum buffer size.
     * 
     * @param size
     */
    void setMaximumBufferSize(long size);

    /**
     * Sets initial production status.
     * 
     * @param status
     */
    void setInitialProductionStatus(FSP_ProductionStatus status);

    /**
     * Sets the initial online directive invocation.
     * 
     * @param yesNo
     */
    void setInitialDirectiveInvocationOnline(SLE_YesNo yesNo);

    /**
     * Gets the maximum frame length.
     * 
     * @return
     */
    long getMaximumFrameLength();

    /**
     * Gets the maximum packet length.
     * 
     * @return
     */
    long getMaximumPacketLength();

    /**
     * Gets the vc mux scheme.
     * 
     * @return
     */
    FSP_MuxScheme getVcMuxScheme();

    /**
     * Gets the vc priority list.
     * 
     * @return
     */
    FSP_AbsolutePriority[] getVcPriorityList();

    /**
     * Gets the vc polling vector.
     * 
     * @return
     */
    long[] getVcPollingVector();

    /**
     * gets the blocking timeout.
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
     * Gets the enabled directive invocation.
     * 
     * @return
     */
    SLE_YesNo getDirectiveInvocationEnabled();

    /**
     * Gets the present segment header.
     * 
     * @return
     */
    SLE_YesNo getSegmentHeaderPresent();

    /**
     * Gets the application id list.
     * 
     * @return
     */
    long[] getApIdList();

    /**
     * Gets the map list.
     * 
     * @return
     */
    long[] getMapList();

    /**
     * Gets the virtual channel.
     * 
     * @return
     */
    long getVirtualChannel();

    /**
     * Gets the permitted transmission mode.
     * 
     * @return
     */
    FSP_PermittedTransmissionMode getPermittedTransmissionMode();

    /**
     * Gets the maximum buffer size.
     * 
     * @return
     */
    long getMaximumBufferSize();

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
     * Gets the FSP config parameter minimum-reporting-cycle in seconds
     * SLES parameter ID 301
     * @since SLES V5
     * @return long
     */
    long getMinimumReportingCycle();
    
    /**
     * Sets the FSP config parameter minimum-reporting-cycle in seconds
     * SLES parameter ID 301
     * @since SLES V5
     */
    void setMinimumReportingCycle(long mrc);
    
    /**
     * Gets the FSP config parameter clcw-global-vcId
     * SLES parameter ID 202
     * @since SLES V5
     */
    public FSP_ClcwGvcId getClcwGvcId();

    /**
     * sets the FSP config parameter clcw-global-vcId
     * SLES parameter ID 202
     * @since SLES V5
     */
	public void setClcwGvcId(FSP_ClcwGvcId gvcId);

	/**
     * Gets the FSP config parameter clcw-Physical-Channel
     * SLES parameter ID 203
     * @since SLES V5
     */
	public FSP_ClcwPhysicalChannel getClcwPhysicalChannel();

	/**
     * sets the FSP config parameter clcw-Physical-Channel
     * SLES parameter ID 203
     * @since SLES V5
     */
	public void setClcwPhysicalChannel(FSP_ClcwPhysicalChannel clcwPhysicalChannel);

	/**
     * Gets the FSP config parameter copCntrFramesRepetition
     * SLES parameter ID 300
     * @since SLES V5
     */
	public int getCopCntrFramesRepetition();
	
	/**
     * Sets the FSP config parameter copCntrFramesRepetition
     * SLES parameter ID 300
     * @since SLES V5
     */
	public void setCopCntrFramesRepetition(int copCntrFramesRepetition);

	/**
     * Gets the FSP config parameter seqCntrFramesRepetition
     * SLES parameter ID 303
     * @since SLES V5
     */
	public int getSeqCntrFramesRepetition();

	/**
     * Sets the FSP config parameter seqCntrFramesRepetition
     * SLES parameter ID 303
     * @since SLES V5
     */
	public void setSeqCntrFramesRepetition(int seqCntrFramesRepetition);

	/**
     * Getter for the FSP config parameter Throw-Event-Operation-Enabled
     * SLES parameter ID 304
     * @since SLES V5
     */
	public SLE_YesNo getThrowEventOperation();

	/**
    * Sets the FSP config parameter Throw-Event-Operation-Enabled
    * SLES parameter ID 304
    * @since SLES V5
    */
	public void setThrowEventOperation(SLE_YesNo throwEventOperation);
	
	/**
     * Getter for the FSP config parameter repetition-limit
     * @since SLES V5
     */
	public int getRepetitionLimit();

	/**
    * Sets the FSP config parameter repetition-limit
    * @since SLES V5
    */
	public void setRepetitionLimit(int repetitionLimit);
}
