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
package org.switchyard.component.itests.property;

import javax.inject.Named;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

/**
 * A camel route builder.
 */
@Named("CamelPropertyInOutRouteBuilder")
public class CamelPropertyInOutRouteBuilder extends RouteBuilder {

    public static final String FILE_NAME = "org.switchyard.component.itests.property.FileName";
    public void configure() {
        from("switchyard://CamelPropertyInOutService")
            .setProperty(FILE_NAME, body().append("-processed"))
        
        .choice()
            .when(body().isEqualTo("DeclaredFault"))
                .throwException(new CustomFault("Throwing declared fault"))
            .when(body().isEqualTo("UndeclaredException"))
                .throwException(new RuntimeException("Throwing undeclared Exception"))
            .when(body().isEqualTo("ExceptionContent"))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setBody(new Exception("Sending an Exception as a content"));
                    }
                });
    }
}
