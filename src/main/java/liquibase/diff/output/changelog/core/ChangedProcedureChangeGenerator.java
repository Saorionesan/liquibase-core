package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AlterProcedureChange;
import liquibase.change.core.AlterSequenceChange;
import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.StoredProcedure;

public class ChangedProcedureChangeGenerator extends AbstractChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        //如果两个数据库不相同不再进行比较存储过程，直接返回null
        if(!referenceDatabase.getShortName().equals(comparisonDatabase.getShortName())){
            return null;
        }
        StoredProcedure storedProcedure=(StoredProcedure)changedObject;
        //此处只需要比较一个存储过程的body是否相同，如果不相同则生成change
        //如果要比较多个元素，则需要多个change
        if(differences.isDifferent("body")){
            AlterProcedureChange alterProcedureChange=createAlterProcedureChange(storedProcedure,control);
            alterProcedureChange.setProcedureText(storedProcedure.getBody());
            return new Change[]{
                    alterProcedureChange
            };
        }
        return null;
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



    protected AlterProcedureChange createAlterProcedureChange(StoredProcedure procedure, DiffOutputControl control) {
        AlterProcedureChange change = new AlterProcedureChange();
        if (control.getIncludeCatalog()) {
            change.setCatalogName(procedure.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(procedure.getSchema().getName());
        }
        change.setProcedureName(procedure.getName());
        return change;
    }
}
