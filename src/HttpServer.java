import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {

    private HashMap<String,Servlet> servletPool = new HashMap<>();//Servlet 容器
    public static class runtimeException extends RuntimeException{
        public runtimeException() {
            super();
        }

        public runtimeException(String message) {
            super(message);
        }

        public runtimeException(String message, Throwable cause) {
            super(message, cause);
        }

        public runtimeException(Throwable cause) {
            super(cause);
        }

        protected runtimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
    public HttpServer(int port) throws IOException {
        if (port<1 || port>65535) {
            throw new runtimeException("端口错误");
        }
        ServerSocket serverSocket = new ServerSocket(port);
        ExecutorService pool = Executors.newFixedThreadPool(50);//线程池
        System.out.println("服务器启动成功,端口号为："+port);
        while (true){
            Socket clientSocket = serverSocket.accept();

            if (clientSocket != null && !clientSocket.isClosed()){
                Runnable r = () -> {
                    try{
                        acceptToClient(clientSocket);
                    } catch (IOException e){
                        e.printStackTrace();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                };
                pool.submit(r);

            }


        }

    }

    /**
     * 获取对应的servlet(fanshe)
     * @param servletName
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
      private  synchronized Servlet getServlet(String servletName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
          servletName = servletName.substring(0,1).toUpperCase() + servletName.substring(1) + "Servlet";
        if (servletPool.containsKey(servletName)) {
            return servletPool.get(servletName);
        }
        Class servletClass = Class.forName(servletName);
        Servlet servlet = (Servlet) servletClass.newInstance();
        servletPool.put(servletName,servlet);
        return servlet;
      }
    private  void acceptToClient(Socket clientSocket) throws IOException, InterruptedException {
        InputStream clientIn = clientSocket.getInputStream();
        OutputStream clientOut = clientSocket.getOutputStream();
        if (clientIn.available() == 0){
            writeToclient(clientOut,"text/html",200,"OK","<h1>ok</h1>".getBytes());

            return;
        }
        byte[] requestBuff = new byte[clientIn.available()];//构造读取客户端请求数据二进制
        clientIn.read(requestBuff);
        String requestStr = new String(requestBuff);
        String firstLine = requestStr.split("\r\n")[0];
        String requestUrl = firstLine.split(" ")[1];
        /**
         * 动态页面
         */
        if (requestUrl.indexOf("/servlet") !=-1) {
//            System.out.println(requestUrl);
             String servletName = null;
             if (requestUrl.indexOf("?") != -1) {
                 servletName = requestUrl.substring(requestUrl.indexOf("/servlet") + "/servlet/".length(),requestUrl.indexOf("?"));
             } else {
                 servletName = requestUrl.substring(requestUrl.indexOf("/servlet") + "/servlet/".length());

             }
             servletName = servletName.trim();//分割空格
            if (servletName == null || servletName.equals("")){
                writeToclient(clientOut,"text/html",404,"Not Found","<h1>该文件找不到哦</h1>".getBytes());
                return;
            }
            //存在，获取
            try {
                Servlet servlet = getServlet(servletName);
                String content = servlet.doRequest(requestUrl,requestStr);//servlet 的 content
                writeToclient(clientOut,"text/html",200,"OK",content.getBytes());

            } catch (ClassNotFoundException e) {
                writeToclient(clientOut,"text/html",404,"Not Found","<h1>该文件找不到哦</h1>".getBytes());
                return;
            } catch (InstantiationException e) {
                writeToclient(clientOut,"text/html",404,"Not Found","<h1>该文件找不到哦</h1>".getBytes());
                return;
            } catch (IllegalAccessException e) {
                writeToclient(clientOut,"text/html",404,"Not Found","<h1>该文件找不到哦</h1>".getBytes());
                return;
            }
            return;
        }


        /**
         * 静态页面
         */

        if (requestUrl.equals("/favicon.ico")){
            writeToclient(clientOut,"text/html",200,"OK","/favicon.ico".getBytes());
            return;
        }
        String contentType = "null";
        if (requestUrl.indexOf("htm") !=-1 || requestUrl.equals("/")){
            contentType = "text/html";
        } else if (requestUrl.indexOf("jpg") !=-1 || requestUrl.indexOf("jpeg") != -1) {
            contentType = "image/jpeg";
        } else if (requestUrl.indexOf("gif") != -1) {
            contentType = "image/gif";
        } else {
            contentType = "application/octet-stream";//字节流类型
        }
        String resourcePath = requestUrl.equals("/" ) ? "index.html" : requestUrl.substring(1);
//        System.out.println(resourcePath);
//        System.out.println("qingiuqchangdu"+ clientIn.available());
//        System.out.println(requestUrl);
//        System.out.println("=========");
        //读取服务器内容
        byte[] content = null;
        URL resourceUrl = getClass().getClassLoader().getResource(resourcePath);
        //404 not found
        if (resourceUrl == null){
            writeToclient(clientOut,"text/html",404,"Not Found","<h1>该文件找不到哦</h1>".getBytes());
            return;
        }

        //读取资源文件输入流
        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(resourceUrl.getPath())) ){
            content = bis.readAllBytes();
        }
//        System.out.println(resourceUrl);
        writeToclient(clientOut,contentType,200,"OK",content);



    }

    private void writeToclient(OutputStream clientOut,String contentType,int resCode,String resDes, byte[] content) throws IOException {
        clientOut.write(("HTTP/1.1 " + resCode + " " +resDes + "\r\n").getBytes());//状态行
        clientOut.write("Server: KRGServer/1.0\r\n".getBytes());//响应头
        clientOut.write(("Content-Type: " + contentType +"; charset=UTF-8\r\n").getBytes());//响应头
        clientOut.write(("Date:" + (new Date()).toString() + "\r\n").getBytes());//响应头
        clientOut.write("\r\n".getBytes());//空行
        //响应正文
        clientOut.write(content);
        clientOut.flush();
        clientOut.close();

    }


    public static void main(String[] args) throws IOException {
        new HttpServer(9999);

    }
}
