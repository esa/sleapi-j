package ccsds.sle.api.isrv.ircf.types;

public enum RCF_LockStatus
{

    rcfLS_inLock(0, "in lock"), rcfLS_outOfLock(1, "out of lock"), rcfLS_notInUse(2, "not in use"),
    /** only for sub - carrier lock */
    rcfLS_unknown(3, "unknown"), rcfLS_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor RCF_LockStatus.
     * 
     * @param code
     * @param msg
     */
    private RCF_LockStatus(int code, String msg)
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
     * Gets the RCF lock status by code.
     * 
     * @param code
     * @return null if there is no RCF lock status at the given code.
     */
    public static RCF_LockStatus getLockStatusByCode(int code)
    {
        for (RCF_LockStatus e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
