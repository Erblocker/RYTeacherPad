package com.netspace.library.servers;

import android.net.http.Headers;
import android.support.v4.media.session.PlaybackStateCompat;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HTTP;

public class NanoHTTPD {
    public static final String HTTP_BADREQUEST = "400 Bad Request";
    public static final String HTTP_FORBIDDEN = "403 Forbidden";
    public static final String HTTP_INTERNALERROR = "500 Internal Server Error";
    public static final String HTTP_NOTFOUND = "404 Not Found";
    public static final String HTTP_NOTIMPLEMENTED = "501 Not Implemented";
    public static final String HTTP_OK = "200 OK";
    public static final String HTTP_REDIRECT = "301 Moved Permanently";
    private static final String LICENCE = "Copyright (C) 2001,2005-2008 by Jarno Elonen <elonen@iki.fi>\n\nRedistribution and use in source and binary forms, with or without\nmodification, are permitted provided that the following conditions\nare met:\n\nRedistributions of source code must retain the above copyright notice,\nthis list of conditions and the following disclaimer. Redistributions in\nbinary form must reproduce the above copyright notice, this list of\nconditions and the following disclaimer in the documentation and/or other\nmaterials provided with the distribution. The name of the author may not\nbe used to endorse or promote products derived from this software without\nspecific prior written permission. \n \nTHIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR\nIMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\nOF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\nIN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,\nINCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\nNOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,\nDATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY\nTHEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\nOF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";
    public static final String MIME_DEFAULT_BINARY = "application/octet-stream";
    public static final String MIME_HTML = "text/html";
    public static final String MIME_PLAINTEXT = "text/plain";
    private static SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
    private static Hashtable theMimeTypes = new Hashtable();
    File myFileDir;
    private int myTcpPort;
    private boolean runThread = true;

    private class HTTPSession implements Runnable {
        private Socket mySocket;

        public HTTPSession(Socket s) {
            this.mySocket = s;
            try {
                InetAddress localhost = InetAddress.getByName("127.0.0.1");
            } catch (Exception e) {
            }
            Thread t = new Thread(this);
            t.setDaemon(true);
            t.start();
        }

        public void run() {
            try {
                InputStream is = this.mySocket.getInputStream();
                if (is != null) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(is));
                    String szReadLine = in.readLine();
                    if (szReadLine != null) {
                        StringTokenizer st = new StringTokenizer(szReadLine);
                        if (!st.hasMoreTokens()) {
                            sendError("400 Bad Request", "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
                        }
                        String method = st.nextToken();
                        if (!st.hasMoreTokens()) {
                            sendError("400 Bad Request", "BAD REQUEST: Missing URI. Usage: GET /example/file.html");
                        }
                        String uri = st.nextToken();
                        Properties parms = new Properties();
                        int qmi = uri.indexOf(63);
                        if (qmi >= 0) {
                            decodeParms(uri.substring(qmi + 1), parms);
                            uri = decodePercent(uri.substring(0, qmi));
                        } else {
                            uri = decodePercent(uri);
                        }
                        Properties header = new Properties();
                        if (st.hasMoreTokens()) {
                            for (String line = in.readLine(); line.trim().length() > 0; line = in.readLine()) {
                                int p = line.indexOf(58);
                                header.put(line.substring(0, p).trim().toLowerCase(), line.substring(p + 1).trim());
                            }
                        }
                        if (method.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
                            long size = Long.MAX_VALUE;
                            String contentLength = header.getProperty(Headers.CONTENT_LEN);
                            if (contentLength != null) {
                                try {
                                    size = (long) Integer.parseInt(contentLength);
                                } catch (NumberFormatException e) {
                                }
                            }
                            String postLine = "";
                            char[] buf = new char[512];
                            int read = in.read(buf);
                            while (read >= 0 && size > 0 && !postLine.endsWith("\r\n")) {
                                size -= (long) read;
                                postLine = new StringBuilder(String.valueOf(postLine)).append(String.valueOf(buf, 0, read)).toString();
                                if (size > 0) {
                                    read = in.read(buf);
                                }
                            }
                            decodeParms(postLine.trim(), parms);
                        }
                        Response r = NanoHTTPD.this.serve(uri, method, header, parms);
                        if (r == null) {
                            sendError("500 Internal Server Error", "SERVER INTERNAL ERROR: Serve() returned a null response.");
                        } else {
                            sendResponse(r.status, r.mimeType, r.header, r.data);
                        }
                        in.close();
                    }
                }
            } catch (IOException ioe) {
                sendError("500 Internal Server Error", "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            } catch (InterruptedException e2) {
            } catch (Throwable th) {
            }
        }

