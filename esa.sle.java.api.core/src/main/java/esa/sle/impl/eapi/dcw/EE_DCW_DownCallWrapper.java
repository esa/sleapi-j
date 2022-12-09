package esa.sle.impl.eapi.dcw;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.icc.ISLE_Concurrent;
import ccsds.sle.api.isle.icc.ISLE_TraceControl;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SEAdmin;
import ccsds.sle.api.isle.ise.ISLE_SIAdmin;
import ccsds.sle.api.isle.ise.ISLE_SIFactory;
import ccsds.sle.api.isle.ise.ISLE_ServiceInitiate;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_SIState;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.eapi.dcw.type.DCW_Event_Type;
import esa.sle.impl.eapi.dcw.type.DCW_State;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_DCW_DownCallWrapper implements IDCW_EventQueue, IDCW_SIFactory, IDCW_Admin, ISLE_TraceControl
{
    private static final Logger LOG = Logger.getLogger(EE_DCW_DownCallWrapper.class.getName());

    private ISLE_Reporter pReporter;

    private ISLE_SIFactory pSI_Factory;

    private ISLE_Trace pTrace;

    private ISLE_UtilFactory pUtilFactory;

    private ISLE_SEAdmin pSE;

    private SLE_TraceLevel tracelevel;

    private boolean traceForward;

    private DCW_State state;

    private long windowSize;

    private static Map<String, EE_DCW_DownCallWrapper> pSingleMap = new HashMap<>();

    TreeMap<EE_DCW_ServiceInstance, IUnknown> manages = new TreeMap<EE_DCW_ServiceInstance, IUnknown>();

    EE_DCW_EventDispatcher dispatcher = new EE_DCW_EventDispatcher();

    ReentrantLock objMutex = new ReentrantLock();


    private EE_DCW_DownCallWrapper(final EE_DCW_DownCallWrapper right)
    {
        this.pReporter = right.pReporter;
        this.pSI_Factory = right.pSI_Factory;
        this.pTrace = right.pTrace;
        this.pUtilFactory = right.pUtilFactory;
        this.pSE = right.pSE;
        this.traceForward = right.traceForward;
        this.state = right.state;
        this.windowSize = right.windowSize;
    }

    private EE_DCW_DownCallWrapper()
    {
        this.pReporter = null;
        this.pSI_Factory = null;
        this.pTrace = null;
        this.pUtilFactory = null;
        this.pSE = null;
        this.traceForward = false;
        this.state = DCW_State.dcwSTT_created;
        this.windowSize = 10;
    }

    private IUnknown getServiceInstance(EE_DCW_ServiceInstance pSI)
    {
        if (pSI == null)
        {
            return null;
        }
        return this.manages.get(pSI);
    }

    public static synchronized EE_DCW_DownCallWrapper getDCW(String instanceKey)
    {
    	EE_DCW_DownCallWrapper pSingle = pSingleMap.get(instanceKey);
        if (pSingle == null)
        {
            pSingle = new EE_DCW_DownCallWrapper();
            pSingleMap.put(instanceKey, pSingle);
        }
        return pSingle;
    }

    public static synchronized EE_DCW_DownCallWrapper getpSingle(String instanceKey)
    {
        return pSingleMap.get(instanceKey);
    }

    public EE_DCW_ServiceInstance getDCWServiceInstance(IUnknown pSI)
    {
        this.objMutex.lock();
        if (this.state != DCW_State.dcwSTT_running)
        {
            this.objMutex.unlock();
            return null;
        }
        EE_DCW_ServiceInstance pRetVal = null;
        Iterator<Entry<EE_DCW_ServiceInstance, IUnknown>> mi = this.manages.entrySet().iterator();

        while (mi.hasNext())
        {
            Entry<EE_DCW_ServiceInstance, IUnknown> entry = mi.next();
            if (entry.getValue().equals(pSI))
            {
                pRetVal = entry.getKey();
                break;
            }
        }
        this.objMutex.unlock();
        return pRetVal;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == IDCW_SIFactory.class)
        {
            return (T) this;
        }
        else if (iid == IDCW_EventQueue.class)
        {
            return (T) this;
        }
        else if (iid == IDCW_Admin.class)
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
    public void configure(ISLE_SEAdmin pse, ISLE_UtilFactory putilFactory, ISLE_Reporter preporter) throws SleApiException
    {
        this.objMutex.lock();
        if (this.state != DCW_State.dcwSTT_created)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }
        else
        {
            this.pSI_Factory = pse.queryInterface(ISLE_SIFactory.class);
            if (this.pSI_Factory == null)
            {
                this.objMutex.unlock();
                throw new SleApiException(HRESULT.E_FAIL);
            }
            else
            {
                this.pSE = pse;
                this.pReporter = preporter;
                this.pUtilFactory = putilFactory;
                this.state = DCW_State.dcwSTT_configured;
                this.objMutex.unlock();
            }
        }
    }

    @Override
    public void start() throws SleApiException
    {
        this.objMutex.lock();
        if ((this.state != DCW_State.dcwSTT_configured) && (this.state != DCW_State.dcwSTT_terminated))
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }
        else
        {
            ISLE_Concurrent retval = this.pSE.queryInterface(ISLE_Concurrent.class);
            HRESULT res = HRESULT.S_OK;
            if (retval != null)
            {
                try
                {
                    retval.startConcurrent();
                }
                catch (SleApiException e)
                {
                    res = e.getHResult();
                }
                if (res == HRESULT.S_OK)
                {
                    this.state = DCW_State.dcwSTT_running;
                }
                else
                {
                    res = HRESULT.E_FAIL;
                }
            }
            else
            {
                res = HRESULT.E_FAIL;
            }
            this.objMutex.unlock();
            if (res != HRESULT.S_OK)
            {
                throw new SleApiException(res);
            }
        }
    }

    @Override
    public void terminate() throws SleApiException
    {
        this.objMutex.lock();
        if (this.state != DCW_State.dcwSTT_running)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }
        else
        {
            ISLE_Concurrent ptmp = this.pSE.queryInterface(ISLE_Concurrent.class);
            this.state = DCW_State.dcwSTT_terminated;
            HRESULT res = HRESULT.S_OK;
            if (ptmp != null)
            {
                try
                {
                    ptmp.terminateConcurrent();
                }
                catch (SleApiException e)
                {
                    res = e.getHResult();
                }
                if (res != HRESULT.S_OK)
                {
                    ptmp = null;
                }
            }
            else
            {
                res = HRESULT.E_FAIL;
            }
            this.objMutex.unlock();
            if (res != HRESULT.S_OK)
            {
                throw new SleApiException(res);
            }
        }
    }

    @Override
    public void shutdown() throws SleApiException
    {
        this.objMutex.lock();
        if (this.state != DCW_State.dcwSTT_terminated)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }
        else
        {
            Iterator<Entry<EE_DCW_ServiceInstance, IUnknown>> itrs = this.manages.entrySet().iterator();
            while (itrs.hasNext())
            {
                Entry<EE_DCW_ServiceInstance, IUnknown> entry = itrs.next();
                EE_DCW_ServiceInstance ptmp = entry.getKey();
                ptmp.stopHandlingEvents();
            }
            this.manages.clear();
            // HRESULT hres = _pSE->ShutDown();
            // don't do the shutdown, done by the builder
            this.pSE = null;
            this.pUtilFactory = null;
            this.pTrace = null;
            this.state = DCW_State.dcwSTT_shutDown;
            this.objMutex.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T createServiceInstance(Class<T> iid,
                                                        SLE_ApplicationIdentifier serviceType,
                                                        int version,
                                                        SLE_AppRole role,
                                                        int maxPendingEvents) throws SleApiException
    {
        this.objMutex.lock();
        ISLE_SIAdmin pAdmin = null;
        HRESULT res = HRESULT.S_OK;
        if (this.state != DCW_State.dcwSTT_running)
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }
        else if (maxPendingEvents < 1)
        {
            throw new SleApiException(HRESULT.SLE_E_BADVALUE);
        }
        else
        {
            EE_DCW_ServiceInstance pNew = new EE_DCW_ServiceInstance(this.windowSize,
                                                                     this.dispatcher,
                                                                     this.pReporter,
                                                                     maxPendingEvents);
            ISLE_ServiceInform servInform = pNew.queryInterface(ISLE_ServiceInform.class);
            // down call wrapper requires 2 interfaces
            // - IUnknown, and ISLE_admin.

            if (servInform != null)
            {
                try
                {
                    pAdmin = this.pSI_Factory.createServiceInstance(ISLE_SIAdmin.class,
                                                                    serviceType,
                                                                    version,
                                                                    role,
                                                                    servInform);
                }
                catch (SleApiException e)
                {
                    LOG.log(Level.FINE, "SleApiException ", e);
                    res = e.getHResult();
                }
                if (pAdmin != null)
                {
                    IUnknown local = pAdmin.queryInterface(IUnknown.class);
                    if (local != null)
                    {
                        T ppsi = pAdmin.queryInterface(iid);
                        if (ppsi != null)
                        {
                            // now have IUnknown, ISLE_SIAdmin,
                            // iid, plus internal
                            // reference to EE_DCW_ServiceInstance.
                            pNew.setSource(pAdmin);
                            this.manages.put(pNew, ppsi);
                        }
                        else
                        {
                            // couldnt get requested interface of SI
                            res = HRESULT.SLE_E_UNKNOWN;
                            throw new SleApiException(res);
                        }
                    }
                    else
                    {
                        // couldnt get IUnknown of SI
                        res = HRESULT.E_FAIL;
                        throw new SleApiException(res);
                    }
                }
                else
                {
                    // couldn't create a ServiceInstance with Admin role.
                    res = HRESULT.E_FAIL;
                    throw new SleApiException(res);
                }

            }
            else
            {
                // couldn't get the ServiceInform Interface.
                res = HRESULT.E_FAIL;
                throw new SleApiException(res);
            }

        }
        this.objMutex.unlock();

        return (T) pAdmin;
    }

    @Override
    public void destroyServiceInstance(IUnknown psi) throws SleApiException
    {
        this.objMutex.lock();
        ISLE_ServiceInitiate pService = psi.queryInterface(ISLE_ServiceInitiate.class);
        HRESULT retVal = HRESULT.S_OK;
        if (pService != null)
        {
            if (pService.getSIState() != SLE_SIState.sleSIS_unbound)
            {
                retVal = HRESULT.SLE_E_STATE;
                throw new SleApiException(retVal);
            }
            else
            {
                IUnknown psi_iu = psi.queryInterface(IUnknown.class);
                if (psi_iu == null)
                {
                    this.objMutex.unlock();
                    throw new SleApiException(HRESULT.E_FAIL);
                }
                retVal = HRESULT.S_OK;
                Iterator<Entry<EE_DCW_ServiceInstance, IUnknown>> itrs = this.manages.entrySet().iterator();
                while (itrs.hasNext())
                {
                    Entry<EE_DCW_ServiceInstance, IUnknown> entry = itrs.next();
                    if (entry.getValue().equals(psi_iu))
                    {
                        EE_DCW_ServiceInstance ptmp = entry.getKey();
                        ptmp.stopHandlingEvents();
                        IUnknown ptmp2 = entry.getValue();
                        HRESULT res2 = HRESULT.S_OK;
                        try
                        {
                            this.pSI_Factory.destroyServiceInstance(ptmp2);
                        }
                        catch (SleApiException e)
                        {
                            LOG.log(Level.FINE, "SleApiException ", e);
                            res2 = e.getHResult();
                        }
                        if (res2 != HRESULT.S_OK)
                        {
                            retVal = HRESULT.E_FAIL;
                        }
                        else
                        {
                            retVal = HRESULT.S_OK;
                        }
                        this.manages.remove(entry.getKey());
                        break;
                    }
                }

            }

        }
        else
        {
            retVal = HRESULT.E_FAIL;
            throw new SleApiException(retVal);
        }
        this.objMutex.unlock();

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ISLE_Operation> T nextEvent(EE_Reference<DCW_Event_Type> eventType,
                                                  EE_Reference<IUnknown> ppsi,
                                                  EE_Reference<ISLE_Operation> ppop,
                                                  int timeoutSec,
                                                  int timeoutMilliSec) throws SleApiException
    {
        this.objMutex.lock();
        boolean bStateIsGood = false;
        switch (this.state)
        {
        case dcwSTT_created:
            break;
        case dcwSTT_configured:
            break;
        case dcwSTT_running:
            bStateIsGood = true;
            break;
        case dcwSTT_shutDown:
        case dcwSTT_terminated:
        default:
            break;
        }
        this.objMutex.unlock();// prevents locking of the DCW when blocking here
                               // ...
        HRESULT RetVal = HRESULT.SLE_E_STATE;
        if (bStateIsGood)
        {
            EE_Reference<EE_DCW_ServiceInstance> pTmp = new EE_Reference<EE_DCW_ServiceInstance>();
            RetVal = this.dispatcher.nextEvent(eventType, pTmp, ppop, timeoutSec, timeoutMilliSec);
            if (RetVal == HRESULT.S_OK)
            {
                ppsi.setReference(getServiceInstance(pTmp.getReference()));// does
                                                                           // the
                                                                           // addref.
            }
        }
        if (RetVal != HRESULT.S_OK)
        {
            throw new SleApiException(RetVal);
        }
        return (T) ppop.getReference();

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ISLE_Operation> T pollEvent(EE_Reference<DCW_Event_Type> eventType,
                                                  EE_Reference<IUnknown> ppsi,
                                                  EE_Reference<ISLE_Operation> ppop,
                                                  int timeoutSec,
                                                  int timeoutMilliSec) throws SleApiException
    {
        this.objMutex.lock();
        boolean bStateIsGood = false;
        switch (this.state)
        {
        case dcwSTT_created:
            break;
        case dcwSTT_configured:
            break;
        case dcwSTT_running:
            bStateIsGood = true;
            break;
        case dcwSTT_shutDown:
        case dcwSTT_terminated:
        default:
            break;
        }
        HRESULT RetVal = HRESULT.SLE_E_STATE;
        if (bStateIsGood)
        {
            EE_Reference<EE_DCW_ServiceInstance> pTmp = new EE_Reference<EE_DCW_ServiceInstance>();
            RetVal = this.dispatcher.pollEvent(eventType, pTmp, ppop, timeoutSec, timeoutMilliSec);
            ppsi.setReference(getServiceInstance(pTmp.getReference()));
        }
        this.objMutex.unlock();
        if (RetVal != HRESULT.S_OK)
        {
            throw new SleApiException(RetVal);
        }
        return (T) ppsi;
    }

    @Override
    public void flushQueue(IUnknown psi) throws SleApiException
    {
        this.objMutex.lock();
        boolean bStateIsGood = false;
        switch (this.state)
        {
        case dcwSTT_created:
            break;
        case dcwSTT_configured:
            break;
        case dcwSTT_running:
            bStateIsGood = true;
            break;
        case dcwSTT_shutDown:
        case dcwSTT_terminated:
        default:
            break;
        }
        HRESULT retVal = HRESULT.SLE_E_STATE;
        if (bStateIsGood)
        {
            Iterator<Entry<EE_DCW_ServiceInstance, IUnknown>> itrs = this.manages.entrySet().iterator();
            retVal = HRESULT.E_FAIL;
            while (itrs.hasNext())
            {
                Entry<EE_DCW_ServiceInstance, IUnknown> entry = itrs.next();
                if (entry.getValue().equals(psi))
                {
                    entry.getKey().flushQueue();
                    retVal = HRESULT.S_OK;
                    break;
                }
            }
        }
        this.objMutex.unlock();
        if (retVal != HRESULT.S_OK)
        {
            throw new SleApiException(retVal);
        }
    }

    @Override
    public void suspend() throws SleApiException
    {
        HRESULT res = this.dispatcher.suspend();
        if (res != HRESULT.S_OK)
        {
            throw new SleApiException(res);
        }
    }

    @Override
    public void resume() throws SleApiException
    {
        HRESULT res = this.dispatcher.resume();
        if (res != HRESULT.S_OK)
        {
            throw new SleApiException(res);
        }
    }

    @Override
    public void startTrace(ISLE_Trace ptrace, SLE_TraceLevel level, boolean forward) throws SleApiException
    {
        this.objMutex.lock();

        this.pTrace = ptrace;

        this.tracelevel = level;
        this.traceForward = forward;
        HRESULT retval = HRESULT.S_OK;
        if (this.pSE != null)
        {
            ISLE_TraceControl retvalqi = this.pSE.queryInterface(ISLE_TraceControl.class);
            if (retvalqi != null)
            {
                try
                {
                    retvalqi.startTrace(ptrace, this.tracelevel, this.traceForward);
                }
                catch (SleApiException e)
                {
                    retval = e.getHResult();
                }
            }
        }
        this.objMutex.unlock();
        if (retval != HRESULT.S_OK)
        {
            throw new SleApiException(retval);
        }
    }

    @Override
    public void stopTrace() throws SleApiException
    {
        this.objMutex.lock();
        HRESULT res = HRESULT.S_OK;
        if (this.pSE != null)
        {
            ISLE_TraceControl retvalqi = this.pSE.queryInterface(ISLE_TraceControl.class);
            if (retvalqi != null)
            {
                try
                {
                    retvalqi.stopTrace();
                }
                catch (SleApiException e)
                {
                    res = e.getHResult();
                }
            }
        }
        this.pTrace = null;
        this.objMutex.unlock();
        if (res != HRESULT.S_OK)
        {
            throw new SleApiException(res);
        }
    }

    void setWindowSize(long windowSize)
    {
        this.objMutex.lock();
        this.windowSize = windowSize;
        this.objMutex.unlock();
    }
}
