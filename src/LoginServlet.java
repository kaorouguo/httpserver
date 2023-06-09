import java.util.Map;

public class LoginServlet extends Servlet{
    /**
     *
     * @param requestUrl 路径
     * @param requestStr 正文
     * @return
     */
    @Override
    public String doRequest(String requestUrl, String requestStr) {
        //GET
        String paramStr = requestUrl.substring(requestUrl.indexOf("?") + 1);
//        System.out.println(paramStr);
//       Map<String,Object> params =  getUrlParams(paramStr);
        //Post
        Map<String,Object> params = getPostParam(requestStr);

        String username = params.get("username").toString();
        String password = params.get("password").toString();
        if (!username.equals("krg") || !password.equals("krg123")) {
            return "<h1>用户名或密码错误</h1>";
        }
//        Map<String,Object> params = getPostParam(requestStr);
        return "<h1>登陆成功</h1>";



    }
}
