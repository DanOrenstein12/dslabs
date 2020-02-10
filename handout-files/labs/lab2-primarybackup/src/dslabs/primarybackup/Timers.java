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
    final Request request;
}

@Data
final class ViewServerTimer implements Timer {
    static final int VIEW_SERVER_REGET_MILLIS = 200;
}

@Data
final class BackupAppRequestTimer implements Timer {
    static final int APP_REQUEST_RETRY_MILLIS = 25;
    final AMOApplication app;
}

@Data
final class ForwardRequestTimer implements Timer {
    static final int FORWARD_RETRY_MILLIS = 50;
    final ForwardRequest request;
}

@Data
final class BackupReplyTimer implements Timer {
    static final int BACKUP_REPLY_RETRY_MILLIS = 50;
    final Reply reply;
}

@Data
final class ClientInitTimer implements Timer {
    static final int INIT_GET_VIEW_RETRY_MILLIS = 100;
}
