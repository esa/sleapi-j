package ccsds.sle.api.isle.it;

public enum SLE_UnbindReason
{
    sleUBR_end(0, "end"),
    sleUBR_suspend(1, "suspend"),
    sleUBR_versionNotSupported(2, "version not supported"),
    sleUBR_otherReason(127, "other reason"),
    sleUBR_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor SLE_UnbindReason.
     * 
     * @param code
     * @param msg
     */
    private SLE_UnbindReason(int code, String msg)
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
     * Gets the SLE unbind reason by code.
     * 
     * @param code
     * @return null if there is no unbind reason at the given code.
     */
    public static SLE_UnbindReason getUnbindReasonByCode(int code)
    {
        for (SLE_UnbindReason e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
