package esa.sle.impl.tst.systst.types;

public enum T_Cmd
{
    T_Cmd_initialise(0, "initialise"),
    T_Cmd_start(1, "start"),
    T_Cmd_terminate(2, "terminate"),
    T_Cmd_create_si(3, "create_si"),
    T_Cmd_use_si(4, "use_si"),
    T_Cmd_destroy_si(5, "destroy_si"),
    T_Cmd_list_si(6, "list_si"),
    T_Cmd_shutdown(7, "shutdown"),
    T_Cmd_wait_event_all_si(8, "wait_event_all_si"),
    T_Cmd_help(9, "help"),
    T_Cmd_exit(10, "exit"),
    T_Cmd_down(11, "down"),
    T_Cmd_base_sii_rtn(12, "base_sii_rtn"),
    T_Cmd_base_sii_fwd(13, "base_sii_fwd"),
    T_Cmd_dummy(14, "dummy"),
    T_Cmd_start_rec(15, "start_rec"),
    T_Cmd_stop_rec(16, "stop_rec"),
    T_Cmd_playback(17, "playback"),
    T_Cmd_Max(18, "max"); // can be used for invalid and/or max num of commands

    private int code;

    private String msg;


    private T_Cmd(int code, String msg)
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

    public static T_Cmd getDiagByCode(int code)
    {
        for (T_Cmd e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
