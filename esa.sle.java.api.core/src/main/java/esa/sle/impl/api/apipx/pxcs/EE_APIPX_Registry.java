/**
 * @(#) EE_APIPX_Registry.java
 */

package esa.sle.impl.api.apipx.pxcs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class holds all information per registered service instance that is
 * needed to route PDU's to the service instance they belong.
 */
public class EE_APIPX_Registry
{
    private final List<EE_APIPX_RegCard> eeAPIPXRegCard;

    private final AtomicInteger sequencer;


    public EE_APIPX_Registry()
    {
        this.eeAPIPXRegCard = new ArrayList<EE_APIPX_RegCard>();
        this.sequencer = new AtomicInteger(0);
    }

    /**
     * Registers the port. S_OK The port has been registered. SLE_E_DUPLICATE
     * The port is already registered. E_FAIL The registration fails due to a
     * further unspecified error.
     */
    public HRESULT registerPort(ISLE_SII psiid, String portId, EE_Reference<Integer> regId)
    {
        // check if the siid is registered
        if (this.eeAPIPXRegCard.contains(psiid))
        {
            return HRESULT.SLE_E_DUPLICATE;
        }

        // insert a new regcard
        int regCardId = this.sequencer.incrementAndGet();
        EE_APIPX_RegCard regCard = new EE_APIPX_RegCard(regCardId, psiid, portId, null);
        this.eeAPIPXRegCard.add(regCard);

        regId.setReference(regCardId);

        return HRESULT.S_OK;
    }

    /**
     * De-registers the port. S_OK The port has been de-registered.
     * SLE_E_UNKNOWN The port is not registered. E_FAIL The de-registration
     * fails due to a further unspecified error.
     */
    public HRESULT deregisterPort(int regId, EE_Reference<String> portId)
    {
        // check if the reg card is registered
        boolean isRegistered = false;

        // iterate on the list and check if it contains the regId
        for (Iterator<EE_APIPX_RegCard> it = this.eeAPIPXRegCard.iterator(); it.hasNext();)
        {
            EE_APIPX_RegCard theRegCard = it.next();
            if (theRegCard.getRegCardId() == regId)
            {
                isRegistered = true;
                portId.setReference(theRegCard.getPortId());
                it.remove();
                break;
            }
        }

        if (!isRegistered)
        {
            return HRESULT.SLE_E_UNKNOWN;
        }

        return HRESULT.S_OK;
    }

    /**
     * Set the reference of the link used for the service instance.
     */
    public HRESULT setLink(EE_APIPX_Link plink, ISLE_SII psiid)
    {
        // check if the reg card is registered
        EE_APIPX_RegCard theRegCard = null;

        // iterate on the list and check if it contains the regId with the
        // required siid
        for (Iterator<EE_APIPX_RegCard> it = this.eeAPIPXRegCard.iterator(); it.hasNext();)
        {
            theRegCard = it.next();
            if (theRegCard.getSiid().equals(psiid))
            {
                break;
            }
        }

        if (theRegCard == null)
        {
            return HRESULT.E_FAIL;
        }

        theRegCard.setLink(plink);

        return HRESULT.S_OK;
    }

    /**
     * Return the reference of the link used for the service instance.
     */
    public EE_APIPX_Link getLink(ISLE_SII psiid)
    {
        // check if the reg card is registered
        EE_APIPX_RegCard theRegCard = null;

        // iterate on the list and check if it contains the regId with the
        // required siid
        for (Iterator<EE_APIPX_RegCard> it = this.eeAPIPXRegCard.iterator(); it.hasNext();)
        {
            theRegCard = it.next();
            if (theRegCard.getSiid().equals(psiid))
            {
                break;
            }
            else 
            {
            	theRegCard = null; // SLEAPIJ-50 set the result to null if there was no match
            }
        }

        if (theRegCard != null)
        {
            return theRegCard.getLink();
        }

        return null;
    }

    /**
     * De registers the port. S_OK The port has been de registered.
     * SLE_E_UNKNOWN The port is not registered. E_FAIL The de registration
     * fails due to a further unspecified error.
     */
    public HRESULT deregisterPort(EE_APIPX_Link pLink, EE_Reference<String> portId)
    {
        // check if the reg card is registered
        boolean isRegistered = false;

        // iterate on the list and check if it contains the regId
        for (Iterator<EE_APIPX_RegCard> it = this.eeAPIPXRegCard.iterator(); it.hasNext();)
        {
            EE_APIPX_RegCard theRegCard = it.next();
            if (theRegCard.getLink().equals(pLink))
            {
                isRegistered = true;
                portId.setReference(theRegCard.getPortId());
                it.remove();
                break;
            }
        }

        if (!isRegistered)
        {
            return HRESULT.SLE_E_UNKNOWN;
        }

        return HRESULT.S_OK;
    }

    /**
     * Return the service instance identifier used for the link.
     */
    public ISLE_SII getSii(EE_APIPX_Link pLink)
    {
        // check if the reg card is registered
        boolean isRegistered = false;
        EE_APIPX_RegCard theRegCard = null;

        // iterate on the list and check if it contains the regId
        for (Iterator<EE_APIPX_RegCard> it = this.eeAPIPXRegCard.iterator(); it.hasNext();)
        {
            theRegCard = it.next();
            if (theRegCard.getLink().equals(pLink))
            {
                isRegistered = true;
                break;
            }
        }

        if (isRegistered)
        {
            return theRegCard.getSiid();
        }

        return null;
    }

    public int getPortRegistrationCount(String portId)
    {
        int result = 0;

        for (EE_APIPX_RegCard li : this.eeAPIPXRegCard)
        {
            if (li.getPortId().equals(portId))
            {
                result++;
            }
        }

        return result;
    }
}
