/**
 * @(#) EE_SLE_OpFactory.java
 */

package esa.sle.impl.api.apiop.sleop;

import java.util.HashMap;
import java.util.Map;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_OpType;
import esa.sle.impl.api.apiop.cltuop.EE_CLTU_AsyncNotify;
import esa.sle.impl.api.apiop.cltuop.EE_CLTU_GetParameter;
import esa.sle.impl.api.apiop.cltuop.EE_CLTU_Start;
import esa.sle.impl.api.apiop.cltuop.EE_CLTU_StatusReport;
import esa.sle.impl.api.apiop.cltuop.EE_CLTU_ThrowEvent;
import esa.sle.impl.api.apiop.cltuop.EE_CLTU_TransferData;
import esa.sle.impl.api.apiop.fspop.EE_FSP_AsyncNotify;
import esa.sle.impl.api.apiop.fspop.EE_FSP_GetParameter;
import esa.sle.impl.api.apiop.fspop.EE_FSP_InvokeDirective;
import esa.sle.impl.api.apiop.fspop.EE_FSP_Start;
import esa.sle.impl.api.apiop.fspop.EE_FSP_StatusReport;
import esa.sle.impl.api.apiop.fspop.EE_FSP_ThrowEvent;
import esa.sle.impl.api.apiop.fspop.EE_FSP_TransferData;
import esa.sle.impl.api.apiop.rafop.EE_RAF_GetParameter;
import esa.sle.impl.api.apiop.rafop.EE_RAF_Start;
import esa.sle.impl.api.apiop.rafop.EE_RAF_StatusReport;
import esa.sle.impl.api.apiop.rafop.EE_RAF_SyncNotify;
import esa.sle.impl.api.apiop.rafop.EE_RAF_TransferData;
import esa.sle.impl.api.apiop.rcfop.EE_RCF_GetParameter;
import esa.sle.impl.api.apiop.rcfop.EE_RCF_Start;
import esa.sle.impl.api.apiop.rcfop.EE_RCF_StatusReport;
import esa.sle.impl.api.apiop.rcfop.EE_RCF_SyncNotify;
import esa.sle.impl.api.apiop.rcfop.EE_RCF_TransferData;
import esa.sle.impl.api.apiop.rocfop.EE_ROCF_GetParameter;
import esa.sle.impl.api.apiop.rocfop.EE_ROCF_Start;
import esa.sle.impl.api.apiop.rocfop.EE_ROCF_StatusReport;
import esa.sle.impl.api.apiop.rocfop.EE_ROCF_SyncNotify;
import esa.sle.impl.api.apiop.rocfop.EE_ROCF_TransferData;

/**
 * The class EE_SLE_OpFactory implements the interface ISLE_OperationFactory
 * exported by the component class 'Operation Factory' defined in reference
 * [SLE-API].Note that this class must be extended if further service-type
 * specific operation objects are added.
 */
public class EE_SLE_OpFactory implements ISLE_OperationFactory
{

    /**
     * The unique instance of this class
     */
    private static Map<String, EE_SLE_OpFactory> instanceMap = new HashMap<>();

    /**
     * the reporter interface
     */
    private ISLE_Reporter reporter;


    /**
     * This method is called once to create the EE_SLE_OpFactory instance
     * 
     * @param source
     */
    public static synchronized void initialiseInstance(String instanceKey, ISLE_Reporter preporter)
    {
    	EE_SLE_OpFactory instance = instanceMap.get(instanceKey);
    	
        if (instance == null)
        {
            instance = new EE_SLE_OpFactory(preporter);
            instanceMap.put(instanceKey, instance);
        }
    }

    /**
     * This method is called every time the EE_SLE_OpFactory instance is needed
     * 
     * @return
     */
    public static synchronized EE_SLE_OpFactory getInstance(String instanceKey)
    {
    	EE_SLE_OpFactory instance = instanceMap.get(instanceKey);
    	
        if (instance == null)
        {
            throw new IllegalStateException("The initialise method has never been called and the instance never created");
        }

        return instance;

    }

    /**
     * Private constructor called by the initialiseInstance method.
     * 
     * @param preporter
     */
    private EE_SLE_OpFactory(ISLE_Reporter preporter)
    {
        if (preporter != null)
        {
            this.reporter = preporter;
        }
    }

    /**
     * @param iid
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_OperationFactory.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    /**
	 * 
	 */
    @Override
    public <T extends ISLE_Operation> T createOperation(Class<T> iid,
                                                        SLE_OpType opType,
                                                        SLE_ApplicationIdentifier srvType,
                                                        int version) throws SleApiException
    {

        HRESULT rc = checkConsistency(opType, srvType, version);
        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }

        IEE_SLE_Operation opp = null;

