package com.muzzley.app.workers;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.muzzley.R;
import com.muzzley.util.Utils;
import com.muzzley.util.picasso.CircleBorderTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by bruno.marques on 16/11/2015.
 */
public class DevicePickerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface SmoothScrollInterface{
        public void smoothScroll(int pos);
    }

    public static final int PARENT = 0;
    public static final int CHILD = 1;

    private Context context;
    private List<Item> items;
    private SelectionType selectionType;
    private PublishSubject<List<String>> observer;
    private List<String> currentSelectedChannelId;
    private int selectedPreviousSize = 0, selectedPosition = 0;
    private String textToSearch;
    private SmoothScrollInterface scrollInterface;
//    private boolean multipleSelection;

    public enum SelectionType { SINGLE, MULTIPLE, GROUP }

//    public DevicePickerAdapter(Context context, List<Item> items, boolean multipleSelection) {
//        this.context = context;
//        this.items = items;
//        this.multipleSelection = multipleSelection;
//        currentSelectedChannelId = new ArrayList<>();
//    }
    public DevicePickerAdapter(Context context, List<Item> items, SelectionType selectionType, SmoothScrollInterface scrollInterface) {
        this.context = context;
        this.items = items;
        this.selectionType = selectionType;
//        this.multipleSelection = multipleSelection;
        currentSelectedChannelId = new ArrayList<>();
        this.observer = PublishSubject.create();
        this.scrollInterface = scrollInterface;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case PARENT:
                return new ListParentViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_parent_device_picker, parent, false));
            case CHILD:
                return new ListChildViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_children_device_picker, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder,final int position) {

        switch (items.get(position).type) {
            case PARENT:
                final ListParentViewHolder itemController = (ListParentViewHolder) holder;

                ViewGroup.LayoutParams lp = itemController.itemView.getLayoutParams();

                if(position == 0) {
                    itemController.itemView.setPadding(0, (int)Utils.dpToPx(context, 15), 0, 0);
                }

                if(items.get(position).hideParent){
                    lp.height =  0;
                    itemController.itemView.setLayoutParams(lp);

                } else {
                    lp.height =  ViewGroup.LayoutParams.WRAP_CONTENT;
                    itemController.itemView.setLayoutParams(lp);

                    itemController.header_title.setText(items.get(position).text);

//                    if (items.get(position).invisibleChildren == null) {
//                        //itemController.btn_expand_toggle.setImageResource(R.drawable.circle_minus);
//                        itemController.itemView.setSelected(false);
//                    } else {
//                        //itemController.btn_expand_toggle.setImageResource(R.drawable.circle_plus);
//                        itemController.itemView.setSelected(true);
//                    }

                    itemController.itemView.setSelected(items.get(position).invisibleChildren != null);

//                    final int pos = position;
                    /*
                    itemController.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (items.get(pos).invisibleChildren == null) {

                                items.get(pos).invisibleChildren = new ArrayList<Item>();
                                int count = 0;

                                //vai sempre apagando o item asseguir ao pai enquanto apanhar um filho
                                while (items.size() > pos + 1 && items.get(pos + 1).type == CHILD) {
                                    items.get(pos).invisibleChildren.add(items.remove(pos + 1));
                                    count++;
                                }

                                //TODO problemas com este remove, para já fica o notify data set changed
                                //notifyItemRangeRemoved(pos + 1, count);

                                //itemController.btn_expand_toggle.setImageResource(R.drawable.circle_plus);
                                itemController.itemView.setSelected(true);

                            } else {

                                int index = pos + 1;
                                for (Item i : items.get(pos).invisibleChildren) {
                                    items.add(index, i);
                                    index++;
                                }
                                //notifyItemRangeInserted(pos + 1, index - pos - 1);
                                items.get(pos).invisibleChildren = null;

                                //itemController.btn_expand_toggle.setImageResource(R.drawable.circle_minus);
                                itemController.itemView.setSelected(false);
                            }
                            notifyDataSetChanged();

                        }
                    });
                    */
                }
                break;

            case CHILD:
                final ListChildViewHolder childItemController = (ListChildViewHolder) holder;
                ViewGroup.LayoutParams lpChild = childItemController.itemView.getLayoutParams();

                if(items.get(position).hideChildFromSearch){
                    lpChild.height =  0;
                    childItemController.itemView.setLayoutParams(lpChild);
                } else {
                    lpChild.height = /*(int) Utils.dpToPx(context, */(int) context.getResources().getDimension(R.dimen.device_picker_children_height);//);
                    childItemController.itemView.setLayoutParams(lpChild);
                    childItemController.child_title.setText(items.get(position).text);
                    setImageToChildren(items.get(position), childItemController.child_img);

//                    if(currentSelectedChannelId.size() > 0){
//                        if(currentSelectedChannelId.contains(items.get(position).id)){
//                            childItemController.child_selector.setVisibility(View.VISIBLE);
//                        } else {
//                            childItemController.child_selector.setVisibility(View.INVISIBLE);
//                        }
//                    } else {
//                        childItemController.child_selector.setVisibility(View.INVISIBLE);
//                    }

                    childItemController.child_selector.setVisibility(
                            currentSelectedChannelId.size() > 0 && currentSelectedChannelId.contains(items.get(position).id) ?
                                    View.VISIBLE : View.INVISIBLE);

//                    final int pos2 = position;
                    childItemController.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switch (selectionType) {
                                case SINGLE:
                                    if (!currentSelectedChannelId.remove(items.get(position).id)) {
                                        currentSelectedChannelId.clear();
                                        currentSelectedChannelId.add(items.get(position).id);
                                    }
                                    break;
                                case MULTIPLE:
                                    if (!currentSelectedChannelId.remove(items.get(position).id)) {
                                        currentSelectedChannelId.add(items.get(position).id);
                                    }
                                    break;
                                case GROUP:
                                    String parent = items.get(position).parent;
                                    if (!currentSelectedChannelId.remove(items.get(position).id)) {
                                        currentSelectedChannelId.add(items.get(position).id);

                                        if (currentSelectedChannelId.size() == 1) {
                                            selectedPosition = position;
                                            List<String> currentSelectedClasses = getClassesFromChannel(currentSelectedChannelId.get(0));

                                            for (Item item : items) {
                                                if (item.type == PARENT) {
                                                    item.hideParent = !parent.equals(item.id);
                                                } else { //CHILD
                                                    //item.hideChildFromSearch = !parent.equals(item.parent);
                                                    if(parent.equals(item.parent)){
                                                        if(currentSelectedClasses != null && containEqualClasses(currentSelectedClasses, item.classes)){
                                                            item.hideChildFromSearch=false;
                                                        } else {
                                                            item.hideChildFromSearch=true;
                                                        }
                                                    } else {
                                                        item.hideChildFromSearch=true;
                                                    }

                                                }
                                            }
                                        }
                                    } else {
                                        //FIXME: take previous search into account
                                        if (currentSelectedChannelId.size() == 0) {
                                            for (Item item : items) {
                                                if (item.type == PARENT) {
                                                    item.hideParent = false;
                                                } else { //CHILD
                                                    item.hideChildFromSearch = false;
                                                }
                                            }
                                        }
                                        checkIfExist();
                                    }
                                    break;
                            }

                            observer.onNext(getCurrentSelectedChannel());
                            notifyDataSetChanged();

                            if(currentSelectedChannelId.size() == 0 && selectedPreviousSize == 1){
                                scrollInterface.smoothScroll(selectedPosition);
                                selectedPreviousSize=0;
                            }

                            //last thing to do
                            selectedPreviousSize = currentSelectedChannelId.size();
                        }
                    });

                    //if(items.get(position + 1) == null) {
