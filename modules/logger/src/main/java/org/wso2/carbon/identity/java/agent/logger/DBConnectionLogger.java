package org.wso2.carbon.identity.java.agent.logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.java.agent.connect.InterceptionListener;
import org.wso2.carbon.identity.java.agent.host.InterceptionEventType;
import org.wso2.carbon.identity.java.agent.host.MethodContext;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DBConnectionLogger implements InterceptionListener {

    private static final ThreadLocal<AtomicInteger> connections = ThreadLocal.withInitial(()-> new AtomicInteger(0));

    private static final Log log = LogFactory.getLog(DBConnectionLogger.class);

    @Override
    public void handleEvent(InterceptionEventType type, MethodContext methodContext) {

        switch (type) {
            case METHOD_AFTER:
                String methodName = methodContext.getMethodName();
                if ("getConnection".equals(methodName)) {
                    int count = connections.get().getAndIncrement();
                    if (count != 0) {
                        log.info("Current thread already have " + (count + 1) +
                                " active connection. But we are trying to get another connection here, " +
                                getCurrentStackTrace(Thread.currentThread().getStackTrace()));
                    }
                } else if ("returnConnection".equals(methodName)) {
                    connections.get().getAndDecrement();
                }
        }
    }

    private String getCurrentStackTrace(StackTraceElement[] stackTrace) {

        StringBuilder builder = new StringBuilder("\n");
        for (int i = 7; i < stackTrace.length; i++) {
            builder.append("\t").append(stackTrace[i]).append("\n");
        }
        return builder.toString();
    }

    @Override
    public List<String> getInterceptorNames() {

        return Collections.singletonList("DBConnectionLogger");
    }
}
