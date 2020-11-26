package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.StoredProcedure;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


// 获取存储过程快照
public class ProcedureSnapshotGenerator extends JdbcSnapshotGenerator {


    public ProcedureSnapshotGenerator() {
        super(StoredProcedure.class, new Class[]{Schema.class});
    }


    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (((StoredProcedure) example).getBody() != null) {
            return example;
        }
        // 获取存储过程主体
        StoredProcedure storedProcedure = (StoredProcedure) example;
        Database database = snapshot.getDatabase();
        Schema schema = example.getSchema();
        Connection connection = null;
        if (database.getConnection() instanceof JdbcConnection) {
            connection = ((JdbcConnection) database.getConnection()).getOriginal();
        }
        if (connection != null) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(getBodySql(schema, database, storedProcedure));
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet != null) {
                    if (database instanceof MySQLDatabase) {
                        if ("PROCEDURE".equals(storedProcedure.getAttribute("PROCEDURE_TYPE", String.class).toUpperCase())) {
                            storedProcedure.setBody(resultSet.getString("Create Procedure"));
                        } else {
                            storedProcedure.setBody(resultSet.getString("Create Function"));
                        }
                    } else if (database instanceof OracleDatabase) {
                        storedProcedure.setBody("CREATE OR REPLACE " + getSQL(resultSet));
                    } else {
                        storedProcedure.setBody(getSQL(resultSet));
                    }
                    return storedProcedure;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(StoredProcedure.class)) {
            return;
        }
        //此处不使用从JdbcDatabaseSnapshot 中获取cache方式来获取存储过程，使用SQL方式来获取存储过程
        if (foundObject instanceof Schema) {
            Schema schema = (Schema) foundObject;
            Database database = snapshot.getDatabase();
            List<Map<String, ?>> procedures = ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(getProcedureSql(schema, database)));

            if (procedures != null) {
                for (Map<String, ?> procedure : procedures) {
                    schema.addDatabaseObject(mapToProcedure(procedure, (Schema) foundObject, database));
                }
            }
        }
    }

    private DatabaseObject mapToProcedure(Map<String, ?> procedure, Schema schema, Database database) {
        String name = cleanNameFromDatabase((String) procedure.get("PROCEDURE_NAME"), database);
        StoredProcedure storedProcedure = new StoredProcedure();
        storedProcedure.setName(name);
        storedProcedure.setSchema(schema);
        storedProcedure.setAttribute("liquibase-complete", true);
        if (database instanceof MySQLDatabase) {
            storedProcedure.setAttribute("PROCEDURE_TYPE", procedure.get("PROCEDURE_TYPE"));
        }
        return storedProcedure;
    }

    // 此处只查询存储过程名称
    private String getProcedureSql(Schema schema, Database database) {
        if (database instanceof MySQLDatabase) {
            return "SELECT\n" +
                    "\t`SPECIFIC_NAME` AS PROCEDURE_NAME,\n" +
                    "\t`ROUTINE_SCHEMA` AS SCHEMA_NAME,\n" +
                    "\t`ROUTINE_TYPE` AS PROCEDURE_TYPE\n" +
                    "FROM\n" +
                    "\tINFORMATION_SCHEMA.ROUTINES\n" +
                    "WHERE\n" +
                    "\tROUTINE_SCHEMA = '" + schema.getName() + "'\n" +
                    "\tAND ROUTINE_TYPE IN ('PROCEDURE',\n" +
                    "\t'FUNCTION')\n" +
                    "ORDER BY\n" +
                    "\tROUTINE_NAME";
        } else if (database instanceof OracleDatabase) {
            return "SELECT\n" +
                    "    OBJECT_NAME AS PROCEDURE_NAME,\n" +
                    "\tOWNER AS SCHEMA_NAME\n" +
                    "FROM\n" +
                    "\tALL_OBJECTS\n" +
                    "WHERE\n" +
                    "\tOBJECT_TYPE IN ('PROCEDURE','FUNCTION')\n" +
                    "\tAND OWNER = '" + schema.getName() + "'\n" +
                    "ORDER BY\n" +
                    "\tOBJECT_NAME";
        } else if (database instanceof MSSQLDatabase) {
            return "SELECT p.name as PROCEDURE_NAME\n" +
                    "FROM " + schema.getCatalogName() + ".sys.all_objects p\n" + // mssql 中getCatalogName 获取的是数据库名称 getName获取的是DBO
                    "LEFT OUTER JOIN " + schema.getCatalogName() + ".sys.extended_properties ep ON ep.class=1 AND ep.major_id=p.object_id AND ep.minor_id=0 AND ep.name='MS_Description'\n" +
                    "WHERE p.type IN ('P','PC','X','TF','FN','IF') AND p.schema_id=1\n" +
                    "ORDER BY p.name";
        } else {
            throw new UnexpectedLiquibaseException("Don't know how to query for procedure on " + database);
        }
    }

    private String getBodySql(Schema schema, Database database, StoredProcedure storedProcedure) {
        if (database instanceof MySQLDatabase) {
            String type = storedProcedure.getAttribute("PROCEDURE_TYPE", String.class).toUpperCase();
            return "SHOW CREATE " + type + " " + schema.getName() + ".`" + storedProcedure.getName() + "`";
        } else if (database instanceof OracleDatabase) {
            return "SELECT TEXT FROM DBA_SOURCE WHERE  OWNER='" + schema.getName() + "' AND NAME='" + storedProcedure.getName() + "' ORDER BY LINE";
        } else if (database instanceof MSSQLDatabase) {
            return schema.getCatalogName() + ".sys.sp_helptext 'dbo." + storedProcedure.getName() + "'";
        } else {
            throw new UnexpectedLiquibaseException("Don't know how to query for procedure on " + database);
        }
    }



}
