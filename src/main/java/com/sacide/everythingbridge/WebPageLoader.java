/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge;

import LifxCommander.Messages.DataTypes.HSBK;
import static com.sacide.everythingbridge.Main.printMessage;
import com.sacide.smart.home.api.compilation.DevicesManager;
import com.sacide.smart.home.api.compilation.backend.*;
import com.sf298.saudstoolbox.DeepFileIterator;
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

	public static void addContextsToServer(SHTMLServer server, UserManagerV um, ParamEditorV pe, DevicesManager dm) {
		addStaticContexts(server, um);
		addServices(server, um);
		addDynamicPages(server, um, dm);
		WebPageLoader.addRedirect(server, "/", "/login.html");
	}

	private static void addStaticContexts(SHTMLServer server, UserManagerV um) {
		File root = new File(WebPageLoader.class.getClassLoader().getResource("WebPages").getFile().replace("%20", " "));
		DeepFileIterator dfi = new DeepFileIterator(root);
		while (dfi.hasNext()) {
			File file = dfi.next();
			String context = file.toString().substring(root.toString().length()).replace("\\", "/");
			System.out.println(context);
			try {
				if (context.endsWith("login.html") || context.endsWith(".png")
						|| context.endsWith(".js") || context.endsWith(".css")) {
					server.addContext(context, new HTTPServer.FileContextHandler(file), "GET");
				} else {
					server.addContext(context, (HTTPServer.Request req, HTTPServer.Response resp) -> {
						Map<String, String> params = req.getParams();
						int token = Integer.parseInt(params.getOrDefault("token", "-1"));
						if (token != -1 && um.checkToken(token)) {
							String page = readWholeFile(file);
							if (context.endsWith(".html")) {
								resp.getHeaders().add("Content-Type", "text/html");
							}
							resp.send(200, page);
						} else {
							resp.sendError(403, "Invalid login");
						}
						resp.close();
						return 0;
					});
				}
			} catch (IOException ex) {
			}
		}
	}

	private static void addServices(SHTMLServer server, UserManagerV um) {
		server.addContext("/logout", (HTTPServer.Request req, HTTPServer.Response resp) -> {
			Map<String, String> params = req.getParams();
			if (params.containsKey("token")) {
				try {
					int token = Integer.parseInt(params.get("token"));
					um.logout(token);
				} catch (NumberFormatException ex) {
				}
			}
			resp.send(200, "done");
			resp.close();
			return 0;
		}, "PUT");
		server.addContext("/logoutUser", (HTTPServer.Request req, HTTPServer.Response resp) -> {
			Map<String, String> params = req.getParams();
			if (params.containsKey("token")) {
				try {
					int token = Integer.parseInt(params.get("token"));
					um.logoutUser(token);
				} catch (NumberFormatException ex) {
				}
			}
			resp.send(200, "done");
			resp.close();
			return 0;
		}, "PUT");
		server.addContext("/loginChecker", (HTTPServer.Request req, HTTPServer.Response resp) -> { // process token and login credentials
			Map<String, String> params = req.getParams();
			if (params.containsKey("token")) {
				int token = Integer.parseInt(params.get("token"));
				resp.send(200, String.valueOf(um.checkToken(token)));
			} else if (params.containsKey("uname") && params.containsKey("pswHash")) {
				if (um.checkPasswordHash(params.get("uname"), params.get("pswHash"))) {
					printMessage("User: " + params.get("uname") + " logged in succesfully");
					resp.send(200, String.valueOf(um.newToken(params.get("uname"))));
				} else {
					resp.send(200, "-1");
				}
			} else {
				resp.sendError(403);
			}
			resp.close();
			return 0;
		}, "GET");
	}

	private static void addDynamicPages(SHTMLServer server, UserManagerV um, DevicesManager dm) {
		server.addContext("/home.html", new HTTPServer.ContextHandler() {
			@Override
			public int serve(HTTPServer.Request req, HTTPServer.Response resp) throws IOException {
				Map<String, String> params = req.getParams();
				String tokenStr = params.get("token");
				/*if (tokenStr == null || !um.checkToken(Integer.parseInt(tokenStr))) {
					resp.sendError(403);
					return 0;
				}*/

				StringBuilder contentPartBuilder = new StringBuilder();
				for (Map.Entry<Integer, Device> entry : dm.getDeviceMap().entrySet()) {
					Integer deviceID = entry.getKey();
					Device device = entry.getValue();

					String deviceImgPath = null;

					if (device instanceof LightDevice) {
						deviceImgPath = "/imgs/lightbulb.png";
					}
					contentPartBuilder
							.append("<a href=\"/device.html?token=0twtcht4mtoken0thv303c&deviceid=").append(deviceID).append("\"><div class=\"card\"> \n")
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
						+ "    <script src=\"/tokenCode.js\"></script>\n"
						+ "    \n"
						+ "    <div class=\"container\">\n"
						+ "        <h1> Devices </h1>\n"
						+ "        <div class=\"devicePanel\">\n"
						+ "            " + contentPart + "\n"
						+ "            \n"
						+ "            <!--<a href=\"/custom.html?token=0twtcht4mtoken0thv303c\">Custom Message</a><br>\n"
						+ "            <a href=\"/netflix.html?token=0twtcht4mtoken0thv303c\">Netflix Remote</a><br>\n"
						+ "            <a href=\"/mouse.html?token=0twtcht4mtoken0thv303c\">Mouse Control</a><br>\n"
						+ "            <a href=\"/power.html?token=0twtcht4mtoken0thv303c\">Power Control</a>\n"
						+ "            <br>\n"
						+ "            <button onclick=\"JavaScript:logout()\">Logout</button>-->\n"
						+ "        </div>\n"
						+ "    </div>\n"
						+ "\n"
						+ "\n"
						+ "    <script type=\"text/javascript\"> \n"
						+ "        fillPageWithToken();\n"
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
				Map<String, String> params = req.getParams();
				String tokenStr = params.get("token");
				/*if (tokenStr == null || !um.checkToken(Integer.parseInt(tokenStr))) {
					resp.sendError(403);
					return 0;
				}*/
				Integer deviceID = Integer.parseInt(params.get("deviceid"));
				Device device = dm.getDevice(deviceID);

				StringBuilder contentPartBuilder = new StringBuilder();
				//<editor-fold defaultstate="collapsed" desc="add device controls">
				if (device instanceof OnOffDevice) {
					OnOffDevice temp = (OnOffDevice) device;
					// https://www.w3schools.com/howto/howto_css_switch.asp
					contentPartBuilder
							.append("<p style=\"text-align:left;\">Power\n")
							.append("   <label class=\"switch\" style=\"float:right;\">\n")
							.append("	    <input name=\"devicePower\" id=\"powerSwitch\" type=\"checkbox\"").append((temp.getPowerState()) ? "checked" : "").append("/>\n")
							.append("	    <span class=\"slider round\"/>\n")
							.append("   </label>\n")
							.append("<p/>");
				}
				if (device instanceof LightDevice) {
					LightDevice temp = (LightDevice) device;
					// https://www.w3schools.com/howto/howto_js_rangeslider.asp
					contentPartBuilder
							.append("<p style=\"text-align:left;\">Brightness\n")
							.append("	<input name=\"lightBrightness\" id=\"brightnessSlider\" style=\"float:right; width:60%;\" type=\"range\" min=\"1\" max=\"100\" value=\"").append((int) (temp.getLightBrightness() * 100)).append("\" class=\"rangeslider\">\n")
							.append("<p/>");
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
					contentPart = contentPartBuilder.substring(0, contentPartBuilder.length() - 8);
				} else {
					contentPart = "Devices not found";
				}

				// <editor-fold defaultstate="collapsed" desc=" Website Body ">
				String body = "<!DOCTYPE html>\n"
						+ "<html>\n"
						+ "<head>\n"
						+ "    <title>Everything Bridge</title>\n"
						+ "    <meta charset=\"UTF-8\">\n"
						+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
						+ "    \n"
						+ "    <script src=\"./tokenCode.js\"></script>\n"
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
						+ "			" + contentPart
						+ "		</form>\n"
						+ "    </div>\n"
						+ "\n"
						+ "    <script type=\"text/javascript\"> \n"
						+ "        fillPageWithToken();\n"
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
