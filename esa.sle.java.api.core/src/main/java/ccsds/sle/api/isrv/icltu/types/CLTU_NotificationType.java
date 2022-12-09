package ccsds.sle.api.isrv.icltu.types;

public enum CLTU_NotificationType
{
    cltuNT_cltuRadiated(0, "radiated"),
    cltuNT_slduExpired(1, "expired"),
    cltuNT_productionInterrupted(2, "production interrupted"),
    cltuNT_productionHalted(3, "production halted"),
    cltuNT_productionOperational(4, "production operational"),
    cltuNT_bufferEmpty(5, "buffer empty"),
    cltuNT_actionListCompleted(6, "action list completed"),
    cltuNT_actionListNotCompleted(7, "action list not completed"),
    cltuNT_eventConditionEvFalse(8, "event Condition Ev false"),
    cltuNT_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor CLTU_NotificationType.
     * 
     * @param code
     * @param msg
     */
    private CLTU_NotificationType(int code, String msg)
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
     * Gets notification type by code.
     * 
     * @param code
     * @return null if there is no notification type ad the given code.
     */
    public static CLTU_NotificationType getNotificationTypeByCode(int code)
    {
        for (CLTU_NotificationType e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }

}
