/**
 * @(#) EE_SLE_SII.java
 */

package esa.sle.impl.api.apiut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_GlobalRDN;
import ccsds.sle.api.isle.it.SLE_LocalRDN;
import ccsds.sle.api.isle.iutl.ISLE_SII;

/**
 * The class provides an implementation of the interface ISLE_SII as specified
 * in reference [SLE-API] for the component class 'SLE SII'. The object is
 * created by using the deafult constructor (generated automatically, not shown
 * on the class diagram). The Copy() function is implemented by using the
 * private copy-constructor (generated automatically, not shown on the diagram).
 */
public class EE_SLE_SII implements ISLE_SII
{
    static private Logger LOG = Logger.getLogger(EE_SLE_SII.class.getName());

    /**
     * The array defining the mapping between OID and naming attribute
     * abbreviation. The index is meant to be the OID.
     */
    private static String[] nameAttribV1 = { "***", "ds", "evh", "aos-fsl-fg", "aos-vcdi-fg", "cltugen-fg", // 0....5
                                            "cltu-st", "cltu", "cltu-u", "fsl", "fsp", // 6...10
                                            "tcf-st", "tcf", "tcf-u", "fsl-fg", "tcvca-st", // 11...15
                                            "tcvca", "ftcvc-prod", "tc-vcdi-fg", "vcseg-prod", "nl", // 16...20
                                            "raf-st", "raf", "raf-u", "fde-fg", "fp-fg", // 21...25
                                            "mcf-prod", "mcfsh-st", "mcfsh", "mcf-st", "mcf", // 26...30
                                            "mcocf-prod", "mcocf-st", "mcocf", "mcocf-u", "mc-prod", // 31...35
                                            "mcshf-prod", "rsl", "rsl-fg", "rsp-st", "rsp", // 36...40
                                            "vcf-prod", "vcfsh-prod", "vcfsh-st", "vcfsh", "vcf-st", // 41...45
                                            "vcf", "vcocf-prod", "vcocf-st", "vcocf", "vcocf-u", // 46...50
                                            "vc-prod", "sagr", "spack", "sctree", "scchar", // 51...55
                                            "sctrack" };

    /**
     * The array defining the mapping between OID and naming attribute
     * abbreviation Version 2. The index is meant to be the OID.
     */
    private static String[] nameAttribV2 = { "***", "ds", "evh", "aos-fsl-fg", "aos-vcdi-fg", "cltugen-fg", // 0....5
                                            "cltu-st", "cltu", "cltu-u", "fsl", "fsp", // 6...10
                                            "tcf-st", "tcf", "tcf-u", "fsl-fg", "tcvca-st", // 11...15
                                            "tcvca", "ftcvc-prod", "tc-vcdi-fg", "vcseg-prod", "nl", // 16...20
                                            "raf-st", "raf", "raf-u", "fde-fg", "fp-fg", // 21...25
                                            "mcf-prod", "mcfsh-st", "mcfsh", "mcf-st", "mcf", // 26...30
                                            "mcocf-prod", "mcocf-st", "mcocf", "mcocf-u", "mc-prod", // 31...35
                                            "mcshf-prod", "rsl", "rsl-fg", "rsp-st", "rsp", // 36...40
                                            "vcf-prod", "vcfsh-prod", "vcfsh-st", "rcfsh", "vcf-st", // 41...45
                                            "rcf", "vcocf-prod", "vcocf-st", "rocf", "vcocf-u", // 46...50
                                            "vc-prod", "sagr", "spack", "sctree", "scchar", // 51...55
                                            "sctrack" };

    /**
     * The array defining the mapping between OID and naming attribute
     * abbreviation. The index is meant to be the OID.
     */
    private String[] nameAttrib;

    /**
     * The array size of _nameAttr (see Appendix F.2 of [SLE-API])
     */
    private static long attrArraySize = 56;

    /**
     * The constant part (as far as SLES is concerned) of the global OID V1.
     */
    private static int[] globalOidV1 = { 1, 2, 0, 9, 5, 2 };

    /**
     * The constant part (as far as SLES is concerned) of the global OID Version
     * 2.
     */
    private static int[] globalOidV2 = { 1, 3, 112, 4, 3, 1, 2 };

    /**
     * The constant part (as far as SLES is concerned) of the global OID array.
     */
    private int[] globalOid;

    /**
     * The size of the global OID array.
     */
    private int globalOidSize;

    /**
     * The characters to be returned if the identifier is NULL.
     */
    @SuppressWarnings("unused")
    private static String nullId = "***";

    /**
     * The attribute separator.
     */
    private static char attrSep = '.';

