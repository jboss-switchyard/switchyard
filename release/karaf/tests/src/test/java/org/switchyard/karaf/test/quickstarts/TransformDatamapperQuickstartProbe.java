package org.switchyard.karaf.test.quickstarts;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.switchyard.common.io.Files;
import org.switchyard.common.io.pull.StringPuller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TransformDatamapperQuickstartProbe extends DeploymentProbe {

    private static final Logger LOGGER = Logger.getLogger(TransformDatamapperQuickstartProbe.class);
    
    private static final String SOURCE_INPUT_FILE = "../../../test-classes/quickstarts/transform-datamapper/abc-order.xml";
    private static final String DEST_INPUT_FILE = "target/input/abc-order.xml";
    private static final String ACTUAL_OUTPUT_FILE = "target/output/xyz-order.json";
    private static final String EXPECTED_OUTPUT_FILE = "../../../test-classes/quickstarts/transform-datamapper/xyz-order.json";

    public TransformDatamapperQuickstartProbe() {
    }
    
    @Test
    public void testFeatures() throws Exception {
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
        String result = jsonUnprettyPrint(readFile(ACTUAL_OUTPUT_FILE));
        LOGGER.info(">>>>>>> " + result);
        Assert.assertEquals(jsonUnprettyPrint(readFile(EXPECTED_OUTPUT_FILE)), result);
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
