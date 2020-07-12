package gamal.myappnew.shipperside.Moduel;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import gamal.myappnew.shipperside.R;
import info.hoang8f.widget.FButton;

public class ViewHolderRequest extends RecyclerView.ViewHolder {
    public ImageView imageprofile,count;
    public TextView phone,address,status,total,username,comment,date;
    public   FButton btnshipping;
    public ViewHolderRequest(@NonNull View itemView) {
        super(itemView);
        comment=itemView.findViewById(R.id.comment);
        date=itemView.findViewById(R.id.date_order);
        phone=itemView.findViewById(R.id.phone);
        address=itemView.findViewById(R.id.address);
        status=itemView.findViewById(R.id.status);
        total=itemView.findViewById(R.id.total);
        username=itemView.findViewById(R.id.user);
        count=itemView.findViewById(R.id.count_order);
       btnshipping=itemView.findViewById(R.id.btnShipping);
        imageprofile=itemView.findViewById(R.id.imageprofile);

    }
}