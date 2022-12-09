package ccsds.sle.api.isrv.icltu.types;

public enum CLTU_ParameterName
{
    cltuPN_bitLockRequired(3, "bit lock required"),
    cltuPN_deliveryMode(6, "delivery mode"),
    cltuPN_expectedEventInvocationId(9, "expected event invocation id"),
    cltuPN_expectedSlduIdentification(10, "expected Sldu identification"),
    cltuPN_maximumSlduLength(21, "maximum Sldu length"),
    cltuPN_modulationFrequency(22, "modulation frequency"),
    cltuPN_modulationIndex(23, "modulation index"),
    cltuPN_plopInEffect(25, "plop in effect"),
    cltuPN_reportingCycle(26, "reporting cycle"),
    cltuPN_returnTimeoutPeriod(29, "return timeout period"),
    cltuPN_rfAvailableRequired(31, "rf available required"),
    cltuPN_subcarrierToBitRateRatio(34, "sub carrier to bit rate ratio"),
    cltuPN_acquisitionSequenceLength(201, "acquisition sequence length"),
    cltuPN_plop1IdleSequenceLength(206, "plop 1 idle sequence length"),
    cltuPN_protocolAbortMode(207, "protocol abort mode"),
    cltuPN_notificationMode(205, "notification mode"),
    cltuPN_clcwGlobalVcid(202, "clcw global vcid"),
    cltuPN_clcwPhysicalChannel(203, "clcw physical channel"),
    cltuPN_minimumDelayTime(204, "minimun delay time"),
    cltuPN_minimumReportingCycle(301, "minimum reporting cycle"),
    cltuPN_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor CLTU_ParameterName.
     * 
     * @param code
     * @param msg
     */
    private CLTU_ParameterName(int code, String msg)
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
     * Gets the CLTU parameter name by code.
     * 
     * @param code
     * @return null if there is no CLTU parameter name at the given code.
     */
    public static CLTU_ParameterName getParameterNameByCode(int code)
    {
        for (CLTU_ParameterName e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
