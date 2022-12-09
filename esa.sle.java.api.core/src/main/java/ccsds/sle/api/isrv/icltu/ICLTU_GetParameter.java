package ccsds.sle.api.isrv.icltu;

import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.it.SLE_DeliveryMode;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isrv.icltu.types.CLTU_ClcwGvcId;
import ccsds.sle.api.isrv.icltu.types.CLTU_ClcwPhysicalChannel;
import ccsds.sle.api.isrv.icltu.types.CLTU_GetParameterDiagnostic;
import ccsds.sle.api.isrv.icltu.types.CLTU_NotificationMode;
import ccsds.sle.api.isrv.icltu.types.CLTU_ParameterName;
import ccsds.sle.api.isrv.icltu.types.CLTU_PlopInEffect;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProtocolAbortMode;

public interface ICLTU_GetParameter extends ISLE_ConfirmedOperation
{
    /**
     * Gets the requested parameter.
     * 
     * @return
     */
    CLTU_ParameterName getRequestedParameter();

    /**
     * Gets the returned parameter.
     * 
     * @return
     */
    CLTU_ParameterName getReturnedParameter();

    /**
     * Gets the bit lock required.
     * 
     * @return
     */
    SLE_YesNo getBitLockRequired();

    /**
     * Gets the delivered mode.
     * 
     * @return
     */
    SLE_DeliveryMode getDeliveryMode();

    /**
     * Gets the expected cltu id.
     * 
     * @return
     */
    long getExpectedCltuId();

    /**
     * Gets the expected event invocation id.
     * 
     * @return
     */
    long getExpectedEventInvocationId();

    /**
     * Gets the maximum sldu length.
     * 
     * @return
     */
    long getMaximumSlduLength();

    /**
     * Gets the modulation frequency.
     * 
     * @return
     */
    long getModulationFrequency();

    /**
     * Gets the modulation index.
     * 
     * @return
     */
    int getModulationIndex();

    /**
     * Gets the plop in effect.
     * 
     * @return
     */
    CLTU_PlopInEffect getPlopInEffect();

    /**
     * Gets the reporting cycle.
     * 
     * @return
     */
    long getReportingCycle();

    /**
     * Gets the returning timeout period.
     * 
     * @return
     */
    long getReturnTimeoutPeriod();

    /**
     * Gets the available rf required.
     * 
     * @return
     */
    SLE_YesNo getRfAvailableRequired();

    /**
     * Gets the sub-carrier to bit rate ratio.
     * 
     * @return
     */
    int getSubcarrierToBitRateRatio();

    /**
     * Gets the get diagnostic parameter.
     * 
     * @return
     */
    CLTU_GetParameterDiagnostic getGetParameterDiagnostic();

    /**
     * Sets the requested parameter.
     * 
     * @param name
     */
    void setRequestedParameter(CLTU_ParameterName name);

    /**
     * Sets the required bit lock.
     * 
     * @param yesno
     */
    void setBitLockRequired(SLE_YesNo yesno);

    /**
     * Sets the delivery mode.
     */
    void setDeliveryMode();

    /**
     * Sets the expected cltu id.
     * 
     * @param id
     */
    void setExpectedCltuId(long id);

    /**
     * Sets the expected event invocation id.
     * 
     * @param id
     */
    void setExpectedEventInvocationId(long id);

    /**
     * Sets the maximum sldu length.
     * 
     * @param length
     */
    void setMaximumSlduLength(long length);

    /**
     * Sets the modulation frequency.
     * 
     * @param frequency
     */
    void setModulationFrequency(long frequency);

    /**
     * Sets the modulation index.
     * 
     * @param index
     */
    void setModulationIndex(int index);

    /**
     * Sets the plop in effect.
     * 
     * @param plop
     */
    void setPlopInEffect(CLTU_PlopInEffect plop);

    /**
     * Sets the reporting cycle.
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
     * Sets the available rf requiered.
     * 
     * @param yesno
     */
    void setRfAvailableRequired(SLE_YesNo yesno);

    /**
     * Sets the sub-carrier to bit rate ratio.
     * 
     * @param divisor
     */
    void setSubcarrierToBitRateRatio(int divisor);

    /**
     * Sets the get parameter diagnostic.
     * 
     * @param diagnostic
     */
    void setGetParameterDiagnostic(CLTU_GetParameterDiagnostic diagnostic);

    int getAcquisitionSequenceLength();

    void setAcquisitionSequenceLength(int length);

    int getPlop1IdleSequenceLength();

    void setPlop1IdleSequenceLength(int length);

    CLTU_ProtocolAbortMode getProtocolAbortMode();

    void setProtocolAbortMode(CLTU_ProtocolAbortMode pam);

    CLTU_NotificationMode getNotificationMode();

    void setNotificationMode(CLTU_NotificationMode nm);

    CLTU_ClcwGvcId getClcwGlobalVcid();

    void setClcwGlobalVcid(CLTU_ClcwGvcId cgv);

    CLTU_ClcwPhysicalChannel getClcwPhysicalChannel();

    void setClcwPhysicalChannel(CLTU_ClcwPhysicalChannel cpc);

    long getMinimumDelayTime();

    void setMinimumDelayTime(long mdt);
    
    long getMinimumReportingCycle();
    
    void setMinimumReportingCycle(long mrc);
}
