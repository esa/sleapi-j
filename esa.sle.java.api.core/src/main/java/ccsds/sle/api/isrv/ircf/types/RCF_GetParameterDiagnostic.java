package ccsds.sle.api.isrv.ircf.types;

public enum RCF_GetParameterDiagnostic
{

    rcfGP_unknownParameter(0, "unknown"), rcfGP_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor RCF_GetParameterDiagnostic.
     * 
     * @param code
     * @param msg
     */
    private RCF_GetParameterDiagnostic(int code, String msg)
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
     * Gets the RCF get parameter diagnostic by code.
     * 
     * @param code
     * @return null if there is no RCF get parameter diagnostic at the given
     *         code.
     */
    public static RCF_GetParameterDiagnostic getGetParamDiagByCode(int code)
    {
        for (RCF_GetParameterDiagnostic e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
