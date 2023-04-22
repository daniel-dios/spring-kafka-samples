# spring-kafka-samples

## Simple producer

[Here](src/main/java/com/example/simpleproducer) we can see a basic (blocking) producer.

## Simple consumer

[Here](src/main/java/com/example/simpleconsumer) we can see a basic consumer, with a bonus track: how to set a default handler. 

## Batch consumer

[Here](src/main/java/com/example/batchconsumer) we can see a how con consume our messages in batches (actually is what spring kafka 
does by default event if we consume one by one in our listener). This can be used in case we want to operate our batch of messages 
transactional and reduce the number of executions. (i.e. if our consumption ends in an insert to database with this approach we can 
reduce the number of operations if we can insert in batches). 
