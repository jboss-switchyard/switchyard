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
package org.switchyard.console.component.sca.client;

import java.util.Collections;

import org.jboss.ballroom.client.widgets.ContentGroupLabel;
import org.jboss.ballroom.client.widgets.forms.DefaultGroupRenderer;
import org.jboss.ballroom.client.widgets.forms.FormItem;
import org.jboss.ballroom.client.widgets.forms.RenderMetaData;
import org.jboss.ballroom.client.widgets.forms.TextItem;
import org.switchyard.console.components.client.ui.BaseComponentConfigurationView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * SCAComponentConfigurationView
 * 
 * Customized component configuration view for the SCA component.
 */
public class SCAComponentConfigurationView extends BaseComponentConfigurationView {

    private static final Messages MESSAGES = GWT.create(Messages.class);
    private static final String CACHE_NAME = "cache-name"; //$NON-NLS-1$

    private TextItem _cacheName;

    @SuppressWarnings("rawtypes")
    @Override
    protected Widget createComponentDetailsWidget() {
        VerticalPanel layout = new VerticalPanel();
        layout.setStyleName("fill-layout-width"); //$NON-NLS-1$
        layout.add(new ContentGroupLabel(MESSAGES.label_configuredProperties()));

        String title = MESSAGES.label_cacheName(CACHE_NAME);
        _cacheName = new TextItem(CACHE_NAME, title) {
            @Override
            public void setValue(String value) {
                if (value == null || value.length() == 0) {
                    value = MESSAGES.constant_notSet();
                }
                super.setValue(value);
            }
        };

        RenderMetaData metaData = new RenderMetaData();
        metaData.setNumColumns(1);
        metaData.setTitleWidth(title.length());
        layout.add(new DefaultGroupRenderer().render(metaData, "null", //$NON-NLS-1$
                Collections.<String, FormItem> singletonMap(CACHE_NAME, _cacheName)));

        return layout;
    }

    @Override
    protected void updateComponentDetails() {
        if (getComponent() == null || getComponent().getProperties() == null) {
            _cacheName.setValue(null);
        } else {
            _cacheName.setValue(getComponent().getProperties().get(CACHE_NAME));
        }
    }

    @Override
    protected String getComponentName() {
        return "SCA"; //$NON-NLS-1$
    }

}
