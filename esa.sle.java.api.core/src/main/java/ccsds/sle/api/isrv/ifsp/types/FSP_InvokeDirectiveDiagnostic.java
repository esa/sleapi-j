package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_InvokeDirectiveDiagnostic
{

    fspID_directiveInvocationNotAllowed(0, "Directive invocation not allowed"),
    fspID_directiveIdentificationOutOfSequence(1, "Directive identification out of sequence"),
    fspID_directiveError(2, "Directive error"),
    fspID_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor FSP_InvokeDirectiveDiagnostic.
     * 
     * @param code
     * @param msg
     */
    private FSP_InvokeDirectiveDiagnostic(int code, String msg)
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
     * Gets the FSP invoke directive diagnostic by code.
     * 
     * @param code
     * @return null if there is no FSP invoke directive diagnostic at the given
     *         code.
     */
    public static FSP_InvokeDirectiveDiagnostic getInvokeDirectiveDiagnosticByCode(int code)
    {
        for (FSP_InvokeDirectiveDiagnostic e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
