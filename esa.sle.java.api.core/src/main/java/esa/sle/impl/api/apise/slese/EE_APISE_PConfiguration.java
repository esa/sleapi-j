/**
 * @(#) EE_APISE_PConfiguration.java
 */

package esa.sle.impl.api.apise.slese;

import ccsds.sle.api.isle.it.SLE_DeliveryMode;

/**
 * The class holds all service configuration parameters that are common for all
 * provider service instances. The class is foreseen for usage and inheritance.
 * For the convenient of inheriting classes, EE_APISE_PConfiguration inherits
 * from MTSobject. Note that the accessor and modifier functions get_<Attribute>
 * and set_<Attribute> are generated automatically on the public interface.
 */
public class EE_APISE_PConfiguration
{
    /**
     * The delivery mode
     */
    private SLE_DeliveryMode deliveryMode;

    /**
     * The minimum reporting cycle
     * New since SLES V5.
     */
    private long minimumReportingCycle; 
    
    /**
     * Constructor with no argument
     */
    public EE_APISE_PConfiguration()
    {
        this.deliveryMode = SLE_DeliveryMode.sleDM_invalid;
        this.minimumReportingCycle = 1;
    }

    @SuppressWarnings("unused")
    private EE_APISE_PConfiguration(EE_APISE_PConfiguration right)
    {
        this.deliveryMode = right.deliveryMode;
    }

    public SLE_DeliveryMode getDeliveryMode()
    {
        return this.deliveryMode;
    }

    public void setDeliveryMode(SLE_DeliveryMode deliveryMode)
    {
        this.deliveryMode = deliveryMode;
    }
    
    /**
     * @return
     */
    public long getMinimumReportingCycle() {
		return minimumReportingCycle;
	}

    /**
     * @param minimumReportingCycle
     */
	public void setMinimumReportingCycle(long minimumReportingCycle) {
		this.minimumReportingCycle = minimumReportingCycle;
	}
}
