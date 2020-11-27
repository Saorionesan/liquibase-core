package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropTriggerStatement;

//删除触发器
@DatabaseChange(name="dropTrigger", description = "Drop an existing trigger", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "trigger")
public class DropTriggerChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String triggerName;

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    @Override
    public String getConfirmationMessage() {
        return "Trigger "+getTriggerName()+" dropped";
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
                new DropTriggerStatement(getCatalogName(), getSchemaName(), getTriggerName())
        };
    }
}
