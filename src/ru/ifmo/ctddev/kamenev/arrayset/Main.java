
package ru.ifmo.ctddev.kamenev.arrayset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kamenev on 29.02.16.
 */
public class Main {
    public static void main(String[] args) {

        int[] a = {1, 1, 2, 2, 3, 5, 5};
        List<Integer> list = new ArrayList<>();
        for (int i : a) {
            list.add(i);
        }
        ArraySet<Integer> e=new ArraySet<Integer>(list);
        System.out.println(e);
        System.out.println(e.descendingSet().descendingSet());
    }
}
