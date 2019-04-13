function getCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for(var i = 0; i <ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}
function delCookie(cname) {
    document.cookie = cname+"=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
}
function setCookie(cname, cvalue, expireDays) {
      var d = new Date();
      d.setTime(d.getTime() + (expireDays*24*60*60*1000));
      var expires = "expires="+ d.toUTCString();
      document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}
function fillPageWithToken() {
    var links = document.getElementsByTagName("a");
    var token = getCookie("everythingbridgetoken");
    var re = new RegExp("0twtcht4m"+"token"+"0thv303c");
    for(var i=0; i<links.length; i++) {
        links[i].href = links[i].href.replace(re, token);
    }
}
function logout() {
    var xhr = new XMLHttpRequest();
    xhr.open('PUT', "/logout?token="+getCookie("everythingbridgetoken"), true);
    xhr.send();
    xhr.onreadystatechange = function (e) {
        if (xhr.readyState == 4 && xhr.status == 200) {
            delCookie("everythingbridgetoken");
            window.location.replace("/login.html");
        }
    };
}