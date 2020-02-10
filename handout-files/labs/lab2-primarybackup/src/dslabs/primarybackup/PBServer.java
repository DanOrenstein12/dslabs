package dslabs.primarybackup;

import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Node;
import dslabs.primarybackup.AMOApplication;
import dslabs.primarybackup.AMOCommand;
import dslabs.primarybackup.AMOResult;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import dslabs.primarybackup.*;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class PBServer extends Node {
    private final Address viewServer;

    // Your code here...
    private AMOApplication app;
    private View view;
    private boolean isPrimary = false;
    private boolean isBackup = false;
    private boolean latestApp = false;


    private Request recentRequest;
    private Reply recentReply;


    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    PBServer(Address address, Address viewServer, Application app) {
        super(address);
        this.viewServer = viewServer;
        this.app = new AMOApplication(app);
    }

    @Override
    public void init() {
        this.view = new View(dslabs.primarybackup.ViewServer.STARTUP_VIEWNUM, null, null);
        this.send(new Ping(this.view.viewNum()), this.viewServer);
        this.set(new PingTimer(), PingTimer.PING_MILLIS);
        this.recentRequest = null;
        this.recentReply = null;
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/


//    private synchronized void handleRequest(Request m, Address sender)
//            throws InterruptedException {
//        if (isPrimary) {
//            if (hasBackup()) {
//                if (hasRequest()) {
//                    if (hasReply()) {
//                        if (hasCurrentReply()) {
//                            if (m.globRequestID() ==
//                                    recentRequest.globRequestID() + 1) {
//                                //we are getting the next command in sequence
//                                //set recent request, forward request on to backup and execute
//                                recentRequest = m;
//                                recentReply = null;
//                                this.send(new ForwardRequest(m,sender),this.view.backup());
//                                this.app.execute(m.amoCommand());
//                            } else if (m.globRequestID() ==
//                                    recentRequest.globRequestID()) {
//                                //has a reply, and the incoming request has already been executed by both primary and backup
//                                this.send(recentReply,sender);
//                            }
//                            else if (m.globRequestID() < recentRequest.globRequestID()){
//                                //reply is old, and has been executed by both priamry and backup
//                                this.send(new Reply(this.app.execute(m.amoCommand()),m.globRequestID()),sender);
//                            }
//                            //if request is a future request, don't process it
//                        } else if (m.globRequestID() == recentReply.globRequestID()) {
//                            //dont have the current reply, but do have the reply to the message, so send it back
//                            this.send(recentReply,sender);
//                        } else if (m.globRequestID() < recentReply.globRequestID()) {
//                            //dont have current reply, and the incoming request has already been executed by both primary and backup
//                            this.send(new Reply(this.app.execute(m.amoCommand()),m.globRequestID()),sender);
//                        }
//                    }
//                    else {
//                        //we have a backup and request, and we don't have a reply
//                        if (m.globRequestID() == recentRequest.globRequestID()) {
//                            //if we are getting a repeat of the request, resend the command to the backup
//                            this.send(new ForwardRequest(m,sender),this.view.backup());
//                        }
//                    }
//                } else {
//                    //if we have a backup but dont have a request, then we havent sent any commands
//                    this.recentRequest = m;
//                    this.send(new ForwardRequest(m,sender),this.view.backup());
//                    this.app.execute(m.amoCommand());
//                }
//            }
//            else {
//                //if we dont have a backup, act as sole server
//                if (hasRequest() && m.globRequestID() == recentRequest.globRequestID()+1) {
//                    recentRequest = m;
//                }
//                this.recentReply = new Reply(this.app.execute(m.amoCommand()),m.globRequestID());
//                this.send(recentReply,sender);
//            }
//        }
//
//    }

    private synchronized void handleRequest(Request m, Address sender)
            throws InterruptedException {
        if (isPrimary) {
            if (hasBackup()) {
                ForwardRequest forward_request = new ForwardRequest(m, sender);
                this.send(forward_request, this.view.backup());
                this.set(new ForwardRequestTimer(forward_request), 100);

                }
            }
            this.recentReply = new Reply(this.app.execute(m.amoCommand()),m.globRequestID());
            this.send(recentReply,sender);
            recentReply = null;
    }

    //Waiting could be wrong. Instead it may be right to just have recalling shit in the timer. I think thats similar to what David did






    private void handleForwardRequest(ForwardRequest m, Address sender) {
        Reply work = new Reply(this.app.execute(m.request().amoCommand()), m.globRequestID());
        this.send(work,sender);
        this.recentReply= null;

    }


    private synchronized void handleViewReply(ViewReply m, Address sender) {
        this.view = m.view();
        if(!isPrimary && !isBackup) {//is idle
            if(Objects.equals(m.view().primary(), this.address())) {//idle -> primary
                this.isPrimary = true;
                this.isBackup = false;
            } else if(Objects.equals(m.view().backup(), this.address())) { // idle -> backup
                this.isPrimary = false;
                this.isBackup = true;
                // Become new backup, I need the app from the primary to be synchronized
                this.send(new AppRequest(), this.view.primary());
                // if not receive the app, send again
                this.set(new BackupAppRequestTimer(this.app), BackupAppRequestTimer.APP_REQUEST_RETRY_MILLIS);
            }
        } else if(isBackup && Objects.equals(m.view().primary(), this.address())) {// backup -> primary
            this.isBackup = false;
            this.isPrimary = true;
        } else if(isPrimary && !Objects.equals(m.view().primary(), this.address())) {// primary -> idle
            this.isPrimary = false;
            this.isBackup = false;
            this.reset();
        }
    }

//    private void handleForwardRequest(ForwardRequest req, Address sender) {
//        //if getting a request from the primary server, execute it on AMOApp and send back result
//        //to ensure linearizability of appends on backend server, need to execute in specific order
//        //  this order is given by the global request id of incoming requests
//        //  only execute if it is the first message ever handled, the next message in the global sequence of messages created, or is a duplicate - amoapp ensures appends dont act more than once
//        if (this.recentRequest == null || (latestApp && this.recentRequest.globRequestID()+1 >= req.request().globRequestID())) {
//            if (req.request() != null && req.request().amoCommand() != null) {
//                AMOResult res = app.execute(req.request().amoCommand());
//                if (this.recentRequest == null ||
//                        this.recentRequest.globRequestID() + 1 ==
//                                req.request().globRequestID()) {
//                    this.recentRequest = req.request();
//                }
//                this.recentReply = new Reply(res, req.request().globRequestID());
//                this.send(new BackupReply(this.recentReply, req.request(), req.client()), sender);
//            }
//        }
//    }



    private synchronized void handleReply(Reply m, Address sender) {

        this.recentReply = m;



    }

    //this server is backup, and is getting the most recent application from the new primary
    //also note we only execute this if we don't already have an application from the primary - this ensures at most once execution
    private void handleAppReply(AppReply appReply, Address sender) {
        if(!latestApp && appReply != null && Objects.equals(sender, this.view.primary())) {
            this.app = appReply.app();
            this.recentReply = appReply.recentReply();
            this.recentRequest = appReply.recentRequest();
            latestApp = true;
        }
    }

    // I'm the primary, and I need to send my app to the backup
    //note we do not need a timer for this
    private void handleAppRequest(AppRequest m, Address sender) {
        if(this.view.backup() != null && this.app != null && Objects.equals(sender, this.view.backup())) {
            AMOApplication temp = this.app.clone();
            this.send(new AppReply(temp,this.recentRequest,this.recentReply), sender);
        }
    }

    /* -------------------------------------------------------------------------
        Utils
       -----------------------------------------------------------------------*/
    private void reset() {
        app = null;
        isPrimary = false;
        isBackup = false;
        latestApp = false;
        this.recentReply = null;
        this.recentRequest = null;
    }

    private boolean hasBackup() {
        return (view != null && view.backup() != null);
    }

    private boolean hasRequest() {
        return recentRequest != null;
    }

    private boolean hasReply() {
        return recentReply != null;
    }

    private boolean hasCurrentReply() {
        //assumes has reply
        return (recentReply.globRequestID() == recentRequest.globRequestID());
    }

       /* -------------------------------------------------------------------------
        Timer Handlers
       -----------------------------------------------------------------------*/
    // backup request may need to be reissued - do not reset timer if we already have the app reply from primary
    private void onBackupAppRequestTimer(BackupAppRequestTimer t) {
        if(isBackup && !latestApp) {
            this.send(new AppRequest(), this.view.primary());
            this.set(t, BackupAppRequestTimer.APP_REQUEST_RETRY_MILLIS);
        }
    }


    private void onPingTimer(PingTimer t) {
        if( isPrimary) {
            latestApp = true;
        }
        this.send(new Ping(this.view.viewNum()), this.viewServer);
        this.set(t, PingTimer.PING_MILLIS);
    }

    private void onForwardRequestTimer(ForwardRequestTimer t) {
        if (recentReply == null) {
            this.send(t.request(),this.view.backup());
            this.set(t, t.FORWARD_RETRY_MILLIS);
        }
    }

}
