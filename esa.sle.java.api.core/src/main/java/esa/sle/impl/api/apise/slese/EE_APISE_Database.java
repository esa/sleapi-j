/**
 * @(#) EE_APISE_Database.java
 */

package esa.sle.impl.api.apise.slese;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.it.SLE_AppRole;
import esa.sle.impl.api.apiut.EE_SLE_LibraryInstance;
import esa.sle.impl.ifs.gen.EEGEN_TokenTypes;
import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The configuration database reads all static parameters during configuration
 * from a file and makes them available to the client via accessor functions.
 * The configuration parameters comprise those needed for configuration of the
 * Service Element according to reference [SLE-RM]. Configuration parameters can
 * only be read from the Database. The parameters cannot be written to the
 * database. The client first calls the open() member function, which opens the
 * DB. Then the client has to call readConfigPrms() to start parsing the
 * parameters from the Database. If an error is encountered, the client can
 * obtain the error text by calling the function errorText(). When that
 * succeeds, the object is ready to obtain the parameters via the
 * get_<parameter> functions.
 */
public class EE_APISE_Database extends EE_Database
{
    private static final Logger LOG = Logger.getLogger(EE_APISE_Database.class.getName());

    /**
     * The pointer to the database object (static).
     */
    private static Map<String, EE_APISE_Database> dbMap = new HashMap<>();

    /**
     * The token name for the application-role.
     */
    private static String roleName = "Application_Role";

    /**
     * The token name for the list that contains the port-to-protocol-mapping.
     */
    private static String portListName = "PortList";

    /**
     * The token name for the maximum trace length.
     */
    private static String traceLengthName = "Max_Trace_Length";

    /**
     * The minimum length of an identifier
     */

    @SuppressWarnings("unused")
    private static int minIdLength = 3;

    /**
     * The maximum length of an identifier
     */
    @SuppressWarnings("unused")
    private static int maxIdLength = 16;

    /**
     * The supported application role.
     */
    private SLE_AppRole role;

    /**
     * The default protocol identifier to use when the responder port identifier
     * is not specified in the port-to-protocol mapping list. This attribute is
     * used by a service element supporting user and/or provider applications.
     */
    private String defaultProtocolId;

    /**
     * The last error text generated during parsing of the database file.
     */
    private String errorText;

    /**
     * The information whether or not the role has been set during parsing of
     * the database file.
     */
    private boolean roleIsSet;

    /**
     * The maximum length in bytes of a hex data trace output
     */
    private int maxTraceLength;

    private final EE_APISE_ReportingData reportingData = new EE_APISE_ReportingData();

    private final List<EE_APISE_PeerData> peerData = new ArrayList<EE_APISE_PeerData>();


    // private boolean defaultIssueLineNumber;

    public EE_APISE_Database()
    {
        this.role = SLE_AppRole.sleAR_user;
        this.defaultProtocolId = null;
        this.errorText = null;
        this.roleIsSet = false;
        this.maxTraceLength = 0;
    }

    /**
     * Kept for backward compatibility.
     * 
     * @return db
     */
    public static synchronized EE_APISE_Database getDb()
    {
    	return getDb(EE_SLE_LibraryInstance.LIBRARY_INSTANCE_KEY);
    }

    /**
     * Kept for backward compatibility.
     * 
     */
    public static synchronized void resetDb()
    {
    	resetDb(EE_SLE_LibraryInstance.LIBRARY_INSTANCE_KEY);
    }
    
    /**
     * Creates and returns the database object. The database object is created
     * only once.
     * 
     * @return
     */
    public static synchronized EE_APISE_Database getDb(String instanceKey)
    {
    	EE_APISE_Database db = dbMap.get(instanceKey);
        if (db == null)
        {
            db = new EE_APISE_Database();
            dbMap.put(instanceKey, db);
        }
        return db;
    }

    public static synchronized void resetDb(String instanceKey)
    {
    	EE_APISE_Database db = dbMap.get(instanceKey);
        if (db != null)
        {
            db = null;
            dbMap.remove(instanceKey);
        }
    }

