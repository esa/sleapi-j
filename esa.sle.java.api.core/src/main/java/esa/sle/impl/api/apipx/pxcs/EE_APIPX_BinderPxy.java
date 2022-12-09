/**
 * @(#) EE_APIPX_BinderPxy.java
 */

package esa.sle.impl.api.apipx.pxcs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.api.apipx.pxcs.local.EE_APIPX_LocalLink;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_Database;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_IPCConfig;
import esa.sle.impl.api.apipx.pxspl.EE_APIPX_ChannelFactory;
import esa.sle.impl.api.apipx.pxspl.EE_APIPX_Proxy;
import esa.sle.impl.api.apipx.pxspl.EE_APIPX_RespondingAssoc;
import esa.sle.impl.api.apipx.pxspl.IEE_Binder;
import esa.sle.impl.api.apipx.pxspl.IEE_ChannelInitiate;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class implements the interface IEE_Binder in the SLE application process
 * and forwards the requests to the binder object in the communication server
 * process via inter process communication. For incoming BIND requests, its
 * creates a responding association and links it to a newly created channel
 * proxy object (class EE_APIPX_ChannelPxy). The link object can then forward
 * the encoded PDU's to the channel proxy object. This class is responsible for
 * creating and deleting the link object in the application process. To be able
 * to implement the interface IEE_Binder, a condition variable and a timer are
 * needed to wait for the result of the two methods registerPort and
 * deregisterPort (the result will be received through the IPC link).
 */
