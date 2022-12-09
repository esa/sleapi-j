/**
 * @(#) EE_APIPX_ResponderPort.java
 */

package esa.sle.impl.api.apipx.pxdb;

import java.util.ArrayList;
import java.util.List;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import esa.sle.impl.ifs.gen.EE_Database;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class holds all information needed to map the logical responder port
 * identifier to a physical address or addresses.
 */
public class EE_APIPX_ResponderPort extends EE_APIPX_LoadableElement
{

    /**
     * The logical responder port identifier as specified by CCSDS. By default
     * can be empty string or null. if it empty, it will be present in the
     * responderPortList.
     */
    private String logicalRspPortId = "";

    /**
     * Indicates whether the responder port id is set or not.
     */
    private boolean logicalRspPortIdSet = false;

    /**
     * A boolean value that indicates whether the port is local or foreign (true
     * indicates foreign)
     */
    private boolean isForeign = false;

    /**
     * The heartbeat value for the port.
     */
    private int hbt = -1;

    private int deadfactor = -1;

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    public final static String C_PortName = "PORT_NAME";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    public final static String CI_IPKeyword = "IP_ADDRESS";

    /**
     * Refer to the description of the Proxy Database in the Reference Manual
     */
    public final static String CI_HNAMEKeyword = "HOST_NAME";

    /**
     * The heartbeat timer keyword for a port.
     */
    private final static String CI_HBTKeyword = "PORT_HEARTBEAT_TIMER";

    /**
     * The dead factor keyword for a port.
     */
    private final static String CI_DeadFactorKeyword = "PORT_DEAD_FACTOR";

    /**
     * TheTCP transmit buffer size keyword for a port.
     */
    private final static String CI_XmitBufferSizeKeyword = "TCP_XMIT_BUFFER_SIZE";

    /**
     * TheTCP receive buffer size keyword for a port.
     */
    private final static String CI_RecvBufferSizeKeyword = "TCP_RECV_BUFFER_SIZE";

    /**
     * The TCP transmit buffer size in bytes.
     */
    private int xmitBufferSize = 0;

    /**
     * The TCP receive buffer size in bytes.
     */
    private int recvBufferSize = 0;

    private final List<EE_APIPX_PortData> eePortData = new ArrayList<EE_APIPX_PortData>();

    /**
     * Provides navigation to the aggregating list.
     */
    private EE_APIPX_ResponderPortList parentList;


    public EE_APIPX_ResponderPort(final EE_APIPX_ResponderPort right)
    {
        this.logicalRspPortIdSet = right.logicalRspPortIdSet;
        this.isForeign = right.isForeign;
        this.hbt = right.hbt;
        this.deadfactor = right.deadfactor;
        this.xmitBufferSize = right.xmitBufferSize;
        this.recvBufferSize = right.recvBufferSize;
    }

    public EE_APIPX_ResponderPort(boolean isForeign, EE_APIPX_ResponderPortList parentList)
    {
        this.logicalRspPortIdSet = false;
        this.hbt = -1;
        this.deadfactor = -1;
        this.xmitBufferSize = 0;
        this.recvBufferSize = 0;
        this.isForeign = isForeign;
        this.parentList = parentList;
    }

    /**
     * Returns whether the port is foreign or not.
     */
    public boolean getIsForeign()
    {
        return this.isForeign;
    }

    /**
     * Sets the result to point to an EE_APIPX_PortData object corresponding to
     * the index passed in, where the index must be >= 0 and less than
     * get_portDataCardinality
     */
    public EE_APIPX_PortData getPortData(int index)
    {
        if (index >= 0 && index < this.eePortData.size())
        {
            return this.eePortData.get(index);
        }
        return null;
    }

    /**
     * Returns the number of port data items held by the Responder Port object.
     */
    public int getPortDataCardinality()
    {
        return this.eePortData.size();
    }

    public List<EE_APIPX_PortData> getPortData()
    {
        return this.eePortData;
    }

    /**
     * Returns the logical ID of the responder Port.
     */
    public String getLogicalID()
    {
        return this.logicalRspPortId;
    }

    /**
     * Returns the dead factor.
     */
    public int getDeadFactor()
    {
        return this.deadfactor;
    }

