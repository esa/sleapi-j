package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_EventResult
{
    /**
     * action list completed
     */
    fspER_completed(0),
    /**
     * action list not completed
     */
    fspER_notCompleted(1),
    /**
     * event condition evaluated to false
     */
    fspER_conditionFalse(2);

    private int code;


    /**
     * Constructor FSP_EventResult.
     * 
     * @param code
     */
    private FSP_EventResult(int code)
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
     * Gets the FSP event result by code.
     * 
     * @param code
     * @return null if there is not FSP event result at the given code.
     */
    public static FSP_EventResult getFSP_EventResultByCode(int code)
    {
        for (FSP_EventResult e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
