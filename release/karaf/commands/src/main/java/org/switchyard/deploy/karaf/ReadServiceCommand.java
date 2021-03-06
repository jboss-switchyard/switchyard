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
package org.switchyard.deploy.karaf;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.switchyard.admin.Application;
import org.switchyard.admin.Service;
import org.switchyard.admin.SwitchYard;

/**
 * Shell command for list-service.
 */
@Command(scope = "switchyard", name = "read-service", description = "Returns a details about a SwitchYard service deployed on the system.")
public class ReadServiceCommand extends AbstractSwitchYardServiceCommand {

    @Option(name = "--application", aliases = "-a", description = "If specified, only details for the named application are returned.", multiValued = true)
    private List<String> _applicationNames;
    @Option(name = "--service", aliases = "-s", description = "If specified, only details for the named service are returned.", multiValued = true)
    private List<String> _serviceNames;
    @Option(name = "--regex", description = "If specified, treat the name(s) as a regular expression.")
    private boolean _regex;

    @Override
    protected Object doExecute(final SwitchYard switchYard) throws Exception {
        final Pattern applicationPattern = compilePattern(_applicationNames, _regex);
        final Pattern servicePattern = compilePattern(_serviceNames, _regex);
        for (Application application : switchYard.getApplications()) {
            final String applicationName = application.getName().toString();
            final Matcher applicationMatcher = applicationPattern.matcher(applicationName);
            if (applicationMatcher.find()) {
                System.out.println(application.getName() + " = [");
                for (Service service : application.getServices()) {
                    final String serviceName = service.getName().toString();
                    final Matcher serviceMatcher = servicePattern.matcher(serviceName);
                    if (serviceMatcher.find()) {
                        System.out.println(PrintUtil.printService(service, 1));
                    }
                }
                System.out.println("]");
            }
        }
        return null;
    }

}
