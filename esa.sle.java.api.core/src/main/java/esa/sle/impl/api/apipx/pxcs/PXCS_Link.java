package esa.sle.impl.api.apipx.pxcs;

import ccsds.sle.api.isle.iutl.ISLE_SII;
import esa.sle.impl.api.apipx.pxspl.EE_APIPX_RespondingAssoc;

public class PXCS_Link
{
    private EE_APIPX_Link pLink;

    private EE_APIPX_RespondingAssoc pRspAssoc;

    private int regId;

    private ISLE_SII psii;


    public PXCS_Link(EE_APIPX_Link pLink, EE_APIPX_RespondingAssoc pRspAssoc, int regId, ISLE_SII psii)
    {
        this.pLink = pLink;
        this.pRspAssoc = pRspAssoc;
        this.regId = regId;
        this.psii = psii;
    }

    public EE_APIPX_Link getpLink()
    {
        return this.pLink;
    }

    public void setpLink(EE_APIPX_Link pLink)
    {
        this.pLink = pLink;
    }

    public EE_APIPX_RespondingAssoc getpRspAssoc()
    {
        return this.pRspAssoc;
    }

    public void setpRspAssoc(EE_APIPX_RespondingAssoc pRspAssoc)
    {
        this.pRspAssoc = pRspAssoc;
    }

    public int getRegId()
    {
        return this.regId;
    }

    public void setRegId(int regId)
    {
        this.regId = regId;
    }

    public ISLE_SII getPsii()
    {
        return this.psii;
    }

    public void setPsii(ISLE_SII psii)
    {
        this.psii = psii;
    }
}
