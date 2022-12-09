package esa.sle.impl.api.apise.fspse;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SIOpFactory;
import ccsds.sle.api.isle.it.SLE_AbortOriginator;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_OpType;
import esa.sle.impl.api.apise.slese.EE_APISE_UFSI;
import esa.sle.impl.api.apise.slese.types.EE_TI_SLESE_Event;

/**
 * This class provides the functionality that is specific to FSP forward service
 * instances for user applications. It is responsible for the creation of
 * operation objects supported by the FSP service for the user-role. Furthermore
 * it performs pre-processing checks of operation invocations, received from the
 * proxy and the application, to be compatible with the FSP service. For
 * operation invocations and returns the class performs checks for all
 * service-type specific operation objects received either from the proxy or
 * from the service element. The checks are performed in the functions
 * <operation-type>Inv() or <operation-type>Rtn(). If the checks are passed, the
 * operations are passed to the state-processing by a call to
 * doStateProcessing(). This includes forwarding of the invocations/returns to
 * the proxy or to the service element.
 */
public class EE_APISE_FSP_UFSI extends EE_APISE_UFSI implements ISLE_SIOpFactory
{

    /**
     * The constructor, to be used for service instance creation, passes the
     * supplied arguments to the base-class.@EndFunction
     */
    public EE_APISE_FSP_UFSI(String instanceKey, ISLE_ServiceInform clientIf)
    {
        super(instanceKey, SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt, clientIf);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        T ppv = super.queryInterface(iid);

        if (ppv != null)
        {
            return ppv;
        }
        else
        {
            if (iid == ISLE_SIOpFactory.class)
            {
                return (T) this;
            }
            else
            {
                return null;
            }

        }

    }

    @Override
    public <T extends ISLE_Operation> T createOperation(Class<T> iid, SLE_OpType optype) throws SleApiException
    {

        if (isConfigured() == false)
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        // check operation type supported for a FSP service user SI:
        if (optype != SLE_OpType.sleOT_start && optype != SLE_OpType.sleOT_transferData
            && optype != SLE_OpType.sleOT_getParameter && optype != SLE_OpType.sleOT_bind
            && optype != SLE_OpType.sleOT_unbind && optype != SLE_OpType.sleOT_peerAbort
            && optype != SLE_OpType.sleOT_stop && optype != SLE_OpType.sleOT_throwEvent
            && optype != SLE_OpType.sleOT_invokeDirective && optype != SLE_OpType.sleOT_scheduleStatusReport)
        {
            throw new SleApiException(HRESULT.SLE_E_TYPE);
        }

        ISLE_OperationFactory opf = getOpFactory();
        T a = opf.createOperation(iid, optype, SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt, getVersion());

        setUpOperation(optype, a);

        return a;

    }

    /**
     * Starts processing of the operation invocation received from the
     * application.
     */
    @Override
    protected HRESULT doInitiateOpInvoke(ISLE_Operation poperation)
    {

        HRESULT rc = super.doInitiateOpInvoke(poperation);
        if (rc != HRESULT.S_OK)
        {
            return rc;
        }

        SLE_OpType opType = poperation.getOperationType();

        switch (opType)
        {
        case sleOT_bind:
            return bindInv(poperation);
        case sleOT_unbind:
            return unbindInv(poperation);
        case sleOT_start:
            return startInv(poperation);
        case sleOT_stop:
            return stopInv(poperation);
        case sleOT_transferData:
            return transferDataInv(poperation);
        case sleOT_throwEvent:
            return throwEventInv(poperation);
        case sleOT_invokeDirective:
            return invokeDirectiveInv(poperation);
        case sleOT_scheduleStatusReport:
            return scheduleStatusReportInv(poperation);
        case sleOT_getParameter:
            return getParameterInv(poperation);
        case sleOT_peerAbort:
            return peerAbortInv(poperation, SLE_AbortOriginator.sleAO_application);

        default:
            return HRESULT.SLE_E_ROLE;
        }
    }

    /**
     * Starts processing of the return-operation received from the application.
     */
    @Override
    protected HRESULT doInitiateOpReturn(ISLE_ConfirmedOperation poperation)
    {

        HRESULT rc = super.doInitiateOpReturn(poperation);
        if (rc != HRESULT.S_OK)
        {
            return rc;
        }

        return HRESULT.SLE_E_ROLE;
    }

    /**
     * Starts processing of the operation invocation received from the proxy.
     */
    @Override
    protected HRESULT doInformOpInvoke(ISLE_Operation poperation)
    {

        HRESULT rc = super.doInformOpInvoke(poperation);
        if (rc != HRESULT.S_OK)
        {
            return rc;
        }

        SLE_OpType opType = poperation.getOperationType();

        switch (opType)
        {
        case sleOT_asyncNotify:
            return asyncNotifyInv(poperation);
        case sleOT_statusReport:
            return statusReportInv(poperation);
        case sleOT_peerAbort:
            return peerAbortInv(poperation, SLE_AbortOriginator.sleAO_proxy);

        default:
            return HRESULT.SLE_E_ROLE;
        }

    }

