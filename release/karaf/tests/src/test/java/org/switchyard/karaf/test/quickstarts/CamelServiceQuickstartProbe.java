package org.switchyard.karaf.test.quickstarts;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.switchyard.remote.RemoteInvoker;
import org.switchyard.remote.RemoteMessage;
import org.switchyard.remote.http.HttpInvoker;

public class CamelServiceQuickstartProbe extends DeploymentProbe {

    private static final QName SERVICE = new QName( "urn:switchyard-quickstart:camel-service:0.1.0", "JavaDSL");
    
    public CamelServiceQuickstartProbe() {
    }
    
    @Test
    public void testFeatures() throws Exception {
        // Create a new remote client invoker
        String port = System.getProperty("org.switchyard.component.sca.client.port", "8181");
        RemoteInvoker invoker = new HttpInvoker("http://localhost:" + port + "/switchyard-remote");

        // Create the request message
        RemoteMessage message = new RemoteMessage();
        message.setService(SERVICE).setOperation("acceptMessage").setContent("test");

        // Invoke the service
        invoker.invoke(message);
    }
}
