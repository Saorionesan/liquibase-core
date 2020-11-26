package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateTriggerStatement;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Trigger;

public class  CreateTriggerGenerator extends AbstractSqlGenerator<CreateTriggerStatement> {
    @Override
    public ValidationErrors validate(CreateTriggerStatement statement, Database database, SqlGeneratorChain<CreateTriggerStatement> sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("triggerText", statement.getTriggerText());
        return validationErrors;
    }

    /**
     *
     * @return 返回生成的SQL UnparsedSql 包含要创建的SQL、受影响的数据库对象，一般只要选择自己即可
     */
    @Override
    public Sql[] generateSql(CreateTriggerStatement statement, Database database, SqlGeneratorChain<CreateTriggerStatement> sqlGeneratorChain) {
        return new Sql[]{new UnparsedSql(statement.getTriggerText(),getAffectedTrigger(statement))
        };
    }

    private Trigger getAffectedTrigger(CreateTriggerStatement statement){
        return new Trigger().setName(statement.getTriggerName()).setSchema(new Schema(statement.getCatalogName(),statement.getSchemaName()));
    }

}
