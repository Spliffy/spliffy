package org.spliffy.sync;

import com.bradmcevoy.utils.With;
import com.ettrema.db.Table;
import com.ettrema.db.TableCreatorService;
import com.ettrema.db.TableDefinitionSource;
import com.ettrema.db.UseConnection;
import com.ettrema.db.dialects.Dialect;
import com.ettrema.db.types.FieldTypes;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

/**
 * Uses a hidden directory in each folder to store a file containing hashes
 *
 * @author brad
 */
public class JdbcLastBackedupStore implements LastBackedupStore, Serializable {

    public static final LastBackedUpTable LAST_BACKEDUP = new LastBackedUpTable();
    private final UseConnection useConnection;

    /**
     *
     * @param useConnection
     * @param dialect
     * @param group - so we can cache different collections in one table
     */
    public JdbcLastBackedupStore(UseConnection useConnection, Dialect dialect) {
        this.useConnection = useConnection;
        TableDefinitionSource defs = new TableDefinitionSource() {

            @Override
            public List<? extends Table> getTableDefinitions() {
                return Arrays.asList(LAST_BACKEDUP);
            }

            @Override
            public void onCreate(Table t, Connection con) {
            }
        };
        final TableCreatorService creatorService = new TableCreatorService(null, Arrays.asList(defs), dialect);

        useConnection.use(new With<Connection, Object>() {

            @Override
            public Object use(Connection con) throws Exception {
                creatorService.processTableDefinitions(con);
                return null;
            }
        });
    }

    @Override
    public Long findBackedUpHash(final File file) {
        final String sql = LAST_BACKEDUP.getSelect() + " WHERE " + LAST_BACKEDUP.path.getName() + " = ?";
        Long crc = useConnection.use(new With<Connection, Long>() {

            @Override
            public Long use(Connection con) throws Exception {
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1, file.getAbsolutePath());
                ResultSet rs = stmt.executeQuery();
                try {
                    if (rs.next()) {
                        Long crc = LAST_BACKEDUP.crc.get(rs);
                        return crc;
                    } else {
                        return null;
                    }
                } finally {
                    UseConnection.close(rs);
                    UseConnection.close(stmt);
                }
            }
        });
        return crc;
    }

    @Override
    public void setBackedupHash(final File localFile, final long hash) {
        final String deleteSql = LAST_BACKEDUP.getDeleteBy(LAST_BACKEDUP.path);

        final String insertSql = LAST_BACKEDUP.getInsert();

        useConnection.use(new With<Connection, Object>() {

            @Override
            public Object use(Connection con) throws Exception {
                PreparedStatement stmt = con.prepareStatement(deleteSql);
                stmt.setString(1, localFile.getAbsolutePath());
                stmt.execute();
                UseConnection.close(stmt);

                stmt = con.prepareStatement(insertSql);
                stmt.setString(1, localFile.getAbsolutePath());
                stmt.setLong(2, hash);
                stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                stmt.execute();
                UseConnection.close(stmt);

                return null;
            }
        });

    }

    public static class LastBackedUpTable extends com.ettrema.db.Table {

        public final Table.Field<String> path = add("path", FieldTypes.CHARACTER_VARYING, false);
        public final Table.Field<Long> crc = add("crc", FieldTypes.LONG, false); // the last backed up crc of this local file
        public final Table.Field<java.sql.Timestamp> date = add("date_modified", FieldTypes.TIMESTAMP, false); // the date/time which the file was backed up, or that it was first discoverd assert having been backed up;

        public LastBackedUpTable() {
            super("last_backedup");
        }
    }
}
