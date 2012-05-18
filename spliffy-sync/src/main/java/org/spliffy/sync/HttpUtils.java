package org.spliffy.sync;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.httpclient.MkColMethod;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 *
 * @author brad
 */
public class HttpUtils {


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

    public static int executeHttpWithStatus(org.apache.http.client.HttpClient client, HttpUriRequest m, OutputStream out) throws IOException {
        HttpResponse resp = client.execute(m);
        HttpEntity entity = resp.getEntity();
        if (entity != null) {
            InputStream in = null;
            try {
                in = entity.getContent();
                if (out != null) {
                    IOUtils.copy(in, out);
                }
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
        return resp.getStatusLine().getStatusCode();
    }
}
