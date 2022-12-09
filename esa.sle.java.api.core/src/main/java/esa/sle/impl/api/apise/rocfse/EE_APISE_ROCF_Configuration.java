/**
 * @(#) EE_APISE_ROCF_Configuration.java
 */

package esa.sle.impl.api.apise.rocfse;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isrv.irocf.IROCF_GetParameter;
import ccsds.sle.api.isrv.irocf.types.ROCF_ChannelType;
import ccsds.sle.api.isrv.irocf.types.ROCF_ControlWordType;
import ccsds.sle.api.isrv.irocf.types.ROCF_Gvcid;
import ccsds.sle.api.isrv.irocf.types.ROCF_ParameterName;
import ccsds.sle.api.isrv.irocf.types.ROCF_UpdateMode;
import esa.sle.impl.api.apise.slese.EE_APISE_RSConfiguration;

/**
 * The class holds all configuration parameters for the ROCF service. Besides
 * the accessor and modifier functions for the configuration parameters the
 * class offers also the possibility to set the desired parameter for a
 * GetParameter PDU. If the desired parameter is available as an attribute of
 * the Configuration class, the function setUpGetParameter() sets the value of
 * the desired parameter. Note that the accessor and modifier functions
 * get_<Attribute> and set_<Attribute> are generated automatically for the
 * public interface.
 */
public class EE_APISE_ROCF_Configuration extends EE_APISE_RSConfiguration
{
    /**
     * The list of permitted GvcId 's.
     */
    private ROCF_Gvcid[] permGvcIdList = new ROCF_Gvcid[0];

    /**
     * The list of permitted Control Word Types.
     */
    private ROCF_ControlWordType[] permControlWordTypeSet = new ROCF_ControlWordType[0];

    /**
     * The list of permitted Tc Vcid's.
     */
    private long[] permTcVcidSet = new long[0];

    /**
     * The list of permitted Update Modes.
     */
    private ROCF_UpdateMode[] permUpdateModeSet = new ROCF_UpdateMode[0];
    
    


    @SuppressWarnings("unused")
    private EE_APISE_ROCF_Configuration(final EE_APISE_ROCF_Configuration right)
    {
        this.permGvcIdList = right.permGvcIdList;
        this.permControlWordTypeSet = right.permControlWordTypeSet;
        this.permTcVcidSet = right.permTcVcidSet;
        this.permUpdateModeSet = right.permUpdateModeSet;
    }

    public EE_APISE_ROCF_Configuration()
    {
        this.permGvcIdList = new ROCF_Gvcid[0];
        this.permControlWordTypeSet = new ROCF_ControlWordType[0];
        this.permTcVcidSet = new long[0];
        this.permUpdateModeSet = new ROCF_UpdateMode[0];
    }

    /**
     * Sets the Global VCID list to which the service instance has access.
     */
    public void setPermittedGvcIdSet(ROCF_Gvcid[] idList)
    {

        int n = idList.length;
        this.permGvcIdList = new ROCF_Gvcid[n];
        for (int i = 0; i < n; i++)
        {
            this.permGvcIdList[i] = idList[i];
        }
    }

    /**
     * Returns the Global VCID list to which the service instance has access.
     */
    public ROCF_Gvcid[] getPermittedGvcIdSet()
    {
        return this.permGvcIdList;
    }

    /**
     * Sets the permitted control word types to which the service instance has
     * access.
     */
    public void setPermittedControlWordTypeSet(ROCF_ControlWordType[] typeSet)
    {
        int n = typeSet.length;
        this.permControlWordTypeSet = new ROCF_ControlWordType[n];
        for (int i = 0; i < n; i++)
        {
            this.permControlWordTypeSet[i] = typeSet[i];
        }
    }

    /**
     * Returns the set of control word types to which the service instance has
     * access.
     */
    public ROCF_ControlWordType[] getPermittedControlWordTypeSet()
    {
        return this.permControlWordTypeSet;
    }

    /**
     * Sets the Tc Vcid's to which the service instance has access.
     */
    public void setPermittedTcVcidSet(long[] vcidSet)
    {

        int n = vcidSet.length;
        this.permTcVcidSet = new long[n];
        for (int i = 0; i < n; i++)
        {
            this.permTcVcidSet[i] = vcidSet[i];
        }

    }

    /**
     * Returns the set of TC Vcid's to which the service instance has access.
     */
    public long[] getPermittedTcVcidSet()
    {
        return this.permTcVcidSet;
    }

    /**
     * Sets the Update Modes to which the service instance has access
     */
    public void setPermittedUpdateModeSet(ROCF_UpdateMode[] umSet)
    {
        int n = umSet.length;
        this.permUpdateModeSet = new ROCF_UpdateMode[n];
        for (int i = 0; i < n; i++)
        {
            this.permUpdateModeSet[i] = umSet[i];
        }
    }

