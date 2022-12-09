/**
 * @(#) EE_APIPX_Binder.java
 */

package esa.sle.impl.api.apipx.pxcs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.icc.ISLE_TraceControl;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_Database;
import esa.sle.impl.api.apipx.pxspl.IEE_Binder;
import esa.sle.impl.api.apipx.pxspl.IEE_ChannelInform;
import esa.sle.impl.api.apipx.pxspl.IEE_ChannelInitiate;
import esa.sle.impl.api.apipx.pxtml.EE_APIPX_Listener;
import esa.sle.impl.ifs.gen.EE_MessageRepository;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class EE_APIPX_Binder implements the interface IEE_Binder in the
 * communication server process. It holds all information needed for routing of
 * incoming BIND requests to the registered service instance. The class starts
 * listening on a specific port via delegation to the TML. It creates a new
 * association proxy (class EE_APIPX_AssocPxy) for each new connect request and
 * links it to the channel object passed by the listener (class
 * EE_APIPX_Listener). The Binder holds a list of association proxies, which is
 * needed for clean-up reasons. Furthermore the class is responsible to return
 * the link object (class EE_APIPX_Link) belonging to the registered service
 * instance, when the association proxy needs to forward a BIND PDU to the
 * application process.
 */
public class EE_APIPX_Binder implements IEE_Binder, ISLE_TraceControl
{

    private static final Logger LOG = Logger.getLogger(EE_APIPX_Binder.class.getName());

    /**
     * The unique instance of this class
     */
    private static Map<String, EE_APIPX_Binder> instanceMap = new ConcurrentHashMap<>();

    /**
     * Pointer to the operation factory interface.
     */
    private ISLE_OperationFactory isleOperationFactory = null;

    /**
     * Pointer to the utility factory interface.
     */
    private ISLE_UtilFactory isleUtilFactory = null;

    /**
     * Pointer to the database.
     */
    private EE_APIPX_Database eeAPIPXDatabase = null;

    /**
     * Trace level.
     */
    private SLE_TraceLevel traceLevel;

    /**
     * Indicates if the traces are started or not.
     */
    private boolean traceStarted = false;

    private ISLE_Trace trace = null;

    private EE_APIPX_Listener eeAPIPXListener = null;

    private ReentrantLock eeMutex = null;

    private EE_APIPX_Registry eeAPIPXRegistry = null;

    private List<EE_APIPX_AssocPxy> eeAPIPXAssocPxyList = null;

	private final String instanceId;


    /**
     * This method is called once to create the EE_APIPX_Binder instance
     * 
     * @param preporter
     * @return
     */
    public static synchronized void createBinder(String instanceKey, 
    											 ISLE_OperationFactory popFactory,
                                                 ISLE_UtilFactory putilFactory,
                                                 EE_APIPX_Database pDatabase)
    {
    	EE_APIPX_Binder instance = instanceMap.get(instanceKey);
    	
        if (instance == null)
        {
            instance = new EE_APIPX_Binder(instanceKey, popFactory, putilFactory, pDatabase);
            instanceMap.put(instanceKey, instance);
        }
    }

    /**
     * This method is called every time the EE_APIPX_Binder instance is needed
     * 
     * @return
     */
    public static synchronized EE_APIPX_Binder getInstance(String instanceKey)
    {
    	EE_APIPX_Binder instance = instanceMap.get(instanceKey);
    	
        if (instance == null)
        {
            throw new IllegalStateException("The createBinder method has never been called and the instance never created");
        }

        return instance;
    }

