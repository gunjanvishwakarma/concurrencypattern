package com.gunjan.stm;

import org.multiverse.api.StmUtils;
import org.multiverse.api.references.TxnInteger;
import org.multiverse.api.references.TxnLong;

public class AccountSTMBasedSynchronization
{
    private TxnLong lastUpdate;
    private TxnInteger balance;
    
    public AccountSTMBasedSynchronization(int balance)
    {
        this.lastUpdate = StmUtils.newTxnLong(System.currentTimeMillis());
        this.balance = StmUtils.newTxnInteger(balance);
    }
    
    public void adjustBy(int amount)
    {
        adjustBy(amount, System.currentTimeMillis());
    }
    
    public void transferTo(AccountSTMBasedSynchronization other, int amount)
    {
        StmUtils.atomic(() -> {
            long date = System.currentTimeMillis();
            adjustBy(-amount, date);
            other.adjustBy(amount, date);
        });
    }
    
    public void adjustBy(int amount, long date)
    {
        StmUtils.atomic(() -> {
            try
            {
                Thread.sleep(20);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
            balance.increment(amount);
            lastUpdate.set(date);
            
            /*if (balance.get() <= 0) {
                throw new IllegalArgumentException("Not enough money");
            }*/
        });
    }
    
    public Integer getBalance()
    {
        return balance.atomicGet();
    }
}
