package ccsds.sle.api.isrv.ircf.types;

public enum RCF_ParameterName
{
    rcfPN_bufferSize(4, "buffer size"),
    rcfPN_deliveryMode(6, "delivery mode"),
    rcfPN_latencyLimit(15, "latency limit"),
    rcfPN_permittedGvcidSet(24, "permitted global VcId Set"),
    rcfPN_reportingCycle(26, "reporting cycle"),
    rcfPN_requestedGvcid(28, "requested global VcId"),
    rcfPN_returnTimeoutPeriod(29, "timeout period"),
    rcfPN_minReportingCycle(301, "minimum reporting cycle"),
    rcfPN_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor RCF_ParameterName
     * 
     * @param code
     * @param msg
     */
    private RCF_ParameterName(int code, String msg)
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
     * Gets the RCF parameter name by code.
     * 
     * @param code
     * @return null if there is no RCF parameter name at the given code.
     */
    public static RCF_ParameterName getRCFParamNameByCode(int code)
    {
        for (RCF_ParameterName e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
