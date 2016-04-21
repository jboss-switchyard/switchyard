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

import java.io.File;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.test.SwitchYardRunner;
import org.switchyard.test.SwitchYardTestCaseConfig;

import org.switchyard.component.test.mixins.cdi.CDIMixIn;

/**
 * Functional test for camel property propagation.
 * 
 */
@RunWith(SwitchYardRunner.class)
@SwitchYardTestCaseConfig(
        config = "switchyard-camel-property-test.xml",
        mixins = CDIMixIn.class)
public class CamelPropertyPropagationTest  {
    
    private static final int POLL = 1000;
    private static final int MAX_POLL = 5;
    
    private static final String IN_ONLY_TARGET = "target/input/input-inonly.txt";
    private static final String IN_OUT_TARGET = "target/input/input-inout.txt";
    
    private static final String SRC_SUCCESS = "src/test/resources/org/switchyard/component/itests/property/Success.txt";
    private static final String SRC_DECLARED_FAULT = "src/test/resources/org/switchyard/component/itests/property/DeclaredFault.txt";
    private static final String SRC_EXCEPTION_CONTENT = "src/test/resources/org/switchyard/component/itests/property/ExceptionContent.txt";
    private static final String SRC_UNDECLARED_EXCEPTION = "src/test/resources/org/switchyard/component/itests/property/UndeclaredException.txt";
    
    private static final String RES_IN_ONLY_SUCCESS_NO_PROP = "target/input/input-inonly-success-.txt";
    //private static final String RES_IN_ONLY_SUCCESS = "target/input/input-inonly-success-Success-processed.txt";
    //private static final String RES_IN_ONLY_EXCEPTION_CONTENT = "target/input/input-inonly-success-ExceptionContent-processed.txt";
    private static final String RES_IN_ONLY_ERROR = "target/input/input-inonly-failure-UndeclaredException-processed.txt";
    private static final String RES_IN_OUT_SUCCESS = "target/input/input-inout-success-Success-processed.txt";
    private static final String RES_IN_OUT_EXCEPTION_CONTENT = "target/input/input-inout-success-ExceptionContent-processed.txt";
    private static final String RES_IN_OUT_DECLARED_FAULT = "target/input/input-inout-failure-DeclaredFault-processed.txt";
    private static final String RES_IN_OUT_ERROR = "target/input/input-inout-failure-UndeclaredException-processed.txt";
    
    @Test
    public void testInOnlySuccess() throws Exception {
        // Right now properties are not carried back on IN_ONLY success route.
        // To make that happen, we need to invoke CamelResponseHandler on IN_ONLY as well.
        //perform(SRC_SUCCESS, IN_ONLY_TARGET, RES_IN_ONLY_SUCCESS);
        perform(SRC_SUCCESS, IN_ONLY_TARGET, RES_IN_ONLY_SUCCESS_NO_PROP);
    }
    
    @Test
    public void testInOnlyExceptionContent() throws Exception {
        //perform(SRC_EXCEPTION_CONTENT, IN_ONLY_TARGET, RES_IN_ONLY_EXCEPTION_CONTENT);
        perform(SRC_EXCEPTION_CONTENT, IN_ONLY_TARGET, RES_IN_ONLY_SUCCESS_NO_PROP);
    }
    
    @Test
    public void testInOnlyError() throws Exception {
        perform(SRC_UNDECLARED_EXCEPTION, IN_ONLY_TARGET, RES_IN_ONLY_ERROR);
    }
    
    @Test
    public void testInOutSuccess() throws Exception {
        perform(SRC_SUCCESS, IN_OUT_TARGET, RES_IN_OUT_SUCCESS);
    }
    
    @Test
    public void testInOutExceptionContent() throws Exception {
        perform(SRC_EXCEPTION_CONTENT, IN_OUT_TARGET, RES_IN_OUT_EXCEPTION_CONTENT);
    }
    
    @Test
    public void testInOutError() throws Exception {
        perform(SRC_UNDECLARED_EXCEPTION, IN_OUT_TARGET, RES_IN_OUT_ERROR);
    }
    
    @Test
    public void testInOutFault() throws Exception {
        perform(SRC_DECLARED_FAULT, IN_OUT_TARGET, RES_IN_OUT_DECLARED_FAULT);
    }

    private void perform(String src, String target, String result) throws Exception {
        File resultFile = new File(result);
        if (resultFile.exists()) {
            resultFile.delete();
        }
        Files.copy(new File(src).toPath(), new File(target).toPath());
        for (int i=0; !resultFile.exists() && i<MAX_POLL; i++) {
            Thread.sleep(POLL);
        }
        Assert.assertEquals(true, resultFile.exists());
    }
}

