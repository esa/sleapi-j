package esa.sle.impl.api.apise.cltuse;

import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.icltu.ICLTU_StatusReport;
import esa.sle.impl.api.apise.slese.EE_APISE_MTSStatusInformation;

/**
 * The class defines the parameters maintained by the service instance for the
 * last CLTU for which radiation has completed. The client is responsible to
 * lock/unlock the object. Note that the accessor and modifier functions
 * get_<Attribute> and set_<Attribute> are generated automatically for the
 * public interface. The functions <get/set>_radiationStopTime() cannot be
 * generated automatically, because they need extra coding
 */
public class EE_APISE_CLTU_LastOK extends EE_APISE_MTSStatusInformation
{
    /**
     * The cltu Id of the last OK processed CLTU.
     */
    private long cltuId = 0;

    /**
     * The radiation stop time.
     */
    private ISLE_Time radiationStopTime = null;

    
    public EE_APISE_CLTU_LastOK()
    {
        this.cltuId = 0;
        this.radiationStopTime = null;
    }

    public long getCltuId()
    {
        return this.cltuId;
    }

    public void setCltuId(long cltuId)
    {
        this.cltuId = cltuId;
    }

    public void setRadiationStopTime(final ISLE_Time stopTime)
    {
        if (stopTime != null)
        {
            this.radiationStopTime = stopTime.copy();
        }
        else
        {
            this.radiationStopTime = null;
        }
    }

    /**
     * Returns the radiation stop time.
     */
    public ISLE_Time getRadiationStopTime()
    {
        return this.radiationStopTime;
    }

    /**
     * Initializes the supplied status-report-operation with the current status
     * information data.
     */
    public void setUpReport(ICLTU_StatusReport sr)
    {
        sr.setCltuLastOk(this.cltuId);
        if (this.radiationStopTime != null)
        {
            sr.setRadiationStopTime(this.radiationStopTime);
        }
    }

}
