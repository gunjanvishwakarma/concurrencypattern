package com.gunjan.stm;

public class AccountLockBasedSyncronization
{
    private Long lastUpdate;
    private Integer balance;
    
    public AccountLockBasedSyncronization(int balance)
    {
        this.lastUpdate = System.currentTimeMillis();
        this.balance = balance;
    }
    
    public void adjustBy(int amount)
    {
        adjustBy(amount, System.currentTimeMillis());
    }
    
    public synchronized void transferTo(AccountSTMBasedSynchronization other, int amount)
    {
        long date = System.currentTimeMillis();
        adjustBy(-amount, date);
        other.adjustBy(amount, date);
    }
    
    public synchronized void adjustBy(int amount, long date)
    {
        try
        {
            Thread.sleep(20);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
        balance += amount;
        lastUpdate = date;
    }
    
    public Integer getBalance()
    {
        return balance;
    }
}
