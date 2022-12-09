package ccsds.sle.api.isle.it;

public enum SLE_SIState
{

    sleSIS_unbound(0, "unbound"),
    sleSIS_bindPending(1, "bind pending"),
    sleSIS_bound(2, "bound"),
    sleSIS_unbindPending(3, "unbind pending"),
    sleSIS_startPending(4, "start pending"),
    sleSIS_active(5, "active"),
    sleSIS_stopPending(6, "stop pending");

    private int code;

    private String msg;


    /**
     * Constructor SLE_SIState.
     * 
     * @param code
     * @param msg
     */
    private SLE_SIState(int code, String msg)
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
