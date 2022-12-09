package esa.sle.impl.tst.systst;

import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import esa.sle.impl.tst.systst.types.EE_SYSTST_T_Component;

public class EE_SYSTST_FTCFSIClient extends EE_SYSTST_SIClient
{

    public EE_SYSTST_FTCFSIClient(SLE_AppRole role, EE_SYSTST_TimeSource timeSource, UTL utl)
    {
        super(SLE_ApplicationIdentifier.sleAI_fwdTcFrame, role, timeSource, utl);

    }

    @Override
    public EE_SYSTST_T_Component startUIF(boolean playback)
    {
        return EE_SYSTST_T_Component.eeEE_SYSTST_TestSE;
    }

    @Override
    public EE_SYSTST_OpGen getOpGen()
    {
        return null;
    }

    @Override
    public void testTdSend(long lg, int nbtime, long delay_td, byte[] tdData, int delay)
    {}

    @Override
    public void testTdReceive(long lg, int nbtime)
    {}

}
