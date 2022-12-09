package esa.sle.impl.tst.systst.types;

public enum T_RAFCmd
{
    T_RAFCmd_set_delivery_mode(0, "set_dm"),
    T_RAFCmd_set_latency_limit(1, "set_ll"),
    T_RAFCmd_set_min_rep_cycle(29, "set_mrc"),
    T_RAFCmd_set_buffer_size(2, "set_buffer_size"),
    T_RAFCmd_set_init_prod_status(3, "set_init_ps"),
    T_RAFCmd_set_init_fs_lock(4, "set_init_fsl"),
    T_RAFCmd_set_init_cdm_lock(5, "set_init_cdml"),
    T_RAFCmd_set_init_scd_lock(6, "set_init_scdl"),
    T_RAFCmd_set_init_ss_lock(7, "set_init_ssl"),
    T_RAFCmd_set_perm_frames_quality(8, "set_pfq"),
    // SI_update
    T_RAFCmd_set_prod_status(9, "set_ps"),
    T_RAFCmd_set_fs_lock(10, "set_fsl"),
    T_RAFCmd_set_cdm_lock(11, "set_cdml"),
    T_RAFCmd_set_scd_lock(12, "set_scdl"),
    T_RAFCmd_set_ss_lock(13, "set_ssl"),
    T_RAFCmd_print_si(14, "print"),
    T_RAFCmd_up(15, "up"),
    T_RAFCmd_dummy(16, "      "),

    T_RAFCmd_bind(17, "bind"),
    T_RAFCmd_unbind(18, "unbind"),
    T_RAFCmd_start(19, "start"),
    T_RAFCmd_stop(20, "stop"),
    T_RAFCmd_transfer_data(21, "td"),
    T_RAFCmd_send_buffer(22, "sb"),
    T_RAFCmd_sync_notify(23, "sn"),
    T_RAFCmd_ssr(24, "ssr"),
    T_RAFCmd_get_prm(25, "gp"),
    T_RAFCmd_peer_abort(26, "peer_abort"),
    T_RAFCmd_auto_gen_td(27, "auto_send_td"), // auto generate TD operations
    T_RAFCmd_auto_recv_td(28, "auto_send_td"),
    T_RAFCmd_Max(30, "Max"); // can be used for invalid and/or max num of
                             // commands

    private int code;

    private String msg;


    private T_RAFCmd(int code, String msg)
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

    public static T_RAFCmd getRAFCmdByCode(int code)
    {
        for (T_RAFCmd e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
