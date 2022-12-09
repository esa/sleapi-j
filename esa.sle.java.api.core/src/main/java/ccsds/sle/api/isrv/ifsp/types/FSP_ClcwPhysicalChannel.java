package ccsds.sle.api.isrv.ifsp.types;


public class FSP_ClcwPhysicalChannel {
	
	private String clcwPhysicalChannel = null; 
	
	private FSP_ConfType config;
	
	public FSP_ClcwPhysicalChannel(String str){
		if(str != null){
			config = FSP_ConfType.fspCT_configured;
		}
		else{
			config = FSP_ConfType.fspCT_notConfigured;
		}
		clcwPhysicalChannel = str;
	}
	
	public FSP_ConfType getConfigType(){
		return config;
	}
	
	public String getClcwPhysicalChannel(){
		return clcwPhysicalChannel;
	}
}
