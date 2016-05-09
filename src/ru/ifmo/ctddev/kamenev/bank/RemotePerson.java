package ru.ifmo.ctddev.kamenev.bank;

import java.rmi.Remote;

/**
 * Created by petrovich on 09.05.16.
 */
public class RemotePerson extends AbstractPerson implements Remote {

    public RemotePerson(String name, String surname, String passport, int port) {
        super(name, surname, passport, port);
    }

}
