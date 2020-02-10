package dslabs.primarybackup;

import dslabs.framework.Message;
import dslabs.primarybackup.AMOApplication;
import dslabs.primarybackup.AMOCommand;
import dslabs.primarybackup.AMOResult;
import lombok.Data;
import dslabs.framework.Address;

/* -------------------------------------------------------------------------
    ViewServer Messages
   -----------------------------------------------------------------------*/
@Data
class Ping implements Message {
    private final int viewNum;
}

@Data
class GetView implements Message {
}

@Data
class ViewReply implements Message {
    private final View view;
}

/* -------------------------------------------------------------------------
    Primary-Backup Messages
   -----------------------------------------------------------------------*/
@Data
class Request implements Message {
    // Your code here...
    private final AMOCommand amoCommand;
    private final int globRequestID;
}

@Data
class Reply implements Message {
    // Your code here...
    private final AMOResult amoResult;
    private final int globRequestID;
}

@Data
class ForwardRequest implements Message {
    private final Request request;
    private final int globRequestID;
    private final Address client;
}

@Data
class BackupReply implements Message {
    private final AMOResult amoResult;
 //   private final Request request;
    //private final int ID;
    private final Address client;
}

@Data
class AppRequest implements Message {
    //TODO
}

@Data
class AppReply implements Message {
    private final AMOApplication app;
    private final Request recentRequest;
    private final Reply recentReply;
}
// Your code here...