        switch (opType)
        {
        case sleOT_bind:
            opp = new EE_SLE_Bind(srvType, version, this.reporter);
            break;
        case sleOT_unbind:
            opp = new EE_SLE_Unbind(srvType, version, this.reporter);
            break;
        case sleOT_stop:
            opp = new EE_SLE_Stop(srvType, version, this.reporter);
            break;
        case sleOT_scheduleStatusReport:
            opp = new EE_SLE_ScheduleStatusReport(srvType, version, this.reporter);
            break;
        case sleOT_peerAbort:
            opp = new EE_SLE_PeerAbort(srvType, version, this.reporter);
            break;
        case sleOT_transferBuffer: // only valid for "return-services"
            opp = new EE_SLE_TransferBuffer(srvType, version, this.reporter);
            break;
        case sleOT_transferData:
            if (srvType == SLE_ApplicationIdentifier.sleAI_rtnAllFrames)
            {
                opp = new EE_RAF_TransferData(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_rtnChFrames)
            {
                opp = new EE_RCF_TransferData(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_rtnChOcf)
            {
                opp = new EE_ROCF_TransferData(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_fwdCltu)
            {
                opp = new EE_CLTU_TransferData(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt)
            {
                opp = new EE_FSP_TransferData(version, this.reporter);
            }
            break;
        case sleOT_start:
            if (srvType == SLE_ApplicationIdentifier.sleAI_rtnAllFrames)
            {
                opp = new EE_RAF_Start(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_rtnChFrames)
            {
                opp = new EE_RCF_Start(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_rtnChOcf)
            {
                opp = new EE_ROCF_Start(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_fwdCltu)
            {
                opp = new EE_CLTU_Start(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt)
            {
                opp = new EE_FSP_Start(version, this.reporter);
            }
            break;
        case sleOT_syncNotify:
            if (srvType == SLE_ApplicationIdentifier.sleAI_rtnAllFrames)
            {
                opp = new EE_RAF_SyncNotify(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_rtnChFrames)
            {
                opp = new EE_RCF_SyncNotify(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_rtnChOcf)
            {
                opp = new EE_ROCF_SyncNotify(version, this.reporter);
            }
            break;
        case sleOT_getParameter:
            if (srvType == SLE_ApplicationIdentifier.sleAI_rtnAllFrames)
            {
                opp = new EE_RAF_GetParameter(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_rtnChFrames)
            {
                opp = new EE_RCF_GetParameter(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_rtnChOcf)
            {
                opp = new EE_ROCF_GetParameter(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_fwdCltu)
            {
                opp = new EE_CLTU_GetParameter(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt)
            {
                opp = new EE_FSP_GetParameter(version, this.reporter);
            }
            break;
        case sleOT_statusReport:
            if (srvType == SLE_ApplicationIdentifier.sleAI_rtnAllFrames)
            {
                opp = new EE_RAF_StatusReport(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_rtnChFrames)
            {
                opp = new EE_RCF_StatusReport(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_rtnChOcf)
            {
                opp = new EE_ROCF_StatusReport(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_fwdCltu)
            {
                opp = new EE_CLTU_StatusReport(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt)
            {
                opp = new EE_FSP_StatusReport(version, this.reporter);
            }
            break;
        case sleOT_asyncNotify:

            if (srvType == SLE_ApplicationIdentifier.sleAI_fwdCltu)
            {
                opp = new EE_CLTU_AsyncNotify(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt)
            {
                opp = new EE_FSP_AsyncNotify(version, this.reporter);
            }
            break;
        case sleOT_throwEvent:
            if (srvType == SLE_ApplicationIdentifier.sleAI_fwdCltu)
            {
                opp = new EE_CLTU_ThrowEvent(version, this.reporter);
            }
            else if (srvType == SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt)
            {
                opp = new EE_FSP_ThrowEvent(version, this.reporter);
            }
            break;
        case sleOT_invokeDirective:
            if (srvType == SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt)
            {
                opp = new EE_FSP_InvokeDirective(version, this.reporter);
            }
            break;
        }

        T res = null;

        if (opp != null)
        {
            res = opp.queryInterface(iid);

            if (res == null)
            {
                throw new SleApiException(HRESULT.E_NOINTERFACE,
                                          "Specified interface not found when creating operation");
            }
        }

        return res;
    }

    /**
	 * 
	 */
    @Override
    public <T extends ISLE_Operation> T createOperation(Class<T> iid,
                                                        SLE_OpType opType,
                                                        SLE_ApplicationIdentifier srvType,
                                                        int version,
                                                        long minRepCycle) throws SleApiException
    {

        HRESULT rc = checkConsistency(opType, srvType, version);
        if (rc != HRESULT.S_OK)
        {
            throw new SleApiException(rc);
        }

        IEE_SLE_Operation opp = null;

        switch (opType)
        {

        case sleOT_scheduleStatusReport:
            opp = new EE_SLE_ScheduleStatusReport(srvType, version, this.reporter, minRepCycle);
            break;
        default:
        	throw new SleApiException(HRESULT.SLE_E_UNKNOWN, " Invalid Operation Type.");
        }


        T res = null;

        if (opp != null)
        {
            res = opp.queryInterface(iid);

            if (res == null)
            {
                throw new SleApiException(HRESULT.E_NOINTERFACE,
                                          "Specified interface not found when creating operation");
            }
        }

        return res;
    }
    
    
    /**
     * Checks if the supplied operation type is compatible with the supplied
     * service type. If the specified service type does not support the
     * operation type, SLE_E_INCONSISTENT is returned. Note that this member
     * function must be extended whenever a new SLE service support is added.
     * 
     * @param opType
     * @param srvType
     * @param version
     * @return
     */
    private HRESULT checkConsistency(SLE_OpType opType, SLE_ApplicationIdentifier srvType, int version)
    {
        // check for supported type and common operation-type
        //
        // NOTE: in case the peer sends a BIND invocation for a
        // not supported service type, we must nevertheless
        // allow to create such a BIND operation object.
        // otherwise we would not be able to send back
        // a negative return for that operation!
        if (opType != SLE_OpType.sleOT_bind && !isSupported(srvType, version))
        {
            return HRESULT.SLE_E_INCONSISTENT;
        }

        if (isCommon(opType))
        {
            return HRESULT.S_OK;
        }

        switch (srvType)
        {
        case sleAI_rtnAllFrames:
        {
            switch (opType)
            {
            case sleOT_transferBuffer:
            case sleOT_syncNotify:
            case sleOT_transferData:
            case sleOT_start:
            case sleOT_statusReport:
            case sleOT_getParameter:
                return HRESULT.S_OK;
            default:
                break;
            }
            break;
        }
        case sleAI_rtnChFrames:
        {
            switch (opType)
            {
            case sleOT_transferBuffer:
            case sleOT_syncNotify:
            case sleOT_transferData:
            case sleOT_start:
            case sleOT_statusReport:
            case sleOT_getParameter:
                return HRESULT.S_OK;
            default:
                break;
            }
            break;
        }
        case sleAI_rtnChOcf:
        {
            switch (opType)
            {
            case sleOT_transferBuffer:
            case sleOT_syncNotify:
            case sleOT_transferData:
            case sleOT_start:
            case sleOT_statusReport:
            case sleOT_getParameter:
                return HRESULT.S_OK;
            default:
                break;
            }
            break;
        }
        case sleAI_fwdTcSpacePkt:
        {
            switch (opType)
            {
            case sleOT_transferBuffer:
            case sleOT_asyncNotify:
            case sleOT_transferData:
            case sleOT_start:
            case sleOT_statusReport:
            case sleOT_getParameter:
            case sleOT_throwEvent:
            case sleOT_invokeDirective:
                return HRESULT.S_OK;
            default:
                break;
            }
            break;
        }
        case sleAI_fwdCltu:
        {
            switch (opType)
            {
            case sleOT_transferBuffer:
            case sleOT_asyncNotify:
            case sleOT_transferData:
            case sleOT_start:
            case sleOT_statusReport:
            case sleOT_getParameter:
            case sleOT_throwEvent:
                return HRESULT.S_OK;
            default:
                break;
            }
            break;
        }
        default:
            return HRESULT.SLE_E_INCONSISTENT;
        }

        return HRESULT.SLE_E_INCONSISTENT;
    }

    /**
     * Returns true if the supplied service type is supported by the operations
     * component. Note that this function must be extended if support for new
     * service types is added.
     * 
     * @param srvType
     * @param version
     * @return
     */
    private boolean isSupported(SLE_ApplicationIdentifier srvType, int version)
    {
        switch (srvType)
        {
        case sleAI_rtnAllFrames:
        case sleAI_rtnChFrames:
        case sleAI_fwdCltu:
            if (version == 1 || version == 2 || version == 3 || version == 4 || version == 5)
            {
                return true;
            }
            break;
        case sleAI_rtnChOcf:
        case sleAI_fwdTcSpacePkt:
            if (version == 1 || version == 2 || version == 4 || version == 5)
            {
                return true;
            }
            break;
        default:
            break;
        }

        return false;
    }

    /**
     * Returns true if the supplied operation type is a common operation.
     * 
     * @param opType
     * @return
     */
    private boolean isCommon(SLE_OpType opType)
    {
        if (opType == SLE_OpType.sleOT_bind || opType == SLE_OpType.sleOT_unbind || opType == SLE_OpType.sleOT_stop
            || opType == SLE_OpType.sleOT_peerAbort || opType == SLE_OpType.sleOT_scheduleStatusReport)
        {
            return true;
        }

        return false;
    }

    @Override
    public String toString()
    {
        return "EE_SLE_OpFactory [reporter=" + ((this.reporter != null) ? this.reporter : "") + "]";
    }

}
