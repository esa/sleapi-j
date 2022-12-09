package esa.sle.sicf.si.parser.file;

import java.io.BufferedReader;

import ccsds.sle.api.isrv.irocf.types.ROCF_ChannelType;
import ccsds.sle.api.isrv.irocf.types.ROCF_ControlWordType;
import ccsds.sle.api.isrv.irocf.types.ROCF_DeliveryMode;
import ccsds.sle.api.isrv.irocf.types.ROCF_Gvcid;
import ccsds.sle.api.isrv.irocf.types.ROCF_UpdateMode;
import esa.sle.sicf.si.descriptors.RafSIDescriptor;
import esa.sle.sicf.si.descriptors.RocfSIDescriptor;
import esa.sle.sicf.si.descriptors.SIDescriptor;

public class RocfSIParser extends CommonParser
{

    public RocfSIParser(BufferedReader br)
    {
        super(br);
        this.processors.put("delivery-mode", this::extractSIDeliveryMode);
        this.processors.put("initiator", this::extractSIInitiator);
        this.processors.put("permitted-vcids", this::extractSIPermittedVcids);
        this.processors.put("permitted-tcvcids", this::extractSIPermittedTcvcids);
        this.processors.put("permitted-control-word-types", this::extractSIPermittedControlWordTypes);
        this.processors.put("permitted-update-modes", this::extractSIPermittedUpdateModes);
        this.processors.put("latency-limit", this::extractSILatencyLimit);
        this.processors.put("transfer-buffer-size", this::extractSITransferBufferSize);

        // processors.put("maximum-delivery-rate",
        // this::extractSIMaximumDeliveryRate);
        // processors.put("maximum-reporting-cycle",
        // this::extractSIMaximumReportingCycle);
        //this.processors.put("minimum-reporting-cycle", this::extractSIMinimumReportingCycle);

        this.processors.put("return-timeout-period", this::extractSIReturnTimeoutPeriod);
        // processors.put("service-version-number",
        // this::extractSIVersionNumber);

    }

    private boolean extractSIInitiator(String v, SIDescriptor si)
    {
        ((RocfSIDescriptor) si).setInitiator(v);
        return false;
    }

    private boolean extractSIDeliveryMode(String v, SIDescriptor si)
    {
        ROCF_DeliveryMode dm = ROCF_DeliveryMode.rocfDM_invalid;
        if (v.equalsIgnoreCase("TIMELY_ONLINE"))
        {
            dm = ROCF_DeliveryMode.rocfDM_timelyOnline;
        }
        else
        {
            if (v.equalsIgnoreCase("COMPLETE_ONLINE"))
            {
                dm = ROCF_DeliveryMode.rocfDM_completeOnline;
            }
            else
            {
                if (v.equalsIgnoreCase("OFFLINE"))
                {
                    dm = ROCF_DeliveryMode.rocfDM_offline;
                }
            }
        }
        ((RocfSIDescriptor) si).setDeliveryMode(dm);
        return false;
    }

    private boolean extractSITransferBufferSize(String v, SIDescriptor si)
    {
        long tbs = Long.parseLong(v);
        ((RocfSIDescriptor) si).setTransferBufferSize(tbs);
        return false;
    }

    private boolean extractSILatencyLimit(String v, SIDescriptor si)
    {
        int latencyLimit = Integer.parseInt(v);
        ((RocfSIDescriptor) si).setLatencyLimit(latencyLimit);
        return false;
    }

    private boolean extractSIPermittedUpdateModes(String v, SIDescriptor si)
    {
        String[] value = v.split("\\.");
        ROCF_UpdateMode[] updateMode = new ROCF_UpdateMode[value.length];
        for (int i = 0; i < updateMode.length; i++)
        {
            updateMode[i] = ROCF_UpdateMode.rocfUM_invalid;
            if (value[i].equalsIgnoreCase("continuous"))
            {
                updateMode[i] = ROCF_UpdateMode.rocfUM_continuous;
            }
            if (value[i].equalsIgnoreCase("changeBased"))
            {
                updateMode[i] = ROCF_UpdateMode.rocfUM_changeBased;
            }
        }
        ((RocfSIDescriptor) si).setPermittedUpdateModeSet(updateMode);
        return false;
    }

    private boolean extractSIPermittedControlWordTypes(String v, SIDescriptor si)
    {
        String[] value = v.split("\\.");
        ROCF_ControlWordType[] rocfControlWordType = new ROCF_ControlWordType[value.length];
        for (int i = 0; i < rocfControlWordType.length; i++)
        {
            rocfControlWordType[i] = ROCF_ControlWordType.rocfCWT_invalid;
            if (value[i].equalsIgnoreCase("all"))
            {
                rocfControlWordType[i] = ROCF_ControlWordType.rocfCWT_allControlWords;
            }
            if (value[i].equalsIgnoreCase("clcw"))
            {
                rocfControlWordType[i] = ROCF_ControlWordType.rocfCWT_clcw;
            }
            if (value[i].equalsIgnoreCase("notClcw"))
            {
                rocfControlWordType[i] = ROCF_ControlWordType.rocfCWT_notClcw;
            }
        }
        ((RocfSIDescriptor) si).setPermittedControlWordTypeSet(rocfControlWordType);
        return false;
    }

    private boolean extractSIPermittedTcvcids(String v, SIDescriptor si)
    {
        String[] value = v.split("\\.");
        long[] tcvidset = new long[value.length];
        for (int i = 0; i < value.length; i++)
        {
            tcvidset[i] = Long.parseLong(value[i]);
        }
        ((RocfSIDescriptor) si).setPermittedTcVcidSet(tcvidset);
        return false;
    }

    private boolean extractSIPermittedVcids(String v, SIDescriptor si)
    {
        String[] str = v.split("\\.");
        int n = str.length;
        ROCF_Gvcid[] rocfgvid = new ROCF_Gvcid[n];
        for (int i = 0; i < n; i++)
        {
            String[] arg = (str[i].subSequence(1, str[i].length() - 1)).toString().split("\\,");
            rocfgvid[i] = new ROCF_Gvcid();
            rocfgvid[i].setScid(Integer.parseInt(arg[0]));
            rocfgvid[i].setVersion(Integer.parseInt(arg[1]));
            if (!arg[2].equals("*"))
            {
            	rocfgvid[i].setVcid(Integer.parseInt(arg[2]));
				rocfgvid[i].setType(ROCF_ChannelType.rocfCT_VirtualChannel); // SLEAPIJ-76
			}
            else
            {
            	rocfgvid[i].setType(ROCF_ChannelType.rocfCT_MasterChannel); // SLEAPIJ-76
            }
        }
        ((RocfSIDescriptor) si).setPermittedGvcidSet(rocfgvid);
        return false;
    }

    @Override
    protected SIDescriptor createSIDescription()
    {
        return new RocfSIDescriptor();
    }

    private boolean extractSIReturnTimeoutPeriod(String v, SIDescriptor si)
    {
        long timeoutPeriod = Long.parseLong(v);
        si.setReturnTimeoutPeriod(timeoutPeriod);
        return false;
    }

    /**
     * Extract the minimum-reporting-cycle from ROCF SIC-File
     * @since SLES v5
     * @param v
     * @param si
     * @return
     */
    private boolean extractSIMinimumReportingCycle(String v, SIDescriptor si)
    {
        int n = Integer.parseInt(v);
        ((RafSIDescriptor) si).setMinimumReportingCycle(n);
        return false;
    }
}
