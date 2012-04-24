package org.spliffy.server.db.store;

import org.hashsplit4j.api.BlobStore;
import org.spliffy.server.db.BlobHash;
import org.spliffy.server.db.MiltonOpenSessionInViewFilter;

/**
 *
 * @author brad
 */
public class SpliffyBlobStore implements BlobStore{

    private final VolumeManager volumeManager;

    public SpliffyBlobStore(VolumeManager volumeManager) {
        this.volumeManager = volumeManager;
    }
    
    
    
    @Override
    public void setBlob(long hash, byte[] bytes) {
        BlobHash blobHash = BlobHash.findByHash(hash);
        if( blobHash != null ) {
            return ;
        }
        
        VolumeInstance volume = volumeManager.findNextVolume();
        volume.setBlob(hash, bytes);
        
        blobHash = new BlobHash();
        blobHash.setBlobHash(hash);
        blobHash.setVolumeId(volume.getId());
        
        MiltonOpenSessionInViewFilter.session().save(blobHash);
    }

    @Override
    public byte[] getBlob(long hash) {
        // Find its volume
        BlobHash blobHash = (BlobHash) MiltonOpenSessionInViewFilter.session().get(BlobHash.class, hash);
        if( blobHash == null ) {
            return null;
        } else {
            VolumeInstance vol = volumeManager.findVolume(blobHash.getVolumeId()); 
            return vol.getBlob(hash);
        }
    }

    @Override
    public boolean hasBlob(long hash) {
        BlobHash blobHash = (BlobHash) MiltonOpenSessionInViewFilter.session().get(BlobHash.class, hash);
        return ( blobHash != null );
    }
}
