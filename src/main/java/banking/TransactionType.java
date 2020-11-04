package banking;

import java.util.function.BiFunction;


enum TransactionType
{
    CHECK_ACCOUNT_EXISTENCE(DataBaseLogic::isExist),
    
    ADD_ACCOUNT_TO_DATABASE(DataBaseLogic::addAccount),
    
    AUTHENTICATE_ACCOUNT(DataBaseLogic::authenticateAccount),
    
    SYNCHRONIZE_ACCOUNT(DataBaseLogic::synchronizeAccount),
    
    UPDATE_ACCOUNT(DataBaseLogic::updateAccount), // future: changing PIN, etc.
    
    CLOSE_ACCOUNT(DataBaseLogic::deleteAccount),
    
    MODIFY_BALANCE(DataBaseLogic::modifyBalance),
    
    DO_TRANSFER(DataBaseLogic::doTransfer);
    
    
    BiFunction<DataBase, DataBaseQuery, DataBaseReply> function;
    
    TransactionType(BiFunction<DataBase, DataBaseQuery, DataBaseReply> function)
    {
        this.function = function;
    }
}
