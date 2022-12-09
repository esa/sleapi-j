package esa.sle.impl.tst.systst.types;

public enum T_RAFCmdAssocClient
{
    T_RAFCmd_bind(0, "bind"),
    T_RAFCmd_unbind(1, "unbind"),
    T_RAFCmd_start(2, "start"),
    T_RAFCmd_stop(3, "stop"),
    T_RAFCmd_transfer_data(4, "td"),
    T_RAFCmd_sync_notify(5, "sn"),
    T_RAFCmd_ssr(6, "ssr"),
    T_RAFCmd_get_prm(7, "gp"),
    T_RAFCmd_peer_abort(8, "peer_abort"),
    T_RAFCmd_notify_transmission(9, "nt"),
    T_RAFCmd_protocol_abort(10, "prot_abort"),
    T_RAFCmd_up(11, "up"),
    T_RAFCmd_to_si(12, "s"),
    T_RAFCmd_help(13, "help"),
    T_RAFCmd_send_rtn(14, "send_rtn"),
    T_RAFCmd_Max(15, "Max"); // can be used for invalid and/or max num of
                             // commands

    private int code;

    private String msg;


    private T_RAFCmdAssocClient(int code, String msg)
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

    public static T_RAFCmdAssocClient getTRAFCmdAssocClient(int code)
    {
        for (T_RAFCmdAssocClient e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }

}
