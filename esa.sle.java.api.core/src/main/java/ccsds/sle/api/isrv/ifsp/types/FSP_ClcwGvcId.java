package ccsds.sle.api.isrv.ifsp.types;

/**
 * This class has been introduced with SLES V5 and encapsulates
 * the member of type FSP_GvcId.
 * 
 * configured and notConfigured are acting as choice (ex-or).
 *
 */
public class FSP_ClcwGvcId 
{
	
	private FSP_GvcId gvcid;
	private FSP_ConfType config;
	
	public FSP_ClcwGvcId()
	{
		this.gvcid = null;
		this.config = FSP_ConfType.fspCT_notConfigured;
	}
	
	public FSP_ClcwGvcId(FSP_GvcId gvcId)
	{
		if(gvcId != null)
		{
			if(gvcId.getType() == FSP_ChannelType.fspCT_invalid)
			{
				this.config = FSP_ConfType.fspCT_notConfigured;
			}
			else
			{
				this.gvcid = gvcId;
				this.config = FSP_ConfType.fspCT_configured;
			}
		}
		else
		{
			this.config = FSP_ConfType.fspCT_notConfigured;
		}
	}	
	
	public FSP_ClcwGvcId(FSP_GvcId gvcid, FSP_ConfType config){
		this.gvcid = gvcid;
		this.config = config;
	}
	
	public FSP_GvcId getGvcId() {
		return this.gvcid;
	}
	
	public FSP_ConfType getConfigType()
	{
		return this.config;
	}
}