        private String decodePercent(String str) throws InterruptedException {
            try {
                StringBuffer sb = new StringBuffer();
                int i = 0;
                while (i < str.length()) {
                    char c = str.charAt(i);
                    switch (c) {
                        case '%':
                            sb.append((char) Integer.parseInt(str.substring(i + 1, i + 3), 16));
                            i += 2;
                            break;
                        case '+':
                            sb.append(' ');
                            break;
                        default:
                            sb.append(c);
                            break;
                    }
                    i++;
                }
                return new String(sb.toString().getBytes());
            } catch (Exception e) {
                sendError("400 Bad Request", "BAD REQUEST: Bad percent-encoding.");
                return null;
            }
        }

        private void decodeParms(String parms, Properties p) throws InterruptedException {
            if (parms != null) {
                StringTokenizer st = new StringTokenizer(parms, "&");
                while (st.hasMoreTokens()) {
                    String e = st.nextToken();
                    int sep = e.indexOf(61);
                    if (sep >= 0) {
                        p.put(decodePercent(e.substring(0, sep)).trim(), decodePercent(e.substring(sep + 1)));
                    }
                }
            }
        }

        private void sendError(String status, String msg) throws InterruptedException {
            sendResponse(status, "text/plain", null, new ByteArrayInputStream(msg.getBytes()));
            throw new InterruptedException();
        }

        private void sendResponse(String status, String mime, Properties header, InputStream data) {
            if (status == null) {
                try {
                    throw new Error("sendResponse(): Status can't be null.");
                } catch (IOException e) {
                    this.mySocket.close();
                    return;
                } catch (Throwable th) {
                    return;
                }
            }
            OutputStream out = this.mySocket.getOutputStream();
            PrintWriter pw = new PrintWriter(out);
            pw.print("HTTP/1.0 " + status + " \r\n");
            if (mime != null) {
                pw.print("Content-Type: " + mime + "\r\n");
            }
            if (header == null || header.getProperty(HTTP.DATE_HEADER) == null) {
                pw.print("Date: " + NanoHTTPD.gmtFrmt.format(new Date()) + "\r\n");
            }
            if (header != null) {
                Enumeration e2 = header.keys();
                while (e2.hasMoreElements()) {
                    String key = (String) e2.nextElement();
                    pw.print(new StringBuilder(String.valueOf(key)).append(": ").append(header.getProperty(key)).append("\r\n").toString());
                }
            }
            pw.print("\r\n");
            pw.flush();
            if (data != null) {
                byte[] buff = new byte[2048];
                while (true) {
                    int read = data.read(buff, 0, 2048);
                    if (read <= 0) {
                        break;
                    }
                    out.write(buff, 0, read);
                }
            }
            out.flush();
            out.close();
            if (data != null) {
                data.close();
            }
        }
    }

    public class Response {
        public InputStream data;
        public Properties header;
        public String mimeType;
        public String status;

        public Response() {
            this.header = new Properties();
            this.status = "200 OK";
        }

        public Response(String status, String mimeType, InputStream data) {
            this.header = new Properties();
            this.status = status;
            this.mimeType = mimeType;
            this.data = data;
        }

