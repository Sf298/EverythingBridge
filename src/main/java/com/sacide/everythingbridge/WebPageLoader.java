/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge;

import LifxCommander.Messages.DataTypes.HSBK;
import static com.sacide.everythingbridge.Main.logMessage;
import com.sacide.smart.home.api.compilation.DevicesManager;
import com.sacide.smart.home.api.compilation.backend.*;
import com.sf298.genericwebserver.*;
import com.sf298.genericwebserver.WSLoginInit;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import net.freeutils.httpserver.HTTPServer;

/**
 *
 * @author saud
 */
public class WebPageLoader {

	public static void addContextsToServer(SHTMLServer server, UserManager um, DevicesManager dm) {
		addDynamicPages(server, um, dm);
		addServices(server, um, dm);
	}
	
	private static void addServices(SHTMLServer server, UserManager um, DevicesManager dm) {
		server.addContext("/deviceAction", new HTTPServer.ContextHandler() { // sessionID, deviceID, action, params...
			@Override
			public int serve(HTTPServer.Request req, HTTPServer.Response resp) throws IOException {
				if (WSLoginInit.checkSessionIDAndReplyError(req, resp, um.getPAC()))
					return 0;
				
				Map<String, String> params = req.getParams();
				Device d = dm.getDevice(Integer.parseInt(params.get("deviceID")));
				
				String action = params.get("action");
				String newValue;
				switch(action) {
					case "SetPowerState":
						newValue = params.get("newValue");
						if(d == null || !(d instanceof OnOffDevice)) {
							resp.sendError(403);
							resp.close();
							return 0;
						}
						((OnOffDevice)d).setPowerState(Boolean.parseBoolean(newValue));
						break;
					case "SetBrightness":
						newValue = params.get("newValue"); // [1-100]
						if(d == null || !(d instanceof LightDevice)) {
							resp.sendError(403);
							resp.close();
							return 0;
						}
						((LightDevice)d).setLightBrightness(Double.parseDouble(newValue), 0);
						break;
				}
				
				resp.send(200, "true");
				resp.close();
				return 0;
			}
		}, "PUT");
	}
	
