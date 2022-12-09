package ccsds.sle.api.isrv.ifsp;

import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isrv.ifsp.types.FSP_AbsolutePriority;
import ccsds.sle.api.isrv.ifsp.types.FSP_Directive;
import ccsds.sle.api.isrv.ifsp.types.FSP_DirectiveTimeoutType;
import ccsds.sle.api.isrv.ifsp.types.FSP_InvokeDirectiveDiagnostic;

/**
 * The interface provides access to the parameters of the confirmed operation
 * FSP INVOKE DIRECTIVE.
 * 
 * @version: 1.0, October 2015
 */
public interface IFSP_InvokeDirective extends ISLE_ConfirmedOperation
{
    /**
     * Gets the directive id.
     * 
     * @return
     */
    long getDirectiveId();

    /**
     * Gets the expected directive id.
     * 
     * @return
     */
    long getExpectedDirectiveId();

    /**
     * Gets the directive.
     * 
     * @return
     */
    FSP_Directive getDirective();

    /**
     * Gets the vr.
     * 
     * @return
     */
    long getVR();

    /**
     * Gets the vs.
     * 
     * @return
     */
    long getVS();

    /**
     * Gets the fop sliding window width.
     * 
     * @return
     */
    long getFopSlidingWindowWidth();

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
     * Gets the timeout type.
     * 
     * @return
     */
    FSP_DirectiveTimeoutType getTimeoutType();

    /**
     * Gets the priority.
     * 
     * @return
     */
    FSP_AbsolutePriority[] getPriority();

    /**
     * Gets the polling vector.
     * 
     * @return
     */
    long[] getPollingVector();

    /**
     * Gets the invoked directive diagnostic.
     * 
     * @return
     */
    FSP_InvokeDirectiveDiagnostic getInvokeDirectiveDiagnostic();

    /**
     * Sets the directive id.
     * 
     * @param id
     */
    void setDirectiveId(long id);

    /**
     * Sets the expected directive id.
     * 
     * @param id
     */
    void setExpectedDirectiveId(long id);

    /**
     * Sets the initiate ad without clcw.
     */
    void setInitiateADwithoutCLCW();

    /**
     * Sets initiate ad with clcw.
     */
    void setInitiateADwithCLCW();

    /**
     * Sets initiate ad with unlock.
     */
    void setInitiateADwithUnlock();

    /**
     * Sets initiate ad with set vr.
     * 
     * @param vr
     */
    void setInitiateADwithSetVR(long vr);

    /**
     * Sets the terminate ad.
     */
    void setTerminateAD();

    /**
     * Sets the resume ad.
     */
    void setResumeAD();

    /**
     * Sets the vs.
     * 
     * @param vs
     */
    void setVS(long vs);

    /**
     * Sets the fop sliding window.
     * 
     * @param width
     */
    void setFopSlidingWindow(long width);

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
     * Sets the timeout type.
     * 
     * @param type
     */
    void setTimeoutType(FSP_DirectiveTimeoutType type);

    /**
     * Sets the abort vc.
     */
    void setAbortVC();

    /**
     * Sets the modify priority list.
     * 
     * @param plist
     */
    void setModifyMapPriorityList(FSP_AbsolutePriority[] plist);

    /**
     * Sets the map polling vector.
     * 
     * @param pvec
     */
    void setModifyMapPollingVector(long[] pvec);

    /**
     * Sets the invoked directive diagnostic.
     * 
     * @param diag
     */
    void setInvokeDirectiveDiagnostic(FSP_InvokeDirectiveDiagnostic diag);

}
