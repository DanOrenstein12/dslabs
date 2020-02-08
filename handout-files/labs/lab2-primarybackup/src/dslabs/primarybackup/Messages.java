package dslabs.primarybackup;

import dslabs.framework.Message;
import dslabs.primarybackup.AMOApplication;
import dslabs.primarybackup.AMOCommand;
import dslabs.primarybackup.AMOResult;
import lombok.Data;

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
}

@Data
class Reply implements Message {
    // Your code here...
    private final AMOResult amoResult;
}


@Data
class AppRequest implements Message {
    // Your code here
}

@Data
class AppReply implements Message {
    private final AMOApplication app;
}
// Your code here...
