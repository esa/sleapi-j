/**
 * @(#) EE_APIPX_Database.java
 */

package esa.sle.impl.api.apipx.pxdb;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.it.SLE_AppRole;
import esa.sle.impl.ifs.gen.EEGEN_TokenTypes;
import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class EE_APIPX_Database holds all configuration parameters needed to
 * operate the proxy component. It provides a set of accessor functions to
 * enable the client to read all information needed from the database. Writing
 * information to the database is not possible for the client. This is subject
 * to a maintenance task. The physical database is a text file, which is
 * prepared manually. The format of the file is specified in detail in reference
 * [API-RM]. The approach is to provide different files for the responder and
 * initiator application. The communication server process gets the name of the
 * configuration file at start-up time and it ignores parameters, which are not
 * used. During configuration of the proxy (Configure() call to the proxy
 * administrative interface), the database must be opened, which starts parsing
 * the text-file and verifies the correctness of the configuration parameters.
 */

public class EE_APIPX_Database extends EE_Database
{

    private static final Logger LOG = Logger.getLogger(EE_APIPX_Database.class.getName());

    private EE_APIPX_LocalApplData eeLocalApplData;

    private EE_APIPX_ProxySettings eeProxySettings = new EE_APIPX_ProxySettings();

    private EE_APIPX_SrvTypeList eeSrvTypeList;

    private EE_APIPX_PeerApplDataList eePeerApplDataList;

    private EE_APIPX_ResponderPortList eeResponderPortList;

    private EE_APIPX_IPCConfig eeIPCConfig;

    private EE_APIPX_TMLData eeTMLData;

    /**
     * Contains all the registered keywords and the pointers to the objects that
     * have registered them.
     */
    private final TreeMap<String, EE_APIPX_LoadableElement> keywords = new TreeMap<String, EE_APIPX_LoadableElement>();

    /**
     * Used internally in the readValues method, to check for duplicate words..
     */
    private final TreeMap<String, EE_APIPX_LoadableElement> keywordsFound = new TreeMap<String, EE_APIPX_LoadableElement>();


    /**
     * Returns a pointer to the Proxy Settings data object.
     */
    public EE_APIPX_ProxySettings getProxySettings()
    {
        return this.eeProxySettings;
    }

    /**
     * Returns a pointer to the Responder Ports List data object.
     */
    public EE_APIPX_ResponderPortList getResponderPortList()
    {
        return this.eeResponderPortList;
    }

    /**
     * Returns a pointer to the Peer Application data list.
     */
    public EE_APIPX_PeerApplDataList getPeerApplDataList()
    {
        return this.eePeerApplDataList;
    }

    /**
     * Returns a pointer to the local application data object.
     */
    public EE_APIPX_LocalApplData getLocalApplicationData()
    {
        return this.eeLocalApplData;
    }

    /**
     * Returns the Server Type List
     */
    public EE_APIPX_SrvTypeList getSrvTypeList()
    {
        return this.eeSrvTypeList;
    }

    /**
     * Returns a pointer to the IPC config data.
     */
    public EE_APIPX_IPCConfig getIPCConfigData()
    {
        return this.eeIPCConfig;
    }

    /**
     * Returns a pointer to the TML data object.
     */
    public EE_APIPX_TMLData getTMLData()
    {
        return this.eeTMLData;
    }

