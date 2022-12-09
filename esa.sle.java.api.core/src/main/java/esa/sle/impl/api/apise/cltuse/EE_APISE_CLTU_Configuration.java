package esa.sle.impl.api.apise.cltuse;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isrv.icltu.ICLTU_GetParameter;
import ccsds.sle.api.isrv.icltu.types.CLTU_ClcwGvcId;
import ccsds.sle.api.isrv.icltu.types.CLTU_ClcwPhysicalChannel;
import ccsds.sle.api.isrv.icltu.types.CLTU_NotificationMode;
import ccsds.sle.api.isrv.icltu.types.CLTU_ParameterName;
import ccsds.sle.api.isrv.icltu.types.CLTU_PlopInEffect;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProtocolAbortMode;
import esa.sle.impl.api.apise.slese.EE_APISE_PConfiguration;

/**
 * The class holds all CLTU configuration parameters. Note that the accessor and
 * modifier functions get_<Attribute> and set<Attribute> are generated
 * automatically for the public interface.
 */
public class EE_APISE_CLTU_Configuration extends EE_APISE_PConfiguration
{
    /**
     * The parameter indicating whether bit lock is required to set the
     * production status to waiting.
     */
    private SLE_YesNo bitLockRequired = SLE_YesNo.sleYN_invalid;

    /**
     * The maximum size in byte of a CLTU supported by the provider.
     */
    private long maxCltuLength = 0;

    /**
     * The value of the modulation frquency parameter.
     */
    private long modulationFrequency = 0;

    /**
     * The modulation index used by the provider.
     */
    private int modulationIndex = 0;

    /**
     * The parameter indicating whether PLOP-1 or PLOP-2 is used.
     */
    private CLTU_PlopInEffect plopInEffect = CLTU_PlopInEffect.cltuPIE_invalid;

    /**
     * The parameter indicating whether RF lock is required to set the
     * production state to waiting.
     */
    private SLE_YesNo rfAvailRequired = SLE_YesNo.sleYN_invalid;

    /**
     * The parameter 'sub-carrier to bit-rate ratio'
     */
    private int scToBitrateRatio = 0;

    /**
     * The maximum size in byte of the CLTU buffer supported by the provider.
     */
    private long maxBufferSize = 0;

    /**
     * The parameter indicating if immediate or deferred notification mode is in
     * effect.
     */
    private CLTU_NotificationMode notificationMode = CLTU_NotificationMode.cltuNM_invalid;

    private int acquisitionSequenceLength;

    private int plop1IdleSequenceLength;

    private CLTU_ProtocolAbortMode protocolAbortMode;

    private CLTU_ClcwGvcId clcwGlobalVcid;

    private CLTU_ClcwPhysicalChannel clcwPhysicalChannel;

    private long minimumDelayTime;

    public EE_APISE_CLTU_Configuration()
    {
        this.bitLockRequired = SLE_YesNo.sleYN_invalid;
        this.maxCltuLength = 0;
        this.modulationFrequency = 0;
        this.modulationIndex = 0;
        this.plopInEffect = CLTU_PlopInEffect.cltuPIE_invalid;
        this.rfAvailRequired = SLE_YesNo.sleYN_invalid;
        this.scToBitrateRatio = 0;
        this.maxBufferSize = 0;
        this.notificationMode = CLTU_NotificationMode.cltuNM_invalid;
        this.acquisitionSequenceLength = 65535;
        this.plop1IdleSequenceLength = 65535;
        this.protocolAbortMode = CLTU_ProtocolAbortMode.cltuPAM_invalid;
        this.clcwGlobalVcid = null;
        this.clcwPhysicalChannel = null;
        this.minimumDelayTime = 0;
    }

