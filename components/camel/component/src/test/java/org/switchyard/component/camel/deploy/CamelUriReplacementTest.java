/*
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors.
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
package org.switchyard.component.camel.deploy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.component.test.mixins.cdi.CDIMixIn;
import org.switchyard.test.Invoker;
import org.switchyard.test.ServiceOperation;
import org.switchyard.test.SwitchYardRunner;
import org.switchyard.test.SwitchYardTestCaseConfig;

/** Test header presence populated by CamelMessageComposer in camel messages.
 * 
 * @author Lukasz Dywicki */
@RunWith(SwitchYardRunner.class)
@SwitchYardTestCaseConfig(config = "switchyard-uri-replacement-test.xml", mixins = CDIMixIn.class)
public class CamelUriReplacementTest {


    @ServiceOperation("OrderService.getTitleForItem")
    private Invoker _getTitleForItem;

    @ServiceOperation("WarehouseService.getDataForItem")
    private Invoker _getDataForItem;

    @Test
    public void sendOneWayMessageThroughCamelToSwitchYardService() throws Exception {
        final String title = _getTitleForItem.sendInOut(10).getContent(String.class);
        assertThat(title, is(equalTo("Fletch")));
    }



}
