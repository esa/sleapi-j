package ccsds.sle.api.isle.it;

public enum SLE_Diagnostics
{

    sleD_duplicateInvokeId(100, "duplicate invoke id"), sleD_otherReason(127, "other reason"), sleD_invalid(-1,
                                                                                                            "invalid");

    private int code;

    private String msg;


    /**
     * Constructor SLE_Diagnostics
     * 
     * @param code
     * @param msg
     */
    private SLE_Diagnostics(int code, String msg)
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
     * Gets the SLE diagnostic by code.
     * 
     * @param code
     * @return null if there is no diagnostic at the given code.
     */
    public static SLE_Diagnostics getDiagnosticsByCode(int code)
    {
        for (SLE_Diagnostics e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
