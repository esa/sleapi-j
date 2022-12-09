/**
 * @(#) EE_APIPX_BinderAdapter.java
 */

package esa.sle.impl.api.apipx.pxcs;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.api.apiut.EE_SLE_UtilityFactory;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class decodes requests received via the IPC-link and forwards the
 * (decoded) requests to the binder through the interface IEE_Binder.
 */
public class EE_APIPX_BinderAdapter extends EE_APIPX_LinkAdapter
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_BinderAdapter.class.getName());

    private EE_APIPX_Link eeAPIPXLink;

    private final String instanceId;

    public EE_APIPX_BinderAdapter(String instanceKey)
    {
    	this.instanceId = instanceKey;
        this.eeAPIPXLink = null;
    }

    /**
     * The link object calls this function when some data are received on the
     * IPC link and must be performed by the BinderAdapter. The BinderAdapter
     * decodes the requests and calls registerPort() or deregisterPort() of the
     * IEE_Binder interface.
     */
    @Override
    public void takeData(byte[] data, int dataType, EE_APIPX_Link pLink, boolean lastPdu)
    {
        if (PXCS_MessId.getPXCSMessIdByCode(dataType) == PXCS_MessId.mid_RegisterPort)
        {
            rcvRegisterPort(data);
        }
        else if (PXCS_MessId.getPXCSMessIdByCode(dataType) == PXCS_MessId.mid_DeregisterPort)
        {
            rcvDeregisterPort(data);
        }
    }

    /**
     * This function is called when the IPC connection is closed by the peer
     * side or lost.
     */
    @Override
    public void ipcClosed(EE_APIPX_Link pLink)
    {
        EE_APIPX_Binder pBinder = null;

        if (this.eeAPIPXLink != null)
        {
            // deregister the port
            pBinder = EE_APIPX_Binder.getInstance(this.instanceId);
            pBinder.deregisterPort(this.eeAPIPXLink);
        }

        this.linkClosed = true;
        this.eeAPIPXLink = null;
    }

    /**
     * Set the Link associated with the BinderAdapter object.
     */
    public void setLink(EE_APIPX_Link pLink)
    {
        this.eeAPIPXLink = pLink;
    }

    /**
     * Decodes the registerPort request received from the link and sends it to
     * the Binder.
     */
    private void rcvRegisterPort(byte[] data)
    {
        String messSii = "";
        String portName = "";
        ISLE_UtilFactory puf = null;
        ISLE_SII psii = null;
        EE_APIPX_Binder pBinder = null;

        pBinder = EE_APIPX_Binder.getInstance(this.instanceId);
        PXCS_Register_Mess mess = new PXCS_Register_Mess(data);
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(mess.toString());
        }
        boolean initialFormat = mess.isInitialFormatUsed();

        // retrieve the information from the pdu
        portName = mess.getPortname();
        messSii = mess.getSii();

        // create the Service Instance identifier
        EE_SLE_UtilityFactory.initialiseInstance(this.instanceId, null);
        puf = EE_SLE_UtilityFactory.getInstance(this.instanceId);
        if (puf != null)
        {
            try
            {
                psii = puf.createSII(ISLE_SII.class);
            }
            catch (SleApiException e)
            {
                LOG.log(Level.FINE, "SleApiException ", e);
            }

            if (psii != null)
            {
                EE_Reference<Integer> regId = new EE_Reference<Integer>();
                HRESULT res = HRESULT.S_OK;

                if (initialFormat)
                {
                    psii.setInitialFormat();
                }

                try
                {
                    psii.setAsciiForm(messSii);
                }
                catch (SleApiException e)
                {
                    LOG.log(Level.FINE, "SleApiException ", e);
                    res = e.getHResult();
                }

                // register the port
                if (res == HRESULT.S_OK)
                {
                    res = pBinder.registerPort(psii, portName, regId);
                }

                if (res == HRESULT.S_OK)
                {
                    pBinder.setLink(this.eeAPIPXLink, psii);
                }
                else
                {
                    pBinder.deregisterPort(regId.getReference());
                }

                // send the result
                sendResultMessage(PXCS_MessId.mid_Rsp_RegisterPort.getCode(),
                                  res,
                                  regId.getReference(),
                                  this.eeAPIPXLink);
            }
        }
    }

    /**
     * Decodes the deregisterPort request received from the link and sends it to
     * the Binder.
     */
    private void rcvDeregisterPort(byte[] data)
    {
        EE_APIPX_Binder pBinder = EE_APIPX_Binder.getInstance(this.instanceId);
        PXCS_Register_Mess mess = new PXCS_Register_Mess(data);

        // deregister the port
        HRESULT res = pBinder.deregisterPort(mess.getRegId());

        // send the result
        sendResultMessage(PXCS_MessId.mid_Rsp_DeregisterPort.getCode(), res, mess.getRegId(), this.eeAPIPXLink);
    }

}
