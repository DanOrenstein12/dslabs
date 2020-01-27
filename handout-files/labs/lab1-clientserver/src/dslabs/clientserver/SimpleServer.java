package dslabs.clientserver;

import dslabs.framework.Result;
import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Node;
import dslabs.kvstore.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.Hashtable;
import java.util.Map;

/**
 * Simple server that receives requests and returns responses.
 *
 * See the documentation of {@link Node} for important implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleServer extends Node {
    private final KVStore app = new KVStore();
//    Map<int, int> kv = new Hashtable();





    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public SimpleServer(Address address, Application app) {
        super(address);
//        Map<int, int> client_records = new Hashtable();



    }

    @Override
    public void init() {
        // No initialization necessary
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private void handleRequest(Request m, Address sender) {
        int id = m.sequenceNum
        Result res = app.execute(m.command());

        send(new Reply(res, id), sender);


    }
}
