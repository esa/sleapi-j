package ccsds.sle.api.isrv.icltu.types;

public enum CLTU_PlopInEffect
{

    cltuPIE_plop1(0, "plop 1"), cltuPIE_plop2(1, "plop 2"), cltuPIE_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor CLTU_PlopInEffect.
     * 
     * @param code
     * @param msg
     */
    private CLTU_PlopInEffect(int code, String msg)
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
     * Gets CLTU plop in effect by code.
     * 
     * @param code
     * @return null if there is no CLTU plop in effect at the given code.
     */
    public static CLTU_PlopInEffect getplopInEffectByCode(int code)
    {
        for (CLTU_PlopInEffect e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
