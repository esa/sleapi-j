package ccsds.sle.api.isrv.irocf.types;

public class ROCF_Gvcid
{
    /**
     * Channel type.
     */
    private ROCF_ChannelType type;

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
     * Default constructor ROCF_Gvcid.
     */
    public ROCF_Gvcid()
    {
        this.type = ROCF_ChannelType.rocfCT_invalid;
        setScid(0);
        setVersion(0);
        setVcid(0);
    }

    /**
     * Constructor ROCF_Gvcid.
     * 
     * @param right
     */
    public ROCF_Gvcid(ROCF_Gvcid right)
    {
        this.type = right.type;
        setScid(right.scid);
        setVersion(right.version);
        setVcid(right.vcid);
    }

    /**
     * Constructor ROCF_Gvcid.
     * 
     * @param type
     * @param scid
     * @param version
     * @param vcid
     */
    public ROCF_Gvcid(ROCF_ChannelType type, int scid, int version, int vcid)
    {
        this.type = type;
        setScid(scid);
        setVersion(version);
        setVcid(vcid);
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
     * Sets the type.
     * 
     * @param type
     */
    public void setType(ROCF_ChannelType type)
    {
        this.type = type;
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
     * Gets the type.
     * 
     * @return
     */
    public ROCF_ChannelType getType()
    {
        return this.type;
    }
}
