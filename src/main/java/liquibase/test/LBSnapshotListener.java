package liquibase.test;

import liquibase.database.Database;
import liquibase.snapshot.SnapshotListener;
import liquibase.structure.DatabaseObject;

public class LBSnapshotListener implements SnapshotListener {
    @Override
    public void willSnapshot(DatabaseObject example, Database database) {
    }

    @Override
    public void finishedSnapshot(DatabaseObject example, DatabaseObject snapshot, Database database) {
    }
}
