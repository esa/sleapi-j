package esa.sle.impl.api.apise.slese.types;

public enum EE_TI_SLESE_Event
{
    eeSLESE_BindInv(0, "Bind-Inv"),
    eeSLESE_BindRtn(1, "Bind-Rtn"),
    eeSLESE_UnbindInv(2, "Unbind-Inv"),
    eeSLESE_UnbindRtn(3, "Unbind-Rtn"),
    eeSLESE_StartInv(4, "Start-Inv"),
    eeSLESE_StartRtn(5, "Start-Rtn"),
    eeSLESE_StopInv(6, "Stop-Inv"),
    eeSLESE_StopRtn(7, "Stop-Rtn"),
    eeSLESE_TransferDataInv(8, "Transfer-Data-Inv"),
    eeSLESE_TransferDataRtn(9, "Transfer-Data-Rtn"),
    eeSLESE_TransferBufferInv(10, "Transfer-Buffer-Inv"),
    eeSLESE_SyncNotifyInv(11, "Sync-Notify-Inv"),
    eeSLESE_AsyncNotifyInv(12, "Async-Notify-Inv"),
    eeSLESE_GetPrmInv(13, "Get-Parameter-Inv"),
    eeSLESE_GetPrmRtn(14, "Get-Parameter-Rtn"),
    eeSLESE_SsrInv(15, "SSR-Inv"),
    eeSLESE_SsrRtn(16, "SSR-Rtn"),
    eeSLESE_StatusReportInv(17, "Status-Report-Inv"),
    eeSLESE_ThrowEventInv(18, "Throw-Event-Inv"),
    eeSLESE_ThrowEventRtn(19, "Throw-Event-Rtn"),
    eeSLESE_InvokeDirectiveInv(20, "Invoke-Directive-Inv"),
    eeSLESE_InvokeDirectiveRtn(21, "Invoke-Directive-Rtn"),
    eeSLESE_PeerAbortInv(22, "Peer-Abort-Inv"),
    eeSLESE_ProtocolAbort(23, "Protocol-Abort"),
    eeSLESE_ReportingTimerExpired(24, "Reporting-Timer-Expired"),
    eeSLESE_ReturnTimeout(25, "Return-Timeout"),
    eeSLESE_LatencyTimerExpired(26, "Latency-Timer-Expired"),
    eeSLESE_PduTransmitted(27, "PDU-Transmitted"),
    eeSLESE_ProvisionPeriodEnds(28, "Provision-Period-Ends");

    private int code;

    private String msg;


    private EE_TI_SLESE_Event(int code, String msg)
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