    /**
     * Constructor of the class which takes the operation factory as parameter.
     */
    private EE_APIPX_Binder(String instanceKey, ISLE_OperationFactory popFactory, ISLE_UtilFactory putilFactory, EE_APIPX_Database pDatabase)
    {
    	this.instanceId = instanceKey;
        this.isleOperationFactory = popFactory;
        this.isleUtilFactory = putilFactory;
        this.eeAPIPXDatabase = pDatabase;

        // set the reference of the Listener
        this.eeAPIPXListener = EE_APIPX_Listener.getInstance(this.instanceId);
        // initialize the Listener
        ISLE_Reporter pReporter = EE_APIPX_ReportTrace.getReporterInterface(this.instanceId);
        this.eeAPIPXListener.initialise(this, pReporter, this.eeAPIPXDatabase);

        this.eeMutex = new ReentrantLock();
        this.eeAPIPXAssocPxyList = new ArrayList<EE_APIPX_AssocPxy>();
        this.eeAPIPXRegistry = new EE_APIPX_Registry();
    }

    /**
     * Instantiates a new EE_APIPX_AssocProxy object and links it to the
     * ChannelInform interface passed as parameter when a new TCP connection is
     * established by TML. It is the responsibility of the Binder to increment
     * and decrement the reference counter for the ChannelInform interface.
     */
    public void rcvTcpCnx(IEE_ChannelInitiate pChannelInitiate)
    {
        EE_APIPX_AssocPxy pAssocPxy = new EE_APIPX_AssocPxy(this.instanceId,
        													this.isleOperationFactory,
                                                            this.isleUtilFactory,
                                                            this.eeAPIPXDatabase);
        IEE_ChannelInform pChannelInform = null;
        pChannelInform = pAssocPxy.queryInterface(IEE_ChannelInform.class);

        // insert the new aAssocPxy in the list
        this.eeMutex.lock();
        this.eeAPIPXAssocPxyList.add(pAssocPxy);
        this.eeMutex.unlock();

        pAssocPxy.setChannelInitiate(pChannelInitiate);
        pChannelInitiate.setChannelInform(pChannelInform);

        if (this.traceStarted && this.trace != null)
        {
            // start the trace of default logger on the PDu Translator
            ISLE_TraceControl pTraceControl = pAssocPxy.getTranslatorTraceControl();
            if (pTraceControl != null)
            {
                try
                {
                    pTraceControl.startTrace(this.trace, this.traceLevel, true);
                }
                catch (SleApiException e)
                {
                    LOG.log(Level.FINE, "SleApiException ", e);
                }
            }
            // start the trace of default logger on the PDU translator
            pTraceControl = pAssocPxy.getChannelTraceControl();
            if (pTraceControl != null)
            {
                try
                {
                    pTraceControl.startTrace(this.trace, this.traceLevel, true);
                }
                catch (SleApiException e)
                {
                    LOG.log(Level.FINE, "SleApiException ", e);
                }
            }
            if (this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_medium.getCode())
            {
                // trace
                String mess = EE_MessageRepository.getMessage(1010);
                this.trace.traceRecord(SLE_TraceLevel.sleTL_medium, SLE_Component.sleCP_proxy, null, mess);
            }
        }

        ISLE_Reporter preporter = EE_APIPX_ReportTrace.getReporterInterface(this.instanceId);
        pChannelInitiate.configure(preporter, this.eeAPIPXDatabase);
    }

    /**
     * Notifies the Listener to stop listening for the portname given as
     * parameter. Moreover, check if some AssocPxy not longer used have to be
     * deleted.
     */
    public void cleanupAssocPxy()
    {
        for (Iterator<EE_APIPX_AssocPxy> it = this.eeAPIPXAssocPxyList.iterator(); it.hasNext();)
        {
            if (it.next().isClosed())
            {
                it.remove();
            }
        }
    }

    /**
     * Delete an AssocPxy object from the list.
     */
    public void cleanAssoc(EE_APIPX_AssocPxy pAssocPxy)
    {
        // remove the AssocPxy from the link
        for (Iterator<EE_APIPX_AssocPxy> it = this.eeAPIPXAssocPxyList.iterator(); it.hasNext();)
        {
            if (it.next().equals(pAssocPxy))
            {
                it.remove();
            }
        }
    }

