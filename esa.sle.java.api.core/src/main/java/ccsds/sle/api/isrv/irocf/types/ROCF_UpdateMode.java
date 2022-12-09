package ccsds.sle.api.isrv.irocf.types;

public enum ROCF_UpdateMode
{
    rocfUM_continuous(0, "continuous"), rocfUM_changeBased(1, "change based"), rocfUM_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor ROCF_UpdateMode.
     * 
     * @param code
     * @param msg
     */
    private ROCF_UpdateMode(int code, String msg)
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
     * Gets the ROCF update mode by code.
     * 
     * @param code
     * @return null if there is no ROCF update mode at the given code.
     */
    public static ROCF_UpdateMode getROCFUpdateModeByCode(long code)
    {
        for (ROCF_UpdateMode e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
