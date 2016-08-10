# DeepQueue
A concurrent message broker

DeepQueue is a broker that allows multiple producers to write to it, and multiple consumers to read from it. It runs on a single server. Whenever a producer writes to DeepQueue, a message ID is generated and returned as confirmation. Whenever a consumer polls DeepQueue for new messages, it gets those messages which are NOT processed by any other consumer that may be concurrently accessing DeepQueue. 
When a consumer gets a set of messages, it must notify DeepQueue that it has processed each message (individually). This deletes that message from the DeepQueue database. If a message is received by a consumer, but NOT marked as processed within a configurable amount of time, the message then becomes available to any consumer requesting again.
