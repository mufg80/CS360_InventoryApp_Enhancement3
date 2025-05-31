package com.zybooks.inventoryproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
// Adapter class extending recyclerview adapter.
public class InventoryItemAdapter extends RecyclerView.Adapter<InventoryItemAdapter.ViewHolder> {
    // Interface holding the three click listeners needed by each row.
    public interface OnButtonClickListener {
        void onDecButtonClick(int position);
        void onIncButtonClick(int position);
        void onDelButtonClick(int position);
    }
    // Holding a property of the interface named myListener.
    private OnButtonClickListener myListener;
    // Set function for connecting interface object to this field.
    public void setOnButtonClickListener(OnButtonClickListener listener){
        myListener = listener;
    }
    // List of items for recyclerview.
    private final List<InventoryItem> items;

    // Provide a reference to the views for each data item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Properties for the various elements of the gridrow.
        public TextView titleTextView;
        public TextView descTextView;
        public TextView qtyTextView;
        public Button decButton;
        public Button incButton;
        public Button delButton;

        // Constructor ties the properties to the actual views in the row.
        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title);
            descTextView = itemView.findViewById(R.id.description);
            qtyTextView = itemView.findViewById(R.id.quantity);
            decButton = itemView.findViewById(R.id.decrement_button);
            incButton = itemView.findViewById(R.id.increment_button);
            delButton = itemView.findViewById(R.id.delete_button);
        }
    }

    // Constructor takes list to be displayed.
    public InventoryItemAdapter(List<InventoryItem> items){
        this.items = items;
    }


    // Creates view and returns it.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_row, parent, false);
        return new ViewHolder(v);
    }

    // Binds view to data.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get item at list position.
        InventoryItem item = items.get(position);
        // Set views to appropriate information.
        holder.titleTextView.setText(item.getTitle());
        holder.descTextView.setText(item.getDescription());
        holder.qtyTextView.setText(item.getQuantity());

        // Tie delete button to interface listener.
        holder.delButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(myListener != null){
                    myListener.onDelButtonClick(holder.getAbsoluteAdapterPosition());
                }
            }
        });
        // Tie decrement button to interface listener.
        holder.decButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(myListener != null){
                    myListener.onDecButtonClick(holder.getAbsoluteAdapterPosition());
                }
            }
        });

        // Tie increment button to interface listener.
        holder.incButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(myListener != null){
                    myListener.onIncButtonClick(holder.getAbsoluteAdapterPosition());
                }
            }
        });
    }

    // Supply count of list.
    @Override
    public int getItemCount() {
        return items.size();
    }
}
