package ccsds.sle.api.isrv.icltu.types;

public enum CLTU_ThrowEventDiagnostic
{

    cltuTED_operationNotSupported(0, "unsupported operation"),
    cltuTED_outOfSequence(1, "out of sequence"),
    cltuTED_noSuchEvent(2, "no such Event"),
    cltuTED_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor CLTU_ThrowEventDiagnostic.
     * 
     * @param code
     * @param msg
     */
    private CLTU_ThrowEventDiagnostic(int code, String msg)
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
     * Gets CLTU throw event diagnostic by code.
     * 
     * @param code
     * @return null if there is no CLTU throw event diagnostic at the given
     *         code.
     */
    public static CLTU_ThrowEventDiagnostic getThrowEventDiagnosticByCode(int code)
    {
        for (CLTU_ThrowEventDiagnostic e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
