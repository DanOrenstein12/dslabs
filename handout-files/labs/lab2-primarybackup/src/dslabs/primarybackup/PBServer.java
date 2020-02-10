//package dslabs.primarybackup;
//
//import dslabs.framework.Address;
//import dslabs.framework.Application;
//import dslabs.framework.Node;
//import dslabs.primarybackup.AMOApplication;
//import dslabs.primarybackup.AMOCommand;
//import dslabs.primarybackup.AMOResult;
//import java.util.Objects;
//import lombok.EqualsAndHashCode;
//import lombok.ToString;
//
//import dslabs.primarybackup.*;
//
//@ToString(callSuper = true)
//@EqualsAndHashCode(callSuper = true)
//class PBServer extends Node {
//    private final Address viewServer;
//
//    // Your code here...
//    private AMOApplication app;
//    private View view;
//    private boolean isPrimary = false;
//    private boolean isBackup = false;
//    private boolean latestApp = false;
//
//
//    private Request recentRequest;
//    private Reply recentReply;
//
//
//    /* -------------------------------------------------------------------------
//        Construction and Initialization
//       -----------------------------------------------------------------------*/
//    PBServer(Address address, Address viewServer, Application app) {
//        super(address);
//        this.viewServer = viewServer;
//        this.app = new AMOApplication(app);
//    }
//
//    @Override
//    public void init() {
//        this.view = new View(dslabs.primarybackup.ViewServer.STARTUP_VIEWNUM, null, null);
//        this.send(new Ping(this.view.viewNum()), this.viewServer);
//        this.set(new PingTimer(), PingTimer.PING_MILLIS);
//        this.recentRequest = null;
//        this.recentReply = null;
//    }
//
//    /* -------------------------------------------------------------------------
//        Message Handlers
//       -----------------------------------------------------------------------*/
//
//
//
//
//    private synchronized void handleRequest(Request m, Address sender) throws InterruptedException {
//        if (isPrimary) {
//            if (hasBackup()) {
//                ForwardRequest forward_request = new ForwardRequest(m, sender);
//                this.send(forward_request, this.view.backup());
//                this.set(new ForwardRequestTimer(forward_request), 100);
//
//            }
//
//            this.recentReply = new Reply(this.app.execute(m.amoCommand()), m.globRequestID());
//            this.send(recentReply, sender);
//            recentReply = null;
//
//        }
//    }
//
//
//
//    //Waiting could be wrong. Instead it may be right to just have recalling shit in the timer. I think thats similar to what David did
//
//
//
//
//
//
//    private void handleForwardRequest(ForwardRequest m, Address sender) {
//        Reply work = new Reply(this.app.execute(m.request().amoCommand()), m.globRequestID());
//        this.send(work,sender);
//        this.recentReply= null;
//
//    }
//
//
//    private synchronized void handleViewReply(ViewReply m, Address sender) {
//        this.view = m.view();
//        if(!isPrimary && !isBackup) {//is idle
//            if(Objects.equals(m.view().primary(), this.address())) {//idle -> primary
//                this.isPrimary = true;
//                this.isBackup = false;
//            } else if(Objects.equals(m.view().backup(), this.address())) { // idle -> backup
//                this.isPrimary = false;
//                this.isBackup = true;
//                // Become new backup, I need the app from the primary to be synchronized
//                this.send(new AppRequest(), this.view.primary());
//                // if not receive the app, send again
//                this.set(new BackupAppRequestTimer(this.app), BackupAppRequestTimer.APP_REQUEST_RETRY_MILLIS);
//            }
//        } else if(isBackup && Objects.equals(m.view().primary(), this.address())) {// backup -> primary
//            this.isBackup = false;
//            this.isPrimary = true;
//        } else if(isPrimary && !Objects.equals(m.view().primary(), this.address())) {// primary -> idle
//            this.isPrimary = false;
//            this.isBackup = false;
//            this.reset();
//        }
//    }
//
//
//
//
//
//    private synchronized void handleReply(Reply m, Address sender) {
//
//        this.recentReply = m;
//
//
//
//    }
//
//    //this server is backup, and is getting the most recent application from the new primary
//    //also note we only execute this if we don't already have an application from the primary - this ensures at most once execution
//    private void handleAppReply(AppReply appReply, Address sender) {
//        if(!latestApp && appReply != null && Objects.equals(sender, this.view.primary())) {
//            this.app = appReply.app();
//            this.recentReply = appReply.recentReply();
//            this.recentRequest = appReply.recentRequest();
//            latestApp = true;
//        }
//    }
//
//    // I'm the primary, and I need to send my app to the backup
//    //note we do not need a timer for this
//    private void handleAppRequest(AppRequest m, Address sender) {
//        if(this.view.backup() != null && this.app != null && Objects.equals(sender, this.view.backup())) {
//            AMOApplication temp = this.app.clone();
//            this.send(new AppReply(temp,this.recentRequest,this.recentReply), sender);
//        }
//    }
//
//    /* -------------------------------------------------------------------------
//        Utils
//       -----------------------------------------------------------------------*/
//    private void reset() {
//        app = null;
//        isPrimary = false;
//        isBackup = false;
//        latestApp = false;
//        this.recentReply = null;
//        this.recentRequest = null;
//    }
//
//    private boolean hasBackup() {
//        return (view != null && view.backup() != null);
//    }
//
//    private boolean hasRequest() {
//        return recentRequest != null;
//    }
//
//    private boolean hasReply() {
//        return recentReply != null;
//    }
//
//    private boolean hasCurrentReply() {
//        //assumes has reply
//        return (recentReply.globRequestID() == recentRequest.globRequestID());
//    }
//
//       /* -------------------------------------------------------------------------
//        Timer Handlers
//       -----------------------------------------------------------------------*/
//    // backup request may need to be reissued - do not reset timer if we already have the app reply from primary
//    private void onBackupAppRequestTimer(BackupAppRequestTimer t) {
//        if(isBackup && !latestApp) {
//            this.send(new AppRequest(), this.view.primary());
//            this.set(t, BackupAppRequestTimer.APP_REQUEST_RETRY_MILLIS);
//        }
//    }
//
//
//    private void onPingTimer(PingTimer t) {
//        if( isPrimary) {
//            latestApp = true;
//        }
//        this.send(new Ping(this.view.viewNum()), this.viewServer);
//        this.set(t, PingTimer.PING_MILLIS);
//    }
//
//    private void onForwardRequestTimer(ForwardRequestTimer t) {
//        if (recentReply == null) {
//            this.send(t.request(),this.view.backup());
//            this.set(t, t.FORWARD_RETRY_MILLIS);
//        }
//    }
//
//}

