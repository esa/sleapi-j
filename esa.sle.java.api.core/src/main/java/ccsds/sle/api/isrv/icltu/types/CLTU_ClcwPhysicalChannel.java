package ccsds.sle.api.isrv.icltu.types;


public class CLTU_ClcwPhysicalChannel {
	
	private String phyChannel;
	private CLTU_ConfType config;

	public CLTU_ClcwPhysicalChannel()
	{
		this.phyChannel = null;
		this.config = CLTU_ConfType.cltuCT_notConfigured;
	}
	
	public CLTU_ClcwPhysicalChannel(String str){
		if(str != null)
		{
			this.phyChannel = str;
			this.config = CLTU_ConfType.cltuCT_configured;
		}
		else
		{
			this.config = CLTU_ConfType.cltuCT_notConfigured;
		}
	}
	
	public CLTU_ClcwPhysicalChannel(String str, CLTU_ConfType config){
		if(str != null && config == CLTU_ConfType.cltuCT_configured)
		{
			this.phyChannel = str;
			this.config = config;
		}
		else
		{
			this.config = CLTU_ConfType.cltuCT_notConfigured;
		}
	}
	
	public String getCltuPhyChannel() {
		return this.phyChannel;
	}
	
	public CLTU_ConfType getConfigType()
	{
		return this.config;
	}

}
