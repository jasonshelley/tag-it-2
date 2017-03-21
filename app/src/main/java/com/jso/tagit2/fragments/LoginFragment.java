package com.jso.tagit2.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.SignInButton;
import com.jso.tagit2.IGoogleApiClient;
import com.jso.tagit2.IStateManager;
import com.jso.tagit2.R;

public class LoginFragment extends Fragment {

    IStateManager stateManager;
    IGoogleApiClient googleApiClient;

    public static LoginFragment newInstance(){
        LoginFragment fragment = new LoginFragment();

        return fragment;
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_login, container, false);

        SignInButton sib = (SignInButton)v.findViewById(R.id.sign_in_button);
        sib.setSize(SignInButton.SIZE_WIDE);
        sib.setColorScheme(SignInButton.COLOR_DARK);

        sib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleApiClient.SignIn();
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IStateManager) {
            stateManager = (IStateManager) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement IStateManager");
        }
        if (context instanceof IGoogleApiClient) {
            googleApiClient = (IGoogleApiClient) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement IGoogleApiClient");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stateManager = null;
        googleApiClient = null;
    }

}
