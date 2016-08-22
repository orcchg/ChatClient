package com.orcchg.chatclient.ui.chat.peerslist;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.orcchg.chatclient.data.viewobject.PeerVO;
import com.orcchg.chatclient.ui.chat.ChatActivity;
import com.orcchg.jgravatar.Gravatar;

public class DrawerChatPeersList extends ChatPeersList {

    private Drawer mDrawer;

    public DrawerChatPeersList(Drawer drawer) {
        mDrawer = drawer;
    }

    @Override
    public void addItem(long id, PeerVO peer) {
        String url = new Gravatar().getUrl(peer.getEmail());
        long shiftedId = ChatActivity.DRAWER_ITEM_ID_CUSTOM + id;

        IDrawerItem item = new ProfileDrawerItem()
                .withName(peer.getLogin())
                .withEmail(peer.getEmail())
                .withIcon(url)
                .withIdentifier(shiftedId);

        if (mDrawer.getDrawerItem(shiftedId) == null) {
            mDrawer.addItem(item);
        } else {
            mDrawer.updateItem(item);
        }
    }

    @Override
    public void removeItem(long id) {
        mDrawer.removeItem(ChatActivity.DRAWER_ITEM_ID_CUSTOM + id);
    }
}