        public Response(String status, String mimeType, String txt) {
            this.header = new Properties();
            this.status = status;
            this.mimeType = mimeType;
            this.data = new ByteArrayInputStream(txt.getBytes());
        }

        public void addHeader(String name, String value) {
            this.header.put(name, value);
        }
    }

    public Response serve(String uri, String method, Properties header, Properties parms) {
        System.out.println(new StringBuilder(String.valueOf(method)).append(" '").append(uri).append("' ").toString());
        Enumeration e = header.propertyNames();
        while (e.hasMoreElements()) {
            String value = (String) e.nextElement();
            System.out.println("  HDR: '" + value + "' = '" + header.getProperty(value) + "'");
        }
        e = parms.propertyNames();
        while (e.hasMoreElements()) {
            value = (String) e.nextElement();
            System.out.println("  PRM: '" + value + "' = '" + parms.getProperty(value) + "'");
        }
        return serveFile(uri, header, new File("/sdcard"), true);
    }

    public NanoHTTPD(int port) throws IOException {
        this.myTcpPort = port;
        final ServerSocket ss = new ServerSocket(this.myTcpPort);
        Thread t = new Thread(new Runnable() {
            public void run() {
                while (NanoHTTPD.this.runThread) {
                    try {
                        HTTPSession hTTPSession = new HTTPSession(ss.accept());
                    } catch (IOException e) {
                        return;
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void stop() {
        this.runThread = false;
    }

    public static void main(String[] args) {
        System.out.println("NanoHTTPD 1.11 (C) 2001,2005-2008 Jarno Elonen\n(Command line options: [port] [--licence])\n");
        int lopt = -1;
        for (int i = 0; i < args.length; i++) {
            if (args[i].toLowerCase().endsWith("licence")) {
                lopt = i;
                System.out.println("Copyright (C) 2001,2005-2008 by Jarno Elonen <elonen@iki.fi>\n\nRedistribution and use in source and binary forms, with or without\nmodification, are permitted provided that the following conditions\nare met:\n\nRedistributions of source code must retain the above copyright notice,\nthis list of conditions and the following disclaimer. Redistributions in\nbinary form must reproduce the above copyright notice, this list of\nconditions and the following disclaimer in the documentation and/or other\nmaterials provided with the distribution. The name of the author may not\nbe used to endorse or promote products derived from this software without\nspecific prior written permission. \n \nTHIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR\nIMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\nOF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\nIN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,\nINCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\nNOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,\nDATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY\nTHEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\nOF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n");
            }
        }
        int port = 80;
        if (args.length > 0 && lopt != 0) {
            port = Integer.parseInt(args[0]);
        }
        if (args.length > 1 && args[1].toLowerCase().endsWith("licence")) {
            System.out.println("Copyright (C) 2001,2005-2008 by Jarno Elonen <elonen@iki.fi>\n\nRedistribution and use in source and binary forms, with or without\nmodification, are permitted provided that the following conditions\nare met:\n\nRedistributions of source code must retain the above copyright notice,\nthis list of conditions and the following disclaimer. Redistributions in\nbinary form must reproduce the above copyright notice, this list of\nconditions and the following disclaimer in the documentation and/or other\nmaterials provided with the distribution. The name of the author may not\nbe used to endorse or promote products derived from this software without\nspecific prior written permission. \n \nTHIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR\nIMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\nOF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\nIN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,\nINCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\nNOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,\nDATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY\nTHEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\nOF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n");
        }
        NanoHTTPD nh = null;
        try {
            nh = new NanoHTTPD(port);
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
            System.exit(-1);
        }
        nh.myFileDir = new File("");
        System.out.println("Now serving files in port " + port + " from \"" + new File("").getAbsolutePath() + "\"");
        System.out.println("Hit Enter to stop.\n");
        try {
            System.in.read();
        } catch (Throwable th) {
        }
    }

    private String encodeUri(String uri) {
        String newUri = "";
        StringTokenizer st = new StringTokenizer(uri, "/ ", true);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equals("/")) {
                newUri = new StringBuilder(String.valueOf(newUri)).append("/").toString();
            } else if (tok.equals(" ")) {
                newUri = new StringBuilder(String.valueOf(newUri)).append("%20").toString();
            } else {
                newUri = new StringBuilder(String.valueOf(newUri)).append(URLEncoder.encode(tok)).toString();
            }
        }
        return newUri;
    }

    public Response serveFile(String uri, Properties header, File homeDir, boolean allowDirectoryListing) {
        if (!homeDir.isDirectory()) {
            return new Response("500 Internal Server Error", "text/plain", "INTERNAL ERRROR: serveFile(): given homeDir is not a directory.");
        }
        uri = uri.trim().replace(File.separatorChar, '/');
        if (uri.indexOf(63) >= 0) {
            uri = uri.substring(0, uri.indexOf(63));
        }
        if (uri.startsWith("..") || uri.endsWith("..") || uri.indexOf("../") >= 0) {
            return new Response(HTTP_FORBIDDEN, "text/plain", "FORBIDDEN: Won't serve ../ for security reasons.");
        }
        File f = new File(homeDir, uri);
        if (!f.exists()) {
            return new Response("404 Not Found", "text/plain", "Error 404, file not found.");
        }
        if (f.isDirectory()) {
            if (!uri.endsWith("/")) {
                uri = new StringBuilder(String.valueOf(uri)).append("/").toString();
                Response response = new Response(HTTP_REDIRECT, MIME_HTML, "<html><body>Redirected: <a href=\"" + uri + "\">" + uri + "</a></body></html>");
                response.addHeader("Location", uri);
                return response;
            } else if (new File(f, "index.html").exists()) {
                f = new File(homeDir, new StringBuilder(String.valueOf(uri)).append("/index.html").toString());
            } else if (new File(f, "index.htm").exists()) {
                f = new File(homeDir, new StringBuilder(String.valueOf(uri)).append("/index.htm").toString());
            } else if (!allowDirectoryListing) {
                return new Response(HTTP_FORBIDDEN, "text/plain", "FORBIDDEN: No directory listing.");
            } else {
                Hashtable styles = new Hashtable();
                Hashtable style = new Hashtable();
                style.put("background", "#181818");
                style.put("color", "#dddddd");
                style.put("padding-top", "10px");
                styles.put(".localhttpd", style);
                style = new Hashtable();
                style.put("color", "#ff3333");
                styles.put(".localhttpd a", style);
                style = new Hashtable();
                style.put("color", "#ff6666");
                styles.put(".localhttpd a:hover", style);
                style = new Hashtable();
                style.put("margin", "0px 0px 5px 0px");
                style.put("padding", "0px");
                style.put("font-size", "16px");
                styles.put(".localhttpd h1", style);
                String header_block = "<head><style>";
                Enumeration e = styles.keys();
                while (e.hasMoreElements()) {
                    String key = (String) e.nextElement();
                    Hashtable item = (Hashtable) styles.get(key);
                    header_block = new StringBuilder(String.valueOf(header_block)).append(key).append("{\n").toString();
                    Enumeration e2 = item.keys();
                    while (e2.hasMoreElements()) {
                        String skey = (String) e2.nextElement();
                        header_block = new StringBuilder(String.valueOf(header_block)).append(skey).append(":").append(item.get(skey)).append(";\n").toString();
                    }
                    header_block = new StringBuilder(String.valueOf(header_block)).append(key).append("}\n").toString();
                }
                header_block = new StringBuilder(String.valueOf(header_block)).append("</style><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head>").toString();
                String[] files = f.list();
                String msg = "<html>" + header_block + "<body class=\"localhttpd\"><h1>Directory " + uri + "</h1>";
                if (uri.length() > 1) {
                    String u = uri.substring(0, uri.length() - 1);
                    int slash = u.lastIndexOf(47);
                    if (slash >= 0 && slash < u.length()) {
                        msg = new StringBuilder(String.valueOf(msg)).append("<b><a href=\"").append(uri.substring(0, slash + 1)).append("\">..</a></b><br/>").toString();
                    }
                }
                for (int i = 0; i < files.length; i++) {
                    File curFile = new File(f, files[i]);
                    boolean dir = curFile.isDirectory();
                    if (dir) {
                        msg = new StringBuilder(String.valueOf(msg)).append("<b>").toString();
                        files[i] = files[i] + "/";
                    }
                    String extra = "";
                    if (curFile.isFile()) {
                        extra = "target='_blank'";
                    }
                    msg = new StringBuilder(String.valueOf(msg)).append("<a ").append(extra).append(" href=\"").append(encodeUri(new StringBuilder(String.valueOf(uri)).append(files[i]).toString())).append("\">").append(files[i]).append("</a>").toString();
                    if (curFile.isFile()) {
                        long len = curFile.length();
                        msg = new StringBuilder(String.valueOf(msg)).append(" &nbsp;<font size=2>(").toString();
                        if (len < PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) {
                            msg = new StringBuilder(String.valueOf(msg)).append(curFile.length()).append(" bytes").toString();
                        } else if (len < 1048576) {
                            msg = new StringBuilder(String.valueOf(msg)).append(curFile.length() / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID).append(".").append(((curFile.length() % PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) / 10) % 100).append(" KB").toString();
                        } else {
                            msg = new StringBuilder(String.valueOf(msg)).append(curFile.length() / 1048576).append(".").append(((curFile.length() % 1048576) / 10) % 100).append(" MB").toString();
                        }
                        msg = new StringBuilder(String.valueOf(msg)).append(")</font>").toString();
                    }
                    msg = new StringBuilder(String.valueOf(msg)).append("<br/>").toString();
                    if (dir) {
                        msg = new StringBuilder(String.valueOf(msg)).append("</b>").toString();
                    }
                }
                return new Response("200 OK", MIME_HTML, msg);
            }
        }
        String mime = null;
        try {
            int dot = f.getCanonicalPath().lastIndexOf(46);
            if (dot >= 0) {
                mime = (String) theMimeTypes.get(f.getCanonicalPath().substring(dot + 1).toLowerCase());
            }
            if (mime == null) {
                mime = "application/octet-stream";
            }
            long startFrom = 0;
            String range = header.getProperty("Range");
            if (range == null) {
                range = header.getProperty("range");
            }
            if (range != null && range.startsWith("bytes=")) {
                range = range.substring("bytes=".length());
                int minus = range.indexOf(45);
                if (minus > 0) {
                    range = range.substring(0, minus);
                }
                try {
                    startFrom = Long.parseLong(range);
                } catch (NumberFormatException e3) {
                }
            }
            InputStream fis = new FileInputStream(f);
            fis.skip(startFrom);
            response = new Response("200 OK", mime, fis);
            response.addHeader("Content-length", (f.length() - startFrom));
            response.addHeader("Content-range", startFrom + "-" + (f.length() - 1) + "/" + f.length());
            return response;
        } catch (IOException e4) {
            return new Response(HTTP_FORBIDDEN, "text/plain", "FORBIDDEN: Reading file failed.");
        }
    }

    static {
        StringTokenizer st = new StringTokenizer("htm\t\ttext/html html\t\ttext/html txt\t\ttext/plain asc\t\ttext/plain gif\t\timage/gif jpg\t\timage/jpeg jpeg\t\timage/jpeg png\t\timage/png mp3\t\taudio/mpeg m3u\t\taudio/mpeg-url pdf\t\tapplication/pdf doc\t\tapplication/msword ogg\t\tapplication/x-ogg zip\t\tapplication/octet-stream exe\t\tapplication/octet-stream class\t\tapplication/octet-stream ");
        while (st.hasMoreTokens()) {
            theMimeTypes.put(st.nextToken(), st.nextToken());
        }
        gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
}
