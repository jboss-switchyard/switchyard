package org.jboss.util.propertyeditor;

import java.util.Properties;

import org.jboss.common.beans.property.BeanUtils;

public class PropertyEditors {

    public static void mapJavaBeanProperties(Object bean, Properties beanProps) throws Exception {
        BeanUtils.mapJavaBeanProperties(bean, beanProps);
    }
}
