package org.spliffy.sync;

import com.bradmcevoy.http.exceptions.NotFoundException;
import java.io.IOException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 *
 * @author brad
 */
public class HttpUtils {
    public static byte[] get(HttpClient client, String path) throws NotFoundException {
        GetMethod getMethod = new GetMethod(path);
        int result;
        try {
            result = client.executeMethod(getMethod);
            if( result >= 400 && result < 500 ) {
                throw new NotFoundException("Not found: " + path);
            }
            if (result < 200 || result >= 300) {
                throw new RuntimeException("Download failed. result:" + result + " url: " + path);
            }
            byte[] arr = getMethod.getResponseBody();
            return arr;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }        
    }

    public static void delete(HttpClient client, String path) throws NotFoundException {
        System.out.println("delete: " + path);
        DeleteMethod getMethod = new DeleteMethod(path);
        int result;
        try {
            result = client.executeMethod(getMethod);
            if( result >= 400 && result < 500 ) {
                throw new NotFoundException("Not found: " + path);
            }
            if (result < 200 || result >= 300) {
                throw new RuntimeException("Delete failed. result:" + result + " url: " + path);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }             
    }
}
