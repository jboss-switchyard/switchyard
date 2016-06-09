/*
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.switchyard.transform.jaxb.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.MarshalException;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.namespace.QName;

import org.switchyard.Message;
import org.switchyard.SwitchYardException;
import org.switchyard.common.type.Classes;
import org.switchyard.common.xml.QNameUtil;
import org.switchyard.config.model.Scannable;
import org.switchyard.transform.BaseTransformer;
import org.switchyard.transform.internal.TransformMessages;


/**
 * JAXB Marshalling transformer.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 *
 * @param <F> From Type
 * @param <T> To Type.
 */
@Scannable(false)
public class JAXBMarshalTransformer<F, T> extends BaseTransformer<Message, Message> {

    private JAXBContext _jaxbContext;
    private boolean _isAttachmentEnabled;
    private boolean _isXOPPackage;

    /**
     * Public constructor.
     * @param from From type.
     * @param to To type.
     * @param contextPath JAXB context path (Java package).
     * @param isAttachmentEnabled true if attachment to be enabled.
     * @param isXOPPackage true if XOP Package to be enabled.
     * @throws SwitchYardException Failed to create JAXBContext.
     */
    public JAXBMarshalTransformer(QName from, QName to, String contextPath, boolean isAttachmentEnabled, boolean isXOPPackage) throws SwitchYardException {
        super(from, to);
        _isAttachmentEnabled = isAttachmentEnabled;
        _isXOPPackage = isXOPPackage;
        try {
            if (contextPath != null) {
                _jaxbContext = JAXBContext.newInstance(contextPath);
            } else {
                _jaxbContext = JAXBContext.newInstance(QNameUtil.toJavaMessageType(from));
            }
        } catch (JAXBException e) {
            throw TransformMessages.MESSAGES.failedToCreateJAXBContext(from.toString(), e);
        }
    }

    @Override
    public Message transform(Message message) {
        Marshaller marshaller;

        try {
            marshaller = _jaxbContext.createMarshaller();
            if (_isAttachmentEnabled) {
                marshaller.setAttachmentMarshaller(new JAXBAttachmentMarshaller(message, _isXOPPackage));
            }
        } catch (JAXBException e) {
            throw TransformMessages.MESSAGES.failedToCreateMarshaller(getFrom().toString(), e);
        }

        try {
            StringWriter resultWriter = new StringWriter();
            Object javaObject = message.getContent();

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            try {
                marshaller.marshal(javaObject, resultWriter);
                message.setContent(resultWriter.toString());
                
            // SWITCHYARD-2852 : if the class does not have an XMLRootElement annotation, we can
            // attempt to use the ObjectFactory to create it and wrap it in an JAXBElement and
            // marshal the JAXBElement.
            } catch (MarshalException ife) {
                resultWriter = new StringWriter();
                Class<?> objectFactory = getObjectFactory(javaObject.getClass());
                Method[] methods = objectFactory.getMethods();
                Method method = null;
                for (int i = 0; i < methods.length; i++) {
                    Class<?>[] parameterTypes = methods[i].getParameterTypes();
                    if ((parameterTypes.length == 1) && (parameterTypes[0] == javaObject.getClass())) {
                        method = methods[i];
                    }
                }

                JAXBElement jaxbElement = null;
                try {
                    Object of = objectFactory.newInstance();
                    jaxbElement = (JAXBElement) method.invoke(of, javaObject);
                } catch (IllegalAccessException iae) {
                    throw TransformMessages.MESSAGES.failedToMarshalForType(getFrom().toString(), iae);
                } catch (InvocationTargetException ite) {
                    throw TransformMessages.MESSAGES.failedToMarshalForType(getFrom().toString(), ite);
                } catch (InstantiationException e) {
                    throw TransformMessages.MESSAGES.failedToMarshalForType(getFrom().toString(), e);
                }

                marshaller.marshal(jaxbElement, resultWriter);
                message.setContent(resultWriter.toString());
           }
        } catch (JAXBException e) {
            throw TransformMessages.MESSAGES.failedToMarshallForType(getFrom().toString(), e);
        }

        return message;
    }

    private static Class getObjectFactory(Class<?> type) {
        if (type.getAnnotation(XmlType.class) != null) {
            // Get the ObjectFactory, if it exists...
            String objectFactoryName = type.getPackage().getName() + "." + "ObjectFactory";

            return Classes.forName(objectFactoryName, JAXBTransformerFactory.class);
        }

        return null;
    }

    class JAXBAttachmentMarshaller extends AttachmentMarshaller {

        private Message _message;
        private boolean _xop;
        
        public JAXBAttachmentMarshaller(Message message, boolean isXOPPackage) {
            _message = message;
            _xop = isXOPPackage;
        }
        
        @Override
        public String addMtomAttachment(DataHandler data, String elementNamespace, String elementLocalName) {
            String cid = "cid:" + elementLocalName + "." + UUID.randomUUID() + "@switchyard.jboss.org";
            _message.addAttachment(cid, data.getDataSource());
            return cid;
        }

        @Override
        public String addMtomAttachment(final byte[] data, final int offset, final int length, final String mimeType, final String elementNamespace, final String elementLocalName) {
            final String cid = "cid:" + elementLocalName + "." + UUID.randomUUID() + "@switchyard.jboss.org";
            DataSource ds = new DataSource() {
                @Override
                public InputStream getInputStream() throws IOException {
                    return new ByteArrayInputStream(data, offset, length);
                }
                @Override
                public OutputStream getOutputStream() throws IOException {
                    throw new UnsupportedOperationException("OutputStream is not supported");
                }
                @Override
                public String getContentType() {
                    return "application/octet-stream";
                }
                @Override
                public String getName() {
                    return cid;
                }
            };
            _message.addAttachment(cid, ds);
            return cid;
        }

        @Override
        public String addSwaRefAttachment(DataHandler data) {
            final String cid = "cid:" + data.getName() + "." + UUID.randomUUID() + "@switchyard.jboss.org";
            _message.addAttachment(cid, data.getDataSource());
            return cid;
        }

        @Override
        public boolean isXOPPackage() {
            return _xop;
        }
    }

}