    /**
     * Returns the role of the local application. defaultIssueLineNumber
     * 
     * @return
     */
    public SLE_AppRole getApplicationRole()
    {
        return this.role;
    }

    /**
     * Returns the protocol Id corresponding to the supplied responder-port
     * identifier.
     * 
     * @param rspPort
     * @return
     */
    public String getProtocolId(String rspPort)
    {
        if (rspPort == null)
        {
            return null;
        }

        for (EE_APISE_PeerData pd : this.peerData)
        {
            String port = pd.getRspPortId();
            if (port != null && port.compareToIgnoreCase(rspPort) == 0)
            {
                return pd.getProtocolId();
            }
        }
        return this.defaultProtocolId;
    }

    /**
     * Returns the list of configured peer data. The client can use this list
     * e.g. to check completeness of the registered proxies.
     * 
     * @return
     */
    public List<EE_APISE_PeerData> getPeerData()
    {
        return this.peerData;
    }

    /**
     * Returns the minimum reporting cycle in seconds for the generation of
     * STATUS-REPORT operations. Note that this is only supported for a
     * responding application.
     * 
     * @return
     */
    public int getMinReportingCycle()
    {
        return this.reportingData.getMinReportingCycle();
    }

    /**
     * Returns the maximum reporting cycle in seconds for the generation of
     * STATUS-REPORT operations. Note that this is only supported for a
     * responding application.
     *
     * @return
     */
    public int getMaxReportingCycle()
    {
        return this.reportingData.getMaxReportingCycle();
    }

    /**
     * Returns the maximum trace length of hex data in bytes.
     */
    public int getMaxTraceLength()
    {
        return this.maxTraceLength;
    }

    /**
     * Returns true if the supplied protocol Id is supported.
     * 
     * @param protocolId
     * @return
     */
    public boolean isSupported(String protocolId)
    {
        if (protocolId == null)
        {
            return false;
        }

        for (EE_APISE_PeerData pd : this.peerData)
        {
            String prot = pd.getProtocolId();
            if (prot != null && prot.compareToIgnoreCase(protocolId) == 0)
            {
                return true;
            }
        }
        // protocol id is not in the list, try default protocol id:
        if (this.defaultProtocolId != null && this.defaultProtocolId.compareToIgnoreCase(protocolId) == 0)
        {
            return true;
        }
        return false;
    }

