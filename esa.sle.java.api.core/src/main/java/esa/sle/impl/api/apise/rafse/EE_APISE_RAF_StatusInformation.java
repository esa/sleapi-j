/**
 * @(#) EE_APISE_RAF_StatusInformation.java
 */

package esa.sle.impl.api.apise.rafse;


import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isrv.iraf.IRAF_GetParameter;
import ccsds.sle.api.isrv.iraf.IRAF_StatusReport;
import ccsds.sle.api.isrv.iraf.types.RAF_LockStatus;
import ccsds.sle.api.isrv.iraf.types.RAF_ParFrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_ParameterName;
import ccsds.sle.api.isrv.iraf.types.RAF_ProductionStatus;
import esa.sle.impl.api.apise.slese.EE_APISE_MTSStatusInformation;

/**
 * Status Information The class holds all RAF status information parameters. The
 * client is responsible to lock/unlock the object, which must be done using
 * _lock() and _unlock(). The class offers the service to set-up a SET-PARAMETER
 * operation. The service instance can delegate a SET-PARAMETER invocation to
 * this class (function setUpGetParameter()) , which sets the parameter value if
 * it is one of the attributes of this class. This class also enables the
 * servcie instance to set-up a STATUS-REPORT operation. This is supported by
 * the functon setUpReport(). Note that the accessor and modifier functions
 * get_<Attribute> and set_<Attribute> are generated automatically for the
 * public interface.
 */
public class EE_APISE_RAF_StatusInformation extends EE_APISE_MTSStatusInformation
{
    /**
     * The number of error free frames delivered.
     */
    private long numErrorFreeFrames = 0;

    /**
     * The total number of frames delivered.
     */
    private long numFrames = 0;

    /**
     * The requested frame quality
     */
    private RAF_ParFrameQuality reqFrameQuality = RAF_ParFrameQuality.rafPQ_undefined;

    /**
     * The lock status of the frame synchronisation process.
     */
    private RAF_LockStatus frameSyncLock = RAF_LockStatus.rafLS_invalid;

    /**
     * The lock status of the symbol synchronisation process.
     */
    private RAF_LockStatus symbolSyncLock = RAF_LockStatus.rafLS_invalid;

    /**
     * The lock status of the sub-carrier demodulation process.
     */
    private RAF_LockStatus subCarrDemodLock = RAF_LockStatus.rafLS_invalid;

    /**
     * The lock status of the carrier demodulation process.
     */
    private RAF_LockStatus carrierDemodLock = RAF_LockStatus.rafLS_invalid;

    /**
     * The RAF production status.
     */
    private RAF_ProductionStatus productionStatus = RAF_ProductionStatus.rafPS_invalid;


    public long getNumErrorFreeFrames()
    {
        return this.numErrorFreeFrames;
    }

    public void setNumErrorFreeFrames(long numErrorFreeFrames)
    {
        this.numErrorFreeFrames = numErrorFreeFrames;
    }

    public long getNumFrames()
    {
        return this.numFrames;
    }

    public void setNumFrames(long numFrames)
    {
        this.numFrames = numFrames;
    }

    public RAF_ParFrameQuality getReqFrameQuality()
    {
        return this.reqFrameQuality;
    }

    public void setReqFrameQuality(RAF_ParFrameQuality reqFrameQuality)
    {
        this.reqFrameQuality = reqFrameQuality;
    }
    
    public RAF_LockStatus getFrameSyncLock()
    {
        return this.frameSyncLock;
    }

    public void setFrameSyncLock(RAF_LockStatus frameSyncLock)
    {
        this.frameSyncLock = frameSyncLock;
    }

    public RAF_LockStatus getSymbolSyncLock()
    {
        return this.symbolSyncLock;
    }

    public void setSymbolSyncLock(RAF_LockStatus symbolSyncLock)
    {
        this.symbolSyncLock = symbolSyncLock;
    }

    public RAF_LockStatus getSubCarrDemodLock()
    {
        return this.subCarrDemodLock;
    }

    public void setSubCarrDemodLock(RAF_LockStatus subCarrDemodLock)
    {
        this.subCarrDemodLock = subCarrDemodLock;
    }

    public RAF_LockStatus getCarrierDemodLock()
    {
        return this.carrierDemodLock;
    }

    public void setCarrierDemodLock(RAF_LockStatus carrierDemodLock)
    {
        this.carrierDemodLock = carrierDemodLock;
    }

    public RAF_ProductionStatus getProductionStatus()
    {
        return this.productionStatus;
    }

    public void setProductionStatus(RAF_ProductionStatus productionStatus)
    {
        this.productionStatus = productionStatus;
    }

    @SuppressWarnings("unused")
    private EE_APISE_RAF_StatusInformation(final EE_APISE_RAF_StatusInformation right)
    {
        this.numErrorFreeFrames = right.numErrorFreeFrames;
        this.numFrames = right.numFrames;
        this.reqFrameQuality = right.reqFrameQuality;
        this.frameSyncLock = right.frameSyncLock;
        this.symbolSyncLock = right.symbolSyncLock;
        this.subCarrDemodLock = right.subCarrDemodLock;
        this.carrierDemodLock = right.carrierDemodLock;
        this.productionStatus = right.productionStatus;
    }

    public EE_APISE_RAF_StatusInformation()
    {
        this.numErrorFreeFrames = 0;
        this.numFrames = 0;
        this.reqFrameQuality = RAF_ParFrameQuality.rafPQ_undefined;
        this.frameSyncLock = RAF_LockStatus.rafLS_invalid;
        this.symbolSyncLock = RAF_LockStatus.rafLS_invalid;
        this.subCarrDemodLock = RAF_LockStatus.rafLS_invalid;
        this.carrierDemodLock = RAF_LockStatus.rafLS_invalid;
        this.productionStatus = RAF_ProductionStatus.rafPS_invalid;
    }

    /**
     * Initializes the supplied status-report-operation with the current status
     * information data.
     */
    public void setUpReport(IRAF_StatusReport sr)
    {
        sr.setNumErrorFreeFrames(this.numErrorFreeFrames);
        sr.setNumFrames(this.numFrames);
        sr.setFrameSyncLock(this.frameSyncLock);
        sr.setCarrierDemodLock(this.carrierDemodLock);
        sr.setSubCarrierDemodLock(this.subCarrDemodLock);
        sr.setSymbolSyncLock(this.symbolSyncLock);
        sr.setProductionStatus(this.productionStatus);
    }

    /**
     * Initializes the supplied RAF-GET-PARAMETER-operation with the current
     * status information data.
     */
    public HRESULT setUpGetParameter(IRAF_GetParameter prm)
    {
        HRESULT rc = HRESULT.SLE_E_UNKNOWN;
        RAF_ParameterName pname = prm.getRequestedParameter();
        if (pname == RAF_ParameterName.rafPN_requestFrameQuality)
        {
            prm.setRequestedFrameQuality(this.reqFrameQuality);
            rc = HRESULT.S_OK;
        }
        return rc;
    }

}
