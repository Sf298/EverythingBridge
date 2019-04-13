/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge;

import com.sf298.saudstoolbox.DeepFileIterator;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.freeutils.httpserver.HTTPServer;

/**
 *
 * @author saud
 */
public class WebPageLoader {
	
	public static void addContextsToServer(SHTMLServer server, UserManagerV um, ParamEditorV pe) {
		//String root = "/WebPages";
		File root = new File(WebPageLoader.class.getClassLoader().getResource("WebPages").getFile().replace("%20", " "));
		DeepFileIterator dfi = new DeepFileIterator(root);
		while(dfi.hasNext()) {
			File file = dfi.next();
			String context = file.toString().substring(root.toString().length()).replace("\\", "/");
			System.out.println(context);
			try {
				if(context.endsWith("login.html") || context.endsWith(".png")
						|| context.endsWith(".js") || context.endsWith(".css")) {
					server.addContext(context, new HTTPServer.FileContextHandler(file), "GET");
				} else {
					server.addContext(context, (HTTPServer.Request req, HTTPServer.Response resp) -> {
						Map<String, String> params = req.getParams();
						int token = Integer.parseInt(params.getOrDefault("token", "-1"));
						if(token != -1 && server.checkToken(token)) {
							String page = readWholeFile(file);
							if(context.endsWith(".html"))
								resp.getHeaders().add("Content-Type", "text/html");
							resp.send(200, page);
						} else {
							resp.sendError(403, "Invalid login");
						}
						resp.close();
						return 0;
					});
				}
			} catch(IOException ex) {}
			
			
			
			
			/*server.addContext(context, (HTTPServer.Request req, HTTPServer.Response resp) -> {
				
				Map<String, String> params = req.getParams();
				if(path.endsWith("login.html")) {
					String page = loadResourcePage(path);
					resp.getHeaders().add("Content-Type", "text/html");
					resp.send(200, page);
					resp.close();
					return 0;
				} else {
					int token = Integer.parseInt(params.getOrDefault("token", "-1"));
					if(true/*token != -1 && server.checkToken(token)*) {
						if(path.toLowerCase().endsWith(".png")) {
							resp.getHeaders().add("Content-Type", "image/png");
							resp.send(200, String .valueOf(getImageLength(path)));
							sendImageToBody(path, resp.getBody());
							resp.close();
							return 0;
						} else {
							String page = loadResourcePage(path);
							if(path.endsWith(".js"))
								resp.getHeaders().add("Content-Type", "text/javascript");
							else if(path.endsWith(".html"))
								resp.getHeaders().add("Content-Type", "text/html");
							resp.send(200, page);
						}
					} else {
						resp.sendError(403, "Invalid login");
						resp.close();
						return 0;
					}
				}*/
				
				
				
				
				/*
				String page = loadResourcePage(path);
				
				Map<String, String> params = req.getParams();
				boolean auth = false;
				if(page.contains(PARAM_START+"token"+PARAM_END)) {
					int token = Integer.parseInt(params.getOrDefault("token", "-1"));
					if(token != -1 && server.checkToken(token)) {
						auth = true;
						//page = page.replace(PARAM_START+"token"+PARAM_END,String.valueOf(token));
					}
				} else {
					auth = true;
				}
				
				if(auth) {
					page = page.replace(PARAM_START+"mouseDpi"+PARAM_END,String.valueOf(pe.getParamAsString(ParamEditorV.MOUSE_DPI)));

					if(path.endsWith(".js"))
						resp.getHeaders().add("Content-Type", "text/javascript");
					else
						resp.getHeaders().add("Content-Type", "text/html");

					resp.send(200, page);
				} else {
					resp.sendError(403, "Invalid login");
				}
				*
				resp.close();
				return 0;
			});*/
		}
	}
	
	public static final String PARAM_START = "0twtcht4m"; // <!0--
	public static final String PARAM_END = "0thv303c"; // --0>
	/*public static String loadResourcePage(String file) {
		try {
			return ClassPathIterator.getResourceAsString(file);
		} catch(IOException ex) {
			Logger.getLogger(WebPageLoader.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	public static void sendImageToBody(String file, OutputStream os) {
		try {
			BufferedInputStream bis = new BufferedInputStream(ClassPathIterator.getResourceAsStream(file));
			byte[] buff = new byte[1024];
			int len;
			while((len = bis.read(buff)) != -1) {
				System.out.println("hello "+len);
				os.write(buff);
			}
			os.flush();
			os.close();
			bis.close();
		} catch(IOException ex) {
			Logger.getLogger(WebPageLoader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	public static int getImageLength(String file) {
		try {
			InputStream is = ClassPathIterator.getResourceAsStream(file);
			int out = is.available();
			is.close();
			return out;
		} catch(IOException ex) {
			Logger.getLogger(WebPageLoader.class.getName()).log(Level.SEVERE, null, ex);
		}
		return -1;
	}*/
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
