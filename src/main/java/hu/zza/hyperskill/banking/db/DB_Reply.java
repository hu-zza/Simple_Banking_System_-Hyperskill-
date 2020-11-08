package hu.zza.hyperskill.banking.db;

import java.util.Objects;


public class DB_Reply
{
    private final ReplyType replyType;
    private final String[]  details;
    
    public DB_Reply(ReplyType replyType, String... details)
    {
        this.replyType = replyType;
        this.details   = details.clone();
    }
    
    ReplyType getReplyType()
    {
        return replyType;
    }
    
    public boolean isType(ReplyType replyType)
    {
        return Objects.equals(this.replyType, replyType);
    }
    
    public boolean isNotType(ReplyType replyType)
    {
        return !isType(replyType);
    }
    
    public String[] getDetails()
    {
        return details.clone();
    }
    
    public enum ReplyType
    {
        ERROR,
        
        CONNECTED, NOT_CONNECTED,
        
        EXISTS, NOT_EXISTS,
        
        AUTHENTICATED, SYNCHRONIZED,
        
        CREATED, MODIFIED, UPDATED, AVAILABLE, TRANSFERRED, CLOSED
    }
}