    /**
     * Starts reading all parameters from the database file. It returns E_FAIL
     * when any error is detected, SLE_E_CONFIG if a consistency error is
     * detected, S_OK otherwise.
     * 
     * @return
     */
    public HRESULT readConfigPrms()
    {
        EEGEN_TokenTypes token = EEGEN_TokenTypes.eeGEN_TTinvalidState;

        EE_Reference<String> name = new EE_Reference<>();
        name.setReference("");

        EE_Reference<String> value = new EE_Reference<>();
        value.setReference("");

        HRESULT rc = HRESULT.S_OK;

        while (token != EEGEN_TokenTypes.eeGEN_TTeof)
        {

            token = getNextTokens(name, value);

            switch (token)
            {
            case eeGEN_TTinvalidState:
            {
                setErrorText("Invalid State: database not open", false);
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("Invalid State: database not open");
                }
                return HRESULT.E_FAIL;
            }
            case eeGEN_TTpair:
            {
                rc = setValue(name.getReference(), value.getReference());
                if (rc != HRESULT.S_OK)
                {
                    return HRESULT.E_FAIL;
                }
                break;
            }
            case eeGEN_TTsolist:
            {
                rc = setList(name.getReference());
                if (rc != HRESULT.S_OK)
                {
                    return HRESULT.E_FAIL;
                }
                break;
            }
            case eeGEN_TTeolist:
            {
                setErrorText("End of list does not match begin of list");
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("End of list does not match begin of list");
                }
                return HRESULT.E_FAIL;
            }
            case eeGEN_TTsingle:
            {
                setErrorText("Unexpexted or unknown identifier: " + value.getReference());
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("Unexpexted or unknown identifier: ");
                }
                return HRESULT.E_FAIL;
            }
            case eeGEN_TTblankline:
            {
                break; // ignore
            }
            case eeGEN_TTinvalidFileFormat:
            {
                // this is actually a syntax error, take the error message of
                // the base class
                String aText = getCurrentError();
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest(aText);
                }
                if (aText.isEmpty())
                {
                    setErrorText("Syntax error");
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("Syntax error");
                    }
                }
                else
                {
                    setErrorText(aText);
                }
                return HRESULT.E_FAIL;
            }
            case eeGEN_TTeof:
            {
                // check configuration
                break;
            }
            default:
            {
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("Parsin error");
                }
                setErrorText("Parsin error");
                return HRESULT.E_FAIL;
            }
            }// end switch
        }// end while

        return checkConfiguration();
    }

    /**
     * Returns the last error-text, 0 if no error has been encountered.
     * 
     * @return
     */
    public String getErrorText()
    {
        return this.errorText;
    }

    /**
     * Sets the value of the attribute identified by the supplied name. If any
     * error is detected, or the attribute has already been set, E_FAIL is
     * returned and the error text is set.
     * 
     * @param name
     * @param value
     * @return
     */
    private HRESULT setValue(String name, String value)
    {
        if (roleName.compareToIgnoreCase(name) == 0)
        {
            if (this.roleIsSet)
            {
                setErrorText("Duplicate application role");
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("Duplicate application role");
                }
                return HRESULT.E_FAIL;
            }

            // the role has been parsed
            if (value.compareToIgnoreCase("user") == 0)
            {
                this.role = SLE_AppRole.sleAR_user;
                this.roleIsSet = true;
            }
            else if (value.compareToIgnoreCase("provider") == 0)
            {
                this.role = SLE_AppRole.sleAR_provider;
                this.roleIsSet = true;
            }
            else
            {
                setErrorText("Invalid role, must be 'user' or 'provider'");
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("Invalid role, must be 'user' or 'provider'");
                }
                return HRESULT.E_FAIL;
            }
            return HRESULT.S_OK;
        }
        else if (traceLengthName.compareToIgnoreCase(name) == 0)
        {

            int theVal = Integer.parseInt(value);
            if (theVal <= 0)
            {
                setErrorText("Invalid maximum trace length");
                return HRESULT.E_INVALIDARG;
            }

            this.maxTraceLength = theVal;
            return HRESULT.S_OK;
        }
        else
        {
            // try reporting data
            HRESULT rc = this.reportingData.setValue(name, value);

            if (rc == HRESULT.S_OK)
            {
                return rc;
            }
            if (rc == HRESULT.E_INVALIDARG)
            {
                setErrorText("Invalid minimum/maximum reporting cycle: " + value);
                return HRESULT.E_FAIL;
            }
            if (rc == HRESULT.SLE_E_DUPLICATE)
            {
                setErrorText("Duplicate minimum/maximum reporting cycle");
                return HRESULT.E_FAIL;
            }
            if (rc == HRESULT.E_FAIL) // identifier not found
            {
                setErrorText("No such identifier: " + name);
                return rc;
            }
        }
        return HRESULT.E_FAIL;
    }

    /**
     * Sets the values of the port-to-protocol mapping list of the attribute
     * identified by the supplied name. If any error is detected, or the
     * attribute has already been set, E_FAIL is returned and the error text is
     * set.
     * 
     * @param name
     * @return
     */
    private HRESULT setList(final String name)
    {
        if (portListName.compareToIgnoreCase(name) == 0)
        {
            if (!this.peerData.isEmpty() || this.defaultProtocolId != null)
            {
                setErrorText("Duplicate port-to-protocol mapping list");
                return HRESULT.E_FAIL;
            }

            EE_Reference<String> theName = new EE_Reference<>();
            theName.setReference("");

            EE_Reference<String> theValue = new EE_Reference<>();
            theValue.setReference("");
            EEGEN_TokenTypes token = EEGEN_TokenTypes.eeGEN_TTinvalidState;

            while (token != EEGEN_TokenTypes.eeGEN_TTeolist)
            {
                token = getNextTokens(theName, theValue);
                if (token == EEGEN_TokenTypes.eeGEN_TTpair)
                {
                    if (theName.getReference().compareToIgnoreCase("default") == 0)
                    {
                        if (this.defaultProtocolId != null)
                        {
                            setErrorText("Duplicate default entry");
                            return HRESULT.E_FAIL;
                        }
                        this.defaultProtocolId = theValue.getReference();
                    }
                    else
                    {
                        if (isMember(theName.getReference()))
                        {
                            String theErrorText = "Duplicate responder port identifier: ";
                            theErrorText += theName;
                            setErrorText(theErrorText);
                            return HRESULT.E_FAIL;
                        }
                        EE_APISE_PeerData pd = new EE_APISE_PeerData();
                        pd.setRspPortId(theName.getReference());
                        pd.setProtocolId(theValue.getReference());
                        this.peerData.add(pd);
                    }
                } // token == eeGEN_TTpair
                else
                {
                    if (token != EEGEN_TokenTypes.eeGEN_TTeolist && token != EEGEN_TokenTypes.eeGEN_TTblankline)
                    {
                        setErrorText("Syntax error");
                        return HRESULT.E_FAIL;
                    }
                }
            } // while
        } // if port list
        else
        {
            String theError = "Invalid list identifier: ";
            theError += name;
            setErrorText(theError);
            return HRESULT.E_FAIL;
        }

        return HRESULT.S_OK;
    }

    /**
     * Returns true if the supplied responder-port is already member of the
     * mapping list.
     * 
     * @param rspPortId
     * @return
     */
    private boolean isMember(String rspPortId)
    {
        if (rspPortId == null)
        {
            return false;
        }

        for (EE_APISE_PeerData pd : this.peerData)
        {
            String rsp = pd.getRspPortId();
            if (rsp != null && rsp.compareToIgnoreCase(rspPortId) == 0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Copies the supplied text to the error text. An old error text will be
     * deleted.
     * 
     * @param theText
     * @param issueLineNumber
     */
    private void setErrorText(String theText, boolean issueLineNumber)
    {
        if (theText != null)
        {
            StringBuilder os = new StringBuilder();
            if (issueLineNumber)
            {
                os.append("Line " + getCurrentLineNumber() + ": ");
                os.append(theText + "\n");
                this.errorText = os.toString();
            }
        }
    }

    private void setErrorText(String theText)
    {
        setErrorText(theText, true);
    }

    /**
     * Returns S_OK if the database-configuration is complete and consistent,
     * otherwise SLE_E_CONFIG is returned and the error text is set.
     * 
     * @return
     */
    private HRESULT checkConfiguration()
    {
        if (!this.roleIsSet)
        {
            setErrorText("Database: Missing application role", false);
            return HRESULT.SLE_E_CONFIG;
        }

        // default protocol is optional, therefore removed to check if it is set
        // the peer data have already been checked for duplicates

        if (this.role == SLE_AppRole.sleAR_provider)
        {
            int min = getMinReportingCycle();
            int max = getMaxReportingCycle();

            if (min == 0)
            {
                setErrorText("Database: Minimum reporting cycle missing", false);
                return HRESULT.SLE_E_CONFIG;
            }
            if (max == 0)
            {
                setErrorText("Database: Maximum reporting cycle missing", false);
                return HRESULT.SLE_E_CONFIG;
            }
            if (max <= min)
            {
                setErrorText("Database: Inconsistent reporting cycle", false);
                return HRESULT.SLE_E_CONFIG;
            }
        }
        return HRESULT.S_OK;
    }

    public boolean getRoleIsSet()
    {
        return this.roleIsSet;
    }

    public void setRoleIsSet(boolean value)
    {
        this.roleIsSet = value;
    }
}
