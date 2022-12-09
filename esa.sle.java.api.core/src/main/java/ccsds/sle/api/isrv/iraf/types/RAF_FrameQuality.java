package ccsds.sle.api.isrv.iraf.types;

public enum RAF_FrameQuality
{
    rafFQ_good(0, "good"), rafFQ_erred(1, "erred"), rafFQ_undetermined(2, "undetermined"), rafFQ_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor RAF_FrameQuality.
     * 
     * @param code
     * @param msg
     */
    private RAF_FrameQuality(int code, String msg)
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
     * Gets the RAF frame quality by code.
     * 
     * @param code
     * @return null if there is no RAF frame quality at the given code.
     */
    public static RAF_FrameQuality getFrameQualityByCode(int code)
    {
        for (RAF_FrameQuality e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
