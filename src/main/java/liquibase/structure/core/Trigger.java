package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

public class Trigger extends AbstractDatabaseObject {

    public Trigger() {
    }

    public Trigger(String catalogName, String schemaName, String triggerName) {
        this.setSchema(new Schema(catalogName, schemaName));
        this.setName(triggerName);
    }


    @Override
    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    @Override
    public String getName() {
        return getAttribute("name", String.class);
    }

    @Override
    public Trigger setName(String name) {
        this.setAttribute("name", name);
        return this;
    }

    @Override
    public Schema getSchema() {
        return getAttribute("schema", Schema.class);
    }

    public Trigger setSchema (Schema schema) {
        this.setAttribute("schema", schema);
        return this;
    }


    //设置触发器作用的表
    public Trigger setTable (Table table) {
        this.setAttribute("table", table);
        return this;
    }

    public Table getTable() {
        return getAttribute("table", Table.class);
    }

    //设置触发器的定义语句
    public String getBody() {
        return getAttribute("body", String.class);
    }

    public Trigger setBody(String body) {
        setAttribute("body", body);
        return this;
    }

    @Override
    public boolean equals(Object obj) { //判断两个Trigger是否相等
        if (this == obj) return true;
        if ((obj == null) || (getClass() != obj.getClass())) return false;

        Trigger that = (Trigger) obj;

        if ((this.getSchema() != null) && (that.getSchema() != null)) {
            boolean schemasEqual = StringUtils.trimToEmpty(this.getSchema().getName()).equalsIgnoreCase(StringUtils.trimToEmpty(that.getSchema().getName()));
            if (!schemasEqual) {
                return false;
            }
        }
        return getName().equalsIgnoreCase(that.getName());
    }

    @Override
    public int hashCode() {
        return StringUtils.trimToEmpty(this.getName()).toLowerCase().hashCode();
    }

}
