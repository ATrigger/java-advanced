package ru.ifmo.ctddev.kamenev.bank;

import java.rmi.*;

public interface Bank extends Remote {
    // ������� ���
    Person createPerson(String name, String surname, String passportId)
            throws RemoteException;

    // �����頥� ���
    Person getPerson(String passportId, boolean local)
            throws RemoteException;
}
