package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.DropTriggerChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Trigger;

//生成删除触发器trigger
public class UnexpectedTriggerChangeGenerator extends AbstractChangeGenerator implements UnexpectedObjectChangeGenerator {

    @Override
    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisionDatabase, ChangeGeneratorChain chain) {
        if(!referenceDatabase.getShortName().equals(comparisionDatabase.getShortName())){
            return null;
        }
        Trigger trigger=(Trigger) unexpectedObject;
        DropTriggerChange change=new DropTriggerChange();
        change.setTriggerName(trigger.getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(trigger.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(trigger.getSchema().getName());
        }
        return new Change[]{change};
    }


    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Trigger.class.isAssignableFrom(objectType)) {
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
