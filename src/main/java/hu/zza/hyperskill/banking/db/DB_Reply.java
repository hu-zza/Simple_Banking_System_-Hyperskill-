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
    
    
    // REPLY TYPE
    
    public ReplyType getReplyType()
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
    
    
    // DETAILS
    
    public String[] getDetails()
    {
        return details.clone();
    }
}
