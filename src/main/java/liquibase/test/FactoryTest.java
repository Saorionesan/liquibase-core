package liquibase.test;

import liquibase.CatalogAndSchema;
import liquibase.change.Change;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.core.CreateProcedureChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.CreateViewChange;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.ObjectChangeFilter;
import liquibase.diff.output.changelog.ChangeGeneratorFactory;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.osgi.OSGiPackageScanClassResolver;
import liquibase.servicelocator.DefaultPackageScanClassResolver;
import liquibase.servicelocator.PackageScanClassResolver;
import liquibase.servicelocator.ServiceLocator;
import liquibase.snapshot.*;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.DatabaseObjectFactory;
import liquibase.structure.core.Schema;
import liquibase.structure.core.View;
import liquibase.test.liquibaseTest.LBUtils;
import liquibase.test.liquibaseTest.SQLUtils;
import liquibase.util.StringUtils;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FactoryTest {

    public static String url="jdbc:dm://192.168.218.101:5236/OA9_WC";
    public static String userName="OA9_WC";
    public static String pass="111111111";
    public static String driver="dm.jdbc.driver.DmDriver";

    public static String mysqlUrl="jdbc:mysql://192.168.218.20:3306/ysk";
    public static String mysqlUserName="root";
    public static String mysqlPass="Gepoint";
    public static String mysqlDriver="com.mysql.jdbc.Driver";

    public static String oracleUrl="jdbc:oracle:thin:@192.168.212.110:1521/ORCL";
    public static String oracleUserName="admin";
    public static String oraclePass="admin123";
    public static String oracleDriver="oracle.jdbc.OracleDriver";

    public static String kingbaseUrl="jdbc:kingbase8://192.168.218.35:54321/TEST";
    public static String kingbaseUserName="SYSTEM";
    public static String kingbasePass="Gepoint";
    public static String kingbaseDriver="com.kingbase8.Driver";

    public static String pgUrl="jdbc:postgresql://192.168.218.35:5432/postgres";
    public static String pgUserName="postgres";
    public static String pgPass="postgres";
    public static String pgDriver="org.postgresql.Driver";

    public static String hgUrl="jdbc:highgo://192.168.218.33:5866/highgo";
    public static String hgUserName="highgo";
    public static String hgPass="highgo123";
    public static String hgDriver="com.highgo.jdbc.Driver";


    public static String msUrl="jdbc:sqlserver://192.168.219.88\\sql2016:1433;databaseName=master";
    public static String msUsername="sa";
    public static String msPass="Epoint@123";
    public static String msDriver="com.microsoft.sqlserver.jdbc.SQLServerDriver";



    public static void main(String[] args) throws Exception {
     String sourceSchema="ADMIN"; //OA9_WC EPOINTF9_5 3.0CSJYJ
     String targetSchema="YSK";
     Connection orlCon=getConnection(oracleDriver,oracleUrl,oracleUserName,oraclePass);
     Connection mysql=getConnection(mysqlDriver,mysqlUrl,mysqlUserName,mysqlPass);
     Connection kbCon=getConnection(kingbaseDriver,kingbaseUrl,kingbaseUserName,kingbasePass);
     Connection pgCon=getConnection(pgDriver,pgUrl,pgUserName,pgPass);
     String driverName=orlCon.getMetaData().getDriverName();
     Connection dm=getConnection(driver,url,userName,pass);
     Connection hg=getConnection(hgDriver,hgUrl,hgUserName,hgPass);
     Connection ms=getConnection(msDriver,msUrl,msUsername,msPass);

    PackageScanClassResolver resolver = new DefaultPackageScanClassResolver();
    LBResolverServiceLocator serviceLocator = new LBResolverServiceLocator(resolver);
    if(driverName.startsWith("Oracle")){
        serviceLocator.addExtensions("ora");
    }
    ServiceLocator.setInstance(serviceLocator);
    ChangeGeneratorFactory.reset();

    Database sourceDatabase= DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(orlCon));
    Database targetDatabase= DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(orlCon));
    // 第一个参数为要比较的目标模式名，第二个参数为源模式名
    CompareControl.ComputedSchemas computedSchemas=CompareControl.computeSchemas(targetSchema,sourceSchema,null,null,null,null,null,sourceDatabase);
    CompareControl.SchemaComparison[] finalSchemaComparisons = (computedSchemas == null) ? null : computedSchemas.finalSchemaComparisons;

    finalSchemaComparisons[0].setOutputSchemaAs(targetSchema);
    long startTime=System.currentTimeMillis();
    if (finalSchemaComparisons != null && targetDatabase != null) {
            schemaCorrect(finalSchemaComparisons,targetSchema, targetDatabase);
        }
    final String compareTypes = null;
    final Class[] snapshotType = parseSnapshotTypes(new String[0]);
    final CompareControl control = new CompareControl(finalSchemaComparisons, compareTypes);
    final DatabaseSnapshot sourceSnapshot = createDatabaseSnapshot(sourceDatabase, sourceSchema, new LBSnapshotListener(), snapshotType, null);
    sourceSnapshot.setSchemaComparisons(control.getSchemaComparisons());
    System.out.println("快照："+sourceSnapshot.getDatabase());
    final DatabaseSnapshot targetSnapshot = createDatabaseSnapshot(targetDatabase, targetSchema, new LBSnapshotListener(), snapshotType, null);
    targetSnapshot.setSchemaComparisons(control.getSchemaComparisons());
    System.out.println("目标快照:"+targetSnapshot.getDatabase());
    // 第一个参数为目标要比较的模式，第二个参数为源模式
    final List<ChangeSet> changesets = performDiff(control, targetSnapshot, sourceSnapshot); // 比较控制器 源快照、目标快照
        StringWriter writer=new StringWriter();
        for (ChangeSet changeSet:changesets){
        List<Change> list=changeSet.getChanges();
        for (Change change:list){
            if(change instanceof CreateProcedureChange){
                System.out.println("test");
            }
            try {
                targetDatabase.saveStatements(change, null, writer);
                System.out.print(writer.toString());
            }
            catch (Exception e) {
            }
        }
    }
   writer.flush();
    }

    private static Connection getConnection(String driver,String url,String userName,String pass) throws Exception {
        Class.forName(driver);
        Connection connection=DriverManager.getConnection(url,userName,pass);
        return connection;
    }


    private static void schemaCorrect( CompareControl.SchemaComparison[] comps, String targetCatalogs, final Database targetDatabase) {
        if (targetCatalogs == null && targetDatabase.supportsCatalogs()) {
            targetCatalogs = targetDatabase.getDefaultCatalogName();
            for ( CompareControl.SchemaComparison comp : comps) {
                 CatalogAndSchema ref = comp.getReferenceSchema();
                if (!ref.getCatalogName().equals(targetCatalogs)) {
                    try {
                         Field f = ref.getClass().getDeclaredField("catalogName");
                         f.setAccessible(true);
                         f.set(ref, targetCatalogs);
                    }
                    catch (Exception e) {
                        System.out.println("出现了报错");
                    }
                }
            }
        }
    }


    public static Class<? extends DatabaseObject>[] parseSnapshotTypes(final String... snapshotTypes) {
        if (snapshotTypes == null || snapshotTypes.length == 0 || snapshotTypes[0] == null) {
            return null;
        }
        final Set<Class<? extends DatabaseObject>> types = (Set<Class<? extends DatabaseObject>>) DatabaseObjectFactory.getInstance().parseTypes(StringUtils.join(snapshotTypes, ","));
        return types.toArray(new Class[0]);
    }

    private static List<ChangeSet> performDiff(final CompareControl control, final DatabaseSnapshot targetSnapshot, final DatabaseSnapshot sourceSnapshot) {
        try {
            DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(sourceSnapshot, targetSnapshot, control);
            if (targetSnapshot.getDatabase() instanceof OracleDatabase) {
                fixLiquibaseOracleViews(diffResult.getMissingObjects((Class) View.class), control);
            }
            boolean includeCatalog = false;
            boolean includeSchema = true;
            boolean includeTablespace = true;
            DiffOutputControl diffOutputControl = new DiffOutputControl(includeCatalog, includeSchema, includeTablespace, control.getSchemaComparisons());
            //ObjectChangeFilter objectChangeFilter = (ObjectChangeFilter)new LBObjectChangeFilter(options, sourceSnapshot.getDatabase());
            //diffOutputControl.setObjectChangeFilter(objectChangeFilter);
            CompareControl.SchemaComparison[] schemaComparisons;
            for (int length = (schemaComparisons = control.getSchemaComparisons()).length, i = 0; i < length; ++i) {
                CompareControl.SchemaComparison schema = schemaComparisons[i];
                diffOutputControl.addIncludedSchema(schema.getReferenceSchema());
                diffOutputControl.addIncludedSchema(schema.getComparisonSchema());
            }
            DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult, diffOutputControl);
            diffToChangeLog.setChangeSetAuthor("dbeaver");
            List<ChangeSet> changeSets = diffToChangeLog.generateChangeSets();
            fixLiquibaseChanges(changeSets, control, sourceSnapshot.getDatabase(), targetSnapshot.getDatabase());
            return changeSets;
        } catch (Exception e) {
            System.out.println("出现了报错");
            return null;
        }
    }

    private static void fixLiquibaseOracleViews(final Set<View> views, final CompareControl control) {
        views.parallelStream().forEach(view -> {
            String currSchema="";
            Schema schema = view.getSchema();
            if (schema != null) {
                currSchema = schema.getName();
            }
        });
    }

    private static void fixLiquibaseChanges(final List<ChangeSet> changeSets, final CompareControl control, final Database sourceDatabase, final Database targetDatabase) {
        for ( ChangeSet cs : changeSets) {
            for ( Change change : cs.getChanges()) {
                if (change instanceof CreateTableChange) {
                    String currSchema = ((CreateTableChange)change).getSchemaName();
                    if (currSchema == null || !isSourceSchema(control, currSchema)) {
                        continue;
                    }
                    ((CreateTableChange)change).setSchemaName(currSchema);
                }
                else if (change instanceof CreateViewChange) {
                    String currSchema = ((CreateViewChange)change).getSchemaName();
                    if (currSchema == null || !isSourceSchema(control, currSchema)) {
                        continue;
                    }
                    ((CreateViewChange)change).setSchemaName(currSchema);
                }
                else if (change instanceof CreateIndexChange) {
                    String currSchema = ((CreateIndexChange)change).getSchemaName();
                    if (currSchema == null || !isSourceSchema(control, currSchema)) {
                        continue;
                    }
                    ((CreateIndexChange)change).setSchemaName(currSchema);
                }else if (change instanceof ChangeWithColumns) {
                    List<ColumnConfig> lists=((ChangeWithColumns)change).getColumns();
                    for (ColumnConfig cc : lists) {
                        fixColumnDefinition(sourceDatabase, targetDatabase, cc);
                    }
                }
            }
        }
    }

    private static boolean isSourceSchema(final CompareControl control, final String shemaName) {
        CompareControl.SchemaComparison[] schemaComparisons;
        for (int length = (schemaComparisons = control.getSchemaComparisons()).length, i = 0; i < length; ++i) {
            final CompareControl.SchemaComparison sc = schemaComparisons[i];
            if (sc.getReferenceSchema() != null) {
                if (sc.getReferenceSchema().getSchemaName() != null && sc.getReferenceSchema().getSchemaName().equalsIgnoreCase(shemaName)) {
                    return true;
                }
                if (sc.getReferenceSchema().getCatalogName() != null && sc.getReferenceSchema().getCatalogName().equalsIgnoreCase(shemaName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void fixColumnDefinition(final Database sourceDatabase, final Database targetDatabase, final ColumnConfig columnConfig) {
        if (targetDatabase instanceof MySQLDatabase) {
            final String type = columnConfig.getType();
            if (type != null && type.startsWith("GEOMETRY")) {
                columnConfig.setType(stripColumnTypeModifiers(type));
            }
        }
    }

    public static String stripColumnTypeModifiers(String type) {
        int startPos = type.indexOf("(");
        if (startPos != -1) {
            int endPos = type.lastIndexOf(")");
            if (endPos != -1) {
                return type.substring(0, startPos);
            }
        }
        return type;
    }

    protected static DatabaseSnapshot createDatabaseSnapshot(final Database database,String sourceSchemaName,final SnapshotListener snapshotListener, final Class<? extends DatabaseObject>[] snapshotTypes, final ObjectChangeFilter objectChangeFilter) {
        final List<CatalogAndSchema> catalogAndSchemas = new ArrayList<>();
        catalogAndSchemas.add(new CatalogAndSchema(sourceSchemaName, null));
        try {
            final SnapshotControl snapshotControl = new SnapshotControl(database, objectChangeFilter, (Class[])snapshotTypes);
            //if (input == null) {
            //    return (DatabaseSnapshot)new EmptyDatabaseSnapshot(database, snapshotControl);
            //}
            if (snapshotListener != null) {
                snapshotControl.setSnapshotListener(snapshotListener);
            }
            database.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
            return SnapshotGeneratorFactory.getInstance().createSnapshot(catalogAndSchemas.toArray(new CatalogAndSchema[0]), database, snapshotControl);
        }
        catch (Exception e) {
            System.out.println("创建snpot时出现了错误");
            e.printStackTrace();
            return null;
        }
    }
}
