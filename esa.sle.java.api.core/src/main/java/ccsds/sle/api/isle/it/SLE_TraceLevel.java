package ccsds.sle.api.isle.it;

public enum SLE_TraceLevel
{

    /**
     * only state changes
     */
    sleTL_low(0, "low"),

    /**
     * plus all PDUs and internal events
     */
    sleTL_medium(1, "medium"),

    /**
     * plus arguments of the PDU
     */
    sleTL_high(2, "high"),
    /**
     * plus encoded data
     */
    sleTL_full(3, "full"),
    /**
     * invalid
     */
    sleTL_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor SLE_TraceLevel.
     * 
     * @param code
     * @param msg
     */
    private SLE_TraceLevel(int code, String msg)
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

    public static SLE_TraceLevel getTraceLevelByCode(int code)
    {
        for (SLE_TraceLevel e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
