package org.spliffy.sync;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.httpclient.MkColMethod;
import java.io.IOException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 *
 * @author brad
 */
public class HttpUtils {

    public static byte[] get(HttpClient client, String path, NameValuePair... params) throws NotFoundException {
        GetMethod getMethod = new GetMethod(path);
        if (params != null && params.length > 0) {
            System.out.println("add params: " + params);
            getMethod.setQueryString(params);
        }
        int result;
        try {
            result = client.executeMethod(getMethod);
            if (result >= 400 && result < 500) {
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
            if (result >= 400 && result < 500) {
                throw new NotFoundException("Not found: " + path);
            }
            if (result < 200 || result >= 300) {
                throw new RuntimeException("Delete failed. result:" + result + " url: " + path);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void mkcol(HttpClient client, String path) throws ConflictException {
        System.out.println("mkcol: " + path);
        MkColMethod m = new MkColMethod(path);
        int result;
        try {
            result = client.executeMethod(m);
            if (result >= 400 && result < 500) {
                throw new ConflictException("Conflict: " + path);
            }
            if (result < 200 || result >= 300) {
                throw new RuntimeException("mkcol failed. result:" + result + " url: " + path);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Takes an unencoded local path (eg "/my docs") and turns it into a
     * percentage encoded path (eg "/my%20docs"), with the encoded rootPath
     * added to the front
     *
     * @param path
     * @return
     */
    public static String toHref(Path basePath, Path unencodedPath) {
        Path p = basePath;
        for (String name : unencodedPath.getParts()) {
            p = p.child(com.bradmcevoy.http.Utils.percentEncode(name));
        }
        return p.toString();
    }
}
