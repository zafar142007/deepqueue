# DeepQueue
##A reactive message broker

DeepQueue is a broker written in __[RX Java](http://reactivex.io/ RX Java)__ that allows multiple producers to write to it, and multiple consumers to read from it. It runs on a single server. Whenever a producer writes to DeepQueue, a message ID is generated and returned as confirmation. Whenever a consumer polls DeepQueue for new messages, it gets those messages which are NOT processed by any other consumer that may be concurrently accessing DeepQueue.  

When a consumer gets a set of messages, it must notify DeepQueue that it has processed each message (individually). This deletes that message from the DeepQueue database. If a message is received by a consumer, but NOT marked as processed within a configurable amount of time, the message then becomes available to any consumer requesting again.  

This is a web application. For building it, use maven (run "mvn clean install javadoc:javadoc"), and deploy the resultant war file in a J2EE container with __Servlets 3.1__ support such as Tomcat 8. If deployed on containers adhering to lower versions than the Servlet 3.1 specification, the benefits of a reactive architecture with RX Java will not be completely exploited.   

The message broker uses simple HTTP endpoints for its functions. There are 3 functions that it can serve:  

1. Read:  
  _http://{HOST}:{PORT}/deepqueue/read_  
  This GET request will either return a JSON with a payload, a UUID and a status. This method will return immediately without waiting.  
  _http://{HOST}:{PORT}/deepqueue/readWithBlocking_  
  This GET request will return a JSON with a payload, its UUID and a status or wait until one becomes available until timeout.   

2. Write:  
_http://{HOST}:{PORT}/deepqueue/write_    
This POST request will write to the queue in a FIFO fashion the payload and return a JSON with a UUID and a status. Example POST request: {"data":"Hello!"}  
Response: {"status":"OK","data":{"payload":"","uuid":"1471178519213_-5093567906476327607"}}  

3. Accept Acknowledgement:   
_http://{HOST}:{PORT}/deepqueue/ack/{uuid}_  
This GET request will expel the relavant packet from the system if the acknowledgement is found to be within the expiry time limit of the packet's read time, otherwise this packet will be reinitiated to the head of the queue after its expiry. This will always return a 200 OK.
