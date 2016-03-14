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
package org.switchyard.test.quickstarts;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.common.io.Files;
import org.switchyard.common.io.pull.StringPuller;
import org.switchyard.test.ArquillianUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * CamelDozerQuickstartTest.
 */
@RunWith(Arquillian.class)
public class TransformDatamapperQuickstartTest {

    private static final Logger LOGGER = Logger.getLogger(TransformDatamapperQuickstartTest.class);
    
    private static final String SOURCE_INPUT_FILE = "target/test-classes/org/switchyard/test/quickstarts/transform-datamapper/abc-order.xml";
    private static final String DEST_INPUT_FILE = "target/input/abc-order.xml";
    private static final String ACTUAL_OUTPUT_FILE = "target/output/xyz-order.json";
    private static final String EXPECTED_OUTPUT_FILE = "target/test-classes/org/switchyard/test/quickstarts/transform-datamapper/xyz-order.json";

    @Deployment(testable = true)
    public static JavaArchive createDeployment() {
        return ArquillianUtil.createJarQSDeployment("switchyard-transform-datamapper");
    }

    @Test
    public void testDeployment() throws Exception {
        File output = new File(ACTUAL_OUTPUT_FILE);
        if (output.exists()) {
            output.delete();
        }
        
        Files.copy(new File(SOURCE_INPUT_FILE), new File(DEST_INPUT_FILE));
        for (int i=0; (!output.exists()) && i<10; i++) {
            LOGGER.info("Waiting for an output file to be written...");
            Thread.sleep(1000);
        }
        
        Assert.assertTrue("The output file '" + output.getPath() + "' was not found", output.exists());
        Assert.assertEquals(jsonUnprettyPrint(readFile(EXPECTED_OUTPUT_FILE))
                            , jsonUnprettyPrint(readFile(ACTUAL_OUTPUT_FILE)));
    }

    private String readFile(String filePath) throws Exception {
        return new StringPuller().pull(new FileInputStream(filePath));
    }
    
    private String jsonUnprettyPrint(String jsonString) throws JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        JsonNode node = mapper.readTree(jsonString);
        return node.toString();
    }
}
