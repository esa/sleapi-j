package esa.sle.sicf.si.parser.file;

import java.io.BufferedReader;

import ccsds.sle.api.isrv.ircf.types.RCF_ChannelType;
import ccsds.sle.api.isrv.ircf.types.RCF_DeliveryMode;
import ccsds.sle.api.isrv.ircf.types.RCF_Gvcid;
import esa.sle.sicf.si.descriptors.RafSIDescriptor;
import esa.sle.sicf.si.descriptors.RcfSIDescriptor;
import esa.sle.sicf.si.descriptors.SIDescriptor;

public class RcfSIParser extends CommonParser
{

    public RcfSIParser(BufferedReader br)
    {
        super(br);
        this.processors.put("delivery-mode", this::extractSIDeliveryMode);
        this.processors.put("initiator", this::extractSIInitiator);
        this.processors.put("permitted-vcids", this::extractSIPermittedVcids);
        this.processors.put("latency-limit", this::extractSILatencyLimit);
        this.processors.put("transfer-buffer-size", this::extractSITransferBufferSize);

        // nothing in standard ?
        // processors.put("maximum-delivery-rate",
        // this::extractSIMaximumDeliveryRate);
        // processors.put("maximum-reporting-cycle",
        // this::extractSIMaximumReportingCycle);
        //this.processors.put("minimum-reporting-cycle", this::extractSIMinimumReportingCycle);
        //

        this.processors.put("return-timeout-period", this::extractSIReturnTimeoutPeriod); // long
        // processors.put("service-version-number",
        // this::extractSIVersionNumber); // int

    }

    private boolean extractSITransferBufferSize(String v, SIDescriptor si)
    {
        ((RcfSIDescriptor) si).setTransferBufferSize(Long.parseLong(v));
        return false;
    }

    private boolean extractSILatencyLimit(String v, SIDescriptor si)
    {
        ((RcfSIDescriptor) si).setLatencyLimit(Integer.parseInt(v));
        return false;
    }

    private boolean extractSIPermittedVcids(String v, SIDescriptor si)
    {
        String[] str = v.split("\\.");
        int n = str.length;
        RCF_Gvcid[] rcfgvid = new RCF_Gvcid[n];
        for (int i = 0; i < n; i++)
        {
            String[] arg = (str[i].subSequence(1, str[i].length() - 1)).toString().split("\\,");
            rcfgvid[i] = new RCF_Gvcid();
            rcfgvid[i].setScid(Integer.parseInt(arg[0]));
            rcfgvid[i].setVersion(Integer.parseInt(arg[1]));
            if (!arg[2].equals("*"))
            {
                rcfgvid[i].setVcid(Integer.parseInt(arg[2]));
                rcfgvid[i].setType(RCF_ChannelType.rcfCT_VirtualChannel);
            }
            else
            {
                rcfgvid[i].setType(RCF_ChannelType.rcfCT_MasterChannel);
            }
        }
        ((RcfSIDescriptor) si).setPermittedGvcidSet(rcfgvid);
        return false;
    }

    private boolean extractSIDeliveryMode(String v, SIDescriptor si)
    {
        RCF_DeliveryMode dm = RCF_DeliveryMode.rcfDM_invalid;
        if (v.equalsIgnoreCase("TIMELY_ONLINE"))
        {
            dm = RCF_DeliveryMode.rcfDM_timelyOnline;
        }
        else
        {
            if (v.equalsIgnoreCase("COMPLETE_ONLINE"))
            {
                dm = RCF_DeliveryMode.rcfDM_completeOnline;
            }
            else
            {
                if (v.equalsIgnoreCase("OFFLINE"))
                {
                    dm = RCF_DeliveryMode.rcfDM_offline;
                }
            }
        }
        ((RcfSIDescriptor) si).setDeliveryMode(dm);
        return false;
    }

    private boolean extractSIInitiator(String v, SIDescriptor si)
    {
        ((RcfSIDescriptor) si).setInitiator(v);
        return false;
    }

    @Override
    protected SIDescriptor createSIDescription()
    {
        return new RcfSIDescriptor();
    }

    private boolean extractSIReturnTimeoutPeriod(String v, SIDescriptor si)
    {
        long timeoutPeriod = Long.parseLong(v);
        si.setReturnTimeoutPeriod(timeoutPeriod);
        return false;
    }
    
    /**
     * Extract the minimum-reporting-cycle from RCF SIC-File
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
