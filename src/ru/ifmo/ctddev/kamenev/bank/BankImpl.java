package ru.ifmo.ctddev.kamenev.bank;

import java.util.*;
import java.rmi.server.*;
import java.rmi.*;

public class BankImpl implements Bank {
    private final Map<String, RemotePerson> persons = new HashMap<>();
    private final int port;

    public BankImpl(final int port) {
        this.port = port;
    }

    // ������� ���
    public Person createPerson(String name, String surname, String passportId, boolean local) throws RemoteException {
        if(local) {
            return new LocalPerson(name,surname,passportId);
        } else {
            synchronized (this) {
                RemotePerson person = new RemotePerson(name,surname,passportId,port);
                persons.put(passportId,person);
                UnicastRemoteObject.exportObject(person, port);
                return person;
            }
        }
    }

    // �����頥� ���
    public Person getPerson(String passportId, boolean local) {
        RemotePerson person = persons.get(passportId);
        if(person == null){
            return null;
        }
        if(local) {return new LocalPerson(person);}
        else {return person;}

    }
}
