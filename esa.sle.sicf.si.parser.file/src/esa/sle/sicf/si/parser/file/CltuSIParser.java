package esa.sle.sicf.si.parser.file;

import java.io.BufferedReader;

import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isrv.icltu.types.CLTU_ChannelType;
import ccsds.sle.api.isrv.icltu.types.CLTU_ClcwGvcId;
import ccsds.sle.api.isrv.icltu.types.CLTU_GvcId;
import ccsds.sle.api.isrv.icltu.types.CLTU_ConfType;
import ccsds.sle.api.isrv.icltu.types.CLTU_NotificationMode;
import ccsds.sle.api.isrv.icltu.types.CLTU_PlopInEffect;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProtocolAbortMode;
import esa.sle.sicf.si.descriptors.CltuSIDescriptor;
import esa.sle.sicf.si.descriptors.SIDescriptor;

public class CltuSIParser extends CommonParser
{

    public CltuSIParser(BufferedReader br)
    {
        super(br);
        this.processors.put("maximum-cltu-length", this::extractSIMaximumCltuLength);
        this.processors.put("minimum-cltu-delay", this::extractSIMinumumCltuDelay);
        this.processors.put("maximum-cltu-delay", this::extractSIMaximumCltuDelay);
        this.processors.put("bit-lock-required", this::extractSIBitLockRequired);
        this.processors.put("rf-available-required", this::extractSIRfAvailableRequired);
        this.processors.put("protocol-abort-clear-enabled", this::extractSIProtAbortClearEnabled);

        this.processors.put("acquisition-sequence-length", this::extractSIAcquisitionSequenceLength);
        this.processors.put("clcw-physical-channel", this::extractSIClcwPhysicalChannel);
        this.processors.put("clcw-global-VCID", this::extractSIClcwGlobalVCID);
        // delivery mode not needed.
        this.processors.put("maximum-reporting-cycle", this::extractSIMaximumReportingCycle); // int
        //this.processors.put("minimum-reporting-cycle", this::extractSIMinimumReportingCycle); // int
        this.processors.put("modulation-frequency", this::extractSIModulationFrequency); // long
        this.processors.put("modulation-index", this::extractSIModulationIndex); // int
        this.processors.put("notification-mode", this::extractSINotificationMode); // CLTU_NotificationMode
        this.processors.put("plop-1-idle-sequence-length", this::extractSIPlop1idleSequenceLength); // int
        this.processors.put("plop-in-effect", this::extractSIPlopInEffect); // CLTU_PlopInEffect
        this.processors.put("reporting-cycle", this::extractSIReportingCycle); // long
        this.processors.put("return-timeout-period", this::extractSIReturnTimeoutPeriod); // long
        this.processors.put("subcarrier-to-bit-rate-ratio", this::extractSISubcarrierToBitRateRatio); // int
    }

    private boolean extractSIProtAbortClearEnabled(String v, SIDescriptor si)
    {
        boolean b = Boolean.parseBoolean(v.toLowerCase());
        CLTU_ProtocolAbortMode clpa = (b) ? CLTU_ProtocolAbortMode.cltuPAM_abort
                                         : CLTU_ProtocolAbortMode.cltuPAM_continue;
        ((CltuSIDescriptor) si).setProtocolAbortMode(clpa);
        return false;
    }

    private boolean extractSIRfAvailableRequired(String v, SIDescriptor si)
    {
        boolean b = Boolean.parseBoolean(v.toLowerCase());
        ((CltuSIDescriptor) si).setRfAvailableRequired(SLE_YesNo.getYesNoByBool(b));
        return false;
    }

    private boolean extractSIBitLockRequired(String v, SIDescriptor si)
    {
        boolean b = Boolean.parseBoolean(v.toLowerCase());
        ((CltuSIDescriptor) si).setBitLockRequired(SLE_YesNo.getYesNoByBool(b));
        return false;
    }

    private boolean extractSIMaximumCltuDelay(String v, SIDescriptor si)
    {
        long val = Long.parseLong(v);
        ((CltuSIDescriptor) si).setMaximumCltuDelay(val);
        return false;
    }

    private boolean extractSIMinumumCltuDelay(String v, SIDescriptor si)
    {
        long val = Long.parseLong(v);
        ((CltuSIDescriptor) si).setMinimumCltuDelay(val);
        return false;
    }

    private boolean extractSIMaximumCltuLength(String v, SIDescriptor si)
    {
        long length = Long.parseLong(v);
        ((CltuSIDescriptor) si).setMaximumCltuLength(length);
        return false;
    }

    @Override
    protected SIDescriptor createSIDescription()
    {
        return new CltuSIDescriptor();
    }

