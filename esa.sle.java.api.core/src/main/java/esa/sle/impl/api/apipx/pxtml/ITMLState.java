package esa.sle.impl.api.apipx.pxtml;

public interface ITMLState
{
    public void hlConnectReq(String respPortId);

    public void tcpConnectCnf();

    public void tcpConnectInd();

    public void tcpDataInd(EE_APIPX_TMLMessage msg);

    public void hlDisconnectReq();

    public void tcpDisconnectInd();

    public void delSLEPDUReq(EE_APIPX_TMLMessage pduMsg, boolean last);

    public void hlPeerAbortReq(int diagnostic);

    public void tcpUrgentDataInd();

    public void hlResetReq();

    public void tcpAbortInd();

    public void tcpTimeOut();

    public void tcpError(int code, boolean traceAlso, String[] param);

    public void tmsTimeout();

    public void hbrTimeout();

    public void hbtTimeout();

    public void cpaTimeout();

    public void manageBadFormMsg();
}
