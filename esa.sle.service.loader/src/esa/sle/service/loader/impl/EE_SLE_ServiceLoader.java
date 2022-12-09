package esa.sle.service.loader.impl;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.ServiceException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;

import esa.sle.osgi.ISLE_LibraryInstance;
import esa.sle.service.loader.ISLE_LoadedServiceInstance;
import esa.sle.service.loader.ISLE_SIRepository;
import esa.sle.service.loader.ISLE_SIRepositoryInform;
import esa.sle.service.loader.ISLE_ServiceLoader;
import esa.sle.sicf.si.descriptors.SIDescriptor;

public class EE_SLE_ServiceLoader implements ISLE_ServiceLoader, ISLE_SIRepositoryInform
{
    private static final Logger LOG = Logger.getLogger(EE_SLE_ServiceLoader.class.getName());

    private ISLE_LibraryInstance library;

    private ISLE_SIRepository siRepository;

    private String name;

    private boolean ready = false;

    private ComponentContext context;

    private final Map<String, ServiceRegistration<ISLE_LoadedServiceInstance>> registeredServiceInstances = new HashMap<>();

    private final Map<String, ISLE_LoadedServiceInstance> serviceInstances = new HashMap<>();

    private final Lock libraryInstanceMtx = new ReentrantLock();

    private final Lock repoInstanceMtx = new ReentrantLock();


    protected synchronized void activate(ComponentContext context)
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " Service EE_SLE_ServiceLoader activate method called");
        }

        this.name = context.getProperties().get(NAME_PROPERTY).toString();
        this.context = context;

        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " Service EE_SLE_ServiceLoader initialised: " + this.name);
        }

        this.ready = true;

        new Thread(this::doSubscription).start();
    }

    private void doSubscription()
    {
        ISLE_SIRepository repo = null;
        this.repoInstanceMtx.lock();
        try
        {
            repo = this.siRepository;
        }
        finally
        {
            this.repoInstanceMtx.unlock();
        }
        if (repo != null)
        {
            repo.subscribe(this);
        }
        else
        {
            if (LOG.isLoggable(Level.FINEST)){
            	LOG.finest("repo is null");
            }
        }
    }

    protected synchronized void deactivate(ComponentContext context)
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " Service EE_SLE_ServiceLoader deactivate method called");
        }

        this.ready = false;
    }

    @Override
    public synchronized String getName()
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " Service EE_SLE_ServiceLoader deactivate method called");
        }

        if (!this.ready)
        {
            throw new ServiceException("Name not ready");
        }

        return this.name;
    }

    @Override
    public ISLE_LibraryInstance getLibraryInstance()
    {
        this.libraryInstanceMtx.lock();
        try
        {
            if (!this.ready)
            {
                throw new ServiceException("Library instance not ready!");
            }
            return this.library;
        }
        finally
        {
            this.libraryInstanceMtx.unlock();
        }
    }

    @Override
    public ISLE_SIRepository getSIRepository()
    {
        this.repoInstanceMtx.lock();
        try
        {
            if (!this.ready)
            {
                throw new ServiceException("SI repository not ready!");
            }
            return this.siRepository;
        }
        finally
        {
            this.repoInstanceMtx.unlock();
        }
    }

    public void setLibraryInstance(ISLE_LibraryInstance instance)
    {
        this.libraryInstanceMtx.lock();
        try
        {
            if (LOG.isLoggable(Level.INFO))
            {
                LOG.info(Thread.currentThread().getId() + " Service ISLE_LibraryInstance set: " + instance);
            }

            this.library = instance;
        }
        finally
        {
            this.libraryInstanceMtx.unlock();
        }
    }

    public void unsetLibraryInstance(ISLE_LibraryInstance instance)
    {
        this.libraryInstanceMtx.lock();
        try
        {
            if (LOG.isLoggable(Level.INFO))
            {
                LOG.info(Thread.currentThread().getId() + " Service ISLE_LibraryInstance unset");
            }
            if(this.library == instance)
            {
            	this.library = null;
            }
        }
        finally
        {
            this.libraryInstanceMtx.unlock();
        }
    }

    public void setSIRepository(ISLE_SIRepository siRepository)
    {
        this.repoInstanceMtx.lock();
        try
        {
            if (LOG.isLoggable(Level.INFO))
            {
                LOG.info(Thread.currentThread().getId() + " Service ISLE_SIRepository set: " + siRepository);
            }

            this.siRepository = siRepository;
        }
        finally
        {
            this.repoInstanceMtx.unlock();
        }
    }

    public void unsetSIRepository(ISLE_SIRepository siRepository)
    {
        this.repoInstanceMtx.lock();
        try
        {

            if (LOG.isLoggable(Level.INFO))
            {
                LOG.info(Thread.currentThread().getId() + " Service ISLE_SIRepository unset");
            }

            if(this.siRepository == siRepository)
            {
	            this.siRepository.unsubscribe(this);
	            this.siRepository = null;
            }
        }
        finally
        {
            this.repoInstanceMtx.unlock();
        }
    }

    @Override
    public synchronized void onServiceAdded(SIDescriptor serviceInstanceDescr)
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " SI " + serviceInstanceDescr.getServiceInstanceId()
                     + " added in the repository");
        }

        // create the relative Loaded Service Instance
        EE_SLE_LoadedServiceInstance lsi = new EE_SLE_LoadedServiceInstance(serviceInstanceDescr, this);
        // add it to the map of available loaded service instances
        this.serviceInstances.put(serviceInstanceDescr.getServiceInstanceId().getAsciiForm(), lsi);
        // register the just created Loaded Service Instance
        registerServiceInstance(lsi);
    }

    @Override
    public synchronized void onServiceRemoved(String serviceInstanceId)
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " SI " + serviceInstanceId + " removed from the repository");
        }

        // remove it from the map of available loaded service instances
        this.serviceInstances.remove(serviceInstanceId);
        // deregister the Loaded Service Instance
        deregisterServiceInstance(serviceInstanceId);
    }

    @Override
    public synchronized void onServiceUpdated(SIDescriptor serviceInstanceDescr)
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " SI " + serviceInstanceDescr.getServiceInstanceId() + " updated");
        }

        String siIdentifier = serviceInstanceDescr.getServiceInstanceId().getAsciiForm();
        // take the Loaded Service Instance from the map
        ISLE_LoadedServiceInstance lsi = this.serviceInstances.get(siIdentifier);
        // update the descriptor
        lsi.updateDescriptor(serviceInstanceDescr);
    }

    private void deregisterServiceInstance(String si)
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " Deregistering LSI with name " + si);
        }

        ServiceRegistration<ISLE_LoadedServiceInstance> registration = this.registeredServiceInstances.remove(si);
        if (registration != null)
        {
            registration.unregister();
        }
    }

    private void registerServiceInstance(ISLE_LoadedServiceInstance lsi)
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " Publishing LSI with name " + lsi.getServiceInstanceIdentifier());
        }

        Hashtable<String, Object> props = new Hashtable<>();
        props.put(ISLE_LoadedServiceInstance.SIID_PROPERTY, lsi.getServiceInstanceIdentifier());
        props.put(ISLE_LibraryInstance.INSTANCE_NAME_PROPERTY, library.getInstanceName()); // #hd# put the API instance as a property
        ServiceRegistration<ISLE_LoadedServiceInstance> registration = this.context.getBundleContext()
                .registerService(ISLE_LoadedServiceInstance.class, lsi, props);
        this.registeredServiceInstances.put(lsi.getServiceInstanceIdentifier(), registration);
    }
}