package dslabs.primarybackup;

import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Node;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import dslabs.primarybackup.PBClient;
import dslabs.primarybackup.ViewServer;
import java.util.Objects;
import dslabs.primarybackup.AMOApplication;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class PBServer extends Node {
    private final Address viewServer;

    private AMOApplication app;
    private View view;
    private boolean isPrimary;
    private boolean isBackup;
    private boolean latestApp;

    private java.util.HashMap<Integer,ForwardRequest> outstandingRequests;

    private PingTimer ping;

    private int recentlyHandledForward;
    private int forwardedID;

    private ForwardRequest recentForwarded;

    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    PBServer(Address address, Address viewServer, Application app) {
        super(address);
        this.viewServer = viewServer;
        this.app = new AMOApplication<Application>(app);
    }

    @Override
    public void init() {
        view = new View(ViewServer.STARTUP_VIEWNUM,null,null);
        this.send(new Ping(ViewServer.STARTUP_VIEWNUM),viewServer);
        ping = new PingTimer();
        this.set(ping,ping.PING_MILLIS);
        this.isPrimary = false;
        this.isBackup = false;
        this.latestApp = false;
        this.recentForwarded = null;
        this.forwardedID = 0;
        this.recentlyHandledForward = 0;
        outstandingRequests = new java.util.HashMap<>();
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private void handleRequest(Request m, Address sender) {
        // primary just forwards request onto backup if it exists, or acts as single server if not
        if (latestApp) {
            if (isPrimary) {
                if (hasBackup()) {
                    if (outstandingRequests.containsKey(m.globRequestID())) {
                        this.send(outstandingRequests.get(m.globRequestID()), this.view.backup());
                    } else {
                        ForwardRequest f = new ForwardRequest(m, ++forwardedID, sender);
                        this.send(f, this.view.backup());
                        this.app.execute(m.amoCommand());
                        this.outstandingRequests.put(m.globRequestID(), f);
                        this.recentlyHandledForward = m.globRequestID();
                    }
                } else {
                    Reply toRep = new Reply(this.app.execute(m.amoCommand()), m.globRequestID());
                    this.send(toRep, sender);
                }
            }
        }
    }

    private void handleViewReply(ViewReply m, Address sender) {
        // Your code here...
        //we have cases on whether is primary or is backup or is neither
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

    // Your code here...
    private void handleForwardRequest(ForwardRequest m,Address sender) {
        if (latestApp && this.view != null && this.view.primary() != null && isBackup && Objects.equals(sender,this.view.primary())) {
            if (m.ID() <= recentlyHandledForward + 1 ) {
                this.send(new BackupReply(new Reply(this.app.execute(m.request().amoCommand()), m.request().globRequestID()), m.ID(), m.client()), sender);
                if (m.ID() == recentlyHandledForward + 1) {
                    this.recentlyHandledForward = m.request().globRequestID();
                }
            }
        }
    }

    private void handleBackupReply (BackupReply m,Address sender) {
        if (outstandingRequests.containsKey(m.reply().globRequestID())) {
            outstandingRequests.remove(m.reply().globRequestID());
        }
        this.send(m.reply(),m.client());
    }



    // Your code here...
    //this server is backup, and is getting the most recent application from the new primary
    //also note we only execute this if we don't already have an application from the primary - this ensures at most once execution
    private void handleAppReply(AppReply appReply, Address sender) {
        if(!latestApp && appReply != null && Objects.equals(sender, this.view.primary())) {
            this.reset();
            this.app = appReply.app();
            this.recentlyHandledForward = appReply.mostRecentlyExecutedRequest();
            latestApp = true;
        }
    }

    // I'm the primary, and I need to send my app to the backup
    //note we do not need a timer for this
    private void handleAppRequest(AppRequest m, Address sender) {
        if(this.view.backup() != null && this.app != null && Objects.equals(sender, this.view.backup())) {
            //AMOApplication temp = this.app.clone();
            this.send(new AppReply(this.application, this.recentlyHandledForward), sender);
        }
    }

    /* -------------------------------------------------------------------------
        Timer Handlers
       -----------------------------------------------------------------------*/
    private void onPingTimer(PingTimer t) {
        if( isPrimary) {
            latestApp = true;
        }
        this.send(new Ping(this.view.viewNum()), this.viewServer);
        this.set(t, PingTimer.PING_MILLIS);
    }

    // backup request may need to be reissued - do not reset timer if we already have the app reply from primary
    private void onBackupAppRequestTimer(BackupAppRequestTimer t) {
        if(isBackup && !latestApp && this.view.primary() != null) {
            this.send(new AppRequest(), this.view.primary());
            this.set(t, BackupAppRequestTimer.APP_REQUEST_RETRY_MILLIS);
        }
    }

    /* -------------------------------------------------------------------------
        Utils
       -----------------------------------------------------------------------*/
    // Your code here...
    private boolean hasBackup() {
        return (view != null && view.backup() != null);
    }

    private void reset() {
        this.isPrimary = false;
        this.isBackup = false;
        this.latestApp = false;
        this.recentForwarded = null;
        forwardedID = 0;
        outstandingRequests = new java.util.HashMap<>();
    }
}

