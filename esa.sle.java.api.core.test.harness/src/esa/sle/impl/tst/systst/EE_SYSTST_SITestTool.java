package esa.sle.impl.tst.systst;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SIAdmin;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_BindDiagnostic;
import ccsds.sle.api.isle.it.SLE_SIState;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import esa.sle.impl.ifs.gen.EE_Reference;
import esa.sle.impl.tst.systst.types.EE_SYSTST_T_Component;

public class EE_SYSTST_SITestTool
{
    @SuppressWarnings("unused")
    private final SLE_AppRole role;

    private final ISLE_SII sii;

    private final EE_SYSTST_AssocClient assocClient;

    public EE_SYSTST_SIClient siClient;


    public EE_SYSTST_SITestTool(EE_SYSTST_SIClient sic, EE_SYSTST_AssocClient ac, ISLE_SII sii, SLE_AppRole role)
    {
        this.assocClient = ac;
        this.siClient = sic;
        this.sii = sii;
        this.role = role; // the test set-up
    }

    public ISLE_SII getSII()
    {
        if (this.sii == null)
        {
            return null;
        }
        return this.sii;
    }

    public IUnknown getSIIF()
    {
        ISLE_SIAdmin siAdm = this.siClient.getSiAdmin();
        IUnknown iu = siAdm.queryInterface(IUnknown.class);
        return iu;
    }

    public SLE_SIState getSIState()
    {
        return this.siClient.getSIState();
    }

    public HRESULT bind(ITST_Assoc assoc, ITST_Responder pprsp)
    {
        if (this.assocClient != null)
        {
            pprsp = this.assocClient.queryInterface(ITST_Responder.class);
        }
        if (pprsp == null)
        {
            return HRESULT.E_FAIL;
        }
        // link the test-assoc with the assoc interface
        if (this.assocClient != null)
        {
            this.assocClient.setTstAssoc(assoc);
        }
        return HRESULT.S_OK;
    }

    public HRESULT bind(ITST_Binder binder)
    {
        ISLE_Bind bindOp = this.siClient.makeBindOp();

        EE_Reference<ITST_Responder> rsp = new EE_Reference<ITST_Responder>();

        HRESULT rc = HRESULT.S_OK;

        if (this.assocClient != null)
        {
            rsp.setReference(this.assocClient.queryInterface(ITST_Responder.class));
        }
        if (rsp.getReference() != null)
        {
            return HRESULT.S_OK;
        }

        EE_Reference<ITST_Assoc> assoc = new EE_Reference<ITST_Assoc>();

        rc = HRESULT.S_OK;
        try
        {
            binder.bind(bindOp, rsp, assoc);
        }
        catch (SleApiException e)
        {
            rc = e.getHResult();
        }

        if (assoc.getReference() != null)
        {
            this.assocClient.setTstAssoc(assoc.getReference());
        }
        if (assoc.getReference() != null && rc == HRESULT.S_OK)
        {
            rc = this.assocClient.sendBindInv(bindOp);
        }

        if (rc != HRESULT.S_OK)
        {
            SLE_BindDiagnostic d = bindOp.getBindDiagnostic();
            System.out.println("\nBIND Diagnostic: " + d);
        }
        return rc;
    }

    public void startUIF(boolean playback)
    {
        EE_SYSTST_T_Component cp = EE_SYSTST_T_Component.eeEE_SYSTST_TestSE;
        cp = EE_SYSTST_T_Component.eeEE_SYSTST_TestSI;

        while (cp != EE_SYSTST_T_Component.eeEE_SYSTST_TestSE)
        {
            cp = this.siClient.startUIF(playback);
        }
    }

}
