package com.orcchg.chatclient.ui.chat.peerslist;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;

import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.data.viewobject.PeerVO;
import com.orcchg.chatclient.ui.chat.ChatActivity;

public class PopupMenuChatPeersList extends ChatPeersList {

    private PopupMenu mPopupMenu;

    public PopupMenuChatPeersList(PopupMenu popupMenu) {
        mPopupMenu = popupMenu;
    }

    @Override
    public void addItem(long id, PeerVO peer) {
        Menu menu = mPopupMenu.getMenu();
        MenuItem item1 = menu.findItem((int) id);
        if (item1 == null) {
            menu.add(ChatActivity.MENU_GROUP_ID_USERS, (int) id, menu.size(), peer.getLogin());
            menu.setGroupCheckable(ChatActivity.MENU_GROUP_ID_USERS, true, true);
            setDestId(mDestId);
        }
    }

    @Override
    public void removeItem(long id) {
        Menu menu = mPopupMenu.getMenu();
        menu.removeItem((int) id);
    }

    @Override
    public void setDestId(long id) {
        super.setDestId(id);
        Menu menu = mPopupMenu.getMenu();
        if (mDestId == Status.UNKNOWN_ID) {
            for (int i = 0; i < menu.size(); ++i) {
                menu.getItem(i).setChecked(false);
            }
        } else {
            MenuItem item2 = menu.findItem((int) mDestId);
            if (item2 != null) {
                item2.setChecked(true);
            }
        }
    }
}
