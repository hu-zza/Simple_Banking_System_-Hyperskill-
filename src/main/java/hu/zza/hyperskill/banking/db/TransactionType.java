package hu.zza.hyperskill.banking.db;

import java.util.function.BiFunction;


public enum TransactionType
{
    CHECK_ACCOUNT_EXISTENCE(DB_Logic::isExist),
    
    ADD_ACCOUNT_TO_DATABASE(DB_Logic::addAccount),
    
    AUTHENTICATE_ACCOUNT(DB_Logic::authenticateAccount),
    
    SYNCHRONIZE_ACCOUNT(DB_Logic::synchronizeAccount),
    
    UPDATE_ACCOUNT(DB_Logic::updateAccount), // future: changing PIN, etc.
    
    CLOSE_ACCOUNT(DB_Logic::deleteAccount),
    
    MODIFY_BALANCE(DB_Logic::modifyBalance),
    
    DO_TRANSFER(DB_Logic::doTransfer);
    
    
    BiFunction<DataBase, DB_Query, DB_Reply> function;
    
    TransactionType(BiFunction<DataBase, DB_Query, DB_Reply> function)
    {
        this.function = function;
    }
}
