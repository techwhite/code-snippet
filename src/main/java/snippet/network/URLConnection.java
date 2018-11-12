package snippet.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/*
* RequestMethod: GET, POST, HEAD, OPTIONS, PUT, DELETE, TRACE.
*/
public class URLConnection {
    public static void main(String[] args) {
        String url = "http://www.baidu.com";
        doGet(url);
        doPost(url, Collections.emptyMap());
    }

    /*
     * The HttpUrlConnection class allows us to perform basic HTTP requests without
     * the use of any additional libraries. All the classes that are needed are
     * contained in the java.net package.The disadvantages of using this method are
     * that the code can be more cumbersome than other HTTP libraries, and it does
     * not provide more advanced functionalities such as dedicated methods for
     * adding headers or authentication.
     */
    private static void doGet(String httpUrl) {
        HttpURLConnection con = null;

        try {
            URL url = new URL(httpUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET"); 

            // timeout settings
            // HttpUrlConnection class allows setting the connect and read timeouts. These
            // values define the interval of time to wait for the connection to the server
            // to be established or data to be available for reading.
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            // enable or disable automatically following redirects for a specific connection
            // con.setInstanceFollowRedirects(false);
            // enable or disable automatic redirect for all connections. By default, the
            // behavior is enabled
            // HttpURLConnection.setFollowRedirects(false);

            // send request
            con.connect();

            // Reading the response of the request can be done by parsing the InputStream of
            // the HttpUrlConnection instance. To execute the request we can use the
            // getResponseCode(), connect(), getInputStream() or getOutputStream() methods:
            int status = con.getResponseCode();

            /*// enable or disable automatically following redirects for a specific connection
            con.setInstanceFollowRedirects(false);
            // enable or disable automatic redirect for all connections. By default, the
            // behavior is enabled
            HttpURLConnection.setFollowRedirects(false);

            int status = con.getResponseCode();
            // When a request returns a status code 301 or 302, indicating a redirect, we
            // can retrieve the Location header and create a new request to the new URL:
            if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM) {
                String location = con.getHeaderField("Location");
                if (StringUtils.isNotBlank(location)) {
                    URL newUrl = new URL(location);
                    con = (HttpURLConnection) newUrl.openConnection();
                }
            } */

            if (status == HttpURLConnection.HTTP_OK) {
                // 获取输入流
                InputStream is = con.getInputStream();
                // 封装输入流，指定字符集
                BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                    content.append("\r\n");
                }
                in.close();

                // output
                System.out.println(content.toString());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // close connection
            con.disconnect();
        }
    }

    private static void doPost(String httpUrl, Map<String, String> paramMap) {
        HttpURLConnection con = null;

        try {
            URL url = new URL(httpUrl);
            // 通过远程url连接对象打开连接
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST"); 
            // timeout settings
            // HttpUrlConnection class allows setting the connect and read timeouts. These
            // values define the interval of time to wait for the connection to the server
            // to be established or data to be available for reading.
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。
            // 另外可以在RequestProperty中设置更多参数，如"user-agent", "Content-Type"等
            // 相反，获取里面的参数可以使用 con.getHeaderFields()
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // 设置鉴权信息：Authorization: Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0
            con.setRequestProperty("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");

            // If we want to add parameters to a request, we have to set the doOutput
            // property to true, then write a String of the form param1=value&param2=value
            // to the OutputStream of the HttpUrlConnection instance:
            con.setDoOutput(true);
            String params = paramMap.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));
            // 通过连接对象获取一个输出流
            OutputStream os = con.getOutputStream();
            // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的
            os.write(params.getBytes());

            /*// enable or disable automatically following redirects for a specific connection
            con.setInstanceFollowRedirects(false);
            // enable or disable automatic redirect for all connections. By default, the
            // behavior is enabled
            HttpURLConnection.setFollowRedirects(false);

            int status = con.getResponseCode();
            // When a request returns a status code 301 or 302, indicating a redirect, we
            // can retrieve the Location header and create a new request to the new URL:
            if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM) {
                String location = con.getHeaderField("Location");
                if (StringUtils.isNotBlank(location)) {
                    URL newUrl = new URL(location);
                    con = (HttpURLConnection) newUrl.openConnection();
                }
            } */

            // Reading the response of the request can be done by parsing the InputStream of
            // the HttpUrlConnection instance. To execute the request we can use the
            // getResponseCode(), connect(), getInputStream() or getOutputStream() methods:
            int status = con.getResponseCode();

            if (status == HttpURLConnection.HTTP_OK) {
                // 获取输入流
                InputStream is = con.getInputStream();
                // 封装输入流，指定字符集
                BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                    content.append("\r\n");
                }
                in.close();

                // output
                System.out.println(content.toString());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // close connection
            con.disconnect();
        }
    }
}
