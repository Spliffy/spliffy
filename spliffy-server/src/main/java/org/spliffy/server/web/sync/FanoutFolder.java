package org.spliffy.server.web.sync;

import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hashsplit4j.api.FanoutImpl;
import org.hashsplit4j.api.HashStore;

/**
 *
 * @author brad
 */
public class FanoutFolder extends BaseResource implements PutableResource {

    private final HashStore hashStore;
    private final String name;

    public FanoutFolder(HashStore hashStore, String name, com.bradmcevoy.http.SecurityManager securityManager) {
        super(securityManager);
        this.hashStore = hashStore;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        long hash = Long.parseLong(newName);
        List<Long> list = new ArrayList<Long>();
        DataInputStream din = new DataInputStream(inputStream);
        long actualFanoutLength = din.readLong(); // first long is the content length of the chunks within the fanout. Ie the actual content length (not length of hashes!)
        try {
            while (true) {
                list.add(din.readLong());
            }
        } catch (EOFException e) {
            // cool
        }
        hashStore.setFanout(hash, list, actualFanoutLength);
        FanoutImpl fanoutImpl = new FanoutImpl(list, actualFanoutLength);
        return new FanoutResource(fanoutImpl, hash, securityManager);
    }

    @Override
    public Resource child(String string) throws NotAuthorizedException, BadRequestException {
        return null;
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        return Collections.EMPTY_LIST;
    }
}
