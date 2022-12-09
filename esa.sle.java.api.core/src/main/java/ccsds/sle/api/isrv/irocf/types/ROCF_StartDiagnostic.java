package ccsds.sle.api.isrv.irocf.types;

public enum ROCF_StartDiagnostic
{
    rocfSD_outOfService(0, "out of service"),
    rocfSD_unableToComply(1, "unable to comply"),
    rocfSD_invalidStartTime(2, "invalid start time"),
    rocfSD_invalidStopTime(3, "invalid stop time"),
    rocfSD_missingTimeValue(4, "missing time value"),
    rocfSD_invalidGvcId(5, "invalid global VCID"),
    rocfSD_invalidControlWordType(6, "invalid control word type"),
    rocfSD_invalidTcVcid(7, "invalid TC VCID"),
    rocfSD_invalidUpdateMode(8, "invalid update mode"),
    rocfSD_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor ROCF_StartDiagnostic.
     * 
     * @param code
     * @param msg
     */
    private ROCF_StartDiagnostic(int code, String msg)
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
     * Gets the ROCF start diagnostic by code.
     * 
     * @param code
     * @return null if there is no ROCF start diagnostic at the given code.
     */
    public static ROCF_StartDiagnostic getStartDiagnosticByCode(int code)
    {
        for (ROCF_StartDiagnostic e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
