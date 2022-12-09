package esa.sle.impl.api.apipx.pxspl.types;

public enum EE_APIPX_Event
{
    PXSPL_rcvBindReturn(0, "Rcv Bind Return PDU from network"),
    PXSPL_rcvUnbindReturn(1, "Rcv Unbind Return PDU from network"),
    PXSPL_rcvBindInvoke(2, "Rcv Bind Invoke PDU from network"),
    PXSPL_rcvUnbindInvoke(3, "Rcv Unbind Invoke PDU from network"),
    PXSPL_rcvProtocolAbort(4, "Rcv Protocol Abort"),
    PXSPL_initiateBindInvoke(5, "Rcv Bind Invoke PDU from client"),
    PXSPL_initiateBindReturn(6, "Rcv Bind Return PDU from client"),
    PXSPL_initiateUnbindInvoke(7, "Rcv Unbind Invoke PDU from client"),
    PXSPL_initiateUnbindReturn(8, "Rcv Unbind Return PDU from client"),
    PXSPL_initiatePeerAbort(9, "Initiate a Peer Abort"),
    PXSPL_rcvPeerAbort(10, "Rcv Peer Abort from network");

    private int code;

    private String msg;


    private EE_APIPX_Event(int code, String msg)
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
