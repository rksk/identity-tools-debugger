# identity-tools-debugger
Tools which helps to debug the WSO2 Identity Server performance related issues
Currently this supports,
1. Execution time logger - Log time taken to execute defined methods
2. Database connection logger - Log if any threads occupy more than one connection

# How to build
Use `mvn clean install`

# How to add debug support to IS
* Copy `modules/java-agent/target/org.wso2.carbon.identity.developer.java-agent-1.0.0-jar-with-dependencies.jar` to `IS_HOME/lib`

* Copy
  `modules/logger/target/org.wso2.carbon.identity.developer.logger-1.0.0.jar` to `IS_HOME/repository/components/dropins/`
* Copy `developer-debugger.toml` to `IS_HOME/repository/conf`
* Add the following under JAVA_OPTS to enable agent in `wso2server.sh`. 
```
    -javaagent:$CARBON_HOME/lib/org.wso2.carbon.identity.developer.java-agent-1.0.0-jar-with-dependencies.jar \
```
* Start the server

# How to configure
We can disable the complete debugger tool, by changing the root `enabled` config
```toml
enabled = false
```
## Execution time logger
- If you want to disable the execution time logger, disable execution_time_logger.enable
```toml
[execution_time_logger]
enable = false
```

## Database connection logger
- If you want to disable the database connection logger, disable db_multi_connection_logger.enable
```toml
[db_multi_connection_logger]
enable = false
```