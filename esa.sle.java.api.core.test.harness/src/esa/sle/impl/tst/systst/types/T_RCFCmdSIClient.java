//package esa.sle.impl.tst.systst.types;
//
//public enum T_RCFCmdSIClient
//{
//    T_RCFCmd_set_delivery_mode(0, "set_dm"),
//    T_RCFCmd_set_latency_limit(1, "set_ll"),
//    T_RCFCmd_set_buffer_size(2, "set_buffer_size"),
//    T_RCFCmd_set_init_prod_status(3, "set_init_ps"),
//    T_RCFCmd_set_init_fs_lock(4, "set_init_fsl"),
//    T_RCFCmd_set_init_cdm_lock(5, "set_init_cdml"),
//    T_RCFCmd_set_init_scd_lock(6, "set_init_scdl"),
//    T_RCFCmd_set_init_ss_lock(7, "set_init_ssl"),
//    T_RCFCmd_set_perm_gvcIds(8, "set_pgvcid"),
//    // SI_update
//    T_RCFCmd_set_prod_status(9, "set_ps"),
//    T_RCFCmd_set_fs_lock(10, "set_fsl"),
//    T_RCFCmd_set_cdm_lock(11, "set_cdml"),
//    T_RCFCmd_set_scd_lock(12, "set_scdl"),
//    T_RCFCmd_set_ss_lock(13, "set_ssl"),
//    T_RCFCmd_print_si(14, "print"),
//    T_RCFCmd_up(15, "up"),
//
//    T_RCFCmd_dummy(16, "      "),
//
//    T_RCFCmd_bind(17, "bind"),
//    T_RCFCmd_unbind(18, "unbind"),
//    T_RCFCmd_start(19, "start"),
//    T_RCFCmd_stop(20, "stop"),
//    T_RCFCmd_transfer_data(21, "td"),
//    T_RCFCmd_sync_notify(22, "sn"),
//    T_RCFCmd_ssr(23, "ssr"),
//    T_RCFCmd_get_prm(24, "gp"),
//    T_RCFCmd_peer_abort(25, "peer_abort"),
//    T_RCFCmd_auto_gen_td(26, "auto_gen_td"), // auto generate TD operations
//    T_RCFCmd_Max(27, "Max"); // can be used for invalid and/or max num of
//                             // commands
//
//    private int code;
//
//    private String msg;
//
//
//    private T_RCFCmdSIClient(int code, String msg)
//    {
//        this.code = code;
//        this.msg = msg;
//    }
//
//    public int getCode()
//    {
//        return this.code;
//    }
//
//    @Override
//    public String toString()
//    {
//        return this.msg;
//    }
//
//    public static T_RCFCmdSIClient getTRCFCmdSIClient(int code)
//    {
//        for (T_RCFCmdSIClient e : values())
//        {
//            if (e.code == code)
//            {
//                return e;
//            }
//        }
//
//        return null;
//    }
// }
