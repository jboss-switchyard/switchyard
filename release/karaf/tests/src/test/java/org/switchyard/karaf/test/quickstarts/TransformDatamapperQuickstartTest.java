package org.switchyard.karaf.test.quickstarts;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TransformDatamapperQuickstartTest extends AbstractQuickstartTest {
    private static String bundleName = "org.switchyard.quickstarts.switchyard.transform.datamapper";
    private static String featureName = "switchyard-quickstart-transform-datamapper";

    @BeforeClass
    public static void before() throws Exception {
        startTestContainer(featureName, bundleName, null, CamelDozerQuickstartProbe.class);
    }
    
    @Ignore("https://issues.jboss.org/browse/SWITCHYARD-2869")
    @Test
    public void testFeatures() throws Exception {
        executeProbe("testFeatures");
    }
}
