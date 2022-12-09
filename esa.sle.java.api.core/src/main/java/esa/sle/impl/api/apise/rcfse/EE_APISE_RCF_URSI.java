/**
 * @(#) EE_APISE_RCF_URSI.java
 */

package esa.sle.impl.api.apise.rcfse;

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
import esa.sle.impl.api.apise.slese.EE_APISE_URSI;
import esa.sle.impl.api.apise.slese.types.EE_TI_SLESE_Event;

/**
 * This class provides the functionality that is specific to RCF return service
 * instances for user applications. It is responsible for the creation of RCF
 * service-type specific operation objects that are supported for a
 * user-application. This is provided by the implementation of the interface
 * ISLE_SIOpFactory. For received operation invocations and returns the class
 * checks if the PDU's are compatible with the RAF service type.@EndResponsibility
 * The class performs RCF service specific checks of operation objects received
 * from the application and the proxy. The class implements
 * do<Initiate/Inform>OpInvoke() and do<Initiate/Inform>OpReturn exported by the
 * base-class. These functions look at the operation-type and pass the operation
 * invocation to the appropriate function (<opType>Inv() or <opType>Rtn()),
 * which performs the specific checks. After successful checking of the
 * operation invocations/returns state-processing is executed by a call to
 * doStateProcessing().
 */
public class EE_APISE_RCF_URSI extends EE_APISE_URSI implements ISLE_SIOpFactory
{

    /**
     * The constructor, to be used for service instance creation, passes the
     * supplied arguments to the base-class.
     */
    public EE_APISE_RCF_URSI(String instanceKey, ISLE_ServiceInform clientIf)
    {
        super(instanceKey, SLE_ApplicationIdentifier.sleAI_rtnChFrames, clientIf);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        T ppv = super.queryInterface(iid);
        if (ppv == null)
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
        else
        {
            return ppv;
        }
    }

    @Override
    public <T extends ISLE_Operation> T createOperation(Class<T> iid, SLE_OpType optype) throws SleApiException
    {

        if (isConfigured() == false)
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        // check operation type supported for a RCF service user SI:
        if (optype != SLE_OpType.sleOT_start && optype != SLE_OpType.sleOT_getParameter
            && optype != SLE_OpType.sleOT_bind && optype != SLE_OpType.sleOT_unbind
            && optype != SLE_OpType.sleOT_peerAbort && optype != SLE_OpType.sleOT_stop
            && optype != SLE_OpType.sleOT_scheduleStatusReport)
        {
            throw new SleApiException(HRESULT.SLE_E_TYPE);
        }

        ISLE_OperationFactory opf = getOpFactory();
        T a = opf.createOperation(iid, optype, SLE_ApplicationIdentifier.sleAI_rtnChFrames, getVersion());

        // no specific set-up for RCF-START required
        // no specific set-up for RCF-GET-PARAMETER required
        // no specific set-up for UNBIND required
        // no specific set-up for STOP required
        // no specific set-up for SSR required

        // BIND and PEER-ABORT specific set-up supported by base-class:
        setUpOperation(optype, (a));
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
        case sleOT_transferBuffer:
            return transferBufferInv(poperation);
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
        case sleOT_scheduleStatusReport:
            return scheduleStatusReportRtn(poperation);
        case sleOT_getParameter:
            return getParameterRtn(poperation);

        default:
            return HRESULT.SLE_E_ROLE;
        }
    }

    /**
     * Performs all checks on the RCF-START operation supplied by the
     * application. When the checks are completed successfully, state-processing
     * is initiated.
     */
    private HRESULT startInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_StartInv, poperation);
    }

    /**
     * Performs all checks on the RCF-START return supplied by the proxy. When
     * the checks are completed successfully, state-processing is initiated.
     */
    private HRESULT startRtn(ISLE_ConfirmedOperation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_StartRtn, poperation);
    }

    /**
     * Performs all checks on the RCF-GET-PARAMETER operation supplied by the
     * application. When the checks are completed successfully, state-processing
     * is initiated.
     */
    private HRESULT getParameterInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_GetPrmInv, poperation);

    }

    /**
     * Performs all checks on the RCF-GET-PARAMETER return supplied by the
     * proxy. When the checks are completed successfully, state-processing is
     * initiated.
     */
    private HRESULT getParameterRtn(ISLE_ConfirmedOperation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_GetPrmRtn, poperation);
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
     * Performs all checks on the RCF-STATUS-REPORT invocation supplied by the
     * proxy. When the checks are completed successfully, state-processing is
     * initiated.
     */
    private HRESULT statusReportInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_StatusReportInv, poperation);
    }

}
