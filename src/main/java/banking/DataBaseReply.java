package banking;

import java.util.Objects;

class DataBaseReply
{
    private final ReplyType replyType;
    private final String[] details;
    
    DataBaseReply(ReplyType replyType, String... details)
    {
        this.replyType = replyType;
        this.details = details.clone();
    }
    
    ReplyType getReplyType()
    {
        return replyType;
    }
    
    String[] getDetails()
    {
        return details.clone();
    }
    
    boolean isType(ReplyType replyType)
    {
        return Objects.equals(this.replyType, replyType);
    }
    
    boolean isNotType(ReplyType replyType)
    {
        return !isType(replyType);
    }
    
    enum ReplyType
    {
        ERROR,
        
        CONNECTED,
        NOT_CONNECTED,
    
        EXISTS,
        NOT_EXISTS,

        AUTHENTICATED,
        SYNCHRONIZED,
    
        CREATED,
        MODIFIED,
        UPDATED,
        AVAILABLE,
        TRANSFERRED,
        CLOSED
   }
}
