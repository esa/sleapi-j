package esa.sle.osgi.impl;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.ServiceException;
import org.osgi.service.component.ComponentContext;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_TimeSource;
import ccsds.sle.api.isle.ise.ISLE_SIFactory;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_BindRole;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.api.apise.slese.EE_APISE_Database;
import esa.sle.impl.api.apiut.EE_SLE_TimeSource;
import esa.sle.impl.eapi.bld.ESLE_APIBuilder;
import esa.sle.osgi.ISLE_LibraryInstance;

/**
 * This class collects all the necessary information from the database
 * configuration and from the xml files in order to create Service Intances.
 */
public class EE_SLE_LibraryInstance implements ISLE_LibraryInstance
{
    private static final Logger LOG = Logger.getLogger(EE_SLE_LibraryInstance.class.getName());

    private String seConfigFilePath;

    private String proxyConfigFilePath;

    private String instanceName;

    private SLE_BindRole bindRole;

    private ReporterPxy reporterPxy;

    private ISLE_Reporter reporter;

    private ISLE_TimeSource timeSource;

    private ISLE_SIFactory siFactory;

    private ISLE_UtilFactory utilFactory;

    private boolean ready = false;

    private ESLE_APIBuilder builder;

    private SLE_AppRole role;


    public EE_SLE_LibraryInstance() 
    {
        this.reporterPxy = new ReporterPxy();
        if (this.reporter != null)
        {
            this.reporterPxy.setReporter(this.reporter);
        }    	
    }
    
    protected synchronized void activate(ComponentContext context)
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " Service EE_SLE_LibraryInstance activate method called for @"
            		+ Integer.toHexString(hashCode()));
        }

        // get the configuration information        
        File scFile = new File(context.getProperties().get(SE_CONFIG_PROPERTY).toString());
        File pcFile = new File(context.getProperties().get(PROXY_CONFIG_PROPERTY).toString());

        this.seConfigFilePath = scFile.getAbsolutePath();
        this.proxyConfigFilePath = pcFile.getAbsolutePath();        
        
        if(scFile.exists() == false && System.getProperty(SLE_CONFIG_DIR_SYSTEM_PROPERTY) != null)
        {
        	scFile = new File(System.getProperty(SLE_CONFIG_DIR_SYSTEM_PROPERTY) + File.separatorChar + 
        					  context.getProperties().get(SE_CONFIG_PROPERTY).toString());
        	LOG.info(Thread.currentThread().getId() + " Use  " + SLE_CONFIG_DIR_SYSTEM_PROPERTY + ": " +
        			System.getProperty(SLE_CONFIG_DIR_SYSTEM_PROPERTY));
        	this.seConfigFilePath = scFile.getAbsolutePath();
        }
        
        if(pcFile.exists() == false && System.getProperty(SLE_CONFIG_DIR_SYSTEM_PROPERTY) != null)
        {
        	pcFile = new File(System.getProperty(SLE_CONFIG_DIR_SYSTEM_PROPERTY) + File.separatorChar + 
        			          context.getProperties().get(PROXY_CONFIG_PROPERTY).toString());
        	this.proxyConfigFilePath = pcFile.getAbsolutePath(); 
        }
        

        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(" Configuration files full path - seConfig: " + this.seConfigFilePath + ", proxyConfig: "
                     + this.proxyConfigFilePath);
        }

        this.instanceName = context.getProperties().get(INSTANCE_NAME_PROPERTY).toString();

        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId()
                     + " Service EE_SLE_LibraryInstance initialised: seConfigFilePath = " + this.seConfigFilePath
                     + ", proxyConfigFilePath=" + this.proxyConfigFilePath + ", instanceName = " + this.instanceName);
        }

        // read role from the database
        EE_APISE_Database db = EE_APISE_Database.getDb(this.instanceName);

        HRESULT rc = db.open(this.seConfigFilePath);
        LOG.info("Open the database, result:" + rc);
        if (rc != HRESULT.S_OK)
        {
            if (LOG.isLoggable(Level.INFO))
            {
                LOG.info(Thread.currentThread().getId() + " *** WARNING: Open Configuration File <"
                         + this.seConfigFilePath + "> failed: ");
            }

            if (rc == HRESULT.E_ACCESSDENIED)
            {
                if (LOG.isLoggable(Level.INFO))
                {
                    LOG.info(Thread.currentThread().getId() + " : access denied");
                }
            }
            else if (rc == HRESULT.SLE_E_NOFILE)
            {
                if (LOG.isLoggable(Level.INFO))
                {
                    LOG.info(Thread.currentThread().getId() + " : no such file");
                }
            }
            else
            {
                if (LOG.isLoggable(Level.INFO))
                {
                    LOG.info("");
                }
            }
        }

        rc = db.readConfigPrms();
        LOG.info("Read configuration parameters, result:" + rc);
        if (rc != HRESULT.S_OK)
        {
            if (LOG.isLoggable(Level.INFO))
            {
                LOG.info(Thread.currentThread().getId() + " ");
                LOG.info(Thread.currentThread().getId() + " *** WARNING: Read Configuration Parameters failed. ");
                LOG.info(Thread.currentThread().getId() + "     File:  " + this.seConfigFilePath);
                LOG.info(Thread.currentThread().getId() + "     Error: " + db.getErrorText());
                LOG.info(Thread.currentThread().getId() + " ");
            }
            return;
        }

        this.role = db.getApplicationRole();
        db.close();
        EE_APISE_Database.resetDb(this.instanceName);

        if (this.role.equals(SLE_AppRole.sleAR_user))
        {
            this.bindRole = SLE_BindRole.sleBR_initiator;
        }
        else if (this.role.equals(SLE_AppRole.sleAR_provider))
        {
            this.bindRole = SLE_BindRole.sleBR_responder;
        }

        if (this.timeSource == null)
        {
            this.timeSource = new EE_SLE_TimeSource();
        }

