/**
 * @(#) EE_APIPX_LoadableElement.java
 */

package esa.sle.impl.api.apipx.pxdb;

import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_LoadableElement;
import esa.sle.impl.ifs.gen.EE_LogMsg;
import esa.sle.impl.ifs.gen.EE_MessageRepository;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The proxy database requests all data items (instances of objects inherited
 * from EE_APIPX_LoadableElement) it directly aggegates to register the keywords
 * they use that are not contained in nested lists. Each parsed name/value pair
 * is passed to the responsible class instance. List items are dealt with in the
 * same way.
 */
public class EE_APIPX_LoadableElement extends EE_LoadableElement
{

    @SuppressWarnings("unused")
    private EE_APIPX_LoadableElement(final EE_APIPX_LoadableElement right)
    {}

    public EE_APIPX_LoadableElement()
    {}

    /**
     * This is called to pass a name/value pair to a class derived from
     * EE_APIPX_LoadableElement for processing. The derived class converts the
     * value from string to the required internal format/datatype and performs
     * validity checks and stores the value.
     */
    @Override
    public boolean acceptValue(final String name, final String value, EE_Database db)
    {
        String tmp = EE_MessageRepository.getMessage(EE_LogMsg.PXDBUNKNOWNKEYWORD_1.getCode(), name);
        db.setCurrentError(tmp);
        return false;
    }

    /**
     * Is called to pass a list item to a derived class instance for processing.
     */
    public EE_APIPX_LoadableElement acceptListItem(final String name, EE_Database db)
    {
        String tmp = EE_MessageRepository.getMessage(EE_LogMsg.PXDBLISTELEMENTFOUND_1.getCode(), name);
        db.setCurrentError(tmp);
        return null;
    }

    /**
     * A derived class should return whether all required parameters have been
     * set.
     */
    @Override
    public boolean isFullyLoaded(String diagnostic)
    {
        return false;
    }

    /**
     * It is the responsibility of any derived class that accepts a list item
     * (and hence a list) to implement this function for the name(s) of the
     * list(s) that it accepts. This function is used to ensure that errors are
     * noticed at the earliest possible opportunity. This also prevents empty
     * lists that are unknown being recognised.
     */
    public boolean listIsKnown(String lsitName)
    {
        return false;
    }

    /**
     * Takes in the name of an attribute (keyword) that has not been parsed and
     * returns an appropriate error message - this is an internal utility
     * function.
     */
    public String getNotLoadedMsg(final String attribute)
    {
        String tmp = EE_MessageRepository.getMessage(EE_LogMsg.PXDBVALUENOTSET_1.getCode(), attribute);
        return tmp;
    }

    /**
     * Allows a derived class an opportunity to register keywords used at the
     * outermost level.
     */
    public void registerKeywords(EE_APIPX_Database argdb)
    {

    }

    /**
     * Takes in the name of an attribute (keyword) that has already been parsed
     * and returns an appropriate error message - this is an internal utility
     * function.
     */
    public String getAlreadyLoadedMsg(final String attribute)
    {
        String tmp = EE_MessageRepository.getMessage(EE_LogMsg.PXDBVALUESET_1.getCode(), attribute);
        return tmp;
    }

    /**
     * Takes in the name of an attribute (keyword) and the duplicated value of
     * the attribute and returns an appropriate error message - this is an
     * internal utility function.
     */
    public String getDuplicateMsg(String attribute, String value)
    {
        String tmp = EE_MessageRepository.getMessage(EE_LogMsg.PXDBDUPLICATE_2.getCode(), value, attribute);
        return tmp;
    }

    /**
     * A derived class should return whether all required parameters have been
     * set.
     */
    public boolean isFullyLoaded(EE_Reference<String> reference, EE_APIPX_Database db)
    {
        return false;
    }

}
