package esa.sle.impl.tst.systst.types;

public enum T_ClientCmd
{
    T_Cmd_wait(0, "wait"),
    T_Cmd_wait_event(1, "wait_event"),
    T_Cmd_wait_selected_op(2, "wait_selected_op"),
    T_Cmd_start_loop_seq(3, "start_loop_sequence"),
    T_Cmd_stop_loop_seq(4, "end_loop_sequence"),
    T_Cmd_play_loop(5, "play_loop_sequence"),
    T_Cmd_test_td(6, "test_td"),
    T_Cmd_dummy1(7, "      "),
    T_Cmd_set_siid(8, "set_siid"),
    T_Cmd_set_peer_id(9, "set_peer_id"),
    T_Cmd_set_pp(10, "set_pp"),
    T_Cmd_set_bind_ini(11, "set_bind_ini"),
    T_Cmd_set_rsp_port_id(12, "set_rsp_port_id"),
    T_Cmd_set_rtn_to(13, "set_rtn_to"),
    T_Cmd_config_completed(14, "config_completed"),
    T_Cmd_send_a_return(15, "send_a_return"),
    T_Cmd_send_all_return(16, "send_all_return"),
    T_Cmd_time_offset(17, "timeoffset"),
    T_Cmd_help(18, "help"),
    T_Cmd_suspend(19, "suspend"),
    T_Cmd_resume(20, "resume"),
    T_Cmd_playback_cmd(21, "playback_cmd"),
    T_Cmd_Max(22, "Max"); // can be used for invalid and/or max num of commands

    private int code;

    private String msg;


    private T_ClientCmd(int code, String msg)
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

    public static T_ClientCmd getDiagByCode(int code)
    {
        for (T_ClientCmd e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
