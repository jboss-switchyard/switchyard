package org.switchyard.karaf.test.quickstarts;

import org.junit.BeforeClass;
import org.junit.Test;

public class CamelServiceQuickstartTest extends AbstractQuickstartTest {
    private static String bundleName = "org.switchyard.quickstarts.switchyard.camel.service";
    private static String featureName = "switchyard-quickstart-camel-service";

    @BeforeClass
    public static void before() throws Exception {
        startTestContainer(featureName, bundleName, null, CamelServiceQuickstartProbe.class);
    }
    
    @Test
    public void testFeatures() throws Exception {
        executeProbe("testFeatures");
    }
}
