package org.wso2.carbon.identity.java.agent.logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.java.agent.AgentHelper;
import org.wso2.carbon.identity.java.agent.config.AgentConfig;
import org.wso2.carbon.identity.java.agent.connect.InterceptionListener;
import org.wso2.carbon.identity.java.agent.host.InterceptionEventType;
import org.wso2.carbon.identity.java.agent.host.MethodContext;

import java.util.List;
import java.util.Stack;
import java.util.UUID;

public class InterceptionLogger implements InterceptionListener {

    private static final ThreadLocal<Stack<Long>> methodBeforeTimes = ThreadLocal.withInitial(Stack::new);
    private static final ThreadLocal<Stack<String>> methodCorrelations = ThreadLocal.withInitial(Stack::new);

    private static final Log log = LogFactory.getLog(InterceptionLogger.class);

    @Override
    public void handleEvent(InterceptionEventType type, MethodContext methodContext) {

        switch (type) {
            case METHOD_BEFORE:
                String methodCorrelation = UUID.randomUUID().toString();
                methodCorrelations.get().push(methodCorrelation);
                methodBeforeTimes.get().push(System.currentTimeMillis());
                log.info("[" + methodCorrelation + "] " + methodContext.getClassName()
                        .replaceAll("/", "\\.") + ":" + methodContext.getMethodName()
                        + " starting");
                break;
            case METHOD_AFTER:
                Long beforeTime = methodBeforeTimes.get().pop();
                String correlation = methodCorrelations.get().pop();
                log.info("[" + correlation + "] " + methodContext.getClassName()
                        .replaceAll("/", "\\.") + ":" + methodContext.getMethodName()
                        + " taken : " + (System.currentTimeMillis() - beforeTime));
        }
    }

    @Override
    public List<String> getInterceptorNames() {

        AgentConfig agentConfig = AgentHelper.getInstance().getAgentConfig();
        return agentConfig.getExecutionTimeLoggerInterceptors();
    }
}
