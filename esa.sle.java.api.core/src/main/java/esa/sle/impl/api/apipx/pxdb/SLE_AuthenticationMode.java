package esa.sle.impl.api.apipx.pxdb;

public enum SLE_AuthenticationMode
{
    sleAM_none(0, "NONE"), /* authentication not used */
    sleAM_bindOnly(1, "BIND-ONLY"), /* authetication only for bind */
    sleAM_all(2, "ALL"); /* authentication for all operations */

    private int code;

    private String msg;


    SLE_AuthenticationMode(int code, String msg)
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
