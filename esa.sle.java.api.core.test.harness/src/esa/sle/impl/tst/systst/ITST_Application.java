package esa.sle.impl.tst.systst;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iscm.IUnknown;

public interface ITST_Application extends IUnknown
{
    void bind(ISLE_Bind pbind, ITST_Assoc assoc, ITST_Responder pprsp) throws SleApiException;

    void releaseAssoc(ITST_Assoc assoc) throws SleApiException;

}
