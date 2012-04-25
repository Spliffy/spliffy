package org.spliffy.server.web;

import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;

/**
 *
 * @author brad
 */
public class Services {
    private final HashStore hashStore;
    
    private final BlobStore blobStore;
    
    private final Templater templater;

    public Services(HashStore hashStore, BlobStore blobStore, Templater templater) {
        this.hashStore = hashStore;
        this.blobStore = blobStore;
        this.templater = templater;
    }

    public BlobStore getBlobStore() {
        return blobStore;
    }

    public HashStore getHashStore() {
        return hashStore;
    }

    public Templater getTemplater() {
        return templater;
    }
    
    
}
