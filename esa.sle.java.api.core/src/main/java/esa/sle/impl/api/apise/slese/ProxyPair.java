package esa.sle.impl.api.apise.slese;

import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_BindRole;

public class ProxyPair
{
    private IUnknown iUnknown;

    private SLE_BindRole bindRole;


    public ProxyPair(IUnknown iUnknown, SLE_BindRole bindRole)
    {
        this.iUnknown = iUnknown;
        this.bindRole = bindRole;
    }

    public IUnknown getIunknown()
    {
        return this.iUnknown;
    }

    public void setIunknown(IUnknown iUnknown)
    {
        this.iUnknown = iUnknown;
    }

    public SLE_BindRole getBindRole()
    {
        return this.bindRole;
    }

    public void setBindRole(SLE_BindRole bindRole)
    {
        this.bindRole = bindRole;
    }
}
