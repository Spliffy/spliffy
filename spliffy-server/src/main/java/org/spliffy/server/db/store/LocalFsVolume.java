package org.spliffy.server.db.store;

import java.io.*;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author brad
 */
public class LocalFsVolume implements VolumeInstance{

    private final File root;
    
    private String id;

    public LocalFsVolume(File root) {
        this.root = root;
        if( !root.exists() ) {
            if( !root.mkdirs() ) {
                throw new RuntimeException("Blob store root location does not exist and could not be created: " + root.getAbsolutePath());
            } else {
                System.out.println("Created blob store folder: " + root.getAbsolutePath());
            }
        } else {
            System.out.println("Using local blob store: " + root.getAbsolutePath());
        }        
    }
    
    
    
    @Override
    public void setBlob(long hash, byte[] bytes) {
        File blob = FsHashUtils.toFile(root, hash);
        if (blob.exists()) {
            return; // already exists, so dont overwrite
        }
        File dir = blob.getParentFile();
        if( !dir.exists() ) {
            if( !dir.mkdirs() ) {
                throw new RuntimeException("Couldnt create blob directory: " + dir.getAbsolutePath());
            }
        }
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(blob);
            fout.write(bytes);
            fout.flush();
        } catch (IOException ex) {
            throw new RuntimeException(blob.getAbsolutePath(), ex);
        } finally {
            IOUtils.closeQuietly(fout);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
        
    @Override
    public byte[] getBlob(long hash) {
        File blob = FsHashUtils.toFile(root, hash);
        if (!blob.exists()) {
            return null;
        }
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(blob);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            IOUtils.copy(fin, bout);
            return bout.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(blob.getAbsolutePath(),ex);
        } finally {
            IOUtils.closeQuietly(fin);
        }
    }

}
