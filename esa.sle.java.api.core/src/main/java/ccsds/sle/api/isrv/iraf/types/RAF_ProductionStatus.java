package ccsds.sle.api.isrv.iraf.types;

public enum RAF_ProductionStatus
{

    rafPS_running(0, "running"),
    rafPS_interrupted(1, "interrupted"),
    rafPS_halted(2, "halted"),
    rafPS_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor RAF_ProductionStatus.
     * 
     * @param code
     * @param msg
     */
    private RAF_ProductionStatus(int code, String msg)
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
     * Gets the RAF production status by code.
     * 
     * @param code
     * @return null if there is no RAF production status at the given code.
     */
    public static RAF_ProductionStatus getProductionStatusByCode(int code)
    {
        for (RAF_ProductionStatus e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
