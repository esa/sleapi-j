package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_DirectiveTimeoutType
{
    fspDTT_terminateAD(0, "terminate AD"), fspDTT_suspendAD(1, "suspend AD"), fspDTT_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor FSP_DirectiveTimeoutType.
     * 
     * @param code
     * @param msg
     */
    private FSP_DirectiveTimeoutType(int code, String msg)
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
     * Gets directive timeout type by code.
     * 
     * @param code
     * @return null if there is no directive timeout at the given code.
     */
    public static FSP_DirectiveTimeoutType getDirectiveTimeoutTypeByCode(int code)
    {
        for (FSP_DirectiveTimeoutType e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
