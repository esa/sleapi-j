package esa.sle.impl.tst.systst;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iscm.IUnknown;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * Space Link Extension - Java Application Program Interface Interface:
 * ITST_Binder Service Instance Binder/Locator The interface must be used by the
 * service element tester when he wants to bind to a responding service element
 * over the Test Proxy. The interface must be implemented by the SLESE_TestProxy
 */
public interface ITST_Binder extends IUnknown
{
    public <T extends ITST_Assoc> T bind(ISLE_Bind pbindop,
                                         EE_Reference<ITST_Responder> prsp,
                                         EE_Reference<ITST_Assoc> ppAssoc) throws SleApiException;
}
