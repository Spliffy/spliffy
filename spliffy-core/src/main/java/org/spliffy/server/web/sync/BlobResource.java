package org.spliffy.server.web.sync;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.spliffy.server.db.Organisation;
import org.spliffy.server.web.SpliffySecurityManager;

/**
 *
 * @author brad
 */
public class BlobResource extends BaseResource implements GetableResource{

    private final byte[] blob;
    private final long hash;
    
    public BlobResource(byte[] blob, long hash, SpliffySecurityManager securityManager, Organisation org) {
        super(securityManager, org);
        this.blob = blob;
        this.hash = hash;
    }        
    
    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> map, String string) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        out.write(blob);
        out.flush();
    }

    @Override
    public Long getContentLength() {
        return (long)blob.length;
    }

    @Override
    public String getUniqueId() {
        return hash + "";
    }

    @Override
    public String getName() {
        return hash + "";
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return 60 * 60 * 24 * 365 * 10l; // 10 years
    }

    @Override
    public String getContentType(String string) {
        return "application/octet-stream";
    }


}
