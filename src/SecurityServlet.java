import java.util.Map;

public class SecurityServlet extends Servlet{
    public String doRequest(String requestUrl, String requestStr) {
        //GET
        String paramStr = requestUrl.substring(requestUrl.indexOf("?") + 1);
//        System.out.println(paramStr);
//       Map<String,Object> params =  getUrlParams(paramStr);
        //Post
        Map<String,Object> params = getPostParam(requestStr);

        String username = params.get("username").toString();
        System.out.println(username);
//        String password = params.get("password").toString();
        if (username.equals("krg") ) {
            return "<h1>你好烤肉锅</h1>";
        }
//        else if (username.indexOf("<")!=-1|| username.indexOf("=")!=-1) {
//            return "<h1>非法输入</h1>";
//
//        }
//        Map<String,Object> params = getPostParam(requestStr);
        return "<h1>你好陌生人</h1>";



    }
}
