package ccsds.sle.api.isrv.icltu.types;

public enum CLTU_ConfType {
	cltuCT_configured(0, "configured"), cltuCT_notConfigured(1, "not configured"), cltuCT_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor CLTU config type constructor.
     * 
     * @param code
     * @param msg
     */
    private CLTU_ConfType(int code, String msg)
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
     * Gets CLTU config type by code.
     * 
     * @param code
     * @return null if there is no CLTU config type at the given code.
     */
    public static CLTU_ConfType getNotificationModeByCode(int code)
    {
        for (CLTU_ConfType e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
