/**
 * @(#) EE_ElapsedTimer.java
 */

package esa.sle.impl.ifs.time;

import java.util.Timer;
import java.util.TimerTask;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.icc.ISLE_TimeoutProcessor;

/**
 * Class provides an elapsed time timer. Client of this class should not call
 * the base class (thread) functions with the exception of _AddRef(), and
 * _Release(). When the timer expires, the timer will call ProcessTimeout A
 * condition variable to provide the actual waiting. Note that due to threading
 * scheduling considerations, it is not guaranteed that the accuracy is to the
 * precision of the arguments.
 */
public class EE_ElapsedTimer
{

    private final Timer timer;

    private RemindTask currentTask;

    private ISLE_TimeoutProcessor gtp;


    public EE_ElapsedTimer()
    {
        this.timer = new Timer();
    }

    public void start(EE_Duration argexpiry, ISLE_TimeoutProcessor argtimeoutproc, int invocationId) throws SleApiException
    {
        this.gtp = argtimeoutproc;
        if (argexpiry.getSeconds() > 50000000)
        {
            throw new SleApiException(HRESULT.SLE_E_TIME);
        }
        else
        {
            synchronized (this.timer)
            {
                if (this.currentTask != null)
                {
                    throw new SleApiException(HRESULT.SLE_E_TIME);
                }
                RemindTask rt = new RemindTask(argtimeoutproc, invocationId);

                this.timer.schedule(rt, argexpiry.getSeconds() * 1000);
                this.currentTask = rt;
            }
        }
    }

    public void restart(EE_Duration argexpiry, int invocationId) throws SleApiException
    {
        synchronized (this.timer)
        {
            if (this.currentTask != null)
            {
                this.currentTask.cancelTask();
                this.currentTask = null;
            }
            start(argexpiry, this.gtp, invocationId);
        }
    }

    public void cancel()
    {
        this.timer.cancel();
        synchronized (this.timer)
        {
            if (this.currentTask != null)
            {
                this.currentTask.cancelTask();
                this.currentTask = null;
            }
        }
    }


    class RemindTask extends TimerTask
    {

        private int localInvocationId = 0;

        private final ISLE_TimeoutProcessor localTimeoutProcessor;

        private volatile boolean mustRun = true;


        public RemindTask(ISLE_TimeoutProcessor argtimeoutproc, int invocationId)
        {
            this.localInvocationId = invocationId;
            this.localTimeoutProcessor = argtimeoutproc;
        }

        public void cancelTask()
        {
            synchronized (EE_ElapsedTimer.this.timer)
            {
                this.mustRun = false;
                cancel();
            }
        }

        @Override
        public void run()
        {
            synchronized (EE_ElapsedTimer.this.timer)
            {
                if (!this.mustRun)
                {
                    return;
                }
                else
                {
                    EE_ElapsedTimer.this.currentTask = null;
                }
            }
            this.localTimeoutProcessor.processTimeout(EE_ElapsedTimer.this, this.localInvocationId);
        }
    }
}
