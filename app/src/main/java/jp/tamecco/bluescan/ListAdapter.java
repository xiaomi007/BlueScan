package jp.tamecco.bluescan;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaomi on 15/08/24.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder> {

    Context context;
    List<IBeaconModel> iBeaconModels;

    public ListAdapter(@NonNull Context context, @Nullable List<IBeaconModel> iBeaconModels) {
        this.context = context;
        if (iBeaconModels == null) {
            this.iBeaconModels = new ArrayList<>();
        } else {
            this.iBeaconModels = iBeaconModels;
        }
    }

    public void setiBeaconModels(List<IBeaconModel> iBeaconModels) {
        this.iBeaconModels.clear();
        this.iBeaconModels.addAll(iBeaconModels);
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_beacon, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
        if (iBeaconModels != null && iBeaconModels.size() > 0) {
            IBeaconModel ibeacon = iBeaconModels.get(position);
            myViewHolder.kontaktId.setText(ibeacon.kontaktId);
            myViewHolder.modelName.setText(ibeacon.modelName);
            myViewHolder.macAddress.setText(ibeacon.macAddress);
            myViewHolder.uuid.setText(ibeacon.uuid);
            myViewHolder.rssi.setText("rssi:" + ibeacon.rssi);
            myViewHolder.major.setText("Major:" + ibeacon.major);
            myViewHolder.minor.setText("Minor:" + ibeacon.minor);
        }
    }

    @Override
    public int getItemCount() {
        if (iBeaconModels != null && iBeaconModels.size() > 0) {
            return iBeaconModels.size();
        } else {
            return 0;
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView modelName;
        TextView macAddress;
        TextView rssi;
        TextView uuid;
        TextView major;
        TextView minor;
        TextView kontaktId;

        public MyViewHolder(View itemView) {
            super(itemView);
            modelName = (TextView) itemView.findViewById(R.id.model_name);
            macAddress = (TextView) itemView.findViewById(R.id.mac_address);
            rssi = (TextView) itemView.findViewById(R.id.rssi);
            uuid = (TextView) itemView.findViewById(R.id.uuid);
            major = (TextView) itemView.findViewById(R.id.major);
            minor = (TextView) itemView.findViewById(R.id.minor);
            kontaktId = (TextView) itemView.findViewById(R.id.kontakt_id);
        }
    }
}
