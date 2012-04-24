package org.spliffy.server.db.store;

/**
 * Allocates blobs to volumes.
 * 
 * A volume is a storage facility for storing blob data. A Volume might simply
 * be a directory in a traditional filesystem, or it might be something more
 * sophisticated like a distributed filesystem with arbitrary replication factors,
 * corruption detection and self-healing
 *
 * @author brad
 */
public class VolumeManager {

    private final VolumeInstance theVolume; // TODO: have a more dynamic means of configuring volumes

    public VolumeManager(VolumeInstance theVolume) {
        this.theVolume = theVolume;
    }
    
    
    /**
     * Find the most appropriate volume to store the next blob in.
     * 
     * @return 
     */
    public VolumeInstance findNextVolume() {
        return theVolume;
    }

    public VolumeInstance findVolume(String volumeId) {
        return theVolume;
    }

}