    /**
     * Returns the heartbeat.
     */
    public int getHbt()
    {
        return this.hbt;
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public boolean acceptValue(final String name, final String value, EE_Database db)
    {

        if (name.equals(C_PortName))
        {
            EE_Reference<EE_APIPX_ResponderPort> ptmp = new EE_Reference<EE_APIPX_ResponderPort>();
            if (this.logicalRspPortIdSet)
            {
                db.setCurrentError(getAlreadyLoadedMsg(name));
                return false;
            }
            else if (this.parentList.getResponderPort(value, ptmp) == HRESULT.S_OK)
            {
                db.setCurrentError(getDuplicateMsg(name, value));
                return false;
            }

            this.logicalRspPortIdSet = true;
            this.logicalRspPortId = value;
            return true;
        }
        else if ((name.equals(CI_HNAMEKeyword)) || (name.equals(CI_IPKeyword)))
        {
            db.setCurrentError(name + " is incorrectly given as a name/value pair. Must be given in list format.");
            return false;
        }
        else if ((name.equals(CI_HBTKeyword)) || (name.equals(CI_DeadFactorKeyword)))
        {
            if (!this.isForeign)
            {
                db.setCurrentError(name + " cannot be set for a local port.");
                return false;
            }

            String maxvalue = Integer.MAX_VALUE + "";

            if (value.length() > maxvalue.length())
            {
                db.setCurrentError("value of " + name + " was too large ..." + value);
                return false;
            }
            else if (value.length() == maxvalue.length() && value.compareTo(maxvalue) > 0)
            {
                db.setCurrentError("value of " + name + " was too large ..." + value);
                return false;
            }
            HRESULT convok = HRESULT.S_OK;
            try
            {
                if (name.equals(CI_HBTKeyword))
                {
                    if (this.hbt != -1)
                    {
                        db.setCurrentError(getAlreadyLoadedMsg(name));
                        return false;
                    }
                    this.hbt = EE_Database.convIntegral(value);
                }
                else
                {
                    if (this.deadfactor != -1)
                    {
                        db.setCurrentError(getAlreadyLoadedMsg(name));
                        return false;
                    }
                    this.deadfactor = EE_Database.convIntegral(value);
                }
            }
            catch (SleApiException e)
            {
                convok = e.getHResult();
            }
            if (convok != HRESULT.S_OK)
            {
                db.setCurrentError("value of " + name + " = " + value + " was not able to be converted to a number.");
                return false;
            }
            else
            {
                return true;
            }
        }

        else if (name.equals(CI_XmitBufferSizeKeyword))
        {
            if (this.xmitBufferSize > 0)
            {
                db.setCurrentError(getAlreadyLoadedMsg(name));
                return false;
            }

            HRESULT convres = HRESULT.S_OK;
            try
            {
                this.xmitBufferSize = EE_Database.convIntegral(value);

            }
            catch (SleApiException e)
            {
                convres = e.getHResult();
            }
            if (convres != HRESULT.S_OK || this.xmitBufferSize < 0)
            {
                db.setCurrentError("invalid value: " + name);
                return false;
            }
            return true;
        }

        else if (name.equals(CI_RecvBufferSizeKeyword))
        {
            if (this.recvBufferSize > 0)
            {
                db.setCurrentError(getAlreadyLoadedMsg(name));
                return false;
            }
            HRESULT convres = HRESULT.S_OK;
            try
            {
                this.recvBufferSize = EE_Database.convIntegral(value);

            }
            catch (SleApiException e)
            {
                convres = e.getHResult();
            }

            if (convres != HRESULT.S_OK || this.recvBufferSize < 0)
            {
                db.setCurrentError("invalid value: " + name);
                return false;
            }
            return true;
        }
        return super.acceptValue(name, value, db);
    }

    @Override
    public EE_APIPX_LoadableElement acceptListItem(final String name, EE_Database db)
    {

        if (!this.isForeign && this.eePortData.size() > 0)
        {
            db.setCurrentError("only one IP Address and port can be specified for a local port.");
            return null;
        }
        if (name.equals(CI_IPKeyword))
        {

            EE_APIPX_PortData ptmp = new EE_APIPX_PortData(false, this.isForeign);
            this.eePortData.add(ptmp);
            return ptmp;
        }
        else if (name.equals(CI_HNAMEKeyword))
        {
            EE_APIPX_PortData ptmp = new EE_APIPX_PortData(true, this.isForeign);
            this.eePortData.add(ptmp);
            return ptmp;
        }
        else if (name.equals(C_PortName))
        {
            db.setCurrentError("the " + C_PortName + " is incorrectly given as a list name");
            return null;
        }
        return super.acceptListItem(name, db);

    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public boolean isFullyLoaded(EE_Reference<String> diagnostic, EE_APIPX_Database db)
    {
        if (!this.logicalRspPortIdSet)
        {
            diagnostic.setReference(getNotLoadedMsg(C_PortName));
            return false;
        }
        else if (this.eePortData.size() <= 0)
        {
            diagnostic.setReference("Port name " + this.logicalRspPortId + " - "
                                    + getNotLoadedMsg(CI_IPKeyword + " or " + CI_HNAMEKeyword));
            return false;
        }
        else if ((this.deadfactor == -1) && (this.hbt == -1) && !this.isForeign)
        {
            return true;
        }
        else if ((this.deadfactor != -1) && (this.hbt != -1) && this.isForeign)
        {
            return true;
        }
        else
        {
            diagnostic.setReference("Problem with " + CI_DeadFactorKeyword + " or " + CI_HBTKeyword);
            if (this.isForeign)
            {
                if (this.deadfactor == -1)
                {
                    diagnostic.setReference("Port name " + this.logicalRspPortId + " - "
                                            + getNotLoadedMsg(CI_DeadFactorKeyword));
                }
                else if (this.hbt == -1)
                {
                    diagnostic.setReference("Port name " + this.logicalRspPortId + " - "
                                            + getNotLoadedMsg(CI_HBTKeyword));
                }
            }
            return false;
        }
    }

    /**
     * Refer to the documentation of EE_APIPX_LoadableElement.
     */
    @Override
    public void registerKeywords(EE_APIPX_Database argdb)
    {

    }

    /**
     * Refer to EE_APIPX_LoadableElement documentation.
     */
    @Override
    public boolean listIsKnown(final String listName)
    {
        if (listName.equals(CI_IPKeyword))
        {
            return true;
        }
        else if (listName.equals(CI_HNAMEKeyword))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns the TCP transmit buffer size to be applied for a socket.
     */
    public int getXmitBufferSize()
    {
        return this.xmitBufferSize;
    }

    /**
     * Returns the TCP receive buffer size to be applied for a socket.
     */
    public int getRecvBufferSize()
    {
        return this.recvBufferSize;
    }

}
