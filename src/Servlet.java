import java.util.HashMap;
import java.util.Map;

public abstract class Servlet {
    public abstract String doRequest(String requestUrl, String requestStr);

    /**
     * 获取URL路径 中的参数
     * @param paramStr
     * @return
     */
    public Map<String,Object> getUrlParams(String paramStr) {
        Map<String,Object> map = new HashMap<>();
        if (paramStr == null || paramStr.equals("")) {
            return map;
        }
        String[] params = paramStr.split("&");
        for (String param : params) {
            param = param.trim();
            String[] p = param.split("=");//分割后的数组0 username 1 password
            if (p.length == 2) {
                map.put(p[0],p[1]);
            }
        }
        return map;
    }

    /**
     * 获取Post 提交的表单的参数
     * @param requestStr
     * @return
     */

    public  Map<String,Object> getPostParam(String requestStr) {
        String[] requestArr = requestStr.split("\r\n");
        String paramStr = requestArr[requestArr.length-1];
//        System.out.println(paramStr);
        return getUrlParams(paramStr);


    }


}
