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
    HashMap<Address, Reply> atmostonce = new HashMap<Address, Reply>();






    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public SimpleServer(Address address, Application app) {
        super(address);




    }

    @Override
    public void init() {
        // No initialization necessary

    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private void handleRequest(Request m, Address sender) {
        int id = m.sequenceNum;
        if (this.atmostonce.containsKey(sender)) {
            Reply previous_reply = this.atmostonce.get(sender);
            if (Objects.equals(id, previous_reply.sequenceNum)) {
                send(previous_reply, sender);
            }
            else {
                Result res = app.execute(m.command());
                Reply new_reply = new Reply((res, id), sender);
                this.atmostonce.replace(sender, res);
                send(new_reply, sender);
            }

        }
        else {
            Result res = app.execute(m.command());
            Reply new_reply = new Reply((res, id), sender);
            this.atmostonce.put(sender, new_reply);
            send(new_reply, sender);


        }



    }
}
