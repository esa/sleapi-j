/**
 * @(#) EE_APISE_CLTU_Limits.java
 */

package esa.sle.impl.api.apise.cltuse;

/**
 * The class holds all CLTU service specific limit values of configuration
 * parameters as specified in [CCSDS 912.1].
 */
public class EE_APISE_CLTU_Limits
{
    /**
     * The minimum provider CLTU buffer size in maximum sixed CLTUs.
     */
    private final static int minBufferSize = 1024;


    public static final long getMinBufferSize()
    {
        return minBufferSize;
    }
}
