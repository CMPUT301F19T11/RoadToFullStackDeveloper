package com.example.umood.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.umood.MainActivity;
import com.example.umood.Mood;
import com.example.umood.R;

import com.example.umood.User;
import com.example.umood.addMoodInfo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView;
    private static final String TAG = "qian-map";
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int PICK_MOOD_REQUEST = 1;
    private GoogleMap gMap;
    private Intent intentAdd;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docref;
    private CollectionReference collectionReference = db.collection("users");

    private MainActivity activity;
    private User user;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        activity = (MainActivity) getActivity();
        user = activity.getUser();
        docref = collectionReference.document(user.getUsername());

        Log.d(TAG, "Almost2 ");

        mapView = root.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        if(mapView==null)
            Log.d(TAG, "map view is null");

        mapView.getMapAsync(this);

        intentAdd = new Intent(getActivity(), addMoodInfo.class);

        FloatingActionButton floatingActionButton = root.findViewById(R.id.floatingActionButton);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Almost ");
                startActivityForResult(intentAdd,PICK_MOOD_REQUEST);
            }
        });
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == PICK_MOOD_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                String emotion = data.getStringExtra("Mood");
                String socialSituation = data.getStringExtra("SocialSituation");
                String reason = data.getStringExtra("Reason");
                String path = data.getStringExtra("Path");
                double longitude = data.getDoubleExtra("Longitude",0);
                double latitude = data.getDoubleExtra("Latitude",0);

                if(reason==null || reason.isEmpty())
                    reason = "";
                if(path ==null || path.isEmpty())
                    path = "";

                LatLng edmonton = new LatLng(latitude, longitude);
                BitmapDescriptor myIcon;

                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                if(emotion==null || emotion.isEmpty())
                    return;

                switch (emotion){
                    case "Happy":
                        myIcon = BitmapDescriptorFactory.fromResource(R.drawable.happy);
                        break;
                    case "Sick":
                        myIcon = BitmapDescriptorFactory.fromResource(R.drawable.sick);
                        break;
                    case "Scared":
                        myIcon = BitmapDescriptorFactory.fromResource(R.drawable.scared);
                        break;
                    default:
                        myIcon = BitmapDescriptorFactory.fromResource(R.drawable.angry);
                        break;
                }

                // New a mood event
                Mood mood = new Mood(currentDate,
                            currentTime,
                            emotion,
                            reason,
                            socialSituation,
                            latitude,
                            longitude,
                            user.getUsername(),
                            path);

                String Description = "Today: " + currentDate + "    Time: " + currentTime + socialSituation;

                if(latitude!=0) {
                    // Add a new marker to aap
                    gMap.addMarker(new MarkerOptions()
                            .position(edmonton)
                            .title(emotion)
                            .snippet(Description)
                            .icon(myIcon));
                }

                // Update the mood event to database
                docref.update("moodHistory", FieldValue.arrayUnion(mood));

                // I do not know why i put it here
                mapView.getMapAsync(this);
            }
            else
                Log.d(TAG, "Failed to get result!");
        }
        else
            Log.d(TAG, "What happened??");
    }


    @Override
    public void onMapReady(GoogleMap googleMap){
        // Need to call MapsInitializer before doing any CameraUpdateFactory calls
        gMap = googleMap;
        double latitude = 53.5232;
        double longitude = -113.5263;
        LatLng edmonton = new LatLng(latitude, longitude);
        BitmapDescriptor myIcon;

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(edmonton)           // Sets the center of the map to location user
                .zoom(12)                   // Sets the zoom
                .bearing(0)                 // Sets the orientation of the camera to east
                .build();                   // Creates a CameraPosition from the builder
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        ArrayList<Mood> moodHistory = user.getMoodHistory();
        for(Mood mood:moodHistory) {
            String emotion = mood.getEmotion();
            double latitudeMood = mood.getLatitude();
            if(latitudeMood==0)
                continue;
            double longitudeMood = mood.getLongitude();
            LatLng location = new LatLng(latitudeMood, longitudeMood);


            switch (emotion){
                case "Happy":
                    myIcon = BitmapDescriptorFactory.fromResource(R.drawable.happy);
                    break;
                case "Sick":
                    myIcon = BitmapDescriptorFactory.fromResource(R.drawable.sick);
                    break;
                case "Scared":
                    myIcon = BitmapDescriptorFactory.fromResource(R.drawable.scared);
                    break;
                default:
                    myIcon = BitmapDescriptorFactory.fromResource(R.drawable.angry);
                    break;
            }

            String Description = "Today: " + mood.getDate() + "    Time: " + mood.getTime();
            googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(emotion)
                    .snippet(Description)
                    .icon(myIcon));
        }

        // CameraPosition cameraPosition = new CameraPosition.Builder().target(edmonton).zoom(12).build();
        // gmap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    @Override
    public void onResume() {
        super.onResume();
        activity.update();
        mapView.onResume();

    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onPause() {
        super.onPause();
        activity.update();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


}

