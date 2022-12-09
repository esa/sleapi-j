/**
 * @(#) EE_APISE_ROCF_StatusInformation.java
 */

package esa.sle.impl.api.apise.rocfse;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isrv.irocf.IROCF_GetParameter;
import ccsds.sle.api.isrv.irocf.IROCF_StatusReport;
import ccsds.sle.api.isrv.irocf.types.ROCF_ControlWordType;
import ccsds.sle.api.isrv.irocf.types.ROCF_Gvcid;
import ccsds.sle.api.isrv.irocf.types.ROCF_LockStatus;
import ccsds.sle.api.isrv.irocf.types.ROCF_ParameterName;
import ccsds.sle.api.isrv.irocf.types.ROCF_ProductionStatus;
import ccsds.sle.api.isrv.irocf.types.ROCF_UpdateMode;
import esa.sle.impl.api.apise.slese.EE_APISE_MTSStatusInformation;

/**
 * The class holds all ROCF status information parameters. The client is
 * responsible to lock/unlock the object. The class also offers member functions
 * that set the desired status-parameter for a GET-PARAMETER invocation and for
 * a STATUS-REPORT invocation. Note that the accessor and modifier functions
 * get_<Attribute> and set_<Attribute> are generated automatically for the
 * public attributes.
 */
public class EE_APISE_ROCF_StatusInformation extends EE_APISE_MTSStatusInformation
{
    /**
     * The total number of frames processed.
     */
    private long numFramesProcessed = 0;

    /**
     * The total number of OCFs delivered.
     */
    private long numOcfDelivered = 0;

    /**
     * The lock status of the frame synchronisation process.
     */
    private ROCF_LockStatus frameSyncLock = ROCF_LockStatus.rocfLS_invalid;

    /**
     * The lock status of the symbol synchronisation process.
     */
    private ROCF_LockStatus symbolSyncLock = ROCF_LockStatus.rocfLS_invalid;

    /**
     * The lock status of the sub-carrier demodulation process.
     */
    private ROCF_LockStatus subCarrDemodLock = ROCF_LockStatus.rocfLS_invalid;

    /**
     * The lock status of the carrier demodulation process.
     */
    private ROCF_LockStatus carrierDemodLock = ROCF_LockStatus.rocfLS_invalid;

    /**
     * The ROCF production status.
     */
    private ROCF_ProductionStatus productionStatus = ROCF_ProductionStatus.rocfPS_invalid;

    /**
     * The requested global VCID.
     */
    private ROCF_Gvcid reqGlobalVcId = null;

    /**
     * The requested Control Word Type.
     */
    private ROCF_ControlWordType reqControlWordType = ROCF_ControlWordType.rocfCWT_invalid;

    /**
     * The information whether or not a TcVcid has been specified in the
     * previous ROCF-START operation.
     */
    private boolean tcVcidUsed = false;

    /**
     * The requested TcVcid.
     */
    private long reqTcVcid = 0;

    /**
     * The requested Update Mode.
     */
    private ROCF_UpdateMode reqUpdateMode = ROCF_UpdateMode.rocfUM_invalid;


    @SuppressWarnings("unused")
    private EE_APISE_ROCF_StatusInformation(final EE_APISE_ROCF_StatusInformation right)
    {
        this.numFramesProcessed = right.numFramesProcessed;
        this.numOcfDelivered = right.numOcfDelivered;
        this.frameSyncLock = right.frameSyncLock;
        this.symbolSyncLock = right.symbolSyncLock;
        this.subCarrDemodLock = right.subCarrDemodLock;
        this.carrierDemodLock = right.carrierDemodLock;
        this.productionStatus = right.productionStatus;
        this.reqGlobalVcId = right.reqGlobalVcId;
        this.reqControlWordType = right.reqControlWordType;
        this.tcVcidUsed = right.tcVcidUsed;
        this.reqTcVcid = right.reqTcVcid;
        this.reqUpdateMode = right.reqUpdateMode;
    }

    public EE_APISE_ROCF_StatusInformation()
    {
        this.numFramesProcessed = 0;
        this.numOcfDelivered = 0;
        this.frameSyncLock = ROCF_LockStatus.rocfLS_invalid;
        this.symbolSyncLock = ROCF_LockStatus.rocfLS_invalid;
        this.subCarrDemodLock = ROCF_LockStatus.rocfLS_invalid;
        this.carrierDemodLock = ROCF_LockStatus.rocfLS_invalid;
        this.productionStatus = ROCF_ProductionStatus.rocfPS_invalid;
        this.reqGlobalVcId = null;
        this.reqControlWordType = ROCF_ControlWordType.rocfCWT_invalid;
        this.tcVcidUsed = false;
        this.reqTcVcid = 0;
        this.reqUpdateMode = ROCF_UpdateMode.rocfUM_invalid;
    }

