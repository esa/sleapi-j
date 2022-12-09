package esa.sle.impl.tst.systst;

import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import esa.sle.impl.tst.systst.types.EE_SYSTST_T_Component;

public class EE_SYSTST_ROCFAssocClient extends EE_SYSTST_AssocClient
{
    public EE_SYSTST_ROCFAssocClient(UTL utl)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnChOcf, utl);
    }

    @Override
    public EE_SYSTST_T_Component startUIF()
    {
        System.out.println("ROCF-ASSOC> ");
        return EE_SYSTST_T_Component.eeEE_SYSTST_TestSE;
    }

    public void help()
    {

    }
}
