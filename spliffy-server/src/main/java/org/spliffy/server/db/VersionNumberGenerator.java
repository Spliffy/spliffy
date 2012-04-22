package org.spliffy.server.db;

/**
 *
 * @author brad
 */
public interface VersionNumberGenerator {
    long nextVersionNumber(Repository r);
}
