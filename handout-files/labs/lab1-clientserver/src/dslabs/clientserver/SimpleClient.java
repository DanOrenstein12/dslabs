package dslabs.clientserver;

import dslabs.framework.Address;
import dslabs.framework.Client;
import dslabs.framework.Command;
import dslabs.framework.Node;
import dslabs.framework.Result;
import dslabs.kvstore.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Simple client that sends requests to a single server and returns responses.
 *
 * See the documentation of {@link } and {@link } for important
 * implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleClient extends Node implements Client {
    private final Address serverAddress;

    private int request_number = 0;
    private Command current_command;
    private Result current_result;






    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public SimpleClient(Address address, Address serverAddress) {
        super(address);
        this.serverAddress = serverAddress;
    }

    @Override
    public synchronized void init() {
        // No initialization necessary
    }

    /* -------------------------------------------------------------------------
        Client Methods
       -----------------------------------------------------------------------*/
    @Override
    public synchronized void sendCommand(Command command) {
        this.request_number ++;
        Request outgoing = new Request(command, this.request_number);
        this.current_request = command;
        this.current_reply = null;

        this.send(outgoing, serverAddress);
        this.set(new ClientTimer(command, request_number));

    }

    @Override
    public synchronized boolean hasResult() {
        return current_result != null;
    }

    @Override
    public synchronized Result getResult() throws InterruptedException {
        while (current_result == null) {
            wait();
        }
        return current_result;
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private synchronized void handleReply(Reply m, Address sender) {
        if (Objects.equal(current_command.value(), m.result().value()) && Objects.equal(current_command.key(), m.result().key())) {
            pong = m.pong();
            notify();
        }
    }

    /* -------------------------------------------------------------------------
        Timer Handlers
       -----------------------------------------------------------------------*/
    private synchronized void onClientTimer(ClientTimer t) {
        if (current_command != null && Objects.equal(current_result, t.command()) && pong == null) {
            this.request_number++;
            send(new Request(ping, this.request_number), serverAddress);
            set(t, 100);
        }
    }
}
