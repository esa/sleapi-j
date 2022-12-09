package esa.sle.sicf.si.parser.file;

import java.io.BufferedReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isrv.ifsp.types.FSP_BlockingUsage;
import ccsds.sle.api.isrv.ifsp.types.FSP_ChannelType;
import ccsds.sle.api.isrv.ifsp.types.FSP_ClcwGvcId;
import ccsds.sle.api.isrv.ifsp.types.FSP_GvcId;
import ccsds.sle.api.isrv.ifsp.types.FSP_MuxScheme;
import ccsds.sle.api.isrv.ifsp.types.FSP_PermittedTransmissionMode;
import ccsds.sle.api.isrv.ifsp.types.FSP_TimeoutType;
import esa.sle.sicf.si.descriptors.CltuSIDescriptor;
import esa.sle.sicf.si.descriptors.FspSIDescriptor;
import esa.sle.sicf.si.descriptors.SIDescriptor;

public class FspSIParser extends CommonParser
{

    private static final String ANY = "any";

	public FspSIParser(BufferedReader br)
    {
        super(br);
        this.processors.put("clcw-physical-channel",this::extractSIClcwPhysicalChannel);
        this.processors.put("clcw-global-VCID", this::extractSIClcwGlobalVCID);
        //this.processors.put("cop-controlled-frames-repetition", this::extractSICopCntrFramesRep);
        this.processors.put("directive-invocation-enabled", this::extractSIDirectiveInvocationEnabled);
        this.processors.put("permitted-tcvc", this::extractSIPermittedTcvc);
        this.processors.put("permitted-maps", this::extractSIPermittedMaps);
        this.processors.put("permitted-apids", this::extractSIPermittedApIds);
        this.processors.put("permitted-transmission-mode", this::extractSIPermittedTransmissionMode);
        this.processors.put("bit-lock-required", this::extractSIBitLockRequired);
        this.processors.put("rf-available-required", this::extractSIRfAvailableRequired);

        this.processors.put("maximum-packet-data-length", this::extractSIMaximumPacketDataLength); // long
        // processors.put("vc-multiplexing-control",
        // this::extractSIVCMultiplexingControl);
        this.processors.put("vc-multiplexing-scheme", this::extractSIVCMultiplexingScheme); // FSP_MuxScheme
        this.processors.put("blocking-timeout-period", this::extractSIBlockingTimeoutPeriod); // long
        this.processors.put("blocking-usage", this::extractSIBlockingUsage); // FSP_BlockingUsage
        this.processors.put("fop-sliding-window", this::extractSIFopSlidingWindow); // long
        // processors.put("map-multiplexing-control",
        // this::extractSIMapMultiplexingControl);
        this.processors.put("map-multiplexing-scheme", this::extractSIMapMultiplexingScheme);// FSP_MuxScheme
        this.processors.put("maximum-frame-length", this::extractSIMaximumFrameLength); // long
        this.processors.put("segment-header", this::extractSISegmentHeader); // SLE_YesNo
                                                                             // in
                                                                             // sicf
                                                                             // true/false
        //this.processors.put("sequence-controlled-frames-repetition", this::extractSISeqCntrFramesRep);
        //this.processors.put("throwing-of-events-enabled", this::extractSIThrowEventEnabled);
        this.processors.put("timeout-type", this::extractSITimeoutType); // FSP_TimeoutType
        this.processors.put("timer-initial", this::extractSITimerInitial); // long
        this.processors.put("transmission-limit", this::extractSITransmissionLimit); // long
        this.processors.put("transmitter-frame-sequence-number", this::extractSITransmitterFrameSequenceNumber); // long
        // delivery mode not needed only fwd online in the standard
        // processors.put("expected-packet-identification",
        // this::extractSIExpectedPacketIdentification);
        // processors.put("maximum-reporting-cycle",
        // this::extractSIMaximumReportingCycle);
        //this.processors.put("minimum-reporting-cycle",this::extractSIMinimumReportingCycle);
        this.processors.put("reporting-cycle", this::extractSIReportingCycle); // long
        this.processors.put("return-timeout-period", this::extractSIReturnTimeoutPeriod); // long
        // processors.put("service-version-number",
        // this::extractSIServiceVersionNumber); // int

    }
    
    private boolean extractSIClcwPhysicalChannel(String v, SIDescriptor si)
    {
        ((FspSIDescriptor) si).setClcwPhysicalChannel(v);
        return false;
    }

