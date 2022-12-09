package ccsds.sle.api.isrv.ircf.types;

public class RCF_Gvcid
{
    /**
     * Type of the channel.
     */
    RCF_ChannelType type;

    /**
     * 0 to 1023
     */
    private int scid;

    /**
     * 0 to 3
     */
    private int version;

    /**
     * 0 to 63
     */
    private int vcid;


    /**
     * Default constructor RCF_Gvcid.
     */
    public RCF_Gvcid()
    {
        setScid(0);
        setVersion(0);
        setVcid(0);
        setType(RCF_ChannelType.rcfCT_invalid);
    }

    /**
     * Constructor RCF_Gvcid.
     * 
     * @param scid
     * @param version
     * @param vcid
     * @param type
     */
    public RCF_Gvcid(int scid, int version, int vcid, RCF_ChannelType type)
    {
        setScid(scid);
        setVersion(version);
        setVcid(vcid);
        setType(type);
    }

    /**
     * Sets the scid.
     * 
     * @param scid
     */
    public void setScid(int scid)
    {
        assert (scid >= 0 && scid <= 1023) : "scid is not in the interval " + scid;
        this.scid = scid;
    }

    /**
     * Sets the version.
     * 
     * @param version
     */
    public void setVersion(int version)
    {
        assert (version >= 0 && version <= 3) : "version is not in the interval " + version;
        this.version = version;
    }

    /**
     * Sets the vcid.
     * 
     * @param vcid
     */
    public void setVcid(int vcid)
    {
        assert (vcid >= 0 && vcid <= 63) : "vcid is not in the interval " + vcid;
        this.vcid = vcid;
    }

    /**
     * Gets the scid.
     * 
     * @return
     */
    public int getScid()
    {
        return this.scid;
    }

    /**
     * Gets the version.
     * 
     * @return
     */
    public int getVersion()
    {
        return this.version;
    }

    /**
     * Gets the vcid.
     * 
     * @return
     */
    public int getVcid()
    {
        return this.vcid;
    }

    /**
     * Gets the channel type.
     * 
     * @return
     */
    public RCF_ChannelType getType()
    {
        return this.type;
    }

    /**
     * Sety the channel type.
     * 
     * @param type
     */
    public void setType(RCF_ChannelType type)
    {
        this.type = type;
    }

}
