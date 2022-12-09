package esa.sle.impl.api.apise.rcfse;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isrv.ircf.IRCF_GetParameter;
import ccsds.sle.api.isrv.ircf.IRCF_StatusReport;
import ccsds.sle.api.isrv.ircf.types.RCF_Gvcid;
import ccsds.sle.api.isrv.ircf.types.RCF_LockStatus;
import ccsds.sle.api.isrv.ircf.types.RCF_ParameterName;
import ccsds.sle.api.isrv.ircf.types.RCF_ProductionStatus;
import esa.sle.impl.api.apise.slese.EE_APISE_MTSStatusInformation;

/**
 * The class holds all RCF status information parameters The client is
 * responsible to lock/unlock the object. The class also offers member functions
 * that set the desired status-parameter for a GET-PARAMETER invocation and for
 * a STATUS-REPORT invocation. Note that the accessor and modifier functions
 * get_<Attribute> and set_<Attribute> are generated automatically for the
 * public attributes.
 */
public class EE_APISE_RCF_StatusInformation extends EE_APISE_MTSStatusInformation
{
    /**
     * The total number of frames delivered.
     */
    private long numFrames = 0;

    /**
     * The lock status of the frame synchronisation process.
     */
    private RCF_LockStatus frameSyncLock = RCF_LockStatus.rcfLS_invalid;

    /**
     * The lock status of the symbol synchronisation process.
     */
    private RCF_LockStatus symbolSyncLock = RCF_LockStatus.rcfLS_invalid;

    /**
     * The lock status of the sub-carrier demodulation process.
     */
    private RCF_LockStatus subCarrDemodLock = RCF_LockStatus.rcfLS_invalid;

    /**
     * The lock status of the carrier demodulation process.
     */
    private RCF_LockStatus carrierDemodLock = RCF_LockStatus.rcfLS_invalid;

    /**
     * The RCF production status.
     */
    private RCF_ProductionStatus productionStatus = RCF_ProductionStatus.rcfPS_invalid;

    /**
     * The requested global VCID.
     */
    private RCF_Gvcid reqGlobalVcId = null;


    @SuppressWarnings("unused")
    private EE_APISE_RCF_StatusInformation(final EE_APISE_RCF_StatusInformation right)
    {
        this.numFrames = right.numFrames;
        this.frameSyncLock = right.frameSyncLock;
        this.symbolSyncLock = right.symbolSyncLock;
        this.subCarrDemodLock = right.subCarrDemodLock;
        this.carrierDemodLock = right.carrierDemodLock;
        this.productionStatus = right.productionStatus;
        this.reqGlobalVcId = right.reqGlobalVcId;
    }

    public EE_APISE_RCF_StatusInformation()
    {
        this.numFrames = 0;
        this.frameSyncLock = RCF_LockStatus.rcfLS_invalid;
        this.symbolSyncLock = RCF_LockStatus.rcfLS_invalid;
        this.subCarrDemodLock = RCF_LockStatus.rcfLS_invalid;
        this.carrierDemodLock = RCF_LockStatus.rcfLS_invalid;
        this.productionStatus = RCF_ProductionStatus.rcfPS_invalid;
        this.reqGlobalVcId = null;
    }

    public long getNumFrames()
    {
        return this.numFrames;
    }

    public void setNumFrames(long numFrames)
    {
        this.numFrames = numFrames;
    }

    public RCF_LockStatus getFrameSyncLock()
    {
        return this.frameSyncLock;
    }

    public void setFrameSyncLock(RCF_LockStatus frameSyncLock)
    {
        this.frameSyncLock = frameSyncLock;
    }

    public RCF_LockStatus getSymbolSyncLock()
    {
        return this.symbolSyncLock;
    }

    public void setSymbolSyncLock(RCF_LockStatus symbolSyncLock)
    {
        this.symbolSyncLock = symbolSyncLock;
    }

    public RCF_LockStatus getSubCarrDemodLock()
    {
        return this.subCarrDemodLock;
    }

    public void setSubCarrDemodLock(RCF_LockStatus subCarrDemodLock)
    {
        this.subCarrDemodLock = subCarrDemodLock;
    }

    public RCF_LockStatus getCarrierDemodLock()
    {
        return this.carrierDemodLock;
    }

    public void setCarrierDemodLock(RCF_LockStatus carrierDemodLock)
    {
        this.carrierDemodLock = carrierDemodLock;
    }

    public RCF_ProductionStatus getProductionStatus()
    {
        return this.productionStatus;
    }

    public void setProductionStatus(RCF_ProductionStatus productionStatus)
    {
        this.productionStatus = productionStatus;
    }

    public RCF_Gvcid getReqGlobalVcId()
    {
        return this.reqGlobalVcId;
    }

    /**
     * Initializes the supplied status-report-operation with the current status
     * information data
     */
    public void setUpReport(IRCF_StatusReport sr)
    {
        sr.setNumFrames(this.numFrames);
        sr.setFrameSyncLock(this.frameSyncLock);
        sr.setCarrierDemodLock(this.carrierDemodLock);
        sr.setSubCarrierDemodLock(this.subCarrDemodLock);
        sr.setSymbolSyncLock(this.symbolSyncLock);
        sr.setProductionStatus(this.productionStatus);
    }

    /**
     * Initializes the supplied RCF-GET-PARAMETER-operation with the current
     * status information data. If the requested parameter is 'requesued-global
     * VcId' and it is 0, it takes the first GvcId from the supplied permitted
     * GvcId List.
     */
    public HRESULT setUpGetParameter(IRCF_GetParameter prm, RCF_Gvcid[] permGvcIdList)
    {

        HRESULT rc = HRESULT.SLE_E_UNKNOWN;

        RCF_ParameterName pname = prm.getRequestedParameter();

        if (pname == RCF_ParameterName.rcfPN_requestedGvcid)
        {
            if (getVersion() == 2 || getVersion() == 3)
            {
                prm.setRequestedGvcid(this.reqGlobalVcId);
            }
            else
            {
                if (this.reqGlobalVcId != null)
                {
                    prm.setRequestedGvcid(this.reqGlobalVcId);
                }
                else
                {
                    prm.setRequestedGvcid(permGvcIdList[0]);
                }
            }
            rc = HRESULT.S_OK;
        }
        return rc;
    }

    /**
     * Sets the Global VCID requested via the RCF_Start operation
     */
    public void setReqGlobalVcId(final RCF_Gvcid pgvcId)
    {
        if (this.reqGlobalVcId != null)
        {
            this.reqGlobalVcId = null;
        }
        if (pgvcId != null)
        {
            this.reqGlobalVcId = new RCF_Gvcid();
            this.reqGlobalVcId = pgvcId;
        }
    }

}
