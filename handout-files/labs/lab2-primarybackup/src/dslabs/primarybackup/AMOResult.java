package dslabs.primarybackup;

import dslabs.framework.Result;
import lombok.Data;

@Data
public final class AMOResult implements Result {
    // Your code here...
    private final Result result;
    private final int sequenceNum;
    private final int clientID;

//    public AMOResult(Result r, int s, int c) {
//        setClientID(c);
//        setResult(r);
//        setSequenceNum(s);
//    }
//
//    public Result getResult() {
//        return result;
//    }
//
//    public void setResult(Result result) {
//        this.result = result;
//    }
//
//    public void setSequenceNum(int sequenceNum) {
//        this.sequenceNum = sequenceNum;
//    }
//
//    public int getSequenceNum() {
//        return sequenceNum;
//    }
//
//    public void setClientID(int clientID) {
//        this.clientID = clientID;
//    }
//
//    public int getClientID() {
//        return clientID;
//    }
}
