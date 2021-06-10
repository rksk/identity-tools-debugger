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

package org.wso2.carbon.identity.java.agent;

import net.consensys.cava.toml.Toml;
import net.consensys.cava.toml.TomlParseResult;
import org.wso2.carbon.identity.java.agent.config.AgentConfig;
import org.wso2.carbon.identity.java.agent.config.InterceptorConfig;
import org.wso2.carbon.identity.java.agent.config.InterceptorConfigReader;
import org.wso2.carbon.identity.java.agent.internal.InterceptingClassTransformer;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Java Agent to intercept the given Java Classes and add the relevant methods.
 */
public class DebugAgent {

    private static final Logger log = Logger.getLogger(DebugAgent.class.getName());

    /**
     * As soon as the JVM initializes, This  method will be called.
     * Configs for intercepting will be read and added to Transformer so that Transformer will intercept when the
     * corresponding Java Class and Method is loaded.
     *
     * @param agentArgs       The list of agent arguments
     * @param instrumentation The instrumentation object
     * @throws InstantiationException While  an instantiation of object cause an error.
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) throws InstantiationException {

        InterceptorConfigReader interceptorConfigReader = new InterceptorConfigReader();
        interceptorConfigReader.readConfig();

        AgentConfig config = AgentHelper.getInstance().getAgentConfig();
        if (!config.isEnabled()) {
            System.out.println("WSO2 Debugger Java Agent disabled.");
            return;
        }
        System.out.println("Starting WSO2 Debugger Java Agent......");

        Map<String, InterceptorConfig> interceptors = config.getInterceptors();
        InterceptingClassTransformer interceptingClassTransformer = new InterceptingClassTransformer();
        interceptingClassTransformer.init();

        interceptors.forEach((name, interceptor) -> {
            interceptingClassTransformer.addConfig(interceptor);
        });
        instrumentation.addTransformer(interceptingClassTransformer);
    }

}
