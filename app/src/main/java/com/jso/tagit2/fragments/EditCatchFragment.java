package com.jso.tagit2.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ListPopupWindow;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.jso.tagit2.MainActivity;
import com.jso.tagit2.R;
import com.jso.tagit2.SharedPrefsHelper;
import com.jso.tagit2.database.BaitsTable;
import com.jso.tagit2.database.CatchesTable;
import com.jso.tagit2.database.FishersTable;
import com.jso.tagit2.database.IDatabaseTable;
import com.jso.tagit2.database.SpeciesTable;
import com.jso.tagit2.models.Catch;
import com.jso.tagit2.provider.TagIt2Provider;
import com.jso.tagit2.services.LocationService;
import com.jso.tagit2.utils.AsyncGeocoder;
import com.jso.tagit2.utils.BitmapHelper;
import com.jso.tagit2.utils.ImageAsyncLoader;
import com.jso.tagit2.utils.ImageAsyncSaver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class EditCatchFragment extends Fragment {

    private static final String ARG_CATCH_ID = "catchId";

    private static final int ACTIVITY_SELECT_IMAGE = 0x01;
    private static final int ACTIVITY_TAKE_PHOTO = 0x02;

    ContentObserver observer;

    SharedPrefsHelper prefs;

    private long catchId;

    private View rootView;
    private ImageButton browseButton;
    private ImageButton takePhotoButton;
    private ImageView imageView;

    private TextView textFisher;
    private ArrayAdapter adapterFisher;
    private ListPopupWindow listPopupFisher;

    private TextView textSpecies;
    private ArrayAdapter adapterSpecies;
    private ListPopupWindow listPopupSpecies;

    private TextView textBait;
    private ArrayAdapter adapterBait;
    private ListPopupWindow listPopupBait;

    private TextView textLength;
    private TextView textWeight;
    private Dialog dialog;

    private TextView textLocation;

    private ListPopupWindow currentPopup;

    private MainActivity activity;

    public EditCatchFragment() {
        // Required empty public constructor
    }

    public static EditCatchFragment newInstance(long catchId) {
        EditCatchFragment fragment = new EditCatchFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CATCH_ID, catchId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = new SharedPrefsHelper(getContext());

        if (getArguments() != null) {
            catchId = getArguments().getLong(ARG_CATCH_ID);

            prefs.setCurrentCatchId(catchId);

            ContentResolver resolver = getActivity().getContentResolver();
            observer = new ContentObserver(new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    return false;
                }
            })) {
                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    super.onChange(selfChange, uri);
                    refreshView(rootView);
                }
            };

            resolver.registerContentObserver(prefs.getCurrentCatchUri(), true, observer);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_edit_catch, container, false);

        refreshView(rootView);

        return rootView;
    }

    private void refreshView(View v) {
        Context context = getContext();
        if (context == null)
            return;
        final ContentResolver resolver = context.getContentResolver();
        final Catch currentCatch = prefs.getCurrentCatch();

        browseButton = (ImageButton) v.findViewById(R.id.btn_browse_image);
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        takePhotoButton = (ImageButton) v.findViewById(R.id.btn_take_photo);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        // Fisher
        textFisher = (TextView) v.findViewById(R.id.text_fisher);
        if (currentCatch.fisher == null || currentCatch.fisher.isEmpty()) {
            textFisher.setText("Select a fisher...");
            textFisher.setBackgroundColor(0xFFFFB300);
        } else
            textFisher.setText(currentCatch.fisher);
            initFisherPopupWindow();

        // Species
        textSpecies = (TextView) v.findViewById(R.id.text_species);
        if (currentCatch.species == null || currentCatch.species.isEmpty()) {
            textSpecies.setText("Select a species...");
            textSpecies.setBackgroundColor(0xFFFFB300);
        } else
            textSpecies.setText(currentCatch.species);
        initSpeciesPopupWindow();

        // Bait
        textBait = (TextView) v.findViewById(R.id.text_bait);
        if (currentCatch.bait == null || currentCatch.bait.isEmpty()) {
            textBait.setText("Select a bait...");
            textBait.setBackgroundColor(0xFFFFB300);
        } else {
            textBait.setText(currentCatch.bait);
        }
        initBaitPopupWindow();

        textLength = (TextView)v.findViewById(R.id.text_length);
        textLength.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog(InputType.TYPE_CLASS_NUMBER,
                        "Update Length",
                        "Update the length of your catch for posterity...",
                        CatchesTable.TABLE_NAME,
                        CatchesTable.COL_LENGTH);
            }
        });

        if (currentCatch.length == 0) {
            textLength.setText("Add a length...");
            textLength.setBackgroundColor(0xFFFFB300);
        } else
            textLength.setText(String.format("%.0f cm", currentCatch.length));

        textWeight = (TextView)v.findViewById(R.id.text_weight);
        if (currentCatch.weight == 0) {
            textWeight.setText("Add a weight...");
            textWeight.setBackgroundColor(0xFFFFB300);
        } else
            textWeight.setText(String.format("%.1f kg", currentCatch.weight));
        textWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog(InputType.TYPE_CLASS_NUMBER,
                        "Update Length",
                        "Update the length of your catch for posterity...",
                        CatchesTable.TABLE_NAME,
                        CatchesTable.COL_LENGTH);
            }
        });

        imageView = (ImageView) v.findViewById(R.id.image_catch);

        String imagePath = currentCatch.thumbnailPath;
        if (imagePath != null) {
            ImageAsyncLoader loader = new ImageAsyncLoader(resolver, imageView);
            loader.execute(BitmapHelper.getFileProviderUri(getContext(), imagePath));
        } else
            imageView.setImageBitmap(null);

        if (currentCatch.latitude == 0) {
            LocationService.LocationServiceBinder binder = activity.getLocationServiceBinder();
            if (binder != null) {
                if (binder.hasValidLocation()) {   // if we've got a valid location, use it. Otherwise wait for a better one
                    Location location = binder.getCurrentLocation();
                    currentCatch.latitude = location.getLatitude();
                    currentCatch.longitude = location.getLongitude();

                    resolver.update(prefs.getCurrentCatchUri(), CatchesTable.getContentValues(currentCatch), null, null);

                    AsyncGeocoder ag = new AsyncGeocoder(activity);
                    ag.execute(currentCatch);
                }
            }
        }

        textLocation = (TextView)v.findViewById(R.id.text_location);
        if (currentCatch.locationDescription == null || currentCatch.locationDescription.isEmpty())
            textLocation.setText("Acquiring...");
        else
            textLocation.setText(currentCatch.locationDescription);

        ImageButton btn_add_species = (ImageButton)v.findViewById(R.id.btn_add_species);
        btn_add_species.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog(InputType.TYPE_CLASS_TEXT,
                        "Add a species",
                        "Add a species to your list so you can select it faster next time...",
                        SpeciesTable.TABLE_NAME,
                        SpeciesTable.COL_NAME);
            }
        });

        ImageButton btn_add_fisher = (ImageButton)v.findViewById(R.id.btn_add_fisher);
        btn_add_fisher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog(InputType.TYPE_CLASS_TEXT,
                        "Add a fisher",
                        "Add a fisher to your list so you can select them faster next time...",
                        FishersTable.TABLE_NAME,
                        FishersTable.COL_NAME);
            }
        });
        ImageButton btn_add_bait = (ImageButton)v.findViewById(R.id.btn_add_bait);
        btn_add_bait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog(InputType.TYPE_CLASS_TEXT,
                        "Add a bait",
                        "Add a bait to your list so you can select it faster next time...",
                        BaitsTable.TABLE_NAME,
                        BaitsTable.COL_NAME);
            }
        });
    }

    private void initFisherPopupWindow() {
        ContentResolver resolver = getContext().getContentResolver();
        
        Cursor cursor = resolver.query(TagIt2Provider.Contract.FISHERS_URI,
                TagIt2Provider.Contract.FISHER_PROJECTION,
                null, null,
                FishersTable.COL_NAME + " ASC");
        ArrayList<CharSequence> array = new ArrayList<>();
        while (cursor.moveToNext())
            array.add(cursor.getString(cursor.getColumnIndex(FishersTable.COL_NAME)));
        adapterFisher = new ArrayAdapter<CharSequence>(getContext(), R.layout.single_line, array);

        listPopupFisher = getListPopup(textFisher, adapterFisher, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fisher = (String)adapterFisher.getItem(position);
                saveParam(CatchesTable.COL_FISHER, fisher);
                listPopupFisher.dismiss();
            }
        });
    }

    private void initSpeciesPopupWindow() {
        ContentResolver resolver = getContext().getContentResolver();

        Cursor cursor = resolver.query(TagIt2Provider.Contract.SPECIES_URI,
                TagIt2Provider.Contract.SPECIES_PROJECTION,
                null, null,
                SpeciesTable.COL_NAME + " ASC");
        ArrayList<CharSequence> array = new ArrayList<>();
        while (cursor.moveToNext())
            array.add(cursor.getString(cursor.getColumnIndex(SpeciesTable.COL_NAME)));
        adapterSpecies = new ArrayAdapter<CharSequence>(getContext(), R.layout.single_line, array);

        listPopupSpecies = getListPopup(textSpecies, adapterSpecies, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String species = (String)adapterSpecies.getItem(position);
                saveParam(CatchesTable.COL_SPECIES, species);
                listPopupSpecies.dismiss();
            }
        });
    }

    private void initBaitPopupWindow() {
        ContentResolver resolver = getContext().getContentResolver();

        Cursor cursor = resolver.query(TagIt2Provider.Contract.BAITS_URI,
                TagIt2Provider.Contract.BAIT_PROJECTION,
                null, null,
                BaitsTable.COL_NAME + " ASC");
        ArrayList<CharSequence> array = new ArrayList<>();
        while (cursor.moveToNext())
            array.add(cursor.getString(cursor.getColumnIndex(BaitsTable.COL_NAME)));
        adapterBait = new ArrayAdapter<CharSequence>(getContext(), R.layout.single_line, array);

        listPopupBait = getListPopup(textBait, adapterBait, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String bait = (String)adapterBait.getItem(position);
                saveParam(CatchesTable.COL_BAIT, bait);
                listPopupBait.dismiss();
            }
        });
    }


    private void showEditDialog(final int inputType, String title, String msg, final String tableName, final String column) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle(title);
        alert.setMessage(msg);

        // Set an EditText view to get user input
        final EditText input = new EditText(getContext());
        input.setInputType(inputType);
        input.requestFocus();
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                int nvalue = Integer.parseInt(value);
                ContentValues values = new ContentValues();
                values.put(IDatabaseTable.COL_LAST_MODIFIED, System.currentTimeMillis());
                values.put(IDatabaseTable.COL_IS_SYNCED, 0);
                if (inputType == InputType.TYPE_CLASS_NUMBER)
                    values.put(column, nvalue);
                else
                    values.put(column, value);

                ContentResolver resolver = getActivity().getContentResolver();
                if (tableName == FishersTable.TABLE_NAME) {
                    resolver.insert(TagIt2Provider.Contract.FISHERS_URI, values);
                    initFisherPopupWindow();
                } else if (tableName == BaitsTable.TABLE_NAME) {
                    resolver.insert(TagIt2Provider.Contract.BAITS_URI, values);
                    initBaitPopupWindow();
                } else if (tableName == SpeciesTable.TABLE_NAME) {
                    resolver.insert(TagIt2Provider.Contract.SPECIES_URI, values);
                    initSpeciesPopupWindow();
                } else if (tableName == CatchesTable.TABLE_NAME) {
                    resolver.update(Uri.withAppendedPath(TagIt2Provider.Contract.CATCHES_URI, String.valueOf(catchId)),
                            values, null, null);
                } else if (tableName == CatchesTable.TABLE_NAME) {
                    resolver.update(Uri.withAppendedPath(TagIt2Provider.Contract.CATCHES_URI, String.valueOf(catchId)),
                            values, null, null);
                }
            }
        });

        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

        Dialog d = alert.create();

        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        d.show();
    }

    NumberPicker.Formatter lengthFormatter = new NumberPicker.Formatter() {
        @Override
        public String format(int value) {
            return String.format("%d cm", value);
        }
    };

    NumberPicker.Formatter weightFormatter = new NumberPicker.Formatter() {
        @Override
        public String format(int value) {
            return String.format("%.1f kg", value / 10.0);
        }
    };

    private void showNumericPopup(String label, final INumberPicked numberPicked, int min, int max, int value, NumberPicker.Formatter formatter) {
        dialog = new Dialog(getContext());
        dialog.setTitle(label);
        dialog.setContentView(R.layout.numeric_popup);

        final INumberPicked iNumberPicked = numberPicked;

        final NumberPicker np = (NumberPicker)dialog.findViewById(R.id.numberPicker);
        np.setFormatter(formatter);
        np.setMinValue(min);
        np.setMaxValue(max);
        np.setValue(value);

        TextView cancel = (TextView)dialog.findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        TextView ok = (TextView)dialog.findViewById(R.id.btn_ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newValue = np.getValue();
                dialog.dismiss();

                iNumberPicked.onNumberPicked(newValue);
            }
        });

        dialog.show();
    }

    public interface INumberPicked {
        public void onNumberPicked(int val);
    }

    private void saveParam(String key, String value) {
        ContentResolver resolver = getContext().getContentResolver();

        ContentValues values = new ContentValues();
        values.put(key, value);

        Uri uri = Uri.withAppendedPath(TagIt2Provider.Contract.CATCHES_URI, String.valueOf(catchId));
        resolver.update(uri, values, null, null);
    }

    private void saveParam(String key, double value) {
        ContentResolver resolver = getContext().getContentResolver();

        ContentValues values = new ContentValues();
        values.put(key, value);

        Uri uri = Uri.withAppendedPath(TagIt2Provider.Contract.CATCHES_URI, String.valueOf(catchId));
        resolver.update(uri, values, null, null);
    }

    private ListPopupWindow getListPopup(TextView parent, ArrayAdapter adapter, AdapterView.OnItemClickListener listener) {
        final ListPopupWindow listPopupWindow = new ListPopupWindow(getContext());
        listPopupWindow.setAdapter(adapter);
        listPopupWindow.setAnchorView(parent);
//        listPopupWindow.setWidth(300);
//        listPopupWindow.setHeight(400);

        listPopupWindow.setModal(true);
        listPopupWindow.setOnItemClickListener(listener);
        listPopupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.editbox_dropdown_dark_frame, null));
        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPopup = listPopupWindow;
                listPopupWindow.show();
            }
        });

        return listPopupWindow;
    }
    private long getId(Uri uri, String[] projection, String name) {
        ContentResolver resolver = getContext().getContentResolver();
        Cursor c = resolver.query(uri, projection, BaitsTable.COL_NAME + "=?", new String [] { name }, null);
        c.moveToFirst();
        long id = c.getLong(c.getColumnIndex(IDatabaseTable.COL_ID));
        c.close();

        return id;
    }

    private View getEmptyView(String text) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        View v = inflater.inflate(R.layout.single_line, null);

        TextView textView = (TextView)v.findViewById(R.id.text);

        textView.setText(text);

        return v;
    }



    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), ACTIVITY_SELECT_IMAGE);
    }

    private File createImageFile() throws IOException {
        Context context = getContext();

        String imageFileName = BitmapHelper.getImageName();

        File file = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = new File(file.getPath(), imageFileName);
        image.createNewFile();

        return image;
    }

    private void takePhoto () {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File newPhoto = createImageFile();
            Uri photoURI = BitmapHelper.getFileProviderUri(getContext(), newPhoto);
            new SharedPrefsHelper(getContext()).setNewPhotoUri(photoURI);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, ACTIVITY_TAKE_PHOTO);
        } catch (IOException ex) {
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED)
            return;

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ACTIVITY_SELECT_IMAGE) {
                Uri uri = data.getData();
                new ImageAsyncSaver(getContext(), catchId, uri, false).execute();
            } else if (requestCode == ACTIVITY_TAKE_PHOTO) {
                Uri uri = new SharedPrefsHelper(getContext()).getNewPhotoUri();
                if (uri != null) {
                    new ImageAsyncSaver(getContext(), catchId, uri, true).execute();
                }
            }
        }
    }


    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (context instanceof MainActivity) {
            activity = (MainActivity)context;
        } else {
            throw new RuntimeException("context must be an instance of MainActivity");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
