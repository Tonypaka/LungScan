package com.example.lungscan;

import java.util.HashMap;
import java.util.Map;

public class MockAuth {
    private static final Map<String, String> users = new HashMap<>();

    public static boolean login(String email, String password) {
        return password.equals(users.get(email));
    }

    public static boolean register(String email, String password) {
        if (users.containsKey(email)) return false;
        users.put(email, password);
        return true;
    }
}
