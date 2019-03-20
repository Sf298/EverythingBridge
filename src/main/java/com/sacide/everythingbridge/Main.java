package com.sacide.everythingbridge;

import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    
    public static final String JAR_NAME = "PC Controller.jar";
    public static final String VERSION = "V0.1";
    private static UserManagerV um = new UserManagerV();
    private static ParamEditorV pe = new ParamEditorV();
    
    public static void main(String[] progArgs) {
        //System.out.println(new File("/users.prop").getAbsolutePath());
        try {
            SHTMLServer server = new SHTMLServer(
                    8889, 
                    pe.getParamAsString(ParamEditorV.SSL_KEY_FILE), 
                    pe.getParamAsString(ParamEditorV.SSL_KEY_ALIAS), 
                    pe.getParamAsString(ParamEditorV.SSL_KEY_STORE_PASS), 
                    pe.getParamAsString(ParamEditorV.SSL_KEYPASS)) {
                @Override
                public void handleMessage(SHTMLServerThread t, String line) {
                    boolean printMessage = true;
                    if(line == null) return;
                    String[] req = line.split(" ");
                    try {
                        switch(req[0]) {
                            case "GET":
                                if(req[1].equals("/")) {
                                    t.sendHTML(true, getLoginPage());

                                } else if(req[1].startsWith("/home.html")) {
                                    HashMap<String, String> map = parseArgs(req[1]);
                                    boolean auth = false;
                                    int token=0;
                                    if(map.containsKey("token")) { //validate token if provided
                                        token = Integer.parseInt(map.getOrDefault("token","-1"));
                                        auth = checkToken(token);
                                    } else if(um.checkPassword(map.get("uname"), map.get("psw"))) { // validate password
                                        token = newToken();
                                        auth = true;
                                    }
                                    t.sendHTML(auth, getHomePage(token));

                                } else if(req[1].startsWith("/custom.html")) {
                                    HashMap<String, String> map = parseArgs(req[1]);
                                    int token = Integer.parseInt(map.getOrDefault("token","-1"));
                                    t.sendHTML(checkToken(token), getCustomPage(token));

                                } else if(req[1].startsWith("/mouse.html")) {
                                    HashMap<String, String> map = parseArgs(req[1]);
                                    int token = Integer.parseInt(map.getOrDefault("token","-1"));
                                    t.sendHTML(checkToken(token), getMousePage(token));

                                } else if(req[1].startsWith("/power.html")) {
                                    HashMap<String, String> map = parseArgs(req[1]);
                                    int token = Integer.parseInt(map.getOrDefault("token","-1"));
                                    t.sendHTML(checkToken(token), getPowerPage(token));

                                } else if(req[1].startsWith("/netflix.html")) {
                                    HashMap<String, String> map = parseArgs(req[1]);
                                    int token = Integer.parseInt(map.getOrDefault("token","-1"));
                                    t.sendHTML(checkToken(token), getNetflixPage(token));

                                } else if(req[1].startsWith("/virtualjoystick.js")) {
                                    t.sendHTML(true, "text/javascript", getVirtualJoystick());
                                }
                                break;
                                
                            case "POST":
                                if(req[1].startsWith("/sendMessage.html")) {
                                    HashMap<String, String> map = parseArgs(req[1]);
                                    int token = Integer.parseInt(map.getOrDefault("token","-1"));
                                    if(checkToken(token)) {
                                        printMessage = doAction(map);
                                    }
                                }
                                break;
                                
                            default:
                                break;
                        }
                    } catch(Exception e) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
                    }
                    if(printMessage) {
                        Calendar cal = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        System.out.println(sdf.format(cal.getTime()) + "   " + line);
                    }
                }
            };
            SHTMLServerGUI gui = new SHTMLServerGUI(server, um, pe);
            gui.setIcon("/PC Controller Icon.png");
            gui.show();
        } catch(Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static String getLoginPage() {
        return  "<form action=\"home.html\">\n" +
                "  <div class=\"container\">\n" +
                "    <label for=\"uname\"><b>Username</b></label>\n" +
                "    <input type=\"text\" placeholder=\"Enter Username\" name=\"uname\" required>\n" +
                "    <br>\n" +
                "    <label for=\"psw\"><b>Password</b></label>\n" +
                "    <input type=\"password\" placeholder=\"Enter Password\" name=\"psw\" required>\n" +
                "    <br>\n" +
                "    <button type=\"submit\">Login</button>\n" +
                "  </div>\n" +
                "</form>";
    }
    
    public static String getHomePage(int token) {
        return  "<h1> home page </h1><br>\n" +
                
                "<a href=\"/custom.html?token="+token+"\">Custom Message</a><br>\n" +
                
                "<a href=\"/netflix.html?token="+token+"\">Netflix Remote</a><br>\n" +
                
                "<a href=\"/mouse.html?token="+token+"\">Mouse Control</a><br>\n" +
                
                "<a href=\"/power.html?token="+token+"\">Power Control</a>";
    }
    
    public static String getCustomPage(int token) {
        return  "<form action=\"sendMessage.html\" method=\"post\">\n" +
                "  <div class=\"container\">\n" +
                "    <input type=\"hidden\" name=\"token\" value=\""+token+"\" />" +
                
                "    <label for=\"action\"><b>Action</b></label>\n" +
                "    <input type=\"text\" placeholder=\"Enter Action\" name=\"action\" required><br>\n" +
                
                "    <label for=\"arg1\"><b>Arg 1</b></label>\n" +
                "    <input type=\"text\" placeholder=\"Enter Arg 1\" name=\"arg1\" ><br>\n" +
                
                "    <label for=\"arg2\"><b>Arg 2</b></label>\n" +
                "    <input type=\"text\" placeholder=\"Enter Arg 2\" name=\"arg2\" ><br>\n" +
                
                "    <label for=\"arg3\"><b>Arg 3</b></label>\n" +
                "    <input type=\"text\" placeholder=\"Enter Arg 3\" name=\"arg3\" ><br>\n" +
                
                "    <button type=\"submit\">Send</button>\n" +
                "  </div>\n" +
                "</form>";
    }
    
    public static String getMousePage(int token) {
        return  "<html>\n" +
                "    <head>\n" +
                "        <meta charset=\"utf-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, user-scalable=no, minimum-scale=1.0, maximum-scale=1.0\">\n" +
                "        \n" +
                "        <style>\n" +
                "        body {\n" +
                "            overflow    : hidden;\n" +
                "            padding        : 0;\n" +
                "            margin        : 0;\n" +
                "            background-color: #BBB;\n" +
                "        }\n" +
                "        form {\n" +
                "            float: left; \n" +
                "        }\n" +
                "        #container {\n" +
                "            width        : 100%;\n" +
                "            height        : 85%;\n" +
                "            overflow    : hidden;\n" +
                "            padding        : 0;\n" +
                "            margin        : 0;\n" +
                "            -webkit-user-select    : none;\n" +
                "            -moz-user-select    : none;\n" +
                "        }\n" +
                "        </style>\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <div id=\"container\"></div>" +
                "        <script src=\"/virtualjoystick.js\"></script>\n" +
                "        <script>\n" +
                "            var joystick    = new VirtualJoystick({\n" +
                "                container    : document.getElementById('container'),\n" +
                "                mouseSupport    : true,\n" +
                "            });\n" +
                "            joystick.addEventListener('touchStart', function(){\n" +
                "                console.log('down')\n" +
                "            })\n" +
                "            joystick.addEventListener('touchEnd', function(){\n" +
                "                console.log('up')\n" +
                "            })\n" +
                "            setInterval(function(){\n" +
                "                ;\n" +
                "                joystick.deltaY();\n" +
                "                \n" +
                "                if(joystick.deltaX() != 0 && joystick.deltaX() != 0) {\n" +
                "                    var xmlHttp = new XMLHttpRequest();\n" +
                "                    xmlHttp.open(\"POST\", \"sendMessage.html?token="+token+"&action="+MOUSE_MOVEBY+"&arg1=\"+joystick.deltaX()+\"&arg2=\"+joystick.deltaY(), false ); // false for synchronous request\n" +
                "                    xmlHttp.send( null );\n" +
                "                    return xmlHttp.responseText;\n" +
                "                }\n" +
                "            }, 1/"+pe.getParamAsString(ParamEditorV.MOUSE_DPI)+" * 1000);\n" +
                "        </script>\n" +
                
                "        <iframe width=\"0\" height=\"0\" border=\"0\" name=\"dummyframe\" id=\"dummyframe\"></iframe>\n" +
                "        <form action=\"sendMessage.html\" method=\"post\" target=\"dummyframe\">\n" +
                "            <input type=\"hidden\" name=\"token\" value=\""+token+"\" />" +
                "            <input type=\"hidden\" name=\"action\" value=\""+MOUSE_CLICK+"\" />" +
                "            <input type=\"hidden\" name=\"arg1\" value=\"1\" />" +
                "            <button type=\"submit\">Left</button>\n" +
                "        </form>" +
                "        <form action=\"sendMessage.html\" method=\"post\" target=\"dummyframe\">\n" +
                "            <input type=\"hidden\" name=\"token\" value=\""+token+"\" />" +
                "            <input type=\"hidden\" name=\"action\" value=\""+MOUSE_CLICK+"\" />" +
                "            <input type=\"hidden\" name=\"arg1\" value=\"3\" />" +
                "            <button type=\"submit\">Middle</button>\n" +
                "        </form>" +
                "        <form action=\"sendMessage.html\" method=\"post\" target=\"dummyframe\">\n" +
                "            <input type=\"hidden\" name=\"token\" value=\""+token+"\" />" +
                "            <input type=\"hidden\" name=\"action\" value=\""+MOUSE_CLICK+"\" />" +
                "            <input type=\"hidden\" name=\"arg1\" value=\"2\" />" +
                "            <button type=\"submit\">Right</button>\n" +
                "        </form>" +
                
                "    </body>\n" +
                "</html>";
        
                /*"<iframe width=\"0\" height=\"0\" border=\"0\" name=\"dummyframe\" id=\"dummyframe\"></iframe>\n" +
                
                "<form action=\"sendMessage.html\" target=\"dummyframe\">\n" +
                "    <input type=\"hidden\" name=\"token\" value=\""+token+"\" />" +
                "    <input type=\"hidden\" name=\"action\" value=\""+MOUSE_CLICK+"\" />" +
                "    <input type=\"hidden\" name=\"arg1\" value=\"1\" />" +
                "    <button type=\"submit\">Left</button>\n" +
                "</form>" +
                
                "<form action=\"sendMessage.html\" target=\"dummyframe\">\n" +
                "    <input type=\"hidden\" name=\"token\" value=\""+token+"\" />" +
                "    <input type=\"hidden\" name=\"action\" value=\""+MOUSE_CLICK+"\" />" +
                "    <input type=\"hidden\" name=\"arg1\" value=\"3\" />" +
                "    <button type=\"submit\">Middle</button>\n" +
                "</form>" +
                
                "<form action=\"sendMessage.html\" target=\"dummyframe\">\n" +
                "    <input type=\"hidden\" name=\"token\" value=\""+token+"\" />" +
                "    <input type=\"hidden\" name=\"action\" value=\""+MOUSE_CLICK+"\" />" +
                "    <input type=\"hidden\" name=\"arg1\" value=\"2\" />" +
                "    <button type=\"submit\">Right</button>\n" +
                "</form>";*/
        
                /*"<div id=\"container\"></div>\n" +
                "<script src=\"/virtualjoystick.js\"></script>";*/
                
                /*"<form action=\"sendMessage.html\" method="get">\n" +
                "  <div class=\"container\">\n" +
                "    <input type=\"hidden\" name=\"token\" value=\""+token+"\" />" +
                "    <input type=\"hidden\" name=\"action\" value=\""+MOUSE_MOVE+"\" />" +
                
                "    <label for=\"arg1\"><b>Mouse X</b></label>\n" +
                "    <input type=\"text\" placeholder=\"Enter Mouse X\" name=\"arg1\" required><br>\n" +
                
                "    <label for=\"arg2\"><b>Mouse Y</b></label>\n" +
                "    <input type=\"text\" placeholder=\"Enter Mouse Y\" name=\"arg2\" required><br>\n" +
                
                "    <button type=\"submit\">Move</button>\n" +
                "  </div>\n" +
                "</form>\n" +
                
                "<br><br>\n" +
                "Click<br>\n" +
                "<a href=\"/sendMessage.html?token="+token+"&action="+MOUSE_CLICK+"&arg1=1\">Left</a><br>" +
                "<a href=\"/sendMessage.html?token="+token+"&action="+MOUSE_CLICK+"&arg1=3\">Middle</a><br>" +
                "<a href=\"/sendMessage.html?token="+token+"&action="+MOUSE_CLICK+"&arg1=2\">Right</a><br>";*/
    }
    
    public static String getPowerPage(int token) {
        return  "        <iframe width=\"0\" height=\"0\" border=\"0\" name=\"dummyframe\" id=\"dummyframe\"></iframe>\n" +
                "        <form action=\"sendMessage.html\" method=\"post\" target=\"dummyframe\">\n" +
                "            <input type=\"hidden\" name=\"token\" value=\""+token+"\" />" +
                "            <input type=\"hidden\" name=\"action\" value=\""+POWER_HIBERNATE+"\" />" +
                "            <button type=\"submit\">Hibernate</button>\n" +
                "        </form>";
    }
    
    public static String getNetflixPage(int token) {
        return  "        <iframe width=\"0\" height=\"0\" border=\"0\" name=\"dummyframe\" id=\"dummyframe\"></iframe>\n" +
                "        <form action=\"sendMessage.html\" method=\"post\" target=\"dummyframe\">\n" +
                "            <input type=\"hidden\" name=\"token\" value=\""+token+"\" />" +
                "            <input type=\"hidden\" name=\"action\" value=\""+NETFLIX_SPACE+"\" />" +
                "            <button type=\"submit\" style=\"height:100px; width:500px\">Space Bar</button>\n" +
                "        </form>" +
                "        <form action=\"sendMessage.html\" method=\"post\" target=\"dummyframe\">\n" +
                "            <input type=\"hidden\" name=\"token\" value=\""+token+"\" />" +
                "            <input type=\"hidden\" name=\"action\" value=\""+NETFLIX_FWD+"\" />" +
                "            <button type=\"submit\" style=\"height:100px; width:500px\">Forward</button>\n" +
                "        </form>" +
                "        <form action=\"sendMessage.html\" method=\"post\" target=\"dummyframe\">\n" +
                "            <input type=\"hidden\" name=\"token\" value=\""+token+"\" />" +
                "            <input type=\"hidden\" name=\"action\" value=\""+NETFLIX_BCK+"\" />" +
                "            <button type=\"submit\" style=\"height:100px; width:500px\">Backward</button>\n" +
                "        </form>" +
                
                "<a href=\"/home.html?token="+token+"\">Back</a><br>\n";
    }
    
    public static String getVirtualJoystick() {
        return  "var VirtualJoystick    = function(opts)\n" +
"{\n" +
"    opts            = opts            || {};\n" +
"    this._container        = opts.container    || document.body;\n" +
"    this._strokeStyle    = opts.strokeStyle    || 'cyan';\n" +
"    this._stickEl        = opts.stickElement    || this._buildJoystickStick();\n" +
"    this._baseEl        = opts.baseElement    || this._buildJoystickBase();\n" +
"    this._mouseSupport    = opts.mouseSupport !== undefined ? opts.mouseSupport : false;\n" +
"    this._stationaryBase    = opts.stationaryBase || false;\n" +
"    this._baseX        = this._stickX = opts.baseX || 0\n" +
"    this._baseY        = this._stickY = opts.baseY || 0\n" +
"    this._limitStickTravel    = opts.limitStickTravel || false\n" +
"    this._stickRadius    = opts.stickRadius !== undefined ? opts.stickRadius : 100\n" +
"    this._useCssTransform    = opts.useCssTransform !== undefined ? opts.useCssTransform : false\n" +
"\n" +
"    this._container.style.position    = \"relative\"\n" +
"\n" +
"    this._container.appendChild(this._baseEl)\n" +
"    this._baseEl.style.position    = \"absolute\"\n" +
"    this._baseEl.style.display    = \"none\"\n" +
"    this._container.appendChild(this._stickEl)\n" +
"    this._stickEl.style.position    = \"absolute\"\n" +
"    this._stickEl.style.display    = \"none\"\n" +
"\n" +
"    this._pressed    = false;\n" +
"    this._touchIdx    = null;\n" +
"    \n" +
"    if(this._stationaryBase === true){\n" +
"        this._baseEl.style.display    = \"\";\n" +
"        this._baseEl.style.left        = (this._baseX - this._baseEl.width /2)+\"px\";\n" +
"        this._baseEl.style.top        = (this._baseY - this._baseEl.height/2)+\"px\";\n" +
"    }\n" +
"    \n" +
"    this._transform    = this._useCssTransform ? this._getTransformProperty() : false;\n" +
"    this._has3d    = this._check3D();\n" +
"    \n" +
"    var __bind    = function(fn, me){ return function(){ return fn.apply(me, arguments); }; };\n" +
"    this._$onTouchStart    = __bind(this._onTouchStart    , this);\n" +
"    this._$onTouchEnd    = __bind(this._onTouchEnd    , this);\n" +
"    this._$onTouchMove    = __bind(this._onTouchMove    , this);\n" +
"    this._container.addEventListener( 'touchstart'    , this._$onTouchStart    , false );\n" +
"    this._container.addEventListener( 'touchend'    , this._$onTouchEnd    , false );\n" +
"    this._container.addEventListener( 'touchmove'    , this._$onTouchMove    , false );\n" +
"    if( this._mouseSupport ){\n" +
"        this._$onMouseDown    = __bind(this._onMouseDown    , this);\n" +
"        this._$onMouseUp    = __bind(this._onMouseUp    , this);\n" +
"        this._$onMouseMove    = __bind(this._onMouseMove    , this);\n" +
"        this._container.addEventListener( 'mousedown'    , this._$onMouseDown    , false );\n" +
"        this._container.addEventListener( 'mouseup'    , this._$onMouseUp    , false );\n" +
"        this._container.addEventListener( 'mousemove'    , this._$onMouseMove    , false );\n" +
"    }\n" +
"}\n" +
"\n" +
"VirtualJoystick.prototype.destroy    = function()\n" +
"{\n" +
"    this._container.removeChild(this._baseEl);\n" +
"    this._container.removeChild(this._stickEl);\n" +
"\n" +
"    this._container.removeEventListener( 'touchstart'    , this._$onTouchStart    , false );\n" +
"    this._container.removeEventListener( 'touchend'        , this._$onTouchEnd    , false );\n" +
"    this._container.removeEventListener( 'touchmove'    , this._$onTouchMove    , false );\n" +
"    if( this._mouseSupport ){\n" +
"        this._container.removeEventListener( 'mouseup'        , this._$onMouseUp    , false );\n" +
"        this._container.removeEventListener( 'mousedown'    , this._$onMouseDown    , false );\n" +
"        this._container.removeEventListener( 'mousemove'    , this._$onMouseMove    , false );\n" +
"    }\n" +
"}\n" +
"\n" +
"/**\n" +
" * @returns {Boolean} true if touchscreen is currently available, false otherwise\n" +
"*/\n" +
"VirtualJoystick.touchScreenAvailable    = function()\n" +
"{\n" +
"    return 'createTouch' in document ? true : false;\n" +
"}\n" +
"\n" +
"/**\n" +
" * microevents.js - https://github.com/jeromeetienne/microevent.js\n" +
"*/\n" +
";(function(destObj){\n" +
"    destObj.addEventListener    = function(event, fct){\n" +
"        if(this._events === undefined)     this._events    = {};\n" +
"        this._events[event] = this._events[event]    || [];\n" +
"        this._events[event].push(fct);\n" +
"        return fct;\n" +
"    };\n" +
"    destObj.removeEventListener    = function(event, fct){\n" +
"        if(this._events === undefined)     this._events    = {};\n" +
"        if( event in this._events === false  )    return;\n" +
"        this._events[event].splice(this._events[event].indexOf(fct), 1);\n" +
"    };\n" +
"    destObj.dispatchEvent        = function(event /* , args... */){\n" +
"        if(this._events === undefined)     this._events    = {};\n" +
"        if( this._events[event] === undefined )    return;\n" +
"        var tmpArray    = this._events[event].slice(); \n" +
"        for(var i = 0; i < tmpArray.length; i++){\n" +
"            var result    = tmpArray[i].apply(this, Array.prototype.slice.call(arguments, 1))\n" +
"            if( result !== undefined )    return result;\n" +
"        }\n" +
"        return undefined\n" +
"    };\n" +
"})(VirtualJoystick.prototype);\n" +
"\n" +
"//////////////////////////////////////////////////////////////////////////////////\n" +
"//                                        //\n" +
"//////////////////////////////////////////////////////////////////////////////////\n" +
"\n" +
"VirtualJoystick.prototype.deltaX    = function(){ return this._stickX - this._baseX;    }\n" +
"VirtualJoystick.prototype.deltaY    = function(){ return this._stickY - this._baseY;    }\n" +
"\n" +
"VirtualJoystick.prototype.up    = function(){\n" +
"    if( this._pressed === false )    return false;\n" +
"    var deltaX    = this.deltaX();\n" +
"    var deltaY    = this.deltaY();\n" +
"    if( deltaY >= 0 )                return false;\n" +
"    if( Math.abs(deltaX) > 2*Math.abs(deltaY) )    return false;\n" +
"    return true;\n" +
"}\n" +
"VirtualJoystick.prototype.down    = function(){\n" +
"    if( this._pressed === false )    return false;\n" +
"    var deltaX    = this.deltaX();\n" +
"    var deltaY    = this.deltaY();\n" +
"    if( deltaY <= 0 )                return false;\n" +
"    if( Math.abs(deltaX) > 2*Math.abs(deltaY) )    return false;\n" +
"    return true;    \n" +
"}\n" +
"VirtualJoystick.prototype.right    = function(){\n" +
"    if( this._pressed === false )    return false;\n" +
"    var deltaX    = this.deltaX();\n" +
"    var deltaY    = this.deltaY();\n" +
"    if( deltaX <= 0 )                return false;\n" +
"    if( Math.abs(deltaY) > 2*Math.abs(deltaX) )    return false;\n" +
"    return true;    \n" +
"}\n" +
"VirtualJoystick.prototype.left    = function(){\n" +
"    if( this._pressed === false )    return false;\n" +
"    var deltaX    = this.deltaX();\n" +
"    var deltaY    = this.deltaY();\n" +
"    if( deltaX >= 0 )                return false;\n" +
"    if( Math.abs(deltaY) > 2*Math.abs(deltaX) )    return false;\n" +
"    return true;    \n" +
"}\n" +
"\n" +
"//////////////////////////////////////////////////////////////////////////////////\n" +
"//                                        //\n" +
"//////////////////////////////////////////////////////////////////////////////////\n" +
"\n" +
"VirtualJoystick.prototype._onUp    = function()\n" +
"{\n" +
"    this._pressed    = false; \n" +
"    this._stickEl.style.display    = \"none\";\n" +
"    \n" +
"    if(this._stationaryBase == false){    \n" +
"        this._baseEl.style.display    = \"none\";\n" +
"    \n" +
"        this._baseX    = this._baseY    = 0;\n" +
"        this._stickX    = this._stickY    = 0;\n" +
"    }\n" +
"}\n" +
"\n" +
"VirtualJoystick.prototype._onDown    = function(x, y)\n" +
"{\n" +
"    this._pressed    = true; \n" +
"    if(this._stationaryBase == false){\n" +
"        this._baseX    = x;\n" +
"        this._baseY    = y;\n" +
"        this._baseEl.style.display    = \"\";\n" +
"        this._move(this._baseEl.style, (this._baseX - this._baseEl.width /2), (this._baseY - this._baseEl.height/2));\n" +
"    }\n" +
"    \n" +
"    this._stickX    = x;\n" +
"    this._stickY    = y;\n" +
"    \n" +
"    if(this._limitStickTravel === true){\n" +
"        var deltaX    = this.deltaX();\n" +
"        var deltaY    = this.deltaY();\n" +
"        var stickDistance = Math.sqrt( (deltaX * deltaX) + (deltaY * deltaY) );\n" +
"        if(stickDistance > this._stickRadius){\n" +
"            var stickNormalizedX = deltaX / stickDistance;\n" +
"            var stickNormalizedY = deltaY / stickDistance;\n" +
"            \n" +
"            this._stickX = stickNormalizedX * this._stickRadius + this._baseX;\n" +
"            this._stickY = stickNormalizedY * this._stickRadius + this._baseY;\n" +
"        }     \n" +
"    }\n" +
"    \n" +
"    this._stickEl.style.display    = \"\";\n" +
"    this._move(this._stickEl.style, (this._stickX - this._stickEl.width /2), (this._stickY - this._stickEl.height/2));    \n" +
"}\n" +
"\n" +
"VirtualJoystick.prototype._onMove    = function(x, y)\n" +
"{\n" +
"    if( this._pressed === true ){\n" +
"        this._stickX    = x;\n" +
"        this._stickY    = y;\n" +
"        \n" +
"        if(this._limitStickTravel === true){\n" +
"            var deltaX    = this.deltaX();\n" +
"            var deltaY    = this.deltaY();\n" +
"            var stickDistance = Math.sqrt( (deltaX * deltaX) + (deltaY * deltaY) );\n" +
"            if(stickDistance > this._stickRadius){\n" +
"                var stickNormalizedX = deltaX / stickDistance;\n" +
"                var stickNormalizedY = deltaY / stickDistance;\n" +
"            \n" +
"                this._stickX = stickNormalizedX * this._stickRadius + this._baseX;\n" +
"                this._stickY = stickNormalizedY * this._stickRadius + this._baseY;\n" +
"            }         \n" +
"        }\n" +
"        \n" +
"            this._move(this._stickEl.style, (this._stickX - this._stickEl.width /2), (this._stickY - this._stickEl.height/2));    \n" +
"    }    \n" +
"}\n" +
"\n" +
"\n" +
"//////////////////////////////////////////////////////////////////////////////////\n" +
"//        bind touch events (and mouse events for debug)            //\n" +
"//////////////////////////////////////////////////////////////////////////////////\n" +
"\n" +
"VirtualJoystick.prototype._onMouseUp    = function(event)\n" +
"{\n" +
"    return this._onUp();\n" +
"}\n" +
"\n" +
"VirtualJoystick.prototype._onMouseDown    = function(event)\n" +
"{\n" +
"    event.preventDefault();\n" +
"    var x    = event.clientX;\n" +
"    var y    = event.clientY;\n" +
"    return this._onDown(x, y);\n" +
"}\n" +
"\n" +
"VirtualJoystick.prototype._onMouseMove    = function(event)\n" +
"{\n" +
"    var x    = event.clientX;\n" +
"    var y    = event.clientY;\n" +
"    return this._onMove(x, y);\n" +
"}\n" +
"\n" +
"//////////////////////////////////////////////////////////////////////////////////\n" +
"//        comment                                //\n" +
"//////////////////////////////////////////////////////////////////////////////////\n" +
"\n" +
"VirtualJoystick.prototype._onTouchStart    = function(event)\n" +
"{\n" +
"    // if there is already a touch inprogress do nothing\n" +
"    if( this._touchIdx !== null )    return;\n" +
"\n" +
"    // notify event for validation\n" +
"    var isValid    = this.dispatchEvent('touchStartValidation', event);\n" +
"    if( isValid === false )    return;\n" +
"    \n" +
"    // dispatch touchStart\n" +
"    this.dispatchEvent('touchStart', event);\n" +
"\n" +
"    event.preventDefault();\n" +
"    // get the first who changed\n" +
"    var touch    = event.changedTouches[0];\n" +
"    // set the touchIdx of this joystick\n" +
"    this._touchIdx    = touch.identifier;\n" +
"\n" +
"    // forward the action\n" +
"    var x        = touch.pageX;\n" +
"    var y        = touch.pageY;\n" +
"    return this._onDown(x, y)\n" +
"}\n" +
"\n" +
"VirtualJoystick.prototype._onTouchEnd    = function(event)\n" +
"{\n" +
"    // if there is no touch in progress, do nothing\n" +
"    if( this._touchIdx === null )    return;\n" +
"\n" +
"    // dispatch touchEnd\n" +
"    this.dispatchEvent('touchEnd', event);\n" +
"\n" +
"    // try to find our touch event\n" +
"    var touchList    = event.changedTouches;\n" +
"    for(var i = 0; i < touchList.length && touchList[i].identifier !== this._touchIdx; i++);\n" +
"    // if touch event isnt found, \n" +
"    if( i === touchList.length)    return;\n" +
"\n" +
"    // reset touchIdx - mark it as no-touch-in-progress\n" +
"    this._touchIdx    = null;\n" +
"\n" +
"//??????\n" +
"// no preventDefault to get click event on ios\n" +
"event.preventDefault();\n" +
"\n" +
"    return this._onUp()\n" +
"}\n" +
"\n" +
"VirtualJoystick.prototype._onTouchMove    = function(event)\n" +
"{\n" +
"    // if there is no touch in progress, do nothing\n" +
"    if( this._touchIdx === null )    return;\n" +
"\n" +
"    // try to find our touch event\n" +
"    var touchList    = event.changedTouches;\n" +
"    for(var i = 0; i < touchList.length && touchList[i].identifier !== this._touchIdx; i++ );\n" +
"    // if touch event with the proper identifier isnt found, do nothing\n" +
"    if( i === touchList.length)    return;\n" +
"    var touch    = touchList[i];\n" +
"\n" +
"    event.preventDefault();\n" +
"\n" +
"    var x        = touch.pageX;\n" +
"    var y        = touch.pageY;\n" +
"    return this._onMove(x, y)\n" +
"}\n" +
"\n" +
"\n" +
"//////////////////////////////////////////////////////////////////////////////////\n" +
"//        build default stickEl and baseEl                //\n" +
"//////////////////////////////////////////////////////////////////////////////////\n" +
"\n" +
"/**\n" +
" * build the canvas for joystick base\n" +
" */\n" +
"VirtualJoystick.prototype._buildJoystickBase    = function()\n" +
"{\n" +
"    var canvas    = document.createElement( 'canvas' );\n" +
"    canvas.width    = 126;\n" +
"    canvas.height    = 126;\n" +
"    \n" +
"    var ctx        = canvas.getContext('2d');\n" +
"    ctx.beginPath(); \n" +
"    ctx.strokeStyle = this._strokeStyle; \n" +
"    ctx.lineWidth    = 6; \n" +
"    ctx.arc( canvas.width/2, canvas.width/2, 40, 0, Math.PI*2, true); \n" +
"    ctx.stroke();    \n" +
"\n" +
"    ctx.beginPath(); \n" +
"    ctx.strokeStyle    = this._strokeStyle; \n" +
"    ctx.lineWidth    = 2; \n" +
"    ctx.arc( canvas.width/2, canvas.width/2, 60, 0, Math.PI*2, true); \n" +
"    ctx.stroke();\n" +
"    \n" +
"    return canvas;\n" +
"}\n" +
"\n" +
"/**\n" +
" * build the canvas for joystick stick\n" +
" */\n" +
"VirtualJoystick.prototype._buildJoystickStick    = function()\n" +
"{\n" +
"    var canvas    = document.createElement( 'canvas' );\n" +
"    canvas.width    = 86;\n" +
"    canvas.height    = 86;\n" +
"    var ctx        = canvas.getContext('2d');\n" +
"    ctx.beginPath(); \n" +
"    ctx.strokeStyle    = this._strokeStyle; \n" +
"    ctx.lineWidth    = 6; \n" +
"    ctx.arc( canvas.width/2, canvas.width/2, 40, 0, Math.PI*2, true); \n" +
"    ctx.stroke();\n" +
"    return canvas;\n" +
"}\n" +
"\n" +
"//////////////////////////////////////////////////////////////////////////////////\n" +
"//        move using translate3d method with fallback to translate > 'top' and 'left'        \n" +
"//      modified from https://github.com/component/translate and dependents\n" +
"//////////////////////////////////////////////////////////////////////////////////\n" +
"\n" +
"VirtualJoystick.prototype._move = function(style, x, y)\n" +
"{\n" +
"    if (this._transform) {\n" +
"        if (this._has3d) {\n" +
"            style[this._transform] = 'translate3d(' + x + 'px,' + y + 'px, 0)';\n" +
"        } else {\n" +
"            style[this._transform] = 'translate(' + x + 'px,' + y + 'px)';\n" +
"        }\n" +
"    } else {\n" +
"        style.left = x + 'px';\n" +
"        style.top = y + 'px';\n" +
"    }\n" +
"}\n" +
"\n" +
"VirtualJoystick.prototype._getTransformProperty = function() \n" +
"{\n" +
"    var styles = [\n" +
"        'webkitTransform',\n" +
"        'MozTransform',\n" +
"        'msTransform',\n" +
"        'OTransform',\n" +
"        'transform'\n" +
"    ];\n" +
"\n" +
"    var el = document.createElement('p');\n" +
"    var style;\n" +
"\n" +
"    for (var i = 0; i < styles.length; i++) {\n" +
"        style = styles[i];\n" +
"        if (null != el.style[style]) {\n" +
"            return style;\n" +
"        }\n" +
"    }         \n" +
"}\n" +
"  \n" +
"VirtualJoystick.prototype._check3D = function() \n" +
"{        \n" +
"    var prop = this._getTransformProperty();\n" +
"    // IE8<= doesn't have `getComputedStyle`\n" +
"    if (!prop || !window.getComputedStyle) return module.exports = false;\n" +
"\n" +
"    var map = {\n" +
"        webkitTransform: '-webkit-transform',\n" +
"        OTransform: '-o-transform',\n" +
"        msTransform: '-ms-transform',\n" +
"        MozTransform: '-moz-transform',\n" +
"        transform: 'transform'\n" +
"    };\n" +
"\n" +
"    // from: https://gist.github.com/lorenzopolidori/3794226\n" +
"    var el = document.createElement('div');\n" +
"    el.style[prop] = 'translate3d(1px,1px,1px)';\n" +
"    document.body.insertBefore(el, null);\n" +
"    var val = getComputedStyle(el).getPropertyValue(map[prop]);\n" +
"    document.body.removeChild(el);\n" +
"    var exports = null != val && val.length && 'none' != val;\n" +
"    return exports;\n" +
"}";
    }
    
    
    public static final int VOLUME_SET  = 1; //                   [percent]
    public static final int VOLUME_CHANGE  = 2; //                [percent]

    public static final int POWER_HIBERNATE  = 3; //              []

    public static final int MOUSE_MOVEBY  = 10; //                [pixel Pos, pixel Pos]
    public static final int MOUSE_MOVETO  = 11; //                [perecnt Pos, perecnt Pos]

    public static final int NETFLIX_SPACE  = 20; //                []
    public static final int NETFLIX_FWD  = 21; //                []
    public static final int NETFLIX_BCK  = 22; //                []
    
    public static final int MOUSE_CLICK  = 4; //                  [mouse button]
    
    private static boolean doAction(HashMap<String, String> args) {
        switch(Integer.parseInt(args.get("action"))) {
            case VOLUME_SET:
                //PCFunctions.volumeSet(Integer.parseInt(args.get("arg1")));
                break;
            case VOLUME_CHANGE:
                //PCFunctions.volumeChange(Integer.parseInt(args.get("arg1")));
                break;

            case POWER_HIBERNATE:
                //PCFunctions.computerHibernate();
                break;

            case NETFLIX_SPACE:
                //PCFunctions.sendKey(KeyEvent.VK_SPACE);
                break;
            case NETFLIX_FWD:
                //PCFunctions.sendKey(KeyEvent.VK_RIGHT);
                break;
            case NETFLIX_BCK:
                //PCFunctions.sendKey(KeyEvent.VK_LEFT);
                break;

            case MOUSE_MOVEBY:
                /*PCFunctions.mouseMoveBy(
                        Double.parseDouble(args.get("arg1")),
                        Double.parseDouble(args.get("arg2")));*/
                return false;
            case MOUSE_MOVETO:
                /*PCFunctions.mouseMoveTo(
                        Integer.parseInt(args.get("arg1")),
                        Integer.parseInt(args.get("arg2")));*/
                break;
                
            case MOUSE_CLICK:
                //PCFunctions.mouseClick(Integer.parseInt(args.get("arg1")));
                break;
        }
        return true;
    }
    
}
