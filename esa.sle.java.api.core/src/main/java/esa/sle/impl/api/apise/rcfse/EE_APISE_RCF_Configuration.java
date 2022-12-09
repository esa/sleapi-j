/**
 * @(#) EE_APISE_RCF_Configuration.java
 */

package esa.sle.impl.api.apise.rcfse;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isrv.ircf.IRCF_GetParameter;
import ccsds.sle.api.isrv.ircf.types.RCF_ChannelType;
import ccsds.sle.api.isrv.ircf.types.RCF_Gvcid;
import ccsds.sle.api.isrv.ircf.types.RCF_ParameterName;
import esa.sle.impl.api.apise.slese.EE_APISE_RSConfiguration;

/**
 * The class holds all configuration parameters for the RCF service. Besides the
 * accessor and modifier functions for the configuration parameters the class
 * offers also the possibility to set the desired parameter for a GetParameter
 * PDU. If the desired parameter is available as an attribute of the
 * Configuration class, the function setUpGetParameter() sets the value of the
 * desired parameter. Note that the accessor and modifier functions
 * get_<Attribute> and set_<Attribute> are generated automatically for the
 * public interface.
 */

public class EE_APISE_RCF_Configuration extends EE_APISE_RSConfiguration
{
    /**
     * The list of permitted GvcId 's.
     */
    private RCF_Gvcid[] permGvcIdList = new RCF_Gvcid[0];


    @SuppressWarnings("unused")
    private EE_APISE_RCF_Configuration(final EE_APISE_RCF_Configuration right)
    {
        this.permGvcIdList = right.permGvcIdList;
    }

    public EE_APISE_RCF_Configuration()
    {
        this.permGvcIdList = new RCF_Gvcid[0];
    }

    /**
     * Sets the Global VCID list to which the service instance has access.
     */
    public void setPermittedGvcIdSet(RCF_Gvcid[] idList)
    {
        this.permGvcIdList = idList;
    }

    /**
     * Returns the Global VCID list to which the service instance has access.
     */
    public RCF_Gvcid[] getPermittedGvcIdSet()
    {
        return this.permGvcIdList;
    }

    /**
     * Initializes the supplied RCF-GET-PARAMETER-operation with the current
     * status information data.
     */
    public HRESULT setUpGetParameter(IRCF_GetParameter prm)
    {

        HRESULT rc = HRESULT.SLE_E_UNKNOWN;

        RCF_ParameterName pname = prm.getRequestedParameter();

        if (pname == RCF_ParameterName.rcfPN_bufferSize)
        {
            prm.setTransferBufferSize(getTransferBufferSize());
            rc = HRESULT.S_OK;
        }
        else if (pname == RCF_ParameterName.rcfPN_deliveryMode)
        {
            prm.setDeliveryMode(getDeliveryMode().asRCF_DeliveryMode());
            rc = HRESULT.S_OK;
        }
        else if (pname == RCF_ParameterName.rcfPN_latencyLimit)
        {
            prm.setLatencyLimit(getLatencyLimit());
            rc = HRESULT.S_OK;
        }
        else if (pname == RCF_ParameterName.rcfPN_permittedGvcidSet)
        {
            prm.setPermittedGvcidSet(this.permGvcIdList);
            rc = HRESULT.S_OK;
        }
        else if (pname == RCF_ParameterName.rcfPN_minReportingCycle)
        {
        	prm.setMinimumReportingCycle(this.getMinimumReportingCycle());
        	return HRESULT.S_OK;
        }
        return rc;

    }

    /**
     * Checks the supplied GVCID to be part of the permitted global vcId list.
     * If the check succeeds S_OK is returned, otherwise E_FAIL is returned.
     */
    public HRESULT checkGvcId(final RCF_Gvcid gvcId)
    {
        if (gvcId == null)
        {
            return HRESULT.E_FAIL;
        }

        for (RCF_Gvcid element : this.permGvcIdList)
        {
            RCF_ChannelType chType = element.getType();
            if (chType == RCF_ChannelType.rcfCT_VirtualChannel)
            {
                if (element.getType() == gvcId.getType() && element.getScid() == gvcId.getScid()
                    && element.getVersion() == gvcId.getVersion() && element.getVcid() == gvcId.getVcid())
                {
                    return HRESULT.S_OK;
                }
            }
            if (chType == RCF_ChannelType.rcfCT_MasterChannel)
            {
                if (element.getType() == gvcId.getType() && element.getScid() == gvcId.getScid()
                    && element.getVersion() == gvcId.getVersion())
                {
                    return HRESULT.S_OK;
                }
            }
        }
        return HRESULT.E_FAIL;
    }

}
