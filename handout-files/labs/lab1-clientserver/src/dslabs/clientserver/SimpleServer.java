package dslabs.clientserver;

import dslabs.framework.Result;
import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Node;
import dslabs.kvstore.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashMap;
import com.google.common.base.Objects;

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




    }

    @Override
    public void init() {
        // No initialization necessary
        HashMap<Object, Object> atmostonce = new HashMap<Object, Object>();
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private void handleRequest(Request m, Address sender) {
        int id = m.sequenceNum;
        if (atmostonce.containsKey(sender)) {
            previous_reply = atmostonce.get(sender);
            if (Objects.equals(id, previous_reply.sequenceNum)) {
                send(previous_reply, sender);
            } else {
                Result res = app.execute(m.command());
                atmostonce.replace(sender, res);
            }

        } else {
            atmostonce.put(sender, m);
            Result res = app.execute(m.command());


            send(new Reply(res, id), sender);

        }



    }
}
