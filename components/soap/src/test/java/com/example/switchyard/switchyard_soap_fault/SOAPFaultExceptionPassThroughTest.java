package com.example.switchyard.switchyard_soap_fault;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.component.test.mixins.cdi.CDIMixIn;
import org.switchyard.component.test.mixins.http.HTTPMixIn;
import org.switchyard.test.SwitchYardRunner;
import org.switchyard.test.SwitchYardTestCaseConfig;

@RunWith(SwitchYardRunner.class)
@SwitchYardTestCaseConfig(config = "switchyard-fault.xml", mixins = { HTTPMixIn.class, CDIMixIn.class })
public class SOAPFaultExceptionPassThroughTest {
	private HTTPMixIn httpMixIn;

	String messagePayload = "<?xml version=\"1.0\"?>"
			+ "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
			+ 	"<soap:Body xmlns:m=\"http://fault.switchyard.org\">"
			+ 		"<m:OperationARequest/>" + ""
			+ 	"</soap:Body>"
			+ "</soap:Envelope> ";
	
	String expectedResponse = ""
			+ "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
			+ 	"<soap:Body>"
			+ 		"<soap:Fault>"
			+ 			"<faultcode>soap:Client</faultcode>"
			+ 			"<faultstring>Generic fault</faultstring>"
			+ 			"<detail>"
			+ 				"<sy:Fault xmlns:sy=\"http://fault.switchyard.org/fault\">"
			+ 					"<FaultCode>CUSTOMFAULTCODE</FaultCode>"
			+ 					"<sy:FaultMessage>Custom fault message</sy:FaultMessage>"
			+ 				"</sy:Fault>"
			+ 			"</detail>"
			+ 		"</soap:Fault>"
			+ 	"</soap:Body>"
			+ "</soap:Envelope>";
	
	@Test
	public void testTest() {
		String response = httpMixIn.postString("http://127.0.0.1:8080/soap-fault-thrower/wsdl", messagePayload);
		Assert.assertEquals(expectedResponse, response);
	}
}
