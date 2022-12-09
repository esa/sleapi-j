package esa.sle.si.repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.service.component.ComponentContext;

import esa.sle.service.loader.ISLE_SIRepository;
import esa.sle.service.loader.ISLE_SIRepositoryInform;
import esa.sle.sicf.si.descriptors.SIDescriptor;

public class SIRepository implements ISLE_SIRepository
{

    private static final Logger LOG = Logger.getLogger(SIRepository.class.getName());

    private final CopyOnWriteArrayList<ISLE_SIRepositoryInform> subscribed = new CopyOnWriteArrayList<>(); // List

    private volatile String folderPath;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private WatchDirectory watcher = null;


    protected synchronized void activate(ComponentContext context) throws IOException
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(" si repository activate called");
        }

        this.folderPath = context.getProperties().get("folderPath").toString();

        File f = new File(this.folderPath);
               
        Path dir = Paths.get(f.getAbsolutePath());

        // TODO find a right place to use the constant from
        // if the directory does not exit prepend the system property
        if(new File(f.getAbsolutePath()).exists() == false && System.getProperty("ccsds.sle.config.dir") != null)
        {
        	dir = Paths.get(System.getProperty("ccsds.sle.config.dir") + File.separatorChar +
        			this.folderPath);
        }        
        
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(" Repository full path: " + dir);
        }
        
        try
        {
            this.watcher = new WatchDirectory(dir, this);
            this.executor.execute(this.watcher);
        }
        catch (IOException e)
        {
            LOG.log(Level.SEVERE, "IOException for directory " + dir + ": ", e);
            throw e;
        }
    }

    protected synchronized void deactivate(ComponentContext context)
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(" si repository deactivate called");
        }
        this.executor.shutdown();
    }

    @Override
    public void subscribe(ISLE_SIRepositoryInform subscriber)
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " subscribe request received ");
        }
        synchronized (this)
        {
            this.subscribed.add(subscriber);
            notifySiAdded(subscriber, getServiceInstanceDescriptors());
        }
    }

    @Override
    public void unsubscribe(ISLE_SIRepositoryInform subscriber)
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " unsubscribe request received");
        }
        synchronized (this)
        {
            this.subscribed.remove(subscriber);
        }
    }

    public void notifySiUpdated(Collection<SIDescriptor> sis)
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " notifySiUpdated ");
        }

        for (ISLE_SIRepositoryInform inform : this.subscribed)
        {
            notifySiUpdated(inform, sis);
        }
    }

    private void notifySiUpdated(ISLE_SIRepositoryInform inform, Collection<SIDescriptor> sis)
    {
        sis.stream().forEach(si -> inform.onServiceUpdated(si));
    }

    public void notifySiAdded(Collection<SIDescriptor> sis)
    {
        for (ISLE_SIRepositoryInform inform : this.subscribed)
        {
            notifySiAdded(inform, sis);
        }
    }

    private void notifySiAdded(ISLE_SIRepositoryInform inform, Collection<SIDescriptor> sis)
    {
        sis.stream().forEach(si -> inform.onServiceAdded(si));
    }

    public void notifySiRemoved(Collection<SIDescriptor> sis)
    {
        for (ISLE_SIRepositoryInform inform : this.subscribed)
        {
            notifySiRemoved(inform, sis);
        }
    }

    private void notifySiRemoved(ISLE_SIRepositoryInform inform, Collection<SIDescriptor> sis)
    {
        sis.stream().forEach(si -> inform.onServiceRemoved(si.getServiceInstanceId().getAsciiForm()));
    }

    @Override
    public synchronized List<SIDescriptor> getServiceInstanceDescriptors()
    {
    	if(this.watcher == null)
    	{
    		return new LinkedList<SIDescriptor>(); // return an empty list
    	}
    	
        return this.watcher.getServiceInstanceDescriptors();
    }
}
