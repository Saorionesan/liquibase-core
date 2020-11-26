package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.CreateProcedureChange;
import liquibase.change.core.DropProcedureChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.StoredProcedure;

public class UnexpectedProcedureChangeGenerator extends AbstractChangeGenerator implements UnexpectedObjectChangeGenerator {

    //生成对应的change
    @Override
    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisionDatabase, ChangeGeneratorChain chain) {
        //如果两个数据库不相同不再进行比较存储过程，直接返回null
        if(!referenceDatabase.getShortName().equals(comparisionDatabase.getShortName())){
            return null;
        }
        StoredProcedure storedProcedure=(StoredProcedure)unexpectedObject;
        DropProcedureChange change=new DropProcedureChange();
        change.setProcedureName(storedProcedure.getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(storedProcedure.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(storedProcedure.getSchema().getName());
        }
        return new Change[]{change};
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (StoredProcedure.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }
}
