package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class DropTriggerStatement extends AbstractSqlStatement {
    private String catalogName;
    private String schemaName;
    private String triggerName;

    public DropTriggerStatement(String catalogName, String schemaName, String triggerName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.triggerName = triggerName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTriggerName() { return triggerName; }
}
