package ccsds.sle.api.isrv.irocf.types;

public enum ROCF_ParameterName
{
    rocfPN_bufferSize(4, "transfer buffer size"),
    rocfPN_deliveryMode(6, "delivery mode"),
    rocfPN_latencyLimit(15, "latency limit"),
    rocfPN_minReportingCycle(301, "minimum reporting cycle"),
    rocfPN_permittedGvcidSet(24, "permitted global VCID set"),
    rocfPN_permittedControlWordTypeSet(101, "permitted control word type set"),
    rocfPN_permittedTcVcidSet(102, "permitted TC VCID set"),
    rocfPN_permittedUpdateModeSet(103, "permitted update mode set"),
    rocfPN_reportingCycle(26, "reporting cycle"),
    rocfPN_requestedGvcid(28, "requested global VCID"),
    rocfPN_requestedControlWordType(104, "requested control word type"),
    rocfPN_requestedTcVcid(105, "requested TC VCID"),
    rocfPN_requestedUpdateMode(106, "requested update mode"),
    rocfPN_returnTimeoutPeriod(29, "return timeout period"),
    rocfPN_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor ROCF_ParameterName.
     * 
     * @param code
     * @param msg
     */
    private ROCF_ParameterName(int code, String msg)
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
     * Gets the ROCF parameter name by code.
     * 
     * @param code
     * @return null if there is no ROCF parameter name at the given code.
     */
    public static ROCF_ParameterName getROCFParamNameByCode(int code)
    {
        for (ROCF_ParameterName e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
