package esa.sle.sicf.si.parser.file;

import java.io.BufferedReader;

import ccsds.sle.api.isrv.iraf.types.RAF_DeliveryMode;
import ccsds.sle.api.isrv.iraf.types.RAF_ParFrameQuality;
import esa.sle.sicf.si.descriptors.CltuSIDescriptor;
import esa.sle.sicf.si.descriptors.RafSIDescriptor;
import esa.sle.sicf.si.descriptors.SIDescriptor;

public class RafSIParser extends CommonParser
{

    public RafSIParser(BufferedReader br)
    {
        super(br);
        this.processors.put("delivery-mode", this::extractSIDeliveryMode);
        this.processors.put("initiator", this::extractSIInitiator);
        this.processors.put("permitted-frame-quality", this::extractSIPermittedFrameQuality);
        this.processors.put("latency-limit", this::extractSILatencyLimit);
        this.processors.put("transfer-buffer-size", this::extractSITransferBufferSize);

        // processors.put("maximum-delivery-rate",
        // this::extractSIMaximumDeliveryRate);
        // processors.put("maximum-reporting-cycle",
        // this::extractSIMaximumReportingCycle);
        //processors.put("minimum-reporting-cycle",this::extractSIMinimumReportingCycle);
        //processors.put("permitted-frame-quality-set", this::extractSIPermittedFrameQualitySet);
        this.processors.put("return-timeout-period", this::extractSITimeoutPeriod); // long
        // processors.put("service-version-number",
        // this::extractSIVersionNumber); // int
    }

    private boolean extractSITransferBufferSize(String v, SIDescriptor si)
    {
        Long size = Long.parseLong(v);
        ((RafSIDescriptor) si).setTransferBufferSize(size);
        return false;
    }

    private boolean extractSILatencyLimit(String v, SIDescriptor si)
    {
        int n = Integer.parseInt(v);
        ((RafSIDescriptor) si).setLatencyLimit(n);
        return false;
    }

    private boolean extractSIPermittedFrameQuality(String v, SIDescriptor si)
    {
        String[] str = v.split("\\.");
        RAF_ParFrameQuality[] frameQuality = new RAF_ParFrameQuality[str.length];
        for (int i = 0; i < str.length; i++)
        {
            frameQuality[i] = RAF_ParFrameQuality.rafPQ_invalid;
            if (str[i].equalsIgnoreCase("erredFramesOnly"))
            {
                frameQuality[i] = RAF_ParFrameQuality.rafPQ_erredFramesOnly;
            }
            if (str[i].equalsIgnoreCase("goodFramesOnly"))
            {
                frameQuality[i] = RAF_ParFrameQuality.rafPQ_goodFramesOnly;
            }
            if (str[i].equalsIgnoreCase("allFrames"))
            {
                frameQuality[i] = RAF_ParFrameQuality.rafPQ_allFrames;
            }
        }
        ((RafSIDescriptor) si).setFrameQualitySet(frameQuality);
        return false;
    }

    private boolean extractSIInitiator(String v, SIDescriptor si)
    {
        ((RafSIDescriptor) si).setInitiator(v);
        return false;
    }

    private boolean extractSIDeliveryMode(String v, SIDescriptor si)
    {
        RAF_DeliveryMode dm = RAF_DeliveryMode.rafDM_invalid;
        if (v.equalsIgnoreCase("TIMELY_ONLINE"))
        {
            dm = RAF_DeliveryMode.rafDM_timelyOnline;
        }
        else
        {
            if (v.equalsIgnoreCase("COMPLETE_ONLINE"))
            {
                dm = RAF_DeliveryMode.rafDM_completeOnline;
            }
            else
            {
                if (v.equalsIgnoreCase("OFFLINE"))
                {
                    dm = RAF_DeliveryMode.rafDM_offline;
                }
            }
        }
        ((RafSIDescriptor) si).setDeliveryMode(dm);
        return false;
    }

    @Override
    protected SIDescriptor createSIDescription()
    {
        return new RafSIDescriptor();
    }

    private boolean extractSITimeoutPeriod(String v, SIDescriptor si)
    {
        long timeoutPeriod = Long.parseLong(v);
        si.setReturnTimeoutPeriod(timeoutPeriod);
        return false;
    }
    
    /**
     * Extract the minimum-reporting-cycle from RAF SIC-File
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
