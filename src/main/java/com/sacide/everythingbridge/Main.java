package com.sacide.everythingbridge;

import com.sacide.smart.home.api.compilation.DevicesManager;
import com.sacide.smart.home.api.compilation.LifxCommanderW;
import com.sacide.smart.home.api.compilation.backend.Device;
import com.sacide.smart.home.api.compilation.backend.LightDevice;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class Main {
    
    public static final String JAR_NAME = "PC Controller.jar";
    public static final String VERSION = "V0.1";
    private static UserManagerV um;
    private static ParamEditorV pe;
    private static DevicesManager dm;
    
    public static void main(String[] progArgs) throws InterruptedException, IOException {
        pe = new ParamEditorV();
		pe.init();
        um = new UserManagerV(true, "psiuofgosurf", "fineowufnoerun");
		um.addUser("saud", Encryptor.hashSHA256("a"));

		dm = new DevicesManager();
		LifxCommanderW lifx = new LifxCommanderW();
		for(Device d : lifx.discoverDevices()) {
			/*System.out.print(d);
			System.out.print(" ");
			System.out.println(d.getIp_id());*/
			dm.addDevice(d);
		}
		/*for(int i=10; i<15; i++) {
			dm.addDevice(lifx.toRGBLightDevice(new Device("192.168.0."+i, 100, "MYLIGHt "+i))); // test devices to test website
		}*/

			//System.out.println(new File("/users.prop").getAbsolutePath());

		SHTMLServer server = new SHTMLServer(8889,
			pe.getParamAsString(ParamEditorV.SSL_KEY_FILE),
			pe.getParamAsString(ParamEditorV.SSL_KEY_STORE_PASS),
			pe.getParamAsString(ParamEditorV.SSL_KEYPASS)
		);


		WebPageLoader.addContextsToServer(server, um, pe, dm);

		server.start();
    }
    
    private static void doAction(Map<String, String> args) {
        /*switch(Integer.parseInt(args.get("action"))) {
            case VOLUME_SET:
                //PCFunctions.volumeSet(Integer.parseInt(args.get("arg1")));
                System.out.println("VOLUME_SET");
                break;
            case VOLUME_CHANGE:
                //PCFunctions.volumeChange(Integer.parseInt(args.get("arg1")));
                System.out.println("VOLUME_CHANGE");
                break;

            case POWER_HIBERNATE:
                //PCFunctions.computerHibernate();
                System.out.println("POWER_HIBERNATE");
                break;

            case NETFLIX_SPACE:
                //PCFunctions.sendKey(KeyEvent.VK_SPACE);
                System.out.println("NETFLIX_SPACE");
                break;
            case NETFLIX_FWD:
                //PCFunctions.sendKey(KeyEvent.VK_RIGHT);
                System.out.println("NETFLIX_FWD");
                break;
            case NETFLIX_BCK:
                //PCFunctions.sendKey(KeyEvent.VK_LEFT);
                System.out.println("NETFLIX_BCK");
                break;

            case MOUSE_MOVEBY:
                /*PCFunctions.mouseMoveBy(
                        Double.parseDouble(args.get("arg1")),
                        Double.parseDouble(args.get("arg2")));*
                System.out.println("MOUSE_MOVEBY");
            case MOUSE_MOVETO:
                /*PCFunctions.mouseMoveTo(
                        Integer.parseInt(args.get("arg1")),
                        Integer.parseInt(args.get("arg2")));*
                System.out.println("MOUSE_MOVETO");
                break;
                
            case MOUSE_CLICK:
                //PCFunctions.mouseClick(Integer.parseInt(args.get("arg1")));
                System.out.println("MOUSE_CLICK");
                break;
        }*/
    }
    
    public static void printMessage(String line) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println(sdf.format(cal.getTime()) + "   " + line);
    }
    
}