public class EE_APIPX_BinderPxy extends EE_APIPX_LinkAdapter implements IEE_Binder
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_BinderPxy.class.getName());

    /**
     * RegId return by the registerPort through the IPC link.
     */
    private int regID;

    /**
     * Result of the registration or deregistration port.
     */
    private HRESULT result;

    /**
     * The reference to the proxy.
     */
    private final EE_APIPX_Proxy pProxy;

    /**
     * The ipc name used for the connection to the communication server.
     */
    private String ipcName;

    /**
     * The pointer to the reporter.
     */
    private final ISLE_Reporter reporter;

    /**
     * The pointer to the database.
     */
    private final EE_APIPX_Database database;

    private final ISLE_OperationFactory opfactory;

    private final ISLE_UtilFactory utilfactory;

    private final List<PXCS_Link> eeAPIPXLinkList;

    private final ReentrantLock mutex;

    private boolean useNagleFlag;

    private final String instanceId;
    /**
     * Creator of the class which takes the reference to the proxy, to the
     * database, to the reporter, and to the operation factory as parameter.
     */
    public EE_APIPX_BinderPxy(String instanceKey, 
    						  EE_APIPX_Proxy pProxy,
                              ISLE_Reporter pReporter,
                              EE_APIPX_Database pDatabase,
                              ISLE_OperationFactory pOpfactory,
                              ISLE_UtilFactory pUtilfactory)
    {
    	super();
    	this.instanceId = instanceKey;
        this.result = HRESULT.E_FAIL;
        this.pProxy = pProxy;
        if (pDatabase != null)
        {
            EE_APIPX_IPCConfig pIpcConfig = pDatabase.getIPCConfigData();
            this.ipcName = pIpcConfig.getServiceAddress();
            this.useNagleFlag = pIpcConfig.getUseNagleFlag();
        }
        else
        {
            this.ipcName = "";
            this.useNagleFlag = true;
        }
        this.reporter = pReporter;
        this.opfactory = pOpfactory;
        this.utilfactory = pUtilfactory;
        this.database = pDatabase;
        this.mutex = new ReentrantLock();
        this.eeAPIPXLinkList = new ArrayList<PXCS_Link>();
        this.regID = -1;
    }

    /**
     * Registers the port. A registerPort message is sent on the IPC link to the
     * communication server. S_OK The port has been registered. SLE_E_DUPLICATE
     * Duplicate registration. E_FAIL The registration fails due to a further
     * unspecified error.
     */
    @Override
    public HRESULT registerPort(ISLE_SII siid, String portId, EE_Reference<Integer> regId)
    {
        HRESULT res = HRESULT.E_FAIL;

        assert (!siid.isNull()) : "siid is null";
        if (siid.isNull())
        {
            return HRESULT.E_INVALIDARG;
        }

        assert (!portId.isEmpty()) : "portId is empty";
        if (portId.isEmpty())
        {
            return HRESULT.E_INVALIDARG;
        }

        EE_Reference<EE_APIPX_Link> pLink = new EE_Reference<EE_APIPX_Link>();

        // connect the ipc link
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before mutex lock and start");
        }
        this.mutex.lock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after mutex lock and before start");
        }
        res = start(pLink, siid);
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("before mutex unlock and after start");
        }
        this.mutex.unlock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("after mutex unlock and after start");
        }
        if (res != HRESULT.S_OK)
        {
            return res;
        }

        String siidAscii = siid.getAsciiForm();

        // create the register message
        PXCS_Register_Mess mess = new PXCS_Register_Mess(siid.getInitialFormatUsed(), 0, siidAscii, portId);
        byte[] messByteArray = mess.toByteArray();

        // create the header message
        PXCS_Header_Mess header = new PXCS_Header_Mess(false,
                                                       PXCS_MessId.mid_RegisterPort.getCode(),
                                                       messByteArray.length);

        byte[] data = new byte[PXCS_Header_Mess.hMsgLength + messByteArray.length];
        System.arraycopy(header.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);
        System.arraycopy(messByteArray, 0, data, PXCS_Header_Mess.hMsgLength, messByteArray.length);

        this.result = HRESULT.E_FAIL;
        this.regID = 0;

        if (pLink.getReference() != null)
        {
            sendMessage(data, pLink.getReference(), 3);
            regId.setReference(this.regID);
        }

        if (this.result == HRESULT.S_OK)
        {
            if (this.regID != 0)
            {
                // update the regId in the list
                this.mutex.lock();
                for (PXCS_Link pl : this.eeAPIPXLinkList)
                {
                    if (pl.getPsii().equals(siid))
                    {
                        pl.setRegId(this.regID);
                        break;
                    }
                }
                this.mutex.unlock();
            }
        }
        else
        {
            // remove the link from the link
            this.mutex.lock();
            for (Iterator<PXCS_Link> it = this.eeAPIPXLinkList.iterator(); it.hasNext();)
            {
                PXCS_Link pl = it.next();
                if (pl.getPsii().equals(siid))
                {
                    pLink.setReference(pl.getpLink());
                    it.remove();
                    break;
                }
            }
            this.mutex.unlock();

            // send a Stop message to indicate that it is a normal close
            mess = new PXCS_Register_Mess();
            mess.setRegId(0);
            messByteArray = mess.toByteArray();

            header = new PXCS_Header_Mess(false, PXCS_MessId.mid_NormalStop.getCode(), messByteArray.length);

            data = new byte[PXCS_Header_Mess.hMsgLength + messByteArray.length];
            System.arraycopy(header.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);
            System.arraycopy(messByteArray, 0, data, PXCS_Header_Mess.hMsgLength, messByteArray.length);

            if (pLink.getReference() != null)
            {
                sendMessage(data, pLink.getReference(), 3);
            }
            pLink.getReference().disconnect();
        }

        return this.result;
    }

    /**
     * Deregisters the port. A deregisterPort message is sent on the IPC link to
     * the communication server. S_OK The port has been deregistered.
     * SLE_E_UNKNOWN The port is not registered. E_FAIL The deregistration fails
     * due to a further unspecified error.
     */
    @Override
    public HRESULT deregisterPort(int regId)
    {
        EE_APIPX_Link pLink = null;

        assert (regId != -1) : "regId is invalid";
        if (regId == -1)
        {
            return HRESULT.E_INVALIDARG;
        }

        // check if the link is in the list
        this.mutex.lock();
        boolean isRegistered = false;
        for (PXCS_Link pl : this.eeAPIPXLinkList)
        {
            if (pl.getRegId() == regId)
            {
                pLink = pl.getpLink();
                isRegistered = true;
                break;
            }
        }
        this.mutex.unlock();

        if (!isRegistered)
        {
            return HRESULT.SLE_E_UNKNOWN;
        }

        PXCS_Register_Mess mess = new PXCS_Register_Mess();
        mess.setRegId(regId);
        byte[] messByteArray = mess.toByteArray();

        PXCS_Header_Mess header = new PXCS_Header_Mess(false,
                                                       PXCS_MessId.mid_DeregisterPort.getCode(),
                                                       messByteArray.length);

        byte[] data = new byte[PXCS_Header_Mess.hMsgLength + messByteArray.length];
        System.arraycopy(header.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);
        System.arraycopy(messByteArray, 0, data, PXCS_Header_Mess.hMsgLength, messByteArray.length);

        this.result = HRESULT.E_FAIL;
        this.regID = 0;

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("initial result :" + this.result);
        }
        if (pLink != null)
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("before from sendMessage A:" + regId);
            }
            sendMessage(data, pLink, 3);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("after from sendMessage A:" + regId);
            }
        }

        // send a Stop message to indicate that it is a normal close
        mess = new PXCS_Register_Mess();
        mess.setRegId(regId);
        messByteArray = mess.toByteArray();

        header = new PXCS_Header_Mess(false, PXCS_MessId.mid_NormalStop.getCode(), messByteArray.length);

        data = new byte[PXCS_Header_Mess.hMsgLength + messByteArray.length];
        System.arraycopy(header.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);
        System.arraycopy(messByteArray, 0, data, PXCS_Header_Mess.hMsgLength, messByteArray.length);

        if (pLink != null)
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("before from sendMessage B:" + regId);
            }
            sendMessage(data, pLink, 3);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("after from sendMessage B:" + regId);
            }
        }

        this.mutex.lock();
        isRegistered = false;
        for (Iterator<PXCS_Link> it = this.eeAPIPXLinkList.iterator(); it.hasNext();)
        {
            PXCS_Link pl = it.next();
            if (pl.getRegId() == regId)
            {
                pLink = pl.getpLink();
                it.remove();
                isRegistered = true;
                break;
            }
            if (!it.hasNext())
            {
                break;
            }
        }

        if (isRegistered)
        {
            // disconnect the ipc link. The link will be removed from the list
            // when we receive the ipcclose mess
            this.linkClosed = true;
            pLink.disconnect();
        }
        this.mutex.unlock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("final result : " + this.result);
        }

        return this.result;
    }

    /**
     * The link object calls this function when data are received on the IPC
     * link and must be performed by the BinderPxy.
     */
    @Override
    public void takeData(byte[] data, int dataType, EE_APIPX_Link pLink, boolean last_pdu)
    {
        if (dataType == PXCS_MessId.mid_Rsp_RegisterPort.getCode())
        {
            PXCS_Response_Mess mess = new PXCS_Response_Mess(data);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest(mess.toString());
            }
            // give the result
            this.result = mess.getResult();
            this.regID = mess.getRegId();

            // signal response received
            signalResponseReceived();
        }
        else if (dataType == PXCS_MessId.mid_Rsp_DeregisterPort.getCode())
        {

            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("this.result A :" + this.result);
            }

            PXCS_Response_Mess mess = new PXCS_Response_Mess(data);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest(mess.toString());
            }
            // give the result
            this.result = mess.getResult();
            this.regID = mess.getRegId();

            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("this.result B:" + this.result);
            }

            // signal response received
            signalResponseReceived();
        }
        else if (dataType == PXCS_MessId.mid_BindPdu.getCode())
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("BIND received");
            }
            rcvBind(data, pLink);
            // don't delete the bind pdu. It will be transmitted to the channel
            // pxy !!
        }
        else if (dataType == PXCS_MessId.mid_Rsp_NormalStop.getCode())
        {
            signalResponseReceived();
        }
    }

    /**
     * This function is called when the IPC connection is closed by the peer
     * side or lost. On IPC deconnexion, the BinderPxy sends a message to the
     * Proxy.
     */
    @Override
    public void ipcClosed(EE_APIPX_Link pLink)
    {
        if (this.linkClosed)
        {
            return;
        }

        this.linkClosed = true;

        signalResponseReceived();

        this.mutex.lock();
        if (pLink != null)
        {
            // remove the link from the list
            for (Iterator<PXCS_Link> it = this.eeAPIPXLinkList.iterator(); it.hasNext();)
            {
                PXCS_Link pl = it.next();
                if (pl.getpLink().equals(pLink))
                {
                    it.remove();
                    break;
                }
            }
        }
        this.mutex.unlock();
    }

    /**
     * This function is called by the link object when a BIND-request is
     * received on the IPC link, and must be treated by the BinderPxy object.
     * Then the BinderPxy instanciates a responding association and a
     * ChannelPxy, links them together, and register the new association in the
     * Proxy. S_OK The objects are created and linked. E_FAIL The creation fails
     * due to a further unspecified error.
     */
    public HRESULT rcvBind(byte[] data, EE_APIPX_Link pLink)
    {
        EE_APIPX_RespondingAssoc pRespAssoc = null;
        IEE_ChannelInitiate pChannelInitiate = null;

        // instanciate a channelpxy through the channelfactory
        pChannelInitiate = EE_APIPX_ChannelFactory.createChannel(this.instanceId, false, this.reporter, pLink);

        // instanciate a responding association and give it the channelpxy
        pRespAssoc = new EE_APIPX_RespondingAssoc(this.instanceId,
        										  this.pProxy,
        										  pChannelInitiate,
                                                  this.database,
                                                  this.reporter,
                                                  this.opfactory,
                                                  this.utilfactory);

        // update the RspAssoc in the list
        this.mutex.lock();
        for (PXCS_Link li : this.eeAPIPXLinkList)
        {
            if (li.getpLink().equals(pLink))
            {
                li.setpRspAssoc(pRespAssoc);
                break;
            }
        }
        this.mutex.unlock();

        // update the proxy
        if (this.pProxy != null)
        {
            this.pProxy.registerAssoc(pRespAssoc);
        }

        return HRESULT.S_OK;
    }

    /**
     * This function gets the pointer to the responding association, if it
     * exists, for a registered service instance. S_OK The service instance is
     * registered, and the responding association is present. SLE_E_UNKNOWN
     * Cannot find the registered service instance. E_FAIL No responding
     * association for this registered service instance.
     */
    public HRESULT getRspAssoc(ISLE_SII siid, EE_Reference<EE_APIPX_RespondingAssoc> pRspAssoc)
    {
        boolean isRegistered = false;

        for (PXCS_Link li : this.eeAPIPXLinkList)
        {
            if (li.getPsii().equals(siid))
            {
                isRegistered = true;
                pRspAssoc.setReference(li.getpRspAssoc());
                break;
            }
        }

        if (!isRegistered)
        {
            return HRESULT.SLE_E_UNKNOWN;
        }

        if (pRspAssoc.getReference() != null)
        {
            return HRESULT.S_OK;
        }

        return HRESULT.E_FAIL;
    }

    /**
     * Connects the proxy component to the communication server with an IPC
     * connection. S_OK The IPC connection is established. SLE_E_STATE The
     * connection has already been established. E_FAIL The connection fails due
     * to a further unspecified error.
     */
    private HRESULT start(EE_Reference<EE_APIPX_Link> pLink, ISLE_SII siid)
    {
        // check if the portid is yet registered locally
        boolean isRegistered = false;
        for (PXCS_Link li : this.eeAPIPXLinkList)
        {
            if (li.getPsii().equals(siid))
            {
                isRegistered = true;
                break;
            }
        }

        if (!isRegistered)
        {
            // create a new link
            EE_APIPX_Link link = null;
            if (EE_APIPX_LocalLink.isLocalAddress(this.ipcName))
            {
                link = new EE_APIPX_LocalLink(this.instanceId);
            }
            else
            {
                link = new EE_APIPX_Link(this.instanceId);
                link.setUseNagleFlag(this.useNagleFlag);
            }
            pLink.setReference(link);
            link.setBinderPxy(this);

            // connect the link
            if (link.connect(this.ipcName) == HRESULT.S_OK)
            {
                if (link.waitMsg() != HRESULT.S_OK)
                {
                    link.disconnect();
                    link = null;
                    return HRESULT.E_FAIL;
                }
            }
            else
            {
                return HRESULT.E_FAIL;
            }

            // insert the link in the list
            PXCS_Link pxcsLink = new PXCS_Link(pLink.getReference(), null, 0, siid);
            this.eeAPIPXLinkList.add(pxcsLink);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("the receiving thread started succesfully.");
            }

            return HRESULT.S_OK;
        }
        else
        {
            return HRESULT.SLE_E_DUPLICATE;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IEE_Binder.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

}
