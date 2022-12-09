package ccsds.sle.api.isrv.icltu.types;

public enum CLTU_UplinkStatus
{

    cltuUS_notAvailable(0, "not available"),
    cltuUS_noRfAvailable(1, "no rf available"),
    cltuUS_noBitLock(2, "no bit lock"),
    cltuUS_nominal(3, "nominal"),
    cltuUS_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor CLTU_UplinkStatus.
     * 
     * @param code
     * @param msg
     */
    private CLTU_UplinkStatus(int code, String msg)
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
     * Gets the CLTU uplink status by code.
     * 
     * @param code
     * @return null if there is no CLTU uplink status at the given code.
     */
    public static CLTU_UplinkStatus getUplinkStatusByCode(int code)
    {
        for (CLTU_UplinkStatus e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
