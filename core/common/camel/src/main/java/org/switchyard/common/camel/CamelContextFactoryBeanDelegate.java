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
package org.switchyard.common.camel;

import java.util.Iterator;
import java.util.List;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.ShutdownRoute;
import org.apache.camel.ShutdownRunningTask;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.core.xml.AbstractCamelContextFactoryBean;
import org.apache.camel.core.xml.CamelJMXAgentDefinition;
import org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition;
import org.apache.camel.core.xml.CamelStreamCachingStrategyDefinition;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ContextScanDefinition;
import org.apache.camel.model.InterceptDefinition;
import org.apache.camel.model.InterceptFromDefinition;
import org.apache.camel.model.InterceptSendToEndpointDefinition;
import org.apache.camel.model.OnCompletionDefinition;
import org.apache.camel.model.OnExceptionDefinition;
import org.apache.camel.model.PackageScanDefinition;
import org.apache.camel.model.PropertiesDefinition;
import org.apache.camel.model.RestContextRefDefinition;
import org.apache.camel.model.RouteBuilderDefinition;
import org.apache.camel.model.RouteContextRefDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.ThreadPoolProfileDefinition;
import org.apache.camel.model.dataformat.DataFormatsDefinition;
import org.apache.camel.model.rest.RestConfigurationDefinition;
import org.apache.camel.model.rest.RestDefinition;
import org.apache.camel.spi.PackageScanFilter;
import org.apache.camel.spring.CamelContextFactoryBean;
import org.apache.camel.spring.CamelEndpointFactoryBean;
import org.apache.camel.spring.CamelRedeliveryPolicyFactoryBean;
import org.jboss.logging.Logger;

/**
 * Unlike other concrete CamelContextFactoryBean like spring and blueprint, this class is used to import
 * configurations held by the other CamelContextFactoryBean instance into existing SwitchYardCamelContext.
 */
public class CamelContextFactoryBeanDelegate extends AbstractCamelContextFactoryBean<SwitchYardCamelContext> {

    private static final Logger LOG = Logger.getLogger(CamelContextFactoryBeanDelegate.class);
    
    private CamelContextFactoryBean _factoryBean;
    private SwitchYardCamelContext _camelContext;

    /**
     * Constructor.
     * @param context SwitchYard CamelContext
     * @param bean CamelContextFactoryBean
     */
    public CamelContextFactoryBeanDelegate(SwitchYardCamelContext context, CamelContextFactoryBean bean) {
        _camelContext = context;
        _factoryBean = bean;
        String id = bean.getId();
        if (id != null && context instanceof DefaultCamelContext) {
            ((DefaultCamelContext)context).setName(id);
        } else {
            id = context.getName();
        }
        setId(id);
    }

