package com.ragentek.exercisespaper.view.widgets;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.ragentek.exercisespaper.R;

import java.util.ArrayList;
import java.util.List;


public class ListDeviceDialog extends Dialog {
    private static final String TAG = "ListDeviceDialog";

    private final Context mContext;

    private RecyclerView deviceRv;
    private Button scanBtn;
    private Button cancelBtn;
    private ProgressBar loadingBar;
    private TextView messageView;


    private RecyclerViewAdapter mRecyclerViewAdapter;
    private Builder dialogBuilder;


    private ListDeviceDialog(@NonNull Context context, Builder build) {
        super(context);
        mContext = context;
        dialogBuilder = build;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        initView();
    }

    private void initView() {
        View contentView = View.inflate(mContext, R.layout.dialog_listview, null);
        setContentView(contentView);
        loadingBar = contentView.findViewById(R.id.loading_progress);

        messageView = contentView.findViewById(R.id.message_tv);


        scanBtn = contentView.findViewById(R.id.btn_scanner);
        scanBtn.setText(dialogBuilder.positiveButtonText);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.clickListener.onClick(Builder.ID_POSITIVE_BUTTON);
            }
        });
        cancelBtn = contentView.findViewById(R.id.btn_cancel);
        cancelBtn.setText(dialogBuilder.negativeButtonText);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.clickListener.onClick(Builder.ID_NEGATIVE_BUTTON);
            }
        });
        deviceRv = contentView.findViewById(R.id.recycle_devices);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        deviceRv.setLayoutManager(layoutManager);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        mRecyclerViewAdapter = new RecyclerViewAdapter();
        deviceRv.setAdapter(mRecyclerViewAdapter);
        deviceRv.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        deviceRv.setItemAnimator(new DefaultItemAnimator());

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = mContext.getResources().getDisplayMetrics();
        if (d.widthPixels > d.heightPixels) {
            lp.width = (int) (d.widthPixels * 0.6);
            lp.height = (int) (d.heightPixels * 0.6);
        } else {
            lp.width = (int) (d.widthPixels * 0.9);
            lp.height = (int) (d.heightPixels * 0.4);
        }

        dialogWindow.setAttributes(lp);
    }

    public void refresh(boolean clearData) {
        if (clearData) {
            mRecyclerViewAdapter.deviceList.clear();
        }
        messageView.setVisibility(View.GONE);
        deviceRv.setVisibility(View.GONE);
        loadingBar.setVisibility(View.VISIBLE);
    }

    public void stopRefresh() {
        loadingBar.setVisibility(View.GONE);
        messageView.setVisibility(View.GONE);
        deviceRv.setVisibility(View.VISIBLE);

    }

    public void showMessage(String msg) {
        loadingBar.setVisibility(View.GONE);
        deviceRv.setVisibility(View.GONE);
        messageView.setVisibility(View.VISIBLE);
        messageView.setText(msg);
    }

    public void addData(String name) {
        mRecyclerViewAdapter.addData(name);

    }

    public void removeData(String name) {
        mRecyclerViewAdapter.removeData(name);

    }

    public void setSellected(String name) {
        mRecyclerViewAdapter.sellect(name);
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
        private List<String> deviceList;
        private int sellectIndex = -1;

        public RecyclerViewAdapter() {
            deviceList = new ArrayList<>();
        }

        public void addData(String name) {
            deviceList.add(name);
            notifyDataSetChanged();
        }


        public void removeData(String name) {
            deviceList.remove(name);
            notifyDataSetChanged();

        }

        public void sellect(String name) {
            Log.d(TAG, "sellect:" + name);
            if (name == null) {
                sellectIndex = -1;
                notifyDataSetChanged();
                return;
            }
            for (int i = 0; i < deviceList.size(); i++) {
                if (name.equals(deviceList.get(i))) {
                    sellectIndex = i;
                }
            }
            Log.d(TAG, "sellectIndex:" + sellectIndex);

            notifyItemChanged(sellectIndex);
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
            RecyclerViewHolder holder = new RecyclerViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(final RecyclerViewHolder holder, final int position) {
            Log.d(TAG, "position:" + position);
            Log.d(TAG, "sellectIndex:" + sellectIndex);

            printLog(deviceList.get(position));
            holder.deviceItem.setText(deviceList.get(position));
            if (sellectIndex == position) {
                holder.deviceItem.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            }
            holder.deviceItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogBuilder.clickListener.onClick(position);
                }
            });

        }

        @Override
        public int getItemCount() {
            return deviceList.size();
        }
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView deviceItem;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            deviceItem = itemView.findViewById(R.id.tv_device_item);
        }


    }

    public static class Builder {
        private String positiveButtonText;
        private String negativeButtonText;
        private OnClickListener clickListener;
        private ListDeviceDialog dialog;
        public static final int ID_NEGATIVE_BUTTON = -1;
        public static final int ID_POSITIVE_BUTTON = -2;

        public Builder() {
        }

        public Builder setPositiveButton(String positiveButtonText) {
            this.positiveButtonText = positiveButtonText;
            return this;
        }

        public Builder setNegativeButton(String negativeButtonText) {
            this.negativeButtonText = negativeButtonText;
            return this;
        }

        public Builder setItemClickListener(OnClickListener listener) {
            this.clickListener = listener;
            return this;
        }

        public ListDeviceDialog create(Context context) {
            dialog = new ListDeviceDialog(context, this);
            return dialog;
        }
    }

    public interface OnClickListener {

        void onClick(int position);
    }

    private void printLog(String message) {
        Log.d(TAG, message);
    }
}
