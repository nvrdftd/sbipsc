package io.lungchen.sbipsc;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;

import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;

@Contract(name = "sbipsc",
        info = @Info(
                title = "Account Contract",
                description = "",
                version = "1.0.0"
        )
)

@Default
public class AccountContract implements ContractInterface {

    @Transaction
    public Account createAccount(final Context ctx, final String accountId, final String publicKey, final Double initialBalance) {

        ChaincodeStub stub = ctx.getStub();

        if (accountExists(ctx, accountId)) {
            String errMsg = String.format("Account %s already exists.", accountId);
            throw new ChaincodeException(errMsg);
        }

        Account account = new Account(accountId, publicKey, initialBalance);
        stub.putStringState(accountId, account.toJSONString());

        return account;
    }

    @Transaction
    private boolean accountExists(final Context ctx, final String accountId) {
        ChaincodeStub stub = ctx.getStub();

        String value = stub.getStringState(accountId);
        return value != null && !value.isEmpty();
    }

    @Transaction
    public String peekPublicKey(final Context ctx, final String accountId) {
        ChaincodeStub stub = ctx.getStub();

        if (!accountExists(ctx, accountId)) {
            String errMsg = String.format("Account %s does not exist.", accountId);
            throw new ChaincodeException(errMsg);
        }

        Account account = Account.fromJSONString(stub.getStringState(accountId));

        return account.getPublicKey();
    }

    @Transaction
    public Double peekBalance(final Context ctx, final String accountId, final String publicKey) {

        ChaincodeStub stub = ctx.getStub();

        String accountStr = stub.getStringState(accountId);

        if (accountStr == null || accountStr.isEmpty()) {
            String errMsg = String.format("Account %s does not exist.", accountStr);
            throw new ChaincodeException(errMsg);
        }

        Account account = Account.fromJSONString(accountStr);

        if (!publicKey.equals(account.getPublicKey())) {
            String errMsg = String.format("Account %s is not associated with %s", accountId, publicKey);
            throw new ChaincodeException(errMsg);
        }

        return account.getBalance();
    }

    @Transaction
    public void updateBalance(final Context ctx, final String accountId, final Double balance) {

        if (!accountExists(ctx, accountId)) {
            String errMsg = String.format("Account %s does not exist.", accountId);
            throw new ChaincodeException(errMsg);
        }

        ChaincodeStub stub = ctx.getStub();

        Account account = Account.fromJSONString(stub.getStringState(accountId));
        account.setBalance(balance);
        stub.putStringState(accountId, account.toJSONString());
    }

    @Transaction
    public void transfer(final Context ctx, String fromAccountId, final String toAccountId, final Double amount, final String fromAccountPublicKey) {

        if (fromAccountId == toAccountId) {
            String errMsg = String.format("Two accounts cannot be the same.");
            throw new ChaincodeException(errMsg);
        }

        if (amount <= 0.0) {
            String errMsg = String.format("Amount cannot be zero or negative.");
            throw new ChaincodeException(errMsg);
        }

        ChaincodeStub stub = ctx.getStub();

        String fromStr = stub.getStringState(fromAccountId);

        if (fromStr == null || fromStr.isEmpty()) {
            String errMsg = String.format("Account %s does not exist.", fromAccountId);
            throw new ChaincodeException(errMsg);
        }

        String toStr = stub.getStringState(toAccountId);

        if (toStr == null || toStr.isEmpty()) {
            String errMsg = String.format("Account %s does not exist.", toAccountId);
            throw new ChaincodeException(errMsg);
        }

        Account fromAccount = Account.fromJSONString(fromStr);

        if (!fromAccountPublicKey.equals(fromAccount.getPublicKey())) {
            String errMsg = String.format("Account %s is not associated with %s", fromAccountId, fromAccountPublicKey);
            throw new ChaincodeException(errMsg);
        }

        Account toAccount = Account.fromJSONString(toStr);

        if (fromAccount.getBalance() - amount >= 0) {
            fromAccount.setBalance(fromAccount.getBalance() - amount);
            toAccount.setBalance(toAccount.getBalance() + amount);

            stub.putStringState(fromAccountId, fromAccount.toJSONString());
            stub.putStringState(toAccountId, toAccount.toJSONString());

        } else {
            String errMsg = String.format("Account %s does not have enough balance.", fromAccountId);
            throw new ChaincodeException(errMsg);
        }
    }

}