    private boolean extractSIAcquisitionSequenceLength(String v, SIDescriptor si)
    {
        long size = Long.parseLong(v);
        ((CltuSIDescriptor) si).setAcquisitionSequenceLength(size);
        return false;
    }

    private boolean extractSIClcwPhysicalChannel(String v, SIDescriptor si)
    {
        ((CltuSIDescriptor) si).setClcwPhysicalChannel(v);
        return false;
    }

    private boolean extractSIClcwGlobalVCID(String v, SIDescriptor si)
    {
        CLTU_GvcId gvcid = new CLTU_GvcId();
        String[] str = v.split("\\.");
        int n = str.length;
        for (int i = 0; i < n; i++)
        {
            String[] arg = (str[i].subSequence(1, str[i].length() - 1)).toString().split("\\,");
            gvcid.setScid(Integer.parseInt(arg[0]));
            gvcid.setVersion(Integer.parseInt(arg[1]));
            if (!arg[2].equals("*"))
            {
            	gvcid.setVcid(Integer.parseInt(arg[2]));
            	gvcid.setType(CLTU_ChannelType.cltuCT_VirtualChannel);
            }
            else
            {
            	gvcid.setType(CLTU_ChannelType.cltuCT_MasterChannel);
            }
        }
        ((CltuSIDescriptor) si).setClcwGlobalVCID(new CLTU_ClcwGvcId(gvcid));
        return false;
    }

    private boolean extractSIMaximumReportingCycle(String v, SIDescriptor si)
    {
        int n = Integer.parseInt(v);
        ((CltuSIDescriptor) si).setMaximumReportingCycle(n);
        return false;
    }

    private boolean extractSIMinimumReportingCycle(String v, SIDescriptor si)
    {
        int n = Integer.parseInt(v);
        ((CltuSIDescriptor) si).setMinimumReportingCycle(n);
        return false;
    }

    private boolean extractSIModulationFrequency(String v, SIDescriptor si)
    {
        long n = Long.parseLong(v);
        ((CltuSIDescriptor) si).setModulationFrequency(n);
        return false;
    }

    private boolean extractSIModulationIndex(String v, SIDescriptor si)
    {
        int n = Integer.parseInt(v);
        ((CltuSIDescriptor) si).setModulationIndex(n);
        return false;
    }

    private boolean extractSINotificationMode(String v, SIDescriptor si)
    {
        CLTU_NotificationMode cldtuNotificationMode = CLTU_NotificationMode.cltuNM_invalid;
        if (v.equalsIgnoreCase("deferred"))
        {
            cldtuNotificationMode = CLTU_NotificationMode.cltuNM_deferred;
        }
        if (v.equalsIgnoreCase("immediate"))
        {
            cldtuNotificationMode = CLTU_NotificationMode.cltuNM_immediate;
        }
        ((CltuSIDescriptor) si).setNotificationMode(cldtuNotificationMode);
        return false;
    }

    private boolean extractSIPlop1idleSequenceLength(String v, SIDescriptor si)
    {
        int plop1idleSequenceLength = Integer.parseInt(v);
        ((CltuSIDescriptor) si).setPlop1idleSequenceLength(plop1idleSequenceLength);
        return false;
    }

    private boolean extractSIPlopInEffect(String v, SIDescriptor si)
    {
        CLTU_PlopInEffect plopInEffect = CLTU_PlopInEffect.cltuPIE_invalid;
        if (v.equals("0"))
        {
            plopInEffect = CLTU_PlopInEffect.cltuPIE_plop1;
        }
        if (v.equals("1"))
        {
            plopInEffect = CLTU_PlopInEffect.cltuPIE_plop2;
        }
        ((CltuSIDescriptor) si).setPlopInEffect(plopInEffect);
        return false;
    }

    private boolean extractSIReportingCycle(String v, SIDescriptor si)
    {
        long reportingCycle = Long.parseLong(v);
        ((CltuSIDescriptor) si).setReportingCycle(reportingCycle);
        return false;
    }

    private boolean extractSIReturnTimeoutPeriod(String v, SIDescriptor si)
    {
        long returnTimeoutPeriod = Long.parseLong(v);
        ((CltuSIDescriptor) si).setReturnTimeoutPeriod(returnTimeoutPeriod);
        return false;
    }

    private boolean extractSISubcarrierToBitRateRatio(String v, SIDescriptor si)
    {
        int subcarrierToBitRateRatio = Integer.parseInt(v);
        ((CltuSIDescriptor) si).setSubcarrierToBitRateRatio(subcarrierToBitRateRatio);
        return false;
    }

}
