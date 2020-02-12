package dslabs.primarybackup;

import dslabs.framework.Address;
import dslabs.framework.Node;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static dslabs.primarybackup.PingCheckTimer.PING_CHECK_MILLIS;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class ViewServer extends Node {
    static final int STARTUP_VIEWNUM = 0;
    private static final int INITIAL_VIEWNUM = 1;

    // Your code here...
    private View view;
    private boolean acknowledge = false;
    private HashSet<Address> prevFrame;
    private HashSet<Address> currFrame;

    static final int STARTER = 0;
    public static int globalID = STARTER;
    private View new_view;

    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public ViewServer(Address address) {
        super(address);
    }

    @Override
    public void init() {
        set(new PingCheckTimer(), PING_CHECK_MILLIS);
        // Your code here...
        this.view = new View(STARTUP_VIEWNUM, null, null);
        this. new_view = this.view;
        prevFrame = new HashSet<>();
        currFrame = new HashSet<>();
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       ----------------------------------------------------------------------- reverting*/
    private void handlePing(Ping m, Address sender) {
        this.currFrame.add(sender);
        //if we don't have a primary, and are starting up
        if(this.view.primary() == null && this.view.viewNum() == STARTUP_VIEWNUM) {
            this.new_view = new View(INITIAL_VIEWNUM, sender, null);//set the sender to be the primary
            this.view = this.new_view;

            acknowledge = false;
        }
        if(Objects.equals(sender, this.new_view.primary()) && m.viewNum() == this.new_view.viewNum()) {
            acknowledge = true;
            this.view = this.new_view;
        }
        if(acknowledge) {//if primary is on the same view as viewserver
            if(this.view.backup() == null) {//if current view does not have a backup
                if(!Objects.equals(sender, this.view.primary())) {//if the sender is not the primary - so sender is idle
                    this.new_view = new View(this.view.viewNum() + 1, this.view.primary(), sender);//set sender to be the backup
                    acknowledge = false;//primary hasnt acknowledged this view
                } else if(getRealExtra(sender) != null) {//if there is an idle server, and the sender is the primary
                    this.new_view = new View(this.view.viewNum() + 1, this.view.primary(), getRealExtra(sender));//set idle server to backup
                    acknowledge = false;//primary hasn't  acknowledged this view yet
                }

            }

        }
        this.send(new ViewReply(this.view), sender);



    }

    private synchronized void handleGetView(GetView m, Address sender) {


        if (this.view.viewNum() == STARTUP_VIEWNUM) {
            this.send(new ViewReply(this.view), sender);

        }
        else if (Objects.equals(this.new_view.primary(), sender)) {
            this.send(new ViewReply(this.new_view), sender);



        } else {
            this.send(new ViewReply(this.view), sender);
        }
    }

    /* -------------------------------------------------------------------------
        Timer Handlers
       -----------------------------------------------------------------------*/
    private void onPingCheckTimer(PingCheckTimer t) {
        this.prevFrame = (HashSet)this.currFrame.clone();
        this.currFrame = new HashSet<>();
        if(acknowledge) {//primary server has the same view as viewserver as of last ping check timer
            if(!isAlive(this.view.primary())) {//if primary is not alive
                if(isAlive(this.view.backup())) {//if backup is alive
                    //then get new backup from idle pool, and promote backup to primary
                    this.new_view = new View(this.view.viewNum() + 1, this.view.backup(), getExtra(this.view.backup(), prevFrame));
                    acknowledge = false;
                }
            }
            else if(this.view.backup()!= null && !isAlive(this.view.backup())) {//primary is alive, but backup is not
                //get new backup from idle
                this.new_view = new View(this.view.viewNum() + 1, this.view.primary(), getExtra(this.view.primary(), prevFrame));
            }
        }
        set(t, PING_CHECK_MILLIS);
    }

    /* -------------------------------------------------------------------------
        Utils
       -----------------------------------------------------------------------*/
    // Your code here...
    private boolean isAlive(Address address) {
        if(currFrame.contains(address) || prevFrame.contains(address)) {
            return true;
        }
        return false;
    }

    private Address getRealExtra(Address address) {
        if(!this.currFrame.isEmpty()) {
            Address a1 = getExtra(address, currFrame);
            if(a1 != null)
                return a1;
        }
        return getExtra(address, prevFrame);
    }

    private Address getExtra(Address address, HashSet set) {
        Iterator<Address> it = set.iterator();
        while(it.hasNext()) {
            Address t = it.next();
            if(!Objects.equals(t, address)) {
                return t;
            }
        }
        return null;
    }

}
