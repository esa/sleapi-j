package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_StartDiagnostic
{
    fspSTD_outOfService(0, "out of service"),
    fspSTD_unableToComply(1, "unable to comply"),
    fspSTD_productionTimeExpired(2, "production time expired"),
    fspSTD_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor FSP_StartDiagnostic.
     * 
     * @param code
     * @param msg
     */
    private FSP_StartDiagnostic(int code, String msg)
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
     * Gets the FSP start diagnostic by code.
     * 
     * @param code
     * @return null if there is no FSP start diagnostic at the given code.
     */
    public static FSP_StartDiagnostic getStartDiagnosticByCode(int code)
    {
        for (FSP_StartDiagnostic e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
