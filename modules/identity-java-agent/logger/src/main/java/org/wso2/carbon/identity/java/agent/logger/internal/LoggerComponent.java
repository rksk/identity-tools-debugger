package org.wso2.carbon.identity.java.agent.logger.internal;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.identity.java.agent.AgentHelper;
import org.wso2.carbon.identity.java.agent.connect.InterceptionEngine;
import org.wso2.carbon.identity.java.agent.host.DefaultInterceptionEngine;
import org.wso2.carbon.identity.java.agent.logger.DBConnectionLogger;
import org.wso2.carbon.identity.java.agent.logger.DebugListenerConfigurator;
import org.wso2.carbon.identity.java.agent.logger.InterceptionLogger;

@Component(
        name = "agent.logger.component",
        immediate = true
)
public class LoggerComponent {

    @Activate
    protected void activate(ComponentContext ctxt) {

        DebugListenerConfigurator debugListenerConfigurator =
                new DebugListenerConfigurator(new InterceptionLogger());
        debugListenerConfigurator.configure();

//        debugListenerConfigurator =
//                new DebugListenerConfigurator(new DBConnectionLogger());
//        debugListenerConfigurator.configure();
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

    }
}
