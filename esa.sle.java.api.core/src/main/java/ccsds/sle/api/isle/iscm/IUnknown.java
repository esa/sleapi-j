package ccsds.sle.api.isle.iscm;

/**
 * Base interface for all COM components
 */
public interface IUnknown
{
    /**
     * Queries an interface.
     * 
     * @param iid Id of the received object.
     * @return
     */
    <T extends IUnknown> T queryInterface(Class<T> iid);
}
