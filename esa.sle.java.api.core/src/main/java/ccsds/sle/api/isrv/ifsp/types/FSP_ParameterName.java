package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_ParameterName
{
    fspPN_blockingTimeoutPeriod(0, "blocking timeout period"),
    fspPN_blockingUsage(1, "blocking usage"),
    fspPN_apidList(2, "APID list"),
    fspPN_bitLockRequired(3, "bit lock required"),
    fspPN_deliveryMode(6, "delivery mode"),
    fspPN_directiveInvocationEnabled(7, "directive invocation enabled"),
    fspPN_expectedDirectiveId(8, "expected directive ID"),
    fspPN_expectedEventInvocationId(9, "expected event invocation ID"),
    fspPN_expectedSlduIdentification(10, "expected SLDU identification"),
    fspPN_fopSlidingWindow(11, "FOP sliding window"),
    fspPN_fopState(12, "FOP state"),
    fspPN_mapList(16, "map list"),
    fspPN_mapMuxControl(17, "map multiplexing control"),
    fspPN_mapMuxScheme(18, "map multiplexing scheme"),
    fspPN_maximumFrameLength(19, "maximum frame length"),
    fspPN_maximumPacketLength(20, "maximum packet length"),
    fspPN_reportingCycle(26, "reporting cycle"),
    fspPN_returnTimeoutPeriod(29, "return timeout period"),
    fspPN_rfAvailableRequired(31, "rf available required"),
    fspPN_segmentHeader(32, "segment header"),
    fspPN_timeoutType(35, "timeout type"),
    fspPN_timerInitial(36, "T1 initial"),
    fspPN_transmissionLimit(37, "transmission limit"),
    fspPN_transmitterFrameSequenceNumber(38, "transmitter frame sequence number"),
    fspPN_vcMuxControl(39, "VC multiplexing control"),
    fspPN_vcMuxScheme(40, "VC multiplexing scheme"),
    fspPN_virtualChannel(41, "VC"),
    fspPN_permittedTransmissionMode(107, "permitted transmission mode"),
    fspPN_directiveInvocationOnline(108, "directive invocation online"),
    fspPN_clcwGlobalVcId(202, "clcw global VCID"),
    fspPN_clcwPhysicalChannel(203, " clcw physical channel"),
    fspPN_copCntrFramesRepetion(300, "COP counter frames repetition"),
    fspPN_minReportingCycle(301, "minimum reporting cycle"),
    fspPN_seqCntrFramesRepetition(303, "sequence counter frames repetition"),
    fspPN_throwEventOperation(304, "throw event operation"),
    fspPN_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor FSP_ParameterName
     * 
     * @param code
     * @param msg
     */
    private FSP_ParameterName(int code, String msg)
    {
        this.code = code;
        this.msg = msg;
    }

    /**
     * Gets the code.
     * 
     * @return
     */
    public int getCode()
    {
        return this.code;
    }

    @Override
    public String toString()
    {
        return this.msg;
    }

    /**
     * Gets the FSP parameter name by code.
     * 
     * @param code
     * @return null if there is no FSP parameter name at the given code.
     */
    public static FSP_ParameterName getParameterNameByCode(int code)
    {
        for (FSP_ParameterName e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
