package ccsds.sle.api.isrv.iraf.types;

public enum RAF_RequestedFrameQuality
{

    rafRQ_goodFramesOnly(0, "good frames"),
    rafRQ_erredFramesOnly(1, "erred frames"),
    rafRQ_allFrames(2, "all frames"),
    rafRQ_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor RAF_RequestedFrameQuality.
     * 
     * @param code
     * @param msg
     */
    private RAF_RequestedFrameQuality(int code, String msg)
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
     * Gets the RAF requested frame quality by code.
     * 
     * @param code
     * @return null if there is no RAF requested frame quality at the given
     *         code.
     */
    public static RAF_RequestedFrameQuality getRequestedFrameQualityByCode(int code)
    {
        for (RAF_RequestedFrameQuality e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
