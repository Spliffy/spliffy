package org.spliffy.sync;

import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.httpclient.HttpException;
import java.io.File;
import java.io.IOException;

/**
 * This class will walk over the local and remote trees, and will callback to the 
 * given listener when it finds a change
 *
 * @author brad
 */
public interface DeltaWalker {
    void scanDir(File localParent, String encodedPath, ScanningHashStore scanningHashStore, DeltaListener listener) throws IOException, HttpException, NotAuthorizedException, BadRequestException, ConflictException, NotFoundException;
}
