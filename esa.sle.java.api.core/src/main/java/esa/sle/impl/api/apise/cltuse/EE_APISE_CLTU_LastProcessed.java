package esa.sle.impl.api.apise.cltuse;

import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.icltu.ICLTU_StatusReport;
import ccsds.sle.api.isrv.icltu.types.CLTU_Status;
import esa.sle.impl.api.apise.slese.EE_APISE_MTSStatusInformation;

/**
 * The class defines the parameters maintained by the service instance for the
 * last CLTU for which radiation has been attempted. The client is responsible
 * to lock/unlock the object. Note that the accessor and modifier functions
 * get_<Attribute> and set_<Attribute> are generated automatically for the
 * public interface. . The functions <get/set>_radiationStartTime() cannot be
 * generated automatically, because they need extra coding.
 */
public class EE_APISE_CLTU_LastProcessed extends EE_APISE_MTSStatusInformation
{
    /**
     * The cltu Id of the last processed CLTU.
     */
    private long cltuId = 0;

    /**
     * The radiation start time.
     */
    private ISLE_Time radiationStartTime = null;

    /**
     * The CLTU status.
     */
    private CLTU_Status cltuStatus = CLTU_Status.cltuST_invalid;

    public EE_APISE_CLTU_LastProcessed()
    {
        this.cltuId = 0;
        this.radiationStartTime = null;
        this.cltuStatus = CLTU_Status.cltuST_invalid;
    }

    public void setRadiationStartTime(ISLE_Time startTime)
    {
        if (startTime != null)
        {
            this.radiationStartTime = startTime.copy();
        }
        else
        {
            this.radiationStartTime = null;
        }
    }

    /**
     * Initializes the supplied status-report-operation with the current status
     * information data.
     */
    public void setUpReport(ICLTU_StatusReport sr)
    {
        sr.setCltuLastProcessed(this.cltuId);
        sr.setCltuStatus(this.cltuStatus);
        if (this.radiationStartTime != null)
        {
            sr.setRadiationStartTime(this.radiationStartTime);
        }

    }

    public long getCltuId()
    {
        return this.cltuId;
    }

    public void setCltuId(long cltuId)
    {
        this.cltuId = cltuId;
    }

    public CLTU_Status getCltuStatus()
    {
        return this.cltuStatus;
    }

    public void setCltuStatus(CLTU_Status cltuStatus)
    {
        this.cltuStatus = cltuStatus;
    }

    public ISLE_Time getRadiationStartTime()
    {
        return this.radiationStartTime;
    }

}
