package org.switchyard.bus.camel;


import org.apache.camel.impl.DefaultExchange;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.switchyard.bus.camel.CamelMessage;
import org.switchyard.common.camel.SwitchYardCamelContextImpl;
import org.switchyard.test.TestDataSource;

public class CamelMessageAttachmentTest {

    private static final String TESTDATASOURCE_NAME = "testdatasource";
    private static final String TEST_ATTACHMENT_ID = "testId";
    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void testRemoveAttachment() throws Exception {
        CamelMessage message = new CamelMessage(new DefaultExchange(new SwitchYardCamelContextImpl(false)));
        Assert.assertThat(message.getAttachment(TEST_ATTACHMENT_ID), IsNull.nullValue());

        message.addAttachment(TEST_ATTACHMENT_ID, new TestDataSource(TESTDATASOURCE_NAME));
        Assert.assertThat(message.getAttachment(TEST_ATTACHMENT_ID), IsNull.notNullValue());
        Assert.assertThat(message.getAttachment(TEST_ATTACHMENT_ID).getName(), Is.is(TESTDATASOURCE_NAME));

       message.removeAttachment(TEST_ATTACHMENT_ID);
        Assert.assertThat(message.getAttachment(TEST_ATTACHMENT_ID), IsNull.nullValue());
    }

}
