package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;

public class Trigger extends AbstractDatabaseObject {
    @Override
    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[0];
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
        return null;
    }
}
