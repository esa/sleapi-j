package esa.sle.sicf.si.parser.file;

import java.util.ArrayList;
import java.util.List;

import ccsds.sle.api.isle.iutl.ISLE_Time;
import esa.sle.sicf.si.descriptors.SIDescriptor;

public class ServiceInstanceContainer
{

    private String description = "";

    private String requesterName = "";

    private ISLE_Time creationDate;

    private int version = -1;

    private List<SIDescriptor> SIDescriptionList = new ArrayList<SIDescriptor>();


    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getRequesterName()
    {
        return this.requesterName;
    }

    public void setRequesterName(String requesterName)
    {
        this.requesterName = requesterName;
    }

    public ISLE_Time getCreationDate()
    {
        return this.creationDate;
    }

    public void setCreationDate(ISLE_Time creationDate)
    {
        this.creationDate = creationDate;
    }

    public int getVersion()
    {
        return this.version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public List<SIDescriptor> getSIDescriptionList()
    {
        return this.SIDescriptionList;
    }

    public void setSIDescription(List<SIDescriptor> SIDescription)
    {
        this.SIDescriptionList = SIDescription;
    }

    public void addElementToList(SIDescriptor el)
    {
        this.SIDescriptionList.add(el);
    }

}
