package ccsds.sle.api.isrv.icltu;

import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isrv.icltu.types.CLTU_ClcwGvcId;
import ccsds.sle.api.isrv.icltu.types.CLTU_ClcwPhysicalChannel;
import ccsds.sle.api.isrv.icltu.types.CLTU_NotificationMode;
import ccsds.sle.api.isrv.icltu.types.CLTU_PlopInEffect;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProductionStatus;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProtocolAbortMode;
import ccsds.sle.api.isrv.icltu.types.CLTU_UplinkStatus;

public interface ICLTU_SIAdmin extends IUnknown
{
    /**
     * Sets bit lock required.
     * 
     * @param yesno
     */
    void setBitLockRequired(SLE_YesNo yesno);

    /**
     * Sets maximum sldu length.
     * 
     * @param length
     */
    void setMaximumSlduLength(long length);

    /**
     * Sets modulation frequency.
     * 
     * @param frequency
     */
    void setModulationFrequency(long frequency);

    /**
     * Sets modulation index.
     * 
     * @param index
     */
    void setModulationIndex(int index);

    /**
     * Sets plop in effect.
     * 
     * @param plop
     */
    void setPlopInEffect(CLTU_PlopInEffect plop);

    /**
     * Sets Rf available required.
     * 
     * @param yesno
     */
    void setRfAvailableRequired(SLE_YesNo yesno);

    /**
     * Sets sub carrier to bit rate ratio.
     * 
     * @param divisor
     */
    void setSubcarrierToBitRateRatio(int divisor);

    /**
     * Sets maximum buffer size.
     * 
     * @param size
     */
    void setMaximumBufferSize(long size);

    /**
     * Sets initial production status.
     * 
     * @param status
     */
    void setInitialProductionStatus(CLTU_ProductionStatus status);

    /**
     * Sets initial uplink status.
     * 
     * @param status
     */
    void setInitialUplinkStatus(CLTU_UplinkStatus status);

    /**
     * Sets notification mode
     * 
     * @param mode
     */
    void setNotificationMode(CLTU_NotificationMode mode);

    /**
     * Gets bit lock required.
     * 
     * @return
     */
    SLE_YesNo getBitLockRequired();

    /**
     * Gets maximum sldu length.
     * 
     * @return
     */
    long getMaximumSlduLength();

    /**
     * Gets modulation frequency.
     * 
     * @return
     */
    long getModulationFrequency();

    /**
     * Gets modulation index.
     * 
     * @return
     */
    int getModulationIndex();

    /**
     * Gets plop in effect.
     * 
     * @return
     */
    CLTU_PlopInEffect getPlopInEffect();

    /**
     * Gets RF available required.
     * 
     * @return
     */
    SLE_YesNo getRfAvailableRequired();

    /**
     * Gets sub carrier to bit rate ratio
     * 
     * @return
     */
    int getSubcarrierToBitRateRatio();

    /**
     * Gets maximum buffer size()
     * 
     * @return
     */
    long getMaximumBufferSize();

    /**
     * Gets notification mode.
     * 
     * @return
     */
    CLTU_NotificationMode getNotificationMode();

    void setAcquisitionSequenceLength(int length);

    int getAcquisitionSequenceLength();

    void setPlop1IdleSequenceLength(int length);

    int getPlop1IdleSequenceLength();

    void setProtocolAbortMode(CLTU_ProtocolAbortMode pam);

    CLTU_ProtocolAbortMode getProtocolAbortMode();

    void setClcwGlobalVcid(CLTU_ClcwGvcId cgv);

    CLTU_ClcwGvcId getClcwGlobalVcid();

    void setClcwPhysicalChannel(CLTU_ClcwPhysicalChannel cgv);

    CLTU_ClcwPhysicalChannel getClcwPhysicalChannel();

    void setMinimumDelayTime(long mdt);

    long getMinimumDelayTime();
    
    /**
     * Sets the CLTU config parameter minimum-reporting-cycle in seconds
     * SLES parameter ID 301
     * @since SLES V5
     */
    void setMinimumReportingCycle(long mrc);
    
    /**
     * Gets the CLTU config parameter minimum-reporting-cycle in seconds
     * SLES parameter ID 301
     * @since SLES V5
     * @return long
     */
    long getMinimumReportingCycle();
}
