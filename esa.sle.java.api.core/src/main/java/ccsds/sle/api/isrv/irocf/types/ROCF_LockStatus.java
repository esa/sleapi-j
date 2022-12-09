package ccsds.sle.api.isrv.irocf.types;

public enum ROCF_LockStatus
{
    rocfLS_inLock(0, "in-lock"), rocfLS_outOfLock(1, "out-of-lock"), rocfLS_notInUse(2, "not-in-use"),
    /**
     * for sub - carrier lock
     */
    rocfLS_unknown(3, "unknown"), rocfLS_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor ROCF_LockStatus.
     * 
     * @param code
     * @param msg
     */
    private ROCF_LockStatus(int code, String msg)
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
     * Gets the ROCF lock status by code.
     * 
     * @param code
     * @return null if there is no ROCF lock at the given code.
     */
    public static ROCF_LockStatus getLockStatusByCode(int code)
    {
        for (ROCF_LockStatus e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
