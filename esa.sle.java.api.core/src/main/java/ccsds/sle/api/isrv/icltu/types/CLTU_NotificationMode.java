package ccsds.sle.api.isrv.icltu.types;

public enum CLTU_NotificationMode
{
    cltuNM_deferred(0, "deferred"), cltuNM_immediate(1, "immediate"), cltuNM_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor CLTU_NotificationMode.
     * 
     * @param code
     * @param msg
     */
    private CLTU_NotificationMode(int code, String msg)
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
     * Gets CLTU notification mode by code.
     * 
     * @param code
     * @return null if there is no CLTU notification at the given code.
     */
    public static CLTU_NotificationMode getNotificationModeByCode(int code)
    {
        for (CLTU_NotificationMode e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
