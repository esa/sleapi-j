package ccsds.sle.api.isrv.irocf.types;

public enum ROCF_AntennaIdFormat
{
    rocfAF_global(0, "global"), rocfAF_local(1, "local"), rocfAF_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor ROCF_AntennaIdFormat.
     * 
     * @param code
     * @param msg
     */
    private ROCF_AntennaIdFormat(int code, String msg)
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