    private boolean extractSIClcwGlobalVCID(String v, SIDescriptor si)
    {
    	FSP_GvcId gvcid = new FSP_GvcId();
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
            	gvcid.setType(FSP_ChannelType.fspCT_VirtualChannel);
            }
            else
            {
            	gvcid.setType(FSP_ChannelType.fspCT_MasterChannel);
            }
        }
        ((FspSIDescriptor) si).setClcwGlobalVcid(new FSP_ClcwGvcId(gvcid));
        return false;
    }
    
    private boolean extractSICopCntrFramesRep(String v, SIDescriptor si)
    {
        boolean b = Boolean.parseBoolean(v.toLowerCase());
        SLE_YesNo rfa = SLE_YesNo.getYesNoByBool(b);
        ((FspSIDescriptor) si).setRfAvailableRequired(rfa);
        return false;
    }
    
    private boolean extractSISeqCntrFramesRep(String v, SIDescriptor si)
    {
        boolean b = Boolean.parseBoolean(v.toLowerCase());
        SLE_YesNo rfa = SLE_YesNo.getYesNoByBool(b);
        ((FspSIDescriptor) si).setRfAvailableRequired(rfa);
        return false;
    }

    private boolean extractSIRfAvailableRequired(String v, SIDescriptor si)
    {
        boolean b = Boolean.parseBoolean(v.toLowerCase());
        SLE_YesNo rfa = SLE_YesNo.getYesNoByBool(b);
        ((FspSIDescriptor) si).setRfAvailableRequired(rfa);
        return false;
    }

    private boolean extractSIBitLockRequired(String v, SIDescriptor si)
    {
        boolean b = Boolean.parseBoolean(v.toLowerCase());
        SLE_YesNo blr = SLE_YesNo.getYesNoByBool(b);
        ((FspSIDescriptor) si).setBitLockRequired(blr);
        return false;
    }

    private boolean extractSIPermittedTransmissionMode(String v, SIDescriptor si)
    {
        String[] m = v.split("\\.");
        Set<FSP_PermittedTransmissionMode> modes = getTransmissionModes(m);
        if (modes.size() == 1)
        {
            Iterator<FSP_PermittedTransmissionMode> itr = modes.iterator();
            ((FspSIDescriptor) si).setPermittedTransmissionMode(itr.next());
        }
        else
        {
            ((FspSIDescriptor) si).setPermittedTransmissionMode(FSP_PermittedTransmissionMode.fspPTM_any);
        }
        return false;
    }
    
    /**
     * Extract the minimum-reporting-cycle from FSP SIC-File
     * @since SLES v5
     * @param v
     * @param si
     * @return
     */
    private boolean extractSIMinimumReportingCycle(String v, SIDescriptor si)
    {
        int n = Integer.parseInt(v);
        ((FspSIDescriptor) si).setMinimumReportingCycle(n);
        return false;
    }

    private Set<FSP_PermittedTransmissionMode> getTransmissionModes(String[] m)
    {
        Set<FSP_PermittedTransmissionMode> set = new HashSet<FSP_PermittedTransmissionMode>();
        for (String element : m)
        {
            if (element.equals("AD"))
            {
                set.add(FSP_PermittedTransmissionMode.fspPTM_sequenceControlled);
            }
            if (element.equals("BD"))
            {
                set.add(FSP_PermittedTransmissionMode.fspPTM_expedited);
            }
        }
        return set;
    }

    private boolean extractSIPermittedApIds(String v, SIDescriptor si)
    {
    	if(v.compareToIgnoreCase(ANY) == 0)
    	{
    		long[] mapApiList = {-1};
    		((FspSIDescriptor) si).setPermittedApis(mapApiList);
    	}
    	else
    	{
	        String[] m = v.split("\\.");
	        long[] mapApiList = getLongs(m);
	        ((FspSIDescriptor) si).setPermittedApis(mapApiList);
    	}
        return false;
    }

    private boolean extractSIPermittedMaps(String v, SIDescriptor si)
    {
        String[] m = v.split("\\.");
        long[] mapIdList = getLongs(m);
        ((FspSIDescriptor) si).setPermittedMaps(mapIdList);
        return false;
    }

    private long[] getLongs(String[] m)
    {
        long[] mapIdList = new long[m.length];
        for (int i = 0; i < mapIdList.length; i++)
        {
            mapIdList[i] = Long.valueOf(m[i]);
        }
        return mapIdList;
    }

    private boolean extractSIPermittedTcvc(String v, SIDescriptor si)
    {
        Long vcId = Long.valueOf(v);
        ((FspSIDescriptor) si).setPermittedTcvc(vcId);
        return false;
    }

    private boolean extractSIDirectiveInvocationEnabled(String v, SIDescriptor si)
    {
        boolean b = Boolean.parseBoolean(v.toLowerCase());
        SLE_YesNo yn = SLE_YesNo.getYesNoByBool(b);
        ((FspSIDescriptor) si).setDirectiveInvocationEnabled(yn);
        return false;
    }

    @Override
    protected SIDescriptor createSIDescription()
    {
        return new FspSIDescriptor();
    }

    private boolean extractSIMaximumPacketDataLength(String v, SIDescriptor si)
    {
        long maximumPacketDataLength = Long.parseLong(v);
        ((FspSIDescriptor) si).setMaximumPacketDataLength(maximumPacketDataLength);
        return false;
    }

    private boolean extractSIVCMultiplexingScheme(String v, SIDescriptor si)
    {
        int n = Integer.parseInt(v);
        FSP_MuxScheme vcMultiplexingScheme = FSP_MuxScheme.getFSP_MuxSchemeByCode(n);
        ((FspSIDescriptor) si).setVCMultiplexingScheme(vcMultiplexingScheme);
        return false;
    }

    private boolean extractSIBlockingTimeoutPeriod(String v, SIDescriptor si)
    {
        long blockingTimeoutPeriod = Long.parseLong(v);
        ((FspSIDescriptor) si).setBlockingTimeoutPeriod(blockingTimeoutPeriod);
        return false;
    }

    private boolean extractSIBlockingUsage(String v, SIDescriptor si)
    {
        int code = Integer.parseInt(v);
        FSP_BlockingUsage blockingUsage = FSP_BlockingUsage.getFSP_BlockingUsageByCode(code);
        ((FspSIDescriptor) si).setBlockingUsage(blockingUsage);
        return false;
    }

    private boolean extractSIFopSlidingWindow(String v, SIDescriptor si)
    {
        long fopSlidingWindow = Long.parseLong(v);
        ((FspSIDescriptor) si).setFopSlidingWindow(fopSlidingWindow);
        return false;
    }

    private boolean extractSIMapMultiplexingScheme(String v, SIDescriptor si)
    {
        int code = Integer.parseInt(v);
        FSP_MuxScheme mapMultiplexingControl = FSP_MuxScheme.getFSP_MuxSchemeByCode(code);
        ((FspSIDescriptor) si).setMapMultiplexingScheme(mapMultiplexingControl);
        return false;
    }
    
    private boolean extractSIMaximumFrameLength(String v, SIDescriptor si)
    {
        long maximumFrameLength = Long.parseLong(v);
        ((FspSIDescriptor) si).setMaximumFrameLength(maximumFrameLength);
        return false;
    }

    private boolean extractSISegmentHeader(String v, SIDescriptor si)
    {
        boolean b = Boolean.parseBoolean(v.toLowerCase());
        SLE_YesNo segmentHeader = SLE_YesNo.getYesNoByBool(b);
        ((FspSIDescriptor) si).setSegmentHeader(segmentHeader);
        return false;
    }
    
    private boolean extractSIThrowEventEnabled(String v, SIDescriptor si)
    {
    	boolean b = Boolean.parseBoolean(v.toLowerCase());
        SLE_YesNo toee = SLE_YesNo.getYesNoByBool(b);
        ((FspSIDescriptor) si).setThrowingOfEventsEnabled(toee);
        return false;
    }

    private boolean extractSITimeoutType(String v, SIDescriptor si)
    {
        int code = Integer.parseInt(v);
        FSP_TimeoutType timeoutType = FSP_TimeoutType.getFSPTimeoutTypeByCode(code);
        ((FspSIDescriptor) si).setTimeoutType(timeoutType);
        return false;
    }

    private boolean extractSITimerInitial(String v, SIDescriptor si)
    {
        long timerInitial = Long.parseLong(v);
        ((FspSIDescriptor) si).setTimerInitial(timerInitial);
        return false;
    }

    private boolean extractSITransmissionLimit(String v, SIDescriptor si)
    {
        long transmissionLimit = Long.parseLong(v);
        ((FspSIDescriptor) si).setTransmissionLimit(transmissionLimit);
        return false;
    }

    private boolean extractSITransmitterFrameSequenceNumber(String v, SIDescriptor si)
    {
        long transmitterFrameSequenceNumber = Long.parseLong(v);
        ((FspSIDescriptor) si).setTransmitterFrameSequenceNumber(transmitterFrameSequenceNumber);
        return false;
    }

    private boolean extractSIReportingCycle(String v, SIDescriptor si)
    {
        long reportingCycle = Long.parseLong(v);
        ((FspSIDescriptor) si).setReportingCycle(reportingCycle);
        return false;
    }

    private boolean extractSIReturnTimeoutPeriod(String v, SIDescriptor si)
    {
        long returnTimeoutPeriod = Long.parseLong(v);
        ((FspSIDescriptor) si).setReturnTimeoutPeriod(returnTimeoutPeriod);
        return false;
    }

}
