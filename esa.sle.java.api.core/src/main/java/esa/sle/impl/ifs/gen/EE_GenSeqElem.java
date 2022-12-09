/**
 * @(#) EE_GenSeqElem.java
 */

package esa.sle.impl.ifs.gen;

import ccsds.sle.api.isle.iop.ISLE_Operation;

/**
 * Functions as a map element for the EE_OpSequencer map.
 */
public class EE_GenSeqElem
{
    private boolean isInvoke = false;

    private ISLE_Operation pop = null;

    private long seqCount = 0;


    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.isInvoke ? 1231 : 1237);
        result = prime * result + ((this.pop == null) ? 0 : this.pop.hashCode());
        result = prime * result + (int) (this.seqCount ^ (this.seqCount >>> 32));
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
        EE_GenSeqElem other = (EE_GenSeqElem) obj;
        if (this.isInvoke != other.isInvoke)
        {
            return false;
        }
        if (this.pop == null)
        {
            if (other.pop != null)
            {
                return false;
            }
        }
        else if (!this.pop.equals(other.pop))
        {
            return false;
        }
        if (this.seqCount != other.seqCount)
        {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unused")
    private EE_GenSeqElem()
    {
        this.isInvoke = false;
        this.pop = null;
        this.seqCount = 0;
    }

    @SuppressWarnings("unused")
    private EE_GenSeqElem(final EE_GenSeqElem right)
    {
        this.isInvoke = right.isInvoke;
        this.pop = right.pop;
        this.seqCount = right.seqCount;
    }

    public EE_GenSeqElem(boolean isInvoke, ISLE_Operation pop, long seqCount)
    {
        this.isInvoke = isInvoke;
        this.pop = pop;
        this.seqCount = seqCount;
    }

    public boolean getIsInvoke()
    {
        return this.isInvoke;
    }

    public ISLE_Operation getPop()
    {
        return this.pop;
    }

    /**
     * Returns the sequence count belonging to the operation object.
     */
    public long getSeqCount()
    {
        return 0;
    }

}
