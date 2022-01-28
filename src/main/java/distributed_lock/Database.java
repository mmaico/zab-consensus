package distributed_lock;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Database {
    public static LinkedList<Integer> data = new LinkedList<>();

    static {
        for (int i = 1; i <= 100 ; i++) {
            data.add(i);
        }
    }

    public static List<Integer> findNext(Integer lastValueProcessed, Integer quantity) {
        int index = data.indexOf(lastValueProcessed);
        if (lastValueProcessed == 0) return data.subList(0, quantity);
        if (index < 0) return new ArrayList<>();
        if ((index + quantity) > data.size()) return new ArrayList<>();
        return data.subList(index + 1, (index + quantity) + 1);
    }
}
