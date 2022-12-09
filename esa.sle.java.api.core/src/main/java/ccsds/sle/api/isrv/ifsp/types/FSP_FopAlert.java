package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_FopAlert
{
    fspFA_noAlert(0, "no alert"),
    fspFA_limit(1, "limit"),
    fspFA_lockOutDetected(2, "lock-out detected"),
    fspFA_synch(3, "synch"),
    fspFA_invalidNR(4, "invalid NR"),
    fspFA_Clcw(5, "CLCW"),
    fspFA_lowerLayerOutOfSync(6, "lower layer out of sync"),
    fspFA_terminateAD(7, "terminate AD"),
    fspFA_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor FSP_FopAlert.
     * 
     * @param code
     * @param msg
     */
    private FSP_FopAlert(int code, String msg)
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
     * Gets the FSP FopAlert by code.
     * 
     * @param code
     * @return null if there is no FSP FopAlert at the given code.
     */
    public static FSP_FopAlert getFopAlertByCode(int code)
    {
        for (FSP_FopAlert e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
