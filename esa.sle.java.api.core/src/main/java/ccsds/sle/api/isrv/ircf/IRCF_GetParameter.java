package ccsds.sle.api.isrv.ircf;

import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isrv.ircf.types.RCF_DeliveryMode;
import ccsds.sle.api.isrv.ircf.types.RCF_GetParameterDiagnostic;
import ccsds.sle.api.isrv.ircf.types.RCF_Gvcid;
import ccsds.sle.api.isrv.ircf.types.RCF_ParameterName;

public interface IRCF_GetParameter extends ISLE_ConfirmedOperation
{
    /**
     * Gets the requested parameter.
     * 
     * @return
     */
    RCF_ParameterName getRequestedParameter();

    /**
     * Gets the returned parameter.
     * 
     * @return
     */
    RCF_ParameterName getReturnedParameter();

    /**
     * Gets the delivery mode.
     * 
     * @return
     */
    RCF_DeliveryMode getDeliveryMode();

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
     * Gets the requested gvcid.
     * 
     * @return
     */
    RCF_Gvcid getRequestedGvcid();

    /**
     * Gets the permitted gvcid set.
     * 
     * @return
     */
    RCF_Gvcid[] getPermittedGvcidSet();

    /**
     * Removes permitted gvcid set.
     * 
     * @return
     */
    RCF_Gvcid[] removePermittedGvcidSet();

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
     * Gets the minimum reporting cycle in seconds (SLE para ID = 301).
     * 
     * @return type long in sec.
     */
    long getMinimumReportingCycle();

    /**
     * Gets the parameter diagnostic.
     * 
     * @return
     */
    RCF_GetParameterDiagnostic getGetParameterDiagnostic();

    /**
     * Sets the requested parameter.
     * 
     * @param name
     */
    void setRequestedParameter(RCF_ParameterName name);

    /**
     * Sets the delivery mode.
     * 
     * @param mode
     */
    void setDeliveryMode(RCF_DeliveryMode mode);

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
     * Sets the requested gvcid.
     * 
     * @param id
     */
    void setRequestedGvcid(RCF_Gvcid id);

    /**
     * Puts the requested gvcid.
     * 
     * @param pid
     */
    void putRequestedGvcid(RCF_Gvcid pid);

    /**
     * Sets the permitted gvcid set.
     * 
     * @param idList
     */
    void setPermittedGvcidSet(RCF_Gvcid[] idList);

    /**
     * Puts the permitted gvcid set.
     * 
     * @param idList
     */
    void putPermittedGvcidSet(RCF_Gvcid[] idList);

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
    void setGetParameterDiagnostic(RCF_GetParameterDiagnostic diagostic);

    /**
     * Sets the minimum reporting cycle in seconds (SLE para ID = 301).
     * 
     * @param mrc as type long.
     */
    void setMinimumReportingCycle(long mrc);
}
