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
        prevFrame = new HashSet<>();
        currFrame = new HashSet<>();

    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private void handlePing(Ping m, Address sender) {
        // Your code here...
        this.currFrame.add(sender);
        if(this.view.primary() == null && this.view.viewNum() == STARTUP_VIEWNUM) {
            this.view = new View(INITIAL_VIEWNUM, sender, null);
            acknowledge = false;
        }
        if(Objects.equals(sender, this.view.primary()) && m.viewNum() == this.view.viewNum()) {
            acknowledge = true;
        }
        if(acknowledge) {
//            System.out.println(this.view.viewNum());
            if(this.view.backup() == null) {
                if(!Objects.equals(sender, this.view.primary())) {
                    this.view = new View(this.view.viewNum() + 1, this.view.primary(), sender);
                    acknowledge = false;
                } else if(getRealExtra(sender) != null) {
                    this.view = new View(this.view.viewNum() + 1, this.view.primary(), getRealExtra(sender));
                    acknowledge = false;
                }

            }
        }
        this.send(new ViewReply(this.view), sender);

    }

    private synchronized void handleGetView(GetView m, Address sender) {
        // Your code here...
        this.send(new ViewReply(this.view), sender);
    }

    /* -------------------------------------------------------------------------
        Timer Handlers
       -----------------------------------------------------------------------*/
    private void onPingCheckTimer(PingCheckTimer t) {
        // Your code here...
        this.prevFrame = (HashSet)this.currFrame.clone();
        this.currFrame = new HashSet<>();
        if(acknowledge) {
            if(!isAlive(this.view.primary())) {

                if(isAlive(this.view.backup())) {
                    this.view = new View(this.view.viewNum() + 1, this.view.backup(), getExtra(this.view.backup(), prevFrame));
                    acknowledge = false;
                }
            }
            else if(!isAlive(this.view.backup())) {
//                System.out.println("Im dead");
//                System.out.println(getExtra(this.view.backup(), prevFrame));
                this.view = new View(this.view.viewNum() + 1, this.view.primary(), getExtra(this.view.primary(), prevFrame));
//
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
