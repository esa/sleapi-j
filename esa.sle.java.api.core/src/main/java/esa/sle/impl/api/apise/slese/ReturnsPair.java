package esa.sle.impl.api.apise.slese;

import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import esa.sle.impl.ifs.time.EE_ElapsedTimer;

public class ReturnsPair
{
    private ISLE_ConfirmedOperation iconfoperation;

    private EE_ElapsedTimer elapsTimer;


    public ReturnsPair(ISLE_ConfirmedOperation iconfoperation, EE_ElapsedTimer elapsTimer)
    {
        this.iconfoperation = iconfoperation;
        this.elapsTimer = elapsTimer;
    }

    public ISLE_ConfirmedOperation getIConfOperation()
    {
        return this.iconfoperation;
    }

    public void setIConfOperation(ISLE_ConfirmedOperation iconfoperation)
    {
        this.iconfoperation = iconfoperation;
    }

    public EE_ElapsedTimer getElapsTimer()
    {
        return this.elapsTimer;
    }

    public void setElapsTimer(EE_ElapsedTimer elapsTimer)
    {
        this.elapsTimer = elapsTimer;
    }
}