    /**
     * The name-value separator.
     */
    private static char valSep = '=';

    /**
     * The list holding the pairs of OID and attribute-value string.
     */
    private LinkedHashMap<Integer, String> rdnList;

    /**
     * The list iterator
     */
    private Iterator<Entry<Integer, String>> iter;

    /**
     * The information whether or not the initial SIID format is used.
     */
    private boolean initialFormatUsed;

    /**
     * The sagr attribute Id.
     */
    private static int sagrId = 52;

    /**
     * The spack attribute Id.
     */
    private static int spackId = 53;

    /**
     * The fsl-fg attribute Id.
     */
    private static int fslFgId = 14;

    /**
     * The rsl-fg attribute Id.
     */
    private static int rslFgId = 38;

    /**
     * The cltu attribute Id.
     */
    private static int cltuId = 7;

    /**
     * The fsp attribute Id.
     */
    private static int fspId = 10;

    /**
     * The raf attribute Id.
     */
    private static int rafId = 22;

    /**
     * The rcf attribute Id.
     */
    private static int rcfId = 46;

    /**
     * The rcfsh attribute Id.
     */
    private static int rcfshId = 44;

    /**
     * The rocf attribute Id.
     */
    private static int rocfId = 49;

    /**
     * The rsp attribute Id.
     */
    private static int rspId = 40;

    /**
     * The tcf attribute Id.
     */
    private static int tcfId = 12;

    /**
     * The tcva attribute Id.
     */
    private static int tcvaId = 16;

    /**
     * The number of RDNs the V2 SIID consists of.
     */
    private static int expectedNumRdn = 4;


    /**
     * Constructor
     */
    public EE_SLE_SII()
    {
        this.nameAttrib = nameAttribV2;
        this.globalOid = globalOidV2;
        this.globalOidSize = 7;
        this.initialFormatUsed = false;
        this.rdnList = new LinkedHashMap<Integer, String>();
        this.iter = this.rdnList.entrySet().iterator();
    }

    /**
     * Copy constructor
     */
    public EE_SLE_SII(EE_SLE_SII previous)
    {
        this.initialFormatUsed = previous.getInitialFormatUsed();
        this.globalOid = previous.getGlobalOid();
        this.nameAttrib = previous.nameAttrib;
        this.globalOidSize = previous.getGlobalOidSize();
        this.rdnList = new LinkedHashMap<Integer, String>(previous.getRdnList());
        this.iter = this.rdnList.entrySet().iterator();

    }

    public LinkedHashMap<Integer, String> getRdnList()
    {
        return this.rdnList;
    }

    public int getGlobalOidSize()
    {
        return this.globalOidSize;
    }

    public String[] getNameAttrib()
    {
        return this.nameAttrib;
    }

    public int[] getGlobalOid()
    {
        return this.globalOid;
    }

    /**
     * @param iid
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_SII.class)
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
    public boolean getInitialFormatUsed()
    {
        return this.initialFormatUsed;
    }

    /**
	 * 
	 */
    @Override
    public void setInitialFormat()
    {
        this.initialFormatUsed = true;
        this.nameAttrib = nameAttribV1;
        this.globalOid = globalOidV1;
        this.globalOidSize = 6;
    }

    /**
     * @param siiString
     * @throws SleApiException
     */
    @Override
    public void setAsciiForm(String siiString) throws SleApiException
    {
        if (siiString.isEmpty())
        {
            throw new SleApiException(HRESULT.E_INVALIDARG, "Invalid string passed as input argument");
        }

        // Remove spaces
        siiString = siiString.replaceAll("\\s+", "");

        if (siiString.isEmpty())
        {
            throw new SleApiException(HRESULT.E_INVALIDARG, "Invalid string passed as input argument");
        }

        int numMembers = checkSyntax(siiString, false);

        int numSeparators = numMembers - 1;

        // separate the name-attribute pairs
        List<String> nameList = new ArrayList<String>();
        for (int j = 0; j <= numSeparators; j++)
        {
            int p = siiString.indexOf(attrSep);

            String nameStr = "";

            if (p == -1)
            {
                nameStr = siiString;
            }
            else
            {
                nameStr = siiString.substring(0, p);
            }

            nameList.add(nameStr);

            if (p != -1)
            {
                siiString = siiString.substring(p + 1, siiString.length());
            }
            else
            {
                siiString = "";
            }
        }

        // generate name-value pairs
        LinkedHashMap<Integer, String> tempList = new LinkedHashMap<Integer, String>();
        for (String member : nameList)
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest(member);
            }
            int p = member.indexOf(valSep);
            if (p == -1)
            {
                throw new SleApiException(HRESULT.E_INVALIDARG);
            }

