package esa.sle.impl.api.apipx.pxdel;

import java.util.HashSet;
import java.util.Set;

public class PXDEL_Gvcid
{
    private boolean isMasterChannel;

    private long scid;

    private long version;

    private Set<Long> vcid = new HashSet<Long>();


    public PXDEL_Gvcid(boolean isMasterChannel, long scid, long version, Set<Long> vcid)
    {
        this.isMasterChannel = isMasterChannel;
        this.scid = scid;
        this.version = version;
        this.vcid = vcid;
    }

    public PXDEL_Gvcid()
    {
        this.isMasterChannel = false;
        this.scid = -1;
        this.version = -1;
        this.vcid = new HashSet<Long>();
    }

    public boolean isMasterChannel()
    {
        return this.isMasterChannel;
    }

    public void setMasterChannel(boolean isMasterChannel)
    {
        this.isMasterChannel = isMasterChannel;
    }

    public long getScid()
    {
        return this.scid;
    }

    public void setScid(long scid)
    {
        this.scid = scid;
    }

    public long getVersion()
    {
        return this.version;
    }

    public void setVersion(long version)
    {
        this.version = version;
    }

    public Set<Long> getVcid()
    {
        return this.vcid;
    }

    public void setVcid(Set<Long> vcid)
    {
        this.vcid = vcid;
    }
}
