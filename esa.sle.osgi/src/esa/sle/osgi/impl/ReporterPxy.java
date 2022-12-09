package esa.sle.osgi.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_Alarm;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.iutl.ISLE_SII;

public class ReporterPxy implements ISLE_Reporter, IUnknown
{
    static private Logger LOG = Logger.getLogger(ReporterPxy.class.getName());

    private ISLE_Reporter reporter;


    public ReporterPxy()
    {
        this.reporter = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T extends IUnknown> T queryInterface(Class<T> arg0)
    {
        if (this.reporter != null)
        {
            return this.reporter.queryInterface(arg0);
        }
        else
        {
            if (ISLE_Reporter.class == arg0)
            {
                return (T) this;
            }
            else
            {
                return null;
            }
        }
    }

    @Override
    public synchronized void logRecord(SLE_Component arg0,
                                       ISLE_SII arg1,
                                       SLE_LogMessageType arg2,
                                       long arg3,
                                       String arg4)
    {
        if (this.reporter != null)
        {
            this.reporter.logRecord(arg0, arg1, arg2, arg3, arg4);
        }
        else
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("The reporter has not been set from the application");
            }
        }
    }

    @Override
    public synchronized void notify(SLE_Alarm arg0, SLE_Component arg1, ISLE_SII arg2, long arg3, String arg4)
    {
        if (this.reporter != null)
        {
            this.reporter.notify(arg0, arg1, arg2, arg3, arg4);
        }
        else
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("The reporter has not been set from the application");
            }
        }
    }

    public synchronized ISLE_Reporter getReporter()
    {
        return this.reporter;
    }

    public synchronized void setReporter(ISLE_Reporter reporter)
    {
        this.reporter = reporter;
    }
}
