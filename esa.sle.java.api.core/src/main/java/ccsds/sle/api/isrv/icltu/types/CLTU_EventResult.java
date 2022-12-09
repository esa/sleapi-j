package ccsds.sle.api.isrv.icltu.types;

public enum CLTU_EventResult
{

    /**
     * action list completed
     */
    cltuER_completed(0),
    /**
     * action list not completed
     */
    cltuER_notCompleted(1),
    /**
     * event condition evaluated to false
     */
    cltuER_conditionFalse(2);

    private int code;


    /**
     * Constructor CLTU_EventResult.
     * 
     * @param code
     */
    private CLTU_EventResult(int code)
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
     * Gets CLTU event result by code.
     * 
     * @param code
     * @return null if there is no CLTU event result at the given code.
     */
    public static CLTU_EventResult getEventResultByCode(int code)
    {
        for (CLTU_EventResult e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
