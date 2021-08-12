/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.java.agent.logger;

import org.wso2.carbon.identity.java.agent.AgentHelper;
import org.wso2.carbon.identity.java.agent.config.AgentConfig;
import org.wso2.carbon.identity.java.agent.config.InterceptorConfig;
import org.wso2.carbon.identity.java.agent.config.MethodInfoConfig;
import org.wso2.carbon.identity.java.agent.connect.InterceptionEngine;
import org.wso2.carbon.identity.java.agent.connect.InterceptionListener;
import org.wso2.carbon.identity.java.agent.connect.MethodEntryInterceptionFilter;

import java.util.List;

/**
 * Configures the debug listeners for the current runtime.
 */
public class DebugListenerConfigurator {

    private final InterceptionListener interceptionLogger;
    private final InterceptionEngine interceptionEngine = AgentHelper.getInstance().getInterceptionEngine();

    public DebugListenerConfigurator(InterceptionListener interceptionLogger) {

        this.interceptionLogger = interceptionLogger;
    }

    /**
     * This method will help to add the filters(Listeners) to the InterceptionEngine.
     *
     */
    public void configure() {

        AgentConfig agentConfig = AgentHelper.getInstance().getAgentConfig();
        List<String> executionTimeLoggerInterceptors = interceptionLogger.getInterceptorNames();
        executionTimeLoggerInterceptors.forEach((name) -> {
            InterceptorConfig interceptor = agentConfig.getInterceptor(name);
            if (interceptor.isAllPublicMethods()) {
                MethodEntryInterceptionFilter methodEntryInterceptionFilter =
                        new MethodEntryInterceptionFilter(interceptor.getClassName(), true);
                interceptionEngine.addListener(methodEntryInterceptionFilter, this.interceptionLogger);
            }
            for (MethodInfoConfig methodInfoConfig : interceptor.getMethodInfoConfigs()) {
                MethodEntryInterceptionFilter methodEntryInterceptionFilter =
                        new MethodEntryInterceptionFilter(interceptor.getClassName(),
                        methodInfoConfig.getMethodName(), methodInfoConfig.getSignature());
                interceptionEngine.addListener(methodEntryInterceptionFilter, this.interceptionLogger);
            }
        });

    }
}
