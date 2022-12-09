package esa.sle.impl.api.apipx.pxtml.types;

public enum EE_APIPX_TMLErrors
{
    eeAPIPXtml_protocolError(128, "TML protocol error"),
    eeAPIPXtml_badTMLMsg(129, "Badly formatted TML message"),
    eeAPIPXtml_heartbeatParamsNotOk(130, "Heartbeat parameters not acceptable"),
    eeAPIPXtml_establishTimeout(131, "Association establishment timeout"),
    eeAPIPXtml_HBRTimeout(132, "Heartbeat receive timeout"),
    eeAPIPXtml_unexpectedClose(133, "Unexpected disconnect by peer"),
    eeAPIPXtml_noUrgent1(134, "Premature disconnect during peer abort"),
    eeAPIPXtml_noUrgent2(135, "Timeout during peer abort"),
    eeAPIPXtml_empty(136, "Empty"),
    eeAPIPXtml_other(199, "Other reason");

    private int code;

    private String msg;


    private EE_APIPX_TMLErrors(int code, String msg)
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

    public static String getStringByCode(int code)
    {
        for (EE_APIPX_TMLErrors e : values())
        {
            if (e.code == code)
            {
                return e.toString();
            }
        }

        return null;
    }

    public static EE_APIPX_TMLErrors getDiagByCode(int code)
    {
        for (EE_APIPX_TMLErrors e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
