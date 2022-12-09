package ccsds.sle.api.isrv.iraf.types;

import java.util.Arrays;

public enum RAF_ParFrameQuality
{
    rafPQ_goodFramesOnly(0, "good frames"),
    rafPQ_erredFramesOnly(1, "erred frames"),
    rafPQ_allFrames(2, "all frames"),
    rafPQ_undefined(3, "undefined"),
    rafPQ_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor RAF_ParFrameQuality.
     * 
     * @param code
     * @param msg
     */
    private RAF_ParFrameQuality(int code, String msg)
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
     * Gets the RAF par frame quality by code.
     * 
     * @param code
     * @return null if there is no RAF par frame quality at the given code.
     */
    public static RAF_ParFrameQuality getRAFParFrameQualByCode(int code)
    {
        for (RAF_ParFrameQuality e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
