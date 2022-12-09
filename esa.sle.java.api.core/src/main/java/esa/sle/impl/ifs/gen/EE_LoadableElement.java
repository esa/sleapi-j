/**
 * @(#) EE_LoadableElement.java
 */

package esa.sle.impl.ifs.gen;

/**
 * Designed as a base class for Aggregated items of a databse.
 */
public class EE_LoadableElement
{

    /**
     * a name/value pair to a derived class instance for processing.
     */
    public boolean acceptValue(String name, String value, EE_Database db)
    {
        return true;
    }

    /**
     * A derived class should return whether all required parameters have been
     * set.
     */
    public boolean isFullyLoaded(String diagnostic)
    {
        return true;
    }

}
