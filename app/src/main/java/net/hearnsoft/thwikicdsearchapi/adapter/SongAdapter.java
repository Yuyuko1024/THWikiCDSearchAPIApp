package net.hearnsoft.thwikicdsearchapi.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import net.hearnsoft.thwikicdsearchapi.MainActivity;
import net.hearnsoft.thwikicdsearchapi.PhotoViewActivity;
import net.hearnsoft.thwikicdsearchapi.PrivateBrowserActivity;
import net.hearnsoft.thwikicdsearchapi.R;
import net.hearnsoft.thwikicdsearchapi.bean.DataBean;
import net.hearnsoft.thwikicdsearchapi.utils.DataToClipboard;
import net.hearnsoft.thwikicdsearchapi.widget.MarqueeTextView;

import org.apache.commons.text.StringEscapeUtils;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongInfoViewHolder>
{
    private DataBean.ResuBean resuBean;
    private View mItemView;

    public SongAdapter(DataBean.ResuBean resuBeanBean) {
        this.resuBean = resuBeanBean;
    }

    @NonNull
    @Override
    public SongInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_info_item,parent,false);
        return new SongInfoViewHolder(mItemView, resuBean);
    }

    @Override
    public void onBindViewHolder(@NonNull SongInfoViewHolder holder, int position) {
        String album = StringEscapeUtils.unescapeHtml3(replace(resuBean.getData().get(0).get(position).toString()));
        String title = StringEscapeUtils.unescapeHtml3(replace(resuBean.getData().get(1).get(position).toString()));
        String circle = StringEscapeUtils.unescapeHtml3(replace(resuBean.getData().get(2).get(position).toString()));
        String time = StringEscapeUtils.unescapeHtml3(replace(resuBean.getData().get(3).get(position).toString()));
        String artists = StringEscapeUtils.unescapeHtml3(replace(resuBean.getData().get(4).get(position).toString()));
        String ogmusic = replace(resuBean.getData().get(7).get(position).toString());
        String ogwork = replace(resuBean.getData().get(10).get(position).toString());
        String link = "https://thwiki.cc" + resuBean.getLink().get(position);
        String coverLink = replace(resuBean.getData().get(11).get(position).toString());
        Glide.with(mItemView.getContext())
                .load(coverLink)
                .placeholder(R.drawable.ic_unknown_pic)
                .into(holder.mCover);
        holder.title.setText(title);
        holder.circle.setText(mItemView.getContext().getString(R.string.item_str_circle,circle));
        holder.artists.setText(mItemView.getContext().getString(R.string.item_str_artists,artists));
        holder.album.setText(mItemView.getContext().getString(R.string.item_str_album,album));
        holder.ogmusic.setText(mItemView.getContext().getString(R.string.item_str_ogmusic,ogmusic));
        holder.ogwork.setText(mItemView.getContext().getString(R.string.item_str_ogwork,ogwork));
        holder.time.setText(time);
        holder.itemView.setOnClickListener(v -> {
            /*Intent intent = new Intent(v.getContext(), PrivateBrowserActivity.class);
            intent.putExtra("url",link);*/
            Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(link));
            v.getContext().startActivity(intent);
        });
        holder.mCover.setOnLongClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PhotoViewActivity.class);
            intent.putExtra("URL",coverLink);
            intent.putExtra("title",album);
            v.getContext().startActivity(intent);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return resuBean == null ? 0 : resuBean.getPage().size();
    }

    private static String replace(String text){
        return text.replace("[","")
                .replace("]","");
    }

    static class SongInfoViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener{

        private final ImageView mCover;
        private final MarqueeTextView title;
        private final MarqueeTextView album;
        private final MarqueeTextView circle;
        private final MarqueeTextView artists;
        private final MarqueeTextView ogmusic;
        private final MarqueeTextView ogwork;
        private final TextView time;
        private DataBean.ResuBean resuBean;

        public SongInfoViewHolder(@NonNull View itemView, DataBean.ResuBean resuBean) {
            super(itemView);
            this.mCover = itemView.findViewById(R.id.cover);
            this.title = itemView.findViewById(R.id.title);
            this.album = itemView.findViewById(R.id.album);
            this.circle = itemView.findViewById(R.id.circle);
            this.artists = itemView.findViewById(R.id.artists);
            this.ogmusic = itemView.findViewById(R.id.ogmusic);
            this.ogwork = itemView.findViewById(R.id.ogwork);
            this.time = itemView.findViewById(R.id.time);
            this.resuBean = resuBean;
            itemView.setOnCreateContextMenuListener(this);

        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(this.title.getText());
            initCopySubMenu(menu);
            for (int i = 0; i < menu.size(); i++) {
                menu.getItem(i).setOnMenuItemClickListener(this);
            }
        }

        @Override
        public boolean onMenuItemClick(@NonNull MenuItem item) {
            if (item.getGroupId() == 1){
                switch (item.getItemId()){
                    case 0:
                        DataToClipboard.copyToClipboard(itemView.getContext(),
                                this.title.getText().toString());
                        break;
                    case 1:
                        DataToClipboard.copyToClipboard(itemView.getContext(),
                                replace(this.resuBean.getData().get(2).get(getAdapterPosition()).toString()));
                        break;
                    case 2:
                        DataToClipboard.copyToClipboard(itemView.getContext(),
                                replace(this.resuBean.getData().get(4).get(getAdapterPosition()).toString()));
                        break;
                    case 3:
                        DataToClipboard.copyToClipboard(itemView.getContext(),
                                replace(this.resuBean.getData().get(0).get(getAdapterPosition()).toString()));
                        break;
                    case 4:
                        DataToClipboard.copyToClipboard(itemView.getContext(),
                                replace(this.resuBean.getData().get(7).get(getAdapterPosition()).toString()));
                        break;
                    case 5:
                        DataToClipboard.copyToClipboard(itemView.getContext(),
                                replace(this.resuBean.getData().get(10).get(getAdapterPosition()).toString()));
                        break;
                    case 6:
                        DataToClipboard.copyToClipboard(itemView.getContext(),
                                "https://thwiki.cc" + replace(this.resuBean.getLink().get(getAdapterPosition())));
                        break;
                    case 7:
                        DataToClipboard.copyToClipboard(itemView.getContext(),
                                replace(this.resuBean.getData().get(11).get(getAdapterPosition()).toString()));
                        break;
                }
            }
            return true;
        }

        private void initCopySubMenu(ContextMenu menu){
            SubMenu subMenu = menu.addSubMenu(R.string.item_menu_copy);
            subMenu.setIcon(R.drawable.ic_content_copy);
            subMenu.add(1,0,0,this.title.getText());
            subMenu.add(1,1,0,this.circle.getText());
            subMenu.add(1,2,0,this.artists.getText());
            subMenu.add(1,3,0,this.album.getText());
            subMenu.add(1,4,0,this.ogmusic.getText());
            subMenu.add(1,5,0,this.ogwork.getText());
            subMenu.add(1,6,0,R.string.submenu_copy_album_link);
            subMenu.add(1,7,0,R.string.submenu_copy_cover_link);
            for (int i = 0; i < subMenu.size(); i++) {
                subMenu.getItem(i).setOnMenuItemClickListener(this);
            }
        }

    }

}
