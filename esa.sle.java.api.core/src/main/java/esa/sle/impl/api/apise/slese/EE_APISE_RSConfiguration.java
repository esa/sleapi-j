/**
 * @(#) EE_APISE_RSConfiguration.java
 */

package esa.sle.impl.api.apise.slese;

/**
 * The class holds all configuration parameters that are common for all return
 * service types. Note that the accessor and modifier functions get_<Attribute>
 * and set_<Attribute> are generated automatically on the public interface.
 */
public class EE_APISE_RSConfiguration extends EE_APISE_PConfiguration
{
    /**
     * The latency limit
     */
    public int latencyLimit;

    /**
     * The size of the transfer buffer.
     */
    public long transferBufferSize;


    /**
     * Constructor with no arguments
     */
    public EE_APISE_RSConfiguration()
    {
        this.latencyLimit = 0;
        this.transferBufferSize = 0;
    }

    @SuppressWarnings("unused")
    private EE_APISE_RSConfiguration(EE_APISE_RSConfiguration right)
    {
        this.latencyLimit = right.latencyLimit;
        this.transferBufferSize = right.transferBufferSize;
    }

    /**
     * @return
     */
    public int getLatencyLimit()
    {
        return this.latencyLimit;
    }

    /**
     * @param value
     */
    public void setLatencyLimit(int value)
    {
        this.latencyLimit = value;
    }

    /**
     * @return
     */
    public long getTransferBufferSize()
    {
        return this.transferBufferSize;
    }

    /**
     * @param value
     */
    public void setTransferBufferSize(long value)
    {
        this.transferBufferSize = value;
    }
}
