package ccsds.sle.api.isrv.ircf.types;

public enum RCF_StartDiagnostic
{

    rcfSD_outOfService(0, "out of service"),
    rcfSD_unableToComply(1, "unable to comply"),
    rcfSD_invalidStartTime(2, "invalid start time"),
    rcfSD_invalidStopTime(3, "invalid stop time"),
    rcfSD_missingTimeValue(4, "missing time value"),
    rcfSD_invalidGvcId(5, "invalid GvcId"),
    rcfSD_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor RCF_StartDiagnostic.
     * 
     * @param code
     * @param msg
     */
    private RCF_StartDiagnostic(int code, String msg)
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
     * Gets the RCF start diagnostic by code.
     * 
     * @param code
     * @return null if there is no RCF start diagnostic at the given code.
     */
    public static RCF_StartDiagnostic getStartDiagnosticByCode(int code)
    {
        for (RCF_StartDiagnostic e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
