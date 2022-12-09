package ccsds.sle.api.isrv.iraf.types;

public enum RAF_NotificationType
{
    rafNT_lossFrameSync(0, "loss of fram sync"),
    rafNT_productionStatusChange(1, "prodoction status change"),
    rafNT_excessiveDataBacklog(2, "excessive data backlog"),
    rafNT_endOfData(3, "end of data"),
    rafNT_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor RAF_NotificationType.
     * 
     * @param code
     * @param msg
     */
    private RAF_NotificationType(int code, String msg)
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

}
