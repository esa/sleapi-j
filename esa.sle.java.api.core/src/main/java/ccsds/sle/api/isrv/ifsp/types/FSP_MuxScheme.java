package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_MuxScheme
{

    fspMS_fifo(0, "FIFO"),
    fspMS_absolutePriority(1, "absolute priority"),
    fspMS_pollingVector(2, "polling vector"),
    fspMS_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor FSP_MuxScheme.
     * 
     * @param code
     * @param msg
     */
    private FSP_MuxScheme(int code, String msg)
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
     * Gets the FSP muxScheme by code.
     * 
     * @param code
     * @return null if there is no FSP muxScheme at the given code.
     */
    public static FSP_MuxScheme getFSP_MuxSchemeByCode(int code)
    {
        for (FSP_MuxScheme e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
