package ccsds.sle.api.isrv.irocf.types;

public enum ROCF_ProductionStatus
{
    rocfPS_running(0, "running"),
    rocfPS_interrupted(1, "interrupted"),
    rocfPS_halted(2, "halted"),
    rocfPS_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor ROCF_ProductionStatus.
     * 
     * @param code
     * @param msg
     */
    private ROCF_ProductionStatus(int code, String msg)
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
     * Gets the ROCF production status by code.
     * 
     * @param code
     * @return null if there is no ROCF production status at the given code.
     */
    public static ROCF_ProductionStatus getProductionStatusByCode(int code)
    {
        for (ROCF_ProductionStatus e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