//                    if(items.size() == position + 1){
//                        childItemController.child_bottom_divider.setVisibility(View.INVISIBLE);
//                    } else {
//                        if (items.get(position + 1).type == PARENT) {
//                            childItemController.child_bottom_divider.setVisibility(View.INVISIBLE);
//                        } else {
//                            childItemController.child_bottom_divider.setVisibility(View.VISIBLE);
//                        }
//                    }
                    childItemController.child_bottom_divider.setVisibility(
                            items.size() == position + 1 || items.get(position + 1).type == PARENT ?
                            View.INVISIBLE : View.VISIBLE);

                }
                break;
        }
    }




    /**
     * get the classes from the first selected device
     *
     * @param channelId
     * @return
     */
    private List<String> getClassesFromChannel(String channelId){
        for (Item item : items) {
            if(item.id.equals(channelId)){
                return item.classes;
            }
        }
        return null;
    }

    /**
     * check if there is at least one equal class
     *
     * @param currentSelectedClasses
     * @param classes
     * @return
     */
    private boolean containEqualClasses(List<String> currentSelectedClasses, List<String> classes){
        for(String classesSelected : currentSelectedClasses){
            if(classes.contains(classesSelected)){
                return true;
            }
        }
        return false;
    }

    private void setImageToChildren(Item item, ImageView child_img) {
        //if(!item.parent.equals(Constants.DEVICE_PICKER_GENERIC_ID_LOCATION_TIME)) {
            if(item.imageUrl != null && !item.imageUrl.isEmpty()) {
                bindImage(item.imageUrl, child_img);
            } else {
                if(item.imageUrlBackup != null && !item.imageUrlBackup.isEmpty()) {
                    bindImage(item.imageUrlBackup, child_img);
                }
            }
            /*
        } else {
            if(item.id.equals(Constants.DEVICE_PICKER_GENERIC_ID_TIME)){
                child_img.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_time_agent));
            } else {
                child_img.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_location_agent));
            }
        }*/
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    //TODO ver o caso em que nao veem imagens, como nao vem imagem o picasso nao faz nada e a recycler deixa ficar a imagem que já lá estava
    private void bindImage(String imgUrl, ImageView iv){
        Picasso.get()
                .load(imgUrl)
                .fit()
                .transform(new CircleBorderTransform(context))
                .into(iv);
    }

    public List<String> getCurrentSelectedChannel(){
        return currentSelectedChannelId;
    }

    public Observable<List<String>> getCurrentSelectedChannelObservable(){
        return observer;
    }


    public void setFilter(String textToSearch) {
        this.textToSearch = textToSearch;
        checkIfExist();
        notifyDataSetChanged();
    }

    private void checkIfExist(){
        if (textToSearch != null) {
            if (items != null && !items.isEmpty()) {
                for (DevicePickerAdapter.Item item : items) {
                    if (item.type == DevicePickerAdapter.CHILD) {

                        item.hideChildFromSearch = !item.text.toLowerCase().contains(textToSearch.toLowerCase());

                        if (textToSearch.isEmpty()) {
                            item.hideChildFromSearch = false;
                        }
                    }

                    if (item.invisibleChildren != null) {
                        for (DevicePickerAdapter.Item itemChild : item.invisibleChildren) {
                            itemChild.hideChildFromSearch = !itemChild.text.toLowerCase().contains(textToSearch.toLowerCase());

                            if (textToSearch.isEmpty()) {
                                itemChild.hideChildFromSearch = false;
                            }
                        }
                    }
                }

                hideParentsIfNeeded();
            }
        }
    }

    private void hideParentsIfNeeded() {
        //trocar para hash talvez, reduzir os for
        for (DevicePickerAdapter.Item item : items) {
            if (item.type == DevicePickerAdapter.PARENT) {
                String parentId = item.id;

                boolean haveChildrenToShow = false;

                for (DevicePickerAdapter.Item itemPesquisa : items) {
                    if (itemPesquisa.type == DevicePickerAdapter.CHILD
                            && itemPesquisa.parent.equals(parentId)
                            && !itemPesquisa.hideChildFromSearch) {
                        haveChildrenToShow = true;
                    }
                }

                if(item.invisibleChildren != null && item.invisibleChildren.size()>0) {
                    for (DevicePickerAdapter.Item itemPesquisa : item.invisibleChildren) {
                        if (itemPesquisa.type == DevicePickerAdapter.CHILD
                                && itemPesquisa.parent.equals(parentId)
                                && !itemPesquisa.hideChildFromSearch) {
                            haveChildrenToShow = true;
                        }
                    }
                }

                item.hideParent = !haveChildrenToShow;
            }
        }

    }


    private static class ListParentViewHolder extends RecyclerView.ViewHolder {
        public TextView header_title;
        //public ImageView btn_expand_toggle;

        public ListParentViewHolder(View itemView) {
            super(itemView);
            header_title = (TextView) itemView.findViewById(R.id.header_title);
            //btn_expand_toggle = (ImageView) itemView.findViewById(R.id.btn_expand_toggle);
        }
    }

    private static class ListChildViewHolder extends RecyclerView.ViewHolder {
        public TextView child_title;
        public ImageView child_img, child_selector;
        public View child_bottom_divider;

        public ListChildViewHolder(View itemView) {
            super(itemView);
            child_title = (TextView) itemView.findViewById(R.id.children_title);
            child_img = (ImageView) itemView.findViewById(R.id.children_image);
            child_selector = (ImageView) itemView.findViewById(R.id.children_selector);
            child_bottom_divider = itemView.findViewById(R.id.children_shadow_line);

        }
    }


    public static class Item {
        public int type;
        public String id;
        public String text;
        public String parent;
        public String imageUrl;
        public String imageUrlBackup;
        public boolean hideParent = false;
        public boolean hideChildFromSearch = false;
        public boolean isChildrenSelected = false;
        public List<Item> invisibleChildren;
        public boolean isGroupable;
        public List<String> classes;

        public Item() {
        }

        /**
         * initialization for parent item
         *
         * @param type
         * @param text
         * @param id
         */
        public Item(int type, String text, String id) {
            this.id = id;
            this.type = type;
            this.text = text;
        }

        /**
         * initialization for children item
         *
         * @param type
         * @param text
         * @param parent
         * @param id
         * @param imageUrl
         */
        public Item(int type, String text, String parent, String id, String imageUrl, String imageUrlBackup) {
            this.id = id;
            this.type = type;
            this.text = text;
            this.parent = parent;
            this.imageUrl = imageUrl;
            this.imageUrlBackup = imageUrlBackup;
        }

        /**
         * Initialization for groups
         *
         * @param type
         * @param text
         * @param parent
         * @param id
         * @param imageUrl
         * @param imageUrlBackup
         * @param isGroupable
         * @param classes
         */
        public Item(int type, String text, String parent, String id, String imageUrl, String imageUrlBackup, boolean isGroupable, List<String> classes) {
            this.id = id;
            this.type = type;
            this.text = text;
            this.parent = parent;
            this.imageUrl = imageUrl;
            this.imageUrlBackup = imageUrlBackup;
            this.isGroupable = isGroupable;
            this.classes = classes;
        }
    }

}
