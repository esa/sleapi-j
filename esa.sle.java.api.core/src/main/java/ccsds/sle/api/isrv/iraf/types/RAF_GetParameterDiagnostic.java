package ccsds.sle.api.isrv.iraf.types;

public enum RAF_GetParameterDiagnostic
{
    rafGP_unknownParameter(0, "unknown parameter"), rafGP_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor RAF_GetParameterDiagnostic.
     * 
     * @param code
     * @param msg
     */
    private RAF_GetParameterDiagnostic(int code, String msg)
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
     * Gets the RAF parameter diagnostic by code.
     * 
     * @param code
     * @return null if there is no parameter diagnostic at the given code.
     */
    public static RAF_GetParameterDiagnostic getGetParamDiagByCode(int code)
    {
        for (RAF_GetParameterDiagnostic e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
