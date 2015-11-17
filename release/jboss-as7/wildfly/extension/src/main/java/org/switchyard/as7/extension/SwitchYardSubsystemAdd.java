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
package org.switchyard.as7.extension;

import static org.switchyard.as7.extension.CommonAttributes.SOCKET_BINDING;

import java.util.Map;
import java.util.StringTokenizer;

import org.infinispan.Cache;
import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.resource.AbstractSocketBindingResourceDefinition;
import org.jboss.as.network.SocketBinding;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceBuilder.DependencyType;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.switchyard.admin.SwitchYard;
import org.switchyard.as7.extension.deployment.SwitchYardConfigDeploymentProcessor;
import org.switchyard.as7.extension.deployment.SwitchYardConfigProcessor;
import org.switchyard.as7.extension.deployment.SwitchYardDependencyProcessor;
import org.switchyard.as7.extension.deployment.SwitchYardDeploymentProcessor;
import org.switchyard.as7.extension.services.SwitchYardAdminService;
import org.switchyard.as7.extension.services.SwitchYardInjectorService;
import org.switchyard.as7.extension.services.SwitchYardServiceDomainManagerService;
import org.switchyard.as7.extension.services.SwitchYardSystemSecurityService;
import org.switchyard.as7.extension.util.ServerUtil;
import org.switchyard.deploy.ServiceDomainManager;
import org.switchyard.security.system.SystemSecurity;
import org.wildfly.clustering.infinispan.spi.service.CacheServiceName;

/**
 * The SwitchYard subsystem add update handler.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2011 Red Hat Inc.
 */
public final class SwitchYardSubsystemAdd extends AbstractBoottimeAddStepHandler {

    private static final Logger LOG = Logger.getLogger("org.switchyard");

    static final SwitchYardSubsystemAdd INSTANCE = new SwitchYardSubsystemAdd();

    // Private to ensure a singleton.
    private SwitchYardSubsystemAdd() {
    }



    @Override
    protected void populateModel(final ModelNode operation, final ModelNode model) throws OperationFailedException {
        Attributes.PROPERTIES.validateAndSet(operation, model);
        Attributes.SOCKET_BINDING.validateAndSet(operation, model);
    }

    @Override
    protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        LOG.trace("Performing boot time operation " + operation);
        context.addStep(new AbstractDeploymentChainStep() {
            protected void execute(DeploymentProcessorTarget processorTarget) {
                int priority = 0x4000;
                processorTarget.addDeploymentProcessor(SwitchYardExtension.SUBSYSTEM_NAME, Phase.PARSE, priority++, new SwitchYardConfigDeploymentProcessor());
                processorTarget.addDeploymentProcessor(SwitchYardExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, priority++, new SwitchYardDependencyProcessor());
                processorTarget.addDeploymentProcessor(SwitchYardExtension.SUBSYSTEM_NAME, Phase.POST_MODULE, priority++, new SwitchYardConfigProcessor());
                processorTarget.addDeploymentProcessor(SwitchYardExtension.SUBSYSTEM_NAME, Phase.INSTALL, priority++, new SwitchYardDeploymentProcessor());
            }
        }, OperationContext.Stage.RUNTIME);
        LOG.trace("Activating SwitchYard Subsystem");

        ServerUtil.setRegistry(context.getServiceRegistry(false));
        final SwitchYardInjectorService injectorService = new SwitchYardInjectorService();
        final ServiceBuilder<Map<String, String>> injectorServiceBuilder = context.getServiceTarget().addService(SwitchYardInjectorService.SERVICE_NAME, injectorService);
        if (model.hasDefined(SOCKET_BINDING)) {
            StringTokenizer sockets = new StringTokenizer(Attributes.SOCKET_BINDING.resolveModelAttribute(context,model).asString(), ",");
            while (sockets.hasMoreTokens()) {
                String socketName = sockets.nextToken();
                final ServiceName serviceName = context.getCapabilityServiceName(AbstractSocketBindingResourceDefinition.SOCKET_BINDING_CAPABILITY_NAME, socketName, SocketBinding.class);
                injectorServiceBuilder.addDependency(serviceName, SocketBinding.class, injectorService.getSocketBinding(socketName));
            }
        }
        injectorServiceBuilder.setInitialMode(Mode.ACTIVE);
        injectorServiceBuilder.install();

        // Add the AS7 Service for the SystemSecurity...
        final SwitchYardSystemSecurityService systemSecurityService = new SwitchYardSystemSecurityService();
        context.getServiceTarget()
               .addService(SwitchYardSystemSecurityService.SERVICE_NAME, systemSecurityService)
               .install();

        // Add the AS7 Service for the ServiceDomainManager...
        final SwitchYardServiceDomainManagerService serviceDomainManagerService = new SwitchYardServiceDomainManagerService();
        context.getServiceTarget()
               .addService(SwitchYardServiceDomainManagerService.SERVICE_NAME, serviceDomainManagerService)
               .addDependency(SwitchYardSystemSecurityService.SERVICE_NAME, SystemSecurity.class, serviceDomainManagerService.getSystemSecurity())
               // TODO find an alternative to using DependencyType.OPTIONAL
               .addDependency(DependencyType.OPTIONAL, CacheServiceName.CACHE.getServiceName("cluster"), Cache.class, serviceDomainManagerService.getCache())
               .install();

        final SwitchYardAdminService adminService = new SwitchYardAdminService();
        final ServiceBuilder<SwitchYard> adminServiceBuilder = context.getServiceTarget().addService(SwitchYardAdminService.SERVICE_NAME, adminService);
        adminServiceBuilder.setInitialMode(Mode.ACTIVE);
        adminServiceBuilder.addDependency(SwitchYardInjectorService.SERVICE_NAME, Map.class, adminService.getSocketBindings())
                           .addDependency(SwitchYardServiceDomainManagerService.SERVICE_NAME, ServiceDomainManager.class, adminService.getServiceDomainManager())
                           .install();

    }

}
