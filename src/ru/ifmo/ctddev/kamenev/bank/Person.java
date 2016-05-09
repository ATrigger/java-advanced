package ru.ifmo.ctddev.kamenev.bank;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by petrovich on 09.05.16.
 */
public interface Person extends Remote, Serializable {
    String getName() throws RemoteException;

    String getSurname() throws RemoteException;

    Account addAccount(String accountId) throws RemoteException;

    Account getAccount(String accountId) throws RemoteException;

}
