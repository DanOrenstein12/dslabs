package dslabs.primarybackup;

import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Result;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import dslabs.kvstore.*;

@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public final class AMOApplication<T extends Application>
        implements Application {
    @Getter @NonNull private final T application;

    // Your code here...
    private java.util.HashMap<Integer,AMOResult> clients = new java.util.HashMap<Integer,AMOResult>(2^10);

    @Override
    public AMOResult execute(Command command) {
        if (!(command instanceof AMOCommand)) {
            throw new IllegalArgumentException();
        }

        AMOCommand amoCommand = (AMOCommand) command;

        // Your code here...
        if (!alreadyExecuted(amoCommand)) {
            AMOResult res = new AMOResult(application.execute(amoCommand.command()),amoCommand.sequenceNum(),amoCommand.clientID());
            clients.put(amoCommand.clientID(), res);
            return res;
        }
        else {
            //already been executed - resend last reply sent to that client
            return clients.get(amoCommand.clientID());

        }
    }

    public Result executeReadOnly(Command command) {
        if (!command.readOnly()) {
            throw new IllegalArgumentException();
        }

        if (command instanceof AMOCommand) {
            return execute(command);
        }

        return application.execute(command);
    }

    public boolean alreadyExecuted(AMOCommand amoCommand) {
        // Your code here...
        int id = amoCommand.clientID();
        if (clients.containsKey(id) ){
            int seqNum = clients.get(id).sequenceNum();
            if (amoCommand.sequenceNum() <= seqNum) {//we only execute a command if it's later in the sequence of commands
                return true;
            }
        }
        return false;
    }

    private void setHashMap(java.util.HashMap<Integer,AMOResult> map) {
        this.clients = (java.util.HashMap<Integer,AMOResult>) map.clone();
    }

    @Override
    public AMOApplication clone() {
        AMOApplication clone = new AMOApplication(this.application);
        clone.setHashMap(this.clients);
        return clone;
    }
}
