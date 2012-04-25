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
    /**
     * Scan the local and remote directories, updating local files and 
     * sending new data to the server as it goes.
     * 
     * Data sent to the server should only be objects, not the top level hash
     * which links everything together. That hash should be returned by this function
     * and a higher level function will submit that
     * 
     * @param remoteDirHash
     * @param localParent
     * @param encodedPath
     * @param scanningHashStore
     * @param listener
     * @return - the new hash of the directory, based on local data after it has
     * been merged with changes from the server
     * @throws IOException
     * @throws HttpException
     * @throws NotAuthorizedException
     * @throws BadRequestException
     * @throws ConflictException
     * @throws NotFoundException 
     */
    Long scanDir(Long remoteDirHash, File localParent, String encodedPath, ScanningHashStore scanningHashStore, DeltaListener listener) throws IOException, HttpException, NotAuthorizedException, BadRequestException, ConflictException, NotFoundException;
}
