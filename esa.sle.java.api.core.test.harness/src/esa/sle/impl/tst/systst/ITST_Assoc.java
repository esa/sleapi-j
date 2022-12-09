package esa.sle.impl.tst.systst;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;

public interface ITST_Assoc extends IUnknown
{
    void initiateOpInv(ISLE_Operation pop) throws SleApiException;

    void initiateOpRtn(ISLE_ConfirmedOperation pcop) throws SleApiException;

    void notifyTransmission(ISLE_Operation pop) throws SleApiException;

    void protocolAbort() throws SleApiException;
}