    /**
     * Starts processing of the return-operation received from the proxy.
     */
    @Override
    protected HRESULT doInformOpReturn(ISLE_ConfirmedOperation poperation)
    {

        HRESULT rc = super.doInformOpReturn(poperation);
        if (rc != HRESULT.S_OK)
        {
            return rc;
        }

        SLE_OpType opType = poperation.getOperationType();

        switch (opType)
        {
        case sleOT_bind:
            return bindRtn(poperation);
        case sleOT_unbind:
            return unbindRtn(poperation);
        case sleOT_start:
            return startRtn(poperation);
        case sleOT_stop:
            return stopRtn(poperation);
        case sleOT_transferData:
            return transferDataRtn(poperation);
        case sleOT_throwEvent:
            return throwEventRtn(poperation);
        case sleOT_invokeDirective:
            return invokeDirectiveRtn(poperation);
        case sleOT_scheduleStatusReport:
            return scheduleStatusReportRtn(poperation);
        case sleOT_getParameter:
            return getParameterRtn(poperation);

        default:
            return HRESULT.SLE_E_ROLE;
        }
    }

    /**
     * Performs all checks on the FSP-TRANSFER-DATA operation supplied by the
     * Application. When the checks are completed successfully, state-processing
     * is initiated.
     */

    private HRESULT transferDataInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_TransferDataInv, poperation);
    }

    /**
     * Performs all checks on the FSP-TRANSFER-DATA return-PDU supplied by the
     * proxy. When the checks are completed successfully, state-processing is
     * initiated.
     */

    private HRESULT transferDataRtn(ISLE_ConfirmedOperation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_TransferDataRtn, poperation);
    }

    /**
     * Performs all checks on the FSP-THROW-EVENT operation supplied by the
     * application. When the checks are completed successfully, state-processing
     * is initiated.
     */

    private HRESULT throwEventInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_ThrowEventInv, poperation);
    }

    /**
     * Performs all checks on the FSP-THROW-EVENT return-PDU supplied by the
     * proxy. When the checks are completed successfully, state-processing is
     * initiated.
     */

    private HRESULT throwEventRtn(ISLE_ConfirmedOperation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_ThrowEventRtn, poperation);
    }

    /**
     * Performs all checks on the FSP-INVOKE-DIRECTIVE operation supplied by the
     * application. When the checks are completed successfully, state-processing
     * is initiated.
     */

    private HRESULT invokeDirectiveInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_application,
                                 EE_TI_SLESE_Event.eeSLESE_InvokeDirectiveInv,
                                 poperation);
    }

    /**
     * Performs all checks on the FSP-INVOKE-DIRECTIVE return-PDU supplied by
     * the proxy. When the checks are completed successfully, state-processing
     * is initiated.
     */

    private HRESULT invokeDirectiveRtn(ISLE_ConfirmedOperation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_InvokeDirectiveRtn, poperation);
    }

    /**
     * Performs all checks on the FSP-GET-PARAMETER operation supplied by the
     * application. When the checks are completed successfully, state-processing
     * is initiated.
     */

    private HRESULT getParameterInv(ISLE_Operation poperation)
    {

        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_GetPrmInv, poperation);
    }

    /**
     * Performs all checks on the FSP-GET-PARAMETER return supplied by the
     * proxy. When the checks are completed successfully, state-processing is
     * initiated.
     */

    private HRESULT getParameterRtn(ISLE_ConfirmedOperation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_GetPrmRtn, poperation);
    }

    /**
     * Performs all checks on the FSP-START operation supplied by the
     * application. When the checks are completed successfully, state-processing
     * is initiated.
     */

    private HRESULT startInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_StartInv, poperation);
    }

    /**
     * Performs all checks on the FSP-START return supplied by the proxy. When
     * the checks are completed successfully, state-processing is initiated.
     * Status parameters are also updated by reading them from the return-PDU.
     */

    private HRESULT startRtn(ISLE_ConfirmedOperation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_StartRtn, poperation);
    }

    /**
     * Performs all checks on the FSP-ASYNC-NOTIFY invocation supplied by the
     * proxy. When the checks are completed successfully, state-processing is
     * initiated.
     */

    private HRESULT asyncNotifyInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_AsyncNotifyInv, poperation);
    }

    /**
     * Performs all checks on the STOP operation supplied by the application.
     * When the checks are completed successfully, state-processing is
     * initiated.
     */

    private HRESULT stopInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_StopInv, poperation);
    }

    /**
     * Performs all checks on the STOP return supplied by the proxy. When the
     * checks are completed successfully, state-processing is initiated.
     */

    private HRESULT stopRtn(ISLE_ConfirmedOperation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_StopRtn, poperation);
    }

    /**
     * Performs all checks on the FSP-STATUS-REPORT invocation supplied by the
     * proxy. When the checks are completed successfully, state-processing is
     * initiated.
     */

    private HRESULT statusReportInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_StatusReportInv, poperation);
    }

}
