package hu.zza.hyperskill.banking.db;

public enum ReplyType {
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