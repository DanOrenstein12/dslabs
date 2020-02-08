package dslabs.primarybackup;

import dslabs.framework.Address;
import dslabs.framework.Timer;
import dslabs.primarybackup.AMOApplication;
import dslabs.primarybackup.AMOCommand;
import dslabs.primarybackup.AMOResult;
import lombok.Data;

@Data
final class PingCheckTimer implements Timer {
    static final int PING_CHECK_MILLIS = 100;
}

@Data
final class PingTimer implements Timer {
    static final int PING_MILLIS = 25;
}

@Data
final class ClientTimer implements Timer {
    static final int CLIENT_RETRY_MILLIS = 100;

    // Your code here...
    final AMOCommand amoCommand;
}

@Data
final class ViewServerTimer implements Timer {
    static final int VIEW_SERVER_REGET_MILLIS = 200;
}

@Data
final class BackupAppRequestTimer implements Timer {
    static final int REQUEST_APP = 25;
    final AMOApplication app;

}

@Data
final class ForwardRequestTimer implements Timer {
    static final int FORWARD_AGAIN = 50;
    final Address sender;
    final AMOCommand command;
    final AMOResult result;


}
// Your code here...
