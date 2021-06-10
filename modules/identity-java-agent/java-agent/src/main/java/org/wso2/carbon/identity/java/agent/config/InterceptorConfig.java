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

import java.util.ArrayList;
import java.util.List;

/**
 * Debug interceptor configuration.
 */
public class InterceptorConfig {

    private String className;
    private boolean isAllPublicMethods;
    private List<MethodInfoConfig> methodInfoConfigs = new ArrayList<>();

    /**
     * This method is to get the Class Name.
     *
     * @return Name of the Intercepting Class
     */
    public String getClassName() {

        return className;
    }

    /**
     * This method is to set the Class Name.
     *
     * @param className The Class name of the Config.
     */
    public void setClassName(String className) {

        this.className = className;
    }

    /**
     * This method is to add the Method Configs .
     *
     * @param methodName   The method name of the Config.
     * @param signature    The method signature of the Config.
     * @param insertBefore Whether to intercept at start of the method body.
     * @param insertAfter  Whether to intercept at end of the method body.
     */
    public void addMethodConfigs(String methodName, String signature, boolean insertBefore, boolean insertAfter) {

        methodInfoConfigs.add(new MethodInfoConfig(methodName, signature, insertBefore, insertAfter));
    }

    /**
     * This method is to get Method Info Configs.
     *
     * @return list of configs for a class.
     */
    public List<MethodInfoConfig> getMethodInfoConfigs() {

        return methodInfoConfigs;
    }

    public boolean isAllPublicMethods() {

        return isAllPublicMethods;
    }

    public void setAllPublicMethods(boolean allPublicMethods) {

        isAllPublicMethods = allPublicMethods;
    }
}
