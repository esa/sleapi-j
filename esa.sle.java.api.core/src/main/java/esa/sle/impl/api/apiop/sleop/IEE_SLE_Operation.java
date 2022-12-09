package esa.sle.impl.api.apiop.sleop;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.iutl.ISLE_Credentials;
import esa.sle.impl.ifs.gen.EE_MessageRepository;

/**
 * The class EE_SLEOP_Operation implements the interface ISLE_Operation exported
 * by the component class 'Operation' defined in reference [SLE-API]. It defines
 * those attributes, which are common to all specific types of operation
 * objects. At creation time the client class deriving from EE_SLE_Operation has
 * to call the special protected constructor that takes the service type and the
 * operation type as input argument. Usage for derived classes: The most derived
 * class implements the Copy() function, which performs a deep copy of the
 * operation object. The implementation uses the protected copy-constructor of
 * the base-class. The copy-constructor is not seen on the class diagram,
 * because it is generated automatically. Furthermore the derived class has to
 * implement the methods Print() and VerifyInvocationArguments(). The Print()
 * function shall be implemented such that it first calls the protected
 * printOn() function of IEE_SLE_Operation. The VerifyInvocationArguments() has
 * to be implemented such that the base-class is called first. Note that the
 * derived class has to implement only the methods inherited by the interface it
 * implements directly. The class uses EE_RecursiveMutex for the implementation
 * of the locking methods. This has been chosen for the convenience of the SLE
 * application programmer to prevent self-inflicting deadlocks.
 */
public abstract class IEE_SLE_Operation implements ISLE_Operation
{
    /**
     * The SLE service type for the operation.
     */
    private final SLE_ApplicationIdentifier opServiceType;

    /**
     * The operation type
     */
    private final SLE_OpType opType;

    /**
     * The credentials of the invoker of the operation. If null, credentials are
     * not used.
     */
    private ISLE_Credentials invokerCredentials;

    /**
     * Confirmed/unconfirmed operation. To be set via the constructor.
     */
    private final boolean confirmed;

    private final int version;

    private final ISLE_Reporter reporter;

    private final Lock lock;


    /**
     * /////////////////////////////////////////////////////// This protected
     * constructor initializes the object according to the delivered
     * argument(s). Note that the derived class has to set the argument
     * <confirmed> only if it is a confirmed operation.
     * ///////////////////////////////////////////////////////
     */
    protected IEE_SLE_Operation(SLE_ApplicationIdentifier srvType,
                                SLE_OpType opType,
                                int version,
                                boolean confirmed,
                                ISLE_Reporter preporter)
    {
        this.opServiceType = srvType;
        this.opType = opType;
        this.version = version;
        this.confirmed = confirmed;
        this.reporter = preporter;
        this.invokerCredentials = null;
        this.lock = new ReentrantLock();
    }

    /**
     * Constructor with no arguments
     */
    @SuppressWarnings("unused")
    private IEE_SLE_Operation()
    {
        this.opServiceType = SLE_ApplicationIdentifier.sleAI_invalid;
        this.opType = SLE_OpType.sleOT_bind;
        this.version = 0;
        this.confirmed = false;
        this.reporter = null;
        this.invokerCredentials = null;
        this.lock = new ReentrantLock();
    }

    /**
     * Copy constructor
     * 
     * @param right
     */
    protected IEE_SLE_Operation(IEE_SLE_Operation right)
    {
        this.opServiceType = right.opServiceType;
        this.opType = right.opType;
        this.version = 0;
        this.confirmed = right.confirmed;
        this.reporter = right.reporter;
        this.invokerCredentials = right.invokerCredentials.copy();
        this.lock = new ReentrantLock();
    }

    /**
	 * 
	 */
    @Override
    public SLE_ApplicationIdentifier getOpServiceType()
    {
        return this.opServiceType;
    }

    /**
	 * 
	 */
    @Override
    public SLE_OpType getOperationType()
    {
        return this.opType;
    }

    /**
	 * 
	 */
    @Override
    public int getOpVersionNumber()
    {
        return this.version;
    }

    /**
	 * 
	 */
    @Override
    public boolean isConfirmed()
    {
        return this.confirmed;
    }

    /**
	 * 
	 */
    @Override
    public ISLE_Credentials getInvokerCredentials()
    {
        return this.invokerCredentials;
    }

