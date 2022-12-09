package ccsds.sle.api.isle.it;

public enum SLE_BindRole
{

    sleBR_initiator(0, "initiator"),
    sleBR_responder(1, "responder"),
    sleBR_initiatorAndResponder(2, "initiatorAndResponder");

    private int code;

    private String msg;


    /**
     * Constructor SLE_BindRole
     * 
     * @param code
     * @param msg
     */
    private SLE_BindRole(int code, String msg)
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
