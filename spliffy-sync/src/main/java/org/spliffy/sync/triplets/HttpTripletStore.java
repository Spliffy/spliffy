package org.spliffy.sync.triplets;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.exceptions.NotFoundException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import org.apache.commons.httpclient.HttpClient;
import org.spliffy.common.HashUtils;
import org.spliffy.common.Triplet;
import org.spliffy.sync.HttpUtils;

/**
 * Loads triplets from a remote server over HTTP
 *
 * @author brad
 */
public class HttpTripletStore implements TripletStore {
    private final HttpClient httpClient;
    private final String rootPath;

    /**
     * 
     * @param httpClient
     * @param rootPath 
     */
    public HttpTripletStore(HttpClient httpClient, String rootPath) {
        this.httpClient = httpClient;
        this.rootPath = rootPath;
    }



    @Override
    public List<Triplet> getTriplets(Path path) {
        String href = toHref(path);
        try {            
            byte[] arrRemoteTriplets = HttpUtils.get(httpClient, href + "?type=hashes");
            List<Triplet> triplets = HashUtils.parseTriplets(new ByteArrayInputStream(arrRemoteTriplets));
            return triplets;
        } catch (IOException ex) {
            throw new RuntimeException(href, ex);
        } catch (NotFoundException ex) {
            return null;
        }
    }
    
    /**
     * Takes an unencoded local path (eg "/my docs") and turns it into
     * a percentage encoded path (eg "/my%20docs"), with the rootPath
     * added to the front
     * 
     * @param path
     * @return 
     */
    private String toHref(Path path) {
        StringBuilder sb = new StringBuilder("/");
        for(String name : path.getParts()) {
            sb.append(name);
            
        }
        return rootPath + sb.toString();
    }
}
