package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_GetParameterDiagnostic
{

    fspGP_unknownParameter(0, "unknown parameter"), fspGP_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor FSP_GetParameterDiagnostic.
     * 
     * @param code
     * @param msg
     */
    private FSP_GetParameterDiagnostic(int code, String msg)
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
     * Gets the FSP parameter diagnostic by code.
     * 
     * @param code
     * @return null if there is no FSP parameter diagnostic by code.
     */
    public static FSP_GetParameterDiagnostic getGetParamDiagByCode(int code)
    {
        for (FSP_GetParameterDiagnostic e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
