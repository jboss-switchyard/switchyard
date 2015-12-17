package com.example.switchyard.switchyard_soap_fault;


import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.soap.SOAPFaultException;

import org.switchyard.component.bean.Service;

@Service(FaultBean.class)
public class FaultBeanImpl implements FaultBean {
	@Override
	public void OperationA() throws Exception {
        MessageFactory fac = MessageFactory.newInstance();

        //Create a SOAPFault and throw it through SOAPFaultException
        SOAPMessage soapMessage = fac.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body = envelope.getBody();

        //Create a generic SOAPFault object
        SOAPFault soapFault = body.addFault();
        soapFault.setFaultCode("Client");
        soapFault.setFaultString("Generic fault");

        //Add custom fault detail information
        Detail customFaultDetail = soapFault.addDetail();
        
        Name customFaultElementName = envelope.createName("Fault", "sy", "http://fault.switchyard.org/fault");
        DetailEntry customFaultElement = customFaultDetail.addDetailEntry(customFaultElementName);
        
        // Add a custom fault code element
        SOAPElement customFaultCodeElement = customFaultElement.addChildElement("FaultCode");
        customFaultCodeElement.addTextNode("CUSTOMFAULTCODE");
        
        // Add a custom fault message element with qualified name
        SOAPElement customQNamedFaultElement = customFaultElement.addChildElement(envelope.createName("FaultMessage", "sy", "http://fault.switchyard.org/fault"));
        customQNamedFaultElement.addTextNode("Custom fault message");
		
		SOAPFaultException soapFaultException = new SOAPFaultException(soapFault);
		
		throw soapFaultException;
	}
	
}
