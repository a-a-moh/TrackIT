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

public class RouteAdapter extends ArrayAdapter<DriverRoute> {
    private Context mContext;
    int mResource;

    public RouteAdapter(@NonNull Context context, int resource, @NonNull List<DriverRoute> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String packageID = getItem(position).getPackageID();
        String customerName = getItem(position).getCustomerName();
        String customerAddress = getItem(position).getCustomerAddress();

        DriverRoute d = new DriverRoute(packageID, customerName, customerAddress);

        LayoutInflater inflator = LayoutInflater.from(mContext);
        convertView = inflator.inflate(mResource, parent, false);

        TextView tvParcel = (TextView) convertView.findViewById(R.id.tvPackageID);
        TextView tvCustomerName = (TextView) convertView.findViewById(R.id.tvCustomerName);
        TextView tvCustomerAddress = (TextView) convertView.findViewById(R.id.tvCustomerAddress);

        tvParcel.setText(packageID);
        tvCustomerName.setText(customerName);
        tvCustomerAddress.setText(customerAddress);

        return convertView;
    }

}
