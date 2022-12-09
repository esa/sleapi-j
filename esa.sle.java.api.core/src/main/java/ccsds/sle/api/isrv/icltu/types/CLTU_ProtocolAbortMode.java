package ccsds.sle.api.isrv.icltu.types;

public enum CLTU_ProtocolAbortMode
{
    cltuPAM_abort(0, "abort"), cltuPAM_continue(1, "continue"), cltuPAM_invalid(-1, "invalid");

    private int code;

    private String msg;


    private CLTU_ProtocolAbortMode(int code, String msg)
    {
        this.code = code;
        this.msg = msg;
    }

    public int getCode()
    {
        return this.code;
    }

    @Override
    public String toString()
    {
        return this.msg;
    }

    public static CLTU_ProtocolAbortMode getProtAbportModeByCode(int code)
    {
        for (CLTU_ProtocolAbortMode e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
