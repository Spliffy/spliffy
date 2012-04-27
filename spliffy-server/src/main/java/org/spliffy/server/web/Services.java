package org.spliffy.server.web;

import com.ettrema.mail.send.MailSender;
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
    
    private final SpliffySecurityManager securityManager;
    
    private final MailSender mailSender;
    
    public Services(HashStore hashStore, BlobStore blobStore, Templater templater, SpliffySecurityManager securityManager, MailSender mailSender) {
        this.hashStore = hashStore;
        this.blobStore = blobStore;
        this.templater = templater;
        this.securityManager = securityManager;
        this.mailSender = mailSender;
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

    public MailSender getMailSender() {
        return mailSender;
    }
}
