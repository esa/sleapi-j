/**
 * @(#) EE_SLE_Credentials.java
 */

package esa.sle.impl.api.apiut;

import java.util.Arrays;

import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_TimeFmt;
import ccsds.sle.api.isle.it.SLE_TimeRes;
import ccsds.sle.api.isle.iutl.ISLE_Credentials;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;

/**
 * ////////////////////////////////////////////////// The class provides an
 * implementation of the interface ISLE_Credentials as specified in reference
 * [SLE-API] for the component class 'SLE Credentials'. For the object creation
 * the client has to use the default constructor (generated automatically). The
 * Copy() function is implemented by using the private copy-constructor
 * (generated automatically). //////////////////////////////////////////////////
 */
public class EE_SLE_Credentials implements ISLE_Credentials
{
    /**
     * The time when the message digest was generated.
     */
    private ISLE_Time timeRef;

    /**
     * The random number
     */
    private long randomNumber;

    /**
     * The message digest (the protected)
     */
    private byte[] messageDigest;


    /**
	 * 
	 */
    public EE_SLE_Credentials()
    {
        this.timeRef = null;
        this.randomNumber = 0;
        this.messageDigest = null;
    }

    /**
     * @param right
     */
    private EE_SLE_Credentials(EE_SLE_Credentials right)
    {
        if ((right.messageDigest != null) && (right.messageDigest.length != 0))
        {
            this.messageDigest = new byte[right.messageDigest.length];
            System.arraycopy(right.messageDigest, 0, this.messageDigest, 0, right.messageDigest.length);
        }
        else
        {
            this.messageDigest = null;
        }

        if (right.timeRef != null)
        {
            this.timeRef = right.timeRef.copy();
        }
        else
        {
            this.timeRef = null;
        }

        this.randomNumber = right.randomNumber;
    }

    /**
     * @param iid
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Credentials.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    /**
     * @return
     */
    @Override
    public byte[] getProtected()
    {
        if ((this.messageDigest.length != 0) && (this.messageDigest != null))
        {
            return Arrays.copyOf(this.messageDigest, this.messageDigest.length);
        }

        return null;
    }

    /**
     * @param hashCode
     */
    @Override
    public void setProtected(byte[] hashCode)
    {
        if ((hashCode != null) && (hashCode.length != 0))
        {
            this.messageDigest = Arrays.copyOf(hashCode, hashCode.length);
        }
    }

    /**
     * @return
     */
    @Override
    public long getRandomNumber()
    {
        return this.randomNumber;
    }

    /**
     * @return
     */
    @Override
    public ISLE_Time getTimeRef()
    {
        return this.timeRef;
    }

    /**
     * @param randomNumber
     */
    @Override
    public void setRandomNumber(long randomNumber)
    {
        this.randomNumber = randomNumber;
    }

    /**
     * @param time
     */
    @Override
    public void setTimeRef(ISLE_Time time)
    {
        this.timeRef = time.copy();
    }

    /**
	 *
	 */
    @Override
    public ISLE_Credentials copy()
    {
        return new EE_SLE_Credentials(this);
    }

    /**
     * @return
     */
    @Override
    public String dump()
    {
        StringBuilder dumpStr = new StringBuilder();
        dumpStr.append("\n");
        dumpStr.append("       Random Number        : " + this.randomNumber + "\n");

        if (this.timeRef != null)
        {
            dumpStr.append("       Generation Time      : "
                           + this.timeRef.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_microSec)
                           + "\n");
        }

        byte[] time_cds = this.timeRef.getCDS();
        if (time_cds != null)
        {
            dumpStr.append("       Generation Time (CDS): " + EE_GenStrUtil.convAscii(time_cds, time_cds.length) + "\n");
        }

        if ((this.messageDigest != null) && (this.messageDigest.length != 0))
        {
            dumpStr.append("       Hash Code            : "
                           + EE_GenStrUtil.convAscii(this.messageDigest, this.messageDigest.length) + "\n");
        }

        dumpStr.append("       Hash Code size       : " + this.messageDigest.length);

        return dumpStr.toString();
    }

    /**
     * @return
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.messageDigest);
        result = prime * result + (int) (this.randomNumber ^ (this.randomNumber >>> 32));
        result = prime * result + ((this.timeRef == null) ? 0 : this.timeRef.hashCode());
        return result;
    }

    /**
     * @param obj
     * @return
     */
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

        EE_SLE_Credentials other = (EE_SLE_Credentials) obj;

        if (!Arrays.equals(this.messageDigest, other.messageDigest))
        {
            return false;
        }

        if (this.randomNumber != other.randomNumber)
        {
            return false;
        }

        if (this.timeRef == null)
        {
            if (other.timeRef != null)
            {
                return false;
            }
        }
        else if (!this.timeRef.equals(other.timeRef))
        {
            return false;
        }

        return true;
    }

    @Override
    public String toString()
    {
        return "EE_SLE_Credentials [timeRef=" + this.timeRef + ", randomNumber=" + this.randomNumber
               + ", messageDigest=" + Arrays.toString(this.messageDigest) + "]";
    }
}
