package ccsds.sle.api.isrv.ircf.types;

public enum RCF_NotificationType
{

    rcfNT_lossFrameSync(0, "loss of frame sync"),
    rcfNT_productionStatusChange(1, "production status change"),
    rcfNT_excessiveDataBacklog(2, "excessive data backlog"),
    rcfNT_endOfData(3, "end of data"),
    rcfNT_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor RCF_NotificationType.
     * 
     * @param code
     * @param msg
     */
    private RCF_NotificationType(int code, String msg)
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
