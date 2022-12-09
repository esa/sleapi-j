package ccsds.sle.api.isrv.iraf.types;

public enum RAF_LockStatus
{

    rafLS_inLock(0, "in lock"), rafLS_outOfLock(1, "out of lock"),
    /**
     * for sub - carrier lock
     */
    rafLS_notInUse(2, "not in use"), rafLS_unknown(3, "unknown"), rafLS_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor RAF_LockStatus
     * 
     * @param code
     * @param msg
     */
    private RAF_LockStatus(int code, String msg)
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
     * Gets the RAF lock status by code.
     * 
     * @param code
     * @return null if there is no RAF lock status at the given code.
     */
    public static RAF_LockStatus getLockStatusByCode(int code)
    {
        for (RAF_LockStatus e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
