/**
 * @(#) EE_APISE_CLTU_StatusInformation.java
 */

package esa.sle.impl.api.apise.cltuse;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isrv.icltu.ICLTU_GetParameter;
import ccsds.sle.api.isrv.icltu.ICLTU_StatusReport;
import ccsds.sle.api.isrv.icltu.types.CLTU_ParameterName;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProductionStatus;
import ccsds.sle.api.isrv.icltu.types.CLTU_UplinkStatus;
import esa.sle.impl.api.apise.slese.EE_APISE_MTSStatusInformation;

/**
 * The class holds all CLTU status information parameters. These are update via
 * the interface ICLTU_SIUpdate.@EndResponsibility The client is responsible to
 * lock/unlock the object.@EndBehaviour Note that the accessor and modifier
 * functions get_<Attribute> and set_<Attribute> are generated automatically for
 * the public interface. ///////////////////////////////////////////////////////
 */
public class EE_APISE_CLTU_StatusInformation extends EE_APISE_MTSStatusInformation
{
    /**
     * The CLTU production status
     */
    private CLTU_ProductionStatus productionStatus = CLTU_ProductionStatus.cltuPS_invalid;

    /**
     * The size of the available CLTU buffer.
     */
    private long cltuBufferAvailable = 0;

    /**
     * The number of CLTUs received. This number is incremented by one for every
     * TRANSFER-DATA return with posistive result.
     */
    private long numCltusReceived = 0;

    /**
     * The number of CLTUs for which radiation has been attempted. This number
     * is incremented in CltuStarted() and CltuNotStarted().
     */
    private long numCltusProcessed = 0;

    /**
     * The number of CLTUs which have been radiated. This number shall be
     * incremented in CltuRadiated.
     */
    private long numCltusRadiated = 0;

    /**
     * The next expected CLTU Id.
     */
    private long expectedCltuId = 0;

    /**
     * The next expected event invocation Id.
     */
    private long expectedEventInvId = 0;
    
    /**
     * The maximum size in byte of the CLTU buffer supported by the provider.
     */
    private long maxBufferSize = 0;

    /**
     * The CLTU uplink status
     */
    private CLTU_UplinkStatus uplinkStatus = CLTU_UplinkStatus.cltuUS_invalid;


    public EE_APISE_CLTU_StatusInformation()
    {
        this.productionStatus = CLTU_ProductionStatus.cltuPS_invalid;
        this.cltuBufferAvailable = 0;
        this.numCltusReceived = 0;
        this.numCltusProcessed = 0;
        this.numCltusRadiated = 0;
        this.expectedCltuId = 0;
        this.expectedEventInvId = 0;
        this.setMaxBufferSize(0);
        this.uplinkStatus = CLTU_UplinkStatus.cltuUS_invalid;

    }

    /**
     * Increments the number of processed CLTUs.
     */

    public void incrNumProcessed()
    {
        this.numCltusProcessed++;
    }

    /**
     * Increments the number of radiated CLTUs.
     */
    public void incrNumRadiated()
    {
        this.numCltusRadiated++;
    }

    /**
     * Increments the number of received CLTUs.
     */
    public void incrNumReceived()
    {
        this.numCltusReceived++;
    }

    /**
     * Initialises the supplied status-report-operation with the current status
     * information data.
     */
    public void setUpReport(ICLTU_StatusReport sr)
    {

        sr.setProductionStatus(this.productionStatus);
        sr.setUplinkStatus(this.uplinkStatus);
        sr.setNumberOfCltusReceived(this.numCltusReceived);
        sr.setNumberOfCltusProcessed(this.numCltusProcessed);
        sr.setNumberOfCltusRadiated(this.numCltusRadiated);
        sr.setCltuBufferAvailable(this.cltuBufferAvailable);
    }

    /**
     * Initializes the supplied CLTU-GET-PARAMETER-operation with the current
     * status information data.
     */
    public HRESULT setUpGetParameter(ICLTU_GetParameter prm)
    {

        HRESULT rc = HRESULT.SLE_E_UNKNOWN;

        CLTU_ParameterName pname = prm.getRequestedParameter();

        if (pname == CLTU_ParameterName.cltuPN_expectedSlduIdentification)
        {
            prm.setExpectedCltuId(this.expectedCltuId);
            rc = HRESULT.S_OK;
        }
        else if (pname == CLTU_ParameterName.cltuPN_expectedEventInvocationId)
        {
            prm.setExpectedEventInvocationId(this.expectedEventInvId);
            rc = HRESULT.S_OK;
        }

        return rc;
    }

    public CLTU_ProductionStatus getProductionStatus()
    {
        return this.productionStatus;
    }

    public void setProductionStatus(CLTU_ProductionStatus productionStatus)
    {
        this.productionStatus = productionStatus;
    }

    public long getCltuBufferAvailable()
    {
        return this.cltuBufferAvailable;
    }

    public void setCltuBufferAvailable(long cltuBufferAvailable)
    {
        this.cltuBufferAvailable = cltuBufferAvailable;
    }

    public long getNumCltusReceived()
    {
        return this.numCltusReceived;
    }

    public void setNumCltusReceived(long numCltusReceived)
    {
        this.numCltusReceived = numCltusReceived;
    }

    public long getNumCltusProcessed()
    {
        return this.numCltusProcessed;
    }

    public void setNumCltusProcessed(long numCltusProcessed)
    {
        this.numCltusProcessed = numCltusProcessed;
    }

    public long getNumCltusRadiated()
    {
        return this.numCltusRadiated;
    }

    public void setNumCltusRadiated(long numCltusRadiated)
    {
        this.numCltusRadiated = numCltusRadiated;
    }

    public long getExpectedCltuId()
    {
        return this.expectedCltuId;
    }

    public void setExpectedCltuId(long expectedCltuId)
    {
        this.expectedCltuId = expectedCltuId;
    }

    public long getExpectedEventInvId()
    {
        return this.expectedEventInvId;
    }

    public void setExpectedEventInvId(long expectedEventInvId)
    {
        this.expectedEventInvId = expectedEventInvId;
    }

    public CLTU_UplinkStatus getUplinkStatus()
    {
        return this.uplinkStatus;
    }

    public void setUplinkStatus(CLTU_UplinkStatus uplinkStatus)
    {
        this.uplinkStatus = uplinkStatus;
    }

	public long getMaxBufferSize() {
		return maxBufferSize;
	}

	public void setMaxBufferSize(long maxBufferSize) {
		this.maxBufferSize = maxBufferSize;
	}

}
