package ccsds.sle.api.isrv.icltu.types;

public enum CLTU_ProductionStatus
{
    cltuPS_operational(0, "operational"),
    cltuPS_configured(1, "configured"),
    cltuPS_interrupted(2, "interrupted"),
    cltuPS_halted(3, "halted"),
    cltuPS_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor CLTU_ProductionStatus.
     * 
     * @param code
     * @param msg
     */
    private CLTU_ProductionStatus(int code, String msg)
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
     * Gets CLTU production status by code.
     * 
     * @param code
     * @return null if there is no CLTU production status at the given code.
     */
    public static CLTU_ProductionStatus getProductionStatusByCode(int code)
    {
        for (CLTU_ProductionStatus e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
