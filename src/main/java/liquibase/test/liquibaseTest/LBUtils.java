package liquibase.test.liquibaseTest;

import liquibase.diff.compare.CompareControl;

public class LBUtils {
    public static String getTargetSchema(final CompareControl control, final String sourceSchema) {
        CompareControl.SchemaComparison[] schemaComparisons;
        for (int length = (schemaComparisons = control.getSchemaComparisons()).length, i = 0; i < length; ++i) {
            final CompareControl.SchemaComparison sc = schemaComparisons[i];
            if (sc.getReferenceSchema() != null) {
                if (sc.getReferenceSchema().getSchemaName() != null && sc.getReferenceSchema().getSchemaName().equalsIgnoreCase(sourceSchema)) {
                    return sc.getComparisonSchema().getSchemaName();
                }
                if (sc.getReferenceSchema().getCatalogName() != null && sc.getReferenceSchema().getCatalogName().equalsIgnoreCase(sourceSchema)) {
                    return sc.getComparisonSchema().getCatalogName();
                }
            }
        }
        return null;
    }


}
