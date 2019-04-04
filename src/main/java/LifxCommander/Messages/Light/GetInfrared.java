package LifxCommander.Messages.Light;

import LifxCommander.Messages.DataTypes.Payload;

public class GetInfrared extends Payload {
	int code = 120;
	
	public GetInfrared() {}
	
	public int getCode() {
		return code;
	}
}
