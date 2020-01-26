package dslabs.clientserver;

import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Node;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Simple server that receives requests and returns responses.
 *
 * See the documentation of {@link Node} for important implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleServer extends Node {
    private final KVStore app = new KVStore();



    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public SimpleServer(Address address, Application app) {
        super(address);

        // Your code here...
    }

    @Override
    public void init() {
        // No initialization necessary
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private void handleRequest(Request m, Address sender) {
        KVStoreResult res = app.execute(m.command());
        send(new Reply(res, m.sequenceNum), sender);


    }
    @Data
    class Request implements Message {
        private final Command command;
        private final int sequenceNum;
    }

    @Data
    class Reply implements Message {
        private final Result result;
        private final int sequenceNum;
    }
}
