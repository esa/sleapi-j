package ccsds.sle.api.isrv.ircf.types;

public enum RCF_ProductionStatus
{

    rcfPS_running(0, "running"),
    rcfPS_interrupted(1, "interrupted"),
    rcfPS_halted(2, "halted"),
    rcfPS_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor RCF_ProductionStatus.
     * 
     * @param code
     * @param msg
     */
    private RCF_ProductionStatus(int code, String msg)
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
     * Gets RCF production status by code by code.
     * 
     * @param code
     * @return null if there is no RCF production status at the given code.
     */
    public static RCF_ProductionStatus getProductionStatusByCode(int code)
    {
        for (RCF_ProductionStatus e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
