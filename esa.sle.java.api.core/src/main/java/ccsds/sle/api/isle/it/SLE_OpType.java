package ccsds.sle.api.isle.it;

public enum SLE_OpType
{
    sleOT_bind(0, "BIND"),
    sleOT_unbind(1, "UNBIND"),
    sleOT_peerAbort(2, "PEER-ABORT"),
    sleOT_start(3, "START"),
    sleOT_stop(4, "STOP"),
    sleOT_transferData(5, "TRANSFER-DATA"),
    sleOT_transferBuffer(6, "TRANSFER-BUFFER"),
    sleOT_syncNotify(7, "SYNC-NOTIFY"),
    sleOT_asyncNotify(8, "ASYNC-NOTIFY"),
    sleOT_scheduleStatusReport(9, "SCHEDULE-STATUS-REPORT"),
    sleOT_statusReport(10, "STATUS-REPORT"),
    sleOT_getParameter(11, "GET-PARAMETER"),
    sleOT_throwEvent(12, "THROW-EVENT"),
    sleOT_invokeDirective(13, "INVOKE-DIRECTIVE");

    private int code;

    private String msg;


    /**
     * Constructor SLE_OpType.
     * 
     * @param code
     * @param msg
     */
    private SLE_OpType(int code, String msg)
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
     * Gets the operation type by code.
     * 
     * @param code
     * @return null if there is no operation type for the given code.
     */
    public static SLE_OpType getOpTypeByCode(int code)
    {
        for (SLE_OpType e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
