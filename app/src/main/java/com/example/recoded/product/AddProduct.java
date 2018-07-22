package com.example.recoded.product;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.recoded.product.Data.ProductContract;

public class AddProduct extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Identifier for the Product data loader
     */
    private static final int EXISTING_Product_LOADER = 0;
    FloatingActionButton CallSupplier;
    Button IncreaseQuantity;
    Button DecreaseQuantity;
    /**
     * Content URI for the existing Product (null if it's a new Product)
     */
    private Uri mCurrentProductUri;

    /**
     * EditText field to enter the Product's name
     */
    private EditText mProductNameEditText;

    /**
     * EditText field to enter the Product's breed
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the Product's weight
     */
    private EditText mQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneNumberEditText;

    /**
     * Boolean flag that keeps track of whether the Product has been edited (true) or not (false)
     */
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new Product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a Product content URI, then we know that we are
        // creating a new Product.
        if (mCurrentProductUri == null) {
            // This is a new Product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.editor_activity_title_new_Product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a Product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            CallSupplier = findViewById(R.id.CallSupplier);
            CallSupplier.setVisibility(View.VISIBLE);
            // Otherwise this is an existing Product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.editor_activity_title_edit_Product));

            // Initialize a loader to read the Product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_Product_LOADER, null, this);

            CallSupplier.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String phone = mSupplierPhoneNumberEditText.getText().toString();
                    if (!phone.isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                        startActivity(intent);
                    } else {
                        Toast.makeText(AddProduct.this, getString(R.string.NoSupplierPhone), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Find all relevant views that we will need to read user input from
        mProductNameEditText = (EditText) findViewById(R.id.ProductNameEditText);


        mPriceEditText = (EditText) findViewById(R.id.ProductPriceEditText);
        mPriceEditText.setFilters(new InputFilter[]{new MinMaxFilter("0", "999")});


        mQuantityEditText = (EditText) findViewById(R.id.ProductQuantityEditText);
        mQuantityEditText.setFilters(new InputFilter[]{new MinMaxFilter("0", "999")});

        mSupplierNameEditText = (EditText) findViewById(R.id.SupplierNameEditText);

        mSupplierPhoneNumberEditText = (EditText) findViewById(R.id.SupplierPhoneNumberEditText);

        IncreaseQuantity = (Button) findViewById(R.id.IncreaseQuantity);
        DecreaseQuantity = (Button) findViewById(R.id.DecreaseQuantity);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mProductNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneNumberEditText.setOnTouchListener(mTouchListener);

        IncreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentQuantity;
                if (mQuantityEditText.getText().toString().isEmpty()) {
                    currentQuantity = 0;
                } else currentQuantity = Integer.parseInt(mQuantityEditText.getText().toString());
                if (currentQuantity != 999) {

                    currentQuantity += 1;
                    mQuantityEditText.setText(String.valueOf(currentQuantity));
                }
                mQuantityEditText.setText(String.valueOf(currentQuantity));

            }
        });
        DecreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentQuantity;
                if (mQuantityEditText.getText().toString().isEmpty()) {
                    currentQuantity = 0;
                } else currentQuantity = Integer.parseInt(mQuantityEditText.getText().toString());
                if (currentQuantity != 0) {
                    currentQuantity -= 1;
                    mQuantityEditText.setText(String.valueOf(currentQuantity));
                }
                mQuantityEditText.setText(String.valueOf(currentQuantity));

            }
        });


    }


    /**
     * Get user input from editor and save Product into database.
     */
    private boolean saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space

        String ProductNameString = mProductNameEditText.getText().toString().trim();
        String ProductPriceString = mPriceEditText.getText().toString().trim();
        String ProductQuantityString = mQuantityEditText.getText().toString().trim();
        String SupplierNameString = mSupplierNameEditText.getText().toString().trim();
        String SupplierPhoneNumberString = mSupplierPhoneNumberEditText.getText().toString().trim();


        // Check if this is supposed to be a new Product
        // and check if all the fields in the editor are blank
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(ProductNameString) && TextUtils.isEmpty(ProductPriceString) &&
                TextUtils.isEmpty(ProductQuantityString) && TextUtils.isEmpty(SupplierNameString)
                && TextUtils.isEmpty(SupplierPhoneNumberString)) {
            // Since no fields were modified, we can return early without creating a new Product.
            // No need to create ContentValues and no need to do any ContentProvider operations.


            Toast.makeText(AddProduct.this, getString(R.string.UnableToSave), Toast.LENGTH_SHORT).show();

            return false;
        }

        if (ProductNameString.isEmpty()) {

            Toast.makeText(AddProduct.this, getString(R.string.UnableToSaveNoProductName), Toast.LENGTH_SHORT).show();

            return false;
        }

        if (ProductPriceString.isEmpty()) {

            Toast.makeText(AddProduct.this, getString(R.string.UnableToSaveNoProductPrice), Toast.LENGTH_SHORT).show();


            return false;
        }

        if (ProductQuantityString.isEmpty()) {

            Toast.makeText(AddProduct.this, getString(R.string.UnableToSaveNoProductQuantity), Toast.LENGTH_SHORT).show();


            return false;
        }

        if (SupplierNameString.isEmpty()) {

            Toast.makeText(AddProduct.this, getString(R.string.UnableToSaveNoSupplierName), Toast.LENGTH_SHORT).show();


            return false;
        }

        if (SupplierPhoneNumberString.isEmpty()) {

            Toast.makeText(AddProduct.this, getString(R.string.UnableToSaveNoSupplierPhoneNumber), Toast.LENGTH_SHORT).show();


            return false;
        }

        // Create a ContentValues object where column names are the keys,
        // and Product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, ProductNameString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, ProductPriceString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, ProductQuantityString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIERNAME, SupplierNameString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIERPHONENUMBER, SupplierPhoneNumberString);
        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.



        // Determine if this is a new or existing Product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            // This is a NEW Product, so insert a new Product into the provider,
            // returning the content URI for the new Product.
            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_Product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_Product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING Product, so update the Product with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_Product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_Product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

    return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new Product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save Product to database
                if (saveProduct())
                {          // Exit activity
                finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the Product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(AddProduct.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(AddProduct.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the Product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all Product attributes, define a projection that contains
        // all columns from the Product table
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIERNAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIERPHONENUMBER};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current Product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of Product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIERNAME);
            int supplierPhoneNumberColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIERPHONENUMBER);

            // Extract out the value from the Cursor for the given column index
            String ProductName = cursor.getString(nameColumnIndex);
            String ProductPrice = cursor.getString(priceColumnIndex);
            String ProductQuntity = cursor.getString(quantityColumnIndex);
            String SupplierName = cursor.getString(supplierNameColumnIndex);
            String SupplierPhoneNumber = cursor.getString(supplierPhoneNumberColumnIndex);

            // Update the views on the screen with the values from the database
            mProductNameEditText.setText(ProductName);
            mPriceEditText.setText(ProductPrice);
            mQuantityEditText.setText(ProductQuntity);
            mSupplierNameEditText.setText(SupplierName);
            mSupplierPhoneNumberEditText.setText(SupplierPhoneNumber);


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneNumberEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the Product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this Product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the Product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the Product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the Product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing Product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the Product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the Product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_Product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_Product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }


    public class MinMaxFilter implements InputFilter {

        private int mIntMin, mIntMax;

        public MinMaxFilter(int minValue, int maxValue) {
            this.mIntMin = minValue;
            this.mIntMax = maxValue;
        }

        public MinMaxFilter(String minValue, String maxValue) {
            this.mIntMin = Integer.parseInt(minValue);
            this.mIntMax = Integer.parseInt(maxValue);
        }

        @Override
        public CharSequence filter(CharSequence source, int Min, int Max, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(mIntMin, mIntMax, input))

                    return null;
            } catch (NumberFormatException nfe) {
            }
            return "";
        }

        private boolean isInRange(int start, int end, int value) {
            return end > start ? value >= start
                    && value <= end : value >= end
                    && value <= start;
        }
    }
}