package esa.sle.impl.api.apipx.pxspl;

import ccsds.sle.api.isle.iop.ISLE_Operation;

public class PXSPL_Operation
{
    private ISLE_Operation pOperation;

    private boolean lastPdu;

    private boolean isInvoke;

    private boolean reportTransmission;


    public PXSPL_Operation(ISLE_Operation pOperation, boolean lastPdu, boolean isInvoke, boolean reportTransmission)
    {
        this.pOperation = pOperation;
        this.lastPdu = lastPdu;
        this.isInvoke = isInvoke;
        this.reportTransmission = reportTransmission;
    }

    public PXSPL_Operation()
    {
        this.pOperation = null;
        this.lastPdu = false;
        this.isInvoke = false;
        this.reportTransmission = false;
    }

    public ISLE_Operation getpOperation()
    {
        return this.pOperation;
    }

    public void setpOperation(ISLE_Operation pOperation)
    {
        this.pOperation = pOperation;
    }

    public boolean isLastPdu()
    {
        return this.lastPdu;
    }

    public void setLastPdu(boolean lastPdu)
    {
        this.lastPdu = lastPdu;
    }

    public boolean isInvoke()
    {
        return this.isInvoke;
    }

    public void setInvoke(boolean isInvoke)
    {
        this.isInvoke = isInvoke;
    }

    public boolean isReportTransmission()
    {
        return this.reportTransmission;
    }

    public void setReportTransmission(boolean reportTransmission)
    {
        this.reportTransmission = reportTransmission;
    }
}
