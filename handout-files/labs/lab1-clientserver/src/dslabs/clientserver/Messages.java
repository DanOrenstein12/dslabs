package dslabs.clientserver;

import dslabs.framework.Command;
import dslabs.framework.Message;
import dslabs.framework.Result;
import lombok.Data;

@Data
class Request implements Message {
    private final Command command;
    public final int sequenceNum;
}

@Data
class Reply implements Message {
    private final Result result;
    public final int sequenceNum;
}
