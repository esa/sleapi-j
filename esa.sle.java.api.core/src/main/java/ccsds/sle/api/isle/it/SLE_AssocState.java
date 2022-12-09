package ccsds.sle.api.isle.it;

public enum SLE_AssocState
{

    sleAST_unbound(0, "unbound"),
    /**
     * Bind initiated remotely
     */
    sleAST_bindPending(1, "bind pending"), sleAST_bound(2, "bound"),
    /**
     * Unbind initiated remotely
     */
    sleAST_remoteUnbindPending(3, "remote unbind pending"),
    /**
     * Unbind initiated locally
     */
    sleAST_localUnbindPending(4, "local unbind pending");

    private int code;

    private String msg;


    /**
     * Gets the state.
     * 
     * @param code
     * @param msg
     */
    private SLE_AssocState(int code, String msg)
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
