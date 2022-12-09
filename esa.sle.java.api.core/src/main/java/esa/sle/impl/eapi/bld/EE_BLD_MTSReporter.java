/**
 * @(#) EE_BLD_MTSReporter.java
 */

package esa.sle.impl.eapi.bld;

import java.util.concurrent.locks.ReentrantLock;

import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_Alarm;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.iutl.ISLE_SII;

/**
 * The class implements a multi thread safe wrapper around the ESLE_Reporter.
 */
public class EE_BLD_MTSReporter implements ISLE_Reporter
{
    ReentrantLock objMutex = new ReentrantLock();

    /**
     * The pointer to the reporter object to be used.
     */
    private ESLE_Reporter pEsleReporter = null;


    /**
     * Constructor which sets the pointer to the ESLE_Reporter.
     */
    public EE_BLD_MTSReporter(ESLE_Reporter pEsleReporter)
    {
        this.pEsleReporter = pEsleReporter;
    }

    /**
     * Enters a message into the system log.
     */
    @Override
    public void logRecord(SLE_Component component, ISLE_SII sii, SLE_LogMessageType type, long messageId, String message)
    {
        this.objMutex.lock();
        this.pEsleReporter.logRecord(component, sii, type, messageId, message);
        this.objMutex.unlock();
    }

    /**
     * Notifies the application of a specific event.
     */
    @Override
    public void notify(SLE_Alarm alarm, SLE_Component component, ISLE_SII sii, long messageId, String message)
    {
        this.objMutex.lock();
        this.pEsleReporter.notify(alarm, component, sii, messageId, message);
        this.objMutex.unlock();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (ISLE_Reporter.class == iid)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

}
