/**
 * @(#) EE_APISE_MTSStatusInformation.java
 */

package esa.sle.impl.api.apise.slese;

import java.util.concurrent.locks.ReentrantLock;

/**
 * The class provides a _lock() and _unlock() function, both constant, to be
 * used by the client that wants to lock any configuration object. The class is
 * foreseen for inheritance only. Classes that implement collection of status
 * information are foreseen to inherit from this class.
 */
public class EE_APISE_MTSStatusInformation
{
    /**
     * The version number of the service type the Service Instance supports.
     */
    private int version;

    private final ReentrantLock mtsMutex = new ReentrantLock();


    protected EE_APISE_MTSStatusInformation()
    {
        this.version = 0;
    }

    public void lock()
    {
        this.mtsMutex.lock();
    }

    public void unlock()
    {
        this.mtsMutex.unlock();
    }

    public int getVersion()
    {
        return this.version;
    }

    public void setVersion(int value)
    {
        this.version = value;
    }
}
