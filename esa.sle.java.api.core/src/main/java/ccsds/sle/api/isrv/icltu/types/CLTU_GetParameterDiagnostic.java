package ccsds.sle.api.isrv.icltu.types;

public enum CLTU_GetParameterDiagnostic
{

    cltuGP_unknownParameter(0, "unknown parameter"), cltuGP_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor CLTU_GetParameterDiagnostic.
     * 
     * @param code
     * @param msg
     */
    private CLTU_GetParameterDiagnostic(int code, String msg)
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
     * Gets CLTU parameter diagnostic by code.
     * 
     * @param code
     * @return null if there is no CLTU parameter diagnostic at the given code.
     */
    public static CLTU_GetParameterDiagnostic getGetParamDiagByCode(int code)
    {
        for (CLTU_GetParameterDiagnostic e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
