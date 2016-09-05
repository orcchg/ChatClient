package com.orcchg.chatclient.ui.chat.peerslist;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.orcchg.chatclient.R;
import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.data.viewobject.PeerVO;
import com.orcchg.chatclient.resources.PhotoItem;
import com.orcchg.jgravatar.Gravatar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

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

    @Override
    public void setDestId(long id) {
        super.setDestId(id);
        mAdapter.setDestId(id);
    }

    /* Adapter */
    // --------------------------------------------------------------------------------------------
    public static class PeersAdapter extends RecyclerView.Adapter<PeersAdapter.PeerViewHolder> {
        private List<PeerVO> mPeerVOs;
        private Map<Long, Integer> mPeerIds;

        private int mPrevPosition = -1;
        private PeerVO mPrevSelectedPeer;

        public interface OnPeerSelect {
            void onSelect(long id, boolean selected);
        }

        private OnPeerSelect mCallback;

        public PeersAdapter(OnPeerSelect callback) {
            mPeerVOs = new ArrayList<>();
            mPeerIds = new HashMap<>();
            mCallback = callback;
        }

        private void addItem(long id, PeerVO peer) {
            if (id != Status.UNKNOWN_ID && !mPeerIds.containsKey(Long.valueOf(id))) {
                mPeerVOs.add(peer);
                int position = mPeerVOs.size() - 1;
                mPeerIds.put(id, position);
                notifyItemInserted(mPeerVOs.size());
            }
        }

        private void removeItem(long id) {
            if (mPeerIds.containsKey(id)) {
                int index = mPeerIds.get(id);
                if (mPeerVOs.size() > index) {
                    mPeerVOs.remove(index);
                }
                mPeerIds.remove(id);
                notifyItemRemoved(index);
            }
        }

        private void setDestId(long id) {
            if (id == Status.UNKNOWN_ID) {
                if (mPrevPosition >= 0) {
                    notifyItemChanged(mPrevPosition);
                }
                if (mPrevSelectedPeer != null) {
                    mPrevSelectedPeer.setSelected(false);
                }
                mPrevPosition = -1;
                mPrevSelectedPeer = null;
            } else if (mPeerIds.containsKey(id)) {
                int index = mPeerIds.get(id);
                if (mPeerVOs.size() > index) {
                    mPrevSelectedPeer = mPeerVOs.get(index);
                    if (mPrevSelectedPeer != null) {
                        mPrevPosition = index;
                        mPrevSelectedPeer.setSelected(true);
                        notifyItemChanged(index);
                    }
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
            holder.bind(viewObject, position);
        }

        @Override
        public int getItemCount() {
            return mPeerVOs.size();
        }

        class PeerViewHolder extends RecyclerView.ViewHolder {
            private View mRoot;
            @Bind(R.id.ll_item_container) ViewGroup mContainer;
            @Bind(R.id.pi_icon) PhotoItem mIcon;
            @Bind(R.id.tv_label) TextView mLabel;
            @Bind(R.id.select_indicator) View mSelectIndicator;

            private @ColorInt int mSelectedBackground;

            private PeerViewHolder(View rootView) {
                super(rootView);
                mRoot = rootView;
                ButterKnife.bind(this, mRoot);

                mSelectedBackground = rootView.getContext().getResources().getColor(R.color.colorControlActivatedSemi);
            }

            private void bind(final PeerVO viewObject, final int position) {
                Gravatar gravatar = new Gravatar();
                String url = gravatar.getUrl(viewObject.getEmail());
                Drawable drawable = mRoot.getContext().getResources().getDrawable(R.drawable.green_circle);

                mIcon.setPhoto(url, true);
                mLabel.setText(viewObject.getLogin());

                mRoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean selected = !viewObject.isSelected();
                        viewObject.setSelected(selected);
                        notifyItemChanged(position);

                        if (mPrevSelectedPeer != null) {
                            mPrevSelectedPeer.setSelected(false);
                            notifyItemChanged(mPrevPosition);
                        }

                        mPrevPosition = position;
                        mPrevSelectedPeer = viewObject;

                        if (mCallback != null) {
                            mCallback.onSelect(viewObject.getId(), selected);
                        }
                    }
                });

                selectItem(viewObject.isSelected());
            }

            private void selectItem(boolean isSelected) {
                if (isSelected) {
                    mContainer.setBackgroundColor(mSelectedBackground);
                    mSelectIndicator.setVisibility(View.VISIBLE);
                } else {
                    mContainer.setBackgroundColor(Color.TRANSPARENT);
                    mSelectIndicator.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
}
