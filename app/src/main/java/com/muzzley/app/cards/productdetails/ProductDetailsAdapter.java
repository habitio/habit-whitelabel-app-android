package com.muzzley.app.cards.productdetails;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.muzzley.App;
import com.muzzley.R;
import com.muzzley.model.productDetails.ProductDetails;
import com.muzzley.model.productDetails.Stores;
import com.muzzley.services.PreferencesRepository;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by bruno.marques on 17/03/2016.
 */
public class ProductDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(RecyclerView.ViewHolder viewHolder, Stores store);
    }

    public static int DISPLACEMENT_VALUE = 2;
    private ProductDetails detailsData;
    private Context context;
    private int emptyHeight;
    private OnProductClickListener mOnProductClickListener;
    @Inject protected PreferencesRepository preferencesRepository;


    public ProductDetailsAdapter(Context context, int emptyHeight) {
        this.context = context;
        this.emptyHeight = emptyHeight;
        App.appComponent.inject(this);
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        mOnProductClickListener = listener;
    }

    public void setProductDetail(ProductDetails productDetail) {
        detailsData = productDetail;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if(detailsData != null) {
            count = detailsData.getStores().size() + 3;
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0){
            //0 é um item invisivel com o tamanho da imagem
            return 0;
        } else if(position==1){
            //1 é o titulo e texto sobre o produto
            return 1;
        } else if(position == (getItemCount()-1)){
            //getItemCount()-1 é o ultimo item com specs do producto
            return 3;
        } else {
            //2 sao as lojas
            return 2;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new ProductDetailsItemEmptyHolder(context, LayoutInflater.from(context).inflate(R.layout.product_details_adapter_item_invisible, parent, false), emptyHeight);

            case 1:
                return new ProductDetailsTitleHolder(LayoutInflater.from(context).inflate(R.layout.product_details_adapter_item_title, parent, false));

            case 2:
                return new ProductDetailsItemHolder(LayoutInflater.from(context).inflate(R.layout.product_details_adapter_item_store, parent, false),
                        mOnProductClickListener);

            case 3:
                return new ProductDetailsDescriptionHolder(LayoutInflater.from(context).inflate(R.layout.product_details_adapter_item_description, parent, false));

            default:
                return new ProductDetailsItemEmptyHolder(context, LayoutInflater.from(context).inflate(R.layout.product_details_adapter_item_invisible, parent, false), 1);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        if(position == 0){
            final ProductDetailsItemEmptyHolder itemController = (ProductDetailsItemEmptyHolder) holder;

        } else if(position == 1){
            final ProductDetailsTitleHolder itemController = (ProductDetailsTitleHolder) holder;
            itemController.title.setText(detailsData.getTitle());
            itemController.description.setText(detailsData.getDescription());

        } else if(position == (getItemCount()-1)){
            final ProductDetailsDescriptionHolder itemDescription = (ProductDetailsDescriptionHolder) holder;
            itemDescription.description.setText(detailsData.getSpecs());

        } else {
            Stores store = detailsData.getStores().get(position - DISPLACEMENT_VALUE);
            final ProductDetailsItemHolder itemHolder = (ProductDetailsItemHolder) holder;

            String delivery = String.format(context.getResources().getString(R.string.product_details_delivery_text),
                    store.getDeliversIn().getTimeSpan().get(0),
                    store.getDeliversIn().getTimeSpan().get(1),
                    store.getDeliversIn().getUnit());

            if(store.getPhysical().getNearest() != null) {
//                String loc = String.format(context.getResources().getString(R.string.product_details_location_text),
//                        store.getPhysical().getNearest());

                boolean metric = preferencesRepository.getPreferences().isMetric();
                String loc = context.getResources().getQuantityString(
                        metric ? R.plurals.product_details_location_text_metric : R.plurals.product_details_location_text_imperial,
                        store.getPhysical().getLocations().size(),
                        store.getPhysical().getLocations().size(),
                        store.getPhysical().getNearest() * (metric ? 1 : 0.62137));

                itemHolder.location.setText(loc);
                itemHolder.location.setVisibility(View.VISIBLE);
            } else {
                itemHolder.location.setVisibility(View.INVISIBLE);
            }

            itemHolder.layoutAroudn.setSelected(store.getHighlighted());
            itemHolder.imgHighlight.setVisibility(store.getHighlighted() ? View.VISIBLE : View.INVISIBLE );
            Timber.e("" + detailsData.getStores().get(position - DISPLACEMENT_VALUE).getHighlighted());
            itemHolder.delivery.setText(delivery);
            itemHolder.price.setText(store.getPrice());

            if(store.getLogo()!=null) {
                itemHolder.storeLogo.setVisibility(View.VISIBLE);
                itemHolder.companyName.setVisibility(View.INVISIBLE);
                setImg(itemHolder.storeLogo, store.getLogo());
            } else {
                itemHolder.storeLogo.setVisibility(View.INVISIBLE);
                itemHolder.companyName.setVisibility(View.VISIBLE);
                itemHolder.companyName.setText("AT " + store.getName());
            }

            itemHolder.setLinkAddress(store);

            store.setIdPosition(position - DISPLACEMENT_VALUE);
            store.resetTimers();
            store.saveStartTime();
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);

        String posId = holder.itemView.getTag().toString();
        int pos = Integer.parseInt(posId);
        if(!posId.equals("0") && !posId.equals("1") && !posId.equals(""+ (getItemCount()-1))){
            detailsData.getStores().get(pos - DISPLACEMENT_VALUE).saveFinishTime();
            detailsData.getStores().get(pos - DISPLACEMENT_VALUE).printTimes();
            detailsData.getStores().get(pos - DISPLACEMENT_VALUE).resetTimers();
        }
    }

    public void resumeView(int firstVisibleItemPosition, int lastVisibleItemPosition){
        Timber.e("resumeView()");
        Timber.e("getItemCount(): " + getItemCount());
        for(int i = firstVisibleItemPosition; i<=lastVisibleItemPosition; i++){
            if(i!=0 && i != 1 && i != getItemCount()-1) {
                detailsData.getStores().get(i - DISPLACEMENT_VALUE).resetTimers();
                detailsData.getStores().get(i - DISPLACEMENT_VALUE).saveStartTime();
                Timber.e("idPosition: " + detailsData.getStores().get(i-DISPLACEMENT_VALUE).idPosition);
                Timber.e("getStartTime(): " + detailsData.getStores().get(i-DISPLACEMENT_VALUE).getStartTime());
                Timber.e("getFinishTime(): " + detailsData.getStores().get(i-DISPLACEMENT_VALUE).getFinishTime());
            }
        }
    }

    public void pauseView(){
        Timber.e("pauseView()");
        if(detailsData == null)
            return;

        for(Stores store : detailsData.getStores()){
            Timber.e("idPosition: " + store.idPosition);
            Timber.e("getStartTime(): " + store.getStartTime());
            Timber.e("getFinishTime(): " + store.getFinishTime());

            if(!store.getStartTime().isEmpty() && !store.getFinishTime().isEmpty()) {
                Timber.e("------------ JA SAIRAM DO ECRA ------------");
                store.printTimesFromPause();
                store.resetTimers();
            }

            if(!store.getStartTime().isEmpty() && store.getFinishTime().isEmpty()) {
                Timber.e("------------ VISIVEIS NO ECRA ------------");
                store.saveFinishTime();
                store.printTimesFromPause();
                store.resetTimers();
            }
        }
    }

    public String getItemIdAux(int index) {
        if(index!=0 && index != 1 && index != getItemCount()-1) {
            return "" + detailsData.getStores().get(index - DISPLACEMENT_VALUE).getId();
        } else {
            return null;
        }
    }

    public void setImg(ImageView img, String url){
        Picasso.get()
                .load(url)
                .into(img);
    }
}