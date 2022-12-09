package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_ProductionStatus
{
    fspPS_configured(0, "configured"),
    fspPS_operationalBd(1, "operational BD"),
    fspPS_operationalAdAndBd(2, "operational AD and BD"),
    fspPS_operationalAdSuspended(3, "operational AD suspended"),
    fspPS_interrupted(4, "interrupted"),
    fspPS_halted(5, "halted"),
    fspPS_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor FSP_ProductionStatus.
     * 
     * @param code
     * @param msg
     */
    private FSP_ProductionStatus(int code, String msg)
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
     * Gets the FSP production status by code.
     * 
     * @param code
     * @return null if there is no FSP production status at the given code.
     */
    public static FSP_ProductionStatus getProductionStatusByCode(int code)
    {
        for (FSP_ProductionStatus e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