            String nameAttr = member.substring(0, p);
            String valAttr = member.substring(p + 1);

            // check and get the name OID
            int id = getOid(nameAttr);
            if (id == -1)
            {
                throw new SleApiException(HRESULT.SLE_E_INVALIDID);
            }

            // make name-value pair
            tempList.put(id, valAttr);
        }

        this.rdnList = tempList;

        if (!this.initialFormatUsed)
        {
            HRESULT rc = checkSiidV2();
            if (rc != HRESULT.S_OK)
            {
                setToNull();
                throw new SleApiException(rc);
            }
        }
    }

    /**
     * @return
     */
    @Override
    public boolean isNull()
    {
        return (this.rdnList.isEmpty()) ? true : false;
    }

    /**
     * @param objId
     * @param value
     * @throws SleApiException
     */
    @Override
    public void addGlobalRDN(int[] objId, String value) throws SleApiException
    {
        if ((objId.length != (this.globalOidSize + 1)) || (checkOidList(objId) != HRESULT.S_OK))
        {
            throw new SleApiException(HRESULT.SLE_E_INVALIDID);
        }

        addLocalRDN(objId[this.globalOidSize], value);
    }

    /**
     * @param objId
     * @param value
     * @throws SleApiException
     */
    @Override
    public void addLocalRDN(int objId, String value) throws SleApiException
    {
        if (objId < 1 || objId > attrArraySize)
        {
            throw new SleApiException(HRESULT.SLE_E_BADVALUE);
        }

        if (value.isEmpty())
        {
            throw new SleApiException(HRESULT.SLE_E_BADVALUE);
        }

        // Remove spaces
        value = value.replaceAll("\\s+", "");

        if (value.isEmpty())
        {
            throw new SleApiException(HRESULT.SLE_E_BADVALUE);
        }

        int numElems = 0;
        numElems = checkSyntax(value, true);

        if (numElems != 1)
        {
            throw new SleApiException(HRESULT.SLE_E_BADVALUE);
        }

        // put the name-value pair
        this.rdnList.put(objId, value);
        if (!this.initialFormatUsed)
        { // make ckecks for version 2 and 3
            HRESULT rc = checkSiidV2();
            int size = this.rdnList.size();
            // missing arg is OK, as the whole SIID might not
            // yet be ready.
            if (((size >= expectedNumRdn || rc != HRESULT.SLE_E_MISSINGARG) && rc != HRESULT.S_OK))
            {

                throw new SleApiException(rc);
            }
        }
    }

    /**
	 * 
	 */
    @Override
    public void addGlobalRDN(SLE_GlobalRDN globalRDN) throws SleApiException
    {
        int[] objId = globalRDN.getOid();
        String value = globalRDN.getValue();

        addGlobalRDN(objId, value);
    }

    /**
	 * 
	 */
    @Override
    public void addLocalRDN(SLE_LocalRDN localRDN) throws SleApiException
    {
        int objId = localRDN.getOid();
        String value = localRDN.getValue();

        addLocalRDN(objId, value);
    }

    /**
	 * 
	 */
    @Override
    public boolean moreData()
    {
        return this.iter.hasNext();
    }

    @Override
    public SLE_GlobalRDN nextGlobalRDN() throws SleApiException
    {
        SLE_LocalRDN localRDN = nextLocalRDN();

        int[] oid_p = new int[this.globalOidSize + 1];
        // use global oids depending on Set_InitialFormat (V1 or V2)
        oid_p = Arrays.copyOf(this.globalOid, this.globalOidSize + 1); // set
                                                                       // global
                                                                       // oids
        oid_p[this.globalOidSize] = localRDN.getOid(); // set local oid

        return new SLE_GlobalRDN(localRDN.getValue(), oid_p);
    }

    /**
	 * 
	 */
    @Override
    public SLE_LocalRDN nextLocalRDN() throws SleApiException
    {

        if (this.rdnList.isEmpty())
        {
            throw new SleApiException(HRESULT.SLE_S_NULL);
        }
        else if (!this.iter.hasNext())
        {
            throw new SleApiException(HRESULT.SLE_S_EOD);
        }

        // this.iter = this.rdnList.entrySet().iterator();
        Entry<Integer, String> entry = this.iter.next();
        SLE_LocalRDN np = new SLE_LocalRDN(entry.getValue(), entry.getKey());

        return np;
    }

    /**
	 * 
	 */
    @Override
    public String getAsciiForm()
    {
        String asciiForm = "";

        if (!this.rdnList.isEmpty())
        {
            Iterator<Entry<Integer, String>> iter = this.rdnList.entrySet().iterator();
            boolean isFirst = true;
            while (iter.hasNext())
            {
                if (!isFirst)
                {
                    asciiForm += attrSep;
                }

                isFirst = false;
                Entry<Integer, String> namePair = iter.next();
                int id = namePair.getKey();
                String value = namePair.getValue();
                asciiForm += this.nameAttrib[id] + valSep;
                asciiForm += value;
            }
        }
        return asciiForm;
    }

    /**
	 * 
	 */
    @Override
    public String getLastRDN()
    {
        String asciiForm = "";

        if (!this.rdnList.isEmpty())
        {
            List<Entry<Integer, String>> entryList = new ArrayList<Map.Entry<Integer, String>>(this.rdnList.entrySet());
            Entry<Integer, String> lastEntry = entryList.get(entryList.size() - 1);
            asciiForm += this.nameAttrib[lastEntry.getKey()] + valSep;
            asciiForm += lastEntry.getValue();
        }
        return asciiForm;
    }

    /**
	 * 
	 */
    @Override
    public void setToNull()
    {
        this.rdnList.clear();
    }

    /**
     * @return
     */
    @Override
    public ISLE_SII copy()
    {
        EE_SLE_SII aux = this;
        EE_SLE_SII newsii = new EE_SLE_SII(aux);
        ISLE_SII isii = newsii.queryInterface(ISLE_SII.class);

        if (isii != null)
        {
            return isii;
        }
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Are they equal " + equals(isii));
        }

        return null;
    }

    @Override
    public void reset()
    {
        this.iter = this.rdnList.entrySet().iterator();

    }

    /**
     * Returns the OID of the supplied name. If the name does not correspond to
     * an OID, -1 is returned.
     * 
     * @param name
     * @return
     */
    private int getOid(String name)
    {
        for (int i = 0; i < attrArraySize; i++)
        {
            if (name.equals(this.nameAttrib[i]))
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * ////////////////////////////////////////////////// Checks the syntax of
     * the supplied attribute-value pairs. This member function can also be used
     * for the syntax check of an attribute value. The number of attribute-value
     * pairs is returned by the <members> argument (if it is needed by the
     * client). If there is only an attribute value to be checked, <valueOnly>
     * must be set to true. //////////////////////////////////////////////////
     */
    private int checkSyntax(String siiVal, boolean valueOnly) throws SleApiException
    {
        int numSeparators = 0;
        int numValueSepar = 0;
        int members = 0;

        // Check syntax
        if (!siiVal.matches("\\A\\p{ASCII}*\\z"))
        {
            throw new SleApiException(HRESULT.E_INVALIDARG, "The input string contains not ASCII characters");
        }

        for (int i = 0; i < siiVal.length(); i++)
        {
            if (Character.isWhitespace(siiVal.charAt(i)))
            {
                throw new SleApiException(HRESULT.E_INVALIDARG,
                                          "The input string contains white spaces or not ASCII characters");
            }

            if (siiVal.charAt(i) == attrSep)
            {
                numSeparators++;
            }
            else if (siiVal.charAt(i) == valSep)
            {
                numValueSepar++;
            }
        }

        members = numSeparators + 1;

        if (valueOnly)
        {
            // the passed string is an attribute value
            if (!(members == 1 && numValueSepar == 0))
            {
                throw new SleApiException(HRESULT.E_INVALIDARG);
            }
            return members;
        }

        if (numValueSepar != members)
        {
            throw new SleApiException(HRESULT.E_INVALIDARG);
        }

        return members;
    }

    /**
     * @param objId
     * @return
     */
    private HRESULT checkOidList(int[] objId)
    {
        System.arraycopy(this.globalOid, 0, objId, 0, this.globalOidSize);

        int lastId = objId[this.globalOidSize];

        if ((lastId < 1) || (lastId > attrArraySize))
        {
            return HRESULT.E_FAIL;
        }

        return HRESULT.S_OK;
    }

    /**
     * @param sii
     * @return
     */
    @SuppressWarnings("unused")
    private boolean isEqualTo(ISLE_SII sii)
    {
        String in_s = sii.getAsciiForm();
        String myself_s = getAsciiForm();

        if (in_s.equals(myself_s))
        {
            return true;
        }
        return false;
    }

    /**
     * @throws SleApiException
     */
    private HRESULT checkSiidV2()
    {
        if (this.rdnList.isEmpty())
        {
            return HRESULT.SLE_E_MISSINGARG;
        }

        if (this.rdnList.size() > expectedNumRdn)
        {
            return HRESULT.E_INVALIDARG;
        }

        // now check the correct sequence and contents
        // of the RDNs, example:
        // sagr=abc.spack=def.rsl-fg=rslfg.raf=onlt1

        int memberNum = 0;
        int fg = 0; // functional group

        Iterator<Entry<Integer, String>> iter = this.rdnList.entrySet().iterator();
        while (iter.hasNext())
        {
            Entry<Integer, String> namePair = iter.next();
            int id = namePair.getKey();
            String value = namePair.getValue();

            switch (memberNum)
            {
            case 0:
            {
                if (id != sagrId)
                {
                    return HRESULT.SLE_E_SEQUENCE;
                }
                break;
            }
            case 1:
            {
                if (id != spackId)
                {
                    return HRESULT.SLE_E_SEQUENCE;
                }
                break;
            }
            case 2:
            {
                if ((id != rslFgId) && (id != fslFgId))
                {
                    return HRESULT.SLE_E_SEQUENCE;
                }
                else
                {
                    fg = id;
                }
                break;
            }
            case 3:
            {
                // the last element must be value-checked differently for
                // forward or return services
                if (fg == rslFgId)
                {
                    if (id == rafId || id == rcfId || id == rcfshId || id == rocfId || id == rspId)
                    {
                        return checkServiceValueV2(id, value);
                    }
                    else
                    {
                        return HRESULT.SLE_E_SEQUENCE;
                    }
                }
                else if (fg == fslFgId)
                {
                    if (id == cltuId || id == fspId || id == tcfId || id == tcvaId)
                    {
                        return checkServiceValueV2(id, value);
                    }
                    else
                    {
                        return HRESULT.SLE_E_SEQUENCE;
                    }
                }
                break;
            }
            default:
            {
                return HRESULT.SLE_E_SEQUENCE;
            }
            }

            memberNum++;
        }

        if (memberNum != expectedNumRdn)
        {
            return HRESULT.SLE_E_MISSINGARG;
        }

        return HRESULT.S_OK;
    }

    /**
     * Checks the value of the Service Name Identifier according to the new
     * version.
     * 
     * @param id
     * @param value
     * @return
     */
    private HRESULT checkServiceValueV2(int id, String value)
    {
        String numberStr = "***";
        String onlc = "onlc";
        String onlt = "onlt";
        String offl = "offl";

        if (id == rafId || id == rcfId || id == rcfshId || id == rocfId || id == rspId)
        {
            // for rtn services the value must be delivery mode
            // followed by a number
            String dlvMode = "";
            if (value.contains(onlc))
            {
                dlvMode = onlc;
            }
            else if (value.contains(onlt))
            {
                dlvMode = onlt;
            }
            else if (value.contains(offl))
            {
                dlvMode = offl;
            }
            else
            {
                return HRESULT.E_INVALIDARG;
            }

            numberStr = value;
            String numbSubStr = numberStr.substring(0, dlvMode.length());
            numberStr = numberStr.replace(numbSubStr, "");
            if (numberStr.length() <= 0)
            {
                return HRESULT.E_INVALIDARG;
            }
        }
        else if (id == cltuId || id == fspId || id == tcfId || id == tcvaId)
        {
            // forward services
            // for fwd services the value must be service type
            // followed by a number

            String nameAttr = this.nameAttrib[id];
            if (value.contains(nameAttr))
            {
                numberStr = value;
                String numbSubStr = numberStr.substring(0, nameAttr.length());
                numberStr = numberStr.replace(numbSubStr, "");
                if (numberStr.length() <= 0)
                {
                    return HRESULT.E_INVALIDARG;
                }
            }
            else
            {
                return HRESULT.E_INVALIDARG;
            }
        }
        else
        {
            return HRESULT.E_INVALIDARG;
        }

        // the rest must be a number
        for (int i = 0; i < numberStr.length(); i++)
        {
            if (!Character.isDigit(numberStr.charAt(i)))
            {
                return HRESULT.E_INVALIDARG;
            }
        }

        return HRESULT.S_OK;
    }

    @Override
    public int hashCode()
    {
        return getAsciiForm().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        boolean result = false;
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
        EE_SLE_SII other = (EE_SLE_SII) obj;
        String a = other.getAsciiForm();
        String b = getAsciiForm();
        result = a.equals(b);
        return result;
    }

    @Override
    public String toString()
    {
        return "EE_SLE_SII [nameAttrib=" + Arrays.toString(this.nameAttrib) + ", globalOid="
               + Arrays.toString(this.globalOid) + ", globalOidSize=" + this.globalOidSize + ", rdnList="
               + this.rdnList + ", initialFormatUsed=" + this.initialFormatUsed + "]";
    }

}
