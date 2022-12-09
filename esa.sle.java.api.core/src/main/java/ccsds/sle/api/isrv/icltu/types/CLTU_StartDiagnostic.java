package ccsds.sle.api.isrv.icltu.types;

public enum CLTU_StartDiagnostic
{

    cltuSTD_outOfService(0, "out of service"),
    cltuSTD_unableToComply(1, "unable to comply"),
    cltuSTD_productionTimeExpired(2, "time expired"),
    cltuSTD_invalidCltuId(3, "invalid cltu id"),
    cltuSTD_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor CLTU_StartDiagnostic.
     * 
     * @param code
     * @param msg
     */
    private CLTU_StartDiagnostic(int code, String msg)
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
     * Gets CLTU start diagnostic by code.
     * 
     * @param code
     * @return null if there is no CLTU start diagnostic at the given code.
     */
    public static CLTU_StartDiagnostic getStartDiagnosticByCode(int code)
    {
        for (CLTU_StartDiagnostic e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
