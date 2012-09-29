We provide here an example of how an InvocationHandlerDelegate can add information extracted from the consumer to pass along to the producer, working in conjunction with an
associated producer-side InvocationHandlerDelegate to establish a round-trip communication channel outside of the standard WSRP protocol.

The scenario is as follows:
- ExampleConsumerInvocationHandlerDelegate provides an implementation of InvocationHandlerDelegate that can be attached to the consumer to add an extension containing the current
session id to render requests sent to the producer.
- ExampleProducerInvocationHandlerDelegate provides the counterpart of ExampleConsumerInvocationHandlerDelegate on the producer. It checks incoming render requests for potential
 extensions matching what ExampleConsumerInvocationHandlerDelegate sends and adds an extension of its own to the render response so that the consumer-side delegate can know that
 the information it passed was properly processed.