package com.ape.transfer.util;

import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import com.ape.transfer.App;
import com.ape.transfer.BuildConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by Mikhael LOPEZ on 14/12/2015.
 */
public class AndroidWebServer extends NanoHTTPD {
    private static final String APK_NAME = "ApeTransfer";
    private static final String INDEX = "<!DOCTYPE HTML>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta http-equiv='Content-Type' content='text/html; charset=utf-8'>\n" +
            "    <title>%1$s</title>\n" +
            "    <meta name=viewport content=width=device-width, initial-scale=1.0,\n" +
            "          maximum-scale=1.0user-scalable=0>\n" +
            "    <script type=text/javascript> function download(uri){window.location.href=uri;}</script>\n" +
            "    <style>body{overflow: hidden;background-color:#F4F4F4;}.iwrap{width:100%;margin:0\n" +
            "        auto;text-align:center;}a,input,button{ outline:none;\n" +
            "        }::-moz-focus-inner{border:0px;}.logo{margin-top: 100px;font-size: 50px;color:\n" +
            "        #31c27c;}.prompt{margin-top: 45px;color: #818181;font-size: 20px;}button{margin-top:\n" +
            "        30px;border:#31c27c solid 1px;background-color: #F4F4F4;padding: 5px 5px;text-align:\n" +
            "        center;}:hover{background-color: #cdeadc;}.title {padding: 10px;float: left;color:\n" +
            "        #31c27c;font-weight: normal;font-size: 20px;text-transform: uppercase;}.version {padding:\n" +
            "        10px;float: left;color: #818181;font-weight: normal;font-size: 20px;text-transform:\n" +
            "        uppercase;}\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "<div class='iwrap fix'>\n" +
            "    <div class='logo' style='clear:both;'>%2$s</div>\n" +
            "    <div class='prompt' style=clear:both;>%3$s</div>\n" +
            "    <div style=clear:both;></div>\n" +
            "    <button onclick=download('/%5$s');>\n" +
            "        <div class='title'>%6$s</div>\n" +
            "        <div class='version'>%7$s</div>\n" +
            "    </button>\n" +
            "</div>\n" +
            "</body>\n" +
            "</html>";

    public AndroidWebServer(int port) {
        super(port);
    }

    public AndroidWebServer(String hostname, int port) {
        super(hostname, port);
    }

    @Override
    public Response serve(IHTTPSession session) {
       /* String msg = "<html><body><h1>Hello server</h1>";
        Map<String, String> parms = session.getParms();
        if (parms.get("username") == null) {
            msg += "<form action='?' method='get'>  <p>Your name: <input type='text' name='username'></p>" + "</form>";
        } else {
            msg += "<p>Hello, " + parms.get("username") + "!</p>";
        }
        return newFixedLengthResponse( msg + "</body></html>" );*/
//        String answer = "";
//        try {
//            // Open file from SD Card
//            File root = Environment.getExternalStorageDirectory();
//            FileReader index = new FileReader(root.getAbsolutePath() + File.separator +
//                    "index.html");
//                    BufferedReader reader = new BufferedReader(index);
//            String line = "";
//            while ((line = reader.readLine()) != null) {
//                answer += line;
//            }
//        } catch(IOException ioe) {
//            Log.w("Httpd", ioe.toString());
//        }
//
//        return newFixedLengthResponse(answer);


        Log.d("Httpd", "client ip = " + session.getHeaders().get("remote-addr"));

        // 默认传入的url是以“/”开头的，需要删除掉，否则就变成了绝对路径
        String fileName = session.getUri().substring(1);
        Log.d("Httpd", "fileName = " + fileName);
        // 默认的页面名称设定为index.html
        if (fileName.equalsIgnoreCase("")) {
            //fileName = "index.html";
            //fileName = "t-share-files.html";
            return responseIndex();
        } else if (TextUtils.equals(fileName, "ApeTransfer.apk")) {
            try {
                String filepath = App.getContext().getPackageManager()
                        .getApplicationInfo(BuildConfig.APPLICATION_ID, 0).sourceDir;
                File file = new File(filepath);
                Log.d("Httpd", "apk patch = " + filepath);
                FileInputStream fileInputStream = new FileInputStream(file);

                return newFixedLengthResponse(Response.Status.OK, "application/apk", fileInputStream, file.length());
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Httpd", "e = " + e.getMessage());
            }
            return response404(session.getUri());
        }

        try {
            //通过AssetManager直接打开文件进行读取操作
            InputStream inputStream = App.getContext().getAssets().open(fileName,
                    AssetManager.ACCESS_BUFFER);

            byte[] buffer = new byte[inputStream.available()];

            inputStream.read(buffer);
            inputStream.close();
            return newFixedLengthResponse(new String(buffer));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response404(session.getUri());
    }

    public Response response404(String url) {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("Sorry, Can't Found " + url + " !");
        builder.append("</body></html>");
        return newFixedLengthResponse(builder.toString());
    }

    public Response responseIndex() {
//        String msg = String.format(INDEX, "互传-零流量下载", "互传", "零流量，下载文件", APK_NAME, "下 载", BuildConfig.VERSION_NAME);
//        return newFixedLengthResponse(msg);
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><head>");
        builder.append("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
        builder.append("<title>" + "互传-零流量下载" + "</title>");
        builder.append("<meta name=viewport content=width=device-width, initial-scale=1.0," +
                "          maximum-scale=1.0user-scalable=0>" +
                "    <script type=text/javascript> function download(uri){window.location.href=uri;}</script>" +
                "    <style>body{overflow: hidden;background-color:#F4F4F4;}.iwrap{width:100%;margin:0" +
                "        auto;text-align:center;}a,input,button{ outline:none;" +
                "        }::-moz-focus-inner{border:0px;}.logo{margin-top: 100px;font-size: 50px;color:" +
                "        #31c27c;}.prompt{margin-top: 45px;color: #818181;font-size: 20px;}button{margin-top:" +
                "        30px;border:#31c27c solid 1px;background-color: #F4F4F4;padding: 5px 5px;text-align:" +
                "        center;}:hover{background-color: #cdeadc;}.title {padding: 10px;float: left;color:" +
                "        #31c27c;font-weight: normal;font-size: 20px;text-transform: uppercase;}.version {padding:" +
                "        10px;float: left;color: #818181;font-weight: normal;font-size: 20px;text-transform:" +
                "        uppercase;}" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "<div class='iwrap fix'>");
        builder.append("<div class='logo' style='clear:both;'>" + "互传" + "</div>");
        builder.append("<div class='prompt' style='clear:both;'>" + "零流量，下载文件" + "</div>");
        builder.append("<div style=clear:both;></div>");
        builder.append("<button onclick=download('/ApeTransfer.apk');>");
        builder.append("<div class='title'>" + "下 载" + "</div>");
        builder.append("<div class='version'>" + BuildConfig.VERSION_NAME + "</div>");
        builder.append("</button></div></body></html>");
        return newFixedLengthResponse(builder.toString());
    }
}
