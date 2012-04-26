package org.spliffy.server.db.store;

import java.util.ArrayList;
import java.util.List;
import org.hashsplit4j.api.Fanout;
import org.hashsplit4j.api.HashStore;
import org.spliffy.server.db.FanoutEntry;
import org.spliffy.server.db.FanoutHash;
import org.spliffy.server.db.SessionManager;

/**
 *
 * @author brad
 */
public class SpliffyHashStore implements HashStore{

    @Override
    public void setFanout(long hash, List<Long> childCrcs, long actualContentLength) {
        if( hasFanout(hash)) {
            return ;
        }
        FanoutHash fanout = new FanoutHash();
        fanout.setFanoutHash(hash);
        fanout.setActualContentLength(actualContentLength);
        List<FanoutEntry> list = new ArrayList<>(childCrcs.size());
        for( Long l : childCrcs){
            FanoutEntry fe = new FanoutEntry();
            fe.setChunkHash(l);
            fe.setFanout(fanout);
            list.add(fe);
        }
        fanout.setFanoutEntrys(list);
        SessionManager.session().save(fanout);
    }

    @Override
    public Fanout getFanout(long hash) {
        FanoutHash fanoutHash = (FanoutHash) SessionManager.session().get(FanoutHash.class, hash);
        if( fanoutHash == null ) {
            return null;
        } else {
            return fanoutHash;
        }
    }

    @Override
    public boolean hasFanout(long hash) {
        FanoutHash fanoutHash = (FanoutHash) SessionManager.session().get(FanoutHash.class, hash);
        return fanoutHash != null;
    }

}
