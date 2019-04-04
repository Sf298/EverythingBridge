package LifxCommander.Messages.Device;

import LifxCommander.Messages.DataTypes.Payload;

public class GetWifiInfo extends Payload {
	int code = 16;
	
	public GetWifiInfo() {}
	
	public int getCode() {
		return code;
	}

}