    /**
     * Initializes the supplied status-report-operation with the current status
     * information data.
     */
    public void setUpReport(IROCF_StatusReport sr)
    {
        sr.setNumFrames(this.numFramesProcessed);
        sr.setNumOcfDelivered(this.numOcfDelivered);
        sr.setFrameSyncLock(this.frameSyncLock);
        sr.setCarrierDemodLock(this.carrierDemodLock);
        sr.setSubCarrierDemodLock(this.subCarrDemodLock);
        sr.setSymbolSyncLock(this.symbolSyncLock);
        sr.setProductionStatus(this.productionStatus);
    }

    /**
     * Initializes the supplied ROCF-GET-PARAMETER-operation with the current
     * status information data. If the requested parameter is 'requesued-global
     * VcId' and it is 0, it takes the first GvcId from the supplied permitted
     * GvcId List.
     */
    public HRESULT setUpGetParameter(IROCF_GetParameter prm)
    {
        HRESULT rc = HRESULT.SLE_E_UNKNOWN;
        ROCF_ParameterName pname = prm.getRequestedParameter();
        if (pname == ROCF_ParameterName.rocfPN_requestedGvcid)
        {
            prm.setRequestedGvcid(this.reqGlobalVcId);
            rc = HRESULT.S_OK;
        }
        else if (pname == ROCF_ParameterName.rocfPN_requestedControlWordType)
        {
            prm.setRequestedControlWordType(this.reqControlWordType);
            rc = HRESULT.S_OK;
        }
        else if (pname == ROCF_ParameterName.rocfPN_requestedTcVcid)
        {
            if (this.tcVcidUsed)
            {
                prm.setRequestedTcVcid(this.reqTcVcid);
            }
            else
            {
                prm.setRequestedTcVcid(0);
            }
            rc = HRESULT.S_OK;
        }
        else if (pname == ROCF_ParameterName.rocfPN_requestedUpdateMode)
        {
            prm.setRequestedUpdateMode(this.reqUpdateMode);
            rc = HRESULT.S_OK;
        }
        return rc;
    }

    /**
     * Sets the Global VCID requested via the ROCF_Start operation.
     */
    public void setReqGlobalVcId(ROCF_Gvcid pgvcId)
    {
        this.reqGlobalVcId = null;
        if (pgvcId != null)
        {
            this.reqGlobalVcId = new ROCF_Gvcid(pgvcId);
        }
    }

    public long getNumFramesProcessed()
    {
        return this.numFramesProcessed;
    }

    public void setNumFramesProcessed(long numFramesProcessed)
    {
        this.numFramesProcessed = numFramesProcessed;
    }

    public long getNumOcfDelivered()
    {
        return this.numOcfDelivered;
    }

    public void setNumOcfDelivered(long numOcfDelivered)
    {
        this.numOcfDelivered = numOcfDelivered;
    }

    public ROCF_LockStatus getFrameSyncLock()
    {
        return this.frameSyncLock;
    }

    public void setFrameSyncLock(ROCF_LockStatus frameSyncLock)
    {
        this.frameSyncLock = frameSyncLock;
    }

    public ROCF_LockStatus getSymbolSyncLock()
    {
        return this.symbolSyncLock;
    }

    public void setSymbolSyncLock(ROCF_LockStatus symbolSyncLock)
    {
        this.symbolSyncLock = symbolSyncLock;
    }

    public ROCF_LockStatus getSubCarrDemodLock()
    {
        return this.subCarrDemodLock;
    }

    public void setSubCarrDemodLock(ROCF_LockStatus subCarrDemodLock)
    {
        this.subCarrDemodLock = subCarrDemodLock;
    }

    public ROCF_LockStatus getCarrierDemodLock()
    {
        return this.carrierDemodLock;
    }

    public void setCarrierDemodLock(ROCF_LockStatus carrierDemodLock)
    {
        this.carrierDemodLock = carrierDemodLock;
    }

    public ROCF_ProductionStatus getProductionStatus()
    {
        return this.productionStatus;
    }

    public void setProductionStatus(ROCF_ProductionStatus productionStatus)
    {
        this.productionStatus = productionStatus;
    }

    public ROCF_ControlWordType getReqControlWordType()
    {
        return this.reqControlWordType;
    }

    public void setReqControlWordType(ROCF_ControlWordType reqControlWordType)
    {
        this.reqControlWordType = reqControlWordType;
    }

    public boolean getTcVcidUsed()
    {
        return this.tcVcidUsed;
    }

    public void setTcVcidUsed(boolean tcVcidUsed)
    {
        this.tcVcidUsed = tcVcidUsed;
    }

    public long getReqTcVcid()
    {
        return this.reqTcVcid;
    }

    public void setReqTcVcid(long reqTcVcid)
    {
        this.reqTcVcid = reqTcVcid;
    }

    public ROCF_UpdateMode getReqUpdateMode()
    {
        return this.reqUpdateMode;
    }

    public void setReqUpdateMode(ROCF_UpdateMode reqUpdateMode)
    {
        this.reqUpdateMode = reqUpdateMode;
    }

    public ROCF_Gvcid getReqGlobalVcId()
    {
        return this.reqGlobalVcId;
    }

}