	private static void addDynamicPages(SHTMLServer server, UserManager um, DevicesManager dm) {
		server.addContext("/home.html", new HTTPServer.ContextHandler() {
			@Override
			public int serve(HTTPServer.Request req, HTTPServer.Response resp) throws IOException {
				if (!um.checkSessionID(WSLoginInit.getSessionID(req))) {
					resp.sendError(403);
					return 0;
				}

				StringBuilder contentPartBuilder = new StringBuilder();
				for (Map.Entry<Integer, Device> entry : dm.getDeviceMap().entrySet()) {
					Integer deviceID = entry.getKey();
					Device device = entry.getValue();

					String deviceImgPath = null;

					if (device instanceof LightDevice) {
						deviceImgPath = "/imgs/lightbulb.png";
					}
					contentPartBuilder
							.append("<a href=\"/device.html?").append(WSLoginInit.SESSION_ID_URL_PARAM).append("&deviceID=").append(deviceID).append("\"><div class=\"card\"> \n")
							.append("    <img src=\"").append(deviceImgPath).append("\" alt=\"light bulb img\" width=\"100%\">\n")
							.append("    <p> ").append(device.getLabel()).append(" </p>\n")
							.append("</div></a><!--\n")
							.append("-->");
				}
				String contentPart;
				if (!dm.getDeviceMap().isEmpty()) {
					contentPart = contentPartBuilder.substring(0, contentPartBuilder.length() - 8);
				} else {
					contentPart = "No devices found";
				}

				// <editor-fold defaultstate="collapsed" desc=" Website Body ">
				String body = "<!DOCTYPE html>\n"
						+ "<html>\n"
						+ "<head>\n"
						+ "    <title>Everything Bridge</title>\n"
						+ "    <meta charset=\"UTF-8\">\n"
						+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
						+ "    \n"
						+ "    <style>\n"
						+ "        @media only screen and (min-device-width: 600px) {\n"
						+ "            .container {\n"
						+ "                margin: 0 25% 0 25%;\n"
						+ "            }\n"
						+ "            .card {\n"
						+ "                width : calc(25% - 10px - 20px);\n"
						+ "            }\n"
						+ "        }\n"
						+ "        @media only screen and (max-device-width: 600px) {\n"
						+ "            .card {\n"
						+ "                width : calc(50% - 10px - 20px);\n"
						+ "            }\n"
						+ "        }\n"
						+ "        .container {\n"
						+ "            text-align: center;\n"
						+ "        }\n"
						+ "        .card {\n"
						+ "            text-align : center;\n"
						+ "            border: 1px solid gray;\n"
						+ "            margin: 10px;\n"
						+ "            display: inline-block;\n"
						+ "        }\n"
						+ "        body {\n"
						+ "            font-family: Sans-Serif;\n"
						+ "            font-weight: bold;\n"
						+ "        }\n"
						+ "        a {\n"
						+ "            color: #3E3D3D;\n"
						+ "        }\n"
						+ "    </style>\n"
						+ "</head>\n"
						+ "\n"
						+ "<body class=\"w3-light-grey w3-content\" style=\"max-width:1600px\">\n"
						+ "    \n"
						+ "    <script src=\"/sessionCode.js\"></script>\n"
						+ "    \n"
						+ "    <div class=\"container\">\n"
						+ "        <h1> Devices </h1>\n"
						+ "        <div class=\"devicePanel\">\n"
						+ "            " + contentPart + "\n"
						+ "        </div>\n"
						+ "    </div>\n"
						+ "\n"
						+ "\n"
						+ "    <script type=\"text/javascript\"> \n"
						+ "        fillPageWithSessionID();\n"
						+ "    </script>\n"
						+ "</body>";
				// </editor-fold>
				resp.send(200, body);
				return 0;
			}
		}, "GET");
		server.addContext("/device.html", new HTTPServer.ContextHandler() {
			@Override
			public int serve(HTTPServer.Request req, HTTPServer.Response resp) throws IOException {
				if (!um.checkSessionID(WSLoginInit.getSessionID(req))) {
					resp.sendError(403);
					return 0;
				}
				Map<String, String> params = req.getParams();
				Integer deviceID = Integer.parseInt(params.get("deviceID"));
				Device device = dm.getDevice(deviceID);

				StringBuilder contentPartBuilder = new StringBuilder();
				//<editor-fold defaultstate="collapsed" desc="add device controls">
				if (device instanceof OnOffDevice) {
					OnOffDevice temp = (OnOffDevice) device;
					// https://www.w3schools.com/howto/howto_css_switch.asp
					contentPartBuilder
							.append("            <script type=\"text/javascript\">\n")
							.append("                function toggleOnOff() {\n")
							.append("                    var powerElement = document.getElementById(\"powerSwitch\")\n")
							.append("                    var newPowerState = powerElement.checked;\n")
							.append("                    var deviceID = getUrlParameter(\"deviceID\");\n")
							.append("                    var xhr = new XMLHttpRequest();\n")
							.append("                    xhr.open('PUT', \"/deviceAction?sessionID=\"+getSessionID()+\"&deviceID=\"+deviceID+\"&action=SetPowerState&newValue=\"+newPowerState, true);\n")
							.append("                    xhr.send();\n")
							.append("                    xhr.onreadystatechange = function (e) {\n")
							.append("                        if (xhr.readyState == 4 && xhr.status == 200) {\n")
							.append("                            if(xhr.responseText != \"true\") {\n")
							.append("                                powerElement.checked = !newPowerState;\n")
							.append("                            }\n")
							.append("                        }\n")
							.append("                    };\n")
							.append("                }\n")
							.append("            </script>")
							.append("            <p style=\"text-align:left;\">Power\n")
							.append("                <label class=\"switch\" style=\"float:right;\">\n")
							.append("                    <input name=\"devicePower\" id=\"powerSwitch\" onclick=\"toggleOnOff()\" type=\"checkbox\"").append((temp.getPowerState()) ? "checked" : "").append("/>\n")
							.append("                    <span class=\"slider round\"/>\n")
							.append("                </label>\n")
							.append("            <p/>\n");
				}
				if (device instanceof LightDevice) {
					LightDevice temp = (LightDevice) device;
					// https://www.w3schools.com/howto/howto_js_rangeslider.asp
					int currVal = (int) (temp.getLightBrightness() * 100);
					contentPartBuilder
							.append("            <script type=\"text/javascript\">\n")
							.append("                function updateBrightness() {\n")
							.append("                    var element = document.getElementById(\"brightnessSlider\")\n")
							.append("                    var newVal = element.value/100.0;\n")
							.append("                    var deviceID = getUrlParameter(\"deviceID\");\n")
							.append("                    var xhr = new XMLHttpRequest();\n")
							.append("                    xhr.open('PUT', \"/deviceAction?sessionID=\"+getSessionID()+\"&deviceID=\"+deviceID+\"&action=SetBrightness&newValue=\"+newVal, true);\n")
							.append("                    xhr.send();\n")
							.append("                    xhr.onreadystatechange = function (e) {\n")
							.append("                        if (xhr.readyState == 4 && xhr.status == 200) {\n")
							.append("                            \n")
							.append("                        }\n")
							.append("                    };\n")
							.append("                }\n")
							.append("            </script>")
							.append("            <p style=\"text-align:left;\">Brightness\n")
							.append("                <input name=\"lightBrightness\" id=\"brightnessSlider\" onchange=\"updateBrightness()\" style=\"float:right; width:60%;\" type=\"range\" min=\"0\" max=\"100\" value=\"").append(currVal).append("\" class=\"rangeslider\">\n")
							.append("            <p/>\n");
				}
				if (device instanceof RGBLightDevice) {
					RGBLightDevice temp = (RGBLightDevice) device;
					Color c = temp.getLightColor().asColor();
					String hex = String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
					// http://jscolor.com/examples/#example-usage
					contentPartBuilder
							.append("<p style=\"text-align:left;\">Colour\n")
							.append("   <input name=\"lightColor\" style=\"float:right;\" class=\"jscolor\" value=\"").append(hex).append("\">\n")
							.append("<p/>");
				}

				if (device instanceof BlindsDevice) {
					contentPartBuilder.append("");
				}
				//</editor-fold>

				String contentPart;
				if (contentPartBuilder.length() > 0) {
					contentPart = contentPartBuilder.toString();
				} else {
					contentPart = "Error: device not loaded!";
				}

				// <editor-fold defaultstate="collapsed" desc=" Website Body ">
				String body = "<!DOCTYPE html>\n"
						+ "<html>\n"
						+ "<head>\n"
						+ "    <title>Everything Bridge</title>\n"
						+ "    <meta charset=\"UTF-8\">\n"
						+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
						+ "    \n"
						+ "    <script src=\"./sessionCode.js\"></script>\n"
						+ "	\n"
						+ "	<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.4.0/jquery.min.js\"></script>\n"
						+ "	<script src=\"jscolor.js\"></script>\n"
						+ "	\n"
						+ "	<link href=\"./rangeSlider.css\" rel=\"stylesheet\">\n"
						+ "	<link href=\"./toggleSwitch.css\" rel=\"stylesheet\">\n"
						+ "	<style>\n"
						+ "        @media only screen and (min-device-width: 600px) {\n"
						+ "            .container {\n"
						+ "                margin: 0 25% 0 25%;\n"
						+ "            }\n"
						+ "            .card {\n"
						+ "                width : calc(25% - 10px - 20px);\n"
						+ "            }\n"
						+ "        }\n"
						+ "        @media only screen and (max-device-width: 600px) {\n"
						+ "            .card {\n"
						+ "                width : calc(50% - 10px - 20px);\n"
						+ "            }\n"
						+ "        }\n"
						+ "        body {\n"
						+ "            font-family: Sans-Serif;\n"
						+ "            font-weight: bold;\n"
						+ "        }\n"
						+ "        a {\n"
						+ "            color: #3E3D3D;\n"
						+ "        }\n"
						+ "    </style>\n"
						+ "</head>\n"
						+ "\n"
						+ "<body class=\"w3-light-grey w3-content\" style=\"max-width:1600px\">\n"
						+ "    \n"
						+ "    <div class=\"container\">\n"
						+ "        <h1> "+device.getLabel()+" </h1>\n"
						+ "		<br>\n"
						+ "		<form action=\"show_data.html\">\n"
						+ contentPart
						+ "		</form>\n"
						+ "    </div>\n"
						+ "\n"
						+ "    <script type=\"text/javascript\"> \n"
						+ "        fillPageWithSessionID();\n"
						+ "    </script>\n"
						+ "</body>";
				// </editor-fold>
				resp.send(200, body);
				return 0;
			}
		}, "GET");
	}

	public static void addRedirect(SHTMLServer server, String oldURL, String newUrl) {
		server.addContext(oldURL, (HTTPServer.Request req, HTTPServer.Response resp) -> { // redirect to login
			resp.redirect(newUrl, true);
			return 0;
		}, "GET");
	}

	public static String readWholeFile(File f) {
		StringBuilder sb = new StringBuilder(512);
		try {
			Scanner in = new Scanner(f);
			while (in.hasNext()) {
				sb.append(in.nextLine()).append("\n");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return sb.toString();
	}

}
