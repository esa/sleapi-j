package ccsds.sle.api.isle.iutl;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_GlobalRDN;
import ccsds.sle.api.isle.it.SLE_LocalRDN;

/**
 * Objects exporting this interface handle the Service Instance Identifier
 * defined by the CCSDS Recommendations. The service instance identifier is a
 * distinguished name as defined by reference [ISO 9594-2], which is constructed
 * according to the containment relationships of the managed objects in the
 * CCSDS Recommendation on SLE Service Management. The Object supports two
 * formats for the service instance identifier * The standard format as defined
 * by reference [ISO 9594-2] with the constraint that the attribute are always
 * character strings. * A standard character string representation defined in
 * appendix F of this specification. The standard format consists of a sequence
 * of "attribute value assertions", i.e. pairs of an attribute identifier and an
 * attribute value. The attribute identifier is an object identifier as defined
 * by ASN.1 (reference [ISO 8824]). The object is able to process the standard
 * ASCII representation defined in Appendix E to this specification for input
 * and output. It also accepts the standard format as defined by reference [ISO
 * 9594-2] and produces output in this format. For the global form of the object
 * identifier the object uses the full object identifier presented as an array
 * of integers. For the local form, it accepts and outputs only the trailing
 * component of the object identifier, which is unique for all attributes used
 * in a service instance identifier. For retrieval of the standard format, the
 * object supports a simple built in iterator by which the name components can
 * be read. After creation the value of the service instance identifier is NULL.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_SII extends IUnknown
{
    /**
     * Gets the initial format used.
     * 
     * @return
     */
    boolean getInitialFormatUsed();

    /**
     * Sets the initial format.
     */
    void setInitialFormat();

    /**
     * Gets the ascii form.
     * 
     * @return
     */
    String getAsciiForm();

    /**
     * Gets the Last RDN.
     * 
     * @return
     */
    String getLastRDN();

    /**
     * Sets ascii form.
     * 
     * @param siiString
     * @throws SleApiException
     */
    void setAsciiForm(String siiString) throws SleApiException;

    /**
     * Checks if the object is null.
     * 
     * @return
     */
    boolean isNull();

    /**
     * Sets the object to null.
     */
    void setToNull();

    /**
     * Copies the object.
     * 
     * @return
     */
    ISLE_SII copy();

    /**
     * Adds global RDN.
     * 
     * @param objId
     * @param value
     * @throws SleApiException
     */
    void addGlobalRDN(int[] objId, String value) throws SleApiException;

    /**
     * Adds local RDN.
     * 
     * @param objId
     * @param value
     * @throws SleApiException
     */
    void addLocalRDN(int objId, String value) throws SleApiException;

    /**
     * Adds global RDN.
     * 
     * @param globalRDN
     * @throws SleApiException
     */
    void addGlobalRDN(SLE_GlobalRDN globalRDN) throws SleApiException;

    /**
     * Adds local RDN.
     * 
     * @param localRDN
     * @throws SleApiException
     */
    void addLocalRDN(SLE_LocalRDN localRDN) throws SleApiException;

    /**
     * Resets.
     */
    void reset();

    /**
     * Mode data.
     * 
     * @return
     */
    boolean moreData();

    /**
     * Gets next global RDN.
     * 
     * @return
     * @throws SleApiException
     */
    SLE_GlobalRDN nextGlobalRDN() throws SleApiException;

    /**
     * Gets next local RDN.
     * 
     * @return
     * @throws SleApiException
     */
    SLE_LocalRDN nextLocalRDN() throws SleApiException;

    /**
     * @param o
     * @return
     */
    @Override
    boolean equals(Object o);

    /**
     * @return
     */
    @Override
    int hashCode();
}
