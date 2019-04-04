package LifxCommander.Messages.Device;

import LifxCommander.Messages.DataTypes.Payload;

public class Acknowledgement extends Payload{
	int code = 45;
	
	public Acknowledgement() {}
	
	public int getCode() {
		return code;
	}
}
