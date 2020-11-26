package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

/**
 * 创建触发器生成SQL
 */
public class CreateTriggerStatement extends AbstractSqlStatement {
    private String catalogName;
    private String schemaName;
    private String triggerName;
    private String triggerText;

    public CreateTriggerStatement(String catalogName, String schemaName, String triggerName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.triggerName = triggerName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public CreateTriggerStatement setCatalogName(String catalogName) {
        this.catalogName = catalogName;
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public CreateTriggerStatement setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public CreateTriggerStatement setTriggerName(String triggerName) {
        this.triggerName = triggerName;
        return this;
    }

    public String getTriggerText() {
        return triggerText;
    }

    public CreateTriggerStatement setTriggerText(String triggerText) {
        this.triggerText = triggerText;
        return this;
    }
}
