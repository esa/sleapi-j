package esa.sle.impl.tst.systst;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_AssocState;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.ifs.gen.EE_Reference;
import esa.sle.impl.tst.systst.types.EE_SYSTST_T_Component;

public abstract class EE_SYSTST_AssocClient implements ITST_Responder
{

    protected ISLE_UtilFactory utilFactory;

    protected ISLE_OperationFactory opFactory;

    protected SLE_ApplicationIdentifier srvType;

    protected ITST_Assoc assoc;

    protected ISLE_ConfirmedOperation lastConfirmedOp;

    protected UTL utl;

    public EE_SYSTST_AssocClient(SLE_ApplicationIdentifier srvType, UTL utl)
    {
        this.srvType = srvType;
        this.assoc = null;
        this.utilFactory = null;
        this.opFactory = null;
        this.lastConfirmedOp = null;
        this.utl = utl;
    }

    public void init(ISLE_UtilFactory uf, ISLE_OperationFactory opf)
    {
        this.utilFactory = uf;
        this.opFactory = opf;
    }

    @Override
    public void initiateOpInvoke(ISLE_Operation poperation, boolean reportTransmission, long seqCount) throws SleApiException
    {
        System.out.println("\n ASSOC-CLIENT: operation invocation received: ");
        SLE_OpType ot = poperation.getOperationType();
        int vn = poperation.getOpVersionNumber();
        System.out.println("Operation Type: " + ot);
        System.out.println("Operation Version Number: " + vn);

        if (poperation.isConfirmed())
        {
            System.out.println("Send positive return PDU now ? (y/n): ");
            EE_Reference<String> send = new EE_Reference<String>();
            utl.read(send, false);

            if (send.equals("y"))
            {
                ISLE_ConfirmedOperation cop = (ISLE_ConfirmedOperation) poperation;
                cop.setPositiveResult();
                HRESULT res = HRESULT.S_OK;
                try
                {
                    this.assoc.initiateOpRtn(cop);
                }
                catch (SleApiException e)
                {
                    res = e.getHResult();
                    if (res != HRESULT.S_OK)
                    {
                        throw new SleApiException(e.getHResult());
                    }
                }
                finally
                {
                    if (res != HRESULT.S_OK)
                    {
                        System.out.println("Send Return PDU failed: " + res);
                    }
                }
            }
            else
            {
                this.lastConfirmedOp = poperation.queryInterface(ISLE_ConfirmedOperation.class);
                if (this.lastConfirmedOp != null)
                {
                    this.lastConfirmedOp.setPositiveResult();
                }
                else
                {
                    throw new SleApiException(HRESULT.E_NOINTERFACE);
                }
            }
        }

        if (reportTransmission)
        {
            throw new SleApiException(HRESULT.SLE_S_TRANSMITTED);
        }
        else
        {
            throw new SleApiException(HRESULT.SLE_S_QUEUED);
        }
    }

    @Override
    public void initiateOpReturn(ISLE_ConfirmedOperation poperation, boolean report, long seqCount) throws SleApiException
    {
        System.out.println("\nASSOC-CLIENT: Return received:");
        SLE_OpType ot = poperation.getOperationType();
        System.out.println("Operation Type:   " + ot);
        int vn = poperation.getOpVersionNumber();
        System.out.println("Operation Version Number: " + vn);
        SLE_Result res = poperation.getResult();
        System.out.println("Operation Result: " + res);
        String ps = poperation.print(40);
        System.out.println(ps);
    }

    @Override
    public void discardBuffer() throws SleApiException
    {
        System.out.println("\nASSOC-CLIENT: Discard Buffer received:");
    }

    /**
     * This function is actually not needed, it is implemented to satisfy the
     * compiler.
     */
    @Override
    public SLE_AssocState getAssocState()
    {
        return SLE_AssocState.sleAST_unbound;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ITST_Responder.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    public void setTstAssoc(ITST_Assoc assoc)
    {
        this.assoc = assoc;
    }

    /**
     * Sends a bind inv to the TestAsoc and further on to the SI. Note that this
     * function is foreseen to be used for the PROVIDER set-up only
     * 
     * @param bindOp
     * @return
     */
    public HRESULT sendBindInv(ISLE_Bind bindOp)
    {
        HRESULT res = HRESULT.S_OK;
        try
        {
            this.assoc.initiateOpInv(bindOp);
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
            return res;
        }
        return res;
    }

    public void prompt()
    {
        System.out.println(this.srvType + "-ASSOC> ");
    }

    public void displayResult(HRESULT rc)
    {
        System.out.println("Result: " + rc);
    }

    public abstract EE_SYSTST_T_Component startUIF();
}