    /**
     * Returns the set of update modes to which the service instance has access.
     */
    public ROCF_UpdateMode[] getPermittedUpdateModeSet()
    {
        return this.permUpdateModeSet;
    }

    /**
     * Initializes the supplied ROCF-GET-PARAMETER-operation with the current
     * status information data.
     */
    public HRESULT setUpGetParameter(IROCF_GetParameter prm)
    {

        HRESULT rc = HRESULT.SLE_E_UNKNOWN;

        ROCF_ParameterName pname = prm.getRequestedParameter();

        if (pname == ROCF_ParameterName.rocfPN_bufferSize)
        {
            prm.setTransferBufferSize(getTransferBufferSize());
            rc = HRESULT.S_OK;
        }
        else if (pname == ROCF_ParameterName.rocfPN_deliveryMode)
        {
            prm.setDeliveryMode(getDeliveryMode().asROCF_DeliveryMode());
            rc = HRESULT.S_OK;
        }
        else if (pname == ROCF_ParameterName.rocfPN_latencyLimit)
        {
            prm.setLatencyLimit(getLatencyLimit());
            rc = HRESULT.S_OK;
        }
        else if (pname == ROCF_ParameterName.rocfPN_permittedGvcidSet)
        {
            prm.setPermittedGvcidSet(this.permGvcIdList);
            rc = HRESULT.S_OK;
        }
        else if (pname == ROCF_ParameterName.rocfPN_permittedControlWordTypeSet)
        {
            prm.setPermittedControlWordTypeSet(this.permControlWordTypeSet);
            rc = HRESULT.S_OK;
        }
        else if (pname == ROCF_ParameterName.rocfPN_permittedTcVcidSet)
        {
            prm.setPermittedTcVcidSet(this.permTcVcidSet);
            rc = HRESULT.S_OK;
        }
        else if (pname == ROCF_ParameterName.rocfPN_permittedUpdateModeSet)
        {
            prm.setPermittedUpdateModeSet(this.permUpdateModeSet);
            rc = HRESULT.S_OK;
        }
        else if (pname == ROCF_ParameterName.rocfPN_minReportingCycle)
        {
            prm.setMinimumReportingCycle(getMinimumReportingCycle());
            rc = HRESULT.S_OK;
        }
        return rc;
    }

    /**
     * Checks the supplied GVCID to be part of the permitted global vcId list.
     * If the check succeeds S_OK is returned, otherwise E_FAIL is returned.
     */
    public HRESULT checkGvcId(ROCF_Gvcid gvcId)
    {
        if (gvcId == null)
        {
            return HRESULT.E_FAIL;
        }

        for (ROCF_Gvcid element : this.permGvcIdList)
        {
            ROCF_ChannelType chType = element.getType();
            if (chType == ROCF_ChannelType.rocfCT_VirtualChannel)
            {

                if (element.getType() == gvcId.getType() && element.getScid() == gvcId.getScid()
                    && element.getVersion() == gvcId.getVersion() && element.getVcid() == gvcId.getVcid())
                {
                    return HRESULT.S_OK;
                }
            }
            if (chType == ROCF_ChannelType.rocfCT_MasterChannel)
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

    /**
     * Checks the supplied Control Word Type to be part of the permitted Control
     * Word Types list. If the check succeeds S_OK is returned, otherwise E_FAIL
     * is returned.
     */
    public HRESULT checkControlWordType(ROCF_ControlWordType cwType)
    {
        if (cwType == ROCF_ControlWordType.rocfCWT_invalid)
        {
            return HRESULT.E_FAIL;
        }
        for (ROCF_ControlWordType element : this.permControlWordTypeSet)
        {
            if (element == cwType)
            {
                return HRESULT.S_OK;
            }
        }
        return HRESULT.E_FAIL;
    }

    /**
     * Checks the supplied Tc Vcid to be part of the permitted Tc Vcid Set. If
     * the check succeeds S_OK is returned, otherwise E_FAIL is returned
     */
    public HRESULT checkTcVcid(long tcVcid)
    {
        for (long element : this.permTcVcidSet)
        {
            if (element == tcVcid)
            {
                return HRESULT.S_OK;
            }
        }

        return HRESULT.E_FAIL;
    }

    /**
     * Checks the supplied Update Mode to be part of the permitted Update Mode
     * Set. If the check succeeds S_OK is returned, otherwise E_FAIL is returned
     */
    public HRESULT checkUpdateMode(ROCF_UpdateMode updateMode)
    {
        if (updateMode == ROCF_UpdateMode.rocfUM_invalid)
        {
            return HRESULT.E_FAIL;
        }

        for (ROCF_UpdateMode element : this.permUpdateModeSet)
        {
            if (element == updateMode)
            {
                return HRESULT.S_OK;
            }
        }
        return HRESULT.E_FAIL;
    }

}
