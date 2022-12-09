/**
 * @(#) EE_APIPX_PortData.java
 */

package esa.sle.impl.api.apipx.pxdb;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class holds the port data, which comprise the IP address and the TCP port
 * number.
 */
public class EE_APIPX_PortData extends EE_APIPX_LoadableElement
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_PortData.class.getName());

    /**
     * This will be either blank, or a valid entry resolveable using appropriate
     * DNS resolver functions.
     */
    private String hostName;

    /**
     * Indicates whether the _hostName attribute has been set.
     */
    private boolean hostNameSet = false;

    /**
     * Indicates whether an IP Address or a hostname is used (or will be used)
     * by the object.
     */
    private boolean isIndirect = false;

    /**
     * Whether or not the port data is foreign (connecting) or local port.
     */
    private boolean isForeign = false;

    /**
     * The TCP port number.
     */
    private int tcpPortNumber;

    /**
     * This is the actual IP version 4 address. Note that if the hostname is
     * set, then the hostname is always used as the source of the address.
     */
    private InetAddress tcpIPAddress;

    /**
     * Indicates whether the TCP/IP Address has been set.
     */
    private boolean tcpIPAddressSet = false;

    private final boolean tcpPortNumberSet;


    @SuppressWarnings("unused")
    private EE_APIPX_PortData(final EE_APIPX_PortData right)
    {
        this.hostNameSet = right.hostNameSet;
        this.isIndirect = right.isIndirect;
        this.isForeign = right.isForeign;
        this.tcpIPAddressSet = right.tcpIPAddressSet;
        this.tcpPortNumberSet = right.tcpPortNumberSet;
    }

    /**
     * One, or both of the two input string parameters can be blank.. If both
     * are blank, then the function get_tcpIPAddress will use INADDR_ANY as the
     * IP address. Note that both string parameters should not be used
     * simultaneously.
     */
    public EE_APIPX_PortData(boolean isIndirect, boolean isForeign)
    {
        this.hostNameSet = false;
        this.isIndirect = isIndirect;
        this.isForeign = isForeign;
        this.tcpPortNumberSet = false;
        this.tcpIPAddressSet = false;
    }

    /**
     * Returns the actual TCP port associated with the port data.
     */
    public final int getTcpPortNumber()
    {
        return this.tcpPortNumber;
    }

    /**
     * Returns the TCP/IP address. Note that if the Port Data contains a
     * hostname, the return value will be resolved from the hostname, at the
     * time this function is called. This ensures that the latest DNS entry is
     * always used. S_OK The TCP address is valid E_INVAL The TCP address is not
     * valid.
     */
    public InetAddress getTcpIPAddress()
    {
        InetAddress retVal = null;
        if (this.hostNameSet)
        {
            try
            {
                retVal = InetAddress.getByName(this.hostName);
            }
            catch (UnknownHostException e)
            {
                LOG.log(Level.FINE, "UnknownHostException ", e);

            }
            if (retVal == null)
            {
                return retVal;
            }
            else
            {
                byte[] ptmp = retVal.getAddress();
                if (ptmp == null)
                {
                    return retVal;
                }
                else
                {
                    return retVal;
                }
            }
        }
        retVal = this.tcpIPAddress;
        return retVal;

    }

    /**
     * Returns whether the host name has been set or not.
     */
    public boolean getHostNameSet()
    {
        return this.hostNameSet;
    }

    /**
     * Returns the hostname as specified by the configuration file.
     */
    public String getHostName()
    {
        return this.hostName;
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public boolean acceptValue(final String name, final String value, EE_Database db)
    {
        if (!name.equals(EE_APIPX_ResponderPort.CI_HNAMEKeyword) && !name.equals(EE_APIPX_ResponderPort.CI_IPKeyword))
        {
            return super.acceptValue(name, value, db);
        }
        if (!this.isForeign)
        {
            if (name.equals(EE_APIPX_ResponderPort.CI_HNAMEKeyword))
            {
                db.setCurrentError("Only dotted decimal addresses can be specified for local ports.");
                return false;
            }
        }
        String part1;
        String part2;
        int posColon = value.lastIndexOf(':');
        if (posColon == -1)
        {
            db.setCurrentError(":<port> suffix not found in " + value);
            return false;
        }
        String maxport = "65535"; // 16 bit integer.
        part1 = value.substring(0, posColon);
        part2 = value.substring(posColon + 1, value.length());
        int portLen = part2.length();
        if (portLen > maxport.length())
        {
            db.setCurrentError("length of TCP port is too long ..." + part2);
            return false;
        }
        else if (portLen == maxport.length())
        {
            if (part2.compareTo(maxport) > 0)
            {
                db.setCurrentError("incorrect TCP port given ..." + part2);
                return false;
            }
        }
        HRESULT isOK = HRESULT.S_OK;
        try
        {
            this.tcpPortNumber = EE_Database.convIntegral(part2);
        }
        catch (SleApiException e)
        {
            isOK = e.getHResult();
        }
        if (isOK != HRESULT.S_OK)
        {
            db.setCurrentError("incorrect TCP port given ..." + part2);
            return false;
        }
        if (this.isIndirect)
        {
            this.hostName = part1;
            this.hostNameSet = true;
            this.tcpIPAddress = getTcpIPAddress();
            if (this.tcpIPAddress == null)
            {
                // Give a warning at this stage, but do not terminate the process
                LOG.log(Level.WARNING, "Could not resolve host name - " + this.hostName
                        + " - to a valid IP Addresss. The host must be available latest on bind operation.");
                return true;
            }
            else
            {
                this.hostNameSet = true;
                return true;
            }
        }
        else
        {
            if (part1.equals("*"))
            {
                boolean catchError = false;
                if (this.isForeign)
                {
                    // foreign ports cannot have a "*"
                    db.setCurrentError("cannot specify the any IP address (*) for a foreign port.");
                    return false;
                }
                byte[] ipAddr = new byte[] { 0, 0, 0, 0 };
                try
                {
                    this.tcpIPAddress = InetAddress.getByAddress(ipAddr);
                    this.tcpIPAddressSet = true;

                }
                catch (UnknownHostException e)
                {
                    LOG.log(Level.FINE, "UnknownHostException ", e);
                    db.setCurrentError("could not parse " + part1 + " as a dotted decimal IP Address.");
                    catchError = true;
                    return false;

                }
                if (!catchError)
                {
                    this.tcpIPAddressSet = true;
                    return true;
                }
                return false;
            }
            else
            {

                boolean catchError = false;
                try
                {
                    this.tcpIPAddress = InetAddress.getByName(part1);
                }
                catch (UnknownHostException e)
                {
                    LOG.log(Level.FINE, "UnknownHostException ", e);
                    db.setCurrentError("could not parse " + part1 + " as a dotted decimal IP Address.");
                    catchError = true;
                    return false;
                }
                if (!catchError)
                {
                    this.tcpIPAddressSet = true;
                    return true;
                }
                return false;
            }
        }
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public EE_APIPX_LoadableElement acceptListItem(String name, EE_Database db)
    {
        return super.acceptListItem(name, db);
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public boolean isFullyLoaded(EE_Reference<String> diagnostic, EE_APIPX_Database db)
    {
        if (!this.hostNameSet && !this.tcpIPAddressSet)
        {
            diagnostic.setReference(getNotLoadedMsg(EE_APIPX_ResponderPort.CI_HNAMEKeyword + " or "
                                                    + EE_APIPX_ResponderPort.CI_IPKeyword));
            return false;
        }
        return true;
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public void registerKeywords(EE_APIPX_Database argdb)
    {

    }

}
