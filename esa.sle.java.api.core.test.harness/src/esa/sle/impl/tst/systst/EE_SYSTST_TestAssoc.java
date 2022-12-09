package esa.sle.impl.tst.systst;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SrvProxyInform;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_AssocState;
import ccsds.sle.api.isle.it.SLE_BindRole;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;

public class EE_SYSTST_TestAssoc
{

    private ISLE_SrvProxyInform srvProxyInform;

    private ITST_Responder tstResponder;

    private ITST_Application tstApl;

    @SuppressWarnings("unused")
    private ISLE_Trace trace;

    @SuppressWarnings("unused")
    private final SLE_ApplicationIdentifier serviceType;

    @SuppressWarnings("unused")
    private final SLE_AssocState state;

    private long sequenceCounter;

    private int invokeId;

    @SuppressWarnings("unused")
    private boolean traceStarted;

    @SuppressWarnings("unused")
    private SLE_TraceLevel traceLevel;

    @SuppressWarnings("unused")
    private final SLE_BindRole role;

    @SuppressWarnings("unused")
    private ISLE_Reporter reporter;

    @SuppressWarnings("unused")
    private ISLE_OperationFactory opFactory;

    @SuppressWarnings("unused")
    private ISLE_UtilFactory utilFactory;

    byte[] protocolAbortDiag;


    public EE_SYSTST_TestAssoc(SLE_ApplicationIdentifier srvType)
    {
        this.serviceType = srvType;

        this.reporter = null;
        this.trace = null;
        this.opFactory = null;
        this.utilFactory = null;
        this.srvProxyInform = null;
        this.state = SLE_AssocState.sleAST_unbound;

        this.sequenceCounter = 0;
        this.invokeId = 0;

        this.traceStarted = false;
        this.traceLevel = SLE_TraceLevel.sleTL_low;
        this.role = SLE_BindRole.sleBR_initiator;

        this.tstResponder = null;
        this.tstApl = null;

        this.protocolAbortDiag = new byte[4];
        this.protocolAbortDiag[0] = '\1';
        this.protocolAbortDiag[1] = '\2';
        this.protocolAbortDiag[2] = '\3';
        this.protocolAbortDiag[3] = '\4';
    }

    HRESULT startTrace(ISLE_Trace trace, SLE_TraceLevel level, boolean forward)
    {
        this.trace = trace;
        this.traceLevel = level;
        this.traceStarted = true;
        return HRESULT.S_OK;
    }

    HRESULT stopTrace()
    {
        this.traceStarted = false;
        this.trace = null;
        return HRESULT.S_OK;
    }

    @SuppressWarnings("unchecked")
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    public HRESULT discardBuffer()
    {
        if (this.tstResponder != null)
        {
            try
            {
                this.tstResponder.discardBuffer();
            }
            catch (SleApiException e)
            {
                return e.getHResult();
            }
        }
        return HRESULT.SLE_S_DISCARDED;
    }

    public SLE_AssocState getAssocState()
    {
        return SLE_AssocState.sleAST_unbound;
    }

    public HRESULT initiateOpInvoke(ISLE_Operation poperation, boolean reportTransmission, long seqCount)
    {
        // for a user test set-up a bind invocation has to go
        // via ITST_Application:

        if (poperation.getOperationType() == SLE_OpType.sleOT_bind)
        {
            ISLE_Bind b = (ISLE_Bind) poperation;

            ITST_Assoc passoc = this.queryInterface(ITST_Assoc.class);

            ITST_Responder rsp = null;
            HRESULT rc = HRESULT.S_OK;
            try
            {
                this.tstApl.bind(b, passoc, rsp);
            }
            catch (SleApiException e)
            {
                rc = e.getHResult();
            }
            if (rc != HRESULT.S_OK)
            {
                return rc;
            }

            this.tstResponder = rsp;
            // _tstResponder->AddRef(); add ref not needed here, got it
            // form tstApl->Bind (QueryInt)

        }

        HRESULT rc = HRESULT.S_OK;
        try
        {
            this.tstResponder.initiateOpInvoke(poperation, reportTransmission, seqCount);
        }
        catch (SleApiException e)
        {
            rc = e.getHResult();
        }
        return rc;
    }

    public HRESULT initiateOpReturn(ISLE_ConfirmedOperation poperation, boolean report, long seqCount)
    {
        HRESULT rc = HRESULT.S_OK;
        try
        {
            System.out.println("poperation.result:::::: " + poperation.getResult());
            this.tstResponder.initiateOpReturn(poperation, report, seqCount);
        }
        catch (SleApiException e)
        {
            rc = e.getHResult();
        }
        return rc;
    }

    public void setSrvProxyInform(ISLE_SrvProxyInform spif)
    {
        this.srvProxyInform = spif;
    }

    public void setResponder(ITST_Responder rsp)
    {
        this.tstResponder = rsp;
    }

    public void setApplication(ITST_Application apl)
    {
        this.tstApl = apl;
    }

    public HRESULT initiateOpInv(ISLE_Operation pop)
    {
        if (pop.getOperationType() == SLE_OpType.sleOT_bind)
        {
            this.sequenceCounter = 1;
        }
        else
        {
            this.sequenceCounter++;
        }

        if (pop.isConfirmed() == true)
        {
            this.invokeId++;
            ISLE_ConfirmedOperation cop = (ISLE_ConfirmedOperation) pop;
            cop.setInvokeId(this.invokeId);
        }
        HRESULT rc = HRESULT.S_OK;
        try
        {
            this.srvProxyInform.informOpInvoke(pop, this.sequenceCounter);
        }
        catch (SleApiException e)
        {
            rc = e.getHResult();
        }
        return rc;
    }

    public HRESULT initiateOpRtn(ISLE_ConfirmedOperation pcop)
    {
        if (pcop.getOperationType() == SLE_OpType.sleOT_bind)
        {
            this.sequenceCounter = 1;
        }
        else
        {
            this.sequenceCounter++;
        }

        this.sequenceCounter++;
        HRESULT rc = HRESULT.S_OK;
        try
        {
            this.srvProxyInform.informOpReturn(pcop, this.sequenceCounter);
        }
        catch (SleApiException e)
        {
            rc = e.getHResult();
        }
        return rc;
    }

    public HRESULT notifyTransmission(ISLE_Operation pop)
    {
        HRESULT rc = HRESULT.S_OK;
        try
        {
            this.srvProxyInform.pduTransmitted(pop);
        }
        catch (SleApiException e)
        {
            rc = e.getHResult();
        }
        return rc;
    }

    public HRESULT protocolAbort()
    {
        HRESULT rc = HRESULT.S_OK;
        try
        {
            this.srvProxyInform.protocolAbort(this.protocolAbortDiag);
        }
        catch (SleApiException e)
        {
            rc = e.getHResult();
        }
        return rc;
    }

    public void prepareRelease()
    {

        this.reporter = null;
        this.trace = null;
        this.opFactory = null;
        this.utilFactory = null;
        this.tstResponder = null;
        this.tstApl = null;
        this.srvProxyInform = null;
    }

}
