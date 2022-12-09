/**
 * @(#) EE_SLE_TransferBuffer.java
 */

package esa.sle.impl.api.apiop.sleop;

import java.util.Iterator;
import java.util.LinkedList;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_TransferBuffer;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_OpType;

/**
 * The class implements the TRANSFER-BUFFER operation.
 */
public class EE_SLE_TransferBuffer extends IEE_SLE_Operation implements ISLE_TransferBuffer
{
    /**
     * The list holding all operation objects belonging to the transfer buffer
     * operation.
     */
    private final LinkedList<ISLE_Operation> opList;

    /**
     * Holds the maximum numbers of elements that can be stored in the buffer
     */
    private long maxSize;

    /**
     * The list iterator.
     */
    private Iterator<ISLE_Operation> iter;


    /**
     * This constructor initializes the object according to the delivered
     * argument(s) and passes the argument(s) to the constructor of the
     * base-class.
     * 
     * @param opSrvType
     * @param version
     * @param preporter
     */
    public EE_SLE_TransferBuffer(SLE_ApplicationIdentifier opSrvType, int version, ISLE_Reporter preporter)
    {
        super(opSrvType, SLE_OpType.sleOT_transferBuffer, version, false, preporter);
        this.maxSize = 1;
        this.opList = new LinkedList<ISLE_Operation>();
        this.iter = this.opList.iterator();

    }

    /**
     * Copy constructor
     * 
     * @param right
     */
    protected EE_SLE_TransferBuffer(EE_SLE_TransferBuffer right)
    {
        super(right);
        this.maxSize = right.maxSize;
        this.opList = new LinkedList<ISLE_Operation>(right.opList);
        this.iter = this.opList.iterator();

    }

    /**
	 * 
	 */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Operation.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_TransferBuffer.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    @Override
    public synchronized long getMaximumSize()
    {
        return this.maxSize;
    }

    @Override
    public synchronized void setMaximumSize(long size) throws SleApiException
    {
        long currentSize = this.opList.size();
        if (currentSize < size)
        {
            this.maxSize = size;
        }
        else
        {
            throw new SleApiException(HRESULT.E_FAIL, "Error while setting the Maximum Size");
        }
    }

    /**
	 * 
	 */
    @Override
    public synchronized long getSize()
    {
        return this.opList.size();
    }

    /**
	 * 
	 */
    @Override
    public synchronized boolean full()
    {
        return (this.opList.size() >= this.maxSize) ? true : false;
    }

    /**
	 * 
	 */
    @Override
    public synchronized boolean empty()
    {
        return this.opList.isEmpty();
    }

    /**
	 * 
	 */
    @Override
    public synchronized void append(ISLE_Operation poperation)
    {
        this.opList.addLast(poperation); // SLEAPIJ-34 inverse order of frames
    }

    /**
	 * 
	 */
    @Override
    public synchronized void prepend(ISLE_Operation poperation, boolean extend)
    {
        if (full() && extend)
        {
            this.maxSize++;
        }
        this.opList.addFirst(poperation); // SLEAPIJ-34 inverse order of frames
    }

    /**
	 * 
	 */
    @Override
    public synchronized ISLE_Operation removeFront()
    {
        if (!this.opList.isEmpty())
        {
            return this.opList.removeFirst();
        }
        return null;
    }

    /**
	 * 
	 */
    @Override
    public synchronized ISLE_Operation removeRear()
    {
        if (!this.opList.isEmpty())
        {
            return this.opList.removeLast();
        }
        return null;
    }

    /**
	 * 
	 */
    @Override
    public synchronized ISLE_Operation front()
    {
        if (this.opList.isEmpty())
        {
            return this.opList.getFirst();
        }
        return null;
    }

    /**
	 * 
	 */
    @Override
    public synchronized void clear()
    {
        this.opList.clear();
    }

    /**
	 * 
	 */
    @Override
    public synchronized void reset()
    {
        this.iter = this.opList.listIterator();
    }

    /**
	 * 
	 */
    @Override
    public synchronized boolean moreData()
    {
        return this.iter.hasNext();
    }

    /**
	 * 
	 */
    @Override
    public synchronized ISLE_Operation next()
    {
        if (!moreData())
        {
            return null;
        }

        ISLE_Operation pop = this.iter.next();
        return pop;
    }

    /**
	 * 
	 */
    @Override
    public synchronized ISLE_Operation copy()
    {
        return new EE_SLE_TransferBuffer(this);
    }

    /**
	 * 
	 */
    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder os = new StringBuilder(maxDumpLength);
        printOn(os, maxDumpLength);

        os.append("Maximum Buffer Size    : " + this.maxSize + "\n");
        os.append("Current Buffer Size    : " + this.opList.size() + "\n");
        os.append("\n");

        return os.toString();
    }

    @Override
    public synchronized int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (this.maxSize ^ (this.maxSize >>> 32));
        result = prime * result + (moreData() ? 1231 : 1237);
        result = prime * result + ((this.opList == null) ? 0 : this.opList.hashCode());
        return result;
    }

    @Override
    public synchronized boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        EE_SLE_TransferBuffer other = (EE_SLE_TransferBuffer) obj;
        if (this.maxSize != other.maxSize)
        {
            return false;
        }
        if (moreData() != other.moreData())
        {
            return false;
        }
        if (this.opList == null)
        {
            if (other.opList != null)
            {
                return false;
            }
        }
        else if (!this.opList.equals(other.opList))
        {
            return false;
        }
        return true;
    }
}
