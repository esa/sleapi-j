package ccsds.sle.api.isle.it;

public enum SLE_AppRole
{

    sleAR_user(0, "user"), sleAR_provider(1, "provider"), sleAR_userAndProvider(2, "user and provider");

    private int code;

    private String msg;


    /**
     * Constructor SLE_AppRole.
     * 
     * @param code
     * @param msg
     */
    private SLE_AppRole(int code, String msg)
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
