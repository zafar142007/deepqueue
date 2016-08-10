# DeepQueue
A concurrent message broker

DeepQueue is a broker that allows multiple producers to write to it, and multiple consumers to read from it. It runs on a single server. Whenever a producer writes to DeepQueue, a message ID is generated and returned as confirmation. Whenever a consumer polls DeepQueue for new messages, it gets those messages which are NOT processed by any other consumer that may be concurrently accessing DeepQueue. 
When a consumer gets a set of messages, it must notify DeepQueue that it has processed each message (individually). This deletes that message from the DeepQueue database. If a message is received by a consumer, but NOT marked as processed within a configurable amount of time, the message then becomes available to any consumer requesting again.
This is a web application. For building it, use maven, and deploy it in a J2EE container such as Tomcat. The message broker uses simple HTTP endpoints for its functions.

There are 3 functions that it can serve:  
1. Read:  
  http://{HOST}:{PORT}/deepqueue/read  
  It will either return an empty response or a JSON with a payload and a UUID. This method will not wait.  
  http://{HOST}:{PORT}/deepqueue/readWithBlocking  
  This method will return a JSON with a payload and a UUID or wait until one becomes available.    
2. Write:  
  http://{HOST}:{PORT}/deepqueue/write/<payload>  
  This will write to the queue in a FIFO fashion the payload and return a UUID.    
3. Accept Acknowledgement:  
  http://{HOST}:{PORT}/deepqueue/ack/{uuid}  
  This will expel the relavant packet if the acknowledgement is found to be within the expiry time limit of the packet's read time, otherwise this packet will be reinitiated to the head of the queue. 
