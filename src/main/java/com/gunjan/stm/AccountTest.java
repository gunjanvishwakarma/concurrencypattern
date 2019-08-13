package com.gunjan.stm;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public class AccountTest
{
    @Test
    public void givenAccount_whenDecrement_thenShouldReturnProperValue()
    {
        AccountSTMBasedSynchronization a = new AccountSTMBasedSynchronization(10);
        a.adjustBy(-5);
        
        assertEquals(a.getBalance().intValue(), 5);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void givenAccount_whenDecrementTooMuch_thenShouldThrow()
    {
        // given
        AccountSTMBasedSynchronization a = new AccountSTMBasedSynchronization(10);
        
        // when
        a.adjustBy(-11);
    }
    
    @Test
    public void testTransferTo()
    {
        AccountSTMBasedSynchronization a = new AccountSTMBasedSynchronization(10);
        AccountSTMBasedSynchronization b = new AccountSTMBasedSynchronization(10);
        
        a.transferTo(b, 5);
        
        assertEquals(a.getBalance().intValue(), 5);
        assertEquals(b.getBalance().intValue(), 15);
    }
    
    @Test
    public void deadlock() throws InterruptedException
    {
        ExecutorService ex = Executors.newFixedThreadPool(2);
        AccountSTMBasedSynchronization a = new AccountSTMBasedSynchronization(10);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicBoolean exceptionThrown = new AtomicBoolean(false);
        
        ex.submit(() -> {
            try
            {
                countDownLatch.await();
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
            
            try
            {
                a.adjustBy(-6);
            }
            catch(IllegalArgumentException e)
            {
                exceptionThrown.set(true);
            }
        });
        ex.submit(() -> {
            try
            {
                countDownLatch.await();
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
            try
            {
                a.adjustBy(-5);
            }
            catch(IllegalArgumentException e)
            {
                exceptionThrown.set(true);
            }
        });
        
        countDownLatch.countDown();
        ex.awaitTermination(1, TimeUnit.SECONDS);
        ex.shutdown();
        
        assertTrue(exceptionThrown.get());
    }
    
    @Test
    public void withdrawAndDepositConcurrentlyUnsafeAcount() throws InterruptedException
    {
        long start = System.currentTimeMillis();
        AccountNoSynchronization a = new AccountNoSynchronization(500);
        
        ExecutorService ex = Executors.newFixedThreadPool(200);
        
        ArrayList<Callable<String>> callables = new ArrayList<>();
        
        for(int i = 0; i < 1000; i++)
        {
            callables.add(() -> {
                a.adjustBy(10);
                return null;
            });
            
            callables.add(() -> {
                a.adjustBy(-10);
                return null;
            });
        }
        List<Future<String>> futures = ex.invokeAll(callables);
        ex.shutdown();
        ex.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
    
        long end = System.currentTimeMillis();
        System.out.println("Time taken = " + (end - start) + " milliseconds");
        assertEquals(100, a.getBalance().intValue());
    }
    
    @Test
    public void withdrawAndDepositConcurrentlySafeAcount() throws InterruptedException
    {
        long start = System.currentTimeMillis();
        AccountSTMBasedSynchronization a = new AccountSTMBasedSynchronization(100);
        
        ExecutorService ex = Executors.newFixedThreadPool(500);
        
        ArrayList<Callable<String>> callables = new ArrayList<>();
        
        for(int i = 0; i < 1000; i++)
        {
            callables.add(() -> {
                a.adjustBy(10);
                return null;
            });
            
            callables.add(() -> {
                a.adjustBy(-10);
                return null;
            });
        }
        ex.invokeAll(callables);
        ex.shutdown();
        ex.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        
        long end = System.currentTimeMillis();
        System.out.println("Time taken = " + (end - start) + " milliseconds");
        assertEquals(100, a.getBalance().intValue());
    }
    
    @Test
    public void withdrawAndDepositConcurrentlyAccountLockBasedSyncronization() throws InterruptedException
    {
        long start = System.currentTimeMillis();
        AccountLockBasedSyncronization a = new AccountLockBasedSyncronization(100);
        
        ExecutorService ex = Executors.newFixedThreadPool(500);
        
        ArrayList<Callable<String>> callables = new ArrayList<>();
        
        for(int i = 0; i < 1000; i++)
        {
            callables.add(() -> {
                a.adjustBy(10);
                return null;
            });
            
            callables.add(() -> {
                a.adjustBy(-10);
                return null;
            });
        }
        ex.invokeAll(callables);
        ex.shutdown();
        ex.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
    
    
        long end = System.currentTimeMillis();
        System.out.println("Time taken = " + (end - start) + " milliseconds");
        assertEquals(100, a.getBalance().intValue());
    }
}