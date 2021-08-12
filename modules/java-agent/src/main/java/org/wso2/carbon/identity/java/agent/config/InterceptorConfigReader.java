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

package org.wso2.carbon.identity.java.agent.config;

import net.consensys.cava.toml.Toml;
import net.consensys.cava.toml.TomlArray;
import net.consensys.cava.toml.TomlParseResult;
import net.consensys.cava.toml.TomlTable;
import org.wso2.carbon.identity.java.agent.AgentHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads the interceptor config from the resources file in the classpath.
 */
public class InterceptorConfigReader {

    private static Map<String, String> definedCharacters = new HashMap<>();

    static {
        definedCharacters.put("boolean", "Z");
        definedCharacters.put("byte", "B");
        definedCharacters.put("char", "C");
        definedCharacters.put("short", "S");
        definedCharacters.put("int", "I");
        definedCharacters.put("long", "J");
        definedCharacters.put("float", "F");
        definedCharacters.put("double", "D");
    }

    /**
     * Reads the configs in the class resource.
     * As per the documentation have to pass the Method signature in binary format.
     * use this link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html to add the binary format.
     *
     */
    public void readConfig() {

        loadConfig();
    }

    private void loadConfig() {

        String configLocation = System.getProperty("carbon.config.dir.path") + File.separator +
                                            "developer-debugger.toml";
        File configFile = new File(configLocation);
        if (!configFile.exists()) {
            return;
        }
        TomlParseResult parseResult = null;
        try {
            parseResult = Toml.parse(Paths.get(configFile.getAbsolutePath()));
            if (parseResult.hasErrors()) {
                parseResult.errors().forEach(error -> System.out.println(error.toString()));
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        if (parseResult == null) {
            return;
        }
        Boolean isEnabled = parseResult.getBoolean("enabled");
        AgentConfig agentConfig = AgentHelper.getInstance().getAgentConfig();
        if (isEnabled != null) {
            agentConfig.setEnabled(isEnabled);
        }
        TomlArray interceptions = parseResult.getArray("interception");
        for (int i = 0; i < interceptions.size(); i++) {
            TomlTable interception = interceptions.getTable(i);
            String name = interception.getString("name");
            String className = interception.getString("class").replaceAll("\\.", "/");
            InterceptorConfig interceptorConfig = new InterceptorConfig();
            interceptorConfig.setClassName(className);
            Boolean publicMethods = interception.getBoolean("all_public_methods");
            if (publicMethods != null && publicMethods) {
                interceptorConfig.setAllPublicMethods(true);
            }
            TomlArray methods = parseResult.getArray(name);
            if (methods != null) {
                for (int j = 0; j < methods.size(); j++) {
                    TomlTable method = methods.getTable(j);
                    String methodName = method.getString("method");
                    TomlArray arguments = method.getArray("arguments");
                    StringBuilder argument = new StringBuilder("");
                    for (int k = 0; k < arguments.size(); k++) {
                        processClassName(arguments.getString(k), argument);
                    }
                    String returnTypeString = method.getString("return");
                    StringBuilder returnType;
                    if (returnTypeString == null || "".equals(returnTypeString)) {
                        returnType = new StringBuilder("V");
                    } else {
                        returnType = new StringBuilder();
                        processClassName(returnTypeString, returnType);
                    }
                    String signature = "(" + argument + ")" + returnType;
                    interceptorConfig.addMethodConfigs(methodName, signature, true, true);
                }
            }
            agentConfig.addInterceptor(name, interceptorConfig);
        }

        TomlTable timeLogger = parseResult.getTable("execution_time_logger");
        if (timeLogger != null) {
            Boolean isLoggerEnabled = timeLogger.getBoolean("enable");
            if (isLoggerEnabled != null) {
                agentConfig.setExecutionTimeLoggerEnabled(isLoggerEnabled);
            }
            if (isLoggerEnabled) {
                TomlArray timeLoggerInterceptions = timeLogger.getArray("interceptions");
                for (int i = 0; i < timeLoggerInterceptions.size(); i++) {
                    agentConfig.addExecutionTimeLoggerInterceptors(timeLoggerInterceptions.getString(i));
                }
            }
        }

        TomlTable connectionLogger = parseResult.getTable("db_multi_connection_logger");
        if (connectionLogger != null) {
            Boolean isLoggerEnabled = connectionLogger.getBoolean("enable");
            if (isLoggerEnabled != null && isLoggerEnabled) {
                InterceptorConfig interceptorConfig = new InterceptorConfig();
                interceptorConfig.setClassName("org/apache/tomcat/jdbc/pool/ConnectionPool");
                interceptorConfig.addMethodConfigs("getConnection", "()Ljava/sql/Connection;", true, true);
                interceptorConfig.addMethodConfigs("returnConnection", "(Lorg/apache/tomcat/jdbc/pool/PooledConnection;)V", true, true);
                agentConfig.addInterceptor("DBConnectionLogger", interceptorConfig);
            }
        }
    }

    private void processClassName(String arg, StringBuilder argument) {

        if (definedCharacters.containsKey(arg)) {
            argument.append(definedCharacters.get(arg));
        } else {
            arg = arg.replaceAll("\\.", "/");
            argument.append("L").append(arg).append(";");
        }
    }
}
