package adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;
import java.util.concurrent.ExecutionException;

import Likol.ImageMessageDecoder;
import bean.Msg;
public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {
    private List<Msg> mMsgList;

    static class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        ImageView leftImg;
        TextView rightMsg;
        ImageView rightImg;
        TextView timeView_l;
        TextView timeView_r;
        TextView name;
        LinearLayout event_layout;
        TextView event_view;


        public ViewHolder(View itemView) {
            super(itemView);
            leftLayout = (LinearLayout) itemView.findViewById(R.id.left_layout);
            rightLayout = (LinearLayout) itemView.findViewById(R.id.right_layout);
            leftMsg = (TextView) itemView.findViewById(R.id.left_msg);
            rightMsg = (TextView) itemView.findViewById(R.id.right_msg);
            timeView_l = (TextView) itemView.findViewById(R.id.time_view_left);
            timeView_r = (TextView) itemView.findViewById(R.id.time_view_right);
            name = (TextView) itemView.findViewById(R.id.name);
            event_layout = itemView.findViewById(R.id.event_layout);
            event_view = itemView.findViewById(R.id.event);
            leftImg = itemView.findViewById(R.id.left_img);
            rightImg = itemView.findViewById(R.id.right_img);
        }
    }

    public MsgAdapter(List<Msg> mMsgList) {
        this.mMsgList = mMsgList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item_layout,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Msg msg = mMsgList.get(position);

        String data = msg.getContent();
        Bitmap bm = null;
        if (data.startsWith("/event:image "))
        {
            data = data.replace("/event:image ", "");
            try {
                bm = new ImageMessageDecoder().execute(data).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (msg.getType() == Msg.TYPE_RECEIVED){
            holder.event_layout.setVisibility(View.GONE);
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);

            if (bm != null)
            {
                holder.leftImg.setImageBitmap(bm);
                holder.leftImg.setVisibility(View.VISIBLE);
                holder.leftMsg.setVisibility(View.GONE);
            }
            else
            {
                holder.leftMsg.setText(data);
            }

            holder.timeView_l.setText(msg.getMsgTime());
            holder.name.setText(msg.getName());
        }else if (msg.getType() == Msg.TYPE_SENT){
            holder.event_layout.setVisibility(View.GONE);
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.leftLayout.setVisibility(View.GONE);
            if (bm != null)
            {
                holder.rightImg.setImageBitmap(bm);
                holder.rightImg.setVisibility(View.VISIBLE);
                holder.rightMsg.setVisibility(View.GONE);
            }
            else
            {
                holder.rightMsg.setText(data);
            }
            holder.timeView_r.setText(msg.getMsgTime());
        }
        // event view
        else {
            holder.event_layout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftLayout.setVisibility(View.GONE);
            holder.event_view.setText(msg.getContent());
        }

    }

    @Override
    public int getItemCount() {
        return mMsgList.size();
    }

}
