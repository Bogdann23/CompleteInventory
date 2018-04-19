package com.example.android.completeinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.completeinventory.data.InventoryContract.InventoryEntry;

/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class InventoryCursorAdapter extends CursorAdapter {
    private ListView lv;


    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c, ListView lv) {
        super(context, c, 0 /* flags */);
        this.lv = lv;
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView productNameTextView = (TextView) view.findViewById(R.id.product);
        TextView supplierNameTextView = (TextView) view.findViewById(R.id.supplier);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView btn_sell = (TextView) view.findViewById(R.id.btn_sell);

        // Find the columns of product attributes that we're interested in
        int productNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);

        // Read the product attributes from the Cursor for the current product
        if (productNameColumnIndex != -1) {
            String productName = context.getString(R.string.product) + ": " + cursor.getString(productNameColumnIndex);
            productNameTextView.setText(productName);
        }

        if (supplierNameColumnIndex != -1) {
            String supplierName = context.getString(R.string.supplier) + ": " + cursor.getString(supplierNameColumnIndex);
            supplierNameTextView.setText(supplierName);
        }

        if (priceColumnIndex != -1) {
            double priceValue = cursor.getDouble(priceColumnIndex);
            String priceUnit = String.valueOf(priceValue) + "€";
            priceTextView.setText(priceUnit);
        }

        int quantityValue = 0;
        if (quantityColumnIndex != -1) {
            quantityValue = cursor.getInt(quantityColumnIndex);
            String productQuantity = context.getString(R.string.quantity) + ": " + String.valueOf(quantityValue);
            quantityTextView.setText(productQuantity);
        }

        final int qv = quantityValue;
        btn_sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = decreaseQuantity(qv);

                //get Uri with appended ID for current product
                Uri mCurrentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, getItemId(lv.getPositionForView(view)));

                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_QUANTITY, quantity);
                context.getContentResolver().update(mCurrentProductUri, values, null, null);

            }
        });

    }

    private String decreaseQuantity(int quantity) {
        if (quantity > 0) {
            quantity--;
        }

        return String.valueOf(quantity);
    }
}