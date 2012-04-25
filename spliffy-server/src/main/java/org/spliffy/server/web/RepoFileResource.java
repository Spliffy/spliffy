package org.spliffy.server.web;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import org.hashsplit4j.api.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.spliffy.server.db.ResourceVersionMeta;
import org.spliffy.server.db.SessionManager;

/**
 * An instance of this class represents the root folder of a repository at a
 * particular revision. Modifications through this class result in a new
 * revision being added to the database
 *
 * @author brad
 */
public class RepoFileResource extends AbstractMutableSpliffyResource implements ReplaceableResource {

    private Fanout fanout;

    public RepoFileResource(String name, ResourceVersionMeta meta, MutableCollection parent, Services services) {
        super(name, meta, parent, services);
    }

    @Override
    public void copyTo(CollectionResource toCollection, String newName) throws NotAuthorizedException, BadRequestException, ConflictException {
        if (toCollection instanceof MutableCollection) {
            Session session = SessionManager.session();
            Transaction tx = session.beginTransaction();

            MutableCollection newParent = (MutableCollection) toCollection;
            ResourceVersionMeta newMeta = Utils.newFileMeta();
            RepoFileResource fileResource = new RepoFileResource(newName, newMeta, newParent, services);
            fileResource.setHash(hash);
            newParent.addChild(fileResource);
            newParent.save(session);
            tx.commit();
        } else {
            throw new ConflictException(this, "Can't copy to collection of type: " + toCollection.getClass());
        }
    }    
    
    @Override
    public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
        Session session = SessionManager.session();
        Transaction tx = session.beginTransaction();

        String ct = HttpManager.request().getContentTypeHeader();
        if (ct != null && ct.equals("spliffy/hash")) {
            // read the new hash and set it on this
            DataInputStream din = new DataInputStream(in);
            try {
                hash = din.readLong();
            } catch (IOException ex) {
                throw new BadRequestException("Couldnt read the new hash", ex);
            }

        } else {
            // parse data and persist to stores
            Parser parser = new Parser();
            long fileHash;
            try {
                fileHash = parser.parse(in, getHashStore(), getBlobStore());
            } catch (IOException ex) {
                throw new BadRequestException("Couldnt parse given data", ex);
            }
            setHash(fileHash);

            // Create a new Version Meta record
            meta = Utils.newFileMeta(meta.getResourceMeta());
        }
        // update parent
        parent.onChildChanged(this);
        parent.save(session);
        tx.commit();
    }
    

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        Combiner combiner = new Combiner();
        List<Long> fanoutCrcs = getFanout().getHashes();
        combiner.combine(fanoutCrcs, getHashStore(), getBlobStore(), out);
        out.flush();
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
        if (fanout == null) {
            fanout = getHashStore().getFanout(hash);
            if (fanout == null) {
                throw new RuntimeException("Fanout not found: " + hash);
            }
        }
        return fanout;
    }
    
    @Override
    public boolean isDir() {
        return false;
    }    
}
