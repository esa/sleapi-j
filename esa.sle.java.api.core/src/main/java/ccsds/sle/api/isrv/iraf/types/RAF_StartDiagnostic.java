package ccsds.sle.api.isrv.iraf.types;

public enum RAF_StartDiagnostic
{
    rafSD_outOfService(0, "out of service"),
    rafSD_unableToComply(1, "unable to comply"),
    rafSD_invalidStartTime(2, "invalid start time"),
    rafSD_invalidStopTime(3, "invalid stop time"),
    rafSD_missingTimeValue(4, "missing time value"),
    rafSD_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor RAF_StartDiagnostic.
     * 
     * @param code
     * @param msg
     */
    private RAF_StartDiagnostic(int code, String msg)
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
     * Gets the RAF start diagnostic by code.
     * 
     * @param code
     * @return null if there is no RAF start diagnostic at the given code.
     */
    public static RAF_StartDiagnostic getStartDiagnosticByCode(int code)
    {
        for (RAF_StartDiagnostic e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
