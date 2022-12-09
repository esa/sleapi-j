package esa.sle.impl.tst.systst.types;

public enum EE_SYSTST_T_Component
{
    eeEE_SYSTST_TestSI(0, "TestSI"), eeEE_SYSTST_TestAssoc(1, "TestAssoc"), eeEE_SYSTST_TestSE(2, "TestSE");

    private int code;

    private String msg;


    private EE_SYSTST_T_Component(int code, String msg)
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
