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

import static dslabs.primarybackup.BackupAppRequestTimer.REQUEST_APP;
import static dslabs.primarybackup.ForwardRequestTimer.FORWARD_AGAIN;
import static dslabs.primarybackup.PingTimer.PING_MILLIS;
import static dslabs.primarybackup.ViewServer.STARTUP_VIEWNUM;

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

    private AMOResult backupResult;
    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    PBServer(Address address, Address viewServer, Application app) {
        super(address);
        this.viewServer = viewServer;
        // Your code here...
        this.app = new AMOApplication(app);
    }

    @Override
    public void init() {
        // Your code here...
        this.view = new View(STARTUP_VIEWNUM, null, null);
        this.send(new Ping(this.view.viewNum()), this.viewServer);
        this.set(new PingTimer(), PING_MILLIS);

    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/

    /**
     * Case 1: I'm primary, and primary forward the request to backup
     * Case 2: I'm backup, send the result back to Primary for say, I'm done
     * Case 3: I'm primary, and backup is not respond, send again.
     * Case 4: I'm back up, and primary wants my answer again, (Same as case 1)
     * Case 5, there is no backup, send back to client directly
     * @param m clients to primary
     * @param sender the address of sender
     */
    private synchronized void handleRequest(Request m, Address sender)
            throws InterruptedException {
        // Your code here...
        AMOResult result;
        if(m.amoCommand() != null) {
            AMOCommand amoCommand = m.amoCommand();
//            System.out.println(isPrimary);
            if(isPrimary) {
                result = this.app.execute(amoCommand);
//                System.out.println(m);
                if(this.view.backup() != null) {
                    // forward the request to the backup server and execute again
                    this.send(new Request(amoCommand), this.view.backup());
                    // Use timer to send again if not receive the result from the backup
                    this.set(new ForwardRequestTimer(sender, m.amoCommand(), result), FORWARD_AGAIN);
                    // Maybe need to wait for backup server
                    while (!Objects.equals(result, this.backupResult)) {
                        // Objects.equals(sender, this.view.backup()) &&
                        this.wait();
                    }
                }
                this.send(new Reply(result), sender);
            }else if (isBackup) {
                // Backup needs to tell primary its synchronized

                if(Objects.equals(sender, this.view.primary())) {
                    result = this.app.execute(amoCommand);
                    this.send(new Reply(result), sender);
                }
            }
        }

    }

    private synchronized void handleReply(Reply m, Address sender) {
        if(m.amoResult() != null && Objects.equals(sender, this.view.backup())) {
            this.backupResult = m.amoResult();
            this.notify();
        }
    }

    private void onForwardRequestTimer(ForwardRequestTimer t) {
        if (Objects.equals(t.sender(), this.view.backup()) &&
                !Objects.equals(t.result(), this.backupResult)) {
            this.send(new Request(t.command()), t.sender());
            this.set(t, FORWARD_AGAIN);
        }
    }



    private synchronized void handleViewReply(ViewReply m, Address sender) {
        // Your code here...
        this.view = m.view();
        if(!isPrimary && !isBackup) {
            if(Objects.equals(this.view.primary(), this.address())) {
                this.isPrimary = true;
                this.isBackup = false;
            } else if(Objects.equals(this.view.backup(), this.address())) { // I'm a backup now
                this.isPrimary = false;
                this.isBackup = true;
                // Become new backup, I need the app from the primary to be synchronized
                this.send(new AppRequest(), this.view.primary());
                // if not receive the app, send again
                this.set(new BackupAppRequestTimer(this.app), REQUEST_APP);
            }
        } else if(isBackup && Objects.equals(this.view.primary(), this.address())) {// become primary
            this.isBackup = false;
            this.isPrimary = true;
        } else if(isPrimary && !Objects.equals(this.view.primary(), this.address())) {// primary expired
            this.isPrimary = false;
            this.isBackup = false;
        }
        // Tell the handleRequest that the backup may change
        this.notify();
    }

    // Your code here...

    /* -------------------------------------------------------------------------
        Utils
       -----------------------------------------------------------------------*/
    // Your code here...

    private void handleAppReply(AppReply appReply, Address sender) {
        if(appReply != null && Objects.equals(sender, this.view.primary())) {
            this.app = appReply.app();
        }
    }

    // Your code here...
    // I'm the primary, and I need to send my app to the backup
    private void handleAppRequest(AppRequest m, Address sender) {
        if(Objects.equals(sender, this.view.backup())) {
            AMOApplication temp = this.app.clone();
            this.send(new AppReply(temp), sender);
        }

    }

    // backup request may need to be reassured
    private void onBackupAppRequestTimer(BackupAppRequestTimer t) {
        if(isBackup && !latestApp) {
            this.send(new AppRequest(), this.view.primary());
            this.set(t, REQUEST_APP);
        }
    }


    /* -------------------------------------------------------------------------
        Timer Handlers
       -----------------------------------------------------------------------*/
    private void onPingTimer(PingTimer t) {
        // Your code here...
        this.send(new Ping(this.view.viewNum()), this.viewServer);
        this.set(t, PING_MILLIS);

    }


}
