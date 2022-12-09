package esa.sle.impl.ifs.gen;

public enum EE_TI_GEN_PState
{
    eeGEN_idle(0), // no operation is currently processed
    eeGEN_processing(1), // The previous OP is still under processing
    eeGEN_unbinding(2), // a nominal reset during unbinding
    eeGEN_aborting(3); // An abort operation is currently processed

    private int code;


    private EE_TI_GEN_PState(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return this.code;
    }
}
