package ru.ifmo.ctddev.kamenev.bank;

import java.io.Serializable;
import java.math.BigDecimal;
import java.rmi.*;

public interface Account extends Remote, Serializable {
    // Узнать идентификатор
    String getId()
        throws RemoteException;

    // Узнать количество денег
    BigDecimal getAmount()
        throws RemoteException;

    // Установить количество денег
    void incAmount(BigDecimal amount)
        throws RemoteException;
}