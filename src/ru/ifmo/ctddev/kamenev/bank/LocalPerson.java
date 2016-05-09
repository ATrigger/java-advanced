package ru.ifmo.ctddev.kamenev.bank;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by petrovich on 09.05.16.
 */
public class LocalPerson extends AbstractPerson {
    public LocalPerson(String name, String surname, String passport) {
        super(name, surname, passport, 0);
    }

    public LocalPerson(AbstractPerson other) {
        super(other);
    }
}
