package ccsds.sle.api.isrv.ircf.types;

public enum RCF_AntennaIdFormat
{
    rcfAF_global(0, "global"), rcfAF_local(1, "local"), rcfAF_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor RCF_AntennaIdFormat.
     * 
     * @param code
     * @param msg
     */
    private RCF_AntennaIdFormat(int code, String msg)
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
