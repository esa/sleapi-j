package ccsds.sle.api.isrv.icltu.types;

public enum CLTU_AbortReason
{

    cltuAR_interrupted(0, "interrupted"), cltuAR_halted(1, "halted"), cltuSTD_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor CLTU_AbortReason.
     * 
     * @param code
     * @param msg
     */
    private CLTU_AbortReason(int code, String msg)
    {
        this.code = code;
        this.msg = msg;
    }

    /**
     * Gets code.
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
