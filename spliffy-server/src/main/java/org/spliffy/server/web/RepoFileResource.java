package org.spliffy.server.web;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hashsplit4j.api.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.spliffy.server.db.MiltonOpenSessionInViewFilter;
import org.spliffy.server.db.ResourceMeta;

/**
 *
 * @author brad
 */
public class RepoFileResource extends AbstractSpliffyResource implements PropFindableResource, ReplaceableResource, GetableResource{
    
    private String name;
    
    private final RepoDirectoryResource parent;
    
    private final ResourceMeta meta;
    
    private long hash;
    
    private Fanout fanout;
    
    public RepoFileResource(String name, ResourceMeta meta, RepoDirectoryResource parent, HashStore hashStore, BlobStore blobStore) {
        super(hashStore, blobStore);
        this.meta = meta;
        this.name = name;
        this.parent = parent;
    }

    @Override
    public Date getCreateDate() {
        return meta.getCreateDate();
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onChildChanged(Session session) {
        // Not for files
    }

    @Override
    public Long getEntryHash() {
        return hash;
    }

    public void setHash(long hash) {
        this.hash = hash;
    }
        

    @Override
    public UUID getMetaId() {
        return meta.getId();
    }

    @Override
    public Date getModifiedDate() {
        return meta.getModifiedDate();
    }

    @Override
    public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
        Session session = MiltonOpenSessionInViewFilter.session();
        Transaction tx = session.beginTransaction();
        
        // parse data and persist to stores
        Parser parser = new Parser();
        long fileHash;        
        try {
            fileHash = parser.parse(in, hashStore, blobStore);
        } catch (IOException ex) {
            throw new BadRequestException("Couldnt parse given data", ex);
        }
        setHash(fileHash);
                
        // update parent
        parent.onChildChanged(session);
        
        tx.commit();        
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        Combiner combiner = new Combiner();
        List<Long> fanoutCrcs = getFanout().getHashes();
        combiner.combine(fanoutCrcs, hashStore, blobStore, out);
        out.flush();
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        return null;
    }

    @Override
    public Long getContentLength() {
        return getFanout().getActualContentLength();
    }

    private Fanout getFanout() {
        if( fanout == null ) {
            fanout = hashStore.getFanout(hash);
            if( fanout == null ) {
                throw new RuntimeException("Fanout not found: " + hash);
            }
        }
        return fanout;
    }
    
    
}
