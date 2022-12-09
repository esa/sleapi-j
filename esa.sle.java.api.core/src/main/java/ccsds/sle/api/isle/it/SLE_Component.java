package ccsds.sle.api.isle.it;

public enum SLE_Component
{

    sleCP_application(0, "application"),
    sleCP_serviceElement(1, "service element"),
    sleCP_proxy(2, "proxy"),
    sleCP_operations(3, "operations"),
    sleCP_utilities(4, "utilities"),
    sleCP_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor SLE_Component.
     * 
     * @param code
     * @param msg
     */
    private SLE_Component(int code, String msg)
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

    public static SLE_Component getComponentByCode(int code)
    {
        for (SLE_Component e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
