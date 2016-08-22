package com.orcchg.chatclient.ui.chat.peerslist;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.orcchg.chatclient.R;
import com.orcchg.chatclient.data.viewobject.PeerVO;
import com.orcchg.chatclient.resources.PhotoItem;
import com.orcchg.jgravatar.Gravatar;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

public class SideChatPeersList extends ChatPeersList {

    private PeersAdapter mAdapter;

    public SideChatPeersList(PeersAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public void addItem(long id, PeerVO peer) {
        mAdapter.addItem(id, peer);
    }

    @Override
    public void removeItem(long id) {
        mAdapter.removeItem(id);
    }

    /* Adapter */
    // --------------------------------------------------------------------------------------------
    public static class PeersAdapter extends RecyclerView.Adapter<PeersAdapter.PeerViewHolder> {
        private List<PeerVO> mPeerVOs;

        public PeersAdapter() {
            mPeerVOs = new ArrayList<>();
        }

        private void addItem(long id, PeerVO peer) {
            mPeerVOs.add(peer);
            notifyItemInserted(mPeerVOs.size());
        }

        private void removeItem(long id) {
            for (int index = 0; index < mPeerVOs.size(); ++index) {
                if (mPeerVOs.get(index).getId() == id) {
                    mPeerVOs.remove(index);
                    notifyItemRemoved(index);
                    break;
                }
            }
        }

        @Override
        public PeerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_chat_peers_list_item, parent, false);
            return new PeerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PeerViewHolder holder, int position) {
            PeerVO viewObject = mPeerVOs.get(position);
            holder.bind(viewObject);
        }

        @Override
        public int getItemCount() {
            return mPeerVOs.size();
        }

        class PeerViewHolder extends RecyclerView.ViewHolder {
            private View mRoot;
            @Bind(R.id.pi_icon) PhotoItem mIcon;
            @Bind(R.id.tv_label) TextView mLabel;

            private PeerViewHolder(View rootView) {
                super(rootView);
                mRoot = rootView;
//                ButterKnife.bind(mRoot);

                mIcon = (PhotoItem) mRoot.findViewById(R.id.pi_icon);
                mLabel = (TextView) mRoot.findViewById(R.id.tv_label);
            }

            private void bind(PeerVO viewObject) {
                Gravatar gravatar = new Gravatar();
                String url = gravatar.getUrl(viewObject.getEmail());
                Drawable drawable = mRoot.getContext().getResources().getDrawable(R.drawable.green_circle);

                mIcon.setPhoto(url, true);
                mLabel.setText(viewObject.getLogin());

                mRoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO: click peer
                    }
                });
            }
        }
    }
}