package esa.sle.impl.api.apipx.pxtml.types;

public enum EE_APIPX_TCPErrors
{
    eeAPIPXtcp_establishmentTimeout(200, "Connection establishment timeout"),
    eeAPIPXtcp_connectionRefused(201, "Connection refused by peer"),
    eeAPIPXtcp_connectionReset(202, "Connection reset by peer"),
    eeAPIPXtcp_sendTimeout(203, "Send timeout"),
    eeAPIPXtcp_invalidLocalAddress(204, "Invalid local address"),
    eeAPIPXtcp_insufficientResources(205, "Insufficient resources"),
    eeAPIPXtcp_localNetworkError(206, "Local network interface error"),
    eeAPIPXtcp_generalNetworkError(207, "Host or network unreachable"),
    eeAPIPXtcp_remotePeerClosed(208, "Remote disconnect"),
    eeAPIPXtcp_other(255, "Other TCP error");

    private int code;

    private String msg;


    private EE_APIPX_TCPErrors(int code, String msg)
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
}
