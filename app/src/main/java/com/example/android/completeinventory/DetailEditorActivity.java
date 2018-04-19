package com.example.android.completeinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.completeinventory.data.InventoryContract.InventoryEntry;

import static java.lang.Integer.parseInt;

//Allows user to create a new product or edit an existing one.
public class DetailEditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    //Identifier for the product data loader
    private static final int EXISTING_PRODUCT_LOADER = 0;

    private static final int IMAGE_GALLERY_REQUEST = 0;

    private Uri mCurrentProductUri;
    private EditText mProductNameEditText;
    private EditText mTotalQuantityEditText;
    private EditText mPriceEditText;
    private EditText mSupplierNameEdit;
    private Button mIncreaseQuantity;
    private Button mDecreaseQuantity;
    private Button mOrderButton;
    private Button mBrowseImage;
    private int quantity;
    private ImageView mImageView;
    private static Uri mImageUri;


    //Boolean flag that keeps track of whether the product has been edited (true) or not (false)
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
        setContentView(R.layout.activity_detail_editor);


        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product
        if (mCurrentProductUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.editor_activity_title_new_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {


            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mProductNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mTotalQuantityEditText = (EditText) findViewById(R.id.edit_total_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mSupplierNameEdit = (EditText) findViewById(R.id.edit_supplier_name);
        mDecreaseQuantity = (Button) findViewById(R.id.decrease_quantity);
        mIncreaseQuantity = (Button) findViewById(R.id.increase_quantity);
        mOrderButton = (Button) findViewById(R.id.order_btn);
        mBrowseImage = (Button) findViewById(R.id.browse_image_btn);
        mImageView = (ImageView) findViewById(R.id.product_image);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mProductNameEditText.setOnTouchListener(mTouchListener);
        mTotalQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEdit.setOnTouchListener(mTouchListener);

        mIncreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseQuantity();
            }
        });

        mDecreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseQuantity();

            }
        });

        //Order intent
        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                quantity = Integer.parseInt(mTotalQuantityEditText.getText().toString());
                String product = mProductNameEditText.getText().toString();

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Order request for " + product);
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Order request for " + product + '\n' + "Quantity needed: " + quantity);

                if (emailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(emailIntent);
                } else {
                    Toast.makeText(DetailEditorActivity.this,
                            "Order product detail incomplete", Toast.LENGTH_LONG).show();
                }

            }
        });

        mBrowseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // if we are here, everything processed successfully.
            if (requestCode == IMAGE_GALLERY_REQUEST) {
                // if we are here, we can access the image gallery.

                //the address of the image on the SD Card.
                mImageUri = data.getData();

                mImageView.setImageURI(mImageUri);
            }
        }
    }

    private void decreaseQuantity() {
        //Before trying to parse the quantity, you must check if there is any in the edit text if not, the app crashes.
        if (!TextUtils.isEmpty(mTotalQuantityEditText.getText())) {
            //get the value from editext and convert it to integer
            quantity = Integer.parseInt(mTotalQuantityEditText.getText().toString());

            if (quantity > 0) {
                quantity--;

                //parse from integer to String to populate the edittext
                mTotalQuantityEditText.setText(String.valueOf(quantity));
            }
        }
    }

    private void increaseQuantity() {
        //Before trying to parse the quantity, you must check if there is any in the edit text if not, the app crashes.
        if (!TextUtils.isEmpty(mTotalQuantityEditText.getText())) {
            quantity = Integer.parseInt(mTotalQuantityEditText.getText().toString());
            quantity++;

            mTotalQuantityEditText.setText(String.valueOf(quantity));
        }
    }

    //GET USER INPUT FROM EDITOR AND SAVE PRODUCT INTO DATABASE
    public static void saveProduct(Context context, String productString, String quantityString, String priceString,
                                   String supplierString, String imageString, Uri mCurrentProductUri) {
        // Content values is a class that stores a bunch of key value pairs where the key is the
        // column name in the db and the are the values is whatever value you want to insert in
        ContentValues values = new ContentValues();

        // check if all the fields in the editor are blank, if yes then exit the editor
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(productString) && TextUtils.isEmpty(supplierString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(imageString)) {

            Toast.makeText(context, R.string.no_data_no_save,
                    Toast.LENGTH_SHORT).show();

            return;
        }

        // check if some fields in the editor are blank, if yes then inform the user
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(productString) || TextUtils.isEmpty(supplierString) ||
                TextUtils.isEmpty(priceString) || TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(imageString)) {

            Toast.makeText(context, R.string.insert_required_data,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if ((!TextUtils.isEmpty(productString)) && (!TextUtils.isEmpty(supplierString)) && (!TextUtils.isEmpty(priceString))
                && (!TextUtils.isEmpty(quantityString)) && (!TextUtils.isEmpty(imageString))) {

            values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productString);
            values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, supplierString);
            values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, priceString);
            values.put(InventoryEntry.COLUMN_QUANTITY, quantityString);
            values.put(InventoryEntry.COLUMN_IMAGE, imageString);
        }

        // If the quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = parseInt(quantityString);
        }
        values.put(InventoryEntry.COLUMN_QUANTITY, quantity);


        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = context.getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(context, context.getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(context, context.getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = context.getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(context, context.getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {

                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(context, context.getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();

            }
        }

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
        // If this is a new product, hide the "Delete" menu item.
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
                // Save product to database
                String product = mProductNameEditText.getText().toString().trim();
                String quantity = mTotalQuantityEditText.getText().toString().trim();
                String price = mPriceEditText.getText().toString().trim();
                String supplier = mSupplierNameEdit.getText().toString().trim();
                String image = null;
                if (mImageUri != null) {
                    image = mImageUri.toString();
                }


                saveProduct(this, product, quantity, price, supplier, image, mCurrentProductUri);
                // Exit activity
                finish();
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailEditorActivity.this);
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
                                NavUtils.navigateUpFromSameTask(DetailEditorActivity.this);
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
        // If the product hasn't changed, continue with handling back button press
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
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_IMAGE
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,     // Query the content URI for the current product
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
            // Find the columns of products attributes that we're interested in
            int productColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String product = cursor.getString(productColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String quantity = cursor.getString(quantityColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            // Update the views with the values from the database
            mProductNameEditText.setText(product);
            mSupplierNameEdit.setText(supplier);
            mTotalQuantityEditText.setText(String.valueOf(quantity));
            mPriceEditText.setText(String.valueOf(price));
            if (image != null) {
                mImageView.setImageURI(Uri.parse(image));

            }
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductNameEditText.setText("");
        mSupplierNameEdit.setText("");
        mTotalQuantityEditText.setText("");
        mPriceEditText.setText("");
        mImageView.setImageDrawable(null);
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
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
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
     * Prompt the user to confirm that they want to delete this product
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
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
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}