package com.orcchg.chatclient.mock;

import com.orcchg.chatclient.data.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MockProvider {

    public static List<Message> createMessages() {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message.Builder(1000, "Maxim", "maxim@ya.ru").setMessage("Hello! I'm Maxim").build());
        messages.add(new Message.Builder(1000, "Maxim", "maxim@ya.ru").setMessage("Good weather today").build());
        messages.add(new Message.Builder(1000, "Maxim", "maxim@ya.ru").setMessage("Let's go eat something").build());
        messages.add(new Message.Builder(1001, "Oleg", "oleg@ya.ru").setMessage("Oleg Muratov").build());
        messages.add(new Message.Builder(1001, "Oleg", "oleg@ya.ru").setMessage("I'm going to be hired").build());
        messages.add(new Message.Builder(1001, "Oleg", "oleg@ya.ru").setMessage("In beautiful California").build());
        messages.add(new Message.Builder(1002, "Artem", "artem@ya.ru").setMessage("Artem Shamsuarov").build());
        messages.add(new Message.Builder(1002, "Artem", "artem@ya.ru").setMessage("Huawei is my employer").build());
        messages.add(new Message.Builder(1002, "Artem", "artem@ya.ru").setMessage("So, I'm an employee of Huawei").build());
        messages.add(new Message.Builder(1003, "Vladimir", "vladimir@ya.ru").setMessage("Vova invites all to Seliger").build());
        messages.add(new Message.Builder(1004, "Lyuba", "lyuba@ya.ru").setMessage("I'm pregnant!").build());
        messages.add(new Message.Builder(1005, "Nastya", "nastya@ya.ru").setMessage("She is very special").build());
        messages.add(new Message.Builder(1006, "Olga", "olga@ya.ru").setMessage("Olga Frolova online").build());
        messages.add(new Message.Builder(1007, "President", "president@ya.ru").setMessage("Mr. President").build());
        return messages;
    }
}
