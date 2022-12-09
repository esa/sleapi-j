package ccsds.sle.api.isle.it;

import java.util.Arrays;

public class SLE_GlobalRDN
{

    private final String value;

    private final int[] oid;


    /**
     * Constructor SLE_GlobalRDN
     * 
     * @param value
     * @param oid
     */
    public SLE_GlobalRDN(String value, int[] oid)
    {
        this.value = value;
        this.oid = Arrays.copyOf(oid, oid.length);
    }

    /**
     * Gets the value.
     * 
     * @return
     */
    public String getValue()
    {
        return this.value;
    }

    /**
     * Gets the oid.
     * 
     * @return
     */
    public int[] getOid()
    {
        return this.oid;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.oid);
        result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
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
        SLE_GlobalRDN other = (SLE_GlobalRDN) obj;
        if (!Arrays.equals(this.oid, other.oid))
        {
            return false;
        }
        if (this.value == null)
        {
            if (other.value != null)
            {
                return false;
            }
        }
        else if (!this.value.equals(other.value))
        {
            return false;
        }
        return true;
    }
}
