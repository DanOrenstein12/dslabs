package dslabs.clientserver;

import dslabs.framework.Message;
import dslabs.framework;
import lombok.Data;

@Data
class Request implements Message {
    private final Command command;
    private final int sequenceNum;
}

@Data
class Reply implements Message {
    private final Result result;
    private final int sequenceNum;
}
