package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_TimeoutType
{

    fspTT_generateAlert(0, "generate alert"), fspTT_suspendAD(1, "suspend AD"), fspTT_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor FSP_TimeoutType.
     * 
     * @param code
     * @param msg
     */
    private FSP_TimeoutType(int code, String msg)
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
     * Gets the FSP timeout type by code.
     * 
     * @param code
     * @return null if there is no FSP timeout type at the given code.
     */
    public static FSP_TimeoutType getFSPTimeoutTypeByCode(int code)
    {
        for (FSP_TimeoutType e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
