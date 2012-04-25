package org.spliffy.sync;

import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 *
 * @author brad
 */
class SyncingDeltaListener implements DeltaListener {
    private final Syncer syncer;
    private final Archiver archiver;

    public SyncingDeltaListener(Syncer syncer, Archiver archiver) {
        this.syncer = syncer;
        this.archiver = archiver;
    }


    @Override
    public void onRemoteChange(long hash, File localChild) throws IOException {
        syncer.downloadSync(hash, localChild);
    }

    @Override
    public void onLocalChange(String encodedPath, File localFile) throws IOException {
        //
        if (localFile.isFile()) {
            System.out.println("LocalChange - " + localFile.getAbsolutePath());
            syncer.upSync(encodedPath, localFile);
        } else {
            // ignore locally new directories. Remote dirs get created implicitly
            // when files are uploaded
            // Probably should explicitly create empty dirs if needed though
            System.out.println("New directory, but ignoring for now");
        }
    }

    @Override
    public void onTreeConflict(File localChild) {
        Thread.dumpStack();
        JOptionPane.showMessageDialog(null, "Oh oh, remote is a file but local is a directory: " + localChild.getAbsolutePath());
    }

    @Override
    public void onFileConflict(long remoteHash, File localFile, String encodedPath) {
        Thread.dumpStack();
        JOptionPane.showMessageDialog(null, "Files are in conflict. There has been a change to a local file, but also a change to the corresponding remote file: " + localFile.getAbsolutePath());
    }

    @Override
    public void onLocalDeletetion(File localChild, String childEncodedPath) {
        //syncer.deleteRemoteFile(childEncodedPath);
    }

    @Override
    public void onRemoteDeletion(String childEncodedPath, File childFile) {
        System.out.println("Archiving remotely deleted file: " + childFile.getAbsolutePath());
        archiver.archive(childFile);
    }

}
