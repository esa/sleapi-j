package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_BlockingUsage
{

    fspAU_permitted(0, "permitted"), fspAU_notPermitted(1, "not permitted"), fspAU_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor FSP_BlockingUsage.
     * 
     * @param code
     * @param msg
     */
    private FSP_BlockingUsage(int code, String msg)
    {
        this.code = code;
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
     * Gets the FSP blocking usage by code.
     * 
     * @param code
     * @return null if there is no FSP blocking usage at the given code.
     */
    public static FSP_BlockingUsage getFSP_BlockingUsageByCode(int code)
    {
        for (FSP_BlockingUsage e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }

}
