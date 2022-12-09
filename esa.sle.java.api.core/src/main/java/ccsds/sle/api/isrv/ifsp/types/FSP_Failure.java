package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_Failure
{
    /**
     * production expired
     */
    fspF_expired(0),
    /**
     * production interrupted
     */
    fspF_interrupted(1),
    /**
     * transmission mode mismatch
     */
    fspF_modeMismatch(2);

    private int code;


    /**
     * Constructor FSP_Failure.
     * 
     * @param code
     */
    private FSP_Failure(int code)
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

    /**
     * Gets the FSP failure by code.
     * 
     * @param code
     * @return null if there is no FSP failure at the given code.
     */
    public static FSP_Failure getFSP_FailureByCode(int code)
    {
        for (FSP_Failure e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
