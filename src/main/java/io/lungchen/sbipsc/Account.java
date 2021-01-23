package io.lungchen.sbipsc;


import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

@DataType
public class Account {

    @Property
    private String accountId;

    @Property
    private Double balance;

    @Property
    private String publicKey;

    public Account(String accountId, String publicKey, Double balance) {
        this.accountId = accountId;
        this.publicKey = publicKey;
        this.balance = balance;
    }

    public String getAccountId() {
        return accountId;
    }

    public Double getBalance() {
        return balance;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String toJSONString() {
        return new JSONObject(this).toString();
    }

    public static Account fromJSONString(String jsonStr) {
        JSONObject jsonObj = new JSONObject(jsonStr);

        String accountId = jsonObj.getString("accountId");
        String publicKey = jsonObj.getString("publicKey");
        Double balance = jsonObj.getDouble("balance");

        return new Account(accountId, publicKey, balance);
    }

}
