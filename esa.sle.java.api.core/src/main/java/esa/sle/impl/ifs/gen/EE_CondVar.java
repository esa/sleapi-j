package esa.sle.impl.ifs.gen;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class EE_CondVar
{

    private final ReentrantLock lock;

    private final Condition condVar;


    public EE_CondVar()
    {
        this.lock = new ReentrantLock();
        this.condVar = this.lock.newCondition();
    }

    public EE_CondVar(ReentrantLock lock)
    {
        this.lock = lock;
        this.condVar = this.lock.newCondition();
    }

    public Condition getConditionVariable()
    {
        return this.condVar;
    }

    public void signalAll()
    {
        this.condVar.signalAll();
    }

    public void lock()
    {
        this.lock.lock();
    }

    public void unlock()
    {
        this.lock.unlock();
    }

    public void await() throws InterruptedException
    {
        this.condVar.await();
    }

    public void timeWait(long time, TimeUnit unit) throws InterruptedException
    {
        this.condVar.await(time, unit);
    }
}