    /**
	 * 
	 */
    @Override
    public void setInvokerCredentials(ISLE_Credentials credentials)
    {
        this.invokerCredentials = credentials.copy();
    }

    /**
	 * 
	 */
    @Override
    public void putInvokerCredentials(ISLE_Credentials pcredentials)
    {
        this.invokerCredentials = pcredentials;
    }

    /**
     * @throws SleApiException
     */
    @Override
    public void verifyInvocationArguments() throws SleApiException
    {
        // no checks are defined for ISLE_Operation, basically there is OK
        // HRESULT code
    }

    /**
	 * 
	 */
    @Override
    public void lock()
    {
        this.lock.lock();
    }

    /**
     * @throws SleApiException
     */
    @Override
    public void tryLock() throws SleApiException
    {
        boolean isLocked = this.lock.tryLock();
        if (!isLocked)
        {
            throw new SleApiException(HRESULT.SLE_S_LOCKED);
        }
    }

    @Override
    public void unlock()
    {
        unlock();
    }

    /**
     * @param os
     * @param maxDumpLength
     */
    protected void printOn(StringBuilder os, int maxDumpLength)
    {
        os.append("\n");
        os.append("Operation              : " + this.opServiceType.toString() + " - " + this.opType.toString() + "\n");
        os.append("Version                : " + this.version + "\n");
        os.append("Confirmed Operation    : " + this.confirmed + "\n");
        os.append("Invoker Credentials    : ");

        if (this.invokerCredentials != null)
        {
            os.append(this.invokerCredentials.dump() + "\n");
        }
        os.append("\n");
    }

    /**
     * Forwards the logging information to the application.
     * 
     * @param msgType
     * @param msgId
     * @param p1
     * @param p2
     * @param p3
     */
    protected void logRecord(SLE_LogMessageType msgType, long msgId, String... p)
    {
        if (this.reporter == null)
        {
            return;
        }
        String theMsg = EE_MessageRepository.getMessage(msgId, p);
        this.reporter.logRecord(SLE_Component.sleCP_operations, null, msgType, msgId, theMsg);
    }

    /**
     * Logs a message to the reporter. Returns the given result code.
     * 
     * @param result
     * @param msgId
     * @return
     */
    protected HRESULT logAlarm(HRESULT result, long msgId, String... params)
    {
        if (result != HRESULT.S_OK && this.reporter != null)
        {
            String os = this.opServiceType.toString() + " - " + this.opType.toString();

            if (params.length == 0)
            {
                logRecord(SLE_LogMessageType.sleLM_alarm, msgId, os);
            }
            else if (params.length == 1 && params[0] != null)
            {
                logRecord(SLE_LogMessageType.sleLM_alarm, msgId, os, params[0]);
            }
            else if (params.length == 2 && params[0] != null && params[1] != null)
            {
                logRecord(SLE_LogMessageType.sleLM_alarm, msgId, os, params[0], params[1]);
            }

        }
        return result;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.confirmed ? 1231 : 1237);
        result = prime * result + ((this.invokerCredentials == null) ? 0 : this.invokerCredentials.hashCode());
        result = prime * result + ((this.opServiceType == null) ? 0 : this.opServiceType.hashCode());
        result = prime * result + ((this.opType == null) ? 0 : this.opType.hashCode());
        result = prime * result + this.version;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        IEE_SLE_Operation other = (IEE_SLE_Operation) obj;
        if (this.confirmed != other.confirmed)
        {
            return false;
        }
        if (this.invokerCredentials == null)
        {
            if (other.invokerCredentials != null)
            {
                return false;
            }
        }
        else if (!this.invokerCredentials.equals(other.invokerCredentials))
        {
            return false;
        }
        if (this.opServiceType != other.opServiceType)
        {
            return false;
        }
        if (this.opType != other.opType)
        {
            return false;
        }
        if (this.version != other.version)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "IEE_SLE_Operation [opServiceType=" + this.opServiceType + ", opType=" + this.opType
               + ", invokerCredentials=" + ((this.invokerCredentials != null) ? this.invokerCredentials : "")
               + ", confirmed=" + this.confirmed + ", version=" + this.version + ", reporter=" + this.reporter
               + ", lock=" + this.lock + "]";
    }

}
