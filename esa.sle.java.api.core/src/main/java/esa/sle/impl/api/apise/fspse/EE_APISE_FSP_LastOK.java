/**
 * @(#) EE_APISE_FSP_LastOK.java
 */

package esa.sle.impl.api.apise.fspse;

import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.ifsp.IFSP_StatusReport;
import esa.sle.impl.api.apise.slese.EE_APISE_MTSStatusInformation;

/**
 * The class defines the parameters maintained by the service instance for the
 * last packet for which radiation has completed. The client is responsible to
 * lock/unlock the object. Note that the accessor and modifier functions
 * get_<Attribute> and set_<Attribute> are generated automatically for the
 * public interface. The functions <get/set>_radiationStopTime() cannot be
 * generated automatically, because they need extra coding.
 */
public class EE_APISE_FSP_LastOK extends EE_APISE_MTSStatusInformation
{
    /**
     * The packet Id of the last OK processed packet.
     */
    private long packetId = 0;

    /**
     * The production stop time.
     */
    private ISLE_Time productionStopTime = null;


    @SuppressWarnings("unused")
    private EE_APISE_FSP_LastOK(final EE_APISE_FSP_LastOK right)
    {
        this.packetId = right.packetId;
        this.productionStopTime = right.productionStopTime;
    }

    public EE_APISE_FSP_LastOK()
    {
        this.packetId = 0;
        this.productionStopTime = null;
    }

    public void setProductionStopTime(ISLE_Time stopTime)
    {
        if (stopTime != null)
        {
            this.productionStopTime = stopTime.copy();
        }
    }

    public ISLE_Time getProductionStopTime()
    {
        return this.productionStopTime;
    }

    public void setUpReport(IFSP_StatusReport sr)
    {
        sr.setPacketLastOk(this.packetId);
        if (this.productionStopTime != null)
        {
            sr.setProductionStopTime(this.productionStopTime);
        }

    }

    public long getPacketId()
    {
        return this.packetId;
    }

    public void setPacketId(long value)
    {
        this.packetId = value;
    }

}
