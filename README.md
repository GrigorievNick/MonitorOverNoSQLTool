# MonitorOverNoSQLTool
TODO:
- Separate CommandEvent to Different Request Model
- batch sent to ui
- settable TIME Window on UI: validation of time range, toggle behavior 
    - Must be disable to turn off live mode, until END and Start defined
    - Must be disable to turn on live mode, until  Start defined
- Error handling and error reporting to UI
- Meta data api (rest/ws ...): take opType and Date Range  and default isLive
- dont forget finish Auth flow
- BUG in GRID : Page size can't be only increased : FIXME
- Hide column : BE support to low traffic
- Disable Live toggle if date range do not set
- generate more representive data set
- add deploy to AWS host

 I don't have enough time ti finish this list
  
How to build:
```
mvn clean install 
mvn spring-boot:run
 ```