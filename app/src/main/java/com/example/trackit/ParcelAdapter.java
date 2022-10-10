package com.example.trackit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ParcelAdapter extends ArrayAdapter<DeliveredPackage> {
    private Context mContext;
    int mResource;

    public ParcelAdapter(@NonNull Context context, int resource, @NonNull List<DeliveredPackage> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String packageID = getItem(position).getPackageID();
        String date = getItem(position).getDate();
        String driver = getItem(position).getDriver();

        DeliveredPackage p = new DeliveredPackage(packageID, date, driver);

        LayoutInflater inflator = LayoutInflater.from(mContext);
        convertView = inflator.inflate(mResource, parent, false);

        TextView tvParcel = (TextView) convertView.findViewById(R.id.tvParcelID);
        TextView tvDate = (TextView) convertView.findViewById(R.id.tvDeliveryDate);
        TextView tvDriver = (TextView) convertView.findViewById(R.id.tvDriver);

        tvParcel.setText(packageID);
        tvDate.setText(date);
        tvDriver.setText(driver);

        return convertView;
    }

}
