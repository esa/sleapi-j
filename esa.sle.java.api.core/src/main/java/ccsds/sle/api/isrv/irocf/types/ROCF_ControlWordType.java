package ccsds.sle.api.isrv.irocf.types;

public enum ROCF_ControlWordType
{
    // all | clcw | notClcw
    rocfCWT_allControlWords(0, "all control words"),
    rocfCWT_clcw(1, "CLCW"),
    rocfCWT_notClcw(2, "not CLCW"),
    rocfCWT_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor ROCF_ControlWordType.
     * 
     * @param code
     * @param msg
     */
    private ROCF_ControlWordType(int code, String msg)
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
     * Gets the ROCF control word type by code.
     * 
     * @param code
     * @return null if there is not ROCF control word type at the given code.
     */
    public static ROCF_ControlWordType getControlWordTypeByCode(long code)
    {
        for (ROCF_ControlWordType e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
