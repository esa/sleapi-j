package esa.sle.impl.eapi.dcw.type;

public enum DCW_State
{
    dcwSTT_created(0), dcwSTT_configured(1), dcwSTT_running(2), dcwSTT_shutDown(3), dcwSTT_terminated(4);

    private int code;


    private DCW_State(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return this.code;
    }

}
