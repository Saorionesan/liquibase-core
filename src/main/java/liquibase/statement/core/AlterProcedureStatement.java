package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;


/**
 * 对于存储过程的修改，只修改存储过程名以及定义语句
 * Oracle中可以直接使用修改后的存储过程语句
 * MySQL中需要先删除该存储过程语句才能再继续执行
 */
public class AlterProcedureStatement extends AbstractSqlStatement {
    private String catalogName;
    private String schemaName;
    private String procedureName;
    private String procedureText;
    private String endDelimiter;
    private Boolean replaceIfExists; //部分数据库的存储过程支持该属性

    public AlterProcedureStatement(String catalogName, String schemaName, String procedureName, String endDelimiter) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.procedureName = procedureName;
        this.endDelimiter = endDelimiter;
    }

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

    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    public String getProcedureText() {
        return procedureText;
    }

    public void setProcedureText(String procedureText) {
        this.procedureText = procedureText;
    }

    public String getEndDelimiter() {
        return endDelimiter;
    }

    public void setEndDelimiter(String endDelimiter) {
        this.endDelimiter = endDelimiter;
    }

    public Boolean getReplaceIfExists() {
        return replaceIfExists;
    }

    public void setReplaceIfExists(Boolean replaceIfExists) {
        this.replaceIfExists = replaceIfExists;
    }

}
