package com.tesfaye.lolchat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.github.theholywaffle.lolchatapi.LolChat;
import com.github.theholywaffle.lolchatapi.listeners.ChatListener;
import com.github.theholywaffle.lolchatapi.wrapper.Friend;

import java.util.ArrayList;

public class ChatActivity extends Activity implements ServiceConnection, ChatListener
        {
    private String friendName;
    private Friend friend;
    private ListView conversation;
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lolchat_chat);
        final EditText messageBox = (EditText) findViewById(R.id.messageBox);
        Button send = (Button) findViewById(R.id.messageSend);
        conversation = (ListView) findViewById(R.id.listView);
        friendName = getIntent().getStringExtra("friend");
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(friend != null)
                {
                    String message = messageBox.getText().toString();
                    friend.sendMessage(message, ChatActivity.this);
                    ArrayAdapter adapter = (ArrayAdapter)conversation.getAdapter();
                    adapter.add("Me: " + message.toString());
                    adapter.notifyDataSetChanged();
                    messageBox.setText("");
                }
            }
        });
        if(savedInstanceState != null)
        {
            conversation.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, savedInstanceState.getStringArrayList("messages")));
            conversation.onRestoreInstanceState(savedInstanceState.getParcelable("listView"));
        }else
        {
            conversation.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>()));
        }
        bindService(new Intent(this, ChatService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
        ChatService chatService = ((ChatService.LocalBinder) service).getService();
        LolChat lolChat = chatService.getLolChat();
        friend = lolChat.getFriendByName(friendName);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
    }
    @Override
    public void onServiceDisconnected(final ComponentName name) {}

    @Override
    public void onMessage(final Friend friend, final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter adapter = (ArrayAdapter)conversation.getAdapter();
                adapter.add(friend.getName() + ": " + message);
                adapter.notifyDataSetChanged();
            }
        });
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable("listView", conversation.onSaveInstanceState());
        ArrayList<String> messages = new ArrayList<String>();
        for (int i = 0; i < conversation.getAdapter().getCount(); i++) {
            messages.add((String)conversation.getAdapter().getItem(i));
        }
        savedInstanceState.putStringArrayList("messages", messages);
    }
}
