package com.example.recoded.product;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recoded.product.Data.ProductContract;
import com.example.recoded.product.Data.ProductDbHelper;

public class ProductCursorAdapter extends CursorAdapter {
    private LayoutInflater mInflater;
    Context context;

    static private class Holder {
        TextView ProductNameTextView;
        TextView ProductPriceTextView;
        TextView ProductQuantityTextView;
        Button SaleButton;

        public Holder(View view) {
            ProductNameTextView = (TextView) view.findViewById(R.id.ProductNameItem);
            ProductPriceTextView = (TextView) view.findViewById(R.id.ProductPriceItem);
            ProductQuantityTextView = (TextView) view.findViewById(R.id.QuantityProductItem);
            SaleButton = view.findViewById(R.id.SaleButton);
        }
    }

    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.list_item, parent, false);
        Holder holder = new Holder(view);
        view.setTag(holder);
        return view;
    }


    /**
     * This method binds the Product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current Product can be set on the name TextView
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


        final Holder holder = (Holder) view.getTag();


        // Find the columns of Product attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int PriceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);

        // Read the Product attributes from the Cursor for the current Product
        String ProductName = cursor.getString(nameColumnIndex);
        String ProductPrice = cursor.getString(PriceColumnIndex);
        String ProductQuantity = cursor.getString(quantityColumnIndex);
        // If the Product breed is empty string or null, then use some default text
        // that says "Unknown breed", so the TextView isn't blank.

        holder.ProductNameTextView.setText(ProductName);


        holder.ProductPriceTextView.setText(ProductPrice);


        holder.ProductQuantityTextView.setText(ProductQuantity);

        if (TextUtils.isEmpty(ProductPrice)) {

            ProductPrice = context.getString(R.string.editor_activity_title_new_Product);

        }

        final int position = cursor.getPosition();

        holder.SaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor.moveToPosition(position);
                int currentQuantity = Integer.parseInt(holder.ProductQuantityTextView.getText().toString());
                if (currentQuantity <= 0) {
                    Toast.makeText(view.getContext(), R.string.Not_avaliable, Toast.LENGTH_SHORT).show();
                    return;
                }

                currentQuantity -= 1;
                holder.ProductQuantityTextView.setText(String.valueOf(currentQuantity));
                ProductDbHelper dbHelper = new ProductDbHelper(view.getContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, currentQuantity);

                long id = cursor.getLong(cursor.getColumnIndex(ProductContract.ProductEntry._ID));
                db.update(ProductContract.ProductEntry.TABLE_NAME, values, "_id=" + id, null);
                db.close();


            }
        });

        // Update the TextViews with the attributes for the current Product

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to update the position
                cursor.moveToPosition(position);

                Intent next = new Intent(context, AddProduct.class);
                long id = cursor.getLong(cursor.getColumnIndex(ProductContract.ProductEntry._ID));

                Uri currentProductUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                next.setData(currentProductUri);

                context.startActivity(next);


            }
        });
    }

}
