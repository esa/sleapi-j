package ccsds.sle.api.isle.it;

public enum SLE_LogMessageType
{

    sleLM_alarm(0, "alarm"), sleLM_information(1, "information"), sleLM_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor SLE_LogMessageType.
     * 
     * @param code
     * @param msg
     */
    private SLE_LogMessageType(int code, String msg)
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

    public static SLE_LogMessageType getLogMessageByCode(int code)
    {
        for (SLE_LogMessageType e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
