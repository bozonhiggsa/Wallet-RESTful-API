package mobi.puut.controllers;


import java.util.*;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.utils.MonetaryFormat;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletChangeEventListener;

import static mobi.puut.controllers.WalletManager.networkParameters;

/**
 * Created by Chaklader on 6/12/17.
 */
public class WalletModel {

    private List<Transaction> transactions = Collections.synchronizedList(new ArrayList<>());

    private ProgressBarUpdater syncProgressUpdater = new ProgressBarUpdater();

    private static double SYNCHRONISATION_FINISHED = 1.0;

    private double syncProgress = -1.0;

    private Coin balance = Coin.ZERO;

    private Address address;

    private String transaction;

    private int UserId;

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        this.UserId = userId;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    private List<String> history = new ArrayList<>();

    public List<String> getHistory() {

        for (Transaction t : transactions) {
            history.add(addTransactionHistory(t));
        }
        return history;
    }

    public WalletModel() {

    }

    public WalletModel(Wallet wallet) {
        setWallet(wallet);
    }

    private void update(Wallet wallet) {

        this.balance = wallet.getBalance();

        System.out.println("The balance = " + this.balance);

        this.address = wallet.currentReceiveAddress();

        transactions.addAll(wallet.getRecentTransactions(100,
                true));

        this.transaction = Objects.isNull(transactions) || transactions.isEmpty()
                ? "" : String.valueOf(transactions.get(0));
    }

    public boolean setWallet(Wallet wallet) {
        try {
            wallet.addChangeEventListener(new WalletChangeEventListener() {
                @Override
                public void onWalletChanged(Wallet wallet) {
                    update(wallet);
                }
            });
            update(wallet);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private class ProgressBarUpdater extends DownloadProgressTracker {

        @Override
        protected void progress(double percentage, int blocksSoFar, Date date) {
            super.progress(percentage, blocksSoFar, date);
            syncProgress = percentage / 100.0;
        }

        @Override
        protected void doneDownload() {
            super.doneDownload();
            syncProgress = SYNCHRONISATION_FINISHED;
        }
    }

    public boolean isSyncFinished() {
        return syncProgress == SYNCHRONISATION_FINISHED;
    }

    public Address getAddress() {
        return address;
    }

    /**
     * @param
     * @return the Satoshi coin based on the wallet balance
     */
    public Coin getBalance() {
        return balance;
    }


    /**
     * @return get the BTC amount as float from the wallet balance
     */
    public float getBalanceFloatFormat() {

        float bal = (float) balance.getValue();
        float fac = (float) Math.pow(10, 8);

        float result = bal / fac;
        return result;
    }

    /**
     * @param transaction take the wallet transaction as an input
     * @return the trasaction info of the wallet
     */
    private String addTransactionHistory(Transaction transaction) {

        if (Objects.isNull(transaction)) {
            return "No transaction";
        }

        Coin value = transaction.getValue(WalletManager.bitcoin.wallet());

        if (value.isPositive()) {
            String message = "Incoming payment of " + MonetaryFormat.BTC.format(value);
            return message;
        } else if (value.isNegative()) {
            Address address = transaction.getOutput(0).getAddressFromP2PKHScript(networkParameters);
            String message = "Outbound payment to " + address + " worth of " +
                    (MonetaryFormat.BTC.format(value)).toString().replaceAll("-", "");
            return message;
        }

        String message = "Payment with id " + transaction.getHash();
        return message;
    }

    public double getSyncProgress() {
        return syncProgress;
    }

    public ProgressBarUpdater getSyncProgressUpdater() {
        return syncProgressUpdater;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}

