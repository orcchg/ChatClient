package com.orcchg.chatclient.mock;

import com.orcchg.chatclient.data.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MockProvider {

    public static List<Message> createMessages() {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message.Builder(1000, "Maxim").setMessage("Hello! I'm Maxim").build());
        messages.add(new Message.Builder(1000, "Maxim").setMessage("Good weather today").build());
        messages.add(new Message.Builder(1000, "Maxim").setMessage("Let's go eat something").build());
        messages.add(new Message.Builder(1001, "Oleg").setMessage("Oleg Muratov").build());
        messages.add(new Message.Builder(1001, "Oleg").setMessage("I'm going to be hired").build());
        messages.add(new Message.Builder(1001, "Oleg").setMessage("In beautiful California").build());
        messages.add(new Message.Builder(1002, "Artem").setMessage("Artem Shamsuarov").build());
        messages.add(new Message.Builder(1002, "Artem").setMessage("Huawei is my employer").build());
        messages.add(new Message.Builder(1002, "Artem").setMessage("So, I'm an employee of Huawei").build());
        messages.add(new Message.Builder(1003, "Vladimir").setMessage("Vova invites all to Seliger").build());
        messages.add(new Message.Builder(1004, "Lyuba").setMessage("I'm pregnant!").build());
        messages.add(new Message.Builder(1005, "Nastya").setMessage("She is very special").build());
        messages.add(new Message.Builder(1006, "Olga").setMessage("Olga Frolova online").build());
        messages.add(new Message.Builder(1007, "President").setMessage("Mr. President").build());
        return messages;
    }
}