    /**
     * This call sets the new configuration. CodesS_OK The new configuration is
     * successfully set. E_SLE_STATE The referenced listener is still not
     * initialized.
     */
    public HRESULT updateConfiguration(EE_APIPX_Database db)
    {
        this.eeMutex.lock();
        if (this.eeAPIPXDatabase == null)
        {
            // not possible
            this.eeMutex.unlock();
            return HRESULT.SLE_E_STATE;
        }

        // set the Listener before
        if (this.eeAPIPXListener == null)
        {
            this.eeMutex.unlock();
            return HRESULT.SLE_E_STATE;
        }
        else
        {
            HRESULT res = HRESULT.S_OK;
            res = this.eeAPIPXListener.updateConfiguration(db);
            if (res == HRESULT.S_OK)
            {
                // listener set - set the private field
                this.eeAPIPXDatabase = db;
            }
            this.eeMutex.unlock();
            return res;
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
        else if (iid == ISLE_TraceControl.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void startTrace(ISLE_Trace trace, SLE_TraceLevel level, boolean forward) throws SleApiException
    {
        this.traceStarted = true;
        this.traceLevel = level;
        this.trace = trace;
    }

    @Override
    public void stopTrace() throws SleApiException
    {
        this.traceStarted = false;
    }

    @Override
    public HRESULT registerPort(ISLE_SII ssid, String portId, EE_Reference<Integer> regId)
    {
        HRESULT res = HRESULT.S_OK;

        if (this.eeAPIPXListener == null)
        {
            return HRESULT.E_FAIL;
        }

        this.eeMutex.lock();
        res = this.eeAPIPXRegistry.registerPort(ssid, portId, regId);
        int count = this.eeAPIPXRegistry.getPortRegistrationCount(portId);
        this.eeMutex.unlock();

        if (res == HRESULT.S_OK && count == 1)
        {
            res = this.eeAPIPXListener.startListen(portId);
        }

        return res;
    }

    @Override
    public HRESULT deregisterPort(int regId)
    {
        if (this.eeAPIPXListener == null)
        {
            return HRESULT.E_FAIL;
        }

        EE_Reference<String> portId = new EE_Reference<String>();
        portId.setReference("");
        this.eeMutex.lock();
        HRESULT res = this.eeAPIPXRegistry.deregisterPort(regId, portId);
        if (res == HRESULT.S_OK)
        {
            if (this.eeAPIPXRegistry.getPortRegistrationCount(portId.getReference()) == 0)
            {
                this.eeAPIPXListener.stopListen(portId.getReference());
            }
            cleanupAssocPxy();
        }

        this.eeMutex.unlock();
        return res;
    }

    public HRESULT deregisterPort(EE_APIPX_Link plink)
    {
        EE_Reference<String> portId = new EE_Reference<String>();
        portId.setReference("");
        this.eeMutex.lock();
        HRESULT res = this.eeAPIPXRegistry.deregisterPort(plink, portId);
        if (res == HRESULT.S_OK)
        {
            if (this.eeAPIPXRegistry.getPortRegistrationCount(portId.getReference()) == 0)
            {
                this.eeAPIPXListener.stopListen(portId.getReference());
            }
            cleanupAssocPxy();
        }

        this.eeMutex.unlock();
        return res;
    }

    public EE_APIPX_Link getLink(ISLE_SII psiid)
    {
        if (psiid == null)
        {
            return null;
        }

        this.eeMutex.lock();
        EE_APIPX_Link pLink = this.eeAPIPXRegistry.getLink(psiid);
        this.eeMutex.unlock();

        return pLink;
    }

    public void setLink(EE_APIPX_Link plink, ISLE_SII psiid)
    {
        this.eeMutex.lock();
        this.eeAPIPXRegistry.setLink(plink, psiid);
        this.eeMutex.unlock();
    }

    public ISLE_SII getSii(EE_APIPX_Link pLink)
    {
        this.eeMutex.lock();
        ISLE_SII psii = this.eeAPIPXRegistry.getSii(pLink);
        this.eeMutex.unlock();
        return psii;
    }

}
