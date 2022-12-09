package ccsds.sle.api.isrv.iraf;

import java.util.List;

import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isrv.iraf.types.RAF_DeliveryMode;
import ccsds.sle.api.isrv.iraf.types.RAF_GetParameterDiagnostic;
import ccsds.sle.api.isrv.iraf.types.RAF_ParFrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_ParameterName;
import ccsds.sle.api.isrv.iraf.types.RAF_RequestedFrameQuality;

public interface IRAF_GetParameter extends ISLE_ConfirmedOperation
{
    /**
     * Gets the requested parameter.
     * 
     * @return
     */
    RAF_ParameterName getRequestedParameter();

    /**
     * Gets the returned parameter.
     * 
     * @return
     */
    RAF_ParameterName getReturnedParameter();

    /**
     * Gets the delivery mode.
     * 
     * @return
     */
    RAF_DeliveryMode getDeliveryMode();

    /**
     * Gets the requested frame quality.
     * 
     * @return
     */
    RAF_ParFrameQuality getRequestedFrameQuality();

    /**
     * Gets the latency limit.
     * 
     * @return
     */
    int getLatencyLimit();

    /**
     * Gets the transfer buffer size.
     * 
     * @return
     */
    long getTransferBufferSize();

    /**
     * Gets the reporting cycle.
     * 
     * @return
     */
    long getReportingCycle();

    /**
     * Gets the returned timeout period.
     * 
     * @return
     */
    long getReturnTimeoutPeriod();

    /**
     * Gets the parameter diagnostic.
     * 
     * @return
     */
    RAF_GetParameterDiagnostic getGetParameterDiagnostic();

    /**
     * Sets the requested parameter.
     * 
     * @param name
     */
    void setRequestedParameter(RAF_ParameterName name);

    /**
     * Sets the delivery mode.
     * 
     * @param mode
     */
    void setDeliveryMode(RAF_DeliveryMode mode);

    /**
     * Sets the delivery mode.
     * 
     * @param quality
     */
    void setRequestedFrameQuality(RAF_ParFrameQuality quality);

    /**
     * Sets the latency limit.
     * 
     * @param limit
     */
    void setLatencyLimit(int limit);

    /**
     * Sets the transfer buffer size.
     * 
     * @param size
     */
    void setTransferBufferSize(long size);

    /**
     * Sets the reporting cycle.
     * 
     * @param cycle
     */
    void setReportingCycle(long cycle);

    /**
     * Sets the return timeout period.
     * 
     * @param period
     */
    void setReturnTimeoutPeriod(long period);

    /**
     * Sets the get parameter diagnostic.
     * 
     * @param diagostic
     */
    void setGetParameterDiagnostic(RAF_GetParameterDiagnostic diagostic);
    
    /**
     * Gets the misinum reporting cycle
     * @return the mrc in seconds as type long
     */
    long getMinimumReportingCycle();
    
    /**
     * Sets the mininum reporting cycle
     * @param mrc in seconds
     */
    void setMinimumReportingCycle(long mrc);
    
    /**
     * Gets the permitted frame quality 
     * @return list of permitted frame quality
     */
    List<RAF_RequestedFrameQuality> getPermittedFrameQuality();
    
    /**
     * Sets the permitted frame quality by taking an array of
     * RAF_ParFrameQuality[] and setting the internal list.
     * @param setReqQualFrames type of RAF_ParFrameQuality
     */
    void setPermittedFrameQuality(RAF_ParFrameQuality[] setReqQualFrames);
}