    /**
     * Imports configurations held by the CamelContextFactoryBean into SwitchYardCamelContext.
     * @throws Exception failed to import
     */
    public void importConfiguration() throws Exception {
        afterPropertiesSet();
        // Keep using concrete factory bean classes to prevent CCE due to module loader difference
        if (_factoryBean.getEndpoints() != null) {
            for (CamelEndpointFactoryBean endpoint : _factoryBean.getEndpoints()) {
                endpoint.setCamelContext(_camelContext);
                endpoint.afterPropertiesSet();
                Object created = endpoint.getObject();
                _camelContext.getWritebleRegistry().put(endpoint.getId(), created);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Registered camel endpoint '" + endpoint.getId() + "'");
                }
            }
        }
        if (_factoryBean.getRedeliveryPolicies() != null) {
            for (CamelRedeliveryPolicyFactoryBean policy : _factoryBean.getRedeliveryPolicies()) {
                policy.setCamelContext(_camelContext);
                policy.afterPropertiesSet();
                Object created = policy.getObject();
                _camelContext.getWritebleRegistry().put(policy.getId(), created);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Registered camel RedeliveryPolicy '" + policy.getId() + "'");
                }
            }
        }
        setupRoutes();
    }

    @Override
    public void setRoutes(List<RouteDefinition> routes) {
        _factoryBean.setRoutes(routes);
    }

    @Override
    public void setRests(List<RestDefinition> rests) {
        _factoryBean.setRests(rests);
    }

    @Override
    public Class<SwitchYardCamelContext> getObjectType() {
        return SwitchYardCamelContext.class;
    }

    @Override
    public SwitchYardCamelContext getContext(boolean create) {
        return _camelContext;
    }

    /**
     * Sets SwitchYardCamelContext.
     * @param context SwitchYard CamelContext
     */
    public void setContext(SwitchYardCamelContext context) {
        _camelContext = context;
    }

    @Override
    public List<RouteDefinition> getRoutes() {
        return _factoryBean.getRoutes();
    }

    @Override
    public List<RestDefinition> getRests() {
        return _factoryBean.getRests();
    }

    @Override
    public RestConfigurationDefinition getRestConfiguration() {
        return _factoryBean.getRestConfiguration();
    }

    @Override
    public List<CamelEndpointFactoryBean> getEndpoints() {
        return _factoryBean.getEndpoints();
    }

    @Override
    public List<CamelRedeliveryPolicyFactoryBean> getRedeliveryPolicies() {
        return _factoryBean.getRedeliveryPolicies();
    }

    @Override
    public List<InterceptDefinition> getIntercepts() {
        return _factoryBean.getIntercepts();
    }

    @Override
    public List<InterceptFromDefinition> getInterceptFroms() {
        return _factoryBean.getInterceptFroms();
    }

    @Override
    public List<InterceptSendToEndpointDefinition> getInterceptSendToEndpoints() {
        return _factoryBean.getInterceptSendToEndpoints();
    }

    @Override
    public PropertiesDefinition getProperties() {
        return _factoryBean.getProperties();
    }

    @Override
    public String[] getPackages() {
        return _factoryBean.getPackages();
    }

    @Override
    public PackageScanDefinition getPackageScan() {
        return _factoryBean.getPackageScan();
    }

    @Override
    public void setPackageScan(PackageScanDefinition packageScan) {
        _factoryBean.setPackageScan(packageScan);
    }

    @Override
    public ContextScanDefinition getContextScan() {
        return _factoryBean.getContextScan();
    }

    @Override
    public void setContextScan(ContextScanDefinition contextScan) {
        _factoryBean.setContextScan(contextScan);
    }

    @Override
    public CamelPropertyPlaceholderDefinition getCamelPropertyPlaceholder() {
        return _factoryBean.getCamelPropertyPlaceholder();
    }

    @Override
    public String getTrace() {
        return _factoryBean.getTrace();
    }

    @Override
    public String getMessageHistory() {
        return _factoryBean.getMessageHistory();
    }

    @Override
    public String getStreamCache() {
        return _factoryBean.getStreamCache();
    }

    @Override
    public String getDelayer() {
        return _factoryBean.getDelayer();
    }

    @Override
    public String getHandleFault() {
        return _factoryBean.getHandleFault();
    }

    @Override
    public String getAutoStartup() {
        return _factoryBean.getAutoStartup();
    }

    @Override
    public String getUseMDCLogging() {
        return _factoryBean.getUseMDCLogging();
    }

    @Override
    public String getUseBreadcrumb() {
        return _factoryBean.getUseBreadcrumb();
    }

    @Override
    public String getAllowUseOriginalMessage() {
        return _factoryBean.getAllowUseOriginalMessage();
    }

    @Override
    public String getRuntimeEndpointRegistryEnabled() {
        return _factoryBean.getRuntimeEndpointRegistryEnabled();
    }

    @Override
    public String getManagementNamePattern() {
        return _factoryBean.getManagementNamePattern();
    }

    @Override
    public String getThreadNamePattern() {
        return _factoryBean.getThreadNamePattern();
    }

    @Deprecated
    @Override
    public Boolean getLazyLoadTypeConverters() {
        return _factoryBean.getLazyLoadTypeConverters();
    }

    @Override
    public Boolean getTypeConverterStatisticsEnabled() {
        return _factoryBean.getTypeConverterStatisticsEnabled();
    }

    @Override
    public CamelJMXAgentDefinition getCamelJMXAgent() {
        return _factoryBean.getCamelJMXAgent();
    }

    @Override
    public CamelStreamCachingStrategyDefinition getCamelStreamCachingStrategy() {
        return _factoryBean.getCamelStreamCachingStrategy();
    }

    @Override
    public List<RouteBuilderDefinition> getBuilderRefs() {
        return _factoryBean.getBuilderRefs();
    }

    @Override
    public List<RouteContextRefDefinition> getRouteRefs() {
        return _factoryBean.getRouteRefs();
    }

    @Override
    public List<RestContextRefDefinition> getRestRefs() {
        return _factoryBean.getRestRefs();
    }

    @Override
    public String getErrorHandlerRef() {
        return _factoryBean.getErrorHandlerRef();
    }

    @Override
    public DataFormatsDefinition getDataFormats() {
        return _factoryBean.getDataFormats();
    }

    @Override
    public List<OnExceptionDefinition> getOnExceptions() {
        return _factoryBean.getOnExceptions();
    }

    @Override
    public List<OnCompletionDefinition> getOnCompletions() {
        return _factoryBean.getOnCompletions();
    }

    @Override
    public ShutdownRoute getShutdownRoute() {
        return _factoryBean.getShutdownRoute();
    }

    @Override
    public ShutdownRunningTask getShutdownRunningTask() {
        return _factoryBean.getShutdownRunningTask();
    }

    @Override
    public List<ThreadPoolProfileDefinition> getThreadPoolProfiles() {
        return _factoryBean.getThreadPoolProfiles();
    }

    @Override
    public String getDependsOn() {
        return _factoryBean.getDependsOn();
    }
    
    @Override
    protected <S> S getBeanForType(Class<S> clazz) {
        Iterator<S> iterator = _camelContext.getRegistry().findByType(clazz).iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }
    
    
    /*
    * Unsupported operations - just ignore the call.
    */
    
    @Override
    protected void findRouteBuildersByPackageScan(String[] packages, PackageScanFilter filter, List<RoutesBuilder> builders) throws Exception {
    }
    @Override
    protected void findRouteBuildersByContextScan(PackageScanFilter filter, List<RoutesBuilder> builders) throws Exception {
    }
    @Override
    protected void initCustomRegistry(SwitchYardCamelContext context) {
    }
    @Override
    protected void initBeanPostProcessor(SwitchYardCamelContext context) {
    }
    @Override
    protected void postProcessBeforeInit(RouteBuilder builder) {
    }

}
