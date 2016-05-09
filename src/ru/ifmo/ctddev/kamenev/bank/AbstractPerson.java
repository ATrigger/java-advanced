package ru.ifmo.ctddev.kamenev.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by petrovich on 09.05.16.
 */
public abstract class AbstractPerson implements  Person{
    protected final String name;
    protected final String surname;
    protected final String passport;
    protected final Map<String, Account> accs;
    protected final int port;

    public AbstractPerson(String name, String surname, String passport, int port){
        this.name = name;
        this.surname = surname;
        this.passport = passport;
        this.port = port;
        this.accs = new ConcurrentHashMap<>();
    }
    public AbstractPerson(AbstractPerson other){
        this.name = other.name;
        this.surname = other.surname;
        this.passport = other.passport;
        synchronized (other.accs){
            this.accs = new ConcurrentHashMap<>(other.accs);
        }
        this.port = other.port;
    }
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public String getSurname() throws RemoteException {
        return surname;
    }

    @Override
    public Account addAccount(String accountId) throws RemoteException {
        if (accs.containsKey(accountId)) {
            throw new IllegalStateException();
        }
        AccountImpl acc = new AccountImpl(accountId);
        accs.put(accountId,acc);
        if(this instanceof RemotePerson) {
            UnicastRemoteObject.exportObject(acc,port);
        }
        return acc;
    }

    @Override
    public Account getAccount(String accountId) throws RemoteException {
        return accs.get(accountId);
    }
}
