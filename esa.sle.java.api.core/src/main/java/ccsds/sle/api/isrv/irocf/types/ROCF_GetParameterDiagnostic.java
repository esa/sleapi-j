package ccsds.sle.api.isrv.irocf.types;

public enum ROCF_GetParameterDiagnostic
{
    rocfGP_unknownParameter(0, "unknown parameter"), rocfGP_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor ROCF_GetParameterDiagnostic.
     * 
     * @param code
     * @param msg
     */
    private ROCF_GetParameterDiagnostic(int code, String msg)
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
     * Gets the ROCF get parameter diagnostic by code.
     * 
     * @param code
     * @return null if there is no ROCF get parameter diagnostic at the given
     *         code.
     */
    public static ROCF_GetParameterDiagnostic getGetParamDiagByCode(int code)
    {
        for (ROCF_GetParameterDiagnostic e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
