package com.sacide.everythingbridge;

import com.sacide.smart.home.api.compilation.DevicesManager;
import com.sf298.genericwebserver.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import sauds.toolbox.encryption.Encryptor;

public class Main {
    
    public static final String JAR_NAME = "PC Controller.jar";
    public static final String VERSION = "V0.1";
    private static DefaultUserManager um;
    private static DevicesManager dm;
    
    public static void main(String[] args) throws InterruptedException, IOException {
		String keystoreFilePath = (args.length < 1) ? "D:\\Dropbox\\Java Projects\\EverythingBridge\\src\\main\\resources\\mykey.keystore" : args[0];
		String storepass = (args.length < 2) ? "123456" : args[1];
		String keypass = (args.length < 3) ? "123456" : args[2];
		
        um = new DefaultUserManager(false, "psiuofgosurf", "fineowufnoerun");
		um.addUser("saud", Encryptor.hashSHA256("a", ""));
		um.setPAC(PagesAccessChecker.ALLOWED);
		
		dm = new DevicesManager();
		dm.scanDevices();
		/*for(int i=10; i<15; i++) {
			dm.addDevice(lifx.toRGBLightDevice(new Device("192.168.0."+i, 100, "MYLIGHt "+i))); // test devices to test website
		}*/
		
		SHTMLServer server = new SHTMLServer(keystoreFilePath, storepass, keypass);
		WSFilesLoaderInit.addToServer(server, um, "WebPages");
		WSLoginInit.addToServer(server, "Everythingbridge", "/home.html", um);
		WebPageLoader.addContextsToServer(server, um, dm);
		
		server.start();
    }
	
    public static void logMessage(String line) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println(sdf.format(cal.getTime()) + "   " + line);
    }
    
}
