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
import java.util.ArrayList;
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
        ArrayList<InterceptorConfig> interceptorConfigs = new ArrayList<>();
        addSAMLConfig(interceptorConfigs);
        addOIDCConfig(interceptorConfigs);
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
            TomlArray timeLoggerInterceptions = timeLogger.getArray("interceptions");
            for (int i = 0; i < timeLoggerInterceptions.size(); i++) {
                agentConfig.addExecutionTimeLoggerInterceptors(timeLoggerInterceptions.getString(i));
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

    private void addOIDCConfig(ArrayList<InterceptorConfig> interceptorConfigs) {

        // OIDC Authorization Endpoint Configs.
        InterceptorConfig oidcAuthzFilterConfig = new InterceptorConfig();
        oidcAuthzFilterConfig.setClassName(
                "org/wso2/carbon/identity/oauth/endpoint/authz/OAuth2AuthzEndpoint");
        oidcAuthzFilterConfig.addMethodConfigs("handleInitialAuthorizationRequest",
                "(Lorg/wso2/carbon/identity/oauth/endpoint/message/OAuthMessage;)Ljavax/ws/rs/core/Response;",
                true, false);
        oidcAuthzFilterConfig.addMethodConfigs("manageOIDCSessionState",
                "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;" +
                        "Lorg/wso2/carbon/identity/oidc/session/OIDCSessionState;" +
                        "Lorg/wso2/carbon/identity/oauth2/model/OAuth2Parameters;Ljava/lang/String;Ljava/lang/String;" +
                        ")Ljava/lang/String;",
                false, true);
        interceptorConfigs.add(oidcAuthzFilterConfig);

        // OIDC Token Endpoint Configs.
        InterceptorConfig oidcTokenFilterConfig = new InterceptorConfig();
        oidcTokenFilterConfig.setClassName(
                "org/wso2/carbon/identity/oauth/endpoint/token/OAuth2TokenEndpoint");
        oidcTokenFilterConfig.addMethodConfigs("buildTokenResponse",
                "(Lorg/wso2/carbon/identity/oauth2/dto/OAuth2AccessTokenRespDTO;)Ljavax/ws/rs/core/Response;",
                true, false);
        oidcTokenFilterConfig.addMethodConfigs("buildCarbonOAuthTokenRequest",
                "(Ljavax/servlet/http/HttpServletRequestWrapper;)" +
                        "Lorg/wso2/carbon/identity/oauth2/model/CarbonOAuthTokenRequest;",
                false, true);

        interceptorConfigs.add(oidcTokenFilterConfig);
    }

    private void addSAMLConfig(ArrayList<InterceptorConfig> interceptorConfigs) {

        InterceptorConfig samlFilterConfig = new InterceptorConfig();
        samlFilterConfig.setClassName(
                "org/wso2/carbon/identity/sso/saml/servlet/SAMLSSOProviderServlet");

        samlFilterConfig.addMethodConfigs("doPost",
                "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V",
                true, false);

        samlFilterConfig.addMethodConfigs("sendResponse",
                "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;" +
                        "Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/" +
                        "String;Ljava/lang/String;)V", false, true);

        interceptorConfigs.add(samlFilterConfig);
    }
}
