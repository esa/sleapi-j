package esa.sle.impl.api.apise.fspse;

import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.ifsp.IFSP_StatusReport;
import ccsds.sle.api.isrv.ifsp.types.FSP_PacketStatus;
import esa.sle.impl.api.apise.slese.EE_APISE_MTSStatusInformation;

/**
 * The class defines the parameters maintained by the service instance for the
 * last packet for which radiation has been attempted. The client is responsible
 * to lock/unlock the object. Note that the accessor and modifier functions
 * get_<Attribute> and set_<Attribute> are generated automatically for the
 * public interface. . The functions <get/set>_radiationStartTime() cannot be
 * generated automatically, because they need extra coding
 */
public class EE_APISE_FSP_LastProcessed extends EE_APISE_MTSStatusInformation
{
    /**
     * The packet Id of the last processed packet.
     */
    private long packetId = 0;

    /**
     * The production start time.
     */
    private ISLE_Time productionStartTime = null;

    /**
     * The packet status.
     */
    private FSP_PacketStatus packetStatus = FSP_PacketStatus.fspST_invalid;


    public EE_APISE_FSP_LastProcessed()
    {}

    /**
     * Sets the production start time.
     */
    public void setProductionStartTime(ISLE_Time startTime)
    {
        if (this.productionStartTime != null)
        {
            this.productionStartTime = null;
        }
        // start time can also be 0.
        if (startTime != null)
        {
            this.productionStartTime = startTime.copy();
        }
    }

    public ISLE_Time getProductionStartTime()
    {
        return this.productionStartTime;
    }

    public void setUpReport(IFSP_StatusReport sr)
    {

        sr.setPacketLastProcessed(this.packetId);
        sr.setPacketStatus(this.packetStatus);
        if (this.productionStartTime != null)
        {
            sr.setProductionStartTime(this.productionStartTime);
        }
    }

    public boolean isContainedIn(long[] affectedPackets)
    {

        for (long affectedPacket : affectedPackets)
        {
            if (affectedPacket == this.packetId)
            {
                return true;
            }
        }

        return false;

    }

    public final long getPacketId()
    {
        return this.packetId;
    }

    public void setPacketId(long value)
    {
        this.packetId = value;
    }

    public final FSP_PacketStatus getPacketStatus()
    {
        return this.packetStatus;
    }

    public void setPacketStatus(FSP_PacketStatus value)
    {
        this.packetStatus = value;
    }

}
