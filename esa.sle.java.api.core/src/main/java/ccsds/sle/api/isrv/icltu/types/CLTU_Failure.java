package ccsds.sle.api.isrv.icltu.types;

public enum CLTU_Failure
{

    cltuF_expired(0, "expired"),
    /**
     * production interrupted
     */
    cltuF_interrupted(1, "interrupted");

    private int code;

    private String msg;


    /**
     * Constructor CLTU_Failure.
     * 
     * @param code
     * @param msg
     */
    private CLTU_Failure(int code, String msg)
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
     * Gets the CLTU_Failure by code.
     * 
     * @param code
     * @return null if there is no CLTU_Failure at the given code.
     */
    public static CLTU_Failure getFailureByCode(int code)
    {
        for (CLTU_Failure e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
