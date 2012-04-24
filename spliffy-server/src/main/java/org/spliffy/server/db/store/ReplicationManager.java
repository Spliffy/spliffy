package org.spliffy.server.db.store;

import java.util.UUID;

/**
 *
 * @author brad
 */
public interface ReplicationManager {
    void newBlob(UUID volumeInstanceId, long hash);
}
