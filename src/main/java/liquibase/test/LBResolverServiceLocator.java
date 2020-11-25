package liquibase.test;

import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.CustomResolverServiceLocator;
import liquibase.servicelocator.PackageScanClassResolver;

import java.util.ArrayList;
import java.util.List;

public class LBResolverServiceLocator extends CustomResolverServiceLocator {

    private List<String> extPackageNames;

    public LBResolverServiceLocator(PackageScanClassResolver classResolver) {
        super(classResolver);
        this.extPackageNames = new ArrayList<>();
    }

    public void setResourceAccessor( ResourceAccessor resourceAccessor) {
        super.setResourceAccessor(resourceAccessor);
        this.getPackages().remove("liquibase.ext");
    }

    public List<String> getExtPackageNames() {
        return this.extPackageNames;
    }

    public void addExtensions(final String extPackageName) {
        if (this.extPackageNames.contains(extPackageName)) {
            return;
        }
        this.extPackageNames.add(extPackageName);
        this.addPackageToScan("liquibase.ext." + extPackageName);
    }

}
