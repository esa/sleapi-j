package esa.sle.impl.tst.systst.types;

public enum T_CLTUCmd
{
    T_CLTUCmd_set_bit_lock_req(0, "set_blr"),
    T_CLTUCmd_set_max_sldu_length(1, "set_maxl"),
    T_CLTUCmd_set_modulation_frequ(2, "set_mf"),
    T_CLTUCmd_set_modulation_index(3, "set_mi"),
    T_CLTUCmd_set_plop_in_effect(4, "set_plop"),
    T_CLTUCmd_set_rf_avail_requ(5, "set_rfr"),
    T_CLTUCmd_set_sc_to_bitr_rat(6, "set_scbrr"),
    T_CLTUCmd_set_max_buffer_size(7, "set_mbs"),
    T_CLTUCmd_set_init_prod_status(8, "set_init_ps"),
    T_CLTUCmd_set_init_ul_status(9, "set_init_uls"),
    T_CLTUCmd_dummy1(10, "   "),

    // SI_update
    T_CLTUCmd_cltu_started(11, "cltu_started"),
    T_CLTUCmd_cltu_not_started(12, "cltu_ns"),
    T_CLTUCmd_cltu_radiated(13, "cltu_rad"),
    T_CLTUCmd_cltu_aborted(14, "cltu_aborted"),
    T_CLTUCmd_prod_status_change(15, "set_ps"),
    T_CLTUCmd_set_uplink_status(16, "set_uls"),
    T_CLTUCmd_print_si(17, "print"),
    T_CLTUCmd_up(18, "up"),
    T_CLTUCmd_dummy2(19, "   "),
    T_CLTUCmd_bind(20, "bind"),
    T_CLTUCmd_unbind(21, "unbind"),
    T_CLTUCmd_start(22, "start"),
    T_CLTUCmd_stop(23, "stop"),
    T_CLTUCmd_transfer_data(24, "td"),
    T_CLTUCmd_async_notify(25, "an"),
    T_CLTUCmd_ssr(26, "ssr"),
    T_CLTUCmd_get_prm(27, "gp"),
    T_CLTUCmd_throw_event(28, "te"),
    T_CLTUCmd_peer_abort(29, "peer_abort"),
    T_CLTUCmd_auto_gen_td(30, "auto_gen_td"), // auto generate TD operations

    T_CLTUCmd_buffer_empty(31, "buffer_empty"), // CHANGED-v2: new
                                                // cltu_buffer_empty
    T_CLTUCmd_event_proc_completed(32, "evt_proc_compl"), // CHANGED-v2: new
                                                          // cltu_buffer_empty
    T_CLTUCmd_set_notification_mode(33, "set_nm"), // CHANGED-v2: new
                                                   // set_notification_mode
    T_CLTUCmd_set_acquisition_seq_length(34, "set_acqsl"),
    T_CLTUCmd_set_plop1_idle_seq_length(35, "set_plop1_idle_sl"),
    T_CLTUCmd_set_protocol_abort_mode(36, "set_pam"),
    T_CLTUCmd_set_clcw_global_vcid(37, "set_cgv"),
    T_CLTUCmd_set_clcw_physical_channel(38, "set_cpc"),
    T_CLTUCmd_set_minimum_delay_time(39, "set_mdt"),
    T_CLTUCmd_set_minimum_reporting_cycle(40, "set_mrc"), // Since V5
    T_CLTUCmd_Max(41, "Max"); // can be used for invalid and/or max num of
                              // commands
    

    private int code;

    private String msg;


    private T_CLTUCmd(int code, String msg)
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

    public static T_CLTUCmd getCLTUCmdByCode(int code)
    {
        for (T_CLTUCmd e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
