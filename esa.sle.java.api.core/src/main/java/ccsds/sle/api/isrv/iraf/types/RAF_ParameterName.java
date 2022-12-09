package ccsds.sle.api.isrv.iraf.types;

public enum RAF_ParameterName
{
    rafPN_bufferSize(4, "buffer size"),
    rafPN_deliveryMode(6, "delivery mode"),
    rafPN_latencyLimit(15, "latency limit"),
    rafPN_reportingCycle(26, "reporting cycle"),
    rafPN_requestFrameQuality(27, "request frame quality"),
    rafPN_returnTimeoutPeriod(29, "return timeout period"),
    rafPN_minReportingCycle(301, "minimum reporting cycle"),
    rafPN_permittedFrameQuality(302, "permitted frame quality"),
    rafPN_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor RAF_ParameterName.
     * 
     * @param code
     * @param msg
     */
    private RAF_ParameterName(int code, String msg)
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
     * Gets the RAF parameter name by code.
     * 
     * @param code
     * @return null if there is no RAF parameter name at the given code.
     */
    public static RAF_ParameterName getRAFParamNameByCode(int code)
    {
        for (RAF_ParameterName e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
