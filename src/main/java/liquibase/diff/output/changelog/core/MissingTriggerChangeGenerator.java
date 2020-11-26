package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.CreateTriggerChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.StoredProcedure;
import liquibase.structure.core.Trigger;

public class MissingTriggerChangeGenerator extends AbstractChangeGenerator implements MissingObjectChangeGenerator {


    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisionDatabase, ChangeGeneratorChain chain) {
        //如果两个数据库不相同不再进行比较存储过程，直接返回null
        if(!referenceDatabase.getShortName().equals(comparisionDatabase.getShortName())){
            return null;
        }
        Trigger trigger=(Trigger) missingObject;
        CreateTriggerChange change=new CreateTriggerChange();
        change.setTriggerName(trigger.getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(trigger.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(trigger.getSchema().getName());
        }
        String triggerText=trigger.getBody();
        change.setTriggerText(triggerText);
        return new Change[]{change};
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Trigger.class.isAssignableFrom(objectType)) { //注意，此处类型要对应，即objectType为structure包中抽象出来的数据库结构
            return PRIORITY_DEFAULT; //有就返回，否则即不返回
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
