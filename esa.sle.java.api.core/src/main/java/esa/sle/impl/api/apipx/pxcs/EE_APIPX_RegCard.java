/**
 * @(#) EE_APIPX_RegCard.java
 */

package esa.sle.impl.api.apipx.pxcs;

import ccsds.sle.api.isle.iutl.ISLE_SII;

/**
 * The class holds all information for one registered service instance that is
 * needed to route PDU's to the service instance they belong.
 */
public class EE_APIPX_RegCard
{
    /**
     * The responder port identifier.
     */
    private final String rspPortId;

    /**
     * The reference to the link object.
     */
    private EE_APIPX_Link link;

    /**
     * The service instance identifier.
     */
    private final ISLE_SII psiid;

    private final int regCardId;


    public EE_APIPX_RegCard(int regCardId, ISLE_SII psiid, String rspPortId, EE_APIPX_Link link)
    {
        this.regCardId = regCardId;
        this.psiid = psiid;
        this.link = link;
        this.rspPortId = rspPortId;
    }

    public ISLE_SII getSiid()
    {
        return this.psiid;
    }

    /**
     * Set the reference of the link.
     */
    public void setLink(EE_APIPX_Link plink)
    {
        this.link = plink;
    }

    /**
     * Get the reference of the link.
     */
    public EE_APIPX_Link getLink()
    {
        return this.link;
    }

    /**
     * Get the logical port.
     */
    public String getPortId()
    {
        return this.rspPortId;
    }

    public int getRegCardId()
    {
        return this.regCardId;
    }

}
