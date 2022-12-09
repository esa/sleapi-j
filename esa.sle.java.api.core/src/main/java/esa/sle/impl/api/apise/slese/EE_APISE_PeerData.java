/**
 * @(#) EE_APISE_PeerData.java
 */

package esa.sle.impl.api.apise.slese;

/**
 * Maps the responder port identifier to the protocol identifier, which is used
 * to select the corresponding proxy instance.
 */
public class EE_APISE_PeerData
{
    /**
     * The (logical) responder port Identifier.
     */
    public String rspPortId;

    /**
     * The protocol identifier, used to select the proxy instance.
     */
    public String protocolId;


    /**
     * Constructor
     */
    public EE_APISE_PeerData()
    {
        this.rspPortId = null;
        this.protocolId = null;
    }

    /**
     * Copy constructor
     */
    public EE_APISE_PeerData(EE_APISE_PeerData right)
    {
        this.rspPortId = null;
        this.protocolId = null;
    }

    /**
     * @return
     */
    public String getRspPortId()
    {
        return this.rspPortId;
    }

    /**
     * @param value
     */
    public void setRspPortId(String value)
    {
        this.rspPortId = value;
    }

    /**
     * @return
     */
    public String getProtocolId()
    {
        return this.protocolId;
    }

    /**
     * @param value
     */
    public void setProtocolId(String value)
    {
        this.protocolId = value;
    }
}
