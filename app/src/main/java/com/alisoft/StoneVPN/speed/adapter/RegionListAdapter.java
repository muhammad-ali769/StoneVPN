package com.alisoft.StoneVPN.speed.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.anchorfree.partner.api.data.Country;
import com.alisoft.StoneVPN.speed.MainApplication;
import com.alisoft.StoneVPN.speed.R;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegionListAdapter extends RecyclerView.Adapter<RegionListAdapter.ViewHolder> {

    private List<Country> regions;
    private RegionListAdapterInterface listAdapterInterface;
    private Context context;

    public RegionListAdapter(Context context, RegionListAdapterInterface listAdapterInterface) {
        this.listAdapterInterface = listAdapterInterface;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.region_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Country country = regions.get(position);
        Locale locale = new Locale("", regions.get(position).getCountry());
        if (country.getCountry() != null && !country.getCountry().equals("")) {
            holder.regionTitle.setText(locale.getDisplayCountry());
            String str = regions.get(position).getCountry();
            holder.regionImage.setImageResource(MainApplication.getStaticContext().getResources().getIdentifier("drawable/" + str, null, MainApplication.getStaticContext().getPackageName()));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listAdapterInterface.onCountrySelected(regions.get(holder.getAdapterPosition()));
                    Prefs.putString("sname", regions.get(position).getCountry());
                    Prefs.putString("simage", regions.get(position).getCountry());
                }
            });
        } else {
            holder.regionTitle.setText("St Location ");
            holder.regionImage.setImageResource(R.drawable.select_flag_image);
            holder.setClicks();
        }
    }

    @Override
    public int getItemCount() {
        return regions != null ? regions.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.region_title)
        TextView regionTitle;

        @BindView(R.id.region_image)
        ImageView regionImage;

        @BindView(R.id.parent)
        CardView cardView;

        @BindView(R.id.region_signal_image)
        ImageView signalImage;


        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        public void setClicks() {
            regionTitle.setClickable(false);
            regionImage.setClickable(false);
            cardView.setClickable(false);
            signalImage.setClickable(false);
            regionTitle.setFocusable(false);
            regionImage.setFocusable(false);
            cardView.setFocusable(false);
            signalImage.setFocusable(false);
        }
    }

    public void setRegions(List<Country> list) {
        regions = new ArrayList<>();
        regions.add(new Country(""));
        regions.addAll(list);
        notifyDataSetChanged();
    }

    public interface RegionListAdapterInterface {
        void onCountrySelected(Country item);
    }
}
