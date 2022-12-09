package ccsds.sle.api.isrv.icltu.types;


/**
 * This class has been introduced with SLES V5 and encapsulates
 * the member of type CLTU_GvcId which was 
 * previously used as this class.
 * 
 * configured and notConfigured are acting as choice (ex-or).
 *
 */
public class CLTU_ClcwGvcId{
		
	private CLTU_GvcId gvcid;
	private CLTU_ConfType config;
	
	public CLTU_ClcwGvcId()
	{
		this.gvcid = null;
		this.config = CLTU_ConfType.cltuCT_notConfigured;
	}
	
	public CLTU_ClcwGvcId(CLTU_GvcId gvcId)
	{
		if(gvcId != null)
		{
			if(gvcId.getType() == CLTU_ChannelType.cltuCT_invalid)
			{
				this.config = CLTU_ConfType.cltuCT_notConfigured;
			}
			else
			{
				this.gvcid = gvcId;
				this.config = CLTU_ConfType.cltuCT_configured;
			}
		}
		else
		{
			this.config = CLTU_ConfType.cltuCT_notConfigured;
		}
	}
	
	public CLTU_ClcwGvcId(CLTU_GvcId gvcId, CLTU_ConfType config){
		if(gvcId != null)
		{
			this.gvcid = gvcId;
			this.config = config;
		}
		else
		{
			this.config = CLTU_ConfType.cltuCT_notConfigured;
		}
	}
	
	public CLTU_GvcId getCltuGvcId() {
		return this.gvcid;
	}
	
	public CLTU_ConfType getConfigType()
	{
		return this.config;
	}
}
