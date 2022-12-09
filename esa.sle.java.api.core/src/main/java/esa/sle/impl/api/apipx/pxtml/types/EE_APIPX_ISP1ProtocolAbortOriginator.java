package esa.sle.impl.api.apipx.pxtml.types;

public enum EE_APIPX_ISP1ProtocolAbortOriginator
{
    tcpError(1, ""), localTML(2, ""), peerTML(3, ""), invalid(0, "");

    private int code;

    private String msg;


    private EE_APIPX_ISP1ProtocolAbortOriginator(int code, String msg)
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

    public static EE_APIPX_ISP1ProtocolAbortOriginator getISP1PaOByCode(int code)
    {
        for (EE_APIPX_ISP1ProtocolAbortOriginator e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
