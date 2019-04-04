package LifxCommander.Messages.Device;

import LifxCommander.Messages.DataTypes.Payload;

public class GetLabel extends Payload{
	int code = 23;
	
	public GetLabel() {}
	
	public int getCode() {
		return code;
	}
}
