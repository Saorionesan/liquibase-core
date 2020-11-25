package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.core.RawCallStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.util.JdbcUtils;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DMDatabase extends AbstractJdbcDatabase {

    private  static final String PRODUCT_NAME="DM DBMS";
    private static final Integer PORT=5236;
    private Set<String> reservedWords = new HashSet<>(); // 关键字集合

    private Set<String> userDefinedTypes;


    /**
     * Default constructor for an object that represents the Oracle Database DBMS.
     */
    public DMDatabase() {
        super.unquotedObjectsAreUppercased=true;
        //noinspection HardCodedStringLiteral
        super.setCurrentDateTimeFunction("SYSDATE");
        // Setting list of Oracle's native functions
        //noinspection HardCodedStringLiteral
        dateFunctions.add(new DatabaseFunction("SYSDATE"));
        //noinspection HardCodedStringLiteral
        dateFunctions.add(new DatabaseFunction("SYSTIMESTAMP"));
        //noinspection HardCodedStringLiteral
        dateFunctions.add(new DatabaseFunction("CURRENT_TIMESTAMP"));
        //noinspection HardCodedStringLiteral
        super.sequenceNextValueFunction = "%s.nextval"; // 注意
        //noinspection HardCodedStringLiteral
        super.sequenceCurrentValueFunction = "%s.currval";
    }

    @Override
    public void setConnection(DatabaseConnection conn) {
        //noinspection HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral,
        // HardCodedStringLiteral
        //noinspection HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral,
        // HardCodedStringLiteral
        reservedWords.addAll(Arrays.asList("GROUP", "USER", "SESSION", "PASSWORD", "RESOURCE", "START", "SIZE", "UID", "DESC", "ORDER")); //more reserved words not returned by driver
        Connection sqlConn = null;
        if (!(conn instanceof OfflineConnection)) {
            try {
                /*
                 * Don't try to call getWrappedConnection if the conn instance is
                 * is not a JdbcConnection. This happens for OfflineConnection.
                 * see https://liquibase.jira.com/browse/CORE-2192
                 */
                if (conn instanceof JdbcConnection) {
                    sqlConn = ((JdbcConnection) conn).getWrappedConnection();
                }
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }

            if (sqlConn != null) {
                try {
                    //noinspection HardCodedStringLiteral
                    reservedWords.addAll(Arrays.asList(sqlConn.getMetaData().getSQLKeywords().toUpperCase().split(",\\s*")));
                } catch (SQLException e) {
                    //noinspection HardCodedStringLiteral
                    LogService.getLog(getClass()).info(LogType.LOG, "Could get sql keywords on OracleDatabase: " + e.getMessage());
                    //can not get keywords. Continue on
                }
                try {
                    Method method = sqlConn.getClass().getMethod("setRemarksReporting", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(sqlConn, true);
                } catch (Exception e) {
                    //noinspection HardCodedStringLiteral
                    LogService.getLog(getClass()).info(LogType.LOG, "Could not set remarks reporting on OracleDatabase: " + e.getMessage());
                    //cannot set it. That is OK
                }
            }
        }
        super.setConnection(conn);
    }

    @Override
    public String getJdbcCatalogName(CatalogAndSchema schema) {
        return null;
    }

    @Override
    public String getJdbcSchemaName(CatalogAndSchema schema) {
        return correctObjectName((schema.getCatalogName() == null) ? schema.getSchemaName() : schema.getCatalogName(), Schema.class);
    }

    @Override
    public String generatePrimaryKeyName(String tableName) {
        if (tableName.length() > 50) {
            //noinspection HardCodedStringLiteral
            return "PK_" + tableName.toUpperCase(Locale.US).substring(0, 27);
        } else {
            //noinspection HardCodedStringLiteral
            return "PK_" + tableName.toUpperCase(Locale.US);
        }
    }

    @Override
    public String getDateLiteral(String isoDate) {
        String normalLiteral = super.getDateLiteral(isoDate);

        if (isDateOnly(isoDate)) {
            StringBuffer val = new StringBuffer();
            //noinspection HardCodedStringLiteral
            val.append("TO_DATE(");
            val.append(normalLiteral);
            //noinspection HardCodedStringLiteral
            val.append(", 'YYYY-MM-DD')");
            return val.toString();
        } else if (isTimeOnly(isoDate)) {
            StringBuffer val = new StringBuffer();
            //noinspection HardCodedStringLiteral
            val.append("TO_DATE(");
            val.append(normalLiteral);
            //noinspection HardCodedStringLiteral
            val.append(", 'HH24:MI:SS')");
            return val.toString();
        } else if (isTimestamp(isoDate)) {
            StringBuffer val = new StringBuffer(26);
            //noinspection HardCodedStringLiteral
            val.append("TO_TIMESTAMP(");
            val.append(normalLiteral);
            //noinspection HardCodedStringLiteral
            val.append(", 'YYYY-MM-DD HH24:MI:SS.FF')");
            return val.toString();
        } else if (isDateTime(isoDate)) {
            int seppos = normalLiteral.lastIndexOf('.');
            if (seppos != -1) {
                normalLiteral = normalLiteral.substring(0, seppos) + "'";
            }
            StringBuffer val = new StringBuffer(26);
            //noinspection HardCodedStringLiteral
            val.append("TO_DATE(");
            val.append(normalLiteral);
            //noinspection HardCodedStringLiteral
            val.append(", 'YYYY-MM-DD HH24:MI:SS')");
            return val.toString();
        }
        //noinspection HardCodedStringLiteral
        return "UNSUPPORTED:" + isoDate;
    }

    @Override
    public boolean supportsAutoIncrement() {
       return true;
    }

    @Override
    public boolean supportsRestrictForeignKeys() {
        return false;
    }

    @Override
    public int getDataTypeMaxParameters(String dataTypeName) {
        //noinspection HardCodedStringLiteral
        if ("BINARY_FLOAT".equals(dataTypeName.toUpperCase())) {
            return 0;
        }
        //noinspection HardCodedStringLiteral
        if ("BINARY_DOUBLE".equals(dataTypeName.toUpperCase())) {
            return 0;
        }
        return super.getDataTypeMaxParameters(dataTypeName);
    }


    @Override
    public boolean jdbcCallsCatalogsSchemas() {
        return true;
    }

    @Override
    public String generateDatabaseFunctionValue(DatabaseFunction databaseFunction) {
        //noinspection HardCodedStringLiteral
        if ((databaseFunction != null) && "current_timestamp".equalsIgnoreCase(databaseFunction.toString())) {
            return databaseFunction.toString();
        }
        if((databaseFunction instanceof SequenceNextValueFunction) || (databaseFunction instanceof
                SequenceCurrentValueFunction)){
            String quotedSeq = super.generateDatabaseFunctionValue(databaseFunction);
            // replace "myschema.my_seq".nextval with "myschema"."my_seq".nextval
            return quotedSeq.replaceFirst("\"([^\\.\"]+)\\.([^\\.\"]+)\"","\"$1\".\"$2\"");

        }

        return super.generateDatabaseFunctionValue(databaseFunction);
    }

    public boolean isValidOracleIdentifier(String identifier, Class<? extends DatabaseObject> type) {
        if ((identifier == null) || (identifier.length() < 1))
            return false;

        if (!identifier.matches("^(i?)[A-Z][A-Z0-9\\$\\_\\#]*$"))
            return false;

        /*
         * @todo It seems we currently do not have a class for tablespace identifiers, and all other classes
         * we do know seem to be supported as 12cR2 long identifiers, so:
         */
        return (identifier.length() <= 128);
    }


    @Override
    public boolean supportsSequences() {
        return true;
    }

    @Override
    public boolean supportsSchemas() {
        return false;
    }

    @Override
    protected String getConnectionCatalogName() throws DatabaseException {
        if (getConnection() instanceof OfflineConnection) {
            return getConnection().getCatalog();
        }
        try {
            //noinspection HardCodedStringLiteral
            return ExecutorService.getInstance().getExecutor(this).queryForObject(new RawCallStatement("select sys_context( 'userenv', 'current_schema' ) from dual"), String.class);
        } catch (Exception e) {
            //noinspection HardCodedStringLiteral
            LogService.getLog(getClass()).info(LogType.LOG, "Error getting default schema", e);
        }
        return null;
    }

    @Override
    public boolean isReservedWord(String objectName) {
        return reservedWords.contains(objectName.toUpperCase());
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return PRODUCT_NAME;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return PRODUCT_NAME.equals(conn.getDatabaseProductName());
    }


    @Override
    public String getDefaultDriver(String url) {
        //noinspection HardCodedStringLiteral
        if (url.startsWith("jdbc:dm")) {
            return "dm.jdbc.driver.DmDriver";
        }
        return null;
    }

    @Override
    public String getShortName() {
        return "dm";
    }

    @Override
    public Integer getDefaultPort() {
        return PORT;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return true;
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public Set<String> getUserDefinedTypes() {
        if (userDefinedTypes == null) {
            userDefinedTypes = new HashSet<>();
            if ((getConnection() != null) && !(getConnection() instanceof OfflineConnection)) {
                try {
                    try {
                        //noinspection HardCodedStringLiteral
                        userDefinedTypes.addAll(ExecutorService.getInstance().getExecutor(this).queryForList(new RawSqlStatement("SELECT DISTINCT TYPE_NAME FROM ALL_TYPES"), String.class));
                    } catch (DatabaseException e) { //fall back to USER_TYPES if the user cannot see ALL_TYPES
                        //noinspection HardCodedStringLiteral
                        userDefinedTypes.addAll(ExecutorService.getInstance().getExecutor(this).queryForList(new RawSqlStatement("SELECT TYPE_NAME FROM USER_TYPES"), String.class));
                    }
                } catch (DatabaseException e) {
                    //ignore error
                }
            }
        }
        return userDefinedTypes;
    }

}
