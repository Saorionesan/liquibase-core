package liquibase.diff.output.changelog.core;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateProcedureStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleCreateIndexStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.util.JdbcConstants;
import liquibase.change.Change;
import liquibase.change.core.CreateProcedureChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.StoredProcedure;

import java.util.List;

public class MissingProcedureChangeGenerator extends AbstractChangeGenerator implements MissingObjectChangeGenerator {

    /**
     * 添加 存储过程 change 模块
     * @param chain
     * @return
     */
    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisionDatabase, ChangeGeneratorChain chain) {
        //如果两个数据库不相同不再进行比较存储过程，直接返回null
        if(!referenceDatabase.getShortName().equals(comparisionDatabase.getShortName())){
            return null;
        }
        StoredProcedure storedProcedure=(StoredProcedure)missingObject;
        CreateProcedureChange change=new CreateProcedureChange();
       change.setProcedureName(storedProcedure.getName());
       if (control.getIncludeCatalog()) {
            change.setCatalogName(storedProcedure.getSchema().getCatalogName());
        }
       if (control.getIncludeSchema()) {
            change.setSchemaName(storedProcedure.getSchema().getName());
        }
       String procedureText=storedProcedure.getBody();
       change.setProcedureText(procedureText);
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
