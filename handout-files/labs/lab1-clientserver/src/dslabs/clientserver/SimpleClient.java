package dslabs.clientserver;

import dslabs.framework.Address;
import dslabs.framework.Client;
import dslabs.framework.Command;
import dslabs.framework.Node;
import dslabs.framework.Result;
import dslabs.kvstore.*;
import java.util.Objects.*;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.google.common.base.Objects;



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
        request_number ++;
        Request outgoing = new Request(command, request_number);
        this.current_command = command;
        this.current_result = null;

        this.send(outgoing, serverAddress);
        this.set(new ClientTimer(command, request_number),100);

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
        if (Objects.equal(current_command.value(), m.result().value())) {
            current_result = m.result();
            notify();
        }
    }

    /* -------------------------------------------------------------------------
        Timer Handlers
       -----------------------------------------------------------------------*/
    private synchronized void onClientTimer(ClientTimer t) {
        if (current_command != null && Objects.equal(current_result, t.command()) && current_result == null) {
            this.request_number++;
            send(new Request(current_command, this.request_number), serverAddress);
            set(t, 100);
        }
    }
}
