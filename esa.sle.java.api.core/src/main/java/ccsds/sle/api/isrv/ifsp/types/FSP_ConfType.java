package ccsds.sle.api.isrv.ifsp.types;


public enum FSP_ConfType {
	fspCT_configured(0, "configured"), fspCT_notConfigured(1, "not configured"), fspCT_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor clcwGlobalVcid config type constructor.
     * 
     * @param code
     * @param msg
     */
    private FSP_ConfType(int code, String msg)
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
     * Gets FSP clcwGlobalVcid config type by code.
     * 
     * @param code
     * @return null if there is no FSP config type at the given code.
     */
    public static FSP_ConfType getNotificationModeByCode(int code)
    {
        for (FSP_ConfType e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
