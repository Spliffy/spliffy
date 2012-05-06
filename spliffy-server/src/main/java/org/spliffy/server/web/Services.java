package org.spliffy.server.web;

import com.ettrema.mail.send.MailSender;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;
import org.spliffy.server.db.UserDao;
import org.spliffy.server.manager.ResourceManager;
import org.spliffy.server.manager.ShareManager;

/**
 *
 * @author brad
 */
public class Services {

    private final HashStore hashStore;
    private final BlobStore blobStore;
    private final Templater templater;
    private final SpliffySecurityManager securityManager;
    private final ResourceManager resourceManager;
    private final ShareManager shareManager;

    public Services(HashStore hashStore, BlobStore blobStore, Templater templater, SpliffySecurityManager securityManager, ResourceManager resourceManager, ShareManager shareManager) {
        this.hashStore = hashStore;
        this.blobStore = blobStore;
        this.templater = templater;
        this.securityManager = securityManager;
        this.resourceManager = resourceManager;
        this.shareManager = shareManager;
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

    public SpliffySecurityManager getSecurityManager() {
        return securityManager;
    }


    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public ShareManager getShareManager() {
        return shareManager;
    }
    
    
}
