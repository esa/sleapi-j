package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_ThrowEventDiagnostic
{
    fspTED_operationNotSupported(0, "operation not supported"),
    fspTED_outOfSequence(1, "out of sequence"),
    fspTED_noSuchEvent(2, "no such event"),
    fspTED_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor FSP_ThrowEventDiagnostic.
     * 
     * @param code
     * @param msg
     */
    private FSP_ThrowEventDiagnostic(int code, String msg)
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
     * Gets the FSP throw event diagnostic by code.
     * 
     * @param code
     * @return null if there is no FSP throw event diagnostic at the given code.
     */
    public static FSP_ThrowEventDiagnostic getThrowEventDiagnosticByCode(int code)
    {
        for (FSP_ThrowEventDiagnostic e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
