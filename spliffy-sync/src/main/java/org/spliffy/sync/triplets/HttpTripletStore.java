package org.spliffy.sync.triplets;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.exceptions.NotFoundException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
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
    private final Path rootPath;

    /**
     * 
     * @param httpClient
     * @param rootPath 
     */
    public HttpTripletStore(HttpClient httpClient, String rootPath) {
        this.httpClient = httpClient;
        this.rootPath = Path.path(rootPath);
    }



    @Override
    public List<Triplet> getTriplets(Path path) {
        String href = HttpUtils.toHref(rootPath, path) + "/?type=hashes";
        try {            
            byte[] arrRemoteTriplets = HttpUtils.get(httpClient, href);
            List<Triplet> triplets = HashUtils.parseTriplets(new ByteArrayInputStream(arrRemoteTriplets));
            System.out.println("HttpTripletStore: getTriples: " + href + " -> " + triplets.size());
            return triplets;
        } catch (IOException ex) {
            throw new RuntimeException(href, ex);
        } catch (NotFoundException ex) {
            System.out.println("HttpTripletStore: not found: " + href);
            return null;
        } catch(Throwable e) {
            throw new RuntimeException(href, e);
        }
    }    
}
