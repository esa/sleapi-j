package esa.sle.impl.ifs.gen;

public enum EEGEN_TokenTypes
{

    eeGEN_TTinvalidState(0),
    eeGEN_TTeof(1),
    eeGEN_TTeolist(2),
    eeGEN_TTsolist(3),
    eeGEN_TTpair(4),
    eeGEN_TTsingle(5),
    eeGEN_TTblankline(6),
    eeGEN_TTinvalidFileFormat(7);

    private int code;


    private EEGEN_TokenTypes(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return this.code;
    }
}
