package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropTriggerStatement;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Trigger;

public class DropTriggerGenerator extends AbstractSqlGenerator<DropTriggerStatement> {
    @Override
    public ValidationErrors validate(DropTriggerStatement statement, Database database, SqlGeneratorChain<DropTriggerStatement> sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("triggerName", statement.getTriggerName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropTriggerStatement statement, Database database, SqlGeneratorChain<DropTriggerStatement> sqlGeneratorChain) {
        return new Sql[] {
                new UnparsedSql("DROP TRIGGER "+database.escapeObjectName(statement.getCatalogName(), statement.getSchemaName(), statement.getTriggerName(), Trigger.class),
                        new Trigger().setName(statement.getTriggerName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName())))
        };
    }
}
