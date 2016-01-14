package org.switchyard.component.soap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.component.test.mixins.http.HTTPMixIn;
import org.switchyard.test.MockHandler;
import org.switchyard.test.SwitchYardRunner;
import org.switchyard.test.SwitchYardTestCaseConfig;
import org.switchyard.test.SwitchYardTestKit;

@RunWith(SwitchYardRunner.class)
@SwitchYardTestCaseConfig(config = "soap-property-switchyard.xml", mixins = { HTTPMixIn.class })
public class SOAPPropertyExtensionTest {
    private static final String SOAP11_ENDPOINT = "http://localhost:18001/soap11/HelloWebService";

    //@formatter:off
    private static final String RESPONSE =
              "<test:sayHelloResponse xmlns:test=\"urn:switchyard-component-soap:test-ws:1.0\">"
            + "   <return>Hello, SwitchYard!</return>"
            + "</test:sayHelloResponse>";
    private static final String FAULT = "<message>ERROR!</message>";
    //@formatter:on

    private SwitchYardTestKit _testKit;
    private HTTPMixIn _httpMixIn;

    private MockHandler _mock;

    @Before
    public void setUp() {
        _mock = _testKit.registerInOutService("HelloSOAPService");
    }

    @Test
    public void toSOAP11Endpoint_soap11() {
        _mock.replyWithOut(RESPONSE);
        _httpMixIn.postResourceAndTestXML(SOAP11_ENDPOINT, "soap11-request.xml", "soap11-response.xml");
    }

}
