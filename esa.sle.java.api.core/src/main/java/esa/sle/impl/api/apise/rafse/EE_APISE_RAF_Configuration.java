/**
 * @(#) EE_APISE_RAF_Configuration.java
 */

package esa.sle.impl.api.apise.rafse;


import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isrv.iraf.IRAF_GetParameter;
import ccsds.sle.api.isrv.iraf.types.RAF_ParFrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_ParameterName;
import esa.sle.impl.api.apise.slese.EE_APISE_RSConfiguration;

/**
 * RAF Configuration The class holds all configuration parameters. The class
 * offers the service to set-up a SET-PARAMETER operation. The service instance
 * can delegate a SET-PARAMETER invocation to this class (function
 * setUpGetParameter()) , which sets the parameter value if it is one of the
 * attributes of this class.
 */
public class EE_APISE_RAF_Configuration extends EE_APISE_RSConfiguration
{
	private RAF_ParFrameQuality[] permittedFrameQuality = {RAF_ParFrameQuality.rafPQ_goodFramesOnly,RAF_ParFrameQuality.rafPQ_allFrames};
	
    @SuppressWarnings("unused")
    private EE_APISE_RAF_Configuration(final EE_APISE_RAF_Configuration right)
    {}

    /**
     * Initializes the supplied RAF-GET-PARAMETER-operation with the current
     * status information data.
     */
    public EE_APISE_RAF_Configuration()
    {

    }

    public RAF_ParFrameQuality[] getPermittedFrameQuality()
    {
    	return this.permittedFrameQuality;
    }
    
    public void setPermittedFrameQuality(RAF_ParFrameQuality[] permFrameQuality)
    {
    	this.permittedFrameQuality = permFrameQuality;
    }
    
    public HRESULT setUpGetParameter(IRAF_GetParameter prm)
    {
        HRESULT rc = HRESULT.SLE_E_UNKNOWN;
        RAF_ParameterName pname = prm.getRequestedParameter();
        if (pname == RAF_ParameterName.rafPN_bufferSize)
        {
            prm.setTransferBufferSize(getTransferBufferSize());
            rc = HRESULT.S_OK;
        }
        else if (pname == RAF_ParameterName.rafPN_deliveryMode)
        {
            prm.setDeliveryMode(getDeliveryMode().asRAF_DeliveryMode());
            rc = HRESULT.S_OK;
        }
        else if (pname == RAF_ParameterName.rafPN_latencyLimit)
        {
            prm.setLatencyLimit(getLatencyLimit());
            rc = HRESULT.S_OK;
        }
        // New since SLES V5 as defined in table 3-1 of CCSDS RAF doc
        else if (pname == RAF_ParameterName.rafPN_permittedFrameQuality)
        {
        	prm.setPermittedFrameQuality(getPermittedFrameQuality());
        	rc = HRESULT.S_OK;
        }
        else if (pname == RAF_ParameterName.rafPN_minReportingCycle)
        {
        	prm.setMinimumReportingCycle(getMinimumReportingCycle());
        	rc = HRESULT.S_OK;
        }
        return rc;
    }

}
