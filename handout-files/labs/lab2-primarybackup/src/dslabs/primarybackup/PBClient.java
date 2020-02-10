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
    private AMOCommand command;
    private AMOResult result;
    private final int clientID = ++ViewServer.globalID;
    private int MaxjobID;

    private View view;
    private boolean isViewCurrent;
//    private int numRetries;

    private static int globalRequestID = 0;

    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public PBClient(Address address, Address viewServer) {
        super(address);
        this.viewServer = viewServer;
    }

    @Override
    public void init() {
        view = null;
        isViewCurrent = false;
//        this.set(new ViewServerTimer(), VIEW_SERVER_REGET_MILLIS);
        this.send(new GetView(), this.viewServer);
//        numRetries = 0;
        result = new AMOResult(null,this.MaxjobID,this.clientID);
    }

    /* -------------------------------------------------------------------------
        Client Methods
       -----------------------------------------------------------------------*/


    @Override
    public void sendCommand(Command command) {
//        numRetries = 0;
        this.MaxjobID += 1;
        this.command = new AMOCommand(command, this.MaxjobID, this.clientID);
        Request req = new Request(this.command,++globalRequestID);
        if (isViewCurrent && this.view.primary() != null) {
            this.send(req,this.view.primary());
        }
        this.set(new ClientTimer(req), CLIENT_RETRY_MILLIS);
    }

    @Override
    public synchronized boolean hasResult() {
        return this.result.sequenceNum() == this.command.sequenceNum();
    }

    @Override
    public synchronized Result getResult() throws InterruptedException {
        while(!hasResult()) {
            this.wait();
        }
        return this.result.result();
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private synchronized void handleReply(Reply m, Address sender) {
        if(m.amoResult() != null && m.amoResult().sequenceNum() >= this.MaxjobID) {
            this.result = m.amoResult();
            this.notify();
        }
    }

    private synchronized void handleViewReply(ViewReply m, Address sender) {
        this.view = m.view();
        isViewCurrent = true;
        //numRetries = 0;
    }

    /* -------------------------------------------------------------------------
        Timer Handlers
       -----------------------------------------------------------------------*/
    private synchronized void onClientTimer(ClientTimer t) {
        if (result.sequenceNum() < t.request().amoCommand().sequenceNum()) {




            if (this.view.primary() != null) {
                this.send(t.request(), this.view.primary());

            }
            this.set(t, t.CLIENT_RETRY_MILLIS);
            this.send(new GetView(), this.viewServer);

        }

    }


}
//package dslabs.primarybackup;
//
//import com.google.common.base.Objects;
//import dslabs.framework.Address;
//import dslabs.framework.Client;
//import dslabs.framework.Command;
//import dslabs.framework.Node;
//import dslabs.framework.Result;
//import dslabs.primarybackup.AMOCommand;
//import lombok.EqualsAndHashCode;
//import lombok.ToString;
//
//import static dslabs.primarybackup.ClientTimer.CLIENT_RETRY_MILLIS;
//import static dslabs.primarybackup.ViewServerTimer.VIEW_SERVER_REGET_MILLIS;
//
//@ToString(callSuper = true)
//@EqualsAndHashCode(callSuper = true)
//class PBClient extends Node implements Client {
//    private final Address viewServer;
//
//    // Your code here...
//    private AMOCommand command;
//    private AMOResult result;
//    private final int clientID = ++ViewServer.globalID;
//    private int MaxjobID;
//
//    private View view;
//    private boolean isViewCurrent;
//    private int numRetries;
//
//    private static int globalRequestID = 0;
//
//    /* -------------------------------------------------------------------------
//        Construction and Initialization
//       -----------------------------------------------------------------------*/
//    public PBClient(Address address, Address viewServer) {
//        super(address);
//        this.viewServer = viewServer;
//    }
//
//    @Override
//    public void init() {
//        view = null;
//        isViewCurrent = false;
//        //        this.set(new ViewServerTimer(), VIEW_SERVER_REGET_MILLIS);
//        this.send(new GetView(), this.viewServer);
//        numRetries = 0;
//        result = new AMOResult(null,this.MaxjobID,this.clientID);
//    }
//
//    /* -------------------------------------------------------------------------
//        Client Methods
//       -----------------------------------------------------------------------*/
//
//
//    @Override
//    public void sendCommand(Command command) {
//        numRetries = 0;
//        this.MaxjobID += 1;
//        this.command = new AMOCommand(command, this.MaxjobID, this.clientID);
//        Request req = new Request(this.command,++globalRequestID);
//        if (isViewCurrent && this.view.primary() != null) {
//            this.send(req,this.view.primary());
//        }
//        this.set(new ClientTimer(req), CLIENT_RETRY_MILLIS);
//    }
//
//    @Override
//    public synchronized boolean hasResult() {
//        return this.result.sequenceNum() == this.command.sequenceNum();
//    }
//
//    @Override
//    public synchronized Result getResult() throws InterruptedException {
//        while(!hasResult()) {
//            this.wait();
//        }
//        return this.result.result();
//    }
//
//    /* -------------------------------------------------------------------------
//        Message Handlers
//       -----------------------------------------------------------------------*/
//    private synchronized void handleReply(Reply m, Address sender) {
//        if(m.amoResult() != null && m.amoResult().sequenceNum() >= this.MaxjobID) {
//            this.result = m.amoResult();
//            this.notify();
//        }
//    }
//
//    private synchronized void handleViewReply(ViewReply m, Address sender) {
//        this.view = m.view();
//        isViewCurrent = true;
//        numRetries = 0;
//    }
//
//    /* -------------------------------------------------------------------------
//        Timer Handlers
//       -----------------------------------------------------------------------*/
//    private synchronized void onClientTimer(ClientTimer t) {
//        if (result.sequenceNum() < t.request().amoCommand().sequenceNum()) {
//            numRetries++;
//            if (numRetries > 5) {
//                isViewCurrent = false;
//                numRetries = 0;
//                this.send(new GetView(), this.viewServer);
//            }
//            if (isViewCurrent && this.view.primary() != null) {
//                this.send(t.request(), this.view.primary());
//            }
//            else {
//                this.send(new GetView(), this.viewServer);
//            }
//            this.set(t, t.CLIENT_RETRY_MILLIS);
//        }
//    }

    //    private void onViewServerTimer(ViewServerTimer t) {
    //        if(!isViewCurrent) {
    //            this.send(new GetView(), this.viewServer);
    //            this.set(t, VIEW_SERVER_REGET_MILLIS);
    //        }
    //    }
}
