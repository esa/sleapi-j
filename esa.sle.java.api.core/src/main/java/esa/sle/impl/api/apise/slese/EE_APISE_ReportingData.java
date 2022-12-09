/**
 * @(#) EE_APISE_ReportingData.java
 */

package esa.sle.impl.api.apise.slese;

import ccsds.sle.api.isle.exception.HRESULT;

/**
 * Holds the minimum and maximum reporting cycle.
 */
public class EE_APISE_ReportingData
{
    /**
     * The token name for the minimum reporting cycle.
     */
    private static String minRepCycleName = "Min_ReportingCycle";

    /**
     * The token name for the maximum reporting cycle.
     */
    private static String maxRepCycleName = "Max_ReportingCycle";

    /**
     * The minimum reporting cycle in seconds.
     */
    private int minReportingCycle;

    /**
     * The maximum reporting cycle in seconds.
     */
    private int maxReportingCycle;


    /**
     * Constructor
     * 
     * @param minReportingCycle
     * @param maxReportingCycle
     */
    public EE_APISE_ReportingData()
    {
        this.minReportingCycle = 0;
        this.maxReportingCycle = 0;
    }

    /**
     * Constructor
     * 
     * @param minReportingCycle
     * @param maxReportingCycle
     */
    @SuppressWarnings("unused")
    private EE_APISE_ReportingData(EE_APISE_Database right)
    {
        this.minReportingCycle = 0;
        this.maxReportingCycle = 0;
    }

    /**
     * Sets the value of the attribute identified by the supplied name. If any
     * error is detected, or the attribute has already been set, E_FAIL is
     * returned.
     */
    public HRESULT setValue(String name, String value)
    {
        if (name.compareToIgnoreCase(minRepCycleName) == 0)
        {
            if (this.minReportingCycle > 0)
            {
                return HRESULT.SLE_E_DUPLICATE; // already set
            }

            int theVal = Integer.parseInt(value);

            if (theVal <= 0)
            {
                return HRESULT.E_INVALIDARG;
            }

            this.minReportingCycle = theVal;
        }
        else if (name.compareToIgnoreCase(maxRepCycleName) == 0)
        {
            if (this.maxReportingCycle > 0)
            {
                return HRESULT.SLE_E_DUPLICATE; // already set
            }

            int theVal = Integer.parseInt(value);
            if (theVal <= 0)
            {
                return HRESULT.E_INVALIDARG;
            }
            this.maxReportingCycle = theVal;
        }
        else
        {
            return HRESULT.E_FAIL;
        }
        return HRESULT.S_OK;
    }

    /**
     * @return
     */
    public int getMinReportingCycle()
    {
        return this.minReportingCycle;
    }

    /**
     * @return
     */
    public int getMaxReportingCycle()
    {
        return this.maxReportingCycle;
    }
}
