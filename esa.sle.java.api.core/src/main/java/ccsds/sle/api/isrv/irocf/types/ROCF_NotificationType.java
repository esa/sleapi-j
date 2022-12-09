package ccsds.sle.api.isrv.irocf.types;

public enum ROCF_NotificationType
{

    rocfNT_lossFrameSync(0, "loss of frame sync"),
    rocfNT_productionStatusChange(1, "production status change"),
    rocfNT_excessiveDataBacklog(2, "excessive data backlog"),
    rocfNT_endOfData(3, "end of data"),
    rocfNT_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor ROCF_NotificationType.
     * 
     * @param code
     * @param msg
     */
    private ROCF_NotificationType(int code, String msg)
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
