package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateTriggerStatement;


@DatabaseChange( name = "createTrigger",description = "创建触发器",priority = ChangeMetaData.PRIORITY_DEFAULT)
public class CreateTriggerChange extends AbstractChange {


    private String catalogName;
    private String schemaName;
    private String triggerName;
    private String triggerText;


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

    @DatabaseChangeProperty(description = "Name of the trigger to create")
    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public String getTriggerText() {
        return triggerText;
    }

    public void setTriggerText(String triggerText) {
        this.triggerText = triggerText;
    }

    @Override
    public String getConfirmationMessage() {
        return "Trigger " + getTriggerName() + " created";
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
                new CreateTriggerStatement(getCatalogName(), getSchemaName(), getTriggerName())
                        .setTriggerText(getTriggerText())
        };
    }
}