    /**
     * Initializes the supplied CLTU-GET-PARAMETER-operation with the current
     * status information data.
     */
    public HRESULT setUpGetParameter(final ICLTU_GetParameter prm)
    {
        CLTU_ParameterName pname = prm.getRequestedParameter();

        switch (pname)
        {
        case cltuPN_bitLockRequired:
        {
            prm.setBitLockRequired(this.bitLockRequired);
            return HRESULT.S_OK;
        }
        case cltuPN_deliveryMode:
        {
            prm.setDeliveryMode();
            return HRESULT.S_OK;
        }
        case cltuPN_maximumSlduLength:
        {
            prm.setMaximumSlduLength(this.maxCltuLength);
            return HRESULT.S_OK;
        }
        case cltuPN_modulationFrequency:
        {
            prm.setModulationFrequency(this.modulationFrequency);
            return HRESULT.S_OK;
        }
        case cltuPN_modulationIndex:
        {
            prm.setModulationIndex(this.modulationIndex);
            return HRESULT.S_OK;
        }
        case cltuPN_plopInEffect:
        {
            prm.setPlopInEffect(this.plopInEffect);
            return HRESULT.S_OK;
        }
        case cltuPN_rfAvailableRequired:
        {
            prm.setRfAvailableRequired(this.rfAvailRequired);
            return HRESULT.S_OK;
        }
        case cltuPN_subcarrierToBitRateRatio:
        {
            prm.setSubcarrierToBitRateRatio(this.scToBitrateRatio);
            return HRESULT.S_OK;
        }
        case cltuPN_acquisitionSequenceLength:
        {
            prm.setAcquisitionSequenceLength(this.acquisitionSequenceLength);
            return HRESULT.S_OK;
        }
        case cltuPN_plop1IdleSequenceLength:
        {
            prm.setPlop1IdleSequenceLength(this.plop1IdleSequenceLength);
            return HRESULT.S_OK;
        }
        case cltuPN_protocolAbortMode:
        {
            prm.setProtocolAbortMode(this.protocolAbortMode);
            return HRESULT.S_OK;
        }
        case cltuPN_clcwGlobalVcid:
        {
            prm.setClcwGlobalVcid(this.clcwGlobalVcid);
            return HRESULT.S_OK;
        }
        case cltuPN_clcwPhysicalChannel:
        {
            prm.setClcwPhysicalChannel(this.clcwPhysicalChannel);
            return HRESULT.S_OK;
        }
        case cltuPN_minimumDelayTime:
        {
            prm.setMinimumDelayTime(this.minimumDelayTime);
            return HRESULT.S_OK;
        }
        case cltuPN_notificationMode:
        {
            prm.setNotificationMode(this.notificationMode);
            return HRESULT.S_OK;
        }
        case cltuPN_minimumReportingCycle:
        {
        	prm.setMinimumReportingCycle(this.getMinimumReportingCycle());
        	return HRESULT.S_OK;
        }
        default:
            return HRESULT.SLE_E_UNKNOWN;
        }

    }

    public SLE_YesNo getBitLockRequired()
    {
        return this.bitLockRequired;
    }

    public void setBitLockRequired(SLE_YesNo bitLockRequired)
    {
        this.bitLockRequired = bitLockRequired;
    }

    public long getMaxCltuLength()
    {
        return this.maxCltuLength;
    }

    public void setMaxCltuLength(long maxCltuLength)
    {
        this.maxCltuLength = maxCltuLength;
    }

    public long getModulationFrequency()
    {
        return this.modulationFrequency;
    }

    public void setModulationFrequency(long modulationFrequency)
    {
        this.modulationFrequency = modulationFrequency;
    }

    public int getModulationIndex()
    {
        return this.modulationIndex;
    }

    public void setModulationIndex(int modulationIndex)
    {
        this.modulationIndex = modulationIndex;
    }

    public CLTU_PlopInEffect getPlopInEffect()
    {
        return this.plopInEffect;
    }

    public void setPlopInEffect(CLTU_PlopInEffect plopInEffect)
    {
        this.plopInEffect = plopInEffect;
    }

    public SLE_YesNo getRfAvailRequired()
    {
        return this.rfAvailRequired;
    }

    public void setRfAvailRequired(SLE_YesNo rfAvailRequired)
    {
        this.rfAvailRequired = rfAvailRequired;
    }

    public int getScToBitrateRatio()
    {
        return this.scToBitrateRatio;
    }

    public void setScToBitrateRatio(int scToBitrateRatio)
    {
        this.scToBitrateRatio = scToBitrateRatio;
    }

    public long getMaxBufferSize()
    {
        return this.maxBufferSize;
    }

    public void setMaxBufferSize(long maxBufferSize)
    {
        this.maxBufferSize = maxBufferSize;
    }

    public CLTU_NotificationMode getNotificationMode()
    {
        return this.notificationMode;
    }

    public void setNotificationMode(CLTU_NotificationMode notificationMode)
    {
        this.notificationMode = notificationMode;
    }

    public int getAcquisitionSequenceLength()
    {
        return this.acquisitionSequenceLength;
    }

    public void setAcquisitionSequenceLength(int length)
    {
        this.acquisitionSequenceLength = length;
    }

    public int getPlop1IdleSequenceLength()
    {
        return this.plop1IdleSequenceLength;
    }

    public void setPlop1IdleSequenceLength(int value)
    {
        this.plop1IdleSequenceLength = value;
    }

    public CLTU_ProtocolAbortMode getProtocolAbortMode()
    {
        return this.protocolAbortMode;
    }

    public void setProtocolAbortMode(CLTU_ProtocolAbortMode value)
    {
        this.protocolAbortMode = value;
    }

    public CLTU_ClcwGvcId getClcwGlobalVcid()
    {
        return this.clcwGlobalVcid;
    }

    public void setClcwGlobalVcid(CLTU_ClcwGvcId value)
    {
        if (value != null)
        {
            this.clcwGlobalVcid = value;
        }
        else
        {
            if (this.clcwGlobalVcid != null)
            {
                this.clcwGlobalVcid = null;
            }
        }
    }

    public CLTU_ClcwPhysicalChannel getClcwPhysicalChannel()
    {
        return this.clcwPhysicalChannel;
    }

    public void setClcwPhysicalChannel(CLTU_ClcwPhysicalChannel value)
    {
        this.clcwPhysicalChannel = value;
    }

    public long getMinimumDelayTime()
    {
        return this.minimumDelayTime;
    }

    public void setMinimumDelayTime(long value)
    {
        this.minimumDelayTime = value;
    }
}