    /**
     * Reads in all the values of the database into the internal data
     * structures. This must be preceeded by a call to open() of the base class
     * EE_Database. S_OK The data has been loaded successfully E_FAIL The data
     * was not loaded, a low level error occurred. SLE_E_STATE The database was
     * not initialised.
     * 
     * @throws IOException
     */
    public HRESULT readValues(EE_Reference<String> diagnostic, EE_Reference<Integer> lineNo)
    {

        this.keywords.clear();
        this.keywordsFound.clear();
        registerElements();

        EE_Reference<String> name = new EE_Reference<String>();
        name.setReference("");

        EE_Reference<String> value = new EE_Reference<String>();
        value.setReference("");

        EEGEN_TokenTypes getVal = getNextTokens(name, value);
        setCurrentError("no error");

        boolean errFound = false;
        while ((getVal != EEGEN_TokenTypes.eeGEN_TTinvalidFileFormat && getVal != EEGEN_TokenTypes.eeGEN_TTinvalidState && getVal != EEGEN_TokenTypes.eeGEN_TTeof))
        {	
            if (getVal == EEGEN_TokenTypes.eeGEN_TTsolist)
            {

                if (!this.keywords.containsKey(name.getReference()))
                {
                    errFound = true;
                    setCurrentError("unknown keyword " + name.getReference());
                    break;
                }
                else if (this.keywordsFound.get(name.getReference()) != null)
                {
                    setCurrentError("outer duplicate encountered. " + name);
                    errFound = true;
                    break;
                }
                else
                {
                    if (!parseList(this.keywords.get(name.getReference()), name))
                    {

                        errFound = true;
                        break;
                    }
                    this.keywordsFound.put(name.getReference(), new EE_APIPX_LoadableElement());
                }
            }
            else if (getVal == EEGEN_TokenTypes.eeGEN_TTeolist)
            {
                errFound = true;
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("unexpectedly encountered an end of list token.");
                }
                setCurrentError("unexpectedly encountered an end of list token.");
                break;
            }
            else if (getVal == EEGEN_TokenTypes.eeGEN_TTpair)
            {
                if (!this.keywords.containsKey(name.getReference()))
                {
                    errFound = true;
                    setCurrentError("unknown keyword encountered " + name);
                    break;
                }
                else if (this.keywordsFound.get(name.getReference()) != null)
                {
                    setCurrentError("outer duplicate encountered. " + name);
                    errFound = true;
                    break;
                }
                else
                {
                    if (!this.keywords.get(name.getReference()).acceptValue(name.getReference(),
                                                                            value.getReference(),
                                                                            this))
                    {
                        errFound = true;
                        break;
                    }
                    this.keywordsFound.put(name.getReference(), new EE_APIPX_LoadableElement());
                }
            }
            else if (getVal == EEGEN_TokenTypes.eeGEN_TTsingle)
            {
                errFound = true;
                setCurrentError("value not given for " + name.getReference());
                break;
            }
            getVal = getNextTokens(name, value);

        }

        EE_Reference<String> diag = new EE_Reference<String>();
        diag.setReference("");
        String oss = "";
        if (getVal == EEGEN_TokenTypes.eeGEN_TTinvalidState)
        {
            oss += "Database is not yet opened.";
            diagnostic.setReference(oss);
            lineNo.setReference(getCurrentLineNumber());
            return HRESULT.SLE_E_STATE;
        }
        else if (getVal == EEGEN_TokenTypes.eeGEN_TTinvalidFileFormat)
        {
            oss += "Database was incorrectly formatted. Please refer to the documentation." + getCurrentError();
            diagnostic.setReference(oss);
            lineNo.setReference(getCurrentLineNumber());
            return HRESULT.E_FAIL;
        }

