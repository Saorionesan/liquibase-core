package liquibase.change.core;

import liquibase.change.*;
import liquibase.changelog.ChangeLogParameters;
import liquibase.database.Database;
import liquibase.database.DatabaseList;
import liquibase.database.core.AbstractDb2Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterProcedureStatement;
import liquibase.statement.core.CreateProcedureStatement;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;

@DatabaseChange(
        name = "alterProcedure",
        description = "修改存储过程",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
public class AlterProcedureChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String procedureName;
    private String procedureText;
    private String endDelimiter;
    private Boolean replaceIfExists; //部分数据库的存储过程支持该属性
    private String dbms;



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

    @DatabaseChangeProperty(
            exampleValue = "h2, oracle",
            since = "3.1"
    )
    public String getDbms() {
        return dbms;
    }

    public void setDbms(String dbms) {
        this.dbms = dbms;
    }

    @Override
    public String getConfirmationMessage() {
        return "Stored procedure altered";
    }
    @Override
    public ValidationErrors validate(Database database) {
        // Not falling back to default because of path/procedureText option group. Need to specify everything.
        ValidationErrors validate = new ValidationErrors();
        if (StringUtils.trimToNull(getProcedureText()) != null) {
            validate.addError(
                    "Cannot specify both 'path' and a nested procedure text in " +
                            ChangeFactory.getInstance().getChangeMetaData(this).getName()
            );
        }

        if (StringUtils.trimToNull(getProcedureText()) == null) {
            validate.addError(
                    "Cannot specify either 'path' or a nested procedure text in " +
                            ChangeFactory.getInstance().getChangeMetaData(this).getName()
            );
        }

        if ((this.getReplaceIfExists() != null) && (DatabaseList.definitionMatches(getDbms(), database, true))) {
            if (database instanceof MSSQLDatabase) {
                if (this.getReplaceIfExists() && (this.getProcedureName() == null)) {
                    validate.addError("procedureName is required if replaceIfExists = true");
                }
            } else {
                validate.checkDisallowedField("replaceIfExists", this.getReplaceIfExists(), database);
            }
        }
        return validate;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        String endDelimiter = ";";
        if (database instanceof OracleDatabase) {
            endDelimiter = ""; // endDelimiter = "\n/" 修改为 ""
        } else if (database instanceof AbstractDb2Database) {
            endDelimiter = "";
        }
        String procedureText=StringUtils.trimToNull(getProcedureText());
        return generateStatements(procedureText, endDelimiter, database);
    }
    protected SqlStatement[] generateStatements(String logicText, String endDelimiter, Database database) {
        AlterProcedureStatement statement =
                new AlterProcedureStatement(
                        getCatalogName(),
                        getSchemaName(),
                        getProcedureName(),
                        endDelimiter
                );
        statement.setReplaceIfExists(getReplaceIfExists());
        statement.setProcedureText(logicText);
        return new SqlStatement[]{
                statement,
        };
    }
}
