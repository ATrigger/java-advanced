package ru.ifmo.ctddev.kamenev.bank;

import java.io.Serializable;
import java.math.BigDecimal;
import java.rmi.*;

public interface Account extends Remote, Serializable {

    String getId()
            throws RemoteException;


    BigDecimal getAmount()
            throws RemoteException;


    void incAmount(BigDecimal amount)
            throws RemoteException;
}