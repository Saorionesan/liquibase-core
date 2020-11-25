package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.changelog.column.LiquibaseColumn;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawCallStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtils;
import liquibase.util.StringUtils;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class KingBaseDatabase extends AbstractJdbcDatabase {
    public static final String PRODUCT_NAME = "KingbaseES";
    private static final int KINGBASE_DEFAULT_TCP_PORT_NUMBER = 54321;
    private static final Logger LOG = LogService.getLog(KingBaseDatabase.class);

    private Set<String> systemTablesAndViews = new HashSet<>();


    private Set<String> reservedWords = new HashSet<>();

    public KingBaseDatabase() {
        super.setCurrentDateTimeFunction("NOW()");
        // "Reserved" or "reserved (can be function or type)" in PostgreSQL
        // from https://www.postgresql.org/docs/9.6/static/sql-keywords-appendix.html
        reservedWords.addAll(Arrays.asList("ALL", "ANALYSE", "ANALYZE", "AND", "ANY", "ARRAY", "AS", "ASC",
                "ASYMMETRIC", "AUTHORIZATION", "BINARY", "BOTH", "CASE", "CAST", "CHECK", "COLLATE", "COLLATION",
                "COLUMN", "CONCURRENTLY", "CONSTRAINT", "CREATE", "CROSS", "CURRENT_CATALOG", "CURRENT_DATE",
                "CURRENT_ROLE", "CURRENT_SCHEMA", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "DEFAULT",
                "DEFERRABLE", "DESC", "DISTINCT", "DO", "ELSE", "END", "EXCEPT", "FALSE", "FETCH", "FOR", "FOREIGN",
                "FREEZE", "FROM", "FULL", "GRANT", "GROUP", "HAVING", "ILIKE", "IN", "INITIALLY", "INNER", "INTERSECT",
                "INTO", "IS", "ISNULL", "JOIN", "LATERAL", "LEADING", "LEFT", "LIKE", "LIMIT", "LOCALTIME",
                "LOCALTIMESTAMP", "NATURAL", "NOT", "NOTNULL", "NULL", "OFFSET", "ON", "ONLY", "OR", "ORDER", "OUTER",
                "OVERLAPS", "PLACING", "PRIMARY", "REFERENCES", "RETURNING", "RIGHT", "SELECT", "SESSION_USER",
                "SIMILAR", "SOME", "SYMMETRIC", "TABLE", "TABLESAMPLE", "THEN", "TO", "TRAILING", "TRUE", "UNION",
                "UNIQUE", "USER", "USING", "VARIADIC", "VERBOSE", "WHEN", "WHERE", "WINDOW", "WITH"));
        super.sequenceNextValueFunction = "nextval('%s')";
        super.sequenceCurrentValueFunction = "currval('%s')";
        super.unmodifiableDataTypes.addAll(Arrays.asList("bool", "int4", "int8", "float4", "float8", "bigserial", "serial", "oid", "bytea", "date", "timestamptz", "text"));
        super.unquotedObjectsAreUppercased=false;
    }

    @Override
    public boolean equals(Object o) {
        // Actually, we don't need and more specific checks than the base method. This exists just to make SONAR happy.
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        // Actually, we don't need and more specific hashing than the base method. This exists just to make SONAR happy.
        return super.hashCode();
    }

    @Override
    public Set<String> getSystemViews() {
        return systemTablesAndViews;
    }



    @Override
    protected String getDefaultDatabaseProductName() {
        return PRODUCT_NAME;
    }


    @Override // 该方法判断是否为liquibase实现的database
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:kingbase8:")) {
            return "com.kingbase8.Driver";
        }
        return null;
    }

    @Override
    public String getShortName() {
        return "kingbase";
    }

    @Override
    public Integer getDefaultPort() {
        return KINGBASE_DEFAULT_TCP_PORT_NUMBER;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supportsCatalogInObjectName(Class<? extends DatabaseObject> type) {
        return false;
    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    @Override
    public String getDatabaseChangeLogTableName() {
        return super.getDatabaseChangeLogTableName().toLowerCase(Locale.US);
    }

    @Override
    public String getDatabaseChangeLogLockTableName() {
        return super.getDatabaseChangeLogLockTableName().toLowerCase(Locale.US);
    }

    @Override
    public void setConnection(DatabaseConnection conn) {
        super.setConnection(conn);
    }


    @Override
    public boolean isSystemObject(DatabaseObject example) {
        // All tables in the schemas pg_catalog and pg_toast are definitely system tables.
        if
        (
                (example instanceof Table)
                        && (example.getSchema() != null)
                        && (
                        ("SYS_CATALOG".equals(example.getSchema().getName()))
                                || ("SYS_TOAST".equals(example.getSchema().getName()))
                )
        ) {
            return true;
        }

        return super.isSystemObject(example);
    }

    @Override
    public String getAutoIncrementClause() {
        return "";
    }

    @Override
    public boolean generateAutoIncrementStartWith(BigInteger startWith) {
        return false;
    }

    @Override
    public boolean generateAutoIncrementBy(BigInteger incrementBy) {
        return false;
    }

    @Override
    public String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if ((quotingStrategy == ObjectQuotingStrategy.LEGACY) && hasMixedCase(objectName)) {
            return "\"" + objectName + "\"";
        } else if (objectType != null && LiquibaseColumn.class.isAssignableFrom(objectType)) {
            return (objectName != null && !objectName.isEmpty()) ? objectName.trim() : objectName;
        }

        return super.escapeObjectName(objectName, objectType);
    }

    @Override
    public String correctObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if ((objectName == null) || (quotingStrategy != ObjectQuotingStrategy.LEGACY)) {
            return super.correctObjectName(objectName, objectType);
        }
        if (objectName.contains("-")
                || hasMixedCase(objectName)
                || startsWithNumeric(objectName)
                || isReservedWord(objectName)) {
            return objectName;
        } else {
            return objectName.toLowerCase(Locale.US);
        }
    }

    protected boolean hasMixedCase(String tableName) {
        if (tableName == null) {
            return false;
        }
        return StringUtils.hasUpperCase(tableName) && StringUtils.hasLowerCase(tableName);
    }

    @Override
    public boolean isReservedWord(String tableName) {
        return reservedWords.contains(tableName.toUpperCase());
    }

    @Override
    protected SqlStatement getConnectionSchemaNameCallStatement() {
        return new RawCallStatement("select current_schema()");
    }

    @Override
    public String generatePrimaryKeyName(final String tableName) {
        return tableName.toUpperCase(Locale.US) + "_PKEY";
    }


    @Override
    public CatalogAndSchema.CatalogAndSchemaCase getSchemaAndCatalogCase() {
        return CatalogAndSchema.CatalogAndSchemaCase.LOWER_CASE;
    }


}