        else if (!errFound)
        {
            Iterator<Entry<String, EE_APIPX_LoadableElement>> itrs = this.keywords.entrySet().iterator();
            while (itrs.hasNext())
            {
                Entry<String, EE_APIPX_LoadableElement> entry = itrs.next();
                // here we can not just leave the SERVER_TYPES
                if (entry.getKey().equals("SERVER_TYPES") || entry.getKey().equals("FOREIGN_LOGICAL_PORTS")
                    || entry.getKey().equals("LOCAL_LOGICAL_PORTS") || entry.getKey().equals("REMOTE_PEERS")
                    || (this.eeProxySettings.getRole() == SLE_AppRole.sleAR_provider))
                {

                    if (!entry.getValue().isFullyLoaded(diag, this))
                    {
                        oss += "Database information was not complete ";
                        oss += diag.getReference();
                        diagnostic.setReference(oss);
                        lineNo.setReference(getCurrentLineNumber());
                        return HRESULT.E_FAIL;
                    }

                }
            }
            if (!performCrossValidation(diag))
            {
                oss += diag.getReference();
                diagnostic.setReference(oss);
                lineNo.setReference(getCurrentLineNumber());
                return HRESULT.E_FAIL;
            }
        }
        else
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("errFound " + errFound);
            }
            oss += getCurrentError();
            diagnostic.setReference(oss);
            lineNo.setReference(getCurrentLineNumber());
            return HRESULT.E_FAIL;
        }
        lineNo.setReference(getCurrentLineNumber());

        return HRESULT.S_OK;

    }

    /**
     * Reads in a list, and returns a false if it fails.
     */
    private boolean parseList(EE_APIPX_LoadableElement elem, EE_Reference<String> listname)
    {
        Map<String, String> elems2 = new TreeMap<String, String>();
        EE_Reference<String> name = new EE_Reference<String>();
        name.setReference("");

        EE_Reference<String> value = new EE_Reference<String>();
        value.setReference("");
        boolean isItemList = false;

        EEGEN_TokenTypes getVal = getNextTokens(name, value);

        while (getVal == EEGEN_TokenTypes.eeGEN_TTblankline)
        {
            getVal = getNextTokens(name, value);
        }

        int itemPos = 0;
        EE_APIPX_LoadableElement localElement = null;
        // only get a list item if the list is not empty.
        if ((getVal != EEGEN_TokenTypes.eeGEN_TTeolist) && (getVal != EEGEN_TokenTypes.eeGEN_TTeof)
            && (getVal != EEGEN_TokenTypes.eeGEN_TTinvalidFileFormat))
        {
            localElement = elem.acceptListItem(listname.getReference(), this);
            if (localElement == null)
            {
                return false;
            }
        }
        if (getVal == EEGEN_TokenTypes.eeGEN_TTsingle)
        {
            isItemList = true;
        }
        EE_Reference<String> diag = new EE_Reference<String>();
        diag.setReference("");

        while (getVal != EEGEN_TokenTypes.eeGEN_TTeolist && getVal != EEGEN_TokenTypes.eeGEN_TTeof
               && getVal != EEGEN_TokenTypes.eeGEN_TTinvalidFileFormat)
        {
            if ((getVal == EEGEN_TokenTypes.eeGEN_TTsolist) || (getVal == EEGEN_TokenTypes.eeGEN_TTpair)
                || (getVal == EEGEN_TokenTypes.eeGEN_TTsingle))
            {

                if (getVal == EEGEN_TokenTypes.eeGEN_TTsingle)
                {
                    if (!isItemList)
                    {
                        // previously had a name/value pair
                        setCurrentError("a list of single items must not include name value pairs ");
                        return false;
                    }
                    // each time through, must first check the previous list
                    // item is fully loaded, and obtain another list item
                    if (itemPos != 0)
                    {
                        if (!localElement.isFullyLoaded(diag, this))
                        {
                            setCurrentError(" not all values set for list item for " + listname.getReference()
                                            + " error is " + diag.getReference());
                            return false;
                        }
                        // get another list element;
                        localElement = elem.acceptListItem(listname.getReference(), this);
                        if (localElement == null)
                        {
                            if (getCurrentError().isEmpty())
                            {
                                setCurrentError("internal error");
                            }
                            return false;
                        }
                    }
                    name.setReference(listname.getReference());// provides
                                                               // additional
                                                               // info.
                    // needed by server version list.
                }
                else
                {
                    if (getVal == EEGEN_TokenTypes.eeGEN_TTsolist)
                    {
                        if (!localElement.listIsKnown(name.getReference()))
                        {
                            setCurrentError("unknown list " + name.getReference() + " parsed.");
                            return false;
                        }
                    }
                    if (isItemList)
                    {
                        // cannot mix name/value pairs with item lists ...
                        setCurrentError("a list of single items must not include name value pairs");
                        return false;
                    }
                    else if (elems2.containsKey(name.getReference()))
                    {

                        elems2.clear();
                        if (!localElement.isFullyLoaded(diag, this))
                        {
                            setCurrentError(" not all values set for list item for " + listname.getReference()
                                            + " error is " + diag.getReference());
                            return false;
                        }
                        localElement = elem.acceptListItem(listname.getReference(), this);
                        if (localElement == null)
                        {
                            return false;
                        }

                    }
                }

                if (getVal == EEGEN_TokenTypes.eeGEN_TTsolist)
                {
                    if (!parseList(localElement, name))
                    {
                        return false;
                    }
                }
                else
                {

                    if (!localElement.acceptValue(name.getReference(), value.getReference(), this))
                    {
                        if (getCurrentError().isEmpty())
                        {
                            setCurrentError("internal error 2.");
                        }
                        return false;
                    }

                }
                // only put it in once its been processed
                elems2.put(name.getReference(), value.getReference());
                getVal = getNextTokens(name, value);

                while (getVal == EEGEN_TokenTypes.eeGEN_TTblankline)
                {
                    getVal = getNextTokens(name, value);
                }
                itemPos++;
            }
        }
        // last list element was never tested for being fully loaded.
        if (getVal == EEGEN_TokenTypes.eeGEN_TTinvalidFileFormat)
        {
            return false;
        }
        // only check if the list is not empty.
        if (localElement != null)
        {
            if (!localElement.isFullyLoaded(diag, this))
            {
                setCurrentError(diag.getReference());
                return false;
            }
        }
        if (getVal == EEGEN_TokenTypes.eeGEN_TTeolist)
        {

            return true;
        }
        else
        {
            // when parsing a list it must finish with end of list token.
            setCurrentError("expected to find end of list for list " + listname.getReference());
            return false;
        }
    }

    /**
     * This function is called after all attributes have been loaded, and
     * verifies that the attributes are OK. An example of cross validation is
     * when one configuration item is dependent upon another item - eg the local
     * ports when the role is "user".
     */
    private boolean performCrossValidation(EE_Reference<String> diagnostic)
    {
        int numForeign = 0;
        int numLocal = 0;
        for (int i = 0; i < this.eeResponderPortList.getNumResponderPorts(); i++)
        {
            EE_APIPX_ResponderPort tmpAux = new EE_APIPX_ResponderPort(false, null);
            EE_Reference<EE_APIPX_ResponderPort> tmp = new EE_Reference<EE_APIPX_ResponderPort>();
            tmp.setReference(tmpAux);

            EE_APIPX_ResponderPort tmpA = this.eeResponderPortList.getResponderPort(i);
            assert (tmpA != null) : "programming error in database";
            if (tmpA.getIsForeign())
            {
                numForeign++;
            }
            else
            {
                numLocal++;
            }
        }
        if (this.eeProxySettings.getRole() == SLE_AppRole.sleAR_user)
        {
            if (numForeign <= 0)
            {
                diagnostic.setReference("a user application must contain at least one remote port");
                return false;
            }
        }
        else if (this.eeProxySettings.getRole() == SLE_AppRole.sleAR_provider)
        {
            if (numLocal <= 0)
            {
                diagnostic.setReference("a provider application must contain at least one local port");
                return false;
            }
            EE_APIPX_SrvType srvType;
            for (int i = 0; i < this.eeSrvTypeList.getNumSrvTypes(); i++)
            {
                srvType = this.eeSrvTypeList.getSrvTypeByPos(i);
                if (srvType != null)
                {
                    boolean bErr = (srvType.getNumVersions() <= 0);
                    if (bErr)
                    {
                        diagnostic.setReference("a provider application must set the service types supported versions");
                        return false;
                    }
                }
            }
        }
        else if (this.eeProxySettings.getRole() == SLE_AppRole.sleAR_userAndProvider)
        {
            diagnostic.setReference("the user and provider role is not currently supported.");
            return false;
        }
        if (this.eePeerApplDataList.getNumPeerApplDataItems() <= 0)
        {
            diagnostic.setReference("there must be at least one remote peer present in the list of remote peers.");
            return false;
        }
        return true;
    }

    /**
     * Used by the instances of classes derived from EE_APIPX_LoadableElement.
     * Registers a keyword to a loadable element - when the keyword is parsed
     * (only at the outer level) then the loadable element will be called. Will
     * return false if the keyword is registered. Note that this is a
     * programming error if it occurs.
     */
    public boolean registerOuterKeyword(String akeyword, EE_APIPX_LoadableElement toWhom)
    {
        if (this.keywords.containsKey(akeyword))
        {
            setCurrentError("keyword " + akeyword + " was attempted to be registered twice");
            return false;
        }
        else
        {
            this.keywords.put(akeyword, toWhom);
            return true;
        }
    }

    /**
     * Instantiates and requests all the directly aggregated Loadable Elements
     * to register their keywords
     */
    private boolean registerElements()
    {
        this.eeLocalApplData = new EE_APIPX_LocalApplData();
        this.eeProxySettings = new EE_APIPX_ProxySettings();
        this.eeSrvTypeList = new EE_APIPX_SrvTypeList();
        this.eePeerApplDataList = new EE_APIPX_PeerApplDataList();
        this.eeResponderPortList = new EE_APIPX_ResponderPortList();
        this.eeIPCConfig = new EE_APIPX_IPCConfig();
        this.eeTMLData = new EE_APIPX_TMLData();
        this.eeLocalApplData.registerKeywords(this);
        this.eeProxySettings.registerKeywords(this);
        this.eeSrvTypeList.registerKeywords(this);
        this.eePeerApplDataList.registerKeywords(this);
        this.eeResponderPortList.registerKeywords(this);
        this.eeIPCConfig.registerKeywords(this);
        this.eeTMLData.registerKeywords(this);

        return true;
    }

}
