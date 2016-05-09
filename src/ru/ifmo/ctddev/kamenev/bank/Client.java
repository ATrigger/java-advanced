package ru.ifmo.ctddev.kamenev.bank;

import java.math.BigDecimal;
import java.rmi.*;
import java.net.*;

public class Client {

    public static void main(String[] args) throws RemoteException {
        if (args.length != 5) {
            System.out.println("Invalid number of arguments");
        }
        Bank bank;
        String name = args[0];
        String surname = args[1];
        String passportId = args[2];
        String accountId = args[3];
        BigDecimal deltaMoney = new BigDecimal(args[4]);
        boolean isLocal = args.length > 5 && args[5].equals("1");

        try {
            bank = (Bank) Naming.lookup("rmi://localhost/bank");
        } catch (NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }
        Person person = bank.getPerson(passportId, isLocal);
        if (person == null) {
            System.out.println("Creating person");
            person = bank.createPerson(name, surname, passportId);
        }

        String bankName = person.getName();
        String bankSurname = person.getSurname();

        if (!name.equals(bankName) || !surname.equals(bankSurname)) {
            System.out.println("Profile information does not match");
            return;
        }

        Account account = person.getAccount(accountId);
        if (account == null) {
            System.out.println("Creating account");
            account = person.addAccount(accountId);
        } else {
            System.out.println("Account already exists");
        }

        System.out.println("Money: " + account.getAmount());
        System.out.println("Adding money");
        account.incAmount(deltaMoney);
        System.out.println("Money: " + account.getAmount());
    }
}
