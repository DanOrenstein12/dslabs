package dslabs.primarybackup;

import com.google.common.base.Objects;
import dslabs.framework.Address;
import dslabs.framework.Client;
import dslabs.framework.Command;
import dslabs.framework.Node;
import dslabs.framework.Result;
import dslabs.primarybackup.AMOCommand;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static dslabs.primarybackup.ClientTimer.CLIENT_RETRY_MILLIS;
import static dslabs.primarybackup.ViewServerTimer.VIEW_SERVER_REGET_MILLIS;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class PBClient extends Node implements Client {
    private final Address viewServer;

    // Your code here...
    private Command command;
    private Result result;
    private final int clientID = ++ViewServer.globalID;
    private int MaxjobID;

    private View view;

    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public PBClient(Address address, Address viewServer) {
        super(address);
        this.viewServer = viewServer;
    }

    @Override
    public void init() {
        // Your code here...
        this.send(new GetView(), this.viewServer);
        this.set(new ViewServerTimer(), VIEW_SERVER_REGET_MILLIS);
        while (view == null) {
            wait();
        }

    }

    /* -------------------------------------------------------------------------
        Client Methods
       -----------------------------------------------------------------------*/
    @Override
    public void sendCommand(Command command) {
        // Your code here...
        this.command = command;
        this.result = null;

        AMOCommand c = new AMOCommand(command, ++this.MaxjobID, this.clientID);
//        System.out.println(this.view);
        this.send(new Request(c), this.view.primary());
        this.set(new ClientTimer(c), CLIENT_RETRY_MILLIS);
    }

    @Override
    public synchronized boolean hasResult() {
        // Your code here...
        return this.result != null;
    }

    @Override
    public synchronized Result getResult() throws InterruptedException {
        // Your code here...
        while(this.result == null) {
            this.wait();
        }
        return result;

    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private synchronized void handleReply(Reply m, Address sender) {
        // Your code here...
        if(m.amoResult() != null && m.amoResult().sequenceNum() == this.MaxjobID) {
            this.result = m.amoResult().result();
            this.notify();
        }
    }

    private synchronized void handleViewReply(ViewReply m, Address sender) throws InterruptedException {
        // Your code here...
        this.view = m.view();
        notify();
//        System.out.println("view reply: " + this.view);
    }

    // Your code here...

    /* -------------------------------------------------------------------------
        Timer Handlers
       -----------------------------------------------------------------------*/
    private void onClientTimer(ClientTimer t) {
        // Your code here...
        if (this.command != null && this.result == null && t.amoCommand().sequenceNum() == this.MaxjobID
                && Objects.equal(this.command, t.amoCommand().command())) {
            this.send(new Request(t.amoCommand), this.view.primary());
            this.set(t, CLIENT_RETRY_MILLIS);
        }
    }

    private void onViewServerTimer(ViewServerTimer t) {
        this.send(new GetView(), this.viewServer);

        this.set(t, VIEW_SERVER_REGET_MILLIS);
    }
}