//        this.reporterPxy = new ReporterPxy();
//        if (this.reporter != null)
//        {
//            this.reporterPxy.setReporter(this.reporter);
//        }

        // get the builder
        this.builder = ESLE_APIBuilder.getESLEAPIBuilder(this.instanceName);

        // initialize the builder
        this.builder.initialise(this.seConfigFilePath,
                                this.proxyConfigFilePath,
                                this.reporterPxy,
                                this.bindRole,
                                this.timeSource);

        this.builder.start();

        this.siFactory = this.builder.getSIFactory();
        this.utilFactory = this.builder.getUtilFactory();

        this.ready = true;
    }

    protected synchronized void deactivate(ComponentContext context)
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " Service EE_SLE_LibraryInstance deactivate method called");
        }

        this.siFactory = null;
        this.utilFactory = null;
        this.ready = false;

        this.builder.terminate();
        this.builder.shutdown();
    }

    @Override
    public synchronized String getInstanceName()
    {
        if (!this.ready)
        {
            throw new ServiceException("Instance not ready");
        }
        return this.instanceName;
    }

    @Override
    public synchronized ISLE_SIFactory getSIFactory()
    {
        if (!this.ready)
        {
            throw new ServiceException("SIFactory not ready");
        }
        return this.siFactory;
    }

    @Override
    public synchronized ISLE_UtilFactory getUtilFactory()
    {
        if (!this.ready)
        {
            throw new ServiceException("UtilFactory not ready");
        }
        return this.utilFactory;
    }

    @Override
    public ISLE_Reporter getReporter()
    {
        return this.reporterPxy;
    }

    @Override
    public ISLE_TimeSource getTimeSource()
    {
        return this.timeSource;
    }

    @Override
    public SLE_AppRole getApplRole()
    {
        return this.role;
    }

    public synchronized void setReporter(ISLE_Reporter reporter)
    {
        this.reporterPxy.setReporter(reporter);
        if(reporter != null)
        {
        	LOG.fine("The reporter (@" + Integer.toHexString(reporter.hashCode()) 
        			+ ") has been set into the SLE API Service Component @" 
        			+ Integer.toHexString(hashCode()));
        }
    }

    public synchronized void unsetReporter(ISLE_Reporter reporter)
    {
    	if(this.reporterPxy.getReporter() == reporter)
    	{
    		this.reporterPxy.setReporter(null);
    	}
    }

    public synchronized void setTimeSource(ISLE_TimeSource timeSource)
    {
        this.timeSource = timeSource;
    }

    public synchronized void unsetTimeSource()
    {
        this.timeSource = null;
    }
}
