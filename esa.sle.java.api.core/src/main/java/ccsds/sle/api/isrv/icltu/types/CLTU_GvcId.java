package ccsds.sle.api.isrv.icltu.types;

public class CLTU_GvcId 
{
    CLTU_ChannelType type;

    private int scid; /* 0 to 1023 */

    private int version; /* 0 to 3 */

    private int vcid; /* 0 to 63 */


    public CLTU_GvcId()
    {
        setScid(0);
        setVersion(0);
        setVcid(0);
        setType(CLTU_ChannelType.cltuCT_invalid);
    }

    public CLTU_GvcId(int scid, int version, int vcid, CLTU_ChannelType type)
    {
        setScid(scid);
        setVersion(version);
        setVcid(vcid);
        setType(type);
    }

    public CLTU_GvcId(CLTU_GvcId right)
    {
        this.type = right.type;
        this.scid = right.scid;
        this.version = right.version;
        this.vcid = right.vcid;
    }

    public void setScid(int scid)
    {
        assert (scid >= 0 && scid <= 1023) : "scid is not in the interval " + scid;
        this.scid = scid;
    }

    public void setVersion(int version)
    {
        assert (version >= 0 && version <= 3) : "version is not in the interval " + version;
        this.version = version;
    }

    public void setVcid(int vcid)
    {
        assert (vcid >= 0 && vcid <= 63) : "vcid is not in the interval " + vcid;
        this.vcid = vcid;
    }

    public int getScid()
    {
        return this.scid;
    }

    public int getVersion()
    {
        return this.version;
    }

    public int getVcid()
    {
        return this.vcid;
    }

    public CLTU_ChannelType getType()
    {
        return this.type;
    }

    public void setType(CLTU_ChannelType type)
    {
        this.type = type;
    }
	
	public String toString()
	{
		StringBuffer strbuff = new StringBuffer("ChannelType="+type.toString());
		strbuff.append(" SCID="+scid);
		strbuff.append(" VCID="+vcid);
		strbuff.append(" Version="+version);
		return strbuff.toString();
	}
}
