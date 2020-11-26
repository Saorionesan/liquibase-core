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
import liquibase.structure.core.Table;
import liquibase.structure.core.Trigger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 生成创建触发器快照
 *
 */
public class TriggerSnapshotGenerator extends JdbcSnapshotGenerator {

    public TriggerSnapshotGenerator() {
        super(Trigger.class, new Class[]{Schema.class}); //添加到所属的模式中
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (((Trigger) example).getBody() != null) {
            return example;
        }
        // 获取存储过程主体
        Trigger trigger = (Trigger) example;
        Database database = snapshot.getDatabase();
        Schema schema = example.getSchema();
        Connection connection = null;
        if (database.getConnection() instanceof JdbcConnection) {
            connection = ((JdbcConnection) database.getConnection()).getOriginal();
        }
        if (connection != null) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(getBodySql(schema, database, trigger));
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet != null) {
                    if (database instanceof MySQLDatabase) {
                            trigger.setBody(resultSet.getString("SQL Original Statement"));
                    } else if (database instanceof OracleDatabase) {
                        trigger.setBody("CREATE " + getSQL(resultSet));
                    } else {
                        trigger.setBody(getSQL(resultSet));
                    }
                    return trigger;
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
        if (!snapshot.getSnapshotControl().shouldInclude(Trigger.class)) {
            return;
        }
        //此处不使用从JdbcDatabaseSnapshot 中获取cache方式来获取存储过程，使用SQL方式来获取存储过程
        if (foundObject instanceof Schema) {
            Schema schema = (Schema) foundObject;
            Database database = snapshot.getDatabase();
            List<Map<String, ?>> triggers = ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(getTriggerSql(schema, database)));
            if (triggers != null) {
                for (Map<String, ?> trigger : triggers) {
                    schema.addDatabaseObject(mapToTrigger(trigger, (Schema) foundObject, database));
                }
            }
        }
    }


    private DatabaseObject mapToTrigger(Map<String, ?> trigger, Schema schema, Database database) {
        String name = cleanNameFromDatabase((String) trigger.get("TRIGGER_NAME"), database);
        Trigger addTrigger = new Trigger(schema.getCatalogName(),schema.getName(),name);
        addTrigger.setAttribute("liquibase-complete", true);
        String tableName=(String)trigger.get("TABLE_NAME");
        Table table=new Table();
        table.setName(tableName);
        table.setSchema(schema);
        addTrigger.setTable(table);
        return addTrigger;
    }

    private String getBodySql(Schema schema, Database database, Trigger trigger) {
        if (database instanceof MySQLDatabase) {
            return "SHOW CREATE TRIGGER "+schema.getName()+"."+trigger.getName();
        } else if (database instanceof OracleDatabase) {
            return "SELECT\n" +
                    "\tTEXT\n" +
                    "FROM\n" +
                    "\tDBA_SOURCE\n" +
                    "WHERE\n" +
                    "\tTYPE ='TRIGGER' \n" +
                    "\tAND OWNER ='" +schema.getName()+"'\n"+
                    "\tAND NAME ='" +trigger.getName()+"'\n"+
                    "ORDER BY\n" +
                    "\tLINE";
        } else if (database instanceof MSSQLDatabase) {
            return schema.getCatalogName() + ".sys.sp_helptext 'dbo." + trigger.getName() + "'";
        } else {
            throw new UnexpectedLiquibaseException("Don't know how to query for procedure on " + database);
        }
    }

    // 此处只查询触发器名称
    private String getTriggerSql(Schema schema, Database database) {
        if (database instanceof MySQLDatabase) {
            return "SELECT `TRIGGER_NAME` AS TRIGGER_NAME,\n" +
                    "`TRIGGER_SCHEMA` AS SCHEMA_NAME,\n" +
                    "`EVENT_OBJECT_TABLE` AS TABLE_NAME\n" +
                    "FROM INFORMATION_SCHEMA.TRIGGERS\n" +
                    "WHERE TRIGGER_SCHEMA = '"+schema.getName()+"'";
        } else if (database instanceof OracleDatabase) {
            return "SELECT\n" +
                    "\tOWNER AS SCHEMA_NAME,\n" +
                    "\tTRIGGER_NAME AS TRIGGER_NAME,\n" +
                    "\tTABLE_NAME AS TABLE_NAME\n" +
                    "FROM\n" +
                    "\tALL_TRIGGERS\n" +
                    "WHERE\n" +
                    "\tTABLE_OWNER = '"+schema.getName()+"'\n"+
                    "\tAND BASE_OBJECT_TYPE='TABLE'\n" +
                    "ORDER BY\n" +
                    "\tTRIGGER_NAME";
        } else if (database instanceof MSSQLDatabase) {
            return "SELECT t.name AS TRIGGER_NAME,\n" +
                    "'dbo' AS SCHMEA_NAME,\n" +
                    "obj.name AS TABLE_NAME \n"+
                    "FROM \n" +
                    schema.getCatalogName()+".sys.triggers t,"+schema.getCatalogName()+".sys.all_objects o, "+schema.getCatalogName()+".sys.sysobjects obj \n"+
                    "WHERE o.object_id=t.object_id AND obj.id = t.parent_id AND obj.xtype ='U' AND o.schema_id=1\n"+
                    "ORDER BY t.name";
        } else {
            throw new UnexpectedLiquibaseException("Don't know how to query for procedure on " + database);
        }
    }

}
