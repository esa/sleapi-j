package ccsds.sle.api.isrv.iraf.types;

public enum RAF_AntennaIdFormat
{
    rafAF_global(0, "global"), rafAF_local(1, "local"), rafAF_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor RAF_AntennaIdFormat
     * 
     * @param code
     * @param msg
     */
    private RAF_AntennaIdFormat(int code, String msg)
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

}
