package ccsds.sle.api.isle.it;

public enum SLE_SlduStatusNotification
{

    sleSN_produceNotification(0, "Sldu, produce notification"),
    sleSN_doNotProduceNotification(1, "Sldu, do not produce notification"),
    sleSN_invalid(-1, "invalid Sldu status notification");

    private int code;

    private String msg;


    /**
     * Constructor SLE_SlduStatusNotification.
     * 
     * @param code
     * @param msg
     */
    private SLE_SlduStatusNotification(int code, String msg)
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
     * Gets the status notification by code.
     * 
     * @param code
     * @return null if there is no status notification at the given code.
     */
    public static SLE_SlduStatusNotification getSlduStatusNotificationByCode(int code)
    {
        for (SLE_SlduStatusNotification e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }

}
